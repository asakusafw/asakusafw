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
package com.asakusafw.runtime.stage;

import static com.asakusafw.runtime.stage.StageConstants.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.stage.input.StageInputDriver;
import com.asakusafw.runtime.stage.input.StageInputFormat;
import com.asakusafw.runtime.stage.input.StageInputMapper;
import com.asakusafw.runtime.stage.output.LegacyBridgeOutputCommitter;
import com.asakusafw.runtime.stage.output.StageOutputDriver;
import com.asakusafw.runtime.stage.output.StageOutputFormat;
import com.asakusafw.runtime.stage.resource.StageResourceDriver;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;

/**
 * ステージごとの処理を起動するクライアントの基底クラス。
 * @since 0.1.0
 * @version 0.7.1
 */
public abstract class AbstractStageClient extends BaseStageClient {

    /**
     * {@link #getStageOutputPath()}のメソッド名。
     */
    public static final String METHOD_STAGE_OUTPUT_PATH = "getStageOutputPath"; //$NON-NLS-1$

    /**
     * {@link #getStageInputs()}のメソッド名。
     */
    public static final String METHOD_STAGE_INPUTS = "getStageInputs"; //$NON-NLS-1$

    /**
     * {@link #getStageOutputs()}のメソッド名。
     */
    public static final String METHOD_STAGE_OUTPUTS = "getStageOutputs"; //$NON-NLS-1$

    /**
     * {@link #getStageResources()}のメソッド名。
     */
    public static final String METHOD_STAGE_RESOURCES = "getStageResources"; //$NON-NLS-1$

    /**
     * {@link #getShuffleKeyClassOrNull()}のメソッド名。
     */
    public static final String METHOD_SHUFFLE_KEY_CLASS = "getShuffleKeyClassOrNull"; //$NON-NLS-1$

    /**
     * {@link #getShuffleValueClassOrNull()}のメソッド名。
     */
    public static final String METHOD_SHUFFLE_VALUE_CLASS = "getShuffleValueClassOrNull"; //$NON-NLS-1$

    /**
     * {@link #getPartitionerClassOrNull()}のメソッド名。
     */
    public static final String METHOD_PARTITIONER_CLASS = "getPartitionerClassOrNull"; //$NON-NLS-1$

    /**
     * {@link #getCombinerClassOrNull()}のメソッド名。
     */
    public static final String METHOD_COMBINER_CLASS = "getCombinerClassOrNull"; //$NON-NLS-1$

    /**
     * {@link #getSortComparatorClassOrNull()}のメソッド名。
     */
    public static final String METHOD_SORT_COMPARATOR_CLASS = "getSortComparatorClassOrNull"; //$NON-NLS-1$

    /**
     * {@link #getGroupingComparatorClassOrNull()}のメソッド名。
     */
    public static final String METHOD_GROUPING_COMPARATOR_CLASS = "getGroupingComparatorClassOrNull"; //$NON-NLS-1$

    /**
     * {@link #getReducerClassOrNull()}のメソッド名。
     */
    public static final String METHOD_REDUCER_CLASS = "getReducerClassOrNull"; //$NON-NLS-1$

    static final Log LOG = LogFactory.getLog(AbstractStageClient.class);

    /**
     * Configures the {@link Job} object for this stage.
     * @param job the target job
     * @param variables current variable table
     * @throws IOException if failed to configure the job
     * @throws InterruptedException if interrupted while configuring {@link Job} object
     */
    protected void configureStage(Job job, VariableTable variables) throws IOException, InterruptedException {
        ClassLoader loader = job.getConfiguration().getClassLoader();
        for (StageConfigurator configurator : ServiceLoader.load(StageConfigurator.class, loader)) {
            configurator.configure(job);
        }
    }

    /**
     * このステージへの入力一覧を返す。
     * @return 入力一覧
     */
    protected abstract List<StageInput> getStageInputs();

    /**
     * このステージからの出力に利用するベースパスを返す。
     * @return ベースパス
     */
    protected abstract String getStageOutputPath();

    /**
     * このステージからの出力一覧を返す。
     * @return 出力一覧
     */
    protected List<StageOutput> getStageOutputs() {
        return Collections.emptyList();
    }

    /**
     * このステージで利用するキャッシュファイルの一覧を返す。
     * @return キャッシュファイル一覧
     */
    protected List<StageResource> getStageResources() {
        return Collections.emptyList();
    }

    /**
     * このステージのシャッフルフェーズで利用するキークラスを返す。
     * @return シャッフルフェーズで利用するキークラス、利用しない場合は{@code null}
     */
    protected Class<? extends Writable> getShuffleKeyClassOrNull() {
        return null;
    }

    /**
     * このステージのシャッフルフェーズで利用する値クラスを返す。
     * @return シャッフルフェーズで利用する値クラス、利用しない場合は{@code null}
     */
    protected Class<? extends Writable> getShuffleValueClassOrNull() {
        return null;
    }

    /**
     * このステージで利用するパーティショナークラスを返す。
     * @return パーティショナークラス、利用しない場合は{@code null}
     */
    @SuppressWarnings("rawtypes")
    protected Class<? extends Partitioner> getPartitionerClassOrNull() {
        return null;
    }

    /**
     * このステージで利用するコンバイナークラスを返す。
     * @return コンバイナークラス、利用しない場合は{@code null}
     */
    @SuppressWarnings("rawtypes")
    protected Class<? extends Reducer> getCombinerClassOrNull() {
        return null;
    }

    /**
     * キーの整列に利用する比較クラスを返す。
     * @return 比較クラス、利用しない場合は{@code null}
     */
    @SuppressWarnings("rawtypes")
    protected Class<? extends RawComparator> getSortComparatorClassOrNull() {
        return null;
    }

    /**
     * キーのグループ化に利用する比較クラスを返す。
     * @return 比較クラス、利用しない場合は{@code null}
     */
    @SuppressWarnings("rawtypes")
    protected Class<? extends RawComparator> getGroupingComparatorClassOrNull() {
        return null;
    }

    /**
     * このステージで利用するレデューサークラスを返す。
     * @return レデューサークラス、利用しない場合は{@code null}
     */
    @SuppressWarnings("rawtypes")
    protected Class<? extends Reducer> getReducerClassOrNull() {
        return null;
    }

    @Override
    protected int execute(String[] args) throws Exception {
        Configuration conf = getConf();
        conf.set(StageConstants.PROP_BATCH_ID, getBatchId());
        conf.set(StageConstants.PROP_FLOW_ID, getFlowId());
        LOG.info(MessageFormat.format(
                "Initializing Job: batchId={0}, flowId={1}, executionId={2}, stageId={3}",
                getBatchId(),
                getFlowId(),
                getExecutionId(),
                getStageId()));
        Job job = createJob(conf);
        return submit(job);
    }

    /**
     * Creates a new job.
     * @param conf asakusa job configuration
     * @return the created job
     * @throws IOException if failed to create a new job
     * @throws InterruptedException if interrupted while creating {@link Job} object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Job createJob(Configuration conf) throws IOException, InterruptedException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        Job job = JobCompatibility.newJob(conf);
        VariableTable variables = getPathParser(job.getConfiguration());
        configureJobInfo(job, variables);
        configureStageInput(job, variables);
        configureStageOutput(job, variables);
        configureShuffle(job, variables);
        configureStageResource(job, variables);
        configureStage(job, variables);
        return job;
    }

    private int submit(Job job) throws IOException, InterruptedException, ClassNotFoundException {
        String jobRunnerClassName = job.getConfiguration().get(StageConstants.PROP_JOB_RUNNER);
        JobRunner runner = DefaultJobRunner.INSTANCE;
        if (jobRunnerClassName != null) {
            Class<?> jobRunnerClass = job.getConfiguration().getClassByName(jobRunnerClassName);
            runner = (JobRunner) ReflectionUtils.newInstance(jobRunnerClass, job.getConfiguration());
        }
        LOG.info(MessageFormat.format(
                "Submitting Job: {0} (runner: {1})",
                job.getJobName(),
                runner));
        long start = System.currentTimeMillis();
        boolean succeed;
        if (RuntimeContext.get().isSimulation()) {
            LOG.info(MessageFormat.format(
                    "Job is skipped because current execution status is in simulation mode: name={0}",
                    job.getJobName()));
            succeed = true;
        } else {
            succeed = runner.run(job);
        }
        long end = System.currentTimeMillis();
        LOG.info(MessageFormat.format(
                "Job Finished: elapsed=[{3}]ms, succeed={2}, id={0}, name={1}",
                job.getJobID(),
                job.getJobName(),
                succeed,
                String.valueOf(end - start)));

        return succeed ? ToolLauncher.JOB_SUCCEEDED : ToolLauncher.JOB_FAILED;
    }

    private void configureJobInfo(Job job, VariableTable variables) {
        Class<?> clientClass = getClass();
        String operationId = getOperationId();

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Hadoop Job Client: {0}", clientClass.getName())); //$NON-NLS-1$
        }
        String jar = job.getConfiguration().get(PROP_APPLICATION_JAR);
        if (jar == null || (job.getConfiguration() instanceof JobConf) == false) {
            job.setJarByClass(clientClass);
        } else {
            ((JobConf) job.getConfiguration()).setJar(jar);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Hadoop Job Name: {0}", operationId)); //$NON-NLS-1$
        }
        job.setJobName(operationId);
    }

    private void configureStageInput(Job job, VariableTable variables) {
        List<StageInput> inputList = new ArrayList<StageInput>();
        for (StageInput input : getStageInputs()) {
            Class<? extends Mapper<?, ?, ?, ?>> mapperClass = input.getMapperClass();
            String pathString = input.getPathString();
            Class<? extends InputFormat<?, ?>> formatClass = input.getFormatClass();
            String expanded = variables.parse(pathString);
            Map<String, String> attributes = input.getAttributes();
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Input: path={0}, format={1}, mapper={2}, attributes={3}", //$NON-NLS-1$
                        expanded,
                        formatClass.getName(),
                        mapperClass.getName(),
                        attributes));
            }
            inputList.add(new StageInput(expanded, formatClass, mapperClass, attributes));
        }
        StageInputDriver.set(job, inputList);
        job.setInputFormatClass(StageInputFormat.class);
        job.setMapperClass(StageInputMapper.class);
    }

    @SuppressWarnings("rawtypes")
    private void configureShuffle(Job job, VariableTable variables) {
        Class<? extends Reducer> reducer = getReducerClassOrNull();
        if (reducer != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Reducer: {0}", reducer.getName())); //$NON-NLS-1$
            }
            job.setReducerClass(reducer);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reducer: N/A"); //$NON-NLS-1$
            }
            job.setNumReduceTasks(0);
            return;
        }

        Class<? extends Writable> outputKeyClass = or(getShuffleKeyClassOrNull(), NullWritable.class);
        Class<? extends Writable> outputValueClass = or(getShuffleValueClassOrNull(), NullWritable.class);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Shuffle: key={0}, value={1}", //$NON-NLS-1$
                    outputKeyClass.getName(),
                    outputValueClass.getName()));
        }
        job.setMapOutputKeyClass(outputKeyClass);
        job.setMapOutputValueClass(outputValueClass);

        Class<? extends Reducer> combiner = getCombinerClassOrNull();
        if (combiner != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Combiner: {0}", combiner.getName())); //$NON-NLS-1$
            }
            job.setCombinerClass(combiner);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Combiner: N/A"); //$NON-NLS-1$
            }
        }

        Class<? extends Partitioner> partitioner = getPartitionerClassOrNull();
        if (partitioner != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Partitioner: {0}", partitioner.getName())); //$NON-NLS-1$
            }
            job.setPartitionerClass(partitioner);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Partitioner: DEFAULT"); //$NON-NLS-1$
            }
        }

        Class<? extends RawComparator> groupingComparator = getGroupingComparatorClassOrNull();
        if (groupingComparator != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("GroupingComparator: {0}", groupingComparator.getName())); //$NON-NLS-1$
            }
            job.setGroupingComparatorClass(groupingComparator);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("GroupingComparator: DEFAULT"); //$NON-NLS-1$
            }
        }

        Class<? extends RawComparator> sortComparator = getSortComparatorClassOrNull();
        if (sortComparator != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("SortComparator: {0}", sortComparator.getName())); //$NON-NLS-1$
            }
            job.setSortComparatorClass(sortComparator);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("SortComparator: DEFAULT"); //$NON-NLS-1$
            }
        }
    }

    private void configureStageResource(Job job, VariableTable variables) throws IOException {
        List<StageResource> resources = getStageResources();
        for (StageResource cache : resources) {
            String resolved = variables.parse(cache.getLocation());
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Distributed Cache: {0} @ {1}", //$NON-NLS-1$
                        cache.getName(),
                        resolved));
            }
            if (RuntimeContext.get().isSimulation()) {
                LOG.info("Preparing distributed cache is skipped in simulation mode");
            } else {
                StageResourceDriver.add(job, resolved, cache.getName());
            }
        }
    }

    private void configureStageOutput(Job job, VariableTable variables) throws IOException {
        String outputPath = variables.parse(getStageOutputPath());
        List<StageOutput> outputList = new ArrayList<StageOutput>();
        for (StageOutput output : getStageOutputs()) {
            String name = output.getName();
            Class<?> keyClass = output.getKeyClass();
            Class<?> valueClass = output.getValueClass();
            Class<? extends OutputFormat<?, ?>> formatClass = output.getFormatClass();
            Map<String, String> attributes = output.getAttributes();
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Output: path={0}/{1}-*, format={2}, key={3}, value={4}, attributes={5}", //$NON-NLS-1$
                        outputPath,
                        name,
                        formatClass.getName(),
                        keyClass.getName(),
                        valueClass.getName(),
                        attributes));
            }
            outputList.add(new StageOutput(name, keyClass, valueClass, formatClass, attributes));
        }
        StageOutputDriver.set(job, outputPath, outputList);

        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(NullWritable.class);
        job.setOutputFormatClass(StageOutputFormat.class);
        job.getConfiguration().setClass(
                "mapred.output.committer.class", //$NON-NLS-1$
                LegacyBridgeOutputCommitter.class,
                org.apache.hadoop.mapred.OutputCommitter.class);
    }

    private <T> T or(T a, T b) {
        if (a != null) {
            return a;
        } else {
            return b;
        }
    }

    private VariableTable getPathParser(Configuration configuration) {
        assert configuration != null;
        VariableTable variables = new VariableTable(RedefineStrategy.IGNORE);
        variables.defineVariable(VAR_USER, getUser());
        variables.defineVariable(VAR_DEFINITION_ID, getDefinitionId());
        variables.defineVariable(VAR_STAGE_ID, getStageId());
        variables.defineVariable(VAR_BATCH_ID, getBatchId());
        variables.defineVariable(VAR_FLOW_ID, getFlowId());
        variables.defineVariable(VAR_EXECUTION_ID, getExecutionId());
        String arguments = configuration.get(PROP_ASAKUSA_BATCH_ARGS);
        if (arguments == null) {
            LOG.warn(MessageFormat.format(
                    "Missing configuration \"{0}\" (batch arguments)",
                    PROP_ASAKUSA_BATCH_ARGS));
        } else {
            variables.defineVariables(arguments);
        }

        // replace variables
        configuration.set(PROP_ASAKUSA_BATCH_ARGS, variables.toSerialString());
        return variables;
    }

    private static final class DefaultJobRunner implements JobRunner {

        static final JobRunner INSTANCE = new DefaultJobRunner();

        @Override
        public boolean run(Job job) throws IOException, InterruptedException, ClassNotFoundException {
            job.submit();
            LOG.info(MessageFormat.format(
                    "starting job using {0}: {1} ({2})",
                    this,
                    job.getJobID(),
                    job.getJobName()));
            return job.waitForCompletion(true);
        }

        @Override
        public String toString() {
            return "Hadoop job runner"; //$NON-NLS-1$
        }
    }
}
