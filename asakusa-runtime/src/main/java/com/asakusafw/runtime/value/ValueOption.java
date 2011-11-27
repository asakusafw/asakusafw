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

import org.apache.hadoop.io.WritableComparable;

/**
 * {@code null}値を許容する変更可能な値。
 * <p>
 * このクラスのサブクラスの実装は、基本的にスレッドセーフでない。
 * </p>
 * @param <V> 値の種類
 */
public abstract class ValueOption<V extends ValueOption<V>> implements WritableComparable<V>, Restorable {

    /**
     * この値が{@code null}を表す場合に{@code true}となる。
     * <p>
     * サブクラスのインスタンスで{@code null}以外の値が設定された場合、この値を{@code false}にすること。
     * </p>
     */
    protected boolean nullValue = true;

    /**
     * この値が{@code null}値を表す場合のみ{@code true}を返す。
     * @return この値が{@code null}値を表す場合のみ{@code true}
     */
    public final boolean isNull() {
        return nullValue;
    }

    /**
     * この値が{@code null}値であるかどうかを変更する。
     * @deprecated アプリケーションからは利用しない
     * @return 自身のオブジェクト
     */
    @Deprecated
    public final ValueOption<V> setNull() {
        this.nullValue = true;
        return this;
    }

    /**
     * このオブジェクトの内容を、指定のオブジェクトの内容で上書きする。
     * @param otherOrNull 上書きする内容、
     *     {@code null}の場合はこのオブジェクトが{@code null}値を表すようになる
     * @deprecated アプリケーションからは利用しない
     */
    @Deprecated
    public abstract void copyFrom(V otherOrNull);

    /**
     * この値と指定された値を比較し、小さいものをこの値の内容とする。
     * <p>
     * ただし、この値または比較対象の値が{@code null}を表す場合、
     * この値は{@code null}を表す値となる。
     * </p>
     * @param other 対象の値
     */
    @SuppressWarnings("deprecation")
    public final void min(V other) {
        if (this == other) {
            return;
        }
        if (this.isNull() || other.isNull()) {
            setNull();
        } else if (compareTo(other) > 0) {
            copyFrom(other);
        }
    }

    /**
     * この値と指定された値を比較し、大きなものをこの値の内容とする。
     * <p>
     * ただし、この値または比較対象の値が{@code null}を表す場合、
     * この値は{@code null}を表す値となる。
     * </p>
     * @param other 対象の値
     */
    @SuppressWarnings("deprecation")
    public final void max(V other) {
        if (this == other) {
            return;
        }
        if (this.isNull() || other.isNull()) {
            setNull();
        } else if (compareTo(other) < 0) {
            copyFrom(other);
        }
    }
}
