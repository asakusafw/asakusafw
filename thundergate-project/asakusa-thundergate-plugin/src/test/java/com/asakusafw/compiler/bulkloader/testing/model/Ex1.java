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
package com.asakusafw.compiler.bulkloader.testing.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.bulkloader.testing.io.Ex1Input;
import com.asakusafw.compiler.bulkloader.testing.io.Ex1Output;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.bulkloader.ColumnOrder;
import com.asakusafw.vocabulary.bulkloader.OriginalName;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;
/**
 * ex1を表すデータモデルクラス。
 */
@ColumnOrder(value = {"SID", "VALUE", "STRING"})@DataModelKind("DMDL")@ModelInputLocation(Ex1Input.class)@
        ModelOutputLocation(Ex1Output.class)@OriginalName(value = "EX1")@PrimaryKey(value = {"sid"})@PropertyOrder({
            "sid", "value", "string"}) public class Ex1 implements DataModel<Ex1>, Writable {
    private final LongOption sid = new LongOption();
    private final IntOption value = new IntOption();
    private final StringOption string = new StringOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.sid.setNull();
        this.value.setNull();
        this.string.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Ex1 other) {
        this.sid.copyFrom(other.sid);
        this.value.copyFrom(other.value);
        this.string.copyFrom(other.string);
    }
    /**
     * sidを返す。
     * @return sid
     * @throws NullPointerException sidの値が<code>null</code>である場合
     */
    public long getSid() {
        return this.sid.get();
    }
    /**
     * sidを設定する。
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setSid(long value0) {
        this.sid.modify(value0);
    }
    /**
     * <code>null</code>を許すsidを返す。
     * @return sid
     */
    @OriginalName(value = "SID") public LongOption getSidOption() {
        return this.sid;
    }
    /**
     * sidを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setSidOption(LongOption option) {
        this.sid.copyFrom(option);
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
    @OriginalName(value = "VALUE") public IntOption getValueOption() {
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
     * stringを返す。
     * @return string
     * @throws NullPointerException stringの値が<code>null</code>である場合
     */
    public Text getString() {
        return this.string.get();
    }
    /**
     * stringを設定する。
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setString(Text value0) {
        this.string.modify(value0);
    }
    /**
     * <code>null</code>を許すstringを返す。
     * @return string
     */
    @OriginalName(value = "STRING") public StringOption getStringOption() {
        return this.string;
    }
    /**
     * stringを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setStringOption(StringOption option) {
        this.string.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=ex1");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", value=");
        result.append(this.value);
        result.append(", string=");
        result.append(this.string);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + value.hashCode();
        result = prime * result + string.hashCode();
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
        Ex1 other = (Ex1) obj;
        if(this.sid.equals(other.sid)== false) {
            return false;
        }
        if(this.value.equals(other.value)== false) {
            return false;
        }
        if(this.string.equals(other.string)== false) {
            return false;
        }
        return true;
    }
    /**
     * stringを返す。
     * @return string
     * @throws NullPointerException stringの値が<code>null</code>である場合
     */
    public String getStringAsString() {
        return this.string.getAsString();
    }
    /**
     * stringを設定する。
     * @param string0 設定する値
     */
    @SuppressWarnings("deprecation") public void setStringAsString(String string0) {
        this.string.modify(string0);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        value.write(out);
        string.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        value.readFields(in);
        string.readFields(in);
    }
}