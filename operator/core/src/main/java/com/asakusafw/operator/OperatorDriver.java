/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.operator;

import java.util.Objects;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorDescription;

/**
 * A service interface that supports new Asakusa Operator DSL annotations.
 * <p>
 * Adding a new operator annotations to Operator DSL compiler, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.lang.compiler.operator.OperatorDriver}.
 * </p>
 */
public interface OperatorDriver {

    /**
     * Returns the target annotation type name.
     * @return the target annotation type name
     */
    ClassDescription getAnnotationTypeName();

    /**
     * Analyzes the operator method and returns its description.
     * If failed to analyze the target method, this method returns {@code null}.
     * @param context target context
     * @return related description
     */
    OperatorDescription analyze(Context context);

    /**
     * Returns a list of proposals about target annotation element.
     * @param context target context
     * @param member target annotation element
     * @param userText user's text
     * @return list of proposals, or an empty list
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Completions
     */
    Iterable<? extends Completion> getCompletions(Context context, ExecutableElement member, String userText);

    /**
     * Represents processing context.
     */
    final class Context {

        private final CompileEnvironment environment;

        private final AnnotationMirror annotation;

        private final ExecutableElement method;

        /**
         * Creates a new instance.
         * @param environment current compiling environment
         * @param annotation target operator annotation
         * @param method target operator method
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Context(CompileEnvironment environment, AnnotationMirror annotation, ExecutableElement method) {
            this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
            this.annotation = Objects.requireNonNull(annotation, "annotation must not be null"); //$NON-NLS-1$
            this.method = Objects.requireNonNull(method, "method must not be null"); //$NON-NLS-1$
        }

        /**
         * Returns the current compiling environment.
         * @return the current compiling environment
         */
        public CompileEnvironment getEnvironment() {
            return environment;
        }

        /**
         * Returns the target annotation.
         * @return the target annotation
         */
        public AnnotationMirror getAnnotation() {
            return annotation;
        }

        /**
         * Returns the target operator method.
         * @return the target operator method
         */
        public ExecutableElement getMethod() {
            return method;
        }
    }
}
