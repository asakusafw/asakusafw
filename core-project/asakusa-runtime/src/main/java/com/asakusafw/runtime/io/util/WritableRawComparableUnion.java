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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * An abstract implementation of union object.
 * @since 0.2.5
 */
public class WritableRawComparableUnion implements Union, WritableRawComparable {

    private final WritableRawComparable[] objects;

    private int position;

    /**
     * Creates a new instance.
     * @param classes element classes
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WritableRawComparableUnion(Class<?>... classes) {
        if (classes == null) {
            throw new IllegalArgumentException("classes must not be null"); //$NON-NLS-1$
        }
        this.objects = new WritableRawComparable[classes.length];
        for (int i = 0; i < classes.length; i++) {
            this.objects[i] = (WritableRawComparable) ReflectionUtils.newInstance(classes[i], null);
        }
    }

    /**
     * Creates a new instance.
     * @param objects elements
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WritableRawComparableUnion(WritableRawComparable... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects must not be null"); //$NON-NLS-1$
        }
        this.objects = objects.clone();
    }

    @Override
    public final int getPosition() {
        return position;
    }

    @Override
    public Object switchObject(int newPosition) {
        this.position = newPosition;
        return getObject();
    }

    @Override
    public final Object getObject() {
        return objects[position];
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeVInt(out, position);
        objects[position].write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.position = WritableUtils.readVInt(in);
        objects[position].readFields(in);
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        WritableRawComparableUnion other = (WritableRawComparableUnion) o;
        int p1 = position;
        int p2 = other.position;
        if (p1 < p2) {
            return -1;
        } else if (p1 > p2) {
            return +1;
        }
        return objects[p1].compareTo(other.objects[p2]);
    }

    @Override
    public int getSizeInBytes(byte[] buf, int offset) throws IOException {
        int pos = WritableComparator.readVInt(buf, offset);
        WritableRawComparable object = objects[pos];
        int meta = WritableUtils.decodeVIntSize(buf[offset]);
        return meta + object.getSizeInBytes(buf, offset + meta);
    }

    @Override
    public int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        int p1 = WritableComparator.readVInt(b1, o1);
        int p2 = WritableComparator.readVInt(b2, o2);
        if (p1 < p2) {
            return -1;
        } else if (p1 > p2) {
            return +1;
        }
        WritableRawComparable object = objects[p1];
        int meta = WritableUtils.decodeVIntSize(b1[o1]);
        return object.compareInBytes(b1, o1 + meta, b2, o2 + meta);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + position;
        result = prime * result + Arrays.hashCode(objects);
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
        WritableRawComparableUnion other = (WritableRawComparableUnion) obj;
        if (position != other.position) {
            return false;
        }
        if (!Arrays.equals(objects, other.objects)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        try {
            return MessageFormat.format(
                    "{0} (position={1}, object={2})", //$NON-NLS-1$
                    getClass().getSimpleName(),
                    position,
                    getObject());
        } catch (RuntimeException e) {
            return MessageFormat.format(
                    "{0} (position={1}, object=(invalid))", //$NON-NLS-1$
                    getClass().getSimpleName(),
                    position);
        }
    }
}
