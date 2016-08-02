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

import java.lang.reflect.Type;

/**
 * Represents operator input/output port information.
 * @since 0.9.0
 */
public class PortInfo extends EdgeInfo<PortInfo> {

    private final Direction direction;

    private final String name;

    private final Type type;

    /**
     * Creates a new instance.
     * @param direction the port direction
     * @param name the port name
     * @param type the data type on the port
     */
    public PortInfo(Direction direction, String name, Type type) {
        this.direction = direction;
        this.name = name;
        this.type = type;
    }

    @Override
    protected PortInfo getSelf() {
        return this;
    }

    /**
     * Returns the port direction.
     * @return the port direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Returns the port name.
     * @return the port name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the data type on the port.
     * @return the data type
     */
    public Type getType() {
        return type;
    }

    /**
     * Represents a port direction.
     */
    public enum Direction {

        /**
         * input ports.
         */
        INPUT,

        /**
         * output ports.
         */
        OUTPUT,
    }
}
