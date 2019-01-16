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
package com.asakusafw.operator.model;

import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.util.AnnotationHelper;

/**
 * Represents {@code Import}/{@code Export} annotation model.
 */
public final class ExternMirror {

    private final AnnotationMirror source;

    private final String name;

    private final TypeMirror description;

    private ExternMirror(AnnotationMirror source, String name, TypeMirror description) {
        this.source = source;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the source annotation.
     * @return the source
     */
    public AnnotationMirror getSource() {
        return source;
    }

    /**
     * Returns the extern name.
     * @return the extern name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the extern description type.
     * @return the extern description type
     */
    public TypeMirror getDescription() {
        return description;
    }


    /**
     * Parses the target {@code Import}/{@code Export} annotation.
     * @param environment current environment
     * @param source the source annotation
     * @param annotationOwner annotated target
     * @return the parsed result, or {@code null} if failed to parse
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ExternMirror parse(
            CompileEnvironment environment,
            AnnotationMirror source,
            Element annotationOwner) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(source, "source must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(annotationOwner, "annotationOwner must not be null"); //$NON-NLS-1$
        Map<String, AnnotationValue> pairs = AnnotationHelper.getValues(environment, source);
        AnnotationValue nameValue = pairs.get("name"); //$NON-NLS-1$
        if (nameValue == null || (nameValue.getValue() instanceof String) == false) {
            return null;
        }
        AnnotationValue descriptionValue = pairs.get("description"); //$NON-NLS-1$
        if (descriptionValue == null || (descriptionValue.getValue() instanceof TypeMirror) == false) {
            return null;
        }
        return new ExternMirror(source, (String) nameValue.getValue(), (TypeMirror) descriptionValue.getValue());
    }
}
