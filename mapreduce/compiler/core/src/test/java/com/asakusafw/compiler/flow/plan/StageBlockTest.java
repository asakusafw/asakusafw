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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.compiler.flow.FlowGraphGenerator;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;

/**
 * Test for {@link StageBlock}.
 */
public class StageBlockTest {

    /**
     * {@link StageBlock#isEmpty()}
     */
    @Test
    public void isEmpty_true() {
        StageBlock stage = new StageBlock(set(), set());
        assertThat(stage.isEmpty(), is(true));
    }

    /**
     * {@link StageBlock#isEmpty()}
     */
    @Test
    public void isEmpty_hasMapper() {
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

        StageBlock stage = new StageBlock(set(b1), set());
        assertThat(stage.isEmpty(), is(false));
    }

    /**
     * {@link StageBlock#isEmpty()}
     */
    @Test
    public void isEmpty_hasReducer() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineOperator("op1", "in", "out");
        gen.defineOperator("op2", "in", "out", FlowBoundary.SHUFFLE);
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
                Arrays.asList(gen.output("op1")),
                gen.getAsSet("op1"));
        FlowBlock b2 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op2")),
                Arrays.asList(gen.output("op2")),
                gen.getAsSet("op2"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        FlowBlock.connect(b1.getBlockOutputs().get(0), b2.getBlockInputs().get(0));
        FlowBlock.connect(b2.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        b2.detach();
        bout.detach();

        StageBlock stage = new StageBlock(set(b1), set(b2));
        assertThat(stage.isEmpty(), is(false));
    }

    /**
     * {@link StageBlock#compaction()}
     */
    @Test
    public void compaction_stable() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.defineOperator("op1", "in", "out");
        gen.defineOutput("out1");
        gen.connect("in1", "op1").connect("op1", "out1");
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
                Arrays.asList(gen.output("op1")),
                gen.getAsSet("op1"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        FlowBlock.connect(b1.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        StageBlock stage = new StageBlock(set(b1), set());
        assertThat(stage.isEmpty(), is(false));
    }

    /**
     * {@link StageBlock#compaction()}
     */
    @Test
    public void compaction_hasReducer() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.definePseud("op1");
        gen.defineOperator("op2", "in", "out", FlowBoundary.SHUFFLE);
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
                Arrays.asList(gen.output("op1")),
                gen.getAsSet("op1"));
        FlowBlock b2 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op2")),
                Arrays.asList(gen.output("op2")),
                gen.getAsSet("op2"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        FlowBlock.connect(b1.getBlockOutputs().get(0), b2.getBlockInputs().get(0));
        FlowBlock.connect(b2.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        b2.detach();
        bout.detach();

        StageBlock stage = new StageBlock(set(b1), set(b2));
        assertThat(stage.compaction(), is(false));
    }

    /**
     * {@link StageBlock#compaction()}
     */
    @Test
    public void compaction_bypass() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.definePseud("id");
        gen.defineOperator("op1", "in", "out");
        gen.defineOutput("out1");
        gen.connect("in1", "id").connect("id", "out1");
        gen.connect("in1", "op1").connect("op1", "out1");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1")),
                gen.getAsSet("in1"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("op1"), gen.input("id")),
                Arrays.asList(gen.output("op1"), gen.output("id")),
                gen.getAsSet("op1", "id"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(1));
        FlowBlock.connect(b1.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        FlowBlock.connect(b1.getBlockOutputs().get(1), bout.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        StageBlock stage = new StageBlock(set(b1), set());
        assertThat(stage.compaction(), is(true));
        assertThat(stage.getMapBlocks().size(), is(1));

        assertThat(b1.getBlockInputs().size(), is(1));
        assertThat(b1.getBlockOutputs().size(), is(1));

        assertThat(b1.getBlockInputs().get(0).getElementPort().getDescription(),
                is(gen.input("op1").getDescription()));
        assertThat(b1.getBlockOutputs().get(0).getElementPort().getDescription(),
                is(gen.output("op1").getDescription()));

        FlowBlock.Output binOut = bin.getBlockOutputs().get(0);
        FlowBlock.Input boutIn = bout.getBlockInputs().get(0);
        assertThat(binOut.getConnections().size(), is(2));
        assertThat(boutIn.getConnections().size(), is(2));

        assertThat(FlowBlock.isConnected(binOut, boutIn), is(true));
    }

    /**
     * {@link StageBlock#compaction()}
     */
    @Test
    public void compaction_removeBlock() {
        FlowGraphGenerator gen = new FlowGraphGenerator();
        gen.defineInput("in1");
        gen.definePseud("id");
        gen.defineOutput("out1");
        gen.connect("in1", "id").connect("id", "out1");
        FlowBlock bin = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                new ArrayList<>(gen.inputs()),
                Arrays.asList(gen.output("in1")),
                gen.getAsSet("in1"));
        FlowBlock b1 = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("id")),
                Arrays.asList(gen.output("id")),
                gen.getAsSet("id"));
        FlowBlock bout = FlowBlock.fromPorts(
                0,
                gen.toGraph(),
                Arrays.asList(gen.input("out1")),
                new ArrayList<>(gen.outputs()),
                gen.getAsSet("out1"));
        FlowBlock.connect(bin.getBlockOutputs().get(0), b1.getBlockInputs().get(0));
        FlowBlock.connect(b1.getBlockOutputs().get(0), bout.getBlockInputs().get(0));
        bin.detach();
        b1.detach();
        bout.detach();

        StageBlock stage = new StageBlock(set(b1), set());
        assertThat(stage.compaction(), is(true));
        assertThat(stage.getMapBlocks().size(), is(0));
    }

    private Set<FlowBlock> set(FlowBlock... blocks) {
        Set<FlowBlock> results = new HashSet<>();
        Collections.addAll(results, blocks);
        return results;
    }
}
