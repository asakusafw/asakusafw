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
package com.asakusafw.windgate.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} with counting bytes to be written.
 * @since 0.2.4
 */
public final class CountingOutputStream extends OutputStream {

    private final OutputStream stream;

    private long count;

    /**
     * Creates a new instance.
     * @param stream target stream to be measured
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CountingOutputStream(OutputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        this.stream = stream;
    }

    /**
     * Returns the count since have read bytes.
     * @return the count
     */
    public long getCount() {
        return count;
    }

    @Override
    public void write(int b) throws IOException {
        count += 1;
        stream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        count += b.length;
        stream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        count += len;
        stream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
