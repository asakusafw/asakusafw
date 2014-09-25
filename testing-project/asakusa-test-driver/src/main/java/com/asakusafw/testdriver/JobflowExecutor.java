/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.testing.StageInfo;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.launcher.LauncherOptionsParser;
import com.asakusafw.runtime.stage.optimizer.LibraryCopySuppressionConfigurator;
import com.asakusafw.testdriver.TestExecutionPlan.Command;
import com.asakusafw.testdriver.TestExecutionPlan.Job;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Prepares and executes jobflows.
 * @since 0.2.0
 * @version 0.6.0
 */
public class JobflowExecutor {

    static final Logger LOG = LoggerFactory.getLogger(JobflowExecutor.class);

    private final TestDriverContext context;

    private final TestModerator moderator;

    private final ConfigurationFactory configurations;

    private final JobExecutor jobExecutor;

    /**
     * Creates a new instance.
     * @param context submittion context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JobflowExecutor(TestDriverContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.moderator = new TestModerator(context.getRepository(), context);
        this.jobExecutor = context.getJobExecutor();
        this.configurations = ConfigurationFactory.getDefault();
    }

    /**
     * Cleans up the working directory on the DFS.
     * @throws IOException if failed to clean up
     */
    public void cleanWorkingDirectory() throws IOException {
        Configuration conf = configurations.newInstance();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(context.getClusterWorkDir());
        Path fullPath = fs.makeQualified(path);
        LOG.debug("クラスタワークディレクトリを初期化します。Path: {}", fullPath);
        boolean deleted = fs.delete(fullPath, true);
        if (deleted) {
            LOG.debug("クラスタワークディレクトリを削除しました。Path: {}", fullPath);
        } else {
            LOG.debug("クラスタワークディレクトリを削除できませんでした。Path: {}", fullPath);
        }
    }

    /**
     * Cleans up target jobflow's input/output.
     * @param info target jobflow
     * @throws IOException if failed to clean up
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void cleanInputOutput(JobflowInfo info) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipCleanInput() == false) {
            for (Map.Entry<String, ImporterDescription> entry : info.getImporterMap().entrySet()) {
                LOG.debug("入力{}を初期化しています", entry.getKey());
                moderator.truncate(entry.getValue());
            }
        } else {
            LOG.info("入力の初期化をスキップしました");
        }

        if (context.isSkipCleanOutput() == false) {
            for (Map.Entry<String, ExporterDescription> entry : info.getExporterMap().entrySet()) {
                LOG.debug("出力{}を初期化しています", entry.getKey());
                moderator.truncate(entry.getValue());
            }
        } else {
            LOG.info("出力の初期化をスキップしました");
        }
    }

    /**
     * Prepares the target jobflow's inputs.
     * @param info target jobflow
     * @param inputs target inputs
     * @throws IOException if failed to create job processes
     * @throws IllegalStateException if input is not defined in the jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepareInput(
            JobflowInfo info,
            Iterable<? extends DriverInputBase<?>> inputs) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipPrepareInput() == false) {
            for (DriverInputBase<?> input : inputs) {
                DataModelSourceFactory source = input.getSource();
                if (source != null) {
                    String name = input.getName();
                    LOG.debug("入力{}を配置しています: {}", name, source);
                    ImporterDescription description = info.findImporter(name);
                    if (description == null) {
                        throw new IllegalStateException(MessageFormat.format(
                                "入力{0}はフロー{1}で定義されていません",
                                name,
                                info.getJobflow().getFlowId()));
                    }
                    moderator.prepare(input.getModelType(), description, source);
                }
            }
        } else {
            LOG.info("入力の配置をスキップしました");
        }
    }

    /**
     * Prepares the target jobflow's output.
     * @param info target jobflow
     * @param outputs target ouputs
     * @throws IOException if failed to create job processes
     * @throws IllegalStateException if output is not defined in the jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepareOutput(
            JobflowInfo info,
            Iterable<? extends DriverOutputBase<?>> outputs) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        if (outputs == null) {
            throw new IllegalArgumentException("outputs must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipPrepareOutput() == false) {
            for (DriverOutputBase<?> output : outputs) {
                DataModelSourceFactory source = output.getSource();
                if (source != null) {
                    String name = output.getName();
                    LOG.debug("出力{}を配置しています: {}", name, source);
                    ExporterDescription description = info.findExporter(name);
                    if (description == null) {
                        throw new IllegalStateException(MessageFormat.format(
                                "出力{0}はフロー{1}で定義されていません",
                                name,
                                info.getJobflow().getFlowId()));
                    }
                    moderator.prepare(output.getModelType(), description, source);
                }
            }
        } else {
            LOG.info("出力の配置をスキップしました");
        }
    }

    /**
     * Runs the target jobflow.
     * @param info target jobflow
     * @throws IOException if failed to create job processes
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void runJobflow(JobflowInfo info) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipRunJobflow() == false) {
            deployApplication(info);
            CommandContext commands = context.getCommandContext();
            Map<String, String> dPropMap = createHadoopProperties(commands);
            TestExecutionPlan plan = createExecutionPlan(info, commands, dPropMap);
            validatePlan(plan);
            executePlan(plan, info.getPackageFile());
        } else {
            LOG.info("フローの実行をスキップしました");
        }
    }

    private void deployApplication(JobflowInfo info) throws IOException {
        LOG.debug("Deploying application library: {}", info.getPackageFile());
        File jobflowDest = context.getJobflowPackageLocation(info.getJobflow().getBatchId());
        FileUtils.copyFileToDirectory(info.getPackageFile(), jobflowDest);

        File dependenciesDest = context.getLibrariesPackageLocation(info.getJobflow().getBatchId());
        if (dependenciesDest.exists()) {
            LOG.debug("Cleaning up dependency libraries: {}", dependenciesDest);
            FileUtils.deleteDirectory(dependenciesDest);
        }

        File dependencies = context.getLibrariesPath();
        if (dependencies.exists()) {
            LOG.debug("Deplogying dependency libraries: {} -> {}", dependencies, dependenciesDest);
            if (dependenciesDest.mkdirs() == false && dependenciesDest.isDirectory() == false) {
                LOG.warn(MessageFormat.format(
                        "フォルダの作成に失敗しました: {0}",
                        dependenciesDest.getAbsolutePath()));
            }
            for (File file : dependencies.listFiles()) {
                if (file.isFile() == false) {
                    continue;
                }
                LOG.debug("Copying a library: {} -> {}", file, dependenciesDest);
                FileUtils.copyFileToDirectory(file, dependenciesDest);
            }
        }
    }

    private Map<String, String> createHadoopProperties(CommandContext commands) {
        assert commands != null;
        Map<String, String> dPropMap = Maps.create();
        dPropMap.put(StageConstants.PROP_USER, context.getOsUser());
        dPropMap.put(StageConstants.PROP_EXECUTION_ID, commands.getExecutionId());
        dPropMap.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS, commands.getVariableList());
        // disables libraries cache
        dPropMap.put(LauncherOptionsParser.KEY_CACHE_ENABLED, String.valueOf(false));
        // suppresses library copying only if is on local mode
        dPropMap.put(LibraryCopySuppressionConfigurator.KEY_ENABLED, String.valueOf(true));
        dPropMap.putAll(context.getExtraConfigurations());
        return dPropMap;
    }

    private TestExecutionPlan createExecutionPlan(
            JobflowInfo info,
            CommandContext commands,
            Map<String, String> properties) {
        assert info != null;
        assert commands != null;
        assert properties != null;
        List<Job> jobs = Lists.create();
        for (StageInfo stage : info.getStages()) {
            jobs.add(new Job(stage.getClassName(), commands.getExecutionId(), properties));
        }

        List<Command> initializers = Lists.create();
        List<Command> importers = Lists.create();
        List<Command> exporters = Lists.create();
        List<Command> finalizers = Lists.create();

        for (ExternalIoCommandProvider provider : info.getCommandProviders()) {
            initializers.addAll(convert(provider.getInitializeCommand(commands)));
            importers.addAll(convert(provider.getImportCommand(commands)));
            exporters.addAll(convert(provider.getExportCommand(commands)));
            finalizers.addAll(convert(provider.getFinalizeCommand(commands)));
        }

        return new TestExecutionPlan(
                info.getJobflow().getFlowId(),
                commands.getExecutionId(),
                initializers,
                importers,
                jobs,
                exporters,
                finalizers);
    }

    private List<TestExecutionPlan.Command> convert(List<ExternalIoCommandProvider.Command> commands) {
        List<TestExecutionPlan.Command> results = Lists.create();
        for (ExternalIoCommandProvider.Command cmd : commands) {
            results.add(new TestExecutionPlan.Command(
                    cmd.getCommandTokens(),
                    cmd.getModuleName(),
                    cmd.getProfileName(),
                    cmd.getEnvironment()));
        }
        return results;
    }

    private void validatePlan(TestExecutionPlan plan) {
        jobExecutor.validatePlan(plan);
    }

    private void executePlan(TestExecutionPlan plan, File jobflowPackageFile) throws IOException {
        assert plan != null;
        assert jobflowPackageFile != null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing plan: home={}, batchId={}, flowId={}, execId={}, args={}, executor={}", new Object[] {
                    context.getFrameworkHomePath(),
                    context.getCurrentBatchId(),
                    context.getCurrentFlowId(),
                    context.getCurrentExecutionId(),
                    context.getBatchArgs(),
                    jobExecutor.getClass().getName(),
            });
        }
        try {
            runJobFlowCommands(plan.getInitializers());
            runJobFlowCommands(plan.getImporters());
            runJobflowJobs(jobflowPackageFile, plan.getJobs());
            runJobFlowCommands(plan.getExporters());
        } finally {
            runJobFlowCommands(plan.getFinalizers());
        }
    }

    private void runJobflowJobs(File jobflowPackageFile, List<Job> jobs) throws IOException {
        assert jobflowPackageFile != null;
        assert jobs != null;
        for (Job job : jobs) {
            jobExecutor.execute(job, getEnvironmentVariables());
        }
    }

    private void runJobFlowCommands(List<TestExecutionPlan.Command> cmdList) throws IOException {
        assert cmdList != null;
        for (TestExecutionPlan.Command command : cmdList) {
            jobExecutor.execute(command, getEnvironmentVariables());
        }
    }

    private Map<String, String> getEnvironmentVariables() {
        Map<String, String> variables = Maps.from(context.getEnvironmentVariables());
        return variables;
    }

    /**
     * Verifies the jobflow's results.
     * @param info target jobflow
     * @param verifyContext verification context
     * @param outputs output information
     * @throws IOException if failed to verify
     * @throws IllegalStateException if output is not defined in the jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @throws AssertionError if actual output is different for the exected output
     */
    public void verify(
            JobflowInfo info,
            VerifyContext verifyContext,
            Iterable<? extends DriverOutputBase<?>> outputs) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        if (verifyContext == null) {
            throw new IllegalArgumentException("verifyContext must not be null"); //$NON-NLS-1$
        }
        if (outputs == null) {
            throw new IllegalArgumentException("outputs must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipVerify() == false) {
            StringBuilder sb = new StringBuilder();
            boolean sawError = false;
            for (DriverOutputBase<?> output : outputs) {
                String name = output.getName();
                ExporterDescription description = info.findExporter(name);
                if (description == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "出力{0}はフロー{1}で定義されていません",
                            name,
                            info.getJobflow().getFlowId()));
                }
                if (output.getResultSink() != null) {
                    LOG.debug("出力{}を保存しています: {}", output.getName(), output.getResultSink());
                    moderator.save(output.getModelType(), description, output.getResultSink());
                }
                if (output.getVerifier() != null) {
                    LOG.debug("出力{}を検証しています: {}", name, output.getVerifier());
                    List<Difference> diffList = moderator.inspect(
                            output.getModelType(),
                            description,
                            verifyContext,
                            output.getVerifier());
                    if (diffList.isEmpty() == false) {
                        sawError = true;
                        String message = MessageFormat.format(
                                "{0}.{1}の出力{2}には{3}個の差異があります",
                                info.getJobflow().getBatchId(),
                                info.getJobflow().getFlowId(),
                                output.getName(),
                                diffList.size());
                        sb.append(String.format("%s:%n", message));
                        LOG.warn(message);
                        if (output.getDifferenceSink() != null) {
                            LOG.debug("出力{}の差異を出力しています: {}", name, output.getDifferenceSink());
                            moderator.save(output.getModelType(), diffList, output.getDifferenceSink());
                        }
                        for (Difference difference : diffList) {
                            sb.append(String.format("%s: %s%n",
                                    output.getModelType().getSimpleName(),
                                    difference));
                        }
                    }
                }
            }
            if (sawError) {
                throw new AssertionError(sb);
            }
        } else {
            LOG.info("実行結果の検証をスキップしました");
        }
    }
}
