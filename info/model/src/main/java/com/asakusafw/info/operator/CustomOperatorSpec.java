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
package com.asakusafw.info.operator;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details of a core operator.
 * @since 0.9.2
 */
public final class CustomOperatorSpec implements OperatorSpec {

    static final String KIND = "custom";

    @JsonProperty(Constants.ID_CATEGORY)
    private final String category;

    private CustomOperatorSpec(String annotation) {
        this.category = annotation;
    }

    /**
     * Returns an instance.
     * @param category the category
     * @return the instance
     */
    @JsonCreator
    public static CustomOperatorSpec of(
            @JsonProperty(Constants.ID_CATEGORY) String category) {
        return new CustomOperatorSpec(category);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.CUSTOM;
    }

    /**
     * Returns the operator category.
     * @return the kind
     */
    public String getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getOperatorKind());
        result = prime * result + Objects.hashCode(category);
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
        CustomOperatorSpec other = (CustomOperatorSpec) obj;
        return Objects.equals(category, other.category);
    }

    @Override
    public String toString() {
        return String.format("Custom(%s)", category);
    }
}
