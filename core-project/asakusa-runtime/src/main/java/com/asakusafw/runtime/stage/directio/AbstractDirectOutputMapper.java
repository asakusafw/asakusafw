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
package com.asakusafw.runtime.stage.directio;

import java.io.IOException;

import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.flow.MapperWithRuntimeResource;
import com.asakusafw.runtime.model.DataModel;

/**
 * An abstract implementation of each mapper class for direct output.
 * This writes output for {@link DirectOutputReducer}.
 * @param <T> target data type
 * @since 0.2.5
 * @version 0.5.1
 */
public abstract class AbstractDirectOutputMapper<T extends DataModel<T>> extends MapperWithRuntimeResource<
        Object, T,
        AbstractDirectOutputKey, AbstractDirectOutputValue> {

    private final AbstractDirectOutputKey outputKey;

    private final T outputValue;

    private final AbstractDirectOutputValue outputValueUnion;

    /**
     * Creates a new instance.
     * @param position position of target output
     * @param keyClass key class
     * @param valueClass value class
     */
    @SuppressWarnings("unchecked")
    protected AbstractDirectOutputMapper(
            int position,
            Class<? extends AbstractDirectOutputKey> keyClass,
            Class<? extends AbstractDirectOutputValue> valueClass) {
        this.outputKey = ReflectionUtils.newInstance(keyClass, null);
        this.outputValueUnion = ReflectionUtils.newInstance(valueClass, null);
        this.outputKey.setPosition(position);
        this.outputValue = (T) outputValueUnion.switchObject(position);
    }

    @Override
    protected void map(Object key, T value, Context context) throws IOException, InterruptedException {
        outputKey.setObject(value);
        outputValue.copyFrom(value);
        context.write(outputKey, outputValueUnion);
    }
}
