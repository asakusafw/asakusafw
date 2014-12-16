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
package com.asakusafw.runtime.mapreduce.simple;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.serializer.SerializationFactory;
import org.apache.hadoop.io.serializer.Serializer;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import com.asakusafw.runtime.io.util.DataBuffer;
import com.asakusafw.runtime.io.util.DataBufferOutputStream;
import com.asakusafw.utils.io.Source;
import com.asakusafw.utils.io.Sources;

/**
 * Simple implementation of key-value pair sorter.
 * @param <K> key object type
 * @param <V> value object type
 * @since 0.7.1
 */
public class KeyValueSorter<K, V> implements Closeable {

    static final Log LOG = LogFactory.getLog(KeyValueSorter.class);

    private static final String NAME_PREFIX_BLOCK_FILE = "asakusa-sort"; //$NON-NLS-1$

    static final double BUFFER_EXPANSION_FACTOR = 1.2;

    private static final int MAX_RECORD_PER_PAGE = 1000000;

    private final KeyValuePageBuffer<K, V> pageBuffer;

    private final BlockBuffer blockBuffer;

    private final BlockStore blockStore;

    private final Class<K> keyClass;

    private final Class<V> valueClass;

    private final KeyValueSliceComparator comparator;

    private long recordCount = 0;

    /**
     * Creates a new instance.
     * @param serialization the serialization factory
     * @param keyClass the key class
     * @param valueClass the value class
     * @param comparator the shuffle sort comparator
     * @param options the sorter options
     */
    public KeyValueSorter(
            SerializationFactory serialization,
            Class<K> keyClass, Class<V> valueClass,
            RawComparator<?> comparator,
            Options options) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.pageBuffer = new KeyValuePageBuffer<K, V>(
                options.getPageSize() / 4, options.getPageSize(),
                serialization, keyClass, valueClass, comparator);
        this.blockBuffer = new BlockBuffer(0, options.getBlockSize());
        this.blockStore = new BlockStore(options.getTemporaryDirectory(), options.isCompressBlock());
        this.comparator = new KeyValueSliceComparator(comparator);
    }

    /**
     * Returns the target key class.
     * @return the key class
     */
    public Class<K> getKeyClass() {
        return keyClass;
    }

    /**
     * Returns the target value class.
     * @return the value class
     */
    public Class<V> getValueClass() {
        return valueClass;
    }

    /**
     * Resets this sorter.
     */
    void reset() {
        pageBuffer.reset();
        blockBuffer.reset();
        blockStore.reset();
    }

    /**
     * Puts key and value into this buffer.
     * @param key the key object
     * @param value the value object
     * @throws IOException if failed to put objects
     * @throws InterruptedException if interrupted while putting object
     */
    public void put(K key, V value) throws IOException, InterruptedException {
        KeyValuePageBuffer<K, V> page = pageBuffer;
        page.put(key, value);
        recordCount++;
        if (page.isFlushRequired()) {
            flushPageBuffer();
        }
    }

    /**
     * Returns the total record count.
     * @return the total record count
     */
    public long getRecordCount() {
        return recordCount;
    }

    /**
     * Returns the total size in bytes.
     * @return the total size in bytes
     */
    public long getSizeInBytes() {
        return pageBuffer.getSizeInBytes() + blockBuffer.getSizeInBytes() + blockStore.getSizeInBytes();
    }

    /**
     * Sort previously added key-value pairs.
     * @return the sorted source
     * @throws IOException if failed to sort pairs
     * @throws InterruptedException if interrupted while preparing
     */
    public Source<KeyValueSlice> sort() throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "merging records: page-buffer={0}bytes, block-buffer={1}bytes, block-files={2}bytes", //$NON-NLS-1$
                    pageBuffer.getSizeInBytes(),
                    blockBuffer.getSizeInBytes(),
                    blockStore.getSizeInBytes()));
        }
        List<Source<KeyValueSlice>> sources = new ArrayList<Source<KeyValueSlice>>();
        sources.addAll(pageBuffer.createSources());
        sources.addAll(blockBuffer.createSources());
        sources.addAll(blockStore.createSources());
        return Sources.merge(sources, comparator);
    }

    private void flushPageBuffer() throws IOException, InterruptedException {
        KeyValuePageBuffer<?, ?> page = pageBuffer;
        int count = page.getCount();
        if (count == 0) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "flushing page buffer: {0}records, {1}bytes", //$NON-NLS-1$
                    page.getCount(),
                    page.getSizeInBytes()));
        }
        KeyValueRange[] ranges = page.sort();
        byte[] bytes = page.getData();
        BlockBuffer block = blockBuffer;
        block.ensureWrite(page.getSizeInBytes());
        for (int i = 0; i < count; i++) {
            KeyValueRange range = ranges[i];
            if (block.put(bytes, range.offset, range.keyLength, range.valueLength) == false) {
                spillOut();
                block.put(bytes, range.offset, range.keyLength, range.valueLength);
            }
        }
        page.reset();
        block.pageBreak();
    }

    private void spillOut() throws IOException, InterruptedException {
        Source<KeyValueSlice> merged = Sources.merge(blockBuffer.createSources(), comparator);
        try {
            blockStore.put(merged, blockBuffer.getSizeInBytes());
        } finally {
            merged.close();
        }
        blockBuffer.reset();
    }

    @Override
    public void close() throws IOException {
        reset();
    }

    /**
     * Settings for {@link KeyValueSorter}.
     * @since 0.7.1
     */
    public static final class Options {

        private static final int DEFAULT_PAGE_SIZE = 1 * 1024 * 1024;

        private static final int DEFAULT_BLOCK_SIZE = 4 * 1024 * 1024;

        private static final int MIN_PAGE_SIZE = 64 * 1024;

        private static final int MAX_PAGE_SIZE = 128 * 1024 * 1024;

        private static final int MIN_BLOCK_SIZE = 256 * 1024;

        private static final int MAX_BLOCK_SIZE = 1 * 1024 * 1024 * 1024;

        private static final double PAGE_OVERFLOW_MARGIN = 0.25;

        private static final int DEFAULT_PAGE_PER_BLOCK = 5;

        private int pageSize = DEFAULT_PAGE_SIZE;

        private int blockSize = DEFAULT_BLOCK_SIZE;

        private File temporaryDirectory;

        private boolean compressBlock;

        /**
         * Returns in-memory sort buffer size.
         * @return the in-memory sort buffer size
         */
        public int getPageSize() {
            return pageSize;
        }

        /**
         * Returns in-memory block buffer size.
         * @return the in-memory block buffer size
         */
        public int getBlockSize() {
            return blockSize;
        }

        /**
         * Returns the temporary directory for storing spill-out block files.
         * @return the temporary directory, or {@code null} if it is not set
         */
        public File getTemporaryDirectory() {
            return temporaryDirectory;
        }

        /**
         * Returns whether spill-out block file is compressed or not.
         * @return {@code true} if compress block files, otherwise {@code false}
         */
        public boolean isCompressBlock() {
            return compressBlock;
        }

        /**
         * Sets the total buffer size.
         * @param bufferSize the total buffer size
         * @return this
         */
        public Options withBufferSize(int bufferSize) {
            //       buffer = page-buffer + page-margin + block-buffer
            //  page-margin = page-buffer * PAGE_OVERFLOW_MARGIN
            // block-buffer = page-buffer * DEFAULT_PAGE_PER_BLOCK
            // ->    buffer = page-buffer * (1 + PAGE_OVERFLOW_MARGIN + DEFAULT_PAGE_PER_BLOCK)
            int rawPage = (int) (bufferSize / (1 + PAGE_OVERFLOW_MARGIN + DEFAULT_PAGE_PER_BLOCK));
            int page = in(rawPage, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
            int block = in(bufferSize - (int) (page * (1 + PAGE_OVERFLOW_MARGIN)), MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
            return withBufferSize(page, block);
        }

        /**
         * Sets individual buffer sizes.
         * @param pageBufferSize in-memory sort buffer size
         * @param blockBufferSize in-memory block buffer size
         * @return this
         */
        public Options withBufferSize(int pageBufferSize, int blockBufferSize) {
            int page = in(pageBufferSize, MIN_PAGE_SIZE, MAX_BLOCK_SIZE);
            int block = in(blockBufferSize, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
            this.blockSize = block;
            this.pageSize = page;
            return this;
        }

        /**
         * Sets the temporary directory for the sorter.
         * @param path the temporary directory path
         * @return this
         */
        public Options withTemporaryDirectory(File path) {
            this.temporaryDirectory = path;
            return this;
        }

        /**
         * Returns whether spill-out block file is compressed or not.
         * @param enable {@code true} to enable compression
         * @return this
         */
        public Options withCompressBlock(boolean enable) {
            this.compressBlock = enable;
            return this;
        }

        private int in(int value, int min, int max) {
            assert min <= max;
            return Math.max(min, Math.min(max, value));
        }
    }

    private static final class KeyValuePageBuffer<K, V> {

        private final DataBuffer buffer;

        private final int bufferLimit;

        private KeyValueRange[] ranges = new KeyValueRange[256];

        private int rangesIndex;

        private final Serializer<K> keySerializer;

        private final Serializer<V> valueSerializer;

        private final RawComparator<?> comparator;

        public KeyValuePageBuffer(
                int initialBufferSize, int bufferLimit,
                SerializationFactory serialization,
                Class<K> keyClass, Class<V> valueClass, RawComparator<?> comparator) {
            this.buffer = new DataBuffer(initialBufferSize, BUFFER_EXPANSION_FACTOR);
            this.bufferLimit = bufferLimit;
            this.keySerializer = serialization.getSerializer(keyClass);
            this.valueSerializer = serialization.getSerializer(valueClass);
            try {
                keySerializer.open(new DataBufferOutputStream(buffer));
                valueSerializer.open(new DataBufferOutputStream(buffer));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            this.comparator = comparator;
        }

        byte[] getData() {
            return buffer.getData();
        }

        KeyValueRange[] sort() {
            Arrays.sort(ranges, 0, rangesIndex, new InPageBufferComparator(buffer.getData(), comparator));
            return ranges;
        }

        int getCount() {
            return rangesIndex;
        }

        int getSizeInBytes() {
            return buffer.getWritePosition() + getCount() * 8;
        }

        void reset() {
            buffer.reset(0, 0);
            rangesIndex = 0;
        }

        void put(K key, V value) throws IOException {
            KeyValueRange[] rs = ranges;
            int index = rangesIndex;
            if (rs.length < index + 1) {
                int newSize = (int) (index * BUFFER_EXPANSION_FACTOR) + 1;
                KeyValueRange[] newRanges = new KeyValueRange[newSize];
                System.arraycopy(rs, 0, newRanges, 0, index);
                rs = newRanges;
                ranges = rs;
            }
            KeyValueRange range = rs[index];
            if (range == null) {
                range = new KeyValueRange();
                rs[index] = range;
            }
            DataBuffer b = buffer;
            int offset = b.getWritePosition();
            keySerializer.serialize(key);
            int keyEnd = b.getWritePosition();
            valueSerializer.serialize(value);
            int valueEnd = b.getWritePosition();
            range.offset = offset;
            range.keyLength = keyEnd - offset;
            range.valueLength = valueEnd - keyEnd;
            rangesIndex++;
        }

        boolean isFlushRequired() {
            return getSizeInBytes() >= bufferLimit || getCount() >= MAX_RECORD_PER_PAGE;
        }

        List<Source<KeyValueSlice>> createSources() {
            if (getCount() == 0) {
                return Collections.emptyList();
            } else {
                return Collections.<Source<KeyValueSlice>>singletonList(new PageBufferSource(this));
            }
        }
    }

    private static final class BlockBuffer {

        private final DataBuffer buffer;

        private final int bufferLimit;

        private int[] pageLimitPositions = new int[0];

        private int pageCount = 0;

        public BlockBuffer(int initialBufferSize, int bufferLimit) {
            this.buffer = new DataBuffer(initialBufferSize, BUFFER_EXPANSION_FACTOR);
            this.bufferLimit = bufferLimit;
        }

        void ensureWrite(int pageSize) {
            int oldCapacity = buffer.getData().length;
            int blockSize = getSizeInBytes();
            if (blockSize + pageSize <= oldCapacity) {
                return;
            }
            int newCapacity = Math.min(
                    bufferLimit,
                    Math.max((int) (blockSize + pageSize * 2.5), (int) (blockSize * BUFFER_EXPANSION_FACTOR) + 1));
            if (newCapacity <= oldCapacity) {
                // may be newCapacity == bufferLimit
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "expanding block buffer: {0}->{1}bytes", //$NON-NLS-1$
                        oldCapacity, newCapacity));
            }
            buffer.ensureCapacity(newCapacity);
        }

        int getSizeInBytes() {
            return buffer.getWritePosition();
        }

        boolean put(byte[] bytes, int offset, int keyLength, int valueLength) throws IOException {
            DataBuffer b = buffer;
            int bufferSize = b.getWritePosition();
            assert bufferSize <= bufferLimit;
            int sliceSize = keyLength + valueLength;
            if (bufferSize + sliceSize + 8 > bufferLimit) {
                return false;
            }
            b.writeInt(keyLength);
            b.writeInt(valueLength);
            b.write(bytes, offset, sliceSize);
            return true;
        }

        void pageBreak() {
            int position = buffer.getWritePosition();
            if (position != 0) {
                if (pageCount >= pageLimitPositions.length) {
                    int[] newInts = new int[(int) (pageLimitPositions.length * BUFFER_EXPANSION_FACTOR) + 1];
                    System.arraycopy(pageLimitPositions, 0, newInts, 0, pageCount);
                    pageLimitPositions = newInts;
                }
                pageLimitPositions[pageCount++] = position;
            }
        }

        void reset() {
            buffer.reset(0, 0);
            pageCount = 0;
        }

        List<Source<KeyValueSlice>> createSources() {
            List<Source<KeyValueSlice>> sources = new ArrayList<Source<KeyValueSlice>>();
            byte[] bytes = buffer.getData();
            int last = 0;
            for (int i = 0, n = pageCount; i < n; i++) {
                int limit = pageLimitPositions[i];
                if (last != limit) {
                    sources.add(new PartialPageSource(bytes, last, limit));
                }
                last = limit;
            }
            int limit = getSizeInBytes();
            if (limit > last) {
                sources.add(new PartialPageSource(bytes, last, limit));
            }
            return sources;
        }
    }

    private static final class BlockStore {

        private static final int INPUT_BUFFER_SIZE = 32 * 1024;

        private static final int OUTPUT_BUFFER_SIZE = 256 * 1024;

        private final List<File> files = new ArrayList<File>();

        private final File temporaryDirectory;

        private final boolean compress;

        private long totalSize;

        public BlockStore(File temporaryDirectory, boolean compress) {
            this.temporaryDirectory = temporaryDirectory;
            this.compress = compress;
        }

        long getSizeInBytes() {
            return totalSize;
        }

        void put(Source<KeyValueSlice> source, long size) throws IOException, InterruptedException {
            long t0 = -1;
            if (LOG.isDebugEnabled()) {
                t0 = System.currentTimeMillis();
            }
            File file = createTemporaryFile();
            boolean success = false;
            try {
                DataOutputStream output = createBlockFileOutput(file);
                try {
                    while (source.next()) {
                        KeyValueSlice slice = source.get();
                        output.writeInt(slice.getKeyLength());
                        output.writeInt(slice.getValueLength());
                        output.write(slice.getBytes(), slice.getSliceOffset(), slice.getSliceLength());
                    }
                    // EOF
                    output.writeInt(-1);
                } finally {
                    output.close();
                }
                files.add(file);
                success = true;
            } finally {
                if (success == false) {
                    deleteTemporaryFile(file);
                }
            }
            if (LOG.isDebugEnabled()) {
                long t1 = System.currentTimeMillis();
                LOG.debug(MessageFormat.format(
                        "saved block file: {0} (data={1}->{2}bytes, compress={3}, elapsed={4}ms)", //$NON-NLS-1$
                        file,
                        size,
                        file.length(),
                        compress,
                        t1 - t0));
            }
            totalSize += size;
        }

        private File createTemporaryFile() throws IOException {
            return File.createTempFile(NAME_PREFIX_BLOCK_FILE, ".tmp", temporaryDirectory); //$NON-NLS-1$
        }

        void reset() {
            for (Iterator<File> iter = files.iterator(); iter.hasNext();) {
                File file = iter.next();
                deleteTemporaryFile(file);
                iter.remove();
            }
        }

        List<Source<KeyValueSlice>> createSources() throws IOException {
            List<Source<KeyValueSlice>> sources = new ArrayList<Source<KeyValueSlice>>();
            boolean succeed = false;
            try {
                for (File file : files) {
                    sources.add(new StreamSource(createBlockFileInput(file)));
                }
                succeed = true;
                return sources;
            } finally {
                if (succeed == false) {
                    for (Source<KeyValueSlice> s : sources) {
                        s.close();
                    }
                }
            }
        }

        private DataInputStream createBlockFileInput(File file) throws IOException {
            if (compress) {
                return new DataInputStream(new SnappyInputStream(new FileInputStream(file)));
            } else {
                return new DataInputStream(new BufferedInputStream(new FileInputStream(file), INPUT_BUFFER_SIZE));
            }
        }

        private DataOutputStream createBlockFileOutput(File file) throws IOException {
            if (compress) {
                return new DataOutputStream(new SnappyOutputStream(new FileOutputStream(file)));
            } else {
                return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), OUTPUT_BUFFER_SIZE));
            }
        }

        private void deleteTemporaryFile(File file) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "deleting temporary file: {0}", //$NON-NLS-1$
                        file));
            }
            if (file.delete() == false && file.exists()) {
                LOG.warn(MessageFormat.format(
                        "failed to delete sorter block file: {0}",
                        file));
            }
        }
    }

    private static final class StreamSource implements Source<KeyValueSlice> {

        private final DataInputStream input;

        private boolean prepared;

        private boolean sawEof;

        private final KeyValueSlice slice;

        public StreamSource(DataInputStream input) {
            this.input = input;
            this.slice = new KeyValueSlice();
        }

        @Override
        public boolean next() throws IOException, InterruptedException {
            if (sawEof) {
                return false;
            }
            int keyLength = input.readInt();
            if (keyLength < 0) {
                prepared = false;
                sawEof = true;
                return false;
            }
            int valueLength = input.readInt();
            int sliceLength = keyLength + valueLength;
            if (slice.getBytes().length < sliceLength) {
                slice.set(new byte[sliceLength], 0, 0, 0);
            }
            input.readFully(slice.getBytes(), 0, sliceLength);
            slice.set(0, keyLength, valueLength);
            prepared = true;
            return true;
        }

        @Override
        public KeyValueSlice get() throws IOException, InterruptedException {
            if (prepared == false) {
                throw new NoSuchElementException();
            }
            return slice;
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }

    private static final class PageBufferSource implements Source<KeyValueSlice> {

        private final KeyValueRange[] ranges;

        private final int size;

        private int index;

        private final KeyValueSlice slice;

        public PageBufferSource(KeyValuePageBuffer<?, ?> buffer) {
            this.ranges = buffer.sort();
            this.size = buffer.getCount();
            this.slice = new KeyValueSlice();
            slice.set(buffer.getData(), 0, 0, 0);
        }

        @Override
        public boolean next() {
            if (index >= size) {
                slice.set(0, 0, 0);
                return false;
            }
            KeyValueRange range = ranges[index++];
            slice.set(range.offset, range.keyLength, range.valueLength);
            return true;
        }

        @Override
        public KeyValueSlice get() {
            return slice;
        }

        @Override
        public void close() {
            index = size;
            slice.set(0, 0, 0);
            return;
        }
    }

    private static final class PartialPageSource implements Source<KeyValueSlice> {

        private final byte[] bytes;

        private int offset;

        private final int limit;

        private boolean prepared;

        private final KeyValueSlice slice;

        public PartialPageSource(byte[] bytes, int offset, int limit) {
            this.bytes = bytes;
            this.offset = offset;
            this.limit = limit;
            this.slice = new KeyValueSlice();
            this.slice.set(bytes, 0, 0, 0);
        }

        @Override
        public boolean next() throws IOException, InterruptedException {
            if (offset >= limit) {
                prepared = false;
                return false;
            } else {
                int keyLength = readInt();
                int valueLength = readInt();
                slice.set(offset, keyLength, valueLength);
                offset += keyLength + valueLength;
                prepared = true;
                return true;
            }
        }

        @Override
        public KeyValueSlice get() throws IOException, InterruptedException {
            if (prepared == false) {
                throw new NoSuchElementException();
            }
            return slice;
        }

        @Override
        public void close() throws IOException {
            prepared = false;
            offset = limit;
        }

        private int readInt() {
            int off = offset;
            byte[] b = bytes;
            int result = 0
                    | (b[off + 0] & 0xff) << 24
                    | (b[off + 1] & 0xff) << 16
                    | (b[off + 2] & 0xff) <<  8
                    | (b[off + 3] & 0xff);
            offset += 4;
            return result;
        }
    }

    private static final class KeyValueRange {

        int offset;

        int keyLength;

        int valueLength;

        KeyValueRange() {
            return;
        }
    }

    private static final class InPageBufferComparator implements Comparator<KeyValueRange> {

        private final byte[] bytes;

        private final RawComparator<?> comparator;

        public InPageBufferComparator(byte[] bytes, RawComparator<?> comparator) {
            this.bytes = bytes;
            this.comparator = comparator;
        }

        @Override
        public int compare(KeyValueRange o1, KeyValueRange o2) {
            return comparator.compare(
                    bytes, o1.offset, o1.keyLength,
                    bytes, o2.offset, o2.keyLength);
        }
    }

    private static final class KeyValueSliceComparator implements Comparator<KeyValueSlice> {

        private final RawComparator<?> comparator;

        public KeyValueSliceComparator(RawComparator<?> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(KeyValueSlice o1, KeyValueSlice o2) {
            return comparator.compare(
                    o1.getBytes(), o1.getKeyOffset(), o1.getKeyLength(),
                    o2.getBytes(), o2.getKeyOffset(), o2.getKeyLength());
        }
    }
}
