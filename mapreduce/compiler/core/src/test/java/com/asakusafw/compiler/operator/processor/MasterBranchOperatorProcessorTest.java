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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.model.MockFoo;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.compiler.operator.model.MockKeyValue1;
import com.asakusafw.compiler.operator.model.MockKeyValue2;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;

/**
 * Test for {@link MasterBranchOperatorProcessor}.
 */
public class MasterBranchOperatorProcessorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
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
     * w/ master selection.
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
     * parameterized.
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
     * w/ user parameter and selector (the selector w/o user parameters).
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
     * w/ user parameter and selector (the selector w/o user parameters).
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
     * with normal field.
     */
    @Test
    public void normal_field() {
        add("com.example.NormalField");
        add("com.example.NormalFieldEnum");
        ClassLoader loader = start(new MasterBranchOperatorProcessor());

        Object factory = create(loader, "com.example.NormalFieldFactory");

        MockIn<MockHoge> a = MockIn.of(MockHoge.class, "a");
        MockIn<MockFoo> b = MockIn.of(MockFoo.class, "b");

        Object masterBranch = invoke(factory, "example", a, b);

        Set<String> fieldNames = new HashSet<>();
        for (Field field : masterBranch.getClass().getFields()) {
            fieldNames.add(field.getName());
        }
        assertThat(fieldNames, containsInAnyOrder("unknown", "low", "middle", "high"));
    }

    /**
     * w/ abstract modifier.
     */
    @Test
    public void Abstract() {
        add("com.example.Abstract");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * refers empty enum type.
     */
    @Test
    public void Empty() {
        add("com.example.Empty");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * refers not an enum type.
     */
    @Test
    public void NotEnum() {
        add("com.example.NotEnum");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
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
     * not a data model parameter.
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * not a valid user parameters.
     */
    @Test
    public void NotUserParameter() {
        add("com.example.NotUserParameter");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * missing key annotation.
     */
    @Test
    public void NoKey() {
        add("com.example.NoKey");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * missing key annotation.
     */
    @Test
    public void SelectorWithTooMatchParameters() {
        add("com.example.SelectorWithTooMatchParameters");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * selector w/ invalid master type.
     */
    @Test
    public void SelectorWithInvalidMaster() {
        add("com.example.SelectorWithInvalidMaster");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * selector w/ invalid tx type.
     */
    @Test
    public void SelectorWithInvalidTx() {
        add("com.example.SelectorWithInvalidTx");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * selector w/ invalid return type.
     */
    @Test
    public void SelectorWithInvalidReturn() {
        add("com.example.SelectorWithInvalidReturn");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * missing selector method.
     */
    @Test
    public void SelectorWithoutMethod() {
        add("com.example.SelectorWithoutMethod");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * selector w/o the mandatory annotation.
     */
    @Test
    public void SelectorWithoutAnnotated() {
        add("com.example.SelectorWithoutAnnotated");
        add("com.example.EmptyEnum");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * Grouping key properties are inconsistent.
     */
    @Test
    public void inconsistentGroupCount() {
        add("com.example.InconsistentGroupCount");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }

    /**
     * Grouping key properties have inconsistent types.
     */
    @Test
    public void inconsistentGroupType() {
        add("com.example.InconsistentGroupType");
        add("com.example.ExampleEnum");
        error(new MasterBranchOperatorProcessor());
    }
}
