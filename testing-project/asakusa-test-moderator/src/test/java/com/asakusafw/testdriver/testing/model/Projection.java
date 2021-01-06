/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.StringOption;
/**
 * A projective data model interface that represents projection.
 */
@DataModelKind("DMDL")@PropertyOrder({"data"}) public interface Projection extends Writable {
    /**
     * Returns data.
     * @return data
     * @throws NullPointerException if data is <code>null</code>
     */
    Text getData();
    /**
     * Sets data.
     * @param value the value
     */
    void setData(Text value);
    /**
     * Returns data which may be represent <code>null</code>.
     * @return data
     */
    StringOption getDataOption();
    /**
     * Sets data.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    void setDataOption(StringOption option);
    /**
     * Returns data.
     * @return data
     * @throws NullPointerException if data is <code>null</code>
     */
    String getDataAsString();
    /**
     * Returns data.
     * @param data0 the value
     */
    void setDataAsString(String data0);
}