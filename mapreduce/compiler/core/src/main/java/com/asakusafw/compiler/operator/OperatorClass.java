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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.asakusafw.compiler.common.Precondition;

/**
 * Structural information of operator classes.
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorClass {

    private final TypeElement element;

    private final List<OperatorMethod> methods;

    /**
     * Creates a new instance.
     * @param type the corresponded operator class
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public OperatorClass(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        this.element = type;
        this.methods = new ArrayList<>();
    }

    /**
     * Returns the corresponded operator class.
     * @return the corresponded operator class
     */
    public TypeElement getElement() {
        return this.element;
    }

    /**
     * Registers an operator method into this class.
     * @param annotation the operator annotation
     * @param methodElement the operator method
     * @param processor corresponded operator method
     */
    public void add(AnnotationMirror annotation, ExecutableElement methodElement, OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(annotation, "annotation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(methodElement, "methodElement"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        if (element.equals(methodElement.getEnclosingElement()) == false) {
            throw new IllegalArgumentException("methodElement must be a member of this class"); //$NON-NLS-1$
        }
        OperatorMethod method = new OperatorMethod(annotation, methodElement, processor);
        methods.add(method);
    }

    /**
     * Adds an operator method.
     * @param methodElement the target method
     * @param processor the operator processor that processes the target method
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public void add(ExecutableElement methodElement, OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(methodElement, "methodElement"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        if (element.equals(methodElement.getEnclosingElement()) == false) {
            throw new IllegalArgumentException("methodElement must be a member of this class"); //$NON-NLS-1$
        }
        OperatorMethod method = new OperatorMethod(methodElement, processor);
        methods.add(method);
    }

    /**
     * Returns structural information of operator methods in this operator class.
     * @return the operator methods
     */
    public List<OperatorMethod> getMethods() {
        return methods;
    }
}
