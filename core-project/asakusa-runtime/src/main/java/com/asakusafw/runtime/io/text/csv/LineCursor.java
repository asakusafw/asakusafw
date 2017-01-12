/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

import com.asakusafw.runtime.io.text.TextFormatException;
import com.asakusafw.runtime.io.text.TextUtil;

/**
 * Extracts records delimited by line separator characters.
 * @since 0.9.1
 */
final class LineCursor implements Closeable {

    static final int READ_BUFFER_SIZE = 4096;

    private static final int EOF = -1;

    private final Reader reader;

    private final char quoteCharacter;

    private final char fieldSeparator;

    private final boolean denyLineFeedInQuote;

    private final StringBuilder lineBuffer = new StringBuilder();

    private final char[] readBuffer = new char[READ_BUFFER_SIZE];

    private int readOffset = 0;

    private int readLimit = 0;

    private int emitOffset = 0;

    private long currentLineNumber = -1L;

    private long nextLineNumber = 0L;

    private boolean sawInvalidLineFeed = false;

    LineCursor(Reader reader, char quoteCharacter, char fieldSeparator, boolean allowLineFeed) {
        this.reader = reader;
        this.quoteCharacter = quoteCharacter;
        this.fieldSeparator = fieldSeparator;
        this.denyLineFeedInQuote = allowLineFeed == false;
    }

    public boolean next() throws IOException {
        State state = State.BEGIN_RECORD;
        lineBuffer.setLength(0);
        currentLineNumber = nextLineNumber;
        do {
            int c = get();
            switch (state) {
            case BEGIN_RECORD:
                state = doBeginRecord(c);
                break;
            case BEGIN_FIELD:
                state = doBeginField(c);
                break;
            case BARE_BODY:
                state = doBareBody(c);
                break;
            case BODY_SAW_CR:
                state = doBodySawCr(c);
                break;
            case QUOTE_BODY:
                state = doQuoteBody(c);
                break;
            case QUOTE_BODY_SAW_CR:
                state = doQuoteBodySawCr(c);
                break;
            case QUOTE_BODY_SAW_QUOTE:
                state = doQuoteBodySawQuote(c);
                break;
            default:
                throw new AssertionError(state);
            }
        } while (state.more);
        if (sawInvalidLineFeed) {
            sawInvalidLineFeed = false;
            throw new TextFormatException(MessageFormat.format(
                    "no line-feed in quote is allowed: {0}",
                    TextUtil.quote(lineBuffer)));
        }
        return state == State.END_OF_RECORD;
    }

    private State doBeginRecord(int c) {
        if (c == EOF) {
            return State.END_OF_CONTENT;
        } else {
            return doBeginField(c);
        }
    }

    // ^ ...
    private State doBeginField(int c) {
        if (c == quoteCharacter) {
            // ^ QUOTE
            return State.QUOTE_BODY;
        } else if (c == fieldSeparator) {
            // ^ FS
            return State.BEGIN_FIELD; // empty field
        } else if (c == '\r') {
            // ^ CR
            return State.BODY_SAW_CR;
        } else if (c == '\n') {
            // ^ LF
            nextLineNumber++;
            emit(-1);
            return State.END_OF_RECORD;
        } else if (c == EOF) {
            // ^ EOF
            emit(0);
            return State.END_OF_RECORD;
        } else {
            // ^ c
            return State.BARE_BODY;
        }
    }

    // ^ !QUOTE ...
    private State doBareBody(int c) {
        if (c == quoteCharacter) {
            // QUOTE
            // NOTE: unexpected quote in bare body
            return State.BARE_BODY;
        } else if (c == fieldSeparator) {
            // FS
            return State.BEGIN_FIELD;
        } else if (c == '\r') {
            // CR
            return State.BODY_SAW_CR;
        } else if (c == '\n') {
            // LF
            nextLineNumber++;
            emit(-1);
            return State.END_OF_RECORD;
        } else if (c == EOF) {
            // EOF
            emit(0);
            return State.END_OF_RECORD;
        } else {
            // c
            return State.BARE_BODY;
        }
    }

    // CR, ...
    private State doBodySawCr(int c) {
        nextLineNumber++;
        if (c == '\n') {
            // CR LF
            emit(-2);
            return State.END_OF_RECORD;
        } else if (c == EOF) {
            // CR EOF
            emit(-1);
            return State.END_OF_RECORD;
        } else {
            // CR c
            // CR QUOTE
            // CR FS
            // CR CR
            pushBack();
            emit(-1);
            return State.END_OF_RECORD;
        }
    }

    // ^ QUOTE, *, ...
    private State doQuoteBody(int c) {
        if (c == quoteCharacter) {
            // (in-quote) QUOTE
            return State.QUOTE_BODY_SAW_QUOTE;
        } else if (c == '\r') {
            // (in-quote) CR
            return State.QUOTE_BODY_SAW_CR;
        } else if (c == '\n') {
            // (in-quote) LF
            recordLineFeedInQuote();
            nextLineNumber++;
            return State.QUOTE_BODY;
        } else if (c == EOF) {
            // (in-quote) EOF
            // NOTE: unexpected end of file
            emit(0);
            return State.END_OF_RECORD;
        } else {
            // (in-quote) c
            // (in-quote) FS
            return State.QUOTE_BODY;
        }
    }

    // ^ QUOTE, *, CR, ...
    private State doQuoteBodySawCr(int c) {
        nextLineNumber++;
        if (c == quoteCharacter) {
            // (in-quote) CR QUOTE
            return State.QUOTE_BODY_SAW_QUOTE;
        } else if (c == '\r') {
            // (in-quote) CR CR
            return State.QUOTE_BODY_SAW_CR;
        } else if (c == '\n') {
            // (in-quote) CR LF
            recordLineFeedInQuote();
            return State.QUOTE_BODY;
        } else if (c == EOF) {
            // (in-quote) EOF
            // NOTE: unexpected end of file
            emit(0);
            return State.END_OF_RECORD;
        } else {
            // (in-quote) c
            // (in-quote) FS
            // (in-quote) LF
            return State.QUOTE_BODY;
        }
    }

    // ^ QUOTE, *, QUOTE, ...
    private State doQuoteBodySawQuote(int c) {
        if (c == quoteCharacter) {
            // (in-quote) QUOTE QUOTE
            return State.QUOTE_BODY;
        } else if (c == fieldSeparator) {
            // (in-quote) QUOTE FS
            return State.BEGIN_FIELD;
        } else if (c == '\r') {
            // (in-quote) QUOTE CR
            return State.BODY_SAW_CR;
        } else if (c == '\n') {
            // (in-quote) QUOTE LF
            nextLineNumber++;
            emit(-1);
            return State.END_OF_RECORD;
        } else if (c == EOF) {
            // (in-quote) QUOTE EOF
            emit(0);
            return State.END_OF_RECORD;
        } else {
            // (in-quote) QUOTE c
            // NOTE: unexpected end of quote
            return State.QUOTE_BODY;
        }
    }

    private void recordLineFeedInQuote() {
        if (denyLineFeedInQuote) {
            sawInvalidLineFeed = true;
        }
    }

    private int get() throws IOException {
        if (prepareBuffer()) {
            return readBuffer[readOffset++];
        }
        return EOF;
    }

    private void pushBack() {
        assert readOffset != 0;
        readOffset--;
    }

    private void emit(int offset) {
        assert offset <= 0;
        int flushLimit = readOffset + offset;
        if (flushLimit > emitOffset) {
            // remains characters to emit
            flush(flushLimit);
        } else {
            // some characters were already emitted
            int dropCount = emitOffset - flushLimit;
            if (dropCount > 0) {
                assert lineBuffer.length() >= dropCount;
                lineBuffer.delete(lineBuffer.length() - dropCount, lineBuffer.length());
            }
        }
        emitOffset = readOffset;
    }

    private boolean prepareBuffer() throws IOException {
        if (readOffset < readLimit) {
            return true;
        }
        // is read buffer full?
        if (readLimit == readBuffer.length) {
            flush(readLimit);
            emitOffset = 0;
            readLimit = 0;
            readOffset = 0;
        }
        // read to buffer
        int count = reader.read(readBuffer, readLimit, readBuffer.length - readLimit);
        if (count < 0) {
            return false;
        } else if (count > 0) {
            readLimit += count;
            return true;
        }
        // for non-blocking readers?
        int c = reader.read();
        if (c < 0) {
            return false;
        } else {
            readBuffer[readLimit++] = (char) c;
            return true;
        }
    }

    private void flush(int end) {
        int offset = emitOffset;
        assert offset <= end;
        if (offset < end) {
            lineBuffer.append(readBuffer, offset, end - offset);
        }
        emitOffset = end;
    }

    public long getLineNumber() {
        return currentLineNumber;
    }

    public CharSequence getContent() {
        return lineBuffer;
    }

    @Override
    public void close() throws IOException {
        currentLineNumber = -1;
        reader.close();
    }

    private enum State {

        BEGIN_RECORD(true),

        BEGIN_FIELD(true),

        BARE_BODY(true),

        BODY_SAW_CR(true),

        QUOTE_BODY(true),

        QUOTE_BODY_SAW_QUOTE(true),

        QUOTE_BODY_SAW_CR(true),

        END_OF_RECORD(false),

        END_OF_CONTENT(false),
        ;

        final boolean more;

        State(boolean more) {
            this.more = more;
        }
    }
}
