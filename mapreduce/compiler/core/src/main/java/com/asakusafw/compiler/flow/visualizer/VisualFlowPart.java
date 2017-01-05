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

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.flow.graph.FlowElement;

/**
 * A visual model for flow-parts.
 */
public class VisualFlowPart implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private final FlowElement element;

    private final Set<VisualNode> nodes;

    /**
     * Creates a new instance.
     * @param element the original flow-part
     * @param nodes the element nodes
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public VisualFlowPart(FlowElement element, Set<? extends VisualNode> nodes) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(nodes, "nodes"); //$NON-NLS-1$
        this.element = element;
        this.nodes = Sets.from(nodes);
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
     * Returns the original flow-parts.
     * @return the original flow-parts
     */
    public FlowElement getElement() {
        return element;
    }

    /**
     * Returns the element nodes of this flow-part.
     * @return the element nodes
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
