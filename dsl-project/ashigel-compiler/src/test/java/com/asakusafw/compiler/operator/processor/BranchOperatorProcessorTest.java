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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;

/**
 * Test for {@link BranchOperatorProcessor}.
 */
public class BranchOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純な例。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new BranchOperatorProcessor());

        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> high = MockOut.of(MockHoge.class, "high");
        MockOut<MockHoge> middle = MockOut.of(MockHoge.class, "middle");
        MockOut<MockHoge> low = MockOut.of(MockHoge.class, "low");
        Object branch = invoke(factory, "example", in);
        high.add(output(MockHoge.class, branch, "high"));
        middle.add(output(MockHoge.class, branch, "middle"));
        low.add(output(MockHoge.class, branch, "low"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("high", "middle", "low"));
    }

    /**
     * パラメータ化。
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new BranchOperatorProcessor());

        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> high = MockOut.of(MockHoge.class, "high");
        MockOut<MockHoge> middle = MockOut.of(MockHoge.class, "middle");
        MockOut<MockHoge> low = MockOut.of(MockHoge.class, "low");
        Object branch = invoke(factory, "example", in, 500, 200);
        high.add(output(MockHoge.class, branch, "high"));
        middle.add(output(MockHoge.class, branch, "middle"));
        low.add(output(MockHoge.class, branch, "low"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("Parameterized.example"), isJust("high", "middle", "low"));
    }

    /**
     * ジェネリックメソッド。
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new BranchOperatorProcessor());

        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> high = MockOut.of(MockHoge.class, "high");
        MockOut<MockHoge> middle = MockOut.of(MockHoge.class, "middle");
        MockOut<MockHoge> low = MockOut.of(MockHoge.class, "low");
        Object branch = invoke(factory, "example", in);
        high.add(output(MockHoge.class, branch, "high"));
        middle.add(output(MockHoge.class, branch, "middle"));
        low.add(output(MockHoge.class, branch, "low"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("high", "middle", "low"));
    }

    /**
     * Special words.
     */
    @Test
    public void special_words() {
        add("com.example.Reserved");
        add("com.example.ReservedEnum");
        ClassLoader loader = start(new BranchOperatorProcessor());

        create(loader, "com.example.ReservedFactory");
    }

    /**
     * with normal field.
     */
    @Test
    public void normal_field() {
        add("com.example.NormalField");
        add("com.example.NormalFieldEnum");
        ClassLoader loader = start(new BranchOperatorProcessor());

        Object factory = create(loader, "com.example.NormalFieldFactory");
        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        Object branch = invoke(factory, "example", in);

        Set<String> fieldNames = new HashSet<String>();
        for (Field field : branch.getClass().getFields()) {
            fieldNames.add(field.getName());
        }
        assertThat(fieldNames, containsInAnyOrder("low", "middle", "high"));
    }

    /**
     * 空の列挙。
     */
    @Test
    public void emptyEnum() {
        add("com.example.Empty");
        add("com.example.EmptyEnum");
        error(new BranchOperatorProcessor());
    }

    /**
     * 非列挙。
     */
    @Test
    public void notEnum() {
        add("com.example.NotEnum");
        add("com.example.EmptyEnum");
        error(new BranchOperatorProcessor());
    }

    /**
     * With a not public enum.
     */
    @Test
    public void notPublicEnum() {
        add("com.example.NotPublicEnum");
        error(new BranchOperatorProcessor());
    }

    /**
     * 非モデル。
     */
    @Test
    public void notModel() {
        add("com.example.NotModel");
        add("com.example.EmptyEnum");
        error(new BranchOperatorProcessor());
    }

    /**
     * 抽象メソッド。
     */
    @Test
    public void _abstract() {
        add("com.example.Abstract");
        add("com.example.EmptyEnum");
        error(new BranchOperatorProcessor());
    }

    /**
     * ユーザー定義パラメーターでない。
     */
    @Test
    public void notUserParameter() {
        add("com.example.NotUserParameter");
        add("com.example.EmptyEnum");
        error(new BranchOperatorProcessor());
    }
}
