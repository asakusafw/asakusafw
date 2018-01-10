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
package com.asakusafw.runtime.io.text.tabular;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.asakusafw.runtime.io.text.FieldWriter;
import com.asakusafw.runtime.io.text.LineSeparator;
import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.UnmappableOutput;
import com.asakusafw.runtime.io.text.UnmappableOutputException;
import com.asakusafw.runtime.io.text.driver.FieldOutput;

/**
 * A {@link FieldWriter} for tabular style text contents.
 * @since 0.9.1
 */
public class TabularFieldWriter implements FieldWriter {

    private static final int ABSENT = -2;

    private final Writer writer;

    private final int escapeCharacter;

    private final CharMap escapeEncode;

    private final CharMap escapeDecode;

    private final char fieldSeparator;

    private final String recordSeparatorSequence;

    private final String nullSequence;

    private final boolean escapeRecordSeparator;

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
     * @param escapeSequences the escape sequences definition (nullable)
     * @param transformer the output transformer (nullable)
     */
    public TabularFieldWriter(
            Writer writer,
            LineSeparator lineSeparator,
            char fieldSeparator,
            EscapeSequence escapeSequences,
            UnaryOperator<CharSequence> transformer) {
        this.writer = writer;
        EscapeSequence esc = escapeSequences;
        this.escapeCharacter = esc == null ? ABSENT : esc.getEscapeCharacter();
        this.escapeEncode = esc == null ? CharMap.EMPTY : CharMap.backward(esc);
        this.escapeDecode = esc == null ? CharMap.EMPTY : CharMap.forward(esc);
        this.recordSeparatorSequence = lineSeparator.getSequence();
        this.nullSequence = buildNullSequence(escapeCharacter, escapeEncode);
        this.fieldSeparator = fieldSeparator;
        this.escapeRecordSeparator = esc == null ? false : esc.canEscapeLineSeparator();
        this.transformer = transformer == null ? UnaryOperator.identity() : transformer;
    }

    private String buildNullSequence(int escape, CharMap map) {
        if (escape == ABSENT || map == null || map.getNullKey() == CharMap.ABSENT) {
            return null;
        }
        return new StringBuilder(2).append((char) escape).append((char) map.getNullKey()).toString();
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
        case END_OF_FIELD_WITH_ESCAPE:
            putEndOfFieldSawEscape();
            break;
        default:
            throw new AssertionError(lastState);
        }
        currentFieldIndex++;
        State state = State.BODY;
        if (contents == null) {
            state = putNull();
        } else {
            for (int i = 0, n = contents.length(); i < n; i++) {
                char c = contents.charAt(i);
                switch (state) {
                case BODY:
                    state = putCharBody(c);
                    break;
                case SAW_CR:
                    state = putCharSawCr(c);
                    break;
                case SAW_ESCAPE:
                    state = putCharSawEscape(c);
                    break;
                case SAW_ESCAPE_CR:
                    state = putCharSawEscapeCr(c);
                    break;
                default:
                    throw new AssertionError(state);
                }
            }
        }
        switch (state) {
        case BODY:
            lastState = State.END_OF_FIELD;
            break;
        case SAW_ESCAPE:
            lastState = State.END_OF_FIELD_WITH_ESCAPE;
            break;
        case SAW_CR:
            consumeSawCr();
            lastState = State.END_OF_FIELD;
            break;
        case SAW_ESCAPE_CR:
            consumeSawEscapeCr();
            lastState = State.END_OF_FIELD;
            break;
        default:
            throw new AssertionError(state);
        }
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
            case END_OF_FIELD_WITH_ESCAPE:
                putEndOfRecordSawEscape();
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

    private State putCharBody(char c) {
        int d = escapeEncode.get(c);
        if (d != CharMap.ABSENT) {
            emit(escapeCharacter);
            emit(d);
            return State.BODY;
        }
        if (c == '\r') {
            return State.SAW_CR;
        } else if (c == '\n') {
            if (escapeRecordSeparator) {
                emit(escapeCharacter);
            } else {
                handleUnmap(UnmappableOutput.ErrorCode.EXTRA_RECORD_SEPARATOR);
            }
            emit(c);
            return State.BODY;
        } else if (c == fieldSeparator) {
            handleUnmap(UnmappableOutput.ErrorCode.EXTRA_FIELD_SEPARATOR);
            emit(c);
            return State.BODY;
        } else if (c == escapeCharacter) {
            emit(c);
            return State.SAW_ESCAPE;
        } else {
            emit(c);
            return State.BODY;
        }
    }

    private State putCharSawCr(char c) {
        if (c == '\n') {
            consumeSawCr();
            emit('\n');
            return State.BODY;
        } else {
            consumeSawCr();
            return putCharBody(c);
        }
    }

    private State putCharSawEscape(char c) {
        assert escapeEncode.get(escapeCharacter) == CharMap.ABSENT;
        int d = escapeEncode.get(c);
        if (d != CharMap.ABSENT) {
            emit(escapeCharacter);
            emit(d);
            return State.BODY;
        }
        if (c == '\r') {
            return State.SAW_ESCAPE_CR;
        } else if (c == '\n') {
            if (escapeRecordSeparator == false) {
                handleUnmap(UnmappableOutput.ErrorCode.EXTRA_RECORD_SEPARATOR);
            }
            emit(c);
            return State.BODY;
        } else if (c == fieldSeparator) {
            handleUnmap(UnmappableOutput.ErrorCode.EXTRA_FIELD_SEPARATOR);
            emit(c);
            return State.BODY;
        } else if (c == escapeCharacter) {
            assert escapeDecode.get(c) == CharMap.ABSENT;
            emit(c);
            return State.SAW_ESCAPE;
        } else {
            if (escapeDecode.get(c) != CharMap.ABSENT) {
                handleUnmap(UnmappableOutput.ErrorCode.CONFLICT_SEQUENCE,
                        TextUtil.quote(new StringBuilder(2).append(escapeCharacter).append(c)));
            }
            emit(c);
            return State.BODY;
        }
    }

    private State putCharSawEscapeCr(char c) {
        assert escapeEncode.get(escapeCharacter) == CharMap.ABSENT;
        if (c == '\n') {
            consumeSawEscapeCr();
            emit('\n');
            return State.BODY;
        } else {
            consumeSawEscapeCr();
            return putCharBody(c);
        }
    }

    private void consumeSawCr() {
        if (escapeRecordSeparator) {
            emit(escapeCharacter);
        } else {
            handleUnmap(UnmappableOutput.ErrorCode.EXTRA_RECORD_SEPARATOR);
        }
        emit('\r');
    }

    private void consumeSawEscapeCr() {
        assert escapeEncode.get(escapeCharacter) == CharMap.ABSENT;
        assert escapeEncode.get('\r') == CharMap.ABSENT;
        if (escapeRecordSeparator == false) {
            handleUnmap(UnmappableOutput.ErrorCode.EXTRA_RECORD_SEPARATOR);
        }
        emit('\r');
    }

    private void putEndOfFieldBody() {
        emit(fieldSeparator);
    }

    private void putEndOfFieldSawEscape() {
        assert escapeEncode.get(escapeCharacter) == CharMap.ABSENT;
        if (escapeEncode.get(fieldSeparator) != CharMap.ABSENT) {
            handleUnmap(UnmappableOutput.ErrorCode.LOST_FIELD_SEPARATOR);
        }
        putEndOfFieldBody();
    }

    private void putEndOfRecordBody() {
        emit(recordSeparatorSequence);
    }

    private void putEndOfRecordSawEscape() {
        assert escapeEncode.get(escapeCharacter) == CharMap.ABSENT;
        if (escapeRecordSeparator) {
            handleUnmap(UnmappableOutput.ErrorCode.LOST_RECORD_SEPARATOR);
        }
        putEndOfRecordBody();
    }

    private State putNull() {
        if (nullSequence == null) {
            handleUnmap(UnmappableOutput.ErrorCode.UNDEFINED_NULL_SEQUENCE);
        } else {
            emit(nullSequence);
        }
        return State.BODY;
    }

    private void emit(int c) {
        assert c >= 0;
        lineBuffer.append((char) c);
    }

    private void emit(String string) {
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
        BODY,
        SAW_CR,
        SAW_ESCAPE,
        SAW_ESCAPE_CR,
        END_OF_FIELD,
        END_OF_FIELD_WITH_ESCAPE,
    }
}
