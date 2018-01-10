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
package com.asakusafw.directio.hive.parquet;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.directio.hive.util.TemporalUtil;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.ValueOption;

import parquet.column.Dictionary;
import parquet.io.api.Binary;
import parquet.io.api.RecordConsumer;
import parquet.schema.OriginalType;
import parquet.schema.PrimitiveType;
import parquet.schema.PrimitiveType.PrimitiveTypeName;
import parquet.schema.Type;
import parquet.schema.Type.Repetition;

/**
 * Converts between {@link ValueOption} and {@code string (binary)}.
 * @since 0.7.0
 * @version 0.7.2
 */
public enum StringValueDrivers implements ParquetValueDriver {

    /**
     * {@link DecimalOption}.
     */
    DECIMAL(DecimalOption.class) {
        @Override
        public ValueConverter getConverter() {
            return new ToDecimalOption();
        }
        @Override
        public ValueWriter getWriter() {
            return new FromDecimalOption();
        }
    },

    /**
     * {@link DateOption}.
     */
    DATE(DateOption.class) {
        @Override
        public ValueConverter getConverter() {
            return new ToDateOption();
        }
        @Override
        public ValueWriter getWriter() {
            return new FromDateOption();
        }
    },

    /**
     * {@link DateTimeOption}.
     */
    DATETIME(DateTimeOption.class) {
        @Override
        public ValueConverter getConverter() {
            return new ToDateTimeOption();
        }
        @Override
        public ValueWriter getWriter() {
            return new FromDateTimeOption();
        }
    },
    ;

    final Class<? extends ValueOption<?>> valueOptionClass;

    StringValueDrivers(Class<? extends ValueOption<?>> valueOptionClass) {
        this.valueOptionClass = valueOptionClass;
    }

    @Override
    public Type getType(String name) {
        return new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.BINARY, name, OriginalType.UTF8);
    }

    /**
     * Returns a {@link ParquetValueDriver} for the specified type.
     * @param valueClass the {@link ValueOption} type
     * @return the corresponded {@link ParquetValueDriver}, or {@code null} if it is not found
     */
    public static ParquetValueDriver find(Class<?> valueClass) {
        return Lazy.FROM_CLASS.get(valueClass);
    }

    private static final class Lazy {

        static final Map<Class<?>, StringValueDrivers> FROM_CLASS;
        static {
            Map<Class<?>, StringValueDrivers> map = new HashMap<>();
            for (StringValueDrivers element : StringValueDrivers.values()) {
                map.put(element.valueOptionClass, element);
            }
            FROM_CLASS = map;
        }

        private Lazy() {
            return;
        }
    }

    abstract static class AbstractWriter implements ValueWriter {

        protected abstract String toString(Object value);

        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addBinary(Binary.fromString(toString(value)));
        }
    }

    abstract static class AbstractConverter<T> extends ValueConverter {

        private T[] dict;

        protected AbstractConverter() {
            return;
        }

        protected abstract T parse(String value);

        protected abstract void drive(T value);

        @Override
        public boolean hasDictionarySupport() {
            return true;
        }

        @Override
        public void setDictionary(Dictionary dictionary) {
            T[] buf = prepareDictionaryBuffer(dictionary);
            for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                buf[id] = parse(dictionary.decodeToBinary(id).toStringUsingUTF8());
            }
        }

        @Override
        public void addValueFromDictionary(int dictionaryId) {
            drive(dict[dictionaryId]);
        }

        @Override
        public void addBinary(Binary value) {
            drive(parse(value.toStringUsingUTF8()));
        }

        @SuppressWarnings("unchecked")
        private T[] prepareDictionaryBuffer(Dictionary dictionary) {
            int size = dictionary.getMaxId() + 1;
            if (this.dict == null || this.dict.length < size) {
                int capacity = (int) (size * 1.2) + 1;
                this.dict = (T[]) new Object[capacity];
            } else {
                Arrays.fill(this.dict, null);
            }
            return this.dict;
        }
    }

    abstract static class AbstractIntConverter extends ValueConverter {

        private int[] dict;

        protected AbstractIntConverter() {
            return;
        }

        protected abstract int parse(String value);

        protected abstract void drive(int value);

        @Override
        public boolean hasDictionarySupport() {
            return true;
        }

        @Override
        public void setDictionary(Dictionary dictionary) {
            int[] buf = prepareDictionaryBuffer(dictionary);
            for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                buf[id] = parse(dictionary.decodeToBinary(id).toStringUsingUTF8());
            }
        }

        @Override
        public void addValueFromDictionary(int dictionaryId) {
            drive(dict[dictionaryId]);
        }

        @Override
        public void addBinary(Binary value) {
            drive(parse(value.toStringUsingUTF8()));
        }

        private int[] prepareDictionaryBuffer(Dictionary dictionary) {
            int size = dictionary.getMaxId() + 1;
            if (this.dict == null || this.dict.length < size) {
                int capacity = (int) (size * 1.2) + 1;
                this.dict = new int[capacity];
            } else {
                Arrays.fill(this.dict, -1);
            }
            return this.dict;
        }
    }

    abstract static class AbstractLongConverter extends ValueConverter {

        private long[] dict;

        protected AbstractLongConverter() {
            return;
        }

        protected abstract long parse(String value);

        protected abstract void drive(long value);

        @Override
        public boolean hasDictionarySupport() {
            return true;
        }

        @Override
        public void setDictionary(Dictionary dictionary) {
            long[] buf = prepareDictionaryBuffer(dictionary);
            for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                buf[id] = parse(dictionary.decodeToBinary(id).toStringUsingUTF8());
            }
        }

        @Override
        public void addValueFromDictionary(int dictionaryId) {
            drive(dict[dictionaryId]);
        }

        @Override
        public void addBinary(Binary value) {
            drive(parse(value.toStringUsingUTF8()));
        }

        private long[] prepareDictionaryBuffer(Dictionary dictionary) {
            int size = dictionary.getMaxId() + 1;
            if (this.dict == null || this.dict.length < size) {
                int capacity = (int) (size * 1.2) + 1;
                this.dict = new long[capacity];
            } else {
                Arrays.fill(this.dict, -1L);
            }
            return this.dict;
        }
    }

    static final class FromDecimalOption extends AbstractWriter {

        @Override
        protected String toString(Object value) {
            DecimalOption option = (DecimalOption) value;
            return option.get().toPlainString();
        }
    }

    static final class ToDecimalOption extends AbstractConverter<BigDecimal> {

        private DecimalOption target;

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DecimalOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void drive(BigDecimal value) {
            target.modify(value);
        }

        @Override
        protected BigDecimal parse(String value) {
            return new BigDecimal(value);
        }
    }

    static final class FromDateOption extends AbstractWriter {

        @Override
        protected String toString(Object value) {
            DateOption option = (DateOption) value;
            return TemporalUtil.toDateString(option.get().getElapsedDays());
        }
    }

    static final class ToDateOption extends AbstractIntConverter {

        private DateOption target;

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DateOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void drive(int value) {
            target.modify(value);
        }

        @Override
        protected int parse(String value) {
            int days = TemporalUtil.parseDate(value);
            if (days < 0) {
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("StringValueDrivers.errorInvalidDateString"), //$NON-NLS-1$
                        value));
            }
            return days;
        }
    }

    static final class FromDateTimeOption extends AbstractWriter {

        @Override
        protected String toString(Object value) {
            DateTimeOption option = (DateTimeOption) value;
            return TemporalUtil.toTimestampString(option.get().getElapsedSeconds());
        }
    }

    static final class ToDateTimeOption extends AbstractLongConverter {

        private DateTimeOption target;

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DateTimeOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void drive(long value) {
            target.modify(value);
        }

        @Override
        protected long parse(String value) {
            long seconds = TemporalUtil.parseTimestamp(value);
            if (seconds < 0) {
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("StringValueDrivers.errorInvalidTimestampString"), //$NON-NLS-1$
                        value));
            }
            return seconds;
        }
    }
}
