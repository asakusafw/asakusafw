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
package com.asakusafw.runtime.mapreduce.simple;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.util.Progress;

import com.asakusafw.utils.io.Source;

/**
 * Read shuffle outputs.
 * @since 0.7.1
 */
public class ShuffleReader implements RawKeyValueIterator {

    private final KeyValueSorter<?, ?> sorter;

    private Source<KeyValueSlice> source;

    private final Progress progress;

    private final DataInputBuffer keyBuffer = new DataInputBuffer();

    private final DataInputBuffer valueBuffer = new DataInputBuffer();

    private boolean prepared;

    /**
     * Creates a new instance.
     * @param sorter the source sorter
     * @param progress the progress
     */
    public ShuffleReader(KeyValueSorter<?, ?> sorter, Progress progress) {
        this.sorter = sorter;
        this.progress = progress;
    }

    @Override
    public boolean next() throws IOException {
        if (source == null) {
            try {
                source = sorter.sort();
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        }
        try {
            if (source.next()) {
                KeyValueSlice slice = source.get();
                keyBuffer.reset(slice.getBytes(), slice.getKeyOffset(), slice.getKeyLength());
                valueBuffer.reset(slice.getBytes(), slice.getValueOffset(), slice.getValueLength());
                prepared = true;
            } else {
                prepared = false;
            }
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException().initCause(e);
        }
        return prepared;
    }

    @Override
    public DataInputBuffer getKey() throws IOException {
        return keyBuffer;
    }

    @Override
    public DataInputBuffer getValue() throws IOException {
        return valueBuffer;
    }

    @Override
    public void close() throws IOException {
        try {
            if (source != null) {
                source.close();
            }
        } finally {
            sorter.close();
        }
    }

    @Override
    public Progress getProgress() {
        return progress;
    }
}
