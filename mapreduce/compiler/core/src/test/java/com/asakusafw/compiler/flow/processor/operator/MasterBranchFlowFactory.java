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
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.KeyInfo;
import com.asakusafw.vocabulary.operator.MasterBranch;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>MasterBranchFlow</code>.
 * @see MasterBranchFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(MasterBranchFlow.class) public class 
        MasterBranchFlowFactory {
    /**
     * simple.
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         * high speed.
         */
        public final Source<Ex1> high;
        /**
         * low speed.
         */
        public final Source<Ex1> low;
        /**
         * stopped.
         */
        public final Source<Ex1> stop;
        Simple(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(MasterBranch.class);
            builder.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "simple");
            builder.declareParameter(Ex2.class);
            builder.declareParameter(Ex1.class);
            builder.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("high", model);
            builder.addOutput("low", model);
            builder.addOutput("stop", model);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.high = this.$.resolveOutput("high");
            this.low = this.$.resolveOutput("low");
            this.stop = this.$.resolveOutput("stop");
        }
        /**
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterBranchFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * simple.
     * @param master  the master data
     * @param model  target data model
     * @return the created operator object
     * @see MasterBranchFlow#simple(Ex2, Ex1)
     */
    @OperatorInfo(kind = MasterBranch.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, position = 
                0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@OperatorInfo.Output(
                name = "high", type = Ex1.class),@OperatorInfo.Output(name = "low", type = Ex1.class),@OperatorInfo.
                Output(name = "stop", type = Ex1.class)}, parameter = {}) public MasterBranchFlowFactory.Simple simple(@
            KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex2> master,@KeyInfo(group = {@
                KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> model) {
        return new MasterBranchFlowFactory.Simple(master, model);
    }
    /**
     * parameterized.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         * high speed.
         */
        public final Source<Ex1> high;
        /**
         * low speed.
         */
        public final Source<Ex1> low;
        /**
         * stopped.
         */
        public final Source<Ex1> stop;
        WithParameter(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterBranch.class);
            builder0.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex2.class);
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("high", model);
            builder0.addOutput("low", model);
            builder0.addOutput("stop", model);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.high = this.$.resolveOutput("high");
            this.low = this.$.resolveOutput("low");
            this.stop = this.$.resolveOutput("stop");
        }
        /**
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterBranchFlowFactory.WithParameter as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * parameterized.
     * @param master  the master data
     * @param model  target data model
     * @param parameter  additional parameter
     * @return the created operator object
     * @see MasterBranchFlow#withParameter(Ex2, Ex1, int)
     */
    @OperatorInfo(kind = MasterBranch.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, position = 
                0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@OperatorInfo.Output(
                name = "high", type = Ex1.class),@OperatorInfo.Output(name = "low", type = Ex1.class),@OperatorInfo.
                Output(name = "stop", type = Ex1.class)}, parameter = {@OperatorInfo.Parameter(name = "parameter", type 
                = int.class, position = 2)}) public MasterBranchFlowFactory.WithParameter withParameter(@KeyInfo(group = 
            {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex2> master,@KeyInfo(group = {@KeyInfo.Group(
                expression = "string")}, order = {}) Source<Ex1> model, int parameter) {
        return new MasterBranchFlowFactory.WithParameter(master, model, parameter);
    }
    /**
     * w/ selector.
     */
    public static final class Selection implements Operator {
        private final FlowElementResolver $;
        /**
         * high speed.
         */
        public final Source<Ex1> high;
        /**
         * low speed.
         */
        public final Source<Ex1> low;
        /**
         * stopped.
         */
        public final Source<Ex1> stop;
        Selection(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(MasterBranch.class);
            builder1.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "selection");
            builder1.declareParameter(Ex2.class);
            builder1.declareParameter(Ex1.class);
            builder1.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addOutput("high", model);
            builder1.addOutput("low", model);
            builder1.addOutput("stop", model);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            builder1.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex1.class})));
            this.$ = builder1.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.high = this.$.resolveOutput("high");
            this.low = this.$.resolveOutput("low");
            this.stop = this.$.resolveOutput("stop");
        }
        /**
         * Configures the name of this operator.
         * @param newName1 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterBranchFlowFactory.Selection as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * w/ selector.
     * @param master  the master data
     * @param model  target data model
     * @return the created operator object
     * @see MasterBranchFlow#selection(Ex2, Ex1)
     */
    @OperatorInfo(kind = MasterBranch.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, position = 
                0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@OperatorInfo.Output(
                name = "high", type = Ex1.class),@OperatorInfo.Output(name = "low", type = Ex1.class),@OperatorInfo.
                Output(name = "stop", type = Ex1.class)}, parameter = {}) public MasterBranchFlowFactory.Selection 
            selection(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex2> master,@KeyInfo(
            group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> model) {
        return new MasterBranchFlowFactory.Selection(master, model);
    }
    /**
     * w/ parameter and (non-parameterized) selector.
     */
    public static final class SelectionWithParameter0 implements Operator {
        private final FlowElementResolver $;
        /**
         * high speed.
         */
        public final Source<Ex1> high;
        /**
         * low speed.
         */
        public final Source<Ex1> low;
        /**
         * stopped.
         */
        public final Source<Ex1> stop;
        SelectionWithParameter0(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder2 = new OperatorDescription.Builder(MasterBranch.class);
            builder2.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "selectionWithParameter0");
            builder2.declareParameter(Ex2.class);
            builder2.declareParameter(Ex1.class);
            builder2.declareParameter(int.class);
            builder2.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder2.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder2.addOutput("high", model);
            builder2.addOutput("low", model);
            builder2.addOutput("stop", model);
            builder2.addParameter("parameter", int.class, parameter);
            builder2.addAttribute(FlowBoundary.SHUFFLE);
            builder2.addAttribute(ObservationCount.DONT_CARE);
            builder2.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex1.class})));
            this.$ = builder2.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.high = this.$.resolveOutput("high");
            this.low = this.$.resolveOutput("low");
            this.stop = this.$.resolveOutput("stop");
        }
        /**
         * Configures the name of this operator.
         * @param newName2 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterBranchFlowFactory.SelectionWithParameter0 as(String newName2) {
            this.$.setName(newName2);
            return this;
        }
    }
    /**
     * w/ parameter and (non-parameterized) selector.
     * @param master  the master data
     * @param model  target data model
     * @param parameter  additional parameter
     * @return the created operator object
     * @see MasterBranchFlow#selectionWithParameter0(Ex2, Ex1, int)
     */
    @OperatorInfo(kind = MasterBranch.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, position = 
                0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@OperatorInfo.Output(
                name = "high", type = Ex1.class),@OperatorInfo.Output(name = "low", type = Ex1.class),@OperatorInfo.
                Output(name = "stop", type = Ex1.class)}, parameter = {@OperatorInfo.Parameter(name = "parameter", type 
                = int.class, position = 2)}) public MasterBranchFlowFactory.SelectionWithParameter0 
            selectionWithParameter0(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex2> 
            master,@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> model, int 
            parameter) {
        return new MasterBranchFlowFactory.SelectionWithParameter0(master, model, parameter);
    }
    /**
     * w/ parameter and (parameterized) selector.
     */
    public static final class SelectionWithParameter1 implements Operator {
        private final FlowElementResolver $;
        /**
         * high speed.
         */
        public final Source<Ex1> high;
        /**
         * low speed.
         */
        public final Source<Ex1> low;
        /**
         * stopped.
         */
        public final Source<Ex1> stop;
        SelectionWithParameter1(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder3 = new OperatorDescription.Builder(MasterBranch.class);
            builder3.declare(MasterBranchFlow.class, MasterBranchFlowImpl.class, "selectionWithParameter1");
            builder3.declareParameter(Ex2.class);
            builder3.declareParameter(Ex1.class);
            builder3.declareParameter(int.class);
            builder3.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder3.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder3.addOutput("high", model);
            builder3.addOutput("low", model);
            builder3.addOutput("stop", model);
            builder3.addParameter("parameter", int.class, parameter);
            builder3.addAttribute(FlowBoundary.SHUFFLE);
            builder3.addAttribute(ObservationCount.DONT_CARE);
            builder3.addAttribute(new OperatorHelper("selectorWithParameter", Arrays.asList(new Class<?>[]{List.class, 
                        Ex1.class, int.class})));
            this.$ = builder3.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.high = this.$.resolveOutput("high");
            this.low = this.$.resolveOutput("low");
            this.stop = this.$.resolveOutput("stop");
        }
        /**
         * Configures the name of this operator.
         * @param newName3 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterBranchFlowFactory.SelectionWithParameter1 as(String newName3) {
            this.$.setName(newName3);
            return this;
        }
    }
    /**
     * w/ parameter and (parameterized) selector.
     * @param master  the master data
     * @param model  target data model
     * @param parameter  additional parameter
     * @return the created operator object
     * @see MasterBranchFlow#selectionWithParameter1(Ex2, Ex1, int)
     */
    @OperatorInfo(kind = MasterBranch.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, position = 
                0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@OperatorInfo.Output(
                name = "high", type = Ex1.class),@OperatorInfo.Output(name = "low", type = Ex1.class),@OperatorInfo.
                Output(name = "stop", type = Ex1.class)}, parameter = {@OperatorInfo.Parameter(name = "parameter", type 
                = int.class, position = 2)}) public MasterBranchFlowFactory.SelectionWithParameter1 
            selectionWithParameter1(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex2> 
            master,@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> model, int 
            parameter) {
        return new MasterBranchFlowFactory.SelectionWithParameter1(master, model, parameter);
    }
}