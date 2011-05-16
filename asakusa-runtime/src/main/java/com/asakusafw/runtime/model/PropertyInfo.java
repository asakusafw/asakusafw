/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.runtime.model;

import java.text.MessageFormat;

/**
 * Property information.
 * @since 0.2.0
 */
public final class PropertyInfo {

    private final String name;

    private final Class<?> type;

    private final int ordinal;

    /**
     * Creates a new instance.
     * @param name the property name in {@code snake_case}
     * @param type the property type
     * @param ordinal the property ordinal (0-origin)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PropertyInfo(String name, Class<?> type, int ordinal) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (ordinal < 0) {
            throw new IllegalArgumentException("ordinal must not be < 0"); //$NON-NLS-1$
        }
        this.name = name;
        this.type = type;
        this.ordinal = ordinal;
    }

    /**
     * Returns the property name in {@code snake_case}.
     * <p>
     * For example, the property getter method with option {@code getHelloWorldOption}
     * corresponds to the property name {@code hello_world}.
     * </p>
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the proeprty type.
     * @return the proeprty type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the 0-origin property ordinal.
     * @return the 0-origin property ordinal
     */
    public int getOrdinal() {
        return ordinal;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}@{2}:{1}",
                name,
                type,
                String.valueOf(ordinal));
    }
}
