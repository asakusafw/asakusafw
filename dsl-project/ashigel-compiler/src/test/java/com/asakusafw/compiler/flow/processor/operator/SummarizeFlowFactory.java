/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.processor.operator;
import java.util.Arrays;

import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.operator.Summarize;
/**
 * {@link SummarizeFlow}に関する演算子ファクトリークラス。
 * @see SummarizeFlow
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class SummarizeFlowFactory {
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         *  結果
         */
        public final Source<ExSummarized> out;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Summarize.class);
            builder.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("out", ExSummarized.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            builder.addAttribute(PartialAggregation.DEFAULT);
            this.$ = builder.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public SummarizeFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * 通常の演算子。
     * @param model モデル
     * @return 生成した演算子オブジェクト
     * @see SummarizeFlow#simple(Ex1)
     */
    public SummarizeFlowFactory.Simple simple(Source<Ex1> model) {
        return new SummarizeFlowFactory.Simple(model);
    }
    /**
     * キーの名前変更を含む演算子。
     */
    public static final class RenameKey implements Operator {
        private final FlowElementResolver $;
        /**
         *  結果
         */
        public final Source<ExSummarized2> out;
        RenameKey(Source<Ex1> model) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Summarize.class);
            builder0.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "renameKey");
            builder0.declareParameter(Ex1.class);
            builder0.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("out", ExSummarized2.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            builder0.addAttribute(PartialAggregation.DEFAULT);
            this.$ = builder0.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName0 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public SummarizeFlowFactory.RenameKey as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * キーの名前変更を含む演算子。
     * @param model モデル
     * @return 生成した演算子オブジェクト
     * @see SummarizeFlow#renameKey(Ex1)
     */
    public SummarizeFlowFactory.RenameKey renameKey(Source<Ex1> model) {
        return new SummarizeFlowFactory.RenameKey(model);
    }
    /**
     * Grouping key is also used for other aggregation operations.
     */
    public static final class KeyConflict implements Operator {
        private final FlowElementResolver $;
        /**
         *  result model
         */
        public final Source<com.asakusafw.compiler.flow.testing.model.KeyConflict> out;
        KeyConflict(Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(Summarize.class);
            builder1.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "keyConflict");
            builder1.declareParameter(Ex1.class);
            builder1.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addOutput("out", com.asakusafw.compiler.flow.testing.model.KeyConflict.class);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            builder1.addAttribute(PartialAggregation.DEFAULT);
            this.$ = builder1.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName1 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public SummarizeFlowFactory.KeyConflict as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * Grouping key is also used for other aggregation operations.
     * @param model target model
     * @return 生成した演算子オブジェクト
     * @see SummarizeFlow#keyConflict(Ex1)
     */
    public SummarizeFlowFactory.KeyConflict keyConflict(Source<Ex1> model) {
        return new SummarizeFlowFactory.KeyConflict(model);
    }
}