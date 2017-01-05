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
import com.asakusafw.vocabulary.flow.graph.Connectivity;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Logging;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>LoggingFlow</code>.
 * @see LoggingFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(LoggingFlow.class) public class LoggingFlowFactory {
    /**
     * simple. 
             * <p>Note that, each output port of this operator will automatically connect to "stop" operator if not connected to any other ports.</p>
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         * input data
         */
        public final Source<Ex1> out;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Logging.class);
            builder.declare(LoggingFlow.class, LoggingFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", model);
            builder.addOutput("out", model);
            builder.addAttribute(ObservationCount.AT_LEAST_ONCE);
            builder.addAttribute(Connectivity.OPTIONAL);
            builder.addAttribute(Logging.Level.INFO);
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
        public LoggingFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * simple. 
             * <p>Note that, each output port of this operator will automatically connect to "stop" operator if not connected to any other ports.</p>
     * @param model  target data model
     * @return the created operator object
     * @see LoggingFlow#simple(Ex1)
     */
    @OperatorInfo(kind = Logging.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, parameter = {}) public LoggingFlowFactory.
            Simple simple(Source<Ex1> model) {
        return new LoggingFlowFactory.Simple(model);
    }
    /**
     * parameterized. 
             * <p>Note that, each output port of this operator will automatically connect to "stop" operator if not connected to any other ports.</p>
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         * input data
         */
        public final Source<Ex1> out;
        WithParameter(Source<Ex1> model, String parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Logging.class);
            builder0.declare(LoggingFlow.class, LoggingFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(String.class);
            builder0.addInput("model", model);
            builder0.addOutput("out", model);
            builder0.addParameter("parameter", String.class, parameter);
            builder0.addAttribute(ObservationCount.AT_LEAST_ONCE);
            builder0.addAttribute(Connectivity.OPTIONAL);
            builder0.addAttribute(Logging.Level.INFO);
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
        public LoggingFlowFactory.WithParameter as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * parameterized. 
             * <p>Note that, each output port of this operator will automatically connect to "stop" operator if not connected to any other ports.</p>
     * @param model  target data model
     * @param parameter  additional parameter
     * @return the created operator object
     * @see LoggingFlow#withParameter(Ex1, String)
     */
    @OperatorInfo(kind = Logging.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, parameter = {@OperatorInfo.Parameter(name = 
                "parameter", type = String.class, position = 1)}) public LoggingFlowFactory.WithParameter withParameter(
            Source<Ex1> model, String parameter) {
        return new LoggingFlowFactory.WithParameter(model, parameter);
    }
}