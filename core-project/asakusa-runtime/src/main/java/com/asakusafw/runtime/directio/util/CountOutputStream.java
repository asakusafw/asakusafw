/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.io.OutputStream;

import com.asakusafw.runtime.directio.Counter;

/**
 * {@link OutputStream} with counting bytes to be written.
 * @since 0.2.5
 */
public class CountOutputStream extends OutputStream {

    private final OutputStream stream;

    private final Counter counter;

    /**
     * Creates a new instance.
     * @param stream target stream to be measured
     * @param counter target bytes counter
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CountOutputStream(OutputStream stream, Counter counter) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        if (counter == null) {
            throw new IllegalArgumentException("counter must not be null"); //$NON-NLS-1$
        }
        this.stream = stream;
        this.counter = counter;
    }

    /**
     * Returns the count since have read bytes.
     * @return the count
     */
    public long getCount() {
        return counter.get();
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
        counter.add(1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        stream.write(b);
        counter.add(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
        counter.add(len);
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
