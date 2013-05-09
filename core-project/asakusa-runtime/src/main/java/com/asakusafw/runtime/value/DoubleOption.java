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
package com.asakusafw.runtime.value;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.io.WritableComparator;

import com.asakusafw.runtime.io.util.WritableRawComparable;

/**
 * {@code null}値を許容する{@code double}値。
 */
public final class DoubleOption extends ValueOption<DoubleOption> {

    private double value;

    /**
     * Creates a new instance which represents {@code null} value.
     */
    public DoubleOption() {
        super();
    }

    /**
     * Creates a new instance which represents the specified value.
     * @param value the initial value
     */
    public DoubleOption(double value) {
        super();
        this.value = value;
        this.nullValue = false;
    }

    /**
     * このオブジェクトが表現する値を返す。
     * @return このオブジェクトが表現する値
     * @throws NullPointerException この値が{@code null}を表現する場合
     */
    public double get() {
        if (nullValue) {
            throw new NullPointerException();
        }
        return value;
    }

    /**
     * このオブジェクトが表現する値を返す。
     * @param alternate このオブジェクトが{@code null}を表現する場合に返す値
     * @return このオブジェクトが表現する値、{@code null}を表現する場合は引数の値
     */
    public double or(double alternate) {
        if (nullValue) {
            return alternate;
        }
        return value;
    }

    /**
     * このオブジェクトの内容と指定の値を合計した結果を、このオブジェクトに書き出す。
     * @param delta 追加する値
     * @throws NullPointerException このオブジェクトが{@code null}を表現する場合
     */
    public void add(double delta) {
        if (nullValue) {
            throw new NullPointerException();
        }
        this.value += delta;
    }

    /**
     * このオブジェクトの内容と指定のオブジェクトの内容を合計した結果を、このオブジェクトに書き出す。
     * @param other 対象のオブジェクト、{@code null}が指定された場合には何も行わない
     * @throws NullPointerException このオブジェクトが{@code null}を表現する場合
     */
    public void add(DoubleOption other) {
        if (nullValue) {
            throw new NullPointerException();
        }
        if (other.nullValue) {
            return;
        }
        this.value += other.value;
    }

    /**
     * このオブジェクトが表現する値を変更する。
     * @param newValue 変更後の値
     * @return 自身のオブジェクト
     * @see ValueOption#setNull()
     * @deprecated アプリケーションからは利用しない
     */
    @Deprecated
    public DoubleOption modify(double newValue) {
        this.nullValue = false;
        this.value = newValue;
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
    public void copyFrom(DoubleOption optionOrNull) {
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
        long bits = Double.doubleToLongBits(result);
        result = prime * result + (int) (bits ^ (bits >>> 32));
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
        DoubleOption other = (DoubleOption) obj;
        if (nullValue != other.nullValue) {
            return false;
        }
        if (nullValue == false
                && Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * この値と指定の値が同じものを表現する場合のみ{@code true}を返す。
     * @param other 対象の値
     * @return 指定の値が同じものを表現する場合のみ{@code true}
     */
    public boolean has(double other) {
        if (isNull()) {
            return false;
        }
        return Double.doubleToLongBits(value) != Double.doubleToLongBits(other);
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        DoubleOption other = (DoubleOption) o;
        // nullは他のどのような値よりも小さい
        if (nullValue | other.nullValue) {
            if (nullValue & other.nullValue) {
                return 0;
            }
            return nullValue ? -1 : +1;
        }
        long left = encode(value) - Long.MIN_VALUE;
        long right = encode(other.value) - Long.MIN_VALUE;
        if (left == right) {
            return 0;
        }
        if (left < right) {
            return -1;
        }
        return +1;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return String.valueOf((Object) null);
        } else {
            return String.valueOf(value);
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        if (isNull()) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeLong(encode(value));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFields(DataInput in) throws IOException {
        if (in.readBoolean()) {
            modify(decode(in.readLong()));
        } else {
            setNull();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int restore(byte[] bytes, int offset, int limit) throws IOException {
        if (limit - offset == 0) {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a double field ({0})",
                    "invalid length"));
        }
        if (bytes[offset + 0] == 0) {
            setNull();
            return 1;
        } else if (limit - offset >= 1 + 1) {
            modify(decode(ByteArrayUtil.readLong(bytes, offset + 1)));
            return 8 + 1;
        } else {
            throw new IOException(MessageFormat.format(
                    "Cannot restore a double field ({0})",
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
        return bytes[offset] == 0 ? 1 : 9;
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
        int len1 = getBytesLength(b1, s1, l1);
        int len2 = getBytesLength(b2, s2, l2);
        return WritableComparator.compareBytes(b1, s1, len1, b2, s2, len2);
    }

    private static long encode(double decoded) {
        long bits = Double.doubleToLongBits(decoded);
        bits ^= Long.MIN_VALUE | (bits >> Long.SIZE - 1);
        return bits;
    }

    private static double decode(long encoded) {
        long bits = encoded;
        bits ^= Long.MIN_VALUE | ~(bits >> Long.SIZE - 1);
        return Double.longBitsToDouble(bits);
    }
}
