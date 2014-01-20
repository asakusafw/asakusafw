/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;


import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * バッチ一つ分処理を表すワークフロー構造。
 */
public class Workflow {

    private final BatchDescription description;

    private final Graph<Unit> graph;

    /**
     * インスタンスを生成する。
     * @param description バッチの構造
     * @param graph 処理単位の依存元から依存先へのグラフ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Workflow(BatchDescription description, Graph<Unit> graph) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        this.description = description;
        this.graph = graph;
    }

    /**
     * このバッチの構造を返す。
     * @return このバッチの構造
     */
    public BatchDescription getDescription() {
        return description;
    }

    /**
     * 処理単位の依存元から依存先へのグラフを返す。
     * @return 処理単位の依存元から依存先へのグラフ
     */
    public Graph<Unit> getGraph() {
        return graph;
    }

    /**
     * ワークフロー中に含まれる処理の単位。
     */
    public static class Unit {

        private final WorkDescription description;

        private boolean isProcessed;

        private Object processed;

        /**
         * インスタンスを生成する。
         * @param description この処理を表す記述
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Unit(WorkDescription description) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            this.description = description;
        }

        /**
         * この処理を表す記述を返す。
         * @return この処理を表す記述
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public WorkDescription getDescription() {
            return description;
        }

        /**
         * この単位に対する処理済みのデータを返す。
         * @return この単位に対する処理済みのデータ、存在しない場合は{@code null}
         * @throws IllegalStateException 処理が行われていない場合
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Object getProcessed() {
            if (isProcessed == false) {
                throw new IllegalStateException();
            }
            return processed;
        }

        /**
         * この単位に対する処理済みのデータを設定する。
         * @param result 設定するデータ、存在しない場合は{@code null}
         * @throws IllegalStateException 処理が既に行われていた場合
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public void setProcessed(Object result) {
            if (isProcessed) {
                throw new IllegalStateException();
            }
            isProcessed = true;
            this.processed = result;
        }
    }
}
