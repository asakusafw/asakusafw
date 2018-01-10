/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

/**
 * Represents a kind of {@link FlowElement}.
 */
public enum FlowElementKind {

    /**
     * A flow input.
     * The {@link FlowElement#getDescription()} must returns {@link InputDescription}.
     */
    INPUT,

    /**
     * A flow output.
     * The {@link FlowElement#getDescription()} must returns {@link OutputDescription}.
     */
    OUTPUT,

    /**
     * An operator.
     * The {@link FlowElement#getDescription()} must returns {@link OperatorDescription}.
     */
    OPERATOR,

    /**
     * A flow-part.
     * The {@link FlowElement#getDescription()} must returns {@link FlowPartDescription}.
     */
    FLOW_COMPONENT,

    /**
     * A pseudo-element.
     */
    PSEUD,
}
