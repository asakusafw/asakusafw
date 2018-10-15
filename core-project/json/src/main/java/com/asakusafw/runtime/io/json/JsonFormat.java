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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.SerializedString;

/**
 * Provides {@link JsonInput} and {@link JsonOutput}.
 * @param <T> the data type
 * @since 0.10.3
 */
public class JsonFormat<T> {

    /**
     * The default {@link JsonFactory} instance.
     */
    public static final JsonFactory DEFAULT_JSON_FACTORY = new JsonFactory();

    /**
     * The default charset.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * The default line separator.
     */
    public static final LineSeparator DEFAULT_LINE_SEPARATOR = LineSeparator.UNIX;

    /**
     * The default behavior of whether or not use plain style to decimal numbers.
     */
    public static final boolean DEFAULT_USE_PLAIN_DECIMAL = false;

    /**
     * The default behavior of whether or not escaping no-ASCII characters.
     */
    public static final boolean DEFAULT_ESCAPE_NO_ASCII_CHARACTER = false;

    /**
     * The default error action of unknown properties.
     */
    public static final ErrorAction DEFAULT_ON_UNKNOWN_INPUT = ErrorAction.ERROR;

    private static final Map<Charset, JsonEncoding> ENCODING_MAP;
    static {
        Map<Charset, JsonEncoding> map = new HashMap<>();
        for (JsonEncoding encoding : JsonEncoding.values()) {
            map.put(Charset.forName(encoding.getJavaName()), encoding);
        }
        ENCODING_MAP = map;
    }

    /**
     * The default input options.
     */
    public static final Set<InputOption> DEFAULT_INPUT_OPTIONS = Collections.unmodifiableSet(EnumSet.of(
            InputOption.ENABLE_SOURCE_POSITION,
            InputOption.ENABLE_RECORD_INDEX));

    /**
     * The default output options.
     */
    public static final Set<OutputOption> DEFAULT_OUTPUT_OPTIONS = Collections.emptySet();

    // TODO: make it configurable
    private final JsonFactory factory = new JsonFactory();

    private final List<PropertyInfo<T, ?>> properties;

    private final List<Pattern> excludes;

    private final Charset charset;

    private final LineSeparator lineSeparator;

    private final boolean usePlainDecimal;

    private final boolean escapeNoAsciiCharacter;

    private final ErrorAction onUnknownInput;

    /**
     * Creates a new instance.
     * @param builder the builder of this
     */
    protected JsonFormat(Builder<T> builder) {
        this.properties = new ArrayList<>(builder.properties);
        this.excludes = new ArrayList<>(builder.excludes);
        this.charset = builder.charset;
        this.lineSeparator = builder.lineSeparator;
        this.usePlainDecimal = builder.usePlainDecimal;
        this.escapeNoAsciiCharacter = builder.escapeNoAsciiCharacter;
        this.onUnknownInput = builder.onUnknownInput;
    }

    /**
     * Creates a new builder.
     * @param <T> the data type
     * @param dataType the target data type
     * @return the created builder
     */
    public static <T> Builder<T> builder(Class<T> dataType) {
        return new Builder<>(dataType);
    }

    /**
     * Opens {@link JsonInput} for the given input.
     * @param path the source path name
     * @param input the source input stream
     * @param options input options
     * @return the opened {@link JsonInput}
     * @throws IOException if I/O error was occurred while initializing the reader
     */
    public JsonInput<T> open(
            String path, InputStream input, Collection<? extends InputOption> options) throws IOException {
        JsonEncoding encoding = ENCODING_MAP.get(charset);
        if (encoding != null) {
            // TODO: can detect charset?
            return createInput(path, factory.createParser(input), options);
        } else {
            return open(path, new InputStreamReader(input, charset), options);
        }
    }

    /**
     * Opens {@link JsonOutput} for the given output.
     * @param path the source path name
     * @param output the destination output stream
     * @param options output options
     * @return the opened {@link JsonOutput}
     * @throws IOException if I/O error was occurred while initializing the writer
     */
    public JsonOutput<T> open(
            String path, OutputStream output, Collection<? extends OutputOption> options) throws IOException {
        JsonEncoding encoding = ENCODING_MAP.get(charset);
        if (encoding != null) {
            return open(path, factory.createGenerator(output, encoding), options);
        } else {
            return open(path, new OutputStreamWriter(output, charset), options);
        }
    }

    /**
     * Opens {@link JsonInput} for the given input.
     * @param path the source path name
     * @param input the source input reader
     * @param options input options
     * @return the opened {@link JsonInput}
     * @throws IOException if I/O error was occurred while initializing the reader
     */
    public JsonInput<T> open(
            String path, Reader input, Collection<? extends InputOption> options) throws IOException {
        return createInput(path, factory.createParser(input), options);
    }

    /**
     * Opens {@link JsonOutput} for the given output.
     * @param path the source path name
     * @param output the destination output writer
     * @param options output options
     * @return the opened {@link JsonOutput}
     * @throws IOException if I/O error was occurred while initializing the writer
     */
    public JsonOutput<T> open(
            String path, Writer output, Collection<? extends OutputOption> options) throws IOException {
        return open(path, factory.createGenerator(output), options);
    }

    private JsonInput<T> createInput(String path, JsonParser parser, Collection<? extends InputOption> options) {
        configure(parser, JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        configure(parser, JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        configure(parser, JsonParser.Feature.ALLOW_COMMENTS, true);
        configure(parser, JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        configure(parser, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        configure(parser, JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return new InputDriver<>(
                path,
                parser,
                properties.stream().map(PropertyInfo::forInput).collect(Collectors.toList()),
                excludes.stream().map(Pattern::asPredicate).reduce(Predicate::or).orElse(s -> false),
                onUnknownInput,
                options.contains(InputOption.ENABLE_SOURCE_POSITION),
                options.contains(InputOption.ENABLE_RECORD_INDEX));
    }

    private static void configure(JsonParser parser, JsonParser.Feature feature, boolean enabled) {
        if (feature.enabledByDefault() == enabled) {
            return;
        }
        parser.configure(feature, enabled);
    }

    private JsonOutput<T> open(String path, JsonGenerator generator, Collection<? extends OutputOption> options) {
        configure(generator, JsonGenerator.Feature.AUTO_CLOSE_TARGET, true);
        configure(generator, JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true);
        configure(generator, JsonGenerator.Feature.ESCAPE_NON_ASCII, escapeNoAsciiCharacter);
        configure(generator, JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        configure(generator, JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, true);
        configure(generator, JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, usePlainDecimal);
        configure(generator, JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
        generator.setRootValueSeparator(new SerializedString(lineSeparator.getSequence()));
        return new OutputDriver<>(
                path,
                generator,
                properties.stream().map(PropertyInfo::forOutput).collect(Collectors.toList()));
    }

    private static void configure(JsonGenerator generator, JsonGenerator.Feature feature, boolean enabled) {
        if (feature.enabledByDefault() == enabled) {
            return;
        }
        generator.configure(feature, enabled);
    }

    /**
     * A builder for {@link JsonFormat}.
     * @param <T> the data type
     * @since 0.10.3
     */
    public static class Builder<T> {

        final Class<T> dataType;

        final List<PropertyInfo<T, ?>> properties = new ArrayList<>();

        final List<Pattern> excludes = new ArrayList<>();

        Charset charset = DEFAULT_CHARSET;

        LineSeparator lineSeparator = DEFAULT_LINE_SEPARATOR;

        boolean usePlainDecimal = DEFAULT_USE_PLAIN_DECIMAL;

        boolean escapeNoAsciiCharacter = DEFAULT_ESCAPE_NO_ASCII_CHARACTER;

        ErrorAction onUnknownInput = DEFAULT_ON_UNKNOWN_INPUT;

        /**
         * Creates a new instance.
         * @param dataType the target data type
         */
        public Builder(Class<T> dataType) {
            this.dataType = dataType;
        }

        /**
         * Adds a property.
         * @param <P> the property type
         * @param extractor the property extractor
         * @param definition the property definition
         * @return this
         */
        public <P> Builder<T> withProperty(
                Function<? super T, ? extends P> extractor,
                PropertyDefinition<P> definition) {
            this.properties.add(new PropertyInfo<>(extractor, definition));
            return this;
        }

        /**
         * Adds property name patterns to ignore from input.
         * @param values the property name patterns
         * @return this
         */
        public Builder<T> withExclude(Pattern... values) {
            Collections.addAll(this.excludes, values);
            return this;
        }

        /**
         * Sets the charset.
         * @param value the new value
         * @return this
         */
        public Builder<T> withCharset(Charset value) {
            this.charset = value;
            return this;
        }

        /**
         * Sets the line separator (for output only).
         * @param value the new value
         * @return this
         */
        public Builder<T> withLineSeparator(LineSeparator value) {
            this.lineSeparator = value;
            return this;
        }

        /**
         * Sets the decimal output style (for output only).
         * @param value {@code true} if never use ten's exponentials, otherwise {@code false}
         * @return this
         */
        public Builder<T> withUsePlainDecimal(boolean value) {
            this.usePlainDecimal = value;
            return this;
        }

        /**
         * Sets whether or not no-ASCII characters should be escaped (for output only).
         * @param value {@code true} if escape no-ASCII characters, otherwise {@code false}
         * @return this
         */
        public Builder<T> withEscapeNoAsciiCharacter(boolean value) {
            this.escapeNoAsciiCharacter = value;
            return this;
        }

        /**
         * Sets the error action of unknown properties.
         * @param value the new value
         * @return this
         */
        public Builder<T> withOnUnknownInput(ErrorAction value) {
            this.onUnknownInput = value;
            return this;
        }

        /**
         * Builds an instance.
         * @return the created instance
         */
        public JsonFormat<T> build() {
            return new JsonFormat<>(this);
        }
    }

    /**
     * Options for input.
     * @since 0.10.3
     */
    public enum InputOption {

        /**
         * Source position tracking is whether or not enabled.
         */
        ENABLE_SOURCE_POSITION,

        /**
         * Record index tracking is whether or not enabled.
         */
        ENABLE_RECORD_INDEX,
    }

    /**
     * Options for output.
     * @since 0.10.3
     */
    public enum OutputOption {
        // no special members
    }

    private static class PropertyInfo<TRecord, TProperty> {

        final Function<? super TRecord, ? extends TProperty> extractor;

        final PropertyDefinition<TProperty> definition;

        PropertyInfo(
                Function<? super TRecord, ? extends TProperty> extractor,
                PropertyDefinition<TProperty> definition) {
            this.extractor = extractor;
            this.definition = definition;
        }

        InputDriver.PropertyDriver<TRecord, TProperty> forInput() {
            return new InputDriver.PropertyDriver<>(extractor, definition);
        }

        OutputDriver.PropertyDriver<TRecord, TProperty> forOutput() {
            return new OutputDriver.PropertyDriver<>(extractor, definition);
        }
    }
}
