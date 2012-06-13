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

import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.FlowElement;

/**
 * 可視化用のフロー要素。
 */
public class VisualElement implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private FlowElement element;

    /**
     * インスタンスを生成する。
     * @param element 対象の要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public VisualElement(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        this.element = element;
    }

    @Override
    public Kind getKind() {
        return Kind.ELEMENT;
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

    @Override
    public <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E {
        Precondition.checkMustNotBeNull(visitor, "visitor"); //$NON-NLS-1$
        R result = visitor.visitElement(context, this);
        return result;
    }
}
