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
/**
 *
 */
package com.asakusafw.runtime.io.util;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

/**
 * Accesses {@link DataBuffer} via {@link InputStream} interface.
 * @since 0.7.1
 */
public class DataBufferInputStream extends InputStream implements DataInput {

    private final DataBuffer dataBuffer;

    /**
     * Creates a new instance.
     * @param dataBuffer the internal data buffer
     */
    public DataBufferInputStream(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    @Override
    public int available() throws IOException {
        return dataBuffer.getReadRemaining();
    }

    @Override
    public int read() throws IOException {
        return dataBuffer.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len <= 0) {
            return 0;
        }
        int size = Math.min(len, dataBuffer.getReadRemaining());
        if (size <= 0) {
            return -1;
        }
        dataBuffer.readFully(b, off, size);
        return size;
    }

    @Override
    public long skip(long n) throws IOException {
        return dataBuffer.skipBytes((int) n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return dataBuffer.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return dataBuffer.readByte();
    }

    @Override
    public char readChar() throws IOException {
        return dataBuffer.readChar();
    }

    @Override
    public double readDouble() throws IOException {
        return dataBuffer.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return dataBuffer.readFloat();
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        dataBuffer.readFully(b, off, len);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        dataBuffer.readFully(b);
    }

    @Override
    public int readInt() throws IOException {
        return dataBuffer.readInt();
    }

    @Override
    public String readLine() throws IOException {
        return dataBuffer.readLine();
    }

    @Override
    public long readLong() throws IOException {
        return dataBuffer.readLong();
    }

    @Override
    public short readShort() throws IOException {
        return dataBuffer.readShort();
    }

    @Override
    public String readUTF() throws IOException {
        return dataBuffer.readUTF();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return dataBuffer.readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return dataBuffer.readUnsignedShort();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return dataBuffer.skipBytes(n);
    }
}
