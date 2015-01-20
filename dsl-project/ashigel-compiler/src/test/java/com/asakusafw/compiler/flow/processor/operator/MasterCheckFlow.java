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

import java.util.List;

import com.asakusafw.compiler.flow.processor.MasterCheckFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.MasterCheck;
import com.asakusafw.vocabulary.operator.MasterSelection;


/**
 * {@link MasterCheckFlowProcessor}に対するテスト演算子。
 */
public abstract class MasterCheckFlow {

    /**
     * 通常の演算子。
     * @param master マスタ
     * @param model モデル
     * @return 引き当て結果
     */
    @MasterCheck
    public abstract boolean simple(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model);

    /**
     * セレクタつき演算子。
     * @param master マスタ
     * @param model モデル
     * @return 引き当て結果
     */
    @MasterCheck(selection = "selector")
    public abstract boolean selection(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model);

    /**
     * 引数無しのセレクタ。
     * @param masters マスタ一覧
     * @param model 対象のモデル
     * @return 選択したマスタ、利用しない場合は{@code null}
     */
    @MasterSelection
    public Ex2 selector(List<Ex2> masters, Ex1 model) {
        for (Ex2 master : masters) {
            if (master.getValueOption().equals(model.getValueOption())) {
                return master;
            }
        }
        return null;
    }
}
