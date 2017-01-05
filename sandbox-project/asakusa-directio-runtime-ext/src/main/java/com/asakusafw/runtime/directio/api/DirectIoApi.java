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
package com.asakusafw.runtime.directio.api;

import java.io.IOException;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.io.ModelInput;

/**
 * The Direct I/O API.
 * @since 0.9.0
 */
public interface DirectIoApi {

    /**
     * Returns data model objects from Direct I/O data sources.
     * @param <T> the data model object type
     * @param formatClass the Direct I/O data format class
     * @param basePath the base path (must not contain variables)
     * @param resourcePattern the resource pattern (must not contain variables)
     * @return the data model objects
     * @throws IOException if failed to open data model objects on the data source
     */
    <T> ModelInput<T> open(
            Class<? extends DataFormat<T>> formatClass,
            String basePath, String resourcePattern) throws IOException;
}
