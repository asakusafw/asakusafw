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
package com.asakusafw.compiler.operator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import com.asakusafw.compiler.common.Precondition;

/**
 * Structural information of operator methods.
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorMethod {

    private final ExecutableElement element;

    private final OperatorProcessor processor;

    private final AnnotationMirror annotation;

    /**
     * Creates a new instance.
     * @param element the declaration of operator method
     * @param processor the operator processor for processing this method
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public OperatorMethod(ExecutableElement element, OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        this.element = element;
        this.processor = processor;

        // fail fast
        this.annotation = processor.getOperatorAnnotation(element);
        Precondition.checkMustNotBeNull(annotation, "annotation"); //$NON-NLS-1$
    }

    /**
     * Creates a new instance.
     * @param annotation operator annotation
     * @param element target operator method
     * @param processor corresponded operator processor
     * @since 0.7.0
     */
    public OperatorMethod(AnnotationMirror annotation, ExecutableElement element, OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(annotation, "annotation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        this.annotation = annotation;
        this.element = element;
        this.processor = processor;
    }

    /**
     * Returns the operator annotation.
     * @return the operator annotation
     */
    public AnnotationMirror getAnnotation() {
        return annotation;
    }

    /**
     * Returns the declaration of operator method.
     * @return the declaration of operator method
     */
    public ExecutableElement getElement() {
        return this.element;
    }

    /**
     * Returns the operator processor for processing this method.
     * @return the operator processor
     */
    public OperatorProcessor getProcessor() {
        return this.processor;
    }
}
