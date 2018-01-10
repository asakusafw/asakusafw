/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

    private final String methodName;

    private final List<ValueDescription> arguments;

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param methodName the factory method name, or {@code null} to indicate constructor
     * @param arguments the method or constructor arguments
     */
    public ObjectDescription(
            ClassDescription objectType, String methodName, List<? extends ValueDescription> arguments) {
        this.objectType = objectType;
        this.methodName = methodName;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
    }

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param arguments the constructor arguments
     * @return the created instance
     */
    public static ObjectDescription of(ClassDescription objectType, List<? extends ValueDescription> arguments) {
        return new ObjectDescription(objectType, null, arguments);
    }

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param arguments the constructor arguments
     * @return the created instance
     */
    public static ObjectDescription of(ClassDescription objectType, ValueDescription... arguments) {
        return of(objectType, Arrays.asList(arguments));
    }

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param methodName the factory method name
     * @param arguments the method arguments
     * @return the created instance
     */
    public static ObjectDescription of(ClassDescription objectType, String methodName,
            List<? extends ValueDescription> arguments) {
        return new ObjectDescription(objectType, methodName, arguments);
    }

    /**
     * Creates a new instance.
     * @param objectType the object type
     * @param methodName the factory method name
     * @param arguments the method arguments
     * @return the created instance
     */
    public static ObjectDescription of(ClassDescription objectType, String methodName,
            ValueDescription... arguments) {
        return of(objectType, methodName, Arrays.asList(arguments));
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
     * Returns the factory method name.
     * @return the factory method name, or {@code null} if constructor is instead of class methods
     */
    public String getMethodName() {
        return methodName;
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
        result = prime * result + Objects.hashCode(methodName);
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
        if (!Objects.equals(methodName, other.methodName)) {
            return false;
        }
        if (!Objects.equals(arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Object(type={0}, method={1}, args={2})", //$NON-NLS-1$
                objectType,
                methodName == null ? "new" : methodName, //$NON-NLS-1$
                arguments);
    }
}
