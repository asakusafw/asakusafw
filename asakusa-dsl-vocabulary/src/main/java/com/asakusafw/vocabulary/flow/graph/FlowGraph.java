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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.vocabulary.flow.FlowDescription;


/**
 * フローの構造を表すグラフ。
 */
public class FlowGraph {

    private Class<? extends FlowDescription> description;

    private List<FlowIn<?>> flowInputs;

    private List<FlowOut<?>> flowOutputs;

    /**
     * インスタンスを生成する。
     * @param description このフローを記述するクラス
     * @param flowInputs フローへの入力一覧
     * @param flowOutputs フローからの出力一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowGraph(
            Class<? extends FlowDescription> description,
            List<? extends FlowIn<?>> flowInputs,
            List<? extends FlowOut<?>> flowOutputs) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (flowInputs == null) {
            throw new IllegalArgumentException("flowInputs must not be null"); //$NON-NLS-1$
        }
        if (flowOutputs == null) {
            throw new IllegalArgumentException("flowOutputs must not be null"); //$NON-NLS-1$
        }
        this.description = description;
        this.flowInputs = Collections.unmodifiableList(new ArrayList<FlowIn<?>>(flowInputs));
        this.flowOutputs = Collections.unmodifiableList(new ArrayList<FlowOut<?>>(flowOutputs));
    }

    /**
     * このフローを記述するクラスを返す。
     * @return このフローを記述するクラス
     */
    public Class<? extends FlowDescription> getDescription() {
        return description;
    }

    /**
     * このグラフへの入力として利用される要素の一覧を返す。
     * @return このグラフへの入力として利用される要素の一覧
     */
    public List<FlowIn<?>> getFlowInputs() {
        return flowInputs;
    }

    /**
     * このグラフからの出力として利用される要素の一覧を返す。
     * @return このグラフからの出力として利用される要素の一覧
     */
    public List<FlowOut<?>> getFlowOutputs() {
        return flowOutputs;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "FlowGraph({0})",
                getDescription().getName(),
                getFlowInputs(),
                getFlowOutputs());
    }
}
