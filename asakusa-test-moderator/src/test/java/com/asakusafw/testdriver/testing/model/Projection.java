package com.asakusafw.testdriver.testing.model;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.testdriver.testing.io.ProjectionInput;
import com.asakusafw.testdriver.testing.io.ProjectionOutput;
import org.apache.hadoop.io.Text;
/**
 * projectionを表す射影モデルインターフェース。
 */
@DataModelKind("DMDL")@ModelInputLocation(ProjectionInput.class)@ModelOutputLocation(ProjectionOutput.class) public 
        interface Projection {
    /**
     * dataを返す。
     * @return data
     * @throws NullPointerException dataの値が<code>null</code>である場合
     */
    Text getData();
    /**
     * dataを設定する。
     * @param value 設定する値
     */
    void setData(Text value);
    /**
     * <code>null</code>を許すdataを返す。
     * @return data
     */
    StringOption getDataOption();
    /**
     * dataを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    void setDataOption(StringOption option);
    /**
     * dataを返す。
     * @return data
     * @throws NullPointerException dataの値が<code>null</code>である場合
     */
    String getDataAsString();
    /**
     * dataを設定する。
     * @param data0 設定する値
     */
    @SuppressWarnings("deprecation") void setDataAsString(String data0);
}