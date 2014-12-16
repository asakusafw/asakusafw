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
package com.asakusafw.compiler.flow.stage;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.DataClassRepository;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.compiler.flow.ShuffleDescription;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Arrangement;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Term;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;

/**
 * 各ステージのシャッフルフェーズで行われる内容を解析する。
 * @since 0.1.0
 * @version 0.7.1
 */
public class ShuffleAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(ShuffleAnalyzer.class);

    private final FlowCompilingEnvironment environment;

    private boolean sawError;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleAnalyzer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 現在までにエラーが発生していた場合に{@code true}を返す。
     * @return 現在までにエラーが発生していた場合に{@code true}
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * 現在までに発生したエラーの情報をクリアする。
     * @see #hasError()
     */
    public void clearError() {
        sawError = false;
    }

    /**
     * 指定のステージブロックを解析し、対象ステージのシャッフルに関する情報を返す。
     * @param block 対象のステージブロック
     * @return シャッフルに関する情報、解析に失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleModel analyze(StageBlock block) {
        Precondition.checkMustNotBeNull(block, "block"); //$NON-NLS-1$
        if (block.hasReduceBlocks() == false) {
            return null;
        }
        LOG.debug("start analyzing shuffle phase in stage: {}", block); //$NON-NLS-1$
        List<FlowElement> elements = collectRendezvousElements(block.getReduceBlocks());
        List<ShuffleModel.Segment> segments = collectSegments(elements);
        ShuffleModel model = new ShuffleModel(block, segments);

        if (environment.hasError()) {
            return null;
        }

        LOG.debug("finish analyzing shuffle phase in stage: {} ({})", block, model); //$NON-NLS-1$
        return model;
    }

    private List<ShuffleModel.Segment> collectSegments(
            List<FlowElement> elements) {
        assert elements != null;
        List<ShuffleModel.Segment> segments = Lists.create();
        for (int elementId = 0, n = elements.size(); elementId < n; elementId++) {
            FlowElement element = elements.get(elementId);
            FlowElementDescription description = element.getDescription();
            RendezvousProcessor proc = environment.getProcessors()
                .findRendezvousProcessor(description);
            if (proc == null) {
                error("{0}に対する{1}が見つかりませんでした",
                        description,
                        FlowElementProcessor.class.getName());
                continue;
            }

            List<ShuffleModel.Segment> segmentsInElement = Lists.create();
            LOG.debug("applying {}: {}", proc, element); //$NON-NLS-1$
            for (FlowElementInput input : element.getInputPorts()) {
                ShuffleDescription desc = extractDescription(proc, input);
                ShuffleModel.Segment segment = resolveDescription(
                        elementId,
                        segments.size() + segmentsInElement.size() + 1,
                        input,
                        desc);
                if (segment != null) {
                    segmentsInElement.add(segment);
                }
            }
            checkValidSegmentsInElement(segmentsInElement);
            segments.addAll(segmentsInElement);
        }
        return segments;
    }

    private void checkValidSegmentsInElement(List<ShuffleModel.Segment> segmentsInElement) {
        assert segmentsInElement != null;
        if (segmentsInElement.size() == 1) {
            return;
        }
        ShuffleModel.Segment first = segmentsInElement.get(0);
        List<ShuffleModel.Term> group = getGroupingTerms(first);
        for (int i = 1, n = segmentsInElement.size(); i < n; i++) {
            List<ShuffleModel.Term> other = getGroupingTerms(segmentsInElement.get(i));
            if (group.size() != other.size()) {
                error(
                        "グループ化項目の個数が一致しません: {0}",
                        first.getPort().getOwner());
                break;
            }
            for (int j = 0, m = group.size(); j < m; j++) {
                Property firstTerm = group.get(j).getSource();
                Property otherTerm = other.get(j).getSource();
                if (isCompatible(firstTerm.getType(), otherTerm.getType()) == false) {
                    error(
                            "グループ化項目の種類が一致しません: {0}",
                            first.getPort().getOwner());
                }
            }
        }
    }

    private boolean isCompatible(Type a, Type b) {
        assert a != null;
        assert b != null;
        return a.equals(b);
    }

    private List<Term> getGroupingTerms(ShuffleModel.Segment segment) {
        assert segment != null;
        List<Term> results = Lists.create();
        for (ShuffleModel.Term term : segment.getTerms()) {
            if (term.getArrangement() == Arrangement.GROUPING) {
                results.add(term);
            }
        }
        return results;
    }

    private ShuffleDescription extractDescription(
            RendezvousProcessor processor,
            FlowElementInput input) {
        assert processor != null;
        assert input != null;
        ShuffleDescription desc = processor.getShuffleDescription(
                input.getOwner().getDescription(),
                input.getDescription());
        return desc;
    }

    private List<FlowElement> collectRendezvousElements(Set<FlowBlock> reduceBlocks) {
        assert reduceBlocks != null;
        assert reduceBlocks.isEmpty() == false;
        LOG.debug("collecting shuffle result inputs: {}", reduceBlocks); //$NON-NLS-1$

        List<FlowElement> results = Lists.create();
        Set<FlowElement> saw = Sets.create();
        for (FlowBlock reducer : reduceBlocks) {
            for (FlowBlock.Input input : reducer.getBlockInputs()) {
                FlowElement rendezvous = input.getElementPort().getOwner();
                if (saw.contains(rendezvous)) {
                    continue;
                }
                LOG.debug("operator {} will use shuffle results: {}", rendezvous, reducer); //$NON-NLS-1$
                saw.add(rendezvous);
                results.add(rendezvous);
            }
        }
        return results;
    }

    private ShuffleModel.Segment resolveDescription(
            int elementId,
            int portId,
            FlowElementInput input,
            ShuffleDescription desciption) {
        assert input != null;
        assert desciption != null;
        ShuffleKey keyInfo = desciption.getKeyInfo();

        Type inputType = input.getDescription().getDataType();

        DataClassRepository dataClasses = environment.getDataClasses();
        DataClass source = dataClasses.load(inputType);
        DataClass target = dataClasses.load(desciption.getOutputType());
        if (source == null) {
            error("データクラス{0}は定義されていません", inputType);
        }
        if (target == null) {
            error("データクラス{0}は定義されていません", desciption.getOutputType());
        }
        if (source == null || target == null) {
            return null;
        }

        List<ShuffleModel.Term> terms = Lists.create();
        for (String name : keyInfo.getGroupProperties()) {
            int termId = terms.size() + 1;
            DataClass.Property property = target.findProperty(name);
            if (property == null) {
                error("データクラス{0}にはプロパティ{1}が定義されていません", target, name);
                continue;
            }
            terms.add(new ShuffleModel.Term(
                    termId,
                    property,
                    ShuffleModel.Arrangement.GROUPING));
        }
        for (ShuffleKey.Order order : keyInfo.getOrderings()) {
            int termId = terms.size() + 1;
            DataClass.Property property = target.findProperty(order.getProperty());
            if (property == null) {
                error("データクラス{0}にはプロパティ{1}が定義されていません",
                        target,
                        order.getProperty());
                continue;
            }
            ShuffleModel.Arrangement arrange;
            if (order.getDirection() == ShuffleKey.Direction.ASC) {
                arrange = ShuffleModel.Arrangement.ASCENDING;
            } else {
                arrange = ShuffleModel.Arrangement.DESCENDING;
            }

            terms.add(new ShuffleModel.Term(
                    termId,
                    property,
                    arrange));
        }

        return new ShuffleModel.Segment(
                elementId,
                portId,
                desciption,
                input,
                source,
                target,
                terms);
    }

    private void error(String format, Object...args) {
        environment.error(format, args);
        sawError = true;
    }
}
