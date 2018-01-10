/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.windgate.inprocess;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;
import com.asakusafw.testdriver.windgate.PluginClassLoader;
import com.asakusafw.testdriver.windgate.WindGateTestHelper;
import com.asakusafw.vocabulary.windgate.Constants;
import com.asakusafw.windgate.cli.CommandLineUtil;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.TaskExecutors.IoConsumer;
import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;
import com.google.common.base.Objects;

/**
 * Utilities about WindGate in-process execution for testing.
 * @since 0.10.0
 */
public final class WindGateTaskExecutors {

    private static final Logger LOG = LoggerFactory.getLogger(WindGateTaskExecutors.class);

    static final String MODULE_NAME_PREFIX = Constants.MODULE_NAME + '.';

    static final String PATH_WINDGATE = "windgate"; //$NON-NLS-1$

    private static final String PATH_CONF = PATH_WINDGATE + "/conf"; //$NON-NLS-1$

    private static final String PATH_PLUGIN = WindGateTestHelper.PRODUCTION_PLUGIN_DIRECTORY;

    private static final String PATTERN_PROFILE = WindGateTestHelper.PRODUCTION_PROFILE_PATH;

    static final int ARG_PROFILE = 0;

    private WindGateTaskExecutors() {
        return;
    }

    static boolean isSupported(TaskExecutionContext context, TaskInfo task, String command, int minimumArgs) {
        if ((task instanceof CommandTaskInfo) == false) {
            return false;
        }
        CommandTaskInfo mirror = (CommandTaskInfo) task;
        if (mirror.getModuleName().equals(Constants.MODULE_NAME) == false
                && mirror.getModuleName().startsWith(MODULE_NAME_PREFIX) == false) {
            return false;
        }
        if (mirror.getArguments().size() < minimumArgs) {
            return false;
        }
        String profile = getProfileName(context, mirror);
        if (findProfile(context, profile).filter(Files::isRegularFile).isPresent() == false) {
            return false;
        }
        if (Objects.equal(Paths.get(mirror.getCommand()), Paths.get(command)) == false) {
            return false;
        }
        return true;
    }

    static void withLibraries(
            TaskExecutionContext context,
            IoConsumer<? super ClassLoader> action) throws IOException, InterruptedException {
        try (PluginClassLoader classLoader = createClassLoader(context)) {
            ClassLoader contextClassLoader = ApplicationLauncher.switchContextClassLoader(classLoader);
            try {
                action.accept(classLoader);
            } finally {
                ApplicationLauncher.switchContextClassLoader(contextClassLoader);
                WindGateTestHelper.disposePluginClassLoader(classLoader);
            }
        }
    }

    static PluginClassLoader createClassLoader(TaskExecutionContext context) {
        ClassLoader parent = context.getClassLoader();

        List<Path> libraries = new ArrayList<>();

        TaskExecutors.findJobflowLibrary(context)
            .filter(Files::isRegularFile)
            .ifPresent(libraries::add);

        TaskExecutors.findAttachedLibraries(context).stream()
            .filter(Files::isRegularFile)
            .forEach(libraries::add);

        TaskExecutors.findFrameworkFile(context, PATH_CONF)
            .filter(Files::isDirectory)
            .ifPresent(libraries::add);

        TaskExecutors.findFrameworkLibraries(context, PATH_PLUGIN).stream()
            .filter(Files::isRegularFile)
            .forEach(libraries::add);

        LOG.debug("WindGate in-process additional libraries: {}", libraries);
        return PluginClassLoader.newInstance(parent, toUrlList(libraries));
    }

    private static List<URL> toUrlList(Collection<? extends Path> libraries) {
        return libraries.stream()
                .map(Path::toUri)
                .flatMap(uri -> {
                    try {
                        return Stream.of(uri.toURL());
                    } catch (MalformedURLException e) {
                        LOG.warn("failed to convert URI: {}", uri, e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    static GateProfile loadProfile(TaskExecutionContext context, ClassLoader classLoader, CommandTaskInfo task) {
        String profile = getProfileName(context, task);
        LOG.debug("loading WindGate profile: {}", profile); //$NON-NLS-1$
        try {
            Path path = findProfile(context, profile)
                    .filter(Files::isRegularFile)
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                            "WindGate profile \"{0}\" is not found",
                            profile)));
            Map<String, String> variables = new HashMap<>(context.getEnvironmentVariables());
            variables.put("WINDGATE_PROFILE", profile); //$NON-NLS-1$
            ParameterList contextParameters = new ParameterList(variables);
            ProfileContext profileContext = new ProfileContext(classLoader, contextParameters)
                    .withResource(Configuration.class, context.findResource(Configuration.class).get());
            URI uri = path.toUri();
            Properties properties = CommandLineUtil.loadProperties(uri, classLoader);
            return GateProfile.loadFrom(CommandLineUtil.toName(uri), properties, profileContext);
        } catch (IOException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "failed to load WindGate profile \"{0}\"",
                    profile), e);
        }
    }

    private static String getProfileName(TaskExecutionContext context, CommandTaskInfo task) {
        return TaskExecutors.resolveCommandToken(context, task.getArguments().get(ARG_PROFILE));
    }

    private static Optional<Path> findProfile(ExecutionContext context, String profile) {
        return TaskExecutors.findFrameworkFile(context, MessageFormat.format(PATTERN_PROFILE, profile));
    }
}
