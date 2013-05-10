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
import java.util.List;

import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.flow.processor.InputBuffer;
import com.asakusafw.vocabulary.operator.CoGroup;
/**
 * {@link GroupSortFlow}に関する演算子ファクトリークラス。
 * @see GroupSortFlow
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class GroupSortFlowFactory {
    /**
     * 値がパラメーター以下かそうでないかで結果の出力先を変える。
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         *  パラメーター以下の値を持つ結果
         */
        public final Source<Ex1> r1;
        /**
         *  パラメーターを超える値を持つ結果
         */
        public final Source<Ex1> r2;
        WithParameter(Source<Ex1> a1, int parameter) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "withParameter");
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(int.class);
            builder.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.ASC)})));
            builder.addOutput("r1", Ex1.class);
            builder.addOutput("r2", Ex1.class);
            builder.addParameter("parameter", int.class, parameter);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            builder.addAttribute(InputBuffer.EXPAND);
            this.$ = builder.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public GroupSortFlowFactory.WithParameter as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * 値がパラメーター以下かそうでないかで結果の出力先を変える。
     * @param a1 グループ
     * @param parameter パラメーター
     * @return 生成した演算子オブジェクト
     * @see GroupSortFlow#withParameter(List, Result, Result, int)
     */
    public GroupSortFlowFactory.WithParameter withParameter(Source<Ex1> a1, int parameter) {
        return new GroupSortFlowFactory.WithParameter(a1, parameter);
    }
    /**
     * グループ内で値が最小のものを返す。
     */
    public static final class Min implements Operator {
        private final FlowElementResolver $;
        /**
         *  結果
         */
        public final Source<Ex1> r1;
        Min(Source<Ex1> a1) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(CoGroup.class);
            builder0.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "min");
            builder0.declareParameter(List.class);
            builder0.declareParameter(Result.class);
            builder0.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.ASC)})));
            builder0.addOutput("r1", Ex1.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            builder0.addAttribute(InputBuffer.EXPAND);
            this.$ = builder0.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName0 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public GroupSortFlowFactory.Min as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * グループ内で値が最小のものを返す。
     * @param a1 グループ
     * @return 生成した演算子オブジェクト
     * @see GroupSortFlow#min(List, Result)
     */
    public GroupSortFlowFactory.Min min(Source<Ex1> a1) {
        return new GroupSortFlowFactory.Min(a1);
    }
    /**
     * グループ内で値が最大のものを返す。
     */
    public static final class Max implements Operator {
        private final FlowElementResolver $;
        /**
         *  結果
         */
        public final Source<Ex1> r1;
        Max(Source<Ex1> a1) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(CoGroup.class);
            builder1.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "max");
            builder1.declareParameter(List.class);
            builder1.declareParameter(Result.class);
            builder1.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.DESC)})));
            builder1.addOutput("r1", Ex1.class);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            builder1.addAttribute(InputBuffer.EXPAND);
            this.$ = builder1.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName1 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public GroupSortFlowFactory.Max as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * グループ内で値が最大のものを返す。
     * @param a1 グループ
     * @return 生成した演算子オブジェクト
     * @see GroupSortFlow#max(List, Result)
     */
    public GroupSortFlowFactory.Max max(Source<Ex1> a1) {
        return new GroupSortFlowFactory.Max(a1);
    }
}