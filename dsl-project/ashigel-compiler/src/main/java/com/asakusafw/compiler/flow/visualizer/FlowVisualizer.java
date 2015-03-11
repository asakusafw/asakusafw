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
package com.asakusafw.compiler.flow.visualizer;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * 各種要素を可視化のためのモデルに変換する。
 * @since 0.1.0
 * @version 0.4.0
 */
public class FlowVisualizer {

    private static final String PATH_FLOW_GRAPH = "META-INF/visualize/flowgraph.dot"; //$NON-NLS-1$

    private static final String PATH_STAGE_GRAPH = "META-INF/visualize/stagegraph.dot"; //$NON-NLS-1$

    private static final String PATH_STAGE_BLOCK = "META-INF/visualize/stageblock-{0}.dot"; //$NON-NLS-1$

    private final FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowVisualizer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * ステージグラフの構造を可視化して環境に書き出す。
     * @param graph 対象のグラフ
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void visualize(StageGraph graph) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        VisualGraph model = VisualAnalyzer.convertStageGraph(graph);
        emit(PATH_STAGE_GRAPH, false, model);
    }

    /**
     * ステージブロックの構造を可視化して環境に書き出す。
     * @param block 対象のブロック
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void visualize(StageBlock block) throws IOException {
        Precondition.checkMustNotBeNull(block, "block"); //$NON-NLS-1$
        VisualGraph model = VisualAnalyzer.convertStageBlock(block);
        emit(MessageFormat.format(PATH_STAGE_BLOCK, String.valueOf(block.getStageNumber())), true, model);
    }

    /**
     * 演算子グラフの構造を可視化して環境に書き出す。
     * @param graph 対象のグラフ
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void visualize(FlowGraph graph) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        VisualGraph model = VisualAnalyzer.convertFlowGraph(graph);
        emit(PATH_FLOW_GRAPH, false, model);
    }

    private void emit(String path, boolean partial, VisualGraph model) throws IOException {
        assert path != null;
        assert model != null;
        OutputStream output = environment.openResource(null, path);
        try {
            VisualGraphEmitter.emit(model, partial, output);
        } finally {
            output.close();
        }
    }
}
