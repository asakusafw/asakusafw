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
package com.asakusafw.compiler.flow.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.Repository;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.collections.Tuple2;
import com.asakusafw.utils.collections.Tuples;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Analyzes external I/O descriptions in each jobflow.
 */
public class ExternalIoAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(ExternalIoAnalyzer.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExternalIoAnalyzer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Returns whether the external I/O descriptions in the target flow graph are valid or not.
     * @param graph the target flow graph
     * @return {@code true} if the all external I/O descriptions are valid, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public boolean validate(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("validating external I/O: {}", graph.getDescription().getName()); //$NON-NLS-1$
        List<Tuple2<InputDescription, ExternalIoDescriptionProcessor>> inputs = new ArrayList<>();
        List<Tuple2<OutputDescription, ExternalIoDescriptionProcessor>> outputs = new ArrayList<>();

        if (collect(graph, inputs, outputs) == false) {
            return false;
        }

        boolean valid = true;
        Set<ExternalIoDescriptionProcessor> processors = getActiveProcessors(inputs, outputs);
        for (ExternalIoDescriptionProcessor proc : processors) {
            List<InputDescription> in = getOnly(inputs, proc);
            List<OutputDescription> out = getOnly(outputs, proc);
            valid &= proc.validate(in, out);
        }

        return valid;
    }

    private <T> List<T> getOnly(
            List<Tuple2<T, ExternalIoDescriptionProcessor>> inputs,
            ExternalIoDescriptionProcessor proc) {
        assert inputs != null;
        assert proc != null;
        List<T> results = new ArrayList<>();
        for (Tuple2<T, ExternalIoDescriptionProcessor> tuple : inputs) {
            if (tuple.second.equals(proc)) {
                results.add(tuple.first);
            }
        }
        return results;
    }

    private Set<ExternalIoDescriptionProcessor> getActiveProcessors(
            List<Tuple2<InputDescription, ExternalIoDescriptionProcessor>> inputs,
            List<Tuple2<OutputDescription, ExternalIoDescriptionProcessor>> outputs) {
        assert inputs != null;
        assert outputs != null;
        Map<Class<?>, ExternalIoDescriptionProcessor> actives = new HashMap<>();

        // collect
        for (Tuple2<InputDescription, ExternalIoDescriptionProcessor> tuple : inputs) {
            actives.put(tuple.second.getClass(), tuple.second);
        }
        for (Tuple2<OutputDescription, ExternalIoDescriptionProcessor> tuple : outputs) {
            actives.put(tuple.second.getClass(), tuple.second);
        }

        // normalize
        normalize(inputs, actives);
        normalize(outputs, actives);

        return Sets.from(actives.values());
    }

    private <T> void normalize(
            List<Tuple2<T, ExternalIoDescriptionProcessor>> list,
            Map<Class<?>, ExternalIoDescriptionProcessor> actives) {
        assert list != null;
        assert actives != null;
        for (ListIterator<Tuple2<T, ExternalIoDescriptionProcessor>> iter = list.listIterator();
                iter.hasNext();) {
            Tuple2<T, ExternalIoDescriptionProcessor> tuple = iter.next();
            ExternalIoDescriptionProcessor normal = actives.get(tuple.second.getClass());
            iter.set(Tuples.of(tuple.first, normal));
        }
    }

    private boolean collect(
            FlowGraph graph,
            List<Tuple2<InputDescription, ExternalIoDescriptionProcessor>> inputs,
            List<Tuple2<OutputDescription, ExternalIoDescriptionProcessor>> outputs) {
        assert graph != null;
        assert inputs != null;
        assert outputs != null;
        boolean valid = true;
        Repository externals = environment.getExternals();
        for (FlowIn<?> port : graph.getFlowInputs()) {
            InputDescription desc = port.getDescription();
            ExternalIoDescriptionProcessor processor = externals.findProcessor(desc);
            if (processor != null) {
                inputs.add(Tuples.of(desc, processor));
            } else {
                environment.error(
                        Messages.getString("ExternalIoAnalyzer.errorMissingProcessor"), //$NON-NLS-1$
                        desc.getClass().getName());
                valid = false;
            }
        }
        for (FlowOut<?> port : graph.getFlowOutputs()) {
            OutputDescription desc = port.getDescription();
            ExternalIoDescriptionProcessor processor = externals.findProcessor(desc);
            if (processor != null) {
                outputs.add(Tuples.of(desc, processor));
            } else {
                valid = false;
            }
        }
        return valid;
    }
}
