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
package com.asakusafw.vocabulary.flow.util;

import static com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Checkpoint;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Empty;

/**
 * Test for {@link CoreOperatorFactory}.
 */
public class CoreOperatorFactoryTest {

    MockIn<String> in = new MockIn<>(String.class, "in");

    MockIn<String> in2 = new MockIn<>(String.class, "in2");

    MockOut<String> out = new MockOut<>(String.class, "out");

    /**
     * test for {@link CoreOperatorFactory#empty(Class)}.
     */
    @Test
    public void empty() {
        CoreOperatorFactory f = new CoreOperatorFactory();
        Empty<String> empty = f.empty(String.class);
        out.add(empty);

        Graph<String> graph = toGraph();
        assertThat(
                graph.getConnected("in"),
                connected());
        assertThat(
                graph.getConnected("in2"),
                connected());
        assertThat(
                graph.getConnected(EMPTY_NAME),
                connected("out"));
    }

    /**
     * test for {@link CoreOperatorFactory#stop(Source)}.
     */
    @Test
    public void stop() {
        CoreOperatorFactory f = new CoreOperatorFactory();
        f.stop(in);

        Graph<String> graph = toGraph();
        assertThat(
                graph.getConnected("in"),
                connected(STOP_NAME));
        assertThat(
                graph.getConnected("in2"),
                connected());
    }

    /**
     * test for {@link CoreOperatorFactory#confluent(Source, Source)}.
     */
    @Test
    public void confluent() {
        CoreOperatorFactory f = new CoreOperatorFactory();
        out.add(f.confluent(in, in2));

        Graph<String> graph = toGraph();
        assertThat(
                graph.getConnected("in"),
                connected(CONFLUENT_NAME));
        assertThat(
                graph.getConnected("in2"),
                connected(CONFLUENT_NAME));
        assertThat(
                graph.getConnected(CONFLUENT_NAME),
                connected("out"));
    }

    /**
     * test for {@link CoreOperatorFactory#checkpoint(Source)}.
     */
    @Test
    public void checkpoint() {
        CoreOperatorFactory f = new CoreOperatorFactory();
        Checkpoint<String> cp = f.checkpoint(in);
        out.add(cp);

        Graph<String> graph = toGraph();
        assertThat(
                graph.getConnected("in"),
                connected(CHECKPOINT_NAME));
        assertThat(
                graph.getConnected("in2"),
                connected());
        assertThat(
                graph.getConnected(CHECKPOINT_NAME),
                connected("out"));
    }

    private Matcher<? super Set<String>> connected(String... names) {
        return Matchers.is(new HashSet<>(Arrays.asList(names)));
    }

    private Graph<String> toGraph() {
        Set<String> saw = new HashSet<>();
        LinkedList<FlowElement> work = new LinkedList<>();
        work.add(in.toElement());
        work.add(in2.toElement());
        work.add(out.toElement());
        Graph<String> graph = Graphs.newInstance();

        while (work.isEmpty() == false) {
            FlowElement elem = work.removeFirst();
            String self = elem.getDescription().getName();
            if (saw.contains(self)) {
                continue;
            }
            saw.add(self);
            for (FlowElementInput input : elem.getInputPorts()) {
                for (PortConnection conn : input.getConnected()) {
                    work.add(conn.getUpstream().getOwner());
                }
            }
            for (FlowElementOutput output : elem.getOutputPorts()) {
                for (PortConnection conn : output.getConnected()) {
                    FlowElement opposite = conn.getDownstream().getOwner();
                    work.add(opposite);
                    String dest = opposite.getDescription().getName();
                    graph.addEdge(self, dest);
                }
            }
        }

        return graph;
    }
}
