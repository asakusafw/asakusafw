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
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.CoGroup;

/**
 * {@link CoGroupFlow}に関する演算子ファクトリークラス。
 */
@Generated("com.asakusafw.compiler.operator.OperatorFactoryClassGenerator") public class
        CoGroupFlowFactory {
    /**
     * valueの合計をグループを入れ替えて返す。
     */
    public static final class Op2 implements Operator {
        /**
         *  結果1
         */
        public final Source<Ex1> r1;
        /**
         *  結果2
         */
        public final Source<Ex2> r2;
        Op2(Source<Ex1> a1, Source<Ex2> a2) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "op2");
            builder.declareParameter(List.class);
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(Result.class);
            builder.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addInput("a2", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addOutput("r1", Ex1.class);
            builder.addOutput("r2", Ex2.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("a1", a1);
            resolver.resolveInput("a2", a2);
            this.r1 = resolver.resolveOutput("r1");
            this.r2 = resolver.resolveOutput("r2");
        }
    }
    /**
     * valueの合計をグループを入れ替えて返す。
     * @param a1 グループ1
     * @param a2 グループ2
     * @return 生成した演算子オブジェクト
     */
    public CoGroupFlowFactory.Op2 op2(Source<Ex1> a1, Source<Ex2> a2) {
        return new CoGroupFlowFactory.Op2(a1, a2);
    }
    /**
     * valueの合計を1->2->3の順にローテートして返す。
     */
    public static final class Op3 implements Operator {
        /**
         *  結果1
         */
        public final Source<Ex1> r1;
        /**
         *  結果2
         */
        public final Source<Ex1> r2;
        /**
         *  結果3
         */
        public final Source<Ex1> r3;
        Op3(Source<Ex1> a1, Source<Ex1> a2, Source<Ex1> a3) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "op3");
            builder.declareParameter(List.class);
            builder.declareParameter(List.class);
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(Result.class);
            builder.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addInput("a2", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addInput("a3", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addOutput("r1", Ex1.class);
            builder.addOutput("r2", Ex1.class);
            builder.addOutput("r3", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("a1", a1);
            resolver.resolveInput("a2", a2);
            resolver.resolveInput("a3", a3);
            this.r1 = resolver.resolveOutput("r1");
            this.r2 = resolver.resolveOutput("r2");
            this.r3 = resolver.resolveOutput("r3");
        }
    }
    /**
     * valueの合計を1->2->3の順にローテートして返す。
     * @param a1 グループ1
     * @param a2 グループ2
     * @param a3 グループ3
     * @return 生成した演算子オブジェクト
     */
    public CoGroupFlowFactory.Op3 op3(Source<Ex1> a1, Source<Ex1> a2, Source<Ex1> a3) {
        return new CoGroupFlowFactory.Op3(a1, a2, a3);
    }
    /**
     * valueの合計 + 引数を返す。
     */
    public static final class WithParameter implements Operator {
        /**
         *  結果
         */
        public final Source<Ex1> r1;
        WithParameter(Source<Ex1> a1, int parameter) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "withParameter");
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(int.class);
            builder.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addOutput("r1", Ex1.class);
            builder.addParameter("parameter", int.class, parameter);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("a1", a1);
            this.r1 = resolver.resolveOutput("r1");
        }
    }
    /**
     * valueの合計 + 引数を返す。
     * @param a1 グループ
     * @param parameter パラメーター
     * @return 生成した演算子オブジェクト
     */
    public CoGroupFlowFactory.WithParameter withParameter(Source<Ex1> a1, int parameter) {
        return new CoGroupFlowFactory.WithParameter(a1, parameter);
    }
    /**
     * valueの合計を返す。
     */
    public static final class Sorted implements Operator {
        /**
         *  結果
         */
        public final Source<Ex1> r1;
        Sorted(Source<Ex1> a1) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "sorted");
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.DESC)})));
            builder.addOutput("r1", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("a1", a1);
            this.r1 = resolver.resolveOutput("r1");
        }
    }
    /**
     * valueの合計を返す。
     * @param a1 グループ
     * @return 生成した演算子オブジェクト
     */
    public CoGroupFlowFactory.Sorted sorted(Source<Ex1> a1) {
        return new CoGroupFlowFactory.Sorted(a1);
    }
    /**
     * valueの合計を返す。
     */
    public static final class Op1 implements Operator {
        /**
         *  結果
         */
        public final Source<Ex1> r1;
        Op1(Source<Ex1> a1) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "op1");
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.addInput("a1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addOutput("r1", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("a1", a1);
            this.r1 = resolver.resolveOutput("r1");
        }
    }
    /**
     * valueの合計を返す。
     * @param a1 グループ
     * @return 生成した演算子オブジェクト
     */
    public CoGroupFlowFactory.Op1 op1(Source<Ex1> a1) {
        return new CoGroupFlowFactory.Op1(a1);
    }
}