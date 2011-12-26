package com.asakusafw.compiler.flow.testing.operator;
import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
/**
 * {@link ExOperator}に関する演算子実装クラス。
 */
@Generated("OperatorImplementationClassGenerator:0.0.1") public class ExOperatorImpl extends ExOperator {
    /**
     * インスタンスを生成する。
     */
    public ExOperatorImpl() {
        return;
    }
    @Override public ExSummarized summarize(Ex1 model) {
        throw new UnsupportedOperationException("単純集計演算子は組み込みの方法で処理されます");
    }
}