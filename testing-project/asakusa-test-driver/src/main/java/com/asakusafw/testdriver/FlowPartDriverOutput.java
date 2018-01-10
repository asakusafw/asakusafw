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
package com.asakusafw.testdriver;

import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;

/**
 * A flow output driver for testing flow-parts.
 * @since 0.2.0
 * @version 0.8.0
 * @param <T> the data model type
 */
public class FlowPartDriverOutput<T> extends FlowDriverOutput<T, FlowPartDriverOutput<T>> implements Out<T> {

    private final Out<T> out;

    /**
     * Creates a new instance.
     * @param driverContext the current test driver context
     * @param name the flow output name
     * @param modelType the data model class
     * @param vocabulary the original vocabulary
     */
    public FlowPartDriverOutput(TestDriverContext driverContext, String name, Class<T> modelType, Out<T> vocabulary) {
        super(driverContext.getCallerClass(), driverContext.getRepository(), name, modelType);
        this.out = vocabulary;
    }

    @Override
    protected FlowPartDriverOutput<T> getThis() {
        return this;
    }

    @Override
    public void add(Source<T> upstream) {
        out.add(upstream);
    }

    @Override
    public FlowElementInput toInputPort() {
        return out.toInputPort();
    }
}
