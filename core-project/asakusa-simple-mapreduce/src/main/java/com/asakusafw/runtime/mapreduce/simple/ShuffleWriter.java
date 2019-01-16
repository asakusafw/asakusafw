/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * A {@link RecordWriter} for shuffle input.
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.7.1
 */
public class ShuffleWriter<K, V> extends RecordWriter<K, V> {

    private final KeyValueSorter<K, V> sorter;

    /**
     * Creates a new instance.
     * @param sorter the key value sorter
     */
    public ShuffleWriter(KeyValueSorter<K, V> sorter) {
        this.sorter = sorter;
    }

    @Override
    public void write(K key, V value) throws IOException, InterruptedException {
        sorter.put(key, value);
    }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        // do nothing
    }
}
