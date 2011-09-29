package com.asakusafw.compiler.flow.processor.operator;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.compiler.flow.testing.model.KeyConflict;
import javax.annotation.Generated;
/**
 * {@link SummarizeFlow}に関する演算子実装クラス。
 */
@Generated("OperatorImplementationClassGenerator:0.0.1") public class SummarizeFlowImpl extends SummarizeFlow {
    /**
     * インスタンスを生成する。
     */
    public SummarizeFlowImpl() {
        return;
    }
    @Override public ExSummarized simple(Ex1 model) {
        throw new UnsupportedOperationException("単純集計演算子は組み込みの方法で処理されます");
    }
    @Override public ExSummarized2 renameKey(Ex1 model) {
        throw new UnsupportedOperationException("単純集計演算子は組み込みの方法で処理されます");
    }
    @Override public KeyConflict keyConflict(Ex1 model) {
        throw new UnsupportedOperationException("単純集計演算子は組み込みの方法で処理されます");
    }
}