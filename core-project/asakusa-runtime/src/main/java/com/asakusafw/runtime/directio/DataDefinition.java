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
package com.asakusafw.runtime.directio;

/**
 * Represents the data definition.
 * @param <T> the data type
 * @since 0.7.0
 * @version 0.7.3
 */
public interface DataDefinition<T> {

    /**
     * Returns the target data model class.
     * @return the target data model class
     */
    Class<? extends T> getDataClass();

    /**
     * Returns the data format.
     * @return the data format
     */
    DataFormat<T> getDataFormat();

    /**
     * Returns the data filter.
     * @return the data filter, or {@code null} if the filter is not defined
     * @since 0.7.3
     */
    DataFilter<? super T> getDataFilter();
}
