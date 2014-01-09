/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.directio;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import com.asakusafw.runtime.io.util.WritableRawComparable;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;

/**
 * Generates a string for the object.
 * @since 0.2.5
 */
public abstract class StringTemplate implements WritableRawComparable {

    /**
     * An empty template.
     * @since 0.4.0
     */
    public static final StringTemplate EMPTY = new StringTemplate() {
        @Override
        public void set(Object object) {
            return;
        }
    };

    private final PropertyFormatter[] formatters;

    private final Text nameBuffer = new Text();

    /**
     * Creates a new instance.
     * @param specs format specifications
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected StringTemplate(FormatSpec... specs) {
        if (specs == null) {
            throw new IllegalArgumentException("specs must not be null"); //$NON-NLS-1$
        }
        this.formatters = new PropertyFormatter[specs.length];
        for (int i = 0; i < specs.length; i++) {
            formatters[i] = specs[i].newFormatter();
        }
    }

    /**
     * Sets an object for generating a name.
     * @param object target object
     * @throws RuntimeException if the object or its properties are wrong
     */
    public abstract void set(Object object);

    /**
     * Sets a property value.
     * Each index must be related to the specified in constructor.
     * @param index target index
     * @param value property value
     * @throws RuntimeException if the value is wrong
     */
    protected final void setProperty(int index, Object value) {
        formatters[index].set(value);
    }

    /**
     * Returns a generated name.
     * @return the generated name
     */
    public final String apply() {
        nameBuffer.clear();
        for (int i = 0; i < formatters.length; i++) {
            Text text = formatters[i].representation;
            nameBuffer.append(text.getBytes(), 0, text.getLength());
        }
        return nameBuffer.toString();
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        for (int i = 0; i < formatters.length; i++) {
            formatters[i].write(out);
        }
    }

    @Override
    public final void readFields(DataInput in) throws IOException {
        for (int i = 0; i < formatters.length; i++) {
            formatters[i].readFields(in);
        }
    }

    /**
     * Computes and returns size in bytes.
     * @param buf bytes array
     * @param offset bytes offset
     * @return size in bytes
     * @throws IOException if failed to compute size
     */
    @Override
    public final int getSizeInBytes(byte[] buf, int offset) throws IOException {
        int cursor = 0;
        for (int i = 0; i < formatters.length; i++) {
            int metaSize = WritableUtils.decodeVIntSize(buf[offset + cursor]);
            int bodySize = WritableComparator.readVInt(buf, offset + cursor);
            cursor += metaSize + bodySize;
        }
        return cursor;
    }

    /**
     * Compares two objects in bytes.
     * @param b1 bytes representation of the first object
     * @param o1 offset of the first object
     * @param b2 bytes representation of the second object
     * @param o2 offset of the second object
     * @return the comparison result
     * @throws IOException if failed to comparison
     */
    @Override
    public final int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        int l1 = getSizeInBytes(b1, o1);
        int l2 = getSizeInBytes(b2, o2);
        return WritableComparator.compareBytes(b1, o1, l1, b2, o2, l2);
    }

    @Override
    public final int compareTo(WritableRawComparable o) {
        assert this.getClass() == o.getClass();
        if (this == o) {
            return 0;
        }
        PropertyFormatter[] fs1 = formatters;
        PropertyFormatter[] fs2 = ((StringTemplate) o).formatters;
        for (int i = 0, n = fs1.length; i < n; i++) {
            int diff = fs1[i].representation.compareTo(fs2[i].representation);
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(formatters);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StringTemplate other = (StringTemplate) obj;
        return Arrays.equals(formatters, other.formatters);
    }

    /**
     * Format of each generator element.
     * @since 0.2.5
     */
    public enum Format {

        /**
         * Plain string.
         */
        PLAIN {
            @Override
            public PropertyFormatter newFormatter(String formatString) {
                return new Constant(formatString);
            }

            @Override
            public void check(java.lang.reflect.Type valueType, String formatString) {
                if (formatString == null) {
                    throw new IllegalArgumentException("format string must not be null");
                }
            }
        },

        /**
         * Converts naturally (format string will be ignored).
         */
        NATURAL {
            @Override
            public PropertyFormatter newFormatter(String formatString) {
                return new Variable() {
                    @Override
                    void set(Object propertyValue) {
                        representation.set(String.valueOf(propertyValue));
                    }
                };
            }

            @Override
            public void check(java.lang.reflect.Type valueType, String formatString) {
                if (formatString != null) {
                    throw new IllegalArgumentException("format string must be null");
                }
            }
        },

        /**
         * Converts {@link Date} date (use {@link SimpleDateFormat}).
         */
        DATE {
            @Override
            public PropertyFormatter newFormatter(String formatString) {
                final Calendar calendar = Calendar.getInstance();
                final DateFormat dateFormat = new SimpleDateFormat(formatString);
                return new Variable() {

                    @Override
                    void set(Object propertyValue) {
                        DateOption option = (DateOption) propertyValue;
                        if (option.isNull()) {
                            representation.set(String.valueOf(option));
                        } else {
                            Date date = option.get();
                            DateUtil.setDayToCalendar(date.getElapsedDays(), calendar);
                            representation.set(String.valueOf(dateFormat.format(calendar.getTime())));
                        }
                    }
                };
            }

            @Override
            public void check(java.lang.reflect.Type valueType, String formatString) {
                if (valueType != DateOption.class) {
                    throw new IllegalArgumentException("type must be Date");
                }
                if (formatString == null) {
                    throw new IllegalArgumentException("format string must not be null");
                }
                SimpleDateFormat format = new SimpleDateFormat();
                format.applyPattern(formatString);
            }
        },

        /**
         * Converts {@link DateTime} date (use {@link SimpleDateFormat}).
         */
        DATETIME {
            @Override
            public PropertyFormatter newFormatter(String formatString) {
                final Calendar calendar = Calendar.getInstance();
                final DateFormat dateFormat = new SimpleDateFormat(formatString);
                return new Variable() {

                    @Override
                    void set(Object propertyValue) {
                        DateTimeOption option = (DateTimeOption) propertyValue;
                        if (option.isNull()) {
                            representation.set(String.valueOf(option));
                        } else {
                            DateTime date = option.get();
                            DateUtil.setSecondToCalendar(date.getElapsedSeconds(), calendar);
                            representation.set(String.valueOf(dateFormat.format(calendar.getTime())));
                        }
                    }
                };
            }

            @Override
            public void check(java.lang.reflect.Type valueType, String formatString) {
                if (valueType != DateTimeOption.class) {
                    throw new IllegalArgumentException("type must be DateTime");
                }
                if (formatString == null) {
                    throw new IllegalArgumentException("format string must not be null");
                }
                SimpleDateFormat format = new SimpleDateFormat();
                format.applyPattern(formatString);
            }
        },
        ;

        /**
         * Creates a new formatter using the format string.
         * @param formatString format string
         * @return the created formatter
         */
        public abstract PropertyFormatter newFormatter(String formatString);

        /**
         * Checks whether this format accepts the value type and format string.
         * @param valueType value type, or {@code null} if no values
         * @param formatString format string, or {@code null} if not specified
         * @throws IllegalArgumentException if this format does not accept
         */
        public abstract void check(java.lang.reflect.Type valueType, String formatString);
    }

    /**
     * The format spec.
     * @since 0.2.5
     */
    public static final class FormatSpec {

        private final Format format;

        private final String string;

        /**
         * Creates a new instance.
         * @param format format kind
         * @param string format string (nullable)
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public FormatSpec(Format format, String string) {
            if (format == null) {
                throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
            }
            this.format = format;
            this.string = string;
        }

        /**
         * Returns the format kind.
         * @return the format kind
         */
        public Format getFormat() {
            return format;
        }

        /**
         * Returns the format string.
         * @return the string, or {@code null} if not specified
         */
        public String getString() {
            return string;
        }

        /**
         * Creates a new formatter from this spec.
         * @return the created formatter
         */
        public PropertyFormatter newFormatter() {
            return format.newFormatter(string);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + format.hashCode();
            result = prime * result + ((string == null) ? 0 : string.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            FormatSpec other = (FormatSpec) obj;
            if (format != other.format) {
                return false;
            }
            if (string == null) {
                if (other.string != null) {
                    return false;
                }
            } else if (!string.equals(other.string)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("FormatSpec [format=");
            builder.append(format);
            builder.append(", string=");
            builder.append(string);
            builder.append("]");
            return builder.toString();
        }
    }

    private abstract static class PropertyFormatter implements Writable {

        final Text representation;

        PropertyFormatter() {
            this.representation = new Text();
        }

        abstract void set(Object propertyValue);

        @Override
        public final int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + representation.hashCode();
            return result;
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PropertyFormatter other = (PropertyFormatter) obj;
            if (!representation.equals(other.representation)) {
                return false;
            }
            return true;
        }
    }

    private static final class Constant extends PropertyFormatter {

        Constant(String value) {
            this.representation.set(value);
        }

        @Override
        void set(Object propertyValue) {
            return;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            WritableUtils.writeVInt(out, 0);
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            WritableUtils.readVInt(in);
        }
    }

    private abstract static class Variable extends PropertyFormatter {

        Variable() {
            return;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            representation.write(out);
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            representation.readFields(in);
        }
    }
}
