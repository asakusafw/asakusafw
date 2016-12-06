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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.PortConnection;

/**
 * Builds operator graphs.
 * @since 0.9.0
 * @version 0.9.1
 */
public abstract class FlowElementBuilder {

    private static final FlowElementAttribute[] EMPTY_ATTRS = new FlowElementAttribute[0];

    private final List<PortInfo> inputs = new ArrayList<>();

    private final List<PortInfo> outputs = new ArrayList<>();

    private final List<DataInfo> args = new ArrayList<>();

    private final List<AttributeInfo> attrs = new ArrayList<>();

    private final Map<String, FlowElementOutput> inputMapping = new HashMap<>();

    /**
     * Creates a new instance for operator method.
     * @param annotationType operator annotation type.
     * @param operatorClass operator class
     * @param methodName operator method name
     * @param methodParameterTypes the operator method parameter types
     * @return created builder
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FlowElementBuilder createOperator(
            Class<? extends Annotation> annotationType,
            Class<?> operatorClass,
            String methodName,
            Class<?>... methodParameterTypes) {
        return new OperatorNodeBuilder(
                annotationType, operatorClass, operatorClass, methodName, methodParameterTypes);
    }

    /**
     * Creates a new instance for operator method.
     * @param annotationType operator annotation type.
     * @param operatorClass operator class
     * @param implementationClass operator implementation class
     * @param methodName operator method name
     * @param methodParameterTypes the operator method parameter types
     * @return created builder
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FlowElementBuilder createOperator(
            Class<? extends Annotation> annotationType,
            Class<?> operatorClass,
            Class<?> implementationClass,
            String methodName,
            Class<?>... methodParameterTypes) {
        return new OperatorNodeBuilder(
                annotationType, operatorClass, implementationClass, methodName, methodParameterTypes);
    }

    /**
     * Creates a new instance for flow description class.
     * @param flowDescriptionClass flow description class
     * @param constructorParameterTypes constructor parameter types
     * @return created builder
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FlowElementBuilder createFlow(
            Class<? extends FlowDescription> flowDescriptionClass,
            Class<?>... constructorParameterTypes) {
        return new FlowNodeBuilder(flowDescriptionClass, constructorParameterTypes);
    }

    /**
     * Defines a new input for operator.
     * @param name input name
     * @param upstream upstream dataset
     * @return defined port information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PortInfo defineInput(String name, Source<?> upstream) {
        return defineInput(name, upstream, EMPTY_ATTRS);
    }

    /**
     * Defines a new input for operator.
     * @param name input name
     * @param upstream upstream dataset
     * @param attributes the port attributes
     * @return defined port information
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.1
     */
    public PortInfo defineInput(String name, Source<?> upstream, FlowElementAttribute... attributes) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(upstream, "upstream must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(attributes);
        FlowElementOutput output = upstream.toOutputPort();
        Type type = getType(output);
        PortInfo info = defineInput0(name, type, attributes);
        inputMapping.put(name, output);
        return info;
    }

    private PortInfo defineInput0(String name, Type type, FlowElementAttribute... attributes) {
        assert name != null;
        assert type != null;
        assert attributes != null;
        PortInfo info = new PortInfo(PortInfo.Direction.INPUT, name, type, Arrays.asList(attributes));
        inputs.add(info);
        return info;
    }

    private static Type getType(FlowElementOutput output) {
        return output.getDescription().getDataType();
    }

    /**
     * Defines a new output for operator.
     * @param name output name
     * @param type output type
     * @return defined port information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PortInfo defineOutput(String name, Type type) {
        return defineOutput(name, type, EMPTY_ATTRS);
    }

    /**
     * Defines a new output for operator.
     * @param name output name
     * @param typeRef output type reference
     * @return defined port information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PortInfo defineOutput(String name, Source<?> typeRef) {
        return defineOutput(name, typeRef, EMPTY_ATTRS);
    }

    /**
     * Defines a new output for operator.
     * @param name output name
     * @param type output type
     * @param attributes the port attributes
     * @return defined port information
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.1
     */
    public PortInfo defineOutput(String name, Type type, FlowElementAttribute... attributes) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(attributes);
        return defineOutput0(name, type, attributes);
    }

    /**
     * Defines a new output for operator.
     * @param name output name
     * @param typeRef output type reference
     * @param attributes the port attributes
     * @return defined port information
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.1
     */
    public PortInfo defineOutput(String name, Source<?> typeRef, FlowElementAttribute... attributes) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(typeRef, "typeRef must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(attributes);
        return defineOutput0(name, getType(typeRef.toOutputPort()), attributes);
    }

    private PortInfo defineOutput0(String name, Type type, FlowElementAttribute... attributes) {
        assert name != null;
        assert type != null;
        assert attributes != null;
        PortInfo info = new PortInfo(PortInfo.Direction.OUTPUT, name, type, Arrays.asList(attributes));
        outputs.add(info);
        return info;
    }

    /**
     * Defines a new data for operator.
     * @param name the argument name
     * @param data data representation
     * @return defined data information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataInfo defineData(String name, Data data) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(data, "data must not be null"); //$NON-NLS-1$
        DataInfo info = new DataInfo(name, data);
        args.add(info);
        return info;
    }

    /**
     * Defines a new argument for operator.
     * @param name the argument name
     * @param type the value type
     * @param value the constant value
     * @return defined data information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataInfo defineData(String name, Type type, Object value) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        return defineData(name, new Constant(type, value));
    }

    /**
     * Defines an attribute.
     * @param attribute the attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void defineAttribute(Object attribute) {
        Objects.requireNonNull(attribute, "attribute must not be null"); //$NON-NLS-1$
        attrs.add(new AttributeInfo(attribute));
    }

    /**
     * Resolves current operator input/output/arguments.
     * @return the resolved information
     * @throws IllegalStateException if failed to resolve the operator
     */
    public FlowElementEditor resolve() {
        FlowElement resolved = new FlowElement(build(inputs, outputs, args, attrs));
        FlowElementEditor editor = new FlowElementEditor(resolved);
        for (Map.Entry<String, FlowElementOutput> entry : inputMapping.entrySet()) {
            FlowElementOutput upstream = entry.getValue();
            FlowElementInput downstream = editor.getInput(entry.getKey());
            PortConnection.connect(upstream, downstream);
        }
        return editor;
    }

    /**
     * Builds a flow from operator input/output/arguments.
     * @param inputPorts list of operator input
     * @param outputPorts list of operator output
     * @param arguments list of operator argument
     * @param attributes list of operator attribute
     * @return the resolved information
     * @throws IllegalStateException if failed to resolve the operator
     */
    protected abstract FlowElementDescription build(
            List<PortInfo> inputPorts,
            List<PortInfo> outputPorts,
            List<DataInfo> arguments,
            List<AttributeInfo> attributes);
}
