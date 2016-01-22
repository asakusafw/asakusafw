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
package com.asakusafw.runtime.directio;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A repository of {@link DirectDataSource}.
 * @since 0.2.5
 */
public class DirectDataSourceRepository {

    static final Log LOG = LogFactory.getLog(DirectDataSourceRepository.class);

    private final Node root = new Node();

    /**
     * Creates a new instance.
     * @param providers provider objects
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectDataSourceRepository(Collection<? extends DirectDataSourceProvider> providers) {
        if (providers == null) {
            throw new IllegalArgumentException("providers must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Preparing directio datasources (count={0})", //$NON-NLS-1$
                    providers.size()));
        }
        for (DirectDataSourceProvider provider : providers) {
            NodePath path = NodePath.of(provider.getPath());
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Preparing datasource (id={0}, path={1})", //$NON-NLS-1$
                        provider.getId(),
                        path));
            }
            Node current = root;
            for (String segment : path) {
                current = current.addChild(segment);
            }
            if (current.hasContent()) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The path \"{0}\" is mapped multiple DirectDataSources ({1}, {2})",
                        path,
                        provider,
                        current.provider));
            }
            current.provider = provider;
        }
    }

    private Node findNode(NodePath path) throws IOException {
        assert path != null;
        Node lastContent = null;
        Node current = root;
        for (String segment : path) {
            if (current.hasContent()) {
                lastContent = current;
            }
            Node next = current.getChild(segment);
            if (next == null) {
                break;
            }
            current = next;
        }
        if (current.hasContent()) {
            lastContent = current;
        }
        if (lastContent == null) {
            throw new IOException(MessageFormat.format(
                    "There are no data sources for path: {0}",
                    path));
        }
        return lastContent;
    }

    /**
     * Returns an ID which related to the specified path.
     * @param path target path
     * @return the related ID
     * @throws IOException if failed to prepare the data source
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public String getRelatedId(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        NodePath nodePath = NodePath.of(path);
        Node node = findNode(nodePath);
        assert node != null;
        assert node.hasContent();
        return node.getId();
    }

    /**
     * Returns a data source which related to the specified path.
     * @param path target path
     * @return the related data source
     * @throws IOException if failed to prepare the data source
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectDataSource getRelatedDataSource(String path) throws IOException, InterruptedException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        NodePath nodePath = NodePath.of(path);
        Node node = findNode(nodePath);
        assert node != null;
        assert node.hasContent();
        return node.getInstance();
    }

    /**
     * Returns the container path of the target path.
     * @param path target path
     * @return corresponded container's path
     * @throws IOException if failed to detect the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public String getContainerPath(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        NodePath nodePath = NodePath.of(path);
        Node node = findNode(nodePath);
        assert node != null;
        int depth = node.getDepth();
        return nodePath.subPath(0, depth).getPathString();
    }

    /**
     * Returns the component path of the target path.
     * The component path is a relative path from the {@link #getContainerPath(String) container path}.
     * @param path target path
     * @return corresponded component path
     * @throws IOException if failed to detect the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public String getComponentPath(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        NodePath nodePath = NodePath.of(path);
        Node node = findNode(nodePath);
        assert node != null;
        int depth = node.getDepth();
        return nodePath.subPath(depth, nodePath.size()).getPathString();
    }

    /**
     * Returns all {@link DirectDataSource}s registered in this repository.
     * @return all {@link DirectDataSource}s
     * @throws IOException if failed to initialize data sources
     * @throws InterruptedException if interrupted
     */
    public Collection<String> getContainerPaths() throws IOException, InterruptedException {
        Collection<String> results = new ArrayList<>();
        LinkedList<Node> work = new LinkedList<>();
        work.add(root);
        while (work.isEmpty() == false) {
            Node node = work.removeFirst();
            if (node.hasContent()) {
                results.add(node.path.getPathString());
            }
            work.addAll(node.children.values());
        }
        return results;
    }

    private final class Node {

        final Node parent;

        final NodePath path;

        final Map<String, Node> children = new HashMap<>();

        volatile DirectDataSourceProvider provider;

        private DirectDataSource instance;

        Node() {
            this.parent = null;
            this.path = NodePath.ROOT;
        }

        Node(Node parent, String name) {
            assert parent != null;
            assert name != null;
            this.parent = parent;
            this.path = parent.path.append(name);
        }

        String getId() {
            if (provider == null) {
                throw new IllegalStateException();
            }
            return provider.getId();
        }

        synchronized DirectDataSource getInstance() throws IOException, InterruptedException {
            if (provider == null) {
                throw new IllegalStateException();
            }
            if (instance == null) {
                instance = provider.newInstance();
            }
            return instance;
        }

        Node addChild(String childName) {
            Node child = getChild(childName);
            if (child == null) {
                child = new Node(this, childName);
                children.put(childName, child);
            }
            return child;
        }

        Node getChild(String childName) {
            return children.get(childName);
        }

        int getDepth() {
            int depth = 0;
            for (Node current = parent; current != null; current = current.parent) {
                depth++;
            }
            return depth;
        }

        boolean hasContent() {
            return provider != null;
        }
    }

    private static final class NodePath implements Comparable<NodePath>, Iterable<String> {

        static final NodePath ROOT = new NodePath(new ArrayList<String>(0));

        private final ArrayList<String> segments;

        private NodePath(ArrayList<String> segments) {
            assert segments != null;
            this.segments = segments;
        }

        public NodePath append(String name) {
            assert name != null;
            ArrayList<String> newSegments = new ArrayList<>(segments.size() + 1);
            newSegments.addAll(segments);
            newSegments.add(name);
            return new NodePath(newSegments);
        }

        public static NodePath of(String pathString) {
            if (pathString == null) {
                throw new IllegalArgumentException("pathString must not be null"); //$NON-NLS-1$
            }
            if (pathString.isEmpty()) {
                return ROOT;
            }
            String[] fields = pathString.split("/"); //$NON-NLS-1$
            ArrayList<String> segments = new ArrayList<>();
            for (String s : fields) {
                if (s.isEmpty() == false) {
                    segments.add(s);
                }
            }
            return new NodePath(segments);
        }

        public NodePath subPath(int start, int end) {
            ArrayList<String> other = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                other.add(segments.get(i));
            }
            return new NodePath(other);
        }

        @Override
        public Iterator<String> iterator() {
            return segments.iterator();
        }

        public int size() {
            return segments.size();
        }

        public String getPathString() {
            if (segments.isEmpty()) {
                return ""; //$NON-NLS-1$
            }
            StringBuilder buf = new StringBuilder();
            ArrayList<String> copy = segments;
            buf.append(copy.get(0));
            for (int i = 1, n = copy.size(); i < n; i++) {
                buf.append('/');
                buf.append(copy.get(i));
            }
            return buf.toString();
        }

        @Deprecated
        @Override
        public String toString() {
            if (segments.isEmpty()) {
                return "(ROOT)"; //$NON-NLS-1$
            } else {
                return getPathString();
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + segments.hashCode();
            return result;
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
            NodePath other = (NodePath) obj;
            if (!segments.equals(other.segments)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(NodePath o) {
            ArrayList<String> a = segments;
            ArrayList<String> b = o.segments;
            for (int i = 0, n = Math.min(a.size(), b.size()); i < n; i++) {
                int cmp = a.get(i).compareTo(b.get(i));
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.compare(a.size(), b.size());
        }
    }
}
