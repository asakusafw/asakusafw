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

import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>running_jobflows</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "running_jobflows", columns = {"JOBFLOW_SID",
            "BATCH_ID", "JOBFLOW_ID", "TARGET_NAME", "EXECUTION_ID", "EXPECTED_COMPLETION_DATETIME"}, primary = {
            "JOBFLOW_SID"})@SuppressWarnings("deprecation") public class RunningJobflows implements Writable {
    /**
     * カラム<code>JOBFLOW_SID</code>を表すフィールド。
     */
    @Property(name = "JOBFLOW_SID") private LongOption jobflowSid = new LongOption();
    /**
     * カラム<code>BATCH_ID</code>を表すフィールド。
     */
    @Property(name = "BATCH_ID") private StringOption batchId = new StringOption();
    /**
     * カラム<code>JOBFLOW_ID</code>を表すフィールド。
     */
    @Property(name = "JOBFLOW_ID") private StringOption jobflowId = new StringOption();
    /**
     * カラム<code>TARGET_NAME</code>を表すフィールド。
     */
    @Property(name = "TARGET_NAME") private StringOption targetName = new StringOption();
    /**
     * カラム<code>EXECUTION_ID</code>を表すフィールド。
     */
    @Property(name = "EXECUTION_ID") private StringOption executionId = new StringOption();
    /**
     * カラム<code>EXPECTED_COMPLETION_DATETIME</code>を表すフィールド。
     */
    @Property(name = "EXPECTED_COMPLETION_DATETIME") private DateTimeOption expectedCompletionDatetime = new
            DateTimeOption();
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
     * カラム<code>BATCH_ID</code>の値を返す。
     * @return カラム<code>BATCH_ID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getBatchId() {
        return this.batchId.get();
    }
    /**
     * カラム<code>BATCH_ID</code>の値を変更する。
     * @param batchId 設定する値
     */
    public void setBatchId(Text batchId) {
        this.batchId.modify(batchId);
    }
    /**
     * カラム<code>BATCH_ID</code>の値を返す。
     * @return カラム<code>BATCH_ID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getBatchIdAsString() {
        return this.batchId.getAsString();
    }
    /**
     * カラム<code>BATCH_ID</code>の値を変更する。
     * @param batchId 設定する値
     */
    public void setBatchIdAsString(String batchId) {
        this.batchId.modify(batchId);
    }
    /**
     * {@link #getBatchId()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getBatchId()}
     */
    public StringOption getBatchIdOption() {
        return this.batchId;
    }
    /**
     * {@link #setBatchId(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param batchId 設定する値、消去する場合は{@code null}
     */
    public void setBatchIdOption(StringOption batchId) {
        this.batchId.copyFrom(batchId);
    }
    /**
     * カラム<code>JOBFLOW_ID</code>の値を返す。
     * @return カラム<code>JOBFLOW_ID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getJobflowId() {
        return this.jobflowId.get();
    }
    /**
     * カラム<code>JOBFLOW_ID</code>の値を変更する。
     * @param jobflowId 設定する値
     */
    public void setJobflowId(Text jobflowId) {
        this.jobflowId.modify(jobflowId);
    }
    /**
     * カラム<code>JOBFLOW_ID</code>の値を返す。
     * @return カラム<code>JOBFLOW_ID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getJobflowIdAsString() {
        return this.jobflowId.getAsString();
    }
    /**
     * カラム<code>JOBFLOW_ID</code>の値を変更する。
     * @param jobflowId 設定する値
     */
    public void setJobflowIdAsString(String jobflowId) {
        this.jobflowId.modify(jobflowId);
    }
    /**
     * {@link #getJobflowId()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getJobflowId()}
     */
    public StringOption getJobflowIdOption() {
        return this.jobflowId;
    }
    /**
     * {@link #setJobflowId(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param jobflowId 設定する値、消去する場合は{@code null}
     */
    public void setJobflowIdOption(StringOption jobflowId) {
        this.jobflowId.copyFrom(jobflowId);
    }
    /**
     * カラム<code>TARGET_NAME</code>の値を返す。
     * @return カラム<code>TARGET_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTargetName() {
        return this.targetName.get();
    }
    /**
     * カラム<code>TARGET_NAME</code>の値を変更する。
     * @param targetName 設定する値
     */
    public void setTargetName(Text targetName) {
        this.targetName.modify(targetName);
    }
    /**
     * カラム<code>TARGET_NAME</code>の値を返す。
     * @return カラム<code>TARGET_NAME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTargetNameAsString() {
        return this.targetName.getAsString();
    }
    /**
     * カラム<code>TARGET_NAME</code>の値を変更する。
     * @param targetName 設定する値
     */
    public void setTargetNameAsString(String targetName) {
        this.targetName.modify(targetName);
    }
    /**
     * {@link #getTargetName()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTargetName()}
     */
    public StringOption getTargetNameOption() {
        return this.targetName;
    }
    /**
     * {@link #setTargetName(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param targetName 設定する値、消去する場合は{@code null}
     */
    public void setTargetNameOption(StringOption targetName) {
        this.targetName.copyFrom(targetName);
    }
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
     * カラム<code>EXPECTED_COMPLETION_DATETIME</code>の値を返す。
     * @return カラム<code>EXPECTED_COMPLETION_DATETIME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getExpectedCompletionDatetime() {
        return this.expectedCompletionDatetime.get();
    }
    /**
     * カラム<code>EXPECTED_COMPLETION_DATETIME</code>の値を変更する。
     * @param expectedCompletionDatetime 設定する値
     */
    public void setExpectedCompletionDatetime(DateTime expectedCompletionDatetime) {
        this.expectedCompletionDatetime.modify(expectedCompletionDatetime);
    }
    /**
     * {@link #getExpectedCompletionDatetime()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getExpectedCompletionDatetime()}
     */
    public DateTimeOption getExpectedCompletionDatetimeOption() {
        return this.expectedCompletionDatetime;
    }
    /**
     * {@link #setExpectedCompletionDatetime(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param expectedCompletionDatetime 設定する値、消去する場合は{@code null}
     */
    public void setExpectedCompletionDatetimeOption(DateTimeOption expectedCompletionDatetime) {
        this.expectedCompletionDatetime.copyFrom(expectedCompletionDatetime);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(RunningJobflows source) {
        this.jobflowSid.copyFrom(source.jobflowSid);
        this.batchId.copyFrom(source.batchId);
        this.jobflowId.copyFrom(source.jobflowId);
        this.targetName.copyFrom(source.targetName);
        this.executionId.copyFrom(source.executionId);
        this.expectedCompletionDatetime.copyFrom(source.expectedCompletionDatetime);
    }
    @Override public void write(DataOutput out) throws IOException {
        jobflowSid.write(out);
        batchId.write(out);
        jobflowId.write(out);
        targetName.write(out);
        executionId.write(out);
        expectedCompletionDatetime.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        jobflowSid.readFields(in);
        batchId.readFields(in);
        jobflowId.readFields(in);
        targetName.readFields(in);
        executionId.readFields(in);
        expectedCompletionDatetime.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + jobflowSid.hashCode();
        result = prime * result + batchId.hashCode();
        result = prime * result + jobflowId.hashCode();
        result = prime * result + targetName.hashCode();
        result = prime * result + executionId.hashCode();
        result = prime * result + expectedCompletionDatetime.hashCode();
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
        RunningJobflows other = (RunningJobflows) obj;
        if(this.jobflowSid.equals(other.jobflowSid)== false) {
            return false;
        }
        if(this.batchId.equals(other.batchId)== false) {
            return false;
        }
        if(this.jobflowId.equals(other.jobflowId)== false) {
            return false;
        }
        if(this.targetName.equals(other.targetName)== false) {
            return false;
        }
        if(this.executionId.equals(other.executionId)== false) {
            return false;
        }
        if(this.expectedCompletionDatetime.equals(other.expectedCompletionDatetime)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=running_jobflows");
        result.append(", jobflowSid=");
        result.append(this.jobflowSid);
        result.append(", batchId=");
        result.append(this.batchId);
        result.append(", jobflowId=");
        result.append(this.jobflowId);
        result.append(", targetName=");
        result.append(this.targetName);
        result.append(", executionId=");
        result.append(this.executionId);
        result.append(", expectedCompletionDatetime=");
        result.append(this.expectedCompletionDatetime);
        result.append("}");
        return result.toString();
    }
}