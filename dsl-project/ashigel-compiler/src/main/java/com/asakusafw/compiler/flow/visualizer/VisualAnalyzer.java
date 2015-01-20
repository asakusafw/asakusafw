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
 * @since 0.1.0
 * @version 0.4.0
 */
public final class VisualAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(VisualAnalyzer.class);

    private final Set<FlowElement> sawElements = Sets.create();

    private VisualAnalyzer() {
        return;
    }

    /**
     * 演算子グラフを可視化モデルに変換する。
     * @param graph 対象のグラフ
     * @return 変換後のモデル
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static VisualGraph convertFlowGraph(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("{}の構造を可視化用に分析しています", graph);
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = Sets.create();
        for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
            VisualNode node = analyzer.convertElement(element);
            if (node != null) {
                nodes.add(node);
            }
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
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = Sets.create();
        nodes.add(analyzer.convertBlock("(source)", graph.getInput()));
        for (StageBlock stage : graph.getStages()) {
            nodes.add(analyzer.convertStage(stage));
        }
        nodes.add(analyzer.convertBlock("(sink)", graph.getOutput()));
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
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = Sets.create();
        for (FlowBlock head : stage.getMapBlocks()) {
            for (FlowBlock.Input input : head.getBlockInputs()) {
                for (FlowBlock.Connection conn : input.getConnections()) {
                    FlowElement element = conn.getUpstream().getElementPort().getOwner();
                    VisualNode node = analyzer.convertElement(element);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
            }
        }
        nodes.add(analyzer.convertStage(stage));
        Set<FlowBlock> tails = stage.hasReduceBlocks() ? stage.getReduceBlocks() : stage.getMapBlocks();
        for (FlowBlock tail : tails) {
            for (FlowBlock.Output output : tail.getBlockOutputs()) {
                for (FlowBlock.Connection conn : output.getConnections()) {
                    FlowElement element = conn.getDownstream().getElementPort().getOwner();
                    VisualNode node = analyzer.convertElement(element);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
            }
        }
        return new VisualGraph(null, nodes);
    }

    /**
     * Converts {@link FlowBlock} into {@link VisualGraph}.
     * @param block target block
     * @return converted graph
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public static VisualGraph convertFlowBlock(FlowBlock block) {
        Precondition.checkMustNotBeNull(block, "block"); //$NON-NLS-1$
        LOG.debug("Visualizing a flow block: {}", block);
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = Sets.create();
        nodes.add(analyzer.convertBlock("block", block));
        return new VisualGraph(null, nodes);
    }

    private VisualGraph convertStage(StageBlock stage) {
        assert stage != null;
        Set<VisualBlock> nodes = Sets.create();
        for (FlowBlock block : stage.getMapBlocks()) {
            // "cluster"'s bug of dot
            nodes.add(convertBlock(null, block));
        }
        for (FlowBlock block : stage.getReduceBlocks()) {
            nodes.add(convertBlock(null, block));
        }
        return new VisualGraph(
                Naming.getStageName(stage.getStageNumber()),
                nodes);
    }

    private VisualBlock convertBlock(String label, FlowBlock block) {
        assert block != null;
        Set<VisualNode> nodes = Sets.create();
        for (FlowElement element : block.getElements()) {
            VisualNode node = convertElement(element);
            if (node != null) {
                nodes.add(node);
            }
        }
        return new VisualBlock(
                label,
                Sets.from(block.getBlockInputs()),
                Sets.from(block.getBlockOutputs()),
                nodes);
    }

    private VisualNode convertElement(FlowElement element) {
        assert element != null;
        if (sawElements.contains(element)) {
            LOG.debug("Ignored already presented element: {}", element);
            return null;
        }
        sawElements.add(element);
        if (element.getDescription().getKind() == FlowElementKind.FLOW_COMPONENT) {
            FlowPartDescription desc = (FlowPartDescription) element.getDescription();
            Set<VisualNode> nodes = Sets.create();
            for (FlowElement inner : FlowGraphUtil.collectElements(desc.getFlowGraph())) {
                VisualNode node = convertElement(inner);
                if (node != null) {
                    nodes.add(node);
                }
            }
            return new VisualFlowPart(element, nodes);
        }
        return new VisualElement(element);
    }
}
