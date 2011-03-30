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
import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.Fold;

/**
 * {@link FoldFlow}に関する演算子ファクトリークラス。
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class FoldFlowFactory {
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        /**
         * 畳み込みの結果
         */
        public final Source<Ex1> out;
        Simple(Source<Ex1> in) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Fold.class);
            builder.declare(FoldFlow.class, FoldFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(Ex1.class);
            builder.addInput("in", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("out", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("in", in);
            this.out = resolver.resolveOutput("out");
        }
    }
    /**
     * 通常の演算子。
     * @param in 畳み込む値
     * @return 生成した演算子オブジェクト
     */
    public FoldFlowFactory.Simple simple(Source<Ex1> in) {
        return new FoldFlowFactory.Simple(in);
    }
    /**
     * 引数つきの演算子。
     */
    public static final class WithParameter implements Operator {
        /**
         * 畳み込みの結果
         */
        public final Source<Ex1> out;
        WithParameter(Source<Ex1> in, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Fold.class);
            builder0.declare(FoldFlow.class, FoldFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("in", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("out", Ex1.class);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver0 = builder0.toResolver();
            resolver0.resolveInput("in", in);
            this.out = resolver0.resolveOutput("out");
        }
    }
    /**
     * 引数つきの演算子。
     * @param in 畳み込む値
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     */
    public FoldFlowFactory.WithParameter withParameter(Source<Ex1> in, int parameter) {
        return new FoldFlowFactory.WithParameter(in, parameter);
    }
}