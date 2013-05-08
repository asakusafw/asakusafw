/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.compiler.directio.testing.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.directio.testing.io.Line1Input;
import com.asakusafw.compiler.directio.testing.io.Line1Output;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
/**
 * line1を表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(Line1Input.class)@ModelOutputLocation(Line1Output.class)@PropertyOrder({
            "value", "first", "position", "length"}) public class Line1 implements DataModel<Line1>, Line, Writable {
    private final StringOption value = new StringOption();
    private final StringOption first = new StringOption();
    private final LongOption position = new LongOption();
    private final IntOption length = new IntOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.value.setNull();
        this.first.setNull();
        this.position.setNull();
        this.length.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Line1 other) {
        this.value.copyFrom(other.value);
        this.first.copyFrom(other.first);
        this.position.copyFrom(other.position);
        this.length.copyFrom(other.length);
    }
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    @Override
    public Text getValue() {
        return this.value.get();
    }
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setValue(Text value0) {
        this.value.modify(value0);
    }
    /**
     * <code>null</code>を許すvalueを返す。
     * @return value
     */
    @Override
    public StringOption getValueOption() {
        return this.value;
    }
    /**
     * valueを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @Override
    @SuppressWarnings("deprecation") public void setValueOption(StringOption option) {
        this.value.copyFrom(option);
    }
    /**
     * firstを返す。
     * @return first
     * @throws NullPointerException firstの値が<code>null</code>である場合
     */
    @Override
    public Text getFirst() {
        return this.first.get();
    }
    /**
     * firstを設定する。
     * @param value0 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setFirst(Text value0) {
        this.first.modify(value0);
    }
    /**
     * <code>null</code>を許すfirstを返す。
     * @return first
     */
    @Override
    public StringOption getFirstOption() {
        return this.first;
    }
    /**
     * firstを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @Override
    @SuppressWarnings("deprecation") public void setFirstOption(StringOption option) {
        this.first.copyFrom(option);
    }
    /**
     * positionを返す。
     * @return position
     * @throws NullPointerException positionの値が<code>null</code>である場合
     */
    @Override
    public long getPosition() {
        return this.position.get();
    }
    /**
     * positionを設定する。
     * @param value0 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setPosition(long value0) {
        this.position.modify(value0);
    }
    /**
     * <code>null</code>を許すpositionを返す。
     * @return position
     */
    @Override
    public LongOption getPositionOption() {
        return this.position;
    }
    /**
     * positionを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @Override
    @SuppressWarnings("deprecation") public void setPositionOption(LongOption option) {
        this.position.copyFrom(option);
    }
    /**
     * lengthを返す。
     * @return length
     * @throws NullPointerException lengthの値が<code>null</code>である場合
     */
    @Override
    public int getLength() {
        return this.length.get();
    }
    /**
     * lengthを設定する。
     * @param value0 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setLength(int value0) {
        this.length.modify(value0);
    }
    /**
     * <code>null</code>を許すlengthを返す。
     * @return length
     */
    @Override
    public IntOption getLengthOption() {
        return this.length;
    }
    /**
     * lengthを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @Override
    @SuppressWarnings("deprecation") public void setLengthOption(IntOption option) {
        this.length.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=line1");
        result.append(", value=");
        result.append(this.value);
        result.append(", first=");
        result.append(this.first);
        result.append(", position=");
        result.append(this.position);
        result.append(", length=");
        result.append(this.length);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
        result = prime * result + first.hashCode();
        result = prime * result + position.hashCode();
        result = prime * result + length.hashCode();
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
        Line1 other = (Line1) obj;
        if(this.value.equals(other.value)== false) {
            return false;
        }
        if(this.first.equals(other.first)== false) {
            return false;
        }
        if(this.position.equals(other.position)== false) {
            return false;
        }
        if(this.length.equals(other.length)== false) {
            return false;
        }
        return true;
    }
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    @Override
    public String getValueAsString() {
        return this.value.getAsString();
    }
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setValueAsString(String value0) {
        this.value.modify(value0);
    }
    /**
     * firstを返す。
     * @return first
     * @throws NullPointerException firstの値が<code>null</code>である場合
     */
    @Override
    public String getFirstAsString() {
        return this.first.getAsString();
    }
    /**
     * firstを設定する。
     * @param first0 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setFirstAsString(String first0) {
        this.first.modify(first0);
    }
    @Override public void write(DataOutput out) throws IOException {
        value.write(out);
        first.write(out);
        position.write(out);
        length.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        value.readFields(in);
        first.readFields(in);
        position.readFields(in);
        length.readFields(in);
    }
}