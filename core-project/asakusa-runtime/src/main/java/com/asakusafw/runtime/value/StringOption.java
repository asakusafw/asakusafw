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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.runtime.io.util.WritableRawComparable;

/**
 * {@code null}値を許容する文字列。
 * <p>
 * この文字列を比較する場合、次のように書くとよい。
 * </p>
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
 */
public final class StringOption extends ValueOption<StringOption> {

    private static final ThreadLocal<Text> BUFFER_POOL = new ThreadLocal<Text>() {
        @Override
        protected Text initialValue() {
            return new Text();
        }
    };

    private final Text entity = new Text();

    /**
     * Creates a new instance.
     */
    public StringOption() {
        this.nullValue = true;
    }

    /**
     * Creates a new instance.
     * @param textOrNull 生成するインスタンスの初期値
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
     * このオブジェクトが表現する値を返す。
     * @return このオブジェクトが表現する値
     * @throws NullPointerException この値が{@code null}を表現する場合
     */
    public Text get() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return entity;
    }

    /**
     * このオブジェクトが表現する値を返す。
     * @return このオブジェクトが表現する値
     * @throws NullPointerException この値が{@code null}を表現する場合
     */
    public String getAsString() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return entity.toString();
    }

    /**
     * このオブジェクトが表現する値を返す。
     * @param alternate このオブジェクトが{@code null}を表現する場合に返す値
     * @return このオブジェクトが表現する値、{@code null}を表現する場合は引数の値
     */
    public Text or(Text alternate) {
        if (nullValue) {
            return alternate;
        }
        return get();
    }

    /**
     * このオブジェクトが表現する値を返す。
     * @param alternate このオブジェクトが{@code null}を表現する場合に返す値
     * @return このオブジェクトが表現する値、{@code null}を表現する場合は引数の値
     */
    public String or(String alternate) {
        if (nullValue) {
            return alternate;
        }
        return getAsString();
    }

    /**
     * このオブジェクトを空の文字列に変更する。
     * <p>
     * このオブジェクトが{@code null}を表していた場合にも、この呼び出しによって空の文字列を表すようになる。
     * </p>
     */
    public void reset() {
        nullValue = false;
        entity.clear();
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
        if (nullValue == false && entity.equals(other.entity) == false) {
            return false;
        }
        return true;
    }

    /**
     * この値と指定の値が同じものを表現する場合のみ{@code true}を返す。
     * @param other 対象の値、または{@code null}
     * @return 指定の値が同じものを表現する場合のみ{@code true}
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
     * この値と指定の値が同じものを表現する場合のみ{@code true}を返す。
     * @param other 対象の値、または{@code null}
     * @return 指定の値が同じものを表現する場合のみ{@code true}
     */
    public boolean has(Text other) {
        if (isNull()) {
            return other == null;
        }
        if (other == null) {
            return false;
        }
        return entity.equals(other);
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
        return entity.compareTo(other.entity);
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
        return WritableComparator.compareBytes(
                b1, s1 + 1 + n1, len1,
                b2, s2 + 1 + n2, len2);
    }
}
