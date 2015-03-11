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
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;

/**
 * {@link MasterJoinUpdateFlow}に関する演算子ファクトリークラス。
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class MasterJoinUpdateFlowFactory {
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        /**
         * 引き当ておよび更新が成功したデータ
         */
        public final Source<Ex1> updated;
        /**
         * 引き当てに失敗したデータ
         */
        public final Source<Ex1> missed;
        Simple(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(MasterJoinUpdate.class);
            builder.declare(MasterJoinUpdateFlow.class, MasterJoinUpdateFlowImpl.class, "simple");
            builder.declareParameter(Ex2.class);
            builder.declareParameter(Ex1.class);
            builder.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("updated", Ex1.class);
            builder.addOutput("missed", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("master", master);
            resolver.resolveInput("model", model);
            this.updated = resolver.resolveOutput("updated");
            this.missed = resolver.resolveOutput("missed");
        }
    }
    /**
     * 通常の演算子。
     * @param master マスタ
     * @param model モデル
     * @return 生成した演算子オブジェクト
     */
    public MasterJoinUpdateFlowFactory.Simple simple(Source<Ex2> master, Source<Ex1> model) {
        return new MasterJoinUpdateFlowFactory.Simple(master, model);
    }
    /**
     * パラメータつき演算子。
     */
    public static final class WithParameter implements Operator {
        /**
         * 引き当ておよび更新が成功したデータ
         */
        public final Source<Ex1> updated;
        /**
         * 引き当てに失敗したデータ
         */
        public final Source<Ex1> missed;
        WithParameter(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterJoinUpdate.class);
            builder0.declare(MasterJoinUpdateFlow.class, MasterJoinUpdateFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex2.class);
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder0.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder0.addOutput("updated", Ex1.class);
            builder0.addOutput("missed", Ex1.class);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver0 = builder0.toResolver();
            resolver0.resolveInput("master", master);
            resolver0.resolveInput("model", model);
            this.updated = resolver0.resolveOutput("updated");
            this.missed = resolver0.resolveOutput("missed");
        }
    }
    /**
     * パラメータつき演算子。
     * @param master マスタ
     * @param model モデル
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     */
    public MasterJoinUpdateFlowFactory.WithParameter withParameter(Source<Ex2> master, Source<Ex1> model, int parameter) 
            {
        return new MasterJoinUpdateFlowFactory.WithParameter(master, model, parameter);
    }
    /**
     * セレクタつき演算子。
     */
    public static final class Selection implements Operator {
        /**
         * 引き当ておよび更新が成功したデータ
         */
        public final Source<Ex1> updated;
        /**
         * 引き当てに失敗したデータ
         */
        public final Source<Ex1> missed;
        Selection(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(MasterJoinUpdate.class);
            builder1.declare(MasterJoinUpdateFlow.class, MasterJoinUpdateFlowImpl.class, "selection");
            builder1.declareParameter(Ex2.class);
            builder1.declareParameter(Ex1.class);
            builder1.addInput("master", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder1.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(
                    new ShuffleKey.Order[]{})));
            builder1.addOutput("updated", Ex1.class);
            builder1.addOutput("missed", Ex1.class);
            builder1.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex1.class})));
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver1 = builder1.toResolver();
            resolver1.resolveInput("master", master);
            resolver1.resolveInput("model", model);
            this.updated = resolver1.resolveOutput("updated");
            this.missed = resolver1.resolveOutput("missed");
        }
    }
    /**
     * セレクタつき演算子。
     * @param master マスタ
     * @param model モデル
     * @return 生成した演算子オブジェクト
     */
    public MasterJoinUpdateFlowFactory.Selection selection(Source<Ex2> master, Source<Ex1> model) {
        return new MasterJoinUpdateFlowFactory.Selection(master, model);
    }
}