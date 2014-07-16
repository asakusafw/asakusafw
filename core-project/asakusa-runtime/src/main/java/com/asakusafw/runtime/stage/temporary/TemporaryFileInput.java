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
import java.io.InputStream;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Writable;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.io.ModelInput;

/**
 * Input raw data.
 * @param <T> the data model type
 * @since 0.7.0
 */
public class TemporaryFileInput<T extends Writable> implements ModelInput<T> {

    private final InputStream input;

    private final DataInputBuffer buffer = new DataInputBuffer();

    private int positionInBlock = 0;

    private int currentBlock = 0;

    private int blockRest;

    private String dataTypeName;

    private boolean sawEof = false;

    /**
     * Creates a new instance.
     * @param input the input stream (must be on the head of a block)
     * @param blocks the number of blocks to read, or {@code 0} to read all pages in the stream
     */
    public TemporaryFileInput(InputStream input, int blocks) {
        this.input = input;
        this.blockRest = Math.max(blocks - 1, -1);
    }

    /**
     * Returns the target data type name.
     * @return the data type name
     * @throws IOException if failed to extract the data type name
     */
    public String getDataTypeName() throws IOException {
        prepareBuffer();
        return dataTypeName;
    }

    /**
     * Returns the current block number ({@code 0..}).
     * @return the current block number
     */
    public int getCurrentBlock() {
        return currentBlock;
    }

    /**
     * Returns the byte position in current block.
     * @return the position in block
     */
    public int getPositionInBlock() {
        return positionInBlock;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        if (prepareBuffer() == false) {
            return false;
        }
        model.readFields(buffer);
        return true;
    }

    private boolean prepareBuffer() throws IOException {
        int length = buffer.getLength();
        if (length == 0) {
            return readPage();
        }
        int position = buffer.getPosition();
        if (position < length) {
            return true;
        }
        return readPage();
    }

    private boolean readPage() throws IOException {
        if (sawEof) {
            return false;
        }
        if (positionInBlock == 0) {
            StringBuilder buf = new StringBuilder();
            int headSize = TemporaryFile.readBlockHeader(input);
            if (headSize < 0) {
                sawEof = true;
                return false;
            }
            int size = TemporaryFile.readString(input, buf);
            if (size < 0) {
                sawEof = true;
                return false;
            }
            positionInBlock += size;
            this.dataTypeName = buf.toString();
        }
        int value = TemporaryFile.readPageHeader(input);
        positionInBlock += TemporaryFile.PAGE_HEADER_SIZE;
        if (value == TemporaryFile.PAGE_HEADER_EOF) {
            sawEof = true;
            return false;
        }
        if (value == TemporaryFile.PAGE_HEADER_EOB) {
            if (blockRest == 0) {
                return false;
            }
            IOUtils.skipFully(input, TemporaryFile.BLOCK_SIZE - positionInBlock);
            positionInBlock = 0;
            currentBlock++;
            if (blockRest > 0) {
                blockRest--;
            }
            return readPage();
        }
        byte[] b = readFully(value);
        fillBuffer(b, value);
        return true;
    }

    private byte[] readFully(int length) throws IOException {
        byte[] b = TemporaryFile.getInstantBuffer(length);
        IOUtils.readFully(input, b, 0, length);
        positionInBlock += length;
        return b;
    }

    private void fillBuffer(byte[] bytes, int length) throws IOException {
        int rawLength = Snappy.uncompressedLength(bytes, 0, length);
        byte[] data = buffer.getData();
        if (data.length < rawLength) {
            data = new byte[(int) (rawLength * 1.2)];
        }
        Snappy.uncompress(bytes, 0, length, data, 0);
        buffer.reset(data, rawLength);
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
