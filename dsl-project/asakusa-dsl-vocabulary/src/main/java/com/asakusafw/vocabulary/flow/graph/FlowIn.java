/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.vocabulary.flow.In;

/**
 * Represents an input of flow graph.
 * @param <T> the data type
 */
public final class FlowIn<T> implements In<T> {

    private final InputDescription description;

    private final FlowElementResolver resolver;

    /**
     * Creates a new instance.
     * @param description the description
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowIn(InputDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        this.description = description;
        this.resolver = new FlowElementResolver(description);
    }

    /**
     * Creates a new instance.
     * @param <T> the data type
     * @param description the description
     * @return the created instance
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <T> FlowIn<T> newInstance(InputDescription description) {
        return new FlowIn<T>(description);
    }

    /**
     * Returns the description of this input.
     * @return the description
     */
    public InputDescription getDescription() {
        return description;
    }

    /**
     * Returns the corresponding {@link FlowElement} object.
     * @return the corresponding {@link FlowElement} object
     */
    public FlowElement getFlowElement() {
        return resolver.getElement();
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return resolver.getOutput(InputDescription.OUTPUT_PORT_NAME);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})", //$NON-NLS-1$
                getDescription(),
                getFlowElement());
    }
}
