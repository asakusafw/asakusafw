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
package com.asakusafw.compiler.operator.processor;

import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;

/**
 * Test for {@link ExtractOperatorProcessorTest}.
 */
public class ExtractOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純な例。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new ExtractOperatorProcessor());
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
     * パラメータ化。
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        ClassLoader loader = start(new ExtractOperatorProcessor());
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
     * generic method.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        ClassLoader loader = start(new ExtractOperatorProcessor());
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
     * 抽象メソッド。
     */
    @Test
    public void Abstract() {
        add("com.example.Abstract");
        error(new ExtractOperatorProcessor());
    }

    /**
     * 結果がない。
     */
    @Test
    public void NoResults() {
        add("com.example.NoResults");
        error(new ExtractOperatorProcessor());
    }

    /**
     * モデルでない。
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new ExtractOperatorProcessor());
    }

    /**
     * 結果でない。
     */
    @Test
    public void NotResult() {
        add("com.example.NotResult");
        error(new ExtractOperatorProcessor());
    }

    /**
     * ユーザー定義パラメーターでない。
     */
    @Test
    public void NotUserParameter() {
        add("com.example.NotUserParameter");
        error(new ExtractOperatorProcessor());
    }

    /**
     * 結果を返す。
     */
    @Test
    public void NotVoid() {
        add("com.example.NotVoid");
        error(new ExtractOperatorProcessor());
    }

    /**
     * output port uses unbound type variables.
     */
    @Test
    public void UnboundGenerics() {
        add("com.example.UnboundGenerics");
        error(new ExtractOperatorProcessor());
    }
}
