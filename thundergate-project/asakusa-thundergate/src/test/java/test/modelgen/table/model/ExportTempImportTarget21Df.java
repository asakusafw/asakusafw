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
 * テーブル<code>export_temp_import_target2_1_df</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "export_temp_import_target2_1_df", columns = {
            "TEMP_SID"}, primary = {"TEMP_SID"})@SuppressWarnings("deprecation") public class ExportTempImportTarget21Df
        implements Writable {
    /**
     * カラム<code>TEMP_SID</code>を表すフィールド。
     */
    @Property(name = "TEMP_SID") private LongOption tempSid = new LongOption();
    /**
     * カラム<code>TEMP_SID</code>の値を返す。
     * @return カラム<code>TEMP_SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getTempSid() {
        return this.tempSid.get();
    }
    /**
     * カラム<code>TEMP_SID</code>の値を変更する。
     * @param tempSid 設定する値
     */
    public void setTempSid(long tempSid) {
        this.tempSid.modify(tempSid);
    }
    /**
     * {@link #getTempSid()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTempSid()}
     */
    public LongOption getTempSidOption() {
        return this.tempSid;
    }
    /**
     * {@link #setTempSid(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param tempSid 設定する値、消去する場合は{@code null}
     */
    public void setTempSidOption(LongOption tempSid) {
        this.tempSid.copyFrom(tempSid);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(ExportTempImportTarget21Df source) {
        this.tempSid.copyFrom(source.tempSid);
    }
    @Override public void write(DataOutput out) throws IOException {
        tempSid.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        tempSid.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + tempSid.hashCode();
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
        ExportTempImportTarget21Df other = (ExportTempImportTarget21Df) obj;
        if(this.tempSid.equals(other.tempSid)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=export_temp_import_target2_1_df");
        result.append(", tempSid=");
        result.append(this.tempSid);
        result.append("}");
        return result.toString();
    }
}