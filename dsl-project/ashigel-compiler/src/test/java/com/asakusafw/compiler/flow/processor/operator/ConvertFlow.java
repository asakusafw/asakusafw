/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.processor.ConvertFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.operator.Convert;


/**
 * {@link ConvertFlowProcessor}に対するテスト演算子。
 */
public abstract class ConvertFlow {

    private Ex2 ex2 = new Ex2();

    /**
     * 通常の演算子。
     * @param model 対象のモデル
     * @return 変換結果
     */
    @Convert
    public Ex2 simple(Ex1 model) {
        return withParameter(model, 1);
    }

    /**
     * パラメーター付きの演算子。
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 変換結果
     */
    @Convert
    public Ex2 withParameter(Ex1 model, int parameter) {
        ex2.setValue(model.getValue() + parameter);
        return ex2;
    }
}
