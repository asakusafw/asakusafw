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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;

/**
 * 各種要素を可視化のためのモデルに変換する。
 */
public final class VisualAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(VisualAnalyzer.class);

    /**
     * 演算子グラフを可視化モデルに変換する。
     * @param graph 対象のグラフ
     * @return 変換後のモデル
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static VisualGraph convertFlowGraph(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("{}の構造を可視化用に分析しています", graph);
        Set<VisualNode> nodes = Sets.create();
        for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
            nodes.add(convertElement(element));
        }
        return new VisualGraph(null, nodes);
    }

    /**
     * ステージグラフを可視化モデルに変換する。
     * @param graph 対象のグラフ
     * @return 変換後のモデル
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static VisualGraph convertStageGraph(StageGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("{}の構造を可視化用に分析しています", graph);
        Set<VisualNode> nodes = Sets.create();
        nodes.add(convertBlock("(source)", graph.getInput()));
        for (StageBlock stage : graph.getStages()) {
            nodes.add(convertStage(stage));
        }
        nodes.add(convertBlock("(sink)", graph.getOutput()));
        return new VisualGraph(null, nodes);
    }

    /**
     * ステージグラフを可視化モデルに変換する。
     * @param stage 対象のブロック
     * @return 変換後のモデル
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static VisualGraph convertStageBlock(StageBlock stage) {
        Precondition.checkMustNotBeNull(stage, "stage"); //$NON-NLS-1$
        LOG.debug("{}の構造を可視化用に分析しています", stage);
        Set<VisualNode> nodes = Sets.create();
        for (FlowBlock head : stage.getMapBlocks()) {
            for (FlowBlock.Input input : head.getBlockInputs()) {
                for (FlowBlock.Connection conn : input.getConnections()) {
                    FlowElement element = conn.getUpstream().getElementPort().getOwner();
                    nodes.add(convertElement(element));
                }
            }
        }
        nodes.add(convertStage(stage));
        Set<FlowBlock> tails = stage.hasReduceBlocks() ? stage.getReduceBlocks() : stage.getMapBlocks();
        for (FlowBlock tail : tails) {
            for (FlowBlock.Output output : tail.getBlockOutputs()) {
                for (FlowBlock.Connection conn : output.getConnections()) {
                    FlowElement element = conn.getDownstream().getElementPort().getOwner();
                    nodes.add(convertElement(element));
                }
            }
        }
        return new VisualGraph(null, nodes);
    }

    private static VisualGraph convertStage(StageBlock stage) {
        assert stage != null;
        Set<VisualBlock> nodes = Sets.create();
        for (FlowBlock block : stage.getMapBlocks()) {
            // TODO "cluster"'s bug of dot
            nodes.add(convertBlock(null, block));
        }
        for (FlowBlock block : stage.getReduceBlocks()) {
            nodes.add(convertBlock(null, block));
        }
        return new VisualGraph(
                Naming.getStageName(stage.getStageNumber()),
                nodes);
    }

    private static VisualBlock convertBlock(String label, FlowBlock block) {
        assert block != null;
        Set<VisualNode> nodes = Sets.create();
        for (FlowElement element : block.getElements()) {
            nodes.add(convertElement(element));
        }
        return new VisualBlock(
                label,
                Sets.from(block.getBlockInputs()),
                Sets.from(block.getBlockOutputs()),
                nodes);
    }

    private static VisualNode convertElement(FlowElement element) {
        assert element != null;
        if (element.getDescription().getKind() == FlowElementKind.FLOW_COMPONENT) {
            FlowPartDescription desc = (FlowPartDescription) element.getDescription();
            Set<VisualNode> nodes = Sets.create();
            for (FlowElement inner : FlowGraphUtil.collectElements(desc.getFlowGraph())) {
                nodes.add(convertElement(inner));
            }
            return new VisualFlowPart(element, nodes);
        }
        return new VisualElement(element);
    }

    private VisualAnalyzer() {
        return;
    }
}
