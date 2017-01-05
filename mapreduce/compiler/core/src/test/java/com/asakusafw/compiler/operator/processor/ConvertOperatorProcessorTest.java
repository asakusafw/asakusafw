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
import com.asakusafw.compiler.operator.model.MockFoo;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;

/**
 * Test for {@link ConvertOperatorProcessor}.
 */
public class ConvertOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new ConvertOperatorProcessor());

        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> orig = MockOut.of(MockHoge.class, "orig");
        MockOut<MockFoo> out = MockOut.of(MockFoo.class, "out");
        Object update = invoke(factory, "example", in);
        orig.add(output(MockHoge.class, update, "original"));
        out.add(output(MockFoo.class, update, "out"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("out", "orig"));
    }

    /**
     * parameterized.
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        ClassLoader loader = start(new ConvertOperatorProcessor());

        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> orig = MockOut.of(MockHoge.class, "orig");
        MockOut<MockFoo> out = MockOut.of(MockFoo.class, "out");
        Object update = invoke(factory, "example", in, 100);
        orig.add(output(MockHoge.class, update, "original"));
        out.add(output(MockFoo.class, update, "out"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("Parameterized.example"), isJust("out", "orig"));
    }

    /**
     * generic method.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        ClassLoader loader = start(new ConvertOperatorProcessor());

        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> orig = MockOut.of(MockHoge.class, "orig");
        MockOut<MockFoo> out = MockOut.of(MockFoo.class, "out");
        Object update = invoke(factory, "example", in);
        orig.add(output(MockHoge.class, update, "original"));
        out.add(output(MockFoo.class, update, "out"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("out", "orig"));
    }

    /**
     * w/ abstract modifier.
     */
    @Test
    public void _abstract() {
        add("com.example.Abstract");
        error(new ConvertOperatorProcessor());
    }

    /**
     * not a valid user parameters.
     */
    @Test
    public void notUserParameter() {
        add("com.example.NotUserParameter");
        error(new ConvertOperatorProcessor());
    }

    /**
     * returns void type.
     */
    @Test
    public void returnsVoid() {
        add("com.example.ReturnsVoid");
        error(new ConvertOperatorProcessor());
    }

    /**
     * not a data model parameter.
     */
    @Test
    public void notModel() {
        add("com.example.NotModel");
        error(new ConvertOperatorProcessor());
    }

    /**
     * returns type variables.
     */
    @Test
    public void returnsTypeVariable() {
        add("com.example.ReturnsTypeVariable");
        error(new ConvertOperatorProcessor());
    }
}
