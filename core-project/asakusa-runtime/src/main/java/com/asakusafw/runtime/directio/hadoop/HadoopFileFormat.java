/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.FragmentableDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Data model format of {@link InputStream} / {@link OutputStream} .
 * This implementation class must have a public constructor without any parameters.
 * @param <T> the type of target data model
 * @since 0.2.6
 */
public abstract class HadoopFileFormat<T> extends Configured implements FragmentableDataFormat<T> {

    /**
     * Creates a new instance without configuration.
     */
    public HadoopFileFormat() {
        super();
    }

    /**
     * Creates a new instance with configuration.
     * @param conf configuration
     */
    public HadoopFileFormat(Configuration conf) {
        super(conf);
    }

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
     * @param fileSystem the file system to open the target path
     * @param path the path to the target file
     * @param offset starting stream offset
     * @param fragmentSize suggested fragment bytes count, or {@code -1} as infinite
     * @param counter the current counter
     * @return the created reader
     * @throws IOException if failed to create reader
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if this does not support target property sequence,
     *     or any parameter is {@code null}
     */
    public abstract ModelInput<T> createInput(
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            long offset,
            long fragmentSize,
            Counter counter) throws IOException, InterruptedException;

    /**
     * Creates a new {@link ModelOutput} for the specified properties.
     * @param dataType the target data type
     * @param fileSystem the file system to open the target path
     * @param path the path to the target file
     * @param counter the current counter
     * @return the created writer
     * @throws IOException if failed to create writer
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if this does not support property sequence,
     *     or any parameter is {@code null}
     */
    public abstract ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            Counter counter) throws IOException, InterruptedException;
}
