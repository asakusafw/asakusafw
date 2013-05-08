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
package com.asakusafw.compiler.flow.plan;

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;

/**
 * 実行ステージ間の関係を示したグラフ。
 */
public class StageGraph {

    private FlowBlock input;

    private FlowBlock output;

    private List<StageBlock> stages;

    /**
     * インスタンスを生成する。
     * @param input プログラム全体の入力が含まれるブロック
     * @param output プログラム全体の出力が含まれるブロック
     * @param stages 入力から出力の間に存在するブロックの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StageGraph(
            FlowBlock input,
            FlowBlock output,
            List<StageBlock> stages) {
        Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stages, "stages"); //$NON-NLS-1$
        this.input = input;
        this.output = output;
        this.stages = Lists.from(stages);
    }

    /**
     * ステージグラフへの入力のみを含むブロックを返す。
     * @return ステージグラフへの入力のみを含むブロック
     */
    public FlowBlock getInput() {
        return input;
    }

    /**
     * ステージグラフからの出力のみを含むブロックを返す。
     * @return ステージグラフからの出力のみを含むブロック
     */
    public FlowBlock getOutput() {
        return output;
    }

    /**
     * このステージグラフに含まれるステージブロックの一覧を返す。
     * @return ステージブロックの一覧
     */
    public List<StageBlock> getStages() {
        return stages;
    }
}
