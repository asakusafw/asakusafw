/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

/**
 * Represents nullable and modifiable value.
 * Note that, the sub-classes may be thread unsafe.
 * @param <V> self type
 * @since 0.1.0
 * @version 0.9.1
 */
public abstract class ValueOption<V extends ValueOption<V>> implements WritableRawComparable, Restorable {

    /**
     * Whether this value represents {@code null} or not.
     */
    protected boolean nullValue = true;

    /**
     * Returns whether this object represents {@code null} or not.
     * @return {@code true} if this object represents {@code null}, otherwise {@code false}
     */
    public final boolean isNull() {
        return nullValue;
    }

    /**
     * Returns whether this object has any value or not.
     * @return {@code true} if this object DOES NOT represent {@code null}, otherwise {@code false}
     * @since 0.9.1
     */
    public final boolean isPresent() {
        return nullValue == false;
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
     * Returns the given value if this represents {@code null}.
     * @param other the target value which will be returned if this object represents {@code null}
     * @return the given value if this represents {@code null}, otherwise this
     * @since 0.9.1
     */
    @SuppressWarnings("unchecked")
    public final V orOption(V other) {
        if (nullValue) {
            return other;
        }
        return (V) this;
    }

    /**
     * Copies the value from the specified values into this.
     * @param otherOrNull the source object, or {@code null} to make this value represent {@code null}
     * @deprecated Application developer should not use this method directly
     */
    @Deprecated
    public abstract void copyFrom(V otherOrNull);

    /**
     * Sets the specified value to this object only if the specified value is less than this object. However, if either
     * this object or the specified value represents {@code null}, this object will also turn to {@code null}.
     * @param other the target value
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
     * Sets the specified value to this object only if the specified value is greater than this object. However, if
     * either this object or the specified value represents {@code null}, this object will also turn to {@code null}.
     * @param other the target value
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
