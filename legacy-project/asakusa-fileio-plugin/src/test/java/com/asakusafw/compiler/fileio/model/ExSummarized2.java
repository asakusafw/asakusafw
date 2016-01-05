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

import com.asakusafw.compiler.fileio.io.ExSummarized2Input;
import com.asakusafw.compiler.fileio.io.ExSummarized2Output;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.Summarized;
/**
 * ex_summarized2を表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(ExSummarized2Input.class)@ModelOutputLocation(ExSummarized2Output.class)@
        Summarized(term = @Summarized.Term(source = Ex1.class, foldings = {@Summarized.Folding(aggregator = Summarized.
            Aggregator.ANY, source = "string", destination = "key"),@Summarized.Folding(aggregator = Summarized.
            Aggregator.SUM, source = "value", destination = "value"),@Summarized.Folding(aggregator = Summarized.
            Aggregator.COUNT, source = "sid", destination = "count")}, shuffle = @Key(group = {"string"}))) public class 
        ExSummarized2 implements DataModel<ExSummarized2>, Writable {
    private final StringOption key = new StringOption();
    private final LongOption value = new LongOption();
    private final LongOption count = new LongOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.key.setNull();
        this.value.setNull();
        this.count.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(ExSummarized2 other) {
        this.key.copyFrom(other.key);
        this.value.copyFrom(other.value);
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
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setKey(Text value0) {
        this.key.modify(value0);
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
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    public long getValue() {
        return this.value.get();
    }
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setValue(long value0) {
        this.value.modify(value0);
    }
    /**
     * <code>null</code>を許すvalueを返す。
     * @return value
     */
    public LongOption getValueOption() {
        return this.value;
    }
    /**
     * valueを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setValueOption(LongOption option) {
        this.value.copyFrom(option);
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
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setCount(long value0) {
        this.count.modify(value0);
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
        result.append("class=ex_summarized2");
        result.append(", key=");
        result.append(this.key);
        result.append(", value=");
        result.append(this.value);
        result.append(", count=");
        result.append(this.count);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + key.hashCode();
        result = prime * result + value.hashCode();
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
        ExSummarized2 other = (ExSummarized2) obj;
        if(this.key.equals(other.key)== false) {
            return false;
        }
        if(this.value.equals(other.value)== false) {
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
        value.write(out);
        count.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        key.readFields(in);
        value.readFields(in);
        count.readFields(in);
    }
}