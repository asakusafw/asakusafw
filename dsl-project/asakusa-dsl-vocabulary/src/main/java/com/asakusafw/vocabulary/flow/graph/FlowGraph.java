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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.vocabulary.flow.FlowDescription;


/**
 * フローの構造を表すグラフ。
 * @since 0.1.0
 * @version 0.2.6
 */
public class FlowGraph {

    private final Class<? extends FlowDescription> description;

    private final List<FlowIn<?>> flowInputs;

    private final List<FlowOut<?>> flowOutputs;

    private FlowGraph origin;

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
        this.origin = this;
    }

    /**
     * このフローグラフの元の情報を設定する。
     * @param origin このフローグラフの元の情報
     * @since 0.2.6
     */
    public void setOrigin(FlowGraph origin) {
        if (origin == null) {
            this.origin = this;
        } else {
            this.origin = origin;
        }
    }

    /**
     * このフローグラフの元の情報を返す。
     * @return このフローグラフの元の情報
     * @since 0.2.6
     */
    public FlowGraph getOrigin() {
        return origin;
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
