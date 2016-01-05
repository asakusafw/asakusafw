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
package com.asakusafw.runtime.stage.collector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.SecureRandom;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.runtime.io.util.DataBuffer;

/**
 * A sortable data slot.
 */
public class SortableSlot implements WritableComparable<SortableSlot> {

    /**
     * The number of bits for grouping operation.
     * This will be used for computing hash code with this bit length from this object.
     */
    static final int GROUPING_BITS = 10;

    /**
     * The method name of {@link #begin(int)}.
     */
    public static final String NAME_BEGIN = "begin"; //$NON-NLS-1$

    /**
     * The method name of {@link #addByte(int)}.
     */
    public static final String NAME_ADD_BYTE = "addByte"; //$NON-NLS-1$

    /**
     * The method name of {@link #addRandom()}.
     */
    public static final String NAME_ADD_RANDOM = "addRandom"; //$NON-NLS-1$

    /**
     * The method name of {@link #add(Writable)}.
     */
    public static final String NAME_ADD = "add"; //$NON-NLS-1$

    private final SecureRandom random = new SecureRandom();

    private final DataBuffer buffer = new DataBuffer();

    private int slotNumber = -1;

    /**
     * Begins to write key items into this object.
     * @param slot the target slot number
     */
    public void begin(int slot) {
        this.slotNumber = slot;
        buffer.reset(0, 0);
    }

    /**
     * Returns the current slot number.
     * @return the current target slot number specified by {@link #begin(int)}
     */
    public int getSlot() {
        return slotNumber;
    }

    /**
     * Adds an unsigned byte value.
     * @param data the value (only use lower 8-bits)
     * @throws IOException if failed to add the value
     */
    public void addByte(int data) throws IOException {
        buffer.writeByte(data);
    }

    /**
     * Adds a 32-bits random value.
     * @throws IOException if failed to add the value
     */
    public void addRandom() throws IOException {
        buffer.writeInt(random.nextInt());
    }

    /**
     * Adds an object.
     * @param data the object
     * @throws IOException if failed to add the value
     */
    public void add(Writable data) throws IOException {
        data.write(buffer);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeVInt(out, slotNumber);
        WritableUtils.writeVInt(out, buffer.getReadLimit());
        out.write(buffer.getData(), 0, buffer.getReadLimit());
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.slotNumber = WritableUtils.readVInt(in);
        int length = WritableUtils.readVInt(in);
        buffer.reset(0, 0);
        buffer.write(in, length);
    }

    @Override
    public int compareTo(SortableSlot o) {
        if (slotNumber < o.slotNumber) {
            return -1;
        } else if (slotNumber > o.slotNumber) {
            return +1;
        }
        return WritableComparator.compareBytes(
                buffer.getData(), 0, buffer.getReadLimit(),
                o.buffer.getData(), 0, o.buffer.getReadLimit());
    }

    int hashCode(int ignoreTailBits) {
        int ignoreTailBytes = ignoreTailBits / Byte.SIZE;
        int ignoreTailByteMask = -1 << (ignoreTailBits & (Byte.SIZE - 1));
        if (buffer.getReadLimit() <= ignoreTailBytes) {
            return 0;
        }
        int hash = 1;
        final int prime = 31;
        hash = hash * prime + slotNumber;
        byte[] content = buffer.getData();
        for (int i = 0, n = buffer.getReadLimit() - ignoreTailBytes - 1; i < n; i++) {
            hash = hash * prime + content[i];
        }
        hash = hash * prime + (content[buffer.getReadLimit() - ignoreTailBytes - 1] & ignoreTailByteMask);
        return hash;
    }

    @Override
    public int hashCode() {
        return hashCode(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SortableSlot other = (SortableSlot) obj;
        return this.compareTo(other) == 0;
    }

    static {
        WritableComparator.define(SortableSlot.class, new Comparator());
    }

    /**
     * A comparator for {@link SortableSlot}.
     */
    public static class Comparator extends WritableComparator implements Externalizable {

        /**
         * Creates a new instance.
         */
        public Comparator() {
            super(SortableSlot.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            try {
                int varIntSize;
                int offset1 = 0;
                int offset2 = 0;

                varIntSize = WritableUtils.decodeVIntSize(b1[s1 + offset1]);
                int slot1 = WritableComparator.readVInt(b1, s1 + offset1);
                offset1 += varIntSize;
                varIntSize = WritableUtils.decodeVIntSize(b2[s2 + offset2]);
                int slot2 = WritableComparator.readVInt(b2, s2 + offset2);
                offset2 += varIntSize;
                if (slot1 != slot2) {
                    return slot1 - slot2;
                }

                varIntSize = WritableUtils.decodeVIntSize(b1[s1 + offset1]);
                int length1 = WritableComparator.readVInt(b1, s1 + offset1);
                offset1 += varIntSize;
                varIntSize = WritableUtils.decodeVIntSize(b2[s2 + offset2]);
                int length2 = WritableComparator.readVInt(b2, s2 + offset2);
                offset2 += varIntSize;

                return compareBytes(
                        b1, s1 + offset1, length1,
                        b2, s2 + offset2, length2);
            } catch (IOException e) {
                // bad manner
                throw new RuntimeException(e);
            }
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            return;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            return;
        }

        private Object readResolve() {
            return new Comparator();
        }
    }

    /**
     * A partitioner for {@link SortableSlot}.
     */
    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<SortableSlot, Object> {

        @Override
        public int getPartition(SortableSlot key, Object value, int numPartitions) {
            int hash = key.hashCode(GROUPING_BITS);
            return (hash & Integer.MAX_VALUE) % numPartitions;
        }
    }
}
