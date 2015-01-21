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
package com.asakusafw.runtime.stage.output;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.security.TokenCache;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;

/**
 * A temporary output format.
 * @param <T> target type
 * @since 0.2.5
 * @version 0.7.0
 */
public final class TemporaryOutputFormat<T> extends OutputFormat<NullWritable, T> {

    static final Log LOG = LogFactory.getLog(TemporaryOutputFormat.class);

    /**
     * The default output name prefix.
     */
    public static final String DEFAULT_FILE_NAME = "part"; //$NON-NLS-1$

    private static final String KEY_OUTPUT_PATH = "com.asakusafw.temporary.output"; //$NON-NLS-1$

    private final Map<TaskAttemptID, FileOutputCommitter> commiterCache =
            new WeakHashMap<TaskAttemptID, FileOutputCommitter>();

    @Override
    public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Path path = getOutputPath(context);
        if (TemporaryOutputFormat.getOutputPath(context) == null) {
            throw new IOException("Temporary output path is not set");
        }
        TokenCache.obtainTokensForNamenodes(
                context.getCredentials(),
                new Path[] { path },
                context.getConfiguration());
        if (path.getFileSystem(context.getConfiguration()).exists(path)) {
            throw new IOException(MessageFormat.format(
                    "Output directory {0} already exists",
                    path));
        }
    }

    @Override
    public RecordWriter<NullWritable, T> getRecordWriter(
            TaskAttemptContext context) throws IOException, InterruptedException {
        @SuppressWarnings("unchecked")
        Class<T> valueClass = (Class<T>) context.getOutputValueClass();
        return createRecordWriter(context, DEFAULT_FILE_NAME, valueClass);
    }

    /**
     * Creates a new {@link RecordWriter} to output temporary data.
     * @param <V> value type
     * @param context current context
     * @param name output name
     * @param dataType value type
     * @return the created writer
     * @throws IOException if failed to create a new {@link RecordWriter}
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <V> RecordWriter<NullWritable, V> createRecordWriter(
            TaskAttemptContext context,
            String name,
            Class<V> dataType) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        CompressionCodec codec = null;
        Configuration conf = context.getConfiguration();
        if (FileOutputFormat.getCompressOutput(context)) {
            Class<?> codecClass = FileOutputFormat.getOutputCompressorClass(context, DefaultCodec.class);
            codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
        }
        FileOutputCommitter committer = getOutputCommitter(context);
        final Path file = new Path(
                committer.getWorkPath(),
                FileOutputFormat.getUniqueFile(context, name, "")); //$NON-NLS-1$
        final ModelOutput<V> out = TemporaryStorage.openOutput(conf, dataType, file, codec);
        return new RecordWriter<NullWritable, V>() {

            @Override
            public void write(NullWritable key, V value) throws IOException {
                out.write(value);
            }

            @Override
            public void close(TaskAttemptContext ignored) throws IOException {
                out.close();
            }

            @Override
            public String toString() {
                return String.format("TemporaryOutput(%s)", file); //$NON-NLS-1$
            }
        };
    }

    @Override
    public FileOutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException {
        synchronized (this) {
            TaskAttemptID id = context.getTaskAttemptID();
            FileOutputCommitter committer = commiterCache.get(id);
            if (committer == null) {
                committer = createOutputCommitter(context);
            }
            commiterCache.put(id, committer);
            return committer;
        }
    }

    private FileOutputCommitter createOutputCommitter(TaskAttemptContext context) throws IOException {
        assert context != null;
        if (getOutputPath(context).equals(FileOutputFormat.getOutputPath(context))) {
            return (FileOutputCommitter) new EmptyFileOutputFormat().getOutputCommitter(context);
        } else {
            return new FileOutputCommitter(getOutputPath(context), context);
        }
    }

    /**
     * Returns the output path.
     * @param context current context
     * @return the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #setOutputPath(JobContext, Path)
     */
    public static Path getOutputPath(JobContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        String pathString = context.getConfiguration().get(KEY_OUTPUT_PATH);
        if (pathString == null) {
            return null;
        }
        return new Path(pathString);
    }

    /**
     * Configures output path.
     * @param context current context
     * @param path target output path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void setOutputPath(JobContext context, Path path) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        context.getConfiguration().set(KEY_OUTPUT_PATH, path.toString());
    }
}
