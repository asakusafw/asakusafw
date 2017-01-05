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

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.compiler.flow.ShuffleDescription;
import com.asakusafw.runtime.util.TypeUtil;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.model.Summarized;
import com.asakusafw.vocabulary.model.Summarized.Aggregator;
import com.asakusafw.vocabulary.operator.Summarize;

/**
 * Processes {@link Summarize} operators.
 */
@TargetOperator(Summarize.class)
public class SummarizeFlowProcessor extends RendezvousProcessor {

    @Override
    public ShuffleDescription getShuffleDescription(
            FlowElementDescription element,
            FlowElementPortDescription port) {
        FlowElementPortDescription output = element.getOutputPorts().get(Summarize.ID_OUTPUT);
        LinePartProcessor line = new Prologue(port.getDataType(), output.getDataType());
        line.initialize(getEnvironment());
        return new ShuffleDescription(
                output.getDataType(),
                rebuildShuffleKey(output, port),
                line);
    }

    private ShuffleKey rebuildShuffleKey(FlowElementPortDescription output, FlowElementPortDescription input) {
        assert output != null;
        assert input != null;
        Summarized summarized = TypeUtil.erase(output.getDataType()).getAnnotation(Summarized.class);
        if (summarized == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Internal Error: {0} does not declared in {1} ({2})", //$NON-NLS-1$
                    Summarized.class.getSimpleName(),
                    output.getDataType(),
                    output));
        }

        Map<String, String> mapping = new HashMap<>();
        for (Summarized.Folding folding : summarized.term().foldings()) {
            if (folding.aggregator() == Aggregator.ANY) {
                mapping.put(folding.source(), folding.destination());
            }
        }
        List<String> remapped = new ArrayList<>();
        for (String original : input.getShuffleKey().getGroupProperties()) {
            String target = mapping.get(original);
            if (target == null) {
                throw new IllegalStateException(MessageFormat.format(
                        "Internal Error: Grouping key mismatched (output={0}, grouping={1}, mapping={2})", //$NON-NLS-1$
                        output.getDataType(),
                        input.getShuffleKey(),
                        mapping));
            }
            remapped.add(target);
        }
        return new ShuffleKey(remapped, Collections.emptyList());
    }

    @Override
    public void emitRendezvous(Context context) {
        ModelFactory f = context.getModelFactory();

        FlowElementPortDescription input = context.getInputPort(Summarize.ID_INPUT);
        FlowElementPortDescription output = context.getOutputPort(Summarize.ID_OUTPUT);

        Expression init = context.createField(boolean.class, "initialized"); //$NON-NLS-1$
        context.addBegin(new ExpressionBuilder(f, init)
            .assignFrom(Models.toLiteral(f, false))
            .toStatement());

        DataObjectMirror cache = context.createModelCache(output.getDataType());

        List<Statement> combine = new ArrayList<>();
        DataClass outputType = getEnvironment().getDataClasses().load(output.getDataType());
        Summarized summarized = TypeUtil.erase(output.getDataType()).getAnnotation(Summarized.class);
        for (Summarized.Folding folding : summarized.term().foldings()) {
            if (folding.aggregator() == Aggregator.ANY) {
                continue;
            }
            combine.add(createAddSummarizeFor(context, folding, outputType, cache.get()));
        }
        context.addProcess(input, f.newIfStatement(
                init,
                f.newBlock(combine),
                f.newBlock(
                        cache.createSet(context.getProcessInput(input)),
                        new ExpressionBuilder(f, init)
                            .assignFrom(Models.toLiteral(f, true))
                            .toStatement())));

        ResultMirror result = context.getOutput(output);
        context.addEnd(result.createAdd(cache.get()));
    }

    private Statement createAddSummarizeFor(
            Context context,
            Summarized.Folding folding,
            DataClass summarizing,
            Expression outputCache) {
        assert context != null;
        assert folding != null;
        assert summarizing != null;
        assert outputCache != null;
        Property property = summarizing.findProperty(folding.destination());
        Expression input = context.getProcessInput(context.getInputPort(Summarize.ID_INPUT));
        ModelFactory f = context.getModelFactory();
        // TODO for only DMDL
        switch (folding.aggregator()) {
        case MAX:
            return new ExpressionBuilder(f, property.createGetter(outputCache))
                .method("max", property.createGetter(input)) //$NON-NLS-1$
                .toStatement();
        case MIN:
            return new ExpressionBuilder(f, property.createGetter(outputCache))
                .method("min", property.createGetter(input)) //$NON-NLS-1$
                .toStatement();
        case SUM:
        case COUNT:
            return new ExpressionBuilder(f, property.createGetter(outputCache))
                .method("add", property.createGetter(input)) //$NON-NLS-1$
                .toStatement();
        default:
            throw new AssertionError();
        }
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

    static class Prologue extends LinePartProcessor {

        private final Type inputType;

        private final Type outputType;

        Prologue(Type inputType, Type outputType) {
            assert inputType != null;
            assert outputType != null;
            this.inputType = inputType;
            this.outputType = outputType;
        }

        @Override
        public void emitLinePart(Context context) {
            Summarized summarized = TypeUtil.erase(outputType).getAnnotation(Summarized.class);
            DataObjectMirror cache = context.createModelCache(outputType);
            DataClass inputData = getEnvironment().getDataClasses().load(inputType);
            DataClass outputData = getEnvironment().getDataClasses().load(outputType);
            for (Summarized.Folding folding : summarized.term().foldings()) {
                context.add(createStartSummarizeFor(context, folding, inputData, outputData, cache.get()));
            }
            context.setOutput(cache.get());
        }

        private Statement createStartSummarizeFor(
                Context context,
                Summarized.Folding folding,
                DataClass input,
                DataClass output,
                Expression outputCache) {
            Property source = input.findProperty(folding.source());
            Property destination = output.findProperty(folding.destination());
            ModelFactory f = context.getModelFactory();
            // TODO for only DMDL
            switch (folding.aggregator()) {
            case ANY:
                return destination.createSetter(
                        outputCache,
                        source.createGetter(context.getInput()));
            case MAX:
            case MIN:
            case SUM:
                return new ExpressionBuilder(f, destination.createGetter(outputCache))
                    .method("modify", new ExpressionBuilder(f, source.createGetter(context.getInput())) //$NON-NLS-1$
                        .method("get") //$NON-NLS-1$
                        .toExpression())
                    .toStatement();
            case COUNT:
                return new ExpressionBuilder(f, destination.createGetter(outputCache))
                    .method("modify", Models.toLiteral(f, 1L)) //$NON-NLS-1$
                    .toStatement();
            default:
                throw new AssertionError();
            }
        }
    }
}
