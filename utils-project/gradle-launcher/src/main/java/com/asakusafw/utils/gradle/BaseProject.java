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
package com.asakusafw.utils.gradle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of project structure.
 * @param <TSelf> this type
 * @since 0.9.2
 * @see BasicProject
 */
public abstract class BaseProject<TSelf extends BaseProject<TSelf>> implements ProjectContext {

    private static final Logger LOG = LoggerFactory.getLogger(BaseProject.class);

    static final boolean CASE_SENSITIVE = isCaseSensitiveEnvironmentVariables();

    static final String KEY_GRADLE_VERSION = "gradle.version";

    private final Map<String, String> environment = CASE_SENSITIVE
            ? new LinkedHashMap<>()
            : new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final Map<String, String> properties = new LinkedHashMap<>();

    private static boolean isCaseSensitiveEnvironmentVariables() {
        Map<String, String> env = System.getenv();
        if (env instanceof SortedMap<?, ?>) {
            Comparator<? super String> comparator = ((SortedMap<String, String>) env).comparator();
            return comparator == null || comparator.compare("a", "A") != 0;
        }
        if (env.isEmpty() == false) {
            for (String name : env.keySet()) {
                String upper = name.toUpperCase(Locale.ENGLISH);
                String lower = name.toLowerCase(Locale.ENGLISH);
                if (upper.equals(lower) == false) {
                    String value = System.getenv(name);
                    String upperValue = System.getenv(upper);
                    String lowerValue = System.getenv(lower);
                    return Objects.equals(value, upperValue) == false || Objects.equals(value, lowerValue) == false;
                }
            }
        }
        return !CommandPath.WINDOWS;
    }

    /**
     * Returns this object.
     * @return this
     */
    @SuppressWarnings("unchecked")
    protected TSelf self() {
        return (TSelf) this;
    }

    @Override
    public Map<String, String> environment() {
        return normalize(environment);
    }

    @Override
    public Map<String, String> properties() {
        return normalize(properties);
    }

    private static Map<String, String> normalize(Map<String, String> map) {
        for (Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator(); iter.hasNext();) {
            if (iter.next().getValue() == null) {
                iter.remove();
            }
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String environment(String name) {
        return environment.get(name);
    }

    @Override
    public String property(String key) {
        return properties.get(key);
    }

    /**
     * Returns the project directory.
     * @return the project directory
     */
    protected abstract Path getDirectory();

    @Override
    public CommandLauncher getCommandLauncher() {
        return new BasicCommandLauncher(getDirectory(), environment());
    }

    /**
     * Configures this project.
     * @param configurator the configurator
     * @return this
     */
    public TSelf with(Consumer<? super TSelf> configurator) {
        configurator.accept(self());
        return self();
    }

    /**
     * Puts an environment variable.
     * @param name the variable name
     * @param value the variable value (nullable)
     * @return this
     */
    public TSelf withEnvironment(String name, String value) {
        return withEnvironment(it -> it.put(name, value));
    }

    /**
     * Sets the environment variables.
     * @param configurator the configurator
     * @return this
     */
    public TSelf withEnvironment(Consumer<? super Map<String, String>> configurator) {
        configurator.accept(environment);
        return self();
    }

    /**
     * Puts an system property.
     * @param name the variable name
     * @param value the variable value (nullable)
     * @return this
     */
    public TSelf withProperty(String name, String value) {
        return withProperties(it -> it.put(name, value));
    }

    /**
     * Sets the system properties.
     * @param configurator the configurator
     * @return this
     */
    public TSelf withProperties(Consumer<? super Map<String, String>> configurator) {
        configurator.accept(properties);
        return self();
    }

    /**
     * Returns the project bundle.
     * @return the project bundle
     */
    public Bundle getContents() {
        return getBundle(getDirectory());
    }

    /**
     * Returns the project bundle.
     * @param configurator the project configurator
     * @return this
     */
    public TSelf withContents(TryConsumer<? super Bundle, IOException> configurator) {
        return configure(getContents(), configurator);
    }

    /**
     * Launches Gradle tasks.
     * @param tasks the task names
     * @return this
     * @see #gradle(TryConsumer)
     */
    public TSelf gradle(String... tasks) {
        return gradle(it -> it.launch(tasks));
    }

    /**
     * Proceeds Gradle actions on this project.
     * @param configurator the Gradle action configurator
     * @return this
     * @see #gradle(String...)
     */
    public TSelf gradle(TryConsumer<? super GradleAdapter, IOException> configurator) {
        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(getDirectory().toFile());
        Optional.ofNullable(property(KEY_GRADLE_VERSION))
                .ifPresent(it -> {
                    LOG.debug("using Gradle: {}", it);
                    connector.useGradleVersion(it);
                });
        ProjectConnection connection = connector.connect();
        try {
            return configure(new GradleAdapter(this, connection), configurator);
        } finally {
            connection.close();
        }
    }

    /**
     * Returns a project related bundle.
     * @param path the bundle path
     * @return the created bundle
     */
    protected Bundle getBundle(Path path) {
        return new Bundle(this, path);
    }

    /**
     * Configures the given object.
     * @param <T> the object type
     * @param object the target object
     * @param configurator the object configurator
     * @return this
     */
    protected <T> TSelf configure(T object, TryConsumer<? super T, IOException> configurator) {
        try {
            configurator.accept(object);
        } catch (IOException e) {
            throw new IllegalStateException("error occurred while processing project", e);
        }
        return self();
    }
}
