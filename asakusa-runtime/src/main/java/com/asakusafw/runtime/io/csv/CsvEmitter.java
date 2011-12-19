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
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.io.RecordEmitter;
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
 * A simple CSV emitter.
 * @since 0.2.4
 */
public class CsvEmitter implements RecordEmitter {

    static final Log LOG = LogFactory.getLog(CsvEmitter.class);

    private static final int INITIAL_BUFFER_SIZE = 1024;

    private static final String LINE_DELIMITER = "\r\n";

    private static final char CELL_DELIMITER = ',';

    private static final char ESCAPE = '"';

    private static final Pattern ESCAPE_PATTERN = Pattern.compile("[" + ESCAPE + CELL_DELIMITER + LINE_DELIMITER + "]");

    private final Writer writer;

    private final String trueFormat;

    private final String falseFormat;

    private final SimpleDateFormat dateFormat;

    private final boolean escapeDate;

    private final SimpleDateFormat dateTimeFormat;

    private final boolean escapeDateTime;

    private final List<String> headerCellsFormat;

    private boolean firstLine = true;

    private boolean firstCell = true;

    private boolean open = true;

    private final StringBuilder lineBuffer = new StringBuilder(INITIAL_BUFFER_SIZE);

    private final Calendar calendarBuffer = Calendar.getInstance();

    /**
     * Creates a new instance.
     * @param writer the target stream
     * @param path the destination path
     * @param config current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvEmitter(Writer writer, String path, CsvConfiguration config) {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        this.writer = writer;
        this.trueFormat = escape(config.getTrueFormat());
        this.falseFormat = escape(config.getFalseFormat());
        this.dateFormat = new SimpleDateFormat(config.getDateFormat());
        this.escapeDate = hasMetaCharacter(dateFormat);
        this.dateTimeFormat = new SimpleDateFormat(config.getDateTimeFormat());
        this.escapeDateTime = hasMetaCharacter(dateTimeFormat);
        this.headerCellsFormat = config.getHeaderCells();
    }

    private String escape(String string) {
        assert string != null;
        if (hasEscapeTarget(string)) {
            StringBuilder buffer = new StringBuilder();
            appendEscaped(buffer, string);
            return buffer.toString();
        }
        return string;
    }

    private boolean hasMetaCharacter(SimpleDateFormat format) {
        assert format != null;
        return ESCAPE_PATTERN.matcher(format.toPattern()).find();
    }

    @Override
    public void emit(BooleanOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(toString(option.get()));
        }
    }

    private String toString(boolean value) {
        return value ? trueFormat : falseFormat;
    }

    @Override
    public void emit(ByteOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(option.get());
        }
    }

    @Override
    public void emit(ShortOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(option.get());
        }
    }

    @Override
    public void emit(IntOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(option.get());
        }
    }

    @Override
    public void emit(LongOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(option.get());
        }
    }

    @Override
    public void emit(FloatOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(option.get());
        }
    }

    @Override
    public void emit(DoubleOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(option.get());
        }
    }

    @Override
    public void emit(DecimalOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            lineBuffer.append(option.get());
        }
    }

    @Override
    public void emit(StringOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            String str = option.getAsString();
            if (hasEscapeTarget(str)) {
                appendEscaped(lineBuffer, str);
            } else {
                lineBuffer.append(str);
            }
        }
    }

    private boolean hasEscapeTarget(String string) {
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            if (c == CELL_DELIMITER || c == ESCAPE || c == '\r' || c == '\n') {
                return true;
            }
        }
        return false;
    }

    @Override
    public void emit(DateOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            DateUtil.setDayToCalendar(option.get().getElapsedDays(), calendarBuffer);
            String string = dateFormat.format(calendarBuffer.getTime());
            if (escapeDate) {
                appendEscaped(lineBuffer, string);
            } else {
                lineBuffer.append(string);
            }
        }
    }

    @Override
    public void emit(DateTimeOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            DateUtil.setSecondToCalendar(option.get().getElapsedSeconds(), calendarBuffer);
            String string = dateTimeFormat.format(calendarBuffer.getTime());
            if (escapeDateTime) {
                appendEscaped(lineBuffer, string);
            } else {
                lineBuffer.append(string);
            }
        }
    }

    private void appendEscaped(StringBuilder buffer, String string) {
        buffer.append(ESCAPE);
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            if (c == ESCAPE) {
                buffer.append(ESCAPE);
            }
            buffer.append(c);
        }
        buffer.append(ESCAPE);
    }

    private void addCellDelimiter() {
        if (firstCell) {
            firstCell = false;
        } else {
            lineBuffer.append(CELL_DELIMITER);
        }
    }

    @Override
    public void endRecord() throws IOException {
        lineBuffer.append(LINE_DELIMITER);
        flushBuffer();
        firstLine = false;
        firstCell = true;
    }

    @Override
    public void flush() throws IOException {
        flushBuffer();
        writer.flush();
    }

    private void flushBuffer() throws IOException {
        if (firstLine) {
            firstLine = false;
            Iterator<String> iter = headerCellsFormat.iterator();
            if (iter.hasNext()) {
                writer.append(escape(iter.next()));
                while (iter.hasNext()) {
                    writer.append(CELL_DELIMITER);
                    writer.append(escape(iter.next()));
                }
                writer.append(LINE_DELIMITER);
            }
        }
        if (lineBuffer.length() > 0) {
            writer.append(lineBuffer);
            lineBuffer.setLength(0);
        }
    }

    @Override
    public void close() throws IOException {
        if (open) {
            flush();
            open = false;
            writer.close();
        }
    }
}
