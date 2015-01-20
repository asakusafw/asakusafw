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
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;
import com.asakusafw.vocabulary.operator.MasterSelection;


/**
 * {@link MasterCheckFlowProcessor}に対するテスト演算子。
 */
public abstract class MasterJoinUpdateFlow {

    /**
     * 通常の演算子。
     * @param master マスタ
     * @param model モデル
     */
    @MasterJoinUpdate
    public void simple(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model) {
        System.out.printf("*** Ms: %s, Tx: %s%n", master, model);
        withParameter(master, model, 0);
    }

    /**
     * パラメータつき演算子。
     * @param master マスタ
     * @param model モデル
     * @param parameter 追加パラメータ
     */
    @MasterJoinUpdate
    public void withParameter(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model,
            int parameter) {
        model.setValue((int) master.getSid() + parameter);
    }

    /**
     * セレクタつき演算子。
     * @param master マスタ
     * @param model モデル
     */
    @MasterJoinUpdate(selection = "selector")
    public void selection(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model) {
        withParameter(master, model, 0);
    }

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
