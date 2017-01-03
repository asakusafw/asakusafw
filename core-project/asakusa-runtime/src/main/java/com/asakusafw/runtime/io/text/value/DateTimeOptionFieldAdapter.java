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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.io.text.driver.FieldOutput;
import com.asakusafw.runtime.value.DateTimeOption;

/**
 * An implementation of {@link FieldAdapter} which accepts {@link DateTimeOption}.
 * @since 0.9.1
 */
public final class DateTimeOptionFieldAdapter extends ValueOptionFieldAdapter<DateTimeOption> {

    /**
     * The default value of date-time format.
     */
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

    private final DateTimeFormatter formatter;

    DateTimeOptionFieldAdapter(
            String nullFormat, String dateTimeFormat,
            Collection<? extends FieldOutput.Option> outputOptions) {
        super(nullFormat, outputOptions);
        this.formatter = DateTimeFormatter.newInstance(dateTimeFormat);
    }

    /**
     * Returns a new builder.
     * @return the created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void doParse(CharSequence contents, DateTimeOption property) {
        long value = formatter.parse(contents);
        if (value < 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid date-time value ({0}): {1}",
                    formatter.getPattern(),
                    TextUtil.quote(contents)));
        }
        property.modify(value);
    }

    @Override
    protected void doEmit(DateTimeOption property, StringBuilder output) {
        formatter.emit(property.get().getElapsedSeconds(), output);
    }

    /**
     * A builder of {@link DateTimeOptionFieldAdapter}.
     * @since 0.9.1
     */
    public static class Builder extends BuilderBase<Builder, DateTimeOptionFieldAdapter> {

        private String dateTimeFormat = DEFAULT_FORMAT;

        /**
         * Sets the date-time format.
         * @param newValue the format string in {@link SimpleDateFormat}
         * @return this
         */
        public Builder withDateTimeFormat(String newValue) {
            this.dateTimeFormat = newValue;
            return this;
        }

        @Override
        public DateTimeOptionFieldAdapter build() {
            return new DateTimeOptionFieldAdapter(getNullFormat(), dateTimeFormat, getOutputOptions());
        }
    }
}
