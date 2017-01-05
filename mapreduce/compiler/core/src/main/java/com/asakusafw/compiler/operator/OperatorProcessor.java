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

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;

/**
 * An abstract super interface of operator processors for compiling operator DSL.
 */
public interface OperatorProcessor {

    /**
     * Initializes this operator processor.
     * @param env the current environment object
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void initialize(OperatorCompilingEnvironment env);

    /**
     * Returns the target operator annotation type.
     * @return the target operator annotation type
     */
    Class<? extends Annotation> getTargetAnnotationType();

    /**
     * Returns the operator annotation for the target executable element.
     * @param element the target executable element
     * @return the operator annotation, or {@code null} if the target executable element does not have it
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    AnnotationMirror getOperatorAnnotation(ExecutableElement element);

    /**
     * Analyzes the operator method and returns the structural information of the target operator.
     * @param context the current context
     * @return the structural information object, or {@code null} if the target operator method is not valid
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    OperatorMethodDescriptor describe(Context context);

    /**
     * Returns an implementation method for the target operator method.
     * @param context the current context
     * @return the implementation method declaration, or {@code null} if the target operator method is not valid
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    List<? extends TypeBodyDeclaration> implement(Context context);

    /**
     * The context object for operator processors.
     */
    class Context {

        /**
         * The current environment.
         */
        public final OperatorCompilingEnvironment environment;

        /**
         * The target operator annotation.
         */
        public final AnnotationMirror annotation;

        /**
         * The target executable element.
         */
        public final ExecutableElement element;

        /**
         * The current import declaration builder.
         */
        public final ImportBuilder importer;

        /**
         * The current unique name generator.
         */
        public final NameGenerator names;

        /**
         * Creates a new instance.
         * @param environment the current environment
         * @param annotation the target operator annotation
         * @param element the target executable element
         * @param importer the current import declaration builder
         * @param names the current unique name generator
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Context(
                OperatorCompilingEnvironment environment,
                AnnotationMirror annotation,
                ExecutableElement element,
                ImportBuilder importer,
                NameGenerator names) {
            Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(annotation, "annotation"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(names, "names"); //$NON-NLS-1$
            this.environment = environment;
            this.annotation = annotation;
            this.element = element;
            this.importer = importer;
            this.names = names;
        }
    }
}
