package com.asakusafw.compiler.operator.model;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.StringOption;
/**
 * A projective data model interface that represents mock_key.
 */
@DataModelKind("DMDL")@PropertyOrder({"key"}) public interface MockKey extends Writable {
    /**
     * Returns key.
     * @return key
     * @throws NullPointerException if key is <code>null</code>
     */
    Text getKey();
    /**
     * Sets key.
     * @param value the value
     */
    void setKey(Text value);
    /**
     * Returns key which may be represent <code>null</code>.
     * @return key
     */
    StringOption getKeyOption();
    /**
     * Sets key.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    void setKeyOption(StringOption option);
    /**
     * Returns key.
     * @return key
     * @throws NullPointerException if key is <code>null</code>
     */
    String getKeyAsString();
    /**
     * Returns key.
     * @param key0 the value
     */
    void setKeyAsString(String key0);
}