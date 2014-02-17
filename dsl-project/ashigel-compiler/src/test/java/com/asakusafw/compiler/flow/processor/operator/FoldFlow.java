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
package com.asakusafw.compiler.flow.processor.operator;

import com.asakusafw.compiler.flow.processor.FoldFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.Fold;


/**
 * {@link FoldFlowProcessor}に対するテスト演算子。
 */
public abstract class FoldFlow {

    /**
     * 通常の演算子。
     * @param context 畳み込み先
     * @param right 畳み込む値
     */
    @Fold
    public void simple(@Key(group = "string") Ex1 context, Ex1 right) {
        withParameter(context, right, 0);
    }

    /**
     * 引数つきの演算子。
     * @param context 畳み込み先
     * @param right 畳み込む値
     * @param parameter 追加パラメータ
     */
    @Fold
    public void withParameter(@Key(group = "string") Ex1 context, Ex1 right, int parameter) {
        context.getValueOption().add(right.getValueOption().or(0) + parameter);
    }
}
