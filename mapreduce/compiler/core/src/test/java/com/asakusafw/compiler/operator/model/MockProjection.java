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