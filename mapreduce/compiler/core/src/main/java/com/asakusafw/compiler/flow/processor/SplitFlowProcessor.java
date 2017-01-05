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


import java.util.HashMap;
import java.util.Map;

import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.runtime.util.TypeUtil;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Joined.Term;
import com.asakusafw.vocabulary.operator.Split;

/**
 * Processes {@link Split} operators.
 */
@TargetOperator(Split.class)
public class SplitFlowProcessor extends LineEndProcessor {

    @Override
    public void emitLineEnd(Context context) {
        FlowElementPortDescription inputPort = context.getInputPort(Split.ID_INPUT);
        Joined joined = TypeUtil.erase(inputPort.getDataType()).getAnnotation(Joined.class);
        assert joined != null;
        Map<Class<?>, Term> terms = new HashMap<>();
        for (Term term : joined.terms()) {
            terms.put(term.source(), term);
        }
        for (FlowElementPortDescription output : context.getOperatorDescription().getOutputPorts()) {
            Term term = terms.get(output.getDataType());
            assert term != null;
            emitTerm(context, term, inputPort, output);
        }
    }

    private void emitTerm(
            Context context,
            Joined.Term term,
            FlowElementPortDescription inputPort,
            FlowElementPortDescription outputPort) {
        DataClass inputType = getEnvironment().getDataClasses().load(inputPort.getDataType());
        DataClass outputType = getEnvironment().getDataClasses().load(outputPort.getDataType());
        DataObjectMirror cache = context.createModelCache(term.source());
        context.add(cache.createReset());
        for (Joined.Mapping mapping : term.mappings()) {
            // input: joined(destination), output: origin(source)
            Property source = inputType.findProperty(mapping.destination());
            Property destination = outputType.findProperty(mapping.source());
            context.add(destination.createSetter(
                    cache.get(),
                    source.createGetter(context.getInput())));
        }
        ResultMirror result = context.getOutput(outputPort);
        context.add(result.createAdd(cache.get()));
    }
}
