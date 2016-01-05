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
package com.asakusafw.runtime.io.util;

import java.io.Closeable;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.RandomAccess;

/**
 * An implementation of {@link DataInput} for {@link RandomAccessFile}.
 * @since 0.7.0
 */
public class BufferedFileInput implements RandomAccess, DataInput, Closeable {

    private final byte[] buffuer;

    private final RandomAccessFile file;

    private long physicalPointer = -1L;

    private long physicalSize = -1L;

    private int offset = 0;

    private int limit = 0;

    /**
     * Creates a new instance.
     * @param bufferSize the buffer size
     * @param file the target file
     */
    public BufferedFileInput(RandomAccessFile file, int bufferSize) {
        this(file, new byte[bufferSize]);
    }

    /**
     * Creates a new instance.
     * @param buffer internal buffer
     * @param file the target file
     */
    public BufferedFileInput(RandomAccessFile file, byte[] buffer) {
        this.buffuer = buffer;
        this.file = file;
    }

    /**
     * Resets internal buffer.
     * @throws IOException if failed by I/O error
     */
    public void sync() throws IOException {
        physicalPointer = -1L;
        offset = 0;
        limit = 0;
    }

    /**
     * Sets cursor in the target file.
     * @param position the new position
     * @throws IOException if failed by I/O error
     */
    public void seek(long position) throws IOException {
        long begin = getPosition() - offset;
        long end = begin + limit;
        if (0 <= begin && begin <= position && position <= end) {
            offset = (int) (position - begin);
        } else {
            file.seek(position);
            physicalPointer = Math.min(position, getSize());
            offset = 0;
            limit = 0;
        }
    }

    /**
     * Returns the current file position in bytes.
     * @return the current position
     * @throws IOException if failed by I/O error
     */
    public long getPosition() throws IOException {
        if (physicalPointer < 0L) {
            physicalPointer = file.getFilePointer();
        }
        return physicalPointer - (limit - offset);
    }

    /**
     * Returns the file size in bytes.
     * @return the file size
     * @throws IOException if failed by I/O error
     */
    public long getSize() throws IOException {
        if (physicalSize < 0L) {
            physicalSize = file.length();
        }
        return physicalSize;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        int last = off + len;
        int rest = len;
        while (rest > 0) {
            int available = prepare();
            if (available == 0) {
                throw new EOFException();
            }
            int size = Math.min(rest, available);
            System.arraycopy(buffuer, offset, b, last - rest, size);
            offset += size;
            rest -= size;
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int result = 0;
        if (offset < limit) {
            result = Math.min(n, limit - offset);
            offset += result;
        }
        if (result < n) {
            int skip = file.skipBytes(n - result);
            if (skip > 0 && physicalPointer >= 0) {
                physicalPointer += skip;
            }
            return result + skip;
        } else {
            return result;
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        return read() != 0;
    }

    @Override
    public short readShort() throws IOException {
        byte d0 = read();
        byte d1 = read();
        return (short) (0
                | (d0 << Byte.SIZE * 1) & (0xff << Byte.SIZE * 1)
                | (d1 << Byte.SIZE * 0) & (0xff << Byte.SIZE * 0)
                );
    }

    @Override
    public byte readByte() throws IOException {
        return read();
    }

    @Override
    public int readInt() throws IOException {
        byte d3 = read();
        byte d2 = read();
        byte d1 = read();
        byte d0 = read();
        return 0
                | (d3 << Byte.SIZE * 3) & (0xff << Byte.SIZE * 3)
                | (d2 << Byte.SIZE * 2) & (0xff << Byte.SIZE * 2)
                | (d1 << Byte.SIZE * 1) & (0xff << Byte.SIZE * 1)
                | (d0 << Byte.SIZE * 0) & (0xff << Byte.SIZE * 0)
                ;
    }

    @Override
    public long readLong() throws IOException {
        int i1 = readInt();
        int i0 = readInt();
        return ((long) i1 << Integer.SIZE) | (i0 & 0xffffffffL);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return read() & 0xff;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    @Override
    public char readChar() throws IOException {
        return (char) (readShort() & 0xffff);
    }

    @Override
    public String readLine() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTF() throws IOException {
        return DataIoUtils.readUTF(this);
    }

    private byte read() throws IOException {
        int rest = prepare();
        if (rest < 1) {
            throw new EOFException();
        }
        return buffuer[offset++];
    }

    private int prepare() throws IOException {
        if (offset >= limit) {
            offset = 0;
            limit = 0;
            if (physicalPointer < 0) {
                physicalPointer = file.getFilePointer();
            }
            int read = file.read(buffuer);
            if (read > 0) {
                limit = read;
                physicalPointer += read;
            }
        }
        return limit - offset;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
