/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.asakusafw.utils.graph.Graph.Vertex;

/**
 * Operations for {@link Graph}.
 */
public final class Graphs {

    /**
     * Creates a new {@link Graph} instance without any vertices.
     * @param <V> the vertex value type
     * @return the created instance
     */
    public static <V> Graph<V> newInstance() {
        return new HashGraph<>();
    }

    /**
     * Returns a copy of the target graph.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @return the copy
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <V> Graph<V> copy(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        Graph<V> copy = newInstance();
        for (Graph.Vertex<? extends V> vertex : graph) {
            copy.addEdges(vertex.getNode(), vertex.getConnected());
        }
        return copy;
    }

    /**
     * Returns each head vertex in the graph, which has no preceding vertices.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @return the head vertices
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <V> Set<V> collectHeads(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        Set<V> results = new HashSet<>(graph.getNodeSet());
        for (Vertex<? extends V> vertex : graph) {
            results.removeAll(vertex.getConnected());
        }
        return results;
    }

    /**
     * Returns each tail vertex in the graph, which has no succeeding vertices.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @return the tail vertices
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <V> Set<V> collectTails(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        Set<V> results = new HashSet<>();
        for (Vertex<? extends V> vertex : graph) {
            if (vertex.getConnected().isEmpty()) {
                results.add(vertex.getNode());
            }
        }
        return results;
    }

    /**
     * Returns the transitively connected all vertices from the starting vertices.
     * If a starting vertex is not connected to any vertices, the resulting set will not contain it even if it is a
     * starting vertex.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @param startNodes the starting vertices
     * @return the transitive connected vertices from the starting vertices
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static <V> Set<V> collectAllConnected(Graph<? extends V> graph, Collection<? extends V> startNodes) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        if (startNodes == null) {
            throw new IllegalArgumentException("startNodes must not be null"); //$NON-NLS-1$
        }
        Set<V> connected = new HashSet<>();
        for (V start : startNodes) {
            findAllConnected(graph, start, connected);
        }
        return connected;
    }

    /**
     * Returns the succeeding nearest vertices which match the condition, from the starting vertices.
     * The nearest vertices must be a set of <em>nearest</em> which satisfies the following all conditions:
     * <ul>
     * <li>
     *   Is transitively connected from any starting vertices
     *   ({@link #collectAllConnected(Graph, Collection) collectAllConnected(graph, startNodes).contains(nearest)})
     * </li>
     * <li>
     *   Satisfies the {@code matcher} rule ({@link Predicate#test(Object) matcher.test(nearest)})
     * </li>
     * <li>
     *   Reachable from any starting vertices, NOT via other <em>nearest</em>
     * </li>
     * </ul>
     * <p>
     * The resulting set may not contain the starting vertices, excepts the starting vertices also satisfies above
     * the conditions.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @param startNodes the starting vertices
     * @param matcher the matcher
     * @return the nearest vertices
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static <V> Set<V> findNearest(
            Graph<? extends V> graph,
            Collection<? extends V> startNodes,
            Predicate<? super V> matcher) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        if (startNodes == null) {
            throw new IllegalArgumentException("startNodes must not be null"); //$NON-NLS-1$
        }
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be null"); //$NON-NLS-1$
        }
        LinkedList<V> queue = new LinkedList<>();
        for (V start : startNodes) {
            queue.addAll(graph.getConnected(start));
        }

        Set<V> saw = new HashSet<>();
        Set<V> results = new HashSet<>();
        while (queue.isEmpty() == false) {
            V first = queue.removeFirst();
            if (saw.contains(first)) {
                continue;
            }
            saw.add(first);
            boolean matched = matcher.test(first);
            if (matched) {
                results.add(first);
            } else {
                queue.addAll(graph.getConnected(first));
            }
        }
        return results;
    }

    /**
     * Returns the succeeding nearest vertices which match the condition, from the starting vertices,
     * and vertices on their routes. In other words, this method returns all vertices on the searching route on
     * {@link #findNearest(Graph, Collection, Predicate)}. The resulting set may not contain the starting vertices,
     * excepts the starting vertices also on their searching route.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @param startNodes the starting vertices
     * @param matcher the vertices predicate
     * @return the nearest vertices and vertices on their route
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static <V> Set<V> collectNearest(
            Graph<? extends V> graph,
            Collection<? extends V> startNodes,
            Predicate<? super V> matcher) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        if (startNodes == null) {
            throw new IllegalArgumentException("startNodes must not be null"); //$NON-NLS-1$
        }
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be null"); //$NON-NLS-1$
        }
        LinkedList<V> queue = new LinkedList<>();
        for (V start : startNodes) {
            queue.addAll(graph.getConnected(start));
        }

        Set<V> saw = new HashSet<>();
        Set<V> results = new HashSet<>();
        while (queue.isEmpty() == false) {
            V first = queue.removeFirst();
            if (saw.contains(first)) {
                continue;
            }
            saw.add(first);
            boolean matched = matcher.test(first);
            if (matched) {
                results.add(first);
            } else {
                results.add(first);
                queue.addAll(graph.getConnected(first));
            }
        }
        return results;
    }

    /**
     * Returns the all cyclic sub-graphs in the target graph.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @return the sets of cyclic connected vertices
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <V> Set<Set<V>> findCircuit(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        Set<Set<V>> results = new HashSet<>();
        Set<Set<V>> sccs = Graphs.findStronglyConnectedComponents(graph);
        for (Set<V> scc : sccs) {
            if (scc.size() >= 2) {
                // if SCC has > 2 vertices, it is cyclic
                results.add(scc);
            } else if (scc.size() == 1) {
                // if SCC has = 1 vertex, it is cyclic only if the vertex has self edge
                V vertex = scc.iterator().next();
                if (graph.isConnected(vertex, vertex)) {
                    results.add(scc);
                }
            }
        }
        return results;
    }

    /**
     * Returns the all strongly connected components in the target graph.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @return the strongly connected components
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <V> Set<Set<V>> findStronglyConnectedComponents(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        List<? extends V> postorder = computePostOrderByDepth(graph);
        Graph<? extends V> tgraph = transpose(graph);

        List<Set<V>> results = new ArrayList<>();
        Set<V> saw = new HashSet<>();
        for (int i = postorder.size() - 1; i >= 0; --i) {
            V start = postorder.get(i);
            if (saw.contains(start)) {
                continue;
            }
            saw.add(start);
            Set<V> connected = new HashSet<>();
            connected.add(start);
            VisitFrame<V> top = VisitFrame.build(tgraph, start);
            while (top != null) {
                while (top.branches.hasNext()) {
                    V node = top.branches.next();
                    if (saw.contains(node)) {
                        continue;
                    }
                    saw.add(node);
                    connected.add(node);
                    top = top.push(node);
                }
                top = top.previous;
            }
            results.add(connected);
        }

        return new HashSet<>(results);
    }

    /**
     * Sorts the vertices from the tail to head topologically, and returns their list.
     * If the {@code graph} does not contain any cycles, the resulting result must satisfy the following:
<pre>{@code
for i = 0..list.size-1:
  for j = i+1..list.size-1:
    assert graph.isConnected(list[i], list[j]) == false;
}</pre>
     * @param <V> the vertex value type
     * @param graph the target graph
     * @return the sorted vertices
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <V> List<V> sortPostOrder(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        List<? extends V> postorder = computePostOrderByDepth(graph);
        return new ArrayList<>(postorder);
    }

    /**
     * Creates a new transposed graph from the target graph.
     * The transposed graph <em>reverse</em> must satisfy the following:
<pre>{@code
graph.contains(a) <=> reverse.contains(a), and
graph.isConnected(a, b) <=> reverse.isConnected(b, a).
}</pre>
     * @param <V> the vertex value type
     * @param graph the target graph
     * @return the created graph
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static <V> Graph<V> transpose(Graph<V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        Graph<V> results = new HashGraph<>();
        for (Graph.Vertex<V> vertex : graph) {
            V from = vertex.getNode();
            results.addNode(from);
            for (V to : vertex.getConnected()) {
                results.addEdge(to, from);
            }
        }
        return results;
    }

    /**
     * Creates a new subgraph from the target graph.
     * @param <V> the vertex value type
     * @param graph the target graph
     * @param matcher only test to the subgraph members
     * @return the created subgraph
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static <V> Graph<V> subgraph(Graph<? extends V> graph, Predicate<? super V> matcher) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be null"); //$NON-NLS-1$
        }
        Graph<V> subgraph = newInstance();
        Map<V, Boolean> matchedSet = new HashMap<>();
        for (V vertex : graph.getNodeSet()) {
            boolean matched = matcher.test(vertex);
            if (matched) {
                subgraph.addNode(vertex);
            }
            matchedSet.put(vertex, matched);
        }
        if (subgraph.isEmpty()) {
            return subgraph;
        }

        for (Graph.Vertex<? extends V> vertex : graph) {
            V from = vertex.getNode();
            assert matchedSet.containsKey(from);
            if (Boolean.FALSE.equals(matchedSet.get(from))) {
                continue;
            }
            for (V to : vertex.getConnected()) {
                assert matchedSet.containsKey(to);
                if (Boolean.FALSE.equals(matchedSet.get(to))) {
                    continue;
                }
                subgraph.addEdge(from, to);
            }
        }
        return subgraph;
    }

    private static <V> List<V> computePostOrderByDepth(Graph<? extends V> graph) {
        assert graph != null;
        List<V> results = new ArrayList<>();

        Set<V> saw = new HashSet<>();
        for (Graph.Vertex<? extends V> start : graph) {
            if (saw.contains(start.getNode())) {
                continue;
            }
            saw.add(start.getNode());
            VisitFrame<V> top = VisitFrame.build(graph, start.getNode());
            while (top != null) {
                while (top.branches.hasNext()) {
                    V node = top.branches.next();
                    if (saw.contains(node)) {
                        continue;
                    }
                    saw.add(node);
                    top = top.push(node);
                }
                results.add(top.node);
                top = top.previous;
            }
        }
        return results;
    }

    private static <V> void findAllConnected(Graph<? extends V> graph, V start, Set<V> connected) {
        assert graph != null;
        assert connected != null;
        if (connected.contains(start)) {
            return;
        }
        if (graph.contains(start) == false) {
            return;
        }

        VisitFrame<V> top = VisitFrame.build(graph, start);
        while (top != null) {
            while (top.branches.hasNext()) {
                V node = top.branches.next();
                if (connected.contains(node)) {
                    continue;
                }
                connected.add(node);
                top = top.push(node);
            }
            top = top.previous;
        }
    }

    private Graphs() {
        return;
    }

    private static final class VisitFrame<V> {

        private final Graph<? extends V> graph;

        final VisitFrame<V> previous;

        final V node;

        final Iterator<? extends V> branches;

        VisitFrame(VisitFrame<V> previous, Graph<? extends V> graph, V node, Iterator<? extends V> branch) {
            assert graph != null;
            assert branch != null;
            this.graph = graph;
            this.previous = previous;
            this.node = node;
            this.branches = branch;
        }

        static <V> VisitFrame<V> build(Graph<? extends V> graph, V node) {
            assert graph != null;
            Iterator<? extends V> branch = graph.getConnected(node).iterator();
            return new VisitFrame<>(null, graph, node, branch);
        }

        VisitFrame<V> push(V nextNode) {
            Iterator<? extends V> nextBranch = graph.getConnected(nextNode).iterator();
            return new VisitFrame<>(this, graph, nextNode, nextBranch);
        }
    }
}
