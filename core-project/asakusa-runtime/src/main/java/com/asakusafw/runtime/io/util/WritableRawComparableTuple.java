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
import java.util.Arrays;

import org.apache.hadoop.util.ReflectionUtils;

/**
 * An abstract implementation of tuples.
 * @since 0.2.5
 */
public class WritableRawComparableTuple implements WritableRawComparable, Tuple {

    private final WritableRawComparable[] objects;

    /**
     * Creates a new instance.
     * @param classes element classes
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WritableRawComparableTuple(Class<?>... classes) {
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
    public WritableRawComparableTuple(WritableRawComparable... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects must not be null"); //$NON-NLS-1$
        }
        this.objects = objects.clone();
    }

    @Override
    public final int size() {
        return objects.length;
    }

    @Override
    public final Object get(int index) {
        return objects[index];
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        for (int i = 0; i < objects.length; i++) {
            objects[i].write(out);
        }
    }

    @Override
    public final void readFields(DataInput in) throws IOException {
        for (int i = 0; i < objects.length; i++) {
            objects[i].readFields(in);
        }
    }

    @Override
    public final int compareTo(WritableRawComparable o) {
        assert getClass() == o.getClass();
        WritableRawComparable[] a = objects;
        WritableRawComparable[] b = ((WritableRawComparableTuple) o).objects;
        assert a.length == b.length;
        for (int i = 0; i < a.length; i++) {
            int diff = a[i].compareTo(b[i]);
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    @Override
    public final int getSizeInBytes(byte[] buf, int offset) throws IOException {
        int cursor = 0;
        for (int i = 0; i < objects.length; i++) {
            cursor += objects[i].getSizeInBytes(buf, offset + cursor);
        }
        return cursor;
    }

    @Override
    public final int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        int cursor = 0;
        for (int i = 0; i < objects.length; i++) {
            int diff = objects[i].compareInBytes(b1, o1 + cursor, b2, o2 + cursor);
            if (diff != 0) {
                return diff;
            }
            cursor += objects[i].getSizeInBytes(b1, o1 + cursor);
        }
        return 0;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(objects);
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
        WritableRawComparableTuple other = (WritableRawComparableTuple) obj;
        if (!Arrays.equals(objects, other.objects)) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return Arrays.toString(objects);
    }
}
