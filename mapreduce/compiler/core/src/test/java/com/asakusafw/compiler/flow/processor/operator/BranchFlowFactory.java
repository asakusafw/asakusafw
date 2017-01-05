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
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Branch;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>BranchFlow</code>.
 * @see BranchFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(BranchFlow.class) public class BranchFlowFactory {
    /**
     * simple.
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         * High speed.
         */
        public final Source<Ex1> high;
        /**
         * Low speed.
         */
        public final Source<Ex1> low;
        /**
         * Stopped.
         */
        public final Source<Ex1> stop;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Branch.class);
            builder.declare(BranchFlow.class, BranchFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", model);
            builder.addOutput("high", model);
            builder.addOutput("low", model);
            builder.addOutput("stop", model);
            builder.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder.toResolver();
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
        public BranchFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * simple.
     * @param model  target data model
     * @return the created operator object
     * @see BranchFlow#simple(Ex1)
     */
    @OperatorInfo(kind = Branch.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "high", type = Ex1.class),@OperatorInfo.Output(name = "low", type = 
                Ex1.class),@OperatorInfo.Output(name = "stop", type = Ex1.class)}, parameter = {}) public 
            BranchFlowFactory.Simple simple(Source<Ex1> model) {
        return new BranchFlowFactory.Simple(model);
    }
    /**
     * parameterized.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         * High speed.
         */
        public final Source<Ex1> high;
        /**
         * Low speed.
         */
        public final Source<Ex1> low;
        /**
         * Stopped.
         */
        public final Source<Ex1> stop;
        WithParameter(Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Branch.class);
            builder0.declare(BranchFlow.class, BranchFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("model", model);
            builder0.addOutput("high", model);
            builder0.addOutput("low", model);
            builder0.addOutput("stop", model);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder0.toResolver();
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
        public BranchFlowFactory.WithParameter as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * parameterized.
     * @param model  target data model
     * @param parameter  additional parameter
     * @return the created operator object
     * @see BranchFlow#withParameter(Ex1, int)
     */
    @OperatorInfo(kind = Branch.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "high", type = Ex1.class),@OperatorInfo.Output(name = "low", type = 
                Ex1.class),@OperatorInfo.Output(name = "stop", type = Ex1.class)}, parameter = {@OperatorInfo.Parameter(
                name = "parameter", type = int.class, position = 1)}) public BranchFlowFactory.WithParameter 
            withParameter(Source<Ex1> model, int parameter) {
        return new BranchFlowFactory.WithParameter(model, parameter);
    }
}