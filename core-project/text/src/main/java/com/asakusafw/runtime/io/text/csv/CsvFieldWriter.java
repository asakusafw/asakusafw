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
package com.asakusafw.runtime.io.text.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import com.asakusafw.runtime.io.text.FieldWriter;
import com.asakusafw.runtime.io.text.LineSeparator;
import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.UnmappableOutput;
import com.asakusafw.runtime.io.text.UnmappableOutputException;
import com.asakusafw.runtime.io.text.driver.FieldOutput;
import com.asakusafw.runtime.io.text.driver.StandardFieldOutputOption;

/**
 * A {@link FieldWriter} for RFC4180 style CSV files.
 * @since 0.9.1
 */
public class CsvFieldWriter implements FieldWriter {

    private final Writer writer;

    private final char fieldSeparator;

    private final char quoteCharacter;

    private final String recordSeparatorSequence;

    private final boolean denyLineFeedInQuote;

    private final QuoteStyle defaultQuoteStyle;

    private final QuoteStyle headerQuoteStyle;

    private final UnaryOperator<CharSequence> transformer;

    private final StringBuilder lineBuffer = new StringBuilder();

    private int currentFieldIndex = -1;

    private final List<UnmappableOutput> unmappables = new ArrayList<>();

    private char[] writeBuffer;

    private State lastState = State.BEFORE_RECORD;

    /**
     * Creates a new instance.
     * @param writer the destination writer
     * @param lineSeparator the line separator kind
     * @param fieldSeparator the field separator character
     * @param quoteCharacter the quote character
     * @param allowLineFeed {@code true} to allow LF in field, otherwise {@code false}
     * @param defaultQuoteStyle the default quote style
     * @param headerQuoteStyle the quote style for headers
     * @param transformer the output transformer (nullable)
     */
    public CsvFieldWriter(
            Writer writer,
            LineSeparator lineSeparator, char fieldSeparator, char quoteCharacter,
            boolean allowLineFeed,
            QuoteStyle defaultQuoteStyle, QuoteStyle headerQuoteStyle,
            UnaryOperator<CharSequence> transformer) {
        this.writer = writer;
        this.fieldSeparator = fieldSeparator;
        this.quoteCharacter = quoteCharacter;
        this.recordSeparatorSequence = lineSeparator.getSequence();
        this.denyLineFeedInQuote = allowLineFeed == false;
        this.defaultQuoteStyle = defaultQuoteStyle;
        this.headerQuoteStyle = headerQuoteStyle;
        this.transformer = transformer == null ? UnaryOperator.identity() : transformer;
    }

    @Override
    public void putField(FieldOutput output) throws IOException {
        CharSequence contents = output.get();
        switch (lastState) {
        case BEFORE_RECORD:
            break;
        case END_OF_FIELD:
            putEndOfFieldBody();
            break;
        default:
            throw new AssertionError(lastState);
        }
        currentFieldIndex++;
        if (contents == null) {
            handleUnmap(UnmappableOutput.ErrorCode.UNDEFINED_NULL_SEQUENCE);
        } else {
            QuoteStyle style = computeQuoteStyle(output);
            switch (style) {
            case NEEDED:
                putFieldComputeQuote(contents);
                break;
            case ALWAYS:
                putFieldAlwaysQuote(contents);
                break;
            case NEVER:
                putFieldNeverQuote(contents);
                break;
            default:
                throw new AssertionError(style);
            }
        }
        lastState = State.END_OF_FIELD;
    }

    private QuoteStyle computeQuoteStyle(FieldOutput output) {
        Collection<? extends FieldOutput.Option> options = output.getOptions();
        QuoteStyle result = defaultQuoteStyle;
        if (options.isEmpty() == false) {
            for (FieldOutput.Option option : options) {
                if (option instanceof QuoteStyle) {
                    result = (QuoteStyle) option;
                    break;
                } else if (option == StandardFieldOutputOption.HEADER) {
                    result = headerQuoteStyle;
                }
            }
        }
        return result;
    }

    private void putFieldComputeQuote(CharSequence contents) {
        if (isQuoteRequired(contents)) {
            putWithQuote(contents);
        } else {
            emit(contents);
        }
    }

    private void putFieldAlwaysQuote(CharSequence contents) {
        putWithQuote(contents);
    }

    private void putFieldNeverQuote(CharSequence contents) {
        boolean sawCr = false;
        for (int i = 0, n = contents.length(); i < n; i++) {
            char c = contents.charAt(i);
            if (c == '\r') {
                handleUnmap(UnmappableOutput.ErrorCode.EXTRA_RECORD_SEPARATOR);
                sawCr = true;
            } else if (c == '\n') {
                if (sawCr == false) {
                    handleUnmap(UnmappableOutput.ErrorCode.EXTRA_RECORD_SEPARATOR);
                }
                sawCr = false;
            } else if (c == fieldSeparator) {
                handleUnmap(UnmappableOutput.ErrorCode.EXTRA_FIELD_SEPARATOR);
                sawCr = false;
            } else if (c == quoteCharacter) {
                handleUnmap(UnmappableOutput.ErrorCode.RESTRICTED_SEQUENCE,
                        TextUtil.quote(String.valueOf(c)));
                sawCr = false;
            }
            emit(c);
        }
    }

    private void putWithQuote(CharSequence contents) {
        emit(quoteCharacter);
        for (int i = 0, n = contents.length(); i < n; i++) {
            char c = contents.charAt(i);
            if (c == '\n' && denyLineFeedInQuote) {
                handleUnmap(UnmappableOutput.ErrorCode.RESTRICTED_SEQUENCE,
                        TextUtil.quote(String.valueOf('\n')));
            } else if (c == quoteCharacter) {
                emit(c);
            }
            emit(c);
        }
        emit(quoteCharacter);
    }

    @Override
    public void putEndOfRecord() throws IOException {
        CharSequence output = transformer.apply(lineBuffer);
        if (output != null) {
            write(output);
            resetBuffer();
            switch (lastState) {
            case BEFORE_RECORD:
                handleUnmap(UnmappableOutput.ErrorCode.EXTRA_EMPTY_FIELD);
                putEndOfRecordBody();
                break;
            case END_OF_FIELD:
                putEndOfRecordBody();
                break;
            default:
                throw new AssertionError(lastState);
            }
            write(lineBuffer);
        }
        resetBuffer();
        currentFieldIndex = -1;
        lastState = State.BEFORE_RECORD;

        if (unmappables.isEmpty() == false) {
            // Raise UnmappableOutputException even if the output was transformed
            UnmappableOutputException e = new UnmappableOutputException(unmappables);
            unmappables.clear();
            throw e;
        }
    }

    private void putEndOfFieldBody() {
        emit(fieldSeparator);
    }

    private void putEndOfRecordBody() {
        emit(recordSeparatorSequence);
    }

    private boolean isQuoteRequired(CharSequence cs) {
        for (int i = 0, n = cs.length(); i < n; i++) {
            char c = cs.charAt(i);
            if (c == '\r' || c == '\n' || c == fieldSeparator || c == quoteCharacter) {
                return true;
            }
        }
        return false;
    }

    private void emit(int c) {
        assert c >= 0;
        lineBuffer.append((char) c);
    }

    private void emit(CharSequence string) {
        lineBuffer.append(string);
    }

    private void write(CharSequence output) throws IOException {
        // java.io.Writer#write() may call CharSequence.toString()
        if (output instanceof StringBuilder) {
            StringBuilder src = (StringBuilder) output;
            char[] cbuf = writeBuffer;
            if (cbuf == null) {
                cbuf = new char[4096];
                writeBuffer = cbuf;
            }
            for (int offset = 0, step = cbuf.length, n = output.length(); offset < n; offset += step) {
                int length = Math.min(n - offset, step);
                src.getChars(offset, offset + length, cbuf, 0);
                writer.write(cbuf, 0, length);
            }
        } else {
            writer.append(output);
        }
    }

    private void resetBuffer() {
        lineBuffer.setLength(0);
    }

    private void handleUnmap(UnmappableOutput.ErrorCode kind) {
        handleUnmap(kind, null);
    }

    private void handleUnmap(UnmappableOutput.ErrorCode kind, String sequence) {
        unmappables.add(new UnmappableOutput(kind, currentFieldIndex, sequence));
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private enum State {
        BEFORE_RECORD,
        END_OF_FIELD,
    }
}
