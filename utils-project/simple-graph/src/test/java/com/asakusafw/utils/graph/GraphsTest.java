/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.utils.graph;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link Graphs}.
 */
public class GraphsTest {

    /**
     * 空のグラフをコピー。
     */
    @Test
    public void copy_Empty() {
        Graph<Integer> graph = Graphs.newInstance();
        Graph<Integer> copy = Graphs.copy(graph);
        assertThat(copy.isEmpty(), is(true));
    }

    /**
     * グラフをコピー。
     */
    @Test
    public void copy_normal() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4);
        addPath(graph, 2, 1);
        addPath(graph, 3, 2);
        Graph<Integer> copy = Graphs.copy(graph);
        assertThat(copy.getNodeSet(), is(set(1, 2, 3, 4)));
        assertThat(copy.getConnected(1), is(set(2)));
        assertThat(copy.getConnected(2), is(set(1, 3)));
        assertThat(copy.getConnected(3), is(set(2, 4)));
        assertThat(copy.getConnected(4), is(set()));
    }

    /**
     * サブグラフを作成。
     */
    @Test
    public void subgraph() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 5, 1);
        addPath(graph, 1, 3, 5, 2, 4, 1);
        Graph<Integer> sub = Graphs.subgraph(graph, new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object != 5;
            }
        });

        Graph<Integer> expect = Graphs.newInstance();
        addPath(expect, 1, 2, 3, 4, 1);
        addPath(expect, 1, 3);
        addPath(expect, 2, 4);

        assertThat(sub, is(expect));
    }

    /**
     * 空のサブグラフを作成。
     */
    @Test
    public void subgraph_empty() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 5, 1);
        addPath(graph, 1, 3, 5, 2, 4, 1);
        Graph<Integer> sub = Graphs.subgraph(graph, new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return false;
            }
        });

        assertThat(sub.isEmpty(), is(true));
    }

    /**
     * 同一のサブグラフを作成。
     */
    @Test
    public void subgraph_all() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 5, 1);
        addPath(graph, 1, 3, 5, 2, 4, 1);
        Graph<Integer> sub = Graphs.subgraph(graph, new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return true;
            }
        });

        assertThat(sub, is(graph));
    }

    /**
     * Test method for {@link Graphs#collectAllConnected(Graph, java.util.Collection)}.
     */
    @Test
    public void testCollectAllConnected_Cyclic() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 2);
        addPath(graph, 3, 5);
        assertThat(Graphs.collectAllConnected(graph, set(1)), is(set(2, 3, 4, 5)));
        assertThat(Graphs.collectAllConnected(graph, set(2)), is(set(2, 3, 4, 5)));
        assertThat(Graphs.collectAllConnected(graph, set(3)), is(set(2, 3, 4, 5)));
        assertThat(Graphs.collectAllConnected(graph, set(4)), is(set(2, 3, 4, 5)));
        assertThat(Graphs.collectAllConnected(graph, set(5)), is(set()));
    }

    /**
     * Test method for {@link Graphs#collectAllConnected(Graph, java.util.Collection)}.
     */
    @Test
    public void testCollectAllConnected_Multi() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        addPath(graph, 2, 4);
        addPath(graph, 5, 2);
        assertThat(Graphs.collectAllConnected(graph, set(1, 2)), is(set(2, 3, 4)));
        assertThat(Graphs.collectAllConnected(graph, set(1, 5)), is(set(2, 3, 4)));
        assertThat(Graphs.collectAllConnected(graph, set(3, 4)), is(set()));
    }

    /**
     * Test method for {@link Graphs#collectAllConnected(Graph, java.util.Collection)}.
     */
    @Test
    public void testCollectAllConnected_Single() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        addPath(graph, 2, 4);
        addPath(graph, 5, 2);
        assertThat(Graphs.collectAllConnected(graph, set(1)), is(set(2, 3, 4)));
        assertThat(Graphs.collectAllConnected(graph, set(2)), is(set(3, 4)));
        assertThat(Graphs.collectAllConnected(graph, set(3)), is(set()));
        assertThat(Graphs.collectAllConnected(graph, set(4)), is(set()));
        assertThat(Graphs.collectAllConnected(graph, set(5)), is(set(2, 4, 3)));
    }

    /**
     * Test method for {@link Graphs#findNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void findNearest_simple() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, -3);
        Set<Integer> results = Graphs.findNearest(graph, set(1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2)));
    }

    /**
     * Test method for {@link Graphs#findNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void findNearest_hop() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, -3);
        Set<Integer> results = Graphs.findNearest(graph, set(1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-3)));
    }

    /**
     * Test method for {@link Graphs#findNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void findNearest_ignoreItself() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, -1, 2, -3);
        Set<Integer> results = Graphs.findNearest(graph, set(-1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-3)));
    }

    /**
     * Test method for {@link Graphs#findNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void findNearest_dominants() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, 3, -4, -6);
        addPath(graph, 1, 5, 3);
        Set<Integer> results = Graphs.findNearest(graph, set(1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2, -4)));
    }

    /**
     * Test method for {@link Graphs#findNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void findNearest_multipath() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, -3);
        addPath(graph, 1, -2, -4);
        addPath(graph, 5, 6, -4);
        addPath(graph, 5, -7, -8);
        Set<Integer> results = Graphs.findNearest(graph, set(1, 5), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2, -4, -7)));
    }

    /**
     * Test method for {@link Graphs#findNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void findNearest_overlap() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, 3, -4, 1);
        Set<Integer> results = Graphs.findNearest(graph, set(1, -2), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2, -4)));
    }

    /**
     * Test method for {@link Graphs#collectNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void collectNearest_simple() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, -3);
        Set<Integer> results = Graphs.collectNearest(graph, set(1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2)));
    }

    /**
     * Test method for {@link Graphs#collectNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void collectNearest_hop() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, -3);
        Set<Integer> results = Graphs.collectNearest(graph, set(1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(2, -3)));
    }

    /**
     * Test method for {@link Graphs#collectNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void collectNearest_ignoreItself() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, -1, 2, -3);
        Set<Integer> results = Graphs.collectNearest(graph, set(-1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(2, -3)));
    }

    /**
     * Test method for {@link Graphs#collectNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void collectNearest_dominants() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, 3, -4, -6);
        addPath(graph, 1, 5, 3);
        Set<Integer> results = Graphs.collectNearest(graph, set(1), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2, 3, 5, -4)));
    }

    /**
     * Test method for {@link Graphs#collectNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void collectNearest_multipath() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, -3);
        addPath(graph, 1, -2, -4);
        addPath(graph, 5, 6, -4);
        addPath(graph, 5, -7, -8);
        Set<Integer> results = Graphs.collectNearest(graph, set(1, 5), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2, -4, 6, -7)));
    }

    /**
     * Test method for {@link Graphs#collectNearest(Graph, java.util.Collection, Matcher)}.
     */
    @Test
    public void collectNearest_overlap() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, -2, 3, -4, 1);
        Set<Integer> results = Graphs.collectNearest(graph, set(1, -2), new Matcher<Integer>() {
            @Override
            public boolean matches(Integer object) {
                return object < 0;
            }
        });
        assertThat(results, is(set(-2, 3, -4)));
    }

    /**
     * Test method for {@link Graphs#findCircuit(Graph)}.
     */
    @Test
    public void testFindCircuit_Circuit() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 1);

        Set<Set<Integer>> circuits = Graphs.findCircuit(graph);
        Integer[][] expect = { { 1, 2, 3 } };
        assertThat(circuits, is(toPartition(expect)));
    }

    /**
     * Test method for {@link Graphs#findCircuit(Graph)}.
     */
    @Test
    public void testFindCircuit_Complex() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 6);
        addPath(graph, 2, 5, 7);
        addPath(graph, 3, 4, 5, 3);

        Set<Set<Integer>> circuits = Graphs.findCircuit(graph);

        Integer[][] expect = { { 3, 4, 5 } };
        assertThat(circuits, is(toPartition(expect)));
    }

    /**
     * Test method for {@link Graphs#findCircuit(Graph)}.
     */
    @Test
    public void testFindCircuit_Self() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 2, 3);

        Set<Set<Integer>> circuits = Graphs.findCircuit(graph);

        Integer[][] expect = { { 2 } };
        assertThat(circuits, is(toPartition(expect)));
    }

    /**
     * Test method for {@link Graphs#findCircuit(Graph)}.
     */
    @Test
    public void testFindCircuit_Tree() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        addPath(graph, 2, 4);
        addPath(graph, 1, 5, 6);
        addPath(graph, 5, 7);

        Set<Set<Integer>> circuits = Graphs.findCircuit(graph);
        Integer[][] expect = {};
        assertThat(circuits, is(toPartition(expect)));
    }

    /**
     * Test method for {@link Graphs#findStronglyConnectedComponents(Graph)}.
     */
    @Test
    public void testFindStronglyConnectedComponents_Circuit() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 1);

        Set<Set<Integer>> scc = Graphs.findStronglyConnectedComponents(graph);

        Integer[][] expect = { { 1, 2, 3 } };
        assertThat(scc, is(toPartition(expect)));
    }

    /**
     * Test method for {@link Graphs#findStronglyConnectedComponents(Graph)}.
     */
    @Test
    public void testFindStronglyConnectedComponents_Complex() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 6);
        addPath(graph, 2, 5, 7);
        addPath(graph, 3, 4, 5, 3);

        Set<Set<Integer>> scc = Graphs.findStronglyConnectedComponents(graph);

        Integer[][] expect = { { 1 }, { 2 }, { 6 }, { 7 }, { 3, 4, 5 } };
        assertThat(scc, is(toPartition(expect)));
    }

    /**
     * Test method for {@link Graphs#findStronglyConnectedComponents(Graph)}.
     */
    @Test
    public void testFindStronglyConnectedComponents_Tree() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        addPath(graph, 2, 4);
        addPath(graph, 1, 5, 6);
        addPath(graph, 5, 7);

        Set<Set<Integer>> scc = Graphs.findStronglyConnectedComponents(graph);

        Integer[][] expect = { { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 }, { 7 } };
        assertThat(scc, is(toPartition(expect)));
    }

    /**
     * Test method for {@link Graphs#newInstance()}.
     */
    @Test
    public void testNewInstance() {
        Graph<String> graph = Graphs.newInstance();
        assertThat(graph.getNodeSet().isEmpty(), is(true));
    }

    /**
     * Test method for {@link Graphs#sortPostOrder(Graph)}.
     */
    @Test
    public void testSortPostOrder_Circuit() {
        Graph<Integer> graph = Graphs.newInstance();
        // {1} -> {2, 3, 4} -> {5}
        addPath(graph, 1, 2, 3, 4, 2, 5);
        List<Integer> sorted = Graphs.sortPostOrder(graph);
        assertThat(sorted.size(), is(5));
        assertThat(1, isIn(sorted));
        assertThat(2, isIn(sorted));
        assertThat(3, isIn(sorted));
        assertThat(4, isIn(sorted));
        assertThat(5, isIn(sorted));

        sorted.remove((Integer) 2);
        sorted.remove((Integer) 3);
        sorted.remove((Integer) 4);

        assertThat(sorted, is(Arrays.asList(5, 1)));
    }

    /**
     * Test method for {@link Graphs#sortPostOrder(Graph)}.
     */
    @Test
    public void testSortPostOrder_List() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 5);
        List<Integer> sorted = Graphs.sortPostOrder(graph);
        assertThat(sorted, is(Arrays.asList(5, 4, 3, 2, 1)));
    }

    /**
     * Test method for {@link Graphs#sortPostOrder(Graph)}.
     */
    @Test
    public void testSortPostOrder_Tree() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        addPath(graph, 1, 2, 4);
        addPath(graph, 1, 5, 6);
        addPath(graph, 1, 5, 7);
        List<Integer> sorted = Graphs.sortPostOrder(graph);
        assertThat(sorted.size(), is(7));
        assertThat(1, isIn(sorted));
        assertThat(2, isIn(sorted));
        assertThat(3, isIn(sorted));
        assertThat(4, isIn(sorted));
        assertThat(5, isIn(sorted));
        assertThat(6, isIn(sorted));
        assertThat(7, isIn(sorted));

        assertThat("the last is the root of this tree", sorted.get(6), is(1));
        assertPostOrdered(graph, sorted);
    }

    /**
     * Test method for {@link Graphs#transpose(Graph)}.
     */
    @Test
    public void testTransposeGraph_noEdges() {
        Graph<Integer> graph = Graphs.newInstance();
        prepare(graph, 1);

        Graph<Integer> expect = Graphs.newInstance();
        prepare(expect, 1);

        assertThat(Graphs.transpose(graph), is(expect));
    }

    /**
     * Test method for {@link Graphs#transpose(Graph)}.
     */
    @Test
    public void testTransposeGraph() {
        Graph<Integer> graph = Graphs.newInstance();
        prepare(graph, 1);
        prepare(graph, 2, 1);
        prepare(graph, 3, 2, 3, 4);
        prepare(graph, 4, 5, 6);
        prepare(graph, 5, 6, 7);

        Graph<Integer> expect = Graphs.newInstance();
        prepare(expect, 1, 2);
        prepare(expect, 2, 3);
        prepare(expect, 3, 3);
        prepare(expect, 4, 3);
        prepare(expect, 5, 4);
        prepare(expect, 6, 4, 5);
        prepare(expect, 7, 5);

        assertThat(Graphs.transpose(graph), is(expect));
    }

    /**
     * Test method for {@link Graphs#collectHeads(Graph)}.
     */
    @Test
    public void testCollectHeads_empty() {
        Graph<Integer> graph = Graphs.newInstance();
        assertThat(Graphs.collectHeads(graph), is(set()));
    }

    /**
     * Test method for {@link Graphs#collectHeads(Graph)}.
     */
    @Test
    public void testCollectHeads_single() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1);
        assertThat(Graphs.collectHeads(graph), is(set(1)));
    }

    /**
     * Test method for {@link Graphs#collectHeads(Graph)}.
     */
    @Test
    public void testCollectHeads_connected() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        assertThat(Graphs.collectHeads(graph), is(set(1)));
    }

    /**
     * Test method for {@link Graphs#collectHeads(Graph)}.
     */
    @Test
    public void testCollectHeads_disconnected() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1);
        addPath(graph, 2);
        addPath(graph, 3);
        addPath(graph, 4);
        assertThat(Graphs.collectHeads(graph), is(set(1, 2, 3, 4)));
    }

    /**
     * Test method for {@link Graphs#collectHeads(Graph)}.
     */
    @Test
    public void testCollectHeads_multi() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        addPath(graph, 4, 2, 5);
        assertThat(Graphs.collectHeads(graph), is(set(1, 4)));
    }

    /**
     * Test method for {@link Graphs#collectHeads(Graph)}.
     */
    @Test
    public void testCollectHeads_cyclic() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 1);
        assertThat(Graphs.collectHeads(graph), is(set()));
    }

    /**
     * Test method for {@link Graphs#collectTails(Graph)}.
     */
    @Test
    public void testCollectTails_empty() {
        Graph<Integer> graph = Graphs.newInstance();
        assertThat(Graphs.collectTails(graph), is(set()));
    }

    /**
     * Test method for {@link Graphs#collectTails(Graph)}.
     */
    @Test
    public void testCollectTails_single() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1);
        assertThat(Graphs.collectTails(graph), is(set(1)));
    }

    /**
     * Test method for {@link Graphs#collectTails(Graph)}.
     */
    @Test
    public void testCollectTails_connected() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        assertThat(Graphs.collectTails(graph), is(set(3)));
    }

    /**
     * Test method for {@link Graphs#collectTails(Graph)}.
     */
    @Test
    public void testCollectTails_disconnected() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1);
        addPath(graph, 2);
        addPath(graph, 3);
        addPath(graph, 4);
        assertThat(Graphs.collectTails(graph), is(set(1, 2, 3, 4)));
    }

    /**
     * Test method for {@link Graphs#collectTails(Graph)}.
     */
    @Test
    public void testCollectTails_multi() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3);
        addPath(graph, 4, 2, 5);
        assertThat(Graphs.collectTails(graph), is(set(3, 5)));
    }

    /**
     * Test method for {@link Graphs#collectTails(Graph)}.
     */
    @Test
    public void testCollectTails_cyclic() {
        Graph<Integer> graph = Graphs.newInstance();
        addPath(graph, 1, 2, 3, 4, 1);
        assertThat(Graphs.collectTails(graph), is(set()));
    }

    private <V> void addPath(Graph<V> graph, V first, V...vertexes) {
        graph.addNode(first);
        V current = first;
        for (V v : vertexes) {
            graph.addEdge(current, v);
            current = v;
        }
    }

    private void assertPostOrdered(Graph<?> graph, List<?> list) {
        for (int i = 0, n = list.size(); i < n; i++) {
            Object from = list.get(i);
            for (int j = i + 1; j < n; j++) {
                Object to = list.get(j);
                assertThat(from + "=>" + to, graph.isConnected(from, to), is(false));
            }
        }
    }

    private <T> void prepare(Graph<T> graph, T from, T...to) {
        graph.addNode(from);
        for (T t : to) {
            graph.addEdge(from, t);
        }
    }

    private Set<Integer> set(Integer...values) {
        return new HashSet<Integer>(Arrays.asList(values));
    }

    private <T> Set<Set<T>> toPartition(T[][] tss) {
        Set<Set<T>> results = new HashSet<Set<T>>();
        for (T[] ts : tss) {
            Set<T> part = new HashSet<T>();
            for (T t : ts) {
                part.add(t);
            }
            results.add(part);
        }
        return results;
    }
}
