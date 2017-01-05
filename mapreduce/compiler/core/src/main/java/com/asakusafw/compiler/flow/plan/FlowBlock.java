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
package com.asakusafw.compiler.flow.plan;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
 * A sub-graph of {@link FlowGraph}.
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
     * Creates a new instance from boundary ports.
     * Note that, the created instance will not be {@link #detach() detached}.
     * @param serialNumber the serial number
     * @param source the original flow graph
     * @param inputs the input ports of this block
     * @param outputs the output ports of this block
     * @param elements the elements of this block
     * @return the created instance
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static FlowBlock fromPorts(
            int serialNumber,
            FlowGraph source,
            List<FlowElementInput> inputs,
            List<FlowElementOutput> outputs,
            Set<FlowElement> elements) {
        List<PortConnection> toInput = new ArrayList<>();
        List<PortConnection> fromOutput = new ArrayList<>();
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
            throw new IllegalArgumentException("Cannot merge map blocks and reduce blocks"); //$NON-NLS-1$
        }
        FlowBlock result = new FlowBlock(minSerialNumber, graph);
        for (FlowBlock block : blocks) {
            result.elements.addAll(block.elements);
            for (FlowBlock.Input origin : block.getBlockInputs()) {
                FlowBlock.Input mapped = result.new Input(origin.getElementPort(), Collections.emptySet());
                result.blockInputs.add(mapped);
                Maps.addToSet(inputMapping, origin, mapped);
            }
            for (FlowBlock.Output origin : block.getBlockOutputs()) {
                FlowBlock.Output mapped = result.new Output(origin.getElementPort(), Collections.emptySet());
                result.blockOutputs.add(mapped);
                Maps.addToSet(outputMapping, origin, mapped);
            }
        }
        return result;
    }

    /**
     * Creates a new instance.
     * Note that, the created instance will not be {@link #detach() detached}.
     * @param serialNumber the serial number
     * @param source the original flow graph
     * @param inputs the input connections of this block
     * @param outputs the output connections of this block
     * @param elements the elements of this block
     * @throws IllegalArgumentException if the parameters are {@code null}
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
            throw new IllegalArgumentException("inputs must not be shuffle bounds partially"); //$NON-NLS-1$
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
        this.blockInputs = new ArrayList<>();
        this.blockOutputs = new ArrayList<>();
        this.elements = new HashSet<>();
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
        Map<FlowElementInput, Set<PortConnection>> map = new LinkedHashMap<>();
        for (PortConnection input : inputs) {
            FlowElementInput port = input.getDownstream();
            Maps.addToSet(map, port, input);
        }
        List<FlowBlock.Input> results = new ArrayList<>();
        for (Map.Entry<FlowElementInput, Set<PortConnection>> entry : map.entrySet()) {
            results.add(new FlowBlock.Input(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    private List<FlowBlock.Output> toBlockOutputs(List<PortConnection> outputs) {
        assert outputs != null;
        Map<FlowElementOutput, Set<PortConnection>> map = new LinkedHashMap<>();
        for (PortConnection output : outputs) {
            FlowElementOutput port = output.getUpstream();
            Maps.addToSet(map, port, output);
        }
        List<FlowBlock.Output> results = new ArrayList<>();
        for (Map.Entry<FlowElementOutput, Set<PortConnection>> entry : map.entrySet()) {
            results.add(new FlowBlock.Output(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    /**
     * Returns the original flow graph.
     * @return the original flow graph
     */
    public FlowGraph getSource() {
        return source;
    }

    /**
     * Returns the serial number of this block.
     * @return the serial number
     */
    public int getSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the input ports of this block.
     * @return the input ports
     */
    public List<FlowBlock.Input> getBlockInputs() {
        return blockInputs;
    }

    /**
     * Returns the output ports of this block.
     * @return the output ports
     */
    public List<FlowBlock.Output> getBlockOutputs() {
        return blockOutputs;
    }

    /**
     * Returns the elements of this block.
     * @return the elements
     */
    public Set<FlowElement> getElements() {
        return elements;
    }

    /**
     * Returns whether the target input and output are connected or not.
     * @param upstream the upstream output
     * @param downstream the downstream input
     * @return {@code true} if the target inputs and output are connected, otherwise {@code false}
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static boolean isConnected(FlowBlock.Output upstream, FlowBlock.Input downstream) {
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
     * Connects between the target upstream output and downstream input.
     * @param upstream the upstream output
     * @param downstream the downstream input
     * @throws IllegalArgumentException if the parameters are {@code null}
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
     * Returns whether this block is empty or not.
     * @return {@code true} if this block is empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return blockInputs.isEmpty() && blockOutputs.isEmpty();
    }

    /**
     * Returns whether this block represents a reduce block or not.
     * @return {@code true} if this block represents a reduce block, otherwise {@code false}
     */
    public boolean isReduceBlock() {
        if (blockInputs.isEmpty()) {
            return false;
        }
        FlowBlock.Input first = blockInputs.get(0);
        return FlowGraphUtil.isShuffleBoundary(first.getElementPort().getOwner());
    }

    /**
     * Returns whether the succeeding blocks of this are reduce block or not.
     * Note that this always returns {@code true} if there are no succeeding blocks.
     * This method requires that this block has been {@link #detach() detached}.
     * @return {@code true} if the succeeding blocks of this are reduce block, otherwise {@code false}
     * @throws IllegalStateException if this block has not been {@link #detach() detached}
     */
    public boolean isSucceedingReduceBlock() {
        if (detached == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "{0} was not detached", //$NON-NLS-1$
                    this));
        }
        for (FlowBlock.Output output : blockOutputs) {
            for (FlowBlock.Connection conn : output.getConnections()) {
                FlowBlock successor = conn.getDownstream().getOwner();
                return successor.isReduceBlock();
            }
        }
        return false;
    }

    /**
     * Detaches this block from the original flow graph.
     * This operation will create a copy of this block, and the copy will be disconnected from other blocks.
     */
    public void detach() {
        if (detached) {
            return;
        }
        LOG.debug("detaching from {}: {}", getSource(), this); //$NON-NLS-1$
        Map<FlowElement, FlowElement> elementMapping = new HashMap<>();
        Map<FlowElementInput, FlowElementInput> inputMapping = new HashMap<>();
        Map<FlowElementOutput, FlowElementOutput> outputMapping = new HashMap<>();
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
        Map<FlowElement, FlowElement> elementMapping = new HashMap<>();
        Map<FlowElementInput, FlowElementInput> inputMapping = new HashMap<>();
        Map<FlowElementOutput, FlowElementOutput> outputMapping = new HashMap<>();
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

        LOG.debug("Unifying elements: {}", this); //$NON-NLS-1$

        Map<Object, FlowElement> unifier = new HashMap<>();
        Map<FlowElement, FlowElement> unifiedElements = new HashMap<>();
        Map<FlowElementInput, FlowElementInput> unifiedInputs = new HashMap<>();
        Map<FlowElementOutput, FlowElementOutput> unifiedOutputs = new HashMap<>();

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
                LOG.debug("Unify {} -> {}", dest, unified); //$NON-NLS-1$
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
        Map<FlowElementInput, FlowBlock.Input> map = new HashMap<>();
        for (Iterator<FlowBlock.Input> iter = blockInputs.iterator(); iter.hasNext();) {
            FlowBlock.Input blockPort = iter.next();
            FlowElementInput elementPort = inputMapping.get(blockPort.getElementPort());
            assert elementPort != null;
            FlowBlock.Input unified = map.get(elementPort);
            if (unified == null) {
                map.put(elementPort, blockPort);
                blockPort.setElementPort(elementPort);
            } else {
                LOG.debug("Input port {} will be unified", blockPort); //$NON-NLS-1$
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
        Map<FlowElementOutput, FlowBlock.Output> map = new HashMap<>();
        for (Iterator<FlowBlock.Output> iter = blockOutputs.iterator(); iter.hasNext();) {
            FlowBlock.Output blockPort = iter.next();
            FlowElementOutput elementPort = outputMapping.get(blockPort.getElementPort());
            assert elementPort != null;
            FlowBlock.Output unified = map.get(elementPort);
            if (unified == null) {
                map.put(elementPort, blockPort);
                blockPort.setElementPort(elementPort);
            } else {
                LOG.debug("Output port {} will be unified", blockPort); //$NON-NLS-1$
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
     * Removes unnecessary elements from this block.
     * This method requires that this block has been {@link #detach() detached}.
     * @return {@code true} if this block was modified, or otherwise {@code false}
     * @throws IllegalStateException this block has not been {@link #detach() detached}
     */
    public boolean compaction() {
        if (detached == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "{0} was not detached", //$NON-NLS-1$
                    this));
        }
        LOG.debug("Applying compaction: {}", this); //$NON-NLS-1$
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
        LOG.debug("Merging same block edges: {}", this); //$NON-NLS-1$

        Map<FlowElementInput, FlowBlock.Input> inputMapping = new HashMap<>();
        for (FlowBlock.Input port : blockInputs) {
            FlowBlock.Input prime = inputMapping.get(port.getElementPort());
            if (prime != null) {
                LOG.debug("Merging block input: {} on {}", port, this); //$NON-NLS-1$
                for (FlowBlock.Connection conn : port.getConnections()) {
                    FlowBlock.connect(conn.getUpstream(), prime);
                }
                port.disconnect();
                changed = true;
            } else {
                inputMapping.put(port.getElementPort(), port);
            }
        }

        Map<FlowElementOutput, FlowBlock.Output> outputMapping = new HashMap<>();
        for (FlowBlock.Output port : blockOutputs) {
            FlowBlock.Output prime = outputMapping.get(port.getElementPort());
            if (prime != null) {
                LOG.debug("Merging block output: {} on {}", port, this); //$NON-NLS-1$
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
        LOG.debug("Searching for disconnected block edges: {}", this); //$NON-NLS-1$

        // remove orphaned inputs
        Iterator<FlowBlock.Input> inputs = blockInputs.iterator();
        while (inputs.hasNext()) {
            FlowBlock.Input port = inputs.next();
            if (port.getConnections().isEmpty()) {
                LOG.debug("Deleting unnecessary block edge: {} on {}", port, this); //$NON-NLS-1$
                inputs.remove();
                changed = true;
            }
        }

        // remove orphaned outputs
        Iterator<FlowBlock.Output> outputs = blockOutputs.iterator();
        while (outputs.hasNext()) {
            FlowBlock.Output port = outputs.next();
            if (port.getConnections().isEmpty()) {
                LOG.debug("Deleting dead block edge: {} on {}", port, this); //$NON-NLS-1$
                outputs.remove();
                changed = true;
            }
        }
        return changed;
    }

    private boolean trimDeadElements() {
        boolean changed = false;
        LOG.debug("Searching for unnecessary operators: {}", this); //$NON-NLS-1$

        Set<FlowElement> blockEdge = collectBlockEdges();
        Set<FlowElement> removed = new HashSet<>();
        LinkedList<FlowElement> work = new LinkedList<>();
        work.addAll(elements);

        while (work.isEmpty() == false) {
            FlowElement element = work.removeFirst();

            // ignore already removed in this round
            if (removed.contains(element)) {
                continue;
            }

            // ignore edge elements
            if (blockEdge.contains(element)) {
                continue;
            }

            if (FlowGraphUtil.isAlwaysEmpty(element)) {
                LOG.debug("Deleting operator without input: {} on {}", element, this); //$NON-NLS-1$
                work.addAll(FlowGraphUtil.getSuccessors(element));
                remove(element);
                removed.add(element);
                changed = true;
            } else if (FlowGraphUtil.isAlwaysStop(element)
                    && FlowGraphUtil.hasMandatorySideEffect(element) == false) {
                LOG.debug("Deleting operator without output: {} on {}", element, this); //$NON-NLS-1$
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
        LOG.debug("Searching for unnecessary block edges: {}", this); //$NON-NLS-1$

        Set<FlowElement> inputElements = new HashSet<>();
        Set<FlowElement> outputElements = new HashSet<>();
        for (FlowBlock.Output output : blockOutputs) {
            outputElements.add(output.getElementPort().getOwner());
        }

        // remove unused inputs
        Iterator<FlowBlock.Input> inputs = blockInputs.iterator();
        while (inputs.hasNext()) {
            FlowBlock.Input port = inputs.next();
            FlowElement element = port.getElementPort().getOwner();
            if (FlowGraphUtil.hasSuccessors(element) == false
                    && FlowGraphUtil.hasMandatorySideEffect(element) == false
                    && outputElements.contains(element) == false) {
                LOG.debug("Deleting unnecessary input: {} on {}", port, this); //$NON-NLS-1$
                port.disconnect();
                inputs.remove();
                changed = true;
            } else {
                inputElements.add(element);
            }
        }

        // remove unused outputs
        Iterator<FlowBlock.Output> outputs = blockOutputs.iterator();
        while (outputs.hasNext()) {
            FlowBlock.Output port = outputs.next();
            FlowElement element = port.getElementPort().getOwner();
            if (FlowGraphUtil.hasPredecessors(element) == false
                    && inputElements.contains(element) == false) {
                LOG.debug("Deleting unnecessary output: {} on {}", port, this); //$NON-NLS-1$
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
        Map<FlowBlock.Input, List<FlowBlock.Output>> targets =  new HashMap<>();
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
                list = new ArrayList<>();
                targets.put(opposite, list);
            } else {
                foundTarget = true;
            }
            list.add(output);
        }
        if (foundTarget == false) {
            return changed;
        }
        Map<FlowElementInput, FlowBlock.Input> inputs = new HashMap<>();
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
                LOG.debug("Unifying pseud element: {} -> {}", otherElement, primaryElement); //$NON-NLS-1$

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
     * Removes unused elements.
     * @return {@code true} if this operation removed at least one element
     */
    public boolean collectGarbages() {
        LOG.debug("removing dead operators: {}", this); //$NON-NLS-1$
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
        Set<FlowElement> blockEdge = new HashSet<>();
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
                "FlowBlock[{1}]({0}) - {2}..", //$NON-NLS-1$
                String.valueOf(serialNumber),
                isReduceBlock() ? "R" : "M", //$NON-NLS-1$ //$NON-NLS-2$
                getBlockInputs().isEmpty() ? "?" : getBlockInputs().get(0)); //$NON-NLS-1$
    }

    /**
     * Represents an input port of {@link FlowBlock}.
     */
    public class Input {

        private FlowElementInput input;

        private final List<Connection> connections;

        private Set<PortConnection> originalConnections;

        /**
         * Creates a new instance.
         * @param input the corresponding element port
         * @param originalConnections the original connections for this port
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Input(FlowElementInput input, Set<PortConnection> originalConnections) {
            Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
            this.input = input;
            this.connections = new ArrayList<>();
            this.originalConnections = originalConnections;
        }

        /**
         * Returns the original connections for this port.
         * @return the original connections for this port
         */
        public Set<PortConnection> getOriginalConnections() {
            return originalConnections;
        }

        /**
         * Returns the owner of this port.
         * @return the owner
         */
        public FlowBlock getOwner() {
            return FlowBlock.this;
        }

        /**
         * Returns the corresponding element port.
         * @return the corresponding element port
         */
        public FlowElementInput getElementPort() {
            return this.input;
        }

        /**
         * Returns the connections for other ports.
         * @return the connections
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
                    "{0}'{'owner=FlowBlock@{1}'}'", //$NON-NLS-1$
                    getElementPort(),
                    String.valueOf(FlowBlock.this.hashCode()));
        }
    }

    /**
     * Represents an output port of {@link FlowBlock}.
     */
    public class Output {

        private FlowElementOutput output;

        private final List<Connection> connections;

        private Set<PortConnection> originalConnections;

        /**
         * Creates a new instance.
         * @param output the corresponding element port
         * @param originalConnections the original connections for this port
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Output(FlowElementOutput output, Set<PortConnection> originalConnections) {
            Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
            this.output = output;
            this.connections = new ArrayList<>();
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
         * Returns the original connections for this port.
         * @return the original connections for this port
         */
        public Set<PortConnection> getOriginalConnections() {
            return originalConnections;
        }

        /**
         * Returns the owner of this port.
         * @return the owner
         */
        public FlowBlock getOwner() {
            return FlowBlock.this;
        }

        /**
         * Returns the corresponding element port.
         * @return the corresponding element port
         */
        public FlowElementOutput getElementPort() {
            return this.output;
        }

        /**
         * Returns the connections of this port.
         * @return the connections of this port
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
                    "{0}'{'owner=FlowBlock@{1}'}'", //$NON-NLS-1$
                    getElementPort(),
                    String.valueOf(FlowBlock.this.hashCode()));
        }
    }

    /**
     * Represents connections between {@link Input} and {@link Output}.
     */
    public static class Connection {

        private final FlowBlock.Output upstream;

        private final FlowBlock.Input downstream;

        /**
         * Creates a new instance.
         * @param upstream the upstream output port
         * @param downstream the downstream input port
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Connection(Output upstream, Input downstream) {
            Precondition.checkMustNotBeNull(upstream, "upstream"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(downstream, "downstream"); //$NON-NLS-1$
            this.upstream = upstream;
            this.downstream = downstream;
        }

        /**
         * Returns the upstream output port.
         * @return the upstream output port
         */
        public FlowBlock.Output getUpstream() {
            return upstream;
        }

        /**
         * Returns the downstream input port.
         * @return the downstream input port
         */
        public FlowBlock.Input getDownstream() {
            return downstream;
        }

        /**
         * Disposes this connection.
         */
        public void disconnect() {
            upstream.getConnections().remove(this);
            downstream.getConnections().remove(this);
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0} => {1}", //$NON-NLS-1$
                    getUpstream(),
                    getDownstream());
        }
    }
}
