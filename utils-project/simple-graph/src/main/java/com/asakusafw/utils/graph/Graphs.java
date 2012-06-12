/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import com.asakusafw.utils.graph.Graph.Vertex;

/**
 * {@link Graph}に関する操作を行うライブラリ。
 */
public class Graphs {

    /**
     * 頂点を一つも持たない{@code Graph}のインスタンスを生成して返す。
     * @param <V> ノードを識別する値
     * @return 生成したインスタンス
     */
    public static <V> Graph<V> newInstance() {
        return new HashGraph<V>();
    }

    /**
     * 指定のグラフのコピーを返す。
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @return コピーしたグラフ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 指定のグラフに含まれるノードのうち、先行するノードが存在しないものの一覧を返す。
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @return 指定のグラフに含まれ、かつ先行するノードが存在しないものの一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Set<V> collectHeads(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        Set<V> results = new HashSet<V>(graph.getNodeSet());
        for (Vertex<? extends V> vertex : graph) {
            results.removeAll(vertex.getConnected());
        }
        return results;
    }

    /**
     * 指定のグラフに含まれるノードのうち、後続するノードが存在しないものの一覧を返す。
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @return 指定のグラフに含まれ、かつ後続するノードが存在しないものの一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Set<V> collectTails(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        Set<V> results = new HashSet<V>();
        for (Vertex<? extends V> vertex : graph) {
            if (vertex.getConnected().isEmpty()) {
                results.add(vertex.getNode());
            }
        }
        return results;
    }

    /**
     * 指定のノード一覧から直接または間接的に後続する全てのノードを返す。
     * <p>
     * 返される集合に含まれる値は、{@code startNodes}に含まれるそれぞれの値から
     * 直接接続されたノードに割り当てられた値か、
     * 再帰的にそれらのノードから接続された別のノードに割り当てられた値のみである。
     * つまり、{@code startNodes}自体の値は結果の集合に含まれない場合がある。
     * それらが結果に含まれる場合は、{@code startNodes}に含まれるいずれかの値が
     * 結果に含まれるノードに実際に接続されている場合のみである。
     * </p>
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @param startNodes 対象の開始ノードに割り当てられた値の一覧
     * @return 開始ノードから直接または間接的に接続されたすべてのノード
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Set<V> collectAllConnected(
            Graph<? extends V> graph,
            Collection<? extends V> startNodes) {
        if (graph == null) {
            throw new IllegalArgumentException("graph is null"); //$NON-NLS-1$
        }
        if (startNodes == null) {
            throw new IllegalArgumentException("startNodes is null"); //$NON-NLS-1$
        }
        Set<V> connected = new HashSet<V>();
        for (V start : startNodes) {
            findAllConnected(graph, start, connected);
        }
        return connected;
    }

    /**
     * 指定の開始ノードを起点に、後続するノードの中から条件に合致するノードの一覧を返す。
     * <p>
     * 返される集合は、次の条件を全て満たすような値{@code nearest}の集合である。
     * </p>
     * <ul>
     * <li>
     *   開始ノードのいずれかから、直接または間接的に接続されている
     *   ({@link #collectAllConnected(Graph, Collection)
     *   findAllConnected(graph, startNodes).contains(nearest)})
     * </li>
     * <li>
     *   受け入れ可能である
     *   {@link Matcher#matches(Object) acceptor.accept(nearest)}
     * </li>
     * <li>
     *   開始ノードのいずれかから、他の全ての{@code nearest}を経由せずに
     *   対象の{@code nearest}に到達可能である。
     * </li>
     * </ul>
     * <p>
     * なお、返される一覧には、{@code startNodes}で指定された要素が通常含まれない。
     * それらが結果に含まれる場合は、{@code startNodes}に含まれるいずれかの値が
     * {@code startNodes}の後続にあり、かつ上記の条件を満たしている。
     * </p>
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @param startNodes 対象の開始ノードに割り当てられた値の一覧
     * @param acceptor 利用する条件、合致するるものが条件に合致したとみなされる
     * @return 後続するノードのうち条件に合致するノードの一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Set<V> findNearest(
            Graph<? extends V> graph,
            Collection<? extends V> startNodes,
            Matcher<? super V> acceptor) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        if (startNodes == null) {
            throw new IllegalArgumentException("startNodes must not be null"); //$NON-NLS-1$
        }
        if (acceptor == null) {
            throw new IllegalArgumentException("acceptor must not be null"); //$NON-NLS-1$
        }
        LinkedList<V> queue = new LinkedList<V>();
        for (V start : startNodes) {
            queue.addAll(graph.getConnected(start));
        }

        Set<V> saw = new HashSet<V>();
        Set<V> results = new HashSet<V>();
        while (queue.isEmpty() == false) {
            V first = queue.removeFirst();

            // キャッシュを利用
            if (saw.contains(first)) {
                continue;
            }
            saw.add(first);

            // 該当するノードか検査
            boolean accepted = acceptor.matches(first);

            // 該当するノードならば結果に追加し、以降の探索を打ち切り
            if (accepted) {
                results.add(first);
            }
            // 該当するノードでなければ、続けて検索する
            else {
                queue.addAll(graph.getConnected(first));
            }
        }
        return results;
    }

    /**
     * 指定の開始ノードを起点に、後続するノードの中から条件に合致するノードの一覧と、
     * そこまでのノードの一覧を返す。
     * <p>
     * このメソッドは、{@link #findNearest(Graph, Collection, Matcher)}の
     * 探索経路上のすべてのノードを含めて返す。
     * </p>
     * <p>
     * なお、返される一覧には、{@code startNodes}で指定された要素が通常含まれない。
     * それらが結果に含まれる場合は、{@code startNodes}に含まれるいずれかの値が
     * {@code startNodes}の後続にあり、かつ上記の条件を満たしている。
     * </p>
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @param startNodes 対象の開始ノードに割り当てられた値の一覧
     * @param acceptor 利用する条件、合致するるものが条件に合致したとみなされる
     * @return 後続するノードのうち条件に合致するノードと、そこまでのノードの一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Set<V> collectNearest(
            Graph<? extends V> graph,
            Collection<? extends V> startNodes,
            Matcher<? super V> acceptor) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        if (startNodes == null) {
            throw new IllegalArgumentException("startNodes must not be null"); //$NON-NLS-1$
        }
        if (acceptor == null) {
            throw new IllegalArgumentException("acceptor must not be null"); //$NON-NLS-1$
        }
        LinkedList<V> queue = new LinkedList<V>();
        for (V start : startNodes) {
            queue.addAll(graph.getConnected(start));
        }

        Set<V> saw = new HashSet<V>();
        Set<V> results = new HashSet<V>();
        while (queue.isEmpty() == false) {
            V first = queue.removeFirst();

            // キャッシュを利用
            if (saw.contains(first)) {
                continue;
            }
            saw.add(first);

            // 該当するノードか検査
            boolean accepted = acceptor.matches(first);

            // 該当するノードならば結果に追加し、以降の探索を打ち切り
            if (accepted) {
                results.add(first);
            }
            // 該当するノードでなければ、続けて検索する
            else {
                results.add(first);
                queue.addAll(graph.getConnected(first));
            }
        }
        return results;
    }

    /**
     * 指定の有向グラフ内で循環する要素の集合を検出して返す。
     * @param <V> 頂点要素の型
     * @param graph 対象の有向グラフ
     * @return 循環依存する要素集合の一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Set<Set<V>> findCircuit(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph is null"); //$NON-NLS-1$
        }
        Set<Set<V>> results = new HashSet<Set<V>>();
        Set<Set<V>> sccs = Graphs.findStronglyConnectedComponents(graph);
        for (Set<V> scc : sccs) {
            // 強連結成分が2要素以上ならば、それらは循環
            if (scc.size() >= 2) {
                results.add(scc);
            }
            // 強連結成分が1要素ならば、自己参照がある場合のみ循環
            else if (scc.size() == 1) {
                V vertex = scc.iterator().next();
                if (graph.isConnected(vertex, vertex)) {
                    results.add(scc);
                }
            }
        }
        return results;
    }

    /**
     * 指定の有向グラフに含まれる強連結成分を列挙する。
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @return 強連結成分の一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Set<Set<V>> findStronglyConnectedComponents(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph is null"); //$NON-NLS-1$
        }
        List<? extends V> postorder = computePostOrderByDepth(graph);
        Graph<? extends V> tgraph = transpose(graph);

        List<Set<V>> results = new ArrayList<Set<V>>();
        Set<V> saw = new HashSet<V>();
        for (int i = postorder.size() - 1; i >= 0; --i) {
            V start = postorder.get(i);
            if (saw.contains(start)) {
                continue;
            }
            saw.add(start);
            Set<V> connected = new HashSet<V>();
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

        return new HashSet<Set<V>>(results);
    }

    /**
     * 指定の有向グラフに含まれるノードの一覧を、接続の末尾から順に列挙する。
     * <p>
     * 対象のグラフに循環が存在しない場合、返されるリスト{@code list}と
     * 引数のグラフ{@code graph}には必ず次のような関係が成り立つ。
     * </p>
     * <pre>{@code
     * for i = 0..list.size-1:
     *   for j = i+1..list.size-1
     *     assert graph.isConnected(list[i], list[j]) == false;
     * }</pre>
     * <p>
     * 対象のグラフに循環が存在する場合は、循環部分を取り除けば上記の関係は成り立つ。
     * </p>
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @return ノードの一覧を接続の末尾から順に並べたリスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> List<V> sortPostOrder(Graph<? extends V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph is null"); //$NON-NLS-1$
        }
        List<? extends V> postorder = computePostOrderByDepth(graph);
        return new ArrayList<V>(postorder);
    }

    /**
     * 指定のグラフに含まれるエッジを転置した新しいグラフを返す。
     * <p>
     * 返されるグラフ{@code reverse}は常に次が成り立つ。
     * </p>
     * <pre>{@code
     * graph.contains(a) <=> reverse.contains(a);
     * graph.isConnected(a, b) <=> reverse.isConnected(b, a)
     * }</pre>
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @return 生成したグラフ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Graph<V> transpose(Graph<V> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph is null"); //$NON-NLS-1$
        }
        Graph<V> results = new HashGraph<V>();
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
     * 指定のグラフのうち、指定した頂点のみを持つ部分グラフを新しく作成して返す。
     * @param <V> ノードを識別する値
     * @param graph 対象のグラフ
     * @param acceptor 部分グラフに含める頂点のみを許可するオブジェクト
     * @return 生成した部分グラフ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <V> Graph<V> subgraph(
            Graph<? extends V> graph,
            Matcher<? super V> acceptor) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null"); //$NON-NLS-1$
        }
        if (acceptor == null) {
            throw new IllegalArgumentException("acceptor must not be null"); //$NON-NLS-1$
        }
        Graph<V> subgraph = newInstance();
        Map<V, Boolean> accepted = new HashMap<V, Boolean>();
        for (V vertex : graph.getNodeSet()) {
            boolean matched = acceptor.matches(vertex);
            if (matched) {
                subgraph.addNode(vertex);
            }
            accepted.put(vertex, matched);
        }
        if (subgraph.isEmpty()) {
            return subgraph;
        }

        for (Graph.Vertex<? extends V> vertex : graph) {
            V from = vertex.getNode();
            assert accepted.containsKey(from);
            if (Boolean.FALSE.equals(accepted.get(from))) {
                continue;
            }
            for (V to : vertex.getConnected()) {
                assert accepted.containsKey(to);
                if (Boolean.FALSE.equals(accepted.get(to))) {
                    continue;
                }
                subgraph.addEdge(from, to);
            }
        }
        return subgraph;
    }

    private static <V> List<V> computePostOrderByDepth(Graph<? extends V> graph) {
        assert graph != null;
        List<V> results = new ArrayList<V>();

        Set<V> saw = new HashSet<V>();
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

    /**
     * インスタンス生成の禁止。
     */
    private Graphs() {
        throw new AssertionError();
    }

    /**
     * グラフを渡り歩く際の経路を記憶するフレーム。
     * @version $Date: 2009-08-19 19:09:34 +0900 (水, 19 8 2009) $
     * @param <V> フレームで取り扱うノードの型
     */
    private static final class VisitFrame<V> {

        private final Graph<? extends V> graph;

        /**
         * このフレームの手前のフレーム (ボトムの場合は{@code null})。
         */
        final VisitFrame<V> previous;

        /**
         * このフレームで取り扱うノードの値。
         */
        final V node;

        /**
         * このフレームで現在訪問しているneighborの一覧。
         */
        final Iterator<? extends V> branches;

        VisitFrame(
                VisitFrame<V> previous,
                Graph<? extends V> graph,
                V node,
                Iterator<? extends V> branch) {
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
            return new VisitFrame<V>(null, graph, node, branch);
        }

        VisitFrame<V> push(V nextNode) {
            Iterator<? extends V> nextBranch = graph.getConnected(nextNode).iterator();
            return new VisitFrame<V>(this, graph, nextNode, nextBranch);
        }
    }
}
