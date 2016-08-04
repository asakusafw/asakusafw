/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.builder;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;

/**
 * Helper object for {@link FlowElement}.
 * @since 0.9.0
 */
public class FlowElementEditor {

    private final Map<String, FlowElementInput> inputs = new HashMap<>();

    private final Map<String, FlowElementOutput> outputs = new HashMap<>();

    /**
     * Creates a new instance.
     * @param element target element
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElementEditor(FlowElement element) {
        Objects.requireNonNull(element, "element must not be null"); //$NON-NLS-1$
        for (FlowElementInput port : element.getInputPorts()) {
            inputs.put(port.getDescription().getName(), port);
        }
        for (FlowElementOutput port : element.getOutputPorts()) {
            outputs.put(port.getDescription().getName(), port);
        }
    }

    /**
     * Returns an input port.
     * @param name output name
     * @return the input port
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElementInput getInput(String name) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        FlowElementInput port = inputs.get(name);
        if (port == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid input name: {0}",
                    name));
        }
        return port;
    }

    /**
     * Returns an output port.
     * @param name output name
     * @return the output port
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElementOutput getOutput(String name) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        FlowElementOutput port = outputs.get(name);
        if (port == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid output name: {0}",
                    name));
        }
        return port;
    }

    /**
     * Returns the {@link Source} object which represents output port of the target flow.
     * @param <T> data type
     * @param name output port name
     * @param typeRef other source which has the type as same as target output
     * @return the source object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> Source<T> createSource(String name, Source<T> typeRef) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(typeRef, "typeRef must not be null"); //$NON-NLS-1$
        FlowElementOutput port = getOutput(name);
        return toSource(port, typeRef.toOutputPort().getDescription().getDataType());
    }

    /**
     * Returns the {@link Source} object which represents output port of the target flow.
     * @param <T> data type
     * @param name output port name
     * @param type target output type
     * @return the source object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> Source<T> createSource(String name, Class<T> type) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        FlowElementOutput port = getOutput(name);
        return toSource(port, type);
    }

    private <T> Source<T> toSource(final FlowElementOutput port, java.lang.reflect.Type type) {
        assert port != null;
        assert type != null;
        FlowElementPortDescription description = port.getDescription();
        if (description.getDataType() != type) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid data type: name={0}, expected={1}, actual={2}",
                    description.getName(),
                    description.getDataType(),
                    type));
        }
        return new Source<T>() {
            @Override
            public FlowElementOutput toOutputPort() {
                return port;
            }
            @Override
            public String toString() {
                return port.toString();
            }
        };
    }
}
