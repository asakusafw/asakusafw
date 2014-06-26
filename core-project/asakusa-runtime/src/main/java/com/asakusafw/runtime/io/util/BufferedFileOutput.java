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
package com.asakusafw.runtime.io.util;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.Flushable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.RandomAccess;

import org.apache.hadoop.io.Text;

/**
 * An implementation of {@link DataOutput} for {@link RandomAccessFile}
 * @since 0.7.0
 */
public class BufferedFileOutput implements RandomAccess, DataOutput, Flushable, Closeable {

    private final byte[] buffer;

    private final RandomAccessFile file;

    private long physicalPointer = -1L;

    private int cursor = 0;

    private final int limit;

    /**
     * Creates a new instance.
     * @param bufferSize the buffer size
     * @param file the target file
     */
    public BufferedFileOutput(RandomAccessFile file, int bufferSize) {
        this(file, new byte[bufferSize]);
    }

    /**
     * Creates a new instance.
     * @param buffer internal buffer
     * @param file the target file
     */
    public BufferedFileOutput(RandomAccessFile file, byte[] buffer) {
        this.buffer = buffer;
        this.file = file;
        this.limit = buffer.length;
    }

    /**
     * Resets internal buffer.
     * @throws IOException if failed by I/O error
     */
    public void sync() throws IOException {
        int c = cursor;
        if (c > 0) {
            file.write(buffer, 0, c);
            cursor = 0;
            physicalPointer = -1L;
        }
    }

    /**
     * Sets cursor in the target file.
     * @param position the new position
     * @throws IOException if failed by I/O error
     */
    public void seek(long position) throws IOException {
        if (position == getPosition()) {
            return;
        }
        sync();
        file.seek(position);
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
        return physicalPointer + cursor;
    }

    /**
     * Returns the file size in bytes.
     * @return the file size
     * @throws IOException if failed by I/O error
     */
    public long getSize() throws IOException {
        return Math.max(file.length(), getPosition());
    }

    private void write0(int b) throws IOException {
        if (cursor >= limit) {
            flush();
        }
        buffer[cursor++] = (byte) b;
    }

    @Override
    public void write(int b) throws IOException {
        write0(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int last = off + len;
        int rest = len;
        while (rest > 0) {
            if (cursor >= limit) {
                flush();
            }
            int size = Math.min(rest, limit - cursor);
            if (size == 0) {
                throw new IllegalStateException();
            }
            System.arraycopy(b, last - rest, buffer, cursor, size);
            cursor += size;
            rest -= size;
        }
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        write0(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) throws IOException {
        write0((v >> Byte.SIZE * 0) & 0xff);
    }

    @Override
    public void writeShort(int v) throws IOException {
        write0((v >> Byte.SIZE * 1) & 0xff);
        write0((v >> Byte.SIZE * 0) & 0xff);
    }

    @Override
    public void writeChar(int v) throws IOException {
        write0((v >> Byte.SIZE * 1) & 0xff);
        write0((v >> Byte.SIZE * 0) & 0xff);
    }

    @Override
    public void writeInt(int v) throws IOException {
        write0((v >> Byte.SIZE * 3) & 0xff);
        write0((v >> Byte.SIZE * 2) & 0xff);
        write0((v >> Byte.SIZE * 1) & 0xff);
        write0((v >> Byte.SIZE * 0) & 0xff);
    }

    @Override
    public void writeLong(long v) throws IOException {
        writeInt((int) (v >>> Byte.SIZE * 4));
        writeInt((int) v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToRawIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToRawLongBits(v));
    }

    @Override
    public void writeBytes(String s) throws IOException {
        for (int i = 0, n = s.length(); i < n; i++) {
            writeByte(s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) throws IOException {
        for (int i = 0, n = s.length(); i < n; i++) {
            writeChar(s.charAt(i));
        }
    }

    @Override
    public void writeUTF(String s) throws IOException {
        // TODO use modified UTF-8
        Text.writeString(this, s);
    }

    @Override
    public void flush() throws IOException {
        int c = cursor;
        if (c > 0) {
            file.write(buffer, 0, c);
            if (physicalPointer >= 0L) {
                physicalPointer += cursor;
            }
            cursor = 0;
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
