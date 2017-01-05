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
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;

/**
 * Processes {@link MasterJoinUpdate} operators.
 */
@TargetOperator(MasterJoinUpdate.class)
public class MasterJoinUpdateFlowProcessor extends RendezvousProcessor {

    @Override
    public void emitRendezvous(Context context) {
        MasterKindFlowAnalyzer masterAnalyzer = new MasterKindFlowAnalyzer(context);
        ModelFactory f = context.getModelFactory();
        OperatorDescription desc = context.getOperatorDescription();

        FlowElementPortDescription tx = context.getInputPort(MasterJoinUpdate.ID_INPUT_TRANSACTION);

        FlowElementPortDescription updatedPort = context.getOutputPort(MasterJoinUpdate.ID_OUTPUT_UPDATED);
        FlowElementPortDescription missedPort = context.getOutputPort(MasterJoinUpdate.ID_OUTPUT_MISSED);
        ResultMirror updated = context.getOutput(updatedPort);
        ResultMirror missed = context.getOutput(missedPort);

        Expression impl = context.createImplementation();
        List<Expression> arguments = new ArrayList<>();
        arguments.add(masterAnalyzer.getGetRawMasterExpression());
        arguments.add(context.getProcessInput(tx));
        for (OperatorDescription.Parameter param : desc.getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }

        context.addProcess(tx, f.newIfStatement(
                masterAnalyzer.getHasMasterExpresion(),
                f.newBlock(new Statement[] {
                        new ExpressionBuilder(f, impl)
                            .method(desc.getDeclaration().getName(), arguments)
                            .toStatement(),
                        updated.createAdd(context.getProcessInput(tx))
                }),
                f.newBlock(missed.createAdd(context.getProcessInput(tx)))));
    }
}
