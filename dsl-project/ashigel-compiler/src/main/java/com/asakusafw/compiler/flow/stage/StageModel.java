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
package com.asakusafw.compiler.flow.stage;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.Compilable;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.FlowElementProcessor.Kind;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;


/**
 * Map Reduceプログラムの構造を表すモデル。
 * <p>
 * シャッフルフェーズの内容については、別途{@link ShuffleModel}で表現する。
 * </p>
 */
public class StageModel {

    private final StageBlock stageBlock;

    private final List<MapUnit> mapUnits;

    private final ShuffleModel shuffleModel;

    private final List<ReduceUnit> reduceUnits;

    private final List<Sink> sinks;

    /**
     * インスタンスを生成する。
     * @param stageBlock このモデルに関連するステージブロック
     * @param mapUnits このモデルに含むマップ単位の一覧
     * @param shuffleModel シャッフルの構造
     * @param reduceUnits このモデルに含むレデュース単位の一覧
     * @param sinks このステージの出力一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StageModel(
            StageBlock stageBlock,
            List<MapUnit> mapUnits,
            ShuffleModel shuffleModel,
            List<ReduceUnit> reduceUnits,
            List<Sink> sinks) {
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(mapUnits, "mapUnits"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(reduceUnits, "reduceUnits"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sinks, "sinks"); //$NON-NLS-1$
        this.stageBlock = stageBlock;
        this.shuffleModel = shuffleModel;
        int unitSerial = 1;
        for (MapUnit unit : mapUnits) {
            unit.renumberUnit(unitSerial++);
        }
        for (ReduceUnit unit : reduceUnits) {
            unit.renumberUnit(unitSerial++);
        }
        this.mapUnits = mapUnits;
        this.reduceUnits = reduceUnits;
        this.sinks = sinks;
    }

    /**
     * この要素を構成するステージブロックを返す。
     * @return この要素を構成するステージブロック
     */
    public StageBlock getStageBlock() {
        return stageBlock;
    }

    /**
     * この要素を構成するマップ単位の一覧を返す。
     * @return この要素を構成するマップ単位の一覧
     */
    public List<MapUnit> getMapUnits() {
        return mapUnits;
    }

    /**
     * ステージ内で行われるシャッフルの構造を返す。
     * @return ステージ内で行われるシャッフルの構造、行われない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleModel getShuffleModel() {
        return shuffleModel;
    }

    /**
     * この要素を構成するレデュース単位の一覧を返す。
     * @return この要素を構成するレデュース単位の一覧
     */
    public List<ReduceUnit> getReduceUnits() {
        return reduceUnits;
    }

    /**
     * ステージ内で利用されるサイドデータ入力の一覧を返す。
     * @return サイドデータ入力の一覧
     */
    public Set<InputDescription> getSideDataInputs() {
        Set<ResourceFragment> resources = Sets.create();
        List<Unit<?>> units = Lists.create();
        units.addAll(getMapUnits());
        units.addAll(getReduceUnits());
        for (Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                resources.addAll(fragment.getResources());
            }
        }
        Set<InputDescription> results = Sets.create();
        for (ResourceFragment resource : resources) {
            results.addAll(resource.getDescription().getSideDataInputs());
        }
        return results;
    }

    /**
     * このステージの結果として利用される要素の一覧を返す。
     * @return このステージの結果として利用される要素の一覧
     */
    public List<Sink> getStageResults() {
        return sinks;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Stage(map={0}, shuffle={1}, reduce={2})",
                getMapUnits(),
                getShuffleModel(),
                getReduceUnits());
    }

    /**
     * レデュース処理やマップ処理単位の基底となるクラス。
     * @param <T> コンパイル結果のデータ種
     */
    public abstract static class Unit<T> extends Compilable.Trait<T> {

        private final List<FlowBlock.Input> inputs;

        private final List<Fragment> fragments;

        private int serialNumber = -1;

        /**
         * インスタンスを生成する。
         * @param inputs この処理単位へのブロック入力の一覧
         * @param fragments 処理単位を構成する処理断片の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Unit(List<FlowBlock.Input> inputs, List<Fragment> fragments) {
            Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(fragments, "fragments"); //$NON-NLS-1$
            this.inputs = inputs;
            this.fragments = fragments;
        }

        /**
         * この処理単位にシリアル番号が振られている場合のみ{@code true}を返す。
         * @return シリアル番号が振られている場合のみ{@code true}
         */
        boolean hasSerialNumber() {
            return serialNumber >= 0;
        }

        /**
         * この処理単位のシリアル番号を返す。
         * <p>
         * 全ての処理単位は、ステージ内において異なるシリアル番号を持つ必要がある。
         * </p>
         * @return この処理単位のシリアル番号
         */
        public int getSerialNumber() {
            if (serialNumber < 0) {
                throw new IllegalStateException();
            }
            return serialNumber;
        }

        /**
         * この処理単位へのブロック入力の一覧を返す。
         * @return ブロック入力の一覧
         */
        public List<FlowBlock.Input> getInputs() {
            return inputs;
        }

        /**
         * この処理単位を構成する処理断片の一覧を返す。
         * @return 処理断片の一覧
         */
        public List<Fragment> getFragments() {
            return fragments;
        }

        void renumberUnit(int serial) {
            this.serialNumber = serial;
        }
    }

    /**
     * マップ処理の単位。
     */
    public static class MapUnit extends Unit<CompiledType> {

        /**
         * インスタンスを生成する。
         * @param inputs このマップ単位へのブロック入力の一覧
         * @param fragments 処理断片の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public MapUnit(List<FlowBlock.Input> inputs, List<Fragment> fragments) {
            super(inputs, fragments);
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "MapUnit({2})'{'inputs={0}, fragments={1}'}'",
                    getInputs(),
                    getFragments(),
                    hasSerialNumber() ? String.valueOf(getSerialNumber()) : "?");
        }
    }

    /**
     * レデュース処理の単位。
     */
    public static class ReduceUnit extends Unit<CompiledReduce> {

        /**
         * インスタンスを生成する。
         * @param inputs このレデュース単位へのブロック入力の一覧
         * @param fragments 処理断片の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ReduceUnit(List<FlowBlock.Input> inputs, List<Fragment> fragments) {
            super(inputs, fragments);
        }

        /**
         * この処理単位がコンバイン処理を許す場合のみ{@code true}を返す。
         * @return この処理単位がコンバイン処理を許す場合のみ{@code true}
         */
        public boolean canCombine() {
            List<Fragment> fragments = getFragments();
            if (fragments.isEmpty()) {
                return false;
            }
            Fragment headFragment = fragments.get(0);
            return headFragment.canCombine();
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "ReduceUnit({2})'{'inputs={0}, fragments={1}'}'",
                    getInputs(),
                    getFragments(),
                    hasSerialNumber() ? String.valueOf(getSerialNumber()) : "?");
        }
    }

    /**
     * 処理断片。
     */
    public static class Fragment extends Compilable.Trait<CompiledType> {

        private final int serialNumber;

        private final List<Factor> factors;

        private final List<ResourceFragment> resources;

        /**
         * インスタンスを生成する。
         * @param serialNumber the serial number
         * @param factors 処理の最小単位の一覧
         * @param resources リソースの一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Fragment(int serialNumber, List<Factor> factors, List<ResourceFragment> resources) {
            Precondition.checkMustNotBeNull(factors, "factors"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(resources, "resources"); //$NON-NLS-1$
            if (factors.isEmpty()) {
                throw new IllegalArgumentException();
            }
            Factor first = factors.get(0);
            if (first.getElement().getInputPorts().size() != 1 && first.isRendezvous() == false) {
                throw new IllegalArgumentException();
            }
            if (factors.size() >= 2 && first.isRendezvous()) {
                throw new IllegalArgumentException();
            }
            this.serialNumber = serialNumber;
            this.factors = Lists.from(factors);
            this.resources = resources;
        }

        /**
         * この処理断片のシリアル番号を返す。
         * @return この処理断片のシリアル番号
         */
        public int getSerialNumber() {
            return serialNumber;
        }

        /**
         * この処理断片がコンバイン処理を許す場合のみ{@code true}を返す。
         * @return この処理断片がコンバイン処理を許す場合のみ{@code true}
         */
        public boolean canCombine() {
            if (isRendezvous() == false) {
                return false;
            }
            Factor first = factors.get(0);
            assert first.isRendezvous();
            RendezvousProcessor processor = (RendezvousProcessor) first.getProcessor();
            return processor.isPartial(first.getElement().getDescription());
        }

        /**
         * この処理断片を構成する処理の最小単位の一覧を返す。
         * @return 処理の最小単位の一覧
         */
        public List<Factor> getFactors() {
            return factors;
        }

        /**
         * この処理断片への入力一覧を返す。
         * @return 処理断片への入力一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public List<FlowElementInput> getInputPorts() {
            if (factors.isEmpty()) {
                return Collections.emptyList();
            }
            Factor first = factors.get(0);
            return first.getElement().getInputPorts();
        }

        /**
         * この処理断片からの出力一覧を返す。
         * @return この処理断片からの出力一覧
         */
        public List<FlowElementOutput> getOutputPorts() {
            if (factors.isEmpty()) {
                return Collections.emptyList();
            }
            Factor last = factors.get(factors.size() - 1);
            return last.getElement().getOutputPorts();
        }

        /**
         * この処理断片が利用するリソースの一覧を返す。
         * @return この処理断片が利用するリソースの一覧
         */
        public List<ResourceFragment> getResources() {
            return resources;
        }

        /**
         * この処理断片が全体として合流地点に配置される場合のみ{@code true}を返す。
         * @return 合流地点に配置される場合のみ{@code true}
         */
        public boolean isRendezvous() {
            if (factors.isEmpty()) {
                return false;
            }
            Factor first = factors.get(0);
            return first.isRendezvous();
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Fragment{0}",
                    getInputPorts());
        }
    }

    /**
     * 個々の演算子に関連する要素。
     */
    public static class Factor {

        private final FlowElement element;

        private final FlowElementProcessor processor;

        /**
         * インスタンスを生成する。
         * @param element この要素に関連するフロー要素
         * @param processor この要素を処理する処理器
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Factor(FlowElement element, FlowElementProcessor processor) {
            Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
            this.element = element;
            this.processor = processor;
        }

        /**
         * この要素が合流地点に配置される場合のみ{@code true}を返す。
         * @return 合流地点に配置される場合のみ{@code true}
         */
        public boolean isRendezvous() {
            return processor.getKind() == Kind.RENDEZVOUS;
        }

        /**
         * この要素がラインの末端に配置される場合のみ{@code true}を返す。
         * @return ラインの末端に配置される場合のみ{@code true}
         */
        public boolean isLineEnd() {
            return processor.getKind() == Kind.LINE_END;
        }

        /**
         * この要素に関連するフロー要素を返す。
         * @return 関連するフロー要素
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public FlowElement getElement() {
            return element;
        }

        /**
         * この要素に関連するフロー要素の処理器を返す。
         * @return 関連するフロー要素の処理器
         */
        public FlowElementProcessor getProcessor() {
            return processor;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Factor({0})",
                    element);
        }
    }

    /**
     * リソースに対する要素。
     */
    public static class ResourceFragment extends Compilable.Trait<CompiledType> {

        private final FlowResourceDescription description;

        /**
         * インスタンスを生成する。
         * @param description 対象の記述
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ResourceFragment(FlowResourceDescription description) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            this.description = description;
        }

        /**
         * 対応する記述を返す。
         * @return 対応する記述
         */
        public FlowResourceDescription getDescription() {
            return description;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + description.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ResourceFragment other = (ResourceFragment) obj;
            if (description.equals(other.description) == false) {
                return false;
            }
            return true;
        }
    }

    /**
     * 出力を表す。
     */
    public static class Sink {

        private final Set<FlowBlock.Output> outputs;

        private final String name;

        /**
         * ステージの出力を表すインスタンスを生成する。
         * @param outputs 関連するブロック出力の一覧
         * @param name 出力の名前
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Sink(Set<FlowBlock.Output> outputs, String name) {
            Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
            this.outputs = outputs;
            this.name = name;
        }

        /**
         * この出力に関連するブロック出力を返す。
         * @return 関連するブロック出力
         */
        public Set<FlowBlock.Output> getOutputs() {
            return outputs;
        }

        /**
         * この出力に関連する型を返す。
         * @return 関連する型
         */
        public java.lang.reflect.Type getType() {
            return outputs.iterator().next().getElementPort().getDescription().getDataType();
        }

        /**
         * この出力の名前を返す。
         * @return この出力の名前
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Sink({0})", getName());
        }
    }
}
