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

import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.Compilable;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.ShuffleDescription;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;


/**
 * シャッフルフェーズの構造を表すモデル。
 */
public class ShuffleModel extends Compilable.Trait<CompiledShuffle> {

    private final StageBlock stageBlock;

    private final List<Segment> segments;

    /**
     * インスタンスを生成する。
     * @param stageBlock 対象のステージ
     * @param segments ステージに含まれるセグメント一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleModel(StageBlock stageBlock, List<Segment> segments) {
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(segments, "segments"); //$NON-NLS-1$
        this.stageBlock = stageBlock;
        this.segments = segments;
    }

    /**
     * このシャッフルフェーズの元となるステージブロックを返す。
     * @return シャッフルフェーズの元となるステージブロック
     */
    public StageBlock getStageBlock() {
        return stageBlock;
    }

    /**
     * このシャッフルフェーズに含まれるセグメントの一覧を返す。
     * @return シャッフルフェーズに含まれるセグメントの一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * 指定の入力に関するセグメントを返す。
     * @param input 対象の入力
     * @return 対応するセグメント、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Segment findSegment(FlowElementInput input) {
        Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
        for (Segment segment : getSegments()) {
            if (segment.getPort().equals(input)) {
                return segment;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Shuffle({0})", //$NON-NLS-1$
                segments);
    }

    /**
     * シャッフルの入力ごとの情報。
     */
    public static class Segment extends Compilable.Trait<CompiledShuffleFragment> {

        private final int elementId;

        private final int portId;

        private final ShuffleDescription description;

        private final FlowElementInput port;

        private final DataClass source;

        private final DataClass target;

        private final List<Term> terms;

        /**
         * インスタンスを生成する。
         * @param elementId 要素のシャッフル全体の通し番号
         * @param portId ポートのシャッフル全体の通し番号
         * @param description ポートごとのシャッフル記述
         * @param port 実際のポート
         * @param source シャッフル開始時の型
         * @param target シャッフル終了時の型
         * @param terms シャッフル時のキープロパティの一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Segment(
                int elementId,
                int portId,
                ShuffleDescription description,
                FlowElementInput port,
                DataClass source,
                DataClass target,
                List<Term> terms) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(target, "target"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(terms, "terms"); //$NON-NLS-1$
            this.elementId = elementId;
            this.portId = portId;
            this.description = description;
            this.port = port;
            this.source = source;
            this.target = target;
            this.terms = terms;
        }

        /**
         * 要素のシャッフル全体の通し番号を返す。
         * @return 要素のシャッフル全体の通し番号
         */
        public int getElementId() {
            return elementId;
        }

        /**
         * ポートのシャッフル全体の通し番号を返す。
         * @return ポートのシャッフル全体の通し番号
         */
        public int getPortId() {
            return portId;
        }

        /**
         * ポートごとのシャッフル記述を返す。
         * @return ポートごとのシャッフル記述
         */
        public ShuffleDescription getDescription() {
            return description;
        }

        /**
         * 実際のポートを返す。
         * @return 実際のポート
         */
        public FlowElementInput getPort() {
            return port;
        }

        /**
         * シャッフル開始前の型を返す。
         * @return シャッフル開始前の型
         */
        public DataClass getSource() {
            return source;
        }

        /**
         * シャッフル時の型を返す。
         * @return シャッフル時の型
         */
        public DataClass getTarget() {
            return target;
        }

        /**
         * シャッフル時のキープロパティの一覧を返す。
         * @return シャッフル時のキープロパティの一覧
         */
        public List<Term> getTerms() {
            return terms;
        }

        /**
         * このセグメントに含まれる、指定の名前を持つキープロパティを返す。
         * @param propertyName 対象のプロパティ名
         * @return 対応するキープロパティ、存在しない場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Term findTerm(String propertyName) {
            Precondition.checkMustNotBeNull(propertyName, "propertyName"); //$NON-NLS-1$
            if (propertyName.trim().isEmpty()) {
                return null;
            }
            String name = JavaName.of(propertyName).toMemberName();
            for (Term term : terms) {
                if (term.getSource().getName().equals(name)) {
                    return term;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "ShuffleSegment({2})(port={0}, terms={1})", //$NON-NLS-1$
                    port,
                    terms,
                    portId);
        }
    }

    /**
     * シャッフル時のキープロパティ。
     */
    public static class Term {

        private final int termId;

        private final DataClass.Property source;

        private final Arrangement arrangement;

        /**
         * インスタンスを生成する。
         * @param termId プロパティのセグメント内の通し番号
         * @param source 対応するプロパティ
         * @param arrangement プロパティの整列情報
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Term(
                int termId,
                DataClass.Property source,
                Arrangement arrangement) {
            Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(arrangement, "arrangement"); //$NON-NLS-1$
            this.termId = termId;
            this.source = source;
            this.arrangement = arrangement;
        }

        /**
         * プロパティのセグメント内の通し番号を返す。
         * @return プロパティのセグメント内の通し番号
         */
        public int getTermId() {
            return termId;
        }

        /**
         * 対応するプロパティの情報を返す。
         * @return 対応するプロパティの情報
         */
        public DataClass.Property getSource() {
            return source;
        }

        /**
         * プロパティの整列情報を返す。
         * @return プロパティの整列情報
         */
        public Arrangement getArrangement() {
            return arrangement;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0} {1}", //$NON-NLS-1$
                    getSource().getName(),
                    getArrangement());
        }
    }

    /**
     * 並べ方。
     */
    public enum Arrangement {

        /**
         * 同じ値のものでグループ化する。
         */
        GROUPING,

        /**
         * 昇順に並べる。
         */
        ASCENDING,

        /**
         * 降順に並べる。
         */
        DESCENDING,
    }
}
