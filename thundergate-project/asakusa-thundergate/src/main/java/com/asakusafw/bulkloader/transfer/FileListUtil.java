/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.bulkloader.transfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Utilities for {@link FileList}.
 * @since 0.7.0
 */
public final class FileListUtil {

    private static final byte[] PREAMBLE = "@ASKS_FL:".getBytes(Charset.forName("UTF-8"));

    private FileListUtil() {
        return;
    }

    /**
     * Puts a preamble byte sequence into the {@link OutputStream}.
     * @param output the target stream
     * @throws IOException if failed to put a preamble bytes
     */
    public static void putPreamble(OutputStream output) throws IOException {
        output.write(PREAMBLE);
    }

    /**
     * Drops bytes until a preamble byte sequence was occurred.
     * @param input the source stream
     * @param length the max byte length before preamble bytes is occurred
     * @return the dropped bytes (excludes preamble bytes)
     * @throws IOException if failed to drop bytes
     */
    public static byte[] dropPreamble(InputStream input, int length) throws IOException {
        PreambleState state = new PreambleState(PREAMBLE);
        for (int i = 0; i < length; i++) {
            int c = input.read();
            if (c < 0) {
                break;
            }
            if (state.put(c)) {
                break;
            }
        }
        if (state.isDone() == false) {
            throw new IOException(MessageFormat.format(
                    "FileList is broken (no preamble byte sequence in first {0} bytes)",
                    length));
        }
        return state.getBytes();
    }

    private static final class PreambleState {

        private static final byte[] EMPTY = new byte[0];

        private final byte[] preamble;

        private int position = 0;

        private ByteArrayOutputStream output;

        PreambleState(byte[] preamble) {
            this.preamble = preamble;
        }

        public boolean put(int c) {
            if (position == preamble.length) {
                throw new IllegalStateException();
            }
            assert position < preamble.length;
            if (c == preamble[position]) {
                position++;
                return position == preamble.length;
            }
            if (position != 0) {
                prepare().write(preamble, 0, position);
                position = 0;
            }
            if (c == preamble[0]) {
                position++;
                return position == preamble.length;
            }
            prepare().write(c);
            return false;
        }

        private ByteArrayOutputStream prepare() {
            if (output == null) {
                output = new ByteArrayOutputStream();
            }
            return output;
        }

        public boolean isDone() {
            return position == preamble.length;
        }

        public byte[] getBytes() {
            if (output == null) {
                return EMPTY;
            }
            return output.toByteArray();
        }
    }
}
