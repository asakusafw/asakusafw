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
package com.asakusafw.runtime.flow.join;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.io.util.DataBuffer;

/**
 * A key for {@link LookUpTable}.
 * @since 0.1.0
 * @version 0.8.0
 */
public class LookUpKey {

    private static final int INITIAL_SIZE = 256;

    private final DataBuffer buffer;

    private final DirectView view;

    /**
     * Creates a new instance w/ default initial buffer size.
     */
    public LookUpKey() {
        this(INITIAL_SIZE);
    }

    /**
     * Creates a new instance.
     * @param bufferSize the initial buffer size
     */
    public LookUpKey(int bufferSize) {
        this.buffer = new DataBuffer(bufferSize);
        this.view = new DirectView(buffer);
    }

    /**
     * Clears the buffer.
     * @throws IOException if failed to initialize the buffer
     */
    public void reset() throws IOException {
        buffer.reset(0, 0);
    }

    /**
     * Appends an element into the buffer.
     * @param writable the target element
     * @throws IOException if error occurred while appending the element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void add(Writable writable) throws IOException {
        if (writable == null) {
            throw new IllegalArgumentException("writable must not be null"); //$NON-NLS-1$
        }
        writable.write(buffer);
    }

    /**
     * Returns a copy of this key.
     * @return a copy of this
     * @throws IOException if error occurred while creating a copy
     */
    public LookUpKey copy() throws IOException {
        LookUpKey result = new LookUpKey(0);
        byte[] contents = Arrays.copyOfRange(buffer.getData(), buffer.getReadPosition(), buffer.getReadLimit());
        result.buffer.reset(contents, 0, contents.length);
        return result;
    }

    /**
     * Returns a direct view of this key.
     * @return a direct view
     * @since 0.8.0
     */
    public LookUpKey.View getDirectView() {
        return view;
    }

    /**
     * Returns a frozen view of this key.
     * @return a frozen view
     * @since 0.8.0
     */
    public LookUpKey.View getFrozenView() {
        return new FrozenView(buffer);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        byte[] b = buffer.getData();
        for (int i = buffer.getReadPosition(), n = buffer.getReadLimit(); i < n; i++) {
            result = result * prime + b[i];
        }
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
        LookUpKey other = (LookUpKey) obj;
        if (buffer.getReadRemaining() != other.buffer.getReadRemaining()) {
            return false;
        }
        byte[] b1 = buffer.getData();
        byte[] b2 = other.buffer.getData();
        int o1 = buffer.getReadPosition();
        int o2 = other.buffer.getReadPosition();
        for (int i = 0, n = buffer.getReadRemaining(); i < n; i++) {
            if (b1[o1 + i] != b2[o2 + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Represents a view of {@link LookUpKey}.
     * @since 0.8.0
     */
    public interface View {

        // no special members
    }

    private static final class DirectView implements View {

        final DataBuffer entity;

        public DirectView(DataBuffer entity) {
            this.entity = entity;
        }

        @Override
        public int hashCode() {
            DataBuffer b = entity;
            return hashCodeInBytes(b.getData(), b.getReadPosition(), b.getReadLimit());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof DirectView) {
                DataBuffer b1 = entity;
                DataBuffer b2 = ((DirectView) obj).entity;
                return equalsInBytes(
                        b1.getData(), b1.getReadPosition(), b1.getReadLimit(),
                        b2.getData(), b2.getReadPosition(), b2.getReadLimit());
            } else if (obj instanceof FrozenView) {
                DataBuffer b1 = entity;
                byte[] b2 = ((FrozenView) obj).entity;
                return equalsInBytes(
                        b1.getData(), b1.getReadPosition(), b1.getReadLimit(),
                        b2, 0, b2.length);
            } else {
                return false;
            }
        }
    }

    private static final class FrozenView implements View {

        final byte[] entity;

        public FrozenView(DataBuffer entity) {
            this.entity = Arrays.copyOfRange(entity.getData(), entity.getReadPosition(), entity.getReadLimit());
        }

        @Override
        public int hashCode() {
            byte[] b = entity;
            return hashCodeInBytes(b, 0, b.length);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof DirectView) {
                byte[] b1 = entity;
                DataBuffer b2 = ((DirectView) obj).entity;
                return equalsInBytes(
                        b1, 0, b1.length,
                        b2.getData(), b2.getReadPosition(), b2.getReadLimit());
            } else if (obj instanceof FrozenView) {
                byte[] b1 = entity;
                byte[] b2 = ((FrozenView) obj).entity;
                return equalsInBytes(
                        b1, 0, b1.length,
                        b2, 0, b2.length);
            } else {
                return false;
            }
        }
    }

    static int hashCodeInBytes(byte[] buffer, int from, int to) {
        final int prime = 31;
        int result = 1;
        for (int i = from; i < to; i++) {
            result = result * prime + buffer[i];
        }
        return result;
    }

    static boolean equalsInBytes(byte[] b1, int from1, int to1, byte[] b2, int from2, int to2) {
        if (to1 - from1 != to2 - from2) {
            return false;
        }
        for (int i = 0, n = to1 - from1; i < n; i++) {
            if (b1[from1 + i] != b2[from2 + i]) {
                return false;
            }
        }
        return true;
    }
}
