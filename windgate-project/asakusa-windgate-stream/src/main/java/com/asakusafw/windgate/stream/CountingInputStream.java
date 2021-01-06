/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.windgate.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} with counting bytes to be read.
 * @since 0.2.4
 */
public final class CountingInputStream extends InputStream {

    private final InputStream target;

    private long count;

    /**
     * Creates a new instance.
     * @param target target stream to be measured
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CountingInputStream(InputStream target) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        this.target = target;
    }

    /**
     * Returns the count since have read bytes.
     * @return the count
     */
    public long getCount() {
        return count;
    }

    @Override
    public int read() throws IOException {
        int read = target.read();
        if (read > 0) {
            count += 1;
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = target.read(b);
        if (read > 0) {
            count += read;
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = target.read(b, off, len);
        if (read > 0) {
            count += read;
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long read = target.skip(n);
        return read;
    }

    @Override
    public int available() throws IOException {
        return target.available();
    }

    @Override
    public void close() throws IOException {
        target.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        target.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        target.reset();
    }

    @Override
    public boolean markSupported() {
        return target.markSupported();
    }
}
