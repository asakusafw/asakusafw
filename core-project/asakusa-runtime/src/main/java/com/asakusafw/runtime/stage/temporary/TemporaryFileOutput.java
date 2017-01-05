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
package com.asakusafw.runtime.stage.temporary;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.util.DataBuffer;

/**
 * Output raw data.
 * @param <T> the data model type
 * @since 0.7.0
 */
public class TemporaryFileOutput<T extends Writable> implements ModelOutput<T> {

    private final TemporaryFileOutputHelper helper;

    private DataBuffer buffer;

    private final int pageBreakThreashold;

    /**
     * Creates a new instance.
     * @param output target output stream
     * @param dateTypeName the data type name
     * @param initialBufferSize the initial page buffer size (in bytes)
     * @param pageBreakThreashold the page size hint (in bytes)
     */
    public TemporaryFileOutput(
            OutputStream output,
            String dateTypeName,
            int initialBufferSize,
            int pageBreakThreashold) {
        this.helper = new TemporaryFileOutputHelper(output, dateTypeName);
        this.helper.initialize(initialBufferSize);
        this.pageBreakThreashold = pageBreakThreashold;
    }

    @Override
    public void write(T model) throws IOException {
        prepareBuffer();
        int before = buffer.getWritePosition();
        model.write(buffer);
        int length = buffer.getWritePosition();
        if (length - before == 0) {
            // 0-bytes entry
            buffer.write(TemporaryFile.EMPTY_ENTRY_PADDING);
        }
        if (length >= pageBreakThreashold) {
            flush();
        }
    }

    private void prepareBuffer() throws IOException {
        if (buffer == null) {
            try {
                buffer = helper.acquireBuffer();
                buffer.reset(0, 0);
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        }
    }

    private void flush() throws IOException {
        prepareBuffer();
        try {
            helper.putNextPage(buffer);
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException().initCause(e);
        }
        buffer = null;
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            helper.close();
        }
    }
}
