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
package com.asakusafw.operator.model;

import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

/**
 * Represents an operator method/constructor.
 */
public class OperatorElement {

    private final AnnotationMirror annotation;

    private final ExecutableElement declaration;

    private final OperatorDescription description;

    /**
     * Creates a new instance.
     * @param annotation target annotation
     * @param declaration method/constructor that declares this operator element
     * @param description description of this operator (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OperatorElement(
            AnnotationMirror annotation,
            ExecutableElement declaration,
            OperatorDescription description) {
        this.annotation = Objects.requireNonNull(annotation, "annotation must not be null"); //$NON-NLS-1$
        this.declaration = Objects.requireNonNull(declaration, "declaration must not be null"); //$NON-NLS-1$
        this.description = description;
    }

    /**
     * Returns the annotation of target operator/flow-part.
     * @return the annotation
     */
    public AnnotationMirror getAnnotation() {
        return annotation;
    }

    /**
     * Returns the method/constructor declaration of this operator.
     * @return the method/constructor declaration
     */
    public ExecutableElement getDeclaration() {
        return declaration;
    }

    /**
     * Returns the description of this operator element.
     * @return the description, or {@code null} if it is not described
     */
    public OperatorDescription getDescription() {
        return description;
    }
}
