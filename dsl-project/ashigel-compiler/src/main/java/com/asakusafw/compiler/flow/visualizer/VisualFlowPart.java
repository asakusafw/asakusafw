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
import com.asakusafw.vocabulary.flow.graph.FlowElement;

/**
 * 可視化用のフロー部品。
 */
public class VisualFlowPart implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private final FlowElement element;

    private final Set<VisualNode> nodes;

    /**
     * インスタンスを生成する。
     * @param element このフロー部品を表す要素
     * @param nodes このフロー部品に含まれるノード一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public VisualFlowPart(FlowElement element, Set<? extends VisualNode> nodes) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(nodes, "nodes"); //$NON-NLS-1$
        this.element = element;
        this.nodes = new HashSet<VisualNode>(nodes);
    }

    @Override
    public Kind getKind() {
        return Kind.FLOW_PART;
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * この要素の元となるフロー要素を返す。
     * @return この要素の元となるフロー要素
     */
    public FlowElement getElement() {
        return element;
    }

    /**
     * このフロー部品に含まれるノード一覧を返す。
     * @return このフロー部品に含まれるノード一覧
     */
    public Set<VisualNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E {
        Precondition.checkMustNotBeNull(visitor, "visitor"); //$NON-NLS-1$
        R result = visitor.visitFlowPart(context, this);
        return result;
    }

}
