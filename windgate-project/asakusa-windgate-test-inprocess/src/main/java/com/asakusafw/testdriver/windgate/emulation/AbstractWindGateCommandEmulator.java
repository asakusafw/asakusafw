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
package com.asakusafw.testdriver.windgate.emulation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.inprocess.CommandEmulator;
import com.asakusafw.testdriver.inprocess.EmulatorUtils;
import com.asakusafw.testdriver.windgate.PluginClassLoader;
import com.asakusafw.testdriver.windgate.WindGateTestHelper;
import com.asakusafw.windgate.bootstrap.CommandLineUtil;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProfileContext;

/**
 * An abstract implementation of WindGate command emulators.
 * @since 0.6.0
 */
public abstract class AbstractWindGateCommandEmulator extends CommandEmulator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractWindGateCommandEmulator.class);

    static final String MODULE_NAME_PREFIX = "windgate.";

    static final String PATH_WINDGATE = "windgate";

    private static final String PATH_CONF = PATH_WINDGATE + "/conf";

    private static final String PATH_PLUGIN = WindGateTestHelper.PRODUCTION_PLUGIN_DIRECTORY;

    private static final String PATTERN_PROFILE = WindGateTestHelper.PRODUCTION_PROFILE_PATH;

    private static final int ARG_PROFILE = 1;

    @Override
    public final void execute(
            TestDriverContext context,
            ConfigurationFactory configurations,
            TestExecutionPlan.Command command) throws IOException, InterruptedException {
        configureLogs(context);
        PluginClassLoader classLoader = createClassLoader(context, configurations);
        ClassLoader contextClassLoader = ApplicationLauncher.switchContextClassLoader(classLoader);
        try {
            GateProfile profile = loadProfile(context, classLoader, command.getCommandTokens().get(ARG_PROFILE));
            execute0(context, classLoader, profile, command);
        } finally {
            ApplicationLauncher.switchContextClassLoader(contextClassLoader);
            WindGateTestHelper.disposePluginClassLoader(classLoader);
        }
    }

    /**
     * Executes WindGate tasks internally.
     * @param context the current test context
     * @param classLoader the class loader for this execution
     * @param profile the target WindGate profile
     * @param command the command information
     * @throws IOException if failed to execute the command
     * @throws InterruptedException if interrupted while processing the command
     */
    protected abstract void execute0(
            TestDriverContext context,
            ClassLoader classLoader,
            GateProfile profile,
            TestExecutionPlan.Command command) throws IOException, InterruptedException;

    private static void configureLogs(TestDriverContext context) {
        MDC.put("batchId", context.getCurrentBatchId());
        MDC.put("flowId", context.getCurrentBatchId());
        MDC.put("executionId", context.getCurrentBatchId());
    }

    private static PluginClassLoader createClassLoader(
            TestDriverContext context,
            ConfigurationFactory configurations) throws IOException {
        ClassLoader parent = configurations.newInstance().getClassLoader();
        List<URL> libraries = new ArrayList<URL>();
        libraries.add(new File(context.getFrameworkHomePath(), PATH_CONF).toURI().toURL());
        libraries.add(EmulatorUtils.getJobflowLibraryPath(context).toURI().toURL());
        for (File file : EmulatorUtils.getBatchLibraryPaths(context)) {
            libraries.add(file.toURI().toURL());
        }
        File plugins = new File(context.getFrameworkHomePath(), PATH_PLUGIN);
        if (plugins.isDirectory()) {
            for (File file : plugins.listFiles()) {
                if (file.isFile()) {
                    libraries.add(file.toURI().toURL());
                }
            }
        }
        return PluginClassLoader.newInstance(parent, libraries);
    }

    private static GateProfile loadProfile(
            TestDriverContext context,
            ClassLoader classLoader,
            String profile) {
        LOG.debug("Loading profile: {}", profile);
        try {
            File profilePath = new File(
                    context.getFrameworkHomePath(),
                    MessageFormat.format(PATTERN_PROFILE, profile)).getAbsoluteFile();
            Map<String, String> variables = new HashMap<String, String>(context.getEnvironmentVariables());
            variables.put("WINDGATE_PROFILE", profile);
            ParameterList contextParameters = new ParameterList(variables);
            ProfileContext profileContext = new ProfileContext(classLoader, contextParameters);

            URI uri = profilePath.toURI();
            Properties properties = CommandLineUtil.loadProperties(uri, classLoader);
            return GateProfile.loadFrom(CommandLineUtil.toName(uri), properties, profileContext);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid profile \"{0}\".",
                    profile), e);
        }
    }
}
