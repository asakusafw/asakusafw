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
 * Test for {@link MasterCheckOperatorProcessor}.
 */
public class MasterCheckOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純なテスト。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new MasterCheckOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");
        MockOut<MockFoo> found = MockOut.of(MockFoo.class, "found");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");
        Object masterCheck = invoke(factory, "example", a, b);
        found.add(output(MockFoo.class, masterCheck, "found"));
        missed.add(output(MockFoo.class, masterCheck, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Simple.example"));
        assertThat(graph.getConnected("b"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("found", "missed"));
    }

    /**
     * マスタ選択つき。
     */
    @Test
    public void selector() {
        add("com.example.Selector");
        ClassLoader loader = start(new MasterCheckOperatorProcessor());
        Object factory = create(loader, "com.example.SelectorFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");
        MockOut<MockFoo> found = MockOut.of(MockFoo.class, "found");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");
        Object masterCheck = invoke(factory, "example", a, b);
        found.add(output(MockFoo.class, masterCheck, "found"));
        missed.add(output(MockFoo.class, masterCheck, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Selector.example"));
        assertThat(graph.getConnected("b"), isJust("Selector.example"));
        assertThat(graph.getConnected("Selector.example"), isJust("found", "missed"));
    }

    /**
     * generic method.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        ClassLoader loader = start(new MasterCheckOperatorProcessor());
        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockKeyValue1> a = MockIn.of(MockKeyValue1.class, "a");
        MockIn<MockKeyValue2> b = MockIn.of(MockKeyValue2.class, "b");
        MockOut<MockKeyValue2> found = MockOut.of(MockKeyValue2.class, "found");
        MockOut<MockKeyValue2> missed = MockOut.of(MockKeyValue2.class, "missed");
        Object masterCheck = invoke(factory, "example", a, b);
        found.add(output(MockKeyValue2.class, masterCheck, "found"));
        missed.add(output(MockKeyValue2.class, masterCheck, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Generic.example"));
        assertThat(graph.getConnected("b"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("found", "missed"));
    }

    /**
     * キーの指定がない。
     */
    @Test
    public void NoKeys() {
        add("com.example.NoKeys");
        error(new MasterCheckOperatorProcessor());
    }

    /**
     * 具象メソッド。
     */
    @Test
    public void NotAbstract() {
        add("com.example.NotAbstract");
        error(new MasterCheckOperatorProcessor());
    }

    /**
     * 論理以外を返す。
     */
    @Test
    public void NotBoolean() {
        add("com.example.NotBoolean");
        error(new MasterCheckOperatorProcessor());
    }

    /**
     * モデルでない。
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new MasterCheckOperatorProcessor());
    }

    /**
     * パラメーター化。
     */
    @Test
    public void Parameterized() {
        add("com.example.Parameterized");
        error(new MasterCheckOperatorProcessor());
    }
}
