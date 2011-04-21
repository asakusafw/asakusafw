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
import com.asakusafw.compiler.operator.model.MockJoined;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;
import com.ashigeru.util.graph.Graph;

/**
 * Test for {@link SplitOperatorProcessor}.
 */
public class SplitOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純な例。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new SplitOperatorProcessor());

        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockJoined> in = MockIn.of(MockJoined.class, "in");
        MockOut<MockHoge> a = MockOut.of(MockHoge.class, "a");
        MockOut<MockFoo> b = MockOut.of(MockFoo.class, "b");

        Object update = invoke(factory, "example", in);
        a.add(output(MockHoge.class, update, "hoge"));
        b.add(output(MockFoo.class, update, "foo"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("a", "b"));
    }

    /**
     * 具象メソッド。
     */
    @Test
    public void NotAbstract() {
        add("com.example.NotAbstract");
        error(new SplitOperatorProcessor());
    }

    /**
     * 結合モデルでない。
     */
    @Test
    public void NotJoined() {
        add("com.example.NotJoined");
        error(new SplitOperatorProcessor());
    }

    /**
     * モデルでない。
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new SplitOperatorProcessor());
    }

    /**
     * 結果型でない。
     */
    @Test
    public void NotResult() {
        add("com.example.NotResult");
        error(new SplitOperatorProcessor());
    }

    /**
     * 値を返す。
     */
    @Test
    public void NotVoid() {
        add("com.example.NotVoid");
        error(new SplitOperatorProcessor());
    }

    /**
     * パラメーター化。
     */
    @Test
    public void Parameterized() {
        add("com.example.Parameterized");
        error(new SplitOperatorProcessor());
    }

    /**
     * generic method.
     */
    @Test
    public void Generic() {
        add("com.example.Generic");
        error(new SplitOperatorProcessor());
    }
}
