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
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * An abstract super class of operator processors which are on line.
 */
public abstract class LineProcessor extends AbstractFlowElementProcessor {

    /**
     * An abstract super class of context class for sub-classes of {@link LineProcessor}.
     */
    public abstract static class LineProcessorContext extends AbstractProcessorContext {

        /**
         * The generated statements.
         */
        protected final List<Statement> generatedStatements;

        /**
         * Creates a new instance.
         * @param environment the current context
         * @param element the target element
         * @param importer the import declaration builder
         * @param names the unique name generator
         * @param desc the target operator description
         * @param resources the mapping between external resources and their Java expressions
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        protected LineProcessorContext(
                FlowCompilingEnvironment environment,
                FlowElementAttributeProvider element,
                ImportBuilder importer,
                NameGenerator names,
                OperatorDescription desc,
                Map<FlowResourceDescription, Expression> resources) {
            super(environment, element, importer, names, desc, resources);
            this.generatedStatements = new ArrayList<>();
        }

        /**
         * Adds a statement for the current operator implementation.
         * @param statement the statement
         * @throws IllegalArgumentException if the statement is {@code null}
         */
        public void add(Statement statement) {
            Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
            generatedStatements.add(statement);
        }

        /**
         * Returns the added statements.
         * @return the added statements
         */
        public List<Statement> getGeneratedStatements() {
            return generatedStatements;
        }

        /**
         * Adds a local variable declaration for the current operator implementation.
         * @param type the variable type
         * @param initializer the variable initializer (nullable)
         * @return an expression which accesses the declared variable
         * @throws IllegalArgumentException if {@code type} is {@code null}
         */
        public Expression createLocalVariable(java.lang.reflect.Type type, Expression initializer) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            return createLocalVariable(Models.toType(factory, type), initializer);
        }

        /**
         * Adds a local variable declaration for the current operator implementation.
         * @param type the variable type
         * @param initializer the variable initializer (nullable)
         * @return an expression which accesses the declared variable
         * @throws IllegalArgumentException if {@code type} is {@code null}
         */
        public Expression createLocalVariable(Type type, Expression initializer) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            SimpleName name = names.create("v"); //$NON-NLS-1$
            add(new ExpressionBuilder(factory, initializer)
                .toLocalVariableDeclaration(importer.resolve(type), name));
            return name;
        }
    }
}
