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
package com.asakusafw.directio.hive.parquet;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.typeinfo.BaseCharTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.directio.hive.util.TemporalUtil;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

import parquet.column.Dictionary;
import parquet.io.api.Binary;
import parquet.schema.OriginalType;
import parquet.schema.PrimitiveType;
import parquet.schema.PrimitiveType.PrimitiveTypeName;
import parquet.schema.Type;
import parquet.schema.Type.Repetition;

/**
 * Provides {@link ParquetValueDriver}.
 * @since 0.7.0
 * @version 0.7.4
 */
public enum ParquetValueDrivers implements ParquetValueDriver {

    /**
     * Driver for basic {@link BooleanOption}.
     */
    BOOLEAN(BooleanOption.class, TypeInfoFactory.booleanTypeInfo, PrimitiveTypeName.BOOLEAN) {
        @Override
        public ValueConverter getConverter() {
            return new BooleanOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.BOOLEAN;
        }
    },

    /**
     * Driver for basic {@link ByteOption}.
     */
    BYTE(ByteOption.class, TypeInfoFactory.byteTypeInfo, PrimitiveTypeName.INT32) {
        @Override
        public ValueConverter getConverter() {
            return new ByteOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.BYTE;
        }
    },

    /**
     * Driver for basic {@link ShortOption}.
     */
    SHORT(ShortOption.class, TypeInfoFactory.shortTypeInfo, PrimitiveTypeName.INT32) {
        @Override
        public ValueConverter getConverter() {
            return new ShortOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.SHORT;
        }
    },

    /**
     * Driver for basic {@link IntOption}.
     */
    INT(IntOption.class, TypeInfoFactory.intTypeInfo, PrimitiveTypeName.INT32) {
        @Override
        public ValueConverter getConverter() {
            return new IntOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.INT;
        }
    },

    /**
     * Driver for basic {@link LongOption}.
     */
    LONG(LongOption.class, TypeInfoFactory.longTypeInfo, PrimitiveTypeName.INT64) {
        @Override
        public ValueConverter getConverter() {
            return new LongOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.LONG;
        }
    },

    /**
     * Driver for basic {@link FloatOption}.
     */
    FLOAT(FloatOption.class, TypeInfoFactory.floatTypeInfo, PrimitiveTypeName.FLOAT) {
        @Override
        public ValueConverter getConverter() {
            return new FloatOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.FLOAT;
        }
    },

    /**
     * Driver for basic {@link DoubleOption}.
     */
    DOUBLE(DoubleOption.class, TypeInfoFactory.doubleTypeInfo, PrimitiveTypeName.DOUBLE) {
        @Override
        public ValueConverter getConverter() {
            return new DoubleOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.DOUBLE;
        }
    },

    /**
     * Driver for basic {@link StringOption}.
     */
    STRING(StringOption.class, TypeInfoFactory.stringTypeInfo, PrimitiveTypeName.BINARY) {
        @Override
        public ValueConverter getConverter() {
            return new StringOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.STRING;
        }
        @Override
        public Type getType(String name) {
            return new PrimitiveType(Repetition.OPTIONAL, typeName, name, OriginalType.UTF8);
        }
    },

    /**
     * Driver for basic {@link DateOption}.
     * @since 0.7.4
     */
    DATE(DateOption.class, TypeInfoFactory.dateTypeInfo, PrimitiveTypeName.INT32) {
        @Override
        public ValueConverter getConverter() {
            return new DateOptionConverter();
        }
        @Override
        public ValueWriter getWriter() {
            return BasicValueWriter.DATE;
        }
        @Override
        public Type getType(String name) {
            return new PrimitiveType(Repetition.OPTIONAL, typeName, name, OriginalType.DATE);
        }
    },
    ;

    final boolean standard;

    private final Class<? extends ValueOption<?>> valueOptionClass;

    private final TypeInfo typeInfo;

    final PrimitiveTypeName typeName;

    ParquetValueDrivers(
            Class<? extends ValueOption<?>>
            valueOptionClass, TypeInfo typeInfo,
            PrimitiveTypeName typeName) {
        this(true, valueOptionClass, typeInfo, typeName);
    }

    ParquetValueDrivers(
            boolean standard,
            Class<? extends ValueOption<?>>
            valueOptionClass, TypeInfo typeInfo,
            PrimitiveTypeName typeName) {
        this.standard = standard;
        this.valueOptionClass = valueOptionClass;
        this.typeInfo = typeInfo;
        this.typeName = typeName;
    }

    /**
     * Returns the Asakusa type information for this driver.
     * @return the value option class
     */
    public Class<? extends ValueOption<?>> getValueOptionClass() {
        return valueOptionClass;
    }

    /**
     * Returns the Hive type information for this driver.
     * @return the type information
     */
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    @Override
    public Type getType(String name) {
        return new PrimitiveType(Repetition.OPTIONAL, typeName, name);
    }

    /**
     * Returns a {@link ParquetValueDriver} for the specified type.
     * @param typeInfo the Hive type info
     * @param valueClass the {@link ValueOption} type
     * @return the corresponded {@link ParquetValueDriver}
     */
    public static ParquetValueDriver of(TypeInfo typeInfo, Class<?> valueClass) {
        ParquetValueDriver result = find(typeInfo, valueClass);
        if (result == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("ParquetValueDrivers.errorUndefinedValueDriver"), //$NON-NLS-1$
                    typeInfo.getQualifiedName(),
                    valueClass.getSimpleName()));
        }
        return result;
    }

    /**
     * Returns a {@link ParquetValueDriver} for the specified type.
     * @param typeInfo the Hive type info
     * @param valueClass the {@link ValueOption} type
     * @return the corresponded {@link ParquetValueDriver}, or {@code null} if it is not found
     */
    public static ParquetValueDriver find(TypeInfo typeInfo, Class<?> valueClass) {
        ParquetValueDrivers basic = Lazy.FROM_CLASS.get(valueClass);
        if (basic != null && basic.typeInfo.equals(typeInfo)) {
            return basic;
        }
        if (typeInfo.getCategory() != Category.PRIMITIVE) {
            return null;
        }
        switch (((PrimitiveTypeInfo) typeInfo).getPrimitiveCategory()) {
        case STRING:
            return StringValueDrivers.find(valueClass);

        case DECIMAL:
            if (valueClass == DecimalOption.class) {
                DecimalTypeInfo decimal = (DecimalTypeInfo) typeInfo;
                return new DecimalValueDriver(decimal.precision(), decimal.scale());
            }
            return null;

        case TIMESTAMP:
            return TimestampValueDrivers.find(valueClass);

        case CHAR:
        case VARCHAR:
            if (valueClass == StringOption.class) {
                int length = ((BaseCharTypeInfo) typeInfo).getLength();
                return new LimitedStringValueDriver(length);
            }
            return null;

        case BOOLEAN:
        case BYTE:
        case DOUBLE:
        case FLOAT:
        case INT:
        case LONG:
        case SHORT:
        case UNKNOWN:
        case VOID:
        case DATE:
        case BINARY:
            return null;
        default:
            throw new AssertionError(typeInfo);
        }
    }

    private static final class Lazy {

        static final Map<Class<?>, ParquetValueDrivers> FROM_CLASS;
        static {
            Map<Class<?>, ParquetValueDrivers> map = new HashMap<>();
            for (ParquetValueDrivers element : ParquetValueDrivers.values()) {
                if (element.standard) {
                    map.put(element.getValueOptionClass(), element);
                }
            }
            FROM_CLASS = map;
        }

        private Lazy() {
            return;
        }
    }

    private static final class BooleanOptionConverter extends ValueConverter {

        private BooleanOption target;

        BooleanOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (BooleanOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addBoolean(boolean value) {
            this.target.modify(value);
        }
    }

    private static final class ByteOptionConverter extends ValueConverter {

        private ByteOption target;

        ByteOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (ByteOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addInt(int value) {
            this.target.modify((byte) value);
        }
    }

    private static final class ShortOptionConverter extends ValueConverter {

        private ShortOption target;

        ShortOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (ShortOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addInt(int value) {
            this.target.modify((short) value);
        }
    }

    private static final class IntOptionConverter extends ValueConverter {

        private IntOption target;

        IntOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (IntOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addInt(int value) {
            this.target.modify(value);
        }
    }

    private static final class LongOptionConverter extends ValueConverter {

        private LongOption target;

        LongOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (LongOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addLong(long value) {
            this.target.modify(value);
        }
    }

    private static final class FloatOptionConverter extends ValueConverter {

        private FloatOption target;

        FloatOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (FloatOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addFloat(float value) {
            this.target.modify(value);
        }
    }

    private static final class DoubleOptionConverter extends ValueConverter {

        private DoubleOption target;

        DoubleOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DoubleOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addDouble(double value) {
            this.target.modify(value);
        }
    }

    private static final class StringOptionConverter extends ValueConverter {

        private StringOption target;

        private Binary[] dict;

        StringOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (StringOption) value;
        }

        @Override
        public boolean hasDictionarySupport() {
            return true;
        }

        @Override
        public void setDictionary(Dictionary dictionary) {
            Binary[] buf = prepareDictionaryBuffer(dictionary);
            for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                buf[id] = dictionary.decodeToBinary(id);
            }
        }

        @Override
        public void addValueFromDictionary(int dictionaryId) {
            addBinary(dict[dictionaryId]);
        }

        @Override
        public void addBinary(Binary value) {
            target.reset();
            target.get().set(value.getBytes(), 0, value.length());
        }

        private Binary[] prepareDictionaryBuffer(Dictionary dictionary) {
            int size = dictionary.getMaxId() + 1;
            if (this.dict == null || this.dict.length < size) {
                int capacity = (int) (size * 1.2) + 1;
                this.dict = new Binary[capacity];
            } else {
                Arrays.fill(this.dict, null);
            }
            return this.dict;
        }
    }

    private static final class DateOptionConverter extends ValueConverter {

        private DateOption target;

        DateOptionConverter() {
            return;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DateOption) value;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addInt(int value) {
            this.target.modify(TemporalUtil.toElapsedDays(value));
        }
    }
}
