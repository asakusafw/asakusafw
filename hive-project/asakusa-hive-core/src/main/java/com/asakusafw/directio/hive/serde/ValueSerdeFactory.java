/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BooleanObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.ByteObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.DateObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.DoubleObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.FloatObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveCharObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveDecimalObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveVarcharObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.LongObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.ShortObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.TimestampObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Factory for {@link ValueSerde}.
 * @since 0.7.0
 */
public enum ValueSerdeFactory implements ValueSerde {

    /**
     * {@link BooleanOption}.
     */
    BOOLEAN(BooleanOption.class, new BooleanOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new BooleanOptionDriver((BooleanObjectInspector) target);
        }
    },

    /**
     * {@link ByteOption}.
     */
    BYTE(ByteOption.class, new ByteOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new ByteOptionDriver((ByteObjectInspector) target);
        }
    },

    /**
     * {@link ShortOption}.
     */
    SHORT(ShortOption.class, new ShortOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new ShortOptionDriver((ShortObjectInspector) target);
        }
    },

    /**
     * {@link IntOption}.
     */
    INT(IntOption.class, new IntOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new IntOptionDriver((IntObjectInspector) target);
        }
    },

    /**
     * {@link LongOption}.
     */
    LONG(LongOption.class, new LongOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new LongOptionDriver((LongObjectInspector) target);
        }
    },

    /**
     * {@link FloatOption}.
     */
    FLOAT(FloatOption.class, new FloatOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new FloatOptionDriver((FloatObjectInspector) target);
        }
    },

    /**
     * {@link DoubleOption}.
     */
    DOUBLE(DoubleOption.class, new DoubleOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new DoubleOptionDriver((DoubleObjectInspector) target);
        }
    },

    /**
     * {@link DecimalOption}.
     */
    DECIMAL(DecimalOption.class, new DecimalOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new DecimalOptionDriver((HiveDecimalObjectInspector) target);
        }
    },

    /**
     * {@link StringOption}.
     */
    STRING(StringOption.class, new StringOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new StringOptionDriver((StringObjectInspector) target);
        }
    },

    /**
     * {@link DateOption}.
     */
    DATE(DateOption.class, new DateOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new DateOptionDriver((DateObjectInspector) target);
        }
    },

    /**
     * {@link DateTimeOption}.
     */
    DATETIME(DateTimeOption.class, new DateTimeOptionInspector()) {
        @Override
        public ValueDriver getDriver(ObjectInspector target) {
            return new DateTimeOptionDriver((TimestampObjectInspector) target);
        }
    },
    ;

    private final Class<? extends ValueOption<?>> valueOptionClass;

    private final PrimitiveObjectInspector objectInspector;

    private ValueSerdeFactory(
            Class<? extends ValueOption<?>> valueOptionClass,
            PrimitiveObjectInspector objectInspector) {
        this.valueOptionClass = valueOptionClass;
        this.objectInspector = objectInspector;
    }

    @Override
    public TypeInfo getTypeInfo() {
        return objectInspector.getTypeInfo();
    }

    @Override
    public Class<? extends ValueOption<?>> getValueClass() {
        return valueOptionClass;
    }

    @Override
    public ObjectInspector getInspector() {
        return objectInspector;
    }

    /**
     * {@link StringOption} as {@code char}.
     * @param length the character string length
     * @return the serialization object for string
     */
    public static ValueSerde getChar(final int length) {
        return new ValueSerde() {
            final PrimitiveObjectInspector inspector = new CharStringOptionInspector(length);
            @Override
            public TypeInfo getTypeInfo() {
                return inspector.getTypeInfo();
            }
            @Override
            public Class<? extends ValueOption<?>> getValueClass() {
                return StringOption.class;
            }
            @Override
            public ObjectInspector getInspector() {
                return inspector;
            }
            @Override
            public ValueDriver getDriver(ObjectInspector target) {
                return new CharStringOptionDriver((HiveCharObjectInspector) target);
            }
        };
    }

    /**
     * {@link StringOption} as {@code varchar}.
     * @param length the character string length
     * @return the serialization object for string
     */
    public static ValueSerde getVarchar(final int length) {
        return new ValueSerde() {
            final PrimitiveObjectInspector inspector = new VarcharStringOptionInspector(length);
            @Override
            public TypeInfo getTypeInfo() {
                return inspector.getTypeInfo();
            }
            @Override
            public Class<? extends ValueOption<?>> getValueClass() {
                return StringOption.class;
            }
            @Override
            public ObjectInspector getInspector() {
                return inspector;
            }
            @Override
            public ValueDriver getDriver(ObjectInspector target) {
                return new VarcharStringOptionDriver((HiveVarcharObjectInspector) target);
            }
        };
    }

    /**
     * {@link DecimalOption} with precision, scale.
     * @param precision the decimal precision
     * @param scale the decimal scale
     * @return the serialization object for decimal
     */
    public static ValueSerde getDecimal(final int precision, final int scale) {
        return new ValueSerde() {
            final PrimitiveObjectInspector inspector = new DecimalOptionInspector(precision, scale);
            @Override
            public TypeInfo getTypeInfo() {
                return inspector.getTypeInfo();
            }
            @Override
            public Class<? extends ValueOption<?>> getValueClass() {
                return DecimalOption.class;
            }
            @Override
            public ObjectInspector getInspector() {
                return inspector;
            }
            @Override
            public ValueDriver getDriver(ObjectInspector target) {
                return new DecimalOptionDriver((HiveDecimalObjectInspector) target);
            }
        };
    }

    /**
     * Returns a {@link ValueSerde} for the specified {@link ValueOption} class.
     * @param aClass the target {@link ValueOption} class.
     * @return the default {@link ValueSerde} for the target {@link ValueOption} class,
     *    or {@code null} if the target class has no {@link ValueSerde} object
     */
    public static ValueSerdeFactory fromClass(Class<?> aClass) {
        return Lazy.FROM_CLASS.get(aClass);
    }

    private static final class Lazy {

        static final Map<Class<? extends ValueOption<?>>, ValueSerdeFactory> FROM_CLASS;
        static {
            Map<Class<? extends ValueOption<?>>, ValueSerdeFactory> map =
                    new HashMap<Class<? extends ValueOption<?>>, ValueSerdeFactory>();
            for (ValueSerdeFactory serde : ValueSerdeFactory.values()) {
                map.put(serde.getValueClass(), serde);
            }
            FROM_CLASS = map;
        }

        private Lazy() {
            return;
        }
    }
}
