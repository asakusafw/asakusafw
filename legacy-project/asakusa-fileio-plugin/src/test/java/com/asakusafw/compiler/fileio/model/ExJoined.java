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

import com.asakusafw.compiler.fileio.io.ExJoinedInput;
import com.asakusafw.compiler.fileio.io.ExJoinedOutput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;
/**
 * ex_joinedを表すデータモデルクラス。
 */
@DataModelKind("DMDL")@Joined(terms = {@Joined.Term(source = Ex1.class, mappings = {@Joined.Mapping(source = "sid", 
                destination = "sid1"),@Joined.Mapping(source = "value", destination = "value")}, shuffle = @Key(group = 
            {"value"})),@Joined.Term(source = Ex2.class, mappings = {@Joined.Mapping(source = "sid", destination = 
                "sid2"),@Joined.Mapping(source = "value", destination = "value")}, shuffle = @Key(group = {"value"}))})@
        ModelInputLocation(ExJoinedInput.class)@ModelOutputLocation(ExJoinedOutput.class) public class ExJoined 
        implements DataModel<ExJoined>, Writable {
    private final LongOption sid1 = new LongOption();
    private final IntOption value = new IntOption();
    private final LongOption sid2 = new LongOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.sid1.setNull();
        this.value.setNull();
        this.sid2.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(ExJoined other) {
        this.sid1.copyFrom(other.sid1);
        this.value.copyFrom(other.value);
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
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setSid1(long value0) {
        this.sid1.modify(value0);
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
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    public int getValue() {
        return this.value.get();
    }
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setValue(int value0) {
        this.value.modify(value0);
    }
    /**
     * <code>null</code>を許すvalueを返す。
     * @return value
     */
    public IntOption getValueOption() {
        return this.value;
    }
    /**
     * valueを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setValueOption(IntOption option) {
        this.value.copyFrom(option);
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
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setSid2(long value0) {
        this.sid2.modify(value0);
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
        result.append("class=ex_joined");
        result.append(", sid1=");
        result.append(this.sid1);
        result.append(", value=");
        result.append(this.value);
        result.append(", sid2=");
        result.append(this.sid2);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid1.hashCode();
        result = prime * result + value.hashCode();
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
        ExJoined other = (ExJoined) obj;
        if(this.sid1.equals(other.sid1)== false) {
            return false;
        }
        if(this.value.equals(other.value)== false) {
            return false;
        }
        if(this.sid2.equals(other.sid2)== false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        sid1.write(out);
        value.write(out);
        sid2.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid1.readFields(in);
        value.readFields(in);
        sid2.readFields(in);
    }
}