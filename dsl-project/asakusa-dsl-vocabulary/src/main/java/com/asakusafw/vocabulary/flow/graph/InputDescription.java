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

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * A description of flow input.
 */
public class InputDescription implements FlowElementDescription {

    /**
     * The port name.
     */
    public static final String OUTPUT_PORT_NAME = "port"; //$NON-NLS-1$

    private static final Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> ATTRIBUTES;
    static {
        Map<Class<? extends FlowElementAttribute>, FlowElementAttribute> map =
            new HashMap<Class<? extends FlowElementAttribute>, FlowElementAttribute>();
        map.put(FlowBoundary.class, FlowBoundary.STAGE);
        ATTRIBUTES = Collections.unmodifiableMap(map);
    }

    private final String name;

    private final FlowElementPortDescription port;

    private final ImporterDescription importerDescription;

    /**
     * Creates a new instance.
     * @param name the input name
     * @param type the data type
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public InputDescription(String name, Type type) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.port = createPort(type);
        this.importerDescription = null;
    }

    /**
     * Creates a new instance.
     * @param name the input name
     * @param importer the importer description of this input
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public InputDescription(String name, ImporterDescription importer) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (importer == null) {
            throw new IllegalArgumentException("importer must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.port = createPort(importer.getModelType());
        this.importerDescription = importer;
    }

    private FlowElementPortDescription createPort(Type type) {
        assert type != null;
        return new FlowElementPortDescription(
                OUTPUT_PORT_NAME,
                type,
                PortDirection.OUTPUT);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        throw new UnsupportedOperationException("Renaming input does not permitted"); //$NON-NLS-1$
    }

    /**
     * Returns the importer description of this input.
     * @return the importer description, or {@code null} if this input is not external
     */
    public ImporterDescription getImporterDescription() {
        return this.importerDescription;
    }

    @Override
    public FlowElementKind getKind() {
        return FlowElementKind.INPUT;
    }

    @Override
    public FlowElementDescription getOrigin() {
        return this;
    }

    /**
     * Returns the data type of this input.
     * @return the data type
     */
    public Type getDataType() {
        return port.getDataType();
    }

    @Override
    public List<FlowElementPortDescription> getInputPorts() {
        return Collections.emptyList();
    }

    @Override
    public List<FlowElementPortDescription> getOutputPorts() {
        return Collections.singletonList(port);
    }

    @Override
    public List<FlowResourceDescription> getResources() {
        return Collections.emptyList();
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
        return MessageFormat.format(
                "{0}'{'name={1}'}'", //$NON-NLS-1$
                importerDescription != null
                    ? importerDescription.getClass().getSimpleName()
                    : "N/A", //$NON-NLS-1$
                name);
    }
}
