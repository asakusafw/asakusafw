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

import com.asakusafw.runtime.io.util.WritableRawComparable;

//TODO i18n
/**
 * Represents nullable and modifiable value.
 * Note that, the sub-classes may be thread unsafe.
 * @param <V> self type
 * @since 0.1.0
 * @version 0.2.5
 */
public abstract class ValueOption<V extends ValueOption<V>> implements WritableRawComparable, Restorable {

    /**
     * Whether this value represents {@code null} or not.
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
     * Makes this value represent {@code null}.
     * @deprecated Application developer should not use this method directly
     * @return this
     */
    @Deprecated
    public final ValueOption<V> setNull() {
        this.nullValue = true;
        return this;
    }

    /**
     * Copies the value from the specified values into this.
     * @param otherOrNull the source object, or {@code null} to make this value represent {@code null}
     * @deprecated Application developer should not use this method directly
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
