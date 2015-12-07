/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import com.asakusafw.runtime.core.Result;

/**
 * An implementation of {@link Result} which put data model objects into the final output.
 * @param <T> the data model type
 * @since 0.1.0
 * @version 0.5.0
 */
public class ResultOutput<T> implements Result<T> {

    static final Log LOG = LogFactory.getLog(ResultOutput.class);

    private final TaskAttemptContext context;

    private final RecordWriter<Object, Object> writer;

    private final List<Counter> counters;

    private long records;

    /**
     * Creates a new instance.
     * @param context the current context
     * @param writer the record writer for writing data model objects
     * @throws IOException if failed to initialize the output
     * @throws InterruptedException if interrupted while initializing the output
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    @SuppressWarnings({ "rawtypes" })
    public ResultOutput(TaskAttemptContext context, RecordWriter writer) throws IOException, InterruptedException {
        this(context, writer, Collections.<Counter>emptyList());
    }

    /**
     * Creates a new instance.
     * @param context current context
     * @param writer target writer
     * @param counters record counters
     * @throws IOException if initialization failed by I/O exception
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ResultOutput(
            TaskAttemptContext context,
            RecordWriter writer,
            List<Counter> counters) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        if (counters == null) {
            throw new IllegalArgumentException("counters must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.writer = writer;
        this.counters = counters;
    }

    // CHECKSTYLE:OFF JavadocMethodCheck - Result.OutputException is ignored by checkstyle
    /**
     * Writes a key and value into current context.
     * @param context current context
     * @param key the key object (nullable)
     * @param value the value object (nullable)
     * @param <K> type of key object
     * @param <V> type of value object
     * @throws Result.OutputException if failed to write the objects
     * @since 0.5.0
     */
    public static <K, V> void write(TaskInputOutputContext<?, ?, ? super K, ? super V> context, K key, V value) {
        try {
            context.write(key, value);
        } catch (Exception e) {
            throw new Result.OutputException(e);
        }
    }
    // CHECKSTYLE:ON JavadocMethodCheck

    @Override
    public void add(T result) {
        try {
            writer.write(getKey(result), result);
            context.progress();
            records++;
        } catch (Exception e) {
            throw new Result.OutputException(e);
        }
    }

    /**
     * Returns a key for the value.
     * @param result target value
     * @return a corresponded key
     */
    protected Object getKey(T result) {
        return NullWritable.get();
    }

    /**
     * Finalizes and closes this output.
     * @throws IOException if failed to finalize the output
     * @throws InterruptedException if interrupted while finalizing the output
     */
    public void close() throws IOException, InterruptedException {
        for (Counter counter : counters) {
            counter.increment(records);
        }
        writer.close(context);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", context.getTaskAttemptID(), writer); //$NON-NLS-1$
    }
}
