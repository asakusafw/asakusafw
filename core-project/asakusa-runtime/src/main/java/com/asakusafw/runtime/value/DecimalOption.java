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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Arrays;

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
        if (nullValue) {
            WritableUtils.writeVLong(out, -1);
        } else {
            BigDecimal decimal = entity;
            WritableUtils.writeVInt(out, decimal.precision());
            WritableUtils.writeVInt(out, decimal.scale());
            BigInteger unscaled = decimal.unscaledValue();
            byte[] bytes = unscaled.toByteArray();
            WritableUtils.writeVInt(out, bytes.length);
            out.write(bytes);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFields(DataInput in) throws IOException {
        int precision = WritableUtils.readVInt(in);
        if (precision == -1) {
            setNull();
        } else {
            int scale = WritableUtils.readVInt(in);
            int byteCount = WritableUtils.readVInt(in);
            byte[] bytes = new byte[byteCount];
            in.readFully(bytes);
            modify(new BigDecimal(new BigInteger(bytes), scale, new MathContext(precision)));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int restore(byte[] bytes, int offset, int limit) throws IOException {
        int cursor = offset;
        int precision = WritableComparator.readVInt(bytes, cursor);
        cursor += WritableUtils.decodeVIntSize(bytes[cursor]);
        if (precision < 0) {
            setNull();
        } else {
            int scale = WritableComparator.readVInt(bytes, cursor);
            cursor += WritableUtils.decodeVIntSize(bytes[cursor]);

            int bytesCount = WritableComparator.readVInt(bytes, cursor);
            cursor += WritableUtils.decodeVIntSize(bytes[cursor]);

            byte[] unscaled = Arrays.copyOfRange(bytes, cursor, cursor + bytesCount);
            cursor += bytesCount;

            modify(new BigDecimal(new BigInteger(unscaled), scale, new MathContext(precision)));
        }
        return cursor - offset;
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
        try {
            int cursor = offset;
            int precSize = WritableUtils.decodeVIntSize(bytes[cursor]);
            if (WritableComparator.readVInt(bytes, offset) < 0) {
                return precSize;
            }
            cursor += precSize;
            cursor += WritableUtils.decodeVIntSize(bytes[cursor]);
            int bytesCount = WritableComparator.readVInt(bytes, cursor);
            cursor += WritableUtils.decodeVIntSize(bytes[cursor]);
            cursor += bytesCount;
            return cursor - offset;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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
        try {
            // precision
            int cursor1 = s1;
            int cursor2 = s2;
            int prec1 = WritableComparator.readVInt(b1, cursor1);
            int prec2 = WritableComparator.readVInt(b2, cursor2);
            if (prec1 < 0) {
                if (prec2 < 0) {
                    return 0;
                }
                return -1;
            } else if (prec2 < 0) {
                return +1;
            }
            cursor1 += WritableUtils.decodeVIntSize(b1[cursor1]);
            cursor2 += WritableUtils.decodeVIntSize(b2[cursor2]);

            // scale
            int scale1 = WritableComparator.readVInt(b1, cursor1);
            int scale2 = WritableComparator.readVInt(b2, cursor2);
            cursor1 += WritableUtils.decodeVIntSize(b1[cursor1]);
            cursor2 += WritableUtils.decodeVIntSize(b2[cursor2]);

            // bytesCount
            int bytesCount1 = WritableComparator.readVInt(b1, cursor1);
            int bytesCount2 = WritableComparator.readVInt(b2, cursor2);
            cursor1 += WritableUtils.decodeVIntSize(b1[cursor1]);
            cursor2 += WritableUtils.decodeVIntSize(b2[cursor2]);

            // check sig
            if (b1[cursor1] < 0 && b2[cursor2] >= 0) {
                return -1;
            } else if (b1[cursor1] >= 0 && b2[cursor2] < 0) {
                return +1;
            }

            // bytes
            BigInteger unscale1 = new BigInteger(Arrays.copyOfRange(b1, cursor1, cursor1 + bytesCount1));
            BigInteger unscale2 = new BigInteger(Arrays.copyOfRange(b2, cursor2, cursor2 + bytesCount2));
            if (scale1 > scale2) {
                unscale2 = unscale2.multiply(BigInteger.TEN.pow(scale1 - scale2));
            } else if (scale1 < scale2) {
                unscale1 = unscale1.multiply(BigInteger.TEN.pow(scale2 - scale1));
            }
            return unscale1.compareTo(unscale2);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
