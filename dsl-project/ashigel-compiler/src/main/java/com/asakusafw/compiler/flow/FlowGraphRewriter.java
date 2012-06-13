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
package com.asakusafw.compiler.flow;

import java.util.List;

import com.asakusafw.runtime.flow.FlowResource;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.ashigeru.lang.java.model.syntax.Name;

/**
 * {@link FlowGraph}を書き換えるエンジンのインターフェース。
 */
public interface FlowGraphRewriter extends FlowCompilingEnvironment.Initializable {

    /**
     * このエンジンを利用して指定のグラフを書き換える。
     * @param graph 書き換える対象のグラフオブジェクト
     * @return ひとつでも書き換えた可能性がある場合に{@code true}、そうでなければ{@code false}
     * @throws RewriteException 書き換えに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    boolean rewrite(FlowGraph graph) throws RewriteException;

    /**
     * このエンジンによって指定されたリソースを解決し、{@link FlowResource}の実装クラスとして返す。
     * @param resource 対象のリソース
     * @return コンパイル結果、対象としない場合は{@code null}
     * @throws RewriteException 解決に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    Name resolve(FlowResourceDescription resource) throws RewriteException;

    /**
     * 書き換えに失敗したことを表す例外。
     */
    class RewriteException extends Exception {

        private static final long serialVersionUID = 1L;

        /**
         * インスタンスを生成する。
         */
        public RewriteException() {
            super();
        }

        /**
         * インスタンスを生成する。
         * @param message メッセージ (省略可)
         * @param cause この例外の原因となった別の例外 (省略可)
         */
        public RewriteException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * インスタンスを生成する。
         * @param message メッセージ (省略可)
         */
        public RewriteException(String message) {
            super(message);
        }

        /**
         * インスタンスを生成する。
         * @param cause この例外の原因となった別の例外 (省略可)
         */
        public RewriteException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * {@link FlowGraphRewriter}を取得するためのリポジトリ。
     */
    interface Repository extends FlowCompilingEnvironment.Initializable {

        List<FlowGraphRewriter> getRewriters();
    }
}
