package com.asakusafw.compiler.flow.processor.operator;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import javax.annotation.Generated;
/**
 * An operator implementation class for{@link MasterCheckFlow}.
 */
@Generated("OperatorImplementationClassGenerator:0.1.0") public class MasterCheckFlowImpl extends MasterCheckFlow {
    /**
     * Creates a new instance.
     */
    public MasterCheckFlowImpl() {
        return;
    }
    @Override public boolean simple(Ex2 master, Ex1 model) {
        throw new UnsupportedOperationException("master check operator does not have method body");
    }
    @Override public boolean selection(Ex2 master, Ex1 model) {
        throw new UnsupportedOperationException("master check operator does not have method body");
    }
}