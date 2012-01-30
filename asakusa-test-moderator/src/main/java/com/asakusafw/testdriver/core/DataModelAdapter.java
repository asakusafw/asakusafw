/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

/**
 * Converts data model objects into unified {@link DataModelReflection}s.
 * @since 0.2.0
 */
public interface DataModelAdapter {

    /**
     * Creates the {@link DataModelDefinition} object from a data model class.
     * @param <T> type of data model
     * @param modelClass a data model class
     * @return the corresponded definition object, or {@code null} if not supported
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> DataModelDefinition<T> get(Class<T> modelClass);
}
