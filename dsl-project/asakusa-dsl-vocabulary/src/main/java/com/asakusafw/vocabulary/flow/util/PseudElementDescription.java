/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.PortDirection;
import com.asakusafw.vocabulary.operator.Checkpoint;
import com.asakusafw.vocabulary.operator.Confluent;
import com.asakusafw.vocabulary.operator.Empty;
import com.asakusafw.vocabulary.operator.Stop;


/**
 * Represents definition of a pseudo-element on the flow DSL.
 */
public class PseudElementDescription implements FlowElementDescription {

    /**
     * The input port name.
     */
    public static final String INPUT_PORT_NAME = "in"; //$NON-NLS-1$

    /**
     * The output port name.
     */
    public static final String OUTPUT_PORT_NAME = "out"; //$NON-NLS-1$

    private String name;

    private final List<FlowElementPortDescription> inputPorts;

    private final List<FlowElementPortDescription> outputPorts;

    private final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> attributes;

    /**
     * Creates a new instance.
     * @param name the element name
     * @param type the data type
     * @param hasInput {@code true} to provide an input port
     * @param hasOutput {@code true} to provide an output port
     * @param attributes the element attributes
     */
    public PseudElementDescription(
            String name,
            Type type,
            boolean hasInput,
            boolean hasOutput,
            FlowElementAttribute... attributes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.inputPorts = create(
                hasInput,
                INPUT_PORT_NAME,
                type,
                PortDirection.INPUT);
        this.outputPorts = create(
                hasOutput,
                OUTPUT_PORT_NAME,
                type,
                PortDirection.OUTPUT);
        this.attributes = new HashMap<>();
        for (FlowElementAttribute attribute : attributes) {
            this.attributes.put(attribute.getDeclaringClass(), attribute);
        }
    }

    private List<FlowElementPortDescription> create(
            boolean doCreate,
            String portName,
            Type portType,
            PortDirection direction) {
        assert portName != null;
        assert direction != null;
        if (doCreate == false) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(new FlowElementPortDescription(
                    portName,
                    portType,
                    direction));
        }
    }

    @Override
    public FlowElementKind getKind() {
        return FlowElementKind.PSEUD;
    }

    @Override
    public FlowElementDescription getOrigin() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public List<FlowElementPortDescription> getInputPorts() {
        return inputPorts;
    }

    @Override
    public List<FlowElementPortDescription> getOutputPorts() {
        return outputPorts;
    }

    @Override
    public List<FlowResourceDescription> getResources() {
        return Collections.emptyList();
    }

    @Override
    public Set<? extends Class<? extends FlowElementAttribute>> getAttributeTypes() {
        return attributes.keySet();
    }

    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException("attributeClass must not be null"); //$NON-NLS-1$
        }
        Object attribute = attributes.get(attributeClass);
        return attributeClass.cast(attribute);
    }

    @Override
    public String toString() {
        Class<? extends Annotation> annotation = analyzePseud();
        return MessageFormat.format(
                "{0}#{1}(@{2})", //$NON-NLS-1$
                CoreOperatorFactory.class.getSimpleName(),
                annotation.getSimpleName().toLowerCase(Locale.ENGLISH),
                annotation.getSimpleName());
    }

    private Class<? extends Annotation> analyzePseud() {
        if (getInputPorts().isEmpty()) {
            return Empty.class;
        }
        if (getOutputPorts().isEmpty()) {
            return Stop.class;
        }
        FlowBoundary boundary = getAttribute(FlowBoundary.class);
        if (boundary == FlowBoundary.STAGE) {
            return Checkpoint.class;
        }
        return Confluent.class;
    }
}
