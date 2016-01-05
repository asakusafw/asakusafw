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

import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>export_temp_table</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "export_temp_table", columns = {"JOBFLOW_SID",
            "TABLE_NAME", "EXPORT_TEMP_SEQ", "EXPORT_TEMP_NAME", "DUPLICATE_FLG_NAME", "TEMP_TABLE_STATUS"}, primary = {
            "JOBFLOW_SID", "TABLE_NAME"})@SuppressWarnings("deprecation") public class ExportTempTable implements
        Writable {
    /**
     * カラム<code>JOBFLOW_SID</code>を表すフィールド。
     */
    @Property(name = "JOBFLOW_SID") private LongOption jobflowSid = new LongOption();
    /**
     * カラム<code>TABLE_NAME</code>を表すフィールド。
     */
    @Property(name = "TABLE_NAME") private StringOption tableName = new StringOption();
    /**
     * カラム<code>EXPORT_TEMP_SEQ</code>を表すフィールド。
     */
    @Property(name = "EXPORT_TEMP_SEQ") private LongOption exportTempSeq = new LongOption();
    /**
     * カラム<code>EXPORT_TEMP_NAME</code>を表すフィールド。
     */
    @Property(name = "EXPORT_TEMP_NAME") private StringOption exportTempName = new StringOption();
    /**
     * カラム<code>DUPLICATE_FLG_NAME</code>を表すフィールド。
     */
    @Property(name = "DUPLICATE_FLG_NAME") private StringOption duplicateFlgName = new StringOption();
    /**
     * カラム<code>TEMP_TABLE_STATUS</code>を表すフィールド。
     */
    @Property(name = "TEMP_TABLE_STATUS") private StringOption tempTableStatus = new StringOption();
    /**
     * カラム<code>JOBFLOW_SID</code>の値を返す。
     * @return カラム<code>JOBFLOW_SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getJobflowSid() {
        return this.jobflowSid.get();
    }
    /**
     * カラム<code>JOBFLOW_SID</code>の値を変更する。
     * @param jobflowSid 設定する値
     */
    public void setJobflowSid(long jobflowSid) {
        this.jobflowSid.modify(jobflowSid);
    }
    /**
     * {@link #getJobflowSid()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getJobflowSid()}
     */
    public LongOption getJobflowSidOption() {
        return this.jobflowSid;
    }
    /**
     * {@link #setJobflowSid(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param jobflowSid 設定する値、消去する場合は{@code null}
     */
    public void setJobflowSidOption(LongOption jobflowSid) {
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
     * カラム<code>EXPORT_TEMP_SEQ</code>の値を返す。
     * @return カラム<code>EXPORT_TEMP_SEQ</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getExportTempSeq() {
        return this.exportTempSeq.get();
    }
    /**
     * カラム<code>EXPORT_TEMP_SEQ</code>の値を変更する。
     * @param exportTempSeq 設定する値
     */
    public void setExportTempSeq(long exportTempSeq) {
        this.exportTempSeq.modify(exportTempSeq);
    }
    /**
     * {@link #getExportTempSeq()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getExportTempSeq()}
     */
    public LongOption getExportTempSeqOption() {
        return this.exportTempSeq;
    }
    /**
     * {@link #setExportTempSeq(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param exportTempSeq 設定する値、消去する場合は{@code null}
     */
    public void setExportTempSeqOption(LongOption exportTempSeq) {
        this.exportTempSeq.copyFrom(exportTempSeq);
    }
    /**
     * カラム<code>EXPORT_TEMP_NAME</code>の値を返す。
     * @return カラム<code>EXPORT_TEMP_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getExportTempName() {
        return this.exportTempName.get();
    }
    /**
     * カラム<code>EXPORT_TEMP_NAME</code>の値を変更する。
     * @param exportTempName 設定する値
     */
    public void setExportTempName(Text exportTempName) {
        this.exportTempName.modify(exportTempName);
    }
    /**
     * カラム<code>EXPORT_TEMP_NAME</code>の値を返す。
     * @return カラム<code>EXPORT_TEMP_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getExportTempNameAsString() {
        return this.exportTempName.getAsString();
    }
    /**
     * カラム<code>EXPORT_TEMP_NAME</code>の値を変更する。
     * @param exportTempName 設定する値
     */
    public void setExportTempNameAsString(String exportTempName) {
        this.exportTempName.modify(exportTempName);
    }
    /**
     * {@link #getExportTempName()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getExportTempName()}
     */
    public StringOption getExportTempNameOption() {
        return this.exportTempName;
    }
    /**
     * {@link #setExportTempName(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param exportTempName 設定する値、消去する場合は{@code null}
     */
    public void setExportTempNameOption(StringOption exportTempName) {
        this.exportTempName.copyFrom(exportTempName);
    }
    /**
     * カラム<code>DUPLICATE_FLG_NAME</code>の値を返す。
     * @return カラム<code>DUPLICATE_FLG_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getDuplicateFlgName() {
        return this.duplicateFlgName.get();
    }
    /**
     * カラム<code>DUPLICATE_FLG_NAME</code>の値を変更する。
     * @param duplicateFlgName 設定する値
     */
    public void setDuplicateFlgName(Text duplicateFlgName) {
        this.duplicateFlgName.modify(duplicateFlgName);
    }
    /**
     * カラム<code>DUPLICATE_FLG_NAME</code>の値を返す。
     * @return カラム<code>DUPLICATE_FLG_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getDuplicateFlgNameAsString() {
        return this.duplicateFlgName.getAsString();
    }
    /**
     * カラム<code>DUPLICATE_FLG_NAME</code>の値を変更する。
     * @param duplicateFlgName 設定する値
     */
    public void setDuplicateFlgNameAsString(String duplicateFlgName) {
        this.duplicateFlgName.modify(duplicateFlgName);
    }
    /**
     * {@link #getDuplicateFlgName()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDuplicateFlgName()}
     */
    public StringOption getDuplicateFlgNameOption() {
        return this.duplicateFlgName;
    }
    /**
     * {@link #setDuplicateFlgName(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param duplicateFlgName 設定する値、消去する場合は{@code null}
     */
    public void setDuplicateFlgNameOption(StringOption duplicateFlgName) {
        this.duplicateFlgName.copyFrom(duplicateFlgName);
    }
    /**
     * カラム<code>TEMP_TABLE_STATUS</code>の値を返す。
     * @return カラム<code>TEMP_TABLE_STATUS</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTempTableStatus() {
        return this.tempTableStatus.get();
    }
    /**
     * カラム<code>TEMP_TABLE_STATUS</code>の値を変更する。
     * @param tempTableStatus 設定する値
     */
    public void setTempTableStatus(Text tempTableStatus) {
        this.tempTableStatus.modify(tempTableStatus);
    }
    /**
     * カラム<code>TEMP_TABLE_STATUS</code>の値を返す。
     * @return カラム<code>TEMP_TABLE_STATUS</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTempTableStatusAsString() {
        return this.tempTableStatus.getAsString();
    }
    /**
     * カラム<code>TEMP_TABLE_STATUS</code>の値を変更する。
     * @param tempTableStatus 設定する値
     */
    public void setTempTableStatusAsString(String tempTableStatus) {
        this.tempTableStatus.modify(tempTableStatus);
    }
    /**
     * {@link #getTempTableStatus()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTempTableStatus()}
     */
    public StringOption getTempTableStatusOption() {
        return this.tempTableStatus;
    }
    /**
     * {@link #setTempTableStatus(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param tempTableStatus 設定する値、消去する場合は{@code null}
     */
    public void setTempTableStatusOption(StringOption tempTableStatus) {
        this.tempTableStatus.copyFrom(tempTableStatus);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(ExportTempTable source) {
        this.jobflowSid.copyFrom(source.jobflowSid);
        this.tableName.copyFrom(source.tableName);
        this.exportTempSeq.copyFrom(source.exportTempSeq);
        this.exportTempName.copyFrom(source.exportTempName);
        this.duplicateFlgName.copyFrom(source.duplicateFlgName);
        this.tempTableStatus.copyFrom(source.tempTableStatus);
    }
    @Override public void write(DataOutput out) throws IOException {
        jobflowSid.write(out);
        tableName.write(out);
        exportTempSeq.write(out);
        exportTempName.write(out);
        duplicateFlgName.write(out);
        tempTableStatus.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        jobflowSid.readFields(in);
        tableName.readFields(in);
        exportTempSeq.readFields(in);
        exportTempName.readFields(in);
        duplicateFlgName.readFields(in);
        tempTableStatus.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + jobflowSid.hashCode();
        result = prime * result + tableName.hashCode();
        result = prime * result + exportTempSeq.hashCode();
        result = prime * result + exportTempName.hashCode();
        result = prime * result + duplicateFlgName.hashCode();
        result = prime * result + tempTableStatus.hashCode();
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
        ExportTempTable other = (ExportTempTable) obj;
        if(this.jobflowSid.equals(other.jobflowSid)== false) {
            return false;
        }
        if(this.tableName.equals(other.tableName)== false) {
            return false;
        }
        if(this.exportTempSeq.equals(other.exportTempSeq)== false) {
            return false;
        }
        if(this.exportTempName.equals(other.exportTempName)== false) {
            return false;
        }
        if(this.duplicateFlgName.equals(other.duplicateFlgName)== false) {
            return false;
        }
        if(this.tempTableStatus.equals(other.tempTableStatus)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=export_temp_table");
        result.append(", jobflowSid=");
        result.append(this.jobflowSid);
        result.append(", tableName=");
        result.append(this.tableName);
        result.append(", exportTempSeq=");
        result.append(this.exportTempSeq);
        result.append(", exportTempName=");
        result.append(this.exportTempName);
        result.append(", duplicateFlgName=");
        result.append(this.duplicateFlgName);
        result.append(", tempTableStatus=");
        result.append(this.tempTableStatus);
        result.append("}");
        return result.toString();
    }
}