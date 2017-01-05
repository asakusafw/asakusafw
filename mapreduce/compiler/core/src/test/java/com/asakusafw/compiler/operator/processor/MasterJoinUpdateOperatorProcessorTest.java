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
 * Test for {@link MasterJoinUpdateOperatorProcessor}.
 */
public class MasterJoinUpdateOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        ClassLoader loader = start(new MasterJoinUpdateOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> updated = MockOut.of(MockFoo.class, "updated");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");

        Object masterJoinUpdate = invoke(factory, "example", a, b);
        updated.add(output(MockFoo.class, masterJoinUpdate, "updated"));
        missed.add(output(MockFoo.class, masterJoinUpdate, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Simple.example"));
        assertThat(graph.getConnected("b"), isJust("Simple.example"));
        assertThat(graph.getConnected("Simple.example"), isJust("updated", "missed"));
    }

    /**
     * w/ master selection.
     */
    @Test
    public void selector() {
        add("com.example.Selector");
        ClassLoader loader = start(new MasterJoinUpdateOperatorProcessor());
        Object factory = create(loader, "com.example.SelectorFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> updated = MockOut.of(MockFoo.class, "updated");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");

        Object masterJoinUpdate = invoke(factory, "example", a, b);
        updated.add(output(MockFoo.class, masterJoinUpdate, "updated"));
        missed.add(output(MockFoo.class, masterJoinUpdate, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Selector.example"));
        assertThat(graph.getConnected("b"), isJust("Selector.example"));
        assertThat(graph.getConnected("Selector.example"), isJust("updated", "missed"));
    }

    /**
     * parameterized.
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        ClassLoader loader = start(new MasterJoinUpdateOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> updated = MockOut.of(MockFoo.class, "updated");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");

        Object masterJoinUpdate = invoke(factory, "example", a, b, 100);
        updated.add(output(MockFoo.class, masterJoinUpdate, "updated"));
        missed.add(output(MockFoo.class, masterJoinUpdate, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("b"), isJust("Parameterized.example"));
        assertThat(graph.getConnected("Parameterized.example"), isJust("updated", "missed"));
    }

    /**
     * w/ user parameter and selector (the selector w/o user parameters).
     */
    @Test
    public void parameterizedSelector() {
        add("com.example.ParameterizedSelector");
        ClassLoader loader = start(new MasterJoinUpdateOperatorProcessor());
        Object factory = create(loader, "com.example.ParameterizedSelectorFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> updated = MockOut.of(MockFoo.class, "updated");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");

        Object masterJoinUpdate = invoke(factory, "example", a, b, 100);
        updated.add(output(MockFoo.class, masterJoinUpdate, "updated"));
        missed.add(output(MockFoo.class, masterJoinUpdate, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("ParameterizedSelector.example"));
        assertThat(graph.getConnected("b"), isJust("ParameterizedSelector.example"));
        assertThat(graph.getConnected("ParameterizedSelector.example"), isJust("updated", "missed"));
    }

    /**
     * generic method.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        ClassLoader loader = start(new MasterJoinUpdateOperatorProcessor());
        Object factory = create(loader, "com.example.GenericFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> updated = MockOut.of(MockFoo.class, "updated");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");

        Object masterJoinUpdate = invoke(factory, "example", a, b);
        updated.add(output(MockFoo.class, masterJoinUpdate, "updated"));
        missed.add(output(MockFoo.class, masterJoinUpdate, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("Generic.example"));
        assertThat(graph.getConnected("b"), isJust("Generic.example"));
        assertThat(graph.getConnected("Generic.example"), isJust("updated", "missed"));
    }

    /**
     * generic operator and generic selector.
     */
    @Test
    public void genericSelector() {
        add("com.example.GenericSelector1");
        ClassLoader loader = start(new MasterJoinUpdateOperatorProcessor());
        Object factory = create(loader, "com.example.GenericSelector1Factory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> updated = MockOut.of(MockFoo.class, "updated");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");

        Object masterJoinUpdate = invoke(factory, "example", a, b);
        updated.add(output(MockFoo.class, masterJoinUpdate, "updated"));
        missed.add(output(MockFoo.class, masterJoinUpdate, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("GenericSelector1.example"));
        assertThat(graph.getConnected("b"), isJust("GenericSelector1.example"));
        assertThat(graph.getConnected("GenericSelector1.example"), isJust("updated", "missed"));
    }

    /**
     * plain operator and generic selector.
     */
    @Test
    public void genericSelector_plainOperator() {
        add("com.example.GenericSelector2");
        ClassLoader loader = start(new MasterJoinUpdateOperatorProcessor());
        Object factory = create(loader, "com.example.GenericSelector2Factory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        MockOut<MockFoo> updated = MockOut.of(MockFoo.class, "updated");
        MockOut<MockFoo> missed = MockOut.of(MockFoo.class, "missed");

        Object masterJoinUpdate = invoke(factory, "example", a, b);
        updated.add(output(MockFoo.class, masterJoinUpdate, "updated"));
        missed.add(output(MockFoo.class, masterJoinUpdate, "missed"));

        Graph<String> graph = toGraph(a, b);
        assertThat(graph.getConnected("a"), isJust("GenericSelector2.example"));
        assertThat(graph.getConnected("b"), isJust("GenericSelector2.example"));
        assertThat(graph.getConnected("GenericSelector2.example"), isJust("updated", "missed"));
    }

    /**
     * w/ abstract modifier.
     */
    @Test
    public void Abstract() {
        add("com.example.Abstract");
        error(new MasterJoinUpdateOperatorProcessor());
    }

    /**
     * not void type.
     */
    @Test
    public void Returns() {
        add("com.example.Returns");
        error(new MasterJoinUpdateOperatorProcessor());
    }

    /**
     * not a data model parameter.
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new MasterJoinUpdateOperatorProcessor());
    }

    /**
     * not a valid user parameters.
     */
    @Test
    public void NotUserParameter() {
        add("com.example.NotUserParameter");
        error(new MasterJoinUpdateOperatorProcessor());
    }

    /**
     * missing key annotation.
     */
    @Test
    public void NoKey() {
        add("com.example.NoKey");
        error(new MasterJoinUpdateOperatorProcessor());
    }

    /**
     * Grouping key properties are inconsistent.
     */
    @Test
    public void inconsistentGroupCount() {
        add("com.example.InconsistentGroupCount");
        error(new MasterJoinUpdateOperatorProcessor());
    }

    /**
     * Grouping key properties have inconsistent types.
     */
    @Test
    public void inconsistentGroupType() {
        add("com.example.InconsistentGroupType");
        error(new MasterJoinUpdateOperatorProcessor());
    }
}
