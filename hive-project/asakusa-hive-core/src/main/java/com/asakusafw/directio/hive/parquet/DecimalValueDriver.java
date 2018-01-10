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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.ValueOption;

import parquet.column.Dictionary;
import parquet.io.api.Binary;
import parquet.io.api.RecordConsumer;
import parquet.schema.OriginalType;
import parquet.schema.PrimitiveType;
import parquet.schema.PrimitiveType.PrimitiveTypeName;
import parquet.schema.Type;
import parquet.schema.Types;
import parquet.schema.Types.PrimitiveBuilder;

/**
 * {@link ParquetValueDriver} for decimals.
 * @since 0.10.0
 */
public class DecimalValueDriver implements ParquetValueDriver {

    static final Log LOG = LogFactory.getLog(DataModelMaterializer.class);

    static final String KEY_PREFIX = "com.asakusafw.hive.parquet.decimal.";

    static final String KEY_USE_BINARY = KEY_PREFIX + "binary";

    static final boolean USE_BINARY = Optional.ofNullable(System.getProperty(KEY_USE_BINARY))
            .map(String::trim)
            .map(it -> it.isEmpty() || Boolean.parseBoolean(it))
            .orElse(false);

    private static final Method BUILDER_LENGTH_METHOD;

    private static final Method BUILDER_PRECISION_METHOD;

    private static final Method BUILDER_SCALE_METHOD;

    static {
        BUILDER_LENGTH_METHOD = findBuilderMethod("length");
        BUILDER_PRECISION_METHOD = findBuilderMethod("precision");
        BUILDER_SCALE_METHOD = findBuilderMethod("scale");
    }

    private static Method findBuilderMethod(String name) {
        try {
            return PrimitiveBuilder.class.getMethod(name, int.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Parquet library does not support: {0}#{1}(int)",
                    PrimitiveBuilder.class.getSimpleName(),
                    name), e);
        }
    }

    static final int PRECISION_INT_MAX = 9;

    static final int PRECISION_LONG_MAX = 18;

    private final int precision;

    private final int scale;

    /**
     * Creates a new instance.
     * @param precision the precision
     * @param scale the scale
     */
    public DecimalValueDriver(int precision, int scale) {
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public Type getType(String name) {
        int byteLength = getByteLength(precision);
        PrimitiveTypeName typeName = USE_BINARY ? PrimitiveTypeName.BINARY : PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY;
        PrimitiveBuilder<PrimitiveType> builder = Types.optional(typeName).as(OriginalType.DECIMAL);
        // NOTE: return types of PrimitiveBuilder.{length,precision,scale} are unstable
        try {
            BUILDER_LENGTH_METHOD.invoke(builder, byteLength);
            BUILDER_PRECISION_METHOD.invoke(builder, precision);
            BUILDER_SCALE_METHOD.invoke(builder, scale);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("error occurred while resolving decimal type", e);
        }
        return builder.named(name);
    }

    static int getByteLength(int precision) {
        if (precision == 0) {
            return 1;
        }
        int bits = BigInteger.TEN.pow(precision).bitLength() + 1;
        return (bits + Byte.SIZE - 1) / Byte.SIZE;
    }

    @Override
    public ValueConverter getConverter() {
        return new DecimalConverter(scale);
    }

    @Override
    public ValueWriter getWriter() {
        return new DecimalWriter(getByteLength(precision), precision, scale);
    }

    private static class DecimalConverter extends ValueConverter {

        private final int scale;

        private DecimalOption target;

        private BigDecimal[] dict;

        DecimalConverter(int scale) {
            this.scale = scale;
        }

        @Override
        public void set(ValueOption<?> value) {
            this.target = (DecimalOption) value;
        }

        @Override
        public boolean hasDictionarySupport() {
            return true;
        }

        @Override
        public void setDictionary(Dictionary dictionary) {
            BigDecimal[] buf = prepareDictionaryBuffer(dictionary);
            setDictionaryAsBinary(dictionary, buf);
        }

        private void setDictionaryAsBinary(Dictionary dictionary, BigDecimal[] buf) {
            for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                buf[id] = convert(dictionary.decodeToBinary(id));
            }
        }

        private BigDecimal[] prepareDictionaryBuffer(Dictionary dictionary) {
            int size = dictionary.getMaxId() + 1;
            if (this.dict == null || this.dict.length < size) {
                int capacity = (int) (size * 1.2) + 1;
                this.dict = new BigDecimal[capacity];
            } else {
                Arrays.fill(this.dict, null);
            }
            return this.dict;
        }

        private BigDecimal convert(int value) {
            return new BigDecimal(BigInteger.valueOf(value), scale);
        }

        private BigDecimal convert(long value) {
            return new BigDecimal(BigInteger.valueOf(value), scale);
        }

        private BigDecimal convert(Binary value) {
            BigInteger unscaled = new BigInteger(value.getBytes());
            return new BigDecimal(unscaled, scale);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addInt(int value) {
            target.modify(convert(value));
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addLong(long value) {
            target.modify(convert(value));
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addBinary(Binary value) {
            target.modify(convert(value));
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addValueFromDictionary(int dictionaryId) {
            target.modify(dict[dictionaryId]);
        }
    }

    private static class DecimalWriter implements ValueWriter {

        private final int byteLength;

        private final int scale;

        private final int precision;

        DecimalWriter(int byteLength, int precision, int scale) {
            this.byteLength = byteLength;
            this.scale = scale;
            this.precision = precision;
        }

        @Override
        public final void write(Object value, RecordConsumer consumer) {
            BigDecimal decimal = ((DecimalOption) value).get();
            BigInteger unscaled = decimal
                    .setScale(scale, RoundingMode.FLOOR)
                    .unscaledValue();
            writeAsBinary(consumer, decimal, unscaled);
        }

        private void writeAsBinary(RecordConsumer consumer, BigDecimal original, BigInteger unscaled) {
            byte[] bytes = unscaled.toByteArray();
            Binary binary;
            if (bytes.length == byteLength) {
                binary = Binary.fromByteArray(bytes);
            } else if (bytes.length < byteLength) {
                byte[] newBytes = new byte[byteLength];
                if (unscaled.signum() < 0) {
                    Arrays.fill(newBytes, 0, newBytes.length - bytes.length, (byte) -1);
                }
                System.arraycopy(bytes, 0, newBytes, newBytes.length - bytes.length, bytes.length);
                binary = Binary.fromByteArray(newBytes);
            } else {
                throw new IllegalStateException(MessageFormat.format(
                        Messages.getString("DecimalValueDriver.errorPrecisionTooSmall"), //$NON-NLS-1$
                        original,
                        precision));
            }
            consumer.addBinary(binary);
        }
    }
}
