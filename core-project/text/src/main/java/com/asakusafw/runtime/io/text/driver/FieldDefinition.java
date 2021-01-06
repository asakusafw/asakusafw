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
package com.asakusafw.runtime.io.text.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A definition of field.
 * @param <T> the property type
 * @since 0.9.1
 */
public final class FieldDefinition<T> {

    private final String name;

    private final Supplier<? extends FieldAdapter<? super T>> adapterSupplier;

    private final Boolean trimInput;

    private final Boolean skipEmptyInput;

    private final ErrorAction onMalformedInput;

    private final ErrorAction onUnmappableOutput;

    private final Collection<? extends FieldOutput.Option> outputOptions;

    FieldDefinition(
            String name,
            Supplier<? extends FieldAdapter<? super T>> adapterSupplier,
            Boolean trimInput,
            Boolean skipEmptyInput,
            ErrorAction onMalformedInput,
            ErrorAction onUnmappableOutput,
            Collection<? extends FieldOutput.Option> outputOptions) {
        this.name = name;
        this.adapterSupplier = adapterSupplier;
        this.trimInput = trimInput;
        this.skipEmptyInput = skipEmptyInput;
        this.onMalformedInput = onMalformedInput;
        this.onUnmappableOutput = onUnmappableOutput;
        this.outputOptions = Collections.unmodifiableList(new ArrayList<>(outputOptions));
    }

    /**
     * Creates a new builder.
     * @param <T> the property type
     * @param name the field name
     * @param adapterSupplier the adapter supplier
     * @return the created builder
     */
    public static <T> Builder<T> builder(String name, Supplier<? extends FieldAdapter<T>> adapterSupplier) {
        return new Builder<>(name, adapterSupplier);
    }

    /**
     * Returns the field name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a supplier of the field adapter.
     * @return the adapter supplier
     */
    public Supplier<? extends FieldAdapter<? super T>> getAdapterSupplier() {
        return adapterSupplier;
    }

    /**
     * Returns whether or not leading/trailing white-space characters are trimmed.
     * @return {@code true} if this trims input, otherwise {@code false}
     */
    public Optional<Boolean> getTrimInput() {
        return Optional.ofNullable(trimInput);
    }

    /**
     * Returns whether or not this skips empty inputs.
     * @return {@code true} if this skips empty inputs, otherwise {@code false}
     */
    public Optional<Boolean> getSkipEmptyInput() {
        return Optional.ofNullable(skipEmptyInput);
    }

    /**
     * Returns the error action for malformed inputs.
     * @return the error action for malformed inputs
     */
    public Optional<ErrorAction> getOnMalformedInput() {
        return Optional.ofNullable(onMalformedInput);
    }

    /**
     * Returns the error action for un-mappable outputs.
     * @return the error action for un-mappable outputs
     */
    public Optional<ErrorAction> getOnUnmappableOutput() {
        return Optional.ofNullable(onUnmappableOutput);
    }

    /**
     * Returns the output options.
     * @return the output options
     */
    public Collection<? extends FieldOutput.Option> getOutputOptions() {
        return outputOptions;
    }

    /**
     * A builder of {@link FieldDefinition}.
     * @since 0.9.1
     * @param <T> the property type
     */
    public static class Builder<T> {

        private final String name;

        private final Supplier<? extends FieldAdapter<? super T>> adapterSupplier;

        private Boolean trimInput = null;

        private Boolean skipEmptyInput = null;

        private ErrorAction onMalformedInput = null;

        private ErrorAction onUnmappableOutput = null;

        private final List<FieldOutput.Option> outputOptions = new ArrayList<>();

        /**
         * Creates a new instance.
         * @param name the field name
         * @param adapterSupplier the adapter supplier
         */
        public Builder(String name, Supplier<? extends FieldAdapter<? super T>> adapterSupplier) {
            this.name = name;
            this.adapterSupplier = adapterSupplier;
        }

        /**
         * Sets whether or not leading/trailing white-space characters are trimmed.
         * @param newValue {@code true} to enable, {@code false} to disable, or {@code null} to inherit the parent
         * @return this
         */
        public Builder<T> withTrimInput(Boolean newValue) {
            this.trimInput = newValue;
            return this;
        }

        /**
         * Sets whether or not this skips empty inputs.
         * @param newValue {@code true} to enable, {@code false} to disable, or {@code null} to inherit the parent
         * @return this
         */
        public Builder<T> withSkipEmptyInput(Boolean newValue) {
            this.skipEmptyInput = newValue;
            return this;
        }

        /**
         * Sets the error action for malformed inputs.
         * @param newValue the error action, or {@code null} if inherit the parent setting
         * @return this
         */
        public Builder<T> withOnMalformedInput(ErrorAction newValue) {
            this.onMalformedInput = newValue;
            return this;
        }

        /**
         * Sets the error action for un-mappable outputs.
         * @param newValue the error action, or {@code null} if inherit the parent setting
         * @return this
         */
        public Builder<T> withOnUnmappableOutput(ErrorAction newValue) {
            this.onUnmappableOutput = newValue;
            return this;
        }

        /**
         * Adds an output option.
         * @param newValue the output option
         * @return this
         */
        public Builder<T> withOutputOption(FieldOutput.Option newValue) {
            this.outputOptions.add(newValue);
            return this;
        }

        /**
         * Adds output options.
         * @param newValues the output options
         * @return this
         */
        public Builder<T> withOutputOptions(FieldOutput.Option... newValues) {
            Collections.addAll(this.outputOptions, newValues);
            return this;
        }

        /**
         * Builds a {@link FieldDefinition}.
         * @return the built object
         */
        public FieldDefinition<T> build() {
            return new FieldDefinition<>(
                    name, adapterSupplier,
                    trimInput, skipEmptyInput,
                    onMalformedInput, onUnmappableOutput,
                    outputOptions);
        }
    }
}
