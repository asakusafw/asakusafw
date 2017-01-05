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
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.operator.KeyInfo;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
import com.asakusafw.vocabulary.operator.Summarize;
/**
 * An operator factory class about <code>SummarizeFlow</code>.
 * @see SummarizeFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(SummarizeFlow.class) public class SummarizeFlowFactory 
        {
    /**
     * simple.
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         *  result model
         */
        public final Source<ExSummarized> out;
        Simple(Source<Ex1> model) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(Summarize.class);
            builder.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "simple");
            builder.declareParameter(Ex1.class);
            builder.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("out", ExSummarized.class);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            builder.addAttribute(PartialAggregation.PARTIAL);
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
        public SummarizeFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * simple.
     * @param model  target model
     * @return the created operator object
     * @see SummarizeFlow#simple(Ex1)
     */
    @OperatorInfo(kind = Summarize.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "out", type = ExSummarized.class)}, parameter = {}) public 
            SummarizeFlowFactory.Simple simple(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {}) 
            Source<Ex1> model) {
        return new SummarizeFlowFactory.Simple(model);
    }
    /**
     * summarize w/ renaming its key.
     */
    public static final class RenameKey implements Operator {
        private final FlowElementResolver $;
        /**
         *  result model
         */
        public final Source<ExSummarized2> out;
        RenameKey(Source<Ex1> model) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(Summarize.class);
            builder0.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "renameKey");
            builder0.declareParameter(Ex1.class);
            builder0.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("out", ExSummarized2.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            builder0.addAttribute(PartialAggregation.PARTIAL);
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
        public SummarizeFlowFactory.RenameKey as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * summarize w/ renaming its key.
     * @param model  target model
     * @return the created operator object
     * @see SummarizeFlow#renameKey(Ex1)
     */
    @OperatorInfo(kind = Summarize.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "out", type = ExSummarized2.class)}, parameter = {}) public 
            SummarizeFlowFactory.RenameKey renameKey(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {
            }) Source<Ex1> model) {
        return new SummarizeFlowFactory.RenameKey(model);
    }
    /**
     * Grouping key is also used for other aggregation operations.
     */
    public static final class KeyConflict implements Operator {
        private final FlowElementResolver $;
        /**
         *  result model
         */
        public final Source<com.asakusafw.compiler.flow.testing.model.KeyConflict> out;
        KeyConflict(Source<Ex1> model) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(Summarize.class);
            builder1.declare(SummarizeFlow.class, SummarizeFlowImpl.class, "keyConflict");
            builder1.declareParameter(Ex1.class);
            builder1.addInput("model", model, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addOutput("out", com.asakusafw.compiler.flow.testing.model.KeyConflict.class);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            builder1.addAttribute(PartialAggregation.PARTIAL);
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
        public SummarizeFlowFactory.KeyConflict as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * Grouping key is also used for other aggregation operations.
     * @param model  target model
     * @return the created operator object
     * @see SummarizeFlow#keyConflict(Ex1)
     */
    @OperatorInfo(kind = Summarize.class, input = {@OperatorInfo.Input(name = "model", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "out", type = com.asakusafw.compiler.flow.testing.model.KeyConflict.
                class)}, parameter = {}) public SummarizeFlowFactory.KeyConflict keyConflict(@KeyInfo(group = {@KeyInfo.
                Group(expression = "string")}, order = {}) Source<Ex1> model) {
        return new SummarizeFlowFactory.KeyConflict(model);
    }
}