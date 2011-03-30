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
package com.asakusafw.compiler.flow.testing.operator;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.Connectivity;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.Fold;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Generated;
/**
 * {@link ExOperator}に関する演算子ファクトリークラス。
 * @see ExOperator
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class ExOperatorFactory {
    /**
     */
    public static final class Summarize implements Operator {
        private final FlowElementResolver $;
        /**
         */
        public final Source<ExSummarized> out;
        Summarize(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Summarize.class);
            builder.declare(ExOperator.class, ExOperatorImpl.class, "summarize");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("out", ExSummarized.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.Summarize as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * @param model
     * @return 生成した演算子オブジェクト
     * @see ExOperator#summarize(Ex1)
     */
    public ExOperatorFactory.Summarize summarize(Source<Ex1> model) {
        return new ExOperatorFactory.Summarize(model);
    }
    /**
     */
    public static final class Update implements Operator {
        private final FlowElementResolver $;
        /**
         * 結果
         */
        public final Source<Ex1> out;
        Update(Source<Ex1> model, int value) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Update.class);
            builder0.declare(ExOperator.class, ExOperatorImpl.class, "update");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("model", Ex1.class);
            builder0.addOutput("out", Ex1.class);
            builder0.addParameter("value", int.class, value);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName0 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.Update as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * @param model
     * @param value
     * @return 生成した演算子オブジェクト
     * @see ExOperator#update(Ex1, int)
     */
    public ExOperatorFactory.Update update(Source<Ex1> model, int value) {
        return new ExOperatorFactory.Update(model, value);
    }
    /**
     */
    public static final class Random implements Operator {
        private final FlowElementResolver $;
        /**
         * 結果
         */
        public final Source<Ex1> out;
        Random(Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Update.class);
            builder1.declare(ExOperator.class, ExOperatorImpl.class, "random");
            builder1.declareParameter(Ex1.class);
            builder1.addInput("model", Ex1.class);
            builder1.addOutput("out", Ex1.class);
            builder1.addAttribute(ObservationCount.AT_MOST_ONCE);
            this.$ = builder1.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName1 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.Random as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * @param model
     * @return 生成した演算子オブジェクト
     * @see ExOperator#random(Ex1)
     */
    public ExOperatorFactory.Random random(Source<Ex1> model) {
        return new ExOperatorFactory.Random(model);
    }
    /**
     */
    public static final class Error implements Operator {
        private final FlowElementResolver $;
        /**
         * 結果
         */
        public final Source<Ex1> out;
        Error(Source<Ex1> model) {
            OperatorDescription.Builder builder2 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Update.class);
            builder2.declare(ExOperator.class, ExOperatorImpl.class, "error");
            builder2.declareParameter(Ex1.class);
            builder2.addInput("model", Ex1.class);
            builder2.addOutput("out", Ex1.class);
            builder2.addAttribute(ObservationCount.AT_LEAST_ONCE);
            this.$ = builder2.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName2 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.Error as(String newName2) {
            this.$.setName(newName2);
            return this;
        }
    }
    /**
     * @param model
     * @return 生成した演算子オブジェクト
     * @see ExOperator#error(Ex1)
     */
    public ExOperatorFactory.Error error(Source<Ex1> model) {
        return new ExOperatorFactory.Error(model);
    }
    /**
     */
    public static final class FoldAdd implements Operator {
        private final FlowElementResolver $;
        /**
         * 畳み込みの結果
         */
        public final Source<Ex1> out;
        FoldAdd(Source<Ex1> in) {
            OperatorDescription.Builder builder3 = new OperatorDescription.Builder(Fold.class);
            builder3.declare(ExOperator.class, ExOperatorImpl.class, "foldAdd");
            builder3.declareParameter(Ex1.class);
            builder3.declareParameter(Ex1.class);
            builder3.addInput("in", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"STRING"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder3.addOutput("out", Ex1.class);
            builder3.addAttribute(FlowBoundary.SHUFFLE);
            builder3.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder3.toResolver();
            this.$.resolveInput("in", in);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName3 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.FoldAdd as(String newName3) {
            this.$.setName(newName3);
            return this;
        }
    }
    /**
     * @param in
     * @return 生成した演算子オブジェクト
     * @see ExOperator#foldAdd(Ex1, Ex1)
     */
    public ExOperatorFactory.FoldAdd foldAdd(Source<Ex1> in) {
        return new ExOperatorFactory.FoldAdd(in);
    }
    /**
     * <p>なお、この演算子の出力は結線しなくても自動的に停止演算子に結線される。</p>
     */
    public static final class Logging implements Operator {
        private final FlowElementResolver $;
        /**
         * 入力された内容
         */
        public final Source<Ex1> out;
        Logging(Source<Ex1> ex1) {
            OperatorDescription.Builder builder4 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Logging.class);
            builder4.declare(ExOperator.class, ExOperatorImpl.class, "logging");
            builder4.declareParameter(Ex1.class);
            builder4.addInput("ex1", Ex1.class);
            builder4.addOutput("out", Ex1.class);
            builder4.addAttribute(ObservationCount.AT_LEAST_ONCE);
            builder4.addAttribute(Connectivity.OPTIONAL);
            builder4.addAttribute(com.asakusafw.vocabulary.operator.Logging.Level.INFO);
            this.$ = builder4.toResolver();
            this.$.resolveInput("ex1", ex1);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName4 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.Logging as(String newName4) {
            this.$.setName(newName4);
            return this;
        }
    }
    /**
     * <p>なお、この演算子の出力は結線しなくても自動的に停止演算子に結線される。</p>
     * @param ex1
     * @return 生成した演算子オブジェクト
     * @see ExOperator#logging(Ex1)
     */
    public ExOperatorFactory.Logging logging(Source<Ex1> ex1) {
        return new ExOperatorFactory.Logging(ex1);
    }
    /**
     */
    public static final class CogroupAdd implements Operator {
        private final FlowElementResolver $;
        /**
         */
        public final Source<Ex1> result;
        CogroupAdd(Source<Ex1> list) {
            OperatorDescription.Builder builder5 = new OperatorDescription.Builder(CoGroup.class);
            builder5.declare(ExOperator.class, ExOperatorImpl.class, "cogroupAdd");
            builder5.declareParameter(List.class);
            builder5.declareParameter(Result.class);
            builder5.addInput("list", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"STRING"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("SID", ShuffleKey.Direction.ASC)})));
            builder5.addOutput("result", Ex1.class);
            builder5.addAttribute(FlowBoundary.SHUFFLE);
            builder5.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder5.toResolver();
            this.$.resolveInput("list", list);
            this.result = this.$.resolveOutput("result");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName5 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.CogroupAdd as(String newName5) {
            this.$.setName(newName5);
            return this;
        }
    }
    /**
     * @param list
     * @return 生成した演算子オブジェクト
     * @see ExOperator#cogroupAdd(List, Result)
     */
    public ExOperatorFactory.CogroupAdd cogroupAdd(Source<Ex1> list) {
        return new ExOperatorFactory.CogroupAdd(list);
    }
    /**
     */
    public static final class Cogroup implements Operator {
        private final FlowElementResolver $;
        /**
         */
        public final Source<Ex1> r1;
        /**
         */
        public final Source<Ex2> r2;
        Cogroup(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder6 = new OperatorDescription.Builder(CoGroup.class);
            builder6.declare(ExOperator.class, ExOperatorImpl.class, "cogroup");
            builder6.declareParameter(List.class);
            builder6.declareParameter(List.class);
            builder6.declareParameter(Result.class);
            builder6.declareParameter(Result.class);
            builder6.addInput("ex1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("sid", ShuffleKey.Direction.ASC)})));
            builder6.addInput("ex2", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("string", ShuffleKey.Direction.DESC)})));
            builder6.addOutput("r1", Ex1.class);
            builder6.addOutput("r2", Ex2.class);
            builder6.addAttribute(FlowBoundary.SHUFFLE);
            builder6.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder6.toResolver();
            this.$.resolveInput("ex1", ex1);
            this.$.resolveInput("ex2", ex2);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName6 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.Cogroup as(String newName6) {
            this.$.setName(newName6);
            return this;
        }
    }
    /**
     * @param ex1
     * @param ex2
     * @return 生成した演算子オブジェクト
     * @see ExOperator#cogroup(List, List, Result, Result)
     */
    public ExOperatorFactory.Cogroup cogroup(Source<Ex1> ex1, Source<Ex2> ex2) {
        return new ExOperatorFactory.Cogroup(ex1, ex2);
    }
    /**
     */
    public static final class Branch implements Operator {
        private final FlowElementResolver $;
        /**
         */
        public final Source<Ex1> yes;
        /**
         */
        public final Source<Ex1> no;
        /**
         */
        public final Source<Ex1> cancel;
        Branch(Source<Ex1> model) {
            OperatorDescription.Builder builder7 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Branch.class);
            builder7.declare(ExOperator.class, ExOperatorImpl.class, "branch");
            builder7.declareParameter(Ex1.class);
            builder7.addInput("model", Ex1.class);
            builder7.addOutput("yes", Ex1.class);
            builder7.addOutput("no", Ex1.class);
            builder7.addOutput("cancel", Ex1.class);
            builder7.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder7.toResolver();
            this.$.resolveInput("model", model);
            this.yes = this.$.resolveOutput("yes");
            this.no = this.$.resolveOutput("no");
            this.cancel = this.$.resolveOutput("cancel");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName7 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExOperatorFactory.Branch as(String newName7) {
            this.$.setName(newName7);
            return this;
        }
    }
    /**
     * @param model
     * @return 生成した演算子オブジェクト
     * @see ExOperator#branch(Ex1)
     */
    public ExOperatorFactory.Branch branch(Source<Ex1> model) {
        return new ExOperatorFactory.Branch(model);
    }
}