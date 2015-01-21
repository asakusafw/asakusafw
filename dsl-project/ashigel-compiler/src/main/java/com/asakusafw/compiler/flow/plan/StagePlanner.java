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
package com.asakusafw.compiler.flow.plan;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.FlowGraphRewriter.RewriteException;
import com.asakusafw.compiler.flow.debugging.Debug;
import com.asakusafw.compiler.flow.join.operator.SideDataBranch;
import com.asakusafw.compiler.flow.join.operator.SideDataCheck;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.flow.graph.Connectivity;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.Inline;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.operator.Branch;
import com.asakusafw.vocabulary.operator.Logging;
import com.asakusafw.vocabulary.operator.Project;
import com.asakusafw.vocabulary.operator.Restructure;
import com.asakusafw.vocabulary.operator.Split;
import com.asakusafw.vocabulary.operator.Trace;

/**
 * 演算子グラフの実行計画を立案する。
 */
public class StagePlanner {

    static final String KEY_COMPRESS_FLOW_BLOCK_GROUP = "compressFlowBlockGroup"; //$NON-NLS-1$

    static final GenericOptionValue DEFAULT_COMPRESS_FLOW_BLOCK_GROUP = GenericOptionValue.ENABLED;

    static final Comparator<FlowGraphRewriter> REWRITER_COMPARATOR = new Comparator<FlowGraphRewriter>() {
        @Override
        public int compare(FlowGraphRewriter o1, FlowGraphRewriter o2) {
            int phaseDiff = o1.getPhase().compareTo(o2.getPhase());
            if (phaseDiff != 0) {
                return phaseDiff;
            }
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
    };

    static final Logger LOG = LoggerFactory.getLogger(StagePlanner.class);

    private final List<? extends FlowGraphRewriter> rewriters;

    private final FlowCompilerOptions options;

    private final List<StagePlanner.Diagnostic> diagnostics = Lists.create();

    private int blockSequence = 1;

    /**
     * インスタンスを生成する。
     * @param rewriters フローグラフを書き換えるオブジェクトの一覧
     * @param options コンパイラオプション
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StagePlanner(
            List<? extends FlowGraphRewriter> rewriters,
            FlowCompilerOptions options) {
        Precondition.checkMustNotBeNull(rewriters, "rewriters"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(options, "options"); //$NON-NLS-1$
        this.rewriters = sortRewriters(rewriters);
        this.options = options;
    }

    private List<? extends FlowGraphRewriter> sortRewriters(List<? extends FlowGraphRewriter> rw) {
        // for rewriting stability, we sort rewriters
        List<FlowGraphRewriter> results = new ArrayList<FlowGraphRewriter>(rw);
        Collections.sort(results, REWRITER_COMPARATOR);
        return results;
    }

    /**
     * 指定の演算子グラフを分析し、実行時のステージグラフに変換して返す。
     * @param graph 対象のグラフ
     * @return 変換結果、変換できない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @see #getDiagnostics()
     */
    public StageGraph plan(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        if (validate(graph) == false) {
            return null;
        }
        LOG.debug("creating logical plan: {}", graph); //$NON-NLS-1$
        LOG.debug("compressFlowPart: {}", options.isCompressFlowPart()); //$NON-NLS-1$
        LOG.debug("compressConcurrentStage: {}", options.isCompressConcurrentStage()); //$NON-NLS-1$

        FlowGraph copy = FlowGraphUtil.deepCopy(graph);

        if (rewrite(copy) == false) {
            return null;
        }

        normalizeFlowGraph(copy);

        StageGraph result = buildStageGraph(copy);

        return result;
    }

    private boolean rewrite(FlowGraph graph) {
        assert graph != null;
        LOG.debug("rewriting flow graph: {}", graph); //$NON-NLS-1$
        boolean modified = false;
        for (FlowGraphRewriter rewriter : rewriters) {
            try {
                modified |= rewriter.rewrite(graph);
            } catch (RewriteException e) {
                LOG.warn(MessageFormat.format(
                        "グラフの書き換えに失敗しました: {0} ({1})",
                        rewriter.getClass().getName(),
                        e.getMessage()), e);
                error(
                        graph,
                        Collections.<FlowElement>emptyList(),
                        "グラフの書き換えに失敗しました ({0})",
                        e.getMessage());
                return false;
            }
        }
        if (modified && validate(graph) == false) {
            return false;
        }
        return true;
    }

    private void unifyGlobalSideEffects(FlowGraph graph) {
        assert graph != null;
        LOG.debug("processing operators w/ global side-effects: {}", graph); //$NON-NLS-1$
        for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
            if (FlowGraphUtil.hasGlobalSideEffect(element)) {
                LOG.debug("inserting checkpoint before \"volatile\" operator: {}", element); //$NON-NLS-1$
                for (FlowElementOutput output : element.getOutputPorts()) {
                    FlowGraphUtil.insertCheckpoint(output);
                }
            }
        }
    }

    /**
     * このオブジェクトで実行された処理の診断情報を返す。
     * @return 処理の診断情報
     */
    public List<StagePlanner.Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * {@link #normalizeFlowGraph(FlowGraph)}が適用された前提で、
     * 演算子グラフからステージグラフを構築する。
     * @param graph 対象のグラフ
     * @return 構築したステージグラフ
     */
    StageGraph buildStageGraph(FlowGraph graph) {
        assert graph != null;
        LOG.debug("building stage graph: {}", graph); //$NON-NLS-1$
        FlowBlock input = buildInputBlock(graph);
        FlowBlock output = buildOutputBlock(graph);
        List<FlowBlock> computation = buildComputationBlocks(graph);
        connectFlowBlocks(input, output, computation);
        detachFlowBlocks(input, output, computation);
        trimFlowBlocks(computation);

        List<StageBlock> stageBlocks = buildStageBlocks(computation);
        compressStageBlocks(stageBlocks);
        sortStageBlocks(stageBlocks);

        return new StageGraph(input, output, stageBlocks);
    }

    private void compressStageBlocks(List<StageBlock> blocks) {
        assert blocks != null;
        boolean changed;
        LOG.debug("compressing stage blocks"); //$NON-NLS-1$
        do {
            changed = false;
            Iterator<StageBlock> iter = blocks.iterator();
            while (iter.hasNext()) {
                StageBlock block = iter.next();
                changed |= block.compaction();
                if (block.isEmpty()) {
                    LOG.debug("removing empty stage block: {}", block); //$NON-NLS-1$
                    iter.remove();
                    changed = true;
                }
            }
        } while (changed);
    }

    private void sortStageBlocks(List<StageBlock> stageBlocks) {
        assert stageBlocks != null;
        LOG.debug("sorting stages in stage graph"); //$NON-NLS-1$
        Map<FlowBlock, StageBlock> membership = Maps.create();
        for (StageBlock stage : stageBlocks) {
            for (FlowBlock flow : stage.getMapBlocks()) {
                membership.put(flow, stage);
            }
            for (FlowBlock flow : stage.getReduceBlocks()) {
                membership.put(flow, stage);
            }
        }

        Graph<StageBlock> graph = Graphs.newInstance();
        for (Map.Entry<FlowBlock, StageBlock> entry : membership.entrySet()) {
            FlowBlock flow = entry.getKey();
            StageBlock stage = entry.getValue();
            graph.addNode(stage);
            for (FlowBlock.Output output : flow.getBlockOutputs()) {
                for (FlowBlock.Connection conn : output.getConnections()) {
                    FlowBlock succFlow = conn.getDownstream().getOwner();
                    StageBlock succ = membership.get(succFlow);
                    if (succ == null || succ == stage) {
                        continue;
                    }
                    graph.addEdge(succ, stage);
                }
            }
        }

        List<StageBlock> ordered = Graphs.sortPostOrder(graph);
        int stageNumber = 1;
        for (StageBlock stage : ordered) {
            stage.setStageNumber(stageNumber);
            stageNumber++;
        }

        Collections.sort(stageBlocks, new Comparator<StageBlock>() {
            @Override
            public int compare(StageBlock o1, StageBlock o2) {
                int n1 = o1.getStageNumber();
                int n2 = o2.getStageNumber();
                if (n1 == n2) {
                    return 0;
                } else if (n1 < n2) {
                    return -1;
                } else {
                    return +1;
                }
            }
        });
    }

    private List<StageBlock> buildStageBlocks(List<FlowBlock> blocks) {
        assert blocks != null;
        LOG.debug("building stage blocks: {}", blocks); //$NON-NLS-1$

        List<StageBlock> results = Lists.create();
        List<FlowBlockGroup> flowBlockGroups = collectFlowBlockGroups(blocks);
        compressFlowBlockGroups(flowBlockGroups);
        for (FlowBlockGroup group : flowBlockGroups) {
            if (group.reducer) {
                Set<FlowBlock> predecessors = getPredecessors(group.members);
                assert predecessors.isEmpty() == false;
                StageBlock stage = new StageBlock(predecessors, group.members);
                results.add(stage);
                LOG.debug("stage {}: map={}, reduce={}", new Object[] { //$NON-NLS-1$
                        stage,
                        predecessors,
                        group.members,
                });
            } else {
                StageBlock stage = new StageBlock(group.members, Collections.<FlowBlock>emptySet());
                results.add(stage);
                LOG.debug("stage {}: map={}, reduce=N/A", stage, group.members); //$NON-NLS-1$
            }
        }
        return results;
    }

    private void compressFlowBlockGroups(List<FlowBlockGroup> flowBlockGroups) {
        assert flowBlockGroups != null;
        GenericOptionValue active = options.getGenericExtraAttribute(
                KEY_COMPRESS_FLOW_BLOCK_GROUP,
                DEFAULT_COMPRESS_FLOW_BLOCK_GROUP);
        if (active == GenericOptionValue.DISABLED) {
            return;
        }
        LOG.debug("Compressing flow blocks"); //$NON-NLS-1$

        // merge blocks
        List<FlowBlock> blocks = Lists.create();
        Map<FlowBlock.Input, Set<FlowBlock.Input>> inputMapping = Maps.create();
        Map<FlowBlock.Output, Set<FlowBlock.Output>> outputMapping = Maps.create();
        for (FlowBlockGroup group : flowBlockGroups) {
            if (group.reducer) {
                Set<FlowBlock> predecessors = getPredecessors(group.members);
                if (predecessors.size() >= 2) {
                    LOG.debug("Compressing flow blocks: {}", predecessors); //$NON-NLS-1$
                    FlowBlock mergedPreds = FlowBlock.fromBlocks(predecessors, inputMapping, outputMapping);
                    group.predeceaseBlocks.clear();
                    group.predeceaseBlocks.add(mergedPreds);
                    blocks.add(mergedPreds);
                }
            }
            if (group.members.size() >= 2) {
                LOG.debug("Compressing flow blocks: {}", group.members); //$NON-NLS-1$
                FlowBlock mergedBlocks = FlowBlock.fromBlocks(group.members, inputMapping, outputMapping);
                group.members.clear();
                group.members.add(mergedBlocks);
                blocks.add(mergedBlocks);
            }
        }

        // reconnect
        for (Map.Entry<FlowBlock.Input, Set<FlowBlock.Input>> entry : inputMapping.entrySet()) {
            FlowBlock.Input origin = entry.getKey();
            for (FlowBlock.Connection conn : Lists.from(origin.getConnections())) {
                FlowBlock.Output opposite = conn.getUpstream();
                Collection<FlowBlock.Output> resolvedOpposites;
                if (outputMapping.containsKey(opposite)) {
                    resolvedOpposites = outputMapping.get(opposite);
                } else {
                    resolvedOpposites = Collections.singleton(opposite);
                }
                conn.disconnect();
                for (FlowBlock.Input mapped : entry.getValue()) {
                    for (FlowBlock.Output resolved : resolvedOpposites) {
                        FlowBlock.connect(resolved, mapped);
                    }
                }
            }
        }
        for (Map.Entry<FlowBlock.Output, Set<FlowBlock.Output>> entry : outputMapping.entrySet()) {
            FlowBlock.Output origin = entry.getKey();
            for (FlowBlock.Connection conn : Lists.from(origin.getConnections())) {
                FlowBlock.Input opposite = conn.getDownstream();
                Collection<FlowBlock.Input> resolvedOpposites;
                if (inputMapping.containsKey(opposite)) {
                    resolvedOpposites = inputMapping.get(opposite);
                } else {
                    resolvedOpposites = Collections.singleton(opposite);
                }
                conn.disconnect();
                for (FlowBlock.Output mapped : entry.getValue()) {
                    for (FlowBlock.Input resolved : resolvedOpposites) {
                        FlowBlock.connect(mapped, resolved);
                    }
                }
            }
        }

        // optimize
        detachFlowBlocks(blocks);
        unifyFlowBlocks(blocks);
        trimFlowBlocks(blocks);
    }

    private List<FlowBlockGroup> collectFlowBlockGroups(List<FlowBlock> blocks) {
        assert blocks != null;
        LOG.debug("collecting concurrent stages"); //$NON-NLS-1$

        LinkedList<FlowBlockGroup> groups = new LinkedList<FlowBlockGroup>();
        for (FlowBlock block : blocks) {
            // Reducerが後続するMapperは対象としない
            if (block.isReduceBlock() == false && block.isSucceedingReduceBlock()) {
                continue;
            }
            groups.add(new FlowBlockGroup(block));
        }

        if (options.isCompressConcurrentStage() == false) {
            LOG.debug("compressing concurrent stages is disabled"); //$NON-NLS-1$
            return Lists.from(groups);
        }
        LOG.debug("compressing concurrent stages"); //$NON-NLS-1$

        // クリティカルパス距離の計算
        computeCriticalPaths(groups);

        // 合成可能なものをまとめる
        List<FlowBlockGroup> results = Lists.create();
        while (groups.isEmpty() == false) {
            FlowBlockGroup first = groups.removeFirst();
            Iterator<FlowBlockGroup> rest = groups.iterator();
            while (rest.hasNext()) {
                FlowBlockGroup next = rest.next();
                if (first.combine(next)) {
                    LOG.debug("merging flow block: {}, {}", first.founder, next.founder); //$NON-NLS-1$
                    rest.remove();
                }
            }
            results.add(first);
        }
        return results;
    }

    private void computeCriticalPaths(List<FlowBlockGroup> groups) {
        assert groups != null;
        Map<FlowBlock, FlowBlockGroup> mapping = Maps.create();
        LinkedList<FlowBlockGroup> work = new LinkedList<FlowBlockGroup>();
        for (FlowBlockGroup group : groups) {
            work.add(group);
            mapping.put(group.founder, group);
        }

        PROPAGATION: while (work.isEmpty() == false) {
            int maxDistance = 0;
            FlowBlockGroup first = work.removeFirst();
            for (FlowBlock predecessor : first.predeceaseBlocks) {
                FlowBlockGroup predGroup = mapping.get(predecessor);
                if (predGroup.distance == -1) {
                    work.addLast(first);
                    continue PROPAGATION;
                } else {
                    maxDistance = Math.max(maxDistance, predGroup.distance);
                }
            }
            first.distance = maxDistance + 1;
        }
    }

    private Set<FlowBlock> getPredecessors(Set<FlowBlock> blocks) {
        assert blocks != null;
        Set<FlowBlock> results = Sets.create();
        for (FlowBlock block : blocks) {
            for (FlowBlock.Input port : block.getBlockInputs()) {
                for (FlowBlock.Connection conn : port.getConnections()) {
                    FlowBlock pred = conn.getUpstream().getOwner();
                    results.add(pred);
                }
            }
        }
        return results;
    }

    /**
     * 指定のグラフの入力ブロックを作成する。
     * @param graph 対象のグラフ
     * @return 演算子グラフへの入力のみを含むブロック
     */
    private FlowBlock buildInputBlock(FlowGraph graph) {
        assert graph != null;
        List<FlowElementOutput> outputs = Lists.create();
        Set<FlowElement> elements = Sets.create();
        for (FlowIn<?> node : graph.getFlowInputs()) {
            outputs.add(node.toOutputPort());
            elements.add(node.getFlowElement());
        }
        return FlowBlock.fromPorts(
                nextBlockSequenceNumber(),
                graph,
                Collections.<FlowElementInput>emptyList(),
                outputs,
                elements);
    }

    /**
     * 指定のグラフの出力ブロックを作成する。
     * @param graph 対象のグラフ
     * @return 演算子グラフからの出力のみを含むブロック
     */
    private FlowBlock buildOutputBlock(FlowGraph graph) {
        assert graph != null;
        List<FlowElementInput> inputs = Lists.create();
        Set<FlowElement> elements = Sets.create();
        for (FlowOut<?> node : graph.getFlowOutputs()) {
            inputs.add(node.toInputPort());
            elements.add(node.getFlowElement());
        }
        return FlowBlock.fromPorts(
                nextBlockSequenceNumber(),
                graph,
                inputs,
                Collections.<FlowElementOutput>emptyList(),
                elements);
    }

    /**
     * {@link #normalizeFlowGraph(FlowGraph)}が行われていることを前提に、
     * グラフから入出力を除くすべてのブロックを構築する。
     * @param graph 対象のグラフ
     * @return 構築したブロック
     */
    private List<FlowBlock> buildComputationBlocks(FlowGraph graph) {
        assert graph != null;
        LOG.debug("computing flow blocks: {}", graph); //$NON-NLS-1$

        // shuffle bound -> next stage bounds
        Collection<FlowPath> shuffleSuccessors = Sets.create();

        // shuffle bound <- previous stage bounds
        Collection<FlowPath> shufflePredecessors = Sets.create();

        // stage bound -> any next (shuffle/stage) bounds
        Map<FlowElement, FlowPath> stageSuccessors = Maps.create();

        // stage bound <- any previous (shuffle/stage) bounds
        Map<FlowElement, FlowPath> stagePredecessors = Maps.create();

        for (FlowElement boundary : FlowGraphUtil.collectBoundaries(graph)) {
            boolean shuffle = FlowGraphUtil.isShuffleBoundary(boundary);
            boolean success = FlowGraphUtil.hasSuccessors(boundary);
            boolean predecease = FlowGraphUtil.hasPredecessors(boundary);
            if (shuffle) {
                assert success;
                assert predecease;
                shuffleSuccessors.add(FlowGraphUtil.getSucceedBoundaryPath(boundary));
                shufflePredecessors.add(FlowGraphUtil.getPredeceaseBoundaryPath(boundary));
            } else {
                if (success) {
                    stageSuccessors.put(boundary, FlowGraphUtil.getSucceedBoundaryPath(boundary));
                }
                if (predecease) {
                    stagePredecessors.put(boundary, FlowGraphUtil.getPredeceaseBoundaryPath(boundary));
                }
            }
        }

        List<FlowBlock> results = Lists.create();

        results.addAll(collectShuffleToStage(graph, shuffleSuccessors));
        results.addAll(collectStageToShuffle(graph, shufflePredecessors, stageSuccessors));
        results.addAll(collectStageToStage(graph, stageSuccessors, stagePredecessors));
        return results;
    }

    private List<FlowBlock> collectStageToStage(
            FlowGraph graph,
            Map<FlowElement, FlowPath> stageSuccessors,
            Map<FlowElement, FlowPath> stagePredecessors) {
        assert graph != null;
        assert stageSuccessors != null;
        assert stagePredecessors != null;
        LOG.debug("computing map blocks (w/o succeeding reducers): {}", graph); //$NON-NLS-1$

        // (ステージ境界, ステージ境界) は入力ごとにマップブロックにする
        List<FlowBlock> results = Lists.create();
        Collection<FlowPath> ss = stageSuccessors.values();
        for (FlowPath stageForward : ss) {

            // このステージ境界に後続するステージ境界から、逆順のパスを抽出する
            List<FlowPath> stageBackwards = Lists.create();
            for (FlowElement arrival : stageForward.getArrivals()) {
                if (FlowGraphUtil.isShuffleBoundary(arrival) == false) {
                    FlowPath stageBackward = stagePredecessors.get(arrival);
                    assert stageBackward != null;
                    stageBackwards.add(stageBackward);
                }
            }

            // 逆順のパスが空ならば、全てシャッフル境界が後続していたことになる
            if (stageBackwards.isEmpty()) {
                continue;
            }

            // 後続するステージ境界から逆順のパスの和となるパスを求める
            FlowPath backward = FlowGraphUtil.union(stageBackwards);

            // ステージ境界からステージ境界までの最小のパスを作成する
            FlowPath path = stageForward.transposeIntersect(backward);
            FlowBlock block = path.createBlock(
                    graph,
                    nextBlockSequenceNumber(),
                    false,
                    false);
            results.add(block);
            LOG.debug("add map block (stage -> stage): {} -> {}", //$NON-NLS-1$
                    block.getBlockInputs(),
                    block.getBlockOutputs());
        }
        return results;
    }

    private List<FlowBlock> collectStageToShuffle(
            FlowGraph graph,
            Collection<FlowPath> shufflePredecessors,
            Map<FlowElement, FlowPath> stageSuccessors) {
        assert graph != null;
        assert shufflePredecessors != null;
        assert stageSuccessors != null;
        LOG.debug("computing map blocks (w/ succeeding reducers): {}", graph); //$NON-NLS-1$

        // (ステージ境界, シャッフル境界) は(入力, シャッフル)ごとにマップブロックにする
        List<FlowBlock> results = Lists.create();
        for (FlowPath shuffleBackward : shufflePredecessors) {
            Set<FlowElement> arrivals = shuffleBackward.getArrivals();
            for (FlowElement stageStart : arrivals) {
                assert FlowGraphUtil.isShuffleBoundary(stageStart) == false;

                FlowPath stageForward = stageSuccessors.get(stageStart);
                assert stageForward != null;

                // 単一のステージから始まり、かつシャッフルから先行するパスのみ
                FlowPath path = stageForward.transposeIntersect(shuffleBackward);
                FlowBlock block = path.createBlock(
                        graph,
                        nextBlockSequenceNumber(),
                        false,
                        false);
                results.add(block);
                LOG.debug("add map block (stage -> shuffle): {} -> {}", //$NON-NLS-1$
                        block.getBlockInputs(),
                        block.getBlockOutputs());
            }
        }
        return results;
    }

    private List<FlowBlock> collectShuffleToStage(
            FlowGraph graph,
            Collection<FlowPath> shuffleSuccessors) {
        assert graph != null;
        assert shuffleSuccessors != null;
        LOG.debug("computing reduce blocks (w/ succeeding reducers): {}", graph); //$NON-NLS-1$

        // [シャッフル境界, ステージ境界) は必ず単一のレデュースブロックになる
        List<FlowBlock> results = Lists.create();
        for (FlowPath path : shuffleSuccessors) {
            FlowBlock block = path.createBlock(
                    graph,
                    nextBlockSequenceNumber(),
                    true,
                    false);
            results.add(block);
            LOG.debug("add reduce block (shuffle -> stage): {} -> {}", //$NON-NLS-1$
                    block.getBlockInputs(),
                    block.getBlockOutputs());
        }
        return results;
    }

    private int nextBlockSequenceNumber() {
        return blockSequence++;
    }

    private void connectFlowBlocks(
            FlowBlock inputBlock,
            FlowBlock outputBlock,
            List<FlowBlock> computationBlocks) {
        assert inputBlock != null;
        assert outputBlock != null;
        assert computationBlocks != null;
        LOG.debug("connecting flow blocks"); //$NON-NLS-1$

        List<FlowBlock> blocks = Lists.create();
        blocks.add(inputBlock);
        blocks.add(outputBlock);
        blocks.addAll(computationBlocks);

        Map<PortConnection, Set<FlowBlock.Input>> mapping = Maps.create();
        for (FlowBlock block : blocks) {
            for (FlowBlock.Input input : block.getBlockInputs()) {
                for (PortConnection conn : input.getOriginalConnections()) {
                    Maps.addToSet(mapping, conn, input);
                }
            }
        }

        for (FlowBlock block : blocks) {
            for (FlowBlock.Output output : block.getBlockOutputs()) {
                for (PortConnection conn : output.getOriginalConnections()) {
                    Set<PortConnection> next = FlowGraphUtil.getSucceedingConnections(conn, mapping.keySet());
                    for (PortConnection successor : next) {
                        Set<FlowBlock.Input> connected = mapping.get(successor);
                        for (FlowBlock.Input opposite : connected) {
                            FlowBlock.connect(output, opposite);
                        }
                    }
                }
            }
        }
    }

    private void detachFlowBlocks(FlowBlock input, FlowBlock output, List<FlowBlock> computation) {
        assert input != null;
        assert output != null;
        assert computation != null;
        input.detach();
        output.detach();
        detachFlowBlocks(computation);
    }

    private void detachFlowBlocks(List<FlowBlock> blocks) {
        assert blocks != null;
        for (FlowBlock block : blocks) {
            block.detach();
        }
    }

    private void unifyFlowBlocks(List<FlowBlock> blocks) {
        assert blocks != null;
        for (FlowBlock block : blocks) {
            block.unify();
        }
    }

    private void trimFlowBlocks(List<FlowBlock> blocks) {
        assert blocks != null;
        boolean changed;
        LOG.debug("removing dead operators"); //$NON-NLS-1$
        do {
            changed = false;
            Iterator<FlowBlock> iter = blocks.iterator();
            while (iter.hasNext()) {
                FlowBlock block = iter.next();
                changed |= block.compaction();
                if (block.isEmpty()) {
                    LOG.debug("removing empty block: {}", block); //$NON-NLS-1$
                    iter.remove();
                    changed = true;
                }
            }
        } while (changed);
    }

    /**
     * 演算子グラフを標準形に変換する。
     * <ul>
     * <li> フロー部品のインライン化 </li>
     * <li> shuffle-shuffleへのチェックポイントの挿入 </li>
     * <li> boundary-boundaryへのidentityの挿入 </li>
     * <li> identityの分解 </li>
     * <li> identityの最小化 </li>
     * <li> フロー部品の標準形化 </li>
     * </ul>
     * @param graph 変換対象のグラフ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    void normalizeFlowGraph(FlowGraph graph) {
        assert graph != null;
        LOG.debug("normalizing operator graph: {}", graph); //$NON-NLS-1$

        inlineFlowParts(graph);

        // FIXME process "at most once" (or "volatile") operators
        unifyGlobalSideEffects(graph);

        insertCheckpoints(graph);
        insertIdentities(graph);
        splitIdentities(graph);
        reduceIdentities(graph);
    }

    private void inlineFlowParts(FlowGraph graph) {
        assert graph != null;
        for (FlowElement element : FlowGraphUtil.collectFlowParts(graph)) {
            // first, inlines nested flow parts
            FlowPartDescription desc = (FlowPartDescription) element.getDescription();
            inlineFlowParts(desc.getFlowGraph());

            Inline inlineConfig = element.getAttribute(Inline.class);
            if (inlineConfig == null || inlineConfig == Inline.DEFAULT) {
                inlineConfig = options.isCompressFlowPart()
                        ? Inline.FORCE_AGGREGATE
                        : Inline.KEEP_SEGREGATED;
            }
            if (inlineConfig == Inline.FORCE_AGGREGATE) {
                LOG.debug("compressing flow-part: {}", element.getDescription().getName()); //$NON-NLS-1$
                FlowGraphUtil.inlineFlowPart(element);
            } else {
                FlowGraphUtil.inlineFlowPart(element, FlowBoundary.STAGE);
            }
        }
        assert FlowGraphUtil.collectFlowParts(graph).isEmpty() : FlowGraphUtil.collectFlowParts(graph);
    }

    /**
     * shuffle -* shuffleとなるような接続を発見した際に、チェックポイントを挿入して
     * shuffle -* stageとなるように変換する。
     * @param graph 対象のグラフ
     */
    void insertCheckpoints(FlowGraph graph) {
        assert graph != null;
        LOG.debug("inserting checkpoints on stage bounds: {}", graph); //$NON-NLS-1$
        for (FlowElement element : FlowGraphUtil.collectBoundaries(graph)) {
            insertCheckpoints(element);
        }
    }

    private void insertCheckpoints(FlowElement element) {
        assert element != null;
        if (FlowGraphUtil.isShuffleBoundary(element) == false) {
            return;
        }
        for (FlowElementOutput output : element.getOutputPorts()) {
            insertCheckpointsWithPushDown(output);
        }
    }

    private void insertCheckpointsWithPushDown(FlowElementOutput start) {
        assert start != null;
        LinkedList<FlowElementOutput> work = new LinkedList<FlowElementOutput>();
        work.add(start);
        while (work.isEmpty() == false) {
            FlowElementOutput output = work.removeFirst();
            if (isSuccessShuffleBoundary(output) == false) {
                continue;
            }
            Set<PortConnection> connections = output.getConnected();
            if (connections.size() != 1) {
                LOG.debug("Inserts checkpoint after {}", output); //$NON-NLS-1$
                FlowGraphUtil.insertCheckpoint(output);
                continue;
            }
            FlowElementInput input = connections.iterator().next().getDownstream();
            FlowElement successor = input.getOwner();
            if (isPushDownTarget(successor) == false) {
                LOG.debug("Inserts checkpoint after {}", output); //$NON-NLS-1$
                FlowGraphUtil.insertCheckpoint(output);
                continue;
            }
            LOG.debug("Pushdown operator {}", successor); //$NON-NLS-1$
            work.addAll(successor.getOutputPorts());
        }
    }

    private boolean isSuccessShuffleBoundary(FlowElementOutput output) {
        assert output != null;
        Collection<FlowElement> successors = FlowGraphUtil.getSucceedingBoundaries(output);
        for (FlowElement successor : successors) {
            assert FlowGraphUtil.isBoundary(successor);
            if (FlowGraphUtil.isShuffleBoundary(successor) == false) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean isPushDownTarget(FlowElement element) {
        assert element != null;
        if (element.getInputPorts().size() != 1) {
            return false;
        }
        FlowElementInput input = element.getInputPorts().get(0);
        if (input.getConnected().size() != 1) {
            return false;
        }
        if (FlowGraphUtil.isBoundary(element)) {
            return false;
        }
        FlowElementDescription desc = element.getDescription();
        if (desc.getKind() == FlowElementKind.PSEUD) {
            return true;
        } else if (desc.getKind() == FlowElementKind.OPERATOR) {
            OperatorDescription op = (OperatorDescription) desc;
            Class<? extends Annotation> kind = op.getDeclaration().getAnnotationType();
            if (kind == Branch.class
                    || kind == Split.class
                    || kind == Project.class
                    || kind == Restructure.class
                    || kind == SideDataCheck.class
                    || kind == SideDataBranch.class
                    || kind == Logging.class
                    || kind == Trace.class
                    || kind == Debug.class) {
                return true;
            }
        }
        return false;
    }

    /**
     * stage -1 boundaryとなるような接続を発見した際に、恒等関数を挿入して
     * stage - identity - boundaryとなるように変換する。
     * @param graph 対象のグラフ
     */
    void insertIdentities(FlowGraph graph) {
        assert graph != null;
        for (FlowElement element : FlowGraphUtil.collectBoundaries(graph)) {
            insertIdentities(element);
        }
    }

    private void insertIdentities(FlowElement element) {
        assert element != null;
        if (FlowGraphUtil.isStageBoundary(element) == false) {
            return;
        }
        for (FlowElementOutput output : element.getOutputPorts()) {
            for (FlowElementInput opposite : output.getOpposites()) {
                FlowElement successor = opposite.getOwner();
                if (FlowGraphUtil.isBoundary(successor)) {
                    FlowGraphUtil.insertIdentity(output);
                }
            }
        }
    }

    /**
     * すべてのidentityの入出力が1:1になるようにidentityを分解する。
     * @param graph 対象のグラフ
     */
    void splitIdentities(FlowGraph graph) {
        assert graph != null;
        LOG.debug("normalizing identity operators: {}", graph); //$NON-NLS-1$
        boolean changed;
        do {
            changed = false;
            for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
                if (FlowGraphUtil.isIdentity(element)) {
                    changed |= FlowGraphUtil.splitIdentity(element);
                }
            }
        } while (changed);
    }

    /**
     * {@link #splitIdentities(FlowGraph)}が適用されていることを前提に、
     * 不要な恒等関数をすべて除去する。
     * @param graph 対象のグラフ
     * @see #insertIdentities(FlowGraph)
     */
    void reduceIdentities(FlowGraph graph) {
        assert graph != null;
        LOG.debug("reducing identity operators: {}", graph); //$NON-NLS-1$
        boolean changed;
        do {
            changed = false;
            for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
                if (FlowGraphUtil.isIdentity(element) == false) {
                    continue;
                }
                Set<FlowElement> preds = FlowGraphUtil.getPredecessors(element);
                Set<FlowElement> succs = FlowGraphUtil.getSuccessors(element);
                assert preds.size() == 1 && succs.size() == 1 : "all identities must be splitted"; //$NON-NLS-1$

                // ブロック唯一の要素になりそうであれば残す
                FlowElement pred = preds.iterator().next();
                FlowElement succ = succs.iterator().next();
                if (FlowGraphUtil.isStageBoundary(pred) && FlowGraphUtil.isBoundary(succ)) {
                    continue;
                }
                LOG.debug("removing redundant identity operator: {}", element); //$NON-NLS-1$

                changed = true;
                FlowGraphUtil.skip(element);
            }
        } while (changed);
    }

    /**
     * 演算子グラフの未結線や循環結線がないことを確認する。
     * @param graph 対象のグラフ
     * @return 未結線や循環結線が存在する場合
     */
    boolean validate(FlowGraph graph) {
        assert graph != null;
        LOG.debug("validating flow graph: {}", graph); //$NON-NLS-1$

        Graph<FlowElement> elements = FlowGraphUtil.toElementGraph(graph);

        boolean valid = true;
        valid &= validateConnection(graph, elements);
        valid &= validateAcyclic(graph, elements);
        for (FlowElement element : FlowGraphUtil.collectFlowParts(graph)) {
            FlowPartDescription description = (FlowPartDescription) element.getDescription();
            valid &= validate(description.getFlowGraph());
        }

        return valid;
    }

    private boolean validateConnection(FlowGraph graph, Graph<FlowElement> elements) {
        assert graph != null;
        assert elements != null;
        LOG.debug("validating operator connections: {}", graph); //$NON-NLS-1$

        boolean sawError = false;
        for (FlowElement element : elements.getNodeSet()) {
            Connectivity connectivity = element.getAttribute(Connectivity.class);
            if (connectivity == null) {
                connectivity = Connectivity.getDefault();
            }
            for (FlowElementInput port : element.getInputPorts()) {
                if (port.getConnected().isEmpty() == false) {
                    continue;
                }
                error(
                        graph,
                        Collections.singletonList(element),
                        "{0}の入力{1}が他の演算子からの出力、もしくはジョブフローの入力に接続されていません",
                        element.getDescription(),
                        port.getDescription().getName());
                sawError = true;
            }
            for (FlowElementOutput port : element.getOutputPorts()) {
                if (port.getConnected().isEmpty() == false) {
                    continue;
                }
                if (connectivity == Connectivity.MANDATORY) {
                    error(
                            graph,
                            Collections.singletonList(element),
                            "{0}の出力{1}が他の演算子への入力、もしくはジョブフローの出力に接続されていません",
                            element.getDescription(),
                            port.getDescription().getName());
                    sawError = true;
                } else {
                    LOG.debug("inserting implicit \"stop\" operator: {}.{}", //$NON-NLS-1$
                            element.getDescription().getName(),
                            port.getDescription().getName());
                    FlowGraphUtil.stop(port);
                }
            }
        }

        return sawError == false;
    }

    private boolean validateAcyclic(FlowGraph graph, Graph<FlowElement> elements) {
        assert graph != null;
        assert elements != null;
        LOG.debug("validating cyclicity: {}", graph); //$NON-NLS-1$

        Set<Set<FlowElement>> circuits = Graphs.findCircuit(elements);
        for (Set<FlowElement> cyclic : circuits) {
            List<FlowElement> context = Lists.from(cyclic);
            List<String> names = Lists.create();
            for (FlowElement elem : context) {
                names.add(elem.getDescription().getName());
            }
            error(
                    graph,
                    context,
                    "結線の循環を検出しました {0}",
                    names);
        }

        return circuits.isEmpty();
    }

    private void error(
            FlowGraph graph,
            List<FlowElement> context,
            String message,
            Object... messageArguments) {
        assert graph != null;
        assert context != null;
        assert message != null;
        assert messageArguments != null;
        String text;
        if (messageArguments.length == 0) {
            text = message;
        } else {
            text = MessageFormat.format(message, messageArguments);
        }
        diagnostics.add(new Diagnostic(graph, context, text));
    }

    /**
     * 実行計画の診断情報。
     */
    public static class Diagnostic {

        /**
         * 診断対象のグラフ。
         */
        public final FlowGraph graph;

        /**
         * 診断情報の対象。
         */
        public final List<FlowElement> context;

        /**
         * 診断メッセージ。
         */
        public final String message;

        /**
         * インスタンスを生成する。
         * @param graph 診断対象のグラフ
         * @param context 診断情報の対象
         * @param message 診断メッセージ
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Diagnostic(
                FlowGraph graph,
                List<FlowElement> context,
                String message) {
            Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(message, "message"); //$NON-NLS-1$
            this.graph = graph;
            this.context = Collections.unmodifiableList(context);
            this.message = message;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0} (at {1})",
                    message,
                    graph.getDescription().getName());
        }
    }

    private static class FlowBlockGroup {

        final FlowBlock founder;

        /**
         * グループへの入力を生成する出力。
         */
        @SuppressWarnings("unused")
        private final Set<FlowBlock.Output> groupSource;

        /**
         * 直接先行するブロック。
         * ただし、
         * - Reducerが後続しないMapperである
         * - Reducerである
         * のいずれか。
         */
        final Set<FlowBlock> predeceaseBlocks;

        final Set<FlowBlock> members;

        final boolean reducer;

        int distance = -1;

        FlowBlockGroup(FlowBlock flowBlock) {
            assert flowBlock != null;
            this.founder = flowBlock;
            this.members = Sets.create();
            this.members.add(flowBlock);
            this.reducer = flowBlock.isReduceBlock();
            this.groupSource = collectStageSource(flowBlock);
            this.predeceaseBlocks = collectPredeceaseBlocks(flowBlock);
        }

        private Set<FlowBlock.Output> collectStageSource(FlowBlock flowBlock) {
            assert flowBlock != null;
            if (flowBlock.isReduceBlock()) {
                Set<FlowBlock.Output> results = Sets.create();
                for (FlowBlock predecessor : getPredeceaseBlocks(flowBlock)) {
                    assert predecessor.isReduceBlock() == false;
                    results.addAll(collectBlockSource(predecessor));
                }
                return results;
            } else {
                return collectBlockSource(flowBlock);
            }
        }

        private Set<FlowBlock> getPredeceaseBlocks(FlowBlock flowBlock) {
            assert flowBlock != null;
            Set<FlowBlock> results = Sets.create();
            for (FlowBlock.Input input : flowBlock.getBlockInputs()) {
                for (FlowBlock.Connection conn : input.getConnections()) {
                    FlowBlock pred = conn.getUpstream().getOwner();
                    results.add(pred);
                }
            }
            return results;
        }

        private Set<FlowBlock.Output> collectBlockSource(FlowBlock flowBlock) {
            assert flowBlock != null;
            Set<FlowBlock.Output> results = Sets.create();
            for (FlowBlock.Input input : flowBlock.getBlockInputs()) {
                for (FlowBlock.Connection conn : input.getConnections()) {
                    results.add(conn.getUpstream());
                }
            }
            return results;
        }

        private Set<FlowBlock> collectPredeceaseBlocks(FlowBlock flowBlock) {
            assert flowBlock != null;
            Set<FlowBlock> results = Sets.create();
            LinkedList<FlowBlock> work = new LinkedList<FlowBlock>();
            work.addLast(flowBlock);
            while (work.isEmpty() == false) {
                FlowBlock first = work.removeFirst();
                Set<FlowBlock> preds = getPredeceaseBlocks(first);
                for (FlowBlock block : preds) {
                    // 入力ブロックは除外
                    if (block.getBlockInputs().isEmpty()) {
                        continue;
                    }
                    if (block.isReduceBlock() || block.isSucceedingReduceBlock() == false) {
                        results.add(block);
                    } else {
                        work.addLast(block);
                    }
                }
            }
            return results;
        }

        boolean combine(FlowBlockGroup other) {
            assert other != null;
            // mapperどうし、またはreducerどうしでのみ合成
            if (this.reducer != other.reducer) {
                return false;
            }
            // 入力からの最大長が等しいものを合成
            if (this.distance == -1 || this.distance != other.distance) {
                return false;
            }
            this.members.addAll(other.members);
            return true;
        }
    }
}
