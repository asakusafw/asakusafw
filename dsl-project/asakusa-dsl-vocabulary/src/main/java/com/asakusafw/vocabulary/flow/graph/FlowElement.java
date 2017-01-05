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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a node in flow graph.
 * Application developers should not use this class directly.
 * @since 0.1.0
 * @version 0.9.1
 */
public final class FlowElement implements FlowElementAttributeProvider {

    private final Object identity;

    private final FlowElementDescription description;

    private final List<FlowElementInput> inputPorts;

    private final List<FlowElementOutput> outputPorts;

    private final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> attributeOverride;

    /**
     * Creates a new instance.
     * @param description definition description
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElement(FlowElementDescription description) {
        this(new Object(), description, Collections.emptyList());
    }

    /**
     * Creates a new instance.
     * @param description definition description
     * @param attributeOverride extra attributes for this element
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowElement(
            FlowElementDescription description,
            Collection<? extends FlowElementAttribute> attributeOverride) {
        this(new Object(), description, attributeOverride);
    }

    private FlowElement(
            Object identity,
            FlowElementDescription description,
            Collection<? extends FlowElementAttribute> attributeOverride) {
        assert identity != null;
        assert description != null;
        assert attributeOverride != null;
        this.identity = identity;
        this.description = description;
        this.inputPorts = new ArrayList<>();
        for (FlowElementPortDescription port : description.getInputPorts()) {
            if (port.getDirection() != PortDirection.INPUT) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0} must be an INPUT port", //$NON-NLS-1$
                        port));
            }
            inputPorts.add(new FlowElementInput(port, this));
        }
        this.outputPorts = new ArrayList<>();
        for (FlowElementPortDescription port : description.getOutputPorts()) {
            if (port.getDirection() != PortDirection.OUTPUT) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0} must be an OUTPUT port", //$NON-NLS-1$
                        port));
            }
            outputPorts.add(new FlowElementOutput(port, this));
        }
        this.attributeOverride = new HashMap<>();
        for (FlowElementAttribute attribute : attributeOverride) {
            this.attributeOverride.put(attribute.getDeclaringClass(), attribute);
        }
    }

    /**
     * Creates a new copy without any connections.
     * This copy only has same {@link #getIdentity() identity}.
     * @return the created copy
     */
    public FlowElement copy() {
        return new FlowElement(identity, description, getAttributeOverride());
    }

    /**
     * Returns the original identity.
     * @return the identity
     * @since 0.4.0
     */
    public Object getIdentity() {
        return identity;
    }

    /**
     * Returns the description of this element.
     * @return the description
     */
    public FlowElementDescription getDescription() {
        return description;
    }

    /**
     * Returns input ports of this element.
     * @return input ports
     */
    public List<FlowElementInput> getInputPorts() {
        return inputPorts;
    }

    /**
     * Returns input ports of this element.
     * @return output ports
     */
    public List<FlowElementOutput> getOutputPorts() {
        return outputPorts;
    }

    /**
     * Returns whether this element has the specified attribute or not.
     * @param attribute the target attribute
     * @return {@code true} if this element has the specified attribute, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public boolean hasAttribute(FlowElementAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        FlowElementAttribute own = getAttribute(attribute.getDeclaringClass());
        if (own == null) {
            return false;
        }
        return own.equals(attribute);
    }

    @Override
    public Set<? extends Class<? extends FlowElementAttribute>> getAttributeTypes() {
        Set<Class<? extends FlowElementAttribute>> results = new HashSet<>();
        results.addAll(attributeOverride.keySet());
        results.addAll(getDescription().getAttributeTypes());
        return results;
    }

    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException("attributeClass must not be null"); //$NON-NLS-1$
        }
        FlowElementAttribute override = attributeOverride.get(attributeClass);
        if (override != null) {
            return attributeClass.cast(override);
        }
        return getDescription().getAttribute(attributeClass);
    }

    /**
     * Overwrites the attribute into this element.
     * This does not change the attribute of the description.
     * @param attribute the attribute to be overwritten
     */
    public void override(FlowElementAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        attributeOverride.put(attribute.getDeclaringClass(), attribute);
    }

    /**
     * Returns the overwritten attributes.
     * @return the overwritten attributes
     */
    public Collection<FlowElementAttribute> getAttributeOverride() {
        return new ArrayList<>(attributeOverride.values());
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1}#{2})", //$NON-NLS-1$
                getDescription().getName(),
                getDescription().getKind().name().toLowerCase(),
                String.valueOf(hashCode()));
    }
}
