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
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an input port of {@link FlowElement}.
 * Application developers should not use this class directly.
 */
public final class FlowElementInput extends FlowElementPort {

    /**
     * Creates a new instance.
     * @param description the description of this port
     * @param owner the owner element
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public FlowElementInput(FlowElementPortDescription description, FlowElement owner) {
        super(description, owner);
    }

    /**
     * Returns the opposite ports of this.
     * @return the opposite ports
     */
    public Collection<FlowElementOutput> getOpposites() {
        Collection<FlowElementOutput> results = new ArrayList<>();
        for (PortConnection conn : getConnected()) {
            results.add(conn.getUpstream());
        }
        return results;
    }

    /**
     * Disconnects all opposite ports which connected into this.
     * @return the disconnected ports
     */
    @Override
    public Collection<FlowElementOutput> disconnectAll() {
        Collection<FlowElementOutput> results = new ArrayList<>();
        for (PortConnection conn : new ArrayList<>(getConnected())) {
            results.add(conn.getUpstream());
            conn.disconnect();
        }
        return results;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}->{1}", //$NON-NLS-1$
                getDescription().getName(),
                getOwner());
    }
}
