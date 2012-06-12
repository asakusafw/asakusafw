/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import com.asakusafw.vocabulary.operator.MasterBranch;

/**
 * {@link MasterBranchFlow}に関する演算子ファクトリークラス。
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class MasterBranchFlowFactory {
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
        Simple(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(MasterBranch.class);
            builder.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "simple");
            builder.declareParameter(Ex2.class);
            builder.declareParameter(Ex1.class);
            builder.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("high", Ex1.class);
            builder.addOutput("low", Ex1.class);
            builder.addOutput("stop", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("master", master);
            resolver.resolveInput("model", model);
            this.high = resolver.resolveOutput("high");
            this.low = resolver.resolveOutput("low");
            this.stop = resolver.resolveOutput("stop");
        }
    }
    /**
     * 通常の演算子。
     * @param master マスタ
     * @param model 対象のモデル
     * @return 生成した演算子オブジェクト
     */
    public MasterBranchFlowFactory.Simple simple(Source<Ex2> master, Source<Ex1> model) {
        return new MasterBranchFlowFactory.Simple(master, model);
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
        WithParameter(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterBranch.class);
            builder0.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex2.class);
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder0.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder0.addOutput("high", Ex1.class);
            builder0.addOutput("low", Ex1.class);
            builder0.addOutput("stop", Ex1.class);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver0 = builder0.toResolver();
            resolver0.resolveInput("master", master);
            resolver0.resolveInput("model", model);
            this.high = resolver0.resolveOutput("high");
            this.low = resolver0.resolveOutput("low");
            this.stop = resolver0.resolveOutput("stop");
        }
    }
    /**
     * パラメーター付きの演算子。
     * @param master マスタ
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     */
    public MasterBranchFlowFactory.WithParameter withParameter(Source<Ex2> master, Source<Ex1> model, int parameter) {
        return new MasterBranchFlowFactory.WithParameter(master, model, parameter);
    }
    /**
     * セレクタつきの演算子。
     */
    public static final class Selection implements Operator {
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
        Selection(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(MasterBranch.class);
            builder1.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "selection");
            builder1.declareParameter(Ex2.class);
            builder1.declareParameter(Ex1.class);
            builder1.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder1.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder1.addOutput("high", Ex1.class);
            builder1.addOutput("low", Ex1.class);
            builder1.addOutput("stop", Ex1.class);
            builder1.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex1.class})));
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver1 = builder1.toResolver();
            resolver1.resolveInput("master", master);
            resolver1.resolveInput("model", model);
            this.high = resolver1.resolveOutput("high");
            this.low = resolver1.resolveOutput("low");
            this.stop = resolver1.resolveOutput("stop");
        }
    }
    /**
     * セレクタつきの演算子。
     * @param master マスタ
     * @param model 対象のモデル
     * @return 生成した演算子オブジェクト
     */
    public MasterBranchFlowFactory.Selection selection(Source<Ex2> master, Source<Ex1> model) {
        return new MasterBranchFlowFactory.Selection(master, model);
    }
    /**
     * セレクタおよびパラメータつきの演算子 (セレクタのパラメータはなし)。
     */
    public static final class SelectionWithParameter0 implements Operator {
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
        SelectionWithParameter0(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder2 = new OperatorDescription.Builder(MasterBranch.class);
            builder2.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "selectionWithParameter0");
            builder2.declareParameter(Ex2.class);
            builder2.declareParameter(Ex1.class);
            builder2.declareParameter(int.class);
            builder2.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder2.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder2.addOutput("high", Ex1.class);
            builder2.addOutput("low", Ex1.class);
            builder2.addOutput("stop", Ex1.class);
            builder2.addParameter("parameter", int.class, parameter);
            builder2.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex1.class})));
            builder2.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver2 = builder2.toResolver();
            resolver2.resolveInput("master", master);
            resolver2.resolveInput("model", model);
            this.high = resolver2.resolveOutput("high");
            this.low = resolver2.resolveOutput("low");
            this.stop = resolver2.resolveOutput("stop");
        }
    }
    /**
     * セレクタおよびパラメータつきの演算子 (セレクタのパラメータはなし)。
     * @param master マスタ
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     */
    public MasterBranchFlowFactory.SelectionWithParameter0 selectionWithParameter0(Source<Ex2> master, Source<Ex1> model
            , int parameter) {
        return new MasterBranchFlowFactory.SelectionWithParameter0(master, model, parameter);
    }
    /**
     * セレクタおよびパラメータつきの演算子 (セレクタのパラメータはなし)。
     */
    public static final class SelectionWithParameter1 implements Operator {
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
        SelectionWithParameter1(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder3 = new OperatorDescription.Builder(MasterBranch.class);
            builder3.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "selectionWithParameter1");
            builder3.declareParameter(Ex2.class);
            builder3.declareParameter(Ex1.class);
            builder3.declareParameter(int.class);
            builder3.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder3.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder3.addOutput("high", Ex1.class);
            builder3.addOutput("low", Ex1.class);
            builder3.addOutput("stop", Ex1.class);
            builder3.addParameter("parameter", int.class, parameter);
            builder3.addAttribute(new OperatorHelper("selectorWithParameter", Arrays.asList(new Class<?>[]{List.class, 
                        Ex1.class, int.class})));
            builder3.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver3 = builder3.toResolver();
            resolver3.resolveInput("master", master);
            resolver3.resolveInput("model", model);
            this.high = resolver3.resolveOutput("high");
            this.low = resolver3.resolveOutput("low");
            this.stop = resolver3.resolveOutput("stop");
        }
    }
    /**
     * セレクタおよびパラメータつきの演算子 (セレクタのパラメータはなし)。
     * @param master マスタ
     * @param model 対象のモデル
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     */
    public MasterBranchFlowFactory.SelectionWithParameter1 selectionWithParameter1(Source<Ex2> master, Source<Ex1> model
            , int parameter) {
        return new MasterBranchFlowFactory.SelectionWithParameter1(master, model, parameter);
    }
}