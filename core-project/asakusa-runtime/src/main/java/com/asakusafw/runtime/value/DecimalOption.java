/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.util.Arrays;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.runtime.io.util.WritableRawComparable;

/**
 * {@code null}値を許容する10進数。
 * @since 0.1.0
 * @version 0.7.0
 */
public final class DecimalOption extends ValueOption<DecimalOption> {

    private static final int HEAD_NULL = 0x00;

    private static final int MASK_PRESENT = 0x80;

    private static final int MASK_PLUS = 0x40;

    private static final ThreadLocal<DecimalBuffer> BUFFER_MAIN = new ThreadLocal<DecimalBuffer>() {
        @Override
        protected DecimalBuffer initialValue() {
            return new DecimalBuffer();
        }
    };

    private static final ThreadLocal<DecimalBuffer> BUFFER_SUB = new ThreadLocal<DecimalBuffer>() {
        @Override
        protected DecimalBuffer initialValue() {
            return new DecimalBuffer();
        }
    };

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
            out.writeByte(HEAD_NULL);
        } else {
            DecimalBuffer buffer = BUFFER_MAIN.get();
            buffer.set(entity);

            int head = MASK_PRESENT;
            if (buffer.plus) {
                head |= MASK_PLUS;
            }
            out.writeByte(head);
            WritableUtils.writeVInt(out, buffer.scale);
            byte[] bs = buffer.unsigned;
            int length = bs.length;
            for (int i = 0; i < bs.length; i++) {
                if (bs[i] == 0) {
                    length--;
                } else {
                    break;
                }
            }
            WritableUtils.writeVInt(out, length);
            if (length != 0) {
                out.write(bs, bs.length - length, length);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void readFields(DataInput in) throws IOException {
        int head = in.readByte() & 0xff;
        if ((head & MASK_PRESENT) == 0) {
            setNull();
            return;
        }
        boolean plus = (head & MASK_PLUS) != 0;
        int scale = WritableUtils.readVInt(in);
        int length = WritableUtils.readVInt(in);

        DecimalBuffer buffer = BUFFER_MAIN.get();
        byte[] target = buffer.setMeta(plus, scale, length);
        in.readFully(target, target.length - length, length);
        modify(buffer.toBigDecimal());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int restore(byte[] bytes, int offset, int limit) throws IOException {
        int cursor = offset;
        int head = bytes[cursor++] & 0xff;
        if ((head & MASK_PRESENT) == 0) {
            setNull();
        } else {
            boolean plus = (head & MASK_PLUS) != 0;
            int scale = WritableComparator.readVInt(bytes, cursor);
            cursor += WritableUtils.decodeVIntSize(bytes[cursor]);
            int length = WritableComparator.readVInt(bytes, cursor);
            cursor += WritableUtils.decodeVIntSize(bytes[cursor]);
            DecimalBuffer buffer = BUFFER_MAIN.get();
            buffer.set(plus, scale, bytes, cursor, length);
            cursor += length;
            modify(buffer.toBigDecimal());
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
            int head = bytes[cursor++] & 0xff;
            if ((head & MASK_PRESENT) != 0) {
                cursor += WritableUtils.decodeVIntSize(bytes[cursor]);
                int bytesLength = WritableComparator.readVInt(bytes, cursor);
                cursor += WritableUtils.decodeVIntSize(bytes[cursor]);
                cursor += bytesLength;
            }
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
            // head
            int cursor1 = s1;
            int cursor2 = s2;
            int h1 = b1[cursor1++] & 0xff;
            int h2 = b2[cursor2++] & 0xff;

            // nullity
            boolean null1 = (h1 & MASK_PRESENT) == 0;
            boolean null2 = (h2 & MASK_PRESENT) == 0;
            if (null1) {
                if (null2) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (null2) {
                return +1;
            }

            // sign
            boolean plus1 = (h1 & MASK_PLUS) != 0;
            boolean plus2 = (h2 & MASK_PLUS) != 0;
            if (plus1 && plus2 == false) {
                return +1;
            } else if (plus1 == false && plus2) {
                return -1;
            }

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

            DecimalBuffer d1 = BUFFER_MAIN.get();
            d1.set(plus1, scale1, b1, cursor1, bytesCount1);

            DecimalBuffer d2 = BUFFER_SUB.get();
            d2.set(plus2, scale2, b2, cursor2, bytesCount2);

            return DecimalBuffer.compare(d1, d2);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class DecimalBuffer {

        private static final byte[] EMPTY = new byte[0];

        boolean plus;

        int scale;

        byte[] unsigned = EMPTY;

        public DecimalBuffer() {
            return;
        }

        void set(BigDecimal newValue) {
            this.scale = newValue.scale();

            BigInteger unscaled = newValue.unscaledValue();
            if (unscaled.signum() >= 0) {
                this.plus = true;
            } else {
                this.plus = false;
                unscaled = unscaled.negate();
            }
            setUnsigned(unscaled);
        }

        void set(boolean newPlus, int newScale, byte[] data, int offset, int length) {
            byte[] target = setMeta(newPlus, newScale, length);
            System.arraycopy(data, offset, target, target.length - length, length);
        }

        byte[] setMeta(boolean newPlus, int newScale, int length) {
            this.plus = newPlus;
            this.scale = newScale;
            return ensureBuffer(length);
        }

        byte[] ensureBuffer(int length) {
            if (unsigned.length < length) {
                this.unsigned = new byte[length];
            } else if (unsigned.length > length) {
                Arrays.fill(this.unsigned, 0, unsigned.length - length, (byte) 0);
            }
            return unsigned;
        }

        BigDecimal toBigDecimal() {
            if (isZero()) {
                return new BigDecimal(BigInteger.ZERO, this.scale);
            }
            long compact = toUnsignedCompact();
            if (compact >= 0L) {
                return BigDecimal.valueOf(this.plus ? compact : -compact, this.scale);
            }
            return new BigDecimal(new BigInteger(this.plus ? +1 : -1, this.unsigned), this.scale);
        }

        private BigDecimal toUnsignedBigDecimal() {
            if (isZero()) {
                return new BigDecimal(BigInteger.ZERO, this.scale);
            }
            long compact = toUnsignedCompact();
            if (compact >= 0L) {
                return BigDecimal.valueOf(compact, this.scale);
            }
            return new BigDecimal(new BigInteger(1, this.unsigned), this.scale);
        }

        private long toUnsignedCompact() {
            byte[] bs = this.unsigned;
            int offset = 0;
            if (bs.length >= 8) {
                offset = bs.length - 8;
                for (int i = 0; i < offset; i++) {
                    if (bs[i] != 0) {
                        return -1L;
                    }
                }
                if ((bs[offset] & 0x80) != 0) {
                    return -1L;
                }
            }
            long result = 0;
            assert bs.length - offset <= 8;
            for (int i = offset; i < bs.length; i++) {
                result = (result << 8) | (bs[i] & 0xff);
            }
            assert result >= 0;
            return result;
        }

        private boolean isZero() {
            byte[] bs = this.unsigned;
            for (int i = 0; i < bs.length; i++) {
                if (bs[i] != 0) {
                    return false;
                }
            }
            return true;
        }

        private void setUnsigned(BigInteger value) {
            int sign = value.signum();
            byte[] target = this.unsigned;
            if (sign == 0) {
                Arrays.fill(target, (byte) 0);
                return;
            }
            assert sign > 0;

            int bits = value.bitLength();
            if (bits <= Integer.SIZE - 1) {
                int v = value.intValue();
                assert v > 0;
                byte[] bytes = ensureBuffer(4);
                int offset = bytes.length - 4;
                bytes[offset + 0] = (byte) ((v >> 24) & 0xff);
                bytes[offset + 1] = (byte) ((v >> 16) & 0xff);
                bytes[offset + 2] = (byte) ((v >>  8) & 0xff);
                bytes[offset + 3] = (byte) ((v >>  0) & 0xff);
            } else if (bits <= Long.SIZE - 1) {
                long v = value.longValue();
                assert v > 0;
                byte[] bytes = ensureBuffer(8);
                int offset = bytes.length - 8;
                bytes[offset + 0] = (byte) ((v >> 56) & 0xff);
                bytes[offset + 1] = (byte) ((v >> 48) & 0xff);
                bytes[offset + 2] = (byte) ((v >> 40) & 0xff);
                bytes[offset + 3] = (byte) ((v >> 32) & 0xff);
                bytes[offset + 4] = (byte) ((v >> 24) & 0xff);
                bytes[offset + 5] = (byte) ((v >> 16) & 0xff);
                bytes[offset + 6] = (byte) ((v >>  8) & 0xff);
                bytes[offset + 7] = (byte) ((v >>  0) & 0xff);
            } else {
                byte[] bytes = value.toByteArray();
                if (target.length <= bytes.length) {
                    this.unsigned = bytes;
                } else {
                    Arrays.fill(target, 0, target.length - bytes.length, (byte) 0);
                    System.arraycopy(bytes, 0, target, target.length - bytes.length, bytes.length);
                }
            }
        }

        public static int compare(DecimalBuffer o1, DecimalBuffer o2) {
            if (o1.plus) {
                if (o2.plus == false) {
                    return +1;
                }
            } else if (o2.plus) {
                return -1;
            }
            int unsignedCompare = compareAbsolute(o1, o2);
            if (o1.plus) {
                return unsignedCompare;
            } else {
                return -unsignedCompare;
            }
        }

        private static int compareAbsolute(DecimalBuffer o1, DecimalBuffer o2) {
            if (o1.isZero()) {
                if (o2.isZero() == false) {
                    return -1;
                }
            } else if (o2.isZero()) {
                return +1;
            }
            if (o1.scale == o2.scale) {
                return compareAbsolute(o1.unsigned, o2.unsigned);
            }
            return o1.toUnsignedBigDecimal().compareTo(o2.toUnsignedBigDecimal());
        }

        private static int compareAbsolute(byte[] a, byte[] b) {
            int aOffset = 0;
            int bOffset = 0;
            if (a.length > b.length) {
                aOffset = a.length - b.length;
            } else {
                bOffset = b.length - a.length;
            }
            for (int i = 0; i < aOffset; i++) {
                if (a[i] != 0) {
                    return +1;
                }
            }
            for (int i = 0; i < bOffset; i++) {
                if (b[i] != 0) {
                    return -1;
                }
            }
            for (int i = 0, n = Math.min(a.length, b.length); i < n; i++) {
                int aBits = a[i + aOffset] & 0xff;
                int bBits = b[i + bOffset] & 0xff;
                if (aBits > bBits) {
                    return +1;
                } else if (aBits < bBits) {
                    return -1;
                }
            }
            return 0;
        }
    }
}
