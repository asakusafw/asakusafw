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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an annotation.
 * @since 0.9.2
 */
public final class AnnotationInfo implements ValueInfo {

    static final String KIND = "annotation"; //$NON-NLS-1$

    @JsonIgnore
    private final ClassInfo declaringClass;

    @JsonProperty(Constants.ID_ELEMENTS)
    @JsonInclude(Include.NON_EMPTY)
    private final Map<String, ValueInfo> elements;

    private AnnotationInfo(ClassInfo declaringClass, Map<String, ValueInfo> elements) {
        Objects.requireNonNull(declaringClass);
        Objects.requireNonNull(elements);
        this.declaringClass = declaringClass;
        this.elements = elements;
    }

    @JsonCreator
    static AnnotationInfo of(
            @JsonProperty(Constants.ID_CLASS) String declaringClass,
            @JsonProperty(Constants.ID_ELEMENTS) Map<String, ? extends ValueInfo> elements) {
        return of(ClassInfo.of(declaringClass), elements == null ? Collections.emptyMap() : elements);
    }

    @JsonProperty(Constants.ID_CLASS)
    String getDeclaringClassName() {
        return declaringClass.getName();
    }

    /**
     * Returns an instance for the annotation.
     * @param declaringClass the annotation type
     * @param elements the annotation members
     * @return the instance
     */
    public static AnnotationInfo of(ClassInfo declaringClass, Map<String, ? extends ValueInfo> elements) {
        return new AnnotationInfo(declaringClass, Collections.unmodifiableMap(new LinkedHashMap<>(elements)));
    }

    @Override
    public Kind getKind() {
        return Kind.ANNOTATION;
    }

    @Override
    public String getObject() {
        StringBuilder buf = new StringBuilder();
        buf.append('@');
        buf.append(declaringClass.getName());
        if (elements.isEmpty() == false) {
            buf.append('(');
            ValueInfo value = elements.get("value");
            if (value != null && elements.size() == 1) {
                buf.append(value.getObject());
            } else {
                buf.append(elements.entrySet().stream()
                    .map(e -> String.format("%s=%s", e.getKey(), e.getValue().getObject()))
                    .collect(Collectors.joining(", ")));
            }
            buf.append(')');
        }
        return buf.toString();
    }

    /**
     * Returns the annotation type.
     * @return the annotation type
     */
    public ClassInfo getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Returns the elements.
     * @return the elements
     */
    public Map<String, ValueInfo> getElements() {
        return elements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getKind());
        result = prime * result + Objects.hashCode(declaringClass);
        result = prime * result + Objects.hashCode(elements);
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
        if (!(obj instanceof AnnotationInfo)) {
            return false;
        }
        AnnotationInfo other = (AnnotationInfo) obj;
        return Objects.equals(declaringClass, other.declaringClass)
                && Objects.equals(elements, other.elements);
    }

    @Override
    public String toString() {
        return String.format("%s(class=%s, elements={%s})",
                getKind(),
                declaringClass,
                elements);
    }
}
