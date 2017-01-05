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
package com.asakusafw.compiler.operator.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
/**
 * A data model class that represents mock_hoge.
 */
@DataModelKind("DMDL")@PropertyOrder({"value"}) public class MockHoge implements DataModel<MockHoge>, MockProjection,
        Writable {
    private final IntOption value = new IntOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.value.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(MockHoge other) {
        this.value.copyFrom(other.value);
    }
    /**
     * Returns value.
     * @return value
     * @throws NullPointerException if value is <code>null</code>
     */
    @Override
    public int getValue() {
        return this.value.get();
    }
    /**
     * Sets value.
     * @param value0 the value
     */
    @Override
    @SuppressWarnings("deprecation") public void setValue(int value0) {
        this.value.modify(value0);
    }
    /**
     * Returns value which may be represent <code>null</code>.
     * @return value
     */
    @Override
    public IntOption getValueOption() {
        return this.value;
    }
    /**
     * Sets value.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @Override
    @SuppressWarnings("deprecation") public void setValueOption(IntOption option) {
        this.value.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=mock_hoge");
        result.append(", value=");
        result.append(this.value);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
        return result;
    }
    @Override public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(this.getClass() != obj.getClass()) {
            return false;
        }
        MockHoge other = (MockHoge) obj;
        if(this.value.equals(other.value) == false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        value.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        value.readFields(in);
    }
}