/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.tsv;

import static com.asakusafw.runtime.io.tsv.TsvConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.runtime.io.RecordFormatException;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.StringOptionUtil;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Reads TSV format text and set each cell into {@link ValueOption}.
 * Each method in this class may raise {@link NullPointerException} if parameters were {@code null}.
 */
@SuppressWarnings("deprecation")
public final class TsvParser implements RecordParser {

    private static final Pattern SPECIAL_FLOAT =
            Pattern.compile("(\\+?Inf.*)|(-Inf.*)|((\\+|-)?[Nn]a[Nn])"); //$NON-NLS-1$

    private static final int SPECIAL_FLOAT_POSITIVE_INF = 1;

    private static final int SPECIAL_FLOAT_NEGATIVE_INF = 2;

    private static final Charset TEXT_ENCODE = StandardCharsets.UTF_8;

    private static final int INITIAL_BUFFER_SIZE = 2048;

    private final Reader reader;

    private final CharsetEncoder encoder;

    private int lastSeparator;

    private int lookAhead;

    private char[] charBuffer;

    private CharBuffer wrappedCharBuffer;

    private final ByteBuffer encodeBuffer;

    /**
     * Creates a new instance.
     * @param reader the source reader
     * @throws IOException if failed to initialize the parser
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public TsvParser(Reader reader) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null"); //$NON-NLS-1$
        }
        if (reader instanceof BufferedReader) {
            this.reader = reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
        this.encoder = TEXT_ENCODE
            .newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        this.charBuffer = new char[INITIAL_BUFFER_SIZE];
        this.lastSeparator = RECORD_SEPARATOR;
        this.encodeBuffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
        fillLookAhead();
    }

    /**
     * Consumes a character and put it into the look-ahead buffer.
     * In the current implementation, the look-ahead buffer has always the head of the stream
     * after each method invocation.
     * @throws IOException if failed to read the next character
     */
    private void fillLookAhead() throws IOException {
        this.lookAhead = reader.read();
    }

    // MEMO: keep "throws IOException" for forward compatibility

    @Override
    public boolean next() throws RecordFormatException, IOException {
        lastSeparator = CELL_SEPARATOR;
        return lookAhead != -1;
    }

    /**
     * Validates the current reading position is the head of a cell.
     * @throws RecordFormatException if violated
     */
    private void checkCellStart() throws RecordFormatException {
        if (lastSeparator != CELL_SEPARATOR || lookAhead == -1) {
            throw new RecordFormatException("Next cell is not started");
        }
    }

    @Override
    public void fill(BooleanOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);

        if (lookAhead == BOOLEAN_TRUE) {
            option.modify(true);
        } else if (lookAhead == BOOLEAN_FALSE) {
            option.modify(false);
        } else {
            throw new RecordFormatException(MessageFormat.format(
                    "Invalid character {0} for boolean",
                    (char) lookAhead));
        }
        int next = reader.read();
        if (isSeparator(next) == false) {
            throw new RecordFormatException(MessageFormat.format(
                    "Invalid character {0} for boolean",
                    (char) next));
        }
        setLastSeparator(next);
        fillLookAhead();
    }

    @Override
    public void fill(ByteOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        option.modify((byte) readInt(option));
        fillLookAhead();
    }

    @Override
    public void fill(ShortOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        option.modify((short) readInt(option));
        fillLookAhead();
    }

    @Override
    public void fill(IntOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        int value = readInt(option);
        option.modify(value);
        fillLookAhead();
    }

    @Override
    public void fill(LongOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        boolean negative = false;
        if (lookAhead == '-') {
            lookAhead = reader.read();
            negative = true;
        }
        assertHasRest(option, lookAhead);
        long value = toNumber(lookAhead);
        while (true) {
            int c = reader.read();
            if (isSeparator(c)) {
                setLastSeparator(c);
                break;
            }
            value = value * 10L + toNumber(c);
        }
        if (negative) {
            value = -value;
        }
        option.modify(value);
        fillLookAhead();
    }

    @Override
    public void fill(FloatOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);
        charBuffer[0] = (char) lookAhead;
        int length = readString(1, option);
        String string = new String(charBuffer, 0, length + 1);
        try {
            option.modify(Float.parseFloat(string));
        } catch (NumberFormatException e) {
            Matcher matcher = SPECIAL_FLOAT.matcher(string);
            if (matcher.matches()) {
                if (matcher.group(SPECIAL_FLOAT_POSITIVE_INF) != null) {
                    option.modify(Float.POSITIVE_INFINITY);
                } else if (matcher.group(SPECIAL_FLOAT_NEGATIVE_INF) != null) {
                    option.modify(Float.NEGATIVE_INFINITY);
                } else {
                    option.modify(Float.NaN);
                }
            } else {
                throw new RecordFormatException(MessageFormat.format(
                        "Invalid character in floating-point context {0}",
                        string), e);
            }
        }
        fillLookAhead();
    }

    @Override
    public void fill(DoubleOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);
        charBuffer[0] = (char) lookAhead;
        int length = readString(1, option);
        String string = new String(charBuffer, 0, length + 1);
        try {
            option.modify(Double.parseDouble(string));
        } catch (NumberFormatException e) {
            Matcher matcher = SPECIAL_FLOAT.matcher(string);
            if (matcher.matches()) {
                if (matcher.group(SPECIAL_FLOAT_POSITIVE_INF) != null) {
                    option.modify(Double.POSITIVE_INFINITY);
                } else if (matcher.group(SPECIAL_FLOAT_NEGATIVE_INF) != null) {
                    option.modify(Double.NEGATIVE_INFINITY);
                } else {
                    option.modify(Double.NaN);
                }
            } else {
                throw new RecordFormatException(MessageFormat.format(
                        "Invalid character in floating-point context {0}",
                        string), e);
            }
        }
        fillLookAhead();
    }

    @Override
    public void fill(DecimalOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);
        charBuffer[0] = (char) lookAhead;
        int length = readString(1, option);
        option.modify(new BigDecimal(charBuffer, 0, length + 1));
        fillLookAhead();
    }

    @Override
    public void fill(StringOption option) throws RecordFormatException, IOException {
        checkCellStart();

        // initializes charBuffer for writing
        if (wrappedCharBuffer == null) {
            wrappedCharBuffer = CharBuffer.wrap(charBuffer);
        } else {
            wrappedCharBuffer.clear();
        }

        // resets Text for writing
        option.reset();

        if (lookAhead == ESCAPE_CHAR) {
            int c = reader.read();
            if (c == ESCAPE_NULL_COLUMN) {
                option.setNull();
                int next = reader.read();
                if (isSeparator(next) == false) {
                    throw new RecordFormatException(MessageFormat.format(
                            "Missing separator for {0}",
                            option.getClass().getSimpleName()));
                }
                setLastSeparator(next);
                fillLookAhead();
                return;
            }
            wrappedCharBuffer.append(unescape(c));
        } else if (isSeparator(lookAhead)) {
            setLastSeparator(lookAhead);
            fillLookAhead();
            return;
        } else {
            wrappedCharBuffer.append((char) lookAhead);
        }

        while (true) {
            int c = reader.read();
            if (isSeparator(c)) {
                setLastSeparator(c);
                break;
            } else if (c == ESCAPE_CHAR) {
                int trailing = reader.read();
                wrappedCharBuffer.append(unescape(trailing));
            } else {
                wrappedCharBuffer.append((char) c);
            }

            // flush buffer before limit
            if (wrappedCharBuffer.position() == wrappedCharBuffer.limit()) {
                wrappedCharBuffer.flip();
                append(wrappedCharBuffer, option);
                wrappedCharBuffer.clear();
            }
        }
        // flush rest contents in buffer
        wrappedCharBuffer.flip();
        append(wrappedCharBuffer, option);
        wrappedCharBuffer.clear();

        fillLookAhead();
    }

    @Override
    public void fill(DateOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        int year = toNumber(lookAhead) * 1000 + readNumbers(YEAR_FIELD_LENGTH - 1, option);
        consume(DATE_FIELD_SEPARATOR);
        int month = readNumbers(MONTH_FIELD_LENGTH, option);
        consume(DATE_FIELD_SEPARATOR);
        int day = readNumbers(DATE_FIELD_LENGTH, option);

        int last = reader.read();
        if (isSeparator(last) == false) {
            throw new RecordFormatException(MessageFormat.format(
                    "Missing separator for {0}",
                    option.getClass().getSimpleName()));
        }
        setLastSeparator(last);
        if (year == 0 || month == 0 || day == 0) {
            option.setNull();
        } else {
            option.modify(DateUtil.getDayFromDate(year, month, day));
        }
        fillLookAhead();
    }

    @Override
    public void fill(DateTimeOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        int year = toNumber(lookAhead) * 1000 + readNumbers(YEAR_FIELD_LENGTH - 1, option);
        consume(DATE_FIELD_SEPARATOR);
        int month = readNumbers(MONTH_FIELD_LENGTH, option);
        consume(DATE_FIELD_SEPARATOR);
        int day = readNumbers(DATE_FIELD_LENGTH, option);
        consume(DATE_TIME_SEPARATOR);
        int hour = readNumbers(HOUR_FIELD_LENGTH, option);
        consume(TIME_FIELD_SEPARATOR);
        int minute = readNumbers(MINUTE_FIELD_LENGTH, option);
        consume(TIME_FIELD_SEPARATOR);
        int second = readNumbers(SECOND_FIELD_LENGTH, option);

        int last = reader.read();
        if (isSeparator(last) == false) {
            throw new RecordFormatException(MessageFormat.format(
                    "Missing separator for {0}",
                    option.getClass().getSimpleName()));
        }
        setLastSeparator(last);
        if (year == 0 || month == 0 || day == 0) {
            option.setNull();
        } else {
            long result = DateUtil.getDayFromDate(year, month, day);
            result *= 24L * 60L * 60L;
            result += DateUtil.getSecondFromTime(hour, minute, second);
            option.modify(result);
        }
        fillLookAhead();
    }

    private int readNumbers(int columns, ValueOption<?> option) throws IOException {
        int total = 0;
        for (int i = 0; i < columns; i++) {
            int c = reader.read();
            total = total * 10 + toNumber(c);
        }
        return total;
    }

    private void consume(char expect) throws IOException {
        int c = reader.read();
        if (c != expect) {
            throw new RecordFormatException(MessageFormat.format(
                    "Invalid character in expected ''{0}'' but was \"{1}\"",
                    expect,
                    String.format("\\u%04x", c))); //$NON-NLS-1$
        }
    }

    private int toNumber(int c) throws RecordFormatException {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        throw new RecordFormatException(MessageFormat.format(
                "Invalid character in number context {0}",
                String.format("\\u%04x", c))); //$NON-NLS-1$
    }

    private void append(CharBuffer source, StringOption target) throws RecordFormatException {
        try {
            StringOptionUtil.append(source, target, encoder, encodeBuffer);
        } catch (CharacterCodingException e) {
            throw new RecordFormatException(MessageFormat.format(
                    "Cannot process a character string (\"{0}\")",
                    source), e);
        }
    }

    private char unescape(int c) throws RecordFormatException {
        if (c == ESCAPE_CHAR) {
            return ESCAPE_CHAR;
        }
        if (c == ESCAPE_HT) {
            return '\t';
        }
        if (c == ESCAPE_LF) {
            return '\n';
        }
        throw new RecordFormatException(MessageFormat.format(
                "Unknown escape character \\{0} ({1}) for StringOption",
                (char) c,
                String.format("U%04x", c))); //$NON-NLS-1$
    }

    /**
     * Consumes the next integer.
     * @param option the target object (this method never changes it)
     * @return the next integer
     * @throws RecordFormatException if the format is wrong
     * @throws IOException if failed by I/O error
     */
    private int readInt(ValueOption<?> option) throws IOException, RecordFormatException {
        boolean negative = false;
        if (lookAhead == '-') {
            lookAhead = reader.read();
            negative = true;
        }
        assertHasRest(option, lookAhead);
        int value = toNumber(lookAhead);
        while (true) {
            int c = reader.read();
            if (isSeparator(c)) {
                setLastSeparator(c);
                break;
            }
            value = value * 10 + toNumber(c);
        }
        if (negative) {
            value = -value;
        }
        return value;
    }

    private void setLastSeparator(int c) {
        lastSeparator = c;
    }

    /**
     * Consumes the next string and put it into {@link #charBuffer}.
     * @param start the starting offset index of {@link #charBuffer}
     * @param option the target object (this method never changes it)
     * @return the number of characters to be consumed
     * @throws IOException if failed by I/O error
     */
    private int readString(int start, ValueOption<?> option) throws IOException {
        int current = start;
        while (true) {
            char[] cbuf = charBuffer;
            for (int i = current, n = cbuf.length; i < n; i++) {
                int c = reader.read();
                if (isSeparator(c)) {
                    setLastSeparator(c);
                    return i - start;
                }
                cbuf[i] = (char) c;
            }
            current = cbuf.length;
            expandCharBuffer();
        }
    }

    private void expandCharBuffer() {
        char[] newBuffer = new char[charBuffer.length * 2];
        System.arraycopy(charBuffer, 0, newBuffer, 0, charBuffer.length);
        charBuffer = newBuffer;
        wrappedCharBuffer = null;
    }

    private static boolean isSeparator(int c) {
        return c == -1
                || c == CELL_SEPARATOR
                || c == RECORD_SEPARATOR;
    }

    private void assertHasRest(ValueOption<?> option, int c) throws RecordFormatException {
        if (isSeparator(c)) {
            throw new RecordFormatException(MessageFormat.format(
                    "Empty value for {0}",
                    option.getClass().getSimpleName()));
        }
    }

    /**
     * Consumes {@code null} to the target object only if the next cell actually represents {@code null}.
     * @param option the target object
     * @return {@code true} if actually consumed the next cell, or otherwise {@code false}
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if failed by I/O error
     */
    private boolean applyNull(ValueOption<?> option) throws RecordFormatException, IOException {
        if (lookAhead != ESCAPE_CHAR) {
            return false;
        }
        int c = reader.read();
        if (c == ESCAPE_NULL_COLUMN) {
            option.setNull();
            int next = reader.read();
            if (isSeparator(next) == false) {
                throw new RecordFormatException(MessageFormat.format(
                        "Missing separator for {0}",
                        option.getClass().getSimpleName()));
            }
            setLastSeparator(next);
            fillLookAhead();
            return true;
        } else {
            throw new RecordFormatException(MessageFormat.format(
                    "Cannot recognize \"{1}\" for {0}",
                    option.getClass().getSimpleName(),
                    new StringBuilder().append(ESCAPE_CHAR).append(ESCAPE_NULL_COLUMN)));
        }
    }

    @Override
    public void endRecord() throws RecordFormatException, IOException {
        if (lastSeparator != RECORD_SEPARATOR) {
            throw new RecordFormatException("RECORD_SEPARATOR does not appeared");
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
