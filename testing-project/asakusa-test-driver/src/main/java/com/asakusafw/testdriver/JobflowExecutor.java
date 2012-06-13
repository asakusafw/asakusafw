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
package com.asakusafw.testdriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.asakusafw.testdriver.TestExecutionPlan.Command;
import com.asakusafw.testdriver.TestExecutionPlan.Job;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Prepares and executes jobflows.
 * @since 0.2.0
 */
public class JobflowExecutor {

    static final Logger LOG = LoggerFactory.getLogger(JobflowExecutor.class);

    /**
     * Path to the script to submit a stage job (relative path from {@link TestDriverContext#getFrameworkHomePath()}).
     */
    public static final String SUBMIT_JOB_SCRIPT = "testing/bin/hadoop-execute.sh";

    private final TestDriverContext context;

    private final TestModerator moderator;

    private final ConfigurationFactory configurations;

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
        this.configurations = ConfigurationFactory.getDefault();
    }

    /**
     * Cleans up the working directory on the DFS.
     * @throws IOException if failed to clean up
     */
    public void cleanWorkingDirectory() throws IOException {
        Configuration conf = configurations.newInstance();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(fs.getHomeDirectory(), context.getClusterWorkDir());
        LOG.debug("クラスタワークディレクトリを初期化します。Path: {}", path);
        fs.delete(path, true);
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
            File destDir = context.getJobflowPackageLocation(info.getJobflow().getBatchId());
            FileUtils.copyFileToDirectory(info.getPackageFile(), destDir);

            CommandContext commands = context.getCommandContext();
            Map<String, String> dPropMap = createHadoopProperties(commands);
            TestExecutionPlan plan = createExecutionPlan(info, commands, dPropMap);
            executePlan(plan, info.getPackageFile());
        } else {
            LOG.info("フローの実行をスキップしました");
        }
    }

    private Map<String, String> createHadoopProperties(CommandContext commands) {
        assert commands != null;
        Map<String, String> dPropMap = new HashMap<String, String>();
        dPropMap.put(StageConstants.PROP_USER, context.getOsUser());
        dPropMap.put(StageConstants.PROP_EXECUTION_ID, commands.getExecutionId());
        dPropMap.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS, commands.getVariableList());
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
        List<Job> jobs = new ArrayList<Job>();
        for (StageInfo stage : info.getStages()) {
            jobs.add(new Job(stage.getClassName(), commands.getExecutionId(), properties));
        }

        List<Command> initializers = new ArrayList<Command>();
        List<Command> importers = new ArrayList<Command>();
        List<Command> exporters = new ArrayList<Command>();
        List<Command> finalizers = new ArrayList<Command>();

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
        List<TestExecutionPlan.Command> results = new ArrayList<TestExecutionPlan.Command>();
        for (ExternalIoCommandProvider.Command cmd : commands) {
            results.add(new TestExecutionPlan.Command(
                    cmd.getCommandTokens(),
                    cmd.getModuleName(),
                    cmd.getProfileName(),
                    cmd.getEnvironment()));
        }
        return results;
    }

    private void executePlan(TestExecutionPlan plan, File jobflowPackageFile) throws IOException {
        assert plan != null;
        assert jobflowPackageFile != null;
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
        // DSLコンパイラが生成したHadoopジョブの各ステージを順番に実行する。
        // 各Hadoopジョブを実行した都度、hadoopコマンドの戻り値の検証を行う。
        for (Job job : jobs) {
            HadoopJobInfo jobElement = new HadoopJobInfo(
                    job.getExecutionId(),
                    jobflowPackageFile.getAbsolutePath(),
                    job.getClassName(),
                    job.getProperties());
            runHadoopJob(jobElement);
        }
    }

    private void runJobFlowCommands(List<TestExecutionPlan.Command> cmdList) throws IOException {
        assert cmdList != null;
        // DSLコンパイラが生成したコマンドを順番に実行する。
        // 各コマンドを実行した都度、終了コードの検証を行う。
        for (TestExecutionPlan.Command command : cmdList) {
            List<String> cmdToken = command.getCommandTokens();
            String[] cmd = cmdToken.toArray(new String[cmdToken.size()]);
            runShellAndAssert(cmd, getEnvironmentVariables());
        }
    }

    private void runHadoopJob(HadoopJobInfo hadoopJobInfo) throws IOException {
        assert hadoopJobInfo != null;
        List<String> command = new ArrayList<String>();
        command.add(new File(context.getFrameworkHomePath(), SUBMIT_JOB_SCRIPT).getAbsolutePath());
        command.add(hadoopJobInfo.getJarName());
        command.add(hadoopJobInfo.getClassName());

        Map<String, String> dPropMap = hadoopJobInfo.getDPropMap();
        if (dPropMap != null) {
            for (Map.Entry<String, String> entry : dPropMap.entrySet()) {
                command.add("-D");
                command.add(entry.getKey() + "=" + entry.getValue());
            }
        }

        int exitValue = runShell(command.toArray(new String[command.size()]), getEnvironmentVariables());
        if (exitValue != 0) {
            // 異常終了
            throw new AssertionError(MessageFormat.format(
                    "Hadoopジョブの実行に失敗しました (exitCode={0}, flowId={1}, command={2})",
                    exitValue,
                    hadoopJobInfo.getJobFlowId(),
                    command));
        }
    }

    private Map<String, String> getEnvironmentVariables() {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put(TestDriverContext.ENV_FRAMEWORK_PATH, context.getFrameworkHomePath().getAbsolutePath());
        return variables;
    }

    /**
     * Runs the specified command.
     * @param shellCmd command tokens
     * @param environmentVariables variables
     * @return the exit code
     * @throws IOException if failed to create/destroy a process
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public int runShell(
            String[] shellCmd,
            Map<String, String> environmentVariables) throws IOException {
        if (shellCmd == null) {
            throw new IllegalArgumentException("shellCmd must not be null"); //$NON-NLS-1$
        }
        if (environmentVariables == null) {
            throw new IllegalArgumentException("environmentVariables must not be null"); //$NON-NLS-1$
        }
        LOG.info("[COMMAND] {}", toStringShellCmdArray(shellCmd));

        ProcessBuilder builder = new ProcessBuilder(shellCmd);
        builder.redirectErrorStream(true);
        builder.environment().putAll(environmentVariables);
        builder.directory(new File(System.getProperty("user.home", ".")));

        int exitCode;
        Process process = null;
        InputStream is = null;
        try {
            process = builder.start();
            is = process.getInputStream();
            InputStreamThread it = new InputStreamThread(is);

            it.start();
            exitCode = process.waitFor();
            it.join();
        } catch (InterruptedException e) {
            throw new IOException(MessageFormat.format(
                    "コマンドの実行中に割り込みが指定されました: {0}",
                    toStringShellCmdArray(shellCmd)), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (process != null) {
                    process.getOutputStream().close();
                    process.getErrorStream().close();
                    process.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return exitCode;
    }

    private void runShellAndAssert(String[] shellCmd, Map<String, String> variables) throws IOException {
        assert shellCmd != null;
        assert variables != null;
        int exitCode = runShell(shellCmd, variables);
        if (exitCode != 0) {
            throw new AssertionError(MessageFormat.format(
                    "コマンドの実行に失敗しました (exitCode={0}, command=\"{1}\")",
                    exitCode,
                    toStringShellCmdArray(shellCmd)));
        }
    }

    private String toStringShellCmdArray(String[] shellCmd) {
        assert shellCmd != null;
        StringBuilder sb = new StringBuilder();
        for (String cmd : shellCmd) {
            sb.append(cmd).append(" ");
        }
        return sb.toString().trim();
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
            sb.append(String.format("%n"));
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
                    LOG.debug("出力{}を保存しています: {}", output.getName(), output.getVerifier());
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
                        LOG.warn("{}.{}の出力{}には{}個の差異があります", new Object[] {
                                info.getJobflow().getBatchId(),
                                info.getJobflow().getFlowId(),
                                output.getName(),
                                diffList.size(),
                        });
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

/**
 * InputStreamを読み込むスレッド。
 */
class InputStreamThread extends Thread {

    private BufferedReader br;

    private final List<String> list = new ArrayList<String>();

    /**
     * コンストラクタ。
     *
     * @param is
     *            入力ストリーム
     */
    public InputStreamThread(InputStream is) {
        br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
    }

    /**
     * コンストラクタ。
     *
     * @param is
     *            入力ストリーム
     * @param charset
     *            Readerに渡すcharset
     */
    public InputStreamThread(InputStream is, String charset) {
        try {
            br = new BufferedReader(new InputStreamReader(is, charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        for (;;) {
            try {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                list.add(line);
                System.out.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
