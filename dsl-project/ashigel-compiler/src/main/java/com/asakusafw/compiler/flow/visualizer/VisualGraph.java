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
import java.util.Set;
import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;

/**
 * 可視化用のグラフ。
 */
public class VisualGraph implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private final String label;

    private final Set<VisualNode> nodes;

    /**
     * インスタンスを生成する。
     * @param label このグラフのラベル、省略する場合は{@code null}
     * @param nodes このグラフに含まれるノード一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public VisualGraph(String label, Set<? extends VisualNode> nodes) {
        Precondition.checkMustNotBeNull(nodes, "nodes"); //$NON-NLS-1$
        this.label = label;
        this.nodes = Sets.from(nodes);
    }

    /**
     * このグラフのラベルを返す。
     * @return このグラフのラベル、省略された場合は{@code null}
     */
    public String getLabel() {
        return label;
    }

    @Override
    public UUID getId() {
        return id;
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
        return Kind.GRAPH;
    }

    @Override
    public <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E {
        Precondition.checkMustNotBeNull(visitor, "visitor"); //$NON-NLS-1$
        R result = visitor.visitGraph(context, this);
        return result;
    }
}
