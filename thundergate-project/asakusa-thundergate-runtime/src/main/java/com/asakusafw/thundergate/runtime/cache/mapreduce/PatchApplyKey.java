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
package com.asakusafw.thundergate.runtime.cache.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.VLongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableFactories;
import org.apache.hadoop.io.WritableFactory;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Shuffle key object for {@link PatchApplyReducer}.
 * @since 0.2.3
 */
public class PatchApplyKey implements WritableComparable<PatchApplyKey> {

    static final int POSITION_PATCH = 0;

    static final int POSITION_BASE = 1;

    final VLongWritable systemId = new VLongWritable();

    final VIntWritable position = new VIntWritable();

    /**
     * Sets patch apply key from the patch object.
     * @param model target data model object
     */
    public void setPatch(ThunderGateCacheSupport model) {
        systemId.set(model.__tgc__SystemId());
        position.set(POSITION_PATCH);
    }

    /**
     * Sets patch apply key from the base object.
     * @param model target data model object
     */
    public void setBase(ThunderGateCacheSupport model) {
        systemId.set(model.__tgc__SystemId());
        position.set(POSITION_BASE);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        systemId.write(out);
        position.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        systemId.readFields(in);
        position.readFields(in);
    }

    @Override
    public int compareTo(PatchApplyKey other) {
        int diffSystemId = systemId.compareTo(other.systemId);
        if (diffSystemId != 0) {
            return diffSystemId;
        }
        int diffPosition = position.compareTo(other.position);
        if (diffPosition != 0) {
            return diffPosition;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + position.hashCode();
        result = prime * result + systemId.hashCode();
        return result;
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
        PatchApplyKey other = (PatchApplyKey) obj;
        if (!position.equals(other.position)) {
            return false;
        }
        if (!systemId.equals(other.systemId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PatchApplyKey [systemId=");
        builder.append(systemId);
        builder.append(", position=");
        builder.append(position);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Total comparator for {@link PatchApplyKey}.
     * @since 0.2.3
     */
    public static final class SortComparator extends WritableComparator implements Externalizable {

        /**
         * Creates a new instance.
         */
        public SortComparator() {
            super(PatchApplyKey.class);
        }

        @Override
        public int compare(
                byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            try {
                long id1 = readVLong(b1, s1);
                long id2 = readVLong(b2, s2);
                if (id1 < id2) {
                    return -1;
                } else if (id1 > id2) {
                    return +1;
                }

                int offset1 = WritableUtils.decodeVIntSize(b1[s1]);
                int offset2 = WritableUtils.decodeVIntSize(b2[s2]);

                long pos1 = readVLong(b1, s1 + offset1);
                long pos2 = readVLong(b2, s2 + offset2);
                if (pos1 < pos2) {
                    return -1;
                } else if (pos1 > pos2) {
                    return +1;
                }

                return 0;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            return;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            return;
        }

        private Object readResolve() {
            return new SortComparator();
        }
    }

    /**
     * Group comparator for {@link PatchApplyKey}.
     * @since 0.2.3
     */
    @SuppressWarnings("rawtypes")
    public static final class GroupComparator extends WritableComparator implements Externalizable {

        /**
         * Creates a new instance.
         */
        public GroupComparator() {
            super(PatchApplyKey.class);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            PatchApplyKey w1 = (PatchApplyKey) a;
            PatchApplyKey w2 = (PatchApplyKey) b;
            return w1.systemId.compareTo(w2.systemId);
        }

        @Override
        public int compare(
                byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            try {
                long id1 = WritableComparator.readVLong(b1, s1);
                long id2 = WritableComparator.readVLong(b2, s2);
                if (id1 < id2) {
                    return -1;
                } else if (id1 > id2) {
                    return +1;
                } else {
                    return 0;
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            return;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            return;
        }

        private Object readResolve() {
            return new GroupComparator();
        }
    }

    /**
     * Partitioner for {@link PatchApplyKey}.
     * @since 0.2.3
     */
    public static final class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<PatchApplyKey, Object> {

        @Override
        public int getPartition(PatchApplyKey key, Object value, int numPartitions) {
            int hash = key.systemId.hashCode() & Integer.MAX_VALUE;
            return hash % numPartitions;
        }
    }

    static {
        WritableComparator.define(PatchApplyKey.class, new SortComparator());
        WritableFactories.setFactory(PatchApplyKey.class, new WritableFactory() {
            @Override
            public Writable newInstance() {
                return new PatchApplyKey();
            }
        });
    }
}
