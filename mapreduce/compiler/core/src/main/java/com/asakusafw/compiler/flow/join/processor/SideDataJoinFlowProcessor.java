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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.compiler.flow.join.JoinResourceDescription;
import com.asakusafw.compiler.flow.join.operator.SideDataJoin;
import com.asakusafw.runtime.util.TypeUtil;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.model.Joined;

/**
 * Processes {@link SideDataJoin}.
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

        FlowElementPortDescription joinedPort = context.getOutputPort(SideDataJoin.ID_OUTPUT_JOINED);
        FlowElementPortDescription missedPort = context.getOutputPort(SideDataJoin.ID_OUTPUT_MISSED);

        DataObjectMirror resultCache = context.createModelCache(joinedPort.getDataType());
        DataClass outputType = getEnvironment().getDataClasses().load(joinedPort.getDataType());
        List<Statement> process = new ArrayList<>();
        process.add(resultCache.createReset());
        Joined annotation = TypeUtil.erase(joinedPort.getDataType()).getAnnotation(Joined.class);
        Set<String> saw = new HashSet<>();
        for (Joined.Term term : annotation.terms()) {
            DataClass inputType = getEnvironment().getDataClasses().load(term.source());
            Expression input;
            if (term.source().equals(context.getInputPort(SideDataJoin.ID_INPUT_TRANSACTION).getDataType())) {
                input = context.getInput();
            } else {
                input = helper.getGetRawMasterExpression();
            }
            for (Joined.Mapping mapping : term.mappings()) {
                if (saw.contains(mapping.destination())) {
                    continue;
                }
                saw.add(mapping.destination());
                Property sourceProperty = inputType.findProperty(mapping.source());
                Property destinationProperty = outputType.findProperty(mapping.destination());
                process.add(destinationProperty.createSetter(
                        resultCache.get(),
                        sourceProperty.createGetter(input)));
            }
        }
        ResultMirror joined = context.getOutput(joinedPort);
        process.add(joined.createAdd(resultCache.get()));

        ResultMirror missed = context.getOutput(missedPort);
        context.add(f.newIfStatement(
                helper.getHasMasterExpresion(),
                f.newBlock(process),
                f.newBlock(missed.createAdd(context.getInput()))));
    }
}
