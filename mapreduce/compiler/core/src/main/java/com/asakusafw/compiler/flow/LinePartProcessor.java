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
package com.asakusafw.compiler.flow;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Identity;

/**
 * Processes an operator which is part of line.
 */
public abstract class LinePartProcessor extends LineProcessor {

    @Override
    public final Kind getKind() {
        return Kind.LINE_PART;
    }

    /**
     * Performs this processor for the context.
     * @param context the current context
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public abstract void emitLinePart(Context context);

    /**
     * The context object for {@link LinePartProcessor}.
     */
    public static class Context extends LineProcessorContext {

        private final Expression input;

        private Expression resultValue;

        /**
         * Creates a new instance.
         * @param environment the current context
         * @param element the target element
         * @param importer the import declaration builder
         * @param names the unique name generator
         * @param desc the target operator description
         * @param input an expression of the input value for the operator
         * @param resources the mapping between external resources and their Java expressions
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Context(
                FlowCompilingEnvironment environment,
                FlowElementAttributeProvider element,
                ImportBuilder importer,
                NameGenerator names,
                OperatorDescription desc,
                Expression input,
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, element, importer, names, desc, resources);
            Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
            this.input = input;
            this.resultValue = null;
        }

        /**
         * Returns an expression of the input value.
         * @return the expression of the input value
         */
        public Expression getInput() {
            return input;
        }

        /**
         * Sets an expression of the output value.
         * @param expresion the expression of the output value
         * @throws IllegalStateException if the expression has been already set
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public void setOutput(Expression expresion) {
            Precondition.checkMustNotBeNull(expresion, "expresion"); //$NON-NLS-1$
            if (this.resultValue != null) {
                throw new IllegalStateException();
            }
            this.resultValue = expresion;
        }

        /**
         * Returns the expression of the output value.
         * @return the expression of the output value
         * @throws IllegalStateException if the expression has not been set
         * @see #setOutput(Expression)
         */
        public Expression getOutput() {
            if (resultValue == null) {
                throw new IllegalStateException();
            }
            return resultValue;
        }
    }

    /**
     * An implementation of {@link LinePartProcessor} that does nothing.
     */
    @TargetOperator(Identity.class)
    public static class Nop extends LinePartProcessor {
        @Override
        protected Class<? extends Annotation> loadTargetAnnotationType() {
            return Identity.class;
        }
        @Override
        public void emitLinePart(Context context) {
            context.setOutput(context.getInput());
        }
    }
}
