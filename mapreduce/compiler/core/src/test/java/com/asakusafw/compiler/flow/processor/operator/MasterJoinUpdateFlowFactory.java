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
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>MasterJoinUpdateFlow</code>.
 * @see MasterJoinUpdateFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(MasterJoinUpdateFlow.class) public class 
        MasterJoinUpdateFlowFactory {
    /**
     * simple.
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         * each data model object which is successfully updated
         */
        public final Source<Ex1> updated;
        /**
         * each data model object which does not have corresponding master data
         */
        public final Source<Ex1> missed;
        Simple(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(MasterJoinUpdate.class);
            builder.declare(MasterJoinUpdateFlow.class, MasterJoinUpdateFlowImpl.class, "simple");
            builder.declareParameter(Ex2.class);
            builder.declareParameter(Ex1.class);
            builder.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("updated", model);
            builder.addOutput("missed", model);
            builder.addAttribute(ObservationCount.DONT_CARE);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            this.$ = builder.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.updated = this.$.resolveOutput("updated");
            this.missed = this.$.resolveOutput("missed");
        }
        /**
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterJoinUpdateFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * simple.
     * @param master  the master data
     * @param model  target data model
     * @return the created operator object
     * @see MasterJoinUpdateFlow#simple(Ex2, Ex1)
     */
    @OperatorInfo(kind = MasterJoinUpdate.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, 
                position = 0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@
                OperatorInfo.Output(name = "updated", type = Ex1.class),@OperatorInfo.Output(name = "missed", type = Ex1
                .class)}, parameter = {}) public MasterJoinUpdateFlowFactory.Simple simple(@KeyInfo(group = {@KeyInfo.
                Group(expression = "string")}, order = {}) Source<Ex2> master,@KeyInfo(group = {@KeyInfo.Group(
                expression = "string")}, order = {}) Source<Ex1> model) {
        return new MasterJoinUpdateFlowFactory.Simple(master, model);
    }
    /**
     * parameterized.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         * each data model object which is successfully updated
         */
        public final Source<Ex1> updated;
        /**
         * each data model object which does not have corresponding master data
         */
        public final Source<Ex1> missed;
        WithParameter(Source<Ex2> master, Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterJoinUpdate.class);
            builder0.declare(MasterJoinUpdateFlow.class, MasterJoinUpdateFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex2.class);
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("updated", model);
            builder0.addOutput("missed", model);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.updated = this.$.resolveOutput("updated");
            this.missed = this.$.resolveOutput("missed");
        }
        /**
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterJoinUpdateFlowFactory.WithParameter as(String newName0) {
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
     * @see MasterJoinUpdateFlow#withParameter(Ex2, Ex1, int)
     */
    @OperatorInfo(kind = MasterJoinUpdate.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, 
                position = 0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@
                OperatorInfo.Output(name = "updated", type = Ex1.class),@OperatorInfo.Output(name = "missed", type = Ex1
                .class)}, parameter = {@OperatorInfo.Parameter(name = "parameter", type = int.class, position = 2)}) 
            public MasterJoinUpdateFlowFactory.WithParameter withParameter(@KeyInfo(group = {@KeyInfo.Group(expression = 
                "string")}, order = {}) Source<Ex2> master,@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, 
            order = {}) Source<Ex1> model, int parameter) {
        return new MasterJoinUpdateFlowFactory.WithParameter(master, model, parameter);
    }
    /**
     * w/ selector.
     */
    public static final class Selection implements Operator {
        private final FlowElementResolver $;
        /**
         * each data model object which is successfully updated
         */
        public final Source<Ex1> updated;
        /**
         * each data model object which does not have corresponding master data
         */
        public final Source<Ex1> missed;
        Selection(Source<Ex2> master, Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(MasterJoinUpdate.class);
            builder1.declare(MasterJoinUpdateFlow.class, MasterJoinUpdateFlowImpl.class, "selection");
            builder1.declareParameter(Ex2.class);
            builder1.declareParameter(Ex1.class);
            builder1.addInput("master", master, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addOutput("updated", model);
            builder1.addOutput("missed", model);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            builder1.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex1.class})));
            this.$ = builder1.toResolver();
            this.$.resolveInput("master", master);
            this.$.resolveInput("model", model);
            this.updated = this.$.resolveOutput("updated");
            this.missed = this.$.resolveOutput("missed");
        }
        /**
         * Configures the name of this operator.
         * @param newName1 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterJoinUpdateFlowFactory.Selection as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * w/ selector.
     * @param master  the master data
     * @param model  target data model
     * @return the created operator object
     * @see MasterJoinUpdateFlow#selection(Ex2, Ex1)
     */
    @OperatorInfo(kind = MasterJoinUpdate.class, input = {@OperatorInfo.Input(name = "master", type = Ex2.class, 
                position = 0),@OperatorInfo.Input(name = "model", type = Ex1.class, position = 1)}, output = {@
                OperatorInfo.Output(name = "updated", type = Ex1.class),@OperatorInfo.Output(name = "missed", type = Ex1
                .class)}, parameter = {}) public MasterJoinUpdateFlowFactory.Selection selection(@KeyInfo(group = {@
                KeyInfo.Group(expression = "string")}, order = {}) Source<Ex2> master,@KeyInfo(group = {@KeyInfo.Group(
                expression = "string")}, order = {}) Source<Ex1> model) {
        return new MasterJoinUpdateFlowFactory.Selection(master, model);
    }
}