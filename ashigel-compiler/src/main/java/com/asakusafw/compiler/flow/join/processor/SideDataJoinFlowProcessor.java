/**
 * Copyright 2011 Asakusa Framework Team.
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
import com.asakusafw.compiler.flow.join.operator.SideDataJoin;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;

/**
 * {@link SideDataJoin}を処理する。
 */
@TargetOperator(SideDataJoin.class)
public class SideDataJoinFlowProcessor extends LineEndProcessor {

    @Override
    public void emitLineEnd(Context context) {
        FlowResourceDescription resource = context.getResourceDescription(SideDataJoin.ID_RESOURCE_MASTER);
        SideDataKindFlowAnalyzer helper = new SideDataKindFlowAnalyzer(
                context,
                (JoinResourceDescription) resource);
        ModelFactory f = context.getModelFactory();
        OperatorDescription desc = context.getOperatorDescription();

        FlowElementPortDescription joinedPort = context.getOutputPort(SideDataJoin.ID_OUTPUT_JOINED);
        FlowElementPortDescription missedPort = context.getOutputPort(SideDataJoin.ID_OUTPUT_MISSED);
        ResultMirror joined = context.getOutput(joinedPort);
        ResultMirror missed = context.getOutput(missedPort);

        Expression impl = context.createImplementation();
        List<Expression> arguments = new ArrayList<Expression>();
        arguments.add(helper.getGetRawMasterExpression());
        arguments.add(context.getInput());
        context.add(f.newIfStatement(
                helper.getHasMasterExpresion(),
                f.newBlock(joined.createAdd(new ExpressionBuilder(f, impl)
                    .method(desc.getDeclaration().getName(), arguments)
                    .toExpression())),
                f.newBlock(missed.createAdd(context.getInput()))));
    }
}
