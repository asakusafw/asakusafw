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
import java.io.InterruptedIOException;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.util.DataBuffer;
import com.asakusafw.runtime.stage.temporary.TemporaryFileInputHelper.Result;

/**
 * Input raw data.
 * @param <T> the data model type
 * @since 0.7.0
 */
public class TemporaryFileInput<T extends Writable> implements ModelInput<T> {

    private final TemporaryFileInputHelper helper;

    private DataBuffer buffer;

    private int positionInBlock = 0;

    private int currentBlock = 0;

    private String dataTypeName;

    private boolean sawEof = false;

    /**
     * Creates a new instance.
     * @param input the input stream (must be on the head of a block)
     * @param blocks the number of blocks to read, or {@code 0} to read all pages in the stream
     */
    public TemporaryFileInput(InputStream input, int blocks) {
        this.helper = new TemporaryFileInputHelper(input, blocks);
        helper.initialize();
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
        assert buffer != null;
        int before = buffer.getReadPosition();
        model.readFields(buffer);
        int position = buffer.getReadPosition();
        if (position - before == 0) {
            // read 0-bytes entry
            int c = buffer.read();
            if (c != TemporaryFile.EMPTY_ENTRY_PADDING) {
                throw new IOException("Invalid empty entry padding");
            }
        }
        if (buffer.getReadRemaining() == 0) {
            helper.releaseBuffer(buffer);
            buffer = null;
        }
        return true;
    }

    private boolean prepareBuffer() throws IOException {
        if (buffer != null && buffer.getReadRemaining() == 0) {
            helper.releaseBuffer(buffer);
            buffer = null;
        }
        if (buffer == null) {
            try {
                Result result = helper.getNextPage();
                this.buffer = result.buffer;
                this.sawEof = result.sawEof;
                this.positionInBlock = result.positionInBlock;
                this.currentBlock = result.currentBlock;
                if (result.dataTypeName != null) {
                    this.dataTypeName = result.dataTypeName;
                }
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        }
        return sawEof == false;
    }

    @Override
    public void close() throws IOException {
        helper.close();
    }
}
