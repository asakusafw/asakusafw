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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.internal.impldep.com.google.common.base.Objects;
import org.gradle.tooling.ConfigurableLauncher;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities about Gradle project.
 * @since 0.10.0
 */
public class GradleAdapter {

    static final Logger LOG = LoggerFactory.getLogger(GradleAdapter.class);

    private final ProjectContext context;

    private final ProjectConnection connection;

    private final List<String> arguments = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param context the project context
     * @param connection the target connection
     */
    public GradleAdapter(ProjectContext context, ProjectConnection connection) {
        this.context = context;
        this.connection = connection;
    }

    /**
     * Sets build arguments.
     * It is available during this session.
     * @param args the arguments
     * @return this
     */
    public GradleAdapter withArguments(String... args) {
        arguments.clear();
        Collections.addAll(arguments, args);
        return this;
    }

    /**
     * Returns a Gradle model of the project.
     * @param <T> the model type
     * @param modelType the model type
     * @return the model
     */
    public <T> T get(Class<T> modelType) {
        return configure(connection.model(modelType)).get();
    }

    /**
     * Returns a Gradle model of the project.
     * @param <T> the model type
     * @param modelType the model type
     * @param configurator the model consumer
     * @return this
     */
    public <T> GradleAdapter get(Class<T> modelType, Consumer<? super T> configurator) {
        configurator.accept(get(modelType));
        return this;
    }

    /**
     * Runs tasks in the project.
     * @param tasks the tasks
     * @return this
     */
    public GradleAdapter launch(String... tasks) {
        configure(connection.newBuild().forTasks(tasks)).run();
        return this;
    }

    private <T extends ConfigurableLauncher<T>> T configure(T launcher) {
        List<String> properties = getSystemPropertyArgs();
        List<String> args = new ArrayList<>();
        args.addAll(properties);
        args.addAll(arguments);
        return launcher
                .setEnvironmentVariables(context.environment())
                .setJvmArguments(properties)
                .withArguments(args)
                .addProgressListener(e -> {
                    LOG.debug("[Gradle] {}", e.getDisplayName());
                }, OperationType.TASK, OperationType.TEST)
                .setStandardOutput(System.out)
                .setStandardError(System.err);
    }

    private List<String> getSystemPropertyArgs() {
        Map<String, String> props = context.properties();
        return Stream.concat(
                props.entrySet().stream()
                    .filter(e -> Objects.equal(System.getProperty(e.getKey()), e.getValue()) == false)
                    .map(e -> String.format("-D%s=%s",
                            e.getKey(),
                            Optional.ofNullable(e.getValue()).orElse(""))),
                System.getProperties().keySet().stream()
                    .filter(it -> props.containsKey(it) == false)
                    .map(it -> String.format("-D%s", it)))
                .peek(it -> LOG.debug("Gradle arg: {}", it))
                .collect(Collectors.toList());
    }
}
