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
package com.asakusafw.operator.model;

import java.text.MessageFormat;
import java.util.Objects;

import javax.lang.model.type.TypeMirror;

/**
 * Property in data models.
 */
public final class PropertyMirror {

    private final String name;

    private final TypeMirror type;

    /**
     * Creates a new instance.
     * @param name the normalized property name
     * @param type property type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PropertyMirror(String name, TypeMirror type) {
        this.name = Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        this.type = Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
    }

    /**
     * Returns the name of this property.
     * @return the name of this property
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this property.
     * @return the type of this property
     */
    public TypeMirror getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertyMirror other = (PropertyMirror) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}:{1}", //$NON-NLS-1$
                name,
                type);
    }
}