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
package com.asakusafw.runtime.smalljob;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.serializer.SerializationFactory;
import org.apache.hadoop.io.serializer.Serializer;

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

    private static final int MAX_RECORD_PER_PAGE = 100000;

    private final KeyValuePageBuffer<K, V> pageBuffer;

    private final DataBuffer partialPageBuffer = new DataBuffer(0, 1.2);

    private long recordCount;

    private final int pageLimit;

    private final int bufferLimit;

    private int[] partialPageLimitPositions = new int[0];

    private int partialPageCount;

    private final List<File> sortedBlocks = new ArrayList<File>();

    private final RawComparator<?> comparator;

    private Integer[] indices = new Integer[0];

    private final Class<K> keyClass;

    private final Class<V> valueClass;

    /**
     * Creates a new instance.
     * @param serialization the serialization factory
     * @param keyClass the key class
     * @param valueClass the value class
     * @param comparator the shuffle sort comparator
     * @param pageLimit the in-place sort buffer limit
     * @param bufferLimit the in-memory buffer limit
     */
    public KeyValueSorter(
            SerializationFactory serialization,
            Class<K> keyClass, Class<V> valueClass,
            RawComparator<?> comparator,
            int pageLimit, int bufferLimit) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.pageLimit = Math.max(64 * 1024, pageLimit);
        this.bufferLimit = Math.max(bufferLimit, this.pageLimit * 4);
        this.pageBuffer = new KeyValuePageBuffer<K, V>(pageLimit / 2, serialization, keyClass, valueClass);
        this.comparator = comparator;
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
        if (page.getSizeInBytes() > pageLimit || page.getCount() >= MAX_RECORD_PER_PAGE) {
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
     * Sort previously added key-value pairs.
     * @return the sorted source
     * @throws IOException if failed to sort pairs
     * @throws InterruptedException if interrupted while preparing
     */
    public Source<KeyValueSlice> sort() throws IOException, InterruptedException {
        flushPageBuffer();
        List<Source<KeyValueSlice>> sources = new ArrayList<Source<KeyValueSlice>>();
        sources.addAll(createInMemorySources());
        sources.addAll(createSpillOutSources());
        return Sources.merge(sources, new KeyValueSliceComparator(comparator));
    }

    private void flushPageBuffer() throws IOException, InterruptedException {
        KeyValuePageBuffer<?, ?> page = pageBuffer;
        int count = page.getCount();
        if (count == 0) {
            return;
        }
        if (partialPageBuffer.getWritePosition() + page.getSizeInBytes() <= bufferLimit) {
            // prevent extra buffer expansion / copy
            partialPageBuffer.ensureCapacity(partialPageBuffer.getWritePosition() + page.getSizeInBytes());
        }
        Integer[] is = indices;
        if (is.length < count) {
            indices = is = new Integer[page.getCount()];
        }
        for (int i = 0; i < count; i++) {
            is[i] = Integer.valueOf(i);
        }

        int[] ps = page.keyValueLimitPositions;
        byte[] bytes = page.buffer.getData();
        Arrays.sort(is, 0, page.getCount(), new InPageBufferComparator(page, comparator));
        for (int i = 0; i < count; i++) {
            int index = is[i] * 2;
            int offset = index == 0 ? 0 : ps[index - 1];
            int kEnd = ps[index + 0];
            int vEnd = ps[index + 1];
            put(bytes, offset, kEnd - offset, vEnd - kEnd);
        }
        page.reset();

        // keep current page end
        int position = partialPageBuffer.getWritePosition();
        if (position != 0) {
            if (partialPageCount >= partialPageLimitPositions.length) {
                int[] newInts = new int[(int) (partialPageLimitPositions.length * 1.2) + 1];
                System.arraycopy(partialPageLimitPositions, 0, newInts, 0, partialPageCount);
                partialPageLimitPositions = newInts;
            }
            partialPageLimitPositions[partialPageCount++] = position;
        }
    }

    private void put(
            byte[] bytes,
            int offset, int keyLength, int valueLength) throws IOException, InterruptedException {
        DataBuffer buffer = partialPageBuffer;
        buffer.writeInt(keyLength);
        buffer.writeInt(valueLength);
        buffer.write(bytes, offset, keyLength + valueLength);
        if (buffer.getWritePosition() > bufferLimit) {
            spillOut();
        }
    }

    private void spillOut() throws IOException, InterruptedException {
        File file = File.createTempFile("shuffle", ".tmp");
        boolean success = false;
        try {
            dumpMerged(file);
            success = true;
        } finally {
            if (success == false) {
                file.delete();
            }
        }
        sortedBlocks.add(file);
        partialPageBuffer.reset(0, 0);
        partialPageCount = 0;
    }

    private void dumpMerged(File file) throws IOException, InterruptedException {
        List<PartialPageSource> sources = createInMemorySources();
        Source<KeyValueSlice> merged = Sources.merge(sources, new KeyValueSliceComparator(comparator));
        try {
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            try {
                while (merged.next()) {
                    KeyValueSlice slice = merged.get();
                    output.writeInt(slice.getKeyLength());
                    output.writeInt(slice.getValueLength());
                    output.write(slice.getBytes(), slice.getOffset(), slice.getSliceLength());
                }
                // EOF
                output.writeInt(-1);
            } finally {
                output.close();
            }
        } finally {
            merged.close();
        }
    }

    private List<PartialPageSource> createInMemorySources() {
        List<PartialPageSource> sources = new ArrayList<PartialPageSource>();
        byte[] bytes = partialPageBuffer.getData();
        int last = 0;
        for (int i = 0, n = partialPageCount; i < n; i++) {
            int limit = partialPageLimitPositions[i];
            sources.add(new PartialPageSource(bytes, last, limit));
            last = limit;
        }
        int limit = partialPageBuffer.getWritePosition();
        if (limit > last) {
            sources.add(new PartialPageSource(bytes, last, limit));
        }
        return sources;
    }

    private List<StreamSource> createSpillOutSources() throws IOException {
        List<StreamSource> sources = new ArrayList<StreamSource>();
        boolean succeed = false;
        try {
            for (File file : sortedBlocks) {
                sources.add(new StreamSource(new DataInputStream(new BufferedInputStream(new FileInputStream(file)))));
            }
            succeed = true;
            return sources;
        } finally {
            if (succeed == false) {
                for (StreamSource s : sources) {
                    s.close();
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (Iterator<File> iter = sortedBlocks.iterator(); iter.hasNext(); ) {
            File file = iter.next();
            file.delete();
            iter.remove();
        }
    }

    private static final class KeyValuePageBuffer<K, V> {

        final DataBuffer buffer;

        int[] keyValueLimitPositions = new int[256];

        int positionsIndex;

        final Serializer<K> keySerializer;

        final Serializer<V> valueSerializer;

        public KeyValuePageBuffer(
                int initialBufferSize,
                SerializationFactory serialization,
                Class<K> keyClass, Class<V> valueClass) {
            this.buffer = new DataBuffer(initialBufferSize, 1.2);
            this.keySerializer = serialization.getSerializer(keyClass);
            this.valueSerializer = serialization.getSerializer(valueClass);
            try {
                keySerializer.open(new DataBufferOutputStream(buffer));
                valueSerializer.open(new DataBufferOutputStream(buffer));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        int getCount() {
            return positionsIndex >> 1;
        }

        int getSizeInBytes() {
            return buffer.getWritePosition() + (positionsIndex << 2);
        }

        void reset() {
            buffer.reset(0, 0);
            positionsIndex = 0;
        }

        void put(K key, V value) throws IOException {
            int[] pos = keyValueLimitPositions;
            int offset = positionsIndex;
            if (pos.length < offset + 2) {
                int newSize = ((int) (offset * 0.6) + 1) * 2;
                int[] newInts = new int[newSize];
                System.arraycopy(pos, 0, newInts, 0, offset);
                keyValueLimitPositions = pos = newInts;
            }
            keySerializer.serialize(key);
            pos[offset++] = buffer.getWritePosition();
            valueSerializer.serialize(value);
            pos[offset++] = buffer.getWritePosition();
            positionsIndex = offset;
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

    private static final class InPageBufferComparator implements Comparator<Integer> {

        private final byte[] data;

        private final int[] positions;

        private final RawComparator<?> comparator;

        public InPageBufferComparator(KeyValuePageBuffer<?, ?> buffer, RawComparator<?> comparator) {
            this.data = buffer.buffer.getData();
            this.positions = buffer.keyValueLimitPositions;
            this.comparator = comparator;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            int p1 = o1.intValue() * 2;
            int p2 = o2.intValue() * 2;
            int aFrom = p1 == 0 ? 0 : positions[p1 - 1];
            int aTo = positions[p1 + 0];
            int bFrom = p2 == 0 ? 0 : positions[p2 - 1];
            int bTo = positions[p2 + 0];
            return comparator.compare(data, aFrom, aTo, data, bFrom, bTo);
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
                    o1.getBytes(), o1.getOffset(), o1.getKeyLength(),
                    o2.getBytes(), o2.getOffset(), o2.getValueLength());
        }
    }
}
