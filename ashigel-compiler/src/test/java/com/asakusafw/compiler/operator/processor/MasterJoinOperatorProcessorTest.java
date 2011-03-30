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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.model.MockFoo;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.compiler.operator.model.MockJoined;
import com.asakusafw.compiler.operator.processor.MasterJoinOperatorProcessor;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;
import com.ashigeru.util.graph.Graph;

/**
 * Test for {@link MasterJoinOperatorProcessor}.
 */
public class MasterJoinOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純なテスト (ファクトリ)。
     */
    @Test
    public void simple_Factory() {
        add("com.example.Simple");
        ClassLoader loader = start(new MasterJoinOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");
        MockOut<MockJoined> joined = MockOut.of(MockJoined.class, "joined");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");
        Object masterJoin = invoke(factory, "example", a, b);
        joined.add(output(MockJoined.class, masterJoin, "joined"));
        missed.add(output(MockFoo.class, masterJoin, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Simple.example"));
        assertThat(graph.getConnected("b"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("joined", "missed"));
    }

    /**
     * 単純なテスト (実装)。
     */
    @Test
    public void simple_Implementation() {
        add("com.example.Simple");
        ClassLoader loader = start(new MasterJoinOperatorProcessor());

        Object impl = create(loader, "com.example.SimpleImpl");

        MockHoge hoge = new MockHoge();
        hoge.value = 100;
        MockFoo foo = new MockFoo();
        foo.value = 200;

        MockJoined joined = (MockJoined) invoke(impl, "example", hoge, foo);

        assertThat(joined.hogeValue, is(100));
        assertThat(joined.fooValue, is(200));
    }

    /**
     * 引数の順序を逆にしたテスト (実装)。
     */
    @Test
    public void inverted_Implementation() {
        add("com.example.Inverted");
        ClassLoader loader = start(new MasterJoinOperatorProcessor());

        Object impl = create(loader, "com.example.InvertedImpl");

        MockHoge hoge = new MockHoge();
        hoge.value = 100;
        MockFoo foo = new MockFoo();
        foo.value = 200;

        MockJoined joined = (MockJoined) invoke(impl, "example", foo, hoge);

        assertThat(joined.hogeValue, is(100));
        assertThat(joined.fooValue, is(200));
    }

    /**
     * マスタ選択つき。
     */
    @Test
    public void selector() {
        add("com.example.Selector");
        ClassLoader loader = start(new MasterJoinOperatorProcessor());
        Object factory = create(loader, "com.example.SelectorFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");
        MockOut<MockJoined> joined = MockOut.of(MockJoined.class, "joined");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");
        Object masterJoin = invoke(factory, "example", a, b);
        joined.add(output(MockJoined.class, masterJoin, "joined"));
        missed.add(output(MockFoo.class, masterJoin, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Selector.example"));
        assertThat(graph.getConnected("b"), isJust("Selector.example"));
        assertThat(graph.getConnected("Selector.example"), isJust("joined", "missed"));
    }

    /**
     * 抽象メソッドでない。
     */
    @Test
    public void NotAbstract() {
        add("com.example.NotAbstract");
        error(new MasterJoinOperatorProcessor());
    }

    /**
     * 結合モデルでない。
     */
    @Test
    public void NotJoined() {
        add("com.example.NotJoined");
        error(new MasterJoinOperatorProcessor());
    }

    /**
     * モデルでない。
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new MasterJoinOperatorProcessor());
    }

    /**
     * パラメータ化。
     */
    @Test
    public void Parameterized() {
        add("com.example.Parameterized");
        error(new MasterJoinOperatorProcessor());
    }
}
