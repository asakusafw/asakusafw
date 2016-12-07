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
package com.asakusafw.runtime.flow;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.AbstractList;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.util.DataIoUtils;

/**
 * An implementation of {@link ListBuffer} which may spill contents into backing temporary files.
 * @param <E> the element type
 * @since 0.1.0
 * @version 0.9.1
 */
public class FileMapListBuffer<E extends Writable> extends AbstractList<E> implements ListBuffer<E> {

    private static final int DEFAULT_BUFFER_SIZE = 256;

    private static final int MINIMUM_BUFFER_SIZE = 32;

    private static final int DEFAULT_BUFFER_SOFT_LIMIT = 1 * 1024 * 1024;

    static final Logger LOG = LoggerFactory.getLogger(FileMapListBuffer.class);

    private final Store<E> store;

    private final E[] elements;

    private int currentPageIndex;

    private int sizeInList;

    private int elementsHighWaterMark;

    private int advanceCursorInPage;

    /**
     * Creates a new instance.
     */
    public FileMapListBuffer() {
        this(DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SOFT_LIMIT);
    }

    /**
     * Creates a new instance.
     * @param cacheSize the number of objects should be cached on Java heap
     */
    public FileMapListBuffer(int cacheSize) {
        this(cacheSize, DEFAULT_BUFFER_SOFT_LIMIT);
    }

    /**
     * Creates a new instance.
     * @param cacheSize the number of objects should be cached on Java heap
     * @param bufferSoftLimit the buffer size soft limit in bytes
     * @since 0.9.1
     */
    @SuppressWarnings("unchecked")
    public FileMapListBuffer(int cacheSize, int bufferSoftLimit) {
        this.store = new Store<>(bufferSoftLimit);
        this.elements = (E[]) new Writable[Math.max(cacheSize, MINIMUM_BUFFER_SIZE)];
        this.sizeInList = 0;
        this.currentPageIndex = 0;
        this.advanceCursorInPage = -1;
    }

    @Override
    public void begin() {
        store.reset();
        sizeInList = -1;
        currentPageIndex = 0;
        advanceCursorInPage = 0;
    }

    @Override
    public void end() {
        if (advanceCursorInPage < 0) {
            return;
        }
        if (advanceCursorInPage > 0 && currentPageIndex > 0) {
            try {
                store.putPage(currentPageIndex, elements, advanceCursorInPage);
            } catch (IOException e) {
                throw new BufferException("failed to save a page", e);
            }
        }
        sizeInList = advanceCursorInPage + currentPageIndex * elements.length;
        advanceCursorInPage = -1;
    }

    @Override
    public boolean isExpandRequired() {
        int index = advanceCursorInPage % elements.length;
        return index >= elementsHighWaterMark;
    }

    @Override
    public void expand(E value) {
        elements[elementsHighWaterMark++] = value;
    }

    @Override
    public E advance() {
        if (advanceCursorInPage == elements.length) {
            try {
                store.putPage(currentPageIndex++, elements, advanceCursorInPage);
            } catch (IOException e) {
                throw new BufferException("failed to save a page", e);
            }
            advanceCursorInPage = 0;
        }
        return elements[advanceCursorInPage++];
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= sizeInList) {
            throw new IndexOutOfBoundsException();
        }
        int windowSize = elements.length;
        int pageIndex = index / windowSize;
        int offsetInPage = index % windowSize;
        if (currentPageIndex != pageIndex) {
            int count = Math.min(sizeInList - pageIndex * windowSize, windowSize);
            try {
                store.getPage(pageIndex, elements, count);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            currentPageIndex = pageIndex;
        }
        return elements[offsetInPage];
    }

    @Override
    public int size() {
        return sizeInList;
    }

    @Override
    public void shrink() {
        try {
            store.close();
        } catch (IOException e) {
            LOG.warn("failed to shrink the backing store", e);
        }
    }

    private static class Store<T extends Writable> implements Closeable {

        private static final int[] EMPTY_INTS = new int[0];

        private static final long[] EMPTY_LONGS = new long[0];

        private final int bufferSoftLimit;

        private Path path;

        private FileChannel channel;

        private long[] offsets = EMPTY_LONGS;

        private long[] fragmentEndOffsets = EMPTY_LONGS;

        private int[] fragmentElementCounts = EMPTY_INTS;

        private int fragmentTableLimit;

        private final ResizableNioDataBuffer buffer = new ResizableNioDataBuffer();

        Store(int bufferSoftLimit) {
            this.bufferSoftLimit = bufferSoftLimit;
        }

        void reset() {
            this.fragmentTableLimit = 0;
        }

        void putPage(int index, T[] elements, int count) throws IOException {
            if (path == null) {
                path = Files.createTempFile("spill-", ".bin");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("generating list spill: {}", path);
                }
                channel = FileChannel.open(path,
                        StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE,
                        StandardOpenOption.DELETE_ON_CLOSE);
                offsets = new long[256];
            }
            if (index >= offsets.length) {
                offsets = Arrays.copyOf(offsets, offsets.length * 2);
            }
            int fragmentBegin = 0;
            long offset = index == 0 ? 0L : offsets[index - 1];
            buffer.contents.clear();
            for (int i = 0; i < count; i++) {
                if (buffer.contents.position() > bufferSoftLimit) {
                    // write fragment if buffer was exceeded
                    int fragmentEnd = i;
                    assert fragmentEnd > fragmentBegin;
                    offset = putFragment(offset, fragmentEnd - fragmentBegin, buffer.contents);
                    buffer.contents.clear();
                    fragmentBegin = fragmentEnd;
                }
                elements[i].write(buffer);
            }
            assert buffer.contents.hasRemaining();
            long end = putContents(offset, buffer.contents);
            offsets[index] = end;
        }

        private long putFragment(long begin, int elementCount, ByteBuffer contents) throws IOException {
            assert elementCount > 0;
            if (fragmentTableLimit >= fragmentEndOffsets.length) {
                int size = Math.max(fragmentEndOffsets.length * 2, 256);
                fragmentEndOffsets = Arrays.copyOf(fragmentEndOffsets, size);
                fragmentElementCounts = Arrays.copyOf(fragmentElementCounts, size);
            }
            long end = putContents(begin, contents);
            fragmentEndOffsets[fragmentTableLimit] = end;
            fragmentElementCounts[fragmentTableLimit] = elementCount;
            fragmentTableLimit++;
            return end;
        }

        private long putContents(long begin, ByteBuffer contents) throws IOException {
            contents.flip();
            if (LOG.isTraceEnabled()) {
                LOG.trace(String.format("writing fragment: %s@%,d+%,d", path, begin, contents.remaining())); //$NON-NLS-1$
            }
            long offset = begin;
            while (contents.hasRemaining()) {
                offset += channel.write(contents, offset);
            }
            return offset;
        }

        void getPage(int index, T[] elements, int count) throws IOException {
            long offset = index == 0 ? 0L : offsets[index - 1];
            long end = offsets[index];
            long length = end - offset;
            ByteBuffer buf = buffer.contents;
            if (buf.capacity() >= length) {
                readFragment(offset, end, elements, 0, count);
            } else {
                getPageFragments(offset, end, elements, count);
            }
        }

        private void getPageFragments(long begin, long end, T[] elements, int count) throws IOException {
            long fileOffset = begin;
            int arrayOffset = 0;
            int fIndex = findFragmentsIndex(begin, end);
            for (int i = fIndex, n = fragmentTableLimit; i < n; i++) {
                long fragmentEnd = fragmentEndOffsets[i];
                if (fragmentEnd >= end) {
                    break;
                }
                int arrayEnd = arrayOffset + fragmentElementCounts[i];
                // reads rest elements
                readFragment(fileOffset, fragmentEnd, elements, arrayOffset, arrayEnd);
                fileOffset = fragmentEnd;
                arrayOffset = arrayEnd;
            }
            assert fileOffset < end;
            assert arrayOffset < count;
            // reads rest elements
            readFragment(fileOffset, end, elements, arrayOffset, count);
        }

        private int findFragmentsIndex(long begin, long end) {
            int fIndex = Arrays.binarySearch(fragmentEndOffsets, 0, fragmentTableLimit, begin);
            if (fIndex == fragmentTableLimit || fIndex >= 0) {
                throw new IllegalStateException();
            }
            fIndex = -(fIndex + 1);
            assert begin < fragmentEndOffsets[fIndex] && fragmentEndOffsets[fIndex] < end;
            return fIndex;
        }

        private void readFragment(
                long fileBegin, long fileEnd,
                T[] elements, int arrayBegin, int arrayEnd) throws IOException {
            ByteBuffer buf = buffer.contents;
            int fileSize = (int) (fileEnd - fileBegin);
            buf.clear().limit(fileSize);
            if (LOG.isTraceEnabled()) {
                LOG.trace(String.format("reading fragment: %s@%,d+%,d", path, fileBegin, buf.remaining())); //$NON-NLS-1$
            }
            long offset = fileBegin;
            while (buf.hasRemaining()) {
                int read = channel.read(buf, offset);
                if (read < 0) {
                    throw new IllegalStateException();
                }
                offset += read;
            }
            buf.flip();
            for (int i = arrayBegin; i < arrayEnd; i++) {
                elements[i].readFields(buffer);
            }
        }

        @Override
        public void close() throws IOException {
            if (channel != null) {
                offsets = EMPTY_LONGS;
                fragmentEndOffsets = EMPTY_LONGS;
                fragmentElementCounts = EMPTY_INTS;
                buffer.contents = ResizableNioDataBuffer.EMPTY_BUFFER;
                channel.close(); // DELETE_ON_CLOSE
                if (Files.deleteIfExists(path) == false && Files.exists(path)) {
                    LOG.warn(MessageFormat.format(
                            "failed to delete a temporary file: {0}",
                            path));
                }
                channel = null;
                path = null;
            }
        }
    }

    private static final class ResizableNioDataBuffer implements DataInput, DataOutput {

        static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());

        static final double DEFAULT_BUFFER_EXPANSION_FACTOR = 1.5;

        static final double MINIMUM_BUFFER_EXPANSION_FACTOR = 1.1;

        static final int MINIMUM_EXPANSION_SIZE = 256;

        private final double expansionFactor;

        ByteBuffer contents = EMPTY_BUFFER;

        ResizableNioDataBuffer() {
            this(DEFAULT_BUFFER_EXPANSION_FACTOR);
        }

        ResizableNioDataBuffer(double expansionFactor) {
            this.contents = EMPTY_BUFFER;
            this.expansionFactor = Math.max(expansionFactor, MINIMUM_BUFFER_EXPANSION_FACTOR);
        }

        @Override
        public boolean readBoolean() {
            return contents.get() != 0;
        }

        @Override
        public byte readByte() {
            return contents.get();
        }

        @Override
        public int readUnsignedByte() {
            return contents.get() & 0xff;
        }

        @Override
        public short readShort() {
            return contents.getShort();
        }

        @Override
        public int readUnsignedShort() {
            return contents.getShort() & 0xffff;
        }

        @Override
        public char readChar() {
            return contents.getChar();
        }

        @Override
        public int readInt() {
            return contents.getInt();
        }

        @Override
        public long readLong() {
            return contents.getLong();
        }

        @Override
        public float readFloat() {
            return contents.getFloat();
        }

        @Override
        public double readDouble() {
            return contents.getDouble();
        }

        @Override
        public void readFully(byte[] b) {
            readFully(b, 0, b.length);
        }

        @Override
        public void readFully(byte[] b, int off, int len) {
            contents.get(b, off, len);
        }

        @Override
        public String readLine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String readUTF() {
            try {
                return DataIoUtils.readUTF(this);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public int skipBytes(int n) {
            if (n <= 0) {
                return 0;
            }
            int skip = Math.min(n, contents.remaining());
            if (skip > 0) {
                contents.position(contents.position() + skip);
            }
            return skip;
        }

        @Override
        public void write(int b) {
            ensureWrite(Byte.BYTES);
            contents.put((byte) b);
        }

        @Override
        public void writeBoolean(boolean v) {
            ensureWrite(Byte.BYTES);
            contents.put(v ? (byte) 1 : (byte) 0);
        }

        @Override
        public void writeByte(int v) {
            ensureWrite(Byte.BYTES);
            contents.put((byte) v);
        }

        @Override
        public void writeShort(int v) {
            ensureWrite(Short.BYTES);
            contents.putShort((short) v);
        }

        @Override
        public void writeChar(int v) {
            ensureWrite(Character.BYTES);
            contents.putChar((char) v);
        }

        @Override
        public void writeInt(int v) {
            ensureWrite(Integer.BYTES);
            contents.putInt(v);
        }

        @Override
        public void writeLong(long v) {
            ensureWrite(Long.BYTES);
            contents.putLong(v);
        }

        @Override
        public void writeFloat(float v) {
            ensureWrite(Float.BYTES);
            contents.putFloat(v);
        }

        @Override
        public void writeDouble(double v) {
            ensureWrite(Double.BYTES);
            contents.putDouble(v);
        }

        @Override
        public void write(byte[] b) {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            ensureWrite(len);
            contents.put(b, off, len);
        }

        @Override
        public void writeBytes(String s) {
            for (int i = 0, n = s.length(); i < n; i++) {
                writeByte(s.charAt(i));
            }
        }

        @Override
        public void writeChars(String s) {
            for (int i = 0, n = s.length(); i < n; i++) {
                writeChar(s.charAt(i));
            }
        }

        @Override
        public void writeUTF(String s) {
            try {
                DataIoUtils.writeUTF(this, s);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        private void ensureWrite(int bytes) {
            if (contents.remaining() < bytes) {
                expand(contents.limit() + bytes);
            }
        }

        private void expand(int requiredBytes) {
            ByteBuffer oldBuf = contents;
            int expansion = Math.max((int) (oldBuf.limit() * expansionFactor), MINIMUM_EXPANSION_SIZE);
            int newSize = Math.max(expansion, requiredBytes);

            ByteBuffer newBuf = ByteBuffer.allocateDirect(newSize).order(oldBuf.order());
            newBuf.clear();

            oldBuf.flip();
            newBuf.put(oldBuf);

            this.contents = newBuf;
        }
    }
}
