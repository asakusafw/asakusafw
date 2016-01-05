/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Data model format of {@link InputStream} / {@link OutputStream} .
 * This implementation class must have a public constructor without any parameters.
 * @param <T> the type of target data model
 * @since 0.2.5
 */
public abstract class BinaryStreamFormat<T> implements FragmentableDataFormat<T> {

    /**
     * Returns the preffered fragment size (in bytes).
     * @return the preffered fragment size, or {@code -1} as infinite
     * @throws IOException if failed to compute bytes count
     * @throws InterruptedException if interrupted
     */
    @Override
    public abstract long getPreferredFragmentSize() throws IOException, InterruptedException;

    /**
     * Returns the minimum fragment size (in bytes).
     * @return the minimum fragment size, or {@code -1} as infinite
     * @throws IOException if failed to compute bytes count
     * @throws InterruptedException if interrupted
     */
    @Override
    public abstract long getMinimumFragmentSize() throws IOException, InterruptedException;

    /**
     * Creates a new {@link ModelInput} for the specified properties.
     * @param dataType the target data type
     * @param path the path about the target stream (for label)
     * @param stream the target stream
     * @param offset starting stream offset
     * @param fragmentSize suggested fragment bytes count, or {@code -1} as infinite
     * @return the created reader
     * @throws IOException if failed to create reader
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if this does not support target property sequence,
     *     or any parameter is {@code null}
     */
    public abstract ModelInput<T> createInput(
            Class<? extends T> dataType,
            String path,
            InputStream stream,
            long offset,
            long fragmentSize) throws IOException, InterruptedException;

    /**
     * Creates a new {@link ModelOutput} for the specified properties.
     * @param dataType the target data type
     * @param path the path about the target stream (for label)
     * @param stream the target stream
     * @return the created writer
     * @throws IOException if failed to create writer
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if this does not support property sequence,
     *     or any parameter is {@code null}
     */
    public abstract ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            String path,
            OutputStream stream) throws IOException, InterruptedException;
}
