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
package com.asakusafw.windgate.core.resource;

import java.io.Closeable;
import java.io.IOException;

import com.asakusafw.windgate.file.resource.Preparable;

/**
 * An abstract super interface of data source driver.
 * @param <T> the type of source data models
 * @since 0.2.2
 */
public interface SourceDriver<T> extends Preparable, Closeable {

    /**
     * Prepares this driver to invoke {@link #next()} method.
     * This method will be invoked before {@link #next()} only once.
     * This operation can execute only on the same thread as created this object.
     * @throws IOException if failed to begi
     */
    @Override
    void prepare() throws IOException;

    /**
     * Returns true iff the next data model object exists,
     * and then the {@link #get()} method returns its object.
     * This operation can execute only on the same thread as created this object.
     * @return {@code true} if the next data model object exists, otherwise {@code false}
     * @throws IOException if failed to prepare the next data
     */
    boolean next() throws IOException;

    /**
     * Gets the current data model object prepared by the {@link #next()} method.
     * This operation can execute only on the same thread as created this object.
     * @return the next data model object
     * @throws IOException if failed to prepare the next data
     * @throws IllegalStateException if the next data model object does not exist
     * @see #next()
     */
    T get() throws IOException;

    /**
     * Disposes this object.
     * This operation can execute only on the same thread as created this object.
     * @throws IOException if failed to connect
     */
    @Override
    void close() throws IOException;
}
