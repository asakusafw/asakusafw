/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.compiler.CompilerConstants;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * A default implementation of {@link JobExecutor}.
 * @since 0.6.0
 * @version 0.6.1
 */
public class DefaultJobExecutor extends JobExecutor {

    static final Logger LOG = LoggerFactory.getLogger(DefaultJobExecutor.class);

    /**
     * Path to the script to submit a stage job (relative path from {@link TestDriverContext#getFrameworkHomePath()}).
     */
    public static final String SUBMIT_JOB_SCRIPT = "testing/libexec/hadoop-execute.sh"; //$NON-NLS-1$

    private final TestDriverContext context;

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance.
     * @param context the current test context
     */
    public DefaultJobExecutor(TestDriverContext context) {
        this(context, ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param context the current test context
     * @param configurations the configurations factory
     * @since 0.6.1
     */
    public DefaultJobExecutor(TestDriverContext context, ConfigurationFactory configurations) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (configurations == null) {
            throw new IllegalArgumentException("configurations must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.configurations = configurations;
    }

    /**
     * Returns the current {@link ConfigurationFactory}.
     * @return the configuration factory
     */
    protected ConfigurationFactory getConfigurations() {
        return configurations;
    }

    @Override
    public void validateEnvironment() {
        if (requiresValidateExecutionEnvironment() == false) {
            LOG.debug("skipping test execution environment validation"); //$NON-NLS-1$
            return;
        }
        if (context.getFrameworkHomePathOrNull() == null) {
            raiseInvalid(MessageFormat.format(
                    Messages.getString("DefaultJobExecutor.errorUndefinedEnvironmentVariable"), //$NON-NLS-1$
                    TestDriverContext.ENV_FRAMEWORK_PATH));
        }
        if (configurations.getHadoopCommand() == null) {
            raiseInvalid(MessageFormat.format(
                    Messages.getString("DefaultJobExecutor.errorMissingCommand"), //$NON-NLS-1$
                    "hadoop")); //$NON-NLS-1$
        }
        String runtime = context.getRuntimeEnvironmentVersion();
        if (runtime == null) {
            LOG.debug("Runtime environment version is missing"); //$NON-NLS-1$
        } else {
            String develop = context.getDevelopmentEnvironmentVersion();
            if (develop.equals(runtime) == false) {
                raiseInvalid(MessageFormat.format(
                        Messages.getString("DefaultJobExecutor.errorIncompatibleSdkVersion"), //$NON-NLS-1$
                        develop,
                        runtime));
            }
        }
    }

    private boolean requiresValidateExecutionEnvironment() {
        String value = System.getProperty(TestDriverContext.KEY_FORCE_EXEC);
        if (value != null) {
            if (value.isEmpty() || value.equalsIgnoreCase("true")) { //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    private void raiseInvalid(String message) {
        if (SystemUtils.IS_OS_WINDOWS) {
            LOG.warn(message);
            LOG.info(MessageFormat.format(
                    Messages.getString("DefaultJobExecutor.infoSkipExecution"), //$NON-NLS-1$
                    context.getCallerClass().getName()));
            Assume.assumeTrue(false);
        } else {
            throw new AssertionError(message);
        }
    }

    @Override
    public void execute(
            TestExecutionPlan.Job job,
            Map<String, String> environmentVariables) throws IOException {
        assert job != null;
        assert environmentVariables != null;
        List<String> commandLine = new ArrayList<>();
        commandLine.add(new File(context.getFrameworkHomePath(), SUBMIT_JOB_SCRIPT).getAbsolutePath());
        commandLine.add(findPackage());
        commandLine.add(job.getClassName());
        commandLine.add(context.getCurrentBatchId());

        for (Map.Entry<String, String> entry : job.getProperties().entrySet()) {
            commandLine.add("-D"); //$NON-NLS-1$
            commandLine.add(entry.getKey() + "=" + entry.getValue()); //$NON-NLS-1$
        }
        int exitValue = runCommand(commandLine, environmentVariables);
        if (exitValue != 0) {
            throw new AssertionError(MessageFormat.format(
                    Messages.getString("DefaultJobExecutor.errorNonZeroHadoopExitCode"), exitValue, //$NON-NLS-1$
                    context.getCurrentFlowId(),
                    toCommandLineString(commandLine)));
        }
    }

    private String findPackage() throws IOException {
        File packagePath = context.getJobflowPackageLocation(context.getCurrentBatchId());
        File packageFile = new File(packagePath, CompilerConstants.getJobflowLibraryName(context.getCurrentFlowId()));
        if (packageFile.isFile()) {
            return packageFile.getAbsolutePath();
        }
        throw new FileNotFoundException(packageFile.getAbsolutePath());
    }

    @Override
    public void execute(
            TestExecutionPlan.Command command,
            Map<String, String> environmentVariables) throws IOException {
        assert command != null;
        assert environmentVariables != null;
        int exitCode = runCommand(command.getCommandTokens(), environmentVariables);
        if (exitCode != 0) {
            throw new AssertionError(MessageFormat.format(
                    Messages.getString("DefaultJobExecutor.errorNonZeroCommandExitCode"), //$NON-NLS-1$
                    exitCode,
                    context.getCurrentFlowId(),
                    toCommandLineString(command.getCommandTokens())));
        }
    }

    private int runCommand(
            List<String> commandLine,
            Map<String, String> environmentVariables) throws IOException {
        LOG.info(MessageFormat.format(
                Messages.getString("DefaultJobExecutor.infoEchoCommandLine"), //$NON-NLS-1$
                toCommandLineString(commandLine)));

        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.redirectErrorStream(true);
        builder.environment().putAll(environmentVariables);
        File hadoopCommand = configurations.getHadoopCommand();
        if (hadoopCommand != null) {
            builder.environment().put("HADOOP_CMD", hadoopCommand.getAbsolutePath()); //$NON-NLS-1$
        }
        builder.directory(new File(System.getProperty("user.home", "."))); //$NON-NLS-1$ //$NON-NLS-2$

        int exitCode;
        Process process = builder.start();
        try (InputStream is = process.getInputStream()) {
            InputStreamThread it = new InputStreamThread(is);
            it.start();
            exitCode = process.waitFor();
            it.join();
        } catch (InterruptedException e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("DefaultJobExecutor.errorExecutionInterrupted"), //$NON-NLS-1$
                    toCommandLineString(commandLine)), e);
        } finally {
            process.getOutputStream().close();
            process.getErrorStream().close();
            process.destroy();
        }
        return exitCode;
    }

    private static String toCommandLineString(List<String> commandLine) {
        assert commandLine != null;
        StringBuilder sb = new StringBuilder();
        for (String cmd : commandLine) {
            sb.append(cmd).append(" "); //$NON-NLS-1$
        }
        return sb.toString().trim();
    }

    private static final class InputStreamThread extends Thread {

        private final BufferedReader reader;

        private final List<String> list = new ArrayList<>();

        InputStreamThread(InputStream is) {
            reader = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    list.add(line);
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
