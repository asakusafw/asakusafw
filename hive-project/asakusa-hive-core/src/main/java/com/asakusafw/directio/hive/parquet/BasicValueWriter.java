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

import java.text.MessageFormat;

import org.apache.hadoop.io.Text;

import parquet.io.api.Binary;
import parquet.io.api.RecordConsumer;

import com.asakusafw.directio.hive.util.TemporalUtil;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Provides {@link ValueWriter}s.
 * @since 0.7.0
 * @version 0.7.4
 */
public enum BasicValueWriter implements ValueWriter {

    /**
     * Writer for {@link BooleanOption}.
     */
    BOOLEAN {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addBoolean(((BooleanOption) value).get());
        }
    },

    /**
     * Writer for {@link ByteOption}.
     */
    BYTE {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addInteger(((ByteOption) value).get());
        }
    },

    /**
     * Writer for {@link ShortOption}.
     */
    SHORT {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addInteger(((ShortOption) value).get());
        }
    },

    /**
     * Writer for {@link IntOption}.
     */
    INT {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addInteger(((IntOption) value).get());
        }
    },

    /**
     * Writer for {@link LongOption}.
     */
    LONG {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addLong(((LongOption) value).get());
        }
    },

    /**
     * Writer for {@link FloatOption}.
     */
    FLOAT {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addFloat(((FloatOption) value).get());
        }
    },

    /**
     * Writer for {@link DoubleOption}.
     */
    DOUBLE {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            consumer.addDouble(((DoubleOption) value).get());
        }
    },

    /**
     * Writer for {@link StringOption}.
     */
    STRING {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            Text text = ((StringOption) value).get();
            consumer.addBinary(Binary.fromByteArray(text.getBytes(), 0, text.getLength()));
        }
    },

    /**
     * Writer for {@link DateOption}.
     * @since 0.7.4
     */
    DATE {
        @Override
        public void write(Object value, RecordConsumer consumer) {
            DateOption option = (DateOption) value;
            int days = TemporalUtil.getDaysSinceEpoch(option.get());
            if (days < 0) {
                throw new IllegalStateException(MessageFormat.format(
                        Messages.getString("BasicValueWriter.errorBeforeEpoch"), //$NON-NLS-1$
                        option,
                        new Date(TemporalUtil.DATE_EPOCH_OFFSET)));
            }
            consumer.addInteger(days);
        }
    }
    ;
}
