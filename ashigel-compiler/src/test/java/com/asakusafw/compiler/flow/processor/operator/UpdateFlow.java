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

import com.asakusafw.compiler.flow.processor.UpdateFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.operator.Update;


/**
 * {@link UpdateFlowProcessor}に対するテスト演算子。
 */
public abstract class UpdateFlow {

    /**
     * 通常の演算子。
     * @param model 対象のモデル
     */
    @Update
    public void simple(Ex1 model) {
        withParameter(model, 1);
    }

    /**
     * パラメーター付きの演算子。
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     */
    @Update
    public void withParameter(Ex1 model, int parameter) {
        model.setValue(model.getValue() + parameter);
    }
}
