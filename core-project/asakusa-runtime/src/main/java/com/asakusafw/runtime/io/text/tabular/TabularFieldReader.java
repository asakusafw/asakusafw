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
import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

import com.asakusafw.runtime.io.text.FieldReader;

/**
 * A {@link FieldReader} for tabular-style text contents.
 * @since 0.9.1
 */
public class TabularFieldReader implements FieldReader {

    private static final int EOF = -1;

    private static final int ABSENT = -2;

    private final LineCursor lineCursor;

    private final char fieldSeparator;

    private final int escapeCharacter;

    private final UnaryOperator<CharSequence> transformer;

    private final CharMap escapeDecode;

    private final boolean escapeRecordSeparator;

    private CharSequence currentLine;

    private final StringBuilder fieldBuffer = new StringBuilder();

    private int nextReadIndex = -1;

    private long currentRecordIndex = -1;

    private int currentFieldIndex = -1;

    private State lastState = State.BEFORE_RECORD;

    /**
     * Creates a new instance.
     * @param reader the source text reader
     * @param fieldSeparator the field separator character
     * @param escapeSequences the escape sequences definition (nullable)
     * @param transformer the line content transformer (nullable)
     */
    public TabularFieldReader(
            Reader reader,
            char fieldSeparator,
            EscapeSequence escapeSequences,
            UnaryOperator<CharSequence> transformer) {
        this.lineCursor = buildLineCursor(reader, escapeSequences);
        this.fieldSeparator = fieldSeparator;
        this.escapeCharacter = escapeSequences == null ? ABSENT : escapeSequences.getEscapeCharacter();
        this.escapeDecode = escapeSequences == null ? CharMap.EMPTY : CharMap.forward(escapeSequences);
        this.escapeRecordSeparator = escapeSequences == null ? false : escapeSequences.canEscapeLineSeparator();
        this.transformer = transformer == null ? UnaryOperator.identity() : transformer;
    }

    private static LineCursor buildLineCursor(Reader reader, EscapeSequence esc) {
        if (esc == null) {
            return new LineCursor(reader, '\0', false, false);
        }
        char e = esc.getEscapeCharacter();
        boolean allowEscapeLine = esc.canEscapeLineSeparator();
        boolean allowEscapeEscape = esc.canEscape(e);
        return new LineCursor(reader, e, allowEscapeLine, allowEscapeEscape);
    }

    @Override
    public boolean nextRecord() throws IOException {
        lastState = State.BEFORE_RECORD;
        currentLine = null;
        currentFieldIndex = -1;
        while (lineCursor.next()) {
            CharSequence s = transformer.apply(lineCursor.getContent());
            if (s == null) {
                continue;
            }
            nextReadIndex = 0;
            currentLine = s;
            currentRecordIndex++;
            return true;
        }
        currentRecordIndex = -1;
        return false;
    }

    @Override
    public boolean nextField() throws IOException {
        if (lastState.moreFields == false) {
            nextReadIndex = -1;
            currentFieldIndex = -1;
            lastState = State.AFTER_RECORD;
            return false;
        }
        State state = State.INIT;
        CharSequence line = currentLine;
        assert line != null;
        fieldBuffer.setLength(0);
        int index = nextReadIndex;
        do {
            int c = index == line.length() ? EOF : line.charAt(index++);
            switch (state) {
            case INIT:
                state = doInit(c);
                break;
            case SAW_ESCAPE:
                state = doSawEscape(c);
                break;
            case SAW_NULL:
                state = doSawNull(c);
                break;
            default:
                throw new AssertionError(state);
            }
        } while (state.moreCharacters);

        nextReadIndex = index;
        currentFieldIndex++;
        lastState = state;
        return true;
    }

    @Override
    public void rewindFields() throws IOException {
        lastState = State.BEFORE_RECORD;
        currentFieldIndex = -1;
        nextReadIndex = 0;
    }

    private State doInit(int c) {
        if (c < 0) {
            // EOF
            return State.END_OF_RECORD;
        } else if (c == fieldSeparator) {
            // ::
            return State.END_OF_FIELD;
        } else if (c == escapeCharacter) {
            // \\
            return State.SAW_ESCAPE;
        } else {
            // c
            emit(c);
            return State.INIT;
        }
    }

    private State doSawEscape(int c) {
        int esc = escapeCharacter;
        if (c < 0) {
            // ESC, $
            if (escapeRecordSeparator == false) {
                emit((char) esc);
            }
            return State.END_OF_RECORD;
        }
        // ESC, c
        int d = escapeDecode.get(c);
        if (d == CharMap.ABSENT) {
            // unknown escape sequence
            if (c == esc) {
                // ESC, ESC
                emit(esc);
                return State.SAW_ESCAPE;
            } else if (c == fieldSeparator) {
                // ESC, ::
                emit(esc);
                return State.END_OF_FIELD;
            } else if (escapeRecordSeparator && (c == '\r' || c == '\n')) {
                // ESC, CR
                // ESC, LF
                emit(c);
                return State.INIT;
            } else {
                // ESC, c
                emit(esc);
                emit(c);
                return State.INIT;
            }
        } else if (d == CharMap.NULL_CHARACTER) {
            // null sequence
            if (isEmitted()) {
                // null sequence leads extra characters
                emit(esc);
                emit(c);
                return State.INIT;
            } else {
                return State.SAW_NULL;
            }
        } else {
            // valid escape sequence
            emit(d);
            return State.INIT;
        }
    }

    private State doSawNull(int c) {
        if (c < 0) {
            // NULL, EOR
            return State.END_OF_RECORD_WITH_NULL;
        } else if (c == fieldSeparator) {
            // NULL, ::
            return State.END_OF_FIELD_WITH_NULL;
        } else if (c == escapeCharacter) {
            // NULL, \\
            // null sequence follows extra sequences
            emit(escapeCharacter);
            emit(escapeDecode.getNullKey());
            return State.SAW_ESCAPE;
        } else {
            // NULL, c
            // null sequence follows extra sequences
            emit(escapeCharacter);
            emit(escapeDecode.getNullKey());
            emit(c);
            return State.INIT;
        }
    }

    private boolean isEmitted() {
        return fieldBuffer.length() != 0;
    }

    private void emit(int c) {
        fieldBuffer.append((char) c);
    }

    @Override
    public CharSequence getContent() {
        switch (lastState) {
        case END_OF_FIELD:
        case END_OF_RECORD:
            return fieldBuffer;
        case END_OF_FIELD_WITH_NULL:
        case END_OF_RECORD_WITH_NULL:
            return null;
        case BEFORE_RECORD:
        case AFTER_RECORD:
            throw new NoSuchElementException(String.format(
                    "line-number=%,d, record-index=%,d, field-index=%,d, last-state=%s", //$NON-NLS-1$
                    getRecordLineNumber(),
                    getRecordIndex(),
                    getFieldIndex(),
                    lastState));
        default:
            throw new AssertionError(lastState);
        }
    }

    @Override
    public long getRecordLineNumber() {
        return lineCursor.getLineNumber();
    }

    @Override
    public long getRecordIndex() {
        return currentRecordIndex;
    }

    @Override
    public long getFieldIndex() {
        return currentFieldIndex;
    }

    @Override
    public void close() throws IOException {
        currentLine = null;
        currentFieldIndex = -1;
        currentRecordIndex = -1;
        lastState = State.AFTER_RECORD;
        lineCursor.close();
    }

    private enum State {
        // in decode
        INIT(true, false),
        SAW_ESCAPE(true, false),
        SAW_NULL(true, false),

        // decode end
        END_OF_FIELD(false, true),
        END_OF_RECORD(false, false),
        END_OF_FIELD_WITH_NULL(false, true),
        END_OF_RECORD_WITH_NULL(false, false),

        // lifecycle
        BEFORE_RECORD(false, true),
        AFTER_RECORD(false, false),
        ;
        final boolean moreCharacters;
        final boolean moreFields;

        State(boolean continueField, boolean continueRecord) {
            this.moreCharacters = continueField;
            this.moreFields = continueRecord;
        }
    }
}
