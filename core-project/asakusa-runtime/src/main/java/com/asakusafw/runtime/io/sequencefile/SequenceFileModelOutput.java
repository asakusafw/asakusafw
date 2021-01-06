/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.sequencefile;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;

import com.asakusafw.runtime.io.ModelOutput;

/**
 * An implementation of {@link ModelOutput} using {@link SequenceFile}.
 * @param <T> the data model type
 */
public class SequenceFileModelOutput<T> implements ModelOutput<T> {

    private final SequenceFile.Writer writer;

    private Closeable closeable;

    /**
     * Creates a new instance.
     * @param writer the target writer
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public SequenceFileModelOutput(SequenceFile.Writer writer) {
        this(writer, writer);
    }

    /**
     * Creates a new instance.
     * @param writer target writer
     * @param closeable close target when {@link #close()} is invoked
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SequenceFileModelOutput(SequenceFile.Writer writer, Closeable closeable) {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        if (closeable == null) {
            throw new IllegalArgumentException("closeable must not be null"); //$NON-NLS-1$
        }
        this.writer = writer;
        this.closeable = closeable;
    }

    @Override
    public void write(T model) throws IOException {
        writer.append(NullWritable.get(), model);
    }

    @Override
    public void close() throws IOException {
        writer.close();
        closeable.close();
    }
}
