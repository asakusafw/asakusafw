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
import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Convert;

/**
 * {@link ConvertFlow}に関する演算子ファクトリークラス。
 */
@Generated("com.asakusafw.compiler.operator.OperatorFactoryClassGenerator") public class
        ConvertFlowFactory {
    /**
     * パラメーター付きの演算子。
     */
    public static final class WithParameter implements Operator {
        /**
         * 入力された内容
         */
        public final Source<Ex1> original;
        /**
         *  変換結果
         */
        public final Source<Ex2> out;
        WithParameter(Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Convert.class);
            builder.declare(ConvertFlow.class, ConvertFlowImpl.class, "withParameter");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(int.class);
            builder.addInput("model", Ex1.class);
            builder.addOutput("original", Ex1.class);
            builder.addOutput("out", Ex2.class);
            builder.addParameter("parameter", int.class, parameter);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.original = resolver.resolveOutput("original");
            this.out = resolver.resolveOutput("out");
        }
    }
    /**
     * パラメーター付きの演算子。
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     */
    public ConvertFlowFactory.WithParameter withParameter(Source<Ex1> model, int parameter) {
        return new ConvertFlowFactory.WithParameter(model, parameter);
    }
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        /**
         * 入力された内容
         */
        public final Source<Ex1> original;
        /**
         *  変換結果
         */
        public final Source<Ex2> out;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Convert.class);
            builder.declare(ConvertFlow.class, ConvertFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", Ex1.class);
            builder.addOutput("original", Ex1.class);
            builder.addOutput("out", Ex2.class);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.original = resolver.resolveOutput("original");
            this.out = resolver.resolveOutput("out");
        }
    }
    /**
     * 通常の演算子。
     * @param model 対象のモデル
     * @return 生成した演算子オブジェクト
     */
    public ConvertFlowFactory.Simple simple(Source<Ex1> model) {
        return new ConvertFlowFactory.Simple(model);
    }
}