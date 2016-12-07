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
package com.asakusafw.runtime.value;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.runtime.io.util.WritableRawComparable;

/**
 * Represents a character string value which can be {@code null}.
 * The following snippet works well for comparing {@link StringOption} with a constant value:
<pre><code>
class Something {
    static final StringOption TARGET = new StringOption("something");

    void something(Hoge hoge) {
        if (hoge.getValueOption().equals(TARGET)) {
            ....
        }
    }
}
</code></pre>
 * @since 0.1.0
 * @version 0.9.1
 */
public final class StringOption extends ValueOption<StringOption> {

    static final ThreadLocal<Text> BUFFER_POOL = ThreadLocal.withInitial(Text::new);

    private final Text entity = new Text();

    /**
     * Creates a new instance which represents {@code null} value.
     */
    public StringOption() {
        this.nullValue = true;
    }

    /**
     * Creates a new instance which represents the specified value.
     * @param textOrNull the initial value (nullable)
     */
    public StringOption(String textOrNull) {
        if (textOrNull == null) {
            this.nullValue = true;
        } else {
            entity.set(textOrNull);
            this.nullValue = false;
        }
    }

    /**
     * Returns the value which this object represents.
     * @return the value which this object represents, never {@code null}
     * @throws NullPointerException if this object represents {@code null}
     */
    public Text get() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return entity;
    }

    /**
     * Returns the value which this object represents.
     * @return the value which this object represents, never {@code null}
     * @throws NullPointerException if this object represents {@code null}
     * @see #appendTo(StringBuilder)
     */
    public String getAsString() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return entity.toString();
    }

    /**
     * Appends the text in this object into the given {@link StringBuilder}.
     * @param buffer the destination {@link StringBuilder}
     * @return the appended {@link StringBuilder}
     * @throws NullPointerException if this object represents {@code null}
     * @since 0.9.1
     */
    public StringBuilder appendTo(StringBuilder buffer) {
        if (nullValue) {
            throw new NullPointerException();
        }
        StringOptionUtil.append(buffer, this);
        return buffer;
    }

    /**
     * Returns whether or not this text is empty.
     * @return {@code true} if this text is empty, or {@code false} if it has any characters
     * @throws NullPointerException if this object represents {@code null}
     * @since 0.9.1
     */
    public boolean isEmpty() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return entity.getLength() == 0;
    }

    /**
     * Returns the value which this object represents.
     * @param alternate the alternative value for {@code null}
     * @return the value which this object represents, or the alternative one if this object represents {@code null}
     * @deprecated Use {@link #or(String)} instead
     */
    @Deprecated
    public Text or(Text alternate) {
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
    public String or(String alternate) {
        if (nullValue) {
            return alternate;
        }
        return getAsString();
    }

    /**
     * Reset this object to an empty character string.
     * This method makes the object non-null, an empty string even if the object just represents {@code null}.
     */
    public void reset() {
        entity.clear();
        nullValue = false;
    }

    /**
     * Sets the value.
     * @param newText the value (nullable)
     * @return this
     * @deprecated Application developer should not use this method directly
     */
    @Deprecated
    public StringOption modify(Text newText) {
        if (newText == null) {
            this.nullValue = true;
        } else {
            this.nullValue = false;
            entity.set(newText);
        }
        return this;
    }

    /**
     * Sets the value.
     * @param newText the value (nullable)
     * @return this
     * @deprecated Application developer should not use this method directly
     */
    @Deprecated
    public StringOption modify(String newText) {
        if (newText == null) {
            this.nullValue = true;
        } else {
            this.nullValue = false;
            entity.set(newText);
        }
        return this;
    }

    /**
     * Sets the UTF-8 encode text contents.
     * @param utf8 the UTF-8 encode byte array
     * @param offset the offset in the byte array
     * @param length the content length in bytes
     * @return this
     * @deprecated Application developer should not use this method directly
     * @since 0.8.0
     */
    @Deprecated
    public StringOption modify(byte[] utf8, int offset, int length) {
        entity.set(utf8, offset, length);
        this.nullValue = false;
        return this;
    }

    @Override
    @Deprecated
    public void copyFrom(StringOption optionOrNull) {
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
        StringOption other = (StringOption) obj;
        if (nullValue != other.nullValue) {
            return false;
        }
        if (nullValue) {
            return other.nullValue;
        }
        Text a = entity;
        Text b = other.entity;
        return equalsTexts(a, b);
    }

    /**
     * Returns whether both this object and the specified value represents an equivalent value or not.
     * @param other the target value (nullable)
     * @return {@code true} if this object has the specified value, otherwise {@code false}
     */
    public boolean has(String other) {
        if (isNull()) {
            return other == null;
        }
        if (other == null) {
            return false;
        }
        Text buffer = BUFFER_POOL.get();
        buffer.set(other);
        return entity.equals(buffer);
    }

    /**
     * Returns whether both this object and the specified value represents an equivalent value or not.
     * @param other the target value (nullable)
     * @return {@code true} if this object has the specified value, otherwise {@code false}
     * @deprecated Use {@link #equals(Object)} instead
     */
    @Deprecated
    public boolean has(Text other) {
        if (isNull()) {
            return other == null;
        }
        if (other == null) {
            return false;
        }
        return equalsTexts(entity, other);
    }

    /**
     * Returns whether or not this text contains the given substring.
     * @param sub the substring
     * @return {@code true} if this has the given substring or the substring is empty, otherwise {@code false}
     * @throws NullPointerException if this or the substring is {@code null}
     * @since 0.9.1
     */
    public boolean contains(String sub) {
        if (isNull() || sub == null) {
            throw new NullPointerException();
        }
        if (sub.isEmpty()) {
            return true;
        }
        Text buffer = BUFFER_POOL.get();
        buffer.set(sub);
        return contains(entity, buffer);
    }

    /**
     * Returns whether or not this text contains the given substring.
     * @param sub the substring
     * @return {@code true} if this has the given substring or the substring is empty, otherwise {@code false}
     * @throws NullPointerException if this or the substring is {@code null}
     * @since 0.9.1
     */
    public boolean contains(StringOption sub) {
        if (isNull() || sub == null || sub.isNull()) {
            throw new NullPointerException();
        }
        if (sub.isEmpty()) {
            return true;
        }
        return contains(entity, sub.entity);
    }

    private boolean contains(Text a, Text b) {
        int subLen = b.getLength();
        if (subLen == 0) {
            return true;
        }
        if (a.getLength() < subLen) {
            return false;
        }
        byte[] aBuf = a.getBytes();
        byte[] bBuf = b.getBytes();
        LOOP: for (int i = 0, n = a.getLength() - subLen; i <= n; i++) {
            if (aBuf[i] == bBuf[0]) {
                for (int j = 1; j < subLen; j++) {
                    if (aBuf[i + j] != bBuf[j]) {
                        continue LOOP;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not this text starts with the given prefix.
     * @param prefix the prefix
     * @return {@code true} if this has the given prefix or the prefix is empty, otherwise {@code false}
     * @throws NullPointerException if this or the prefix is {@code null}
     * @since 0.9.1
     */
    public boolean startsWith(String prefix) {
        if (isNull() || prefix == null) {
            throw new NullPointerException();
        }
        if (prefix.isEmpty()) {
            return true;
        }
        Text buffer = BUFFER_POOL.get();
        buffer.set(prefix);
        return startsWith(entity, buffer);
    }

    /**
     * Returns whether or not this text starts with the given prefix.
     * @param prefix the prefix
     * @return {@code true} if this has the given prefix or the prefix is empty, otherwise {@code false}
     * @throws NullPointerException if this or the prefix is {@code null}
     * @since 0.9.1
     */
    public boolean startsWith(StringOption prefix) {
        if (isNull() || prefix == null || prefix.isNull()) {
            throw new NullPointerException();
        }
        if (prefix.isEmpty()) {
            return true;
        }
        return startsWith(entity, prefix.entity);
    }

    private static boolean startsWith(Text a, Text b) {
        if (a.getLength() < b.getLength()) {
            return false;
        }
        byte[] aBuf = a.getBytes();
        byte[] bBuf = b.getBytes();
        for (int i = 0, n = b.getLength(); i < n; i++) {
            if (aBuf[i] != bBuf[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether or not this text ends with the given suffix.
     * @param suffix the suffix
     * @return {@code true} if this has the given suffix or the suffix is empty, otherwise {@code false}
     * @throws NullPointerException if this or the suffix is {@code null}
     * @since 0.9.1
     */
    public boolean endsWith(String suffix) {
        if (isNull() || suffix == null) {
            throw new NullPointerException();
        }
        if (suffix.isEmpty()) {
            return true;
        }
        Text buffer = BUFFER_POOL.get();
        buffer.set(suffix);
        return endsWith(entity, buffer);
    }

    /**
     * Returns whether or not this text ends with the given suffix.
     * @param suffix the suffix
     * @return {@code true} if this has the given suffix or the suffix is empty, otherwise {@code false}
     * @throws NullPointerException if this or the suffix is {@code null}
     * @since 0.9.1
     */
    public boolean endsWith(StringOption suffix) {
        if (isNull() || suffix == null || suffix.isNull()) {
            throw new NullPointerException();
        }
        if (suffix.isEmpty()) {
            return true;
        }
        return endsWith(entity, suffix.entity);
    }

    private static boolean endsWith(Text a, Text b) {
        if (a.getLength() < b.getLength()) {
            return false;
        }
        byte[] aBuf = a.getBytes();
        byte[] bBuf = b.getBytes();
        int base = a.getLength() - b.getLength();
        for (int i = 0, n = b.getLength(); i < n; i++) {
            if (aBuf[i + base] != bBuf[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        StringOption other = (StringOption) o;
        if (nullValue | other.nullValue) {
            if (nullValue & other.nullValue) {
                return 0;
            }
            return nullValue ? -1 : +1;
        }
        return compareTexts(entity, other.entity);
    }

    @Override
    public String toString() {
        if (isNull()) {
            return String.valueOf((Object) null);
        } else {
            return getAsString();
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        if (isNull()) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            entity.write(out);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFields(DataInput in) throws IOException {
        if (in.readBoolean() == false) {
            setNull();
        } else {
            nullValue = false;
            entity.readFields(in);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int restore(byte[] bytes, int offset, int limit) throws IOException {
        if (limit - offset == 0) {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a String field ({0})",
                    "invalid length"));
        }
        if (bytes[offset] == 0) {
            setNull();
            return 1;
        }
        int size = WritableUtils.decodeVIntSize(bytes[offset + 1]);
        if (limit - offset < size + 1) {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a String field ({0})",
                    "invalid length"));
        }
        int length = (int) ByteArrayUtil.readVLong(bytes, offset + 1);
        if (limit - offset >= size + 1 + length) {
            nullValue = false;
            entity.set(bytes, offset + size + 1, length);
            return size + 1 + length;
        } else {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a String field ({0})",
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
        if (bytes[offset] == 0) {
            return 1;
        }
        int size = WritableUtils.decodeVIntSize(bytes[offset + 1]);
        int textLength = (int) ByteArrayUtil.readVLong(bytes, offset + 1);
        return 1 + size + textLength;
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
        int n1 = WritableUtils.decodeVIntSize(b1[s1 + 1]);
        int n2 = WritableUtils.decodeVIntSize(b2[s2 + 1]);
        int len1 = (int) ByteArrayUtil.readVLong(b1, s1 + 1);
        int len2 = (int) ByteArrayUtil.readVLong(b2, s2 + 1);
        return ByteArrayUtil.compare(
                b1, s1 + 1 + n1, len1,
                b2, s2 + 1 + n2, len2);
    }

    private static boolean equalsTexts(Text a, Text b) {
        return ByteArrayUtil.equals(
                a.getBytes(), 0, a.getLength(),
                b.getBytes(), 0, b.getLength());
    }

    private static int compareTexts(Text a, Text b) {
        return ByteArrayUtil.compare(
                a.getBytes(), 0, a.getLength(),
                b.getBytes(), 0, b.getLength());
    }
}
