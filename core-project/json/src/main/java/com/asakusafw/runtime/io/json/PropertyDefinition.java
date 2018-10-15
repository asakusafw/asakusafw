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
package com.asakusafw.runtime.io.json;

import java.util.function.Supplier;

/**
 * Represents JSON properties.
 * @param <T> the property type
 * @see Builder
 * @since 0.10.3
 */
public class PropertyDefinition<T> {

    /**
     * The default value of {@link #getOnMalformedInput()}.
     */
    public static final ErrorAction DEFAULT_ON_MALFORMED_INPUT = ErrorAction.ERROR;

    /**
     * The default value of {@link #getOnMissingInput()}.
     */
    public static final ErrorAction DEFAULT_ON_MISSING_INPUT = ErrorAction.IGNORE;

    private final String name;

    private final Supplier<? extends PropertyAdapter<? super T>> adapterSupplier;

    private final ErrorAction onMalformedInput;

    private final ErrorAction onMissingInput;

    /**
     * Creates a new instance.
     * @param builder the source builder
     */
    protected PropertyDefinition(Builder<T> builder) {
        this.name = builder.name;
        this.adapterSupplier = builder.adapterSupplier;
        this.onMalformedInput = builder.onMalformedInput;
        this.onMissingInput = builder.onMissingInput;
    }

    /**
     * Creates a new builder.
     * @param <T> the property type
     * @param name the field name
     * @param adapterSupplier the adapter supplier
     * @return the created builder
     */
    public static <T> Builder<T> builder(String name, Supplier<? extends PropertyAdapter<? super T>> adapterSupplier) {
        return new Builder<>(name, adapterSupplier);
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the adapter supplier.
     * @return the adapter supplier
     */
    public Supplier<? extends PropertyAdapter<? super T>> getAdapterSupplier() {
        return adapterSupplier;
    }

    /**
     * Returns a new adapter.
     * @return the created adapter
     */
    public PropertyAdapter<? super T> getAdapter() {
        return adapterSupplier.get();
    }

    /**
     * Returns the error action kind of malformed input for this property.
     * @return the error action
     */
    public ErrorAction getOnMalformedInput() {
        return onMalformedInput;
    }

    /**
     * Returns the error action kind of missing this property.
     * @return the error action
     */
    public ErrorAction getOnMissingInput() {
        return onMissingInput;
    }

    /**
     * A builder for {@link PropertyDefinition}.
     * @param <T> the property type
     * @since 0.10.3
     */
    public static class Builder<T> {

        final String name;

        final Supplier<? extends PropertyAdapter<? super T>> adapterSupplier;

        ErrorAction onMalformedInput = DEFAULT_ON_MALFORMED_INPUT;

        ErrorAction onMissingInput = DEFAULT_ON_MISSING_INPUT;

        /**
         * Creates a new instance.
         * @param name the field name
         * @param adapterSupplier the adapter supplier
         */
        public Builder(String name, Supplier<? extends PropertyAdapter<? super T>> adapterSupplier) {
            this.name = name;
            this.adapterSupplier = adapterSupplier;
        }

        /**
         * Sets the error action kind of malformed input for this property.
         * @param value the error action kind
         * @return this
         */
        public Builder<T> withOnMalformedInput(ErrorAction value) {
            this.onMalformedInput = value;
            return this;
        }

        /**
         * Sets the error action kind of missing this property from the input.
         * @param value the error action kind
         * @return this
         */
        public Builder<T> withOnMissingInput(ErrorAction value) {
            this.onMissingInput = value;
            return this;
        }

        /**
         * Builds a {@link PropertyDefinition}.
         * @return the built object
         */
        public PropertyDefinition<T> build() {
            return new PropertyDefinition<>(this);
        }
    }
}
