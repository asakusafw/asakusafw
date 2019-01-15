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
import java.io.Reader;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.io.text.FieldReader;
import com.asakusafw.runtime.io.text.TextUtil;

/**
 * A {@link FieldReader} for RFC4180 style CSV files.
 * @since 0.9.1
 */
public class CsvFieldReader implements FieldReader {

    static final Log LOG = LogFactory.getLog(CsvFieldReader.class);

    private static final int EOF = -1;

    private final LineCursor lineCursor;

    private final char fieldSeparator;

    private final char quoteCharacter;

    private final UnaryOperator<CharSequence> transformer;

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
     * @param quoteCharacter the quote character
     * @param allowLineFeed {@code true} to allow LF in field, otherwise {@code false}
     * @param transformer the line content transformer (nullable)
     */
    public CsvFieldReader(
            Reader reader,
            char fieldSeparator, char quoteCharacter,
            boolean allowLineFeed,
            UnaryOperator<CharSequence> transformer) {
        this.lineCursor = new LineCursor(reader, quoteCharacter, fieldSeparator, allowLineFeed);
        this.fieldSeparator = fieldSeparator;
        this.quoteCharacter = quoteCharacter;
        this.transformer = transformer == null ? UnaryOperator.identity() : transformer;
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
        State state = State.BEGIN_FIELD;
        CharSequence line = currentLine;
        assert line != null;
        fieldBuffer.setLength(0);
        int index = nextReadIndex;
        do {
            int c = index == line.length() ? EOF : line.charAt(index++);
            switch (state) {
            case BEGIN_FIELD:
                state = doBeginField(c);
                break;
            case BARE_BODY:
                state = doBareBody(c);
                break;
            case QUOTE_BODY:
                state = doQuoteBody(c);
                break;
            case QUOTE_BODY_SAW_QUOTE:
                state = doQuoteBodySawQuote(c);
                break;
            default:
                throw new AssertionError(lastState);
            }
        } while (state.moreCharacters);

        nextReadIndex = index;
        currentFieldIndex++;
        lastState = state;
        return true;
    }

    private State doBeginField(int c) {
        if (c == quoteCharacter) {
            // QUOTE
            return State.QUOTE_BODY;
        } else if (c == fieldSeparator) {
            // FS
            return State.END_OF_FIELD;
        } else if (c == EOF) {
            // EOF
            return State.END_OF_RECORD;
        } else {
            // CR
            // LF
            // c
            emit(c);
            return State.BARE_BODY;
        }
    }

    private State doBareBody(int c) {
        if (c == quoteCharacter) {
            // QUOTE
            error(ErrorCode.UNEXPECTED_QUOTE);
            emit(c);
            return State.BARE_BODY;
        } else if (c == fieldSeparator) {
            // FS
            return State.END_OF_FIELD;
        } else if (c == EOF) {
            // EOF
            return State.END_OF_RECORD;
        } else {
            // c
            emit(c);
            return State.BARE_BODY;
        }
    }

    private State doQuoteBody(int c) {
        if (c == quoteCharacter) {
            // QUOTE
            return State.QUOTE_BODY_SAW_QUOTE;
        } else if (c == EOF) {
            // EOF
            error(ErrorCode.UNEXPECTED_END_OF_FILE_IN_QUOTE);
            return State.END_OF_RECORD;
        } else {
            // FS
            // c
            emit(c);
            return State.QUOTE_BODY;
        }
    }

    private State doQuoteBodySawQuote(int c) {
        if (c == quoteCharacter) {
            // QUOTE
            emit(c);
            return State.QUOTE_BODY;
        } else if (c == fieldSeparator) {
            // FS
            return State.END_OF_FIELD;
        } else if (c == EOF) {
            // EOF
            return State.END_OF_RECORD;
        } else {
            // c
            error(ErrorCode.UNEXPECTED_END_OF_QUOTE);
            emit(quoteCharacter);
            emit(c);
            return State.QUOTE_BODY;
        }
    }

    private void emit(int c) {
        fieldBuffer.append((char) c);
    }

    private void error(ErrorCode code) {
        LOG.warn(MessageFormat.format(
                "code={0}, contents={1}, column={2}",
                code,
                TextUtil.quote(currentLine),
                nextReadIndex + 1));
    }

    @Override
    public void rewindFields() throws IOException {
        lastState = State.BEFORE_RECORD;
        currentFieldIndex = -1;
        nextReadIndex = 0;
    }

    @Override
    public CharSequence getContent() {
        switch (lastState) {
        case END_OF_FIELD:
        case END_OF_RECORD:
            return fieldBuffer;
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
        BEGIN_FIELD(true, false),
        BARE_BODY(true, false),
        QUOTE_BODY(true, false),
        QUOTE_BODY_SAW_QUOTE(true, false),

        // decode end
        END_OF_FIELD(false, true),
        END_OF_RECORD(false, false),

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

    private enum ErrorCode {

        UNEXPECTED_QUOTE,

        UNEXPECTED_END_OF_FILE_IN_QUOTE,

        UNEXPECTED_END_OF_QUOTE,
    }
}
