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

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.operator.Fold;

/**
 * Processes {@link Fold} operators.
 */
@TargetOperator(Fold.class)
public class FoldFlowProcessor extends RendezvousProcessor {

    @Override
    public void emitRendezvous(Context context) {
        ModelFactory f = context.getModelFactory();
        OperatorDescription desc = context.getOperatorDescription();

        FlowElementPortDescription input = context.getInputPort(Fold.ID_INPUT);
        FlowElementPortDescription output = context.getOutputPort(Fold.ID_OUTPUT);

        Expression init = context.createField(boolean.class, "initialized"); //$NON-NLS-1$
        context.addBegin(new ExpressionBuilder(f, init)
            .assignFrom(Models.toLiteral(f, false))
            .toStatement());

        DataObjectMirror cache = context.createModelCache(output.getDataType());
        Expression impl = context.createImplementation();
        Expression proc = context.getProcessInput(input);
        List<Expression> arguments = new ArrayList<>();
        arguments.add(cache.get());
        arguments.add(proc);
        for (OperatorDescription.Parameter param : desc.getParameters()) {
            arguments.add(Models.toLiteral(f, param.getValue()));
        }

        context.addProcess(input, f.newIfStatement(
                init,
                f.newBlock(new ExpressionBuilder(f, impl)
                    .method(desc.getDeclaration().getName(), arguments)
                    .toStatement()),
                f.newBlock(
                        cache.createSet(proc),
                        new ExpressionBuilder(f, init)
                            .assignFrom(Models.toLiteral(f, true))
                            .toStatement())));

        ResultMirror result = context.getOutput(context.getOutputPort(Fold.ID_OUTPUT));
        context.addEnd(result.createAdd(cache.get()));
    }

    @Override
    public boolean isPartial(FlowElementDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        PartialAggregation partial = description.getAttribute(PartialAggregation.class);
        if (partial == PartialAggregation.PARTIAL) {
            return true;
        } else if (partial == PartialAggregation.TOTAL) {
            return false;
        }
        return getEnvironment().getOptions().isEnableCombiner();
    }
}
