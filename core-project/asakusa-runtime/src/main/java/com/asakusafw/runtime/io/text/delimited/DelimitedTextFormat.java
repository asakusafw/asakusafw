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
package com.asakusafw.runtime.io.text.delimited;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.function.UnaryOperator;

import com.asakusafw.runtime.io.text.LineSeparator;
import com.asakusafw.runtime.io.text.TextFormat;

/**
 * An implementation of {@link TextFormat} for delimited text files.
 * @since 0.9.1
 */
public class DelimitedTextFormat implements TextFormat {

    /**
     * The default charset.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * The default line separator.
     */
    public static final LineSeparator DEFAULT_LINE_SEPARATOR = LineSeparator.UNIX;

    /**
     * The default field separator.
     */
    public static final char DEFAULT_FIELD_SEPARATOR = '\t';

    private final Charset charset;

    private final LineSeparator lineSeparator;

    private final char fieldSeparator;

    private final EscapeSequence escapeSequence;

    private final UnaryOperator<CharSequence> inputTransformer;

    private final UnaryOperator<CharSequence> outputTransformer;

    DelimitedTextFormat(
            Charset charset,
            LineSeparator lineSeparator, char fieldSeparator,
            EscapeSequence escapeSequence,
            UnaryOperator<CharSequence> inputTransformer, UnaryOperator<CharSequence> outputTransformer) {
        this.charset = charset;
        this.lineSeparator = lineSeparator;
        this.fieldSeparator = fieldSeparator;
        this.escapeSequence = escapeSequence;
        this.inputTransformer = inputTransformer;
        this.outputTransformer = outputTransformer;
    }

    /**
     * Returns a new builder.
     * @return the created builder
     */
    public static final Builder builder() {
        return new Builder();
    }

    @Override
    public DelimitedFieldReader open(InputStream input) throws IOException {
        return new DelimitedFieldReader(
                new InputStreamReader(input, charset),
                fieldSeparator, escapeSequence,
                inputTransformer);
    }

    @Override
    public DelimitedFieldWriter open(OutputStream output) throws IOException {
        return new DelimitedFieldWriter(
                new OutputStreamWriter(output, charset),
                lineSeparator, fieldSeparator, escapeSequence,
                outputTransformer);
    }

    Charset getCharset() {
        return charset;
    }

    LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    /**
     * A builder of {@link DelimitedTextFormat}.
     * @since 0.9.1
     */
    public static class Builder {

        private Charset charset = DEFAULT_CHARSET;

        private LineSeparator lineSeparator = DEFAULT_LINE_SEPARATOR;

        private char fieldSeparator = DEFAULT_FIELD_SEPARATOR;

        private EscapeSequence escapeSequence;

        private UnaryOperator<CharSequence> inputTransformer;

        private UnaryOperator<CharSequence> outputTransformer;

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
         * Sets the escape sequence.
         * @param newValue the escape sequence
         * @return this
         */
        public Builder withEscapeSequence(EscapeSequence newValue) {
            this.escapeSequence = newValue;
            return this;
        }

        /**
         * Sets the input transformer class.
         * @param newValue the input transformer class
         * @return this
         */
        public Builder withInputTransformer(Class<? extends UnaryOperator<CharSequence>> newValue) {
            return withInputTransformer(newInstance(newValue));
        }

        /**
         * Sets the input transformer.
         * @param newValue the input transformer
         * @return this
         */
        public Builder withInputTransformer(UnaryOperator<CharSequence> newValue) {
            this.inputTransformer = newValue;
            return this;
        }

        /**
         * Sets the output transformer class.
         * @param newValue the output transformer class
         * @return this
         */
        public Builder withOutputTransformer(Class<? extends UnaryOperator<CharSequence>> newValue) {
            return withOutputTransformer(newInstance(newValue));
        }

        /**
         * Sets the output transformer.
         * @param newValue the output transformer
         * @return this
         */
        public Builder withOutputTransformer(UnaryOperator<CharSequence> newValue) {
            this.outputTransformer = newValue;
            return this;
        }

        private UnaryOperator<CharSequence> newInstance(Class<? extends UnaryOperator<CharSequence>> aClass) {
            if (aClass == null) {
                return null;
            }
            try {
                return aClass.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "failed to instantiate: {0}",
                        aClass.getName()), e);
            }
        }

        /**
         * Builds a {@link DelimitedTextFormat}.
         * @return the built object
         */
        public DelimitedTextFormat build() {
            return new DelimitedTextFormat(
                    charset,
                    lineSeparator, fieldSeparator,
                    escapeSequence,
                    inputTransformer, outputTransformer);
        }
    }
}
