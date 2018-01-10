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
package com.asakusafw.info.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a list of {@link ValueInfo}.
 * @since 0.9.2
 */
public final class ListInfo implements ValueInfo {

    static final String KIND = "list"; //$NON-NLS-1$

    private static final ListInfo EMPTY = new ListInfo(Collections.emptyList());

    @JsonProperty(Constants.ID_ELEMENTS)
    @JsonInclude(Include.NON_EMPTY)
    private final List<ValueInfo> elements;

    private ListInfo(List<ValueInfo> elements) {
        Objects.requireNonNull(elements);
        this.elements = elements;
    }

    /**
     * Returns an instance represents the given list.
     * @param elements the element values
     * @return the instance
     */
    @JsonCreator
    public static ListInfo of(
            @JsonProperty(Constants.ID_ELEMENTS) List<? extends ValueInfo> elements) {
        if (elements == null || elements.isEmpty()) {
            return EMPTY;
        } else {
            return new ListInfo(Collections.unmodifiableList(new ArrayList<>(elements)));
        }
    }

    /**
     * Returns an instance represents the given list.
     * @param elements the element values
     * @return the instance
     */
    public static ListInfo of(ValueInfo... elements) {
        return of(Arrays.asList(elements));
    }

    @Override
    public Kind getKind() {
        return Kind.LIST;
    }

    @Override
    public List<String> getObject() {
        return getElements().stream()
                .map(ValueInfo::getObject)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Returns the element values.
     * @return the element values
     */
    public List<ValueInfo> getElements() {
        return elements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getKind());
        result = prime * result + Objects.hashCode(getElements());
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
        if (!(obj instanceof ListInfo)) {
            return false;
        }
        ListInfo other = (ListInfo) obj;
        return Objects.equals(elements, other.elements);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getKind(), getObject());
    }
}
