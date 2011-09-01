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

import java.io.EOFException;
import java.io.IOException;

import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;

import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * An implementation of {@link SourceDriver} using {@link SequenceFile}.
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.2.3
 */
public class SequenceFileSourceDriver<K extends Writable, V extends Writable> implements SourceDriver<V> {

    private final SequenceFileProvider provider;

    private final K key;

    private final V value;

    private SequenceFile.Reader reader;

    private boolean sawNext;

    /**
     * Creates a new instance.
     * @param provider the source sequence files
     * @param key the key object used for buffer
     * @param value the value object used for buffer
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public SequenceFileSourceDriver(SequenceFileProvider provider, K key, V value) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null"); //$NON-NLS-1$
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null"); //$NON-NLS-1$
        }
        this.provider = provider;
        this.key = key;
        this.value = value;
    }

    @Override
    public void prepare() {
        sawNext = false;
        reader = null;
    }

    @Override
    public boolean next() throws IOException {
        while (true) {
            if (reader == null) {
                if (provider.next()) {
                    // TODO logging
                    reader = provider.open();
                } else {
                    sawNext = false;
                    return false;
                }
            }
            if (reader.next(key, value)) {
                sawNext = true;
                return true;
            } else {
                reader.close();
                reader = null;
                // continue
            }
        }
    }

    @Override
    public V get() throws IOException {
        if (sawNext) {
            return value;
        }
        throw new EOFException();
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        provider.close();
        sawNext = false;
    }
}
