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
 * Represents a {@code boolean} value which can be {@code null}.
 */
public final class BooleanOption extends ValueOption<BooleanOption> {

    private static final int TRUE_HASHCODE = 1231;

    private static final int FALSE_HASHCODE = 1237;

    private boolean value;

    /**
     * Creates a new instance which represents {@code null} value.
     */
    public BooleanOption() {
        this.nullValue = true;
    }

    /**
     * Creates a new instance which represents the specified value.
     * @param value the initial value
     */
    public BooleanOption(boolean value) {
        this.value = value;
        this.nullValue = false;
    }

    /**
     * Returns the value which this object represents.
     * @return the value which this object represents, never {@code null}
     * @throws NullPointerException if this object represents {@code null}
     */
    public boolean get() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return value;
    }

    /**
     * Returns the value which this object represents.
     * @param alternate the alternative value for {@code null}
     * @return the value which this object represents, or the alternative one if this object represents {@code null}
     */
    public boolean or(boolean alternate) {
        if (nullValue) {
            return alternate;
        }
        return value;
    }

    /**
     * Sets the value.
     * @param newValue the value
     * @return this
     * @see ValueOption#setNull()
     * @deprecated Application developer should not use this method directly
     */
    @Deprecated
    public BooleanOption modify(boolean newValue) {
        this.nullValue = false;
        this.value = newValue;
        return this;
    }

    @Override
    @Deprecated
    public void copyFrom(BooleanOption optionOrNull) {
        if (optionOrNull == null || optionOrNull.nullValue) {
            this.nullValue = true;
        } else {
            this.nullValue = false;
            this.value = optionOrNull.value;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        if (isNull()) {
            return 1;
        }
        int result = 1;
        result = prime * result + (value ? TRUE_HASHCODE : FALSE_HASHCODE);
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
        BooleanOption other = (BooleanOption) obj;
        if (nullValue != other.nullValue) {
            return false;
        }
        if (nullValue == false && value != other.value) {
            return false;
        }
        return true;
    }

    /**
     * Returns whether both this object and the specified value represents an equivalent value or not.
     * @param other the target value
     * @return {@code true} if this object has the specified value, otherwise {@code false}
     */
    public boolean has(boolean other) {
        if (isNull()) {
            return false;
        }
        return value == other;
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        BooleanOption other = (BooleanOption) o;
        if (nullValue | other.nullValue) {
            if (nullValue & other.nullValue) {
                return 0;
            }
            return nullValue ? -1 : +1;
        }
        // true > false
        if (value ^ other.value) {
            return value ? 1 : -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return String.valueOf((Object) null);
        } else {
            return String.valueOf(value);
        }
    }

    private static final int SERIALIZE_NULL = -1;
    private static final int SERIALIZE_TRUE = +1;
    private static final int SERIALIZE_FALSE = 0;

    @Override
    public void write(DataOutput out) throws IOException {
        if (isNull()) {
            out.writeByte(SERIALIZE_NULL);
        } else {
            out.writeByte(value ? SERIALIZE_TRUE : SERIALIZE_FALSE);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        byte field = in.readByte();
        restore(field);
    }

    @Override
    public int restore(byte[] bytes, int offset, int limit) throws IOException {
        if (limit - offset < 1) {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a boolean field ({0})",
                    "invalid length"));
        }
        restore(bytes[offset]);
        return 1;
    }

    @SuppressWarnings("deprecation")
    private void restore(byte field) throws IOException {
        if (field == SERIALIZE_NULL) {
            setNull();
        } else if (field == SERIALIZE_TRUE) {
            modify(true);
        } else if (field == SERIALIZE_FALSE) {
            modify(false);
        } else {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a boolean field ({0})",
                    field));
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
        return 1;
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
        return ByteArrayUtil.compare(b1[s1], b2[s2]);
    }
}
