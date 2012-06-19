/**
 * Copyright 2011-2012 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.testdriver.testing.model;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.testdriver.testing.io.ProjectionInput;
import com.asakusafw.testdriver.testing.io.ProjectionOutput;
/**
 * projectionを表す射影モデルインターフェース。
 */
@DataModelKind("DMDL")@ModelInputLocation(ProjectionInput.class)@ModelOutputLocation(ProjectionOutput.class)@
        PropertyOrder({"data"}) public interface Projection extends Writable {
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
    void setDataAsString(String data0);
}