/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.json.value;

import java.io.IOException;
import java.util.function.Supplier;

import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An abstract implementation of {@link PropertyAdapter} for {@link ValueOption}.
 * @param <T> the value type
 * @since 0.10.3
 */
public abstract class ValueOptionPropertyAdapter<T extends ValueOption<T>> implements PropertyAdapter<T> {

    /**
     * The default value of {@code null} style.
     */
    public static final NullStyle DEFAULT_NULL_STYLE = NullStyle.VALUE;

    private final NullStyle nullStyle;

    /**
     * Creates a new instance.
     * @param builder the source builder
     */
    protected ValueOptionPropertyAdapter(BuilderBase<?, ?> builder) {
        this.nullStyle = builder.nullStyle;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void absent(T property) {
        property.setNull();
    }

    @Override
    public final void read(ValueReader reader, T property) throws IOException {
        if (reader.isNull()) {
            absent(property);
        } else {
            doRead(reader, property);
        }
    }

    @Override
    public void write(T property, ValueWriter writer) throws IOException {
        if (property.isNull()) {
            if (nullStyle == NullStyle.VALUE) {
                writer.writeNull();
            }
        } else {
            doWrite(property, writer);
        }
    }

    /**
     * Reads a value and set it into the given property.
     * @param reader the source reader, never holds {@link ValueReader#isNull() null}
     * @param property the destination property
     * @throws IOException IOException if I/O error was occurred
     * @throws RuntimeException if the property value is not a suitable for the destination property
     */
    protected abstract void doRead(ValueReader reader, T property) throws IOException;

    /**
     * Writes a value from the given property.
     * @param property the source property, never holds {@link ValueOption#isNull() null}
     * @param writer the destination writer
     * @throws IOException if I/O error was occurred
     */
    protected abstract void doWrite(T property, ValueWriter writer) throws IOException;

    /**
     * A basic implementation of builder for {@link ValueOptionPropertyAdapter}.
     * @param <S> this builder type
     * @param <T> the build target type
     * @since 0.10.3
     */
    protected abstract static class BuilderBase<S extends BuilderBase<S, T>, T extends PropertyAdapter<?>> {

        NullStyle nullStyle = DEFAULT_NULL_STYLE;

        /**
         * Returns {@code this} object.
         * @return this
         */
        @SuppressWarnings("unchecked")
        protected S self() {
            return (S) this;
        }

        /**
         * Sets the output style for {@code null}.
         * @param value the new value
         * @return this
         */
        public S withNullStyle(NullStyle value) {
            this.nullStyle = value;
            return self();
        }

        /**
         * Builds the {@link PropertyAdapter}.
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

    /**
     * A kind of output style of {@code null} properties.
     * @since 0.10.3
     */
    public enum NullStyle {

        /**
         * Places explicit {@code null} symbol.
         */
        VALUE,

        /**
         * Skip {@code null} properties.
         */
        ABSENT,
    }
}
