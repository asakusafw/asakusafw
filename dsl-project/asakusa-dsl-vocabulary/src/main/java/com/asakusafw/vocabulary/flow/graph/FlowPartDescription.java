/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Source;

/**
 * A description of flow-part.
 * @since 0.1.0
 * @version 0.9.1
 */
public class FlowPartDescription implements FlowElementDescription {

    private static final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> ATTRIBUTES;
    static {
        Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> map = new HashMap<>();
        map.put(FlowBoundary.class, FlowBoundary.STAGE);
        ATTRIBUTES = Collections.unmodifiableMap(map);
    }

    private final FlowGraph flowGraph;

    private final List<FlowElementPortDescription> inputPorts;

    private final List<FlowElementPortDescription> outputPorts;

    private final List<Parameter> parameters;

    private String name;

    /**
     * Creates a new instance.
     * @param flowGraph the flow-graph which represents this flow-part structure
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowPartDescription(FlowGraph flowGraph) {
        this(flowGraph, Collections.emptyList());
    }

    /**
     * Creates a new instance.
     * @param flowGraph the flow-graph which represents this flow-part structure
     * @param parameters the parameters for this flow-part
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.0
     */
    public FlowPartDescription(FlowGraph flowGraph, List<? extends Parameter> parameters) {
        if (flowGraph == null) {
            throw new IllegalArgumentException("flowGraph must not be null"); //$NON-NLS-1$
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null"); //$NON-NLS-1$
        }
        this.flowGraph = flowGraph;
        List<FlowElementPortDescription> inputs = new ArrayList<>();
        List<FlowElementPortDescription> outputs = new ArrayList<>();
        this.inputPorts = Collections.unmodifiableList(inputs);
        this.outputPorts = Collections.unmodifiableList(outputs);
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));

        for (FlowIn<?> in : flowGraph.getFlowInputs()) {
            inputs.add(new FlowElementPortDescription(
                    in.getDescription().getName(),
                    in.getDescription().getDataType(),
                    PortDirection.INPUT));
        }
        for (FlowOut<?> out : flowGraph.getFlowOutputs()) {
            outputs.add(new FlowElementPortDescription(
                    out.getDescription().getName(),
                    out.getDescription().getDataType(),
                    PortDirection.OUTPUT));
        }
    }

    /**
     * Returns the flow-graph which represents this flow-part structure.
     * @return the flow-graph
     */
    public FlowGraph getFlowGraph() {
        return flowGraph;
    }

    /**
     * Returns the parameters for this flow-part.
     * @return the parameters
     * @since 0.5.0
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public FlowElementKind getKind() {
        return FlowElementKind.FLOW_COMPONENT;
    }

    @Override
    public FlowElementDescription getOrigin() {
        return this;
    }

    @Override
    public String getName() {
        if (name == null) {
            return flowGraph.getDescription().getSimpleName();
        }
        return name;
    }

    @Override
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.name = newName;
    }

    @Override
    public List<FlowElementPortDescription> getInputPorts() {
        return inputPorts;
    }

    @Override
    public List<FlowElementPortDescription> getOutputPorts() {
        return outputPorts;
    }

    /**
     * Returns the flow-inside input ports that individually corresponded to each flow-outside input port.
     * Note that each flow-inside port is a member of {@link #getFlowGraph() flow graph}, on the other hand,
     * each flow-outside port is a member of {@link #getInputPorts() input ports} of this element.
     * @param externalInput flow-outside port
     * @return the corresponded flow-inside port
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowIn<?> getInternalInputPort(FlowElementPortDescription externalInput) {
        if (externalInput == null) {
            throw new IllegalArgumentException("externalInput must not be null"); //$NON-NLS-1$
        }
        assert inputPorts.size() == flowGraph.getFlowInputs().size();
        int index = inputPorts.indexOf(externalInput);
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        return flowGraph.getFlowInputs().get(index);
    }

    /**
     * Returns the flow-inside outputs ports that individually corresponded to each flow-outside output port.
     * Note that each flow-inside port is a member of {@link #getFlowGraph() flow graph}, on the other hand,
     * each flow-outside port is a member of {@link #getOutputPorts() output ports} of this element.
     * @param externalOutput flow-outside port
     * @return the corresponded flow-inside port
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowOut<?> getInternalOutputPort(FlowElementPortDescription externalOutput) {
        if (externalOutput == null) {
            throw new IllegalArgumentException("externalOutput must not be null"); //$NON-NLS-1$
        }
        assert outputPorts.size() == flowGraph.getFlowOutputs().size();
        int index = outputPorts.indexOf(externalOutput);
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        return flowGraph.getFlowOutputs().get(index);
    }

    @Override
    public List<FlowResourceDescription> getResources() {
        return Collections.emptyList();
    }

    @Override
    public Set<? extends Class<? extends FlowElementAttribute>> getAttributeTypes() {
        return ATTRIBUTES.keySet();
    }

    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException("attributeClass must not be null"); //$NON-NLS-1$
        }
        Object attribute = ATTRIBUTES.get(attributeClass);
        return attributeClass.cast(attribute);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Represents a parameter and its argument for flow-part.
     * @since 0.5.0
     */
    public static class Parameter {

        private final String name;

        private final Type type;

        private final Object value;

        /**
         * Creates a new instance.
         * @param name the parameter name
         * @param type the parameter type
         * @param value the argument value (nullable)
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Parameter(String name, Type type, Object value) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (type == null) {
                throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
            }
            this.name = name;
            this.type = type;
            this.value = value;
        }

        /**
         * Returns the parameter name.
         * @return the parameter name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the parameter type.
         * @return the parameter type
         */
        public Type getType() {
            return type;
        }

        /**
         * Returns the argument value.
         * @return the argument value (nullable)
         */
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}[{1}]={2}", //$NON-NLS-1$
                    getName(),
                    getType(),
                    getValue());
        }
    }

    /**
     * A builder for building {@link FlowPartDescription}.
     * @since 0.1.0
     * @version 0.5.0
     */
    public static class Builder {

        private final Class<? extends FlowDescription> declaring;

        private final List<FlowIn<?>> flowInputs;

        private final List<FlowOut<?>> flowOutputs;

        private final List<Parameter> parameters;

        /**
         * Creates a new instance.
         * @param declaring the class declaring the target flow-part
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Builder(Class<? extends FlowDescription> declaring) {
            if (declaring == null) {
                throw new IllegalArgumentException("declaring must not be null"); //$NON-NLS-1$
            }
            this.declaring = declaring;
            this.flowInputs = new ArrayList<>();
            this.flowOutputs = new ArrayList<>();
            this.parameters = new ArrayList<>();
        }

        /**
         * Add a new input port into the target flow-part.
         * @param <T> the data type
         * @param name the port name
         * @param type the data type
         * @return the {@link FlowIn} object that represents the defined port
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public <T> FlowIn<T> addInput(String name, Type type) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (type == null) {
                throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
            }
            FlowIn<T> in = new FlowIn<>(new InputDescription(name, type));
            flowInputs.add(in);
            return in;
        }

        /**
         * Add a new output port into the target flow-part.
         * @param <T> the data type
         * @param name the port name
         * @param type the data type
         * @return the {@link FlowOut} object that represents the defined port
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public <T> FlowOut<T> addOutput(String name, Type type) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (type == null) {
                throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
            }
            FlowOut<T> out = new FlowOut<>(new OutputDescription(name, type));
            flowOutputs.add(out);
            return out;
        }


        /**
         * Add a new input port into the target flow-part.
         * @param <T> the data type
         * @param name the port name
         * @param typeReference a source that has the data type as same to the creating port
         * @return the {@link FlowIn} object that represents the defined port
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public <T> FlowIn<T> addInput(String name, Source<T> typeReference) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (typeReference == null) {
                throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
            }
            Type type = typeReference.toOutputPort().getDescription().getDataType();
            return addInput(name, type);
        }

        /**
         * Add a new output port into the target flow-part.
         * @param <T> the data type
         * @param name the port name
         * @param typeReference a source that has the data type as same to the creating port
         * @return the {@link FlowOut} object that represents the defined port
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public <T> FlowOut<T> addOutput(String name, Source<T> typeReference) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (typeReference == null) {
                throw new IllegalArgumentException("typeReference must not be null"); //$NON-NLS-1$
            }
            Type type = typeReference.toOutputPort().getDescription().getDataType();
            return addOutput(name, type);
        }

        /**
         * Adds a parameter and its argument value for this flow-part.
         * @param parameterName the parameter name
         * @param parameterType the parameter value
         * @param argument the actual parameter argument, or {@code null} if the argument is just {@code null}
         * @throws IllegalArgumentException if some parameters were {@code null}
         * @since 0.5.0
         */
        public void addParameter(String parameterName, Type parameterType, Object argument) {
            if (parameterName == null) {
                throw new IllegalArgumentException("parameterName must not be null"); //$NON-NLS-1$
            }
            if (parameterType == null) {
                throw new IllegalArgumentException("parameterType must not be null"); //$NON-NLS-1$
            }
            parameters.add(new Parameter(parameterName, parameterType, argument));
        }

        /**
         * Creates a new description object from the previously information.
         * @return the created description
         */
        public FlowPartDescription toDescription() {
            FlowGraph graph = new FlowGraph(declaring, flowInputs, flowOutputs);
            return new FlowPartDescription(graph);
        }

        /**
         * Creates a new {@link FlowElementResolver} object from the previously information.
         * @param desc the original flow description object
         * @return the created object
         */
        public FlowElementResolver toResolver(FlowDescription desc) {
            if (desc == null) {
                throw new IllegalArgumentException("desc must not be null"); //$NON-NLS-1$
            }
            desc.start();
            return new FlowElementResolver(toDescription());
        }
    }
}
