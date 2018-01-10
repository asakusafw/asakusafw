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
package com.asakusafw.runtime.io.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * A wrapper of {@link ZipOutputStream} for writing single ZIP entry.
 */
public class ZipEntryOutputStream extends OutputStream {

    private final ZipOutputStream zipped;

    /**
     * Creates a new instance.
     * @param zipped the target {@link ZipOutputStream}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ZipEntryOutputStream(ZipOutputStream zipped) {
        if (zipped == null) {
            throw new IllegalArgumentException("zipped must not be null"); //$NON-NLS-1$
        }
        this.zipped = zipped;
    }

    @Override
    public void close() throws IOException {
        zipped.closeEntry();
    }

    @Override
    public void write(byte[] b) throws IOException {
        zipped.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        zipped.write(b);
    }

    @Override
    public void flush() throws IOException {
        zipped.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        zipped.write(b, off, len);
    }
}
