/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Writable;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.io.ModelOutput;

/**
 * Output raw data.
 * @param <T> the data model type
 * @since 0.7.0
 */
public class TemporaryFileOutput<T extends Writable> implements ModelOutput<T> {

    private static final byte[] ZEROS = new byte[64 * 1024];

    private final OutputStream output;

    private final String dataTypeName;

    private final DataOutputBuffer buffer;

    private final int pageBreakThreashold;

    private int positionInBlock = 0;

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
        this.output = output;
        this.dataTypeName = dateTypeName;
        this.buffer = new DataOutputBuffer(initialBufferSize);
        this.pageBreakThreashold = pageBreakThreashold;
    }

    @Override
    public void write(T model) throws IOException {
        model.write(buffer);
        if (buffer.getLength() >= pageBreakThreashold) {
            flush();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            output.close();
        }
    }

    private void flush() throws IOException {
        if (positionInBlock == 0) {
            positionInBlock += TemporaryFile.writeBlockHeader(output);
            positionInBlock += TemporaryFile.writeString(output, dataTypeName);
        }
        int length = buffer.getLength();
        if (length <= 0) {
            return;
        }
        byte[] buf = TemporaryFile.getInstantBuffer(Snappy.maxCompressedLength(length));
        int compressed = Snappy.compress(buffer.getData(), 0, length, buf, 0);
        writeContentPage(buf, compressed);
        buffer.reset();
    }

    private void writeContentPage(byte[] contents, int length) throws IOException {
        if (TemporaryFile.canWritePage(positionInBlock, length) == false) {
            if (TemporaryFile.canWritePage(0, length) == false) {
                throw new IOException(MessageFormat.format(
                        "Page size is too large: {1} (> {0})",
                        TemporaryFile.BLOCK_SIZE,
                        length));
            }
            writeEndOfPage();
            positionInBlock = 0;
            positionInBlock += TemporaryFile.writeBlockHeader(output);
            positionInBlock += TemporaryFile.writeString(output, dataTypeName);
        }
        TemporaryFile.writeContentPageMark(output, length);
        output.write(contents, 0, length);
        positionInBlock += TemporaryFile.PAGE_HEADER_SIZE + length;
    }

    private void writeEndOfPage() throws IOException {
        TemporaryFile.writeEndOfBlockMark(output);
        positionInBlock += TemporaryFile.PAGE_HEADER_SIZE;
        int rest = TemporaryFile.BLOCK_SIZE - positionInBlock;
        assert rest >= 0;
        byte[] zeros = ZEROS;
        while (rest > 0) {
            int size = Math.min(rest, zeros.length);
            output.write(zeros, 0, size);
            rest -= size;
        }
        positionInBlock = TemporaryFile.BLOCK_SIZE;
    }
}
