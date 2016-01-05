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

import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>locked_table</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "locked_table", columns = {"JOBFLOW_SID",
            "TABLE_NAME"}, primary = {"JOBFLOW_SID", "TABLE_NAME"})@SuppressWarnings("deprecation") public class
        LockedTable implements Writable {
    /**
     * カラム<code>JOBFLOW_SID</code>を表すフィールド。
     */
    @Property(name = "JOBFLOW_SID") private IntOption jobflowSid = new IntOption();
    /**
     * カラム<code>TABLE_NAME</code>を表すフィールド。
     */
    @Property(name = "TABLE_NAME") private StringOption tableName = new StringOption();
    /**
     * カラム<code>JOBFLOW_SID</code>の値を返す。
     * @return カラム<code>JOBFLOW_SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public int getJobflowSid() {
        return this.jobflowSid.get();
    }
    /**
     * カラム<code>JOBFLOW_SID</code>の値を変更する。
     * @param jobflowSid 設定する値
     */
    public void setJobflowSid(int jobflowSid) {
        this.jobflowSid.modify(jobflowSid);
    }
    /**
     * {@link #getJobflowSid()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getJobflowSid()}
     */
    public IntOption getJobflowSidOption() {
        return this.jobflowSid;
    }
    /**
     * {@link #setJobflowSid(int)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param jobflowSid 設定する値、消去する場合は{@code null}
     */
    public void setJobflowSidOption(IntOption jobflowSid) {
        this.jobflowSid.copyFrom(jobflowSid);
    }
    /**
     * カラム<code>TABLE_NAME</code>の値を返す。
     * @return カラム<code>TABLE_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTableName() {
        return this.tableName.get();
    }
    /**
     * カラム<code>TABLE_NAME</code>の値を変更する。
     * @param tableName 設定する値
     */
    public void setTableName(Text tableName) {
        this.tableName.modify(tableName);
    }
    /**
     * カラム<code>TABLE_NAME</code>の値を返す。
     * @return カラム<code>TABLE_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTableNameAsString() {
        return this.tableName.getAsString();
    }
    /**
     * カラム<code>TABLE_NAME</code>の値を変更する。
     * @param tableName 設定する値
     */
    public void setTableNameAsString(String tableName) {
        this.tableName.modify(tableName);
    }
    /**
     * {@link #getTableName()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTableName()}
     */
    public StringOption getTableNameOption() {
        return this.tableName;
    }
    /**
     * {@link #setTableName(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param tableName 設定する値、消去する場合は{@code null}
     */
    public void setTableNameOption(StringOption tableName) {
        this.tableName.copyFrom(tableName);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(LockedTable source) {
        this.jobflowSid.copyFrom(source.jobflowSid);
        this.tableName.copyFrom(source.tableName);
    }
    @Override public void write(DataOutput out) throws IOException {
        jobflowSid.write(out);
        tableName.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        jobflowSid.readFields(in);
        tableName.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + jobflowSid.hashCode();
        result = prime * result + tableName.hashCode();
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
        LockedTable other = (LockedTable) obj;
        if(this.jobflowSid.equals(other.jobflowSid)== false) {
            return false;
        }
        if(this.tableName.equals(other.tableName)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=locked_table");
        result.append(", jobflowSid=");
        result.append(this.jobflowSid);
        result.append(", tableName=");
        result.append(this.tableName);
        result.append("}");
        return result.toString();
    }
}