/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import com.asakusafw.runtime.core.BatchRuntime;
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
 * @version 0.4.0
 */
public abstract class AbstractStageClient extends BaseStageClient {

    /**
     * Method name of {@link #checkEnvironment(Job, VariableTable)}.
     */
    public static final String METHOD_CHECK_ENVIRONMENT = "checkEnvironment";

    /**
     * {@link #getStageOutputPath()}のメソッド名。
     */
    public static final String METHOD_STAGE_OUTPUT_PATH = "getStageOutputPath";

    /**
     * {@link #getStageInputs()}のメソッド名。
     */
    public static final String METHOD_STAGE_INPUTS = "getStageInputs";

    /**
     * {@link #getStageOutputs()}のメソッド名。
     */
    public static final String METHOD_STAGE_OUTPUTS = "getStageOutputs";

    /**
     * {@link #getStageResources()}のメソッド名。
     */
    public static final String METHOD_STAGE_RESOURCES = "getStageResources";

    /**
     * {@link #getShuffleKeyClassOrNull()}のメソッド名。
     */
    public static final String METHOD_SHUFFLE_KEY_CLASS = "getShuffleKeyClassOrNull";

    /**
     * {@link #getShuffleValueClassOrNull()}のメソッド名。
     */
    public static final String METHOD_SHUFFLE_VALUE_CLASS = "getShuffleValueClassOrNull";

    /**
     * {@link #getPartitionerClassOrNull()}のメソッド名。
     */
    public static final String METHOD_PARTITIONER_CLASS = "getPartitionerClassOrNull";

    /**
     * {@link #getCombinerClassOrNull()}のメソッド名。
     */
    public static final String METHOD_COMBINER_CLASS = "getCombinerClassOrNull";

    /**
     * {@link #getSortComparatorClassOrNull()}のメソッド名。
     */
    public static final String METHOD_SORT_COMPARATOR_CLASS = "getSortComparatorClassOrNull";

    /**
     * {@link #getGroupingComparatorClassOrNull()}のメソッド名。
     */
    public static final String METHOD_GROUPING_COMPARATOR_CLASS = "getGroupingComparatorClassOrNull";

    /**
     * {@link #getReducerClassOrNull()}のメソッド名。
     */
    public static final String METHOD_REDUCER_CLASS = "getReducerClassOrNull";

    static final Log LOG = LogFactory.getLog(AbstractStageClient.class);

    /**
     * Checks current environment.
     * @param job target job
     * @param variables variables
     */
    protected void checkEnvironment(Job job, VariableTable variables) {
        BatchRuntime.require(0, 0);
    }

    /**
     * このステージに関する設定を行う。
     * @param job 現在設定中のジョブ
     * @param variables 変数表
     */
    protected void configureStage(Job job, VariableTable variables) {
        // この実装では特に何も行わない
        return;
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
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        conf.set(StageConstants.PROP_BATCH_ID, getBatchId());
        conf.set(StageConstants.PROP_FLOW_ID, getFlowId());
        Job job = createJob(conf);
        return submit(job);
    }

    /**
     * Creates a new job.
     * @param conf asakusa job configuration
     * @return the created job
     * @throws IOException if failed to create a new job
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Job createJob(Configuration conf) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        Job job = new Job(conf);
        VariableTable variables = getPathParser(job.getConfiguration());
        checkEnvironment(job, variables);
        configureJobInfo(job, variables);
        configureStageInput(job, variables);
        configureStageOutput(job, variables);
        configureShuffle(job, variables);
        configureStageResource(job, variables);
        configureStage(job, variables);
        return job;
    }

    private int submit(Job job) throws IOException, InterruptedException, ClassNotFoundException {
        LOG.info(MessageFormat.format(
                "Submitting Job: {0}",
                job.getJobName()));
        long start = System.currentTimeMillis();
        job.submit();
        LOG.info(MessageFormat.format(
                "Job Submitted: id={0}, name={1}",
                job.getJobID(),
                job.getJobName()));

        boolean succeed = job.waitForCompletion(true);
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
        String definitionId = getDefinitionId();

        LOG.info(MessageFormat.format("Hadoop Job Client: {0}", clientClass.getName()));
        job.setJarByClass(clientClass);

        LOG.info(MessageFormat.format("Hadoop Job Name: {0}", definitionId));
        job.setJobName(definitionId);
    }

    private void configureStageInput(Job job, VariableTable variables) {
        List<StageInput> inputList = new ArrayList<StageInput>();
        for (StageInput input : getStageInputs()) {
            Class<? extends Mapper<?, ?, ?, ?>> mapperClass = input.getMapperClass();
            String pathString = input.getPathString();
            Class<? extends InputFormat<?, ?>> formatClass = input.getFormatClass();
            String expanded = variables.parse(pathString);
            Map<String, String> attributes = input.getAttributes();
            LOG.info(MessageFormat.format(
                    "Input: path={0}, format={1}, mapper={2}, attributes={3}",
                    expanded,
                    formatClass.getName(),
                    mapperClass.getName(),
                    attributes));
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
            LOG.info(MessageFormat.format("Reducer: {0}", reducer.getName()));
            job.setReducerClass(reducer);
        } else {
            LOG.info("Reducer: N/A");
            job.setNumReduceTasks(0);
            return;
        }

        Class<? extends Writable> outputKeyClass = or(getShuffleKeyClassOrNull(), NullWritable.class);
        Class<? extends Writable> outputValueClass = or(getShuffleValueClassOrNull(), NullWritable.class);
        LOG.info(MessageFormat.format(
                "Shuffle: key={0}, value={1}",
                outputKeyClass.getName(),
                outputValueClass.getName()));
        job.setMapOutputKeyClass(outputKeyClass);
        job.setMapOutputValueClass(outputValueClass);

        Class<? extends Reducer> combiner = getCombinerClassOrNull();
        if (combiner != null) {
            LOG.info(MessageFormat.format("Combiner: {0}", combiner.getName()));
            job.setCombinerClass(combiner);
        } else {
            LOG.info("Combiner: N/A");
        }

        Class<? extends Partitioner> partitioner = getPartitionerClassOrNull();
        if (partitioner != null) {
            LOG.info(MessageFormat.format("Partitioner: {0}", partitioner.getName()));
            job.setPartitionerClass(partitioner);
        } else {
            LOG.info("Partitioner: DEFAULT");
        }

        Class<? extends RawComparator> groupingComparator = getGroupingComparatorClassOrNull();
        if (groupingComparator != null) {
            LOG.info(MessageFormat.format("GroupingComparator: {0}", groupingComparator.getName()));
            job.setGroupingComparatorClass(groupingComparator);
        } else {
            LOG.info("GroupingComparator: DEFAULT");
        }

        Class<? extends RawComparator> sortComparator = getSortComparatorClassOrNull();
        if (sortComparator != null) {
            LOG.info(MessageFormat.format("SortComparator: {0}", sortComparator.getName()));
            job.setSortComparatorClass(sortComparator);
        } else {
            LOG.info("SortComparator: DEFAULT");
        }
    }

    private void configureStageResource(Job job, VariableTable variables) throws IOException {
        List<StageResource> resources = getStageResources();
        for (StageResource cache : resources) {
            String resolved = variables.parse(cache.getLocation());
            LOG.info(MessageFormat.format("Distributed Cache: {0} @ {1}", cache.getName(), resolved));
            StageResourceDriver.add(job, resolved, cache.getName());
        }
    }

    private void configureStageOutput(Job job, VariableTable variables) {
        String outputPath = variables.parse(getStageOutputPath());
        List<StageOutput> outputList = new ArrayList<StageOutput>();
        for (StageOutput output : getStageOutputs()) {
            String name = output.getName();
            Class<?> keyClass = output.getKeyClass();
            Class<?> valueClass = output.getValueClass();
            Class<? extends OutputFormat<?, ?>> formatClass = output.getFormatClass();
            Map<String, String> attributes = output.getAttributes();
            LOG.info(MessageFormat.format(
                    "Output: path={0}/{1}-*, format={2}, key={3}, value={4}, attributes={5}",
                    outputPath,
                    name,
                    formatClass.getName(),
                    keyClass.getName(),
                    valueClass.getName(),
                    attributes));
            outputList.add(new StageOutput(name, keyClass, valueClass, formatClass, attributes));
        }
        StageOutputDriver.set(job, outputPath, outputList);

        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(NullWritable.class);
        job.setOutputFormatClass(StageOutputFormat.class);
        job.getConfiguration().setClass(
                "mapred.output.committer.class",
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
                    "バッチで利用する引数の\"{0}\"が定義されていません",
                    PROP_ASAKUSA_BATCH_ARGS));
        } else {
            variables.defineVariables(arguments);
        }

        // replace variables
        configuration.set(PROP_ASAKUSA_BATCH_ARGS, variables.toSerialString());
        return variables;
    }
}
