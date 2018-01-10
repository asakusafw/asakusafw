/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.io.RecordEmitter;
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
 * A simple CSV emitter.
 * @since 0.2.4
 */
public class CsvEmitter implements RecordEmitter {

    static final Log LOG = LogFactory.getLog(CsvEmitter.class);

    private static final int INITIAL_BUFFER_SIZE = 1024;

    private static final String LINE_DELIMITER = "\r\n"; //$NON-NLS-1$

    private static final char ESCAPE = '"';

    private final Writer writer;

    private final char separator;

    private final String trueFormat;

    private final String falseFormat;

    private final boolean escapeTrue;

    private final boolean escapeFalse;

    private final DateFormatter dateFormat;

    private final boolean escapeDate;

    private final DateTimeFormatter dateTimeFormat;

    private final boolean escapeDateTime;

    private final List<String> headerCellsFormat;

    private boolean firstLine = true;

    private boolean firstCell = true;

    private boolean open = true;

    private final StringBuilder lineBuffer = new StringBuilder(INITIAL_BUFFER_SIZE);

    private final Pattern escapePattern;

    private final BitSet forceEscapeMask;

    private int columnIndex;

    /**
     * Creates a new instance.
     * @param stream the target stream
     * @param path the destination path
     * @param config current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvEmitter(OutputStream stream, String path, CsvConfiguration config) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        this.writer = new OutputStreamWriter(stream, config.getCharset());
        this.separator = config.getSeparatorChar();
        this.escapePattern = Pattern.compile(
                "[" + ESCAPE + separator + LINE_DELIMITER + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        this.trueFormat = escape(config.getTrueFormat());
        this.escapeTrue = hasEscapeTarget(config.getTrueFormat());
        this.falseFormat = escape(config.getFalseFormat());
        this.escapeFalse = hasEscapeTarget(config.getFalseFormat());
        this.dateFormat = DateFormatter.newInstance(config.getDateFormat());
        this.escapeDate = hasMetaCharacter(dateFormat.getPattern());
        this.dateTimeFormat = DateTimeFormatter.newInstance(config.getDateTimeFormat());
        this.escapeDateTime = hasMetaCharacter(dateTimeFormat.getPattern());
        this.headerCellsFormat = config.getHeaderCells();
        this.forceEscapeMask = buildQuoteMask(config.getForceQuoteColumns());
        this.columnIndex = 0;
    }

    private BitSet buildQuoteMask(int[] indices) {
        if (indices.length == 0) {
            return null;
        }
        BitSet results = new BitSet();
        IntStream.of(indices).forEach(results::set);
        return results;
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

    private boolean hasMetaCharacter(String pattern) {
        assert pattern != null;
        return escapePattern.matcher(pattern).find();
    }

    @Override
    public void emit(BooleanOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            boolean value = option.get();
            String str = value ? trueFormat : falseFormat;
            boolean raw = value ? escapeTrue == false : escapeFalse == false;
            if (raw && isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(str).append(ESCAPE);
            } else {
                lineBuffer.append(str);
            }
        }
    }

    @Override
    public void emit(ByteOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            if (isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(option.get()).append(ESCAPE);
            } else {
                lineBuffer.append(option.get());
            }
        }
    }

    @Override
    public void emit(ShortOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            if (isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(option.get()).append(ESCAPE);
            } else {
                lineBuffer.append(option.get());
            }
        }
    }

    @Override
    public void emit(IntOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            if (isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(option.get()).append(ESCAPE);
            } else {
                lineBuffer.append(option.get());
            }
        }
    }

    @Override
    public void emit(LongOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            if (isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(option.get()).append(ESCAPE);
            } else {
                lineBuffer.append(option.get());
            }
        }
    }

    @Override
    public void emit(FloatOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            if (isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(option.get()).append(ESCAPE);
            } else {
                lineBuffer.append(option.get());
            }
        }
    }

    @Override
    public void emit(DoubleOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            if (isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(option.get()).append(ESCAPE);
            } else {
                lineBuffer.append(option.get());
            }
        }
    }

    @Override
    public void emit(DecimalOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            if (isEscapeTarget()) {
                lineBuffer.append(ESCAPE).append(option.get()).append(ESCAPE);
            } else {
                lineBuffer.append(option.get());
            }
        }
    }

    @Override
    public void emit(StringOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            String str = option.getAsString();
            if (isEscapeTarget() || hasEscapeTarget(str)) {
                appendEscaped(lineBuffer, str);
            } else {
                lineBuffer.append(str);
            }
        }
    }

    private boolean hasEscapeTarget(String string) {
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            if (c == separator || c == ESCAPE || c == '\r' || c == '\n') {
                return true;
            }
        }
        return false;
    }

    @Override
    public void emit(DateOption option) throws IOException {
        addCellDelimiter();
        if (option.isNull() == false) {
            CharSequence string = dateFormat.format(option.get().getElapsedDays());
            if (escapeDate || isEscapeTarget()) {
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
            CharSequence string = dateTimeFormat.format(option.get().getElapsedSeconds());
            if (escapeDateTime || isEscapeTarget()) {
                appendEscaped(lineBuffer, string);
            } else {
                lineBuffer.append(string);
            }
        }
    }

    private boolean isEscapeTarget() {
        BitSet mask = forceEscapeMask;
        return mask != null && mask.get(columnIndex);
    }

    private void appendEscaped(StringBuilder buffer, CharSequence string) {
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
            lineBuffer.append(separator);
            columnIndex++;
        }
    }

    @Override
    public void endRecord() throws IOException {
        lineBuffer.append(LINE_DELIMITER);
        flushBuffer();
        firstLine = false;
        firstCell = true;
        columnIndex = 0;
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
                    writer.append(separator);
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
