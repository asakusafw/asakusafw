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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An implementation of {@link Graph} which uses {@link HashMap} in the internal storage.
 * @param <V> the vertex value type
 */
public class HashGraph<V> implements Graph<V> {

    private final HashMap<V, HashVertex<V>> entity = new HashMap<>();

    @Override
    public void addEdge(V from, V to) {
        HashVertex<V> vertex = prepare(from);
        prepare(to);
        vertex.to.add(to);
    }

    @Override
    public void addEdges(V from, Collection<? extends V> to) {
        if (to == null) {
            throw new IllegalArgumentException("to must not be null"); //$NON-NLS-1$
        }
        HashVertex<V> vertex = prepare(from);
        for (V v : to) {
            prepare(v);
            vertex.to.add(v);
        }
    }

    @Override
    public void addNode(V node) {
        prepare(node);
    }

    @Override
    public void clear() {
        entity.clear();
    }

    @Override
    public boolean contains(Object node) {
        return entity.containsKey(node);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HashGraph<?> other = (HashGraph<?>) obj;
        if ((this.entity.equals(other.entity)) == false) {
            return false;
        }
        return true;
    }

    @Override
    public Set<V> getConnected(Object key) {
        HashVertex<V> vertex = entity.get(key);
        if (vertex != null) {
            return vertex.to;
        }
        return Collections.emptySet();
    }

    @Override
    public Set<V> getNodeSet() {
        return entity.keySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.entity.hashCode();
        return result;
    }

    @Override
    public boolean isConnected(Object from, Object to) {
        HashVertex<V> vertex = entity.get(from);
        if (vertex == null) {
            return false;
        }
        return vertex.to.contains(to);
    }

    @Override
    public boolean isEmpty() {
        return entity.isEmpty();
    }

    @Override
    public Iterator<Graph.Vertex<V>> iterator() {
        return new IteratorWrapper<>(entity.values().iterator());
    }

    @Override
    public void removeEdge(Object from, Object to) {
        HashVertex<V> vertex = entity.get(from);
        if (vertex != null) {
            vertex.to.remove(to);
        }
    }

    @Override
    public void removeNode(Object node) {
        if (entity.remove(node) == null) {
            return;
        }
        for (HashVertex<V> vertex : entity.values()) {
            vertex.to.remove(node);
        }
    }

    @Override
    public void removeNodes(Collection<?> nodes) {
        if (nodes == null) {
            throw new IllegalArgumentException("nodes must not be null"); //$NON-NLS-1$
        }
        if (entity.keySet().removeAll(nodes) == false) {
            return;
        }
        for (HashVertex<V> vertex : entity.values()) {
            vertex.to.removeAll(nodes);
        }
    }

    @Override
    public String toString() {
        return entity.values().toString();
    }

    private HashVertex<V> prepare(V node) {
        HashVertex<V> vertex = entity.get(node);
        if (vertex == null) {
            vertex = new HashVertex<>(node);
            entity.put(node, vertex);
        }
        return vertex;
    }

    private static class HashVertex<V> implements Vertex<V> {

        final V from;

        final Set<V> to;

        HashVertex(V node) {
            this.from = node;
            this.to = new HashSet<>();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            HashVertex<?> other = (HashVertex<?>) obj;
            if (this.from == null) {
                if (other.from != null) {
                    return false;
                }
            } else if ((this.from.equals(other.from)) == false) {
                return false;
            }
            if ((this.to.equals(other.to)) == false) {
                return false;
            }
            return true;
        }

        @Override
        public Set<V> getConnected() {
            return to;
        }

        @Override
        public V getNode() {
            return from;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + to.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0} => {1}", from, to); //$NON-NLS-1$
        }
    }

    private static final class IteratorWrapper<V> implements Iterator<V> {

        private final Iterator<? extends V> iterator;

        IteratorWrapper(Iterator<? extends V> iterator) {
            assert iterator != null;
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public V next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
