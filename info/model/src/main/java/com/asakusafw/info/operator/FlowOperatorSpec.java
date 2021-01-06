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
package com.asakusafw.info.operator;

import java.util.Objects;
import java.util.Optional;

import com.asakusafw.info.value.ClassInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a flow operator details.
 * @since 0.9.2
 */
public final class FlowOperatorSpec implements OperatorSpec {

    static final String KIND = "flow";

    @JsonIgnore
    private final ClassInfo descriptionClass;

    private FlowOperatorSpec(ClassInfo descriptionClass) {
        this.descriptionClass = descriptionClass;
    }

    @JsonCreator
    static FlowOperatorSpec restore(@JsonProperty(Constants.ID_CLASS) String descriptionClass) {
        return of(Optional.ofNullable(descriptionClass)
                .map(ClassInfo::of)
                .orElse(null));
    }

    /**
     * Returns an instance.
     * @param descriptionClass the input description (nullable)
     * @return the instance
     */
    public static FlowOperatorSpec of(ClassInfo descriptionClass) {
        return new FlowOperatorSpec(descriptionClass);
    }

    @JsonProperty(Constants.ID_CLASS)
    String getDescriptionClassName() {
        return Optional.ofNullable(descriptionClass)
                .map(ClassInfo::getName)
                .orElse(null);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.FLOW;
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
        FlowOperatorSpec other = (FlowOperatorSpec) obj;
        return Objects.equals(descriptionClass, other.descriptionClass);
    }

    @Override
    public String toString() {
        return String.format("Flow(%s)",
                descriptionClass);
    }
}
