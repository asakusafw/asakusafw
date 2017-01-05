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
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Convert;

/**
 * Processes {@link Convert} operators.
 */
@TargetOperator(Convert.class)
public class ConvertFlowProcessor extends LineEndProcessor {

    @Override
    public void emitLineEnd(Context context) {
        ModelFactory f = context.getModelFactory();
        Expression input = context.getInput();
        Expression impl = context.createImplementation();
        OperatorDescription desc = context.getOperatorDescription();

        FlowElementPortDescription converted = context.getOutputPort(Convert.ID_OUTPUT_CONVERTED);
        FlowElementPortDescription original = context.getOutputPort(Convert.ID_OUTPUT_ORIGINAL);

        List<Expression> arguments = new ArrayList<>();
        arguments.add(input);
        for (OperatorDescription.Parameter param : desc.getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }
        Expression result = context.createLocalVariable(
                converted.getDataType(),
                new ExpressionBuilder(f, impl)
                    .method(desc.getDeclaration().getName(), arguments)
                    .toExpression());

        context.add(context.getOutput(original).createAdd(input));
        context.add(context.getOutput(converted).createAdd(result));
    }
}
