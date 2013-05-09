/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.processor.MasterBranchFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.MasterBranch;
import com.asakusafw.vocabulary.operator.MasterSelection;


/**
 * {@link MasterBranchFlowProcessor}に対するテスト演算子。
 */
public abstract class MasterBranchFlow {

    /**
     * 通常の演算子。
     * @param master マスタ
     * @param model 対象のモデル
     * @return 分岐先
     */
    @MasterBranch
    public Speed simple(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model) {
        return withParameter(master, model, 30);
    }

    /**
     * パラメーター付きの演算子。
     * @param master マスタ
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 分岐先
     */
    @MasterBranch
    public Speed withParameter(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model,
            int parameter) {
        if (master == null) {
            return Speed.STOP;
        }
        if (master.getValue() + model.getValue() > parameter) {
            return Speed.HIGH;
        }
        if (master.getValue() + model.getValue()  <= 0) {
            return Speed.STOP;
        }
        return Speed.LOW;
    }

    /**
     * セレクタつきの演算子。
     * @param master マスタ
     * @param model 対象のモデル
     * @return 分岐先
     */
    @MasterBranch(selection = "selector")
    public Speed selection(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model) {
        return withParameter(master, model, 30);
    }

    /**
     * セレクタおよびパラメータつきの演算子 (セレクタのパラメータはなし)。
     * @param master マスタ
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 分岐先
     */
    @MasterBranch(selection = "selector")
    public Speed selectionWithParameter0(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model,
            int parameter) {
        return withParameter(master, model, parameter);
    }

    /**
     * セレクタおよびパラメータつきの演算子 (セレクタのパラメータつき)。
     * @param master マスタ
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 分岐先
     */
    @MasterBranch(selection = "selectorWithParameter")
    public Speed selectionWithParameter1(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model,
            int parameter) {
        return withParameter(master, model, parameter);
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

    /**
     * 引数つきのセレクタ。
     * @param masters マスタ一覧
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 選択したマスタ、利用しない場合は{@code null}
     */
    @MasterSelection
    public Ex2 selectorWithParameter(List<Ex2> masters, Ex1 model, int parameter) {
        for (Ex2 master : masters) {
            if (master.getValueOption().has(parameter)) {
                return master;
            }
        }
        return null;
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
