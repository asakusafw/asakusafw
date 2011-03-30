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
package com.asakusafw.compiler.flow.processor;

import java.lang.reflect.Type;

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.compiler.flow.ShuffleDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.model.SummarizedModel;
import com.asakusafw.vocabulary.operator.Summarize;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.Models;

/**
 * {@link Summarize 単純集計演算子}を処理する。
 */
@TargetOperator(Summarize.class)
public class SummarizeFlowProcessor extends RendezvousProcessor {

    @Override
    public ShuffleDescription getShuffleDescription(
            FlowElementDescription element,
            FlowElementPortDescription port) {
        FlowElementPortDescription output = element.getOutputPorts().get(Summarize.ID_OUTPUT);
        LinePartProcessor line = new Prologue(output.getDataType());
        line.initialize(getEnvironment());
        return new ShuffleDescription(
                output.getDataType(),
                port.getShuffleKey(),
                line);
    }

    @Override
    public void emitRendezvous(Context context) {
        ModelFactory f = context.getModelFactory();

        FlowElementPortDescription input = context.getInputPort(Summarize.ID_INPUT);
        FlowElementPortDescription output = context.getOutputPort(Summarize.ID_OUTPUT);

        Expression init = context.createField(boolean.class, "initialized");
        context.addBegin(new ExpressionBuilder(f, init)
            .assignFrom(Models.toLiteral(f, false))
            .toStatement());

        DataObjectMirror cache = context.createModelCache(output.getDataType());
        Expression proc = context.getProcessInput(input);

        context.addProcess(input, f.newIfStatement(
                init,
                f.newBlock(
                        new ExpressionBuilder(context.getModelFactory(), cache.get())
                            .method(SummarizedModel.Interface.METHOD_NAME_COMBINE_SUMMARIZATION, proc)
                            .toStatement()),
                f.newBlock(
                        cache.createSet(proc),
                        new ExpressionBuilder(f, init)
                            .assignFrom(Models.toLiteral(f, true))
                            .toStatement())));

        ResultMirror result = context.getOutput(output);
        context.addEnd(result.createAdd(cache.get()));
    }

    @Override
    public boolean isPartial(FlowElementDescription description) {
        return true;
    }

    static class Prologue extends LinePartProcessor {

        private Type type;

        Prologue(Type type) {
            assert type != null;
            this.type = type;
        }

        @Override
        public void emitLinePart(Context context) {
            Expression input = context.getInput();
            DataObjectMirror cache = context.createModelCache(type);
            context.add(new ExpressionBuilder(context.getModelFactory(), cache.get())
                .method(SummarizedModel.Interface.METHOD_NAME_START_SUMMARIZATION, input)
                .toStatement());
            context.setOutput(cache.get());
        }
    }
}
