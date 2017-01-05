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

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.asakusafw.compiler.flow.FlowGraphGenerator;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;

/**
 * Test for {@link FlowBlock}.
 */
public class FlowBlockTest {

    /**
     * {@link FlowBlock#isEmpty()}
     */
    @Test
    public void isEmpty() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op").connect("op", "out");

        FlowBlock block = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("op"));

        assertThat(block.isEmpty(), is(true));
    }

    /**
     * {@link FlowBlock#isEmpty()}
     */
    @Test
    public void isEmpty_input() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op").connect("op", "out");

        FlowBlock block = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("op"));

        assertThat(block.isEmpty(), is(false));
    }

    /**
     * {@link FlowBlock#isEmpty()}
     */
    @Test
    public void isEmpty_output() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op").connect("op", "out");

        FlowBlock block = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                new ArrayList<>(gen.outputs("op")),
                gen.getAsSet("op"));

        assertThat(block.isEmpty(), is(false));
    }

    /**
     * {@link FlowBlock#isReduceBlock()}
     */
    @Test
    public void isReduceBlock_true() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op", "in", "out", FlowBoundary.SHUFFLE);
        gen.defineOutput("out");
        gen.connect("in", "op").connect("op", "out");

        FlowBlock block = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op")),
                new ArrayList<>(gen.outputs("op")),
                gen.getAsSet("op"));

        assertThat(block.isReduceBlock(), is(true));
    }

    /**
     * {@link FlowBlock#isReduceBlock()}
     */
    @Test
    public void isReduceBlock_false() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op").connect("op", "out");

        FlowBlock block = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op")),
                new ArrayList<>(gen.outputs("op")),
                gen.getAsSet("op"));

        assertThat(block.isReduceBlock(), is(false));
    }

    /**
     * {@link FlowBlock#detach()}
     */
    @Test
    public void detach_1() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op").connect("op", "out");

        FlowBlock block = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op")),
                new ArrayList<>(gen.outputs("op")),
                gen.getAsSet("op"));

        block.detach();
        assertThat(block.getElements().size(), is(1));
        assertThat(block.getBlockInputs().size(), is(1));
        assertThat(block.getBlockOutputs().size(), is(1));

        FlowElement op = block.getElements().iterator().next();
        FlowBlock.Input input = block.getBlockInputs().get(0);
        FlowBlock.Output output = block.getBlockOutputs().get(0);

        assertThat(op, not(sameInstance(gen.get("op"))));
        assertThat(input.getElementPort(), not(sameInstance(gen.input("op"))));
        assertThat(output.getElementPort(), not(sameInstance(gen.output("op"))));

        assertThat(input.getElementPort().getOwner(), is(op));
        assertThat(output.getElementPort().getOwner(), is(op));

        assertThat(input.getConnections().isEmpty(), is(true));
        assertThat(output.getConnections().isEmpty(), is(true));
    }

    /**
     * {@link FlowBlock#detach()}
     */
    @Test
    public void detach_2() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op1").connect("op1", "op2").connect("op2", "out");

        FlowBlock block = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op1")),
                new ArrayList<>(gen.outputs("op2")),
                gen.getAsSet("op1", "op2"));

        block.detach();
        assertThat(block.getElements().size(), is(2));
        assertThat(block.getBlockInputs().size(), is(1));
        assertThat(block.getBlockOutputs().size(), is(1));

        FlowBlock.Input input = block.getBlockInputs().get(0);
        FlowBlock.Output output = block.getBlockOutputs().get(0);

        assertThat(input.getElementPort(), not(sameInstance(gen.input("op1"))));
        assertThat(output.getElementPort(), not(sameInstance(gen.output("op2"))));

        FlowElement op1 = input.getElementPort().getOwner();
        FlowElement op2 = output.getElementPort().getOwner();

        assertThat(op1.getInputPorts().get(0).getConnected().size(), is(0));
        assertThat(op1.getOutputPorts().get(0).getConnected().size(), is(1));
        assertThat(op2.getInputPorts().get(0).getConnected().size(), is(1));
        assertThat(op2.getOutputPorts().get(0).getConnected().size(), is(0));

        assertThat(op1.getOutputPorts().get(0).getConnected(),
                is(op2.getInputPorts().get(0).getConnected()));
    }

    /**
     * {@link FlowBlock#connect(FlowBlock.Output, FlowBlock.Input)}
     */
    @Test
    public void connect() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op1").connect("op1", "op2").connect("op2", "out");

        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op1")),
                new ArrayList<>(gen.outputs("op1")),
                gen.getAsSet("op1"));

        FlowBlock b2 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op2")),
                new ArrayList<>(gen.outputs("op2")),
                gen.getAsSet("op2"));

        FlowBlock.connect(b1.getBlockOutputs().get(0), b2.getBlockInputs().get(0));
        b1.detach();
        b2.detach();
        assertThat(b1.getBlockInputs().get(0).getConnections().size(), is(0));
        assertThat(b2.getBlockInputs().get(0).getConnections().size(), is(1));
        assertThat(b1.getBlockOutputs().get(0).getConnections().size(), is(1));
        assertThat(b2.getBlockOutputs().get(0).getConnections().size(), is(0));

        assertThat(b1.getBlockOutputs().get(0).getConnections().get(0).getUpstream(),
                is(b1.getBlockOutputs().get(0)));
        assertThat(b1.getBlockOutputs().get(0).getConnections().get(0).getDownstream(),
                is(b2.getBlockInputs().get(0)));
        assertThat(b2.getBlockInputs().get(0).getConnections().get(0).getDownstream(),
                is(b2.getBlockInputs().get(0)));
        assertThat(b2.getBlockInputs().get(0).getConnections().get(0).getUpstream(),
                is(b1.getBlockOutputs().get(0)));
    }

    /**
     * {@link FlowBlock#isSucceedingReduceBlock()}
     */
    @Test
    public void isSucceedingReduceBlock_true() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out", FlowBoundary.SHUFFLE);
        gen.defineOutput("out");
        gen.connect("in", "op1").connect("op1", "op2").connect("op2", "out");

        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op1")),
                new ArrayList<>(gen.outputs("op1")),
                gen.getAsSet("op1"));

        FlowBlock b2 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op2")),
                new ArrayList<>(gen.outputs("op2")),
                gen.getAsSet("op2"));

        FlowBlock.connect(b1.getBlockOutputs().get(0), b2.getBlockInputs().get(0));
        b1.detach();
        b2.detach();
        assertThat(b1.isSucceedingReduceBlock(), is(true));
    }

    /**
     * {@link FlowBlock#isSucceedingReduceBlock()}
     */
    @Test
    public void isSucceedingReduceBlock_false() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");
        gen.defineOutput("out");
        gen.connect("in", "op1").connect("op1", "op2").connect("op2", "out");

        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op1")),
                new ArrayList<>(gen.outputs("op1")),
                gen.getAsSet("op1"));

        FlowBlock b2 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op2")),
                new ArrayList<>(gen.outputs("op2")),
                gen.getAsSet("op2"));

        FlowBlock.connect(b1.getBlockOutputs().get(0), b2.getBlockInputs().get(0));
        b1.detach();
        b2.detach();
        assertThat(b1.isSucceedingReduceBlock(), is(false));
    }

    /**
     * {@link FlowBlock#isSucceedingReduceBlock()}
     */
    @Test
    public void isSucceedingReduceBlock_empty() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out", FlowBoundary.SHUFFLE);
        gen.defineOutput("out");
        gen.connect("in", "op1").connect("op1", "op2").connect("op2", "out");

        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op1")),
                new ArrayList<>(gen.outputs("op1")),
                gen.getAsSet("op1"));

        FlowBlock b2 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs("op2")),
                new ArrayList<>(gen.outputs("op2")),
                gen.getAsSet("op2"));

        b1.detach();
        b2.detach();
        assertThat(b1.isSucceedingReduceBlock(), is(false));
    }

    /**
     * {@link FlowBlock#compaction()}
     */
    @Test
    public void compaction_deadIn() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineOperator("op1", "in1 in2", "out1 out2");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.connect("in1", "op1.in1").connect("op1.out1", "out1");
        gen.connect("in2", "op1.in2").connect("op1.out2", "out2");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1"), gen.output("in2")),
                gen.getAsSet("in1", "in2"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op1.in1"), gen.input("op1.in2")),
                Arrays.asList(gen.output("op1.out1"), gen.output("op1.out2")),
                gen.getAsSet("op1"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1"), gen.input("out2")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1", "out2"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        FlowBlock.connect(b1.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        FlowBlock.connect(b1.getBlockOutputs().get(1), bout.getBlockInputs().get(1));
        bin.detach();
        b1.detach();
        bout.detach();

        assertThat(b1.compaction(), is(true));
        assertThat(b1.getBlockInputs().size(), is(1));
        assertThat(b1.getBlockOutputs().size(), is(2));
        assertThat(b1.getElements().size(), is(1));

        assertThat(b1.getBlockInputs().get(0).getElementPort().getDescription().getName(),
                is("in1"));
    }

    /**
     * {@link FlowBlock#compaction()}
     */
    @Test
    public void compaction_deadOut() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineInput("in2");
        gen.defineOperator("op1", "in1 in2", "out1 out2");
        gen.defineOutput("out1");
        gen.defineOutput("out2");
        gen.connect("in1", "op1.in1").connect("op1.out1", "out1");
        gen.connect("in2", "op1.in2").connect("op1.out2", "out2");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1"), gen.output("in2")),
                gen.getAsSet("in1", "in2"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op1.in1"), gen.input("op1.in2")),
                Arrays.asList(gen.output("op1.out1"), gen.output("op1.out2")),
                gen.getAsSet("op1"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1"), gen.input("out2")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1", "out2"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        FlowBlock.connect(bin.getBlockOutputs().get(1), b1.getBlockInputs().get(1));
        FlowBlock.connect(b1.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        assertThat(b1.compaction(), is(true));
        assertThat(b1.getBlockInputs().size(), is(2));
        assertThat(b1.getBlockOutputs().size(), is(1));
        assertThat(b1.getElements().size(), is(1));

        assertThat(b1.getBlockOutputs().get(0).getElementPort().getDescription().getName(),
                is("out1"));
    }

    /**
     * {@link FlowBlock#compaction()}
     */
    @Test
    public void compaction_emptyIn() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");
        gen.defineOutput("out1");
        gen.connect("in1", "op1").connect("op1", "op2").connect("op2", "out1");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1")),
                gen.getAsSet("in1"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op1")),
                Arrays.asList(gen.output("op2")),
                gen.getAsSet("op1", "op2"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(b1.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        assertThat(b1.compaction(), is(true));
        assertThat(b1.getBlockInputs().size(), is(0));
        assertThat(b1.getBlockOutputs().size(), is(0));
        assertThat(b1.getElements().size(), is(0));
    }

    /**
     * {@link FlowBlock#compaction()}
     */
    @Test
    public void compaction_stopOut() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");
        gen.defineOutput("out1");
        gen.connect("in1", "op1").connect("op1", "op2").connect("op2", "out1");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1")),
                gen.getAsSet("in1"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op1")),
                Arrays.asList(gen.output("op2")),
                gen.getAsSet("op1", "op2"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        assertThat(b1.compaction(), is(true));
        assertThat(b1.getBlockInputs().size(), is(0));
        assertThat(b1.getBlockOutputs().size(), is(0));
        assertThat(b1.getElements().size(), is(0));
    }

    /**
     * {@link FlowBlock#compaction()}
     */
    @Test
    public void compaction_mandatoryStop() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out", ObservationCount.AT_LEAST_ONCE);
        gen.defineOutput("out1");
        gen.connect("in1", "op1").connect("op1", "op2").connect("op2", "out1");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1")),
                gen.getAsSet("in1"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op1")),
                Arrays.asList(gen.output("op2")),
                gen.getAsSet("op1", "op2"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        assertThat(b1.compaction(), is(true));
        assertThat(b1.getBlockInputs().size(), is(1));
        assertThat(b1.getBlockOutputs().size(), is(0));
        assertThat(b1.getElements().size(), is(2));
    }

    /**
     * {@link FlowBlock#compaction()}
     */
    @Test
    public void compaction_stable() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out");
        gen.defineOutput("out1");
        gen.connect("in1", "op1").connect("op1", "op2").connect("op2", "out1");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1")),
                gen.getAsSet("in1"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op1")),
                Arrays.asList(gen.output("op2")),
                gen.getAsSet("op1", "op2"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(b1.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        assertThat(b1.compaction(), is(false));
    }
}
