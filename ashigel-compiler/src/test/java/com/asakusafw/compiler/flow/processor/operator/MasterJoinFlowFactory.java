/**
 * Copyright 2011 Asakusa Framework Team.
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
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.MasterJoin;

/**
 * {@link MasterJoinFlow}に関する演算子ファクトリークラス。
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class MasterJoinFlowFactory {
    /**
     * 分解する。
     */
    public static final class Split implements Operator {
        /**
         *  マスタ
         */
        public final Source<Ex1> ex1;
        /**
         *  トラン
         */
        public final Source<Ex2> ex2;
        Split(Source<ExJoined> joined) {
            OperatorDescription.Builder builder = new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.Split.class);
            builder.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "split");
            builder.declareParameter(ExJoined.class);
            builder.declareParameter(Result.class);
            builder.declareParameter(Result.class);
            builder.addInput("joined", ExJoined.class);
            builder.addOutput("ex1", Ex1.class);
            builder.addOutput("ex2", Ex2.class);
            FlowElementResolver resolver = builder.toResolver();
            resolver.resolveInput("joined", joined);
            this.ex1 = resolver.resolveOutput("ex1");
            this.ex2 = resolver.resolveOutput("ex2");
        }
    }
    /**
     * 分解する。
     * @param joined 結合結果
     * @return 生成した演算子オブジェクト
     */
    public MasterJoinFlowFactory.Split split(Source<ExJoined> joined) {
        return new MasterJoinFlowFactory.Split(joined);
    }
    /**
     * 結合する。
     */
    public static final class Join implements Operator {
        /**
         *  結合結果
         */
        public final Source<ExJoined> joined;
        /**
         * 結合に失敗したデータ
         */
        public final Source<Ex2> missed;
        Join(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterJoin.class);
            builder0.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "join");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(Ex2.class);
            builder0.addInput("ex1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder0.addInput("ex2", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder0.addOutput("joined", ExJoined.class);
            builder0.addOutput("missed", Ex2.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver0 = builder0.toResolver();
            resolver0.resolveInput("ex1", ex1);
            resolver0.resolveInput("ex2", ex2);
            this.joined = resolver0.resolveOutput("joined");
            this.missed = resolver0.resolveOutput("missed");
        }
    }
    /**
     * 結合する。
     * @param ex1 マスタ
     * @param ex2 トラン
     * @return 生成した演算子オブジェクト
     */
    public MasterJoinFlowFactory.Join join(Source<Ex1> ex1, Source<Ex2> ex2) {
        return new MasterJoinFlowFactory.Join(ex1, ex2);
    }
    /**
     * 結合する。
     */
    public static final class RenameKey implements Operator {
        /**
         *  結合結果
         */
        public final Source<ExJoined2> joined;
        /**
         * 結合に失敗したデータ
         */
        public final Source<Ex2> missed;
        RenameKey(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder0 = new OperatorDescription.Builder(MasterJoin.class);
            builder0.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "renameKey");
            builder0.declareParameter(Ex1.class);
            builder0.declareParameter(Ex2.class);
            builder0.addInput("ex1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder0.addInput("ex2", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder0.addOutput("joined", ExJoined2.class);
            builder0.addOutput("missed", Ex2.class);
            builder0.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver0 = builder0.toResolver();
            resolver0.resolveInput("ex1", ex1);
            resolver0.resolveInput("ex2", ex2);
            this.joined = resolver0.resolveOutput("joined");
            this.missed = resolver0.resolveOutput("missed");
        }
    }
    /**
     * キー名を変更して結合する。
     * @param ex1 マスタ
     * @param ex2 トラン
     * @return 生成した演算子オブジェクト
     */
    public MasterJoinFlowFactory.RenameKey renameKey(Source<Ex1> ex1, Source<Ex2> ex2) {
        return new MasterJoinFlowFactory.RenameKey(ex1, ex2);
    }
    /**
     * セレクタつき。
     */
    public static final class Selection implements Operator {
        /**
         *  結合結果
         */
        public final Source<ExJoined> joined;
        /**
         * 結合に失敗したデータ
         */
        public final Source<Ex2> missed;
        Selection(Source<Ex1> ex1, Source<Ex2> ex2) {
            OperatorDescription.Builder builder1 = new OperatorDescription.Builder(MasterJoin.class);
            builder1.declare(MasterJoinFlow.class, MasterJoinFlowImpl.class, "selection");
            builder1.declareParameter(Ex1.class);
            builder1.declareParameter(Ex2.class);
            builder1.addInput("ex1", Ex1.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder1.addInput("ex2", Ex2.class, new ShuffleKey(Arrays.asList(new String[]{"value"}), Arrays.asList(new
                    ShuffleKey.Order[]{})));
            builder1.addOutput("joined", ExJoined.class);
            builder1.addOutput("missed", Ex2.class);
            builder1.addAttribute(new OperatorHelper("selector", Arrays.asList(new Class<?>[]{List.class, Ex2.class})));
            builder1.addAttribute(FlowBoundary.SHUFFLE);
            FlowElementResolver resolver1 = builder1.toResolver();
            resolver1.resolveInput("ex1", ex1);
            resolver1.resolveInput("ex2", ex2);
            this.joined = resolver1.resolveOutput("joined");
            this.missed = resolver1.resolveOutput("missed");
        }
    }
    /**
     * セレクタつき。
     * @param ex1 マスタ
     * @param ex2 トラン
     * @return 生成した演算子オブジェクト
     */
    public MasterJoinFlowFactory.Selection selection(Source<Ex1> ex1, Source<Ex2> ex2) {
        return new MasterJoinFlowFactory.Selection(ex1, ex2);
    }
}