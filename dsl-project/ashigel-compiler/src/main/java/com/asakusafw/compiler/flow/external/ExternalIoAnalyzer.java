/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.collections.Tuple2;
import com.asakusafw.utils.collections.Tuples;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * 外部I/Oとの連携に関する分析を行う。
 */
public class ExternalIoAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(ExternalIoAnalyzer.class);

    private final FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ExternalIoAnalyzer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のフローグラフに対する入出力が正しい場合のみ{@code true}を返す。
     * @param graph 対象のグラフ
     * @return 指定のフローグラフに対する入出力が正しい場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public boolean validate(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("{}の入出力を検証しています", graph.getDescription().getName());
        List<Tuple2<InputDescription, ExternalIoDescriptionProcessor>> inputs = Lists.create();
        List<Tuple2<OutputDescription, ExternalIoDescriptionProcessor>> outputs = Lists.create();

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
        List<T> results = Lists.create();
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
        Map<Class<?>, ExternalIoDescriptionProcessor> actives = Maps.create();

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
                        "{0}を処理するプロセッサが見つかりませんでした。",
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
