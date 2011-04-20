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
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.Summarize;

/**
 * {@link SummarizeFlow}に関する演算子ファクトリークラス。
 */
@Generated("com.asakusafw.compiler.operator.OperatorFactoryClassGenerator") public class
        SummarizeFlowFactory {
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        /**
         *  結果
         */
        public final Source<ExSummarized> out;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Summarize.class);
            builder.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addOutput("out", ExSummarized.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.out = resolver.resolveOutput("out");
        }
    }
    /**
     * 通常の演算子。
     * @param model モデル
     * @return 生成した演算子オブジェクト
     */
    public SummarizeFlowFactory.Simple simple(Source<Ex1> model) {
        return new SummarizeFlowFactory.Simple(model);
    }
    /**
     * キー名を変更する演算子。
     */
    public static final class RenameKey implements Operator {
        /**
         *  結果
         */
        public final Source<ExSummarized2> out;
        RenameKey(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Summarize.class);
            builder.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "renameKey");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder.addOutput("out", ExSummarized2.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.out = resolver.resolveOutput("out");
        }
    }
    /**
     * キー名を変更する演算子。
     * @param model モデル
     * @return 生成した演算子オブジェクト
     */
    public SummarizeFlowFactory.RenameKey renameKey(Source<Ex1> model) {
        return new SummarizeFlowFactory.RenameKey(model);
    }
}