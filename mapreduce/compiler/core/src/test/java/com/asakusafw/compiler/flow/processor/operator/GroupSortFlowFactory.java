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
 * An operator factory class about <code>GroupSortFlow</code>.
 * @see GroupSortFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(GroupSortFlow.class) public class GroupSortFlowFactory 
        {
    /**
     * Switches output target whether if the value is less than or equal to the parameter.
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         *  a1 where value of input is less than or equal to the parameter
         */
        public final Source<Ex1> r1;
        /**
         *  a1 where value of input is greater than the parameter
         */
        public final Source<Ex1> r2;
        WithParameter(Source<Ex1> a1, int parameter) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(CoGroup.class);
            builder.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "withParameter");
            builder.declareParameter(List.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(int.class);
            builder.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.ASC)})));
            builder.addOutput("r1", Ex1.class);
            builder.addOutput("r2", Ex1.class);
            builder.addParameter("parameter", int.class, parameter);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            builder.addAttribute(InputBuffer.EXPAND);
            this.$ = builder.toResolver();
            this.$.resolveInput("a1", a1);
            this.r1 = this.$.resolveOutput("r1");
            this.r2 = this.$.resolveOutput("r2");
        }
        /**
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public GroupSortFlowFactory.WithParameter as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * Switches output target whether if the value is less than or equal to the parameter.
     * @param a1  group
     * @param parameter  the parameter for switching output target
     * @return the created operator object
     * @see GroupSortFlow#withParameter(List, Result, Result, int)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class),@OperatorInfo.Output(name = "r2", type = Ex1.
                class)}, parameter = {@OperatorInfo.Parameter(name = "parameter", type = int.class, position = 1)}) 
            public GroupSortFlowFactory.WithParameter withParameter(@KeyInfo(group = {@KeyInfo.Group(expression = 
                "string")}, order = {@KeyInfo.Order(direction = KeyInfo.Direction.ASC, expression = "value")}) Source<
            Ex1> a1, int parameter) {
        return new GroupSortFlowFactory.WithParameter(a1, parameter);
    }
    /**
     * return min.
     */
    public static final class Min implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
         */
        public final Source<Ex1> r1;
        Min(Source<Ex1> a1) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(CoGroup.class);
            builder0.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "min");
            builder0.declareParameter(List.class);
            builder0.declareParameter(Result.class);
            builder0.addInput("a1", a1, new ShuffleKey(Arrays.asList(new String[]{"string"}), Arrays.asList(new 
                    ShuffleKey.Order[]{new ShuffleKey.Order("value", ShuffleKey.Direction.ASC)})));
            builder0.addOutput("r1", Ex1.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            builder0.addAttribute(InputBuffer.EXPAND);
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
        public GroupSortFlowFactory.Min as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * return min.
     * @param a1  group
     * @return the created operator object
     * @see GroupSortFlow#min(List, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class)}, parameter = {}) public GroupSortFlowFactory.
            Min min(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {@KeyInfo.Order(direction = 
                KeyInfo.Direction.ASC, expression = "value")}) Source<Ex1> a1) {
        return new GroupSortFlowFactory.Min(a1);
    }
    /**
     * return max.
     */
    public static final class Max implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
         */
        public final Source<Ex1> r1;
        Max(Source<Ex1> a1) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(CoGroup.class);
            builder1.declare(GroupSortFlow.class, GroupSortFlowImpl.class, "max");
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
        public GroupSortFlowFactory.Max as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * return max.
     * @param a1  group
     * @return the created operator object
     * @see GroupSortFlow#max(List, Result)
     */
    @OperatorInfo(kind = CoGroup.class, input = {@OperatorInfo.Input(name = "a1", type = Ex1.class, position = 0)}, 
            output = {@OperatorInfo.Output(name = "r1", type = Ex1.class)}, parameter = {}) public GroupSortFlowFactory.
            Max max(@KeyInfo(group = {@KeyInfo.Group(expression = "string")}, order = {@KeyInfo.Order(direction = 
                KeyInfo.Direction.DESC, expression = "value")}) Source<Ex1> a1) {
        return new GroupSortFlowFactory.Max(a1);
    }
}