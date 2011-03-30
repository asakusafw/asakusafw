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
package com.asakusafw.runtime.stage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;

import com.asakusafw.runtime.stage.input.StageInputDriver;
import com.asakusafw.runtime.stage.output.StageOutputDriver;
import com.asakusafw.runtime.stage.resource.StageResourceDriver;
import com.asakusafw.runtime.util.VariableTable;

/**
 * ステージごとの処理を起動するクライアントの基底クラス。
 */
public abstract class AbstractStageClient extends Configured implements Tool {

    /**
     * 実行ユーザー名のプロパティキー。
     */
    public static final String PROP_USER = "com.asakusafw.user";

    /**
     * 実行IDのプロパティキー。
     */
    public static final String PROP_EXECUTION_ID = "com.asakusafw.executionId";

    /**
     * 環境変数表のプロパティキー。
     */
    public static final String PROP_ASAKUSA_BATCH_ARGS = "com.asakusafw.batchArgs";

    /**
     * 実行ユーザー名の変数名。
     */
    public static final String VAR_USER = "user";

    /**
     * 実行IDの変数名。
     */
    public static final String VAR_EXECUTION_ID = "execution_id";

    /**
     * バッチIDの変数名。
     */
    public static final String VAR_BATCH_ID = "batch_id";

    /**
     * フローIDの変数名。
     */
    public static final String VAR_FLOW_ID = "flow_id";

    /**
     * 定義IDの変数名。
     */
    public static final String VAR_DEFINITION_ID = "jobflow_name";

    /**
     * ステージ名の変数名。
     */
    public static final String VAR_STAGE_ID = "stage_name";

    /**
     * 実行ユーザー名の変数表記。
     */
    public static final String EXPR_USER = VariableTable.toVariable(VAR_USER);

    /**
     * 実行IDの変数表記。
     */
    public static final String EXPR_EXECUTION_ID = VariableTable.toVariable(VAR_EXECUTION_ID);

    /**
     * 定義IDの変数表記。
     */
    public static final String EXPR_DEFINITION_ID = VariableTable.toVariable(VAR_DEFINITION_ID);

    /**
     * ステージ名の変数表記。
     */
    public static final String EXPR_STAGE_ID = VariableTable.toVariable(VAR_STAGE_ID);

    /**
     * {@link #getBatchId()}のメソッド名。
     */
    public static final String METHOD_BATCH_ID = "getBatchId";

    /**
     * {@link #getFlowId()}のメソッド名。
     */
    public static final String METHOD_FLOW_ID = "getFlowId";

    /**
     * {@link #getStageId()}のメソッド名。
     */
    public static final String METHOD_STAGE_ID = "getStageId";

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

    /**
     * {@link #getOutputKeyClassOrNull()}のメソッド名。
     */
    public static final String METHOD_OUTPUT_KEY_CLASS = "getOutputKeyClassOrNull";

    /**
     * {@link #getOutputValueClassOrNull()}のメソッド名。
     */
    public static final String METHOD_OUTPUT_VALUE_CLASS = "getOutputValueClassOrNull";

    /**
     * {@link #getOutputFormatClassOrNull()}のメソッド名。
     */
    public static final String METHOD_OUTPUT_FORMAT_CLASS = "getOutputFormatClassOrNull";

    static final Log LOG = LogFactory.getLog(AbstractStageClient.class);

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
     * 処理中のユーザー名を返す。
     * @return 処理中のユーザー名
     */
    protected String getUser() {
        return getMandatoryProperty(PROP_USER);
    }

    /**
     * このフロー全体(一連のステージ)の実行時識別子を返す。
     * @return 実行時識別子
     */
    protected String getExecutionId() {
        return getMandatoryProperty(PROP_EXECUTION_ID);
    }

    private String getMandatoryProperty(String key) {
        assert key != null;
        String value = getConf().get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "プロパティ\"{0}\"が設定されていません",
                    key));
        }
        return value;
    }

    /**
     * このクライアントによって起動されるジョブのバッチIDを返す。
     * @return バッチID
     */
    protected abstract String getBatchId();

    /**
     * このクライアントによって起動されるジョブのフローIDを返す。
     * @return フローID
     */
    protected abstract String getFlowId();

    /**
     * このクライアントによって起動されるジョブのステージ名を返す。
     * @return ステージ名
     */
    protected abstract String getStageId();

    /**
     * このクライアントによって起動されるジョブの定義IDを返す。
     * @return 定義ID
     */
    protected String getDefinitionId() {
        String batchId = getBatchId();
        String flowId = getFlowId();
        String stageId = getStageId();
        return getDefinitionId(batchId, flowId, stageId);
    }

    /**
     * 指定のIDの組から、対象ジョブの定義IDを算出して返す。
     * @param batchId バッチID
     * @param flowId フローID
     * @param stageId ステージID
     * @return ジョブの定義ID
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getDefinitionId(String batchId, String flowId, String stageId) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        if (stageId == null) {
            throw new IllegalArgumentException("stageId must not be null"); //$NON-NLS-1$
        }
        return MessageFormat.format("{0}.{1}.{2}", batchId, flowId, stageId);
    }

    /**
     * このステージからの出力に利用するベースパスを返す。
     * @return ベースパス
     */
    protected abstract String getStageOutputPath();

    /**
     * このステージへの入力一覧を返す。
     * @return 入力一覧
     */
    protected abstract List<StageInput> getStageInputs();

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

    /**
     * このステージの最終結果で利用するキークラスを返す。
     * @return 最終結果で利用するキークラス、利用しない場合は{@code null}
     */
    protected Class<? extends Writable> getOutputKeyClassOrNull() {
        return null;
    }

    /**
     * このステージの最終結果で利用する値クラスを返す。
     * @return 最終結果で利用する値クラス、利用しない場合は{@code null}
     */
    protected Class<? extends Writable> getOutputValueClassOrNull() {
        return null;
    }

    /**
     * このステージの最終結果で利用する出力フォーマットを返す。
     * @return 最終結果で利用する出力フォーマット、利用しない場合は{@code null}
     */
    @SuppressWarnings("rawtypes")
    protected Class<? extends OutputFormat> getOutputFormatClassOrNull() {
        return null;
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        Job job = new Job(conf);

        VariableTable variables = getPathParser(job.getConfiguration());
        configureJobInfo(job, variables);
        configureStageInput(job, variables);
        configureStageOutput(job, variables);
        configureShuffle(job, variables);
        configureStageResource(job, variables);
        configureStage(job, variables);

        return submit(job);
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
        // TODO 定義IDでは重複実行を許さないのでは
        // おそらくユーザー名などの情報も必要

        Class<?> clientClass = getClass();
        String definitionId = getDefinitionId();

        LOG.info(MessageFormat.format("Hadoop Job Client: {0}", clientClass.getName()));
        job.setJarByClass(clientClass);

        LOG.info(MessageFormat.format("Hadoop Job Name: {0}", definitionId));
        job.setJobName(definitionId);
    }

    private void configureStageInput(Job job, VariableTable variables) {
        for (StageInput input : getStageInputs()) {
            Class<? extends Mapper<?, ?, ?, ?>> mapperClass = input.getMapperClass();
            String pathString = input.getPathString();
            Class<? extends InputFormat<?, ?>> formatClass = input.getFormatClass();
            String expanded = variables.parse(pathString);
            LOG.info(MessageFormat.format(
                    "Input: path={0}, format={1}, mapper={2}",
                    expanded,
                    formatClass.getName(),
                    mapperClass.getName()));
            StageInputDriver.add(
                    job,
                    new Path(expanded),
                    formatClass,
                    mapperClass);
        }
    }

    @SuppressWarnings("rawtypes")
    private void configureShuffle(Job job, VariableTable variables) {
        Class<? extends Reducer> reducer = getReducerClassOrNull();
        if (reducer != null) {
            LOG.info(MessageFormat.format("Reducer: {0}", reducer.getName()));
            job.setReducerClass(reducer);
        } else {
            LOG.info("Reducer: N/A");
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

    private void configureStageResource(Job job, VariableTable variables) {
        List<StageResource> resources = getStageResources();
        for (StageResource cache : resources) {
            String resolved = variables.parse(cache.getLocation());
            LOG.info(MessageFormat.format("Distributed Cache: {0} @ {1}", cache.getName(), resolved));
            StageResourceDriver.add(job, new Path(resolved), cache.getName());
        }
    }

    @SuppressWarnings("rawtypes")
    private void configureStageOutput(Job job, VariableTable variables) {
        String outputPath = variables.parse(getStageOutputPath());
        StageOutputDriver.setPath(job, new Path(outputPath));
        for (StageOutput output : getStageOutputs()) {
            String name = output.getName();
            Class<?> keyClass = output.getKeyClass();
            Class<?> valueClass = output.getValueClass();
            Class<? extends OutputFormat<?, ?>> formatClass = output.getFormatClass();
            LOG.info(MessageFormat.format(
                    "Output: path={0}/{1}-*, format={2}, key={3}, value={4}",
                    outputPath,
                    name,
                    formatClass.getName(),
                    keyClass.getName(),
                    valueClass.getName()));
            StageOutputDriver.add(
                    job,
                    name,
                    formatClass,
                    keyClass,
                    valueClass);
        }

        Class<? extends Writable> outputKeyClass = or(getOutputKeyClassOrNull(), NullWritable.class);
        Class<? extends Writable> outputValueClass = or(getOutputValueClassOrNull(), NullWritable.class);
        Class<? extends OutputFormat> outputFormatClass =
            or(getOutputFormatClassOrNull(), SequenceFileOutputFormat.class);
        LOG.info(MessageFormat.format(
                "Output: path={0}/{1}-*, format={2}, key={3}, value={4}",
                outputPath,
                "part",
                outputFormatClass.getName(),
                outputKeyClass.getName(),
                outputValueClass.getName()));
        job.setOutputKeyClass(outputKeyClass);
        job.setOutputValueClass(outputValueClass);
        job.setOutputFormatClass(outputFormatClass);
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
        VariableTable variables = new VariableTable();
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
        return variables;
    }
}
