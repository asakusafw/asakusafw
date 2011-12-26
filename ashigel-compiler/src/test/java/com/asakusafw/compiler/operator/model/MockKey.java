package com.asakusafw.compiler.operator.model;
import org.apache.hadoop.io.Text;

import com.asakusafw.compiler.operator.io.MockKeyInput;
import com.asakusafw.compiler.operator.io.MockKeyOutput;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.StringOption;
/**
 * mock_keyを表す射影モデルインターフェース。
 */
@DataModelKind("DMDL")@ModelInputLocation(MockKeyInput.class)@ModelOutputLocation(MockKeyOutput.class) public interface 
        MockKey {
    /**
     * keyを返す。
     * @return key
     * @throws NullPointerException keyの値が<code>null</code>である場合
     */
    Text getKey();
    /**
     * keyを設定する。
     * @param value 設定する値
     */
    void setKey(Text value);
    /**
     * <code>null</code>を許すkeyを返す。
     * @return key
     */
    StringOption getKeyOption();
    /**
     * keyを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    void setKeyOption(StringOption option);
    /**
     * keyを返す。
     * @return key
     * @throws NullPointerException keyの値が<code>null</code>である場合
     */
    String getKeyAsString();
    /**
     * keyを設定する。
     * @param key0 設定する値
     */
    void setKeyAsString(String key0);
}