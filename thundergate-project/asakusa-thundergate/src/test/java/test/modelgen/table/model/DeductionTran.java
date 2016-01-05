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

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>deduction_tran</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "deduction_tran", columns = {"SID", "VERSION_NO"
            , "RGST_DATETIME", "UPDT_DATETIME", "DEDUCTION_NO", "SELLER_CODE", "BUYER_CODE", "CLOSED_DATE",
            "CUTOFF_DATE", "CUTOFF_FLAG", "PAYOUT_FLAG", "DISPOSE_NO", "DISPOSE_DATE", "DETAIL_COUNT"}, primary = {"SID"
        })@SuppressWarnings("deprecation") public class DeductionTran implements Writable {
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
     * カラム<code>DEDUCTION_NO</code>を表すフィールド。
     */
    @Property(name = "DEDUCTION_NO") private StringOption deductionNo = new StringOption();
    /**
     * カラム<code>SELLER_CODE</code>を表すフィールド。
     */
    @Property(name = "SELLER_CODE") private StringOption sellerCode = new StringOption();
    /**
     * カラム<code>BUYER_CODE</code>を表すフィールド。
     */
    @Property(name = "BUYER_CODE") private StringOption buyerCode = new StringOption();
    /**
     * カラム<code>CLOSED_DATE</code>を表すフィールド。
     */
    @Property(name = "CLOSED_DATE") private DateOption closedDate = new DateOption();
    /**
     * カラム<code>CUTOFF_DATE</code>を表すフィールド。
     */
    @Property(name = "CUTOFF_DATE") private DateOption cutoffDate = new DateOption();
    /**
     * カラム<code>CUTOFF_FLAG</code>を表すフィールド。
     */
    @Property(name = "CUTOFF_FLAG") private StringOption cutoffFlag = new StringOption();
    /**
     * カラム<code>PAYOUT_FLAG</code>を表すフィールド。
     */
    @Property(name = "PAYOUT_FLAG") private StringOption payoutFlag = new StringOption();
    /**
     * カラム<code>DISPOSE_NO</code>を表すフィールド。
     */
    @Property(name = "DISPOSE_NO") private StringOption disposeNo = new StringOption();
    /**
     * カラム<code>DISPOSE_DATE</code>を表すフィールド。
     */
    @Property(name = "DISPOSE_DATE") private DateOption disposeDate = new DateOption();
    /**
     * カラム<code>DETAIL_COUNT</code>を表すフィールド。
     */
    @Property(name = "DETAIL_COUNT") private LongOption detailCount = new LongOption();
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
     * カラム<code>DEDUCTION_NO</code>の値を返す。
     * @return カラム<code>DEDUCTION_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getDeductionNo() {
        return this.deductionNo.get();
    }
    /**
     * カラム<code>DEDUCTION_NO</code>の値を変更する。
     * @param deductionNo 設定する値
     */
    public void setDeductionNo(Text deductionNo) {
        this.deductionNo.modify(deductionNo);
    }
    /**
     * カラム<code>DEDUCTION_NO</code>の値を返す。
     * @return カラム<code>DEDUCTION_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getDeductionNoAsString() {
        return this.deductionNo.getAsString();
    }
    /**
     * カラム<code>DEDUCTION_NO</code>の値を変更する。
     * @param deductionNo 設定する値
     */
    public void setDeductionNoAsString(String deductionNo) {
        this.deductionNo.modify(deductionNo);
    }
    /**
     * {@link #getDeductionNo()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDeductionNo()}
     */
    public StringOption getDeductionNoOption() {
        return this.deductionNo;
    }
    /**
     * {@link #setDeductionNo(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param deductionNo 設定する値、消去する場合は{@code null}
     */
    public void setDeductionNoOption(StringOption deductionNo) {
        this.deductionNo.copyFrom(deductionNo);
    }
    /**
     * カラム<code>SELLER_CODE</code>の値を返す。
     * @return カラム<code>SELLER_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getSellerCode() {
        return this.sellerCode.get();
    }
    /**
     * カラム<code>SELLER_CODE</code>の値を変更する。
     * @param sellerCode 設定する値
     */
    public void setSellerCode(Text sellerCode) {
        this.sellerCode.modify(sellerCode);
    }
    /**
     * カラム<code>SELLER_CODE</code>の値を返す。
     * @return カラム<code>SELLER_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getSellerCodeAsString() {
        return this.sellerCode.getAsString();
    }
    /**
     * カラム<code>SELLER_CODE</code>の値を変更する。
     * @param sellerCode 設定する値
     */
    public void setSellerCodeAsString(String sellerCode) {
        this.sellerCode.modify(sellerCode);
    }
    /**
     * {@link #getSellerCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getSellerCode()}
     */
    public StringOption getSellerCodeOption() {
        return this.sellerCode;
    }
    /**
     * {@link #setSellerCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param sellerCode 設定する値、消去する場合は{@code null}
     */
    public void setSellerCodeOption(StringOption sellerCode) {
        this.sellerCode.copyFrom(sellerCode);
    }
    /**
     * カラム<code>BUYER_CODE</code>の値を返す。
     * @return カラム<code>BUYER_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getBuyerCode() {
        return this.buyerCode.get();
    }
    /**
     * カラム<code>BUYER_CODE</code>の値を変更する。
     * @param buyerCode 設定する値
     */
    public void setBuyerCode(Text buyerCode) {
        this.buyerCode.modify(buyerCode);
    }
    /**
     * カラム<code>BUYER_CODE</code>の値を返す。
     * @return カラム<code>BUYER_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getBuyerCodeAsString() {
        return this.buyerCode.getAsString();
    }
    /**
     * カラム<code>BUYER_CODE</code>の値を変更する。
     * @param buyerCode 設定する値
     */
    public void setBuyerCodeAsString(String buyerCode) {
        this.buyerCode.modify(buyerCode);
    }
    /**
     * {@link #getBuyerCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getBuyerCode()}
     */
    public StringOption getBuyerCodeOption() {
        return this.buyerCode;
    }
    /**
     * {@link #setBuyerCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param buyerCode 設定する値、消去する場合は{@code null}
     */
    public void setBuyerCodeOption(StringOption buyerCode) {
        this.buyerCode.copyFrom(buyerCode);
    }
    /**
     * カラム<code>CLOSED_DATE</code>の値を返す。
     * @return カラム<code>CLOSED_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getClosedDate() {
        return this.closedDate.get();
    }
    /**
     * カラム<code>CLOSED_DATE</code>の値を変更する。
     * @param closedDate 設定する値
     */
    public void setClosedDate(Date closedDate) {
        this.closedDate.modify(closedDate);
    }
    /**
     * {@link #getClosedDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getClosedDate()}
     */
    public DateOption getClosedDateOption() {
        return this.closedDate;
    }
    /**
     * {@link #setClosedDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param closedDate 設定する値、消去する場合は{@code null}
     */
    public void setClosedDateOption(DateOption closedDate) {
        this.closedDate.copyFrom(closedDate);
    }
    /**
     * カラム<code>CUTOFF_DATE</code>の値を返す。
     * @return カラム<code>CUTOFF_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getCutoffDate() {
        return this.cutoffDate.get();
    }
    /**
     * カラム<code>CUTOFF_DATE</code>の値を変更する。
     * @param cutoffDate 設定する値
     */
    public void setCutoffDate(Date cutoffDate) {
        this.cutoffDate.modify(cutoffDate);
    }
    /**
     * {@link #getCutoffDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getCutoffDate()}
     */
    public DateOption getCutoffDateOption() {
        return this.cutoffDate;
    }
    /**
     * {@link #setCutoffDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param cutoffDate 設定する値、消去する場合は{@code null}
     */
    public void setCutoffDateOption(DateOption cutoffDate) {
        this.cutoffDate.copyFrom(cutoffDate);
    }
    /**
     * カラム<code>CUTOFF_FLAG</code>の値を返す。
     * @return カラム<code>CUTOFF_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getCutoffFlag() {
        return this.cutoffFlag.get();
    }
    /**
     * カラム<code>CUTOFF_FLAG</code>の値を変更する。
     * @param cutoffFlag 設定する値
     */
    public void setCutoffFlag(Text cutoffFlag) {
        this.cutoffFlag.modify(cutoffFlag);
    }
    /**
     * カラム<code>CUTOFF_FLAG</code>の値を返す。
     * @return カラム<code>CUTOFF_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getCutoffFlagAsString() {
        return this.cutoffFlag.getAsString();
    }
    /**
     * カラム<code>CUTOFF_FLAG</code>の値を変更する。
     * @param cutoffFlag 設定する値
     */
    public void setCutoffFlagAsString(String cutoffFlag) {
        this.cutoffFlag.modify(cutoffFlag);
    }
    /**
     * {@link #getCutoffFlag()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getCutoffFlag()}
     */
    public StringOption getCutoffFlagOption() {
        return this.cutoffFlag;
    }
    /**
     * {@link #setCutoffFlag(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param cutoffFlag 設定する値、消去する場合は{@code null}
     */
    public void setCutoffFlagOption(StringOption cutoffFlag) {
        this.cutoffFlag.copyFrom(cutoffFlag);
    }
    /**
     * カラム<code>PAYOUT_FLAG</code>の値を返す。
     * @return カラム<code>PAYOUT_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getPayoutFlag() {
        return this.payoutFlag.get();
    }
    /**
     * カラム<code>PAYOUT_FLAG</code>の値を変更する。
     * @param payoutFlag 設定する値
     */
    public void setPayoutFlag(Text payoutFlag) {
        this.payoutFlag.modify(payoutFlag);
    }
    /**
     * カラム<code>PAYOUT_FLAG</code>の値を返す。
     * @return カラム<code>PAYOUT_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getPayoutFlagAsString() {
        return this.payoutFlag.getAsString();
    }
    /**
     * カラム<code>PAYOUT_FLAG</code>の値を変更する。
     * @param payoutFlag 設定する値
     */
    public void setPayoutFlagAsString(String payoutFlag) {
        this.payoutFlag.modify(payoutFlag);
    }
    /**
     * {@link #getPayoutFlag()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPayoutFlag()}
     */
    public StringOption getPayoutFlagOption() {
        return this.payoutFlag;
    }
    /**
     * {@link #setPayoutFlag(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param payoutFlag 設定する値、消去する場合は{@code null}
     */
    public void setPayoutFlagOption(StringOption payoutFlag) {
        this.payoutFlag.copyFrom(payoutFlag);
    }
    /**
     * カラム<code>DISPOSE_NO</code>の値を返す。
     * @return カラム<code>DISPOSE_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getDisposeNo() {
        return this.disposeNo.get();
    }
    /**
     * カラム<code>DISPOSE_NO</code>の値を変更する。
     * @param disposeNo 設定する値
     */
    public void setDisposeNo(Text disposeNo) {
        this.disposeNo.modify(disposeNo);
    }
    /**
     * カラム<code>DISPOSE_NO</code>の値を返す。
     * @return カラム<code>DISPOSE_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getDisposeNoAsString() {
        return this.disposeNo.getAsString();
    }
    /**
     * カラム<code>DISPOSE_NO</code>の値を変更する。
     * @param disposeNo 設定する値
     */
    public void setDisposeNoAsString(String disposeNo) {
        this.disposeNo.modify(disposeNo);
    }
    /**
     * {@link #getDisposeNo()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDisposeNo()}
     */
    public StringOption getDisposeNoOption() {
        return this.disposeNo;
    }
    /**
     * {@link #setDisposeNo(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param disposeNo 設定する値、消去する場合は{@code null}
     */
    public void setDisposeNoOption(StringOption disposeNo) {
        this.disposeNo.copyFrom(disposeNo);
    }
    /**
     * カラム<code>DISPOSE_DATE</code>の値を返す。
     * @return カラム<code>DISPOSE_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getDisposeDate() {
        return this.disposeDate.get();
    }
    /**
     * カラム<code>DISPOSE_DATE</code>の値を変更する。
     * @param disposeDate 設定する値
     */
    public void setDisposeDate(Date disposeDate) {
        this.disposeDate.modify(disposeDate);
    }
    /**
     * {@link #getDisposeDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDisposeDate()}
     */
    public DateOption getDisposeDateOption() {
        return this.disposeDate;
    }
    /**
     * {@link #setDisposeDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param disposeDate 設定する値、消去する場合は{@code null}
     */
    public void setDisposeDateOption(DateOption disposeDate) {
        this.disposeDate.copyFrom(disposeDate);
    }
    /**
     * カラム<code>DETAIL_COUNT</code>の値を返す。
     * @return カラム<code>DETAIL_COUNT</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getDetailCount() {
        return this.detailCount.get();
    }
    /**
     * カラム<code>DETAIL_COUNT</code>の値を変更する。
     * @param detailCount 設定する値
     */
    public void setDetailCount(long detailCount) {
        this.detailCount.modify(detailCount);
    }
    /**
     * {@link #getDetailCount()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDetailCount()}
     */
    public LongOption getDetailCountOption() {
        return this.detailCount;
    }
    /**
     * {@link #setDetailCount(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param detailCount 設定する値、消去する場合は{@code null}
     */
    public void setDetailCountOption(LongOption detailCount) {
        this.detailCount.copyFrom(detailCount);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(DeductionTran source) {
        this.sid.copyFrom(source.sid);
        this.versionNo.copyFrom(source.versionNo);
        this.rgstDatetime.copyFrom(source.rgstDatetime);
        this.updtDatetime.copyFrom(source.updtDatetime);
        this.deductionNo.copyFrom(source.deductionNo);
        this.sellerCode.copyFrom(source.sellerCode);
        this.buyerCode.copyFrom(source.buyerCode);
        this.closedDate.copyFrom(source.closedDate);
        this.cutoffDate.copyFrom(source.cutoffDate);
        this.cutoffFlag.copyFrom(source.cutoffFlag);
        this.payoutFlag.copyFrom(source.payoutFlag);
        this.disposeNo.copyFrom(source.disposeNo);
        this.disposeDate.copyFrom(source.disposeDate);
        this.detailCount.copyFrom(source.detailCount);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        versionNo.write(out);
        rgstDatetime.write(out);
        updtDatetime.write(out);
        deductionNo.write(out);
        sellerCode.write(out);
        buyerCode.write(out);
        closedDate.write(out);
        cutoffDate.write(out);
        cutoffFlag.write(out);
        payoutFlag.write(out);
        disposeNo.write(out);
        disposeDate.write(out);
        detailCount.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        versionNo.readFields(in);
        rgstDatetime.readFields(in);
        updtDatetime.readFields(in);
        deductionNo.readFields(in);
        sellerCode.readFields(in);
        buyerCode.readFields(in);
        closedDate.readFields(in);
        cutoffDate.readFields(in);
        cutoffFlag.readFields(in);
        payoutFlag.readFields(in);
        disposeNo.readFields(in);
        disposeDate.readFields(in);
        detailCount.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + versionNo.hashCode();
        result = prime * result + rgstDatetime.hashCode();
        result = prime * result + updtDatetime.hashCode();
        result = prime * result + deductionNo.hashCode();
        result = prime * result + sellerCode.hashCode();
        result = prime * result + buyerCode.hashCode();
        result = prime * result + closedDate.hashCode();
        result = prime * result + cutoffDate.hashCode();
        result = prime * result + cutoffFlag.hashCode();
        result = prime * result + payoutFlag.hashCode();
        result = prime * result + disposeNo.hashCode();
        result = prime * result + disposeDate.hashCode();
        result = prime * result + detailCount.hashCode();
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
        DeductionTran other = (DeductionTran) obj;
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
        if(this.deductionNo.equals(other.deductionNo)== false) {
            return false;
        }
        if(this.sellerCode.equals(other.sellerCode)== false) {
            return false;
        }
        if(this.buyerCode.equals(other.buyerCode)== false) {
            return false;
        }
        if(this.closedDate.equals(other.closedDate)== false) {
            return false;
        }
        if(this.cutoffDate.equals(other.cutoffDate)== false) {
            return false;
        }
        if(this.cutoffFlag.equals(other.cutoffFlag)== false) {
            return false;
        }
        if(this.payoutFlag.equals(other.payoutFlag)== false) {
            return false;
        }
        if(this.disposeNo.equals(other.disposeNo)== false) {
            return false;
        }
        if(this.disposeDate.equals(other.disposeDate)== false) {
            return false;
        }
        if(this.detailCount.equals(other.detailCount)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=deduction_tran");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", versionNo=");
        result.append(this.versionNo);
        result.append(", rgstDatetime=");
        result.append(this.rgstDatetime);
        result.append(", updtDatetime=");
        result.append(this.updtDatetime);
        result.append(", deductionNo=");
        result.append(this.deductionNo);
        result.append(", sellerCode=");
        result.append(this.sellerCode);
        result.append(", buyerCode=");
        result.append(this.buyerCode);
        result.append(", closedDate=");
        result.append(this.closedDate);
        result.append(", cutoffDate=");
        result.append(this.cutoffDate);
        result.append(", cutoffFlag=");
        result.append(this.cutoffFlag);
        result.append(", payoutFlag=");
        result.append(this.payoutFlag);
        result.append(", disposeNo=");
        result.append(this.disposeNo);
        result.append(", disposeDate=");
        result.append(this.disposeDate);
        result.append(", detailCount=");
        result.append(this.detailCount);
        result.append("}");
        return result.toString();
    }
}