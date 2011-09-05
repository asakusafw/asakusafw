/**
 * Copyright 2011 Asakusa Framework Team.
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
 * An abstract super interface of data drain driver.
 * @param <T> the type of source data models
 * @since 0.2.2
 */
public interface DrainDriver<T> extends Preparable, Closeable {

    /**
     * Prepares this driver to invoke {@link #put(Object)} method.
     * This method will be invoked before {@link #put(Object)} only once.
     * This operation can execute only on the same thread as created this object.
     * @throws IOException if failed to begin
     */
    @Override
    void prepare() throws IOException;

    /**
     * Puts each data model object into the connected resource.
     * This operation can execute only on the same thread as created this object.
     * @param object the data model object to be put
     * @throws IOException if failed to put data
     * @throws IllegalArgumentException if the specified object is not valid
     */
    void put(T object) throws IOException;

    /**
     * Disposes this object.
     * This operation can execute only on the same thread as created this object.
     * @throws IOException if failed to connect
     */
    @Override
    void close() throws IOException;
}
