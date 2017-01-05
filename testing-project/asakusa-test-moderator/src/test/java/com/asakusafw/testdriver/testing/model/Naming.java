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
package com.asakusafw.testdriver.testing.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
/**
 * A data model class that represents naming.
 */
@DataModelKind("DMDL")@PropertyOrder({"a", "very_very_very_long_name"}) public class Naming implements DataModel<Naming>
        , Writable {
    private final IntOption a = new IntOption();
    private final LongOption veryVeryVeryLongName = new LongOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.a.setNull();
        this.veryVeryVeryLongName.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Naming other) {
        this.a.copyFrom(other.a);
        this.veryVeryVeryLongName.copyFrom(other.veryVeryVeryLongName);
    }
    /**
     * Returns a.
     * @return a
     * @throws NullPointerException if a is <code>null</code>
     */
    public int getA() {
        return this.a.get();
    }
    /**
     * Sets a.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setA(int value) {
        this.a.modify(value);
    }
    /**
     * Returns a which may be represent <code>null</code>.
     * @return a
     */
    public IntOption getAOption() {
        return this.a;
    }
    /**
     * Sets a.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setAOption(IntOption option) {
        this.a.copyFrom(option);
    }
    /**
     * Returns very_very_very_long_name.
     * @return very_very_very_long_name
     * @throws NullPointerException if very_very_very_long_name is <code>null</code>
     */
    public long getVeryVeryVeryLongName() {
        return this.veryVeryVeryLongName.get();
    }
    /**
     * Sets very_very_very_long_name.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setVeryVeryVeryLongName(long value) {
        this.veryVeryVeryLongName.modify(value);
    }
    /**
     * Returns very_very_very_long_name which may be represent <code>null</code>.
     * @return very_very_very_long_name
     */
    public LongOption getVeryVeryVeryLongNameOption() {
        return this.veryVeryVeryLongName;
    }
    /**
     * Sets very_very_very_long_name.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setVeryVeryVeryLongNameOption(LongOption option) {
        this.veryVeryVeryLongName.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=naming");
        result.append(", a=");
        result.append(this.a);
        result.append(", veryVeryVeryLongName=");
        result.append(this.veryVeryVeryLongName);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + a.hashCode();
        result = prime * result + veryVeryVeryLongName.hashCode();
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
        Naming other = (Naming) obj;
        if(this.a.equals(other.a) == false) {
            return false;
        }
        if(this.veryVeryVeryLongName.equals(other.veryVeryVeryLongName) == false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        a.write(out);
        veryVeryVeryLongName.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        a.readFields(in);
        veryVeryVeryLongName.readFields(in);
    }
}