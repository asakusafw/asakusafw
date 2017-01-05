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

import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * Processes an operator which is end of line.
 */
public abstract class LineEndProcessor extends LineProcessor {

    @Override
    public final Kind getKind() {
        return Kind.LINE_END;
    }

    /**
     * Performs this processor for the context.
     * @param context the current context
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public abstract void emitLineEnd(Context context);

    /**
     * A context object for {@link LineEndProcessor}.
     */
    public static class Context extends LineProcessorContext {

        private final Expression input;

        private final Map<FlowElementPortDescription, Expression> outputs;

        /**
         * Creates a new instance.
         * @param environment the current context
         * @param element the target element
         * @param importer the import declaration builder
         * @param names the unique name generator
         * @param desc the target operator description
         * @param input an expression of the input value for the operator
         * @param outputs a mapping of output ports to their expression
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
                Map<FlowElementPortDescription, Expression> outputs,
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, element, importer, names, desc, resources);
            Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
            this.input = input;
            this.outputs = outputs;
        }

        /**
         * Returns an expression of the input value.
         * @return the expression of the input value
         */
        public Expression getInput() {
            return input;
        }

        /**
         * Returns the mirror of {@link Result} object for the target output port.
         * @param port the target output port
         * @return the corresponded output port
         * @throws IllegalArgumentException if there is no such a corresponding a {@link Result} mirror
         */
        public ResultMirror getOutput(FlowElementPortDescription port) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            Expression result = outputs.get(port);
            if (result == null) {
                throw new IllegalArgumentException();
            }
            return new ResultMirror(factory, result);
        }
    }
}
