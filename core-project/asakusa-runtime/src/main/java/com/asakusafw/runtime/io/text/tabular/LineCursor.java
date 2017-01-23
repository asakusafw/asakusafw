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
package com.asakusafw.runtime.io.text.tabular;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * Parses lines in text.
 * @since 0.9.1
 */
class LineCursor implements Closeable {

    static final int READ_BUFFER_SIZE = 4096;

    private static final int EOF = -1;

    private static final int ABSENT = -2;

    private final Reader reader;

    private final int escapeCharacter;

    private final boolean allowEscapeEscape;

    private final StringBuilder lineBuffer = new StringBuilder();

    private final char[] readBuffer = new char[READ_BUFFER_SIZE];

    private int readOffset = 0;

    private int readLimit = 0;

    private int emitOffset = 0;

    private long currentLineNumber = -1L;

    private long nextLineNumber = 0L;

    LineCursor(Reader reader, char escape, boolean allowEscape, boolean allowEscapeEscape) {
        this.reader = reader;
        this.escapeCharacter = allowEscape ? escape : ABSENT;
        this.allowEscapeEscape = allowEscape && allowEscapeEscape;
    }

    public boolean next() throws IOException {
        State state = State.INIT;
        lineBuffer.setLength(0);
        currentLineNumber = nextLineNumber;
        do {
            int c = get();
            switch (state) {
            case INIT:
                state = doInit(c);
                break;
            case SAW_CR:
                state = doSawCr(c);
                break;
            case SAW_META:
                state = doSawMeta(c);
                break;
            case SAW_META_CR:
                state = doSawMetaCr(c);
                break;
            default:
                throw new AssertionError(state);
            }
        } while (state.more);
        return state == State.END_OF_RECORD;
    }

    // (), ...
    private State doInit(int c) {
        if (c < 0) {
            // EOF
            emit(0);
            if (lineBuffer.length() == 0) {
                // first character is EOF
                currentLineNumber = -1L;
                return State.END_OF_CONTENT;
            } else {
                return State.END_OF_RECORD;
            }
        } else if (c == escapeCharacter) {
            // \\
            return State.SAW_META;
        } else if (c == '\r') {
            // \r
            return State.SAW_CR;
        } else if (c == '\n') {
            // \n
            emit(-1); // drop "\n"
            nextLineNumber++;
            return State.END_OF_RECORD;
        } else {
            // c
            return State.INIT;
        }
    }

    // \\, ...
    private State doSawMeta(int c) {
        if (c < 0) {
            // \\, EOF
            emit(0);
            return State.END_OF_RECORD;
        } else if (c == escapeCharacter) {
            // \\, \\
            if (allowEscapeEscape) {
                // becomes a single escape character
                return State.INIT;
            } else {
                // can escape the next character
                return State.SAW_META;
            }
        } else if (c == '\r') {
            // \\, \r
            return State.SAW_META_CR;
        } else if (c == '\n') {
            // \\, \n
            nextLineNumber++;
            return State.INIT;
        } else {
            // \\, c
            return State.INIT;
        }
    }

    // \r, ...
    private State doSawCr(int c) {
        nextLineNumber++;
        if (c < 0) {
            // \r, EOF
            emit(-1); // drop "\r"
            return State.END_OF_RECORD;
        } else if (c == '\r') {
            // \r, \r
            pushBack(); // back last "\r"
            emit(-1); // drop first "\r"
            return State.END_OF_RECORD;
        } else if (c == '\n') {
            // \r, \n
            emit(-2); // drop "\r\n"
            return State.END_OF_RECORD;
        } else {
            // \r, \\
            // \r, c
            pushBack(); // back c
            emit(-1); // drop "\r"
            return State.END_OF_RECORD;
        }
    }

    // \\, \r, ...
    private State doSawMetaCr(int c) {
        nextLineNumber++;
        if (c < 0) {
            // \\, \r, EOF
            emit(0);
            return State.END_OF_RECORD;
        } else if (c == escapeCharacter) {
            // \\, \r, \\
            return State.SAW_META;
        } else if (c == '\r') {
            // \\, \r, \r
            return State.SAW_CR;
        } else {
            // \\, \r, \n
            // \\, \r, c
            return State.INIT;
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

        INIT(true),

        SAW_META(true),

        SAW_CR(true),

        SAW_META_CR(true),

        END_OF_RECORD(false),

        END_OF_CONTENT(false),
        ;

        final boolean more;

        State(boolean more) {
            this.more = more;
        }
    }
}
