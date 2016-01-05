/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.compiler.fileio.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.fileio.io.KeyConflictInput;
import com.asakusafw.compiler.fileio.io.KeyConflictOutput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.Summarized;
/**
 * key_conflictを表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(KeyConflictInput.class)@ModelOutputLocation(KeyConflictOutput.class)@
        Summarized(term = @Summarized.Term(source = Ex1.class, foldings = {@Summarized.Folding(aggregator = Summarized.
            Aggregator.ANY, source = "string", destination = "key"),@Summarized.Folding(aggregator = Summarized.
            Aggregator.COUNT, source = "string", destination = "count")}, shuffle = @Key(group = {"string"}))) public 
        class KeyConflict implements DataModel<KeyConflict>, Writable {
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
     * keyを返す。
     * @return key
     * @throws NullPointerException keyの値が<code>null</code>である場合
     */
    public Text getKey() {
        return this.key.get();
    }
    /**
     * keyを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setKey(Text value) {
        this.key.modify(value);
    }
    /**
     * <code>null</code>を許すkeyを返す。
     * @return key
     */
    public StringOption getKeyOption() {
        return this.key;
    }
    /**
     * keyを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setKeyOption(StringOption option) {
        this.key.copyFrom(option);
    }
    /**
     * countを返す。
     * @return count
     * @throws NullPointerException countの値が<code>null</code>である場合
     */
    public long getCount() {
        return this.count.get();
    }
    /**
     * countを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setCount(long value) {
        this.count.modify(value);
    }
    /**
     * <code>null</code>を許すcountを返す。
     * @return count
     */
    public LongOption getCountOption() {
        return this.count;
    }
    /**
     * countを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
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
        if(this.getClass()!= obj.getClass()) {
            return false;
        }
        KeyConflict other = (KeyConflict) obj;
        if(this.key.equals(other.key)== false) {
            return false;
        }
        if(this.count.equals(other.count)== false) {
            return false;
        }
        return true;
    }
    /**
     * keyを返す。
     * @return key
     * @throws NullPointerException keyの値が<code>null</code>である場合
     */
    public String getKeyAsString() {
        return this.key.getAsString();
    }
    /**
     * keyを設定する。
     * @param key0 設定する値
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