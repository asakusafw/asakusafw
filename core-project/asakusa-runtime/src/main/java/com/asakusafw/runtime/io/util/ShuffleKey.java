/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * An abstract implementation of shuffling keys.
 * @param <TGroup> type for grouping
 * @param <TOrder> type for ordering
 * @since 0.2.5
 */
public abstract class ShuffleKey<
        TGroup extends WritableRawComparable,
        TOrder extends WritableRawComparable> implements WritableRawComparable {

    final TGroup groupObject;

    final TOrder orderObject;

    /**
     * Creates a new instance.
     * @param groupType type for grouping
     * @param orderType tyoe for ordering
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected ShuffleKey(Class<TGroup> groupType, Class<TOrder> orderType) {
        if (groupType == null) {
            throw new IllegalArgumentException("groupType must not be null"); //$NON-NLS-1$
        }
        if (orderType == null) {
            throw new IllegalArgumentException("orderType must not be null"); //$NON-NLS-1$
        }
        this.groupObject = ReflectionUtils.newInstance(groupType, null);
        this.orderObject = ReflectionUtils.newInstance(orderType, null);
    }

    /**
     * Creates a new instance.
     * @param groupObject object for grouping
     * @param orderObject object for ordering
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected  ShuffleKey(TGroup groupObject, TOrder orderObject) {
        if (groupObject == null) {
            throw new IllegalArgumentException("groupObject must not be null"); //$NON-NLS-1$
        }
        if (orderObject == null) {
            throw new IllegalArgumentException("orderObject must not be null"); //$NON-NLS-1$
        }
        this.groupObject = groupObject;
        this.orderObject = orderObject;
    }

    /**
     * Returns an object for grouping.
     * @return the group object
     */
    public final TGroup getGroupObject() {
        return groupObject;
    }

    /**
     * Returns an object for ordering.
     * @return the order object
     */
    public final TOrder getOrderObject() {
        return orderObject;
    }

    @Override
    public final int compareTo(WritableRawComparable o) {
        ShuffleKey<?, ?> other = (ShuffleKey<?, ?>) o;
        int groupDiff = groupObject.compareTo(other.groupObject);
        if (groupDiff != 0) {
            return groupDiff;
        }
        int orderDiff = orderObject.compareTo(other.orderObject);
        return orderDiff;
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        groupObject.write(out);
        orderObject.write(out);
    }

    @Override
    public final void readFields(DataInput in) throws IOException {
        groupObject.readFields(in);
        orderObject.readFields(in);
    }

    @Override
    public final int getSizeInBytes(byte[] buf, int offset) throws IOException {
        int groupSize = groupObject.getSizeInBytes(buf, offset);
        int orderSize = orderObject.getSizeInBytes(buf, offset + groupSize);
        return groupSize + orderSize;
    }

    @Override
    public final int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        int groupDiff = groupObject.compareInBytes(b1, o1, b2, o2);
        if (groupDiff != 0) {
            return groupDiff;
        }
        int groupSize = groupObject.getSizeInBytes(b1, o1);
        assert groupSize == groupObject.getSizeInBytes(b2, o2);

        int orderDiff = orderObject.compareInBytes(b1, o1 + groupSize, b2, o2 + groupSize);
        return orderDiff;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupObject.hashCode();
        result = prime * result + orderObject.hashCode();
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ShuffleKey<?, ?> other = (ShuffleKey<?, ?>) obj;
        if (!groupObject.equals(other.groupObject)) {
            return false;
        }
        if (!orderObject.equals(other.orderObject)) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractShuffleKey [group=");
        builder.append(groupObject);
        builder.append(", order=");
        builder.append(orderObject);
        builder.append("]");
        return builder.toString();
    }

    /**
     * An implementation of partitioner for {@link ShuffleKey}.
     * @since 0.2.5
     */
    @SuppressWarnings("rawtypes")
    public static final class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<ShuffleKey, Object> {

        @Override
        public int getPartition(ShuffleKey key, Object value, int numPartitions) {
            int hash = key.groupObject.hashCode() & Integer.MAX_VALUE;
            return hash % numPartitions;
        }
    }

    /**
     * An abstract implementation of grouping comparator for {@link ShuffleKey}.
     * @since 0.2.5
     */
    @SuppressWarnings("rawtypes")
    public abstract static class AbstractGroupComparator extends WritableComparator {

        private final ShuffleKey<?, ?> object;

        /**
         * Creates a new instance.
         * @param keyClass the key class
         */
        protected AbstractGroupComparator(Class<? extends ShuffleKey> keyClass) {
            super(keyClass);
            this.object = (ShuffleKey<?, ?>) newKey();
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            ShuffleKey<?, ?> ak = (ShuffleKey<?, ?>) a;
            ShuffleKey<?, ?> bk = (ShuffleKey<?, ?>) b;
            return ak.groupObject.compareTo(bk.groupObject);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            try {
                return object.groupObject.compareInBytes(b1, s1, b2, s2);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * An abstract implementation of ordering comparator for {@link ShuffleKey}.
     * @since 0.2.5
     */
    @SuppressWarnings("rawtypes")
    public abstract static class AbstractOrderComparator extends WritableRawComparator {

        /**
         * Creates a new instance.
         * @param keyClass the key class
         */
        protected AbstractOrderComparator(Class<? extends ShuffleKey> keyClass) {
            super(keyClass);
        }
    }
}
