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
package com.asakusafw.workflow.model.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.asakusafw.workflow.model.Element;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a parameter list of batch applications.
 * @since 0.10.0
 */
public class ParameterListAttribute implements Element.Attribute {

    @JsonProperty("elements")
    private final List<ParameterInfo> elements;

    @JsonProperty("strict")
    private final boolean strict;

    /**
     * Creates a new instance.
     * @param elements the elements
     * @param strict {@code true} if only accepts explicitly defined parameters, otherwise {@code false}
     */
    public ParameterListAttribute(Collection<? extends ParameterInfo> elements, boolean strict) {
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
        this.strict = strict;
    }

    @JsonCreator
    static ParameterListAttribute of(
            @JsonProperty("elements") Collection<? extends ParameterInfo> elements,
            @JsonProperty("strict") boolean strict) {
        return new ParameterListAttribute(elements, strict);
    }

    /**
     * Returns the elements.
     * @return the elements
     */
    @JsonProperty
    public List<ParameterInfo> getElements() {
        return elements;
    }

    /**
     * Returns the strict.
     * @return the strict
     */
    @JsonProperty
    public boolean isStrict() {
        return strict;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(elements);
        result = prime * result + Boolean.hashCode(strict);
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
        ParameterListAttribute other = (ParameterListAttribute) obj;
        return Objects.equals(elements, other.elements)
                && strict == other.strict;
    }

    @Override
    public String toString() {
        return String.format("ParameterList(elements=%s, strict=%s)", getElements(), isStrict()); //$NON-NLS-1$
    }
}
