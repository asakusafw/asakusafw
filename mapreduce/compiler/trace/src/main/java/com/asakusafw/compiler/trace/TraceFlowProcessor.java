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
package com.asakusafw.compiler.trace;

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
import com.asakusafw.vocabulary.flow.graph.OperatorDescription.Parameter;
import com.asakusafw.vocabulary.operator.Trace;

/**
 * Processes {@link Trace} operators.
 * @since 0.5.1
 */
@TargetOperator(Trace.class)
public class TraceFlowProcessor extends LinePartProcessor {

    @Override
    public void emitLinePart(Context context) {
        ModelFactory f = Models.getModelFactory();
        Expression operator = context.createImplementation();
        List<Expression> arguments = new ArrayList<>();
        Expression input = context.getInput();
        arguments.add(input);
        for (Parameter parameter : context.getOperatorDescription().getParameters()) {
            arguments.add(Models.toLiteral(f, parameter.getValue()));
        }
        context.add(new TypeBuilder(f, context.convert(Report.class))
                .method("info", new ExpressionBuilder(f, operator)
                        .method(context.getOperatorDescription().getDeclaration().getName(), arguments)
                        .toExpression())
                .toStatement());
        context.setOutput(input);
    }
}
