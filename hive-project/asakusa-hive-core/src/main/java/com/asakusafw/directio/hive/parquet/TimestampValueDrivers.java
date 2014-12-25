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
package com.asakusafw.directio.hive.parquet;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import parquet.column.Dictionary;
import parquet.io.api.Binary;
import parquet.io.api.RecordConsumer;
import parquet.schema.PrimitiveType;
import parquet.schema.PrimitiveType.PrimitiveTypeName;
import parquet.schema.Type;
import parquet.schema.Type.Repetition;

import com.asakusafw.directio.hive.util.TemporalUtil;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Converts between {@link ValueOption} and {@code timestamp (binary)}.
 * @since 0.7.2
 */
public enum TimestampValueDrivers implements ParquetValueDriver {

    /**
     * {@link DateOption}.
     */
    DATE(DateOption.class) {
        @Override
        public ValueConverter getConverter() {
            return new DateConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return new DateWriter();
        }
    },

    /**
     * {@link DateTimeOption}.
     */
    DATETIME(DateTimeOption.class) {
        @Override
        public ValueConverter getConverter() {
            return new DateTimeConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return new DateTimeWriter();
        }
    },
    ;

    final Class<? extends ValueOption<?>> valueOptionClass;

    private TimestampValueDrivers(Class<? extends ValueOption<?>> valueOptionClass) {
        this.valueOptionClass = valueOptionClass;
    }

    @Override
    public Type getType(String name) {
        return new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.INT96, name);
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

        static final Map<Class<?>, TimestampValueDrivers> FROM_CLASS;
        static {
            Map<Class<?>, TimestampValueDrivers> map = new HashMap<Class<?>, TimestampValueDrivers>();
            for (TimestampValueDrivers element : TimestampValueDrivers.values()) {
                map.put(element.valueOptionClass, element);
            }
            FROM_CLASS = map;
        }

        private Lazy() {
            return;
        }
    }

    static int reverseEndian(int value) {
        int result = 0;
        result |= (value & (0xff << 24)) >>> 24;
        result |= (value & (0xff << 16)) >>>  8;
        result |= (value & (0xff <<  8))  <<  8;
        result |= (value & (0xff <<  0))  << 24;
        return result;
    }

    static long reverseEndian(long value) {
        int hi = (int) (value >> 32);
        int lo = (int) (value >>  0);
        long result = ((long) reverseEndian(lo) << 32) | (reverseEndian(hi) & 0xffffffffL);
        return result;
    }

    abstract static class AbstractWriter implements ValueWriter {

        void write(int julianDay, long timeOfDayNanos, RecordConsumer consumer) {
            ByteBuffer buf = ByteBuffer.allocate(12);
            buf.clear();
            buf.putLong(reverseEndian(timeOfDayNanos));
            buf.putInt(reverseEndian(julianDay));
            buf.flip();
            consumer.addBinary(Binary.fromByteBuffer(buf));
        }
    }

    static class DateWriter extends AbstractWriter {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            DateOption option = (DateOption) value;
            int julianDayNumber = TemporalUtil.getJulianDayNumber(option.get());
            long nanoTime = TemporalUtil.getTimeOfDayNanos(option.get());
            write(julianDayNumber, nanoTime, consumer);
        }
    }

    static class DateTimeWriter extends AbstractWriter {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            DateTimeOption option = (DateTimeOption) value;
            int julianDayNumber = TemporalUtil.getJulianDayNumber(option.get());
            long nanoTime = TemporalUtil.getTimeOfDayNanos(option.get());
            write(julianDayNumber, nanoTime, consumer);
        }
    }

    abstract static class AbstractConverter extends ValueConverter {

        private int[] julianDays;

        private long[] nanoTimes;

        protected AbstractConverter() {
            return;
        }

        @Override
        public boolean hasDictionarySupport() {
            return true;
        }

        @Override
        public void setDictionary(Dictionary dictionary) {
            int size = dictionary.getMaxId() + 1;
            if (this.julianDays == null || this.julianDays.length < size) {
                int capacity = (int) (size * 1.2) + 1;
                this.julianDays = new int[capacity];
                this.nanoTimes = new long[capacity];
            }
            for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                ByteBuffer bytes = dictionary.decodeToBinary(id).toByteBuffer();
                long time = reverseEndian(bytes.getLong());
                int day = reverseEndian(bytes.getInt());
                julianDays[id] = day;
                nanoTimes[id] = time;
            }
        }

        @Override
        public void addValueFromDictionary(int dictionaryId) {
            addNanoTime(julianDays[dictionaryId], nanoTimes[dictionaryId]);
        }

        @Override
        public void addBinary(Binary value) {
            ByteBuffer bytes = value.toByteBuffer();
            long time = reverseEndian(bytes.getLong());
            int day = reverseEndian(bytes.getInt());
            addNanoTime(day, time);
        }

        abstract void addNanoTime(int julianDay, long nanoTime);
    }

    static class DateConverter extends AbstractConverter {

        private DateOption target;

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DateOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        void addNanoTime(int julianDay, long nanoTime) {
            long seconds = TemporalUtil.toElapsedSeconds(julianDay, nanoTime);
            target.modify(DateUtil.getDayFromSeconds(seconds));
        }
    }

    static class DateTimeConverter extends AbstractConverter {

        private DateTimeOption target;

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DateTimeOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        void addNanoTime(int julianDay, long nanoTime) {
            target.modify(TemporalUtil.toElapsedSeconds(julianDay, nanoTime));
        }
    }
}
