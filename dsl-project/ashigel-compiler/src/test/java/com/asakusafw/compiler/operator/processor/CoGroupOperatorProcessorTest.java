/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * Test for {@link CoGroupOperatorProcessor}.
 */
public class CoGroupOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純なテスト。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new CoGroupOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockHoge> r1 = MockOut.of(MockHoge.class, "r1");
        MockOut<MockFoo> r2 = MockOut.of(MockFoo.class, "r2");

        Object coGroup = invoke(factory, "example", a, b);
        r1.add(output(MockHoge.class, coGroup, "r1"));
        r2.add(output(MockFoo.class, coGroup, "r2"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Simple.example"));
        assertThat(graph.getConnected("b"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("r1", "r2"));
    }

    /**
     * パラメータ化。
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        ClassLoader loader = start(new CoGroupOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockHoge> r1 = MockOut.of(MockHoge.class, "r1");
        MockOut<MockFoo> r2 = MockOut.of(MockFoo.class, "r2");

        Object coGroup = invoke(factory, "example", a, b, 1);
        r1.add(output(MockHoge.class, coGroup, "r1"));
        r2.add(output(MockFoo.class, coGroup, "r2"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("b"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("Parameterized.example"), isJust("r1", "r2"));
    }

    /**
     * ジェネリックメソッド。
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        ClassLoader loader = start(new CoGroupOperatorProcessor());
        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockHoge> r1 = MockOut.of(MockHoge.class, "r1");
        MockOut<MockFoo> r2 = MockOut.of(MockFoo.class, "r2");

        Object coGroup = invoke(factory, "example", a, b);
        r1.add(output(MockHoge.class, coGroup, "r1"));
        r2.add(output(MockFoo.class, coGroup, "r2"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Generic.example"));
        assertThat(graph.getConnected("b"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("r1", "r2"));
    }

    /**
     * 抽象メソッド。
     */
    @Test
    public void _abstract() {
        add("com.example.Abstract");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * 結果がない。
     */
    @Test
    public void noResult() {
        add("com.example.NoResult");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * リストでない。
     */
    @Test
    public void notList() {
        add("com.example.NotList");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * 結果でない。
     */
    @Test
    public void notResult() {
        add("com.example.NotList");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * モデルでない。
     */
    @Test
    public void notModel() {
        add("com.example.NotModel");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * voidでない。
     */
    @Test
    public void notVoid() {
        add("com.example.NotVoid");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * Key is not specified.
     */
    @Test
    public void noKey() {
        add("com.example.NoKey");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * Grouping key has empty string.
     */
    @Test
    public void emptyStringGroup() {
        add("com.example.EmptyStringGroup");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * Ordering key has empty string.
     */
    @Test
    public void emptyStringOrder() {
        add("com.example.EmptyStringOrder");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * ユーザー引数でない。
     */
    @Test
    public void notUserParameter() {
        add("com.example.NotUserParameter");
        error(new CoGroupOperatorProcessor());
    }

    /**
     * 出力が不明なジェネリックメソッド。
     */
    @Test
    public void unboundGenerics() {
        add("com.example.UnboundGenerics");
        error(new CoGroupOperatorProcessor());
    }
}
