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
package com.asakusafw.runtime.io.json.value;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;
import com.asakusafw.runtime.value.DateTimeOption;

/**
 * An implementation of {@link PropertyAdapter} for {@link DateTimeOption}.
 * @since 0.10.3
 */
public class DateTimeOptionPropertyAdapter extends ValueOptionPropertyAdapter<DateTimeOption> {

    /**
     * The default value of date-time format.
     */
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

    private final DateTimeAdapter formatter;

    private final StringBuilder buffer = new StringBuilder();

    /**
     * Creates a new instance.
     * @param builder the source builder
     */
    protected DateTimeOptionPropertyAdapter(Builder builder) {
        super(builder);
        this.formatter = DateTimeAdapter.newInstance(builder.dateTimeFormat, builder.timeZone);
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
    protected void doRead(ValueReader reader, DateTimeOption property) throws IOException {
        buffer.setLength(0);
        reader.readString(buffer);
        long value = formatter.parse(buffer);
        if (value < 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid date-time value ({0}): {1}",
                    formatter.getPattern(),
                    buffer));
        }
        property.modify(value);
    }

    @Override
    protected void doWrite(DateTimeOption property, ValueWriter writer) throws IOException {
        buffer.setLength(0);
        formatter.emit(property.get().getElapsedSeconds(), buffer);
        writer.writeString(buffer);
    }

    /**
     * A builder for {@link DateTimeOptionPropertyAdapter}.
     * @since 0.10.3
     */
    public static class Builder extends BuilderBase<Builder, DateTimeOptionPropertyAdapter> {

        String dateTimeFormat = DEFAULT_FORMAT;

        String timeZone;

        @Override
        public DateTimeOptionPropertyAdapter build() {
            return new DateTimeOptionPropertyAdapter(this);
        }

        /**
         * Sets the date format.
         * @param value the format string in {@link SimpleDateFormat}
         * @return this
         */
        public Builder withDateTimeFormat(String value) {
            this.dateTimeFormat = value;
            return this;
        }

        /**
         * Sets the time zone.
         * @param newValue a string representation of target time zone
         * @return this
         */
        public Builder withTimeZone(String newValue) {
            this.timeZone = newValue;
            return this;
        }
    }
}
