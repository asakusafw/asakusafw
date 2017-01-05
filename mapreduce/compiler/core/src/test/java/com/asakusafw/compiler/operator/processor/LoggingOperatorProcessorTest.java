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
 * Test for {@link LoggingOperatorProcessor}.
 */
public class LoggingOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new LoggingOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> out = MockOut.of(MockHoge.class, "out");
        Object logging = invoke(factory, "example", in);
        out.add(output(MockHoge.class, logging, "out"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("out"));
    }

    /**
     * parameterized.
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        ClassLoader loader = start(new LoggingOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> out = MockOut.of(MockHoge.class, "out");
        Object logging = invoke(factory, "example", in, 100);
        out.add(output(MockHoge.class, logging, "out"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("Parameterized.example"), isJust("out"));
    }

    /**
     * generic method.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        ClassLoader loader = start(new LoggingOperatorProcessor());
        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> out = MockOut.of(MockHoge.class, "out");
        Object logging = invoke(factory, "example", in);
        out.add(output(MockHoge.class, logging, "out"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("out"));
    }

    /**
     * w/ abstract modifier.
     */
    @Test
    public void Abstract() {
        add("com.example.Abstract");
        error(new LoggingOperatorProcessor());
    }

    /**
     * not a data model parameter.
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new LoggingOperatorProcessor());
    }

    /**
     * not returns string.
     */
    @Test
    public void NotString() {
        add("com.example.NotString");
        error(new LoggingOperatorProcessor());
    }

    /**
     * not a valid user parameters.
     */
    @Test
    public void NotUserParameter() {
        add("com.example.NotUserParameter");
        error(new LoggingOperatorProcessor());
    }
}
