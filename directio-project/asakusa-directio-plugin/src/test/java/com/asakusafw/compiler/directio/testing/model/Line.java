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
package com.asakusafw.compiler.directio.testing.model;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.directio.testing.io.LineInput;
import com.asakusafw.compiler.directio.testing.io.LineOutput;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
/**
 * lineを表す射影モデルインターフェース。
 */
@DataModelKind("DMDL")@ModelInputLocation(LineInput.class)@ModelOutputLocation(LineOutput.class)@PropertyOrder({"value", 
            "first", "position", "length"}) public interface Line extends Writable {
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    Text getValue();
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    void setValue(Text value0);
    /**
     * <code>null</code>を許すvalueを返す。
     * @return value
     */
    StringOption getValueOption();
    /**
     * valueを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    void setValueOption(StringOption option);
    /**
     * firstを返す。
     * @return first
     * @throws NullPointerException firstの値が<code>null</code>である場合
     */
    Text getFirst();
    /**
     * firstを設定する。
     * @param value0 設定する値
     */
    void setFirst(Text value0);
    /**
     * <code>null</code>を許すfirstを返す。
     * @return first
     */
    StringOption getFirstOption();
    /**
     * firstを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    void setFirstOption(StringOption option);
    /**
     * positionを返す。
     * @return position
     * @throws NullPointerException positionの値が<code>null</code>である場合
     */
    long getPosition();
    /**
     * positionを設定する。
     * @param value0 設定する値
     */
    void setPosition(long value0);
    /**
     * <code>null</code>を許すpositionを返す。
     * @return position
     */
    LongOption getPositionOption();
    /**
     * positionを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    void setPositionOption(LongOption option);
    /**
     * lengthを返す。
     * @return length
     * @throws NullPointerException lengthの値が<code>null</code>である場合
     */
    int getLength();
    /**
     * lengthを設定する。
     * @param value0 設定する値
     */
    void setLength(int value0);
    /**
     * <code>null</code>を許すlengthを返す。
     * @return length
     */
    IntOption getLengthOption();
    /**
     * lengthを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    void setLengthOption(IntOption option);
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    String getValueAsString();
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    void setValueAsString(String value0);
    /**
     * firstを返す。
     * @return first
     * @throws NullPointerException firstの値が<code>null</code>である場合
     */
    String getFirstAsString();
    /**
     * firstを設定する。
     * @param first0 設定する値
     */
    void setFirstAsString(String first0);
}