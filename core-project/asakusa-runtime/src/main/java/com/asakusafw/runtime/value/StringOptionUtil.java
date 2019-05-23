/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.value;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.apache.hadoop.io.InputBuffer;
import org.apache.hadoop.io.Text;

/**
 * Utilities for {@link StringOption}.
 * @since 0.8.0
 * @version 0.10.3
 */
public final class StringOptionUtil {

    /**
     * The internal text encoding.
     */
    public static final Charset ENCODING = StandardCharsets.UTF_8;

    static final ThreadLocal<DecoderBuffer> DECODER_POOL = ThreadLocal.withInitial(DecoderBuffer::new);

    private static final ThreadLocal<char[]> CHAR_ARRAY_BUFFERS = ThreadLocal.withInitial(() -> new char[512]);

    private static final int CHAR_ARRAY_PADDING = 16;

    private StringOptionUtil() {
        return;
    }

    /**
     * Returns the number of code-points in the given {@link StringOption}.
     * If the object does represent neither {@code null} nor a valid character string, this operation may raise an
     * error or return a wrong count.
     * @param option the target object
     * @return the number of code-points in this object, or {@code 0} if the object represents {@code null}
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     * @since 0.9.1
     */
    public static int countCodePoints(StringOption option) {
        Text text = option.get();
        byte[] bytes = text.getBytes();
        int len = text.getLength();
        int index = 0;
        int count = 0;
        while (index < len) {
            byte b = bytes[index];
            if ((b & 0b1000_0000) == 0) {
                index += 1;
            } else if ((b & 0b1110_0000) == 0b1100_0000) {
                index += 2;
            } else if ((b & 0b1111_0000) == 0b1110_0000) {
                index += 3;
            } else if ((b & 0b1111_1000) == 0b1111_0000) {
                index += 4;
            } else if ((b & 0b1111_1100) == 0b1111_1000) {
                index += 5;
            } else if ((b & 0b1111_1110) == 0b1111_1100) {
                index += 6;
            } else {
                break;
            }
            count++;
        }
        if (index != len) {
            throw new IllegalStateException(option.toString());
        }
        return count;
    }

    /**
     * Returns a {@link Reader} to read the text contents in the {@link StringOption}.
     * @param option the target {@link StringOption}
     * @return the created reader
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static Reader asReader(StringOption option) {
        Text text = option.get();
        InputBuffer buffer = new InputBuffer();
        buffer.reset(text.getBytes(), 0, text.getLength());
        return new InputStreamReader(buffer, ENCODING);
    }

    /**
     * Trims the leading/trailing classical whitespace characters in the {@link StringOption}.
     * This only removes the following characters:
     * <ul>
     * <li> {@code "\t" (HT:U+0009)} </li>
     * <li> {@code "\n" (LF:U+000a)} </li>
     * <li> {@code "\r" (CR:U+000d)} </li>
     * <li> {@code " " (SP:U+0020)} </li>
     * </ul>
     * This directly modifies the target {@link StringOption}.
     * @param option the target {@link StringOption}
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static void trim(StringOption option) {
        Text text = option.get();
        byte[] bytes = text.getBytes();
        int length = text.getLength();
        int start = 0;
        int last = length - 1;
        for (; start <= last; start++) {
            if (isTrimTarget(bytes[start]) == false) {
                break;
            }
        }
        for (; last >= start; last--) {
            if (isTrimTarget(bytes[last]) == false) {
                break;
            }
        }
        if (start == 0 && last == length - 1) {
            return;
        }
        text.set(bytes, start, last + 1 - start);
    }

    private static boolean isTrimTarget(byte b) {
        switch (b) {
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            return true;
        default:
            return false;
        }
    }

    /**
     * Appends the text in the second {@link StringOption} into the first one.
     * This directly modifies the first {@link StringOption}.
     * @param target the append target
     * @param contents the text contents to be appended
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static void append(StringOption target, StringOption contents) {
        Text text = contents.get();
        append(target, text);
    }

    /**
     * Appends the text in the second {@link StringOption} into the first one.
     * This directly modifies the first {@link StringOption}.
     * @param target the append target
     * @param contents the text contents to be appended
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static void append(StringOption target, String contents) {
        Text buffer = StringOption.BUFFER_POOL.get();
        buffer.set(contents);
        append(target, buffer);
    }

    private static void append(StringOption target, Text text) {
        target.get().append(text.getBytes(), 0, text.getLength());
    }

    /**
     * Appends the text in the given {@link StringOption} into the {@link StringBuilder}.
     * @param target the append target
     * @param contents the text contents to be appended
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     * @since 0.9.1
     */
    public static void append(StringBuilder target, StringOption contents) {
        CharBuffer buffer = DECODER_POOL.get().decode(contents.get());
        target.append(buffer);
    }

    /**
     * Parses the given {@link StringOption} which may represent a {@code int} value.
     * @param contents the text contents
     * @return the parsed value
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     * @throws IllegalArgumentException if the character sequence is wrong
     * @since 0.9.1
     */
    public static int parseInt(StringOption contents) {
        CharBuffer buffer = DECODER_POOL.get().decode(contents.get());
        if (buffer.hasRemaining() == false) {
            throw invalidNumber(contents);
        }
        boolean negative = false;
        char first = buffer.get(0);
        if (first < '0') {
            if (first == '+') {
                buffer.get();
            } else if (first == '-') {
                buffer.get();
                negative = true;
            }
        }
        int negativeResult = 0;
        while (buffer.hasRemaining()) {
            char c = buffer.get();
            int column = Character.digit(c, 10);
            if (column < 0) {
                throw invalidNumber(contents);
            }
            // check overflow
            if (negativeResult < (Integer.MIN_VALUE / 10)) {
                throw invalidNumber(contents);
            }
            negativeResult *= 10;
            // check overflow
            if (negativeResult < (Integer.MIN_VALUE | column)) {
                throw invalidNumber(contents);
            }
            negativeResult -= column;
        }
        if (negative) {
            return negativeResult;
        } else {
            if (negativeResult == Integer.MIN_VALUE) {
                throw invalidNumber(contents);
            }
            return -negativeResult;
        }
    }

    /**
     * Parses the given {@link StringOption} which may represent a {@code long} value.
     * @param contents the text contents
     * @return the parsed value
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     * @throws IllegalArgumentException if the character sequence is wrong
     * @since 0.9.1
     */
    public static long parseLong(StringOption contents) {
        CharBuffer buffer = DECODER_POOL.get().decode(contents.get());
        if (buffer.hasRemaining() == false) {
            throw invalidNumber(contents);
        }
        boolean negative = false;
        char first = buffer.get(0);
        if (first < '0') {
            if (first == '+') {
                buffer.get();
            } else if (first == '-') {
                buffer.get();
                negative = true;
            }
        }
        long negativeResult = 0;
        while (buffer.hasRemaining()) {
            char c = buffer.get();
            int column = Character.digit(c, 10);
            if (column < 0) {
                throw invalidNumber(contents);
            }
            // check overflow
            if (negativeResult < (Long.MIN_VALUE / 10)) {
                throw invalidNumber(contents);
            }
            negativeResult *= 10;
            // check overflow
            if (negativeResult < (Long.MIN_VALUE | column)) {
                throw invalidNumber(contents);
            }
            negativeResult -= column;
        }
        if (negative) {
            return negativeResult;
        } else {
            if (negativeResult == Long.MIN_VALUE) {
                throw invalidNumber(contents);
            }
            return -negativeResult;
        }
    }

    /**
     * Parses the given {@link StringOption} which may represent a decimal value.
     * @param contents the text contents
     * @return the parsed decimal value
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     * @throws IllegalArgumentException if the character sequence is wrong
     * @since 0.9.1
     */
    public static BigDecimal parseDecimal(StringOption contents) {
        CharBuffer buffer = DECODER_POOL.get().decode(contents.get());
        if (buffer.hasRemaining() == false) {
            throw invalidNumber(contents);
        }
        int length = buffer.remaining();
        if (buffer.hasArray()) {
            char[] array = buffer.array();
            int offsetInArray = buffer.position() + buffer.arrayOffset();
            return new BigDecimal(array, offsetInArray, length);
        } else {
            char[] cbuf = borrowCharArrayBuf(length);
            buffer.get(cbuf, 0, length);
            return new BigDecimal(cbuf, 0, length);
        }
    }

    private static NumberFormatException invalidNumber(StringOption contents) {
        return new NumberFormatException(contents.toString());
    }

    private static char[] borrowCharArrayBuf(int length) {
        char[] cs = CHAR_ARRAY_BUFFERS.get();
        if (cs.length < length) {
            cs = new char[Math.max(length, cs.length + CHAR_ARRAY_PADDING)];
            CHAR_ARRAY_BUFFERS.set(cs);
        }
        return cs;
    }

    /**
     * Appends source contents into the destination string.
     * @param source the source buffer
     * @param destination the target StringOption
     * @param encoder the encoder
     * @param buffer the working buffer
     * @throws CharacterCodingException if error occurred while encoding source input
     * @since 0.10.3
     */
    public static void append(
            CharBuffer source, StringOption destination,
            CharsetEncoder encoder, ByteBuffer buffer) throws CharacterCodingException {
        if (source.hasRemaining() == false) {
            return;
        }
        Text text = destination.get();
        encoder.reset();
        while (source.hasRemaining()) {
            buffer.clear();
            CoderResult r1 = encoder.encode(source, buffer, false);
            if (r1.isError()) {
                r1.throwException();
            }
            append0(buffer, text);
            if (r1.isUnderflow()) {
                buffer.clear();
                CoderResult r2 = encoder.encode(source, buffer, true);
                if (r2.isError()) {
                    r2.throwException();
                }
                append0(buffer, text);
                break;
            }
        }
        buffer.clear();
        CoderResult r3 = encoder.flush(buffer);
        if (r3.isError()) {
            r3.throwException();
        }
        append0(buffer, text);
    }

    private static void append0(ByteBuffer buffer, Text text) {
        buffer.flip();
        if (buffer.hasRemaining()) {
            text.append(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
        }
    }

    private static final class DecoderBuffer {

        private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        private CharBuffer charBuffer;

        DecoderBuffer() {
            return;
        }

        CharBuffer decode(Text text) {
            if (charBuffer == null || charBuffer.capacity() < text.getLength() * 2) {
                int newCapacity = Math.max(1024, (int) (text.getLength() * 2.5));
                charBuffer = CharBuffer.allocate(newCapacity);
            }
            charBuffer.clear();
            ByteBuffer bytes = ByteBuffer.wrap(text.getBytes(), 0, text.getLength());
            CoderResult result = decoder.decode(bytes, charBuffer, true);
            if (result.isOverflow() || result.isError()) {
                try {
                    result.throwException();
                } catch (CharacterCodingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            charBuffer.flip();
            return charBuffer;
        }
    }
}
