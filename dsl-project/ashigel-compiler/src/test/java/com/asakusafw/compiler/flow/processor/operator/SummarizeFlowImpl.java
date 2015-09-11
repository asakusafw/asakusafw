package com.asakusafw.compiler.flow.processor.operator;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.compiler.flow.testing.model.KeyConflict;
import javax.annotation.Generated;
/**
 * An operator implementation class for{@link SummarizeFlow}.
 */
@Generated("OperatorImplementationClassGenerator:0.1.0") public class SummarizeFlowImpl extends SummarizeFlow {
    /**
     * Creates a new instance.
     */
    public SummarizeFlowImpl() {
        return;
    }
    @Override public ExSummarized simple(Ex1 model) {
        throw new UnsupportedOperationException("summarize operator does not have method body");
    }
    @Override public ExSummarized2 renameKey(Ex1 model) {
        throw new UnsupportedOperationException("summarize operator does not have method body");
    }
    @Override public KeyConflict keyConflict(Ex1 model) {
        throw new UnsupportedOperationException("summarize operator does not have method body");
    }
}