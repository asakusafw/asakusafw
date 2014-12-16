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
import java.util.Iterator;
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
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;

/**
 * それぞれのステージに含まれるプログラムを表現するブロック。
 */
public class StageBlock {

    static final Logger LOG = LoggerFactory.getLogger(StageBlock.class);

    private static final int NOT_SET = -1;

    private final Set<FlowBlock> mapBlocks;

    private final Set<FlowBlock> reduceBlocks;

    private int stageNumber = NOT_SET;

    /**
     * インスタンスを生成する。
     * @param mapBlocks マップブロックの一覧
     * @param reduceBlocks レデュースブロックの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StageBlock(Set<FlowBlock> mapBlocks, Set<FlowBlock> reduceBlocks) {
        Precondition.checkMustNotBeNull(mapBlocks, "mapBlocks"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(reduceBlocks, "reduceBlocks"); //$NON-NLS-1$
        this.mapBlocks = Sets.from(mapBlocks);
        this.reduceBlocks = Sets.from(reduceBlocks);
    }

    /**
     * このステージのステージ番号を返す。
     * @return ステージ番号
     * @throws IllegalStateException ステージ番号が未設定の場合
     */
    public int getStageNumber() {
        if (stageNumber == NOT_SET) {
            throw new IllegalStateException();
        }
        return stageNumber;
    }

    /**
     * このステージのステージ番号を設定する。
     * @param stageNumber ステージ番号
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void setStageNumber(int stageNumber) {
        if (stageNumber == NOT_SET) {
            throw new IllegalArgumentException();
        }
        LOG.debug("applying stage number {}: {}", stageNumber, this); //$NON-NLS-1$
        this.stageNumber = stageNumber;
    }

    /**
     * マップブロックの一覧を返す。
     * @return マップブロックの一覧
     */
    public Set<FlowBlock> getMapBlocks() {
        return mapBlocks;
    }

    /**
     * レデュースブロックの一覧を返す。
     * @return レデュースブロックの一覧
     */
    public Set<FlowBlock> getReduceBlocks() {
        return reduceBlocks;
    }

    /**
     * このステージにレデュースブロックがひとつでも存在する場合に{@code true}を返す。
     * @return レデュースブロックがひとつでも存在する場合に{@code true}
     */
    public boolean hasReduceBlocks() {
        return reduceBlocks.isEmpty() == false;
    }

    /**
     * このブロックが空のブロックである場合にのみ{@code true}を返す。
     * @return 空のブロックである場合にのみ{@code true}
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
     * このステージブロックに含まれる余計なブロックを削除する。
     * <p>
     * これは次の手順で行われる。
     * </p>
     * <ul>
     * <li>
     *   それぞれのブロックに、入力が直接出力されているようなパスが含まれる場合、
     *   前後のブロックの入出力を直接接続して、このブロックでの処理を行わないようにする。
     * </li>
     * <li>
     *   上記の処理が行われた場合、さらにブロックから不要な入出力を除去する。
     * </li>
     * <li>
     *   さらに、入出力の除去によってブロックに入出力が存在しなくなった場合、
     *   ブロックそのものをステージブロックから削除する。
     * </li>
     * </ul>
     * @return 一つでも変更があった場合のみ{@code true}
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
        // FlowElementOutput -> FlowBlockOutput の逆参照表を作成
        Map<FlowElementOutput, FlowBlock.Output> outputs = Maps.create();
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
        List<FlowBlock.Output> upstreams = Lists.create();
        List<FlowBlock.Connection> inConns = Lists.from(input.getConnections());
        for (FlowBlock.Connection conn : inConns) {
            upstreams.add(conn.getUpstream());
            conn.disconnect();
        }
        List<FlowBlock.Input> downstreams = Lists.create();
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
