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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.io.Text;

/**
 * A data buffer with {@link DataInput} and {@link DataOutput} interfaces.
 * @since 0.7.0
 */
public class DataBuffer implements DataInput, DataOutput {

    private static final byte[] EMPTY = new byte[0];

    private static final double BUFFER_EXPANSION_FACTOR = 1.5;

    private static final int MINIMUM_EXPANSION_SIZE = 256;

    private byte[] buffer;

    private int readCursor;

    private int writeCursor;

    /**
     * Creates a new instance with empty buffer.
     */
    public DataBuffer() {
        this(0);
    }

    /**
     * Creates a new instance with empty buffer.
     * @param initialCapacity the initial buffer capacity in bytes
     */
    public DataBuffer(int initialCapacity) {
        this.buffer = initialCapacity == 0 ? EMPTY : new byte[initialCapacity];
    }

    /**
     * Resets data in this buffer.
     * @param bytes the data
     * @param offset the offset in bytes
     * @param length the length in bytes
     */
    public void reset(byte[] bytes, int offset, int length) {
        this.buffer = bytes;
        reset(offset, length);
    }

    /**
     * Resets cursors in this buffer.
     * @param offset the offset in bytes
     * @param length the length in bytes
     */
    public void reset(int offset, int length) {
        if (offset < 0 || offset > buffer.length) {
            throw new IllegalArgumentException();
        }
        if (length < 0 || offset + length > buffer.length) {
            throw new IllegalArgumentException();
        }
        this.readCursor = offset;
        this.writeCursor = offset + length;
    }

    /**
     * Returns the data bytes.
     * @return the data bytes
     * @see #getReadPosition()
     * @see #getReadRemaining()
     */
    public byte[] getData() {
        return buffer;
    }

    /**
     * Returns the reading position in this buffer.
     * @return the reading position in bytes
     */
    public int getReadPosition() {
        return readCursor;
    }

    /**
     * Returns the reading limit position in this buffer.
     * @return the reading limit position in bytes
     */
    public int getReadLimit() {
        return writeCursor;
    }

    /**
     * Returns the next write position in this buffer.
     * @return the next write position in bytes
     */
    public int getWritePosition() {
        return writeCursor;
    }

    /**
     * Returns the data length in this buffer.
     * @return the data length in bytes
     */
    public int getReadRemaining() {
        return writeCursor - readCursor;
    }

    /**
     * Ensures capacity of this buffer.
     * @param newSize the required capacity in bytes
     */
    public void ensureCapacity(int newSize) {
        byte[] newBuffer = new byte[newSize];
        if (writeCursor != 0) {
            System.arraycopy(buffer, 0, newBuffer, 0, writeCursor);
        }
        buffer = newBuffer;
    }

    /**
     * Reads a byte value.
     * @return an unsigned byte value, or {@code -1} to no remained values
     * @throws IOException if failed to read a value
     */
    public int read() throws IOException {
        if (getReadRemaining() == 0) {
            return -1;
        }
        int offset = prepareRead(1);
        byte result = buffer[offset];
        return result & 0xff;
    }

    @Override
    public boolean readBoolean() throws IOException {
        byte v = readByte();
        return v != 0;
    }

    @Override
    public byte readByte() throws IOException {
        int offset = prepareRead(1);
        byte result = buffer[offset];
        return result;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        byte v = readByte();
        return v & 0xff;
    }

    @Override
    public short readShort() throws IOException {
        int offset = prepareRead(2);
        byte[] b = buffer;
        int result = 0
                | (b[offset + 0] & 0xff) << 8
                | (b[offset + 1] & 0xff);
        return (short) result;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        short v = readShort();
        return v & 0xffff;
    }

    @Override
    public char readChar() throws IOException {
        return (char) readShort();
    }

    @Override
    public int readInt() throws IOException {
        int offset = prepareRead(4);
        byte[] b = buffer;
        int result = 0
                | (b[offset + 0] & 0xff) << 24
                | (b[offset + 1] & 0xff) << 16
                | (b[offset + 2] & 0xff) <<  8
                | (b[offset + 3] & 0xff);
        return result;
    }

    @Override
    public long readLong() throws IOException {
        int offset = prepareRead(8);
        byte[] b = buffer;
        long result = 0L
                | (b[offset + 0] & 0xffL) << 56
                | (b[offset + 1] & 0xffL) << 48
                | (b[offset + 2] & 0xffL) << 40
                | (b[offset + 3] & 0xffL) << 32
                | (b[offset + 4] & 0xffL) << 24
                | (b[offset + 5] & 0xffL) << 16
                | (b[offset + 6] & 0xffL) <<  8
                | (b[offset + 7] & 0xffL);
        return result;
    }

    @Override
    public float readFloat() throws IOException {
        int v = readInt();
        return Float.intBitsToFloat(v);
    }

    @Override
    public double readDouble() throws IOException {
        long v = readLong();
        return Double.longBitsToDouble(v);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        int offset = prepareRead(len);
        System.arraycopy(buffer, offset, b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        int skip = Math.min(n, getReadRemaining());
        assert skip >= 0;
        readCursor += skip;
        return skip;
    }

    @Override
    public String readLine() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTF() throws IOException {
        // TODO use modified UTF-8
        return Text.readString(this);
    }

    private int prepareRead(int length) throws EOFException {
        if (getReadRemaining() < length) {
            throw new EOFException();
        }
        int offset = readCursor;
        readCursor = offset + length;
        return offset;
    }

    @Override
    public void write(int b) throws IOException {
        int offset = prepareWrite(1);
        buffer[offset] = (byte) b;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        int offset = prepareWrite(len);
        System.arraycopy(b, off, buffer, offset, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) throws IOException {
        write(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        int offset = prepareWrite(2);
        byte[] b = buffer;
        b[offset + 0] = (byte) (v >> 8);
        b[offset + 1] = (byte) v;
    }

    @Override
    public void writeChar(int v) throws IOException {
        writeShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        int offset = prepareWrite(4);
        byte[] b = buffer;
        b[offset + 0] = (byte) (v >> 24);
        b[offset + 1] = (byte) (v >> 16);
        b[offset + 2] = (byte) (v >>  8);
        b[offset + 3] = (byte) v;
    }

    @Override
    public void writeLong(long v) throws IOException {
        int offset = prepareWrite(8);
        byte[] b = buffer;
        b[offset + 0] = (byte) (v >> 56);
        b[offset + 1] = (byte) (v >> 48);
        b[offset + 2] = (byte) (v >> 40);
        b[offset + 3] = (byte) (v >> 32);
        b[offset + 4] = (byte) (v >> 24);
        b[offset + 5] = (byte) (v >> 16);
        b[offset + 6] = (byte) (v >>  8);
        b[offset + 7] = (byte) v;
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

    /**
     * Writes data from {@link DataInput}.
     * @param in the source input
     * @param len data size in bytes
     * @throws IOException if failed to read {@link DataInput}
     */
    public void write(DataInput in, int len) throws IOException {
        if (len == 0) {
            return;
        }
        int offset = prepareWrite(len);
        byte[] b = buffer;
        in.readFully(b, offset, len);
    }

    private int prepareWrite(int length) {
        int offset = writeCursor;
        if (buffer.length < offset + length) {
            int newSize = Math.max(
                    Math.max(offset + length, (int) (buffer.length * BUFFER_EXPANSION_FACTOR)) + 1,
                    MINIMUM_EXPANSION_SIZE);
            ensureCapacity(newSize);
        }
        writeCursor += length;
        return offset;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}(read={1}, write={2})",
                getClass().getSimpleName(),
                getReadPosition(),
                getWritePosition());
    }
}
