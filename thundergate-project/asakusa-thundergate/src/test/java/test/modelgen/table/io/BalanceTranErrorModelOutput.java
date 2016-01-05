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
package test.modelgen.table.io;
import java.io.IOException;

import javax.annotation.Generated;

import test.modelgen.table.model.BalanceTranError;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * {@link BalanceTranError}をTSVなどのレコード形式で出力する。
 */
@Generated("ModelOutputEmitter:0.0.1")@SuppressWarnings("deprecation") public final class BalanceTranErrorModelOutput
        implements ModelOutput<BalanceTranError> {
    /**
     * 内部で利用するエミッター。
     */
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public BalanceTranErrorModelOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(BalanceTranError model) throws IOException {
        emitter.emit(model.getSidOption());
        emitter.emit(model.getVersionNoOption());
        emitter.emit(model.getRgstDatetimeOption());
        emitter.emit(model.getUpdtDatetimeOption());
        emitter.emit(model.getSellerCodeOption());
        emitter.emit(model.getPreviousCutoffDateOption());
        emitter.emit(model.getCutoffDateOption());
        emitter.emit(model.getNextCutoffDateOption());
        emitter.emit(model.getPayoutDateOption());
        emitter.emit(model.getCarriedOption());
        emitter.emit(model.getPurchaseOption());
        emitter.emit(model.getRtnOption());
        emitter.emit(model.getDiscountOption());
        emitter.emit(model.getTaxOption());
        emitter.emit(model.getPayableOption());
        emitter.emit(model.getMutualOption());
        emitter.emit(model.getReservesOption());
        emitter.emit(model.getCancelOption());
        emitter.emit(model.getPaymentOption());
        emitter.emit(model.getNextPurchaseOption());
        emitter.emit(model.getNextReturnOption());
        emitter.emit(model.getNextDiscountOption());
        emitter.emit(model.getNextTaxOption());
        emitter.emit(model.getErrorCodeOption());
        emitter.emit(model.getPaymentFlagOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}