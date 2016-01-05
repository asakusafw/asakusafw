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

import java.lang.reflect.Type;
import java.text.MessageFormat;

/**
 * A description of I/O port of flow elements.
 */
public class FlowElementPortDescription {

    private final String name;

    private final Type dataType;

    private final PortDirection direction;

    private final ShuffleKey shuffleKey;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param dataType the data type of the port
     * @param direction the port direction
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public FlowElementPortDescription(String name, Type dataType, PortDirection direction) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.dataType = dataType;
        this.direction = direction;
        this.shuffleKey = null;
    }

    /**
     * Creates a new instance.
     * @param name the port name
     * @param dataType the data type of the port
     * @param shuffleKey information of the shuffle operation
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public FlowElementPortDescription(String name, Type dataType, ShuffleKey shuffleKey) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (shuffleKey == null) {
            throw new IllegalArgumentException("shuffleKey must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.dataType = dataType;
        this.direction = PortDirection.INPUT;
        this.shuffleKey = shuffleKey;
    }

    /**
     * Returns the port name.
     * @return the port name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the data type of this port.
     * @return the data type
     */
    public Type getDataType() {
        return dataType;
    }

    /**
     * Returns the port direction.
     * @return the port direction
     */
    public PortDirection getDirection() {
        return direction;
    }

    /**
     * Returns information of the shuffle operation which will performs on this port.
     * @return shuffle operation information, or {@code null} if this port does not support shuffle operations
     */
    public ShuffleKey getShuffleKey() {
        return shuffleKey;
    }

    @Override
    public String toString() {
        if (direction == PortDirection.INPUT) {
            return MessageFormat.format(
                    "Input({0}):{1}", //$NON-NLS-1$
                    getName(),
                    getDataType());
        } else {
            return MessageFormat.format(
                    "Output({0}):{1}", //$NON-NLS-1$
                    getName(),
                    getDataType());
        }
    }
}
