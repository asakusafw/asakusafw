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
package com.asakusafw.integration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.AssumptionViolatedException;

import com.asakusafw.utils.gradle.BaseProject;
import com.asakusafw.utils.gradle.EnvironmentConfigurator;

/**
 * Provides project configurator about Asakusa framework related features.
 * @since 0.9.2
 */
public final class AsakusaConfigurator {

    static final String PROPERTY_JAVA_COMMAND = "java.cmd";

    static final String PROPERTY_HADOOP_COMMAND = "hadoop.cmd";

    static final String PROPERTY_SPARK_COMMAND = "spark.cmd";

    static final String ENVIRONMENT_PROJECT_HOME = "PROJECT_HOME";

    static final String ENVIRONMENT_JAVA_COMMAND = "JAVA_CMD";

    static final String ENVIRONMENT_HADOOP_COMMAND = "HADOOP_CMD";

    static final String ENVIRONMENT_SPARK_COMMAND = "SPARK_CMD";

    static final String NAME_JAVA_COMMAND = "java";

    static final String NAME_HADOOP_COMMAND = "hadoop";

    static final String NAME_SPARK_COMMAND = "spark-submit";

    private AsakusaConfigurator() {
        return;
    }

    /**
     * Sets {@code PROJECT_HOME} to the project directory.
     * @return the configurator
     * @see BaseProject#with(Consumer)
     */
    public static Consumer<BaseProject<?>> projectHome() {
        return project -> {
            project.with(EnvironmentConfigurator.of(
                    ENVIRONMENT_PROJECT_HOME,
                    project.getContents().getDirectory()));
        };
    }

    /**
     * Sets {@code JAVA_CMD} from system property {@code java.cmd}.
     * @param action action if the system property is not defined
     * @return the project configurator
     * @see BaseProject#with(Consumer)
     */
    public static Consumer<BaseProject<?>> java(Action action) {
        return new CommandConfigureAction(
                PROPERTY_JAVA_COMMAND,
                ENVIRONMENT_JAVA_COMMAND,
                action);
    }

    /**
     * Sets {@code HADOOP_CMD} from system property {@code hadoop.cmd}.
     * @param action action if the system property is not defined
     * @return the project configurator
     * @see BaseProject#with(Consumer)
     */
    public static Consumer<BaseProject<?>> hadoop(Action action) {
        return new CommandConfigureAction(
                PROPERTY_HADOOP_COMMAND,
                ENVIRONMENT_HADOOP_COMMAND,
                action);
    }

    /**
     * Sets {@code SPARK_CMD} from system property {@code spark.cmd}.
     * @param action action if the system property is not defined
     * @return the project configurator
     * @see BaseProject#with(Consumer)
     */
    public static Consumer<BaseProject<?>> spark(Action action) {
        return new CommandConfigureAction(
                PROPERTY_SPARK_COMMAND,
                ENVIRONMENT_SPARK_COMMAND,
                action);
    }

    /**
     * Represents action type.
     * @since 0.9.2
     */
    public enum Action {

        /**
         * Unset always.
         */
        UNSET_ALWAYS,

        /**
         * Unset if it is not defined.
         */
        UNSET_IF_UNDEFINED,

        /**
         * Skip if it is not defined.
         */
        SKIP_IF_UNDEFINED,

        /**
         * Error if it is not defined.
         */
        ERROR_IF_UNDEFINED,
    }

    private static final class CommandConfigureAction implements Consumer<BaseProject<?>> {

        private final String property;

        private final String environment;

        private final Action actionType;

        CommandConfigureAction(String property, String environment, Action onMissing) {
            this.property = property;
            this.environment = environment;
            this.actionType = onMissing;
        }

        @Override
        public void accept(BaseProject<?> project) {
            Path path = Optional.ofNullable(source(project))
                    .filter(it -> it.isEmpty() == false)
                    .map(Paths::get)
                    .orElseGet(this::missing);
            project.with(EnvironmentConfigurator.of(environment, path));
        }

        private String source(BaseProject<?> project) {
            switch (actionType) {
            case UNSET_ALWAYS:
                return null;
            case UNSET_IF_UNDEFINED:
            case SKIP_IF_UNDEFINED:
            case ERROR_IF_UNDEFINED:
                return project.property(property);
            default:
                throw new AssertionError(actionType);
            }
        }

        private Path missing() {
            switch (actionType) {
            case UNSET_ALWAYS:
            case UNSET_IF_UNDEFINED:
                return null;
            case SKIP_IF_UNDEFINED:
                throw new AssumptionViolatedException(MessageFormat.format(
                        "system property \"{0}\" should be defined",
                        property));
            case ERROR_IF_UNDEFINED:
                throw new AssertionError(MessageFormat.format(
                        "system property \"{0}\" must be defined",
                        property));
            default:
                throw new AssertionError(actionType);
            }
        }
    }
}
