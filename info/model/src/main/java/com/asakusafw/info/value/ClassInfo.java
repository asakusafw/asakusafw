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
package com.asakusafw.info.value;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a {@link Class} value.
 * @since 0.9.2
 */
public final class ClassInfo implements ValueInfo, Comparable<ClassInfo> {

    static final String KIND = "class"; //$NON-NLS-1$

    private static final String SUFFIX_ARRAY = "[]";

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    private ClassInfo(String name) {
        this.name = name;
    }

    /**
     * Returns a class value for the name.
     * @param name the class name
     * @return the class
     */
    @JsonCreator
    public static ClassInfo of(
            @JsonProperty(Constants.ID_NAME) String name) {
        return new ClassInfo(name);
    }

    /**
     * Returns the class value.
     * @param aClass the target class
     * @return the class info
     */
    public static ClassInfo of(Class<?> aClass) {
        int dims = 0;
        Class<?> current = aClass;
        while (current.isArray()) {
            current = current.getComponentType();
            dims++;
        }
        if (dims == 0) {
            return of(current.getName());
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(current.getName());
            for (int i = 0; i < dims; i++) {
                buf.append(SUFFIX_ARRAY);
            }
            return of(buf.toString());
        }
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS;
    }

    @Override
    public String getObject() {
        return getName();
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the fully qualified class name.
     * @return the fully qualified class name
     */
    @JsonIgnore
    public String getClassName() {
        return name.replace('$', '.');
    }

    /**
     * Returns the binary name.
     * @return the binary name
     */
    @JsonIgnore
    public String getBinaryName() {
        return name;
    }

    /**
     * Returns the class simple name.
     * @return the class simple name
     */
    @JsonIgnore
    public String getSimpleName() {
        int index = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$'));
        if (index < 0) {
            return name;
        }
        return name.substring(index + 1);
    }

    /**
     * Returns the array type of this.
     * @return the array type
     */
    @JsonIgnore
    public ClassInfo getArrayType() {
        return ClassInfo.of(name + SUFFIX_ARRAY);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getKind());
        result = prime * result + Objects.hashCode(name);
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
        if (!(obj instanceof ClassInfo)) {
            return false;
        }
        ClassInfo other = (ClassInfo) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public int compareTo(ClassInfo o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getKind(), getObject());
    }
}
