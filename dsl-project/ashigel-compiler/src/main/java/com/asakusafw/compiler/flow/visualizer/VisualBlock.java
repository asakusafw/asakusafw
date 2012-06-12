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
package com.asakusafw.compiler.flow.visualizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.plan.FlowBlock;

/**
 * 可視化用のグラフ。
 */
public class VisualBlock implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private final String label;

    private final Set<FlowBlock.Input> inputs;

    private final Set<FlowBlock.Output> outputs;

    private final Set<VisualNode> nodes;

    /**
     * インスタンスを生成する。
     * @param label このグラフのラベル (省略可)
     * @param inputs ブロックへの入力一覧
     * @param outputs ブロックへの出力一覧
     * @param nodes このグラフに含まれるノード一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public VisualBlock(
            String label,
            Set<FlowBlock.Input> inputs,
            Set<FlowBlock.Output> outputs,
            Set<? extends VisualNode> nodes) {
        Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(nodes, "nodes"); //$NON-NLS-1$
        this.label = label;
        this.inputs = new HashSet<FlowBlock.Input>(inputs);
        this.outputs = new HashSet<FlowBlock.Output>(outputs);
        this.nodes = new HashSet<VisualNode>(nodes);
    }

    /**
     * このブロックのラベルを返す。
     * @return このブロックのラベル、省略された場合は{@code null}
     */
    public String getLabel() {
        return label;
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * ブロックの入力一覧を返す。
     * @return ブロックの入力一覧
     */
    public Set<FlowBlock.Input> getInputs() {
        return inputs;
    }

    /**
     * ブロックの出力一覧を返す。
     * @return ブロックの出力一覧
     */
    public Set<FlowBlock.Output> getOutputs() {
        return outputs;
    }

    /**
     * このグラフに含まれるノード一覧を返す。
     * @return このグラフに含まれるノード一覧
     */
    public Set<VisualNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public Kind getKind() {
        return Kind.BLOCK;
    }

    @Override
    public <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E {
        Precondition.checkMustNotBeNull(visitor, "visitor"); //$NON-NLS-1$
        R result = visitor.visitBlock(context, this);
        return result;
    }
}
