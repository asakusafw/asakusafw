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

import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.bulkloader.testing.io.MockErrorModelInput;
import com.asakusafw.compiler.bulkloader.testing.io.MockErrorModelOutput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.vocabulary.bulkloader.ColumnOrder;
import com.asakusafw.vocabulary.bulkloader.OriginalName;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;
/**
 * mock_error_modelを表すデータモデルクラス。
 */
@ColumnOrder(value = {"A", "B", "C", "D", "E"})@DataModelKind("DMDL")@ModelInputLocation(MockErrorModelInput.class)@
        ModelOutputLocation(MockErrorModelOutput.class)@OriginalName(value = "MOCK_ERROR")@PrimaryKey(value = {"a"})@
        PropertyOrder({"a", "b", "c", "d", "e"}) public class MockErrorModel implements DataModel<MockErrorModel>, 
        Writable {
    private final IntOption a = new IntOption();
    private final IntOption b = new IntOption();
    private final IntOption c = new IntOption();
    private final IntOption d = new IntOption();
    private final IntOption e = new IntOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.a.setNull();
        this.b.setNull();
        this.c.setNull();
        this.d.setNull();
        this.e.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(MockErrorModel other) {
        this.a.copyFrom(other.a);
        this.b.copyFrom(other.b);
        this.c.copyFrom(other.c);
        this.d.copyFrom(other.d);
        this.e.copyFrom(other.e);
    }
    /**
     * aを返す。
     * @return a
     * @throws NullPointerException aの値が<code>null</code>である場合
     */
    public int getA() {
        return this.a.get();
    }
    /**
     * aを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setA(int value) {
        this.a.modify(value);
    }
    /**
     * <code>null</code>を許すaを返す。
     * @return a
     */
    @OriginalName(value = "A") public IntOption getAOption() {
        return this.a;
    }
    /**
     * aを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setAOption(IntOption option) {
        this.a.copyFrom(option);
    }
    /**
     * bを返す。
     * @return b
     * @throws NullPointerException bの値が<code>null</code>である場合
     */
    public int getB() {
        return this.b.get();
    }
    /**
     * bを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setB(int value) {
        this.b.modify(value);
    }
    /**
     * <code>null</code>を許すbを返す。
     * @return b
     */
    @OriginalName(value = "B") public IntOption getBOption() {
        return this.b;
    }
    /**
     * bを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setBOption(IntOption option) {
        this.b.copyFrom(option);
    }
    /**
     * cを返す。
     * @return c
     * @throws NullPointerException cの値が<code>null</code>である場合
     */
    public int getC() {
        return this.c.get();
    }
    /**
     * cを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setC(int value) {
        this.c.modify(value);
    }
    /**
     * <code>null</code>を許すcを返す。
     * @return c
     */
    @OriginalName(value = "C") public IntOption getCOption() {
        return this.c;
    }
    /**
     * cを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setCOption(IntOption option) {
        this.c.copyFrom(option);
    }
    /**
     * dを返す。
     * @return d
     * @throws NullPointerException dの値が<code>null</code>である場合
     */
    public int getD() {
        return this.d.get();
    }
    /**
     * dを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setD(int value) {
        this.d.modify(value);
    }
    /**
     * <code>null</code>を許すdを返す。
     * @return d
     */
    @OriginalName(value = "D") public IntOption getDOption() {
        return this.d;
    }
    /**
     * dを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setDOption(IntOption option) {
        this.d.copyFrom(option);
    }
    /**
     * eを返す。
     * @return e
     * @throws NullPointerException eの値が<code>null</code>である場合
     */
    public int getE() {
        return this.e.get();
    }
    /**
     * eを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setE(int value) {
        this.e.modify(value);
    }
    /**
     * <code>null</code>を許すeを返す。
     * @return e
     */
    @OriginalName(value = "E") public IntOption getEOption() {
        return this.e;
    }
    /**
     * eを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setEOption(IntOption option) {
        this.e.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=mock_error_model");
        result.append(", a=");
        result.append(this.a);
        result.append(", b=");
        result.append(this.b);
        result.append(", c=");
        result.append(this.c);
        result.append(", d=");
        result.append(this.d);
        result.append(", e=");
        result.append(this.e);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + a.hashCode();
        result = prime * result + b.hashCode();
        result = prime * result + c.hashCode();
        result = prime * result + d.hashCode();
        result = prime * result + e.hashCode();
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
        MockErrorModel other = (MockErrorModel) obj;
        if(this.a.equals(other.a)== false) {
            return false;
        }
        if(this.b.equals(other.b)== false) {
            return false;
        }
        if(this.c.equals(other.c)== false) {
            return false;
        }
        if(this.d.equals(other.d)== false) {
            return false;
        }
        if(this.e.equals(other.e)== false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        a.write(out);
        b.write(out);
        c.write(out);
        d.write(out);
        e.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        a.readFields(in);
        b.readFields(in);
        c.readFields(in);
        d.readFields(in);
        e.readFields(in);
    }
}