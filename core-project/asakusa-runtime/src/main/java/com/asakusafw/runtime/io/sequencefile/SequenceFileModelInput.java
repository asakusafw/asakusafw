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
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.io.ModelInput;

/**
 * A bridge implementation to {@link SequenceFile} into {@link ModelInput}.
 * The key type of sequence file must be {@link NullWritable}.
 * @param <T> value type
 * @since 0.2.5
 */
public class SequenceFileModelInput<T extends Writable> implements ModelInput<T> {

    private final SequenceFile.Reader reader;

    private final Closeable closeable;

    /**
     * Creates a new instance.
     * @param reader target reader
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SequenceFileModelInput(SequenceFile.Reader reader) {
        this(reader, reader);
    }

    /**
     * Creates a new instance.
     * @param reader target reader
     * @param closeable chained close target (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SequenceFileModelInput(SequenceFile.Reader reader, Closeable closeable) {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null"); //$NON-NLS-1$
        }
        this.reader = reader;
        this.closeable = closeable;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        return reader.next(NullWritable.get(), model);
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }
}
