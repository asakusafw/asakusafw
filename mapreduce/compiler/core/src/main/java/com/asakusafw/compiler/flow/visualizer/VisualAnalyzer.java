/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.util.HashSet;
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
 * Analyzes flow elements and convert them to visual models.
 * @since 0.1.0
 * @version 0.4.0
 */
public final class VisualAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(VisualAnalyzer.class);

    private final Set<FlowElement> sawElements = new HashSet<>();

    private VisualAnalyzer() {
        return;
    }

    /**
     * Converts a flow graph into a corresponded visual model.
     * @param graph the target graph
     * @return the corresponded visual model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static VisualGraph convertFlowGraph(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("analyzing flow graph for visualizing: {}", graph); //$NON-NLS-1$
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = new HashSet<>();
        for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
            VisualNode node = analyzer.convertElement(element);
            if (node != null) {
                nodes.add(node);
            }
        }
        return new VisualGraph(null, nodes);
    }

    /**
     * Converts a stage graph into a corresponded visual model.
     * @param graph the target graph
     * @return the corresponded visual model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static VisualGraph convertStageGraph(StageGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("analyzing stage graph for visualizing: {}", graph); //$NON-NLS-1$
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = new HashSet<>();
        nodes.add(analyzer.convertBlock("(source)", graph.getInput())); //$NON-NLS-1$
        for (StageBlock stage : graph.getStages()) {
            nodes.add(analyzer.convertStage(stage));
        }
        nodes.add(analyzer.convertBlock("(sink)", graph.getOutput())); //$NON-NLS-1$
        return new VisualGraph(null, nodes);
    }

    /**
     * Converts a stage block into a corresponded visual model.
     * @param stage the target block
     * @return the corresponded visual model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static VisualGraph convertStageBlock(StageBlock stage) {
        Precondition.checkMustNotBeNull(stage, "stage"); //$NON-NLS-1$
        LOG.debug("analyzing stage block for visualizing: {}", stage); //$NON-NLS-1$
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = new HashSet<>();
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
     * Converts a flow block into a corresponded visual model.
     * @param block the target block
     * @return the corresponded visual model
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @since 0.4.0
     */
    public static VisualGraph convertFlowBlock(FlowBlock block) {
        Precondition.checkMustNotBeNull(block, "block"); //$NON-NLS-1$
        LOG.debug("analyzing flow block for visualizing: {}", block); //$NON-NLS-1$
        VisualAnalyzer analyzer = new VisualAnalyzer();
        Set<VisualNode> nodes = new HashSet<>();
        nodes.add(analyzer.convertBlock("block", block)); //$NON-NLS-1$
        return new VisualGraph(null, nodes);
    }

    private VisualGraph convertStage(StageBlock stage) {
        assert stage != null;
        Set<VisualBlock> nodes = new HashSet<>();
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
        Set<VisualNode> nodes = new HashSet<>();
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
            LOG.debug("Ignored already presented element: {}", element); //$NON-NLS-1$
            return null;
        }
        sawElements.add(element);
        if (element.getDescription().getKind() == FlowElementKind.FLOW_COMPONENT) {
            FlowPartDescription desc = (FlowPartDescription) element.getDescription();
            Set<VisualNode> nodes = new HashSet<>();
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
