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
import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Extract;
/**
 * {@link ExtractFlow}に関する演算子ファクトリークラス。
 * @see ExtractFlow
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class ExtractFlowFactory {
    /**
     * valueを加算する。
     */
    public static final class Op1 implements Operator {
        private final FlowElementResolver $;
        /**
         *  value + 1
         */
        public final Source<Ex1> r1;
        Op1(Source<Ex1> a1) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Extract.class);
            builder.declare(ExtractFlow.class, ExtractFlowImpl.class, "op1");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(Result.class);
            builder.addInput("a1", a1);
            builder.addOutput("r1", Ex1.class);
            builder.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExtractFlowFactory.Op1 as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * valueを加算する。
     * @param a1 入力
     * @return 生成した演算子オブジェクト
     * @see ExtractFlow#op1(Ex1, Result)
     */
    public ExtractFlowFactory.Op1 op1(Source<Ex1> a1) {
        return new ExtractFlowFactory.Op1(a1);
    }
    /**
     * valueを加算する。
     */
    public static final class Op2 implements Operator {
        private final FlowElementResolver $;
        /**
         *  value + 1
         */
        public final Source<Ex1> r1;
        /**
         *  value + 2
         */
        public final Source<Ex2> r2;
        Op2(Source<Ex1> a1) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Extract.class);
            builder0.declare(ExtractFlow.class, ExtractFlowImpl.class, "op2");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(Result.class);
            builder0.declareParameter(Result.class);
            builder0.addInput("a1", a1);
            builder0.addOutput("r1", Ex1.class);
            builder0.addOutput("r2", Ex2.class);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName0 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExtractFlowFactory.Op2 as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * valueを加算する。
     * @param a1 入力
     * @return 生成した演算子オブジェクト
     * @see ExtractFlow#op2(Ex1, Result, Result)
     */
    public ExtractFlowFactory.Op2 op2(Source<Ex1> a1) {
        return new ExtractFlowFactory.Op2(a1);
    }
    /**
     * valueを加算する。
     */
    public static final class Op3 implements Operator {
        private final FlowElementResolver $;
        /**
         *  value + 1
         */
        public final Source<Ex1> r1;
        /**
         *  value + 2
         */
        public final Source<Ex2> r2;
        /**
         *  value + 3
         */
        public final Source<Ex1> r3;
        Op3(Source<Ex1> a1) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(Extract.class);
            builder1.declare(ExtractFlow.class, ExtractFlowImpl.class, "op3");
            builder1.declareParameter(Ex1.class);
            builder1.declareParameter(Result.class);
            builder1.declareParameter(Result.class);
            builder1.declareParameter(Result.class);
            builder1.addInput("a1", a1);
            builder1.addOutput("r1", Ex1.class);
            builder1.addOutput("r2", Ex2.class);
            builder1.addOutput("r3", Ex1.class);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder1.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
            this.r3 = this.$.resolveOutput("r3");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName1 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExtractFlowFactory.Op3 as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * valueを加算する。
     * @param a1 入力
     * @return 生成した演算子オブジェクト
     * @see ExtractFlow#op3(Ex1, Result, Result, Result)
     */
    public ExtractFlowFactory.Op3 op3(Source<Ex1> a1) {
        return new ExtractFlowFactory.Op3(a1);
    }
    /**
     * valueを加算する。
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         *  value + param
         */
        public final Source<Ex2> r1;
        WithParameter(Source<Ex1> a1, int parameter) {
            OperatorDescription.Builder builder2 = new OperatorDescription.Builder(Extract.class);
            builder2.declare(ExtractFlow.class, ExtractFlowImpl.class, "withParameter");
            builder2.declareParameter(Ex1.class);
            builder2.declareParameter(Result.class);
            builder2.declareParameter(int.class);
            builder2.addInput("a1", a1);
            builder2.addOutput("r1", Ex2.class);
            builder2.addParameter("parameter", int.class, parameter);
            builder2.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder2.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
        }
        /**
         * この演算子の名前を設定する。
         * @param newName2 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExtractFlowFactory.WithParameter as(String newName2) {
            this.$.setName(newName2);
            return this;
        }
    }
    /**
     * valueを加算する。
     * @param a1 入力
     * @param parameter パラメーター
     * @return 生成した演算子オブジェクト
     * @see ExtractFlow#withParameter(Ex1, Result, int)
     */
    public ExtractFlowFactory.WithParameter withParameter(Source<Ex1> a1, int parameter) {
        return new ExtractFlowFactory.WithParameter(a1, parameter);
    }
}