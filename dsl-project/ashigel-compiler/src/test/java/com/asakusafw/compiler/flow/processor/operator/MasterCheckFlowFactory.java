/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.MasterCheck;

/**
 * {@link MasterCheckFlow}に関する演算子ファクトリークラス。
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class MasterCheckFlowFactory {
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        /**
         * masterの引き当てに成功したmodel
         */
        public final Source<Ex1> found;
        /**
         * masterの引き当てに失敗したmodel
         */
        public final Source<Ex1> missed;
        Simple(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(MasterCheck.class);
            builder.declare(MasterCheckFlow.class, MasterCheckFlowImpl.class, "simple");
            builder.declareParameter(Ex2.class);
            builder.declareParameter(Ex1.class);
            builder.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("found", Ex1.class);
            builder.addOutput("missed", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("master", master);
            resolver.resolveInput("model", model);
            this.found = resolver.resolveOutput("found");
            this.missed = resolver.resolveOutput("missed");
        }
    }
    /**
     * 通常の演算子。
     * @param master マスタ
     * @param model モデル
     * @return 生成した演算子オブジェクト
     */
    public MasterCheckFlowFactory.Simple simple(Source<Ex2> master, Source<Ex1> model) {
        return new MasterCheckFlowFactory.Simple(master, model);
    }
    /**
     * セレクタつき演算子。
     */
    public static final class Selection implements Operator {
        /**
         * masterの引き当てに成功したmodel
         */
        public final Source<Ex1> found;
        /**
         * masterの引き当てに失敗したmodel
         */
        public final Source<Ex1> missed;
        Selection(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterCheck.class);
            builder0.declare(MasterCheckFlow.class, MasterCheckFlowImpl.class, "selection");
            builder0.declareParameter(Ex2.class);
            builder0.declareParameter(Ex1.class);
            builder0.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder0.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder0.addOutput("found", Ex1.class);
            builder0.addOutput("missed", Ex1.class);
            builder0.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex1.class})));
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver0 = builder0.toResolver();
            resolver0.resolveInput("master", master);
            resolver0.resolveInput("model", model);
            this.found = resolver0.resolveOutput("found");
            this.missed = resolver0.resolveOutput("missed");
        }
    }
    /**
     * セレクタつき演算子。
     * @param master マスタ
     * @param model モデル
     * @return 生成した演算子オブジェクト
     */
    public MasterCheckFlowFactory.Selection selection(Source<Ex2> master, Source<Ex1> model) {
        return new MasterCheckFlowFactory.Selection(master, model);
    }
}