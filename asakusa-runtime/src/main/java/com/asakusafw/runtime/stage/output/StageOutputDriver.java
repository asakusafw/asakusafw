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
package com.asakusafw.runtime.stage.output;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.ResultOutput;
import com.asakusafw.runtime.stage.StageOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryOutputFormat;

/**
 * ステージ出力を設定するためのドライバ。
 * <p>
 * 現在のところ、{@link FileOutputFormat}およびそのサブクラスに関するもののみを取り扱う。
 * </p>
 */
public class StageOutputDriver {

    static final Log LOG = LogFactory.getLog(StageOutputDriver.class);

    private static final String K_NAMES = "com.asakusafw.stage.output.names";

    private static final String K_FORMAT_PREFIX = "com.asakusafw.stage.output.format.";

    private static final String K_KEY_PREFIX = "com.asakusafw.stage.output.key.";

    private static final String K_VALUE_PREFIX = "com.asakusafw.stage.output.value.";

    private final Map<String, ResultOutput<?>> resultSinks;

    /**
     * インスタンスを生成する。
     * @param context 現在のタスク試行コンテキスト
     * @throws IOException 出力の初期化に失敗した場合
     * @throws InterruptedException 出力の初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public StageOutputDriver(
            TaskInputOutputContext<?, ?, ?, ?> context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.resultSinks = buildSinks(context);
    }

    private Map<String, ResultOutput<?>> buildSinks(
            TaskInputOutputContext<?, ?, ?, ?> context) throws IOException, InterruptedException {
        assert context != null;
        Map<String, ResultOutput<?>> results = new HashMap<String, ResultOutput<?>>();
        Configuration conf = context.getConfiguration();
        for (String name : conf.getStringCollection(K_NAMES)) {
            ResultOutput<?> sink = buildSink(context, name);
            results.put(name, sink);
        }
        return results;
    }

    private ResultOutput<?> buildSink(
            TaskInputOutputContext<?, ?, ?, ?> context,
            String name) throws IOException, InterruptedException {
        assert context != null;
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

        if (TemporaryOutputFormat.class.isAssignableFrom(formatClass)) {
            return buildTemporarySink(context, name, valueClass);
        } else {
            return buildNormalSink(context, name, formatClass, keyClass, valueClass);
        }
    }

    private ResultOutput<?> buildTemporarySink(
            TaskAttemptContext context,
            String name,
            Class<?> valueClass) throws IOException, InterruptedException {
        assert context != null;
        assert name != null;
        assert valueClass != null;
        TemporaryOutputFormat<?> format = new TemporaryOutputFormat<Object>();
        RecordWriter<?, ?> writer = format.createRecordWriter(context, name, valueClass);
        return new ResultOutput<Writable>(context, writer);
    }

    private ResultOutput<?> buildNormalSink(
            TaskAttemptContext context,
            String name,
            @SuppressWarnings("rawtypes") Class<? extends OutputFormat> formatClass,
            Class<?> keyClass,
            Class<?> valueClass) throws IOException, InterruptedException {
        assert context != null;
        assert name != null;
        assert formatClass != null;
        assert keyClass != null;
        assert valueClass != null;
        Job job = new Job(context.getConfiguration());
        job.setOutputFormatClass(formatClass);
        job.setOutputKeyClass(keyClass);
        job.setOutputValueClass(valueClass);
        TaskAttemptContext localContext = new TaskAttemptContext(
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

    private static final String METHOD_SET_OUTPUT_NAME = "setOutputName";

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
     * 指定の名前を持つ出力のシンクオブジェクトを返す。
     * <p>
     * ここに指定する名前は、ジョブの起動時にあらかじめ
     * {@link #set(Job, String, Collection)}で登録しておく必要がある。
     * </p>
     * @param <T> 出力の型
     * @param name 出力の名前
     * @return 対応するシンクオブジェクト
     * @throws IOException 出力の作成に失敗した場合
     * @throws InterruptedException 出力の作成時に割り込みが発行された場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Writable> Result<T> getResultSink(
            String name) throws IOException, InterruptedException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        ResultOutput<?> sink = resultSinks.get(name);
        if (sink == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Output \"{0}\" is not declared",
                    name));
        }
        return (Result<T>) sink;
    }

    /**
     * 現在の出力を破棄する。
     * @throws IOException 出力のフラッシュに失敗した場合
     * @throws InterruptedException 出力の破棄に割り込みが発行された場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void close() throws IOException, InterruptedException {
        for (ResultOutput<?> output : resultSinks.values()) {
            output.close();
        }
    }

    /**
     * Sets the output specification for this job.
     * @param job current job
     * @param outputPath base output path
     * @param outputList each output information
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public static void set(Job job, String outputPath, Collection<StageOutput> outputList) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        if (outputList == null) {
            throw new IllegalArgumentException("outputList must not be null"); //$NON-NLS-1$
        }
        List<StageOutput> brigeOutputs = new ArrayList<StageOutput>();
        List<StageOutput> normalOutputs = new ArrayList<StageOutput>();
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
        Set<String> names = new TreeSet<String>(conf.getStringCollection(K_NAMES));
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
