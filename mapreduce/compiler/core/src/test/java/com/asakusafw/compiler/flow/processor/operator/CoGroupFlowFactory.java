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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.flow.processor.InputBuffer;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.KeyInfo;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>CoGroupFlow</code>.
 * @see CoGroupFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(CoGroupFlow.class) public class CoGroupFlowFactory {
    /**
     * returns total of values.
     */
    public static final class Op1 implements Operator {
        private final FlowElementResolver $;
        /**
         *  sink
         */
        public final Source<Ex1> r1;
        Op1(Source<Ex1> a1) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "op1");
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("r1", Ex1.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            builder.addAttribute(InputBuffer.EXPAND);
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
        public CoGroupFlowFactory.Op1 as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * returns total of values.
     * @param a1  group
     * @return the created operator object
     * @see CoGroupFlow#op1(List, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class)}, parameter = {}) public CoGroupFlowFactory.
            Op1 op1(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> a1) {
        return new CoGroupFlowFactory.Op1(a1);
    }
    /**
     * returns total of values w/ using backing store.
     */
    public static final class Swap implements Operator {
        private final FlowElementResolver $;
        /**
         *  sink
         */
        public final Source<Ex1> r1;
        Swap(Source<Ex1> a1) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(CoGroup.class);
            builder0.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "swap");
            builder0.declareParameter(List.class);
            builder0.declareParameter(Result.class);
            builder0.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("r1", Ex1.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            builder0.addAttribute(InputBuffer.ESCAPE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
        }
        /**
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public CoGroupFlowFactory.Swap as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * returns total of values w/ using backing store.
     * @param a1  group
     * @return the created operator object
     * @see CoGroupFlow#swap(List, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class)}, parameter = {}) public CoGroupFlowFactory.
            Swap swap(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> a1) {
        return new CoGroupFlowFactory.Swap(a1);
    }
    /**
     * returns total of values w/ ordered.
     */
    public static final class Sorted implements Operator {
        private final FlowElementResolver $;
        /**
         *  sink
         */
        public final Source<Ex1> r1;
        Sorted(Source<Ex1> a1) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(CoGroup.class);
            builder1.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "sorted");
            builder1.declareParameter(List.class);
            builder1.declareParameter(Result.class);
            builder1.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.DESC)})));
            builder1.addOutput("r1", Ex1.class);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            builder1.addAttribute(InputBuffer.EXPAND);
            this.$ = builder1.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
        }
        /**
         * Configures the name of this operator.
         * @param newName1 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public CoGroupFlowFactory.Sorted as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * returns total of values w/ ordered.
     * @param a1  group
     * @return the created operator object
     * @see CoGroupFlow#sorted(List, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class)}, parameter = {}) public CoGroupFlowFactory.
            Sorted sorted(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {@KeyInfo.Order(direction = 
                KeyInfo.Direction.DESC, expression = "value")}) Source<Ex1> a1) {
        return new CoGroupFlowFactory.Sorted(a1);
    }
    /**
     * a1 to r2, a2 to r1.
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
        Op2(Source<Ex1> a1, Source<Ex2> a2) {
            OperatorDescription.Builder builder2 = new OperatorDescription.Builder(CoGroup.class);
            builder2.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "op2");
            builder2.declareParameter(List.class);
            builder2.declareParameter(List.class);
            builder2.declareParameter(Result.class);
            builder2.declareParameter(Result.class);
            builder2.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder2.addInput("a2", a2, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder2.addOutput("r1", Ex1.class);
            builder2.addOutput("r2", Ex2.class);
            builder2.addAttribute(FlowBoundary.SHUFFLE);
            builder2.addAttribute(ObservationCount.DONT_CARE);
            builder2.addAttribute(InputBuffer.EXPAND);
            this.$ = builder2.toResolver();
            this.$.resolveInput("a1", a1);
            this.$.resolveInput("a2", a2);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
        }
        /**
         * Configures the name of this operator.
         * @param newName2 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public CoGroupFlowFactory.Op2 as(String newName2) {
            this.$.setName(newName2);
            return this;
        }
    }
    /**
     * a1 to r2, a2 to r1.
     * @param a1  group 1
     * @param a2  group 2
     * @return the created operator object
     * @see CoGroupFlow#op2(List, List, Result, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0),@
                OperatorInfo.Input(name = "a2", type = Ex2.class, position = 1)}, output = {@OperatorInfo.Output(name = 
                "r1", type = Ex1.class),@OperatorInfo.Output(name = "r2", type = Ex2.class)}, parameter = {}) public 
            CoGroupFlowFactory.Op2 op2(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> 
            a1,@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex2> a2) {
        return new CoGroupFlowFactory.Op2(a1, a2);
    }
    /**
     * a1 to r2, a2 to r3, a3 to r1.
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
        public final Source<Ex1> r2;
        /**
         *  result 3
         */
        public final Source<Ex1> r3;
        Op3(Source<Ex1> a1, Source<Ex1> a2, Source<Ex1> a3) {
            OperatorDescription.Builder builder3 = new OperatorDescription.Builder(CoGroup.class);
            builder3.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "op3");
            builder3.declareParameter(List.class);
            builder3.declareParameter(List.class);
            builder3.declareParameter(List.class);
            builder3.declareParameter(Result.class);
            builder3.declareParameter(Result.class);
            builder3.declareParameter(Result.class);
            builder3.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder3.addInput("a2", a2, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder3.addInput("a3", a3, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder3.addOutput("r1", Ex1.class);
            builder3.addOutput("r2", Ex1.class);
            builder3.addOutput("r3", Ex1.class);
            builder3.addAttribute(FlowBoundary.SHUFFLE);
            builder3.addAttribute(ObservationCount.DONT_CARE);
            builder3.addAttribute(InputBuffer.EXPAND);
            this.$ = builder3.toResolver();
            this.$.resolveInput("a1", a1);
            this.$.resolveInput("a2", a2);
            this.$.resolveInput("a3", a3);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
            this.r3 = this.$.resolveOutput("r3");
        }
        /**
         * Configures the name of this operator.
         * @param newName3 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public CoGroupFlowFactory.Op3 as(String newName3) {
            this.$.setName(newName3);
            return this;
        }
    }
    /**
     * a1 to r2, a2 to r3, a3 to r1.
     * @param a1  group 1
     * @param a2  group 2
     * @param a3  group 3
     * @return the created operator object
     * @see CoGroupFlow#op3(List, List, List, Result, Result, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0),@
                OperatorInfo.Input(name = "a2", type = Ex1.class, position = 1),@OperatorInfo.Input(name = "a3", type = 
                Ex1.class, position = 2)}, output = {@OperatorInfo.Output(name = "r1", type = Ex1.class),@OperatorInfo.
                Output(name = "r2", type = Ex1.class),@OperatorInfo.Output(name = "r3", type = Ex1.class)}, parameter = 
            {}) public CoGroupFlowFactory.Op3 op3(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) 
            Source<Ex1> a1,@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> a2,@KeyInfo
            (group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> a3) {
        return new CoGroupFlowFactory.Op3(a1, a2, a3);
    }
    /**
     * sum of value + parameter * size of a1.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
         */
        public final Source<Ex1> r1;
        WithParameter(Source<Ex1> a1, int parameter) {
            OperatorDescription.Builder builder4 = new OperatorDescription.Builder(CoGroup.class);
            builder4.declare(CoGroupFlow.class, CoGroupFlowImpl.class, "withParameter");
            builder4.declareParameter(List.class);
            builder4.declareParameter(Result.class);
            builder4.declareParameter(int.class);
            builder4.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder4.addOutput("r1", Ex1.class);
            builder4.addParameter("parameter", int.class, parameter);
            builder4.addAttribute(FlowBoundary.SHUFFLE);
            builder4.addAttribute(ObservationCount.DONT_CARE);
            builder4.addAttribute(InputBuffer.EXPAND);
            this.$ = builder4.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
        }
        /**
         * Configures the name of this operator.
         * @param newName4 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public CoGroupFlowFactory.WithParameter as(String newName4) {
            this.$.setName(newName4);
            return this;
        }
    }
    /**
     * sum of value + parameter * size of a1.
     * @param a1  group
     * @param parameter  additional parameter
     * @return the created operator object
     * @see CoGroupFlow#withParameter(List, Result, int)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class)}, parameter = {@OperatorInfo.Parameter(name = 
                "parameter", type = int.class, position = 1)}) public CoGroupFlowFactory.WithParameter withParameter(@
            KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> a1, int parameter) {
        return new CoGroupFlowFactory.WithParameter(a1, parameter);
    }
}