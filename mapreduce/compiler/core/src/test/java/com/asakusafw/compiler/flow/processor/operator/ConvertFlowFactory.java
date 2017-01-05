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
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Convert;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>ConvertFlow</code>.
 * @see ConvertFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(ConvertFlow.class) public class ConvertFlowFactory {
    /**
     * simple.
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         * input data
         */
        public final Source<Ex1> original;
        /**
         *  result
         */
        public final Source<Ex2> out;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Convert.class);
            builder.declare(ConvertFlow.class, ConvertFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", model);
            builder.addOutput("original", model);
            builder.addOutput("out", Ex2.class);
            builder.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder.toResolver();
            this.$.resolveInput("model", model);
            this.original = this.$.resolveOutput("original");
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ConvertFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * simple.
     * @param model  target data model
     * @return the created operator object
     * @see ConvertFlow#simple(Ex1)
     */
    @OperatorInfo(kind = Convert.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "original", type = Ex1.class),@OperatorInfo.Output(name = "out", type 
                = Ex2.class)}, parameter = {}) public ConvertFlowFactory.Simple simple(Source<Ex1> model) {
        return new ConvertFlowFactory.Simple(model);
    }
    /**
     * parameterized.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         * input data
         */
        public final Source<Ex1> original;
        /**
         *  result
         */
        public final Source<Ex2> out;
        WithParameter(Source<Ex1> model, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Convert.class);
            builder0.declare(ConvertFlow.class, ConvertFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("model", model);
            builder0.addOutput("original", model);
            builder0.addOutput("out", Ex2.class);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("model", model);
            this.original = this.$.resolveOutput("original");
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public ConvertFlowFactory.WithParameter as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * parameterized.
     * @param model  target data model
     * @param parameter  additional parameter
     * @return the created operator object
     * @see ConvertFlow#withParameter(Ex1, int)
     */
    @OperatorInfo(kind = Convert.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "original", type = Ex1.class),@OperatorInfo.Output(name = "out", type 
                = Ex2.class)}, parameter = {@OperatorInfo.Parameter(name = "parameter", type = int.class, position = 1)}
            ) public ConvertFlowFactory.WithParameter withParameter(Source<Ex1> model, int parameter) {
        return new ConvertFlowFactory.WithParameter(model, parameter);
    }
}