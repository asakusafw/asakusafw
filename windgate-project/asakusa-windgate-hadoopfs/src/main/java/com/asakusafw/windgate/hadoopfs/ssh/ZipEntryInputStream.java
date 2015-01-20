/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.windgate.hadoopfs.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * An {@link InputStream} for single zip entry in {@link ZipInputStream}.
 */
public class ZipEntryInputStream extends InputStream {

    private final ZipInputStream zipped;

    private boolean closed = false;

    /**
     * Creates a new instance.
     * @param zipped target {@link ZipInputStream}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ZipEntryInputStream(ZipInputStream zipped) {
        if (zipped == null) {
            throw new IllegalArgumentException("zipped must not be null"); //$NON-NLS-1$
        }
        this.zipped = zipped;
    }

    @Override
    public void close() throws IOException {
        if (closed == false) {
            zipped.closeEntry();
        }
        closed = true;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return zipped.read(b);
    }

    @Override
    public int read() throws IOException {
        return zipped.read();
    }

    @Override
    public int available() throws IOException {
        return zipped.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return zipped.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return zipped.skip(n);
    }

    @Override
    public boolean markSupported() {
        return zipped.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        zipped.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        zipped.reset();
    }
}
