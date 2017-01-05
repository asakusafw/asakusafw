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
package com.asakusafw.compiler.flow.processor.operator;

import com.asakusafw.compiler.flow.processor.ConvertFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.operator.Convert;

/**
 * An operator class for testing {@link ConvertFlowProcessor}.
 */
public abstract class ConvertFlow {

    private final Ex2 ex2 = new Ex2();

    /**
     * simple.
     * @param model target data model
     * @return result
     */
    @Convert
    public Ex2 simple(Ex1 model) {
        return withParameter(model, 1);
    }

    /**
     * parameterized.
     * @param model target data model
     * @param parameter additional parameter
     * @return result
     */
    @Convert
    public Ex2 withParameter(Ex1 model, int parameter) {
        ex2.setValue(model.getValue() + parameter);
        return ex2;
    }
}
