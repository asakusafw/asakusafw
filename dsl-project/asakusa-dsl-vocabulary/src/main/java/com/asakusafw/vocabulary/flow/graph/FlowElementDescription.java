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
package com.asakusafw.vocabulary.flow.graph;

import java.util.List;

/**
 * A description of flow elements.
 * @since 0.1.0
 * @version 0.5.1
 */
public interface FlowElementDescription extends FlowElementAttributeProvider {

    /**
     * Returns the kind of this element.
     * @return the element kind
     */
    FlowElementKind getKind();

    /**
     * Returns the original description.
     * @return the origin the original description
     * @since 0.5.1
     */
    FlowElementDescription getOrigin();

    /**
     * Returns the name of this element.
     * @return the name
     */
    String getName();

    /**
     * Sets name of this element.
     * @param newName the name
     * @throws UnsupportedOperationException if this does not support changing name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void setName(String newName);

    /**
     * Returns the input ports of this element.
     * @return the input ports
     */
    List<FlowElementPortDescription> getInputPorts();

    /**
     * Returns the output ports of this element.
     * @return the output ports
     */
    List<FlowElementPortDescription> getOutputPorts();

    /**
     * Returns the external resources of this element.
     * @return the external resources
     */
    List<FlowResourceDescription> getResources();
}
