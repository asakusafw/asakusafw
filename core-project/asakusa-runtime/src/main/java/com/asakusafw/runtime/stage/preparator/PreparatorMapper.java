/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.preparator;

import java.io.IOException;

import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.MapperWithRuntimeResource;
import com.asakusafw.runtime.stage.output.StageOutputDriver;

/**
 * The skeletal implementation to output values directly.
 * @param <T>
 * @since 0.2.5
 * @version 0.5.1
 */
public abstract class PreparatorMapper<T> extends MapperWithRuntimeResource<
        Object, T,
        Object, T> {

    /**
     * The method name of {@link #getOutputName()}。
     */
    public static final String NAME_GET_OUTPUT_NAME = "getOutputName";

    private StageOutputDriver output;

    private Result<T> result;

    @SuppressWarnings("unchecked")
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        String name = getOutputName();
        this.output = new StageOutputDriver(context);
        this.result = (Result<T>) output.getResultSink(name);
    }

    /**
     * Returns the output name.
     * @return the output name.
     */
    public abstract String getOutputName();

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        this.output.close();
        this.output = null;
        this.result = null;
    }

    @Override
    protected void map(Object key, T value, Context context) throws IOException, InterruptedException {
        result.add(value);
    }
}
