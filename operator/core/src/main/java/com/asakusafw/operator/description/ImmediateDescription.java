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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an immediate value.
 */
public class ImmediateDescription implements ValueDescription {

    private static final ReifiableTypeDescription NULL_TYPE = ReifiableTypeDescription.of(Object.class);

    private static final Map<Class<?>, BasicTypeDescription> BOXED;
    static {
        Map<Class<?>, BasicTypeDescription> map = new HashMap<>();
        for (BasicTypeDescription.BasicTypeKind kind : BasicTypeDescription.BasicTypeKind.values()) {
            map.put(kind.getWrapperType(), new BasicTypeDescription(kind));
        }
        BOXED = map;
    }

    private final TypeDescription valueType;

    private final Object value;

    /**
     * Creates a new instance.
     * @param valueType the value type
     * @param value the value
     */
    public ImmediateDescription(TypeDescription valueType, Object value) {
        this.valueType = valueType;
        this.value = value;
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(Object value) {
        if (value == null) {
            return nullOf(NULL_TYPE);
        }
        Class<?> aClass = value.getClass();
        ReifiableTypeDescription type;
        if (BOXED.containsKey(aClass)) {
            type = BOXED.get(aClass);
        } else if (aClass == String.class) {
            type = Descriptions.classOf(String.class);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "immediate value must be either boxed value or String: {0}", //$NON-NLS-1$
                    aClass.getName()));
        }
        return new ImmediateDescription(type, value);
    }

    /**
     * Returns whether the target type is boxed type or not.
     * @param aClass the target type
     * @return {@code true} if the target type is boxed, otherwise {@code false}
     */
    public static final boolean isBoxed(Class<?> aClass) {
        return BOXED.containsKey(aClass);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(boolean value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(boolean.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(byte value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(byte.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(short value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(short.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(int value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(int.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(long value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(long.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(float value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(float.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(double value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(double.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(char value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(char.class), value);
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static final ImmediateDescription of(String value) {
        return new ImmediateDescription(ReifiableTypeDescription.of(String.class), value);
    }

    /**
     * Creates a new instance that represents a {@code null} value.
     * @param valueType the value type
     * @return the created instance
     */
    public static final ImmediateDescription nullOf(ReifiableTypeDescription valueType) {
        return new ImmediateDescription(valueType, null);
    }

    @Override
    public ValueKind getValueKind() {
        return ValueKind.IMMEDIATE;
    }

    @Override
    public TypeDescription getValueType() {
        return valueType;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(value);
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
        ImmediateDescription other = (ImmediateDescription) obj;
        if (!Objects.equals(value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
