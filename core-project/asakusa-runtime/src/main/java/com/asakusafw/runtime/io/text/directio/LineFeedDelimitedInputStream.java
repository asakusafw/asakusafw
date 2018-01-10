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
package com.asakusafw.runtime.io.text.directio;

import java.io.IOException;
import java.io.InputStream;

/**
 * Trims {@link InputStream} to provide only contents in the current split for line separated text.
 * This requires that each {@code 0x0d} byte in input always represents line feed (LF).
 * @since 0.9.1
 * @see InputSplitters
 */
public final class LineFeedDelimitedInputStream extends InputStream {

    /*
     * NOTE: splitting text files
     * 0. [input] is already skipped until [offset]
     * 1. scan the first record end from [offset] and drop the range if offset > 0: it is part of the previous split
     * 2. scan the record end from [offset]+[length] and take the range
     *
     * # offset == 0:
     * ---------------- (original text = input) -------------- ... ->EOF
     * <------(length)------->
     * <----(guaranteed)----->---(scan)-->$
     * <=========(CURRENT SPLIT)==========><---(next split)--- ... ->
     *
     * # offset > 0:
     * ## guaranteed > 0
     * ... --------------------------------- (original text) -------------------------------- ... ->EOF
     * ... --(skipped)-->------------------------(input)------------------------------------- ... ->EOF
     * ... --(offset)---><-------------(length)------------->
     *                   ---(scan)-->$<----(guaranteed)----->---(scan)-->$
     * ... ------(prev split)--------><=========(CURRENT SPLIT)==========><---(next split)--- ... ->
     *
     * ## guaranteed < 0 -> current split becomes empty
     * ... --------------------------------- (original text) -------------------------------- ... ->EOF
     * ... --(skipped)-->------------------------(input)------------------------------------- ... ->EOF
     * ... --(offset)---><-------------(length)------------->
     *                   -------------(scan)----------------->$
     * ... ------(prev split)---------------------------------><---------(next split)-------- ... ->
     *
     * ## guaranteed = 0 -> current split becomes NOT empty, and the next split will skip the cascaded range
     * ... --------------------------------- (original text) -------------------------------- ... ->EOF
     * ... --(skipped)-->------------------------(input)------------------------------------- ... ->EOF
     * ... --(offset)---><-------------(length)------------->
     *                   -------------(scan)--------------->$-----(scan)---->$
     * ... ------(prev split)-------------------------------><===(CURRENT)===><----(next)---- ... ->
     */

    private final InputStream source;

    private final byte[] buffer;

    private int bufferPosition;

    private int bufferLimit;

    private final StateMachine stateMachine;

    private boolean skipUntilPrevSplitEnd;

    private long guaranteedSplitRest;

    /**
     * Creates a new instance.
     * @param source the source input stream
     * @param offset the current stream position from the original head, in bytes
     * @param length the split length from the current stream position;
     *    the split may become smaller if the stream does not have enough size,
     *    and is continue until the last record end was appeared over this length
     */
    public LineFeedDelimitedInputStream(InputStream source, long offset, long length) {
        this(source, offset, length, new StateMachine());
    }

    private LineFeedDelimitedInputStream(InputStream source, long offset, long length, StateMachine stateMachine) {
        this.source = source;
        this.buffer = new byte[1024];
        this.stateMachine = stateMachine;
        this.skipUntilPrevSplitEnd = offset > 0;
        this.guaranteedSplitRest = length;
    }

    @Override
    public int read() throws IOException {
        if (prepare()) {
            return buffer[bufferPosition++];
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (prepare()) {
            int pos = bufferPosition;
            int remaining = bufferLimit - pos;
            assert remaining > 0;
            int read = Math.min(remaining, len);
            System.arraycopy(buffer, pos, b, off, read);
            bufferPosition = pos + read;
            return read;
        }
        return -1;
    }

    private boolean prepare() throws IOException {
        // has remaining
        if (bufferPosition < bufferLimit) {
            return true;
        }
        // skip until the last end of split
        if (skipUntilPrevSplitEnd) {
            skipUntilPrevSplitEnd = false;
            doSkipUntilPrevSplitEnd();
            return prepare();
        }
        // already saw end-of-split
        if (stateMachine.isFinished()) {
            return false;
        }
        // read from the upstream
        int read = source.read(buffer);
        assert read != 0;
        if (read < 0) {
            // no more contents
            forceEof();
            return false;
        }
        computeBufferRange(0, read);
        assert bufferPosition < bufferLimit;
        return true;
    }

    private void doSkipUntilPrevSplitEnd() throws IOException {
        StateMachine sm = stateMachine;
        assert sm.isFinished() == false;
        assert bufferPosition == 0;
        assert bufferLimit == 0;
        long rest = guaranteedSplitRest;
        while (true) {
            int read = source.read(buffer);
            if (read < 0) {
                // previous split end is EOF: the current split has no more records
                forceEof();
                break;
            }
            int position = findEndOfSplit(0, read);
            rest -= position < 0 ? read : position;
            if (rest < 0) {
                // previous split end exceeds the current split range: the current split has no more records
                forceEof();
                break;
            }
            if (position >= 0) {
                // found the previous end
                // reset state machine to compute end of the current split
                sm.reset();
                guaranteedSplitRest = rest;
                computeBufferRange(position, read);
                break;
            }
        }
    }

    private void computeBufferRange(int start, int end) {
        bufferPosition = start;
        bufferLimit = end;
        int length = end - start;
        if (length <= guaranteedSplitRest) {
            // the rest buffer content is in the current split range
            guaranteedSplitRest -= length;
        } else {
            // buffer range was exceeded from the current split range: we drop after the end of split
            int found = findEndOfSplit(start + (int) guaranteedSplitRest, end);
            if (found >= 0) {
                assert stateMachine.isFinished();
                bufferLimit = found;
            }
            guaranteedSplitRest = 0L;
        }
    }

    private int findEndOfSplit(int start, int limit) {
        return stateMachine.findEndOfSplit(buffer, start, limit);
    }

    private void forceEof() {
        bufferPosition = 0;
        bufferLimit = 0;
        stateMachine.finish();
    }

    @Override
    public int available() throws IOException {
        return bufferLimit - bufferPosition;
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    private static final class StateMachine {

        static final byte LF = '\n';

        private boolean sawEndOfLine;

        StateMachine() {
            reset();
        }

        void reset() {
            this.sawEndOfLine = false;
        }

        void finish() {
            this.sawEndOfLine = true;
        }

        boolean isFinished() {
            return sawEndOfLine;
        }

        int findEndOfSplit(byte[] contents, int start, int end) {
            assert isFinished() == false;
            for (int i = start; i < end; i++) {
                if (contents[i] == LF) {
                    sawEndOfLine = true;
                    return i + 1;
                }
            }
            return -1;
        }
    }
}
