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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.compiler.flow.FlowGraphGenerator;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Test for {@link FlowPath}.
 */
public class FlowPathTest {

    /**
     * {@link FlowPath#union(FlowPath)}
     */
    @Test
    public void union() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.definePseud("a", FlowBoundary.STAGE);
        gen.definePseud("b", FlowBoundary.STAGE);
        gen.definePseud("c", FlowBoundary.STAGE);
        gen.definePseud("d", FlowBoundary.STAGE);
        gen.definePseud("e", FlowBoundary.STAGE);
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");

        gen.connect("in1", "a").connect("a", "op1").connect("op1", "c").connect("c", "out1");
        gen.connect("in2", "b").connect("b", "op2").connect("op2", "d").connect("d", "out2");
        gen.connect("op1", "e").connect("e", "out2");

        FlowPath a = FlowGraphUtil.getSucceedBoundaryPath(gen.get("a"));
        FlowPath b = FlowGraphUtil.getSucceedBoundaryPath(gen.get("b"));

        FlowPath path = a.union(b);
        assertThat(path.getStartings(), is(gen.getAsSet("a", "b")));
        assertThat(path.getPassings(), is(gen.getAsSet("op1", "op2")));
        assertThat(path.getArrivals(), is(gen.getAsSet("c", "d", "e")));
    }

    /**
     * {@link FlowPath#transposeIntersect(FlowPath)}
     */
    @Test
    public void transposeIntersect() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.definePseud("a", FlowBoundary.STAGE);
        gen.definePseud("b", FlowBoundary.STAGE);
        gen.definePseud("c", FlowBoundary.STAGE);
        gen.definePseud("d", FlowBoundary.STAGE);
        gen.definePseud("e", FlowBoundary.STAGE);
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");

        gen.connect("in1", "a").connect("a", "op1").connect("op1", "c").connect("c", "out1");
        gen.connect("in2", "b").connect("b", "op2").connect("op2", "d").connect("d", "out2");
        gen.connect("op1", "e").connect("e", "out2");

        FlowPath a = FlowGraphUtil.getSucceedBoundaryPath(gen.get("a"));
        FlowPath b = FlowGraphUtil.getPredeceaseBoundaryPath(gen.get("e"));

        FlowPath path = a.transposeIntersect(b);
        assertThat(path.getStartings(), is(gen.getAsSet("a")));
        assertThat(path.getPassings(), is(gen.getAsSet("op1")));
        assertThat(path.getArrivals(), is(gen.getAsSet("e")));
    }

    /**
     * {@link FlowPath#createBlock(FlowGraph, int, boolean, boolean)}
     */
    @Test
    public void createBlock_includeIn_includeOut() {
        FlowGraphGenerator gen = graph();
        FlowPath a = FlowGraphUtil.getSucceedBoundaryPath(gen.get("a"));
        FlowPath b = FlowGraphUtil.getSucceedBoundaryPath(gen.get("b"));
        FlowPath c = FlowGraphUtil.getSucceedBoundaryPath(gen.get("c"));
        FlowPath in3 = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in3"));
        FlowPath path = a.union(b).union(c).union(in3);
        FlowBlock block = path.createBlock(gen.toGraph(), 0, true, true);

        assertThat(block.getElements(),
            is(gen.getAsSet("a", "b", "c", "d", "e", "f", "op1", "op2", "in3", "out3")));
        Set<FlowElementInput> inputs = input(block.getBlockInputs());
        Set<FlowElementOutput> outputs = output(block.getBlockOutputs());
        assertThat(inputs, is(gen.inputs("a", "b", "c")));
        assertThat(outputs, is(gen.outputs("d", "e", "f")));
    }

    /**
     * {@link FlowPath#createBlock(FlowGraph, int, boolean, boolean)}
     */
    @Test
    public void createBlock_excludeIn_includeOut() {
        FlowGraphGenerator gen = graph();
        FlowPath a = FlowGraphUtil.getSucceedBoundaryPath(gen.get("a"));
        FlowPath b = FlowGraphUtil.getSucceedBoundaryPath(gen.get("b"));
        FlowPath c = FlowGraphUtil.getSucceedBoundaryPath(gen.get("c"));
        FlowPath in3 = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in3"));
        FlowPath path = a.union(b).union(c).union(in3);
        FlowBlock block = path.createBlock(gen.toGraph(), 0, false, true);

        assertThat(block.getElements(),
            is(gen.getAsSet("d", "e", "f", "op1", "op2", "out3")));
        Set<FlowElementInput> inputs = input(block.getBlockInputs());
        Set<FlowElementOutput> outputs = output(block.getBlockOutputs());
        assertThat(inputs, is(gen.inputs("op1", "op2")));
        assertThat(outputs, is(gen.outputs("d", "e", "f")));
    }

    /**
     * {@link FlowPath#createBlock(FlowGraph, int, boolean, boolean)}
     */
    @Test
    public void createBlock_includeIn_excludeOut() {
        FlowGraphGenerator gen = graph();
        FlowPath a = FlowGraphUtil.getSucceedBoundaryPath(gen.get("a"));
        FlowPath b = FlowGraphUtil.getSucceedBoundaryPath(gen.get("b"));
        FlowPath c = FlowGraphUtil.getSucceedBoundaryPath(gen.get("c"));
        FlowPath in3 = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in3"));
        FlowPath path = a.union(b).union(c).union(in3);
        FlowBlock block = path.createBlock(gen.toGraph(), 0, true, false);

        assertThat(block.getElements(),
            is(gen.getAsSet("a", "b", "c", "op1", "op2", "in3")));
        Set<FlowElementInput> inputs = input(block.getBlockInputs());
        Set<FlowElementOutput> outputs = output(block.getBlockOutputs());
        assertThat(inputs, is(gen.inputs("a", "b", "c")));
        assertThat(outputs, is(gen.outputs("op1", "op2")));
    }

    /**
     * {@link FlowPath#createBlock(FlowGraph, int, boolean, boolean)}
     */
    @Test
    public void createBlock_excludeIn_excludeOut() {
        FlowGraphGenerator gen = graph();
        FlowPath a = FlowGraphUtil.getSucceedBoundaryPath(gen.get("a"));
        FlowPath b = FlowGraphUtil.getSucceedBoundaryPath(gen.get("b"));
        FlowPath c = FlowGraphUtil.getSucceedBoundaryPath(gen.get("c"));
        FlowPath in3 = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in3"));
        FlowPath path = a.union(b).union(c).union(in3);
        FlowBlock block = path.createBlock(gen.toGraph(), 0, false, false);

        assertThat(block.getElements(), is(gen.getAsSet("op1", "op2")));
        Set<FlowElementInput> inputs = input(block.getBlockInputs());
        Set<FlowElementOutput> outputs = output(block.getBlockOutputs());
        assertThat(inputs, is(gen.inputs("op1", "op2")));
        assertThat(outputs, is(gen.outputs("op1", "op2")));
    }

    /**
     * {@link FlowPath#createBlock(FlowGraph, int, boolean, boolean)}
     */
    @Test
    public void createBlock_empty() {
        FlowGraphGenerator gen = graph();
        gen.defineInput("in");
        gen.defineOutput("out");
        gen.connect("in", "out");
        FlowPath path = FlowGraphUtil.getSucceedBoundaryPath(gen.get("in"));

        FlowBlock b0 = path.createBlock(gen.toGraph(), 0, true, true);
        assertThat(b0.getElements(), is(gen.getAsSet("in", "out")));
        assertThat(input(b0.getBlockInputs()), is(gen.inputs()));
        assertThat(output(b0.getBlockOutputs()), is(gen.outputs()));

        FlowBlock b1 = path.createBlock(gen.toGraph(), 0, false, true);
        assertThat(b1.getElements(), is(gen.getAsSet("out")));
        assertThat(input(b1.getBlockInputs()), is(gen.inputs("out")));
        assertThat(output(b1.getBlockOutputs()), is(gen.outputs()));

        FlowBlock b2 = path.createBlock(gen.toGraph(), 0, true, false);
        assertThat(b2.getElements(), is(gen.getAsSet("in")));
        assertThat(input(b2.getBlockInputs()), is(gen.inputs()));
        assertThat(output(b2.getBlockOutputs()), is(gen.outputs("in")));

        try {
            path.createBlock(gen.toGraph(), 0, false, false);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    private FlowGraphGenerator graph() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineInput("in3");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.defineOutput("out3");
        gen.definePseud("a", FlowBoundary.STAGE);
        gen.definePseud("b", FlowBoundary.STAGE);
        gen.definePseud("c", FlowBoundary.STAGE);
        gen.definePseud("d", FlowBoundary.STAGE);
        gen.definePseud("e", FlowBoundary.STAGE);
        gen.definePseud("f", FlowBoundary.STAGE);
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");
        gen.connect("in1", "a").connect("a", "op1").connect("op1", "d").connect("d", "out1");
        gen.connect("in2", "b").connect("b", "op2").connect("op2", "e").connect("e", "out2");
        gen.connect("in1", "c").connect("c", "op1").connect("op2", "f").connect("f", "out1");
        gen.connect("in3", "op2").connect("op1", "out3");
        return gen;
    }

    private Set<FlowElementInput> input(Collection<FlowBlock.Input> inputs) {
        Set<FlowElementInput> results = new HashSet<>();
        for (FlowBlock.Input port : inputs) {
            results.add(port.getElementPort());
        }
        return results;
    }

    private Set<FlowElementOutput> output(Collection<FlowBlock.Output> outputs) {
        Set<FlowElementOutput> results = new HashSet<>();
        for (FlowBlock.Output port : outputs) {
            results.add(port.getElementPort());
        }
        return results;
    }
}
