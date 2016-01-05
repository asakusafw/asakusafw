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

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>import_target2_rl</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "import_target2_rl", columns = {"SID",
            "JOBFLOW_SID"}, primary = {"SID"})@SuppressWarnings("deprecation") public class ImportTarget2Rl implements
        Writable {
    /**
     * カラム<code>SID</code>を表すフィールド。
     */
    @Property(name = "SID") private LongOption sid = new LongOption();
    /**
     * カラム<code>JOBFLOW_SID</code>を表すフィールド。
     */
    @Property(name = "JOBFLOW_SID") private LongOption jobflowSid = new LongOption();
    /**
     * カラム<code>SID</code>の値を返す。
     * @return カラム<code>SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getSid() {
        return this.sid.get();
    }
    /**
     * カラム<code>SID</code>の値を変更する。
     * @param sid 設定する値
     */
    public void setSid(long sid) {
        this.sid.modify(sid);
    }
    /**
     * {@link #getSid()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getSid()}
     */
    public LongOption getSidOption() {
        return this.sid;
    }
    /**
     * {@link #setSid(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param sid 設定する値、消去する場合は{@code null}
     */
    public void setSidOption(LongOption sid) {
        this.sid.copyFrom(sid);
    }
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
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(ImportTarget2Rl source) {
        this.sid.copyFrom(source.sid);
        this.jobflowSid.copyFrom(source.jobflowSid);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        jobflowSid.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        jobflowSid.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + jobflowSid.hashCode();
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
        ImportTarget2Rl other = (ImportTarget2Rl) obj;
        if(this.sid.equals(other.sid)== false) {
            return false;
        }
        if(this.jobflowSid.equals(other.jobflowSid)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=import_target2_rl");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", jobflowSid=");
        result.append(this.jobflowSid);
        result.append("}");
        return result.toString();
    }
}