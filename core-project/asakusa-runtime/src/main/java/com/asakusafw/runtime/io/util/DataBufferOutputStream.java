/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
/**
 *
 */
package com.asakusafw.runtime.io.util;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Accesses {@link DataBuffer} via {@link OutputStream} interface.
 * @since 0.7.1
 */
public class DataBufferOutputStream extends OutputStream implements DataOutput {

    private final DataBuffer dataBuffer;

    /**
     * Creates a new instance.
     * @param dataBuffer the internal data buffer
     */
    public DataBufferOutputStream(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    @Override
    public void write(int b) throws IOException {
        dataBuffer.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        dataBuffer.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        dataBuffer.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        dataBuffer.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        dataBuffer.writeByte(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        dataBuffer.writeBytes(s);
    }

    @Override
    public void writeChar(int v) throws IOException {
        dataBuffer.writeChar(v);
    }

    @Override
    public void writeChars(String s) throws IOException {
        dataBuffer.writeChars(s);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        dataBuffer.writeDouble(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        dataBuffer.writeFloat(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        dataBuffer.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        dataBuffer.writeLong(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        dataBuffer.writeShort(v);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        dataBuffer.writeUTF(s);
    }
}
