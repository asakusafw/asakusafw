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
package com.asakusafw.compiler.flow.testing.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.Summarized;
/**
 * A data model class that represents key_conflict.
 */
@DataModelKind("DMDL")@PropertyOrder({"key", "count"})@Summarized(term = @Summarized.Term(source = Ex1.class, foldings = 
        {@Summarized.Folding(aggregator = Summarized.Aggregator.ANY, source = "string", destination = "key"),@Summarized
            .Folding(aggregator = Summarized.Aggregator.COUNT, source = "string", destination = "count")}, shuffle = @
        Key(group = {"string"}))) public class KeyConflict implements DataModel<KeyConflict>, Writable {
    private final StringOption key = new StringOption();
    private final LongOption count = new LongOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.key.setNull();
        this.count.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(KeyConflict other) {
        this.key.copyFrom(other.key);
        this.count.copyFrom(other.count);
    }
    /**
     * Returns key.
     * @return key
     * @throws NullPointerException if key is <code>null</code>
     */
    public Text getKey() {
        return this.key.get();
    }
    /**
     * Sets key.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setKey(Text value) {
        this.key.modify(value);
    }
    /**
     * Returns key which may be represent <code>null</code>.
     * @return key
     */
    public StringOption getKeyOption() {
        return this.key;
    }
    /**
     * Sets key.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setKeyOption(StringOption option) {
        this.key.copyFrom(option);
    }
    /**
     * Returns count.
     * @return count
     * @throws NullPointerException if count is <code>null</code>
     */
    public long getCount() {
        return this.count.get();
    }
    /**
     * Sets count.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setCount(long value) {
        this.count.modify(value);
    }
    /**
     * Returns count which may be represent <code>null</code>.
     * @return count
     */
    public LongOption getCountOption() {
        return this.count;
    }
    /**
     * Sets count.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setCountOption(LongOption option) {
        this.count.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=key_conflict");
        result.append(", key=");
        result.append(this.key);
        result.append(", count=");
        result.append(this.count);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + key.hashCode();
        result = prime * result + count.hashCode();
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
        KeyConflict other = (KeyConflict) obj;
        if(this.key.equals(other.key) == false) {
            return false;
        }
        if(this.count.equals(other.count) == false) {
            return false;
        }
        return true;
    }
    /**
     * Returns key.
     * @return key
     * @throws NullPointerException if key is <code>null</code>
     */
    public String getKeyAsString() {
        return this.key.getAsString();
    }
    /**
     * Returns key.
     * @param key0 the value
     */
    @SuppressWarnings("deprecation") public void setKeyAsString(String key0) {
        this.key.modify(key0);
    }
    @Override public void write(DataOutput out) throws IOException {
        key.write(out);
        count.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        key.readFields(in);
        count.readFields(in);
    }
}