/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.processor.operator;

import com.asakusafw.compiler.flow.processor.BranchFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.operator.Branch;


/**
 * {@link BranchFlowProcessor}に対するテスト演算子。
 */
public abstract class BranchFlow {

    /**
     * 通常の演算子。
     * @param model 対象のモデル
     * @return 分岐先
     */
    @Branch
    public Speed simple(Ex1 model) {
        return withParameter(model, 100);
    }

    /**
     * パラメーター付きの演算子。
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 分岐先
     */
    @Branch
    public Speed withParameter(Ex1 model, int parameter) {
        if (model.getValue() > parameter) {
            return Speed.HIGH;
        }
        if (model.getValue() <= 0) {
            return Speed.STOP;
        }
        return Speed.LOW;
    }

    /**
     * 速度。
     */
    public enum Speed {

        /**
         * 速い。
         */
        HIGH,

        /**
         * 遅い。
         */
        LOW,

        /**
         * 停止。
         */
        STOP,
    }
}
