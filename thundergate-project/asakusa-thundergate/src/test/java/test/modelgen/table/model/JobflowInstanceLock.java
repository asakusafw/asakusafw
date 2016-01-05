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
package test.modelgen.table.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.annotation.Generated;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>jobflow_instance_lock</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "jobflow_instance_lock", columns = {
            "EXECUTION_ID"}, primary = {"EXECUTION_ID"})@SuppressWarnings("deprecation") public class
        JobflowInstanceLock implements Writable {
    /**
     * カラム<code>EXECUTION_ID</code>を表すフィールド。
     */
    @Property(name = "EXECUTION_ID") private StringOption executionId = new StringOption();
    /**
     * カラム<code>EXECUTION_ID</code>の値を返す。
     * @return カラム<code>EXECUTION_ID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getExecutionId() {
        return this.executionId.get();
    }
    /**
     * カラム<code>EXECUTION_ID</code>の値を変更する。
     * @param executionId 設定する値
     */
    public void setExecutionId(Text executionId) {
        this.executionId.modify(executionId);
    }
    /**
     * カラム<code>EXECUTION_ID</code>の値を返す。
     * @return カラム<code>EXECUTION_ID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getExecutionIdAsString() {
        return this.executionId.getAsString();
    }
    /**
     * カラム<code>EXECUTION_ID</code>の値を変更する。
     * @param executionId 設定する値
     */
    public void setExecutionIdAsString(String executionId) {
        this.executionId.modify(executionId);
    }
    /**
     * {@link #getExecutionId()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getExecutionId()}
     */
    public StringOption getExecutionIdOption() {
        return this.executionId;
    }
    /**
     * {@link #setExecutionId(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param executionId 設定する値、消去する場合は{@code null}
     */
    public void setExecutionIdOption(StringOption executionId) {
        this.executionId.copyFrom(executionId);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(JobflowInstanceLock source) {
        this.executionId.copyFrom(source.executionId);
    }
    @Override public void write(DataOutput out) throws IOException {
        executionId.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        executionId.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + executionId.hashCode();
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
        JobflowInstanceLock other = (JobflowInstanceLock) obj;
        if(this.executionId.equals(other.executionId)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=jobflow_instance_lock");
        result.append(", executionId=");
        result.append(this.executionId);
        result.append("}");
        return result.toString();
    }
}