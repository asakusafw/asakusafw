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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;

/**
 * A sub-graph of flow graph that represents a MapReduce stage.
 */
public class StageBlock {

    static final Logger LOG = LoggerFactory.getLogger(StageBlock.class);

    private static final int NOT_SET = -1;

    private final Set<FlowBlock> mapBlocks;

    private final Set<FlowBlock> reduceBlocks;

    private int stageNumber = NOT_SET;

    /**
     * Creates a new instance.
     * @param mapBlocks the Map blocks
     * @param reduceBlocks the Reduce blocks
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public StageBlock(Set<FlowBlock> mapBlocks, Set<FlowBlock> reduceBlocks) {
        Precondition.checkMustNotBeNull(mapBlocks, "mapBlocks"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(reduceBlocks, "reduceBlocks"); //$NON-NLS-1$
        this.mapBlocks = Sets.from(mapBlocks);
        this.reduceBlocks = Sets.from(reduceBlocks);
    }

    /**
     * Returns the serial number of this stage.
     * @return the stage number
     * @throws IllegalStateException if the stage number has been not set
     * @see #setStageNumber(int)
     */
    public int getStageNumber() {
        if (stageNumber == NOT_SET) {
            throw new IllegalStateException();
        }
        return stageNumber;
    }

    /**
     * Sets the serial number of this stage.
     * @param stageNumber the stage number
     */
    public void setStageNumber(int stageNumber) {
        if (stageNumber == NOT_SET) {
            throw new IllegalArgumentException();
        }
        LOG.debug("applying stage number {}: {}", stageNumber, this); //$NON-NLS-1$
        this.stageNumber = stageNumber;
    }

    /**
     * Returns the map blocks in this stage.
     * @return the map blocks
     */
    public Set<FlowBlock> getMapBlocks() {
        return mapBlocks;
    }

    /**
     * Returns the reduce blocks in this stage.
     * @return the reduce blocks
     */
    public Set<FlowBlock> getReduceBlocks() {
        return reduceBlocks;
    }

    /**
     * Returns whether this stage block contains one or more reduce blocks or not.
     * @return {@code true} if this stage block contains one or more reduce blocks, or otherwise {@code false}
     */
    public boolean hasReduceBlocks() {
        return reduceBlocks.isEmpty() == false;
    }

    /**
     * Returns whether this stage block is an empty block or not.
     * @return {@code true} if this stage block is an empty block, or otherwise {@code false}
     */
    public boolean isEmpty() {
        if (reduceBlocks.isEmpty() == false) {
            return false;
        }
        for (FlowBlock block : mapBlocks) {
            if (block.isEmpty() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the redundant blocks from this stage block.
     * @return {@code true} if one or more blocks are actually removed, otherwise {@code false}
     */
    public boolean compaction() {
        LOG.debug("applying compaction: {}", this); //$NON-NLS-1$
        boolean changed = false;
        if (reduceBlocks.isEmpty() == false) {
            return changed;
        }
        for (Iterator<FlowBlock> iter = mapBlocks.iterator(); iter.hasNext();) {
            FlowBlock block = iter.next();
            boolean localChanged = false;
            localChanged |= bypass(block);
            changed |= localChanged;
            if (localChanged) {
                changed |= block.compaction();
            }
            if (block.isEmpty()) {
                LOG.debug("removed empty block {}: {}", block, this); //$NON-NLS-1$
                iter.remove();
                changed = true;
            }
        }
        return changed;
    }

    private boolean bypass(FlowBlock block) {
        assert block != null;
        // create mapping: FlowElementOutput -> FlowBlockOutput
        Map<FlowElementOutput, FlowBlock.Output> outputs = new HashMap<>();
        for (FlowBlock.Output blockOutput : block.getBlockOutputs()) {
            outputs.put(blockOutput.getElementPort(), blockOutput);
        }

        boolean changed = false;
        for (FlowBlock.Input blockInput : block.getBlockInputs()) {
            FlowElement element = blockInput.getElementPort().getOwner();
            if (FlowGraphUtil.isIdentity(element) == false) {
                continue;
            }
            FlowElementOutput output = element.getOutputPorts().get(0);
            FlowBlock.Output blockOutput = outputs.get(output);
            if (blockOutput == null) {
                continue;
            }

            // bypass (input -> identity-operator+ -> output)
            LOG.debug("reducing identity path: {} -> {}", blockInput, blockOutput); //$NON-NLS-1$
            bypass(blockInput, blockOutput);
            changed = true;
        }
        return changed;
    }

    private void bypass(FlowBlock.Input input, FlowBlock.Output output) {
        assert input != null;
        assert output != null;
        List<FlowBlock.Output> upstreams = new ArrayList<>();
        List<FlowBlock.Connection> inConns = Lists.from(input.getConnections());
        for (FlowBlock.Connection conn : inConns) {
            upstreams.add(conn.getUpstream());
            conn.disconnect();
        }
        List<FlowBlock.Input> downstreams = new ArrayList<>();
        List<FlowBlock.Connection> outConns = Lists.from(output.getConnections());
        for (FlowBlock.Connection conn : outConns) {
            downstreams.add(conn.getDownstream());
            conn.disconnect();
        }
        for (FlowBlock.Output upstream : upstreams) {
            for (FlowBlock.Input downstream : downstreams) {
                FlowBlock.connect(upstream, downstream);
            }
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "StageBlock(id={0}, maps={1}, reduces={2})", //$NON-NLS-1$
                stageNumber == NOT_SET
                        ? '@' + String.valueOf(hashCode())
                        : String.valueOf(stageNumber),
                String.valueOf(mapBlocks.size()),
                String.valueOf(reduceBlocks.size()));
    }
}
