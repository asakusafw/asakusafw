/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.compiler.operator.model;
import com.asakusafw.compiler.operator.io.MockProjectionInput;
import com.asakusafw.compiler.operator.io.MockProjectionOutput;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.IntOption;
/**
 * mock_projectionを表す射影モデルインターフェース。
 */
@DataModelKind("DMDL")@ModelInputLocation(MockProjectionInput.class)@ModelOutputLocation(MockProjectionOutput.class) 
        public interface MockProjection {
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