/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio;

/**
 * A simple implementation of {@link DataDefinition}.
 * @param <T> the data type
 * @since 0.7.0
 */
public final class SimpleDataDefinition<T> implements DataDefinition<T> {

    private final Class<? extends T> dataClass;

    private final DataFormat<T> dataFormat;

    private SimpleDataDefinition(
            Class<? extends T> dataClass,
            DataFormat<T> dataFormat) {
        this.dataClass = dataClass;
        this.dataFormat = dataFormat;
    }

    /**
     * Creates a new instance.
     * @param dataClass the data model class
     * @param dataFormat the data format
     * @param <T> the data type
     * @return the created instance
     */
    public static <T> DataDefinition<T> newInstance(
            Class<?> dataClass,
            DataFormat<T> dataFormat) {
        return new SimpleDataDefinition<T>(dataClass.asSubclass(dataFormat.getSupportedType()), dataFormat);
    }

    @Override
    public Class<? extends T> getDataClass() {
        return dataClass;
    }

    @Override
    public DataFormat<T> getDataFormat() {
        return dataFormat;
    }
}
