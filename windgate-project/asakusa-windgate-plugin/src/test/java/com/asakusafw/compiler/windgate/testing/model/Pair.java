/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.compiler.windgate.testing.io.PairInput;
import com.asakusafw.compiler.windgate.testing.io.PairOutput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;
/**
 * pairを表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(PairInput.class)@ModelOutputLocation(PairOutput.class) public class Pair 
        implements DataModel<Pair>, Writable {
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
     * keyを返す。
     * @return key
     * @throws NullPointerException keyの値が<code>null</code>である場合
     */
    public int getKey() {
        return this.key.get();
    }
    /**
     * keyを設定する。
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setKey(int value0) {
        this.key.modify(value0);
    }
    /**
     * <code>null</code>を許すkeyを返す。
     * @return key
     */
    public IntOption getKeyOption() {
        return this.key;
    }
    /**
     * keyを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setKeyOption(IntOption option) {
        this.key.copyFrom(option);
    }
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    public Text getValue() {
        return this.value.get();
    }
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setValue(Text value0) {
        this.value.modify(value0);
    }
    /**
     * <code>null</code>を許すvalueを返す。
     * @return value
     */
    public StringOption getValueOption() {
        return this.value;
    }
    /**
     * valueを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
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
        if(this.getClass()!= obj.getClass()) {
            return false;
        }
        Pair other = (Pair) obj;
        if(this.key.equals(other.key)== false) {
            return false;
        }
        if(this.value.equals(other.value)== false) {
            return false;
        }
        return true;
    }
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    public String getValueAsString() {
        return this.value.getAsString();
    }
    /**
     * valueを設定する。
     * @param value0 設定する値
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