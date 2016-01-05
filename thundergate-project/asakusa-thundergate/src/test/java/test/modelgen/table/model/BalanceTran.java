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
 * テーブル<code>balance_tran</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "balance_tran", columns = {"SID", "VERSION_NO",
            "RGST_DATETIME", "UPDT_DATETIME", "SELLER_CODE", "PREVIOUS_CUTOFF_DATE", "CUTOFF_DATE", "NEXT_CUTOFF_DATE",
            "PAYOUT_DATE", "CARRIED", "PURCHASE", "RTN", "DISCOUNT", "TAX", "PAYABLE", "MUTUAL", "RESERVES", "CANCEL",
            "PAYMENT", "NEXT_PURCHASE", "NEXT_RETURN", "NEXT_DISCOUNT", "NEXT_TAX", "PAYMENT_FLAG"}, primary = {"SID"})@
        SuppressWarnings("deprecation") public class BalanceTran implements Writable {
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
     * カラム<code>SELLER_CODE</code>を表すフィールド。
     */
    @Property(name = "SELLER_CODE") private StringOption sellerCode = new StringOption();
    /**
     * カラム<code>PREVIOUS_CUTOFF_DATE</code>を表すフィールド。
     */
    @Property(name = "PREVIOUS_CUTOFF_DATE") private DateOption previousCutoffDate = new DateOption();
    /**
     * カラム<code>CUTOFF_DATE</code>を表すフィールド。
     */
    @Property(name = "CUTOFF_DATE") private DateOption cutoffDate = new DateOption();
    /**
     * カラム<code>NEXT_CUTOFF_DATE</code>を表すフィールド。
     */
    @Property(name = "NEXT_CUTOFF_DATE") private DateOption nextCutoffDate = new DateOption();
    /**
     * カラム<code>PAYOUT_DATE</code>を表すフィールド。
     */
    @Property(name = "PAYOUT_DATE") private DateOption payoutDate = new DateOption();
    /**
     * カラム<code>CARRIED</code>を表すフィールド。
     */
    @Property(name = "CARRIED") private LongOption carried = new LongOption();
    /**
     * カラム<code>PURCHASE</code>を表すフィールド。
     */
    @Property(name = "PURCHASE") private LongOption purchase = new LongOption();
    /**
     * カラム<code>RTN</code>を表すフィールド。
     */
    @Property(name = "RTN") private LongOption rtn = new LongOption();
    /**
     * カラム<code>DISCOUNT</code>を表すフィールド。
     */
    @Property(name = "DISCOUNT") private LongOption discount = new LongOption();
    /**
     * カラム<code>TAX</code>を表すフィールド。
     */
    @Property(name = "TAX") private LongOption tax = new LongOption();
    /**
     * カラム<code>PAYABLE</code>を表すフィールド。
     */
    @Property(name = "PAYABLE") private LongOption payable = new LongOption();
    /**
     * カラム<code>MUTUAL</code>を表すフィールド。
     */
    @Property(name = "MUTUAL") private LongOption mutual = new LongOption();
    /**
     * カラム<code>RESERVES</code>を表すフィールド。
     */
    @Property(name = "RESERVES") private LongOption reserves = new LongOption();
    /**
     * カラム<code>CANCEL</code>を表すフィールド。
     */
    @Property(name = "CANCEL") private LongOption cancel = new LongOption();
    /**
     * カラム<code>PAYMENT</code>を表すフィールド。
     */
    @Property(name = "PAYMENT") private LongOption payment = new LongOption();
    /**
     * カラム<code>NEXT_PURCHASE</code>を表すフィールド。
     */
    @Property(name = "NEXT_PURCHASE") private LongOption nextPurchase = new LongOption();
    /**
     * カラム<code>NEXT_RETURN</code>を表すフィールド。
     */
    @Property(name = "NEXT_RETURN") private LongOption nextReturn = new LongOption();
    /**
     * カラム<code>NEXT_DISCOUNT</code>を表すフィールド。
     */
    @Property(name = "NEXT_DISCOUNT") private LongOption nextDiscount = new LongOption();
    /**
     * カラム<code>NEXT_TAX</code>を表すフィールド。
     */
    @Property(name = "NEXT_TAX") private LongOption nextTax = new LongOption();
    /**
     * カラム<code>PAYMENT_FLAG</code>を表すフィールド。
     */
    @Property(name = "PAYMENT_FLAG") private StringOption paymentFlag = new StringOption();
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
     * カラム<code>PREVIOUS_CUTOFF_DATE</code>の値を返す。
     * @return カラム<code>PREVIOUS_CUTOFF_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getPreviousCutoffDate() {
        return this.previousCutoffDate.get();
    }
    /**
     * カラム<code>PREVIOUS_CUTOFF_DATE</code>の値を変更する。
     * @param previousCutoffDate 設定する値
     */
    public void setPreviousCutoffDate(Date previousCutoffDate) {
        this.previousCutoffDate.modify(previousCutoffDate);
    }
    /**
     * {@link #getPreviousCutoffDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPreviousCutoffDate()}
     */
    public DateOption getPreviousCutoffDateOption() {
        return this.previousCutoffDate;
    }
    /**
     * {@link #setPreviousCutoffDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param previousCutoffDate 設定する値、消去する場合は{@code null}
     */
    public void setPreviousCutoffDateOption(DateOption previousCutoffDate) {
        this.previousCutoffDate.copyFrom(previousCutoffDate);
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
     * カラム<code>NEXT_CUTOFF_DATE</code>の値を返す。
     * @return カラム<code>NEXT_CUTOFF_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getNextCutoffDate() {
        return this.nextCutoffDate.get();
    }
    /**
     * カラム<code>NEXT_CUTOFF_DATE</code>の値を変更する。
     * @param nextCutoffDate 設定する値
     */
    public void setNextCutoffDate(Date nextCutoffDate) {
        this.nextCutoffDate.modify(nextCutoffDate);
    }
    /**
     * {@link #getNextCutoffDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getNextCutoffDate()}
     */
    public DateOption getNextCutoffDateOption() {
        return this.nextCutoffDate;
    }
    /**
     * {@link #setNextCutoffDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param nextCutoffDate 設定する値、消去する場合は{@code null}
     */
    public void setNextCutoffDateOption(DateOption nextCutoffDate) {
        this.nextCutoffDate.copyFrom(nextCutoffDate);
    }
    /**
     * カラム<code>PAYOUT_DATE</code>の値を返す。
     * @return カラム<code>PAYOUT_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getPayoutDate() {
        return this.payoutDate.get();
    }
    /**
     * カラム<code>PAYOUT_DATE</code>の値を変更する。
     * @param payoutDate 設定する値
     */
    public void setPayoutDate(Date payoutDate) {
        this.payoutDate.modify(payoutDate);
    }
    /**
     * {@link #getPayoutDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPayoutDate()}
     */
    public DateOption getPayoutDateOption() {
        return this.payoutDate;
    }
    /**
     * {@link #setPayoutDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param payoutDate 設定する値、消去する場合は{@code null}
     */
    public void setPayoutDateOption(DateOption payoutDate) {
        this.payoutDate.copyFrom(payoutDate);
    }
    /**
     * カラム<code>CARRIED</code>の値を返す。
     * @return カラム<code>CARRIED</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getCarried() {
        return this.carried.get();
    }
    /**
     * カラム<code>CARRIED</code>の値を変更する。
     * @param carried 設定する値
     */
    public void setCarried(long carried) {
        this.carried.modify(carried);
    }
    /**
     * {@link #getCarried()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getCarried()}
     */
    public LongOption getCarriedOption() {
        return this.carried;
    }
    /**
     * {@link #setCarried(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param carried 設定する値、消去する場合は{@code null}
     */
    public void setCarriedOption(LongOption carried) {
        this.carried.copyFrom(carried);
    }
    /**
     * カラム<code>PURCHASE</code>の値を返す。
     * @return カラム<code>PURCHASE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getPurchase() {
        return this.purchase.get();
    }
    /**
     * カラム<code>PURCHASE</code>の値を変更する。
     * @param purchase 設定する値
     */
    public void setPurchase(long purchase) {
        this.purchase.modify(purchase);
    }
    /**
     * {@link #getPurchase()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPurchase()}
     */
    public LongOption getPurchaseOption() {
        return this.purchase;
    }
    /**
     * {@link #setPurchase(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param purchase 設定する値、消去する場合は{@code null}
     */
    public void setPurchaseOption(LongOption purchase) {
        this.purchase.copyFrom(purchase);
    }
    /**
     * カラム<code>RTN</code>の値を返す。
     * @return カラム<code>RTN</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getRtn() {
        return this.rtn.get();
    }
    /**
     * カラム<code>RTN</code>の値を変更する。
     * @param rtn 設定する値
     */
    public void setRtn(long rtn) {
        this.rtn.modify(rtn);
    }
    /**
     * {@link #getRtn()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getRtn()}
     */
    public LongOption getRtnOption() {
        return this.rtn;
    }
    /**
     * {@link #setRtn(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param rtn 設定する値、消去する場合は{@code null}
     */
    public void setRtnOption(LongOption rtn) {
        this.rtn.copyFrom(rtn);
    }
    /**
     * カラム<code>DISCOUNT</code>の値を返す。
     * @return カラム<code>DISCOUNT</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getDiscount() {
        return this.discount.get();
    }
    /**
     * カラム<code>DISCOUNT</code>の値を変更する。
     * @param discount 設定する値
     */
    public void setDiscount(long discount) {
        this.discount.modify(discount);
    }
    /**
     * {@link #getDiscount()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDiscount()}
     */
    public LongOption getDiscountOption() {
        return this.discount;
    }
    /**
     * {@link #setDiscount(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param discount 設定する値、消去する場合は{@code null}
     */
    public void setDiscountOption(LongOption discount) {
        this.discount.copyFrom(discount);
    }
    /**
     * カラム<code>TAX</code>の値を返す。
     * @return カラム<code>TAX</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getTax() {
        return this.tax.get();
    }
    /**
     * カラム<code>TAX</code>の値を変更する。
     * @param tax 設定する値
     */
    public void setTax(long tax) {
        this.tax.modify(tax);
    }
    /**
     * {@link #getTax()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTax()}
     */
    public LongOption getTaxOption() {
        return this.tax;
    }
    /**
     * {@link #setTax(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param tax 設定する値、消去する場合は{@code null}
     */
    public void setTaxOption(LongOption tax) {
        this.tax.copyFrom(tax);
    }
    /**
     * カラム<code>PAYABLE</code>の値を返す。
     * @return カラム<code>PAYABLE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getPayable() {
        return this.payable.get();
    }
    /**
     * カラム<code>PAYABLE</code>の値を変更する。
     * @param payable 設定する値
     */
    public void setPayable(long payable) {
        this.payable.modify(payable);
    }
    /**
     * {@link #getPayable()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPayable()}
     */
    public LongOption getPayableOption() {
        return this.payable;
    }
    /**
     * {@link #setPayable(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param payable 設定する値、消去する場合は{@code null}
     */
    public void setPayableOption(LongOption payable) {
        this.payable.copyFrom(payable);
    }
    /**
     * カラム<code>MUTUAL</code>の値を返す。
     * @return カラム<code>MUTUAL</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getMutual() {
        return this.mutual.get();
    }
    /**
     * カラム<code>MUTUAL</code>の値を変更する。
     * @param mutual 設定する値
     */
    public void setMutual(long mutual) {
        this.mutual.modify(mutual);
    }
    /**
     * {@link #getMutual()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getMutual()}
     */
    public LongOption getMutualOption() {
        return this.mutual;
    }
    /**
     * {@link #setMutual(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param mutual 設定する値、消去する場合は{@code null}
     */
    public void setMutualOption(LongOption mutual) {
        this.mutual.copyFrom(mutual);
    }
    /**
     * カラム<code>RESERVES</code>の値を返す。
     * @return カラム<code>RESERVES</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getReserves() {
        return this.reserves.get();
    }
    /**
     * カラム<code>RESERVES</code>の値を変更する。
     * @param reserves 設定する値
     */
    public void setReserves(long reserves) {
        this.reserves.modify(reserves);
    }
    /**
     * {@link #getReserves()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getReserves()}
     */
    public LongOption getReservesOption() {
        return this.reserves;
    }
    /**
     * {@link #setReserves(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param reserves 設定する値、消去する場合は{@code null}
     */
    public void setReservesOption(LongOption reserves) {
        this.reserves.copyFrom(reserves);
    }
    /**
     * カラム<code>CANCEL</code>の値を返す。
     * @return カラム<code>CANCEL</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getCancel() {
        return this.cancel.get();
    }
    /**
     * カラム<code>CANCEL</code>の値を変更する。
     * @param cancel 設定する値
     */
    public void setCancel(long cancel) {
        this.cancel.modify(cancel);
    }
    /**
     * {@link #getCancel()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getCancel()}
     */
    public LongOption getCancelOption() {
        return this.cancel;
    }
    /**
     * {@link #setCancel(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param cancel 設定する値、消去する場合は{@code null}
     */
    public void setCancelOption(LongOption cancel) {
        this.cancel.copyFrom(cancel);
    }
    /**
     * カラム<code>PAYMENT</code>の値を返す。
     * @return カラム<code>PAYMENT</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getPayment() {
        return this.payment.get();
    }
    /**
     * カラム<code>PAYMENT</code>の値を変更する。
     * @param payment 設定する値
     */
    public void setPayment(long payment) {
        this.payment.modify(payment);
    }
    /**
     * {@link #getPayment()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPayment()}
     */
    public LongOption getPaymentOption() {
        return this.payment;
    }
    /**
     * {@link #setPayment(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param payment 設定する値、消去する場合は{@code null}
     */
    public void setPaymentOption(LongOption payment) {
        this.payment.copyFrom(payment);
    }
    /**
     * カラム<code>NEXT_PURCHASE</code>の値を返す。
     * @return カラム<code>NEXT_PURCHASE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getNextPurchase() {
        return this.nextPurchase.get();
    }
    /**
     * カラム<code>NEXT_PURCHASE</code>の値を変更する。
     * @param nextPurchase 設定する値
     */
    public void setNextPurchase(long nextPurchase) {
        this.nextPurchase.modify(nextPurchase);
    }
    /**
     * {@link #getNextPurchase()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getNextPurchase()}
     */
    public LongOption getNextPurchaseOption() {
        return this.nextPurchase;
    }
    /**
     * {@link #setNextPurchase(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param nextPurchase 設定する値、消去する場合は{@code null}
     */
    public void setNextPurchaseOption(LongOption nextPurchase) {
        this.nextPurchase.copyFrom(nextPurchase);
    }
    /**
     * カラム<code>NEXT_RETURN</code>の値を返す。
     * @return カラム<code>NEXT_RETURN</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getNextReturn() {
        return this.nextReturn.get();
    }
    /**
     * カラム<code>NEXT_RETURN</code>の値を変更する。
     * @param nextReturn 設定する値
     */
    public void setNextReturn(long nextReturn) {
        this.nextReturn.modify(nextReturn);
    }
    /**
     * {@link #getNextReturn()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getNextReturn()}
     */
    public LongOption getNextReturnOption() {
        return this.nextReturn;
    }
    /**
     * {@link #setNextReturn(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param nextReturn 設定する値、消去する場合は{@code null}
     */
    public void setNextReturnOption(LongOption nextReturn) {
        this.nextReturn.copyFrom(nextReturn);
    }
    /**
     * カラム<code>NEXT_DISCOUNT</code>の値を返す。
     * @return カラム<code>NEXT_DISCOUNT</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getNextDiscount() {
        return this.nextDiscount.get();
    }
    /**
     * カラム<code>NEXT_DISCOUNT</code>の値を変更する。
     * @param nextDiscount 設定する値
     */
    public void setNextDiscount(long nextDiscount) {
        this.nextDiscount.modify(nextDiscount);
    }
    /**
     * {@link #getNextDiscount()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getNextDiscount()}
     */
    public LongOption getNextDiscountOption() {
        return this.nextDiscount;
    }
    /**
     * {@link #setNextDiscount(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param nextDiscount 設定する値、消去する場合は{@code null}
     */
    public void setNextDiscountOption(LongOption nextDiscount) {
        this.nextDiscount.copyFrom(nextDiscount);
    }
    /**
     * カラム<code>NEXT_TAX</code>の値を返す。
     * @return カラム<code>NEXT_TAX</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getNextTax() {
        return this.nextTax.get();
    }
    /**
     * カラム<code>NEXT_TAX</code>の値を変更する。
     * @param nextTax 設定する値
     */
    public void setNextTax(long nextTax) {
        this.nextTax.modify(nextTax);
    }
    /**
     * {@link #getNextTax()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getNextTax()}
     */
    public LongOption getNextTaxOption() {
        return this.nextTax;
    }
    /**
     * {@link #setNextTax(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param nextTax 設定する値、消去する場合は{@code null}
     */
    public void setNextTaxOption(LongOption nextTax) {
        this.nextTax.copyFrom(nextTax);
    }
    /**
     * カラム<code>PAYMENT_FLAG</code>の値を返す。
     * @return カラム<code>PAYMENT_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getPaymentFlag() {
        return this.paymentFlag.get();
    }
    /**
     * カラム<code>PAYMENT_FLAG</code>の値を変更する。
     * @param paymentFlag 設定する値
     */
    public void setPaymentFlag(Text paymentFlag) {
        this.paymentFlag.modify(paymentFlag);
    }
    /**
     * カラム<code>PAYMENT_FLAG</code>の値を返す。
     * @return カラム<code>PAYMENT_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getPaymentFlagAsString() {
        return this.paymentFlag.getAsString();
    }
    /**
     * カラム<code>PAYMENT_FLAG</code>の値を変更する。
     * @param paymentFlag 設定する値
     */
    public void setPaymentFlagAsString(String paymentFlag) {
        this.paymentFlag.modify(paymentFlag);
    }
    /**
     * {@link #getPaymentFlag()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPaymentFlag()}
     */
    public StringOption getPaymentFlagOption() {
        return this.paymentFlag;
    }
    /**
     * {@link #setPaymentFlag(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param paymentFlag 設定する値、消去する場合は{@code null}
     */
    public void setPaymentFlagOption(StringOption paymentFlag) {
        this.paymentFlag.copyFrom(paymentFlag);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(BalanceTran source) {
        this.sid.copyFrom(source.sid);
        this.versionNo.copyFrom(source.versionNo);
        this.rgstDatetime.copyFrom(source.rgstDatetime);
        this.updtDatetime.copyFrom(source.updtDatetime);
        this.sellerCode.copyFrom(source.sellerCode);
        this.previousCutoffDate.copyFrom(source.previousCutoffDate);
        this.cutoffDate.copyFrom(source.cutoffDate);
        this.nextCutoffDate.copyFrom(source.nextCutoffDate);
        this.payoutDate.copyFrom(source.payoutDate);
        this.carried.copyFrom(source.carried);
        this.purchase.copyFrom(source.purchase);
        this.rtn.copyFrom(source.rtn);
        this.discount.copyFrom(source.discount);
        this.tax.copyFrom(source.tax);
        this.payable.copyFrom(source.payable);
        this.mutual.copyFrom(source.mutual);
        this.reserves.copyFrom(source.reserves);
        this.cancel.copyFrom(source.cancel);
        this.payment.copyFrom(source.payment);
        this.nextPurchase.copyFrom(source.nextPurchase);
        this.nextReturn.copyFrom(source.nextReturn);
        this.nextDiscount.copyFrom(source.nextDiscount);
        this.nextTax.copyFrom(source.nextTax);
        this.paymentFlag.copyFrom(source.paymentFlag);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        versionNo.write(out);
        rgstDatetime.write(out);
        updtDatetime.write(out);
        sellerCode.write(out);
        previousCutoffDate.write(out);
        cutoffDate.write(out);
        nextCutoffDate.write(out);
        payoutDate.write(out);
        carried.write(out);
        purchase.write(out);
        rtn.write(out);
        discount.write(out);
        tax.write(out);
        payable.write(out);
        mutual.write(out);
        reserves.write(out);
        cancel.write(out);
        payment.write(out);
        nextPurchase.write(out);
        nextReturn.write(out);
        nextDiscount.write(out);
        nextTax.write(out);
        paymentFlag.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        versionNo.readFields(in);
        rgstDatetime.readFields(in);
        updtDatetime.readFields(in);
        sellerCode.readFields(in);
        previousCutoffDate.readFields(in);
        cutoffDate.readFields(in);
        nextCutoffDate.readFields(in);
        payoutDate.readFields(in);
        carried.readFields(in);
        purchase.readFields(in);
        rtn.readFields(in);
        discount.readFields(in);
        tax.readFields(in);
        payable.readFields(in);
        mutual.readFields(in);
        reserves.readFields(in);
        cancel.readFields(in);
        payment.readFields(in);
        nextPurchase.readFields(in);
        nextReturn.readFields(in);
        nextDiscount.readFields(in);
        nextTax.readFields(in);
        paymentFlag.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + versionNo.hashCode();
        result = prime * result + rgstDatetime.hashCode();
        result = prime * result + updtDatetime.hashCode();
        result = prime * result + sellerCode.hashCode();
        result = prime * result + previousCutoffDate.hashCode();
        result = prime * result + cutoffDate.hashCode();
        result = prime * result + nextCutoffDate.hashCode();
        result = prime * result + payoutDate.hashCode();
        result = prime * result + carried.hashCode();
        result = prime * result + purchase.hashCode();
        result = prime * result + rtn.hashCode();
        result = prime * result + discount.hashCode();
        result = prime * result + tax.hashCode();
        result = prime * result + payable.hashCode();
        result = prime * result + mutual.hashCode();
        result = prime * result + reserves.hashCode();
        result = prime * result + cancel.hashCode();
        result = prime * result + payment.hashCode();
        result = prime * result + nextPurchase.hashCode();
        result = prime * result + nextReturn.hashCode();
        result = prime * result + nextDiscount.hashCode();
        result = prime * result + nextTax.hashCode();
        result = prime * result + paymentFlag.hashCode();
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
        BalanceTran other = (BalanceTran) obj;
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
        if(this.sellerCode.equals(other.sellerCode)== false) {
            return false;
        }
        if(this.previousCutoffDate.equals(other.previousCutoffDate)== false) {
            return false;
        }
        if(this.cutoffDate.equals(other.cutoffDate)== false) {
            return false;
        }
        if(this.nextCutoffDate.equals(other.nextCutoffDate)== false) {
            return false;
        }
        if(this.payoutDate.equals(other.payoutDate)== false) {
            return false;
        }
        if(this.carried.equals(other.carried)== false) {
            return false;
        }
        if(this.purchase.equals(other.purchase)== false) {
            return false;
        }
        if(this.rtn.equals(other.rtn)== false) {
            return false;
        }
        if(this.discount.equals(other.discount)== false) {
            return false;
        }
        if(this.tax.equals(other.tax)== false) {
            return false;
        }
        if(this.payable.equals(other.payable)== false) {
            return false;
        }
        if(this.mutual.equals(other.mutual)== false) {
            return false;
        }
        if(this.reserves.equals(other.reserves)== false) {
            return false;
        }
        if(this.cancel.equals(other.cancel)== false) {
            return false;
        }
        if(this.payment.equals(other.payment)== false) {
            return false;
        }
        if(this.nextPurchase.equals(other.nextPurchase)== false) {
            return false;
        }
        if(this.nextReturn.equals(other.nextReturn)== false) {
            return false;
        }
        if(this.nextDiscount.equals(other.nextDiscount)== false) {
            return false;
        }
        if(this.nextTax.equals(other.nextTax)== false) {
            return false;
        }
        if(this.paymentFlag.equals(other.paymentFlag)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=balance_tran");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", versionNo=");
        result.append(this.versionNo);
        result.append(", rgstDatetime=");
        result.append(this.rgstDatetime);
        result.append(", updtDatetime=");
        result.append(this.updtDatetime);
        result.append(", sellerCode=");
        result.append(this.sellerCode);
        result.append(", previousCutoffDate=");
        result.append(this.previousCutoffDate);
        result.append(", cutoffDate=");
        result.append(this.cutoffDate);
        result.append(", nextCutoffDate=");
        result.append(this.nextCutoffDate);
        result.append(", payoutDate=");
        result.append(this.payoutDate);
        result.append(", carried=");
        result.append(this.carried);
        result.append(", purchase=");
        result.append(this.purchase);
        result.append(", rtn=");
        result.append(this.rtn);
        result.append(", discount=");
        result.append(this.discount);
        result.append(", tax=");
        result.append(this.tax);
        result.append(", payable=");
        result.append(this.payable);
        result.append(", mutual=");
        result.append(this.mutual);
        result.append(", reserves=");
        result.append(this.reserves);
        result.append(", cancel=");
        result.append(this.cancel);
        result.append(", payment=");
        result.append(this.payment);
        result.append(", nextPurchase=");
        result.append(this.nextPurchase);
        result.append(", nextReturn=");
        result.append(this.nextReturn);
        result.append(", nextDiscount=");
        result.append(this.nextDiscount);
        result.append(", nextTax=");
        result.append(this.nextTax);
        result.append(", paymentFlag=");
        result.append(this.paymentFlag);
        result.append("}");
        return result.toString();
    }
}