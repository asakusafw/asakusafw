/**
 * Copyright 2011 Asakusa Framework Team.
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
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.CoGroup;

/**
 * {@link GroupSortFlow}に関する演算子ファクトリークラス。
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class GroupSortFlowFactory {
    /**
     * グループ内で値が最小のものを返す。
     */
    public static final class Min implements Operator {
        /**
         *  結果
         */
        public final Source<Ex1> r1;
        Min(Source<Ex1> a1) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "min");
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.ASC)})));
            builder.addOutput("r1", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("a1", a1);
            this.r1 = resolver.resolveOutput("r1");
        }
    }
    /**
     * グループ内で値が最小のものを返す。
     * @param a1 グループ
     * @return 生成した演算子オブジェクト
     */
    public GroupSortFlowFactory.Min min(Source<Ex1> a1) {
        return new GroupSortFlowFactory.Min(a1);
    }
    /**
     * グループ内で値が最大のものを返す。
     */
    public static final class Max implements Operator {
        /**
         *  結果
         */
        public final Source<Ex1> r1;
        Max(Source<Ex1> a1) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(CoGroup.class);
            builder0.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "max");
            builder0.declareParameter(List.class);
            builder0.declareParameter(Result.class);
            builder0.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.DESC)})));
            builder0.addOutput("r1", Ex1.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver0 = builder0.toResolver();
            resolver0.resolveInput("a1", a1);
            this.r1 = resolver0.resolveOutput("r1");
        }
    }
    /**
     * グループ内で値が最大のものを返す。
     * @param a1 グループ
     * @return 生成した演算子オブジェクト
     */
    public GroupSortFlowFactory.Max max(Source<Ex1> a1) {
        return new GroupSortFlowFactory.Max(a1);
    }
    /**
     * 値がパラメーター以下かそうでないかで結果の出力先を変える。
     */
    public static final class WithParameter implements Operator {
        /**
         *  パラメーター以下の値を持つ結果
         */
        public final Source<Ex1> r1;
        /**
         *  パラメーターを超える値を持つ結果
         */
        public final Source<Ex1> r2;
        WithParameter(Source<Ex1> a1, int parameter) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(CoGroup.class);
            builder1.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "withParameter");
            builder1.declareParameter(List.class);
            builder1.declareParameter(Result.class);
            builder1.declareParameter(Result.class);
            builder1.declareParameter(int.class);
            builder1.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.ASC)})));
            builder1.addOutput("r1", Ex1.class);
            builder1.addOutput("r2", Ex1.class);
            builder1.addParameter("parameter", int.class, parameter);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver1 = builder1.toResolver();
            resolver1.resolveInput("a1", a1);
            this.r1 = resolver1.resolveOutput("r1");
            this.r2 = resolver1.resolveOutput("r2");
        }
    }
    /**
     * 値がパラメーター以下かそうでないかで結果の出力先を変える。
     * @param a1 グループ
     * @param parameter パラメーター
     * @return 生成した演算子オブジェクト
     */
    public GroupSortFlowFactory.WithParameter withParameter(Source<Ex1> a1, int parameter) {
        return new GroupSortFlowFactory.WithParameter(a1, parameter);
    }
}