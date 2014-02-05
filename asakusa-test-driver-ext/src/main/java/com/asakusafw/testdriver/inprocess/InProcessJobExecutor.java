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
package com.asakusafw.testdriver.inprocess;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.DefaultJobExecutor;
import com.asakusafw.testdriver.JobExecutor;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.TestExecutionPlan.Job;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * A default implementation of {@link JobExecutor}.
 * @since 0.6.0
 */
public class InProcessJobExecutor extends JobExecutor {

    static final Logger LOG = LoggerFactory.getLogger(InProcessJobExecutor.class);

    private static final String PATH_ASAKUSA_RESOURCES = "core/conf/asakusa-resources.xml";

    private final TestDriverContext context;

    private final DefaultJobExecutor delegate;

    private final ConfigurationFactory configurations;

    private List<CommandEmulator> commandEmulators;

    /**
     * Creates a new instance.
     * @param context the current test context
     */
    public InProcessJobExecutor(TestDriverContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.delegate = new DefaultJobExecutor(context);
        this.configurations = ConfigurationFactory.getDefault();
        this.commandEmulators = null;
    }

    @Override
    public void validateEnvironment() {
        if (requiresValidateExecutionEnvironment() == false) {
            LOG.debug("skipping test execution environment validation");
            return;
        }
        if (context.getFrameworkHomePathOrNull() == null) {
            throw new AssertionError(MessageFormat.format(
                    "環境変数\"{0}\"が未設定です",
                    TestDriverContext.ENV_FRAMEWORK_PATH));
        }
        String runtime = context.getRuntimeEnvironmentVersion();
        if (runtime == null) {
            LOG.debug("Runtime environment version is missing");
        } else {
            String develop = context.getDevelopmentEnvironmentVersion();
            if (develop.equals(runtime) == false) {
                throw new AssertionError(MessageFormat.format(
                        "開発環境とテスト実行環境でフレームワークのバージョンが一致しません（開発環境：{0}, 実行環境：{1}）",
                        develop,
                        runtime));
            }
        }
    }

    @Override
    public void validatePlan(TestExecutionPlan plan) {
        if (requiresValidateExecutionEnvironment() == false) {
            return;
        }
        List<TestExecutionPlan.Command> commands = new ArrayList<TestExecutionPlan.Command>();
        commands.addAll(plan.getInitializers());
        commands.addAll(plan.getImporters());
        commands.addAll(plan.getExporters());
        commands.addAll(plan.getFinalizers());

        for (TestExecutionPlan.Command command : commands) {
            if (findCommandEmulator(command) == null) {
                if (configurations.getHadoopCommand() == null) {
                    throw new AssertionError(MessageFormat.format(
                            "コマンド\"{0}\"を検出できませんでした",
                            "hadoop"));
                }
            }
        }
    }

    private boolean requiresValidateExecutionEnvironment() {
        String value = System.getProperty(TestDriverContext.KEY_FORCE_EXEC);
        if (value != null) {
            if (value.isEmpty() || value.equalsIgnoreCase("true")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void execute(
            TestExecutionPlan.Job job,
            Map<String, String> environmentVariables) throws IOException {
        assert job != null;
        LOG.info(MessageFormat.format(
                "Emulating hadoop job: {0}",
                job.getClassName()));
        List<String> arguments = computeHadoopJobArguments(job);
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            GenericOptionsParser parser = new GenericOptionsParser(
                    configurations.newInstance(), arguments.toArray(new String[arguments.size()]));
            Configuration conf = parser.getConfiguration();
            for (Map.Entry<String, String> entry : job.getProperties().entrySet()) {
                conf.set(entry.getKey(), entry.getValue());
            }
            try {
                Class<?> stageClass = conf.getClassLoader().loadClass(job.getClassName());
                Tool tool = (Tool) ReflectionUtils.newInstance(stageClass, conf);
                int exitValue = tool.run(new String[0]);
                if (exitValue != 0) {
                    throw new AssertionError(MessageFormat.format(
                            "Hadoopジョブの実行に失敗しました (exitCode={0}, flowId={1})",
                            exitValue,
                            context.getCurrentFlowId()));
                }
            } catch (Exception e) {
                throw (AssertionError) new AssertionError(MessageFormat.format(
                        "Hadoopジョブの実行に失敗しました (flowId={0})",
                        context.getCurrentFlowId())).initCause(e);
            } finally {
                dispose(conf);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private void dispose(Configuration conf) {
        ClassLoader cl = conf.getClassLoader();
        if (cl instanceof Closeable) {
            try {
                ((Closeable) cl).close();
            } catch (IOException e) {
                LOG.debug("Failed to dispose a ClassLoader", e);
            }
        }
    }

    private List<String> computeHadoopJobArguments(Job job) throws IOException {
        assert job != null;
        List<String> arguments = new ArrayList<String>();
        computeHadoopLibjars(arguments);
        computeAsakusaResources(arguments);
        return arguments;
    }

    private void computeHadoopLibjars(List<String> arguments) throws IOException {
        assert arguments != null;
        arguments.add("-libjars");
        StringBuilder libjars = new StringBuilder();
        File packageFile = EmulatorUtils.getJobflowLibraryPath(context);
        if (packageFile.isFile() == false) {
            throw new FileNotFoundException(packageFile.getAbsolutePath());
        }
        libjars.append(packageFile.toURI());
        for (File file : EmulatorUtils.getBatchLibraryPaths(context)) {
            libjars.append(',');
            libjars.append(file.toURI());
        }
        arguments.add(libjars.toString());
    }

    private void computeAsakusaResources(List<String> arguments) {
        assert arguments != null;
        File asakusaResources = new File(context.getFrameworkHomePath(), PATH_ASAKUSA_RESOURCES);
        if (asakusaResources.exists()) {
            arguments.add("-conf");
            arguments.add(asakusaResources.toURI().toString());
        }
    }

    @Override
    public void execute(
            TestExecutionPlan.Command command,
            Map<String, String> environmentVariables) throws IOException {
        CommandEmulator emulator = findCommandEmulator(command);
        if (emulator != null) {
            LOG.info(MessageFormat.format(
                    "Emulating command ({1}): {0}",
                    command.getCommandLineString(),
                    emulator.getName()));
            try {
                emulator.execute(context, configurations, command);
            } catch (InterruptedException e) {
                throw (AssertionError) new AssertionError(MessageFormat.format(
                        "コマンドの実行中に割り込みが発生しました (flowId={0}, command=\"{1}\")",
                        context.getCurrentFlowId(),
                        command.getCommandLineString())).initCause(e);
            }
        } else {
            delegate.execute(command, environmentVariables);
        }
    }

    private synchronized CommandEmulator findCommandEmulator(TestExecutionPlan.Command command) {
        if (commandEmulators == null) {
            this.commandEmulators = new ArrayList<CommandEmulator>();
            for (CommandEmulator executor : ServiceLoader.load(CommandEmulator.class, context.getClassLoader())) {
                this.commandEmulators.add(executor);
            }
        }
        for (CommandEmulator executor : commandEmulators) {
            if (executor.accepts(context, configurations, command)) {
                return executor;
            }
        }
        return null;
    }
}
