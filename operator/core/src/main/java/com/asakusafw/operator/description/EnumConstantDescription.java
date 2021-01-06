/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
 * Represents an enum constant.
 */
public class EnumConstantDescription implements ValueDescription {

    private final ClassDescription declaringClass;

    private final String name;

    /**
     * Creates a new instance.
     * @param declaringClass the declaring class
     * @param name the constant name
     */
    public EnumConstantDescription(ClassDescription declaringClass, String name) {
        this.declaringClass = Objects.requireNonNull(declaringClass);
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Creates a new instance.
     * @param value the enum constant
     * @return the created instance
     */
    public static EnumConstantDescription of(Enum<?> value) {
        ClassDescription declaring = Descriptions.classOf(value.getDeclaringClass());
        String name = value.name();
        return new EnumConstantDescription(declaring, name);
    }

    @Override
    public ValueKind getValueKind() {
        return ValueKind.ENUM_CONSTANT;
    }

    @Override
    public ClassDescription getValueType() {
        return getDeclaringClass();
    }

    /**
     * Returns the declaring enum type.
     * @return the declaring enum type
     */
    public ClassDescription getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Returns the constant name.
     * @return the constant name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + declaringClass.hashCode();
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
        EnumConstantDescription other = (EnumConstantDescription) obj;
        if (!declaringClass.equals(other.declaringClass)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "EnumConstant({0}#{1})", //$NON-NLS-1$
                declaringClass.getClassName(),
                name);
    }
}
