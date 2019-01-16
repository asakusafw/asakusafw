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
 * Represents a class.
 */
public class ClassDescription extends ReifiableTypeDescription {

    private final String name;

    /**
     * Creates a new instance.
     * @param name the binary name
     */
    public ClassDescription(String name) {
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Returns an instance.
     * @param aClass the reflective object
     * @return the related instance
     */
    public static ClassDescription of(Class<?> aClass) {
        if (aClass.isArray() || aClass.isPrimitive()) {
            throw new IllegalArgumentException("must be class or interface type"); //$NON-NLS-1$
        }
        return new ClassDescription(aClass.getName());
    }

    @Override
    public TypeKind getTypeKind() {
        return TypeKind.CLASS;
    }

    @Override
    public ClassDescription getErasure() {
        return this;
    }

    /**
     * Returns the fully qualified class name.
     * @return the fully qualified class name
     */
    public String getClassName() {
        return name.replace('$', '.');
    }

    /**
     * Returns the binary name.
     * @return the binary name
     */
    public String getBinaryName() {
        return name;
    }

    /**
     * Returns the binary name.
     * @return the binary name
     */
    public String getInternalName() {
        return name.replace('.', '/');
    }

    /**
     * Returns the binary name.
     * @return the binary name
     */
    public String getName() {
        return getBinaryName();
    }

    /**
     * Returns the class simple name.
     * @return the class simple name
     */
    public String getSimpleName() {
        int index = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$'));
        if (index < 0) {
            return name;
        }
        return name.substring(index + 1);
    }

    /**
     * Returns package name of the class.
     * @return the package name
     */
    public String getPackageName() {
        int index = name.lastIndexOf('.');
        if (index <= 0) {
            return null;
        }
        return name.substring(0, index);
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
        ClassDescription other = (ClassDescription) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Class({0})", //$NON-NLS-1$
                name);
    }
}
