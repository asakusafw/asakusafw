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
package com.asakusafw.compiler.windgate.testing.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;
/**
 * A data model class that represents pair.
 */
@DataModelKind("DMDL")@PropertyOrder({"key", "value"}) public class Pair implements DataModel<Pair>, Writable {
    private final IntOption key = new IntOption();
    private final StringOption value = new StringOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.key.setNull();
        this.value.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Pair other) {
        this.key.copyFrom(other.key);
        this.value.copyFrom(other.value);
    }
    /**
     * Returns key.
     * @return key
     * @throws NullPointerException if key is <code>null</code>
     */
    public int getKey() {
        return this.key.get();
    }
    /**
     * Sets key.
     * @param value0 the value
     */
    @SuppressWarnings("deprecation") public void setKey(int value0) {
        this.key.modify(value0);
    }
    /**
     * Returns key which may be represent <code>null</code>.
     * @return key
     */
    public IntOption getKeyOption() {
        return this.key;
    }
    /**
     * Sets key.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setKeyOption(IntOption option) {
        this.key.copyFrom(option);
    }
    /**
     * Returns value.
     * @return value
     * @throws NullPointerException if value is <code>null</code>
     */
    public Text getValue() {
        return this.value.get();
    }
    /**
     * Sets value.
     * @param value0 the value
     */
    @SuppressWarnings("deprecation") public void setValue(Text value0) {
        this.value.modify(value0);
    }
    /**
     * Returns value which may be represent <code>null</code>.
     * @return value
     */
    public StringOption getValueOption() {
        return this.value;
    }
    /**
     * Sets value.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setValueOption(StringOption option) {
        this.value.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=pair");
        result.append(", key=");
        result.append(this.key);
        result.append(", value=");
        result.append(this.value);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + key.hashCode();
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
        Pair other = (Pair) obj;
        if(this.key.equals(other.key) == false) {
            return false;
        }
        if(this.value.equals(other.value) == false) {
            return false;
        }
        return true;
    }
    /**
     * Returns value.
     * @return value
     * @throws NullPointerException if value is <code>null</code>
     */
    public String getValueAsString() {
        return this.value.getAsString();
    }
    /**
     * Returns value.
     * @param value0 the value
     */
    @SuppressWarnings("deprecation") public void setValueAsString(String value0) {
        this.value.modify(value0);
    }
    @Override public void write(DataOutput out) throws IOException {
        key.write(out);
        value.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        key.readFields(in);
        value.readFields(in);
    }
}