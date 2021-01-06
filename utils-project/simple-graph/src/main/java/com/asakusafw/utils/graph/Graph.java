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
package com.asakusafw.utils.graph;

import java.util.Collection;
import java.util.Set;

/**
 * Represents a directed graph.
 * This identifies each vertex value by using its {@link Object#equals(Object)}.
 * @param <V> the vertex value type
 * @see Graphs
 */
public interface Graph<V> extends Iterable<Graph.Vertex<V>> {

    /**
     * Adds a directed edge from the {@code source} to {@code destination}.
     * This method ignores the edge if it is already in this graph.
     * This method also {@link #addNode(Object)} {@code source} and {@code destination} vertices to this graph
     * if they are not in this graph.
     * @param source the source vertex value (predecessor)
     * @param destination the destination vertex value (successor)
     */
    void addEdge(V source, V destination);

    /**
     * Adds directed edges from the {@code source} to each vertex in {@code destinations}.
     * This method ignores an edge if it is already in this graph.
     * This method also {@link #addNode(Object)} {@code source} and each vertex in {@code destinations} to this graph
     * if they are not in this graph.
     * If {@code destinations} is an empty collection, this method will only add the {@code source} vertex
     * to this graph, and not add any edges.
     * @param source the tail vertex value (predecessor)
     * @param destinations the head vertex values (successors)
     * @throws IllegalArgumentException if {@code to} is {@code null}
     */
    void addEdges(V source, Collection<? extends V> destinations);

    /**
     * Adds a vertex.
     * This method ignores the vertex if it is already in this graph.
     * @param node the vertex value
     */
    void addNode(V node);

    /**
     * Removes the all vertices and edges in this graph.
     */
    void clear();

    /**
     * Returns whether this graph contains the target vertex or not.
     * @param node the target vertex value
     * @return {@code true} if this graph contains the target vertex, otherwise {@code false}
     */
    boolean contains(Object node);

    /**
     * Returns the vertices which are direct successors of the specified vertex.
     * If the vertex is not in this graph, this method will return an empty set.
     * Clients should not modify the resulting set.
     * @param key the target vertex value
     * @return the successors of the specified vertex
     */
    Set<V> getConnected(Object key);

    /**
     * Returns the all vertices in this graph.
     * Clients should not modify the resulting set.
     * @return the all vertices in this graph
     */
    Set<V> getNodeSet();

    /**
     * Returns whether this graph contains the edge from {@code source} to {@code destination} or not.
     * @param source the source vertex value (predecessor)
     * @param destination the destination vertex value (successor)
     * @return {@code true} if this graph contains the edge from {@code source} to {@code destination},
     *     otherwise {@code false}
     */
    boolean isConnected(Object source, Object destination);

    /**
     * Returns whether this graph is empty or not.
     * @return {@code true} if this graph does not contains any vertices, otherwise {@code false}
     */
    boolean isEmpty();

    /**
     * Removes the edge from {@code source} to {@code destination} in this graph.
     * If this graph does not contain such the edge, this method will do nothing.
     * @param source the source vertex value (predecessor)
     * @param destination the destination vertex value (successor)
     */
    void removeEdge(Object source, Object destination);

    /**
     * Removes the vertex in this graph.
     * This method also removes edges which are connected to the target vertex.
     * If this graph does not contain such the vertex, this method will do nothing.
     * @param node the target vertex value
     */
    void removeNode(Object node);

    /**
     * Removes the vertices in this graph.
     * If this method also removes edges which are connected to vertex in the specified collection.
     * This method ignores vertices which are not in this graph.
     * @param nodes the target vertices
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void removeNodes(Collection<?> nodes);

    /**
     * Represents a vertex and its successor.
     * @param <V> the vertex value type
     */
    public interface Vertex<V> {

        /**
         * Returns the succeeding vertices from this vertex.
         * Clients should not modify the resulting set.
         * @return the successors
         */
        Set<V> getConnected();

        /**
         * Returns the vertex value.
         * @return the vertex value
         */
        V getNode();
    }
}
