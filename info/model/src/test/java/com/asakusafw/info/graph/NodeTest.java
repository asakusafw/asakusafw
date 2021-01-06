/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.info.graph;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.info.InfoSerDe;
import com.asakusafw.info.MockAttribute;

/**
 * Test for {@link Node}.
 */
public class NodeTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        checkRestore(new Node());
    }

    /**
     * w/ attributes.
     */
    @Test
    public void attributes() {
        checkRestore(new Node()
                .withAttribute(new MockAttribute("A"))
                .withAttribute(new MockAttribute("B"))
                .withAttribute(new MockAttribute("C")));
    }

    /**
     * w/ inputs.
     */
    @Test
    public void inputs() {
        checkRestore(new Node()
                .withInput(it -> it.withAttribute(new MockAttribute("A")))
                .withInput(it -> it.withAttribute(new MockAttribute("B")))
                .withInput(it -> it.withAttribute(new MockAttribute("C"))));
    }

    /**
     * w/ outputs.
     */
    @Test
    public void outputs() {
        checkRestore(new Node()
                .withOutput(it -> it.withAttribute(new MockAttribute("A")))
                .withOutput(it -> it.withAttribute(new MockAttribute("B")))
                .withOutput(it -> it.withAttribute(new MockAttribute("C"))));
    }

    /**
     * w/ elements.
     */
    @Test
    public void elements() {
        checkRestore(new Node()
                .withElement(it -> it.withAttribute(new MockAttribute("A")))
                .withElement(it -> it.withAttribute(new MockAttribute("B")))
                .withElement(it -> it.withAttribute(new MockAttribute("C"))));
    }

    /**
     * w/ wires.
     */
    @Test
    public void wires() {
        Node result = checkRestore(new Node().configure(root -> {
            Node a = root.newElement();
            Output aOut = a.newOutput();
            Node b = root.newElement().withInput(it -> it
                    .connect(aOut, w -> w.withAttribute(new MockAttribute("A"))));
            Node c = root.newElement().withInput(it -> it
                    .connect(aOut, w -> w.withAttribute(new MockAttribute("B"))));
            root.newElement().withInput(it -> it
                    .connect(b.newOutput(), w -> w.withAttribute(new MockAttribute("C")))
                    .connect(c.newOutput(), w -> w.withAttribute(new MockAttribute("D"))));
        }));
        result.getElements().forEach(n -> assertThat(n.getParent(), is(result)));
        result.getElements().forEach(n -> n.getInputs().forEach(p -> assertThat(p.getParent(), is(n))));
        result.getElements().forEach(n -> n.getOutputs().forEach(p -> assertThat(p.getParent(), is(n))));
        result.getWires().forEach(w -> assertThat(w.getParent(), is(result)));


        Node a = result.getElement(0);
        Node b = result.getElement(1);
        Node c = result.getElement(2);
        Node d = result.getElement(3);

        Wire ab = result.getWire(0);
        Wire ac = result.getWire(1);
        Wire bd = result.getWire(2);
        Wire cd = result.getWire(3);

        Output aOut = a.getOutput(0);
        Output bOut = b.getOutput(0);
        Output cOut = c.getOutput(0);

        Input bIn = b.getInput(0);
        Input cIn = c.getInput(0);
        Input dIn = d.getInput(0);

        assertThat(aOut.getWires(), contains(ab, ac));
        assertThat(bOut.getWires(), contains(bd));
        assertThat(cOut.getWires(), contains(cd));

        assertThat(aOut.getOpposites(), contains(bIn, cIn));
        assertThat(bOut.getOpposites(), contains(dIn));
        assertThat(cOut.getOpposites(), contains(dIn));

        assertThat(bIn.getWires(), contains(ab));
        assertThat(cIn.getWires(), contains(ac));
        assertThat(dIn.getWires(), contains(bd, cd));

        assertThat(bIn.getOpposites(), contains(aOut));
        assertThat(cIn.getOpposites(), contains(aOut));
        assertThat(dIn.getOpposites(), contains(bOut, cOut));
    }

    private static Node checkRestore(Node node) {
        Node result = InfoSerDe.checkRestore(Node.class, node, (a, b) -> a.info().equals(b.info()));
        InfoSerDe.checkRestore(Node.class, node, (a, b) -> a.info().hashCode() == b.info().hashCode());
        return result;
    }
}
