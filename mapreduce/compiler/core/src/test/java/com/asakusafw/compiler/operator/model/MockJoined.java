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
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;
/**
 * A data model class that represents mock_joined.
 */
@DataModelKind("DMDL")@Joined(terms = {@Joined.Term(source = MockHoge.class, mappings = {@Joined.Mapping(source =
                "value", destination = "hogeValue")}, shuffle = @Key(group = {"value"})),@Joined.Term(source = MockFoo.
            class, mappings = {@Joined.Mapping(source = "value", destination = "fooValue")}, shuffle = @Key(group = {
                "value"}))})@PropertyOrder({"hoge_value", "foo_value"}) public class MockJoined implements DataModel<
        MockJoined>, Writable {
    private final IntOption hogeValue = new IntOption();
    private final IntOption fooValue = new IntOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.hogeValue.setNull();
        this.fooValue.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(MockJoined other) {
        this.hogeValue.copyFrom(other.hogeValue);
        this.fooValue.copyFrom(other.fooValue);
    }
    /**
     * Returns hoge_value.
     * @return hoge_value
     * @throws NullPointerException if hoge_value is <code>null</code>
     */
    public int getHogeValue() {
        return this.hogeValue.get();
    }
    /**
     * Sets hoge_value.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setHogeValue(int value) {
        this.hogeValue.modify(value);
    }
    /**
     * Returns hoge_value which may be represent <code>null</code>.
     * @return hoge_value
     */
    public IntOption getHogeValueOption() {
        return this.hogeValue;
    }
    /**
     * Sets hoge_value.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setHogeValueOption(IntOption option) {
        this.hogeValue.copyFrom(option);
    }
    /**
     * Returns foo_value.
     * @return foo_value
     * @throws NullPointerException if foo_value is <code>null</code>
     */
    public int getFooValue() {
        return this.fooValue.get();
    }
    /**
     * Sets foo_value.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setFooValue(int value) {
        this.fooValue.modify(value);
    }
    /**
     * Returns foo_value which may be represent <code>null</code>.
     * @return foo_value
     */
    public IntOption getFooValueOption() {
        return this.fooValue;
    }
    /**
     * Sets foo_value.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setFooValueOption(IntOption option) {
        this.fooValue.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=mock_joined");
        result.append(", hogeValue=");
        result.append(this.hogeValue);
        result.append(", fooValue=");
        result.append(this.fooValue);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + hogeValue.hashCode();
        result = prime * result + fooValue.hashCode();
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
        MockJoined other = (MockJoined) obj;
        if(this.hogeValue.equals(other.hogeValue) == false) {
            return false;
        }
        if(this.fooValue.equals(other.fooValue) == false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        hogeValue.write(out);
        fooValue.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        hogeValue.readFields(in);
        fooValue.readFields(in);
    }
}