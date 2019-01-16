/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.operator.description;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Represents an array type.
 */
public class ArrayTypeDescription extends ReifiableTypeDescription {

    private final TypeDescription componentType;

    /**
     * Creates a new instance.
     * @param componentType the component type
     */
    public ArrayTypeDescription(TypeDescription componentType) {
        this.componentType = Objects.requireNonNull(componentType);
    }

    /**
     * Returns an instance.
     * @param arrayType the reflective object
     * @return the related instance
     */
    public static ArrayTypeDescription of(Class<?> arrayType) {
        if (arrayType.isArray() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "must be an array: {0}", //$NON-NLS-1$
                    arrayType.getName()));
        }
        ReifiableTypeDescription component = ReifiableTypeDescription.of(arrayType.getComponentType());
        return new ArrayTypeDescription(component);
    }

    @Override
    public TypeKind getTypeKind() {
        return TypeKind.ARRAY;
    }

    @Override
    public ArrayTypeDescription getErasure() {
        ReifiableTypeDescription erased = componentType.getErasure();
        if (erased.equals(componentType)) {
            return this;
        }
        return new ArrayTypeDescription(erased);
    }

    /**
     * Returns the component type.
     * @return the component type
     */
    public TypeDescription getComponentType() {
        return componentType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + componentType.hashCode();
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
        ArrayTypeDescription other = (ArrayTypeDescription) obj;
        if (!componentType.equals(other.componentType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "ArrayType({0})", //$NON-NLS-1$
                componentType);
    }
}
