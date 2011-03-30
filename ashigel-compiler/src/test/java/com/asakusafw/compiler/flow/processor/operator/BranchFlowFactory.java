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
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Branch;

/**
 * {@link BranchFlow}に関する演算子ファクトリークラス。
 */
@Generated("com.asakusafw.compiler.operator.OperatorFactoryClassGenerator") public class
        BranchFlowFactory {
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        /**
         * 速い。
         */
        public final Source<Ex1> high;
        /**
         * 遅い。
         */
        public final Source<Ex1> low;
        /**
         * 停止。
         */
        public final Source<Ex1> stop;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Branch.class);
            builder.declare(BranchFlow.class, BranchFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", Ex1.class);
            builder.addOutput("high", Ex1.class);
            builder.addOutput("low", Ex1.class);
            builder.addOutput("stop", Ex1.class);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.high = resolver.resolveOutput("high");
            this.low = resolver.resolveOutput("low");
            this.stop = resolver.resolveOutput("stop");
        }
    }
    /**
     * 通常の演算子。
     * @param model 対象のモデル
     * @return 生成した演算子オブジェクト
     */
    public BranchFlowFactory.Simple simple(Source<Ex1> model) {
        return new BranchFlowFactory.Simple(model);
    }
    /**
     * パラメーター付きの演算子。
     */
    public static final class WithParameter implements Operator {
        /**
         * 速い。
         */
        public final Source<Ex1> high;
        /**
         * 遅い。
         */
        public final Source<Ex1> low;
        /**
         * 停止。
         */
        public final Source<Ex1> stop;
        WithParameter(Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Branch.class);
            builder.declare(BranchFlow.class, BranchFlowImpl.class, "withParameter");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(int.class);
            builder.addInput("model", Ex1.class);
            builder.addOutput("high", Ex1.class);
            builder.addOutput("low", Ex1.class);
            builder.addOutput("stop", Ex1.class);
            builder.addParameter("parameter", int.class, parameter);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.high = resolver.resolveOutput("high");
            this.low = resolver.resolveOutput("low");
            this.stop = resolver.resolveOutput("stop");
        }
    }
    /**
     * パラメーター付きの演算子。
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     */
    public BranchFlowFactory.WithParameter withParameter(Source<Ex1> model, int parameter) {
        return new BranchFlowFactory.WithParameter(model, parameter);
    }
}