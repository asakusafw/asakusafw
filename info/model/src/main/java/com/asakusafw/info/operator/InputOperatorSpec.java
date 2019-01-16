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
package com.asakusafw.info.operator;

import java.util.Objects;
import java.util.Optional;

import com.asakusafw.info.value.ClassInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an operator detail of flow inputs.
 * @since 0.9.2
 */
public final class InputOperatorSpec implements NamedOperatorSpec {

    static final String KIND = "input";

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    @JsonIgnore
    private final ClassInfo descriptionClass;

    private InputOperatorSpec(String name, ClassInfo descriptionClass) {
        this.name = name;
        this.descriptionClass = descriptionClass;
    }

    @JsonCreator
    static InputOperatorSpec restore(
            @JsonProperty(Constants.ID_NAME) String name,
            @JsonProperty(Constants.ID_CLASS) String descriptionClass) {
        return of(
                name,
                Optional.ofNullable(descriptionClass)
                    .map(ClassInfo::of)
                    .orElse(null));
    }

    /**
     * Returns an instance.
     * @param name the port name
     * @param descriptionClass the input description (nullable)
     * @return the instance
     */
    public static InputOperatorSpec of(String name, ClassInfo descriptionClass) {
        return new InputOperatorSpec(name, descriptionClass);
    }

    @JsonProperty(Constants.ID_CLASS)
    String getDescriptionClassName() {
        return Optional.ofNullable(descriptionClass)
                .map(ClassInfo::getName)
                .orElse(null);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.INPUT;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the description class.
     * @return the description class, or {@code null} if it is not defined
     */
    public ClassInfo getDescriptionClass() {
        return descriptionClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getOperatorKind());
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(descriptionClass);
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
        InputOperatorSpec other = (InputOperatorSpec) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(descriptionClass, other.descriptionClass);
    }

    @Override
    public String toString() {
        return String.format("Input(name=%s, desc=%s)", name, descriptionClass);
    }
}
