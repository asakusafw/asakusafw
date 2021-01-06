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
 * Represents an {@link Enum} constant.
 * @since 0.9.2
 */
public final class EnumInfo implements ValueInfo {

    static final String KIND = "enum"; //$NON-NLS-1$

    static final char NAME_SEPARATOR = '.';

    @JsonIgnore
    private final ClassInfo declaringClass;

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    private EnumInfo(ClassInfo declaringClass, String name) {
        this.declaringClass = declaringClass;
        this.name = name;
    }

    @JsonCreator
    static EnumInfo of(
            @JsonProperty(Constants.ID_CLASS) String declaringClassName,
            @JsonProperty(Constants.ID_NAME) String name) {
        return of(ClassInfo.of(declaringClassName), name);
    }

    @JsonProperty(Constants.ID_CLASS)
    String getDeclaringClassName() {
        return declaringClass.getName();
    }

    /**
     * Returns an instance for the enum constant.
     * @param declaringClass the declaring class of the constant
     * @param name the name of the constant
     * @return the instance
     */
    public static EnumInfo of(ClassInfo declaringClass, String name) {
        return new EnumInfo(declaringClass, name);
    }

    /**
     * Returns an instance for the enum constant.
     * @param value the value
     * @return the instance
     */
    public static EnumInfo of(Enum<?> value) {
        return of(ClassInfo.of(value.getDeclaringClass()), value.name());
    }

    @Override
    public Kind getKind() {
        return Kind.ENUM;
    }

    @Override
    public String getObject() {
        return declaringClass.getName() + NAME_SEPARATOR + name;
    }

    /**
     * Returns the declaring class.
     * @return the declaring class
     */
    public ClassInfo getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getKind());
        result = prime * result + Objects.hashCode(declaringClass);
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
        if (!(obj instanceof EnumInfo)) {
            return false;
        }
        EnumInfo other = (EnumInfo) obj;
        return Objects.equals(declaringClass, other.declaringClass)
                && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getKind(), getObject());
    }
}
