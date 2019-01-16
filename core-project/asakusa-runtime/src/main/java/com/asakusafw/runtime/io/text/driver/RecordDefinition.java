/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.asakusafw.runtime.io.text.FieldReader;
import com.asakusafw.runtime.io.text.FieldWriter;
import com.asakusafw.runtime.io.text.TextInput;
import com.asakusafw.runtime.io.text.TextOutput;

/**
 * A definition of record.
 * @param <T> the record type
 * @since 0.9.1
 */
public final class RecordDefinition<T> {

    private static final Set<InputOption> DEFAULT_INPUT_OPTIONS = EnumSet.of(InputOption.FROM_FILE_HEAD);

    private static final Set<OutputOption> DEFAULT_OUTPUT_OPTIONS = Collections.emptySet();

    private final Class<? extends T> dataType;

    private final List<FieldInfo<T, ?>> fields;

    private final HeaderType headerType;

    private final boolean trimInput;

    private final boolean skipEmptyInput;

    private final ErrorAction onMalformedInput;

    private final ErrorAction onUnmappableOutput;

    private final ErrorAction onLessInput;

    private final ErrorAction onMoreInput;

    RecordDefinition(
            Class<? extends T> dataType, List<FieldInfo<T, ?>> fields,
            HeaderType headerType,
            boolean trimInput, boolean skipEmptyInput,
            ErrorAction onMalformedInput, ErrorAction onUnmappableOutput,
            ErrorAction onLessInput, ErrorAction onMoreInput) {
        this.dataType = dataType;
        this.fields = new ArrayList<>(fields);
        this.headerType = headerType;
        this.trimInput = trimInput;
        this.skipEmptyInput = skipEmptyInput;
        this.onMalformedInput = onMalformedInput;
        this.onUnmappableOutput = onUnmappableOutput;
        this.onLessInput = onLessInput;
        this.onMoreInput = onMoreInput;
    }

    /**
     * Returns a new builder.
     * @param <T> the data type
     * @param dataType the data type
     * @return the created builder
     */
    public static <T> Builder<T> builder(Class<T> dataType) {
        return new Builder<>(dataType);
    }

    /**
     * Returns the number of fields in this record.
     * @return the number of fields
     */
    public int getNumberOfFields() {
        return fields.size();
    }

    /**
     * Creates a new {@link TextInput} from this definition with default options.
     * @param reader the formatted text reader
     * @param path the text file path
     * @return the created input
     */
    public TextInput<T> newInput(FieldReader reader, String path) {
        return newInput(reader, path, DEFAULT_INPUT_OPTIONS);
    }

    /**
     * Creates a new {@link TextInput} from this definition.
     * @param reader the formatted text reader
     * @param path the text file path
     * @param options the input options
     * @return the created input
     */
    public TextInput<T> newInput(FieldReader reader, String path, Collection<? extends InputOption> options) {
        List<InputDriver.FieldDriver<T, ?>> fieldDrivers = new ArrayList<>();
        for (FieldInfo<T, ?> info : fields) {
            fieldDrivers.add(toInput(info));
        }
        return new InputDriver<>(
                reader, path,
                dataType, fieldDrivers,
                headerType.getInput(),
                trimInput, skipEmptyInput,
                onLessInput, onMoreInput,
                options.contains(InputOption.FROM_FILE_HEAD));
    }

    /**
     * Creates a new {@link TextOutput} from this definition.
     * @param writer the formatted text writer
     * @param path the text file path
     * @return the created input
     */
    public TextOutput<T> newOutput(FieldWriter writer, String path) {
        return newOutput(writer, path, DEFAULT_OUTPUT_OPTIONS);
    }

    /**
     * Creates a new {@link TextOutput} from this definition.
     * @param writer the formatted text writer
     * @param path the text file path
     * @param options the output options
     * @return the created output
     */
    public TextOutput<T> newOutput(FieldWriter writer, String path, Collection<? extends OutputOption> options) {
        List<OutputDriver.FieldDriver<T, ?>> fieldDrivers = new ArrayList<>();
        for (FieldInfo<T, ?> info : fields) {
            fieldDrivers.add(toOutput(info));
        }
        return new OutputDriver<>(
                writer, path,
                dataType, fieldDrivers,
                headerType.getOutput(),
                onUnmappableOutput);
    }

    private <U> InputDriver.FieldDriver<T, U> toInput(FieldInfo<T, U> info) {
        FieldDefinition<U> def = info.definition;
        return new InputDriver.FieldDriver<>(
                def.getName(), info.extractor, def.getAdapterSupplier().get(),
                def.getTrimInput().orElse(trimInput),
                def.getSkipEmptyInput().orElse(skipEmptyInput),
                def.getOnMalformedInput().orElse(onMalformedInput));
    }

    private <U> OutputDriver.FieldDriver<T, U> toOutput(FieldInfo<T, U> info) {
        FieldDefinition<U> def = info.definition;
        return new OutputDriver.FieldDriver<>(
                def.getName(), info.extractor, def.getAdapterSupplier().get(),
                def.getOnUnmappableOutput().orElse(onUnmappableOutput),
                def.getOutputOptions());
    }

    /**
     * A builder of {@link RecordDefinition}.
     * @since 0.9.1
     * @param <T> the record type
     */
    public static class Builder<T> {

        private final Class<? extends T> dataType;

        private final List<FieldInfo<T, ?>> fields = new ArrayList<>();

        private HeaderType headerType = HeaderType.NOTHING;

        private boolean trimInput = false;

        private boolean skipEmptyInput = false;

        private ErrorAction onMalformedInput = ErrorAction.ERROR;

        private ErrorAction onUnmappableOutput = ErrorAction.ERROR;

        private ErrorAction onLessInput = ErrorAction.ERROR;

        private ErrorAction onMoreInput = ErrorAction.ERROR;

        /**
         * Creates a new instance.
         * @param dataType the data type
         */
        public Builder(Class<? extends T> dataType) {
            this.dataType = dataType;
        }

        /**
         * Adds a field.
         * @param <S> the property type
         * @param extractor the property extractor
         * @param definition the field definition
         * @return this
         */
        public <S> Builder<T> withField(Function<? super T, ? extends S> extractor, FieldDefinition<S> definition) {
            fields.add(new FieldInfo<>(extractor, definition));
            return this;
        }

        /**
         * Sets the header type.
         * @param newValue the header type
         * @return this
         */
        public Builder<T> withHeaderType(HeaderType newValue) {
            this.headerType = newValue;
            return this;
        }

        /**
         * Sets whether or not leading/trailing white-space characters are trimmed.
         * @param newValue {@code true} to enable, or {@code false} to disable
         * @return this
         */
        public Builder<T> withTrimInput(boolean newValue) {
            this.trimInput = newValue;
            return this;
        }

        /**
         * Sets whether or not this skips empty inputs.
         * @param newValue {@code true} to enable, or {@code false} to disable
         * @return this
         */
        public Builder<T> withSkipEmptyInput(boolean newValue) {
            this.skipEmptyInput = newValue;
            return this;
        }

        /**
         * Sets the error action for malformed inputs.
         * @param newValue the error action
         * @return this
         */
        public Builder<T> withOnMalformedInput(ErrorAction newValue) {
            this.onMalformedInput = newValue;
            return this;
        }

        /**
         * Sets the error action for un-mappable outputs.
         * @param newValue the error action
         * @return this
         */
        public Builder<T> withOnUnmappableOutput(ErrorAction newValue) {
            this.onUnmappableOutput = newValue;
            return this;
        }

        /**
         * Sets the error action for record only has less input fields.
         * @param newValue the error action
         * @return this
         */
        public Builder<T> withOnLessInput(ErrorAction newValue) {
            this.onLessInput = newValue;
            return this;
        }

        /**
         * Sets the error action for record has extra input fields.
         * @param newValue the error action
         * @return this
         */
        public Builder<T> withOnMoreInput(ErrorAction newValue) {
            this.onMoreInput = newValue;
            return this;
        }

        /**
         * Builds a {@link RecordDefinition}.
         * @return the built object
         */
        public RecordDefinition<T> build() {
            return new RecordDefinition<>(
                    dataType, fields,
                    headerType,
                    trimInput, skipEmptyInput,
                    onMalformedInput, onUnmappableOutput,
                    onLessInput, onMoreInput);
        }
    }

    private static class FieldInfo<TRecord, TProperty> {

        final Function<? super TRecord, ? extends TProperty> extractor;

        final FieldDefinition<TProperty> definition;

        FieldInfo(
                Function<? super TRecord, ? extends TProperty> extractor,
                FieldDefinition<TProperty> definition) {
            this.extractor = extractor;
            this.definition = definition;
        }
    }
}
