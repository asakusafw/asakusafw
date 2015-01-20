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
package com.asakusafw.directio.hive.parquet;

import java.util.Arrays;

import org.apache.hadoop.hive.common.type.HiveBaseChar;
import org.apache.hadoop.io.Text;

import parquet.column.Dictionary;
import parquet.io.api.Binary;
import parquet.io.api.RecordConsumer;
import parquet.schema.OriginalType;
import parquet.schema.PrimitiveType.PrimitiveTypeName;
import parquet.schema.Type;
import parquet.schema.Types;

import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * {@link ParquetValueDriver} for character strings with its length limit.
 * @since 0.7.2
 */
public class LimitedStringValueDriver implements ParquetValueDriver {

    private final int length;

    /**
     * Creates a new instance.
     * @param length the limit length (in Unicode)
     */
    public LimitedStringValueDriver(int length) {
        this.length = length;
    }

    @Override
    public Type getType(String name) {
        return Types.optional(PrimitiveTypeName.BINARY)
                .as(OriginalType.UTF8)
                .named(name);
    }

    @Override
    public ValueConverter getConverter() {
        return new ToStringOption();
    }

    @Override
    public ValueWriter getWriter() {
        return new FromStringOption(length);
    }

    static final class FromStringOption implements ValueWriter {

        private final int limit;

        FromStringOption(int length) {
            this.limit = length;
        }

        @Override
        public void write(Object value, RecordConsumer consumer) {
            StringOption option = (StringOption) value;
            Text text = option.get();
            byte[] bytes = text.getBytes();
            int length = text.getLength();
            if (length > limit) {
                // if byte-length > limit, the string may code-point-count >= limit
                String stripped = HiveBaseChar.getPaddedValue(text.toString(), limit);
                consumer.addBinary(Binary.fromString(stripped));
            } else {
                consumer.addBinary(Binary.fromByteArray(bytes, 0, length));
            }
        }
    }

    static final class ToStringOption extends ValueConverter {

        private Text[] dict;

        private StringOption target;

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
            Text[] buf = prepareDictionaryBuffer(dictionary);
            for (int id = 0, max = dictionary.getMaxId(); id <= max; id++) {
                Text text = new Text();
                Binary binary = dictionary.decodeToBinary(id);
                setBinary(text, binary);
                buf[id] = text;
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void addValueFromDictionary(int dictionaryId) {
            target.modify(dict[dictionaryId]);
        }

        @Override
        public void addBinary(Binary value) {
            target.reset();
            setBinary(target.get(), value);
        }

        private Text[] prepareDictionaryBuffer(Dictionary dictionary) {
            int size = dictionary.getMaxId() + 1;
            if (this.dict == null || this.dict.length < size) {
                int capacity = (int) (size * 1.2) + 1;
                this.dict = new Text[capacity];
            } else {
                Arrays.fill(this.dict, null);
            }
            return this.dict;
        }

        private void setBinary(Text text, Binary binary) {
            // TODO check it length?
            text.set(binary.getBytes(), 0, binary.length());
        }
    }
}
