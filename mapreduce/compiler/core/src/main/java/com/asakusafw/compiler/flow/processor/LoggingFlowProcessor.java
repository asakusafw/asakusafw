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
package com.asakusafw.compiler.flow.processor;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.runtime.core.Report;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Logging;
import com.asakusafw.vocabulary.operator.Logging.Level;

/**
 * Processes {@link Logging} operators.
 */
@TargetOperator(Logging.class)
public class LoggingFlowProcessor extends LinePartProcessor {

    @Override
    public void emitLinePart(Context context) {
        ModelFactory f = context.getModelFactory();
        Expression input = context.getInput();
        Expression impl = context.createImplementation();
        OperatorDescription desc = context.getOperatorDescription();

        List<Expression> arguments = new ArrayList<>();
        arguments.add(input);
        for (OperatorDescription.Parameter param : desc.getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }
        Expression result = context.createLocalVariable(
                String.class,
                new ExpressionBuilder(f, impl)
                    .method(desc.getDeclaration().getName(), arguments)
                    .toExpression());

        Level level = context.getOperatorDescription().getAttribute(Logging.Level.class);
        switch (level == null ? Level.getDefault() : level) {
        case WARN:
            context.add(new TypeBuilder(f, context.convert(Report.class))
                .method("warn", result) //$NON-NLS-1$
                .toStatement());
            break;
        case ERROR:
            context.add(new TypeBuilder(f, context.convert(Report.class))
                .method("error", result) //$NON-NLS-1$
                .toStatement());
            break;
        default:
            context.add(new TypeBuilder(f, context.convert(Report.class))
                .method("info", result) //$NON-NLS-1$
                .toStatement());
            break;
        }
        context.setOutput(input);
    }
}
