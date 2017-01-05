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
package com.asakusafw.compiler.flow.testing.operator;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

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
import com.asakusafw.vocabulary.flow.processor.InputBuffer;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.Fold;
import com.asakusafw.vocabulary.operator.KeyInfo;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>ExOperator</code>.
 * @see ExOperator
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(ExOperator.class) public class ExOperatorFactory {
    /**
     * Update operator.
     */
    public static final class Update implements Operator {
        private final FlowElementResolver $;
        /**
         * the results
         */
        public final Source<Ex1> out;
        Update(Source<Ex1> model, int value) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Update.class);
            builder.declare(ExOperator.class, ExOperatorImpl.class, "update");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(int.class);
            builder.addInput("model", model);
            builder.addOutput("out", model);
            builder.addParameter("value", int.class, value);
            builder.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.Update as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * Update operator.
     * @param model  target object
     * @param value  value to be set
     * @return the created operator object
     * @see ExOperator#update(Ex1, int)
     */
    @OperatorInfo(kind = com.asakusafw.vocabulary.operator.Update.class, input = {@OperatorInfo.Input(name = "model", 
                type = Ex1.class, position = 0)}, output = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, 
            parameter = {@OperatorInfo.Parameter(name = "value", type = int.class, position = 1)}) public 
            ExOperatorFactory.Update update(Source<Ex1> model, int value) {
        return new ExOperatorFactory.Update(model, value);
    }
    /**
     * Volatile.
     */
    public static final class Random implements Operator {
        private final FlowElementResolver $;
        /**
         * the results
         */
        public final Source<Ex1> out;
        Random(Source<Ex1> model) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Update.class);
            builder0.declare(ExOperator.class, ExOperatorImpl.class, "random");
            builder0.declareParameter(Ex1.class);
            builder0.addInput("model", model);
            builder0.addOutput("out", model);
            builder0.addAttribute(ObservationCount.AT_MOST_ONCE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.Random as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * Volatile.
     * @param model  target object
     * @return the created operator object
     * @see ExOperator#random(Ex1)
     */
    @OperatorInfo(kind = com.asakusafw.vocabulary.operator.Update.class, input = {@OperatorInfo.Input(name = "model", 
                type = Ex1.class, position = 0)}, output = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, 
            parameter = {}) public ExOperatorFactory.Random random(Source<Ex1> model) {
        return new ExOperatorFactory.Random(model);
    }
    /**
     * sticky (raise error).
     */
    public static final class Error implements Operator {
        private final FlowElementResolver $;
        /**
         * the results
         */
        public final Source<Ex1> out;
        Error(Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Update.class);
            builder1.declare(ExOperator.class, ExOperatorImpl.class, "error");
            builder1.declareParameter(Ex1.class);
            builder1.addInput("model", model);
            builder1.addOutput("out", model);
            builder1.addAttribute(ObservationCount.AT_LEAST_ONCE);
            this.$ = builder1.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName1 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.Error as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * sticky (raise error).
     * @param model  target object
     * @return the created operator object
     * @see ExOperator#error(Ex1)
     */
    @OperatorInfo(kind = com.asakusafw.vocabulary.operator.Update.class, input = {@OperatorInfo.Input(name = "model", 
                type = Ex1.class, position = 0)}, output = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, 
            parameter = {}) public ExOperatorFactory.Error error(Source<Ex1> model) {
        return new ExOperatorFactory.Error(model);
    }
    /**
     * branch operator.
     */
    public static final class Branch implements Operator {
        private final FlowElementResolver $;
        /**
         * yes.
         */
        public final Source<Ex1> yes;
        /**
         * no.
         */
        public final Source<Ex1> no;
        /**
         * canceled.
         */
        public final Source<Ex1> cancel;
        Branch(Source<Ex1> model) {
            OperatorDescription.Builder builder2 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Branch.class);
            builder2.declare(ExOperator.class, ExOperatorImpl.class, "branch");
            builder2.declareParameter(Ex1.class);
            builder2.addInput("model", model);
            builder2.addOutput("yes", model);
            builder2.addOutput("no", model);
            builder2.addOutput("cancel", model);
            builder2.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder2.toResolver();
            this.$.resolveInput("model", model);
            this.yes = this.$.resolveOutput("yes");
            this.no = this.$.resolveOutput("no");
            this.cancel = this.$.resolveOutput("cancel");
        }
        /**
         * Configures the name of this operator.
         * @param newName2 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.Branch as(String newName2) {
            this.$.setName(newName2);
            return this;
        }
    }
    /**
     * branch operator.
     * @param model  target model
     * @return the created operator object
     * @see ExOperator#branch(Ex1)
     */
    @OperatorInfo(kind = com.asakusafw.vocabulary.operator.Branch.class, input = {@OperatorInfo.Input(name = "model", 
                type = Ex1.class, position = 0)}, output = {@OperatorInfo.Output(name = "yes", type = Ex1.class),@
                OperatorInfo.Output(name = "no", type = Ex1.class),@OperatorInfo.Output(name = "cancel", type = Ex1.
                class)}, parameter = {}) public ExOperatorFactory.Branch branch(Source<Ex1> model) {
        return new ExOperatorFactory.Branch(model);
    }
    /**
     * summarize operator.
     */
    public static final class Summarize implements Operator {
        private final FlowElementResolver $;
        /**
         *  results
         */
        public final Source<ExSummarized> out;
        Summarize(Source<Ex1> model) {
            OperatorDescription.Builder builder3 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Summarize.class);
            builder3.declare(ExOperator.class, ExOperatorImpl.class, "summarize");
            builder3.declareParameter(Ex1.class);
            builder3.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder3.addOutput("out", ExSummarized.class);
            builder3.addAttribute(FlowBoundary.SHUFFLE);
            builder3.addAttribute(ObservationCount.DONT_CARE);
            builder3.addAttribute(PartialAggregation.PARTIAL);
            this.$ = builder3.toResolver();
            this.$.resolveInput("model", model);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName3 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.Summarize as(String newName3) {
            this.$.setName(newName3);
            return this;
        }
    }
    /**
     * summarize operator.
     * @param model  target object
     * @return the created operator object
     * @see ExOperator#summarize(Ex1)
     */
    @OperatorInfo(kind = com.asakusafw.vocabulary.operator.Summarize.class, input = {@OperatorInfo.Input(name = "model", 
                type = Ex1.class, position = 0)}, output = {@OperatorInfo.Output(name = "out", type = ExSummarized.class
                )}, parameter = {}) public ExOperatorFactory.Summarize summarize(@KeyInfo(group = {@KeyInfo.Group(
                expression = "string")}, order = {}) Source<Ex1> model) {
        return new ExOperatorFactory.Summarize(model);
    }
    /**
     * co-group operator.
     */
    public static final class CogroupAdd implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
         */
        public final Source<Ex1> result;
        CogroupAdd(Source<Ex1> list) {
            OperatorDescription.Builder builder4 = new OperatorDescription.Builder(CoGroup.class);
            builder4.declare(ExOperator.class, ExOperatorImpl.class, "cogroupAdd");
            builder4.declareParameter(List.class);
            builder4.declareParameter(Result.class);
            builder4.addInput("list", list, new ShuffleKey(Arrays.asList(new String[]{"STRING"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("SID", ShuffleKey.Direction.ASC)})));
            builder4.addOutput("result", Ex1.class);
            builder4.addAttribute(FlowBoundary.SHUFFLE);
            builder4.addAttribute(ObservationCount.DONT_CARE);
            builder4.addAttribute(InputBuffer.EXPAND);
            this.$ = builder4.toResolver();
            this.$.resolveInput("list", list);
            this.result = this.$.resolveOutput("result");
        }
        /**
         * Configures the name of this operator.
         * @param newName4 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.CogroupAdd as(String newName4) {
            this.$.setName(newName4);
            return this;
        }
    }
    /**
     * co-group operator.
     * @param list  target list
     * @return the created operator object
     * @see ExOperator#cogroupAdd(List, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "list", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "result", type = Ex1.class)}, parameter = {}) public ExOperatorFactory
            .CogroupAdd cogroupAdd(@KeyInfo(group = {@KeyInfo.Group(expression = "STRING")}, order = {@KeyInfo.Order(
                direction = KeyInfo.Direction.ASC, expression = "SID")}) Source<Ex1> list) {
        return new ExOperatorFactory.CogroupAdd(list);
    }
    /**
     * complex co-group operator.
     */
    public static final class Cogroup implements Operator {
        private final FlowElementResolver $;
        /**
         *  output1
         */
        public final Source<Ex1> r1;
        /**
         *  output2
         */
        public final Source<Ex2> r2;
        Cogroup(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder5 = new OperatorDescription.Builder(CoGroup.class);
            builder5.declare(ExOperator.class, ExOperatorImpl.class, "cogroup");
            builder5.declareParameter(List.class);
            builder5.declareParameter(List.class);
            builder5.declareParameter(Result.class);
            builder5.declareParameter(Result.class);
            builder5.addInput("ex1", ex1, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("sid", ShuffleKey.Direction.ASC)})));
            builder5.addInput("ex2", ex2, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("string", ShuffleKey.Direction.DESC)})));
            builder5.addOutput("r1", Ex1.class);
            builder5.addOutput("r2", Ex2.class);
            builder5.addAttribute(FlowBoundary.SHUFFLE);
            builder5.addAttribute(ObservationCount.DONT_CARE);
            builder5.addAttribute(InputBuffer.EXPAND);
            this.$ = builder5.toResolver();
            this.$.resolveInput("ex1", ex1);
            this.$.resolveInput("ex2", ex2);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
        }
        /**
         * Configures the name of this operator.
         * @param newName5 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.Cogroup as(String newName5) {
            this.$.setName(newName5);
            return this;
        }
    }
    /**
     * complex co-group operator.
     * @param ex1  model1
     * @param ex2  model2
     * @return the created operator object
     * @see ExOperator#cogroup(List, List, Result, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "ex1", type = Ex1.class, position = 0),@
                OperatorInfo.Input(name = "ex2", type = Ex2.class, position = 1)}, output = {@OperatorInfo.Output(name = 
                "r1", type = Ex1.class),@OperatorInfo.Output(name = "r2", type = Ex2.class)}, parameter = {}) public 
            ExOperatorFactory.Cogroup cogroup(@KeyInfo(group = {@KeyInfo.Group(expression = "value")}, order = {@KeyInfo
                .Order(direction = KeyInfo.Direction.ASC, expression = "sid")}) Source<Ex1> ex1,@KeyInfo(group = {@
                KeyInfo.Group(expression = "value")}, order = {@KeyInfo.Order(direction = KeyInfo.Direction.DESC, 
                expression = "string")}) Source<Ex2> ex2) {
        return new ExOperatorFactory.Cogroup(ex1, ex2);
    }
    /**
     * logging operator. 
             * <p>Note that, each output port of this operator will automatically connect to "stop" operator if not connected to any other ports.</p>
     */
    public static final class Logging implements Operator {
        private final FlowElementResolver $;
        /**
         * input data
         */
        public final Source<Ex1> out;
        Logging(Source<Ex1> ex1) {
            OperatorDescription.Builder builder6 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Logging.class);
            builder6.declare(ExOperator.class, ExOperatorImpl.class, "logging");
            builder6.declareParameter(Ex1.class);
            builder6.addInput("ex1", ex1);
            builder6.addOutput("out", ex1);
            builder6.addAttribute(ObservationCount.AT_LEAST_ONCE);
            builder6.addAttribute(Connectivity.OPTIONAL);
            builder6.addAttribute(com.asakusafw.vocabulary.operator.Logging.Level.INFO);
            this.$ = builder6.toResolver();
            this.$.resolveInput("ex1", ex1);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName6 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.Logging as(String newName6) {
            this.$.setName(newName6);
            return this;
        }
    }
    /**
     * logging operator. 
             * <p>Note that, each output port of this operator will automatically connect to "stop" operator if not connected to any other ports.</p>
     * @param ex1  model
     * @return the created operator object
     * @see ExOperator#logging(Ex1)
     */
    @OperatorInfo(kind = com.asakusafw.vocabulary.operator.Logging.class, input = {@OperatorInfo.Input(name = "ex1", 
                type = Ex1.class, position = 0)}, output = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, 
            parameter = {}) public ExOperatorFactory.Logging logging(Source<Ex1> ex1) {
        return new ExOperatorFactory.Logging(ex1);
    }
    /**
     * folding operator.
     */
    public static final class FoldAdd implements Operator {
        private final FlowElementResolver $;
        /**
         * the folding result
         */
        public final Source<Ex1> out;
        FoldAdd(Source<Ex1> in) {
            OperatorDescription.Builder builder7 = new OperatorDescription.Builder(Fold.class);
            builder7.declare(ExOperator.class, ExOperatorImpl.class, "foldAdd");
            builder7.declareParameter(Ex1.class);
            builder7.declareParameter(Ex1.class);
            builder7.addInput("in", in, new ShuffleKey(Arrays.asList(new String[]{"STRING"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder7.addOutput("out", in);
            builder7.addAttribute(FlowBoundary.SHUFFLE);
            builder7.addAttribute(ObservationCount.DONT_CARE);
            builder7.addAttribute(PartialAggregation.DEFAULT);
            this.$ = builder7.toResolver();
            this.$.resolveInput("in", in);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName7 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ExOperatorFactory.FoldAdd as(String newName7) {
            this.$.setName(newName7);
            return this;
        }
    }
    /**
     * folding operator.
     * @param in  felt
     * @return the created operator object
     * @see ExOperator#foldAdd(Ex1, Ex1)
     */
    @OperatorInfo(kind = Fold.class, input = {@OperatorInfo.Input(name = "in", type = Ex1.class, position = 0)}, output 
            = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, parameter = {}) public ExOperatorFactory.FoldAdd 
            foldAdd(@KeyInfo(group = {@KeyInfo.Group(expression = "STRING")}, order = {}) Source<Ex1> in) {
        return new ExOperatorFactory.FoldAdd(in);
    }
}