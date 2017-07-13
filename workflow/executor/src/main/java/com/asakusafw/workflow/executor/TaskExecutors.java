/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.workflow.executor;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.model.CommandToken;

/**
 * Utilities for {@link TaskExecutor}.
 * @since 0.10.0
 */
public final class TaskExecutors {

    static final Logger LOG = LoggerFactory.getLogger(TaskExecutors.class);

    static final Pattern PATTERN_VARIABLE = Pattern.compile("\\$\\{(.*?)\\}"); //$NON-NLS-1$

    /**
     * The environment variable of framework installation path.
     */
    public static final String ENV_FRAMEWORK_PATH = "ASAKUSA_HOME"; //$NON-NLS-1$

    /**
     * The environment variable of application installation path.
     */
    public static final String ENV_BATCHAPPS_PATH = "ASAKUSA_BATCHAPPS_HOME"; //$NON-NLS-1$

    static final String DEFAULT_BATCHAPPS_PATH = "batchapps"; //$NON-NLS-1$

    static final String VAR_USER = "user"; //$NON-NLS-1$

    static final String VAR_EXECUTION_ID = "execution_id"; //$NON-NLS-1$

    static final String VAR_BATCH_ID = "batch_id"; //$NON-NLS-1$

    static final String VAR_FLOW_ID = "flow_id"; //$NON-NLS-1$

    static final String EXTENSION_LIBRARY = ".jar"; //$NON-NLS-1$

    static final String PATTERN_JOBFLOW_LIBRARY = "lib/jobflow-%s.jar"; //$NON-NLS-1$

    static final String LOCATION_CORE_LIBRARIES = "core/lib"; //$NON-NLS-1$

    static final String LOCATION_CORE_CONFIGURATION = "core/conf/asakusa-resources.xml"; //$NON-NLS-1$

    static final String LOCATION_HADOOP_EMBEDDED_LIBRARIES = "hadoop/lib"; //$NON-NLS-1$

    static final String LOCATION_EXTENSION_LIBRARIES = "ext/lib"; //$NON-NLS-1$

    /**
     * The workflow information file path (relative from each application directory).
     */
    public static final String LOCATION_APPLICATION_WORKFLOW_DEFINITION = "etc/workflow.json";

    static final String LOCATION_APPLICATION_ATTACHED_LIBRARIES = "usr/lib"; //$NON-NLS-1$

    static final Method REFLECTION_UTILS_RELEASE =
            findMethod("org.apache.hadoop.util.ReflectionUtils", "clearCache");

    static final Method JCL_RELEASE =
            findMethod("org.apache.commons.logging.LogFactory", "release", ClassLoader.class);

    private TaskExecutors() {
        return;
    }

    /**
     * Returns the default task executor objects via SPI.
     * @param serviceLoader the service loader
     * @return the default task executors
     */
    public static Collection<TaskExecutor> loadDefaults(ClassLoader serviceLoader) {
        List<TaskExecutor> executors = new ArrayList<>();
        for (TaskExecutor executor : ServiceLoader.load(TaskExecutor.class, serviceLoader)) {
            executors.add(executor);
        }
        return executors;
    }

    /**
     * Returns the framework installation path.
     * @param environmentVariables the environment variables
     * @return the framework installation path
     */
    public static Optional<Path> findFrameworkHome(Map<String, String> environmentVariables) {
        return Optional.ofNullable(environmentVariables.get(ENV_FRAMEWORK_PATH))
                .map(Paths::get);
    }

    /**
     * Returns the batch application installation base path.
     * @param environmentVariables the environment variables
     * @return the batch application installation base path
     */
    public static Optional<Path> findApplicationHome(Map<String, String> environmentVariables) {
        Optional<Path> batchapps = Optional.ofNullable(environmentVariables.get(ENV_BATCHAPPS_PATH))
                .map(Paths::get);
        if (batchapps.isPresent()) {
            return batchapps;
        }
        return findFrameworkHome(environmentVariables)
                .map(it -> it.resolve(DEFAULT_BATCHAPPS_PATH));
    }

    /**
     * Returns the current user name.
     * @param context the current context
     * @return the current user name
     */
    public static String getUserName(ExecutionContext context) {
        return Optional.ofNullable(System.getProperty("user.name"))
                .orElseThrow(() -> new IllegalStateException("\"user.name\" is not available"));
    }

    /**
     * Returns the current user home directory.
     * @param context the current context
     * @return the current user home directory
     */
    public static Path getUserHome(ExecutionContext context) {
        return Optional.ofNullable(System.getProperty("user.home"))
                .map(Paths::get)
                .orElseThrow(() -> new IllegalStateException("\"user.name\" is not available"));
    }

    /**
     * Returns the framework installation path.
     * @param context the current task execution context
     * @return the framework file path
     */
    public static Optional<Path> findFrameworkHome(ExecutionContext context) {
        return findFrameworkHome(context.getEnvironmentVariables());
    }

    /**
     * Returns a framework file.
     * @param context the current task execution context
     * @param location the relative location from the framework installation root
     * @return the framework file path
     */
    public static Optional<Path> findFrameworkFile(ExecutionContext context, String location) {
        return TaskExecutors.findFrameworkHome(context)
                .map(it -> it.resolve(location));
    }

    /**
     * Returns the batch application installation base path.
     * @param context the current context
     * @return the batch application installation base path
     */
    public static Optional<Path> findApplicationHome(ExecutionContext context) {
        return findApplicationHome(context.getEnvironmentVariables());
    }

    /**
     * Returns a file in the current batch application package.
     * @param context the current task execution context
     * @param location the relative location from the batch application package root
     * @return the application file path
     */
    public static Optional<Path> findApplicationFile(TaskExecutionContext context, String location) {
        return findApplicationHome(context)
                .map(it -> it.resolve(context.getBatchId()))
                .map(it -> it.resolve(location));
    }

    /**
     * Returns an application workflow definition file.
     * @param context the current task execution context
     * @return the definition file
     */
    public static Optional<Path> findApplicationWorkflowDefinitionFile(TaskExecutionContext context) {
        return findApplicationFile(context, LOCATION_APPLICATION_WORKFLOW_DEFINITION);
    }

    /**
     * Resolves a path string using the current context.
     * @param context the current task execution context
     * @param path the target path expression
     * @return the resolved path
     */
    public static String resolvePath(TaskExecutionContext context, String path) {
        Matcher matcher = PATTERN_VARIABLE.matcher(path);
        int start = 0;
        StringBuilder buf = new StringBuilder();
        while (matcher.find(start)) {
            buf.append(path, start, matcher.start());
            switch (matcher.group(1)) {
            case VAR_USER:
                buf.append(getUserName(context));
                break;
            case VAR_BATCH_ID:
                buf.append(context.getBatchId());
                break;
            case VAR_FLOW_ID:
                buf.append(context.getFlowId());
                break;
            case VAR_EXECUTION_ID:
                buf.append(context.getExecutionId());
                break;
            default:
                throw new IllegalArgumentException(MessageFormat.format(
                        "unknown variable \"{1}\": {0}",
                        path,
                        matcher.group()));
            }
            start = matcher.end();
        }
        buf.append(path, start, path.length());
        return buf.toString();
    }

    /**
     * Resolves a list of {@link CommandToken} for the current execution context.
     * @param context the current task execution context
     * @param tokens the target command tokens
     * @return the resolved tokens
     */
    public static List<String> resolveCommandTokens(TaskExecutionContext context, List<? extends CommandToken> tokens) {
        List<String> results = new ArrayList<>();
        for (CommandToken token : tokens) {
            String resolved = resolveCommandToken(context, token);
            results.add(resolved);
        }
        return results;
    }

    /**
     * Resolves a {@link CommandToken} for the current execution context.
     * @param context the current task execution context
     * @param token the target command token
     * @return the resolved token
     */
    public static String resolveCommandToken(TaskExecutionContext context, CommandToken token) {
        switch (token.getTokenKind()) {
        case TEXT:
            return token.getImage();
        case BATCH_ID:
            return context.getBatchId();
        case FLOW_ID:
            return context.getFlowId();
        case EXECUTION_ID:
            return context.getExecutionId();
        case BATCH_ARGUMENTS:
            return encodeBatchArguments(context.getBatchArguments());
        default:
            throw new AssertionError(token);
        }
    }

    private static String encodeBatchArguments(Map<String, String> arguments) {
        StringBuilder buf = new StringBuilder();
        arguments.forEach((k, v) -> {
            if (buf.length() != 0) {
                buf.append(',');
            }
            escape(buf, k);
            buf.append('=');
            escape(buf, v);
        });
        return buf.toString();
    }

    private static void escape(StringBuilder buf, String value) {
        for (int i = 0, n = value.length(); i < n; i++) {
            char c = value.charAt(i);
            if (c == '=' || c == ',' || c == '\\') {
                buf.append('\\');
            }
            buf.append(c);
        }
    }

    /**
     * Returns the jobflow library file.
     * @param context the current task execution context
     * @return the library file
     */
    public static Optional<Path> findJobflowLibrary(TaskExecutionContext context) {
        String location = String.format(PATTERN_JOBFLOW_LIBRARY, context.getFlowId());
        return findApplicationFile(context, location);
    }

    /**
     * Returns the core configuration file.
     * @param context the current task execution context
     * @return the configuration file (a.k.a. {@code "asakusa-runtime.xml"})
     */
    public static Optional<Path> findCoreConfigurationFile(ExecutionContext context) {
        return findFrameworkFile(context, LOCATION_CORE_CONFIGURATION);
    }

    /**
     * Returns the core configuration URL only if it exists.
     * @param context the current task execution context
     * @return the configuration file (a.k.a. {@code "asakusa-runtime.xml"})
     */
    public static Optional<URL> findCoreConfigurationUrl(ExecutionContext context) {
        return findFrameworkFile(context, LOCATION_CORE_CONFIGURATION)
                .filter(Files::isRegularFile)
                .flatMap(it -> {
                    try {
                        return Optional.of(it.toUri().toURL());
                    } catch (MalformedURLException e) {
                        LOG.warn("failed to convert to URL: {}", it, e);
                        return Optional.empty();
                    }
                });
    }

    /**
     * Returns the attached library files.
     * @param context the current task execution context
     * @return the library files, or an empty set if there are no such files
     */
    public static Set<Path> findAttachedLibraries(TaskExecutionContext context) {
        return findApplicationFile(context, LOCATION_APPLICATION_ATTACHED_LIBRARIES)
                .map(TaskExecutors::findLibraries)
                .orElse(Collections.emptySet());
    }

    /**
     * Returns the library files on the framework directory.
     * @param context the current task execution context
     * @param location the directory location (relative from the framework installation path)
     * @return the library files, or an empty set if there are no such files
     */
    public static Set<Path> findFrameworkLibraries(ExecutionContext context, String location) {
        return findFrameworkFile(context, location)
                .map(TaskExecutors::findLibraries)
                .orElse(Collections.emptySet());
    }

    /**
     * Returns the framework core library files.
     * @param context the current task execution context
     * @return the library files, or an empty set if there are no such files
     */
    public static Set<Path> findCoreLibraries(ExecutionContext context) {
        return findFrameworkLibraries(context, LOCATION_CORE_LIBRARIES);
    }

    /**
     * Returns the Hadoop embedded library files.
     * @param context the current task execution context
     * @return the library files, or an empty set if there are no such files
     */
    public static Set<Path> findHadoopEmbeddedLibraries(ExecutionContext context) {
        return findFrameworkLibraries(context, LOCATION_HADOOP_EMBEDDED_LIBRARIES);
    }

    /**
     * Returns the framework extension library files.
     * @param context the current task execution context
     * @return the library files, or an empty set if there are no such files
     */
    public static Set<Path> findExtensionLibraries(ExecutionContext context) {
        return findFrameworkLibraries(context, LOCATION_EXTENSION_LIBRARIES);
    }

    private static Set<Path> findLibraries(Path directory) {
        if (Files.isDirectory(directory) == false) {
            return Collections.emptySet();
        }
        try {
            return Files.list(directory)
                    .filter(it -> Optional.of(it.getFileName())
                            .map(name -> name.toString().endsWith(EXTENSION_LIBRARY))
                            .orElse(false))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            LOG.warn("failed to list directory entries: {}", directory, e);
            return Collections.emptySet();
        }
    }

    /**
     * Loads the current batch libraries and performs the given action.
     * @param context the current context
     * @param action the target action
     * @throws IOException if I/O error was occurred in this action
     * @throws InterruptedException if interrupted while executing this action
     */
    public static void withLibraries(
            TaskExecutionContext context,
            IoConsumer<? super ClassLoader> action) throws IOException, InterruptedException {
        List<Path> libraries = TaskExecutors.findJobflowLibrary(context)
                .filter(Files::isRegularFile)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
        withLibraries(context, libraries, action);
    }

    /**
     * Loads the libraries and performs the given action.
     * @param context the current context
     * @param libraries the target libraries
     * @param action the target action
     * @throws IOException if I/O error was occurred in this action
     * @throws InterruptedException if interrupted while executing this action
     */
    public static void withLibraries(
            TaskExecutionContext context,
            List<? extends Path> libraries,
            IoConsumer<? super ClassLoader> action) throws IOException, InterruptedException {
        try (ClassLoaderContext cl = new ClassLoaderContext(loader(context, libraries))) {
            action.accept(cl.getClassLoader());
        }
    }

    private static URLClassLoader loader(TaskExecutionContext context, List<? extends Path> libraries) {
        URL[] urls = libraries.stream()
                .map(Path::toUri)
                .flatMap(uri -> {
                    try {
                        return Stream.of(uri.toURL());
                    } catch (MalformedURLException e) {
                        LOG.warn("failed to convert URI: {}", uri, e);
                        return Stream.empty();
                    }
                })
                .toArray(URL[]::new);
        return URLClassLoader.newInstance(urls, context.getClassLoader());
    }

    static Method findMethod(String className, String methodName, Class<?>... parameterTypes) {
        try {
            Class<?> aClass = Class.forName(className, false, TaskExecutors.class.getClassLoader());
            Method method = aClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            LOG.trace("failed to activate method: {}#{}", className, methodName, e);
            return null;
        }
    }

    /**
     * Represents an action.
     * @since 0.10.0
     * @param <T> the parameter type
     */
    @FunctionalInterface
    public interface IoConsumer<T> {

        /**
         * Performs the action.
         * @param parameter the action parameter
         * @throws IOException if I/O error was occurred in this action
         * @throws InterruptedException if interrupted while executing this action
         */
        void accept(T parameter) throws IOException, InterruptedException;
    }

    private static class ClassLoaderContext implements AutoCloseable {

        private final ClassLoader active;

        private final ClassLoader escaped;

        ClassLoaderContext(ClassLoader newClassLoader) {
            this.active = newClassLoader;
            this.escaped = switchContextClassLoader(newClassLoader);
        }

        ClassLoader getClassLoader() {
            return active;
        }

        @Override
        public void close() throws IOException {
            try {
                switchContextClassLoader(escaped);
            } finally {
                disposeClassLoader(active);
            }
        }

        private static ClassLoader switchContextClassLoader(ClassLoader classLoader) {
            return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);
                return old;
            });
        }

        private static void disposeClassLoader(ClassLoader classLoader) {
            if (classLoader == null) {
                return;
            }
            releaseCachedClasses(classLoader);
            if (classLoader instanceof Closeable) {
                try {
                    ((Closeable) classLoader).close();
                } catch (IOException e) {
                    LOG.debug("failed to close a class loader: {}", classLoader, e);
                }
            }
        }

        private static void releaseCachedClasses(ClassLoader classLoader) {
            if (JCL_RELEASE != null) {
                try {
                    JCL_RELEASE.invoke(null, classLoader);
                } catch (Exception e) {
                    LOG.debug("failed to release logger class-loaders: {}", JCL_RELEASE, e);
                }
            }
            if (REFLECTION_UTILS_RELEASE != null) {
                try {
                    REFLECTION_UTILS_RELEASE.invoke(null);
                } catch (Exception e) {
                    LOG.debug("failed to release classes cache: {}", REFLECTION_UTILS_RELEASE, e);
                }
            }
        }
    }
}
