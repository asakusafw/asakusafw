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
package com.asakusafw.compiler.directio.hive.testing;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;

@DataModelKind("DMDL")
@SuppressWarnings({ "deprecation", "javadoc" })
public final class MockDataModel implements DataModel<MockDataModel>, Writable {

    private final IntOption key = new IntOption();

    private final DecimalOption sort = new DecimalOption();

    private final StringOption value = new StringOption();

    public MockDataModel() {
        return;
    }

    public MockDataModel(MockDataModel data) {
        copyFrom(data);
    }

    public MockDataModel(String value) {
        this.value.modify(value);
    }

    public MockDataModel(int key, String value) {
        this.key.modify(key);
        this.value.modify(value);
    }

    public MockDataModel(int key, BigDecimal sort, String value) {
        this.key.modify(key);
        this.sort.modify(sort);
        this.value.modify(value);
    }

    public int getKey() {
        return key.get();
    }

    public BigDecimal getSort() {
        return sort.get();
    }

    public String getValue() {
        return value.getAsString();
    }

    public void setKey(int v) {
        key.modify(v);
    }

    public void setSort(BigDecimal v) {
        sort.modify(v);
    }

    public void setValue(String v) {
        value.modify(v);
    }

    public IntOption getKeyOption() {
        return key;
    }

    public DecimalOption getSortOption() {
        return sort;
    }

    public StringOption getValueOption() {
        return value;
    }

    @Override
    public void copyFrom(MockDataModel other) {
        key.copyFrom(other.key);
        sort.copyFrom(other.sort);
        value.copyFrom(other.value);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        key.write(out);
        sort.write(out);
        value.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        key.readFields(in);
        sort.readFields(in);
        value.readFields(in);
    }

    @Override
    public void reset() {
        key.setNull();
        sort.setNull();
        value.setNull();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + key.hashCode();
        result = prime * result + sort.hashCode();
        result = prime * result + value.hashCode();
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
        MockDataModel other = (MockDataModel) obj;
        if (!key.equals(other.key)) {
            return false;
        }
        if (!sort.equals(other.sort)) {
            return false;
        }
        if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("{key=%s, sort=%s, value=%s}", key, sort, value);
    }
}
