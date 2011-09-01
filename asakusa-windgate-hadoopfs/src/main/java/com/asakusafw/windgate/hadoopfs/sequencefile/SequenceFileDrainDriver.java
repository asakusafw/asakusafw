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
package com.asakusafw.windgate.hadoopfs.sequencefile;

import java.io.IOException;

import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;

import com.asakusafw.windgate.core.resource.DrainDriver;

/**
 * An implementation of {@link DrainDriver} using {@link SequenceFile}.
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.2.3
 */
public class SequenceFileDrainDriver<K extends Writable, V extends Writable> implements DrainDriver<V> {

    private final SequenceFile.Writer writer;

    private final K key;

    /**
     * Creates a new instance.
     * @param writer the drain sequence file
     * @param key the common key
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SequenceFileDrainDriver(SequenceFile.Writer writer, K key) {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        this.writer = writer;
        this.key = key;
    }

    @Override
    public void prepare() throws IOException {
        return;
    }

    @Override
    public void put(V object) throws IOException {
        writer.append(key, object);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
