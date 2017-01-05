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
package com.asakusafw.runtime.directio;

/**
 * A simple implementation of {@link DataDefinition}.
 * @param <T> the data type
 * @since 0.7.0
 * @version 0.7.3
 */
public final class SimpleDataDefinition<T> implements DataDefinition<T> {

    private final Class<? extends T> dataClass;

    private final DataFormat<T> dataFormat;

    private final DataFilter<? super T> dataFilter;

    private SimpleDataDefinition(
            Class<? extends T> dataClass,
            DataFormat<T> dataFormat,
            DataFilter<? super T> dataFilter) {
        this.dataClass = dataClass;
        this.dataFormat = dataFormat;
        this.dataFilter = dataFilter;
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
        return newInstance(dataClass, dataFormat, null);
    }

    /**
     * Creates a new instance.
     * @param dataClass the data model class
     * @param dataFormat the data format
     * @param dataFilter the data filter (nullable)
     * @param <T> the data type
     * @return the created instance
     * @since 0.7.3
     */
    @SuppressWarnings("unchecked")
    public static <T> DataDefinition<T> newInstance(
            Class<?> dataClass,
            DataFormat<T> dataFormat,
            DataFilter<?> dataFilter) {
        return new SimpleDataDefinition<>(
                dataClass.asSubclass(dataFormat.getSupportedType()),
                dataFormat,
                (DataFilter<? super T>) dataFilter);
    }

    @Override
    public Class<? extends T> getDataClass() {
        return dataClass;
    }

    @Override
    public DataFormat<T> getDataFormat() {
        return dataFormat;
    }

    @Override
    public DataFilter<? super T> getDataFilter() {
        return dataFilter;
    }
}
