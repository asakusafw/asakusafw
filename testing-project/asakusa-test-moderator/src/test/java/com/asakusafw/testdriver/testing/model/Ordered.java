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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;
/**
 * A data model class that represents ordered.
 */
@DataModelKind("DMDL")@PropertyOrder({"first", "second_property", "a", "last"}) public class Ordered implements
        DataModel<Ordered>, Writable {
    private final IntOption first = new IntOption();
    private final IntOption secondProperty = new IntOption();
    private final StringOption a = new StringOption();
    private final IntOption last = new IntOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.first.setNull();
        this.secondProperty.setNull();
        this.a.setNull();
        this.last.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Ordered other) {
        this.first.copyFrom(other.first);
        this.secondProperty.copyFrom(other.secondProperty);
        this.a.copyFrom(other.a);
        this.last.copyFrom(other.last);
    }
    /**
     * Returns first.
     * @return first
     * @throws NullPointerException if first is <code>null</code>
     */
    public int getFirst() {
        return this.first.get();
    }
    /**
     * Sets first.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setFirst(int value) {
        this.first.modify(value);
    }
    /**
     * Returns first which may be represent <code>null</code>.
     * @return first
     */
    public IntOption getFirstOption() {
        return this.first;
    }
    /**
     * Sets first.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setFirstOption(IntOption option) {
        this.first.copyFrom(option);
    }
    /**
     * Returns second_property.
     * @return second_property
     * @throws NullPointerException if second_property is <code>null</code>
     */
    public int getSecondProperty() {
        return this.secondProperty.get();
    }
    /**
     * Sets second_property.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setSecondProperty(int value) {
        this.secondProperty.modify(value);
    }
    /**
     * Returns second_property which may be represent <code>null</code>.
     * @return second_property
     */
    public IntOption getSecondPropertyOption() {
        return this.secondProperty;
    }
    /**
     * Sets second_property.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setSecondPropertyOption(IntOption option) {
        this.secondProperty.copyFrom(option);
    }
    /**
     * Returns a.
     * @return a
     * @throws NullPointerException if a is <code>null</code>
     */
    public Text getA() {
        return this.a.get();
    }
    /**
     * Sets a.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setA(Text value) {
        this.a.modify(value);
    }
    /**
     * Returns a which may be represent <code>null</code>.
     * @return a
     */
    public StringOption getAOption() {
        return this.a;
    }
    /**
     * Sets a.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setAOption(StringOption option) {
        this.a.copyFrom(option);
    }
    /**
     * Returns last.
     * @return last
     * @throws NullPointerException if last is <code>null</code>
     */
    public int getLast() {
        return this.last.get();
    }
    /**
     * Sets last.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setLast(int value) {
        this.last.modify(value);
    }
    /**
     * Returns last which may be represent <code>null</code>.
     * @return last
     */
    public IntOption getLastOption() {
        return this.last;
    }
    /**
     * Sets last.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setLastOption(IntOption option) {
        this.last.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=ordered");
        result.append(", first=");
        result.append(this.first);
        result.append(", secondProperty=");
        result.append(this.secondProperty);
        result.append(", a=");
        result.append(this.a);
        result.append(", last=");
        result.append(this.last);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + first.hashCode();
        result = prime * result + secondProperty.hashCode();
        result = prime * result + a.hashCode();
        result = prime * result + last.hashCode();
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
        Ordered other = (Ordered) obj;
        if(this.first.equals(other.first) == false) {
            return false;
        }
        if(this.secondProperty.equals(other.secondProperty) == false) {
            return false;
        }
        if(this.a.equals(other.a) == false) {
            return false;
        }
        if(this.last.equals(other.last) == false) {
            return false;
        }
        return true;
    }
    /**
     * Returns a.
     * @return a
     * @throws NullPointerException if a is <code>null</code>
     */
    public String getAAsString() {
        return this.a.getAsString();
    }
    /**
     * Returns a.
     * @param a0 the value
     */
    @SuppressWarnings("deprecation") public void setAAsString(String a0) {
        this.a.modify(a0);
    }
    @Override public void write(DataOutput out) throws IOException {
        first.write(out);
        secondProperty.write(out);
        a.write(out);
        last.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        first.readFields(in);
        secondProperty.readFields(in);
        a.readFields(in);
        last.readFields(in);
    }
}