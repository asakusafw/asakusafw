/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.io.csv.CsvFormatException.Reason;
import com.asakusafw.runtime.io.csv.CsvFormatException.Status;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * A simple CSV parser.
 * @since 0.2.4
 * @version 0.7.3
 */
public class CsvParser implements RecordParser {

    static final Log LOG = LogFactory.getLog(CsvParser.class);

    private static final String CHAR_END_OF_CELL = "cell separator";

    private static final String CHAR_END_OF_RECORD = "End of Line";

    private static final String CHAR_END_OF_FILE = "End of File";

    private static final String CHAR_LF = "LF (0x0a)"; //$NON-NLS-1$

    private static final String CHAR_DOUBLE_QUOTE = "\""; //$NON-NLS-1$

    private static final int BUFFER_LIMIT = 10 * 1024 * 1024;

    private static final int INPUT_BUFFER_SIZE = 4096;

    private static final int EOF = -1;

    private static final int STATE_LINE_HEAD = 0;

    private static final int STATE_CELL_HEAD = STATE_LINE_HEAD + 1;

    private static final int STATE_CELL_BODY = STATE_CELL_HEAD + 1;

    private static final int STATE_QUOTED = STATE_CELL_BODY + 1;

    private static final int STATE_NEST_QUOTE = STATE_QUOTED + 1;

    private static final int STATE_SAW_CR = STATE_NEST_QUOTE + 1;

    private static final int STATE_QUOTED_SAW_CR = STATE_SAW_CR + 1;

    private static final int STATE_INIT = STATE_LINE_HEAD;

    private static final int STATE_FINAL = -1;

    private final Reader reader;

    private final String path;

    private final char separator;

    private final String trueFormat;

    private final DateFormatter dateFormat;

    private final DateTimeFormatter dateTimeFormat;

    private final boolean forceConsumeHeader;

    private final List<String> headerCellsFormat;

    private final boolean allowLineBreakInValue;

    private boolean firstLine = true;

    private IntBuffer cellBeginPositions = IntBuffer.allocate(256);

    private final CharBuffer readerBuffer = CharBuffer.allocate(INPUT_BUFFER_SIZE);

    private CharBuffer lineBuffer = CharBuffer.allocate(INPUT_BUFFER_SIZE);

    private int currentRecordNumber = 0;

    private int currentPhysicalLine = 1;

    private int currentPhysicalHeadLine = 1;

    private CsvFormatException.Status exceptionStatus = null;

    /**
     * Creates a new instance.
     * @param stream the source stream
     * @param path the source path
     * @param config current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvParser(InputStream stream, String path, CsvConfiguration config) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        this.reader = new InputStreamReader(stream, config.getCharset());
        this.path = path;
        this.separator = config.getSeparatorChar();
        this.trueFormat = config.getTrueFormat();
        this.dateFormat = DateFormatter.newInstance(config.getDateFormat());
        this.dateTimeFormat = DateTimeFormatter.newInstance(config.getDateTimeFormat());
        this.headerCellsFormat = config.getHeaderCells();
        this.forceConsumeHeader = config.isForceConsumeHeader();
        this.allowLineBreakInValue = config.isLineBreakInValue();

        readerBuffer.clear();
        readerBuffer.flip();
    }

    private void decodeLine() throws IOException {
        currentPhysicalHeadLine = currentPhysicalLine;
        lineBuffer.clear();
        cellBeginPositions.clear();
        int state = STATE_INIT;
        addSeparator();
        while (state != STATE_FINAL) {
            int c = getNextCharacter();
            switch (state) {
            case STATE_LINE_HEAD:
                state = onLineHead(c);
                break;
            case STATE_CELL_HEAD:
                state = onCellHead(c);
                break;
            case STATE_CELL_BODY:
                state = onCellBody(c);
                break;
            case STATE_QUOTED:
                state = onQuoted(c);
                break;
            case STATE_NEST_QUOTE:
                state = onNestQuote(c);
                break;
            case STATE_SAW_CR:
                state = onSawCr(c);
                break;
            case STATE_QUOTED_SAW_CR:
                state = onQuotedSawCr(c);
                break;
            default:
                throw new AssertionError(state);
            }
        }
        lineBuffer.flip();
        cellBeginPositions.flip();
    }

    private int onLineHead(int c) throws IOException {
        int state;
        switch (c) {
        case '"':
            state = STATE_QUOTED;
            break;
        case '\r':
            state = STATE_SAW_CR;
            break;
        case '\n':
            state = STATE_FINAL;
            addSeparator();
            currentPhysicalLine++;
            break;
        case EOF:
            state = STATE_FINAL;
            break;
        default:
            if (c == separator) {
                state = STATE_CELL_HEAD;
                addSeparator();
            } else {
                state = STATE_CELL_BODY;
                emit(c);
            }
            break;
        }
        return state;
    }

    private int onCellHead(int c) throws IOException {
        int state;
        switch (c) {
        case '"':
            state = STATE_QUOTED;
            break;
        case '\r':
            state = STATE_SAW_CR;
            break;
        case '\n':
            state = STATE_FINAL;
            addSeparator();
            currentPhysicalLine++;
            break;
        case EOF:
            state = STATE_FINAL;
            addSeparator();
            break;
        default:
            if (c == separator) {
                state = STATE_CELL_HEAD;
                addSeparator();
            } else {
                state = STATE_CELL_BODY;
                emit(c);
            }
            break;
        }
        return state;
    }

    private int onCellBody(int c) throws IOException {
        int state;
        switch (c) {
        case '"': // illegal character
            state = STATE_CELL_BODY;
            emit(c);
            break;
        case '\r':
            state = STATE_SAW_CR;
            break;
        case '\n':
            state = STATE_FINAL;
            addSeparator();
            currentPhysicalLine++;
            break;
        case EOF:
            state = STATE_FINAL;
            addSeparator();
            break;
        default:
            if (c == separator) {
                state = STATE_CELL_HEAD;
                addSeparator();
            } else {
                state = STATE_CELL_BODY;
                emit(c);
            }
            break;
        }
        return state;
    }

    private int onQuoted(int c) throws IOException {
        int state;
        switch (c) {
        case '"':
            state = STATE_NEST_QUOTE;
            break;
        case '\r':
            state = STATE_QUOTED_SAW_CR;
            emit(c);
            break;
        case '\n':
            state = STATE_QUOTED;
            if (allowLineBreakInValue == false) {
                exceptionStatus = createStatusInDecode(Reason.UNEXPECTED_LINE_BREAK, CHAR_DOUBLE_QUOTE, CHAR_LF);
            }
            currentPhysicalLine++;
            emit(c);
            break;
        case EOF: // invalid state
            state = STATE_FINAL;
            addSeparator();
            exceptionStatus = createStatusInDecode(Reason.UNEXPECTED_EOF, CHAR_DOUBLE_QUOTE, CHAR_END_OF_FILE);
            break;
        default:
            state = STATE_QUOTED;
            emit(c);
        }
        return state;
    }

    private int onNestQuote(int c) throws IOException {
        int state;
        switch (c) {
        case '"':
            state = STATE_QUOTED;
            emit(c);
            break;
        case '\r':
            state = STATE_SAW_CR;
            break;
        case '\n':
            state = STATE_FINAL;
            addSeparator();
            currentPhysicalLine++;
            break;
        case EOF:
            state = STATE_FINAL;
            addSeparator();
            break;
        default:
            if (c == separator) {
                state = STATE_CELL_HEAD;
                addSeparator();
            } else {
                state = STATE_CELL_BODY;
                warn(createStatusInDecode(Reason.CHARACTER_AFTER_QUOTE, CHAR_END_OF_CELL, String.valueOf(c)));
                emit(c);
            }
            break;
        }
        return state;
    }

    private int onSawCr(int c) {
        int state;
        currentPhysicalLine++;
        switch (c) {
        case '\n':
            state = STATE_FINAL;
            addSeparator();
            break;
        case EOF:
            state = STATE_FINAL;
            addSeparator();
            break;
        default:
            state = STATE_FINAL;
            addSeparator();
            rewindCharacter();
        }
        return state;
    }

    private int onQuotedSawCr(int c) throws IOException {
        int state;
        currentPhysicalLine++;
        switch (c) {
        case '"':
            state = STATE_NEST_QUOTE;
            break;
        case '\r':
            state = STATE_QUOTED_SAW_CR;
            emit(c);
            break;
        case '\n':
            state = STATE_QUOTED;
            if (allowLineBreakInValue == false) {
                exceptionStatus = createStatusInDecode(Reason.UNEXPECTED_LINE_BREAK, CHAR_DOUBLE_QUOTE, CHAR_LF);
            }
            emit(c);
            break;
        case EOF: // invalid state
            state = STATE_FINAL;
            addSeparator();
            exceptionStatus = createStatusInDecode(Reason.UNEXPECTED_EOF, CHAR_DOUBLE_QUOTE, CHAR_END_OF_FILE);
            break;
        default:
            state = STATE_QUOTED;
            emit(c);
        }
        return state;
    }

    private void warn(Status status) {
        assert status != null;
        LOG.warn(status.toString());
    }

    private int getNextCharacter() throws IOException {
        CharBuffer buf = readerBuffer;
        if (buf.remaining() == 0) {
            buf.clear();
            int read = reader.read(buf);
            buf.flip();
            assert read != 0;
            if (read < 0) {
                return EOF;
            }
        }
        return buf.get();
    }

    private void rewindCharacter() {
        CharBuffer buf = readerBuffer;
        assert buf.position() > 0;
        buf.position(buf.position() - 1);
    }

    private void emit(int c) throws IOException {
        assert c >= 0;
        CharBuffer buf = lineBuffer;
        if (buf.remaining() == 0) {
            if (buf.capacity() == BUFFER_LIMIT) {
                throw new IOException(MessageFormat.format(
                        "Line is too large (near {0}:{1}, size={2}, record-number={3})",
                        path,
                        currentPhysicalHeadLine,
                        BUFFER_LIMIT,
                        currentRecordNumber));
            }
            CharBuffer newBuf = CharBuffer.allocate(Math.min(buf.capacity() * 2, BUFFER_LIMIT));
            newBuf.clear();
            buf.flip();
            newBuf.put(buf);
            buf = newBuf;
            lineBuffer = newBuf;
        }
        buf.put((char) c);
    }

    private void addSeparator() {
        IntBuffer buf = cellBeginPositions;
        if (buf.remaining() == 0) {
            IntBuffer newBuf = IntBuffer.allocate(buf.capacity() * 2);
            newBuf.clear();
            buf.flip();
            newBuf.put(buf);
            buf = newBuf;
            cellBeginPositions = newBuf;
        }
        buf.put(lineBuffer.position());
    }

    private Status createStatusInDecode(Reason reason, String expected, String actual) {
        assert reason != null;
        return new Status(
                reason,
                path,
                currentPhysicalLine,
                currentRecordNumber,
                cellBeginPositions.limit(),
                expected,
                actual);
    }

    @Override
    public boolean next() throws CsvFormatException, IOException {
        exceptionStatus = null;
        currentRecordNumber++;
        if (firstLine) {
            firstLine = false;
            decodeLine();
            if (isEof()) {
                return false;
            }
            if (isHeader()) {
                decodeLine();
            }
        } else {
            decodeLine();
        }
        if (exceptionStatus != null) {
            throw new CsvFormatException(exceptionStatus, null);
        }
        return isEof() == false;
    }

    /**
     * Returns the parsing target path.
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the 1-origin line number where the current record is started.
     * Lines are delimited with {@code CR}, {@code LF}, and {@code CRLF}.
     * @return the current line number
     */
    public int getCurrentLineNumber() {
        return currentPhysicalHeadLine;
    }

    /**
     * Returns the 1-origin record number.
     * @return the current record number.
     */
    public int getCurrentRecordNumber() {
        return currentRecordNumber;
    }

    private boolean isEof() {
        return cellBeginPositions.limit() < 2;
    }

    private boolean isHeader() {
        if (forceConsumeHeader) {
            return true;
        }
        if (headerCellsFormat.isEmpty()) {
            return false;
        }
        if (headerCellsFormat.size() != cellBeginPositions.remaining() - 1) {
            return false;
        }
        for (int i = 0, n = headerCellsFormat.size(); i < n; i++) {
            String fieldName = headerCellsFormat.get(i);
            CharSequence fieldValue = lineBuffer.subSequence(
                    cellBeginPositions.get(i),
                    cellBeginPositions.get(i + 1));
            if (fieldName.contentEquals(fieldValue) == false) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(BooleanOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            boolean value = trueFormat.contentEquals(lineBuffer);
            option.modify(value);
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(ByteOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(ByteOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            try {
                byte value = Byte.parseByte(lineBuffer.toString());
                option.modify(value);
            } catch (NumberFormatException e) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "byte value"), e);
            }
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(ShortOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(ShortOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            try {
                short value = Short.parseShort(lineBuffer.toString());
                option.modify(value);
            } catch (NumberFormatException e) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "short value"), e);
            }
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(IntOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(IntOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            try {
                int value = Integer.parseInt(lineBuffer.toString());
                option.modify(value);
            } catch (NumberFormatException e) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "int value"), e);
            }
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(LongOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(LongOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            try {
                long value = Long.parseLong(lineBuffer.toString());
                option.modify(value);
            } catch (NumberFormatException e) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "long value"), e);
            }
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(FloatOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(FloatOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            try {
                float value = Float.parseFloat(lineBuffer.toString());
                option.modify(value);
            } catch (NumberFormatException e) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "float value"), e);
            }
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(DoubleOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(DoubleOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            try {
                double value = Double.parseDouble(lineBuffer.toString());
                option.modify(value);
            } catch (NumberFormatException e) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "double value"), e);
            }
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(DecimalOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(DecimalOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            try {
                BigDecimal value = toBigDecimal();
                option.modify(value);
            } catch (NumberFormatException e) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "decimal value"), e);
            }
        } else {
            option.setNull();
        }
    }

    private BigDecimal toBigDecimal() {
        if (lineBuffer.hasArray()) {
            char[] array = lineBuffer.array();
            int offset = lineBuffer.arrayOffset() + lineBuffer.position();
            int length = lineBuffer.remaining();
            return new BigDecimal(array, offset, length);
        } else {
            return new BigDecimal(lineBuffer.toString());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(StringOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            String value = lineBuffer.toString();
            option.modify(value);
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(DateOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(DateOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            int value = dateFormat.parse(lineBuffer);
            if (value < 0) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(
                        createStatusInLine(Reason.INVALID_CELL_FORMAT, dateFormat.getPattern()),
                        null);
            }
            option.modify(value);
        } else {
            option.setNull();
        }
    }

    @Override
    public void fill(DateTimeOption option) throws CsvFormatException, IOException {
        seekBuffer();
        fill0(option, true);
    }

    @SuppressWarnings("deprecation")
    private void fill0(DateTimeOption option, boolean doRecover) throws CsvFormatException {
        if (lineBuffer.hasRemaining()) {
            long value = dateTimeFormat.parse(lineBuffer);
            if (value < 0) {
                if (doRecover && trimWhitespaces()) {
                    fill0(option, false);
                    return;
                }
                throw new CsvFormatException(
                        createStatusInLine(Reason.INVALID_CELL_FORMAT, dateTimeFormat.getPattern()),
                        null);
            }
            option.modify(value);
        } else {
            option.setNull();
        }
    }

    private Status createStatusInLine(Reason reason, String expected) {
        return new Status(
                reason,
                path,
                currentPhysicalHeadLine,
                currentRecordNumber,
                cellBeginPositions.position(),
                expected,
                lineBuffer.toString());
    }

    @Override
    public void endRecord() throws CsvFormatException, IOException {
        if (cellBeginPositions.remaining() > 1) {
            seekBuffer();
            throw new CsvFormatException(new Status(
                    Reason.TOO_LONG_RECORD,
                    path,
                    currentPhysicalHeadLine,
                    currentRecordNumber,
                    cellBeginPositions.position(),
                    CHAR_END_OF_RECORD,
                    lineBuffer.toString()), null);
        }
    }

    private void seekBuffer() throws CsvFormatException {
        if (cellBeginPositions.remaining() < 2) {
            throw new CsvFormatException(new Status(
                    Reason.TOO_SHORT_RECORD,
                    path,
                    currentPhysicalHeadLine,
                    currentRecordNumber,
                    cellBeginPositions.position() + 1,
                    "more cells",
                    "no more cells"), null);
        }
        lineBuffer.limit(cellBeginPositions.get(cellBeginPositions.position() + 1));
        lineBuffer.position(cellBeginPositions.get());
    }

    private boolean trimWhitespaces() {
        boolean trim = false;
        for (int i = lineBuffer.position(), n = lineBuffer.limit(); i < n; i++) {
            char c = lineBuffer.get(i);
            if (Character.isWhitespace(c)) {
                trim = true;
                lineBuffer.position(i + 1);
            } else {
                break;
            }
        }
        for (int i = lineBuffer.limit() - 1, n = lineBuffer.position(); i >= n; i--) {
            char c = lineBuffer.get(i);
            if (Character.isWhitespace(c)) {
                trim = true;
                lineBuffer.limit(i);
            } else {
                break;
            }
        }
        return trim;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
