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
package com.asakusafw.compiler.operator.processor;

import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.model.MockFoo;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.compiler.operator.model.MockKeyValue1;
import com.asakusafw.compiler.operator.model.MockKeyValue2;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;
import com.ashigeru.util.graph.Graph;

/**
 * Test for {@link MasterBranchOperatorProcessor}.
 */
public class MasterBranchOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純なテスト。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new MasterBranchOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> unknown = MockOut.of(MockFoo.class, "unknown");
        MockOut<MockFoo> high = MockOut.of(MockFoo.class, "high");
        MockOut<MockFoo> middle = MockOut.of(MockFoo.class, "middle");
        MockOut<MockFoo> low = MockOut.of(MockFoo.class, "low");

        Object masterBranch = invoke(factory, "example", a, b);
        unknown.add(output(MockFoo.class, masterBranch, "unknown"));
        high.add(output(MockFoo.class, masterBranch, "high"));
        middle.add(output(MockFoo.class, masterBranch, "middle"));
        low.add(output(MockFoo.class, masterBranch, "low"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Simple.example"));
        assertThat(graph.getConnected("b"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("unknown", "high", "middle", "low"));
    }

    /**
     * マスタ選択つき。
     */
    @Test
    public void selector() {
        add("com.example.Selector");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new MasterBranchOperatorProcessor());
        Object factory = create(loader, "com.example.SelectorFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> unknown = MockOut.of(MockFoo.class, "unknown");
        MockOut<MockFoo> high = MockOut.of(MockFoo.class, "high");
        MockOut<MockFoo> middle = MockOut.of(MockFoo.class, "middle");
        MockOut<MockFoo> low = MockOut.of(MockFoo.class, "low");

        Object masterBranch = invoke(factory, "example", a, b);
        unknown.add(output(MockFoo.class, masterBranch, "unknown"));
        high.add(output(MockFoo.class, masterBranch, "high"));
        middle.add(output(MockFoo.class, masterBranch, "middle"));
        low.add(output(MockFoo.class, masterBranch, "low"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Selector.example"));
        assertThat(graph.getConnected("b"), isJust("Selector.example"));
        assertThat(graph.getConnected("Selector.example"), isJust("unknown", "high", "middle", "low"));
    }

    /**
     * パラメータ化。
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new MasterBranchOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> unknown = MockOut.of(MockFoo.class, "unknown");
        MockOut<MockFoo> high = MockOut.of(MockFoo.class, "high");
        MockOut<MockFoo> middle = MockOut.of(MockFoo.class, "middle");
        MockOut<MockFoo> low = MockOut.of(MockFoo.class, "low");

        Object masterBranch = invoke(factory, "example", a, b, 100);
        unknown.add(output(MockFoo.class, masterBranch, "unknown"));
        high.add(output(MockFoo.class, masterBranch, "high"));
        middle.add(output(MockFoo.class, masterBranch, "middle"));
        low.add(output(MockFoo.class, masterBranch, "low"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("b"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("Parameterized.example"), isJust("unknown", "high", "middle", "low"));
    }

    /**
     * マスタ選択つきパラメータ化 (セレクタのパラメータ無し)。
     */
    @Test
    public void parameterizedSelector1() {
        add("com.example.ParameterizedSelector1");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new MasterBranchOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedSelector1Factory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> unknown = MockOut.of(MockFoo.class, "unknown");
        MockOut<MockFoo> high = MockOut.of(MockFoo.class, "high");
        MockOut<MockFoo> middle = MockOut.of(MockFoo.class, "middle");
        MockOut<MockFoo> low = MockOut.of(MockFoo.class, "low");

        Object masterBranch = invoke(factory, "example", a, b, 100);
        unknown.add(output(MockFoo.class, masterBranch, "unknown"));
        high.add(output(MockFoo.class, masterBranch, "high"));
        middle.add(output(MockFoo.class, masterBranch, "middle"));
        low.add(output(MockFoo.class, masterBranch, "low"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("ParameterizedSelector1.example"));
        assertThat(graph.getConnected("b"), isJust("ParameterizedSelector1.example"));
        assertThat(graph.getConnected("ParameterizedSelector1.example"), isJust("unknown", "high", "middle", "low"));
    }

    /**
     * マスタ選択つきパラメータ化 (セレクタのパラメータ無し)。
     */
    @Test
    public void parameterizedSelector2() {
        add("com.example.ParameterizedSelector2");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new MasterBranchOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedSelector2Factory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> unknown = MockOut.of(MockFoo.class, "unknown");
        MockOut<MockFoo> high = MockOut.of(MockFoo.class, "high");
        MockOut<MockFoo> middle = MockOut.of(MockFoo.class, "middle");
        MockOut<MockFoo> low = MockOut.of(MockFoo.class, "low");

        Object masterBranch = invoke(factory, "example", a, b, 100);
        unknown.add(output(MockFoo.class, masterBranch, "unknown"));
        high.add(output(MockFoo.class, masterBranch, "high"));
        middle.add(output(MockFoo.class, masterBranch, "middle"));
        low.add(output(MockFoo.class, masterBranch, "low"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("ParameterizedSelector2.example"));
        assertThat(graph.getConnected("b"), isJust("ParameterizedSelector2.example"));
        assertThat(graph.getConnected("ParameterizedSelector2.example"), isJust("unknown", "high", "middle", "low"));
    }

    /**
     * generic method.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        add("com.example.ExampleEnum");
        ClassLoader loader = start(new MasterBranchOperatorProcessor());
        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockKeyValue1> a = MockIn.of(MockKeyValue1.class, "a");
        MockIn<MockKeyValue2> b = MockIn.of(MockKeyValue2.class, "b");

        MockOut<MockKeyValue2> unknown = MockOut.of(MockKeyValue2.class, "unknown");
        MockOut<MockKeyValue2> high = MockOut.of(MockKeyValue2.class, "high");
        MockOut<MockKeyValue2> middle = MockOut.of(MockKeyValue2.class, "middle");
        MockOut<MockKeyValue2> low = MockOut.of(MockKeyValue2.class, "low");

        Object masterBranch = invoke(factory, "example", a, b);
        unknown.add(output(MockKeyValue2.class, masterBranch, "unknown"));
        high.add(output(MockKeyValue2.class, masterBranch, "high"));
        middle.add(output(MockKeyValue2.class, masterBranch, "middle"));
        low.add(output(MockKeyValue2.class, masterBranch, "low"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Generic.example"));
        assertThat(graph.getConnected("b"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("unknown", "high", "middle", "low"));
    }

    /**
     * 抽象メソッド。
     */
    @Test
    public void Abstract() {
        add("com.example.Abstract");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * 列挙が空。
     */
    @Test
    public void Empty() {
        add("com.example.Empty");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * 列挙でない。
     */
    @Test
    public void NotEnum() {
        add("com.example.NotEnum");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * モデルでない。
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * ユーザー定義パラメーターでない。
     */
    @Test
    public void NotUserParameter() {
        add("com.example.NotUserParameter");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * キーの指定がない。
     */
    @Test
    public void NoKey() {
        add("com.example.NoKey");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * キーの指定がない。
     */
    @Test
    public void SelectorWithTooMatchParameters() {
        add("com.example.SelectorWithTooMatchParameters");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * マスタ型が不正。
     */
    @Test
    public void SelectorWithInvalidMaster() {
        add("com.example.SelectorWithInvalidMaster");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * トランザクション型が不正。
     */
    @Test
    public void SelectorWithInvalidTx() {
        add("com.example.SelectorWithInvalidTx");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * 戻り値型が不正。
     */
    @Test
    public void SelectorWithInvalidReturn() {
        add("com.example.SelectorWithInvalidReturn");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * マスタ選択メソッドが存在しない。
     */
    @Test
    public void SelectorWithoutMethod() {
        add("com.example.SelectorWithoutMethod");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * マスタ選択メソッドの注釈が不正。
     */
    @Test
    public void SelectorWithoutAnnotated() {
        add("com.example.SelectorWithoutAnnotated");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }
}
