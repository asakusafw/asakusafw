/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdriver.mapreduce.mock;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;
import com.google.common.base.Objects;

/**
 * Mock data model class.
 * @since 0.1.0
 * @version 0.3.0
 */
public class MockData implements DataModel<MockData>, Writable {

    private final IntOption intValue = new IntOption();

    private final StringOption stringValue = new StringOption();

    private final DateOption dateValue = new DateOption();

    private final DateTimeOption datetimeValue = new DateTimeOption();

    /**
     * Puts values as {@link #getStringValueOption() string_value}.
     * @param output the target output
     * @param values the values to set
     * @throws IOException if failed
     */
    public static void put(ModelOutput<MockData> output, String... values) throws IOException {
        put(output, 0, values);
    }

    /**
     * Puts values as {@link #getStringValueOption() string_value}.
     * @param output the target output
     * @param offset starting ID number
     * @param values the values to set
     * @throws IOException if failed
     * @since 0.1.1
     */
    public static void put(ModelOutput<MockData> output, int offset, String... values) throws IOException {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (String s : values) {
            map.put(offset + map.size(), s);
        }
        put(output, map);
    }

    /**
     * Puts values.
     * @param output the target output
     * @param entries {@code (int_value, string_value)} pairs
     * @throws IOException if failed
     */
    public static void put(ModelOutput<MockData> output, Map<Integer, String> entries) throws IOException {
        MockData buf = new MockData();
        for (Map.Entry<Integer, String> entry : entries.entrySet()) {
            output.write(buf.set(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Collect values.
     * @param input the target input
     * @return {@code (int_value, string_value)} pairs
     * @throws IOException if failed
     */
    public static Map<Integer, String> collect(ModelInput<MockData> input) throws IOException {
        MockData buf = new MockData();
        Map<Integer, String> results = new LinkedHashMap<>();
        while (input.readTo(buf)) {
            results.put(buf.getKey(), buf.getValue());
        }
        return results;
    }

    /**
     * Sets {@code (int_value, string_value)} pair.
     * @param key the {@code int_value}
     * @param value the {@code string_value}
     * @return this
     */
    @SuppressWarnings("deprecation")
    public MockData set(int key, String value) {
        intValue.modify(key);
        stringValue.modify(value);
        return this;
    }

    /**
     * Returns the {@code int_value}.
     * @return {@code int_value}
     */
    public int getKey() {
        return intValue.get();
    }

    /**
     * Returns the {@code string_value}.
     * @return {@code string_value}
     */
    public String getValue() {
        return stringValue.getAsString();
    }

    /**
     * Returns the {@code int_value} as option.
     * @return the option value
     */
    public IntOption getIntValueOption() {
        return intValue;
    }

    /**
     * Returns the {@code string_value} as option.
     * @return the option value
     */
    public StringOption getStringValueOption() {
        return stringValue;
    }

    /**
     * Returns the {@code date_value} as option.
     * @return the option value
     */
    public DateOption getDateValueOption() {
        return dateValue;
    }

    /**
     * Returns the {@code datetime_value} as option.
     * @return the option vaule
     */
    public DateTimeOption getDatetimeValueOption() {
        return datetimeValue;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void reset() {
        intValue.setNull();
        stringValue.setNull();
        dateValue.setNull();
        datetimeValue.setNull();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void copyFrom(MockData other) {
        intValue.copyFrom(other.intValue);
        stringValue.copyFrom(other.stringValue);
        dateValue.copyFrom(other.dateValue);
        datetimeValue.copyFrom(other.datetimeValue);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        intValue.write(out);
        stringValue.write(out);
        dateValue.write(out);
        datetimeValue.write(out);
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        intValue.readFields(input);
        stringValue.readFields(input);
        dateValue.readFields(input);
        datetimeValue.readFields(input);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(intValue);
        result = prime * result + Objects.hashCode(stringValue);
        result = prime * result + Objects.hashCode(dateValue);
        result = prime * result + Objects.hashCode(datetimeValue);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MockData other = (MockData) obj;
        if (!Objects.equal(intValue, other.intValue)) {
            return false;
        }
        if (!Objects.equal(stringValue, other.stringValue)) {
            return false;
        }
        if (!Objects.equal(dateValue, other.dateValue)) {
            return false;
        }
        if (!Objects.equal(datetimeValue, other.datetimeValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("{int=%s, string=%s, date=%s, datetime=%s}", //$NON-NLS-1$
                intValue, stringValue, dateValue, datetimeValue);
    }
}
