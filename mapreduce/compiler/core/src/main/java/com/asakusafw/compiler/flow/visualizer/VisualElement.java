/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.FlowElement;

/**
 * A visual model for flow elements.
 */
public class VisualElement implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private final FlowElement element;

    /**
     * Creates a new instance.
     * @param element the target element
     * @throws IllegalArgumentException if the element is {@code null}
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
     * Returns the original flow element.
     * @return the original flow element
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

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})", //$NON-NLS-1$
                getClass().getSimpleName(),
                element);
    }
}
