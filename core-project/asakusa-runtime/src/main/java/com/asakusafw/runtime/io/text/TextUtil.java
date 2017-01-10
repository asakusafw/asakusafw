/**
 * Copyright 2011-2017 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.runtime.io.text;

import java.math.BigDecimal;
import java.nio.CharBuffer;

/**
 * Utilities about text.
 * @since 0.9.1
 */
public final class TextUtil {

    private static final ThreadLocal<char[]> CHAR_ARRAY_BUFFERS = ThreadLocal.withInitial(() -> new char[512]);

    private static final int CHAR_ARRAY_MARGIN = 16;

    private static final char[] ASCII_SPECIAL_ESCAPE = new char[128];
    static {
        ASCII_SPECIAL_ESCAPE['\b'] = 'b';
        ASCII_SPECIAL_ESCAPE['\t'] = 't';
        ASCII_SPECIAL_ESCAPE['\n'] = 'n';
        ASCII_SPECIAL_ESCAPE['\f'] = 'f';
        ASCII_SPECIAL_ESCAPE['\r'] = 'r';
        ASCII_SPECIAL_ESCAPE['\\'] = '\\';
        ASCII_SPECIAL_ESCAPE['\"'] = '\"';
    }

    private TextUtil() {
        return;
    }

    /**
     * Quotes the given string as Java string literal style, and returns it.
     * @param cs the source string
     * @return the quoted string
     */
    public static String quote(CharSequence cs) {
        StringBuilder buf = new StringBuilder();
        quoteTo(cs, buf);
        return buf.toString();
    }

    /**
     * Quotes the given string as Java string literal style, and puts it into the given buffer.
     * @param cs the source string
     * @param target the destination buffer
     */
    public static void quoteTo(CharSequence cs, StringBuilder target) {
        target.append('"');
        for (int i = 0, n = cs.length(); i < n; i++) {
            char c = cs.charAt(i);
            if (c <= 0x7f && ASCII_SPECIAL_ESCAPE[c] != 0) {
                target.append('\\');
                target.append(ASCII_SPECIAL_ESCAPE[c]);
            } else if (Character.isISOControl(c)
                    || Character.isDefined(c) == false
                    || Character.isLowSurrogate(c)) {
                target.append(String.format("\\u%04x", (int) c)); //$NON-NLS-1$
            } else if (Character.isHighSurrogate(c)) {
                if (i + 1 < n) {
                    char next = cs.charAt(i + 1);
                    if (Character.isLowSurrogate(next)) {
                        target.append(c);
                        target.append(next);
                        i++;
                        continue;
                    }
                }
                target.append(String.format("\\u%04x", (int) c)); //$NON-NLS-1$
            } else {
                target.append(c);
            }
        }
        target.append('"');
    }

    /**
     * Returns the number of leading whitespace characters in the given character sequence range.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the number of leading white space characters
     */
    public static int countLeadingWhitespaces(CharSequence cs, int offset, int length) {
        int count = 0;
        for (int i = offset, n = offset + length; i < n; i++) {
            char c = cs.charAt(i);
            if (Character.isWhitespace(c)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Returns the number of trailing whitespace characters in the given character sequence range.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the number of trailing white space characters
     */
    public static int countTrailingWhitespaces(CharSequence cs, int offset, int length) {
        int count = 0;
        for (int i = offset + length - 1; i >= offset; i--) {
            char c = cs.charAt(i);
            if (Character.isWhitespace(c)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Parses a character sequence which may represent a {@code byte} value.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the character sequence is wrong
     */
    public static byte parseByte(CharSequence cs, int offset, int length) {
        int value = parseInt(cs, offset, length);
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw invalidNumber(cs, offset, length);
        }
        return (byte) value;
    }

    /**
     * Parses a character sequence which may represent a {@code short} value.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the character sequence is wrong
     */
    public static short parseShort(CharSequence cs, int offset, int length) {
        int value = parseInt(cs, offset, length);
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw invalidNumber(cs, offset, length);
        }
        return (short) value;
    }

    /**
     * Parses a character sequence which may represent a {@code int} value.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the character sequence is wrong
     */
    public static int parseInt(CharSequence cs, int offset, int length) {
        if (length == 0) {
            throw invalidNumber(cs, offset, length);
        }
        int start = offset;
        boolean negative = false;
        char first = cs.charAt(start);
        if (first < '0') {
            if (first == '+') {
                start++;
            } else if (first == '-') {
                start++;
                negative = true;
            }
        }
        int negativeResult = 0;
        for (int i = start, n = offset + length; i < n; i++) {
            char c = cs.charAt(i);
            int column = Character.digit(c, 10);
            if (column < 0) {
                throw invalidNumber(cs, offset, length);
            }
            // check overflow
            if (negativeResult < (Integer.MIN_VALUE / 10)) {
                throw invalidNumber(cs, offset, length);
            }
            negativeResult *= 10;
            // check overflow
            if (negativeResult < (Integer.MIN_VALUE | column)) {
                throw invalidNumber(cs, offset, length);
            }
            negativeResult -= column;
        }
        if (negative) {
            return negativeResult;
        } else {
            if (negativeResult == Integer.MIN_VALUE) {
                throw invalidNumber(cs, offset, length);
            }
            return -negativeResult;
        }
    }

    /**
     * Parses a character sequence which may represent a {@code long} value.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the character sequence is wrong
     */
    public static long parseLong(CharSequence cs, int offset, int length) {
        if (length == 0) {
            throw invalidNumber(cs, offset, length);
        }
        int start = offset;
        boolean negative = false;
        char first = cs.charAt(start);
        if (first < '0') {
            if (first == '+') {
                start++;
            } else if (first == '-') {
                start++;
                negative = true;
            }
        }
        long negativeResult = 0;
        for (int i = start, n = offset + length; i < n; i++) {
            char c = cs.charAt(i);
            int column = Character.digit(c, 10);
            if (column < 0) {
                throw invalidNumber(cs, offset, length);
            }
            // check overflow
            if (negativeResult < (Long.MIN_VALUE / 10)) {
                throw invalidNumber(cs, offset, length);
            }
            negativeResult *= 10;
            // check overflow
            if (negativeResult < (Long.MIN_VALUE | column)) {
                throw invalidNumber(cs, offset, length);
            }
            negativeResult -= column;
        }
        if (negative) {
            return negativeResult;
        } else {
            if (negativeResult == Long.MIN_VALUE) {
                throw invalidNumber(cs, offset, length);
            }
            return -negativeResult;
        }
    }

    /**
     * Parses a character sequence which may represent a decimal value.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the character sequence is wrong
     */
    public static BigDecimal parseDecimal(CharSequence cs, int offset, int length) {
        if (cs instanceof CharBuffer && ((CharBuffer) cs).hasArray()) {
            CharBuffer cb = (CharBuffer) cs;
            char[] array = cb.array();
            int offsetInArray = offset + cb.position() + cb.arrayOffset();
            return new BigDecimal(array, offsetInArray, length);
        } else {
            char[] cbuf = borrowCharArrayBuf(length);
            if (cs instanceof String) {
                ((String) cs).getChars(offset, offset + length, cbuf, 0);
            } else if (cs instanceof StringBuilder) {
                ((StringBuilder) cs).getChars(offset, offset + length, cbuf, 0);
            } else {
                for (int i = 0; i < length; i++) {
                    cbuf[i] = cs.charAt(offset + i);
                }
            }
            return new BigDecimal(cbuf, 0, length);
        }
    }

    /**
     * Parses a character sequence which may represent a {@code float} value.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the character sequence is wrong
     */
    public static float parseFloat(CharSequence cs, int offset, int length) {
        return Float.parseFloat(toString(cs, offset, length));
    }

    /**
     * Parses a character sequence which may represent a {@code double} value.
     * @param cs the target character sequence
     * @param offset the character (the number of 16-bit {@code char}) offset in the target sequence
     * @param length the character (the number of 16-bit {@code char}) length
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the character sequence is wrong
     */
    public static double parseDouble(CharSequence cs, int offset, int length) {
        return Double.parseDouble(toString(cs, offset, length));
    }

    private static String toString(CharSequence cs, int offset, int length) {
        if (offset == 0 && length == cs.length()) {
            return cs.toString();
        }
        return cs.subSequence(offset, offset + length).toString();
    }

    private static NumberFormatException invalidNumber(CharSequence cs, int offset, int length) {
        return new NumberFormatException(quote(cs.subSequence(offset, offset + length)));
    }

    private static char[] borrowCharArrayBuf(int length) {
        char[] cs = CHAR_ARRAY_BUFFERS.get();
        if (cs.length < length) {
            cs = new char[length + CHAR_ARRAY_MARGIN];
            CHAR_ARRAY_BUFFERS.set(cs);
        }
        return cs;
    }
}
