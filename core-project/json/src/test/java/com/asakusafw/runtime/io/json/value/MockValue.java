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
package com.asakusafw.runtime.io.json.value;

import java.math.BigDecimal;

import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;

class MockValue implements ValueReader, ValueWriter {

    Object entity;

    MockValue() {
        this(void.class);
    }

    MockValue(Object entity) {
        this.entity = entity;
    }

    Object get() {
        return entity;
    }

    void set(Object value) {
        this.entity = value;
    }

    @Override
    public boolean isNull() {
        return entity == null;
    }

    @Override
    public void readString(StringBuilder buffer) {
        buffer.append((String) entity);
    }

    @Override
    public String readString() {
        return (String) entity;
    }

    @Override
    public BigDecimal readDecimal() {
        return (BigDecimal) entity;
    }

    @Override
    public int readInt() {
        return (int) entity;
    }

    @Override
    public long readLong() {
        return (long) entity;
    }

    @Override
    public float readFloat() {
        return (float) entity;
    }

    @Override
    public double readDouble() {
        return (double) entity;
    }

    @Override
    public boolean readBoolean() {
        return (boolean) entity;
    }

    @Override
    public void writeNull() {
        entity = null;
    }

    @Override
    public void writeString(CharSequence sequence, int offset, int length) {
        entity = sequence.subSequence(offset, offset + length).toString();
    }

    @Override
    public void writeDecimal(BigDecimal value) {
        entity = value;
    }

    @Override
    public void writeInt(int value) {
        entity = value;
    }

    @Override
    public void writeLong(long value) {
        entity = value;
    }

    @Override
    public void writeFloat(float value) {
        entity = value;
    }

    @Override
    public void writeDouble(double value) {
        entity = value;
    }

    @Override
    public void writeBoolean(boolean value) {
        entity = value;
    }
}
