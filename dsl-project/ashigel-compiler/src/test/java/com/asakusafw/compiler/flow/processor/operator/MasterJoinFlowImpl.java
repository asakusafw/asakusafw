package com.asakusafw.compiler.flow.processor.operator;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.compiler.flow.testing.model.ExJoined2;
import com.asakusafw.runtime.core.Result;
import javax.annotation.Generated;
/**
 * An operator implementation class for{@link MasterJoinFlow}.
 */
@Generated("OperatorImplementationClassGenerator:0.1.0") public class MasterJoinFlowImpl extends MasterJoinFlow {
    /**
     * Creates a new instance.
     */
    public MasterJoinFlowImpl() {
        return;
    }
    @Override public ExJoined join(Ex1 ex1, Ex2 ex2) {
        throw new UnsupportedOperationException("master join operator does not have method body");
    }
    @Override public ExJoined2 renameKey(Ex1 ex1, Ex2 ex2) {
        throw new UnsupportedOperationException("master join operator does not have method body");
    }
    @Override public ExJoined selection(Ex1 ex1, Ex2 ex2) {
        throw new UnsupportedOperationException("master join operator does not have method body");
    }
    @Override public void split(ExJoined joined, Result<Ex1> ex1, Result<Ex2> ex2) {
        throw new UnsupportedOperationException("split operator does not have method body");
    }
}