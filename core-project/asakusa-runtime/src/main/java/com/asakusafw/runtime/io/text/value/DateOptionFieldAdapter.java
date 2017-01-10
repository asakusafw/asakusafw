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
import com.asakusafw.runtime.value.DateOption;

/**
 * An implementation of {@link FieldAdapter} which accepts {@link DateOption}.
 * @since 0.9.1
 */
public final class DateOptionFieldAdapter extends ValueOptionFieldAdapter<DateOption> {

    /**
     * The default value of date format.
     */
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

    private final DateFormatter formatter;

    DateOptionFieldAdapter(
            String nullFormat, String dateFormat,
            Collection<? extends FieldOutput.Option> outputOptions) {
        super(nullFormat, outputOptions);
        this.formatter = DateFormatter.newInstance(dateFormat);
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
    protected void doParse(CharSequence contents, DateOption property) {
        int value = formatter.parse(contents);
        if (value < 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid date value ({0}): {1}",
                    formatter.getPattern(),
                    TextUtil.quote(contents)));
        }
        property.modify(value);
    }

    @Override
    protected void doEmit(DateOption property, StringBuilder output) {
        formatter.emit(property.get().getElapsedDays(), output);
    }

    /**
     * A builder of {@link DateOptionFieldAdapter}.
     * @since 0.9.1
     */
    public static class Builder extends BuilderBase<Builder, DateOptionFieldAdapter> {

        private String dateFormat = DEFAULT_FORMAT;

        /**
         * Sets the date format.
         * @param newValue the format string in {@link SimpleDateFormat}
         * @return this
         */
        public Builder withDateFormat(String newValue) {
            this.dateFormat = newValue;
            return this;
        }

        @Override
        public DateOptionFieldAdapter build() {
            return new DateOptionFieldAdapter(getNullFormat(), dateFormat, getOutputOptions());
        }
    }
}
