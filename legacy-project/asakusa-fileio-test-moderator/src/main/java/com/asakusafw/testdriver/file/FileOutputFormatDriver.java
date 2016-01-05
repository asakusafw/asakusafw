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
package com.asakusafw.testdriver.file;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;

/**
 * Deployes model objects using {@link FileOutputFormat}.
 * @param <V> the type of target model to deploy
 * @since 0.2.5
 */
class FileOutputFormatDriver<V> implements ModelOutput<V> {

    static final Logger LOG = LoggerFactory.getLogger(FileOutputFormatDriver.class);

    private final TaskAttemptContext context;

    private final Object key;

    private final FileOutputFormat<?, ?> format;

    @SuppressWarnings("rawtypes")
    private final RecordWriter writer;

    /**
     * Creates a new instance.
     * @param <K> type of key type
     * @param context target context with destination information
     * @param format the output format
     * @param key the common key to output
     * @throws IOException if failed to initialize
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <K> FileOutputFormatDriver(
            TaskAttemptContext context,
            FileOutputFormat<? super K, ? super V> format,
            K key) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Emulating OutputFormat: {}", format.getClass().getName());
        this.context = context;
        this.format = format;
        this.key = key;
        try {
            this.writer = format.getRecordWriter(context);
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(V model) throws IOException {
        try {
            writer.write(key, model);
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Committing output results: {}", format.getClass().getName());
        try {
            writer.close(context);
            OutputCommitter comitter = format.getOutputCommitter(context);
            comitter.commitTask(context);
            comitter.commitJob(context);
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }
}
