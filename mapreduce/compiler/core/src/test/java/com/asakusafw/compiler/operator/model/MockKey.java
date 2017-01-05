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