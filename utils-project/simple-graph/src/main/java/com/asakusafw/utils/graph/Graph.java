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

import java.util.Collection;
import java.util.Set;

/**
 * 各ノードに任意の値を持つグラフ。
 * <p>
 * 各ノードの識別は、ノードに割り当てられた値の{@link Object#equals(Object)}を利用する。
 * </p>
 * @param <V> ノードを識別する値
 * @see Graphs
 */
public interface Graph<V> extends Iterable<Graph.Vertex<V>> {

    /**
     * このグラフに指定の値を持つノードと、そこに直接接続されたノードを追加する。
     * <p>
     * すでにこのグラフに、指定の値が割り当てられたノードと、そこから接続されたノードが存在する場合、
     * この呼び出しは何もしない。
     * </p>
     * <p>
     * {@code from, to}のいずれかがこのグラフ上に存在しない場合、
     * この呼び出しはまず{@link #addNode(Object)}によってそれぞれのノードを
     * このグラフに登録したのち、{@code from}の接続先として{@code to}を登録する。
     * </p>
     * @param from 接続元のノードに割り当てられる値
     * @param to 接続先のノードに割り当てられる値
     */
    void addEdge(V from, V to);

    /**
     * このグラフに指定の値を持つノードと、そこに直接接続されたノードの一覧を追加する。
     * <p>
     * すでにこのグラフに、指定の値が割り当てられたノードと、そこから接続されたノードがすべて存在する場合、
     * この呼び出しは何もしない。
     * </p>
     * <p>
     * {@code from, to}のいずれかがこのグラフ上に存在しない場合、
     * この呼び出しはまず{@link #addNode(Object)}によってそれぞれのノードを
     * このグラフに登録したのち、{@code from}の接続先として{@code to}のそれぞれの値を登録する。
     * </p>
     * <p>
     * {@code to}に空のコレクションが指定された場合、この呼び出しは{@code from}に指定された
     * ノードをグラフに登録したのち、接続を一つも追加しない。
     * </p>
     * @param from 接続元のノードに割り当てられる値
     * @param to 接続先のノードに割り当てられる値の一覧、
     *     空の場合は{@code from}に指定されたノードのみを登録して接続を追加しない
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void addEdges(V from, Collection<? extends V> to);

    /**
     * このグラフに指定の値を持つノードを追加する。
     * <p>
     * すでにこのグラフに指定の値が割り当てられたノードが存在する場合、
     * この呼び出しは何も行わない。
     * </p>
     * @param node 追加するノードに割り当てる値
     */
    void addNode(V node);

    /**
     * このグラフに含まれるノードや接続をすべて削除する。
     * <p>
     * この呼び出し後、 ノードが新しく追加されるまで{@link #isEmpty() this.isEmpty()}は
     * {@code true}を返すようになる。
     * </p>
     */
    void clear();

    /**
     * 指定の値を持つノードがこのグラフ上に存在する場合のみ{@code true}を返す。
     * @param node 対象のノード
     * @return 指定の値を持つノードがこのグラフ上に存在する場合に{@code true}、
     *     存在しない場合は{@code false}
     */
    boolean contains(Object node);

    /**
     * 指定の値を持つノードに接続されたノードの一覧を返す。
     * <p>
     * 存在しないノードを指定した場合、この呼び出しは空の集合を返す。
     * 返される集合を変更した場合の動作は保証されない。
     * </p>
     * @param key キー
     * @return 指定のノードに接続されたノードの一覧、
     *     指定のノードがグラフ上に存在しない場合は空の集合
     */
    Set<V> getConnected(Object key);

    /**
     * このグラフ上に存在する全てのノードに割り当てられた値の集合を返す。
     * <p>
     * 返される集合を変更した場合の動作は保証されない。
     * </p>
     * @return このグラフ上に存在する全てのノードに割り当てられた値の集合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    Set<V> getNodeSet();

    /**
     * {@code to}の値が割り当てられたノードが、{@code from}の値が割り当てられたノードに対して
     * 直接接続されている場合のみ{@code true}を返す。
     * @param from 接続元のノードに割り当てられた値
     * @param to 接続先のノードに割り当てられた値
     * @return 直接接続されている場合は{@code true}、そうでない場合は{@code false}
     */
    boolean isConnected(Object from, Object to);

    /**
     * このグラフに一つもノードが存在しない場合のみ{@code true}を返す。
     * @return このグラフに一つもノードが存在しない場合のみ{@code true}
     */
    boolean isEmpty();

    /**
     * このグラフから指定の値を持つノード同士の接続を削除する。
     * <p>
     * このグラフ上にいずれかのノードが存在しない場合、またはそれらの接続が存在しない場合は、
     * いずれもこの呼び出しは何も行わない。
     * また、この操作によってグラフ上のノードが変化することはない。
     * つまり、このメソッドの呼び出し前と後で{@link #getNodeSet()}が返す値は変化しない。
     * </p>
     * @param from 削除対象の接続元のノードに割り当てられた値
     * @param to 削除対象の接続先のノードに割り当てられた値
     */
    void removeEdge(Object from, Object to);

    /**
     * このグラフから指定の値を持つノードを削除する。
     * <p>
     * この操作によって、指定の値が割り当てられたノードと、そのノードに関する接続はすべて削除される。
     * すなわちこの操作が成功した場合、
     * {@code this.contains(node)}は{@code false}を返すようになり、
     * また任意の{@code x in this.getNodeSet()}に対し
     * {@code this.isConnected(node, x), this.isConnected(x, node)}は
     * いずれも{@code false}を返すようになる。
     * </p>
     * <p>
     * 指定の値が割り当てられたノードが存在しない場合、この呼び出しは何も行わない。
     * </p>
     * @param node 削除するノードに割り当てられた値
     */
    void removeNode(Object node);

    /**
     * このグラフから指定の値を持つノードの一覧を削除する。
     * <p>
     * この操作によって、指定のコレクションに含まれるいずれかの値が割り当てられたノードと、
     * そのノードに関する接続はすべて削除される。
     * すなわちこの操作が成功した場合、{@code nodes}に含まれる任意の値{@code node}に対し、
     * {@code this.contains(node)}は{@code false}を返すようになり、
     * また任意の{@code x in this.getNodeSet()}に対し
     * {@code this.isConnected(node, x), this.isConnected(x, node)}は
     * いずれも{@code false}を返すようになる。
     * </p>
     * <p>
     * 指定の値が割り当てられたノードが存在しない場合、この呼び出しは何も行わない。
     * </p>
     * @param nodes 削除対象のノードに割り当てられた値の一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void removeNodes(Collection<?> nodes);

    /**
     * 頂点とその隣接を表現するインターフェース。
     * @param <V> ノードを識別する値
     */
    public interface Vertex<V> {

        /**
         * この辺の接続先のノードに割り当てられた値の一覧を返す。
         * <p>
         * 返される集合を変更した場合の動作は保証されない。
         * </p>
         * @return この辺の接続先のノードに割り当てられた値の一覧
         */
        Set<V> getConnected();

        /**
         * この辺の接続元のノードに割り当てられた値を返す。
         * @return この辺の接続元のノードに割り当てられた値
         */
        V getNode();
    }
}
