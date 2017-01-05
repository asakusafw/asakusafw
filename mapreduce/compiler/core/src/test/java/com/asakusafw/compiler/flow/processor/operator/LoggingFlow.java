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

import com.asakusafw.compiler.flow.processor.LoggingFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.operator.Logging;

/**
 * An operator class for testing {@link LoggingFlowProcessor}.
 */
public abstract class LoggingFlow {

    /**
     * simple.
     * @param model target data model
     * @return string representation of the input model
     */
    @Logging
    public String simple(Ex1 model) {
        return withParameter(model, model.getStringAsString());
    }

    /**
     * parameterized.
     * @param model target data model
     * @param parameter additional parameter
     * @return = {@code parameter}
     */
    @Logging
    public String withParameter(Ex1 model, String parameter) {
        return parameter;
    }
}
