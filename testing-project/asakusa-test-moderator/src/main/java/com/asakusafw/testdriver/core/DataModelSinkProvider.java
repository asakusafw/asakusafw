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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.net.URI;

/**
 * Service provider interface of {@link DataModelSink} drivers.
 * <p>
 * Adding test data sources, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.testdriver.core.DataModelSinkProvider}.
 * </p>
 * @since 0.2.3
 */
public interface DataModelSinkProvider {

    /**
     * Creates a {@link DataModelSink} object corresponding to the specified URI.
     * <p>
     * If this object does not support the {@link URI}, or
     * the sink does not support for the specified {@link URI},
     * this method will return {@code null}.
     * </p>
     * @param <T> type of data model
     * @param definition the data model definition
     * @param sink the target identifier
     * @param context the current testing context
     * @return the created {@link DataModelSink},
     *     or {@code null} if the specified source is not valid for this object
     * @throws IOException if failed to create a {@link DataModelSink} from the URI
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> DataModelSink create(
            DataModelDefinition<T> definition,
            URI sink,
            TestContext context) throws IOException;
}
