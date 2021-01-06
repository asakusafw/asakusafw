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
package com.asakusafw.runtime.io.text.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.asakusafw.runtime.io.text.LineSeparator;
import com.asakusafw.runtime.io.text.TextFormat;

/**
 * An implementation of {@link TextFormat} for  RFC4180 style CSV files.
 * @since 0.9.1
 */
public class CsvTextFormat implements TextFormat {

    /**
     * The default charset.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * The default line separator.
     */
    public static final LineSeparator DEFAULT_LINE_SEPARATOR = LineSeparator.WINDOWS;

    /**
     * The default field separator.
     */
    public static final char DEFAULT_FIELD_SEPARATOR = ',';

    /**
     * The default quote character.
     */
    public static final char DEFAULT_QUOTE_CHARACTER = '"';

    /**
     * The default value of whether or not line-feed characters can appear in field.
     */
    public static final boolean DEFAULT_ALLOW_LINE_FEED_IN_FIELD = false;

    /**
     * The default quote style.
     */
    public static final QuoteStyle DEFAULT_QUOTE_STYLE = QuoteStyle.NEEDED;

    private final Charset charset;

    private final LineSeparator lineSeparator;

    private final char fieldSeparator;

    private final char quoteCharacter;

    private final boolean allowLineFeedInField;

    private final QuoteStyle defaultQuoteStyle;

    private final QuoteStyle headerQuoteStyle;

    private final Supplier<? extends UnaryOperator<CharSequence>> inputTransformer;

    private final Supplier<? extends UnaryOperator<CharSequence>> outputTransformer;

    CsvTextFormat(
            Charset charset,
            LineSeparator lineSeparator, char fieldSeparator, char quoteCharacter,
            boolean allowLineFeedInQuote,
            QuoteStyle defaultQuoteStyle, QuoteStyle headerQuoteStyle,
            Supplier<? extends UnaryOperator<CharSequence>> inputTransformer,
            Supplier<? extends UnaryOperator<CharSequence>> outputTransformer) {
        this.charset = charset;
        this.lineSeparator = lineSeparator;
        this.fieldSeparator = fieldSeparator;
        this.quoteCharacter = quoteCharacter;
        this.allowLineFeedInField = allowLineFeedInQuote;
        this.defaultQuoteStyle = defaultQuoteStyle;
        this.headerQuoteStyle = headerQuoteStyle;
        this.inputTransformer = inputTransformer != null ? inputTransformer : () -> null;
        this.outputTransformer = outputTransformer != null ? outputTransformer : () -> null;
    }

    /**
     * Returns a new builder.
     * @return the created builder
     */
    public static final Builder builder() {
        return new Builder();
    }

    @Override
    public CsvFieldReader open(InputStream input) throws IOException {
        return open(new InputStreamReader(input, charset));
    }

    @Override
    public CsvFieldWriter open(OutputStream output) throws IOException {
        return open(new OutputStreamWriter(output, charset));
    }

    @Override
    public CsvFieldReader open(Reader input) throws IOException {
        return new CsvFieldReader(input,
                fieldSeparator, quoteCharacter,
                allowLineFeedInField,
                inputTransformer.get());
    }

    @Override
    public CsvFieldWriter open(Writer output) throws IOException {
        return new CsvFieldWriter(
                output,
                lineSeparator, fieldSeparator, quoteCharacter,
                allowLineFeedInField,
                defaultQuoteStyle, headerQuoteStyle,
                outputTransformer.get());
    }

    Charset getCharset() {
        return charset;
    }

    LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    /**
     * A builder of {@link CsvTextFormat}.
     * @since 0.9.1
     */
    public static class Builder {

        private Charset charset = DEFAULT_CHARSET;

        private LineSeparator lineSeparator = DEFAULT_LINE_SEPARATOR;

        private char fieldSeparator = DEFAULT_FIELD_SEPARATOR;

        private char quoteCharacter = DEFAULT_QUOTE_CHARACTER;

        private boolean allowLineFeedInField = DEFAULT_ALLOW_LINE_FEED_IN_FIELD;

        private QuoteStyle defaultQuoteStyle = DEFAULT_QUOTE_STYLE;

        private Optional<QuoteStyle> headerQuoteStyle = Optional.empty();

        private Supplier<? extends UnaryOperator<CharSequence>> inputTransformer;

        private Supplier<? extends UnaryOperator<CharSequence>> outputTransformer;

        /**
         * Sets the charset name.
         * @param newValue the charset name
         * @return this
         */
        public Builder withCharset(String newValue) {
            Charset cs = newValue != null ? Charset.forName(newValue) : null;
            return withCharset(cs);
        }

        /**
         * Sets the charset.
         * @param newValue the charset
         * @return this
         */
        public Builder withCharset(Charset newValue) {
            this.charset = newValue;
            return this;
        }

        /**
         * Sets the line separator.
         * @param newValue the line separator
         * @return this
         */
        public Builder withLineSeparator(LineSeparator newValue) {
            this.lineSeparator = newValue;
            return this;
        }

        /**
         * Sets the field separator.
         * @param newValue the field separator
         * @return this
         */
        public Builder withFieldSeparator(char newValue) {
            this.fieldSeparator = newValue;
            return this;
        }

        /**
         * Sets the quote character.
         * @param newValue the quote character
         * @return this
         */
        public Builder withQuoteCharacter(char newValue) {
            this.quoteCharacter = newValue;
            return this;
        }

        /**
         * Sets whether or not line-feed characters can appear in fields.
         * @param newValue {@code true} if it is enabled, otherwise {@code false}
         * @return this
         */
        public Builder withAllowLineFeedInField(boolean newValue) {
            this.allowLineFeedInField = newValue;
            return this;
        }

        /**
         * Sets the default quote style.
         * @param newValue the quote style
         * @return this
         */
        public Builder withDefaultQuoteStyle(QuoteStyle newValue) {
            this.defaultQuoteStyle = newValue;
            return this;
        }

        /**
         * Sets the quote style for header fields.
         * @param newValue the quote style
         * @return this
         */
        public Builder withHeaderQuoteStyle(QuoteStyle newValue) {
            this.headerQuoteStyle = Optional.ofNullable(newValue);
            return this;
        }

        /**
         * Sets the input transformer class.
         * @param newValue the input transformer class
         * @return this
         */
        public Builder withInputTransformer(Class<? extends UnaryOperator<CharSequence>> newValue) {
            return withInputTransformer(asSupplier(newValue));
        }

        /**
         * Sets the input transformer.
         * @param newValue the input transformer
         * @return this
         */
        public Builder withInputTransformer(Supplier<? extends UnaryOperator<CharSequence>> newValue) {
            this.inputTransformer = newValue;
            return this;
        }

        /**
         * Sets the output transformer class.
         * @param newValue the output transformer class
         * @return this
         */
        public Builder withOutputTransformer(Class<? extends UnaryOperator<CharSequence>> newValue) {
            return withOutputTransformer(asSupplier(newValue));
        }

        /**
         * Sets the output transformer.
         * @param newValue the output transformer
         * @return this
         */
        public Builder withOutputTransformer(Supplier<? extends UnaryOperator<CharSequence>> newValue) {
            this.outputTransformer = newValue;
            return this;
        }

        private static Supplier<? extends UnaryOperator<CharSequence>> asSupplier(
                Class<? extends UnaryOperator<CharSequence>> aClass) {
            if (aClass == null) {
                return null;
            }
            return () -> {
                try {
                    return aClass.newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "failed to instantiate: {0}",
                            aClass.getName()), e);
                }
            };
        }

        /**
         * Builds a {@link CsvTextFormat}.
         * @return the built object
         */
        public CsvTextFormat build() {
            return new CsvTextFormat(
                    charset,
                    lineSeparator, fieldSeparator, quoteCharacter,
                    allowLineFeedInField,
                    defaultQuoteStyle, headerQuoteStyle.orElse(defaultQuoteStyle),
                    inputTransformer, outputTransformer);
        }
    }
}
