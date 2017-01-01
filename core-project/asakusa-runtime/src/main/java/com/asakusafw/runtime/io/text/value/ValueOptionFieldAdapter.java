/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.text.value;

import java.util.function.Supplier;

import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.io.text.driver.FieldOutput;
import com.asakusafw.runtime.io.text.driver.MalformedFieldException;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An abstract implementation of {@link FieldAdapter} which accepts {@link ValueOption}.
 * @param <T> the value type
 * @since 0.9.1
 */
public abstract class ValueOptionFieldAdapter<T extends ValueOption<T>> implements FieldAdapter<T> {

    private String nullFormat;

    /**
     * Creates a new instance.
     */
    public ValueOptionFieldAdapter() {
        this(null);
    }

    /**
     * Creates a new instance.
     * @param nullFormat the {@code null} value format (nullable)
     */
    public ValueOptionFieldAdapter(String nullFormat) {
        this.nullFormat = nullFormat;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void clear(T property) {
        property.setNull();
    }

    @Override
    public void parse(CharSequence contents, T property) {
        if (contents == null || (nullFormat != null && nullFormat.contentEquals(contents))) {
            clear(property);
        } else {
            try {
                doParse(contents, property);
            } catch (IllegalArgumentException e) {
                throw new MalformedFieldException(property.toString(), e);
            }
        }
    }

    @Override
    public void emit(T property, FieldOutput output) {
        if (property.isNull()) {
            if (nullFormat == null) {
                output.putNull();
            } else {
                output.put(nullFormat);
            }
        } else {
            StringBuilder buffer = output.acquireBuffer();
            try {
                doEmit(property, buffer);
            } finally {
                output.releaseBuffer(buffer);
            }
        }
    }

    /**
     * Parses the given non-null character sequence and set the parsed value into property.
     * @param contents the contents, never {@code null}
     * @param property the destination property
     * @throws IllegalArgumentException if the character sequence is malformed for this field
     */
    protected abstract void doParse(CharSequence contents, T property);

    /**
     * Emits the given non-null property value into the string builder.
     * @param property the property value, never {@code null} nor represents {@code null}
     * @param output the destination buffer
     */
    protected abstract void doEmit(T property, StringBuilder output);

    /**
     * A basic implementation of builder for {@link ValueOptionFieldAdapter}.
     * @param <S> this builder type
     * @param <T> the build target type
     * @since 0.9.1
     */
    protected abstract static class BuilderBase<S extends BuilderBase<S, T>, T extends FieldAdapter<?>> {

        private String nullFormat;

        /**
         * Returns {@code this} object.
         * @return this
         */
        @SuppressWarnings("unchecked")
        protected S self() {
            return (S) this;
        }

        /**
         * Sets a sequence which represents {@code NULL} field.
         * @param newValue the new value
         * @return this
         */
        public S withNullFormat(String newValue) {
            this.nullFormat = newValue;
            return self();
        }

        /**
         * Returns the sequence which represents {@code NULL} field.
         * @return the null sequence, or {@code null} if it is not defined
         */
        protected String getNullFormat() {
            return nullFormat;
        }

        /**
         * Builds the {@link FieldAdapter}.
         * @return the created object
         */
        public abstract T build();

        /**
         * Returns a supplier of lazy {@link #build()}.
         * @return the created supplier
         */
        public Supplier<T> lazy() {
            return () -> build();
        }
    }
}
