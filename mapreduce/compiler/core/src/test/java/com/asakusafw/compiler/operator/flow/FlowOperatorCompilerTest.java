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
package com.asakusafw.compiler.operator.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;

/**
 * Test for {@link FlowOperatorCompiler}.
 */
public class FlowOperatorCompilerTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     * @throws Exception if exception was occurred
     */
    @Test
    public void simple() throws Exception {
        add("com.example.Simple");
        ClassLoader loader = start(new FlowOperatorCompiler());

        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> out = MockOut.of(MockHoge.class, "out");

        Object operator = invoke(factory, "create", in);

        Source<MockHoge> flowOut = output(MockHoge.class, operator, "out");
        out.add(flowOut);

        FlowPartDescription desc = (FlowPartDescription) flowOut
            .toOutputPort()
            .getOwner()
            .getDescription();

        assertThat(desc.getInputPorts().size(), is(1));
        assertThat(desc.getInputPorts().get(0).getName(), is("in"));
        assertThat(desc.getInputPorts().get(0).getDataType(), is((Object) MockHoge.class));
        assertThat(desc.getOutputPorts().size(), is(1));
        assertThat(desc.getOutputPorts().get(0).getName(), is("out"));
        assertThat(desc.getOutputPorts().get(0).getDataType(), is((Object) MockHoge.class));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Simple"));
        assertThat(graph.getConnected("Simple"), isJust("out"));

        FlowGraph flow = desc.getFlowGraph();
        assertThat(flow.getDescription(), is((Object) loader.loadClass("com.example.Simple")));
        assertThat(flow.getFlowInputs().size(), is(1));
        assertThat(flow.getFlowInputs().get(0).getDescription().getName(), is("in"));
        assertThat(flow.getFlowInputs().get(0).getDescription().getDataType(), is((Object) MockHoge.class));
        assertThat(flow.getFlowOutputs().size(), is(1));
        assertThat(flow.getFlowOutputs().get(0).getDescription().getName(), is("out"));
        assertThat(flow.getFlowOutputs().get(0).getDescription().getDataType(), is((Object) MockHoge.class));

        Graph<String> inner = toGraph(flow.getFlowInputs());
        assertThat(inner.getConnected("in"), isJust("out"));
    }

    /**
     * generic flow parts.
     * @throws Exception if test was failed
     */
    @Test
    public void generics() throws Exception {
        add("com.example.Generic");
        ClassLoader loader = start(new FlowOperatorCompiler());

        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> out = MockOut.of(MockHoge.class, "out");

        Object operator = invoke(factory, "create", in);

        Source<MockHoge> flowOut = output(MockHoge.class, operator, "out");
        out.add(flowOut);

        FlowPartDescription desc = (FlowPartDescription) flowOut
            .toOutputPort()
            .getOwner()
            .getDescription();

        assertThat(desc.getInputPorts().size(), is(1));
        assertThat(desc.getInputPorts().get(0).getName(), is("in"));
        assertThat(desc.getInputPorts().get(0).getDataType(), is((Object) MockHoge.class));
        assertThat(desc.getOutputPorts().size(), is(1));
        assertThat(desc.getOutputPorts().get(0).getName(), is("out"));
        assertThat(desc.getOutputPorts().get(0).getDataType(), is((Object) MockHoge.class));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Generic"));
        assertThat(graph.getConnected("Generic"), isJust("out"));

        FlowGraph flow = desc.getFlowGraph();
        assertThat(flow.getDescription(), is((Object) loader.loadClass("com.example.Generic")));
        assertThat(flow.getFlowInputs().size(), is(1));
        assertThat(flow.getFlowInputs().get(0).getDescription().getName(), is("in"));
        assertThat(flow.getFlowInputs().get(0).getDescription().getDataType(), is((Object) MockHoge.class));
        assertThat(flow.getFlowOutputs().size(), is(1));
        assertThat(flow.getFlowOutputs().get(0).getDescription().getName(), is("out"));
        assertThat(flow.getFlowOutputs().get(0).getDescription().getDataType(), is((Object) MockHoge.class));

        Graph<String> inner = toGraph(flow.getFlowInputs());
        assertThat(inner.getConnected("in"), isJust("out"));
    }
}
