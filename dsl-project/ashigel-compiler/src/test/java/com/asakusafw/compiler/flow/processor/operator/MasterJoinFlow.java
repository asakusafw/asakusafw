/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.processor.MasterJoinFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.operator.MasterJoin;
import com.asakusafw.vocabulary.operator.MasterSelection;
import com.asakusafw.vocabulary.operator.Split;


/**
 * {@link MasterJoinFlowProcessor}に対するテスト演算子。
 */
public abstract class MasterJoinFlow {

    /**
     * 結合する。
     * @param ex1 マスタ
     * @param ex2 トラン
     * @return 結合結果
     */
    @MasterJoin
    public abstract ExJoined join(Ex1 ex1, Ex2 ex2);

    /**
     * セレクタつき。
     * @param ex1 マスタ
     * @param ex2 トラン
     * @return 結合結果
     */
    @MasterJoin(selection = "selector")
    public abstract ExJoined selection(Ex1 ex1, Ex2 ex2);

    /**
     * 引数無しのセレクタ。
     * @param masters マスタ一覧
     * @param model 対象のモデル
     * @return 選択したマスタ、利用しない場合は{@code null}
     */
    @MasterSelection
    public Ex1 selector(List<Ex1> masters, Ex2 model) {
        for (Ex1 master : masters) {
            if (master.getStringOption().equals(model.getStringOption())) {
                return master;
            }
        }
        return null;
    }

    /**
     * 分解する。
     * @param joined 結合結果
     * @param ex1 マスタ
     * @param ex2 トラン
     */
    @Split
    public abstract void split(ExJoined joined, Result<Ex1> ex1, Result<Ex2> ex2);
}
