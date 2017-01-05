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
package com.asakusafw.compiler.flow.plan;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.asakusafw.compiler.flow.FlowGraphGenerator;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;

/**
 * Test for {@link FlowGraphUtil}.
 */
public class FlowGraphUtilTest {

    FlowGraphGenerator gen = new FlowGraphGenerator();

    /**
     * {@link FlowGraphUtil#collectElements(FlowGraph)}
     */
    @Test
    public void collectElements() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "i0 i1", "o1 o2");
        gen.defineOperator("unbound", "a", "b");

        gen.connect("in", "op.i0");
        gen.connect("empty", "op.i1");
        gen.connect("op.o1", "out");
        gen.connect("op.o2", "stop");

        Set<FlowElement> elements = FlowGraphUtil.collectElements(gen.toGraph());
        assertThat(elements, hasItem(gen.get("in")));
        assertThat(elements, hasItem(gen.get("out")));
        assertThat(elements, hasItem(gen.get("empty")));
        assertThat(elements, hasItem(gen.get("stop")));
        assertThat(elements, hasItem(gen.get("op")));
        assertThat(elements, not(hasItem(gen.get("unbound"))));
    }

    /**
     * {@link FlowGraphUtil#toElementGraph(FlowGraph)}
     */
    @Test
    public void toElementGraph() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "i0 i1", "o1 o2");

        gen.connect("in", "op.i0");
        gen.connect("empty", "op.i1");
        gen.connect("op.o1", "out");
        gen.connect("op.o2", "stop");

        Graph<FlowElement> graph = FlowGraphUtil.toElementGraph(gen.toGraph());
        assertThat(graph.getNodeSet(), is(gen.all()));
        assertThat(graph.getConnected(gen.get("in")), is(gen.getAsSet("op")));
        assertThat(graph.getConnected(gen.get("out")), is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("empty")), is(gen.getAsSet("op")));
        assertThat(graph.getConnected(gen.get("stop")), is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("op")), is(gen.getAsSet("stop", "out")));
    }

    /**
     * {@link FlowGraphUtil#deepCopy(FlowGraph)}
     */
    @Test
    public void copy() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "i0 i1", "o1 o2");

        gen.connect("in", "op.i0");
        gen.connect("empty", "op.i1");
        gen.connect("op.o1", "out");
        gen.connect("op.o2", "stop");

        FlowGraph graph = gen.toGraph();
        FlowGraph copy = FlowGraphUtil.deepCopy(graph);

        assertThat(
                toDescription(FlowGraphUtil.toElementGraph(copy)),
                is(toDescription(FlowGraphUtil.toElementGraph(graph))));
    }

    /**
     * {@link FlowGraphUtil#deepCopy(FlowGraph)}
     */
    @Test
    public void copyDeeply() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "i0 i1", "o1 o2");
        gen.connect("in", "op.i0");
        gen.connect("empty", "op.i1");
        gen.connect("op.o1", "out");
        gen.connect("op.o2", "stop");
        FlowGraph component = gen.toGraph();

        gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineFlowPart("c", component);
        gen.connect("in", "c.in");
        gen.connect("c.out", "out");
        FlowGraph graph = gen.toGraph();

        FlowGraph copy = FlowGraphUtil.deepCopy(graph);

        Set<FlowElement> allComponents = FlowGraphUtil.collectFlowParts(copy);
        assertThat(allComponents.size(), is(1));
        FlowElement compElem = allComponents.iterator().next();

        FlowPartDescription copyComponent = (FlowPartDescription) compElem.getDescription();

        assertThat(
                toDescription(FlowGraphUtil.toElementGraph(copyComponent.getFlowGraph())),
                is(toDescription(FlowGraphUtil.toElementGraph(component))));
        assertThat(
                copyComponent.getFlowGraph(),
                not(sameInstance(component)));
    }

    private Graph<FlowElementDescription> toDescription(Graph<FlowElement> graph) {
        Graph<FlowElementDescription> descriptions = Graphs.newInstance();
        for (Graph.Vertex<FlowElement> vertex : graph) {
            FlowElementDescription from = vertex.getNode().getDescription();
            for (FlowElement to : vertex.getConnected()) {
                descriptions.addEdge(from, to.getDescription());
            }
        }
        return descriptions;
    }

    /**
     * {@link FlowGraphUtil#hasMandatorySideEffect(FlowElement)}
     */
    @Test
    public void hasMandatorySideEffect() {
        gen.defineOperator("nothing", "in", "out");
        gen.defineOperator("atLeastOnce", "in", "out", ObservationCount.AT_LEAST_ONCE);
        gen.defineOperator("atMostOnce", "in", "out", ObservationCount.AT_MOST_ONCE);

        assertThat(FlowGraphUtil.hasMandatorySideEffect(gen.get("nothing")), is(false));
        assertThat(FlowGraphUtil.hasMandatorySideEffect(gen.get("atLeastOnce")), is(true));
        assertThat(FlowGraphUtil.hasMandatorySideEffect(gen.get("atMostOnce")), is(false));
    }

    /**
     * {@link FlowGraphUtil#isAlwaysEmpty(FlowElement)}
     */
    @Test
    public void isAlwaysEmpty() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("conn", "in", "out");
        gen.defineOperator("noIn", "in", "out");
        gen.defineOperator("noOut", "in", "out");

        gen.connect("in", "conn");
        gen.connect("in", "noOut");
        gen.connect("conn", "out");
        gen.connect("noIn", "out");
        gen.connect("empty", "stop");

        assertThat(FlowGraphUtil.isAlwaysEmpty(gen.get("in")), is(false));
        assertThat(FlowGraphUtil.isAlwaysEmpty(gen.get("out")), is(false));
        assertThat(FlowGraphUtil.isAlwaysEmpty(gen.get("empty")), is(false));
        assertThat(FlowGraphUtil.isAlwaysEmpty(gen.get("stop")), is(false));
        assertThat(FlowGraphUtil.isAlwaysEmpty(gen.get("conn")), is(false));
        assertThat(FlowGraphUtil.isAlwaysEmpty(gen.get("noIn")), is(true));
        assertThat(FlowGraphUtil.isAlwaysEmpty(gen.get("noOut")), is(false));
    }

    /**
     * {@link FlowGraphUtil#isAlwaysStop(FlowElement)}
     */
    @Test
    public void isAlwaysStop() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("conn", "in", "out");
        gen.defineOperator("noIn", "in", "out");
        gen.defineOperator("noOut", "in", "out");

        gen.connect("in", "conn");
        gen.connect("in", "noOut");
        gen.connect("conn", "out");
        gen.connect("noIn", "out");
        gen.connect("empty", "stop");

        assertThat(FlowGraphUtil.isAlwaysStop(gen.get("in")), is(false));
        assertThat(FlowGraphUtil.isAlwaysStop(gen.get("out")), is(false));
        assertThat(FlowGraphUtil.isAlwaysStop(gen.get("empty")), is(false));
        assertThat(FlowGraphUtil.isAlwaysStop(gen.get("stop")), is(false));
        assertThat(FlowGraphUtil.isAlwaysStop(gen.get("conn")), is(false));
        assertThat(FlowGraphUtil.isAlwaysStop(gen.get("noIn")), is(false));
        assertThat(FlowGraphUtil.isAlwaysStop(gen.get("noOut")), is(true));
    }

    /**
     * {@link FlowGraphUtil#isIdentity(FlowElement)}
     */
    @Test
    public void isIdentity() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "in", "out");
        gen.definePseud("identity");
        gen.definePseud("checkpoint", FlowBoundary.STAGE);

        assertThat(FlowGraphUtil.isIdentity(gen.get("in")), is(false));
        assertThat(FlowGraphUtil.isIdentity(gen.get("out")), is(false));
        assertThat(FlowGraphUtil.isIdentity(gen.get("empty")), is(false));
        assertThat(FlowGraphUtil.isIdentity(gen.get("stop")), is(false));
        assertThat(FlowGraphUtil.isIdentity(gen.get("op")), is(false));
        assertThat(FlowGraphUtil.isIdentity(gen.get("identity")), is(true));
        assertThat(FlowGraphUtil.isIdentity(gen.get("checkpoint")), is(false));
    }

    /**
     * {@link FlowGraphUtil#splitIdentity(FlowElement)} (1*2)
     */
    @Test
    public void splitIdentity_1_2() {
        gen.defineInput("in1");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.definePseud("id");

        gen.connect("in1", "id");
        gen.connect("id", "out1");
        gen.connect("id", "out2");

        FlowGraphUtil.splitIdentity(gen.get("id"));
        assertThat(gen.input("id").getConnected().size(), is(0));
        assertThat(gen.output("id").getConnected().size(), is(0));

        assertThat(gen.output("in1").getConnected().size(), is(2));
        assertThat(gen.input("out1").getConnected().size(), is(1));
        assertThat(gen.input("out2").getConnected().size(), is(1));
    }

    /**
     * {@link FlowGraphUtil#splitIdentity(FlowElement)} (2*1)
     */
    @Test
    public void splitIdentity_2_1() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineOutput("out1");
        gen.definePseud("id");

        gen.connect("in1", "id");
        gen.connect("in2", "id");
        gen.connect("id", "out1");

        FlowGraphUtil.splitIdentity(gen.get("id"));
        assertThat(gen.input("id").getConnected().size(), is(0));
        assertThat(gen.output("id").getConnected().size(), is(0));

        assertThat(gen.output("in1").getConnected().size(), is(1));
        assertThat(gen.output("in2").getConnected().size(), is(1));
        assertThat(gen.input("out1").getConnected().size(), is(2));
    }

    /**
     * {@link FlowGraphUtil#splitIdentity(FlowElement)} (2*2)
     */
    @Test
    public void splitIdentity_2_2() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.definePseud("id");

        gen.connect("in1", "id");
        gen.connect("in2", "id");
        gen.connect("id", "out1");
        gen.connect("id", "out2");

        FlowGraphUtil.splitIdentity(gen.get("id"));
        assertThat(gen.input("id").getConnected().size(), is(0));
        assertThat(gen.output("id").getConnected().size(), is(0));

        assertThat(gen.output("in1").getConnected().size(), is(2));
        assertThat(gen.output("in2").getConnected().size(), is(2));
        assertThat(gen.input("out1").getConnected().size(), is(2));
        assertThat(gen.input("out1").getConnected().size(), is(2));
    }

    /**
     * {@link FlowGraphUtil#splitIdentity(FlowElement)} (1*1)
     */
    @Test
    public void splitIdentity_1_1() {
        gen.defineInput("in1");
        gen.defineOutput("out1");
        gen.definePseud("id");

        gen.connect("in1", "id");
        gen.connect("id", "out1");

        FlowGraphUtil.splitIdentity(gen.get("id"));
        assertThat(gen.input("id").getConnected().size(), is(1));
        assertThat(gen.output("id").getConnected().size(), is(1));

        assertThat(gen.output("in1").getConnected().size(), is(1));
        assertThat(gen.input("out1").getConnected().size(), is(1));
    }

    /**
     * {@link FlowGraphUtil#splitIdentity(FlowElement)} (1*1)
     */
    @Test(expected = IllegalArgumentException.class)
    public void splitIdentity_notIdentity() {
        gen.defineInput("in1");
        gen.defineOutput("out1");
        gen.defineOperator("op", "in", "out");

        gen.connect("in1", "op");
        gen.connect("op", "out1");

        FlowGraphUtil.splitIdentity(gen.get("op"));
    }

    /**
     * {@link FlowGraphUtil#skip(FlowElement)}
     */
    @Test
    public void testSkip_1_1() {
        gen.defineInput("in1");
        gen.definePseud("id");
        gen.defineOutput("out1");

        gen.connect("in1", "id");
        gen.connect("id", "out1");

        FlowGraphUtil.skip(gen.get("id"));
        Graph<FlowElement> graph = FlowGraphUtil.toElementGraph(gen.toGraph());
        assertThat(graph.getConnected(gen.get("in1")),
                is(gen.getAsSet("out1")));
        assertThat(graph.getConnected(gen.get("id")),
                is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out1")),
                is(gen.getAsSet()));
    }

    /**
     * {@link FlowGraphUtil#skip(FlowElement)}
     */
    @Test
    public void testSkip_1_2() {
        gen.defineInput("in1");
        gen.definePseud("id");
        gen.defineOutput("out1");
        gen.defineOutput("out2");

        gen.connect("in1", "id");
        gen.connect("id", "out1");
        gen.connect("id", "out2");

        FlowGraphUtil.skip(gen.get("id"));
        Graph<FlowElement> graph = FlowGraphUtil.toElementGraph(gen.toGraph());
        assertThat(graph.getConnected(gen.get("in1")),
                is(gen.getAsSet("out1", "out2")));
        assertThat(graph.getConnected(gen.get("id")),
                is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out1")),
                is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out2")),
                is(gen.getAsSet()));
    }

    /**
     * {@link FlowGraphUtil#skip(FlowElement)}
     */
    @Test
    public void testSkip_2_1() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.definePseud("id");
        gen.defineOutput("out1");

        gen.connect("in1", "id");
        gen.connect("in2", "id");
        gen.connect("id", "out1");

        FlowGraphUtil.skip(gen.get("id"));
        Graph<FlowElement> graph = FlowGraphUtil.toElementGraph(gen.toGraph());
        assertThat(graph.getConnected(gen.get("in1")),
                is(gen.getAsSet("out1")));
        assertThat(graph.getConnected(gen.get("in2")),
                is(gen.getAsSet("out1")));
        assertThat(graph.getConnected(gen.get("id")),
                is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out1")),
                is(gen.getAsSet()));
    }

    /**
     * {@link FlowGraphUtil#skip(FlowElement)}
     */
    @Test
    public void testSkip_2_2() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.definePseud("id");
        gen.defineOutput("out1");
        gen.defineOutput("out2");

        gen.connect("in1", "id");
        gen.connect("in2", "id");
        gen.connect("id", "out1");
        gen.connect("id", "out2");

        FlowGraphUtil.skip(gen.get("id"));
        Graph<FlowElement> graph = FlowGraphUtil.toElementGraph(gen.toGraph());
        assertThat(graph.getConnected(gen.get("in1")),
                is(gen.getAsSet("out1", "out2")));
        assertThat(graph.getConnected(gen.get("in2")),
                is(gen.getAsSet("out1", "out2")));
        assertThat(graph.getConnected(gen.get("id")),
                is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out1")),
                is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out2")),
                is(gen.getAsSet()));
    }

    /**
     * {@link FlowGraphUtil#isBoundary(FlowElement)}
     */
    @Test
    public void isBoundary() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "in", "out");
        gen.defineOperator("shuffle", "in", "out", FlowBoundary.SHUFFLE);
        gen.definePseud("identity");
        gen.definePseud("checkpoint", FlowBoundary.STAGE);

        assertThat(FlowGraphUtil.isBoundary(gen.get("in")), is(true));
        assertThat(FlowGraphUtil.isBoundary(gen.get("out")), is(true));
        assertThat(FlowGraphUtil.isBoundary(gen.get("empty")), is(true));
        assertThat(FlowGraphUtil.isBoundary(gen.get("stop")), is(true));
        assertThat(FlowGraphUtil.isBoundary(gen.get("op")), is(false));
        assertThat(FlowGraphUtil.isBoundary(gen.get("shuffle")), is(true));
        assertThat(FlowGraphUtil.isBoundary(gen.get("identity")), is(false));
        assertThat(FlowGraphUtil.isBoundary(gen.get("checkpoint")), is(true));
    }

    /**
     * {@link FlowGraphUtil#isShuffleBoundary(FlowElement)}
     */
    @Test
    public void isShuffleBoundary() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "in", "out");
        gen.defineOperator("shuffle", "in", "out", FlowBoundary.SHUFFLE);
        gen.definePseud("identity");
        gen.definePseud("checkpoint", FlowBoundary.STAGE);

        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("in")), is(false));
        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("out")), is(false));
        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("empty")), is(false));
        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("stop")), is(false));
        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("op")), is(false));
        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("shuffle")), is(true));
        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("identity")), is(false));
        assertThat(FlowGraphUtil.isShuffleBoundary(gen.get("checkpoint")), is(false));
    }

    /**
     * {@link FlowGraphUtil#isStageBoundary(FlowElement)}
     */
    @Test
    public void isStageBoundary() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "in", "out");
        gen.defineOperator("shuffle", "in", "out", FlowBoundary.SHUFFLE);
        gen.definePseud("identity");
        gen.definePseud("checkpoint", FlowBoundary.STAGE);

        assertThat(FlowGraphUtil.isStageBoundary(gen.get("in")), is(true));
        assertThat(FlowGraphUtil.isStageBoundary(gen.get("out")), is(true));
        assertThat(FlowGraphUtil.isStageBoundary(gen.get("empty")), is(true));
        assertThat(FlowGraphUtil.isStageBoundary(gen.get("stop")), is(true));
        assertThat(FlowGraphUtil.isStageBoundary(gen.get("op")), is(false));
        assertThat(FlowGraphUtil.isStageBoundary(gen.get("shuffle")), is(false));
        assertThat(FlowGraphUtil.isStageBoundary(gen.get("identity")), is(false));
        assertThat(FlowGraphUtil.isStageBoundary(gen.get("checkpoint")), is(true));
    }

    /**
     * {@link FlowGraphUtil#isStagePadding(FlowElement)}
     */
    @Test
    public void isStagePadding() {
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.defineEmpty("empty");
        gen.defineStop("stop");
        gen.defineOperator("op", "in", "out");
        gen.defineOperator("shuffle", "in", "out", FlowBoundary.SHUFFLE);
        gen.definePseud("identity");
        gen.definePseud("checkpoint", FlowBoundary.STAGE);

        assertThat(FlowGraphUtil.isStagePadding(gen.get("in")), is(false));
        assertThat(FlowGraphUtil.isStagePadding(gen.get("out")), is(false));
        assertThat(FlowGraphUtil.isStagePadding(gen.get("empty")), is(true));
        assertThat(FlowGraphUtil.isStagePadding(gen.get("stop")), is(true));
        assertThat(FlowGraphUtil.isStagePadding(gen.get("op")), is(false));
        assertThat(FlowGraphUtil.isStagePadding(gen.get("shuffle")), is(false));
        assertThat(FlowGraphUtil.isStagePadding(gen.get("identity")), is(false));
        assertThat(FlowGraphUtil.isStagePadding(gen.get("checkpoint")), is(true));
    }

    /**
     * {@link FlowGraphUtil#getSucceedBoundaryPath(FlowElement)}
     */
    @Test
    public void getSucceedBoundaryPath_direct() {
        gen.defineInput("in");
        gen.defineOutput("out");

        gen.connect("in", "out");

        FlowPath path = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in"));
        assertThat(path.getDirection(), is(FlowPath.Direction.FORWARD));
        assertThat(path.getStartings(), is(gen.getAsSet("in")));
        assertThat(path.getPassings(), is(gen.getAsSet()));
        assertThat(path.getArrivals(), is(gen.getAsSet("out")));
    }

    /**
     * {@link FlowGraphUtil#getSucceedBoundaryPath(FlowElement)}
     */
    @Test
    public void getSucceedBoundaryPath_hop() {
        gen.defineInput("in");
        gen.definePseud("id1");
        gen.definePseud("id2");
        gen.defineOutput("out");

        gen.connect("in", "id1");
        gen.connect("id1", "id2");
        gen.connect("id2", "out");

        FlowPath path = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in"));
        assertThat(path.getDirection(), is(FlowPath.Direction.FORWARD));
        assertThat(path.getStartings(), is(gen.getAsSet("in")));
        assertThat(path.getPassings(), is(gen.getAsSet("id1", "id2")));
        assertThat(path.getArrivals(), is(gen.getAsSet("out")));
    }

    /**
     * {@link FlowGraphUtil#getSucceedBoundaryPath(FlowElement)}
     */
    @Test
    public void getSucceedBoundaryPath_many() {
        gen.defineInput("in");
        gen.definePseud("id1");
        gen.definePseud("id2");
        gen.definePseud("id3");
        gen.defineOutput("out1");
        gen.defineOutput("out2");

        gen.connect("in", "id1");
        gen.connect("in", "id2");
        gen.connect("id1", "id3");
        gen.connect("id2", "id3");
        gen.connect("id3", "out1");
        gen.connect("id3", "out2");

        FlowPath path = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in"));
        assertThat(path.getDirection(), is(FlowPath.Direction.FORWARD));
        assertThat(path.getStartings(), is(gen.getAsSet("in")));
        assertThat(path.getPassings(), is(gen.getAsSet("id1", "id2", "id3")));
        assertThat(path.getArrivals(), is(gen.getAsSet("out1", "out2")));
    }

    /**
     * {@link FlowGraphUtil#getPredeceaseBoundaryPath(FlowElement)}
     */
    @Test
    public void getPredeceaseBoundaryPath_direct() {
        gen.defineInput("in");
        gen.defineOutput("out");

        gen.connect("in", "out");

        FlowPath path = FlowGraphUtil.getPredeceaseBoundaryPath(gen.get("out"));
        assertThat(path.getDirection(), is(FlowPath.Direction.BACKWORD));
        assertThat(path.getStartings(), is(gen.getAsSet("out")));
        assertThat(path.getPassings(), is(gen.getAsSet()));
        assertThat(path.getArrivals(), is(gen.getAsSet("in")));
    }

    /**
     * {@link FlowGraphUtil#getPredeceaseBoundaryPath(FlowElement)}
     */
    @Test
    public void getPredeceaseBoundaryPath_hop() {
        gen.defineInput("in");
        gen.definePseud("id1");
        gen.definePseud("id2");
        gen.defineOutput("out");

        gen.connect("in", "id1");
        gen.connect("id1", "id2");
        gen.connect("id2", "out");

        FlowPath path = FlowGraphUtil.getPredeceaseBoundaryPath(gen.get("out"));
        assertThat(path.getDirection(), is(FlowPath.Direction.BACKWORD));
        assertThat(path.getStartings(), is(gen.getAsSet("out")));
        assertThat(path.getPassings(), is(gen.getAsSet("id1", "id2")));
        assertThat(path.getArrivals(), is(gen.getAsSet("in")));
    }

    /**
     * {@link FlowGraphUtil#getPredeceaseBoundaryPath(FlowElement)}
     */
    @Test
    public void getPredeceaseBoundaryPath_many() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.definePseud("id1");
        gen.definePseud("id2");
        gen.definePseud("id3");
        gen.defineOutput("out");

        gen.connect("in1", "id1");
        gen.connect("in2", "id1");
        gen.connect("id1", "id2");
        gen.connect("id1", "id3");
        gen.connect("id2", "out");
        gen.connect("id3", "out");

        FlowPath path = FlowGraphUtil.getPredeceaseBoundaryPath(gen.get("out"));
        assertThat(path.getDirection(), is(FlowPath.Direction.BACKWORD));
        assertThat(path.getStartings(), is(gen.getAsSet("out")));
        assertThat(path.getPassings(), is(gen.getAsSet("id1", "id2", "id3")));
        assertThat(path.getArrivals(), is(gen.getAsSet("in1", "in2")));
    }

    /**
     * {@link FlowGraphUtil#hasSuccessors(FlowElement)}
     */
    @Test
    public void hasSuccessors() {
        gen.defineInput("in1");
        gen.defineOutput("out1");
        gen.defineInput("in2");
        gen.defineOutput("out2");

        gen.connect("in1", "out1");

        assertThat(FlowGraphUtil.hasSuccessors(gen.get("in1")), is(true));
        assertThat(FlowGraphUtil.hasSuccessors(gen.get("in2")), is(false));
        assertThat(FlowGraphUtil.hasSuccessors(gen.get("out1")), is(false));
        assertThat(FlowGraphUtil.hasSuccessors(gen.get("out2")), is(false));
    }

    /**
     * {@link FlowGraphUtil#hasSuccessors(FlowElement)}
     */
    @Test
    public void hasPredecessors() {
        gen.defineInput("in1");
        gen.defineOutput("out1");
        gen.defineInput("in2");
        gen.defineOutput("out2");

        gen.connect("in1", "out1");

        assertThat(FlowGraphUtil.hasPredecessors(gen.get("in1")), is(false));
        assertThat(FlowGraphUtil.hasPredecessors(gen.get("in2")), is(false));
        assertThat(FlowGraphUtil.hasPredecessors(gen.get("out1")), is(true));
        assertThat(FlowGraphUtil.hasPredecessors(gen.get("out2")), is(false));
    }

    /**
     * {@link FlowGraphUtil#getSuccessors(FlowElement)}
     */
    @Test
    public void getSuccessors() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineInput("in3");
        gen.definePseud("id1");
        gen.definePseud("id2");
        gen.definePseud("id3");
        gen.defineOperator("op", "a b", "c d");
        gen.definePseud("ida");
        gen.definePseud("idb");
        gen.definePseud("idc");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.defineOutput("out3");

        gen.connect("in1", "id1").connect("in2", "id2").connect("in3", "id3");
        gen.connect("id1", "op.a").connect("id2", "op.a").connect("id3", "op.b");
        gen.connect("op.c", "ida").connect("op.d", "idb").connect("op.d", "idc");
        gen.connect("ida", "out1").connect("idb", "out2").connect("idc", "out3");

        assertThat(FlowGraphUtil.getSuccessors(gen.get("op")),
            is(gen.getAsSet("ida", "idb", "idc")));
    }

    /**
     * {@link FlowGraphUtil#getPredecessors(FlowElement)}
     */
    @Test
    public void getPredecessors() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineInput("in3");
        gen.definePseud("id1");
        gen.definePseud("id2");
        gen.definePseud("id3");
        gen.defineOperator("op", "a b", "c d");
        gen.definePseud("ida");
        gen.definePseud("idb");
        gen.definePseud("idc");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.defineOutput("out3");

        gen.connect("in1", "id1").connect("in2", "id2").connect("in3", "id3");
        gen.connect("id1", "op.a").connect("id2", "op.a").connect("id3", "op.b");
        gen.connect("op.c", "ida").connect("op.d", "idb").connect("op.d", "idc");
        gen.connect("ida", "out1").connect("idb", "out2").connect("idc", "out3");

        assertThat(FlowGraphUtil.getPredecessors(gen.get("op")),
            is(gen.getAsSet("id1", "id2", "id3")));
    }

    /**
     * {@link FlowGraphUtil#getSucceedingBoundaries(FlowElementOutput)}
     */
    @Test
    public void getSucceedingBoundaries() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineInput("in3");
        gen.defineOperator("op", "a b", "c d");
        gen.definePseud("ida");
        gen.definePseud("idb");
        gen.definePseud("idc");
        gen.definePseud("cp1", FlowBoundary.STAGE);
        gen.definePseud("cp2", FlowBoundary.STAGE);
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.defineOutput("out3");

        gen.connect("in1", "op.a").connect("in2", "op.a").connect("in3", "op.b");
        gen.connect("op.c", "cp1").connect("op.d", "idb").connect("op.d", "idc");
        gen.connect("cp1", "ida").connect("idb", "cp2").connect("idc", "out3");
        gen.connect("ida", "out1").connect("cp2", "out2");

        assertThat(FlowGraphUtil.getSucceedingBoundaries(gen.output("op.c")),
            is(gen.getAsSet("cp1")));
        assertThat(FlowGraphUtil.getSucceedingBoundaries(gen.output("op.d")),
            is(gen.getAsSet("cp2", "out3")));
    }

    /**
     * {@link FlowGraphUtil#insertCheckpoint(FlowElementOutput)}
     */
    @Test
    public void insertCheckpoint() {
        gen.defineInput("in");
        gen.defineOutput("out");

        gen.connect("in", "out");

        FlowGraphUtil.insertCheckpoint(gen.output("in"));

        Set<FlowElement> succ = FlowGraphUtil.getSuccessors(gen.get("in"));
        assertThat(succ.size(), is(1));
        FlowElement elem = succ.iterator().next();
        assertThat(FlowGraphUtil.isStagePadding(elem), is(true));
        assertThat(FlowGraphUtil.getSuccessors(elem),
            is(gen.getAsSet("out")));
    }

    /**
     * {@link FlowGraphUtil#insertCheckpoint(FlowElementOutput)}
     */
    @Test
    public void insertCheckpoint_3() {
        gen.defineInput("in");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.defineOutput("out3");

        gen.connect("in", "out1");
        gen.connect("in", "out2");
        gen.connect("in", "out3");

        FlowGraphUtil.insertCheckpoint(gen.output("in"));

        Set<FlowElement> succ = FlowGraphUtil.getSuccessors(gen.get("in"));
        assertThat(succ.size(), is(1));
        FlowElement elem = succ.iterator().next();
        assertThat(FlowGraphUtil.isStagePadding(elem), is(true));
        assertThat(FlowGraphUtil.getSuccessors(elem),
            is(gen.getAsSet("out1", "out2", "out3")));
    }

    /**
     * {@link FlowGraphUtil#insertIdentity(FlowElementOutput)}
     */
    @Test
    public void insertIdentity() {
        gen.defineInput("in");
        gen.defineOutput("out");

        gen.connect("in", "out");

        FlowGraphUtil.insertIdentity(gen.output("in"));

        Set<FlowElement> succ = FlowGraphUtil.getSuccessors(gen.get("in"));
        assertThat(succ.size(), is(1));
        FlowElement elem = succ.iterator().next();
        assertThat(FlowGraphUtil.isIdentity(elem), is(true));
        assertThat(FlowGraphUtil.getSuccessors(elem),
            is(gen.getAsSet("out")));
    }

    /**
     * {@link FlowGraphUtil#insertIdentity(FlowElementOutput)}
     */
    @Test
    public void insertIdentity_3() {
        gen.defineInput("in");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.defineOutput("out3");

        gen.connect("in", "out1");
        gen.connect("in", "out2");
        gen.connect("in", "out3");

        FlowGraphUtil.insertIdentity(gen.output("in"));

        Set<FlowElement> succ = FlowGraphUtil.getSuccessors(gen.get("in"));
        assertThat(succ.size(), is(1));
        FlowElement elem = succ.iterator().next();
        assertThat(FlowGraphUtil.isIdentity(elem), is(true));
        assertThat(FlowGraphUtil.getSuccessors(elem),
            is(gen.getAsSet("out1", "out2", "out3")));
    }

    /**
     * {@link FlowGraphUtil#disconnect(FlowElement)}
     */
    @Test
    public void disconnect() {
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineOperator("op1", "a b", "c d");
        gen.defineOperator("op2", "a b", "c d");
        gen.defineOutput("out1");
        gen.defineOutput("out2");

        gen.connect("in1", "op1.a").connect("in1", "op2.a");
        gen.connect("in2", "op1.b").connect("in2", "op2.b");
        gen.connect("op1.c", "out1").connect("op2.c", "out1");
        gen.connect("op1.d", "out2").connect("op2.d", "out2");

        FlowGraphUtil.disconnect(gen.get("op2"));
        Graph<FlowElement> graph = FlowGraphUtil.toElementGraph(gen.toGraph());
        assertThat(graph.getConnected(gen.get("in1")),
            is(gen.getAsSet("op1")));
        assertThat(graph.getConnected(gen.get("in2")),
            is(gen.getAsSet("op1")));
        assertThat(graph.getConnected(gen.get("op1")),
            is(gen.getAsSet("out1", "out2")));
        assertThat(graph.getConnected(gen.get("op2")),
            is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out1")),
            is(gen.getAsSet()));
        assertThat(graph.getConnected(gen.get("out2")),
            is(gen.getAsSet()));
    }

    /**
     * {@link FlowGraphUtil#inlineFlowPart(FlowElement, FlowElementAttribute...)}
     */
    @Test
    public void inlineFlowPart() {
        FlowGraphGenerator cgen = new FlowGraphGenerator();
        cgen.defineInput("in");
        cgen.defineOutput("out");
        cgen.defineOperator("op", "in", "out");
        cgen.connect("in", "op.in");
        cgen.connect("op.out", "out");
        FlowGraph component = cgen.toGraph();

        gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOutput("out");
        FlowElement fc = gen.defineFlowPart("c", component);
        gen.connect("in", "c.in");
        gen.connect("c.out", "out");

        FlowGraphUtil.inlineFlowPart(fc);

        Graph<FlowElement> graph = FlowGraphUtil.toElementGraph(gen.toGraph());

        assertThat(graph.contains(gen.get("c")), is(false));
        Set<FlowElement> path = Graphs.collectAllConnected(graph, gen.getAsSet("in"));
        assertThat(path, hasItems(cgen.get("op"), gen.get("out")));
        assertThat(path, not(hasItem(cgen.get("in"))));
        assertThat(path, not(hasItem(cgen.get("out"))));
    }
}
