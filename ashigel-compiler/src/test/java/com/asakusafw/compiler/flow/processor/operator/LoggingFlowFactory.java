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
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Logging;

/**
 * {@link LoggingFlow}に関する演算子ファクトリークラス。
 */
@Generated("com.asakusafw.compiler.operator.OperatorFactoryClassGenerator") public class
        LoggingFlowFactory {
    /**
     * パラメーター付きの演算子。
     */
    public static final class WithParameter implements Operator {
        /**
         * 入力された内容
         */
        public final Source<Ex1> out;
        WithParameter(Source<Ex1> model, String parameter) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Logging.class);
            builder.declare(LoggingFlow.class, LoggingFlowImpl.class, "withParameter");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(String.class);
            builder.addInput("model", Ex1.class);
            builder.addOutput("out", Ex1.class);
            builder.addParameter("parameter", String.class, parameter);
            builder.addAttribute(ObservationCount.AT_LEAST_ONCE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.out = resolver.resolveOutput("out");
        }
    }
    /**
     * パラメーター付きの演算子。
     * @param model 対象のモデル
     * @param parameter 返す文字列
     * @return 生成した演算子オブジェクト
     */
    public LoggingFlowFactory.WithParameter withParameter(Source<Ex1> model, String parameter) {
        return new LoggingFlowFactory.WithParameter(model, parameter);
    }
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        /**
         * 入力された内容
         */
        public final Source<Ex1> out;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Logging.class);
            builder.declare(LoggingFlow.class, LoggingFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", Ex1.class);
            builder.addOutput("out", Ex1.class);
            builder.addAttribute(ObservationCount.AT_LEAST_ONCE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("model", model);
            this.out = resolver.resolveOutput("out");
        }
    }
    /**
     * 通常の演算子。
     * @param model 対象のモデル
     * @return 生成した演算子オブジェクト
     */
    public LoggingFlowFactory.Simple simple(Source<Ex1> model) {
        return new LoggingFlowFactory.Simple(model);
    }
}