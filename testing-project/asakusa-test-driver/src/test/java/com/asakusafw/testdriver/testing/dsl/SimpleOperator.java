/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.testing.dsl;

import com.asakusafw.runtime.core.BatchContext;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.vocabulary.operator.Update;

/**
 * A simple operator class.
 * @since 0.2.0
 */
public abstract class SimpleOperator {

    /**
     * Sets a value.
     * @param model target model
     * @param value value to be set
     */
    @Update
    public void setValue(Simple model, String value) {
        String orig = model.getValueAsString();
        if (orig.equals("ERROR")) {
            throw new RuntimeException();
        }
        model.setValueAsString(orig + value + arg("t1") + arg("t2") + arg("t3"));
    }

    private static String arg(String key) {
        String value = BatchContext.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }
}
