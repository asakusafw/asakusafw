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
package com.asakusafw.vocabulary.flow.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.asakusafw.vocabulary.flow.Source;

/**
 * Resolves connections of {@link FlowElement}.
 * @since 0.1.0
 * @version 0.4.0
 */
public class FlowElementResolver {

    private final FlowElement element;

    private final Map<String, FlowElementInput> inputPorts;

    private final Map<String, FlowElementOutput> outputPorts;

    /**
     * Creates a new instance.
     * @param element target element.
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public FlowElementResolver(FlowElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        this.element = element;
        this.inputPorts = new HashMap<>();
        this.outputPorts = new HashMap<>();
        for (FlowElementInput input : element.getInputPorts()) {
            inputPorts.put(input.getDescription().getName(), input);
        }
        for (FlowElementOutput output : element.getOutputPorts()) {
            outputPorts.put(output.getDescription().getName(), output);
        }
    }

    /**
     * Creates a new instance.
     * @param description a description of the target {@link FlowElement}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElementResolver(FlowElementDescription description) {
        this(new FlowElement(description));
    }

    /**
     * Returns the target flow element of this resolver.
     * @return the target flow element
     */
    public FlowElement getElement() {
        return element;
    }

    /**
     * Returns the input port of the target flow element.
     * @param name the target port name
     * @return the target port
     * @throws NoSuchElementException if the target flow element does not have such a port
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElementInput getInput(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementInput port = inputPorts.get(name);
        if (port == null) {
            throw new NoSuchElementException(name);
        }
        return port;
    }

    /**
     * Returns the output port of the target flow element.
     * @param name the target port name
     * @return the target port
     * @throws NoSuchElementException if the target flow element does not have such a port
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElementOutput getOutput(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementOutput port = outputPorts.get(name);
        if (port == null) {
            throw new NoSuchElementException(name);
        }
        return port;
    }

    /**
     * Connects between an upstream source and the input port of the flow element.
     * @param name the target port name
     * @param source the upstream source
     * @throws NoSuchElementException if the target flow element does not have such a port
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void resolveInput(String name, Source<?> source) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        FlowElementInput port = getInput(name);
        PortConnection.connect(source.toOutputPort(), port);
    }

    /**
     * Returns the output port of the target flow element as upstream source.
     * @param <T> the data type of the port
     * @param name the target port name
     * @return the target port
     * @throws NoSuchElementException if the target flow element does not have such a port
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> Source<T> resolveOutput(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementOutput port = getOutput(name);
        return new OutputDriver<>(port);
    }

    /**
     * Sets the flow element name.
     * @param name the name
     * @throws UnsupportedOperationException if the target flow element does not support changing its name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementDescription desc = element.getDescription();
        desc.setName(name);
    }

    /**
     * An adapter class that enables {@link FlowElementOutput} performs as {@link Source}.
     * @param <T> the data type
     */
    public static class OutputDriver<T> implements Source<T> {

        private final FlowElementOutput outputPort;

        /**
         * Creates a new instance.
         * @param outputPort the original output port
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public OutputDriver(FlowElementOutput outputPort) {
            if (outputPort == null) {
                throw new IllegalArgumentException("outputPort must not be null"); //$NON-NLS-1$
            }
            this.outputPort = outputPort;
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return outputPort;
        }
    }
}
