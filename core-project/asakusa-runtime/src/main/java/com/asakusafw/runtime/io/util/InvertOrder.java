/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

/**
 * A wrapper for invert ordering of {@link WritableRawComparable}.
 * @since 0.2.5
 */
public class InvertOrder implements WritableRawComparable {

    private final WritableRawComparable entity;

    /**
     * Creates a new instance.
     * @param entity entity object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public InvertOrder(WritableRawComparable entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null"); //$NON-NLS-1$
        }
        this.entity = entity;
    }

    /**
     * Returns the entity object.
     * @return the entity
     */
    public WritableRawComparable getEntity() {
        return entity;
    }

    @Override
    public int getSizeInBytes(byte[] buf, int offset) throws IOException {
        return entity.getSizeInBytes(buf, offset);
    }

    @Override
    public int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        return -entity.compareInBytes(b1, o1, b2, o2);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        entity.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        entity.readFields(in);
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        InvertOrder other = (InvertOrder) o;
        return other.entity.compareTo(entity);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entity.hashCode();
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
        InvertOrder other = (InvertOrder) obj;
        if (!entity.equals(other.entity)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "InvertOrder({0})", //$NON-NLS-1$
                entity);
    }
}
