/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * Represents an unknown value.
 * @since 0.9.2
 */
public final class UnknownInfo implements ValueInfo {

    static final String KIND = "unknown"; //$NON-NLS-1$

    @JsonIgnore
    private final ClassInfo declaringClass;

    @JsonProperty(Constants.ID_VALUE)
    private final String value;

    private UnknownInfo(ClassInfo declaringClass, String value) {
        this.declaringClass = declaringClass;
        this.value = value;
    }

    @JsonCreator
    static UnknownInfo of(
            @JsonProperty(Constants.ID_CLASS) String declaringClass,
            @JsonProperty(Constants.ID_VALUE) String value) {
        return of(ClassInfo.of(declaringClass), value);
    }

    /**
     * Returns an instance for the given unknown value.
     * @param declaringClass class of the value
     * @param value string representation of the value
     * @return the instance
     */
    public static UnknownInfo of(ClassInfo declaringClass, String value) {
        return new UnknownInfo(declaringClass, value);
    }

    /**
     * Returns an instance for the value.
     * @param value the target value
     * @return the instance
     */
    public static UnknownInfo of(Object value) {
        Objects.requireNonNull(value);
        return of(ClassInfo.of(value.getClass()), String.valueOf(value));
    }

    @JsonProperty(Constants.ID_CLASS)
    String getDeclaringClassName() {
        return declaringClass.getName();
    }

    @Override
    public Kind getKind() {
        return Kind.UNKNOWN;
    }

    @Override
    public String getObject() {
        return getValue();
    }

    /**
     * Returns the value.
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the value class.
     * @return the value class
     */
    public ClassInfo getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getKind(), getObject());
    }
}
