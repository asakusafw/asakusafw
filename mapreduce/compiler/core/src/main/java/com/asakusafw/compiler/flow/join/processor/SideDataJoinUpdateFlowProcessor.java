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
package com.asakusafw.compiler.flow.join.processor;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.compiler.flow.join.JoinResourceDescription;
import com.asakusafw.compiler.flow.join.operator.SideDataJoinUpdate;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * Processes {@link SideDataJoinUpdate}.
 */
@TargetOperator(SideDataJoinUpdate.class)
public class SideDataJoinUpdateFlowProcessor extends LineEndProcessor {

    @Override
    public void emitLineEnd(Context context) {
        FlowResourceDescription resource = context.getResourceDescription(SideDataJoinUpdate.ID_RESOURCE_MASTER);
        SideDataKindFlowAnalyzer helper = new SideDataKindFlowAnalyzer(
                context,
                (JoinResourceDescription) resource);
        ModelFactory f = context.getModelFactory();
        OperatorDescription desc = context.getOperatorDescription();

        FlowElementPortDescription updatedPort = context.getOutputPort(SideDataJoinUpdate.ID_OUTPUT_UPDATED);
        FlowElementPortDescription missedPort = context.getOutputPort(SideDataJoinUpdate.ID_OUTPUT_MISSED);
        ResultMirror updated = context.getOutput(updatedPort);
        ResultMirror missed = context.getOutput(missedPort);

        Expression impl = context.createImplementation();
        List<Expression> arguments = new ArrayList<>();
        arguments.add(helper.getGetRawMasterExpression());
        arguments.add(context.getInput());
        for (OperatorDescription.Parameter param : desc.getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }

        context.add(f.newIfStatement(
                helper.getHasMasterExpresion(),
                f.newBlock(new Statement[] {
                        new ExpressionBuilder(f, impl)
                            .method(desc.getDeclaration().getName(), arguments)
                            .toStatement(),
                        updated.createAdd(context.getInput())
                }),
                f.newBlock(missed.createAdd(context.getInput()))));
    }
}
