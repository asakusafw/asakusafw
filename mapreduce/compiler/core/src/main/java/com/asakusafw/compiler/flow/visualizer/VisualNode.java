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

import java.util.UUID;

/**
 * An abstract super interface of visual models about flow elements.
 */
public interface VisualNode {

    /**
     * Returns the node kind.
     * @return the node kind
     */
    Kind getKind();

    /**
     * Returns the ID of this node.
     * @return the node ID
     */
    UUID getId();

    /**
     * Accepts the target visitor and invokes the corresponding visitor method.
     * Accepts and calls back the visitor.
     * @param <C> type of visitor context
     * @param <R> type of visitor result
     * @param <E> type of visitor exception
     * @param context the visitor context
     * @param visitor the visitor to call back
     * @return call back result
     * @throws E if visitor raises an exception
     * @throws IllegalArgumentException if {@code visitor} was {@code null}
     */
    <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E;

    /**
     * Represents a node kind.
     */
    enum Kind {

        /**
         * A graph.
         */
        GRAPH,

        /**
         * A block.
         */
        BLOCK,

        /**
         * A flow-part.
         */
        FLOW_PART,

        /**
         * A generic element.
         */
        ELEMENT,

        /**
         * A label.
         */
        LABEL,
    }
}
