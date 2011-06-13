package com.asakusafw.compiler.flow.processor.operator;
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
import java.util.Arrays;
import javax.annotation.Generated;
/**
 * {@link FoldFlow}に関する演算子ファクトリークラス。
 * @see FoldFlow
 */
@Generated("OperatorFactoryClassGenerator:0.0.1") public class FoldFlowFactory {
    /**
     * 通常の演算子。
     */
    public static final class Simple implements Operator {
        private final FlowElementResolver $;
        /**
         * 畳み込みの結果
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
         * この演算子の名前を設定する。
         * @param newName 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public FoldFlowFactory.Simple as(String newName) {
            this.$.setName(newName);
            return this;
        }
    }
    /**
     * 通常の演算子。
     * @param in 畳み込む値
     * @return 生成した演算子オブジェクト
     * @see FoldFlow#simple(Ex1, Ex1)
     */
    public FoldFlowFactory.Simple simple(Source<Ex1> in) {
        return new FoldFlowFactory.Simple(in);
    }
    /**
     * 引数つきの演算子。
     */
    public static final class WithParameter implements Operator {
        private final FlowElementResolver $;
        /**
         * 畳み込みの結果
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
         * この演算子の名前を設定する。
         * @param newName0 設定する名前
         * @return この演算子オブジェクト (this)
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public FoldFlowFactory.WithParameter as(String newName0) {
            this.$.setName(newName0);
            return this;
        }
    }
    /**
     * 引数つきの演算子。
     * @param in 畳み込む値
     * @param parameter 追加パラメータ
     * @return 生成した演算子オブジェクト
     * @see FoldFlow#withParameter(Ex1, Ex1, int)
     */
    public FoldFlowFactory.WithParameter withParameter(Source<Ex1> in, int parameter) {
        return new FoldFlowFactory.WithParameter(in, parameter);
    }
}