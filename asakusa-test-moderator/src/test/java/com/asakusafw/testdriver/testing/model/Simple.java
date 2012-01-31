/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.testing.model;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.testdriver.testing.io.SimpleInput;
import com.asakusafw.testdriver.testing.io.SimpleOutput;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
/**
 * simpleを表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(SimpleInput.class)@ModelOutputLocation(SimpleOutput.class)@PropertyOrder({
            "data"}) public class Simple implements DataModel<Simple>, Projection, Writable {
    private final StringOption data = new StringOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.data.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Simple other) {
        this.data.copyFrom(other.data);
    }
    /**
     * dataを返す。
     * @return data
     * @throws NullPointerException dataの値が<code>null</code>である場合
     */
    @Override
    public Text getData() {
        return this.data.get();
    }
    /**
     * dataを設定する。
     * @param value 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setData(Text value) {
        this.data.modify(value);
    }
    /**
     * <code>null</code>を許すdataを返す。
     * @return data
     */
    @Override
    public StringOption getDataOption() {
        return this.data;
    }
    /**
     * dataを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @Override
    @SuppressWarnings("deprecation") public void setDataOption(StringOption option) {
        this.data.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=simple");
        result.append(", data=");
        result.append(this.data);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + data.hashCode();
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
        Simple other = (Simple) obj;
        if(this.data.equals(other.data)== false) {
            return false;
        }
        return true;
    }
    /**
     * dataを返す。
     * @return data
     * @throws NullPointerException dataの値が<code>null</code>である場合
     */
    @Override
    public String getDataAsString() {
        return this.data.getAsString();
    }
    /**
     * dataを設定する。
     * @param data0 設定する値
     */
    @Override
    @SuppressWarnings("deprecation") public void setDataAsString(String data0) {
        this.data.modify(data0);
    }
    @Override public void write(DataOutput out) throws IOException {
        data.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        data.readFields(in);
    }
}