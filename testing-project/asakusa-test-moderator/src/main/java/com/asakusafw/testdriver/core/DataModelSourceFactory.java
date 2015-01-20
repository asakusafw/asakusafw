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

/**
 * An abstract factory class of {@link DataModelSource}.
 * @since 0.2.3
 */
public abstract class DataModelSourceFactory {

    /**
     * Creates a new source.
     * @param <T> type of data model
     * @param definition the data model definition
     * @param context the current testing context
     * @return the created object
     * @throws IOException if failed to create a sink object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract <T> DataModelSource createSource(
            DataModelDefinition<T> definition,
            TestContext context) throws IOException;
}
