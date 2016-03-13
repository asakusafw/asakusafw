/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;

/**
 * A flow input driver for testing flow-parts.
 * @since 0.2.0
 * @version 0.8.0
 * @param <T> the data model type
 */
public class FlowPartDriverInput<T> extends FlowDriverInput<T, FlowPartDriverInput<T>> implements In<T> {

    private final In<T> in;

    /**
     * Creates a new instance.
     * @param driverContext the current test driver context
     * @param name the flow input name
     * @param modelType the data model class
     * @param vocabulary the original vocabulary
     */
    public FlowPartDriverInput(TestDriverContext driverContext, String name, Class<T> modelType, In<T> vocabulary) {
        super(driverContext.getCallerClass(), driverContext.getRepository(), name, modelType);
        this.in = vocabulary;
    }

    @Override
    protected FlowPartDriverInput<T> getThis() {
        return this;
    }

    /**
     * Configures the estimated data size for this input.
     * @param dataSize the estimated data size (nullable)
     * @return this
     * @deprecated cannot change the estimated data size on testing flow-part
     */
    @Deprecated
    public FlowPartDriverInput<T> withDataSize(DataSize dataSize) {
        return this;
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return in.toOutputPort();
    }
}
