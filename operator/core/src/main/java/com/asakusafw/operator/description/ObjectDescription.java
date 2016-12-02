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
package com.asakusafw.operator.description;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an object.
 * @since 0.9.1
 */
public class ObjectDescription implements ValueDescription {

    private final ClassDescription objectType;

    private final List<ValueDescription> arguments;

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param arguments the constructor arguments
     */
    public ObjectDescription(ClassDescription objectType, List<? extends ValueDescription> arguments) {
        this.objectType = objectType;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
    }

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param arguments the constructor arguments
     * @return the created instance
     */
    public static ObjectDescription of(ClassDescription objectType, List<? extends ValueDescription> arguments) {
        return new ObjectDescription(objectType, arguments);
    }

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param arguments the constructor arguments
     * @return the created instance
     */
    public static ObjectDescription of(ClassDescription objectType, ValueDescription... arguments) {
        return new ObjectDescription(objectType, Arrays.asList(arguments));
    }

    @Override
    public ValueKind getValueKind() {
        return ValueKind.OBJECT;
    }

    @Override
    public ClassDescription getValueType() {
        return objectType;
    }

    /**
     * Returns the arguments.
     * @return the arguments
     */
    public List<ValueDescription> getArguments() {
        return arguments;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(objectType);
        result = prime * result + Objects.hashCode(arguments);
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
        ObjectDescription other = (ObjectDescription) obj;
        if (!Objects.equals(objectType, other.objectType)) {
            return false;
        }
        if (!Objects.equals(arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Object(type={0}, args={1})", //$NON-NLS-1$
                objectType,
                arguments);
    }
}
