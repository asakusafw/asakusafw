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
package com.asakusafw.windgate.core.vocabulary;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Supports {@link InputStream} / {@link OutputStream} for data models.
 * This implementation class must have public constructor without any parameters.
 * <p><em>
 * Currently this interface is experimental.
 * </em></p>
 * @param <T> the type of target data model
 * @since 0.2.2
 */
public interface DataModelStreamSupport<T> {

    /**
     * Returns the supported data model type.
     * @return the supported type
     */
    Class<T> getSupportedType();

    /**
     * Creates a new {@link DataModelReader} for the specified properties.
     * @param stream the target stream
     * @return the created reader
     * @throws IllegalArgumentException if this does not support target property sequence,
     *     or any parameter is {@code null}
     */
    DataModelReader<T> createReader(InputStream stream);

    /**
     * Creates a new {@link DataModelWriter} for the specified properties.
     * @param stream the target stream
     * @return the created writer
     * @throws IllegalArgumentException if this does not support property sequence,
     *     or any parameter is {@code null}
     */
    DataModelWriter<T> createWriter(OutputStream stream);

    /**
     * Supports {@link InputStream} interface for data models.
     * @since 0.2.2
     * @param <T> the type of target data model
     */
    public interface DataModelReader<T> {

        /**
         * Fetches next object data from the related {@link InputStream},
         * and set properties into the specified object.
         * @param object the target object
         * @return {@code true} if the next object data exists, otherwise {@code false}
         * @throws IOException if failed to read the next object from the related stream
         * @throws IllegalArgumentException if {@code object} is {@code null}
         */
        boolean readTo(T object) throws IOException;
    }

    /**
     * Supports {@link OutputStream} for data models.
     * @since 0.2.2
     * @param <T> the type of target data model
     */
    public interface DataModelWriter<T> extends Flushable {

        /**
         * Write the specific object into the related {@link OutputStream}.
         * Please invoke {@link #flush()} before close the related object
         * when you write all objetcts.
         * @param object the data model
         * @throws IOException if failed to write the object into the related stream
         * @throws IllegalArgumentException if {@code object} is {@code null}
         */
        void write(T object) throws IOException;
    }
}
