/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.math.BigDecimal;
import java.text.MessageFormat;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.runtime.io.util.WritableRawComparable;

/**
 * {@code null}値を許容する10進数。
 */
public final class DecimalOption extends ValueOption<DecimalOption> {

    private BigDecimal entity = BigDecimal.ZERO;

    /**
     * Creates a new instance which represents {@code null} value.
     */
    public DecimalOption() {
        super();
    }

    /**
     * Creates a new instance which represents the specified value.
     * @param valueOrNull the initial value
     */
    public DecimalOption(BigDecimal valueOrNull) {
        super();
        if (valueOrNull != null) {
            this.entity = valueOrNull;
            this.nullValue = false;
        }
    }

    /**
     * このオブジェクトが表現する値を返す。
     * @return このオブジェクトが表現する値
     * @throws NullPointerException この値が{@code null}を表現する場合
     */
    public BigDecimal get() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return entity;
    }

    /**
     * このオブジェクトが表現する値を返す。
     * @param alternate このオブジェクトが{@code null}を表現する場合に返す値
     * @return このオブジェクトが表現する値、{@code null}を表現する場合は引数の値
     */
    public BigDecimal or(BigDecimal alternate) {
        if (nullValue) {
            return alternate;
        }
        return get();
    }

    /**
     * このオブジェクトの内容と指定の値を合計した結果を、このオブジェクトに書き出す。
     * @param delta 追加する値
     * @throws NullPointerException このオブジェクトが{@code null}を表現する場合
     */
    public void add(BigDecimal delta) {
        if (nullValue) {
            throw new NullPointerException();
        }
        this.entity = entity.add(delta);
    }

    /**
     * このオブジェクトの内容と指定のオブジェクトの内容を合計した結果を、このオブジェクトに書き出す。
     * @param other 対象のオブジェクト、{@code null}が指定された場合には何も行わない
     * @throws NullPointerException このオブジェクトが{@code null}を表現する場合
     */
    public void add(DecimalOption other) {
        if (nullValue) {
            throw new NullPointerException();
        }
        if (other.nullValue) {
            return;
        }
        this.entity = entity.add(other.entity);
    }

    /**
     * このオブジェクトが表現する値を変更する。
     * @param newValue 変更後の値、{@code null}を指定した場合はこの値が{@code null}を表すようになる
     * @return 自身のオブジェクト
     * @deprecated アプリケーションからは利用しない
     */
    @Deprecated
    public DecimalOption modify(BigDecimal newValue) {
        if (newValue == null) {
            this.nullValue = true;
        } else {
            this.nullValue = false;
            this.entity = newValue;
        }
        return this;
    }

    /**
     * このオブジェクトの内容を、指定のオブジェクトの内容で上書きする。
     * @param optionOrNull 上書きする内容、
     *     {@code null}の場合はこのオブジェクトが{@code null}値を表すようになる
     * @deprecated アプリケーションからは利用しない
     */
    @Override
    @Deprecated
    public void copyFrom(DecimalOption optionOrNull) {
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
        DecimalOption other = (DecimalOption) obj;
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
    public boolean has(BigDecimal other) {
        if (isNull()) {
            return other == null;
        }
        return entity.equals(other);
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        DecimalOption other = (DecimalOption) o;
        // nullは他のどのような値よりも小さい
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
        // FIXME 効率よくする
        if (nullValue) {
            WritableUtils.writeVLong(out, -1);
        } else {
            byte[] bytes = entity.toString().getBytes("UTF-8");
            WritableUtils.writeVLong(out, bytes.length);
            out.write(bytes);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFields(DataInput in) throws IOException {
        // FIXME 効率よくする
        int length = (int) WritableUtils.readVLong(in);
        if (length == -1) {
            setNull();
        } else {
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            modify(new BigDecimal(new String(bytes, "UTF-8")));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int restore(byte[] bytes, int offset, int limit) throws IOException {
        if (limit - offset == 0) {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a Decimal field ({0})",
                    "invalid length"));
        }
        int size = WritableUtils.decodeVIntSize(bytes[offset]);
        if (limit - offset < size) {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a Decimal field ({0})",
                    "invalid length"));
        }
        int length = (int) ByteArrayUtil.readVLong(bytes, offset);
        if (length == -1) {
            setNull();
            return size;
        } else if (limit - offset >= size + length) {
            modify(new BigDecimal(new String(bytes, offset + size, length, "UTF-8")));
            return size + length;
        } else {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a Decimal field ({0})",
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
     * このクラスの直列化された形式から、占有しているバイト長を返す。
     * @param bytes 対象のバイト配列
     * @param offset バイト配列の開始位置
     * @param length バイト配列の制限長
     * @return 比較結果
     */
    public static int getBytesLength(byte[] bytes, int offset, int length) {
        int size = WritableUtils.decodeVIntSize(bytes[offset]);
        int textLength = (int) ByteArrayUtil.readVLong(bytes, offset);
        if (textLength == -1) {
            return size;
        }
        return size + textLength;
    }

    /**
     * このクラスの2つの直列化された値を比較する。
     * @param b1 比較されるバイト配列
     * @param s1 比較されるバイト配列の開始位置
     * @param l1 比較されるバイト配列内で、このクラスの直列化形式が占有しているバイト長
     * @param b2 比較するバイト配列
     * @param s2 比較するバイト配列の開始位置
     * @param l2 比較するバイト配列内で、このクラスの直列化形式が占有しているバイト長
     * @return 比較結果
     */
    public static int compareBytes(
            byte[] b1, int s1, int l1,
            byte[] b2, int s2, int l2) {
        int len1 = (int) ByteArrayUtil.readVLong(b1, s1 + 1);
        int len2 = (int) ByteArrayUtil.readVLong(b2, s2 + 1);
        if (len1 == -1) {
            if (len2 == -1) {
                return 0;
            } else {
                return -1;
            }
        } else if (len2 == -1) {
            return +1;
        }
        int n1 = WritableUtils.decodeVIntSize(b1[s1]);
        int n2 = WritableUtils.decodeVIntSize(b2[s2]);
        return WritableComparator.compareBytes(
                b1, s1 + n1, len1,
                b2, s2 + n2, len2);
    }
}
