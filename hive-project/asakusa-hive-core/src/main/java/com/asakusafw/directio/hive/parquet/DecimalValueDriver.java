/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Arrays;

import parquet.column.Dictionary;
import parquet.io.api.Binary;
import parquet.io.api.RecordConsumer;
import parquet.schema.OriginalType;
import parquet.schema.PrimitiveType;
import parquet.schema.PrimitiveType.PrimitiveTypeName;
import parquet.schema.Type;
import parquet.schema.Types;
import parquet.schema.Types.PrimitiveBuilder;

import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * {@link ParquetValueDriver} for decimals.
 * @since 0.7.2
 */
public class DecimalValueDriver implements ParquetValueDriver {

    static final int PRECISION_INT_MAX = 9;

    static final int PRECISION_LONG_MAX = 18;

    private final int precision;

    private final int scale;

    private final boolean forceBinary;

    /**
     * Creates a new instance.
     * @param precision the precision
     * @param scale the scale
     */
    public DecimalValueDriver(int precision, int scale) {
        this(precision, scale, true);
    }

    /**
     * Creates a new instance.
     * @param precision the precision
     * @param scale the scale
     * @param forceBinary forcibly use {@code PrimitiveType.BINARY} even if precision is so small
     */
    public DecimalValueDriver(int precision, int scale, boolean forceBinary) {
        this.precision = precision;
        this.scale = scale;
        this.forceBinary = forceBinary;
    }

    @Override
    public Type getType(String name) {
        int byteLength = getByteLength(precision);
        PrimitiveTypeName typeName = getTypeNameFor(byteLength);
        PrimitiveBuilder<PrimitiveType> builder = Types.optional(typeName)
                .as(OriginalType.DECIMAL)
                .precision(precision)
                .scale(scale);
        if (typeName == PrimitiveTypeName.BINARY) {
            builder.length(byteLength);
        }
        return builder.named(name);
    }

    private PrimitiveTypeName getTypeNameFor(int byteLength) {
        if (forceBinary) {
            return PrimitiveTypeName.BINARY;
        } else if (byteLength <= 4) {
            return PrimitiveTypeName.INT32;
        } else if (byteLength <= 8) {
            return PrimitiveTypeName.INT64;
        } else {
            return PrimitiveTypeName.BINARY;
        }
    }

    @Override
    public ValueConverter getConverter() {
        return new DecimalConverter(getByteLength(precision), scale, forceBinary);
    }

    @Override
    public ValueWriter getWriter() {
        return new DecimalWriter(getByteLength(precision), precision, scale, forceBinary);
    }

    static int getByteLength(int precision) {
        if (precision == 0) {
            return 1;
        }
        int bits = BigInteger.TEN.pow(precision).bitLength() + 1;
        return (bits + Byte.SIZE - 1) / Byte.SIZE;
    }

    private static class DecimalConverter extends ValueConverter {

        private final int byteLength;

        private final int scale;

        private final boolean forceBinary;

        private DecimalOption target;

        private BigDecimal[] dict;

        DecimalConverter(int byteLength, int scale, boolean forceBinary) {
            this.byteLength = byteLength;
            this.scale = scale;
            this.forceBinary = forceBinary;
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
            if (forceBinary) {
                setDictionaryAsBinary(dictionary, buf);
            } else if (byteLength <= 4) {
                for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                    buf[id] = convert(dictionary.decodeToInt(id));
                }
            } else if (byteLength <= 8) {
                for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                    buf[id] = convert(dictionary.decodeToLong(id));
                }
            } else {
                setDictionaryAsBinary(dictionary, buf);
            }
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

        private final boolean forceBinary;

        DecimalWriter(int byteLength, int precision, int scale, boolean forceBinary) {
            this.byteLength = byteLength;
            this.scale = scale;
            this.forceBinary = forceBinary;
            this.precision = precision;
        }

        @Override
        public final void write(Object value, RecordConsumer consumer) {
            BigDecimal decimal = ((DecimalOption) value).get();
            BigInteger unscaled = decimal
                    .setScale(scale, RoundingMode.FLOOR)
                    .unscaledValue();
            if (forceBinary) {
                writeAsBinary(consumer, decimal, unscaled);
            } else if (byteLength <= 4) {
                 consumer.addInteger(unscaled.intValue());
            } else if (byteLength <= 8) {
                consumer.addLong(unscaled.intValue());
            } else {
                writeAsBinary(consumer, decimal, unscaled);
            }
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
