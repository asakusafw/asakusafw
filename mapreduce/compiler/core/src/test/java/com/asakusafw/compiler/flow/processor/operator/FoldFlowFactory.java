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

import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.operator.Fold;
import com.asakusafw.vocabulary.operator.KeyInfo;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>FoldFlow</code>.
 * @see FoldFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(FoldFlow.class) public class FoldFlowFactory {
    /**
     * simple.
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         * the folding result
         */
        public final Source<Ex1> out;
        Simple(Source<Ex1> in) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Fold.class);
            builder.declare(FoldFlow.class, FoldFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(Ex1.class);
            builder.addInput("in", in, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("out", in);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            builder.addAttribute(PartialAggregation.DEFAULT);
            this.$ = builder.toResolver();
            this.$.resolveInput("in", in);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public FoldFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * simple.
     * @param in  fold source
     * @return the created operator object
     * @see FoldFlow#simple(Ex1, Ex1)
     */
    @OperatorInfo(kind = Fold.class, input = {@OperatorInfo.Input(name = "in", type = Ex1.class, position = 0)}, output 
            = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, parameter = {}) public FoldFlowFactory.Simple 
            simple(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> in) {
        return new FoldFlowFactory.Simple(in);
    }
    /**
     * parameterized.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         * the folding result
         */
        public final Source<Ex1> out;
        WithParameter(Source<Ex1> in, int parameter) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Fold.class);
            builder0.declare(FoldFlow.class, FoldFlowImpl.class, "withParameter");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(int.class);
            builder0.addInput("in", in, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("out", in);
            builder0.addParameter("parameter", int.class, parameter);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            builder0.addAttribute(PartialAggregation.DEFAULT);
            this.$ = builder0.toResolver();
            this.$.resolveInput("in", in);
            this.out = this.$.resolveOutput("out");
        }
        /**
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public FoldFlowFactory.WithParameter as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * parameterized.
     * @param in  fold source
     * @param parameter  additional parameter
     * @return the created operator object
     * @see FoldFlow#withParameter(Ex1, Ex1, int)
     */
    @OperatorInfo(kind = Fold.class, input = {@OperatorInfo.Input(name = "in", type = Ex1.class, position = 0)}, output 
            = {@OperatorInfo.Output(name = "out", type = Ex1.class)}, parameter = {@OperatorInfo.Parameter(name = 
                "parameter", type = int.class, position = 1)}) public FoldFlowFactory.WithParameter withParameter(@
            KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) Source<Ex1> in, int parameter) {
        return new FoldFlowFactory.WithParameter(in, parameter);
    }
}