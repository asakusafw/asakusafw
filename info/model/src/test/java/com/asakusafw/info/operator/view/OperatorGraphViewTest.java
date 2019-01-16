/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.info.operator.view;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import com.asakusafw.info.graph.Node;
import com.asakusafw.info.graph.NodeTestUtil;
import com.asakusafw.info.operator.CoreOperatorSpec;
import com.asakusafw.info.operator.CoreOperatorSpec.CoreOperatorKind;
import com.asakusafw.info.operator.CustomOperatorSpec;
import com.asakusafw.info.operator.InputAttribute;
import com.asakusafw.info.operator.InputGranularity;
import com.asakusafw.info.operator.InputOperatorSpec;
import com.asakusafw.info.operator.OperatorAttribute;
import com.asakusafw.info.operator.OperatorGraphAttribute;
import com.asakusafw.info.operator.OperatorSpec.OperatorKind;
import com.asakusafw.info.operator.OutputAttribute;
import com.asakusafw.info.operator.OutputOperatorSpec;
import com.asakusafw.info.value.ClassInfo;

/**
 * Test for {@link OperatorGraphView}.
 */
public class OperatorGraphViewTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Node root = new Node();
        OperatorGraphView view = new OperatorGraphView(new OperatorGraphAttribute(root));
        assertThat(view.getOperators(), is(empty()));
    }

    /**
     * w/ operator.
     */
    @Test
    public void operator() {
        Node root = new Node();
        Node op = root.newElement()
                .withAttribute(new OperatorAttribute(
                        CoreOperatorSpec.of(CoreOperatorKind.CHECKPOINT), Collections.emptyList()))
                .withInput(it -> it.withAttribute(new InputAttribute(
                        "in",
                        ClassInfo.of("com.example.Data"),
                        InputGranularity.RECORD,
                        null)))
                .withOutput(it -> it.withAttribute(new OutputAttribute(
                        "out",
                        ClassInfo.of("com.example.Data"))));

        OperatorGraphView view = new OperatorGraphView(new OperatorGraphAttribute(root));
        assertThat(view.getOperators(), hasSize(1));
        assertThat(view.getOperatorMap(OperatorKind.INPUT).keySet(), is(empty()));
        assertThat(view.getOperatorMap(OperatorKind.OUTPUT).keySet(), is(empty()));

        OperatorView v = view.getOperators().stream().findAny().get();
        NodeTestUtil.contentEquals(v.getEntity(), op);

        assertThat(v.getInputs(), hasSize(1));
        v.getInputs().forEach(it -> assertThat(it.getOwner(), is(v)));
        v.getInputs().forEach(it -> assertThat(it.getOpposites(), is(empty())));
        assertThat(v.getInputs().get(0).getName(), is("in"));

        assertThat(v.getOutputs(), hasSize(1));
        assertThat(v.getOutputs().get(0).getName(), is("out"));
        v.getOutputs().forEach(it -> assertThat(it.getOwner(), is(v)));
        v.getOutputs().forEach(it -> assertThat(it.getOpposites(), is(empty())));
    }

    /**
     * w/ io.
     */
    @Test
    public void io() {
        Node root = new Node();
        Node n0 = root.newElement()
                .withAttribute(new OperatorAttribute(
                        InputOperatorSpec.of(
                                "in",
                                ClassInfo.of("com.example.Input")), Collections.emptyList()));
        Node n1 = root.newElement()
                .withAttribute(new OperatorAttribute(
                        OutputOperatorSpec.of(
                                "out",
                                ClassInfo.of("com.example.Output")), Collections.emptyList()));
        n1.newInput()
                .withAttribute(new InputAttribute(
                        "p",
                        ClassInfo.of("com.example.Data"),
                        InputGranularity.RECORD,
                        null))
                .connect(n0.newOutput()
                        .withAttribute(new OutputAttribute(
                                "p",
                                ClassInfo.of("com.example.Data"))));


        OperatorGraphView view = new OperatorGraphView(new OperatorGraphAttribute(root));
        assertThat(view.getOperators(), hasSize(2));
        assertThat(view.getOperatorMap(OperatorKind.INPUT).keySet(), containsInAnyOrder("in"));
        assertThat(view.getOperatorMap(OperatorKind.OUTPUT).keySet(), containsInAnyOrder("out"));

        OperatorView v0 = view.getOperatorMap(OperatorKind.INPUT).get("in");
        OperatorView v1 = view.getOperatorMap(OperatorKind.OUTPUT).get("out");

        assertThat(v0.getOutputs(), hasSize(1));
        assertThat(v1.getInputs(), hasSize(1));
        assertThat(v0.getOutputs().get(0).getOpposites(), contains(v1.getInputs().get(0)));
    }

    /**
     * w/ nested graph.
     */
    @Test
    public void nest() {
        Node root = new Node();
        Node n1 = root.newElement().withAttribute(new OperatorAttribute(
                CustomOperatorSpec.of("parent"), Collections.emptyList()));
        n1.newElement().withAttribute(new OperatorAttribute(
                CustomOperatorSpec.of("child"), Collections.emptyList()));

        OperatorGraphView view = new OperatorGraphView(new OperatorGraphAttribute(root));
        assertThat(view.getOperators(), hasSize(1));
        assertThat(view.getOperatorMap(OperatorKind.INPUT).keySet(), is(empty()));
        assertThat(view.getOperatorMap(OperatorKind.OUTPUT).keySet(), is(empty()));

        OperatorView parent = view.getOperators().stream().findAny().get();
        assertThat(((CustomOperatorSpec) parent.getSpec()).getCategory(), is("parent"));

        OperatorGraphView inner = parent.getElementGraph();
        assertThat(inner.getOperators(), hasSize(1));
        assertThat(inner.getOperatorMap(OperatorKind.INPUT).keySet(), is(empty()));
        assertThat(inner.getOperatorMap(OperatorKind.OUTPUT).keySet(), is(empty()));

        OperatorView child = inner.getOperators().stream().findAny().get();
        assertThat(((CustomOperatorSpec) child.getSpec()).getCategory(), is("child"));
    }
}
