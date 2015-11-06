/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.value;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;

import com.asakusafw.runtime.io.util.WritableRawComparable;

/**
 * Represents a date value which can be {@code null}.
 */
public final class DateOption extends ValueOption<DateOption> {

    private final Date entity = new Date();

    /**
     * Creates a new instance which represents {@code null} value.
     */
    public DateOption() {
        this.nullValue = true;
    }

    /**
     * Creates a new instance which represents the specified value.
     * @param valueOrNull the initial value (nullable)
     */
    public DateOption(Date valueOrNull) {
        if (valueOrNull == null) {
            this.nullValue = true;
        } else {
            this.entity.setElapsedDays(valueOrNull.getElapsedDays());
            this.nullValue = false;
        }
    }

    /**
     * Returns the value which this object represents.
     * @return the value which this object represents, never {@code null}
     * @throws NullPointerException if this object represents {@code null}
     */
    public Date get() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return entity;
    }

    /**
     * Returns the value which this object represents.
     * @param alternate the alternative value for {@code null}
     * @return the value which this object represents, or the alternative one if this object represents {@code null}
     */
    public Date or(Date alternate) {
        if (nullValue) {
            return alternate;
        }
        return get();
    }

    /**
     * Returns the value which this object represents.
     * @param alternate the alternative value for {@code null}
     * @return the value which this object represents, or the alternative one if this object represents {@code null}
     */
    public int or(int alternate) {
        if (nullValue) {
            return alternate;
        }
        return get().getElapsedDays();
    }

    /**
     * Sets the value.
     * @param newValue the value (nullable)
     * @return this
     * @deprecated Application developer should not use this method directly
     */
    @Deprecated
    public DateOption modify(Date newValue) {
        if (newValue == null) {
            this.nullValue = true;
        } else {
            this.nullValue = false;
            this.entity.setElapsedDays(newValue.getElapsedDays());
        }
        return this;
    }

    /**
     * Sets the value.
     * @param newValue the value as {@link Date#getElapsedDays() the elapsed days}
     * @return this
     * @deprecated Application developer should not use this method directly
     */
    @Deprecated
    public DateOption modify(int newValue) {
        this.nullValue = false;
        this.entity.setElapsedDays(newValue);
        return this;
    }

    @Override
    @Deprecated
    public void copyFrom(DateOption optionOrNull) {
        if (this == optionOrNull) {
            return;
        } else if (optionOrNull == null || optionOrNull.nullValue) {
            this.nullValue = true;
        } else {
            modify(optionOrNull.entity);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        if (isNull()) {
            return 1;
        }
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
        DateOption other = (DateOption) obj;
        if (nullValue != other.nullValue) {
            return false;
        }
        if (nullValue == false && entity.equals(other.entity) == false) {
            return false;
        }
        return true;
    }

    /**
     * Returns whether both this object and the specified value represents an equivalent value or not.
     * @param other the target value (nullable)
     * @return {@code true} if this object has the specified value, otherwise {@code false}
     */
    public boolean has(Date other) {
        if (isNull()) {
            return other == null;
        }
        return entity.equals(other);
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        DateOption other = (DateOption) o;
        if (nullValue | other.nullValue) {
            if (nullValue & other.nullValue) {
                return 0;
            }
            return nullValue ? -1 : +1;
        }
        return entity.compareTo(other.entity);
    }

    @Override
    public String toString() {
        if (isNull()) {
            return String.valueOf((Object) null);
        } else {
            return get().toString();
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        if (isNull()) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeInt(entity.getElapsedDays());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFields(DataInput in) throws IOException {
        if (in.readBoolean()) {
            modify(in.readInt());
        } else {
            setNull();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int restore(byte[] bytes, int offset, int limit) throws IOException {
        if (limit - offset == 0) {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a Date field ({0})",
                    "invalid length"));
        }
        if (bytes[offset + 0] == 0) {
            setNull();
            return 1;
        } else if (limit - offset >= 4 + 1) {
            modify(ByteArrayUtil.readInt(bytes, offset + 1));
            return 5;
        } else {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a Date field ({0})",
                    "invalid length"));
        }
    }

    @Override
    public int getSizeInBytes(byte[] buf, int offset) throws IOException {
        return getBytesLength(buf, offset, buf.length - offset);
    }

    @Override
    public int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        return compareBytes(b1, o1, b1.length - o1, b2, o2, b2.length - o2);
    }

    /**
     * Returns the actual number of bytes from the serialized byte array.
     * @param bytes the target byte array
     * @param offset the beginning index in the byte array (inclusive)
     * @param length the limit length of the byte array
     * @return the comparison result
     */
    public static int getBytesLength(byte[] bytes, int offset, int length) {
        return bytes[offset] == 0 ? 1 : 5;
    }

    /**
     * Compares between the two objects in serialized form.
     * @param b1 the first byte array to be compared
     * @param s1 the beginning index in {@code b1}
     * @param l1 the limit byte size in {@code b1}
     * @param b2 the second byte array to be compared
     * @param s2 the beginning index in {@code b2}
     * @param l2 the limit byte size in {@code b2}
     * @return the comparison result
     */
    public static int compareBytes(
            byte[] b1, int s1, int l1,
            byte[] b2, int s2, int l2) {
        if (b1[s1] == 0 || b2[s2] == 0) {
            return ByteArrayUtil.compare(b1[s1], b2[s2]);
        }
        return ByteArrayUtil.compare(
                ByteArrayUtil.readInt(b1, s1 + 1),
                ByteArrayUtil.readInt(b2, s2 + 1));
    }
}
