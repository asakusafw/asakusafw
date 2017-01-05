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
package com.asakusafw.compiler.operator.processor;

import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;

/**
 * Test for {@link GroupSortOperatorProcessor}.
 */
public class GroupSortOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new GroupSortOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> a = MockOut.of(MockHoge.class, "a");
        MockOut<MockHoge> b = MockOut.of(MockHoge.class, "b");
        Object gs = invoke(factory, "example", in);
        a.add(output(MockHoge.class, gs, "first"));
        b.add(output(MockHoge.class, gs, "last"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("a", "b"));
    }

    /**
     * parameterized.
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        ClassLoader loader = start(new GroupSortOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> a = MockOut.of(MockHoge.class, "a");
        MockOut<MockHoge> b = MockOut.of(MockHoge.class, "b");
        Object gs = invoke(factory, "example", in, 1);
        a.add(output(MockHoge.class, gs, "first"));
        b.add(output(MockHoge.class, gs, "last"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("Parameterized.example"), isJust("a", "b"));
    }

    /**
     * w/ iterable input.
     */
    @Test
    public void iterable_input() {
        add("com.example.InputIterable");
        ClassLoader loader = start(new GroupSortOperatorProcessor());
        Object factory = create(loader, "com.example.InputIterableFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> a = MockOut.of(MockHoge.class, "a");
        MockOut<MockHoge> b = MockOut.of(MockHoge.class, "b");
        Object gs = invoke(factory, "example", in);
        a.add(output(MockHoge.class, gs, "first"));
        b.add(output(MockHoge.class, gs, "last"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("InputIterable.example"));
        assertThat(graph.getConnected("InputIterable.example"), isJust("a", "b"));
    }

    /**
     * generic method.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        ClassLoader loader = start(new GroupSortOperatorProcessor());
        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> a = MockOut.of(MockHoge.class, "a");
        MockOut<MockHoge> b = MockOut.of(MockHoge.class, "b");
        Object gs = invoke(factory, "example", in);
        a.add(output(MockHoge.class, gs, "first"));
        b.add(output(MockHoge.class, gs, "last"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("a", "b"));
    }

    /**
     * w/ abstract modifier.
     */
    @Test
    public void Abstract() {
        add("com.example.Abstract");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * missing key annotation.
     */
    @Test
    public void NoKey() {
        add("com.example.NoKey");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * w/o result parameters.
     */
    @Test
    public void NoResults() {
        add("com.example.NoResults");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * not a list parameter.
     */
    @Test
    public void NotList() {
        add("com.example.NotList");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * not a data model parameter.
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * not a result paraemer.
     */
    @Test
    public void NotResult() {
        add("com.example.NotResult");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * not a valid user parameters.
     */
    @Test
    public void NotUserParameter() {
        add("com.example.NotUserParameter");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * not void type method.
     */
    @Test
    public void NotVoid() {
        add("com.example.NotVoid");
        error(new GroupSortOperatorProcessor());
    }

    /**
     * output port uses unbound type variables.
     */
    @Test
    public void UnboundGenerics() {
        add("com.example.UnboundGenerics");
        error(new GroupSortOperatorProcessor());
    }
}
