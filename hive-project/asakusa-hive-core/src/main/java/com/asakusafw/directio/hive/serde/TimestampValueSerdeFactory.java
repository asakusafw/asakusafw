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
package com.asakusafw.directio.hive.serde;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.TimestampObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.ValueOption;

/**
 * {@link ValueSerde}s for {@code timestamp} values.
 * @since 0.7.0
 */
public enum TimestampValueSerdeFactory implements ValueSerde {

    /**
     * {@link DateOption}.
     */
    DATE(DateOption.class) {
        @Override
        public ObjectInspector getInspector() {
            return new TimestampDateOptionInspector();
        }
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new TimestampDateOptionDriver((TimestampObjectInspector) target);
        }
    },
    ;

    private final Class<? extends ValueOption<?>> valueOptionClass;

    TimestampValueSerdeFactory(Class<? extends ValueOption<?>> valueOptionClass) {
        this.valueOptionClass = valueOptionClass;
    }

    /**
     * Returns the common type information which each this enum member returns.
     * @return the common type information
     */
    public static TypeInfo getCommonTypeInfo() {
        return TypeInfoFactory.timestampTypeInfo;
    }

    @Override
    public TypeInfo getTypeInfo() {
        return getCommonTypeInfo();
    }

    @Override
    public Class<? extends ValueOption<?>> getValueClass() {
        return valueOptionClass;
    }

    /**
     * Returns a {@link ValueSerde} for the specified {@link ValueOption} class.
     * @param aClass the target {@link ValueOption} class.
     * @return the default {@link ValueSerde} for the target {@link ValueOption} class,
     *    or {@code null} if the target class has no {@link ValueSerde} object
     */
    public static TimestampValueSerdeFactory fromClass(Class<?> aClass) {
        return Lazy.FROM_CLASS.get(aClass);
    }

    private static final class Lazy {

        static final Map<Class<? extends ValueOption<?>>, TimestampValueSerdeFactory> FROM_CLASS;
        static {
            Map<Class<? extends ValueOption<?>>, TimestampValueSerdeFactory> map = new HashMap<>();
            for (TimestampValueSerdeFactory serde : TimestampValueSerdeFactory.values()) {
                map.put(serde.getValueClass(), serde);
            }
            FROM_CLASS = map;
        }

        private Lazy() {
            return;
        }
    }

    abstract static class AbstractTimestampInspector extends AbstractValueInspector
            implements TimestampObjectInspector {

        protected AbstractTimestampInspector() {
            super(TypeInfoFactory.timestampTypeInfo);
        }

        @Override
        public Timestamp getPrimitiveJavaObject(Object o) {
            ValueOption<?> object = (ValueOption<?>) o;
            if (object == null || object.isNull()) {
                return null;
            }
            return toTimestamp(object);
        }

        protected abstract Timestamp toTimestamp(ValueOption<?> object);

        @Override
        public TimestampWritable getPrimitiveWritableObject(Object o) {
            java.sql.Timestamp value = getPrimitiveJavaObject(o);
            if (value == null) {
                return null;
            }
            return new TimestampWritable(value);
        }
    }

    abstract static class AbstractTimestampDriver extends AbstractValueDriver {

        private final TimestampObjectInspector inspector;

        private final boolean primitive;

        protected AbstractTimestampDriver(TimestampObjectInspector inspector) {
            this.inspector = inspector;
            this.primitive = inspector.preferWritable() == false;
        }

        @SuppressWarnings("deprecation")
        @Override
        public final void set(ValueOption<?> target, Object value) {
            if (value == null) {
                target.setNull();
            } else if (primitive) {
                Timestamp entity = inspector.getPrimitiveJavaObject(value);
                set(target, entity);
            } else {
                TimestampWritable writable = inspector.getPrimitiveWritableObject(value);
                set(target, writable.getTimestamp());
            }
        }

        protected abstract void set(ValueOption<?> target, Timestamp value);
    }

    static class TimestampDateOptionInspector extends AbstractTimestampInspector {

        @Override
        protected ValueOption<?> newObject() {
            return new DateOption();
        }

        @SuppressWarnings("deprecation")
        @Override
        protected Timestamp toTimestamp(ValueOption<?> object) {
            Date value = ((DateOption) object).get();
            return new Timestamp(
                    value.getYear() - 1900, value.getMonth() - 1, value.getDay(),
                    0, 0, 0, 0);
        }
    }

    static class TimestampDateOptionDriver extends AbstractTimestampDriver {

        TimestampDateOptionDriver(TimestampObjectInspector inspector) {
            super(inspector);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void set(ValueOption<?> target, Timestamp value) {
            ((DateOption) target).modify(DateUtil.getDayFromDate(value));
        }
    }
}
