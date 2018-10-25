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
package com.asakusafw.runtime.io.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;

class OutputDriver<T> implements JsonOutput<T> {

    static final Logger LOG = LoggerFactory.getLogger(OutputDriver.class);

    private final String path;

    private final JsonGenerator generator;

    private final Property<T, ?>[] properties;

    private final PropertyWriter adapter;

    OutputDriver(String path, JsonGenerator generator, Collection<? extends PropertyInfo<T, ?>> properties) {
        this.path = path;
        this.generator = generator;
        this.properties = convert(properties); // NOTE: cannot invoke Property(PropertyInfo) via Stream
        this.adapter = new PropertyWriter(generator);
    }

    private static <T> Property<T, ?>[] convert(Collection<? extends PropertyInfo<T, ?>> properties) {
        @SuppressWarnings("unchecked")
        Property<T, ?>[] results = (Property<T, ?>[]) new Property<?, ?>[properties.size()];
        int index = 0;
        for (PropertyInfo<T, ?> info : properties) {
            results[index++] = new Property<>(info);
        }
        return results;
    }

    @Override
    public void write(T model) throws IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format(
                    "writing record: path=%s, object=%s", //$NON-NLS-1$
                    path,
                    model));
        }
        generator.writeStartObject();
        for (Property<T, ?> driver : properties) {
            driver.apply(model, adapter);
        }
        generator.writeEndObject();
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }

    static class Property<T, P> {

        final Function<? super T, ? extends P> extractor;

        final PropertyDefinition<? super P> definition;

        final PropertyAdapter<? super P> adapter;

        final SerializedString name;

        Property(PropertyInfo<T, P> info) {
            this.extractor = info.extractor;
            this.definition = info.definition;
            this.adapter = info.definition.getAdapter();
            this.name = new SerializedString(definition.getName());
        }

        void apply(T model, PropertyWriter writer) throws IOException {
            P property = extractor.apply(model);
            writer.setPropertyName(name);
            adapter.write(property, writer);
        }
    }

    static class PropertyWriter implements ValueWriter {

        private static final int CHAR_BUFFER_PADDING = 16;

        final JsonGenerator generator;

        SerializableString propertyName;

        private char[] charBuffer;

        PropertyWriter(JsonGenerator generator) {
            this.generator = generator;
        }

        void setPropertyName(SerializableString name) {
            this.propertyName = name;
        }

        @Override
        public void writeNull() throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeNull();
        }

        @Override
        public void writeString(CharSequence sequence, int offset, int length) throws IOException {
            if (offset == 0 && sequence.length() == length && sequence instanceof String) {
                generator.writeFieldName(propertyName);
                generator.writeString((String) sequence);
                return;
            }
            if (sequence instanceof StringBuilder) {
                StringBuilder s = (StringBuilder) sequence;
                char[] buf = borrow(length);
                s.getChars(offset, offset + length, buf, 0);
                writeStringField(buf, 0, length);
                return;
            }
            if (sequence instanceof CharBuffer) {
                CharBuffer s = (CharBuffer) sequence;
                if (s.hasArray()) {
                    writeStringField(s.array(), offset + s.arrayOffset(), length);
                    return;
                }
            }
            char[] buf = borrow(length);
            for (int i = 0; i < length; i++) {
                buf[i] = sequence.charAt(i + offset);
            }
            writeStringField(buf, 0, length);
        }

        private char[] borrow(int length) {
            char[] buf = charBuffer;
            if (buf != null && buf.length >= length) {
                return buf;
            }
            charBuffer = new char[length + CHAR_BUFFER_PADDING];
            return charBuffer;
        }

        private void writeStringField(char[] array, int offset, int length) throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeString(array, offset, length);
        }

        @Override
        public void writeDecimal(BigDecimal value) throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeNumber(value);
        }

        @Override
        public void writeInt(int value) throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeNumber(value);
        }

        @Override
        public void writeLong(long value) throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeNumber(value);
        }

        @Override
        public void writeFloat(float value) throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeNumber(value);
        }

        @Override
        public void writeDouble(double value) throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeNumber(value);
        }

        @Override
        public void writeBoolean(boolean value) throws IOException {
            generator.writeFieldName(propertyName);
            generator.writeBoolean(value);
        }
    }
}
