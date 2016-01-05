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
 * テーブル<code>purchase_tran_error</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "purchase_tran_error", columns = {"SID",
            "VERSION_NO", "RGST_DATETIME", "UPDT_DATETIME", "PURCHASE_NO", "PURCHASE_TYPE", "TRADE_TYPE", "TRADE_NO",
            "LINE_NO", "DELIVERY_DATE", "STORE_CODE", "BUYER_CODE", "PURCHASE_TYPE_CODE", "SELLER_CODE", "TENANT_CODE",
            "NET_PRICE_TOTAL", "SELLING_PRICE_TOTAL", "SHIPMENT_STORE_CODE", "SHIPMENT_SALES_TYPE_CODE",
            "DEDUCTION_CODE", "ACCOUNT_CODE", "OWNERSHIP_DATE", "CUTOFF_DATE", "PAYOUT_DATE", "OWNERSHIP_FLAG",
            "CUTOFF_FLAG", "PAYOUT_FLAG", "DISPOSE_NO", "DISPOSE_DATE"}, primary = {"SID"})@SuppressWarnings(
        "deprecation") public class PurchaseTranError implements Writable {
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
     * カラム<code>LINE_NO</code>を表すフィールド。
     */
    @Property(name = "LINE_NO") private LongOption lineNo = new LongOption();
    /**
     * カラム<code>DELIVERY_DATE</code>を表すフィールド。
     */
    @Property(name = "DELIVERY_DATE") private DateOption deliveryDate = new DateOption();
    /**
     * カラム<code>STORE_CODE</code>を表すフィールド。
     */
    @Property(name = "STORE_CODE") private StringOption storeCode = new StringOption();
    /**
     * カラム<code>BUYER_CODE</code>を表すフィールド。
     */
    @Property(name = "BUYER_CODE") private StringOption buyerCode = new StringOption();
    /**
     * カラム<code>PURCHASE_TYPE_CODE</code>を表すフィールド。
     */
    @Property(name = "PURCHASE_TYPE_CODE") private StringOption purchaseTypeCode = new StringOption();
    /**
     * カラム<code>SELLER_CODE</code>を表すフィールド。
     */
    @Property(name = "SELLER_CODE") private StringOption sellerCode = new StringOption();
    /**
     * カラム<code>TENANT_CODE</code>を表すフィールド。
     */
    @Property(name = "TENANT_CODE") private StringOption tenantCode = new StringOption();
    /**
     * カラム<code>NET_PRICE_TOTAL</code>を表すフィールド。
     */
    @Property(name = "NET_PRICE_TOTAL") private LongOption netPriceTotal = new LongOption();
    /**
     * カラム<code>SELLING_PRICE_TOTAL</code>を表すフィールド。
     */
    @Property(name = "SELLING_PRICE_TOTAL") private LongOption sellingPriceTotal = new LongOption();
    /**
     * カラム<code>SHIPMENT_STORE_CODE</code>を表すフィールド。
     */
    @Property(name = "SHIPMENT_STORE_CODE") private StringOption shipmentStoreCode = new StringOption();
    /**
     * カラム<code>SHIPMENT_SALES_TYPE_CODE</code>を表すフィールド。
     */
    @Property(name = "SHIPMENT_SALES_TYPE_CODE") private StringOption shipmentSalesTypeCode = new StringOption();
    /**
     * カラム<code>DEDUCTION_CODE</code>を表すフィールド。
     */
    @Property(name = "DEDUCTION_CODE") private StringOption deductionCode = new StringOption();
    /**
     * カラム<code>ACCOUNT_CODE</code>を表すフィールド。
     */
    @Property(name = "ACCOUNT_CODE") private StringOption accountCode = new StringOption();
    /**
     * カラム<code>OWNERSHIP_DATE</code>を表すフィールド。
     */
    @Property(name = "OWNERSHIP_DATE") private DateOption ownershipDate = new DateOption();
    /**
     * カラム<code>CUTOFF_DATE</code>を表すフィールド。
     */
    @Property(name = "CUTOFF_DATE") private DateOption cutoffDate = new DateOption();
    /**
     * カラム<code>PAYOUT_DATE</code>を表すフィールド。
     */
    @Property(name = "PAYOUT_DATE") private DateOption payoutDate = new DateOption();
    /**
     * カラム<code>OWNERSHIP_FLAG</code>を表すフィールド。
     */
    @Property(name = "OWNERSHIP_FLAG") private StringOption ownershipFlag = new StringOption();
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
     * カラム<code>LINE_NO</code>の値を返す。
     * @return カラム<code>LINE_NO</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getLineNo() {
        return this.lineNo.get();
    }
    /**
     * カラム<code>LINE_NO</code>の値を変更する。
     * @param lineNo 設定する値
     */
    public void setLineNo(long lineNo) {
        this.lineNo.modify(lineNo);
    }
    /**
     * {@link #getLineNo()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getLineNo()}
     */
    public LongOption getLineNoOption() {
        return this.lineNo;
    }
    /**
     * {@link #setLineNo(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param lineNo 設定する値、消去する場合は{@code null}
     */
    public void setLineNoOption(LongOption lineNo) {
        this.lineNo.copyFrom(lineNo);
    }
    /**
     * カラム<code>DELIVERY_DATE</code>の値を返す。
     * @return カラム<code>DELIVERY_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getDeliveryDate() {
        return this.deliveryDate.get();
    }
    /**
     * カラム<code>DELIVERY_DATE</code>の値を変更する。
     * @param deliveryDate 設定する値
     */
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate.modify(deliveryDate);
    }
    /**
     * {@link #getDeliveryDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDeliveryDate()}
     */
    public DateOption getDeliveryDateOption() {
        return this.deliveryDate;
    }
    /**
     * {@link #setDeliveryDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param deliveryDate 設定する値、消去する場合は{@code null}
     */
    public void setDeliveryDateOption(DateOption deliveryDate) {
        this.deliveryDate.copyFrom(deliveryDate);
    }
    /**
     * カラム<code>STORE_CODE</code>の値を返す。
     * @return カラム<code>STORE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getStoreCode() {
        return this.storeCode.get();
    }
    /**
     * カラム<code>STORE_CODE</code>の値を変更する。
     * @param storeCode 設定する値
     */
    public void setStoreCode(Text storeCode) {
        this.storeCode.modify(storeCode);
    }
    /**
     * カラム<code>STORE_CODE</code>の値を返す。
     * @return カラム<code>STORE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getStoreCodeAsString() {
        return this.storeCode.getAsString();
    }
    /**
     * カラム<code>STORE_CODE</code>の値を変更する。
     * @param storeCode 設定する値
     */
    public void setStoreCodeAsString(String storeCode) {
        this.storeCode.modify(storeCode);
    }
    /**
     * {@link #getStoreCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getStoreCode()}
     */
    public StringOption getStoreCodeOption() {
        return this.storeCode;
    }
    /**
     * {@link #setStoreCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param storeCode 設定する値、消去する場合は{@code null}
     */
    public void setStoreCodeOption(StringOption storeCode) {
        this.storeCode.copyFrom(storeCode);
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
     * カラム<code>PURCHASE_TYPE_CODE</code>の値を返す。
     * @return カラム<code>PURCHASE_TYPE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getPurchaseTypeCode() {
        return this.purchaseTypeCode.get();
    }
    /**
     * カラム<code>PURCHASE_TYPE_CODE</code>の値を変更する。
     * @param purchaseTypeCode 設定する値
     */
    public void setPurchaseTypeCode(Text purchaseTypeCode) {
        this.purchaseTypeCode.modify(purchaseTypeCode);
    }
    /**
     * カラム<code>PURCHASE_TYPE_CODE</code>の値を返す。
     * @return カラム<code>PURCHASE_TYPE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getPurchaseTypeCodeAsString() {
        return this.purchaseTypeCode.getAsString();
    }
    /**
     * カラム<code>PURCHASE_TYPE_CODE</code>の値を変更する。
     * @param purchaseTypeCode 設定する値
     */
    public void setPurchaseTypeCodeAsString(String purchaseTypeCode) {
        this.purchaseTypeCode.modify(purchaseTypeCode);
    }
    /**
     * {@link #getPurchaseTypeCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getPurchaseTypeCode()}
     */
    public StringOption getPurchaseTypeCodeOption() {
        return this.purchaseTypeCode;
    }
    /**
     * {@link #setPurchaseTypeCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param purchaseTypeCode 設定する値、消去する場合は{@code null}
     */
    public void setPurchaseTypeCodeOption(StringOption purchaseTypeCode) {
        this.purchaseTypeCode.copyFrom(purchaseTypeCode);
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
     * カラム<code>TENANT_CODE</code>の値を返す。
     * @return カラム<code>TENANT_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTenantCode() {
        return this.tenantCode.get();
    }
    /**
     * カラム<code>TENANT_CODE</code>の値を変更する。
     * @param tenantCode 設定する値
     */
    public void setTenantCode(Text tenantCode) {
        this.tenantCode.modify(tenantCode);
    }
    /**
     * カラム<code>TENANT_CODE</code>の値を返す。
     * @return カラム<code>TENANT_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTenantCodeAsString() {
        return this.tenantCode.getAsString();
    }
    /**
     * カラム<code>TENANT_CODE</code>の値を変更する。
     * @param tenantCode 設定する値
     */
    public void setTenantCodeAsString(String tenantCode) {
        this.tenantCode.modify(tenantCode);
    }
    /**
     * {@link #getTenantCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTenantCode()}
     */
    public StringOption getTenantCodeOption() {
        return this.tenantCode;
    }
    /**
     * {@link #setTenantCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param tenantCode 設定する値、消去する場合は{@code null}
     */
    public void setTenantCodeOption(StringOption tenantCode) {
        this.tenantCode.copyFrom(tenantCode);
    }
    /**
     * カラム<code>NET_PRICE_TOTAL</code>の値を返す。
     * @return カラム<code>NET_PRICE_TOTAL</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getNetPriceTotal() {
        return this.netPriceTotal.get();
    }
    /**
     * カラム<code>NET_PRICE_TOTAL</code>の値を変更する。
     * @param netPriceTotal 設定する値
     */
    public void setNetPriceTotal(long netPriceTotal) {
        this.netPriceTotal.modify(netPriceTotal);
    }
    /**
     * {@link #getNetPriceTotal()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getNetPriceTotal()}
     */
    public LongOption getNetPriceTotalOption() {
        return this.netPriceTotal;
    }
    /**
     * {@link #setNetPriceTotal(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param netPriceTotal 設定する値、消去する場合は{@code null}
     */
    public void setNetPriceTotalOption(LongOption netPriceTotal) {
        this.netPriceTotal.copyFrom(netPriceTotal);
    }
    /**
     * カラム<code>SELLING_PRICE_TOTAL</code>の値を返す。
     * @return カラム<code>SELLING_PRICE_TOTAL</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getSellingPriceTotal() {
        return this.sellingPriceTotal.get();
    }
    /**
     * カラム<code>SELLING_PRICE_TOTAL</code>の値を変更する。
     * @param sellingPriceTotal 設定する値
     */
    public void setSellingPriceTotal(long sellingPriceTotal) {
        this.sellingPriceTotal.modify(sellingPriceTotal);
    }
    /**
     * {@link #getSellingPriceTotal()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getSellingPriceTotal()}
     */
    public LongOption getSellingPriceTotalOption() {
        return this.sellingPriceTotal;
    }
    /**
     * {@link #setSellingPriceTotal(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param sellingPriceTotal 設定する値、消去する場合は{@code null}
     */
    public void setSellingPriceTotalOption(LongOption sellingPriceTotal) {
        this.sellingPriceTotal.copyFrom(sellingPriceTotal);
    }
    /**
     * カラム<code>SHIPMENT_STORE_CODE</code>の値を返す。
     * @return カラム<code>SHIPMENT_STORE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getShipmentStoreCode() {
        return this.shipmentStoreCode.get();
    }
    /**
     * カラム<code>SHIPMENT_STORE_CODE</code>の値を変更する。
     * @param shipmentStoreCode 設定する値
     */
    public void setShipmentStoreCode(Text shipmentStoreCode) {
        this.shipmentStoreCode.modify(shipmentStoreCode);
    }
    /**
     * カラム<code>SHIPMENT_STORE_CODE</code>の値を返す。
     * @return カラム<code>SHIPMENT_STORE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getShipmentStoreCodeAsString() {
        return this.shipmentStoreCode.getAsString();
    }
    /**
     * カラム<code>SHIPMENT_STORE_CODE</code>の値を変更する。
     * @param shipmentStoreCode 設定する値
     */
    public void setShipmentStoreCodeAsString(String shipmentStoreCode) {
        this.shipmentStoreCode.modify(shipmentStoreCode);
    }
    /**
     * {@link #getShipmentStoreCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getShipmentStoreCode()}
     */
    public StringOption getShipmentStoreCodeOption() {
        return this.shipmentStoreCode;
    }
    /**
     * {@link #setShipmentStoreCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param shipmentStoreCode 設定する値、消去する場合は{@code null}
     */
    public void setShipmentStoreCodeOption(StringOption shipmentStoreCode) {
        this.shipmentStoreCode.copyFrom(shipmentStoreCode);
    }
    /**
     * カラム<code>SHIPMENT_SALES_TYPE_CODE</code>の値を返す。
     * @return カラム<code>SHIPMENT_SALES_TYPE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getShipmentSalesTypeCode() {
        return this.shipmentSalesTypeCode.get();
    }
    /**
     * カラム<code>SHIPMENT_SALES_TYPE_CODE</code>の値を変更する。
     * @param shipmentSalesTypeCode 設定する値
     */
    public void setShipmentSalesTypeCode(Text shipmentSalesTypeCode) {
        this.shipmentSalesTypeCode.modify(shipmentSalesTypeCode);
    }
    /**
     * カラム<code>SHIPMENT_SALES_TYPE_CODE</code>の値を返す。
     * @return カラム<code>SHIPMENT_SALES_TYPE_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getShipmentSalesTypeCodeAsString() {
        return this.shipmentSalesTypeCode.getAsString();
    }
    /**
     * カラム<code>SHIPMENT_SALES_TYPE_CODE</code>の値を変更する。
     * @param shipmentSalesTypeCode 設定する値
     */
    public void setShipmentSalesTypeCodeAsString(String shipmentSalesTypeCode) {
        this.shipmentSalesTypeCode.modify(shipmentSalesTypeCode);
    }
    /**
     * {@link #getShipmentSalesTypeCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getShipmentSalesTypeCode()}
     */
    public StringOption getShipmentSalesTypeCodeOption() {
        return this.shipmentSalesTypeCode;
    }
    /**
     * {@link #setShipmentSalesTypeCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param shipmentSalesTypeCode 設定する値、消去する場合は{@code null}
     */
    public void setShipmentSalesTypeCodeOption(StringOption shipmentSalesTypeCode) {
        this.shipmentSalesTypeCode.copyFrom(shipmentSalesTypeCode);
    }
    /**
     * カラム<code>DEDUCTION_CODE</code>の値を返す。
     * @return カラム<code>DEDUCTION_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getDeductionCode() {
        return this.deductionCode.get();
    }
    /**
     * カラム<code>DEDUCTION_CODE</code>の値を変更する。
     * @param deductionCode 設定する値
     */
    public void setDeductionCode(Text deductionCode) {
        this.deductionCode.modify(deductionCode);
    }
    /**
     * カラム<code>DEDUCTION_CODE</code>の値を返す。
     * @return カラム<code>DEDUCTION_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getDeductionCodeAsString() {
        return this.deductionCode.getAsString();
    }
    /**
     * カラム<code>DEDUCTION_CODE</code>の値を変更する。
     * @param deductionCode 設定する値
     */
    public void setDeductionCodeAsString(String deductionCode) {
        this.deductionCode.modify(deductionCode);
    }
    /**
     * {@link #getDeductionCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getDeductionCode()}
     */
    public StringOption getDeductionCodeOption() {
        return this.deductionCode;
    }
    /**
     * {@link #setDeductionCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param deductionCode 設定する値、消去する場合は{@code null}
     */
    public void setDeductionCodeOption(StringOption deductionCode) {
        this.deductionCode.copyFrom(deductionCode);
    }
    /**
     * カラム<code>ACCOUNT_CODE</code>の値を返す。
     * @return カラム<code>ACCOUNT_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getAccountCode() {
        return this.accountCode.get();
    }
    /**
     * カラム<code>ACCOUNT_CODE</code>の値を変更する。
     * @param accountCode 設定する値
     */
    public void setAccountCode(Text accountCode) {
        this.accountCode.modify(accountCode);
    }
    /**
     * カラム<code>ACCOUNT_CODE</code>の値を返す。
     * @return カラム<code>ACCOUNT_CODE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getAccountCodeAsString() {
        return this.accountCode.getAsString();
    }
    /**
     * カラム<code>ACCOUNT_CODE</code>の値を変更する。
     * @param accountCode 設定する値
     */
    public void setAccountCodeAsString(String accountCode) {
        this.accountCode.modify(accountCode);
    }
    /**
     * {@link #getAccountCode()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getAccountCode()}
     */
    public StringOption getAccountCodeOption() {
        return this.accountCode;
    }
    /**
     * {@link #setAccountCode(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param accountCode 設定する値、消去する場合は{@code null}
     */
    public void setAccountCodeOption(StringOption accountCode) {
        this.accountCode.copyFrom(accountCode);
    }
    /**
     * カラム<code>OWNERSHIP_DATE</code>の値を返す。
     * @return カラム<code>OWNERSHIP_DATE</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Date getOwnershipDate() {
        return this.ownershipDate.get();
    }
    /**
     * カラム<code>OWNERSHIP_DATE</code>の値を変更する。
     * @param ownershipDate 設定する値
     */
    public void setOwnershipDate(Date ownershipDate) {
        this.ownershipDate.modify(ownershipDate);
    }
    /**
     * {@link #getOwnershipDate()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getOwnershipDate()}
     */
    public DateOption getOwnershipDateOption() {
        return this.ownershipDate;
    }
    /**
     * {@link #setOwnershipDate(Date)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param ownershipDate 設定する値、消去する場合は{@code null}
     */
    public void setOwnershipDateOption(DateOption ownershipDate) {
        this.ownershipDate.copyFrom(ownershipDate);
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
     * カラム<code>OWNERSHIP_FLAG</code>の値を返す。
     * @return カラム<code>OWNERSHIP_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getOwnershipFlag() {
        return this.ownershipFlag.get();
    }
    /**
     * カラム<code>OWNERSHIP_FLAG</code>の値を変更する。
     * @param ownershipFlag 設定する値
     */
    public void setOwnershipFlag(Text ownershipFlag) {
        this.ownershipFlag.modify(ownershipFlag);
    }
    /**
     * カラム<code>OWNERSHIP_FLAG</code>の値を返す。
     * @return カラム<code>OWNERSHIP_FLAG</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getOwnershipFlagAsString() {
        return this.ownershipFlag.getAsString();
    }
    /**
     * カラム<code>OWNERSHIP_FLAG</code>の値を変更する。
     * @param ownershipFlag 設定する値
     */
    public void setOwnershipFlagAsString(String ownershipFlag) {
        this.ownershipFlag.modify(ownershipFlag);
    }
    /**
     * {@link #getOwnershipFlag()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getOwnershipFlag()}
     */
    public StringOption getOwnershipFlagOption() {
        return this.ownershipFlag;
    }
    /**
     * {@link #setOwnershipFlag(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param ownershipFlag 設定する値、消去する場合は{@code null}
     */
    public void setOwnershipFlagOption(StringOption ownershipFlag) {
        this.ownershipFlag.copyFrom(ownershipFlag);
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
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(PurchaseTranError source) {
        this.sid.copyFrom(source.sid);
        this.versionNo.copyFrom(source.versionNo);
        this.rgstDatetime.copyFrom(source.rgstDatetime);
        this.updtDatetime.copyFrom(source.updtDatetime);
        this.purchaseNo.copyFrom(source.purchaseNo);
        this.purchaseType.copyFrom(source.purchaseType);
        this.tradeType.copyFrom(source.tradeType);
        this.tradeNo.copyFrom(source.tradeNo);
        this.lineNo.copyFrom(source.lineNo);
        this.deliveryDate.copyFrom(source.deliveryDate);
        this.storeCode.copyFrom(source.storeCode);
        this.buyerCode.copyFrom(source.buyerCode);
        this.purchaseTypeCode.copyFrom(source.purchaseTypeCode);
        this.sellerCode.copyFrom(source.sellerCode);
        this.tenantCode.copyFrom(source.tenantCode);
        this.netPriceTotal.copyFrom(source.netPriceTotal);
        this.sellingPriceTotal.copyFrom(source.sellingPriceTotal);
        this.shipmentStoreCode.copyFrom(source.shipmentStoreCode);
        this.shipmentSalesTypeCode.copyFrom(source.shipmentSalesTypeCode);
        this.deductionCode.copyFrom(source.deductionCode);
        this.accountCode.copyFrom(source.accountCode);
        this.ownershipDate.copyFrom(source.ownershipDate);
        this.cutoffDate.copyFrom(source.cutoffDate);
        this.payoutDate.copyFrom(source.payoutDate);
        this.ownershipFlag.copyFrom(source.ownershipFlag);
        this.cutoffFlag.copyFrom(source.cutoffFlag);
        this.payoutFlag.copyFrom(source.payoutFlag);
        this.disposeNo.copyFrom(source.disposeNo);
        this.disposeDate.copyFrom(source.disposeDate);
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
        lineNo.write(out);
        deliveryDate.write(out);
        storeCode.write(out);
        buyerCode.write(out);
        purchaseTypeCode.write(out);
        sellerCode.write(out);
        tenantCode.write(out);
        netPriceTotal.write(out);
        sellingPriceTotal.write(out);
        shipmentStoreCode.write(out);
        shipmentSalesTypeCode.write(out);
        deductionCode.write(out);
        accountCode.write(out);
        ownershipDate.write(out);
        cutoffDate.write(out);
        payoutDate.write(out);
        ownershipFlag.write(out);
        cutoffFlag.write(out);
        payoutFlag.write(out);
        disposeNo.write(out);
        disposeDate.write(out);
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
        lineNo.readFields(in);
        deliveryDate.readFields(in);
        storeCode.readFields(in);
        buyerCode.readFields(in);
        purchaseTypeCode.readFields(in);
        sellerCode.readFields(in);
        tenantCode.readFields(in);
        netPriceTotal.readFields(in);
        sellingPriceTotal.readFields(in);
        shipmentStoreCode.readFields(in);
        shipmentSalesTypeCode.readFields(in);
        deductionCode.readFields(in);
        accountCode.readFields(in);
        ownershipDate.readFields(in);
        cutoffDate.readFields(in);
        payoutDate.readFields(in);
        ownershipFlag.readFields(in);
        cutoffFlag.readFields(in);
        payoutFlag.readFields(in);
        disposeNo.readFields(in);
        disposeDate.readFields(in);
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
        result = prime * result + lineNo.hashCode();
        result = prime * result + deliveryDate.hashCode();
        result = prime * result + storeCode.hashCode();
        result = prime * result + buyerCode.hashCode();
        result = prime * result + purchaseTypeCode.hashCode();
        result = prime * result + sellerCode.hashCode();
        result = prime * result + tenantCode.hashCode();
        result = prime * result + netPriceTotal.hashCode();
        result = prime * result + sellingPriceTotal.hashCode();
        result = prime * result + shipmentStoreCode.hashCode();
        result = prime * result + shipmentSalesTypeCode.hashCode();
        result = prime * result + deductionCode.hashCode();
        result = prime * result + accountCode.hashCode();
        result = prime * result + ownershipDate.hashCode();
        result = prime * result + cutoffDate.hashCode();
        result = prime * result + payoutDate.hashCode();
        result = prime * result + ownershipFlag.hashCode();
        result = prime * result + cutoffFlag.hashCode();
        result = prime * result + payoutFlag.hashCode();
        result = prime * result + disposeNo.hashCode();
        result = prime * result + disposeDate.hashCode();
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
        PurchaseTranError other = (PurchaseTranError) obj;
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
        if(this.lineNo.equals(other.lineNo)== false) {
            return false;
        }
        if(this.deliveryDate.equals(other.deliveryDate)== false) {
            return false;
        }
        if(this.storeCode.equals(other.storeCode)== false) {
            return false;
        }
        if(this.buyerCode.equals(other.buyerCode)== false) {
            return false;
        }
        if(this.purchaseTypeCode.equals(other.purchaseTypeCode)== false) {
            return false;
        }
        if(this.sellerCode.equals(other.sellerCode)== false) {
            return false;
        }
        if(this.tenantCode.equals(other.tenantCode)== false) {
            return false;
        }
        if(this.netPriceTotal.equals(other.netPriceTotal)== false) {
            return false;
        }
        if(this.sellingPriceTotal.equals(other.sellingPriceTotal)== false) {
            return false;
        }
        if(this.shipmentStoreCode.equals(other.shipmentStoreCode)== false) {
            return false;
        }
        if(this.shipmentSalesTypeCode.equals(other.shipmentSalesTypeCode)== false) {
            return false;
        }
        if(this.deductionCode.equals(other.deductionCode)== false) {
            return false;
        }
        if(this.accountCode.equals(other.accountCode)== false) {
            return false;
        }
        if(this.ownershipDate.equals(other.ownershipDate)== false) {
            return false;
        }
        if(this.cutoffDate.equals(other.cutoffDate)== false) {
            return false;
        }
        if(this.payoutDate.equals(other.payoutDate)== false) {
            return false;
        }
        if(this.ownershipFlag.equals(other.ownershipFlag)== false) {
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
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=purchase_tran_error");
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
        result.append(", lineNo=");
        result.append(this.lineNo);
        result.append(", deliveryDate=");
        result.append(this.deliveryDate);
        result.append(", storeCode=");
        result.append(this.storeCode);
        result.append(", buyerCode=");
        result.append(this.buyerCode);
        result.append(", purchaseTypeCode=");
        result.append(this.purchaseTypeCode);
        result.append(", sellerCode=");
        result.append(this.sellerCode);
        result.append(", tenantCode=");
        result.append(this.tenantCode);
        result.append(", netPriceTotal=");
        result.append(this.netPriceTotal);
        result.append(", sellingPriceTotal=");
        result.append(this.sellingPriceTotal);
        result.append(", shipmentStoreCode=");
        result.append(this.shipmentStoreCode);
        result.append(", shipmentSalesTypeCode=");
        result.append(this.shipmentSalesTypeCode);
        result.append(", deductionCode=");
        result.append(this.deductionCode);
        result.append(", accountCode=");
        result.append(this.accountCode);
        result.append(", ownershipDate=");
        result.append(this.ownershipDate);
        result.append(", cutoffDate=");
        result.append(this.cutoffDate);
        result.append(", payoutDate=");
        result.append(this.payoutDate);
        result.append(", ownershipFlag=");
        result.append(this.ownershipFlag);
        result.append(", cutoffFlag=");
        result.append(this.cutoffFlag);
        result.append(", payoutFlag=");
        result.append(this.payoutFlag);
        result.append(", disposeNo=");
        result.append(this.disposeNo);
        result.append(", disposeDate=");
        result.append(this.disposeDate);
        result.append("}");
        return result.toString();
    }
}