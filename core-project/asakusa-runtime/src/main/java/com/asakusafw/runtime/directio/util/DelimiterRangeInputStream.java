/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} for fragments with delimiter.
 * @since 0.2.5
 */
public class DelimiterRangeInputStream extends InputStream {

    private final InputStream origin;

    private final char delimiter;

    private long remaining;

    private boolean endOfRange;

    private final byte[] buffer;

    private int bufferOffset;

    private int bufferLimit;

    /**
     * Creates a new instance.
     * @param stream original input
     * @param delimiter delimiter character
     * @param length soft limit in bytes
     * @param skipFirst whether skip until first delimiter
     * @throws IOException if failed to create instance by I/O error
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DelimiterRangeInputStream(
            InputStream stream,
            char delimiter,
            long length,
            boolean skipFirst) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        this.origin = stream;
        this.delimiter = delimiter;
        this.remaining = length;
        this.endOfRange = false;
        this.buffer = new byte[1024];
        this.bufferOffset = 0;
        this.bufferLimit = 0;
        if (remaining == 0) {
            endOfRange = true;
        } else if (skipFirst) {
            discardHead();
        }
    }

    @Override
    public int read() throws IOException {
        if (isBufferRemaining()) {
            return buffer[bufferOffset++];
        }
        if (endOfRange) {
            return -1;
        }
        if (isSoftLimitExceeded()) {
            int c = origin.read();
            if (c < 0) {
                endOfRange = true;
                return -1;
            }
            if (isDelimiter(c)) {
                endOfRange = true;
            }
            return c;
        } else {
            int c = origin.read();
            if (c < 0) {
                endOfRange = true;
                return -1;
            }
            remaining -= 1;
            return c;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bufferedSize = fillFromBuffer(b, off, len);
        if (bufferedSize > 0) {
            return bufferedSize;
        }
        if (endOfRange) {
            return -1;
        }
        if (isSoftLimitExceeded()) {
            int read = origin.read(b, off, len);
            if (read < 0) {
                endOfRange = true;
                return -1;
            }
            int index = findDelimiter(b, off, read);
            if (index < 0) {
                return read;
            } else {
                endOfRange = true;
                return index - off + 1;
            }
        } else {
            assert remaining > 0;
            int rest = (int) Math.min(len, remaining);
            int read = origin.read(b, off, rest);
            if (read < 0) {
                endOfRange = true;
                return -1;
            }
            remaining -= read;
            return read;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        if (isBufferRemaining()) {
            long bufferRest = bufferLimit - bufferOffset;
            long skipped = Math.min(n, bufferRest);
            bufferOffset += skipped;
            return skipped;
        }
        if (endOfRange) {
            return 0;
        }
        if (isSoftLimitExceeded()) {
            assert isBufferRemaining() == false;
            int read = read(buffer);
            if (read < 0) {
                return 0;
            }
            return read;
        } else {
            assert remaining > 0;
            long skipped = origin.skip(Math.min(n, remaining));
            remaining -= skipped;
            return skipped;
        }
    }

    @Override
    public int available() throws IOException {
        return origin.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        origin.close();
    }

    private void discardHead() throws IOException {
        while (remaining > 0) {
            int limit = (int) Math.min(buffer.length, remaining);
            int read = origin.read(buffer, 0, limit);
            if (read < 0) {
                endOfRange = true;
                break;
            }
            remaining -= read;
            int index = findDelimiter(buffer, 0, read);
            if (index >= 0) {
                this.bufferOffset = index + 1;
                this.bufferLimit = read;
                return;
            }
        }
        // delimiter not found
        endOfRange = true;
    }

    private int fillFromBuffer(byte[] b, int off, int len) {
        assert b != null;
        if (isBufferRemaining()) {
            int bufferLen = bufferLimit - bufferOffset;
            assert bufferLen > 0;
            int size = Math.min(len, bufferLen);
            System.arraycopy(buffer, bufferOffset, b, off, size);
            bufferOffset += size;
            return size;
        }
        return 0;
    }

    private int findDelimiter(byte[] bytes, int offset, int length) {
        assert bytes != null;
        assert length >= 0;
        for (int i = offset, n = offset + length; i < n; i++) {
            if (isDelimiter(bytes[i])) {
                return i;
            }
        }
        return -1;
    }

    private boolean isBufferRemaining() {
        return bufferOffset < bufferLimit;
    }

    private boolean isSoftLimitExceeded() {
        return remaining == 0;
    }

    private boolean isDelimiter(int c) {
        return c == delimiter;
    }
}
