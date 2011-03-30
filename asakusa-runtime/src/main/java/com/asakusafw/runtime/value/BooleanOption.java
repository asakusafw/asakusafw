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
import java.text.MessageFormat;

/**
 * {@code null}値を許容する{@code boolean}値。
 */
public final class BooleanOption extends ValueOption<BooleanOption> {

    private static final int TRUE_HASHCODE = 1231;

    private static final int FALSE_HASHCODE = 1237;

    private boolean value;

    /**
     * このオブジェクトが表現する値を返す。
     * @return このオブジェクトが表現する値
     * @throws NullPointerException この値が{@code null}を表現する場合
     */
    public boolean get() {
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
    public boolean or(boolean alternate) {
        if (nullValue) {
            return alternate;
        }
        return value;
    }

    /**
     * このオブジェクトが表現する値を変更する。
     * @param newValue 変更後の値
     * @return 自身のオブジェクト
     * @see ValueOption#setNull()
     * @deprecated アプリケーションからは利用しない
     */
    @Deprecated
    public BooleanOption modify(boolean newValue) {
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
     * この値と指定の値が同じものを表現する場合のみ{@code true}を返す。
     * @param other 対象の値
     * @return 指定の値が同じものを表現する場合のみ{@code true}
     */
    public boolean has(boolean other) {
        if (isNull()) {
            return false;
        }
        return value == other;
    }

    @Override
    public int compareTo(BooleanOption o) {
        // nullは他のどのような値よりも小さい
        if (nullValue | o.nullValue) {
            if (nullValue & o.nullValue) {
                return 0;
            }
            return nullValue ? -1 : +1;
        }
        // true > false
        if (value ^ o.value) {
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

    /**
     * このクラスの直列化された形式から、占有しているバイト長を返す。
     * @param bytes 対象のバイト配列
     * @param offset バイト配列の開始位置
     * @param length バイト配列の制限長
     * @return 比較結果
     */
    public static int getBytesLength(byte[] bytes, int offset, int length) {
        return 1;
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
        return ByteArrayUtil.compare(b1[s1], b2[s2]);
    }
}
