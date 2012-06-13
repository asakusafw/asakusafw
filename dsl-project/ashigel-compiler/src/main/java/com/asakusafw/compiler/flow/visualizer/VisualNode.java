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
package com.asakusafw.compiler.flow.visualizer;

import java.util.UUID;

/**
 * 可視化ノード。
 */
public interface VisualNode {

    /**
     * このノードの種類を返す。
     * @return このノードの種類
     */
    Kind getKind();

    /**
     * このノードに割り当てられたUUIDを返す。
     * @return このノードに割り当てられたUUID
     */
    UUID getId();

    /**
     * 指定のビジタを受け入れる。
     * @param <R> 戻り値の型
     * @param <C> コンテキストオブジェクトの型
     * @param <E> ビジタで発生する例外の型
     * @param visitor 受け入れるビジタ
     * @param context コンテキストオブジェクト(省略可)
     * @return ビジタの実行結果
     * @throws E ビジタでの処理中に例外が発生した場合
     */
    <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E;

    /**
     * ノードの種類。
     */
    enum Kind {

        /**
         * グラフ。
         */
        GRAPH,

        /**
         * ブロック。
         */
        BLOCK,

        /**
         * フロー部品。
         */
        FLOW_PART,

        /**
         * フロー部品以外の要素。
         */
        ELEMENT,

        /**
         * ラベル。
         */
        LABEL,
    }
}
