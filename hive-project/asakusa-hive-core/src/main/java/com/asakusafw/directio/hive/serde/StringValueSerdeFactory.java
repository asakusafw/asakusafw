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
package com.asakusafw.directio.hive.serde;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.Text;

import com.asakusafw.directio.hive.util.TemporalUtil;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * {@link ValueSerde}s for {@code string} values.
 * @since 0.7.0
 */
public enum StringValueSerdeFactory implements ValueSerde {

    /**
     * {@link DecimalOption}.
     */
    DECIMAL(DecimalOption.class) {
        @Override
        public ObjectInspector getInspector() {
            return new StringDecimalOptionInspector();
        }
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new StringDecimalOptionDriver((StringObjectInspector) target);
        }
    },

    /**
     * {@link DateOption}.
     */
    DATE(DateOption.class) {
        @Override
        public ObjectInspector getInspector() {
            return new StringDateOptionInspector();
        }
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new StringDateOptionDriver((StringObjectInspector) target);
        }
    },

    /**
     * {@link DateTimeOption}.
     */
    DATETIME(DateTimeOption.class) {
        @Override
        public ObjectInspector getInspector() {
            return new StringDateTimeOptionInspector();
        }
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new StringDateTimeOptionDriver((StringObjectInspector) target);
        }
    },
    ;

    private final Class<? extends ValueOption<?>> valueOptionClass;

    private StringValueSerdeFactory(Class<? extends ValueOption<?>> valueOptionClass) {
        this.valueOptionClass = valueOptionClass;
    }

    /**
     * Returns the common type information which each this enum member returns.
     * @return the common type information
     */
    public static TypeInfo getCommonTypeInfo() {
        return TypeInfoFactory.stringTypeInfo;
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
    public static StringValueSerdeFactory fromClass(Class<?> aClass) {
        return Lazy.FROM_CLASS.get(aClass);
    }

    private static final class Lazy {

        static final Map<Class<? extends ValueOption<?>>, StringValueSerdeFactory> FROM_CLASS;
        static {
            Map<Class<? extends ValueOption<?>>, StringValueSerdeFactory> map =
                    new HashMap<Class<? extends ValueOption<?>>, StringValueSerdeFactory>();
            for (StringValueSerdeFactory serde : StringValueSerdeFactory.values()) {
                map.put(serde.getValueClass(), serde);
            }
            FROM_CLASS = map;
        }

        private Lazy() {
            return;
        }
    }

    static abstract class AbstractStringInspector extends AbstractValueInspector
            implements StringObjectInspector {

        protected AbstractStringInspector() {
            super(TypeInfoFactory.stringTypeInfo);
        }

        @Override
        public String getPrimitiveJavaObject(Object o) {
            ValueOption<?> object = (ValueOption<?>) o;
            if (object == null || object.isNull()) {
                return null;
            }
            return toString(object);
        }

        protected abstract String toString(ValueOption<?> object);

        @Override
        public Text getPrimitiveWritableObject(Object o) {
            String value = getPrimitiveJavaObject(o);
            if (value == null) {
                return null;
            }
            return new Text(value);
        }
    }

    static abstract class AbstractStringDriver extends AbstractValueDriver {

        private final StringObjectInspector inspector;

        private final boolean primitive;

        protected AbstractStringDriver(StringObjectInspector inspector) {
            this.inspector = inspector;
            this.primitive = inspector.preferWritable() == false;
        }

        @SuppressWarnings("deprecation")
        @Override
        public final void set(ValueOption<?> target, Object value) {
            if (value == null) {
                target.setNull();
            } else if (primitive) {
                String entity = inspector.getPrimitiveJavaObject(value);
                set(target, entity);
            } else {
                Text writable = inspector.getPrimitiveWritableObject(value);
                set(target, writable.toString());
            }
        }

        protected abstract void set(ValueOption<?> target, String value);
    }

    static class StringDecimalOptionInspector extends AbstractStringInspector {

        @Override
        protected ValueOption<?> newObject() {
            return new DecimalOption();
        }

        @Override
        protected String toString(ValueOption<?> object) {
            return ((DecimalOption) object).get().toPlainString();
        }
    }

    static class StringDecimalOptionDriver extends AbstractStringDriver {

        public StringDecimalOptionDriver(StringObjectInspector inspector) {
            super(inspector);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void set(ValueOption<?> target, String value) {
            try {
                ((DecimalOption) target).modify(new BigDecimal(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid decimal: {0}",
                        value), e);
            }
        }
    }

    static class StringDateOptionInspector extends AbstractStringInspector {

        @Override
        protected ValueOption<?> newObject() {
            return new DateOption();
        }

        @Override
        protected String toString(ValueOption<?> object) {
            return TemporalUtil.toDateString(((DateOption) object).get().getElapsedDays());
        }
    }

    static class StringDateOptionDriver extends AbstractStringDriver {

        public StringDateOptionDriver(StringObjectInspector inspector) {
            super(inspector);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void set(ValueOption<?> target, String value) {
            int result = TemporalUtil.parseDate(value);
            if (result < 0) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid date: {0}",
                        value));
            }
            ((DateOption) target).modify(result);
        }
    }

    static class StringDateTimeOptionInspector extends AbstractStringInspector {

        @Override
        protected ValueOption<?> newObject() {
            return new DateTimeOption();
        }

        @Override
        protected String toString(ValueOption<?> object) {
            return TemporalUtil.toTimestampString(((DateTimeOption) object).get().getElapsedSeconds());
        }
    }

    static class StringDateTimeOptionDriver extends AbstractStringDriver {

        public StringDateTimeOptionDriver(StringObjectInspector inspector) {
            super(inspector);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void set(ValueOption<?> target, String value) {
            long result = TemporalUtil.parseTimestamp(value);
            if (result < 0) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid timestamp: {0}",
                        value));
            }
            ((DateTimeOption) target).modify(result);
        }
    }
}
