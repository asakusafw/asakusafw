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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.utils.collections.Lists;

/**
 * A default implementation of {@link JobExecutor}.
 * @since 0.6.0
 */
public class DefaultJobExecutor extends JobExecutor {

    static final Logger LOG = LoggerFactory.getLogger(DefaultJobExecutor.class);

    /**
     * Path to the script to submit a stage job (relative path from {@link TestDriverContext#getFrameworkHomePath()}).
     */
    public static final String SUBMIT_JOB_SCRIPT = "testing/libexec/hadoop-execute.sh";

    private final TestDriverContext context;

    /**
     * Creates a new instance.
     * @param context the current test context
     */
    public DefaultJobExecutor(TestDriverContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.context = context;
    }

    /**
     * Validates current test execution environment.
     * @throws AssertionError if current test environment is invalid
     */
    @Override
    public void validateEnvironment() {
        if (requiresValidateExecutionEnvironment() == false) {
            LOG.debug("skipping test execution environment validation");
            return;
        }
        if (context.getFrameworkHomePathOrNull() == null) {
            raiseInvalid(MessageFormat.format(
                    "環境変数\"{0}\"が未設定です",
                    TestDriverContext.ENV_FRAMEWORK_PATH));
        }
        if (ConfigurationProvider.findHadoopCommand() == null) {
            raiseInvalid(MessageFormat.format(
                    "コマンド\"{0}\"を検出できませんでした",
                    "hadoop"));
        }
        String runtime = context.getRuntimeEnvironmentVersion();
        if (runtime == null) {
            LOG.debug("Runtime environment version is missing");
        } else {
            String develop = context.getDevelopmentEnvironmentVersion();
            if (develop.equals(runtime) == false) {
                raiseInvalid(MessageFormat.format(
                        "開発環境とテスト実行環境でフレームワークのバージョンが一致しません（開発環境：{0}, 実行環境：{1}）",
                        develop,
                        runtime));
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

    private void raiseInvalid(String message) {
        if (SystemUtils.IS_OS_WINDOWS) {
            LOG.warn(message);
            LOG.info(MessageFormat.format(
                    "この環境では現在のテストを実行できないため、スキップします: {0}",
                    context.getCallerClass().getName()));
            Assume.assumeTrue(false);
        } else {
            throw new AssertionError(message);
        }
    }

    @Override
    public void execute(TestExecutionPlan.Job job, Map<String, String> environmentVariables) throws IOException {
        assert job != null;
        assert environmentVariables != null;
        List<String> commandLine = Lists.create();
        commandLine.add(new File(context.getFrameworkHomePath(), SUBMIT_JOB_SCRIPT).getAbsolutePath());
        commandLine.add(findPackage());
        commandLine.add(job.getClassName());
        commandLine.add(context.getCurrentBatchId());

        for (Map.Entry<String, String> entry : job.getProperties().entrySet()) {
            commandLine.add("-D");
            commandLine.add(entry.getKey() + "=" + entry.getValue());
        }
        int exitValue = runCommand(commandLine, environmentVariables);
        if (exitValue != 0) {
            throw new AssertionError(MessageFormat.format(
                    "Hadoopジョブの実行に失敗しました (exitCode={0}, flowId={1}, command=\"{2}\")", exitValue,
                    context.getCurrentFlowId(),
                    toCommandLineString(commandLine)));
        }
    }

    private String findPackage() throws IOException {
        File packagePath = context.getJobflowPackageLocation(context.getCurrentBatchId());
        File packageFile = new File(packagePath, Naming.getJobflowClassPackageName(context.getCurrentFlowId()));
        if (packageFile.isFile()) {
            return packageFile.getAbsolutePath();
        }
        throw new FileNotFoundException(packageFile.getAbsolutePath());
    }

    @Override
    public void execute(TestExecutionPlan.Command command, Map<String, String> environmentVariables) throws IOException {
        assert command != null;
        assert environmentVariables != null;
        int exitCode = runCommand(command.getCommandTokens(), environmentVariables);
        if (exitCode != 0) {
            throw new AssertionError(MessageFormat.format(
                    "コマンドの実行に失敗しました (exitCode={0}, flowId={1}, command=\"{2}\")",
                    exitCode,
                    context.getCurrentFlowId(),
                    toCommandLineString(command.getCommandTokens())));
        }
    }

    /**
     * Runs the specified command.
     * @param commandLine command line tokens
     * @param environmentVariables variables
     * @return the exit code
     * @throws IOException if failed to create/destroy a process
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static int runCommand(
            List<String> commandLine,
            Map<String, String> environmentVariables) throws IOException {
        if (commandLine == null) {
            throw new IllegalArgumentException("commandLine must not be null"); //$NON-NLS-1$
        }
        if (environmentVariables == null) {
            throw new IllegalArgumentException("environmentVariables must not be null"); //$NON-NLS-1$
        }
        LOG.info("[COMMAND] {}", toCommandLineString(commandLine));

        ProcessBuilder builder = new ProcessBuilder(commandLine);
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
                    toCommandLineString(commandLine)), e);
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

    private static String toCommandLineString(List<String> commandLine) {
        assert commandLine != null;
        StringBuilder sb = new StringBuilder();
        for (String cmd : commandLine) {
            sb.append(cmd).append(" ");
        }
        return sb.toString().trim();
    }

    private static class InputStreamThread extends Thread {

        private final BufferedReader br;

        private final List<String> list = Lists.create();

        public InputStreamThread(InputStream is) {
            br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
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
}
