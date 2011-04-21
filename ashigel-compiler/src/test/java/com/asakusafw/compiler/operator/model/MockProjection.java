package com.asakusafw.compiler.operator.model;
import com.asakusafw.compiler.operator.io.MockProjectionInput;
import com.asakusafw.compiler.operator.io.MockProjectionOutput;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.IntOption;
/**
 * mock_projectionを表す射影モデルインターフェース。
 */
@ModelInputLocation(MockProjectionInput.class)@ModelOutputLocation(MockProjectionOutput.class) public interface
        MockProjection {
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    int getValue();
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    void setValue(int value0);
    /**
     * <code>null</code>を許すvalueを返す。
     * @return value
     */
    IntOption getValueOption();
    /**
     * valueを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    void setValueOption(IntOption option);
}