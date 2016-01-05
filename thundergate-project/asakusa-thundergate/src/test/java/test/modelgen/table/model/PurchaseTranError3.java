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
 * テーブル<code>purchase_tran_error3</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "purchase_tran_error3", columns = {"SID",
            "VERSION_NO", "RGST_DATETIME", "UPDT_DATETIME", "PURCHASE_NO", "PURCHASE_TYPE", "TRADE_TYPE", "TRADE_NO",
            "ERROR_CAUSE", "ERROR_CODE"}, primary = {"SID"})@SuppressWarnings("deprecation") public class
        PurchaseTranError3 implements Writable {
    /**
     * カラム<code>SID</code>を表すフィールド。
     */
    @Property(name = "SID") private LongOption sid = new LongOption();
    /**
     * カラム<code>VERSION_NO</code>を表すフィールド。
     */
    @Property(name = "VERSION_NO") private LongOption versionNo = new LongOption();
    /**
     * カラム<code>RGST_DATETIME</code>を表すフィールド。
     */
    @Property(name = "RGST_DATETIME") private DateTimeOption rgstDatetime = new DateTimeOption();
    /**
     * カラム<code>UPDT_DATETIME</code>を表すフィールド。
     */
    @Property(name = "UPDT_DATETIME") private DateTimeOption updtDatetime = new DateTimeOption();
    /**
     * カラム<code>PURCHASE_NO</code>を表すフィールド。
     */
    @Property(name = "PURCHASE_NO") private StringOption purchaseNo = new StringOption();
    /**
     * カラム<code>PURCHASE_TYPE</code>を表すフィールド。
     */
    @Property(name = "PURCHASE_TYPE") private StringOption purchaseType = new StringOption();
    /**
     * カラム<code>TRADE_TYPE</code>を表すフィールド。
     */
    @Property(name = "TRADE_TYPE") private StringOption tradeType = new StringOption();
    /**
     * カラム<code>TRADE_NO</code>を表すフィールド。
     */
    @Property(name = "TRADE_NO") private StringOption tradeNo = new StringOption();
    /**
     * カラム<code>ERROR_CAUSE</code>を表すフィールド。
     */
    @Property(name = "ERROR_CAUSE") private StringOption errorCause = new StringOption();
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
     * カラム<code>RGST_DATETIME</code>の値を返す。
     * @return カラム<code>RGST_DATETIME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getRgstDatetime() {
        return this.rgstDatetime.get();
    }
    /**
     * カラム<code>RGST_DATETIME</code>の値を変更する。
     * @param rgstDatetime 設定する値
     */
    public void setRgstDatetime(DateTime rgstDatetime) {
        this.rgstDatetime.modify(rgstDatetime);
    }
    /**
     * {@link #getRgstDatetime()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getRgstDatetime()}
     */
    public DateTimeOption getRgstDatetimeOption() {
        return this.rgstDatetime;
    }
    /**
     * {@link #setRgstDatetime(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param rgstDatetime 設定する値、消去する場合は{@code null}
     */
    public void setRgstDatetimeOption(DateTimeOption rgstDatetime) {
        this.rgstDatetime.copyFrom(rgstDatetime);
    }
    /**
     * カラム<code>UPDT_DATETIME</code>の値を返す。
     * @return カラム<code>UPDT_DATETIME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getUpdtDatetime() {
        return this.updtDatetime.get();
    }
    /**
     * カラム<code>UPDT_DATETIME</code>の値を変更する。
     * @param updtDatetime 設定する値
     */
    public void setUpdtDatetime(DateTime updtDatetime) {
        this.updtDatetime.modify(updtDatetime);
    }
    /**
     * {@link #getUpdtDatetime()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getUpdtDatetime()}
     */
    public DateTimeOption getUpdtDatetimeOption() {
        return this.updtDatetime;
    }
    /**
     * {@link #setUpdtDatetime(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param updtDatetime 設定する値、消去する場合は{@code null}
     */
    public void setUpdtDatetimeOption(DateTimeOption updtDatetime) {
        this.updtDatetime.copyFrom(updtDatetime);
    }
    /**
     * カラム<code>PURCHASE_NO</code>の値を返す。
     * @return カラム<code>PURCHASE_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getPurchaseNo() {
        return this.purchaseNo.get();
    }
    /**
     * カラム<code>PURCHASE_NO</code>の値を変更する。
     * @param purchaseNo 設定する値
     */
    public void setPurchaseNo(Text purchaseNo) {
        this.purchaseNo.modify(purchaseNo);
    }
    /**
     * カラム<code>PURCHASE_NO</code>の値を返す。
     * @return カラム<code>PURCHASE_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getPurchaseNoAsString() {
        return this.purchaseNo.getAsString();
    }
    /**
     * カラム<code>PURCHASE_NO</code>の値を変更する。
     * @param purchaseNo 設定する値
     */
    public void setPurchaseNoAsString(String purchaseNo) {
        this.purchaseNo.modify(purchaseNo);
    }
    /**
     * {@link #getPurchaseNo()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPurchaseNo()}
     */
    public StringOption getPurchaseNoOption() {
        return this.purchaseNo;
    }
    /**
     * {@link #setPurchaseNo(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param purchaseNo 設定する値、消去する場合は{@code null}
     */
    public void setPurchaseNoOption(StringOption purchaseNo) {
        this.purchaseNo.copyFrom(purchaseNo);
    }
    /**
     * カラム<code>PURCHASE_TYPE</code>の値を返す。
     * @return カラム<code>PURCHASE_TYPE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getPurchaseType() {
        return this.purchaseType.get();
    }
    /**
     * カラム<code>PURCHASE_TYPE</code>の値を変更する。
     * @param purchaseType 設定する値
     */
    public void setPurchaseType(Text purchaseType) {
        this.purchaseType.modify(purchaseType);
    }
    /**
     * カラム<code>PURCHASE_TYPE</code>の値を返す。
     * @return カラム<code>PURCHASE_TYPE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getPurchaseTypeAsString() {
        return this.purchaseType.getAsString();
    }
    /**
     * カラム<code>PURCHASE_TYPE</code>の値を変更する。
     * @param purchaseType 設定する値
     */
    public void setPurchaseTypeAsString(String purchaseType) {
        this.purchaseType.modify(purchaseType);
    }
    /**
     * {@link #getPurchaseType()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPurchaseType()}
     */
    public StringOption getPurchaseTypeOption() {
        return this.purchaseType;
    }
    /**
     * {@link #setPurchaseType(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param purchaseType 設定する値、消去する場合は{@code null}
     */
    public void setPurchaseTypeOption(StringOption purchaseType) {
        this.purchaseType.copyFrom(purchaseType);
    }
    /**
     * カラム<code>TRADE_TYPE</code>の値を返す。
     * @return カラム<code>TRADE_TYPE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTradeType() {
        return this.tradeType.get();
    }
    /**
     * カラム<code>TRADE_TYPE</code>の値を変更する。
     * @param tradeType 設定する値
     */
    public void setTradeType(Text tradeType) {
        this.tradeType.modify(tradeType);
    }
    /**
     * カラム<code>TRADE_TYPE</code>の値を返す。
     * @return カラム<code>TRADE_TYPE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTradeTypeAsString() {
        return this.tradeType.getAsString();
    }
    /**
     * カラム<code>TRADE_TYPE</code>の値を変更する。
     * @param tradeType 設定する値
     */
    public void setTradeTypeAsString(String tradeType) {
        this.tradeType.modify(tradeType);
    }
    /**
     * {@link #getTradeType()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTradeType()}
     */
    public StringOption getTradeTypeOption() {
        return this.tradeType;
    }
    /**
     * {@link #setTradeType(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param tradeType 設定する値、消去する場合は{@code null}
     */
    public void setTradeTypeOption(StringOption tradeType) {
        this.tradeType.copyFrom(tradeType);
    }
    /**
     * カラム<code>TRADE_NO</code>の値を返す。
     * @return カラム<code>TRADE_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTradeNo() {
        return this.tradeNo.get();
    }
    /**
     * カラム<code>TRADE_NO</code>の値を変更する。
     * @param tradeNo 設定する値
     */
    public void setTradeNo(Text tradeNo) {
        this.tradeNo.modify(tradeNo);
    }
    /**
     * カラム<code>TRADE_NO</code>の値を返す。
     * @return カラム<code>TRADE_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTradeNoAsString() {
        return this.tradeNo.getAsString();
    }
    /**
     * カラム<code>TRADE_NO</code>の値を変更する。
     * @param tradeNo 設定する値
     */
    public void setTradeNoAsString(String tradeNo) {
        this.tradeNo.modify(tradeNo);
    }
    /**
     * {@link #getTradeNo()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTradeNo()}
     */
    public StringOption getTradeNoOption() {
        return this.tradeNo;
    }
    /**
     * {@link #setTradeNo(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param tradeNo 設定する値、消去する場合は{@code null}
     */
    public void setTradeNoOption(StringOption tradeNo) {
        this.tradeNo.copyFrom(tradeNo);
    }
    /**
     * カラム<code>ERROR_CAUSE</code>の値を返す。
     * @return カラム<code>ERROR_CAUSE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getErrorCause() {
        return this.errorCause.get();
    }
    /**
     * カラム<code>ERROR_CAUSE</code>の値を変更する。
     * @param errorCause 設定する値
     */
    public void setErrorCause(Text errorCause) {
        this.errorCause.modify(errorCause);
    }
    /**
     * カラム<code>ERROR_CAUSE</code>の値を返す。
     * @return カラム<code>ERROR_CAUSE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getErrorCauseAsString() {
        return this.errorCause.getAsString();
    }
    /**
     * カラム<code>ERROR_CAUSE</code>の値を変更する。
     * @param errorCause 設定する値
     */
    public void setErrorCauseAsString(String errorCause) {
        this.errorCause.modify(errorCause);
    }
    /**
     * {@link #getErrorCause()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getErrorCause()}
     */
    public StringOption getErrorCauseOption() {
        return this.errorCause;
    }
    /**
     * {@link #setErrorCause(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param errorCause 設定する値、消去する場合は{@code null}
     */
    public void setErrorCauseOption(StringOption errorCause) {
        this.errorCause.copyFrom(errorCause);
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
    public void copyFrom(PurchaseTranError3 source) {
        this.sid.copyFrom(source.sid);
        this.versionNo.copyFrom(source.versionNo);
        this.rgstDatetime.copyFrom(source.rgstDatetime);
        this.updtDatetime.copyFrom(source.updtDatetime);
        this.purchaseNo.copyFrom(source.purchaseNo);
        this.purchaseType.copyFrom(source.purchaseType);
        this.tradeType.copyFrom(source.tradeType);
        this.tradeNo.copyFrom(source.tradeNo);
        this.errorCause.copyFrom(source.errorCause);
        this.errorCode.copyFrom(source.errorCode);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        versionNo.write(out);
        rgstDatetime.write(out);
        updtDatetime.write(out);
        purchaseNo.write(out);
        purchaseType.write(out);
        tradeType.write(out);
        tradeNo.write(out);
        errorCause.write(out);
        errorCode.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        versionNo.readFields(in);
        rgstDatetime.readFields(in);
        updtDatetime.readFields(in);
        purchaseNo.readFields(in);
        purchaseType.readFields(in);
        tradeType.readFields(in);
        tradeNo.readFields(in);
        errorCause.readFields(in);
        errorCode.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + versionNo.hashCode();
        result = prime * result + rgstDatetime.hashCode();
        result = prime * result + updtDatetime.hashCode();
        result = prime * result + purchaseNo.hashCode();
        result = prime * result + purchaseType.hashCode();
        result = prime * result + tradeType.hashCode();
        result = prime * result + tradeNo.hashCode();
        result = prime * result + errorCause.hashCode();
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
        PurchaseTranError3 other = (PurchaseTranError3) obj;
        if(this.sid.equals(other.sid)== false) {
            return false;
        }
        if(this.versionNo.equals(other.versionNo)== false) {
            return false;
        }
        if(this.rgstDatetime.equals(other.rgstDatetime)== false) {
            return false;
        }
        if(this.updtDatetime.equals(other.updtDatetime)== false) {
            return false;
        }
        if(this.purchaseNo.equals(other.purchaseNo)== false) {
            return false;
        }
        if(this.purchaseType.equals(other.purchaseType)== false) {
            return false;
        }
        if(this.tradeType.equals(other.tradeType)== false) {
            return false;
        }
        if(this.tradeNo.equals(other.tradeNo)== false) {
            return false;
        }
        if(this.errorCause.equals(other.errorCause)== false) {
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
        result.append("class=purchase_tran_error3");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", versionNo=");
        result.append(this.versionNo);
        result.append(", rgstDatetime=");
        result.append(this.rgstDatetime);
        result.append(", updtDatetime=");
        result.append(this.updtDatetime);
        result.append(", purchaseNo=");
        result.append(this.purchaseNo);
        result.append(", purchaseType=");
        result.append(this.purchaseType);
        result.append(", tradeType=");
        result.append(this.tradeType);
        result.append(", tradeNo=");
        result.append(this.tradeNo);
        result.append(", errorCause=");
        result.append(this.errorCause);
        result.append(", errorCode=");
        result.append(this.errorCode);
        result.append("}");
        return result.toString();
    }
}