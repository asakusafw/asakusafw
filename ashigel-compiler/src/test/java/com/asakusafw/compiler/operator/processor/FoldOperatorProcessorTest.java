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
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.compiler.operator.processor.FoldOperatorProcessor;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;
import com.ashigeru.util.graph.Graph;

/**
 * Test for {@link FoldOperatorProcessor}.
 */
public class FoldOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * 単純な例。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new FoldOperatorProcessor());

        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> in = MockIn.of(MockHoge.class, "in");
        MockOut<MockHoge> out = MockOut.of(MockHoge.class, "out");
        Object fold = invoke(factory, "example", in, 100);
        out.add(output(MockHoge.class, fold, "out"));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("out"));
    }

    /**
     * 抽象を返す。
     */
    @Test
    public void isAbstract() {
        add("com.example.Abstract");
        error(new FoldOperatorProcessor());
    }

    /**
     * 値を返す。
     */
    @Test
    public void returns() {
        add("com.example.Returns");
        error(new FoldOperatorProcessor());
    }

    /**
     * パラメーターの宣言が足りない。
     */
    @Test
    public void noParameters() {
        add("com.example.NoParameters");
        error(new FoldOperatorProcessor());
    }

    /**
     * パラメーターの宣言が足りない。
     */
    @Test
    public void lessParameters() {
        add("com.example.LessParameters");
        error(new FoldOperatorProcessor());
    }

    /**
     * 入力が多い。
     */
    @Test
    public void tooManyInput() {
        add("com.example.TooManyInput");
        error(new FoldOperatorProcessor());
    }

    /**
     * 第一引数と第二引数の型が異なる。
     */
    @Test
    public void inconsistentType() {
        add("com.example.InconsistentType");
        error(new FoldOperatorProcessor());
    }

    /**
     * キーの指定がない。
     */
    @Test
    public void noKey() {
        add("com.example.NoKey");
        error(new FoldOperatorProcessor());
    }
}
