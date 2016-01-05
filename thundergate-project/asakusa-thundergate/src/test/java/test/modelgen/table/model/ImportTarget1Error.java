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
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>import_target1_error</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "import_target1_error", columns = {"SID",
            "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1", "RGST_DATE", "UPDT_DATE", "ERROR_CODE"}, primary = {})@
        SuppressWarnings("deprecation") public class ImportTarget1Error implements Writable {
    /**
     * カラム<code>SID</code>を表すフィールド。
     */
    @Property(name = "SID") private LongOption sid = new LongOption();
    /**
     * カラム<code>VERSION_NO</code>を表すフィールド。
     */
    @Property(name = "VERSION_NO") private LongOption versionNo = new LongOption();
    /**
     * カラム<code>TEXTDATA1</code>を表すフィールド。
     */
    @Property(name = "TEXTDATA1") private StringOption textdata1 = new StringOption();
    /**
     * カラム<code>INTDATA1</code>を表すフィールド。
     */
    @Property(name = "INTDATA1") private IntOption intdata1 = new IntOption();
    /**
     * カラム<code>DATEDATA1</code>を表すフィールド。
     */
    @Property(name = "DATEDATA1") private DateTimeOption datedata1 = new DateTimeOption();
    /**
     * カラム<code>RGST_DATE</code>を表すフィールド。
     */
    @Property(name = "RGST_DATE") private DateTimeOption rgstDate = new DateTimeOption();
    /**
     * カラム<code>UPDT_DATE</code>を表すフィールド。
     */
    @Property(name = "UPDT_DATE") private DateTimeOption updtDate = new DateTimeOption();
    /**
     * カラム<code>ERROR_CODE</code>を表すフィールド。
     */
    @Property(name = "ERROR_CODE") private StringOption errorCode = new StringOption();
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
     * カラム<code>VERSION_NO</code>の値を返す。
     * @return カラム<code>VERSION_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getVersionNo() {
        return this.versionNo.get();
    }
    /**
     * カラム<code>VERSION_NO</code>の値を変更する。
     * @param versionNo 設定する値
     */
    public void setVersionNo(long versionNo) {
        this.versionNo.modify(versionNo);
    }
    /**
     * {@link #getVersionNo()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getVersionNo()}
     */
    public LongOption getVersionNoOption() {
        return this.versionNo;
    }
    /**
     * {@link #setVersionNo(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param versionNo 設定する値、消去する場合は{@code null}
     */
    public void setVersionNoOption(LongOption versionNo) {
        this.versionNo.copyFrom(versionNo);
    }
    /**
     * カラム<code>TEXTDATA1</code>の値を返す。
     * @return カラム<code>TEXTDATA1</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTextdata1() {
        return this.textdata1.get();
    }
    /**
     * カラム<code>TEXTDATA1</code>の値を変更する。
     * @param textdata1 設定する値
     */
    public void setTextdata1(Text textdata1) {
        this.textdata1.modify(textdata1);
    }
    /**
     * カラム<code>TEXTDATA1</code>の値を返す。
     * @return カラム<code>TEXTDATA1</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTextdata1AsString() {
        return this.textdata1.getAsString();
    }
    /**
     * カラム<code>TEXTDATA1</code>の値を変更する。
     * @param textdata1 設定する値
     */
    public void setTextdata1AsString(String textdata1) {
        this.textdata1.modify(textdata1);
    }
    /**
     * {@link #getTextdata1()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTextdata1()}
     */
    public StringOption getTextdata1Option() {
        return this.textdata1;
    }
    /**
     * {@link #setTextdata1(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param textdata1 設定する値、消去する場合は{@code null}
     */
    public void setTextdata1Option(StringOption textdata1) {
        this.textdata1.copyFrom(textdata1);
    }
    /**
     * カラム<code>INTDATA1</code>の値を返す。
     * @return カラム<code>INTDATA1</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public int getIntdata1() {
        return this.intdata1.get();
    }
    /**
     * カラム<code>INTDATA1</code>の値を変更する。
     * @param intdata1 設定する値
     */
    public void setIntdata1(int intdata1) {
        this.intdata1.modify(intdata1);
    }
    /**
     * {@link #getIntdata1()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getIntdata1()}
     */
    public IntOption getIntdata1Option() {
        return this.intdata1;
    }
    /**
     * {@link #setIntdata1(int)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param intdata1 設定する値、消去する場合は{@code null}
     */
    public void setIntdata1Option(IntOption intdata1) {
        this.intdata1.copyFrom(intdata1);
    }
    /**
     * カラム<code>DATEDATA1</code>の値を返す。
     * @return カラム<code>DATEDATA1</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getDatedata1() {
        return this.datedata1.get();
    }
    /**
     * カラム<code>DATEDATA1</code>の値を変更する。
     * @param datedata1 設定する値
     */
    public void setDatedata1(DateTime datedata1) {
        this.datedata1.modify(datedata1);
    }
    /**
     * {@link #getDatedata1()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDatedata1()}
     */
    public DateTimeOption getDatedata1Option() {
        return this.datedata1;
    }
    /**
     * {@link #setDatedata1(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param datedata1 設定する値、消去する場合は{@code null}
     */
    public void setDatedata1Option(DateTimeOption datedata1) {
        this.datedata1.copyFrom(datedata1);
    }
    /**
     * カラム<code>RGST_DATE</code>の値を返す。
     * @return カラム<code>RGST_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getRgstDate() {
        return this.rgstDate.get();
    }
    /**
     * カラム<code>RGST_DATE</code>の値を変更する。
     * @param rgstDate 設定する値
     */
    public void setRgstDate(DateTime rgstDate) {
        this.rgstDate.modify(rgstDate);
    }
    /**
     * {@link #getRgstDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getRgstDate()}
     */
    public DateTimeOption getRgstDateOption() {
        return this.rgstDate;
    }
    /**
     * {@link #setRgstDate(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param rgstDate 設定する値、消去する場合は{@code null}
     */
    public void setRgstDateOption(DateTimeOption rgstDate) {
        this.rgstDate.copyFrom(rgstDate);
    }
    /**
     * カラム<code>UPDT_DATE</code>の値を返す。
     * @return カラム<code>UPDT_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getUpdtDate() {
        return this.updtDate.get();
    }
    /**
     * カラム<code>UPDT_DATE</code>の値を変更する。
     * @param updtDate 設定する値
     */
    public void setUpdtDate(DateTime updtDate) {
        this.updtDate.modify(updtDate);
    }
    /**
     * {@link #getUpdtDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getUpdtDate()}
     */
    public DateTimeOption getUpdtDateOption() {
        return this.updtDate;
    }
    /**
     * {@link #setUpdtDate(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param updtDate 設定する値、消去する場合は{@code null}
     */
    public void setUpdtDateOption(DateTimeOption updtDate) {
        this.updtDate.copyFrom(updtDate);
    }
    /**
     * カラム<code>ERROR_CODE</code>の値を返す。
     * @return カラム<code>ERROR_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getErrorCode() {
        return this.errorCode.get();
    }
    /**
     * カラム<code>ERROR_CODE</code>の値を変更する。
     * @param errorCode 設定する値
     */
    public void setErrorCode(Text errorCode) {
        this.errorCode.modify(errorCode);
    }
    /**
     * カラム<code>ERROR_CODE</code>の値を返す。
     * @return カラム<code>ERROR_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getErrorCodeAsString() {
        return this.errorCode.getAsString();
    }
    /**
     * カラム<code>ERROR_CODE</code>の値を変更する。
     * @param errorCode 設定する値
     */
    public void setErrorCodeAsString(String errorCode) {
        this.errorCode.modify(errorCode);
    }
    /**
     * {@link #getErrorCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getErrorCode()}
     */
    public StringOption getErrorCodeOption() {
        return this.errorCode;
    }
    /**
     * {@link #setErrorCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param errorCode 設定する値、消去する場合は{@code null}
     */
    public void setErrorCodeOption(StringOption errorCode) {
        this.errorCode.copyFrom(errorCode);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(ImportTarget1Error source) {
        this.sid.copyFrom(source.sid);
        this.versionNo.copyFrom(source.versionNo);
        this.textdata1.copyFrom(source.textdata1);
        this.intdata1.copyFrom(source.intdata1);
        this.datedata1.copyFrom(source.datedata1);
        this.rgstDate.copyFrom(source.rgstDate);
        this.updtDate.copyFrom(source.updtDate);
        this.errorCode.copyFrom(source.errorCode);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        versionNo.write(out);
        textdata1.write(out);
        intdata1.write(out);
        datedata1.write(out);
        rgstDate.write(out);
        updtDate.write(out);
        errorCode.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        versionNo.readFields(in);
        textdata1.readFields(in);
        intdata1.readFields(in);
        datedata1.readFields(in);
        rgstDate.readFields(in);
        updtDate.readFields(in);
        errorCode.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + versionNo.hashCode();
        result = prime * result + textdata1.hashCode();
        result = prime * result + intdata1.hashCode();
        result = prime * result + datedata1.hashCode();
        result = prime * result + rgstDate.hashCode();
        result = prime * result + updtDate.hashCode();
        result = prime * result + errorCode.hashCode();
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
        ImportTarget1Error other = (ImportTarget1Error) obj;
        if(this.sid.equals(other.sid)== false) {
            return false;
        }
        if(this.versionNo.equals(other.versionNo)== false) {
            return false;
        }
        if(this.textdata1.equals(other.textdata1)== false) {
            return false;
        }
        if(this.intdata1.equals(other.intdata1)== false) {
            return false;
        }
        if(this.datedata1.equals(other.datedata1)== false) {
            return false;
        }
        if(this.rgstDate.equals(other.rgstDate)== false) {
            return false;
        }
        if(this.updtDate.equals(other.updtDate)== false) {
            return false;
        }
        if(this.errorCode.equals(other.errorCode)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=import_target1_error");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", versionNo=");
        result.append(this.versionNo);
        result.append(", textdata1=");
        result.append(this.textdata1);
        result.append(", intdata1=");
        result.append(this.intdata1);
        result.append(", datedata1=");
        result.append(this.datedata1);
        result.append(", rgstDate=");
        result.append(this.rgstDate);
        result.append(", updtDate=");
        result.append(this.updtDate);
        result.append(", errorCode=");
        result.append(this.errorCode);
        result.append("}");
        return result.toString();
    }
}