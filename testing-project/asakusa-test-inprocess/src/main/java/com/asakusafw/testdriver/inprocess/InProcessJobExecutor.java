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
package com.asakusafw.testdriver.inprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;
import com.asakusafw.testdriver.DefaultJobExecutor;
import com.asakusafw.testdriver.JobExecutor;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.TestExecutionPlan.Job;
import com.asakusafw.testdriver.TestExecutionPlan.TaskKind;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * A default implementation of {@link JobExecutor}.
 * @since 0.6.0
 */
public class InProcessJobExecutor extends JobExecutor {

    static final Logger LOG = LoggerFactory.getLogger(InProcessJobExecutor.class);

    private static final Settings GLOBAL_SETTINGS = new Settings();

    static final String PATH_ASAKUSA_RESOURCES = "core/conf/asakusa-resources.xml"; //$NON-NLS-1$

    private final TestDriverContext context;

    private final DefaultJobExecutor delegate;

    private final ConfigurationFactory configurations;

    private List<CommandEmulator> commandEmulators;

    /**
     * Creates a new instance.
     * @param context the current test context
     */
    public InProcessJobExecutor(TestDriverContext context) {
        this(context, ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param context the current test context
     * @param configurations the configurations factory
     */
    public InProcessJobExecutor(TestDriverContext context, ConfigurationFactory configurations) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (configurations == null) {
            throw new IllegalArgumentException("configurations must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.delegate = new DefaultJobExecutor(context, configurations);
        this.configurations = configurations;
        this.commandEmulators = null;
    }

    /**
     * Returns the global settings for this executor.
     * Please use the returned object with the synchronized block, like as:
<pre><code>
Settings s = InProcessJobExecutor.getGlobalSettings();
synchronized(s) {
    ...
}
</code></pre>
     * @return the global settings
     */
    public static Settings getGlobalSettings() {
        return GLOBAL_SETTINGS;
    }

    @Override
    public void validateEnvironment() {
        if (requiresValidateExecutionEnvironment() == false) {
            LOG.debug("skipping test execution environment validation"); //$NON-NLS-1$
            return;
        }
        if (context.getFrameworkHomePathOrNull() == null) {
            throw new AssertionError(MessageFormat.format(
                    Messages.getString("InProcessJobExecutor.errorUndefinedEnvironmentVariable"), //$NON-NLS-1$
                    TestDriverContext.ENV_FRAMEWORK_PATH));
        }
        String runtime = context.getRuntimeEnvironmentVersion();
        if (runtime == null) {
            LOG.debug("Runtime environment version is missing"); //$NON-NLS-1$
        } else {
            String develop = context.getDevelopmentEnvironmentVersion();
            if (develop.equals(runtime) == false) {
                throw new AssertionError(MessageFormat.format(
                        Messages.getString("InProcessJobExecutor.errorInconsistentSdkVersion"), //$NON-NLS-1$
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
        List<TestExecutionPlan.Task> tasks = new ArrayList<>();
        tasks.addAll(plan.getInitializers());
        tasks.addAll(plan.getImporters());
        tasks.addAll(plan.getJobs());
        tasks.addAll(plan.getExporters());
        tasks.addAll(plan.getFinalizers());

        for (TestExecutionPlan.Task task : tasks) {
            if (task.getTaskKind() != TaskKind.COMMAND) {
                continue;
            }
            if (findCommandEmulator((TestExecutionPlan.Command) task) == null) {
                if (configurations.getHadoopCommand() == null) {
                    throw new AssertionError(MessageFormat.format(
                            Messages.getString("InProcessJobExecutor.errorMissingCommandPath"), //$NON-NLS-1$
                            "hadoop")); //$NON-NLS-1$
                }
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

    @Override
    public void execute(
            TestExecutionPlan.Job job,
            Map<String, String> environmentVariables) throws IOException {
        assert job != null;
        LOG.info(MessageFormat.format(
                Messages.getString("InProcessJobExecutor.infoStartHadoop"), //$NON-NLS-1$
                job.getClassName()));
        List<String> arguments = new ArrayList<>();
        arguments.add(job.getClassName());
        arguments.addAll(computeHadoopJobArguments(job));
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Configuration conf = configurations.newInstance();
            synchronized (GLOBAL_SETTINGS) {
                for (Map.Entry<String, String> entry : GLOBAL_SETTINGS.getProperties().entrySet()) {
                    conf.set(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String, String> entry : job.getProperties().entrySet()) {
                conf.set(entry.getKey(), entry.getValue());
            }
            try {
                int exitValue = ApplicationLauncher.exec(conf, arguments.toArray(new String[arguments.size()]));
                if (exitValue != 0) {
                    throw new AssertionError(MessageFormat.format(
                            Messages.getString("InProcessJobExecutor.errorNonZeroHadoopExitCode"), //$NON-NLS-1$
                            exitValue,
                            context.getCurrentFlowId()));
                }
            } catch (Exception e) {
                throw (AssertionError) new AssertionError(MessageFormat.format(
                        Messages.getString("InProcessJobExecutor.errorUnknownHadoopException"), //$NON-NLS-1$
                        context.getCurrentFlowId())).initCause(e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private List<String> computeHadoopJobArguments(Job job) throws IOException {
        assert job != null;
        List<String> arguments = new ArrayList<>();
        computeHadoopLibjars(arguments);
        computeAsakusaResources(arguments);
        return arguments;
    }

    private void computeHadoopLibjars(List<String> arguments) throws IOException {
        assert arguments != null;
        arguments.add("-libjars"); //$NON-NLS-1$
        StringBuilder libjars = new StringBuilder();
        File packageFile = EmulatorUtils.getJobflowLibraryPath(context);
        if (packageFile.isFile() == false) {
            throw new FileNotFoundException(packageFile.getAbsolutePath());
        }
        libjars.append(packageFile.toURI());

        // Note: already in classpath?
        for (File file : EmulatorUtils.getBatchLibraryPaths(context)) {
            libjars.append(',');
            libjars.append(file.toURI());
        }
        arguments.add(libjars.toString());
    }

    private void computeAsakusaResources(List<String> arguments) {
        assert arguments != null;
        File asakusaResources = getAsakusaResoucesPath();
        if (asakusaResources.exists()) {
            arguments.add("-conf"); //$NON-NLS-1$
            arguments.add(asakusaResources.toURI().toString());
        }
    }

    File getAsakusaResoucesPath() {
        return new File(context.getFrameworkHomePath(), PATH_ASAKUSA_RESOURCES);
    }

    @Override
    public void execute(
            TestExecutionPlan.Command command,
            Map<String, String> environmentVariables) throws IOException {
        CommandEmulator emulator = findCommandEmulator(command);
        if (emulator != null) {
            LOG.info(MessageFormat.format(
                    Messages.getString("InProcessJobExecutor.infoStartCommandJob"), //$NON-NLS-1$
                    String.join(" ", command.getCommandTokens()), //$NON-NLS-1$
                    emulator.getName()));
            try {
                emulator.execute(context, configurations, command);
            } catch (InterruptedException e) {
                throw (AssertionError) new AssertionError(MessageFormat.format(
                        Messages.getString("InProcessJobExecutor.errorExecutionInterrupted"), //$NON-NLS-1$
                        context.getCurrentFlowId(),
                        String.join(" ", command.getCommandTokens()))).initCause(e); //$NON-NLS-1$
            }
        } else {
            delegate.execute(command, environmentVariables);
        }
    }

    private synchronized CommandEmulator findCommandEmulator(TestExecutionPlan.Command command) {
        if (commandEmulators == null) {
            this.commandEmulators = new ArrayList<>();
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

    /**
     * Represents settings for {@link InProcessJobExecutor}.
     * @since 0.7.1
     */
    public static final class Settings {

        private final Map<String, String> properties = new HashMap<>();

        /**
         * Returns the view of additional Hadoop properties.
         * Clients can modify the returned object.
         * @return the Hadoop properties
         */
        public Map<String, String> getProperties() {
            return properties;
        }

        /**
         * Resets this settings to default values.
         */
        public void reset() {
            properties.clear();
        }
    }
}
