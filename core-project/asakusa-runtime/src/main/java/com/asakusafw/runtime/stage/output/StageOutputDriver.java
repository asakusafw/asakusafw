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
package com.asakusafw.runtime.stage.output;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.ResultOutput;
import com.asakusafw.runtime.stage.StageOutput;

/**
 * A driver for configuring stage outputs.
 */
public class StageOutputDriver {

    static final Log LOG = LogFactory.getLog(StageOutputDriver.class);

    private static final String K_NAMES = "com.asakusafw.stage.output.names"; //$NON-NLS-1$

    private static final String K_FORMAT_PREFIX = "com.asakusafw.stage.output.format."; //$NON-NLS-1$

    private static final String K_KEY_PREFIX = "com.asakusafw.stage.output.key."; //$NON-NLS-1$

    private static final String K_VALUE_PREFIX = "com.asakusafw.stage.output.value."; //$NON-NLS-1$

    private static final String COUNTER_GROUP = "com.asakusafw.stage.output.RecordCounters"; //$NON-NLS-1$

    private final Map<String, ResultOutput<?>> resultSinks;

    private final TaskInputOutputContext<?, ?, ?, ?> context;

    /**
     * Creates a new instance.
     * @param context the current context
     * @throws IOException if failed to initialize this driver
     * @throws InterruptedException if interrupted while initializing this driver
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public StageOutputDriver(
            TaskInputOutputContext<?, ?, ?, ?> context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.resultSinks = prepareSinks(context);
    }

    private static Map<String, ResultOutput<?>> prepareSinks(TaskInputOutputContext<?, ?, ?, ?> context) {
        assert context != null;
        Map<String, ResultOutput<?>> results = new HashMap<>();
        Configuration conf = context.getConfiguration();
        for (String name : conf.getStringCollection(K_NAMES)) {
            results.put(name, null);
        }
        return results;
    }

    private static final String METHOD_SET_OUTPUT_NAME = "setOutputName"; //$NON-NLS-1$

    private void setOutputFilePrefix(JobContext localContext, String name) throws IOException {
        assert localContext != null;
        assert name != null;
        try {
            Method method = FileOutputFormat.class.getDeclaredMethod(
                    METHOD_SET_OUTPUT_NAME, JobContext.class, String.class);
            method.setAccessible(true);
            method.invoke(null, localContext, name);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure output name of \"{0}\" ([MAPREDUCE-370] may be not applied)",
                    name), e);
        }
    }

    /**
     * Returns the result sink object with the specified name.
     * Clients must register the result sink before launching the job by using {@link #set(Job, String, Collection)}.
     * @param <T> the output data type
     * @param name the sink name
     * @return the corresponded sink name
     * @throws IOException if failed to initialize the target sink
     * @throws InterruptedException if interrupted while initializing the target sink
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Writable> Result<T> getResultSink(
            String name) throws IOException, InterruptedException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (resultSinks.containsKey(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Output \"{0}\" is not declared",
                    name));
        }
        ResultOutput<?> sink = resultSinks.get(name);
        if (sink == null) {
            sink = buildSink(name);
            resultSinks.put(name, sink);
        }
        return (Result<T>) sink;
    }

    private ResultOutput<?> buildSink(String name) throws IOException, InterruptedException {
        assert name != null;
        Configuration conf = context.getConfiguration();
        @SuppressWarnings("rawtypes")
        Class<? extends OutputFormat> formatClass = conf.getClass(
                getPropertyName(K_FORMAT_PREFIX, name),
                null,
                OutputFormat.class);
        Class<?> keyClass = conf.getClass(getPropertyName(K_KEY_PREFIX, name), null);
        Class<?> valueClass = conf.getClass(getPropertyName(K_VALUE_PREFIX, name), null);

        if (formatClass == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "OutputFormat is not declared for output \"{0}\"",
                    name));
        }
        if (keyClass == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Output key type is not declared for output \"{0}\"",
                    name));
        }
        if (valueClass == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Output value type is not declared for output \"{0}\"",
                    name));
        }

        List<Counter> counters = getCounters(name);
        if (TemporaryOutputFormat.class.isAssignableFrom(formatClass)) {
            return buildTemporarySink(name, valueClass, counters);
        } else {
            return buildNormalSink(name, formatClass, keyClass, valueClass, counters);
        }
    }

    private List<Counter> getCounters(String name) {
        assert name != null;
        try {
            List<Counter> results = new ArrayList<>();
            if (context.getTaskAttemptID().getTaskType() == TaskType.MAP) {
                results.add(context.getCounter(TaskCounter.MAP_OUTPUT_RECORDS));
            } else {
                results.add(context.getCounter(TaskCounter.REDUCE_OUTPUT_RECORDS));
            }
            results.add(context.getCounter(COUNTER_GROUP, name));
            return results;
        } catch (RuntimeException e) {
            LOG.warn("Failed to create counters", e);
            return Collections.emptyList();
        }
    }

    private ResultOutput<?> buildTemporarySink(
            String name,
            Class<?> valueClass,
            List<Counter> counters) throws IOException, InterruptedException {
        assert context != null;
        assert name != null;
        assert valueClass != null;
        assert counters != null;
        TemporaryOutputFormat<?> format = new TemporaryOutputFormat<>();
        RecordWriter<?, ?> writer = format.createRecordWriter(context, name, valueClass);
        return new ResultOutput<Writable>(context, writer, counters);
    }

    private ResultOutput<?> buildNormalSink(
            String name,
            @SuppressWarnings("rawtypes") Class<? extends OutputFormat> formatClass,
            Class<?> keyClass,
            Class<?> valueClass,
            List<Counter> counters) throws IOException, InterruptedException {
        assert context != null;
        assert name != null;
        assert formatClass != null;
        assert keyClass != null;
        assert valueClass != null;
        assert counters != null;
        Job job = Job.getInstance(context.getConfiguration());
        job.setOutputFormatClass(formatClass);
        job.setOutputKeyClass(keyClass);
        job.setOutputValueClass(valueClass);
        TaskAttemptContext localContext = new TaskAttemptContextImpl(
                job.getConfiguration(),
                context.getTaskAttemptID());
        if (FileOutputFormat.class.isAssignableFrom(formatClass)) {
            setOutputFilePrefix(localContext, name);
        }
        OutputFormat<?, ?> format = ReflectionUtils.newInstance(
                formatClass,
                localContext.getConfiguration());
        RecordWriter<?, ?> writer = format.getRecordWriter(localContext);
        return new ResultOutput<Writable>(localContext, writer);
    }

    /**
     * Closes this driver and finalizes all result sinks.
     * @throws IOException if failed to finalize some result sinks
     * @throws InterruptedException if interrupted while disposing the driver
     */
    public synchronized void close() throws IOException, InterruptedException {
        for (Map.Entry<String, ResultOutput<?>> entry : resultSinks.entrySet()) {
            ResultOutput<?> output = entry.getValue();
            if (output != null) {
                output.close();
                entry.setValue(null);
            }
        }
    }

    /**
     * Sets the output specification for this job.
     * @param job current job
     * @param outputPath base output path
     * @param outputList each output information
     * @throws IOException if failed to configure the output specification
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public static void set(Job job, String outputPath, Collection<StageOutput> outputList) throws IOException {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        if (outputList == null) {
            throw new IllegalArgumentException("outputList must not be null"); //$NON-NLS-1$
        }
        List<StageOutput> brigeOutputs = new ArrayList<>();
        List<StageOutput> normalOutputs = new ArrayList<>();
        boolean sawFileOutput = false;
        boolean sawTemporaryOutput = false;
        for (StageOutput output : outputList) {
            Class<? extends OutputFormat<?, ?>> formatClass = output.getFormatClass();
            if (BridgeOutputFormat.class.isAssignableFrom(formatClass)) {
                brigeOutputs.add(output);
            } else {
                normalOutputs.add(output);
            }
        }
        if (brigeOutputs.isEmpty() == false) {
            BridgeOutputFormat.set(job, brigeOutputs);
        }
        for (StageOutput output : normalOutputs) {
            String name = output.getName();
            Class<?> keyClass = output.getKeyClass();
            Class<?> valueClass = output.getValueClass();
            Class<? extends OutputFormat<?, ?>> formatClass = output.getFormatClass();
            sawFileOutput |= FileOutputFormat.class.isAssignableFrom(formatClass);
            sawTemporaryOutput |= TemporaryOutputFormat.class.isAssignableFrom(formatClass);
            addOutput(job, name, formatClass, keyClass, valueClass);
        }
        if (sawFileOutput) {
            FileOutputFormat.setOutputPath(job, new Path(outputPath));
        }
        if (sawTemporaryOutput) {
            TemporaryOutputFormat.setOutputPath(job, new Path(outputPath));
        }
    }

    private static void addOutput(
            Job job,
            String name,
            Class<?> formatClass,
            Class<?> keyClass,
            Class<?> valueClass) {
        assert job != null;
        assert name != null;
        assert formatClass != null;
        assert keyClass != null;
        assert valueClass != null;
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Output name \"{0}\" is not valid",
                    name));
        }
        Configuration conf = job.getConfiguration();
        Set<String> names = new TreeSet<>(conf.getStringCollection(K_NAMES));
        if (names.contains(name)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Output name \"{0}\" is already declared",
                    name));
        }
        names.add(name);
        conf.setStrings(K_NAMES, names.toArray(new String[names.size()]));
        conf.setClass(getPropertyName(K_FORMAT_PREFIX, name), formatClass, OutputFormat.class);
        conf.setClass(getPropertyName(K_KEY_PREFIX, name), keyClass, Object.class);
        conf.setClass(getPropertyName(K_VALUE_PREFIX, name), valueClass, Object.class);
    }

    private static String getPropertyName(String prefix, String name) {
        assert prefix != null;
        assert name != null;
        return prefix + name;
    }

    private static boolean isValidName(String name) {
        assert name != null;
        for (char c : name.toCharArray()) {
            if (isValidNameChar(c) == false) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidNameChar(char c) {
        return ('0' <= c && c <= '9') || ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z');
    }
}
