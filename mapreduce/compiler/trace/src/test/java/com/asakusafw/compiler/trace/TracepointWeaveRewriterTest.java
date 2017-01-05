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
package com.asakusafw.compiler.trace;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.compiler.flow.FlowGraphGenerator;
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.trace.model.TraceSetting;
import com.asakusafw.trace.model.TraceSetting.Mode;
import com.asakusafw.trace.model.Tracepoint;
import com.asakusafw.trace.model.Tracepoint.PortKind;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Test for {@link TracepointWeaveRewriter}.
 */
public class TracepointWeaveRewriterTest {

    private final FlowGraphGenerator gen = new FlowGraphGenerator();

    /**
     * no trace settings.
     * @throws Exception if failed
     */
    @Test
    public void nothing() throws Exception {
        gen.defineInput("in");
        gen.defineOperator(getClass(), "simple", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "simple.in");
        gen.connect("simple.out", "out");
        rewrite();
        assertThat(succ(gen.get("in")), is(gen.get("simple")));
        assertThat(succ(gen.get("simple")), is(gen.get("out")));
    }

    /**
     * simple trace.
     * @throws Exception if failed
     */
    @Test
    public void simple_input() throws Exception {
        gen.defineInput("in");
        gen.defineOperator(getClass(), "simple", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "simple.in");
        gen.connect("simple.out", "out");
        rewrite(in_trace(getClass(), "simple", "in", Mode.STRICT));
        FlowElement weave = succ(gen.get("in"));
        assertThat(weave, is(pred(gen.get("simple"))));
        assertThat(succ(gen.get("simple")), is(gen.get("out")));
    }

    /**
     * simple trace.
     * @throws Exception if failed
     */
    @Test
    public void simple_output() throws Exception {
        gen.defineInput("in");
        gen.defineOperator(getClass(), "simple", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "simple.in");
        gen.connect("simple.out", "out");
        rewrite(out_trace(getClass(), "simple", "out", Mode.STRICT));
        assertThat(succ(gen.get("in")), is(gen.get("simple")));
        FlowElement weave = succ(gen.get("simple"));
        assertThat(weave, is(pred(gen.get("out"))));
    }

    private TraceSetting in_trace(Class<?> operatorClass, String operatorMethodName, String portName, Mode mode) {
        return new TraceSetting(
                new Tracepoint(operatorClass.getName(), operatorMethodName, PortKind.INPUT, portName),
                mode, attr());
    }

    private TraceSetting out_trace(Class<?> operatorClass, String operatorMethodName, String portName, Mode mode) {
        return new TraceSetting(
                new Tracepoint(operatorClass.getName(), operatorMethodName, PortKind.OUTPUT, portName),
                mode, attr());
    }

    private Map<String, String> attr() {
        return Collections.emptyMap();
    }

    private FlowGraph rewrite(TraceSetting... settings) {
        FlowGraph graph = gen.toGraph();
        TracepointWeaveRewriter.rewrite(graph, Arrays.asList(settings));
        return graph;
    }

    private FlowElement pred(FlowElement elem) {
        return single(FlowGraphUtil.getPredecessors(elem));
    }

    private FlowElement succ(FlowElement elem) {
        return single(FlowGraphUtil.getSuccessors(elem));
    }

    private <T> T single(Iterable<T> collection) {
        Iterator<T> iter = collection.iterator();
        assert iter.hasNext() : collection;
        T result = iter.next();
        assert iter.hasNext() == false : collection;
        return result;
    }
}
