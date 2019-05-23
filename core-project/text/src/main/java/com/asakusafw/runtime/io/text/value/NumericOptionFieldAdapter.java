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
package com.asakusafw.runtime.io.text.value;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Optional;

import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An abstract implementation of {@link ValueOptionFieldAdapter} for numeric types.
 * @param <T> the property type
 * @since 0.9.1
 */
public abstract class NumericOptionFieldAdapter<T extends ValueOption<T>> extends ValueOptionFieldAdapter<T> {

    private final DecimalFormat numberFormat;

    private final StringBuffer emitBuffer = new StringBuffer();

    private final ParsePosition parsePosition = new ParsePosition(0);

    private final FieldPosition fieldPosition = new FieldPosition(0);

    NumericOptionFieldAdapter(String nullFormat, DecimalFormat numberFormat) {
        super(nullFormat);
        this.numberFormat = numberFormat;
    }

    @Override
    protected final void doParse(CharSequence contents, T property) {
        if (numberFormat == null) {
            doParseDefault(contents, property);
        } else {
            ParsePosition position = parsePosition;
            position.setIndex(0);
            position.setErrorIndex(-1);
            Number number = numberFormat.parse(contents.toString(), position);
            if (position.getIndex() == 0 || position.getIndex() != contents.length()) {
                throw new NumberFormatException(MessageFormat.format(
                        "invalid number {0}",
                        TextUtil.quote(contents)));
            } else if (number instanceof BigDecimal) {
                set((BigDecimal) number, property);
            } else {
                setSpecial(contents, number, property);
            }
        }
    }

    @Override
    protected final void doEmit(T property, StringBuilder output) {
        if (numberFormat == null) {
            doEmitDefault(property, output);
        } else {
            fieldPosition.setBeginIndex(0);
            fieldPosition.setEndIndex(0);
            emitBuffer.setLength(0);
            Number value = get(property);
            numberFormat.format(value, emitBuffer, fieldPosition);
            output.append(emitBuffer);
        }
    }

    /**
     * Parses the given non-null character sequence and set the parsed value into property.
     * @param contents the contents, never {@code null}
     * @param property the destination property
     * @throws IllegalArgumentException if the character sequence is malformed for this field
     */
    protected abstract void doParseDefault(CharSequence contents, T property);

    /**
     * Emits the given non-null property value into the string builder.
     * @param property the property value, never {@code null} nor represents {@code null}
     * @param output the destination buffer
     */
    protected abstract void doEmitDefault(T property, StringBuilder output);

    /**
     * Returns the property value.
     * @param property the property
     * @return the property value
     */
    protected abstract Number get(T property);

    /**
     * Sets the given number into the property.
     * @param value the value
     * @param property the property
     */
    protected abstract void set(BigDecimal value, T property);

    /**
     * Sets the given special number into the property.
     * @param contents the original contents
     * @param value the value
     * @param property the property
     */
    protected void setSpecial(CharSequence contents, Number value, T property) {
        throw new NumberFormatException(MessageFormat.format(
                "invalid number {0}",
                TextUtil.quote(contents)));
    }

    /**
     * A basic implementation of builder for {@link NumericOptionFieldAdapter}.
     * @param <S> this builder type
     * @param <T> the build target type
     * @since 0.9.1
     */
    protected abstract static class NumericBuilderBase<S extends NumericBuilderBase<S, T>, T extends FieldAdapter<?>>
            extends BuilderBase<S, T> {

        private String numberFormat;

        private DecimalFormatSymbols decimalFormatSymbols;

        /**
         * Sets the decimal format.
         * @param newValue the format string
         * @return this
         */
        public S withNumberFormat(String newValue) {
            this.numberFormat = newValue;
            return self();
        }

        /**
         * Sets a decimal format symbols.
         * @param newValue the format symbols
         * @return this
         */
        public S withDecimalFormatSymbols(DecimalFormatSymbols newValue) {
            this.decimalFormatSymbols = Optional.ofNullable(newValue)
                    .map(v -> (DecimalFormatSymbols) v.clone())
                    .orElse(null);
            return self();
        }

        /**
         * Returns the number format.
         * @return the number format
         */
        protected DecimalFormat getDecimalFormat() {
            return Optional.ofNullable(numberFormat)
                    .map(s -> {
                        DecimalFormat f = new DecimalFormat(s, Optional.ofNullable(decimalFormatSymbols)
                                .orElseGet(DecimalFormatSymbols::getInstance));
                        f.setParseBigDecimal(true);
                        return f;
                    })
                    .orElse(null);
        }
    }
}
