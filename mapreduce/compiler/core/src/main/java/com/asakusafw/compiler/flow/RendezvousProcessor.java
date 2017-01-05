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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * Processes a rendezvous operator.
 */
public abstract class RendezvousProcessor extends AbstractFlowElementProcessor {

    @Override
    public final Kind getKind() {
        return Kind.RENDEZVOUS;
    }

    /**
     * Returns information of the shuffle operation for the target flow element port.
     * @param element the target flow element
     * @param port the target port
     * @return the analyzed information
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ShuffleDescription getShuffleDescription(
            FlowElementDescription element,
            FlowElementPortDescription port) {
        Precondition.checkMustNotBeNull(element, "description"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
        LinePartProcessor nop = new LinePartProcessor.Nop();
        nop.initialize(getEnvironment());
        return new ShuffleDescription(
                port.getDataType(),
                port.getShuffleKey(),
                nop);
    }

    /**
     * Performs this processor for the context.
     * @param context the current context
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public abstract void emitRendezvous(Context context);

    /**
     * Returns whether the target operator supports combine operation or not.
     * @param description the target operator
     * @return {@code true} if the target operator supports combine operation, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public boolean isPartial(FlowElementDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        return false;
    }

    /**
     * A context object for {@link RendezvousProcessor}.
     */
    public static class Context extends AbstractProcessorContext {

        private final Map<FlowElementPortDescription, Expression> inputs;

        private final Map<FlowElementPortDescription, Expression> outputs;

        private final List<Statement> beginStatements;

        private final Map<FlowElementPortDescription, List<Statement>> processStatements;

        private final List<Statement> endStatements;

        /**
         * Creates a new instance.
         * @param environment the current context
         * @param element the target element
         * @param importer the import declaration builder
         * @param names the unique name generator
         * @param desc the target operator description
         * @param inputs a mapping of input ports to their expression
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
                Map<FlowElementPortDescription, Expression> inputs,
                Map<FlowElementPortDescription, Expression> outputs,
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, element, importer, names, desc, resources);
            Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
            this.inputs = inputs;
            this.outputs = outputs;
            this.beginStatements = new ArrayList<>();
            this.processStatements = new HashMap<>();
            this.endStatements = new ArrayList<>();
            for (FlowElementPortDescription input : inputs.keySet()) {
                processStatements.put(input, new ArrayList<Statement>());
            }
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
                throw new IllegalArgumentException(port.toString());
            }
            return new ResultMirror(factory, result);
        }

        /**
         * Returns an expression of the input value for the target port
         * in {@link Rendezvous#process(org.apache.hadoop.io.Writable)}.
         * @param port the target input
         * @return an expression of the input value
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Expression getProcessInput(FlowElementPortDescription port) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            return getCommonInput(port);
        }

        private Expression getCommonInput(FlowElementPortDescription port) {
            assert port != null;
            Expression input = inputs.get(port);
            if (input == null) {
                throw new IllegalArgumentException(port.toString());
            }
            return input;
        }

        /**
         * Adds a statement for {@link Rendezvous#begin()} of the current operator implementation.
         * @param statement the statement
         * @throws IllegalArgumentException if the statement is {@code null}
         */
        public void addBegin(Statement statement) {
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            beginStatements.add(statement);
        }

        /**
         * Adds a statement for {@link Rendezvous#process(org.apache.hadoop.io.Writable)}
         * of the current operator implementation about the target input port.
         * @param port the target input port
         * @param statement the statement
         * @throws IllegalArgumentException if the statement is {@code null}
         */
        public void addProcess(FlowElementPortDescription port, Statement statement) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            List<Statement> statements = processStatements.get(port);
            if (statements == null) {
                throw new IllegalArgumentException(port.toString());
            }
            statements.add(statement);
        }

        /**
         * Adds a statement for {@link Rendezvous#end()} of the current operator implementation.
         * @param statement the statement
         * @throws IllegalArgumentException if the statement is {@code null}
         */
        public void addEnd(Statement statement) {
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            endStatements.add(statement);
        }

        /**
         * Returns the added statements in {@link Rendezvous#begin()}.
         * @return the added statements
         */
        public List<Statement> getBeginStatements() {
            return beginStatements;
        }

        /**
         * Returns the added statements in {@link Rendezvous#process(org.apache.hadoop.io.Writable)}
         * for the target input port.
         * @param port the target input port
         * @return the added statements
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public List<Statement> getProcessStatements(FlowElementPortDescription port) {
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            List<Statement> statements = processStatements.get(port);
            if (statements == null) {
                throw new IllegalArgumentException(port.toString());
            }
            return statements;
        }

        /**
         * Returns the added statements in {@link Rendezvous#end()}.
         * @return the added statements
         */
        public List<Statement> getEndStatements() {
            return endStatements;
        }
    }
}
