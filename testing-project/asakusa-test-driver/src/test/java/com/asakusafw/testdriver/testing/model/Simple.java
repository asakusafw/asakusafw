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
package com.asakusafw.testdriver.testing.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.value.StringOption;

/**
 * A simple data model.
 */
@DataModelKind("DMDL")
public class Simple implements DataModel<Simple>, Writable {

    private final StringOption value = new StringOption();

    /**
     * Returns the value.
     * @return the value
     */
    public StringOption getValueOption() {
        return value;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public String getValueAsString() {
        return value.getAsString();
    }

    /**
     * Sets the value.
     * @param s the value
     */
    @SuppressWarnings("deprecation")
    public void setValueAsString(String s) {
        value.modify(s);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        value.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        value.readFields(in);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void reset() {
        value.setNull();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void copyFrom(Simple other) {
        value.copyFrom(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
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
        Simple other = (Simple) obj;
        return Objects.equals(value, other.value);
    }
}
