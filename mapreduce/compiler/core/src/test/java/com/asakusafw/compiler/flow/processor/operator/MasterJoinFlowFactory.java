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
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.compiler.flow.testing.model.ExJoined2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.KeyInfo;
import com.asakusafw.vocabulary.operator.MasterJoin;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;
/**
 * An operator factory class about <code>MasterJoinFlow</code>.
 * @see MasterJoinFlow
 */
@Generated("OperatorFactoryClassGenerator:0.1.0")@OperatorFactory(MasterJoinFlow.class) public class 
        MasterJoinFlowFactory {
    /**
     * join.
     */
    public static final class Join implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
         */
        public final Source<ExJoined> joined;
        /**
         * each data model object which does not have corresponding master data
         */
        public final Source<Ex2> missed;
        Join(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(MasterJoin.class);
            builder.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "join");
            builder.declareParameter(Ex1.class);
            builder.declareParameter(Ex2.class);
            builder.addInput("ex1", ex1, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addInput("ex2", ex2, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder.addOutput("joined", ExJoined.class);
            builder.addOutput("missed", ex2);
            builder.addAttribute(FlowBoundary.SHUFFLE);
            builder.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder.toResolver();
            this.$.resolveInput("ex1", ex1);
            this.$.resolveInput("ex2", ex2);
            this.joined = this.$.resolveOutput("joined");
            this.missed = this.$.resolveOutput("missed");
        }
        /**
         * Configures the name of this operator.
         * @param newName the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterJoinFlowFactory.Join as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * join.
     * @param ex1  master
     * @param ex2  transaction
     * @return the created operator object
     * @see MasterJoinFlow#join(Ex1, Ex2)
     */
    @OperatorInfo(kind = MasterJoin.class, input = {@OperatorInfo.Input(name = "ex1", type = Ex1.class, position = 0),@
                OperatorInfo.Input(name = "ex2", type = Ex2.class, position = 1)}, output = {@OperatorInfo.Output(name = 
                "joined", type = ExJoined.class),@OperatorInfo.Output(name = "missed", type = Ex2.class)}, parameter = {
            }) public MasterJoinFlowFactory.Join join(@KeyInfo(group = {@KeyInfo.Group(expression = "value")}, order = {
            }) Source<Ex1> ex1,@KeyInfo(group = {@KeyInfo.Group(expression = "value")}, order = {}) Source<Ex2> ex2) {
        return new MasterJoinFlowFactory.Join(ex1, ex2);
    }
    /**
     * join w/ renaming join key.
     */
    public static final class RenameKey implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
         */
        public final Source<ExJoined2> joined;
        /**
         * each data model object which does not have corresponding master data
         */
        public final Source<Ex2> missed;
        RenameKey(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterJoin.class);
            builder0.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "renameKey");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(Ex2.class);
            builder0.addInput("ex1", ex1, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addInput("ex2", ex2, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder0.addOutput("joined", ExJoined2.class);
            builder0.addOutput("missed", ex2);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            builder0.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder0.toResolver();
            this.$.resolveInput("ex1", ex1);
            this.$.resolveInput("ex2", ex2);
            this.joined = this.$.resolveOutput("joined");
            this.missed = this.$.resolveOutput("missed");
        }
        /**
         * Configures the name of this operator.
         * @param newName0 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterJoinFlowFactory.RenameKey as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * join w/ renaming join key.
     * @param ex1  master
     * @param ex2  transaction
     * @return the created operator object
     * @see MasterJoinFlow#renameKey(Ex1, Ex2)
     */
    @OperatorInfo(kind = MasterJoin.class, input = {@OperatorInfo.Input(name = "ex1", type = Ex1.class, position = 0),@
                OperatorInfo.Input(name = "ex2", type = Ex2.class, position = 1)}, output = {@OperatorInfo.Output(name = 
                "joined", type = ExJoined2.class),@OperatorInfo.Output(name = "missed", type = Ex2.class)}, parameter = 
            {}) public MasterJoinFlowFactory.RenameKey renameKey(@KeyInfo(group = {@KeyInfo.Group(expression = "value")}
            , order = {}) Source<Ex1> ex1,@KeyInfo(group = {@KeyInfo.Group(expression = "value")}, order = {}) Source<
            Ex2> ex2) {
        return new MasterJoinFlowFactory.RenameKey(ex1, ex2);
    }
    /**
     * w/ selector.
     */
    public static final class Selection implements Operator {
        private final FlowElementResolver $;
        /**
         *  result
         */
        public final Source<ExJoined> joined;
        /**
         * each data model object which does not have corresponding master data
         */
        public final Source<Ex2> missed;
        Selection(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(MasterJoin.class);
            builder1.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "selection");
            builder1.declareParameter(Ex1.class);
            builder1.declareParameter(Ex2.class);
            builder1.addInput("ex1", ex1, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addInput("ex2", ex2, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new 
                    ShuffleKey.Order[]{})));
            builder1.addOutput("joined", ExJoined.class);
            builder1.addOutput("missed", ex2);
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            builder1.addAttribute(ObservationCount.DONT_CARE);
            builder1.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex2.class})));
            this.$ = builder1.toResolver();
            this.$.resolveInput("ex1", ex1);
            this.$.resolveInput("ex2", ex2);
            this.joined = this.$.resolveOutput("joined");
            this.missed = this.$.resolveOutput("missed");
        }
        /**
         * Configures the name of this operator.
         * @param newName1 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterJoinFlowFactory.Selection as(String newName1) {
            this.$.setName(newName1);
            return this;
        }
    }
    /**
     * w/ selector.
     * @param ex1  master
     * @param ex2  transaction
     * @return the created operator object
     * @see MasterJoinFlow#selection(Ex1, Ex2)
     */
    @OperatorInfo(kind = MasterJoin.class, input = {@OperatorInfo.Input(name = "ex1", type = Ex1.class, position = 0),@
                OperatorInfo.Input(name = "ex2", type = Ex2.class, position = 1)}, output = {@OperatorInfo.Output(name = 
                "joined", type = ExJoined.class),@OperatorInfo.Output(name = "missed", type = Ex2.class)}, parameter = {
            }) public MasterJoinFlowFactory.Selection selection(@KeyInfo(group = {@KeyInfo.Group(expression = "value")}, 
            order = {}) Source<Ex1> ex1,@KeyInfo(group = {@KeyInfo.Group(expression = "value")}, order = {}) Source<Ex2> 
            ex2) {
        return new MasterJoinFlowFactory.Selection(ex1, ex2);
    }
    /**
     * split.
     */
    public static final class Split implements Operator {
        private final FlowElementResolver $;
        /**
         *  master
         */
        public final Source<Ex1> ex1;
        /**
         *  transaction
         */
        public final Source<Ex2> ex2;
        Split(Source<ExJoined> joined) {
            OperatorDescription.Builder builder2 = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.
                    Split.class);
            builder2.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "split");
            builder2.declareParameter(ExJoined.class);
            builder2.declareParameter(Result.class);
            builder2.declareParameter(Result.class);
            builder2.addInput("joined", joined);
            builder2.addOutput("ex1", Ex1.class);
            builder2.addOutput("ex2", Ex2.class);
            builder2.addAttribute(ObservationCount.DONT_CARE);
            this.$ = builder2.toResolver();
            this.$.resolveInput("joined", joined);
            this.ex1 = this.$.resolveOutput("ex1");
            this.ex2 = this.$.resolveOutput("ex2");
        }
        /**
         * Configures the name of this operator.
         * @param newName2 the new operator name
         * @return this operator object
         * @throws IllegalArgumentException if the parameter is <code>null</code>
         */
        public MasterJoinFlowFactory.Split as(String newName2) {
            this.$.setName(newName2);
            return this;
        }
    }
    /**
     * split.
     * @param joined  joined data
     * @return the created operator object
     * @see MasterJoinFlow#split(ExJoined, Result, Result)
     */
    @OperatorInfo(kind = com.asakusafw.vocabulary.operator.Split.class, input = {@OperatorInfo.Input(name = "joined", 
                type = ExJoined.class, position = 0)}, output = {@OperatorInfo.Output(name = "ex1", type = Ex1.class),@
                OperatorInfo.Output(name = "ex2", type = Ex2.class)}, parameter = {}) public MasterJoinFlowFactory.Split 
            split(Source<ExJoined> joined) {
        return new MasterJoinFlowFactory.Split(joined);
    }
}