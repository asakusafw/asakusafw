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
package com.asakusafw.compiler.flow.plan;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.PortConnection;


/**
 * フロー上のパス。
 */
public class FlowPath {

    private Direction direction;

    private Set<FlowElement> startings;

    private Set<FlowElement> passings;

    private Set<FlowElement> arrivals;

    /**
     * インスタンスを生成する。
     * @param direction パスの方向
     * @param startings 開始要素の一覧
     * @param passings 途中要素の一覧
     * @param arrivals 終了要素の一覧
     */
    public FlowPath(
            Direction direction,
            Set<FlowElement> startings,
            Set<FlowElement> passings,
            Set<FlowElement> arrivals) {
        Precondition.checkMustNotBeNull(direction, "direction"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(startings, "startings"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(passings, "passings"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arrivals, "arrivals"); //$NON-NLS-1$
        this.direction = direction;
        this.startings = Collections.unmodifiableSet(new HashSet<FlowElement>(startings));
        this.passings = Collections.unmodifiableSet(new HashSet<FlowElement>(passings));
        this.arrivals = Collections.unmodifiableSet(new HashSet<FlowElement>(arrivals));
    }

    /**
     * パスの方向を返す。
     * @return パスの方向
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * 開始要素の一覧を返す。
     * @return 開始要素の一覧
     */
    public Set<FlowElement> getStartings() {
        return this.startings;
    }

    /**
     * 途中要素の一覧を返す。
     * @return 途中要素の一覧
     */
    public Set<FlowElement> getPassings() {
        return this.passings;
    }

    /**
     * 終了要素の一覧を返す。
     * @return 終了要素の一覧
     */
    public Set<FlowElement> getArrivals() {
        return this.arrivals;
    }

    /**
     * このパスの情報を元にブロックを生成する。
     * @param graph ブロックの元になるグラフ
     * @param blockSequence ブロック番号
     * @param includeStartings {@code true}ならばブロックに開始要素を含める
     * @param includeArrivals {@code true}ならばブロックに終端要素を含める
     * @return 生成したブロック
     * @throws IllegalArgumentException 開始要素と終端要素を含めない状態で
     *     かつ途中要素が存在しない場合、または引数に{@code null}が含まれる場合
     */
    public FlowBlock createBlock(
            FlowGraph graph,
            int blockSequence,
            boolean includeStartings,
            boolean includeArrivals) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        if (direction != Direction.FORWARD) {
            throw new IllegalStateException("direction must be FORWARD");
        }
        if (includeStartings == false
                && includeArrivals == false
                && passings.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Set<FlowElement> elements = createBlockElements(includeStartings, includeArrivals);
        List<PortConnection> inputs = createBlockInputs(includeStartings);
        List<PortConnection> outputs = createBlockOutputs(includeArrivals);
        return new FlowBlock(blockSequence, graph, inputs, outputs, elements);
    }

    private List<PortConnection> createBlockInputs(boolean includeStartings) {
        List<PortConnection> results = new ArrayList<PortConnection>();
        if (includeStartings) {
            for (FlowElement element : startings) {
                for (FlowElementInput input : element.getInputPorts()) {
                    results.addAll(input.getConnected());
                }
            }
        } else {
            for (FlowElement element : startings) {
                for (FlowElementOutput output : element.getOutputPorts()) {
                    for (PortConnection conn : output.getConnected()) {
                        FlowElement target = conn.getDownstream().getOwner();
                        if (passings.contains(target) || arrivals.contains(target)) {
                            results.add(conn);
                        }
                    }
                }
            }
        }
        return results;
    }

    private List<PortConnection> createBlockOutputs(boolean includeArrivals) {
        List<PortConnection> results = new ArrayList<PortConnection>();
        if (includeArrivals) {
            for (FlowElement element : arrivals) {
                for (FlowElementOutput output : element.getOutputPorts()) {
                    results.addAll(output.getConnected());
                }
            }
        } else {
            for (FlowElement element : arrivals) {
                for (FlowElementInput input : element.getInputPorts()) {
                    for (PortConnection conn : input.getConnected()) {
                        FlowElement target = conn.getUpstream().getOwner();
                        if (passings.contains(target) || startings.contains(target)) {
                            results.add(conn);
                        }
                    }
                }
            }
        }
        return results;
    }

    private Set<FlowElement> createBlockElements(boolean includeStartings,
            boolean includeArrivals) {
        Set<FlowElement> elements = new HashSet<FlowElement>();
        elements.addAll(passings);
        if (includeStartings) {
            elements.addAll(startings);
        }
        if (includeArrivals) {
            elements.addAll(arrivals);
        }
        return elements;
    }

    /**
     * このパスと指定のパスを同時に含むパスを新しく作成して返す。
     * @param other 対象のパス
     * @return 作成したパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowPath union(FlowPath other) {
        Precondition.checkMustNotBeNull(other, "other"); //$NON-NLS-1$
        if (this.direction != other.direction) {
            throw new IllegalArgumentException("other must have same direction"); //$NON-NLS-1$
        }
        Set<FlowElement> newStartings = new HashSet<FlowElement>(startings);
        newStartings.addAll(other.startings);

        Set<FlowElement> newPassings = new HashSet<FlowElement>(passings);
        newPassings.addAll(other.passings);

        Set<FlowElement> newArrivals = new HashSet<FlowElement>(arrivals);
        newArrivals.addAll(other.arrivals);

        return new FlowPath(
                direction,
                newStartings,
                newPassings,
                newArrivals);
    }

    /**
     * 指定のパスの向きを逆にしたものと、このパスに共通する要素のみを含むパスを新しく作成して返す。
     * @param other 対象のパス
     * @return 作成したパス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public FlowPath transposeIntersect(FlowPath other) {
        Precondition.checkMustNotBeNull(other, "other"); //$NON-NLS-1$
        if (this.direction == other.direction) {
            throw new IllegalArgumentException("other must have different direction"); //$NON-NLS-1$
        }
        Set<FlowElement> newStartings = new HashSet<FlowElement>(startings);
        newStartings.retainAll(other.arrivals);

        Set<FlowElement> newPassings = new HashSet<FlowElement>(passings);
        newPassings.retainAll(other.passings);

        Set<FlowElement> newArrivals = new HashSet<FlowElement>(arrivals);
        newArrivals.retainAll(other.startings);

        return new FlowPath(
                direction,
                newStartings,
                newPassings,
                newArrivals);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}: {1}->{2}",
                direction,
                startings,
                arrivals);
    }

    /**
     * パスの方向。
     */
    public enum Direction {

        /**
         * 開始位置に後続する方向。
         */
        FORWARD,

        /**
         * 開始位置に先行する方向。
         */
        BACKWORD,
    }
}
