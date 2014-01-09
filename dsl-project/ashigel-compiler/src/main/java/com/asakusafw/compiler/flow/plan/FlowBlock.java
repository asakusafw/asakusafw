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
package com.asakusafw.compiler.flow.plan;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.PortConnection;

/**
 * 演算子グラフの一部を切り出したブロック。
 */
public class FlowBlock {

    static final Logger LOG = LoggerFactory.getLogger(FlowBlock.class);

    private final int serialNumber;

    private final FlowGraph source;

    private final List<FlowBlock.Input> blockInputs;

    private final List<FlowBlock.Output> blockOutputs;

    private Set<FlowElement> elements;

    private boolean detached;

    /**
     * 境界にあるポート一覧からこのインスタンスを生成する。
     * <p>
     * 生成されるインスタンスは初期状態で{@link #detach()}されていない。
     * </p>
     * @param serialNumber シリアル番号
     * @param source このブロックの元になった演算子グラフ
     * @param inputs このブロックへの入力に利用するフロー要素への入力
     * @param outputs このブロックへの出力に利用するフロー要素からの出力
     * @param elements このブロックに含まれる要素の一覧
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static FlowBlock fromPorts(
            int serialNumber,
            FlowGraph source,
            List<FlowElementInput> inputs,
            List<FlowElementOutput> outputs,
            Set<FlowElement> elements) {
        List<PortConnection> toInput = Lists.create();
        List<PortConnection> fromOutput = Lists.create();
        for (FlowElementInput in : inputs) {
            toInput.addAll(in.getConnected());
        }
        for (FlowElementOutput out : outputs) {
            fromOutput.addAll(out.getConnected());
        }
        return new FlowBlock(serialNumber, source, toInput, fromOutput, elements);
    }

    /**
     * Creates a merged flow blocks from other blocks.
     * @param blocks original blocks
     * @param inputMapping input mapping (created -&gt; original)
     * @param outputMapping output mapping (created -&gt; original)
     * @return the merged block
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FlowBlock fromBlocks(
            Collection<FlowBlock> blocks,
            Map<FlowBlock.Input, Set<FlowBlock.Input>> inputMapping,
            Map<FlowBlock.Output, Set<FlowBlock.Output>> outputMapping) {
        Precondition.checkMustNotBeNull(blocks, "blocks"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputMapping, "inputMapping"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputMapping, "outputMapping"); //$NON-NLS-1$
        FlowGraph graph = null;
        int minSerialNumber = Integer.MAX_VALUE;
        int reduces = 0;
        for (FlowBlock block : blocks) {
            if (block.detached == false) {
                throw new IllegalArgumentException();
            }
            graph = graph == null ? block.source : graph;
            minSerialNumber = Math.min(minSerialNumber, block.serialNumber);
            reduces += block.isReduceBlock() ? 1 : 0;
        }
        if (reduces != 0 && reduces != blocks.size()) {
            throw new IllegalArgumentException("Cannot merge map blocks and reduce blocks");
        }
        final Set<PortConnection> empty = Collections.<PortConnection>emptySet();
        FlowBlock result = new FlowBlock(minSerialNumber, graph);
        for (FlowBlock block : blocks) {
            result.elements.addAll(block.elements);
            for (FlowBlock.Input origin : block.getBlockInputs()) {
                FlowBlock.Input mapped = result.new Input(origin.getElementPort(), empty);
                result.blockInputs.add(mapped);
                Maps.addToSet(inputMapping, origin, mapped);
            }
            for (FlowBlock.Output origin : block.getBlockOutputs()) {
                FlowBlock.Output mapped = result.new Output(origin.getElementPort(), empty);
                result.blockOutputs.add(mapped);
                Maps.addToSet(outputMapping, origin, mapped);
            }
        }
        return result;
    }

    /**
     * インスタンスを生成する。
     * <p>
     * 生成されるインスタンスは初期状態で{@link #detach()}されていない。
     * </p>
     * @param serialNumber シリアル番号
     * @param source このブロックの元になった演算子グラフ
     * @param inputs このブロックへの入力に利用するフロー要素への入力
     * @param outputs このブロックへの出力に利用するフロー要素からの出力
     * @param elements このブロックに含まれる要素の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowBlock(
            int serialNumber,
            FlowGraph source,
            List<PortConnection> inputs,
            List<PortConnection> outputs,
            Set<FlowElement> elements) {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(elements, "elements"); //$NON-NLS-1$
        int shuffles = countShuffleBoundary(inputs);
        if (shuffles != 0 && shuffles != inputs.size()) {
            throw new IllegalArgumentException("inputs must not be shuffle bounds partially");
        }
        this.serialNumber = serialNumber;
        this.source = source;
        this.blockInputs = toBlockInputs(inputs);
        this.blockOutputs = toBlockOutputs(outputs);
        this.elements = Sets.from(elements);
        this.detached = false;
    }

    private FlowBlock(int serialNumber, FlowGraph source) {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        this.serialNumber = serialNumber;
        this.source = source;
        this.blockInputs = Lists.create();
        this.blockOutputs = Lists.create();
        this.elements = Sets.create();
        this.detached = false;
    }

    private int countShuffleBoundary(List<PortConnection> inputs) {
        assert inputs != null;
        int result = 0;
        for (PortConnection input : inputs) {
            if (FlowGraphUtil.isShuffleBoundary(input.getDownstream().getOwner())) {
                result++;
            }
        }
        return result;
    }

    private List<FlowBlock.Input> toBlockInputs(List<PortConnection> inputs) {
        assert inputs != null;
        Map<FlowElementInput, Set<PortConnection>> map = new LinkedHashMap<FlowElementInput, Set<PortConnection>>();
        for (PortConnection input : inputs) {
            FlowElementInput port = input.getDownstream();
            Maps.addToSet(map, port, input);
        }
        List<FlowBlock.Input> results = Lists.create();
        for (Map.Entry<FlowElementInput, Set<PortConnection>> entry : map.entrySet()) {
            results.add(new FlowBlock.Input(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    private List<FlowBlock.Output> toBlockOutputs(List<PortConnection> outputs) {
        assert outputs != null;
        Map<FlowElementOutput, Set<PortConnection>> map = new LinkedHashMap<FlowElementOutput, Set<PortConnection>>();
        for (PortConnection output : outputs) {
            FlowElementOutput port = output.getUpstream();
            Maps.addToSet(map, port, output);
        }
        List<FlowBlock.Output> results = Lists.create();
        for (Map.Entry<FlowElementOutput, Set<PortConnection>> entry : map.entrySet()) {
            results.add(new FlowBlock.Output(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    /**
     * このブロックの元になった演算子グラフを返す。
     * @return このブロックの元になった演算子グラフ
     */
    public FlowGraph getSource() {
        return source;
    }

    /**
     * このブロックのシリアル番号を返す。
     * <p>
     * 番号自体は特に意味を持たないが、同一の実行計画時に作成された
     * それぞれのブロックは異なる番号を持つことが保証される。
     * </p>
     * @return このブロックのシリアル番号
     */
    public int getSerialNumber() {
        return serialNumber;
    }

    /**
     * このブロックへの入力一覧を返す。
     * @return このブロックへの入力一覧
     */
    public List<FlowBlock.Input> getBlockInputs() {
        return blockInputs;
    }

    /**
     * このブロックからの出力一覧を返す。
     * @return このブロックからの出力一覧
     */
    public List<FlowBlock.Output> getBlockOutputs() {
        return blockOutputs;
    }

    /**
     * このブロックに含まれる要素の一覧を返す。
     * @return このブロックに含まれる要素の一覧
     */
    public Set<FlowElement> getElements() {
        return elements;
    }

    /**
     * 指定の出力と指定の入力が接続されている場合のみ{@code true}を返す。
     * @param upstream 上流の出力
     * @param downstream 下流の入力
     * @return 指定の出力と指定の入力が接続されている場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isConnected(
            FlowBlock.Output upstream,
            FlowBlock.Input downstream) {
        Precondition.checkMustNotBeNull(upstream, "upstream"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(downstream, "downstream"); //$NON-NLS-1$
        for (FlowBlock.Connection conn : upstream.getConnections()) {
            if (conn.getDownstream().equals(downstream)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定の出力と指定の入力を接続する。
     * @param upstream 上流の出力
     * @param downstream 下流の入力
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void connect(
            FlowBlock.Output upstream,
            FlowBlock.Input downstream) {
        Precondition.checkMustNotBeNull(upstream, "upstream"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(downstream, "downstream"); //$NON-NLS-1$
        if (upstream.isConnected(downstream)) {
            return;
        }
        FlowBlock.Connection conn = new FlowBlock.Connection(upstream, downstream);
        upstream.addConnection(conn);
        downstream.addConnection(conn);

        assert upstream.getElementPort().getDescription().getDataType().equals(
                downstream.getElementPort().getDescription().getDataType());
    }

    /**
     * このブロックが空のブロックである場合にのみ{@code true}を返す。
     * <p>
     * ブロックが空であるとは、入力も出力も存在しないことをいう。
     * </p>
     * @return 空のブロックである場合にのみ{@code true}
     */
    public boolean isEmpty() {
        return blockInputs.isEmpty() && blockOutputs.isEmpty();
    }

    /**
     * このブロックがレデュースブロックを表現する場合のみ{@code true}を返す。
     * @return レデュースブロックを表現する場合のみ{@code true}
     */
    public boolean isReduceBlock() {
        if (blockInputs.isEmpty()) {
            return false;
        }
        FlowBlock.Input first = blockInputs.get(0);
        return FlowGraphUtil.isShuffleBoundary(first.getElementPort().getOwner());
    }

    /**
     * このブロックに後続するブロックがレデュースブロックである場合のみ{@code true}を返す。
     * <p>
     * このブロックに後続するブロックが存在しない場合、このメソッドは常に{@code false}を返す。
     * </p>
     * <p>
     * この操作はあらかじめ{@link #detach()}が行われている必要がある。
     * </p>
     * @return 後続するブロックがレデュースブロックである場合のみ{@code true}
     * @throws IllegalStateException このブロックがデタッチされていない場合
     */
    public boolean isSucceedingReduceBlock() {
        if (detached == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "{0} was not detached",
                    this));
        }
        for (FlowBlock.Output output : blockOutputs) {
            for (FlowBlock.Connection conn : output.getConnections()) {
                // TODO 後続にReduce/Stageが混在する場合には修正
                FlowBlock successor = conn.getDownstream().getOwner();
                return successor.isReduceBlock();
            }
        }
        return false;
    }

    /**
     * このブロック内のフロー要素を全体のフローグラフから切り離す。
     * <p>
     * この操作によって、まずブロックが内包するフロー要素のコピーが作成される。
     * コピーされたフロー要素はブロック内のほかの要素とのみ結線された状態となり、
     * それ以外の要素との結線はすべて解除される。
     * </p>
     */
    public void detach() {
        if (detached) {
            return;
        }
        LOG.debug("{}を{}からデタッチします", this, getSource());
        Map<FlowElement, FlowElement> elementMapping = Maps.create();
        Map<FlowElementInput, FlowElementInput> inputMapping = Maps.create();
        Map<FlowElementOutput, FlowElementOutput> outputMapping = Maps.create();
        FlowGraphUtil.deepCopy(elements, elementMapping, inputMapping, outputMapping);
        this.elements = Sets.from(elementMapping.values());
        reconnectBlockInOut(inputMapping, outputMapping);
        detached = true;
    }

    private void reconnectBlockInOut(
            Map<FlowElementInput, FlowElementInput> inputMapping,
            Map<FlowElementOutput, FlowElementOutput> outputMapping) {
        assert inputMapping != null;
        assert outputMapping != null;
        for (FlowBlock.Input bound : blockInputs) {
            FlowElementInput port = inputMapping.get(bound.getElementPort());
            assert port != null;
            bound.setElementPort(port);
        }
        for (FlowBlock.Output bound : blockOutputs) {
            FlowElementOutput port = outputMapping.get(bound.getElementPort());
            assert port != null;
            bound.setElementPort(port);
        }
    }

    /**
     * Unifies elements.
     * @since 0.4.0
     */
    public void unify() {
        if (detached == false) {
            throw new IllegalStateException();
        }
        Map<FlowElement, FlowElement> elementMapping = Maps.create();
        Map<FlowElementInput, FlowElementInput> inputMapping = Maps.create();
        Map<FlowElementOutput, FlowElementOutput> outputMapping = Maps.create();
        FlowGraphUtil.deepCopy(elements, elementMapping, inputMapping, outputMapping);
        unifyElements(elementMapping, inputMapping, outputMapping);
        unifyInputs(elementMapping, inputMapping, outputMapping);
        unifyOutputs(elementMapping, inputMapping, outputMapping);
    }

    private void unifyElements(
            Map<FlowElement, FlowElement> elementMapping,
            Map<FlowElementInput, FlowElementInput> inputMapping,
            Map<FlowElementOutput, FlowElementOutput> outputMapping) {
        assert elementMapping != null;
        assert inputMapping != null;
        assert outputMapping != null;

        LOG.debug("Unifying elements: {}", this);

        Map<Object, FlowElement> unifier = Maps.create();
        Map<FlowElement, FlowElement> unifiedElements = Maps.create();
        Map<FlowElementInput, FlowElementInput> unifiedInputs = Maps.create();
        Map<FlowElementOutput, FlowElementOutput> unifiedOutputs = Maps.create();

        // find originals
        for (Map.Entry<FlowElement, FlowElement> entry : elementMapping.entrySet()) {
            FlowElement orig = entry.getKey();
            FlowElement dest = entry.getValue();
            assert orig.getIdentity().equals(orig.getIdentity());
            FlowElement unified;
            if (unifier.containsKey(orig.getIdentity()) == false) {
                unified = dest;
                unifier.put(orig.getIdentity(), unified);
            }  else {
                unified = unifier.get(orig.getIdentity());
                LOG.debug("Unify {} -> {}", dest, unified);
            }
            unifiedElements.put(dest, unified);
            List<FlowElementInput> srcInput = orig.getInputPorts();
            List<FlowElementInput> dstInput = dest.getInputPorts();
            List<FlowElementInput> uniInput = unified.getInputPorts();
            assert srcInput.size() == uniInput.size();
            for (int i = 0, n = srcInput.size(); i < n; i++) {
                if (inputMapping.containsKey(srcInput.get(i))) {
                    inputMapping.put(srcInput.get(i), uniInput.get(i));
                    unifiedInputs.put(dstInput.get(i), uniInput.get(i));
                }
            }
            List<FlowElementOutput> srcOutput = orig.getOutputPorts();
            List<FlowElementOutput> dstOutput = dest.getOutputPorts();
            List<FlowElementOutput> uniOutput = unified.getOutputPorts();
            assert srcOutput.size() == uniOutput.size();
            for (int i = 0, n = srcOutput.size(); i < n; i++) {
                if (outputMapping.containsKey(srcOutput.get(i))) {
                    outputMapping.put(srcOutput.get(i), uniOutput.get(i));
                    unifiedOutputs.put(dstOutput.get(i), uniOutput.get(i));
                }
            }
        }

        // reconnect inputs
        for (Map.Entry<FlowElement, FlowElement> entry : elementMapping.entrySet()) {
            FlowElement elem = entry.getValue();
            FlowElement unified = unifiedElements.get(elem);
            assert unified != null;
            if (elem != unified) {
                List<FlowElementInput> srcInput = elem.getInputPorts();
                List<FlowElementInput> uniInput = unified.getInputPorts();
                assert srcInput.size() == uniInput.size();
                for (int i = 0, n = srcInput.size(); i < n; i++) {
                    FlowElementInput srcPort = srcInput.get(i);
                    FlowElementInput uniPort = uniInput.get(i);
                    for (PortConnection conn : srcPort.getConnected()) {
                        FlowElementOutput opposite = unifiedOutputs.get(conn.getUpstream());
                        assert opposite != null;
                        PortConnection.connect(opposite, uniPort);
                    }
                    srcPort.disconnectAll();
                }
            }
        }

        // reconnect outputs
        for (FlowElement elem : elementMapping.values()) {
            FlowElement unified = unifiedElements.get(elem);
            assert unified != null;
            if (elem != unified) {
                List<FlowElementOutput> srcOutput = elem.getOutputPorts();
                List<FlowElementOutput> uniOutput = unified.getOutputPorts();
                assert srcOutput.size() == uniOutput.size();
                for (int i = 0, n = srcOutput.size(); i < n; i++) {
                    FlowElementOutput srcPort = srcOutput.get(i);
                    FlowElementOutput uniPort = uniOutput.get(i);
                    for (PortConnection conn : srcPort.getConnected()) {
                        FlowElementInput opposite = unifiedInputs.get(conn.getDownstream());
                        assert opposite != null;
                        PortConnection.connect(uniPort, opposite);
                    }
                    srcPort.disconnectAll();
                }
            }
        }

        // delete unified
        for (Map.Entry<FlowElement, FlowElement> entry : elementMapping.entrySet()) {
            FlowElement elem = entry.getValue();
            FlowElement unified = unifiedElements.get(elem);
            assert unified != null;
            entry.setValue(unified);
        }

        this.elements = Sets.from(elementMapping.values());
    }

    private void unifyInputs(
            Map<FlowElement, FlowElement> elementMapping,
            Map<FlowElementInput, FlowElementInput> inputMapping,
            Map<FlowElementOutput, FlowElementOutput> outputMapping) {
        assert elementMapping != null;
        assert inputMapping != null;
        assert outputMapping != null;
        Map<FlowElementInput, FlowBlock.Input> map = Maps.create();
        for (Iterator<FlowBlock.Input> iter = blockInputs.iterator(); iter.hasNext();) {
            FlowBlock.Input blockPort = iter.next();
            FlowElementInput elementPort = inputMapping.get(blockPort.getElementPort());
            assert elementPort != null;
            FlowBlock.Input unified = map.get(elementPort);
            if (unified == null) {
                map.put(elementPort, blockPort);
                blockPort.setElementPort(elementPort);
            } else {
                LOG.debug("Input port {} will be unified", blockPort);
                iter.remove();
                for (FlowBlock.Connection conn : blockPort.getConnections()) {
                    FlowBlock.Output opposite = conn.getUpstream();
                    FlowBlock.connect(opposite, unified);
                }
                blockPort.disconnect();
            }
        }
    }

    private void unifyOutputs(
            Map<FlowElement, FlowElement> elementMapping,
            Map<FlowElementInput, FlowElementInput> inputMapping,
            Map<FlowElementOutput, FlowElementOutput> outputMapping) {
        assert elementMapping != null;
        assert outputMapping != null;
        assert inputMapping != null;
        Map<FlowElementOutput, FlowBlock.Output> map = Maps.create();
        for (Iterator<FlowBlock.Output> iter = blockOutputs.iterator(); iter.hasNext();) {
            FlowBlock.Output blockPort = iter.next();
            FlowElementOutput elementPort = outputMapping.get(blockPort.getElementPort());
            assert elementPort != null;
            FlowBlock.Output unified = map.get(elementPort);
            if (unified == null) {
                map.put(elementPort, blockPort);
                blockPort.setElementPort(elementPort);
            } else {
                LOG.debug("Output port {} will be unified", blockPort);
                iter.remove();
                for (FlowBlock.Connection conn : blockPort.getConnections()) {
                    FlowBlock.Input opposite = conn.getDownstream();
                    FlowBlock.connect(unified, opposite);
                }
                blockPort.disconnect();
            }
        }
    }

    /**
     * このブロックに含まれる不要な要素を削除する。
     * <p>
     * 次のような特性を持つ要素は、不要な要素として判断される。
     * </p>
     * <ul>
     * <li>
     *   他のいずれのブロックにも接続されていないブロックの入出力
     * </li>
     * <li>
     *   ひとつ以上の入力が存在し、かつすべての入力が他のいずれの要素にも結線されていない要素
     * </li>
     * <li>
     *   ひとつ以上の出力が存在し、かつすべての出力が他のいずれの要素にも結線されていない、
     *   かつat least onceの特性を持たない要素
     * </li>
     * </ul>
     * <p>
     * この操作は、あらかじめ{@link #detach()}が行われている必要がある。
     * </p>
     * @return ブロックが変更された場合は{@code true}、変更しなかった場合は{@code false}
     * @throws IllegalStateException このブロックがデタッチされていない場合
     */
    public boolean compaction() {
        if (detached == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "{0} was not detached",
                    this));
        }
        LOG.debug("Applying compaction: {}", this);
        boolean changed = false;
        boolean localChanged;
        do {
            localChanged = false;
            changed |= mergeSameBlockEdges();
            changed |= trimDisconnectedBlockEdges();
            changed |= trimDeadElements();
            changed |= trimDeadBlockEdges();
            localChanged |= mergeIdentity();
            changed |= localChanged;
        } while (localChanged);
        if (changed) {
            collectGarbages();
        }
        return changed;
    }

    private boolean mergeSameBlockEdges() {
        boolean changed = false;
        LOG.debug("Merging same block edges: {}", this);

        Map<FlowElementInput, FlowBlock.Input> inputMapping = Maps.create();
        for (FlowBlock.Input port : blockInputs) {
            FlowBlock.Input prime = inputMapping.get(port.getElementPort());
            if (prime != null) {
                LOG.debug("Merging block input: {} on {}", port, this);
                for (FlowBlock.Connection conn : port.getConnections()) {
                    FlowBlock.connect(conn.getUpstream(), prime);
                }
                port.disconnect();
                changed = true;
            } else {
                inputMapping.put(port.getElementPort(), port);
            }
        }

        Map<FlowElementOutput, FlowBlock.Output> outputMapping = Maps.create();
        for (FlowBlock.Output port : blockOutputs) {
            FlowBlock.Output prime = outputMapping.get(port.getElementPort());
            if (prime != null) {
                LOG.debug("Merging block output: {} on {}", port, this);
                for (FlowBlock.Connection conn : port.getConnections()) {
                    FlowBlock.connect(prime, conn.getDownstream());
                }
                port.disconnect();
                changed = true;
            } else {
                outputMapping.put(port.getElementPort(), port);
            }
        }

        return changed;
    }

    private boolean trimDisconnectedBlockEdges() {
        boolean changed = false;
        LOG.debug("Searching for disconnected block edges: {}", this);

        // 他のブロックと接続されていない入力を削除
        Iterator<FlowBlock.Input> inputs = blockInputs.iterator();
        while (inputs.hasNext()) {
            FlowBlock.Input port = inputs.next();
            if (port.getConnections().isEmpty()) {
                LOG.debug("Deleting unnecessary block edge: {} on {}", port, this);
                inputs.remove();
                changed = true;
            }
        }

        // 他のブロックと接続されていない出力を削除
        Iterator<FlowBlock.Output> outputs = blockOutputs.iterator();
        while (outputs.hasNext()) {
            FlowBlock.Output port = outputs.next();
            if (port.getConnections().isEmpty()) {
                LOG.debug("Deleting dead block edge: {} on {}", port, this);
                outputs.remove();
                changed = true;
            }
        }
        return changed;
    }

    private boolean trimDeadElements() {
        boolean changed = false;
        LOG.debug("Searching for unnecessary operators: {}", this);

        Set<FlowElement> blockEdge = collectBlockEdges();
        Set<FlowElement> removed = Sets.create();
        LinkedList<FlowElement> work = new LinkedList<FlowElement>();
        work.addAll(elements);

        while (work.isEmpty() == false) {
            FlowElement element = work.removeFirst();

            // このセッションで削除済みのものについては処理対象としない
            if (removed.contains(element)) {
                continue;
            }

            // 入出力に関係する要素についてはここでは取り扱わない
            if (blockEdge.contains(element)) {
                continue;
            }

            if (FlowGraphUtil.isAlwaysEmpty(element)) {
                // ひとつ以上の入力が存在し、かつすべての入力が他のいずれの要素にも結線されていない要素
                LOG.debug("Deleting operator without input: {} on {}", element, this);

                // 後続する要素に対する処理を発火させたのち、この要素を除去
                work.addAll(FlowGraphUtil.getSuccessors(element));
                remove(element);
                removed.add(element);
                changed = true;
            } else if (FlowGraphUtil.isAlwaysStop(element)
                    && FlowGraphUtil.hasMandatorySideEffect(element) == false) {
                // ひとつ以上の出力が存在し、かつすべての出力が他のいずれの要素にも結線されていない、
                // かつat least onceの特性を持たない要素
                LOG.debug("Deleting operator without output: {} on {}", element, this);

                // 先行する要素に対する処理を発火させたのち、この要素を除去
                work.addAll(FlowGraphUtil.getPredecessors(element));
                remove(element);
                removed.add(element);
                changed = true;
            }
        }

        return changed;
    }

    private boolean trimDeadBlockEdges() {
        boolean changed = false;
        LOG.debug("Searching for unnecessary block edges: {}", this);

        Set<FlowElement> inputElements = Sets.create();
        Set<FlowElement> outputElements = Sets.create();
        for (FlowBlock.Output output : blockOutputs) {
            outputElements.add(output.getElementPort().getOwner());
        }

        // 利用しない入力は削除
        Iterator<FlowBlock.Input> inputs = blockInputs.iterator();
        while (inputs.hasNext()) {
            FlowBlock.Input port = inputs.next();
            FlowElement element = port.getElementPort().getOwner();
            if (FlowGraphUtil.hasSuccessors(element) == false
                    && FlowGraphUtil.hasMandatorySideEffect(element) == false
                    && outputElements.contains(element) == false) {
                LOG.debug("Deleting unnecessary input: {} on {}", port, this);
                port.disconnect();
                inputs.remove();
                changed = true;
            } else {
                inputElements.add(element);
            }
        }

        // 利用しない出力は削除
        Iterator<FlowBlock.Output> outputs = blockOutputs.iterator();
        while (outputs.hasNext()) {
            FlowBlock.Output port = outputs.next();
            FlowElement element = port.getElementPort().getOwner();
            if (FlowGraphUtil.hasPredecessors(element) == false
                    && inputElements.contains(element) == false) {
                LOG.debug("Deleting unnecessary output: {} on {}", port, this);
                port.disconnect();
                outputs.remove();
                changed = true;
            }
        }

        return changed;
    }

    private boolean mergeIdentity() {
        boolean changed = false;
        boolean foundTarget = false;
        Map<FlowBlock.Input, List<FlowBlock.Output>> targets =  Maps.create();
        for (FlowBlock.Output output : blockOutputs) {
            FlowElement element = output.getElementPort().getOwner();
            if (element.getDescription().getKind() != FlowElementKind.PSEUD) {
                continue;
            }
            if (output.getConnections().size() != 1) {
                continue;
            }
            FlowBlock.Input opposite = output.getConnections().get(0).getDownstream();
            List<FlowBlock.Output> list = targets.get(opposite);
            if (list == null) {
                list = new ArrayList<FlowBlock.Output>();
                targets.put(opposite, list);
            } else {
                foundTarget = true;
            }
            list.add(output);
        }
        if (foundTarget == false) {
            return changed;
        }
        Map<FlowElementInput, FlowBlock.Input> inputs = Maps.create();
        for (FlowBlock.Input input : blockInputs) {
            FlowElementInput elementInput = input.getElementPort();
            assert inputs.containsKey(elementInput) == false;
            inputs.put(elementInput, input);
        }
        for (Map.Entry<FlowBlock.Input, List<FlowBlock.Output>> entry : targets.entrySet()) {
            List<FlowBlock.Output> upstream = entry.getValue();
            if (upstream.size() == 1) {
                continue;
            }
            FlowElement primaryElement = upstream.get(0).getElementPort().getOwner();
            assert primaryElement.getDescription().getKind() == FlowElementKind.PSEUD;
            assert primaryElement.getInputPorts().size() == 1;
            FlowElementInput primaryInput = primaryElement.getInputPorts().get(0);
            FlowBlock.Input primarySource = inputs.get(primaryInput);
            assert primarySource != null;

            for (int i = 1, n = upstream.size(); i < n; i++) {
                FlowBlock.Output otherTarget = upstream.get(i);
                FlowElement otherElement = otherTarget.getElementPort().getOwner();
                LOG.debug("Unifying pseud element: {} -> {}", otherElement, primaryElement);

                assert otherElement.getDescription().getKind() == FlowElementKind.PSEUD;
                assert otherElement.getInputPorts().size() == 1;
                FlowElementInput otherInput = otherElement.getInputPorts().get(0);
                FlowBlock.Input otherSource = inputs.get(otherInput);
                assert otherSource != null;
                for (FlowBlock.Connection conn : otherSource.getConnections()) {
                    FlowBlock.connect(conn.getUpstream(), primarySource);
                }
                otherSource.disconnect();
                otherTarget.disconnect();
                changed = true;
            }
        }
        return changed;
    }

    /**
     * 不要になった要素を削除する。
     * @return 実際に削除した場合は{@code true}
     */
    public boolean collectGarbages() {
        LOG.debug("{}の不要になった要素を削除しています", this);
        Set<FlowElement> blockEdge = collectBlockEdges();
        boolean changed = false;
        LOOP: for (Iterator<FlowElement> iter = elements.iterator(); iter.hasNext();) {
            FlowElement element = iter.next();
            if (blockEdge.contains(element)) {
                continue;
            }
            for (FlowElementInput input : element.getInputPorts()) {
                if (input.getConnected().isEmpty() == false) {
                    continue LOOP;
                }
            }
            for (FlowElementOutput output : element.getOutputPorts()) {
                if (output.getConnected().isEmpty() == false) {
                    continue LOOP;
                }
            }
            iter.remove();
            changed = true;
        }
        return changed;
    }

    private void remove(FlowElement element) {
        assert element != null;
        elements.remove(element);
        FlowGraphUtil.disconnect(element);
    }

    private Set<FlowElement> collectBlockEdges() {
        Set<FlowElement> blockEdge = Sets.create();
        for (FlowBlock.Input input : getBlockInputs()) {
            blockEdge.add(input.getElementPort().getOwner());
        }
        for (FlowBlock.Output output : getBlockOutputs()) {
            blockEdge.add(output.getElementPort().getOwner());
        }
        return blockEdge;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "FlowBlock[{1}]({0}) - {2}..",
                String.valueOf(serialNumber),
                isReduceBlock() ? "R" : "M",
                getBlockInputs().isEmpty() ? "?" : getBlockInputs().get(0));
    }

    /**
     * ブロックへの入力。
     */
    public class Input {

        private FlowElementInput input;

        private final List<Connection> connections;

        private Set<PortConnection> originalConnections;

        /**
         * インスタンスを生成する。
         * @param input 要素への入力
         * @param originalConnections 要素への本来の接続先
         * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
         */
        public Input(FlowElementInput input, Set<PortConnection> originalConnections) {
            Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
            this.input = input;
            this.connections = Lists.create();
            this.originalConnections = originalConnections;
        }

        /**
         * この入力への本来の接続元を返す。
         * @return この入力への本来の接続元
         */
        public Set<PortConnection> getOriginalConnections() {
            return originalConnections;
        }

        /**
         * この出力を所有するブロックを返す。
         * @return この出力を所有するブロック
         */
        public FlowBlock getOwner() {
            return FlowBlock.this;
        }

        /**
         * この入力に対応するフロー要素への入力を返す。
         * @return 対応するフロー要素への入力
         */
        public FlowElementInput getElementPort() {
            return this.input;
        }

        /**
         * この入力への結線情報を返す。
         * @return この入力への結線情報
         */
        public List<Connection> getConnections() {
            return this.connections;
        }

        void setElementPort(FlowElementInput port) {
            assert port != null;
            this.input = port;
            this.originalConnections = Collections.emptySet();
        }

        void addConnection(Connection conn) {
            assert conn != null;
            connections.add(conn);
        }

        void disconnect() {
            for (Connection conn : Lists.from(connections)) {
                conn.disconnect();
            }
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}'{'owner=FlowBlock@{1}'}'",
                    getElementPort(),
                    String.valueOf(FlowBlock.this.hashCode()));
        }
    }

    /**
     * ブロックからの出力。
     */
    public class Output {

        private FlowElementOutput output;

        private final List<Connection> connections;

        private Set<PortConnection> originalConnections;

        /**
         * インスタンスを生成する。
         * @param output 要素への出力
         * @param originalConnections 要素への本来の接続先
         * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
         */
        public Output(FlowElementOutput output, Set<PortConnection> originalConnections) {
            Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
            this.output = output;
            this.connections = Lists.create();
            this.originalConnections = originalConnections;
        }

        boolean isConnected(FlowBlock.Input downstream) {
            for (Connection conn : connections) {
                if (conn.getDownstream() == downstream) {
                    return true;
                }
            }
            return false;
        }

        /**
         * この出力からの本来の接続先を返す。
         * @return この出力からの本来の接続先
         */
        public Set<PortConnection> getOriginalConnections() {
            return originalConnections;
        }

        /**
         * この出力を所有するブロックを返す。
         * @return この出力を所有するブロック
         */
        public FlowBlock getOwner() {
            return FlowBlock.this;
        }

        /**
         * この出力に対応するフロー要素からの出力を返す。
         * @return 対応するフロー要素からの出力
         */
        public FlowElementOutput getElementPort() {
            return this.output;
        }

        /**
         * この出力からの結線情報を返す。
         * @return この出力からの結線情報
         */
        public List<Connection> getConnections() {
            return this.connections;
        }

        void setElementPort(FlowElementOutput port) {
            assert port != null;
            this.output = port;
            this.originalConnections = Collections.emptySet();
        }

        void addConnection(Connection conn) {
            assert conn != null;
            connections.add(conn);
        }

        void disconnect() {
            for (Connection conn : Lists.from(connections)) {
                conn.disconnect();
            }
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}'{'owner=FlowBlock@{1}'}'",
                    getElementPort(),
                    String.valueOf(FlowBlock.this.hashCode()));
        }
    }

    /**
     * ブロック間の接続。
     */
    public static class Connection {

        private final FlowBlock.Output upstream;

        private final FlowBlock.Input downstream;

        /**
         * インスタンスを生成する。
         * @param upstream 上流の出力
         * @param downstream 下流の入力
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Connection(Output upstream, Input downstream) {
            Precondition.checkMustNotBeNull(upstream, "upstream"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(downstream, "downstream"); //$NON-NLS-1$
            this.upstream = upstream;
            this.downstream = downstream;
        }

        /**
         * 上流の出力を返す。
         * @return 上流の出力
         */
        public FlowBlock.Output getUpstream() {
            return upstream;
        }

        /**
         * 下流の入力を返す。
         * @return 下流の入力
         */
        public FlowBlock.Input getDownstream() {
            return downstream;
        }

        /**
         * この接続を解除する。
         */
        public void disconnect() {
            upstream.getConnections().remove(this);
            downstream.getConnections().remove(this);
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0} => {1}",
                    getUpstream(),
                    getDownstream());
        }
    }
}
