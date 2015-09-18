package com.asakusafw.compiler.operator.model;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
/**
 * A projective data model interface that represents mock_projection.
 */
@DataModelKind("DMDL")@PropertyOrder({"value"}) public interface MockProjection extends Writable {
    /**
     * Returns value.
     * @return value
     * @throws NullPointerException if value is <code>null</code>
     */
    int getValue();
    /**
     * Sets value.
     * @param value0 the value
     */
    void setValue(int value0);
    /**
     * Returns value which may be represent <code>null</code>.
     * @return value
     */
    IntOption getValueOption();
    /**
     * Sets value.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    void setValueOption(IntOption option);
}