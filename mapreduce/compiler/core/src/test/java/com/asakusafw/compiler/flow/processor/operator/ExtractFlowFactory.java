/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>ExtractFlow</code>.
 * @see ExtractFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(ExtractFlow.class) public class ExtractFlowFactory {
    /**
     * value of input + 1.
     */
    public static final class Op1 implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
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
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExtractFlowFactory.Op1 as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * value of input + 1.
     * @param a1  input
     * @return the created operator object
     * @see ExtractFlow#op1(Ex1, Result)
     */
    @OperatorInfo(kind = Extract.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class)}, parameter = {}) public ExtractFlowFactory.
            Op1 op1(Source<Ex1> a1) {
        return new ExtractFlowFactory.Op1(a1);
    }
    /**
     * value of input + 1 to r1, value of input + 2 to r2.
     */
    public static final class Op2 implements Operator {
        private final FlowElementResolver $;
        /**
         *  result 1
         */
        public final Source<Ex1> r1;
        /**
         *  result 2
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
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExtractFlowFactory.Op2 as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * value of input + 1 to r1, value of input + 2 to r2.
     * @param a1  input
     * @return the created operator object
     * @see ExtractFlow#op2(Ex1, Result, Result)
     */
    @OperatorInfo(kind = Extract.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class),@OperatorInfo.Output(name = "r2", type = Ex2.
                class)}, parameter = {}) public ExtractFlowFactory.Op2 op2(Source<Ex1> a1) {
        return new ExtractFlowFactory.Op2(a1);
    }
    /**
     * value of input + N to rN.
     */
    public static final class Op3 implements Operator {
        private final FlowElementResolver $;
        /**
         *  result 1
         */
        public final Source<Ex1> r1;
        /**
         *  result 2
         */
        public final Source<Ex2> r2;
        /**
         *  result 3
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
         * Configures the name of this operator.
         * @param newName1 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExtractFlowFactory.Op3 as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * value of input + N to rN.
     * @param a1  input
     * @return the created operator object
     * @see ExtractFlow#op3(Ex1, Result, Result, Result)
     */
    @OperatorInfo(kind = Extract.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class),@OperatorInfo.Output(name = "r2", type = Ex2.
                class),@OperatorInfo.Output(name = "r3", type = Ex1.class)}, parameter = {}) public ExtractFlowFactory.
            Op3 op3(Source<Ex1> a1) {
        return new ExtractFlowFactory.Op3(a1);
    }
    /**
     * value of input + parameter.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
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
         * Configures the name of this operator.
         * @param newName2 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExtractFlowFactory.WithParameter as(String newName2) {
            this.$.setName(newName2);
            return this;
        }
    }
    /**
     * value of input + parameter.
     * @param a1  input
     * @param parameter  additional parameter
     * @return the created operator object
     * @see ExtractFlow#withParameter(Ex1, Result, int)
     */
    @OperatorInfo(kind = Extract.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex2.class)}, parameter = {@OperatorInfo.Parameter(name = 
                "parameter", type = int.class, position = 1)}) public ExtractFlowFactory.WithParameter withParameter(
            Source<Ex1> a1, int parameter) {
        return new ExtractFlowFactory.WithParameter(a1, parameter);
    }
}