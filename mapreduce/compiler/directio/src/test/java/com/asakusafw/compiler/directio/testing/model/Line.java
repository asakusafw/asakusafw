/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
/**
 * A projective data model interface that represents line.
 */
@DataModelKind("DMDL")@PropertyOrder({"value", "first", "position", "length"}) public interface Line extends Writable {
    /**
     * Returns value.
     * @return value
     * @throws NullPointerException if value is <code>null</code>
     */
    Text getValue();
    /**
     * Sets value.
     * @param value0 the value
     */
    void setValue(Text value0);
    /**
     * Returns value which may be represent <code>null</code>.
     * @return value
     */
    StringOption getValueOption();
    /**
     * Sets value.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    void setValueOption(StringOption option);
    /**
     * Returns first.
     * @return first
     * @throws NullPointerException if first is <code>null</code>
     */
    Text getFirst();
    /**
     * Sets first.
     * @param value0 the value
     */
    void setFirst(Text value0);
    /**
     * Returns first which may be represent <code>null</code>.
     * @return first
     */
    StringOption getFirstOption();
    /**
     * Sets first.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    void setFirstOption(StringOption option);
    /**
     * Returns position.
     * @return position
     * @throws NullPointerException if position is <code>null</code>
     */
    long getPosition();
    /**
     * Sets position.
     * @param value0 the value
     */
    void setPosition(long value0);
    /**
     * Returns position which may be represent <code>null</code>.
     * @return position
     */
    LongOption getPositionOption();
    /**
     * Sets position.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    void setPositionOption(LongOption option);
    /**
     * Returns length.
     * @return length
     * @throws NullPointerException if length is <code>null</code>
     */
    int getLength();
    /**
     * Sets length.
     * @param value0 the value
     */
    void setLength(int value0);
    /**
     * Returns length which may be represent <code>null</code>.
     * @return length
     */
    IntOption getLengthOption();
    /**
     * Sets length.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    void setLengthOption(IntOption option);
    /**
     * Returns value.
     * @return value
     * @throws NullPointerException if value is <code>null</code>
     */
    String getValueAsString();
    /**
     * Returns value.
     * @param value0 the value
     */
    void setValueAsString(String value0);
    /**
     * Returns first.
     * @return first
     * @throws NullPointerException if first is <code>null</code>
     */
    String getFirstAsString();
    /**
     * Returns first.
     * @param first0 the value
     */
    void setFirstAsString(String first0);
}