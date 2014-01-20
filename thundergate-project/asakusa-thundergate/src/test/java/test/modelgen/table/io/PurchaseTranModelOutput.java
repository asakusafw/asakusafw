/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package test.modelgen.table.io;
import java.io.IOException;

import javax.annotation.Generated;

import test.modelgen.table.model.PurchaseTran;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * {@link PurchaseTran}をTSVなどのレコード形式で出力する。
 */
@Generated("ModelOutputEmitter:0.0.1")@SuppressWarnings("deprecation") public final class PurchaseTranModelOutput
        implements ModelOutput<PurchaseTran> {
    /**
     * 内部で利用するエミッター。
     */
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public PurchaseTranModelOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(PurchaseTran model) throws IOException {
        emitter.emit(model.getSidOption());
        emitter.emit(model.getVersionNoOption());
        emitter.emit(model.getRgstDatetimeOption());
        emitter.emit(model.getUpdtDatetimeOption());
        emitter.emit(model.getPurchaseNoOption());
        emitter.emit(model.getPurchaseTypeOption());
        emitter.emit(model.getTradeTypeOption());
        emitter.emit(model.getTradeNoOption());
        emitter.emit(model.getLineNoOption());
        emitter.emit(model.getDeliveryDateOption());
        emitter.emit(model.getStoreCodeOption());
        emitter.emit(model.getBuyerCodeOption());
        emitter.emit(model.getPurchaseTypeCodeOption());
        emitter.emit(model.getSellerCodeOption());
        emitter.emit(model.getTenantCodeOption());
        emitter.emit(model.getNetPriceTotalOption());
        emitter.emit(model.getSellingPriceTotalOption());
        emitter.emit(model.getShipmentStoreCodeOption());
        emitter.emit(model.getShipmentSalesTypeCodeOption());
        emitter.emit(model.getDeductionCodeOption());
        emitter.emit(model.getAccountCodeOption());
        emitter.emit(model.getOwnershipDateOption());
        emitter.emit(model.getCutoffDateOption());
        emitter.emit(model.getPayoutDateOption());
        emitter.emit(model.getOwnershipFlagOption());
        emitter.emit(model.getCutoffFlagOption());
        emitter.emit(model.getPayoutFlagOption());
        emitter.emit(model.getDisposeNoOption());
        emitter.emit(model.getDisposeDateOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}