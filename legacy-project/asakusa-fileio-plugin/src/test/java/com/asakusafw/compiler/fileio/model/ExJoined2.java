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

import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.fileio.io.ExJoined2Input;
import com.asakusafw.compiler.fileio.io.ExJoined2Output;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;
/**
 * ex_joined2を表すデータモデルクラス。
 */
@DataModelKind("DMDL")@Joined(terms = {@Joined.Term(source = Ex1.class, mappings = {@Joined.Mapping(source = "sid", 
                destination = "sid1"),@Joined.Mapping(source = "value", destination = "key")}, shuffle = @Key(group = {
                "value"})),@Joined.Term(source = Ex2.class, mappings = {@Joined.Mapping(source = "sid", destination = 
                "sid2"),@Joined.Mapping(source = "value", destination = "key")}, shuffle = @Key(group = {"value"}))})@
        ModelInputLocation(ExJoined2Input.class)@ModelOutputLocation(ExJoined2Output.class) public class ExJoined2 
        implements DataModel<ExJoined2>, Writable {
    private final LongOption sid1 = new LongOption();
    private final IntOption key = new IntOption();
    private final LongOption sid2 = new LongOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.sid1.setNull();
        this.key.setNull();
        this.sid2.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(ExJoined2 other) {
        this.sid1.copyFrom(other.sid1);
        this.key.copyFrom(other.key);
        this.sid2.copyFrom(other.sid2);
    }
    /**
     * sid1を返す。
     * @return sid1
     * @throws NullPointerException sid1の値が<code>null</code>である場合
     */
    public long getSid1() {
        return this.sid1.get();
    }
    /**
     * sid1を設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setSid1(long value) {
        this.sid1.modify(value);
    }
    /**
     * <code>null</code>を許すsid1を返す。
     * @return sid1
     */
    public LongOption getSid1Option() {
        return this.sid1;
    }
    /**
     * sid1を設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setSid1Option(LongOption option) {
        this.sid1.copyFrom(option);
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
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setKey(int value) {
        this.key.modify(value);
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
     * sid2を返す。
     * @return sid2
     * @throws NullPointerException sid2の値が<code>null</code>である場合
     */
    public long getSid2() {
        return this.sid2.get();
    }
    /**
     * sid2を設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setSid2(long value) {
        this.sid2.modify(value);
    }
    /**
     * <code>null</code>を許すsid2を返す。
     * @return sid2
     */
    public LongOption getSid2Option() {
        return this.sid2;
    }
    /**
     * sid2を設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setSid2Option(LongOption option) {
        this.sid2.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=ex_joined2");
        result.append(", sid1=");
        result.append(this.sid1);
        result.append(", key=");
        result.append(this.key);
        result.append(", sid2=");
        result.append(this.sid2);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid1.hashCode();
        result = prime * result + key.hashCode();
        result = prime * result + sid2.hashCode();
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
        ExJoined2 other = (ExJoined2) obj;
        if(this.sid1.equals(other.sid1)== false) {
            return false;
        }
        if(this.key.equals(other.key)== false) {
            return false;
        }
        if(this.sid2.equals(other.sid2)== false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        sid1.write(out);
        key.write(out);
        sid2.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid1.readFields(in);
        key.readFields(in);
        sid2.readFields(in);
    }
}