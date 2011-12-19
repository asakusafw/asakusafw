/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.io.csv.CsvFormatException.Reason;
import com.asakusafw.runtime.io.csv.CsvFormatException.Status;
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

/**
 * A simple CSV parser.
 * @since 0.2.4
 */
public class CsvParser implements RecordParser {

    static final Log LOG = LogFactory.getLog(CsvParser.class);

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

    private final String trueFormat;

    private final SimpleDateFormat dateFormat;

    private final SimpleDateFormat dateTimeFormat;

    private final List<String> headerCellsFormat;

    private boolean firstLine = true;

    private IntBuffer cellBeginPositions = IntBuffer.allocate(256);

    private final CharBuffer readerBuffer = CharBuffer.allocate(INPUT_BUFFER_SIZE);

    private CharBuffer lineBuffer = CharBuffer.allocate(INPUT_BUFFER_SIZE);

    private int currentRecordNumber = 0;

    private int currentPhysicalLine = 1;

    private int currentPhysicalHeadLine = 1;

    private CsvFormatException.Status exceptionStatus = null;

    private final Calendar calendarBuffer = Calendar.getInstance();

    private final Text textBuffer = new Text();

    private final ParsePosition parsePositionBuffer = new ParsePosition(0);

    /**
     * Creates a new instance.
     * @param reader the source stream
     * @param path the source path
     * @param config current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvParser(Reader reader, String path, CsvConfiguration config) {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null"); //$NON-NLS-1$
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        this.reader = reader;
        this.path = path;
        this.trueFormat = config.getTrueFormat();
        this.dateFormat = new SimpleDateFormat(config.getDateFormat());
        this.dateTimeFormat = new SimpleDateFormat(config.getDateTimeFormat());
        this.headerCellsFormat = config.getHeaderCells();

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
            case STATE_CELL_HEAD:
                switch (c) {
                case '"':
                    state = STATE_QUOTED;
                    break;
                case ',':
                    state = STATE_CELL_HEAD;
                    addSeparator();
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
                    if (state == STATE_CELL_HEAD) {
                        addSeparator();
                    }
                    state = STATE_FINAL;
                    break;
                default:
                    state = STATE_CELL_BODY;
                    emit(c);
                }
                break;
            case STATE_CELL_BODY:
                switch (c) {
                case '"': // illegal character
                    // state = STATE_CELL_BODY;
                    emit(c);
                    break;
                case ',':
                    state = STATE_CELL_HEAD;
                    addSeparator();
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
                    state = STATE_CELL_BODY;
                    emit(c);
                }
                break;
            case STATE_QUOTED:
                switch (c) {
                case '"':
                    state = STATE_NEST_QUOTE;
                    break;
                case '\r':
                    state = STATE_QUOTED_SAW_CR;
                    emit(c);
                    break;
                case '\n':
                    // state = STATE_QUOTED;
                    currentPhysicalLine++;
                    emit(c);
                    break;
                case EOF: // invalid state
                    state = STATE_FINAL;
                    addSeparator();
                    exceptionStatus = createStatusInDecode(Reason.UNEXPECTED_EOF, "\"", "End of File");
                    break;
                case ',':
                default:
                    // state = STATE_QUOTED;
                    emit(c);
                }
                break;
            case STATE_NEST_QUOTE:
                switch (c) {
                case '"':
                    state = STATE_QUOTED;
                    emit(c);
                    break;
                case ',':
                    state = STATE_CELL_HEAD;
                    addSeparator();
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
                    state = STATE_CELL_BODY;
                    warn(createStatusInDecode(Reason.CHARACTER_AFTER_QUOTE, "cell separator", String.valueOf(c)));
                    emit(c);
                }
                break;
            case STATE_SAW_CR:
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
                break;
            case STATE_QUOTED_SAW_CR:
                currentPhysicalLine++;
                switch (c) {
                case '"':
                    state = STATE_NEST_QUOTE;
                    break;
                case '\r':
                    // state = STATE_QUOTED_SAW_CR;
                    emit(c);
                    break;
                case '\n':
                    state = STATE_QUOTED;
                    emit(c);
                    break;
                case EOF: // invalid state
                    state = STATE_FINAL;
                    addSeparator();
                    exceptionStatus = createStatusInDecode(Reason.UNEXPECTED_EOF, "\"", "End of File");
                    break;
                case ',':
                default:
                    state = STATE_QUOTED;
                    emit(c);
                }
                break;
            default:
                throw new AssertionError(state);
            }
        }
        lineBuffer.flip();
        cellBeginPositions.flip();
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
            lineBuffer = buf = newBuf;
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
            cellBeginPositions = buf = newBuf;
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
            option.modify(toBooleanValue());
        } else {
            option.setNull();
        }
    }

    private boolean toBooleanValue() {
        return trueFormat.contentEquals(lineBuffer);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(ByteOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toByteValue());
        } else {
            option.setNull();
        }
    }

    private byte toByteValue() throws CsvFormatException {
        try {
            return Byte.parseByte(lineBuffer.toString());
        } catch (NumberFormatException e) {
            throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "byte value"), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(ShortOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toShortValue());
        } else {
            option.setNull();
        }
    }

    private short toShortValue() throws CsvFormatException {
        try {
            return Short.parseShort(lineBuffer.toString());
        } catch (NumberFormatException e) {
            throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "short value"), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(IntOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toIntValue());
        } else {
            option.setNull();
        }
    }

    private int toIntValue() throws CsvFormatException {
        try {
            return Integer.parseInt(lineBuffer.toString());
        } catch (NumberFormatException e) {
            throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "int value"), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(LongOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toLongValue());
        } else {
            option.setNull();
        }
    }

    private long toLongValue() throws CsvFormatException {
        try {
            return Long.parseLong(lineBuffer.toString());
        } catch (NumberFormatException e) {
            throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "long value"), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(FloatOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toFloatValue());
        } else {
            option.setNull();
        }
    }

    private float toFloatValue() throws CsvFormatException {
        try {
            return Float.parseFloat(lineBuffer.toString());
        } catch (NumberFormatException e) {
            throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "float value"), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(DoubleOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toDoubleValue());
        } else {
            option.setNull();
        }
    }

    private double toDoubleValue() throws CsvFormatException {
        try {
            return Double.parseDouble(lineBuffer.toString());
        } catch (NumberFormatException e) {
            throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "double value"), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(DecimalOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toDecimalValue());
        } else {
            option.setNull();
        }
    }

    private BigDecimal toDecimalValue() throws CsvFormatException {
        try {
            return new BigDecimal(lineBuffer.toString());
        } catch (NumberFormatException e) {
            throw new CsvFormatException(createStatusInLine(Reason.INVALID_CELL_FORMAT, "decimal value"), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(StringOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toTextValue());
        } else {
            option.setNull();
        }
    }

    private Text toTextValue() {
        textBuffer.set(lineBuffer.toString());
        return textBuffer;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(DateOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toDateValue());
        } else {
            option.setNull();
        }
    }

    private int toDateValue() throws CsvFormatException {
        parsePositionBuffer.setIndex(0);
        parsePositionBuffer.setErrorIndex(-1);
        java.util.Date parsed = dateFormat.parse(lineBuffer.toString(), parsePositionBuffer);
        if (parsePositionBuffer.getIndex() == 0) {
            throw new CsvFormatException(
                    createStatusInLine(Reason.INVALID_CELL_FORMAT, dateFormat.toPattern()),
                    null);
        }
        calendarBuffer.setTime(parsed);
        return DateUtil.getDayFromCalendar(calendarBuffer);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fill(DateTimeOption option) throws CsvFormatException, IOException {
        seekBuffer();
        if (lineBuffer.hasRemaining()) {
            option.modify(toDateTimeValue());
        } else {
            option.setNull();
        }
    }

    private long toDateTimeValue() throws CsvFormatException {
        parsePositionBuffer.setIndex(0);
        parsePositionBuffer.setErrorIndex(-1);
        java.util.Date parsed = dateTimeFormat.parse(lineBuffer.toString(), parsePositionBuffer);
        if (parsePositionBuffer.getIndex() == 0) {
            throw new CsvFormatException(
                    createStatusInLine(Reason.INVALID_CELL_FORMAT, dateFormat.toPattern()),
                    null);
        }
        calendarBuffer.setTime(parsed);
        return DateUtil.getSecondFromCalendar(calendarBuffer);
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
                    "End of Line",
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

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
