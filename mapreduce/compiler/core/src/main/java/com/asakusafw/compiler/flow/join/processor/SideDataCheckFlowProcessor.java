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

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.compiler.flow.join.JoinResourceDescription;
import com.asakusafw.compiler.flow.join.operator.SideDataCheck;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;

/**
 * Processes {@link SideDataCheck} operators.
 */
@TargetOperator(SideDataCheck.class)
public class SideDataCheckFlowProcessor extends LineEndProcessor {

    @Override
    public void emitLineEnd(Context context) {
        FlowResourceDescription resource = context.getResourceDescription(SideDataCheck.ID_RESOURCE_MASTER);
        SideDataKindFlowAnalyzer helper = new SideDataKindFlowAnalyzer(
                context,
                (JoinResourceDescription) resource);

        ModelFactory f = context.getModelFactory();

        FlowElementPortDescription foundPort = context.getOutputPort(SideDataCheck.ID_OUTPUT_FOUND);
        FlowElementPortDescription missedPort = context.getOutputPort(SideDataCheck.ID_OUTPUT_MISSED);
        ResultMirror found = context.getOutput(foundPort);
        ResultMirror missed = context.getOutput(missedPort);

        context.add(f.newIfStatement(
                helper.getHasMasterExpresion(),
                f.newBlock(found.createAdd(context.getInput())),
                f.newBlock(missed.createAdd(context.getInput()))));
    }
}
