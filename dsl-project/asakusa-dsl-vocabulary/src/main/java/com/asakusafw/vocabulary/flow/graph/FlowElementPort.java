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
package com.asakusafw.vocabulary.flow.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract super class of I/O ports of {@link FlowElement}.
 * Application developers should not use this class directly.
 * @since 0.1.0
 * @version 0.9.1
 */
public abstract class FlowElementPort implements FlowElementAttributeProvider {

    private final FlowElement owner;

    private final FlowElementPortDescription description;

    private final Set<PortConnection> connected;

    /**
     * Creates a new instance.
     * @param description the description of this port
     * @param owner the owner element
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public FlowElementPort(FlowElementPortDescription description, FlowElement owner) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        this.owner = owner;
        this.description = description;
        this.connected = new HashSet<>();
    }

    /**
     * Returns the owner of this port.
     * @return the owner
     */
    public FlowElement getOwner() {
        return owner;
    }

    /**
     * Returns the description of this port.
     * @return the description
     */
    public FlowElementPortDescription getDescription() {
        return description;
    }

    @Override
    public Set<? extends Class<? extends FlowElementAttribute>> getAttributeTypes() {
        return getDescription().getAttributeTypes();
    }

    @Override
    public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
        return getDescription().getAttribute(attributeClass);
    }

    /**
     * Returns the set of connections into this port.
     * @return the connections
     */
    public Set<PortConnection> getConnected() {
        return Collections.unmodifiableSet(connected);
    }

    /**
     * Removes all connections for this port.
     * @return connected opposites
     * @since 0.5.1
     */
    public abstract Collection<? extends FlowElementPort> disconnectAll();

    void register(PortConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        connected.add(connection);
    }

    void unregister(PortConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        boolean removed = connected.remove(connection);
        if (removed == false) {
            throw new IllegalStateException();
        }
    }
}
