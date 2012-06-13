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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.flow.util.PseudElementDescription;

/**
 * {@link FlowGraph}に関するユーティリティ。
 */
public final class FlowGraphUtil {

    /**
     * 対象のグラフに含まれ、かついずれかの入力または出力に結線された全ての要素を返す。
     * <p>
     * 返される結果には、フローの入出力も含まれる。
     * </p>
     * @param graph 対象のグラフ
     * @return グラフに含まれるすべての要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Set<FlowElement> collectElements(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Set<FlowElement> elements = new HashSet<FlowElement>();
        for (FlowIn<?> in : graph.getFlowInputs()) {
            elements.add(in.getFlowElement());
        }
        for (FlowOut<?> out : graph.getFlowOutputs()) {
            elements.add(out.getFlowElement());
        }
        collect(elements);
        return elements;
    }

    /**
     * 対象のグラフに含まれる全てのフロー部品を表す要素を返す。
     * @param graph 対象のグラフ
     * @return グラフに含まれるすべてのフロー部品
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Set<FlowElement> collectFlowParts(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<FlowElement>();
        for (FlowElement element : collectElements(graph)) {
            FlowElementDescription description = element.getDescription();
            if (description.getKind() == FlowElementKind.FLOW_COMPONENT) {
                results.add(element);
            }
        }
        return results;
    }

    /**
     * 対象のグラフに含まれる全てのフロー境界を表す要素を返す。
     * @param graph 対象のグラフ
     * @return グラフに含まれるすべてのフロー境界
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Set<FlowElement> collectBoundaries(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<FlowElement>();
        for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
            if (FlowGraphUtil.isBoundary(element)) {
                results.add(element);
            }
        }
        return results;
    }

    /**
     * 対象のグラフを要素間の結線関係を表す有効グラフに変換して返す。
     * @param graph 対象のグラフ
     * @return 要素間の結線関係を表す有効グラフ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Graph<FlowElement> toElementGraph(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Graph<FlowElement> results = Graphs.newInstance();
        for (FlowElement source : FlowGraphUtil.collectElements(graph)) {
            results.addEdges(source, FlowGraphUtil.getSuccessors(source));
        }
        return results;
    }

    /**
     * 対象のグラフの内容を複製し、新しいインスタンスとして返す。
     * <p>
     * 対象のグラフにネストしたグラフが含まれる場合、それらも再帰的にコピーの対象となる。
     * </p>
     * @param graph 複製するグラフ
     * @return 複製結果のグラフ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static FlowGraph deepCopy(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$

        Map<FlowElement, FlowElement> elemMapping = new HashMap<FlowElement, FlowElement>();

        // 入出力のコピー
        List<FlowIn<?>> flowInputs = new ArrayList<FlowIn<?>>();
        for (FlowIn<?> orig : graph.getFlowInputs()) {
            FlowIn<?> copy = FlowIn.newInstance(orig.getDescription());
            elemMapping.put(orig.getFlowElement(), copy.getFlowElement());
            flowInputs.add(copy);
        }
        List<FlowOut<?>> flowOutputs = new ArrayList<FlowOut<?>>();
        for (FlowOut<?> orig : graph.getFlowOutputs()) {
            FlowOut<?> copy = FlowOut.newInstance(orig.getDescription());
            elemMapping.put(orig.getFlowElement(), copy.getFlowElement());
            flowOutputs.add(copy);
        }
        deepCopy(
                collectElements(graph),
                elemMapping,
                new HashMap<FlowElementInput, FlowElementInput>(),
                new HashMap<FlowElementOutput, FlowElementOutput>());

        FlowGraph copy = new FlowGraph(graph.getDescription(), flowInputs, flowOutputs);
        copy.setOrigin(graph);
        return copy;
    }

    /**
     * 対象のフロー要素一覧のコピーを作成し、対応表に追加する。
     * <p>
     * 対象のグラフにネストしたグラフが含まれる場合、それらも再帰的に複製の対象となる。
     * </p>
     * @param elements コピーする要素の一覧
     * @param elementMapping 対応表、すでに対応付けが与えられているものに関してはコピーを新たに作成しない
     * @param inputMapping 入力の対応表
     * @param outputMapping 出力の対応表
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void deepCopy(
            Set<FlowElement> elements,
            Map<FlowElement, FlowElement> elementMapping,
            Map<FlowElementInput, FlowElementInput> inputMapping,
            Map<FlowElementOutput, FlowElementOutput> outputMapping) {
        Precondition.checkMustNotBeNull(elements, "elements"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(elementMapping, "elementMapping"); //$NON-NLS-1$
        // 全てのポートをコピー前後で対応付け
        for (FlowElement orig : elements) {
            FlowElement copy = createMapping(elementMapping, orig);
            addMapping(inputMapping, orig.getInputPorts(), copy.getInputPorts());
            addMapping(outputMapping, orig.getOutputPorts(), copy.getOutputPorts());
        }

        // ポート接続のコピー
        for (Map.Entry<FlowElementInput, FlowElementInput> entry : inputMapping.entrySet()) {
            FlowElementInput origIn = entry.getKey();
            FlowElementInput copyIn = entry.getValue();
            for (FlowElementOutput origOut : origIn.getOpposites()) {
                if (elements.contains(origOut.getOwner()) == false) {
                    continue;
                }
                FlowElementOutput copyOut = outputMapping.get(origOut);
                assert copyOut != null;
                PortConnection.connect(copyOut, copyIn);
            }
        }
    }

    private static FlowElement createMapping(
            Map<FlowElement, FlowElement> elemMapping,
            FlowElement orig) {
        assert elemMapping != null;
        assert orig != null;
        FlowElement mapped = elemMapping.get(orig);
        if (mapped != null) {
            return mapped;
        }
        FlowElementDescription description = orig.getDescription();
        if (description.getKind() == FlowElementKind.FLOW_COMPONENT) {
            FlowPartDescription fcd = (FlowPartDescription) description;
            FlowGraph subgraph = deepCopy(fcd.getFlowGraph());
            description = new FlowPartDescription(subgraph);
        }
        FlowElement copy = new FlowElement(description, orig.getAttributeOverride());
        elemMapping.put(orig, copy);
        return copy;
    }

    private static <T> void addMapping(
            Map<T, T> mapping,
            List<T> source,
            List<T> target) {
        assert mapping != null;
        assert source != null;
        assert target != null;
        assert source.size() == target.size();
        Iterator<T> sIter = source.iterator();
        Iterator<T> tIter = target.iterator();
        while (sIter.hasNext()) {
            assert tIter.hasNext();
            mapping.put(sIter.next(), tIter.next());
        }
        assert tIter.hasNext() == false;
    }

    private static void collect(Set<FlowElement> collected) {
        assert collected != null;
        LinkedList<FlowElement> work = new LinkedList<FlowElement>(collected);
        while (work.isEmpty() == false) {
            FlowElement first = work.removeFirst();
            if (collected.contains(first) == false) {
                collected.add(first);
            }
            for (FlowElement pred : FlowGraphUtil.getPredecessors(first)) {
                if (collected.contains(pred) == false) {
                    work.add(pred);
                }
            }
            for (FlowElement succ : FlowGraphUtil.getSuccessors(first)) {
                if (collected.contains(succ) == false) {
                    work.add(succ);
                }
            }
        }
    }

    /**
     * 指定の要素が必須の副作用を有する場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return 必須の副作用を有する場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean hasMandatorySideEffect(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        ObservationCount count = element.getAttribute(ObservationCount.class);
        if (count == null) {
            return false;
        }
        return count.atLeastOnce;
    }

    /**
     * 指定の要素が広域の副作用を有する場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return 広域の副作用を有する場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean hasGlobalSideEffect(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        ObservationCount count = element.getAttribute(ObservationCount.class);
        if (count == null) {
            return false;
        }
        return count.atMostOnce;
    }

    /**
     * 指定の要素にひとつ以上の入力が存在し、
     * かつすべての入力が他のいずれの要素にも結線されていない場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return 常に何も入力されない場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isAlwaysEmpty(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        List<FlowElementInput> ports = element.getInputPorts();
        if (ports.isEmpty()) {
            return false;
        }
        for (FlowElementInput input : ports) {
            if (input.getConnected().isEmpty() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定の要素にひとつ以上の出力が存在し、
     * かつすべての出力が他のいずれの要素にも結線されていない場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return 常に何も出力しない場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isAlwaysStop(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        List<FlowElementOutput> ports = element.getOutputPorts();
        if (ports.isEmpty()) {
            return false;
        }
        for (FlowElementOutput output : ports) {
            if (output.getConnected().isEmpty() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定の要素がフロー境界の属性を持っておらず、
     * かつ1入力1出力の恒等関数を表現する場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return フロー境界でなく、かつ恒等関数である場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isIdentity(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        FlowElementDescription description = element.getDescription();
        return isBoundary(element) == false
                && description.getKind() == FlowElementKind.PSEUD
                && element.getInputPorts().size() == 1
                && element.getOutputPorts().size() == 1;
    }

    /**
     * 指定の恒等関数を表す要素が、複数の入力や複数の出力を取る場合に、
     * それら入出力の組合せの個数の恒等関数にする。
     * <p>
     * 指定の恒等関数が一つの入力を取り、かつ一つの出力を取る場合には何も行わない。
     * </p>
     * @param element 対象の要素
     * @return 実際に分割した場合のみ{@code true}
     * @throws IllegalArgumentException 引数に恒等関数を取らない場合、または引数に{@code null}が指定された場合
     */
    public static boolean splitIdentity(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        if (isIdentity(element) == false) {
            throw new IllegalArgumentException("element must be identity operator"); //$NON-NLS-1$
        }
        assert element.getInputPorts().size() == 1;
        assert element.getOutputPorts().size() == 1;
        FlowElementInput input = element.getInputPorts().get(0);
        FlowElementOutput output = element.getOutputPorts().get(0);
        Set<PortConnection> sources = new HashSet<PortConnection>(input.getConnected());
        Set<PortConnection> targets = new HashSet<PortConnection>(output.getConnected());
        if (sources.size() <= 1 && targets.size() <= 1) {
            return false;
        } else {
            for (PortConnection source : sources) {
                FlowElementOutput upstream = source.getUpstream();
                for (PortConnection target : targets) {
                    FlowElementInput downstream = target.getDownstream();
                    connectWithIdentity(upstream, downstream);
                }
            }
            disconnect(element);
            return true;
        }
    }

    /**
     * 指定のポートに停止演算子を接続する。
     * @param output 停止演算子を接続するポート
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void stop(FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        FlowElementDescription desc = new PseudElementDescription(
                "implicit-stop",
                output.getDescription().getDataType(),
                true,
                false,
                FlowBoundary.STAGE);
        FlowElementResolver resolver = new FlowElementResolver(desc);
        FlowElementInput stopIn = resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
        PortConnection.connect(output, stopIn);
    }

    /**
     * 指定の要素のすべての入力された内容を、後続する要素の入力に接続しなおしたのち、
     * この要素を削除する。
     * <p>
     *
     * </p>
     * @param element 対象の要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void skip(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        List<FlowElementOutput> sources = new ArrayList<FlowElementOutput>();
        for (FlowElementInput input : element.getInputPorts()) {
            sources.addAll(input.disconnectAll());
        }
        List<FlowElementInput> targets = new ArrayList<FlowElementInput>();
        for (FlowElementOutput output : element.getOutputPorts()) {
            targets.addAll(output.disconnectAll());
        }
        for (FlowElementOutput upstream : sources) {
            for (FlowElementInput downstream : targets) {
                PortConnection.connect(upstream, downstream);
            }
        }
    }

    /**
     * 指定の要素がフロー境界要素を表現する場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return フロー境界要素を表現する場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isBoundary(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return isStageBoundary(element) || isShuffleBoundary(element);
    }

    /**
     * 対象の要素がシャッフル境界要素を表現する場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return シャッフル境界要素を表現する場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isShuffleBoundary(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return element.getAttribute(FlowBoundary.class) == FlowBoundary.SHUFFLE;
    }

    /**
     * 対象の要素がステージ境界要素を表現する場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return ステージ境界要素を表現する場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isStageBoundary(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return element.getAttribute(FlowBoundary.class) == FlowBoundary.STAGE;
    }

    /**
     * 対象の要素がステージ境界の疑似要素を表現する場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return ステージ境界の疑似要素を表現する場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isStagePadding(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return isStageBoundary(element) && element.getDescription().getKind() == FlowElementKind.PSEUD;
    }

    /**
     * 指定の要素から、直接後続する全ての境界までのパスを作成して返す。
     * @param element 対象の要素
     * @return フロー順方向のパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static FlowPath getSucceedBoundaryPath(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> startings = new HashSet<FlowElement>();
        startings.add(element);
        return getSuccessBoundaryPath(startings);
    }

    private static FlowPath getSuccessBoundaryPath(Set<FlowElement> startings) {
        assert startings != null;
        Set<FlowElement> passings = new HashSet<FlowElement>();
        Set<FlowElement> arrivals = new HashSet<FlowElement>();
        Set<FlowElement> saw = new HashSet<FlowElement>();

        LinkedList<FlowElement> successors = new LinkedList<FlowElement>();
        for (FlowElement starting : startings) {
            addSuccessors(successors, starting);
        }
        while (successors.isEmpty() == false) {
            FlowElement successor = successors.removeFirst();
            if (saw.contains(successor)) {
                continue;
            }
            saw.add(successor);

            if (isBoundary(successor)) {
                arrivals.add(successor);
            } else {
                passings.add(successor);
                addSuccessors(successors, successor);
            }
        }
        return new FlowPath(
                FlowPath.Direction.FORWARD,
                startings,
                passings,
                arrivals);
    }

    /**
     * 指定の要素から、直接先行する全ての境界までのパスを作成して返す。
     * @param element 対象の要素
     * @return フロー逆方向のパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static FlowPath getPredeceaseBoundaryPath(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> startings = new HashSet<FlowElement>();
        startings.add(element);

        return getPredeceaseBoundaryPath(startings);
    }

    private static FlowPath getPredeceaseBoundaryPath(Set<FlowElement> startings) {
        assert startings != null;
        Set<FlowElement> passings = new HashSet<FlowElement>();
        Set<FlowElement> arrivals = new HashSet<FlowElement>();
        Set<FlowElement> saw = new HashSet<FlowElement>();

        LinkedList<FlowElement> predecessors = new LinkedList<FlowElement>();
        for (FlowElement starting : startings) {
            addPredecessors(predecessors, starting);
        }
        while (predecessors.isEmpty() == false) {
            FlowElement predecessor = predecessors.removeFirst();
            if (saw.contains(predecessor)) {
                continue;
            }
            saw.add(predecessor);

            if (isBoundary(predecessor)) {
                arrivals.add(predecessor);
            } else {
                passings.add(predecessor);
                addPredecessors(predecessors, predecessor);
            }
        }
        return new FlowPath(
                FlowPath.Direction.BACKWORD,
                startings,
                passings,
                arrivals);
    }

    /**
     * 指定のパス一覧をすべて含むパスを返す。
     * @param paths 対象のパス一覧
     * @return すべて含むパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static FlowPath union(Collection<FlowPath> paths) {
        Precondition.checkMustNotBeNull(paths, "paths"); //$NON-NLS-1$
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("paths must not be empty"); //$NON-NLS-1$
        }
        Iterator<FlowPath> iter = paths.iterator();
        assert iter.hasNext();

        FlowPath left = iter.next();
        while (iter.hasNext()) {
            FlowPath right = iter.next();
            left = left.union(right);
        }
        return left;
    }

    /**
     * 指定の要素が後続する要素を持つ場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return 後続する要素を持つ場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean hasSuccessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        for (FlowElementOutput output : element.getOutputPorts()) {
            if (output.getConnected().isEmpty() == false) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定の要素が先行する要素を持つ場合のみ{@code true}を返す。
     * @param element 対象の要素
     * @return 先行する要素を持つ場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean hasPredecessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        for (FlowElementInput input : element.getInputPorts()) {
            if (input.getConnected().isEmpty() == false) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定の要素に直接後続する全ての要素を返す。
     * @param element 対象の要素
     * @return 直接後続する全ての要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Set<FlowElement> getSuccessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<FlowElement>();
        addSuccessors(results, element);
        return results;
    }

    private static void addSuccessors(
            Collection<FlowElement> target,
            FlowElement element) {
        assert target != null;
        assert element != null;
        for (FlowElementOutput output : element.getOutputPorts()) {
            for (FlowElementInput opposite : output.getOpposites()) {
                target.add(opposite.getOwner());
            }
        }
    }

    /**
     * 指定の要素に直接先行する全ての要素を返す。
     * @param element 対象の要素
     * @return 直接先行する全ての要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Set<FlowElement> getPredecessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<FlowElement>();
        addPredecessors(results, element);
        return results;
    }

    private static void addPredecessors(
            Collection<FlowElement> target,
            FlowElement element) {
        assert target != null;
        assert element != null;
        for (FlowElementInput input : element.getInputPorts()) {
            for (FlowElementOutput opposite : input.getOpposites()) {
                target.add(opposite.getOwner());
            }
        }
    }

    /**
     * 指定の出力に直接後続するすべてのフロー境界要素を返す。
     * @param output 対象の出力
     * @return 直接後続するすべてのフロー境界要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Set<FlowElement> getSucceedingBoundaries(
            FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        LinkedList<FlowElement> nextSuccessors = new LinkedList<FlowElement>();
        for (FlowElementInput next : output.getOpposites()) {
            nextSuccessors.add(next.getOwner());
        }
        if (nextSuccessors.isEmpty()) {
            return Collections.emptySet();
        }

        Set<FlowElement> saw = new HashSet<FlowElement>();
        Set<FlowElement> results = new HashSet<FlowElement>();
        while (nextSuccessors.isEmpty() == false) {
            FlowElement successor = nextSuccessors.removeFirst();
            if (saw.contains(successor)) {
                continue;
            }
            saw.add(successor);

            if (isBoundary(successor)) {
                results.add(successor);
            } else {
                addSuccessors(nextSuccessors, successor);
            }
        }
        return results;
    }

    /**
     * 指定の入力またはそれらに後続する入力のうち、引数に指定した入力の集合にもっとも近いものの一覧を返す。
     * @param start 開始する入力
     * @param connections 後続する接続の一覧
     * @return 後続する接続に最も近いものの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Set<PortConnection> getSucceedingConnections(
            PortConnection start,
            Set<PortConnection> connections) {
        Precondition.checkMustNotBeNull(start, "start"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(connections, "connections"); //$NON-NLS-1$
        LinkedList<PortConnection> next = new LinkedList<PortConnection>();
        next.add(start);
        Set<PortConnection> results = new HashSet<PortConnection>();
        while (next.isEmpty() == false) {
            PortConnection successor = next.removeFirst();
            if (connections.contains(successor)) {
                results.add(successor);
            } else {
                FlowElementInput nextInput = successor.getDownstream();
                for (FlowElementOutput output : nextInput.getOwner().getOutputPorts()) {
                    next.addAll(output.getConnected());
                }
            }
        }
        return results;
    }

    /**
     * 指定のフロー要素をインライン化する。
     * @param element 対象の要素
     * @param attributes インライン化する際のバイパス要素に付与する属性
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void inlineFlowPart(FlowElement element, FlowElementAttribute... attributes) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        FlowElementDescription description = element.getDescription();
        if (description.getKind() != FlowElementKind.FLOW_COMPONENT) {
            throw new IllegalArgumentException("element must be a flow component"); //$NON-NLS-1$
        }

        FlowPartDescription component = (FlowPartDescription) description;
        FlowGraph graph = component.getFlowGraph();

        // フロー部品の外側からの入力と、フロー部品の内部の入力を結線する
        List<FlowElementInput> externalInputs = element.getInputPorts();
        List<FlowElementOutput> internalInputs = new ArrayList<FlowElementOutput>();
        for (FlowIn<?> fin : graph.getFlowInputs()) {
            internalInputs.add(fin.toOutputPort());
        }
        bypass(externalInputs, internalInputs, attributes);

        // フロー部品の内側からの出力と、フロー部品の外側への出力を結線する
        List<FlowElementOutput> externalOutputs = element.getOutputPorts();
        List<FlowElementInput> internalOutputs = new ArrayList<FlowElementInput>();
        for (FlowOut<?> fout : graph.getFlowOutputs()) {
            internalOutputs.add(fout.toInputPort());
        }
        bypass(internalOutputs, externalOutputs, attributes);

        // 不要な結線を解除
        for (FlowIn<?> fin : graph.getFlowInputs()) {
            disconnect(fin.getFlowElement());
        }
        for (FlowOut<?> fout : graph.getFlowOutputs()) {
            disconnect(fout.getFlowElement());
        }

        // インライン化したフロー部品を除去
        disconnect(element);
    }

    private static void bypass(
            List<FlowElementInput> inputs,
            List<FlowElementOutput> outputs,
            FlowElementAttribute... attributes) {
        assert inputs != null;
        assert outputs != null;
        if (inputs.size() != outputs.size()) {
            throw new IllegalArgumentException();
        }
        Iterator<FlowElementInput> inputIterator = inputs.iterator();
        Iterator<FlowElementOutput> outputIterator = outputs.iterator();
        while (inputIterator.hasNext()) {
            assert outputIterator.hasNext();
            FlowElementInput input = inputIterator.next();
            FlowElementOutput output = outputIterator.next();
            bypass(input, output, attributes);
        }
        assert outputIterator.hasNext() == false;
    }

    private static void bypass(
            FlowElementInput input,
            FlowElementOutput output,
            FlowElementAttribute...attributes) {
        assert input != null;
        assert output != null;
        assert attributes != null;

        Collection<FlowElementOutput> upstreams = input.disconnectAll();
        Collection<FlowElementInput> downstreams = output.disconnectAll();
        for (FlowElementOutput upstream : upstreams) {
            for (FlowElementInput downstream : downstreams) {
                if (attributes.length >= 1) {
                    FlowElementDescription desc = new PseudElementDescription(
                            "bypass",
                            output.getDescription().getDataType(),
                            true,
                            true,
                            attributes);
                    FlowElementResolver resolver = new FlowElementResolver(desc);
                    FlowElementInput bypassIn = resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
                    FlowElementOutput bypassOut = resolver.getOutput(PseudElementDescription.OUTPUT_PORT_NAME);
                    PortConnection.connect(upstream, bypassIn);
                    PortConnection.connect(bypassOut, downstream);
                } else {
                    PortConnection.connect(upstream, downstream);
                }
            }
        }
    }

    private static void connectWithIdentity(
            FlowElementOutput upstream,
            FlowElementInput downstream) {
        assert upstream != null;
        assert downstream != null;
        FlowElementDescription desc = new PseudElementDescription(
                "normalized-identity",
                upstream.getDescription().getDataType(),
                true,
                true);
        FlowElementResolver resolver = new FlowElementResolver(desc);

        FlowElementInput input = resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
        FlowElementOutput output = resolver.getOutput(PseudElementDescription.OUTPUT_PORT_NAME);

        PortConnection.connect(upstream, input);
        PortConnection.connect(output, downstream);
    }

    /**
     * この要素に関連するすべての結線を解除する。
     * @param element 対象の要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void disconnect(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        for (FlowElementInput input : element.getInputPorts()) {
            input.disconnectAll();
        }
        for (FlowElementOutput output : element.getOutputPorts()) {
            output.disconnectAll();
        }
    }

    /**
     * 指定の出力の直後にチェックポイント演算子を挿入する。
     * <p>
     * 現在指定の出力に接続されているすべての入力は、このメソッドの起動後に
     * 挿入した演算子の出力に接続されなおす。
     * </p>
     * @param output 対象の出力
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void insertCheckpoint(FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        insertElement(output, "implicit-checkpoint", FlowBoundary.STAGE);
    }

    /**
     * 指定の出力の直後に恒等演算子を挿入する。
     * <p>
     * 現在指定の出力に接続されているすべての入力は、このメソッドの起動後に
     * 挿入したト演算子の出力に接続されなおす。
     * </p>
     * @param output 対象の出力
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void insertIdentity(FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        insertElement(output, "padding");
    }

    private static void insertElement(
            FlowElementOutput output,
            String name,
            FlowElementAttribute... attributes) {
        assert output != null;
        assert name != null;
        assert attributes != null;

        Collection<FlowElementInput> originalDownstreams = output.disconnectAll();

        FlowElementDescription desc = new PseudElementDescription(
                name,
                output.getDescription().getDataType(),
                true,
                true,
                attributes);
        FlowElementResolver resolver = new FlowElementResolver(desc);

        FlowElementInput insertIn =
            resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
        PortConnection.connect(output, insertIn);

        FlowElementOutput insertOut =
            resolver.getOutput(PseudElementDescription.OUTPUT_PORT_NAME);
        for (FlowElementInput downstream : originalDownstreams) {
            PortConnection.connect(insertOut, downstream);
        }
    }

    private FlowGraphUtil() {
        return;
    }
}
