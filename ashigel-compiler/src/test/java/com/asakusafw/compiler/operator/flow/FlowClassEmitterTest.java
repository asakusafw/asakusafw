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
package com.asakusafw.compiler.operator.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;


import org.junit.Test;

import com.asakusafw.compiler.operator.Callback;
import com.asakusafw.compiler.operator.OperatorCompilerException;
import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.flow.FlowClassEmitter;
import com.asakusafw.compiler.operator.flow.FlowPartClass;
import com.asakusafw.compiler.operator.flow.FlowPartClassCollector;
import com.asakusafw.compiler.operator.model.MockFoo;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;
import com.ashigeru.util.graph.Graph;

/**
 * Test for {@link FlowClassEmitter}.
 */
public class FlowClassEmitterTest extends OperatorCompilerTestRoot {

    /**
     * 単純な例。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void simple() throws Exception {
        add("com.example.Store");
        add("com.example.Simple");
        ClassLoader loader = start(new Collector());

        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> out = MockOut.of(MockHoge.class, "out");

        Object operator = invoke(factory, "create", in);
        assertStored(loader, "OK");

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
     * パラメータ化。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void parameterized() throws Exception {
        add("com.example.Store");
        add("com.example.Parameterized");
        ClassLoader loader = start(new Collector());

        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");
        MockOut<MockHoge> c = MockOut.of(MockHoge.class, "c");
        MockOut<MockFoo> d = MockOut.of(MockFoo.class, "d");

        Object operator = invoke(factory, "create", a, b, 100, "Hello");
        assertStored(loader, "Hello100");

        Source<MockHoge> flowC = output(MockHoge.class, operator, "out1");
        Source<MockFoo> flowD = output(MockFoo.class, operator, "out2");
        c.add(flowC);
        d.add(flowD);


        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Parameterized"));
        assertThat(graph.getConnected("b"), isJust("Parameterized"));
        assertThat(graph.getConnected("Parameterized"), isJust("c", "d"));
    }

    private static void assertStored(ClassLoader loader, Object expected) {
        try {
            Class<?> store = loader.loadClass("com.example.Store");
            Field field = store.getDeclaredField("result");
            Object result = field.get(null);
            assertThat(result, is(expected));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static class Collector extends Callback {

        Collector() {
            return;
        }

        @Override
        protected final void test() {
            try {
                FlowPartClassCollector collector = new FlowPartClassCollector(env);
                Set<? extends Element> annotated = round.getElementsAnnotatedWith(FlowPart.class);
                if (annotated.isEmpty()) {
                    return;
                }
                for (Element elem : annotated) {
                    collector.add(elem);
                }
                List<FlowPartClass> results = collector.collect();
                assertThat(results.size(), is(1));

                FlowClassEmitter emitter = new FlowClassEmitter(env);
                emitter.emit(results.get(0));
            } catch (OperatorCompilerException e) {
                // 大域脱出のためだけなのでスキップ
            }
        }
    }
}
