/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;

/**
 * Represents an execution plan for testing.
 * @since 0.1.0
 * @version 0.7.3
 */
public class TestExecutionPlan implements Serializable {

    private static final long serialVersionUID = -8962301043507876930L;

    private final String definitionId;

    private final String executionId;

    private final List<Task> initializers;

    private final List<Task> importers;

    private final List<Task> jobs;

    private final List<Task> exporters;

    private final List<Task> finalizers;

    /**
     * Creates a new instance.
     * @param definitionId the static definition ID of this plan
     * @param executionId the runtime execution ID of this plan
     * @param initializers tasks in initialize phase
     * @param importers tasks in import phase
     * @param jobs tasks in main phase
     * @param exporters tasks in export phase
     * @param finalizers tasks in finalize phase
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public TestExecutionPlan(
            String definitionId,
            String executionId,
            List<? extends Task> initializers,
            List<? extends Task> importers,
            List<? extends Task> jobs,
            List<? extends Task> exporters,
            List<? extends Task> finalizers) {
        Precondition.checkMustNotBeNull(definitionId, "definitionId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(executionId, "executionId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(initializers, "initializers"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importers, "importers"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(jobs, "jobs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(exporters, "exporters"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(finalizers, "finalizers"); //$NON-NLS-1$
        this.definitionId = definitionId;
        this.executionId = executionId;
        this.initializers = new ArrayList<Task>(initializers);
        this.importers = new ArrayList<Task>(importers);
        this.jobs = new ArrayList<Task>(jobs);
        this.exporters = new ArrayList<Task>(exporters);
        this.finalizers = new ArrayList<Task>(finalizers);
    }

    /**
     * Returns the static definition ID of this plan.
     * @return the static definition ID
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * Returns the runtime execution ID of this plan.
     * @return the runtime execution ID
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Returns tasks in initialize phase.
     * @return tasks
     */
    public List<Task> getInitializers() {
        return initializers;
    }

    /**
     * Returns tasks in import phase.
     * @return tasks
     */
    public List<Task> getImporters() {
        return importers;
    }

    /**
     * Returns tasks in main phase.
     * @return tasks
     */
    public List<Task> getJobs() {
        return jobs;
    }

    /**
     * Returns tasks in export phase.
     * @return tasks
     */
    public List<Task> getExporters() {
        return exporters;
    }

    /**
     * Returns tasks in finalize phase.
     * @return tasks
     */
    public List<Task> getFinalizers() {
        return finalizers;
    }

    /**
     * Represents a task in execution.
     * @since 0.7.3
     */
    public interface Task extends Serializable {

        /**
         * Returns the kind of this task.
         * @return the kind of this task
         */
        TaskKind getTaskKind();
    }

    /**
     * Represents a kind of {@link Task}.
     * @since 0.7.3
     */
    public enum TaskKind {

        /**
         * Represents a command task.
         */
        COMMAND,

        /**
         * Represents a Hadoop job task.
         */
        HADOOP,
    }

    /**
     * Represents a Hadoop job task.
     * @since 0.1.0
     * @version 0.7.3
     */
    public static class Job implements Task {

        private static final long serialVersionUID = -1707317463227716296L;

        private final String className;

        private final Map<String, String> properties;

        /**
         * Creates a new instance.
         * @param className the fully qualified name of the job client class
         * @param properties extra Hadoop properties
         * @throws IllegalArgumentException if arguments contains {@code null}
         */
        public Job(String className, Map<String, String> properties) {
            Precondition.checkMustNotBeNull(className, "className"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(properties, "properties"); //$NON-NLS-1$
            this.className = className;
            this.properties = properties;
        }

        @Override
        public TaskKind getTaskKind() {
            return TaskKind.HADOOP;
        }

        /**
         * Returns the fully qualified name of the job client class.
         * @return the fully qualified name of the job client class
         */
        public String getClassName() {
            return className;
        }

        /**
         * Returns the extra Hadoop properties.
         * @return the extra Hadoop properties
         */
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    /**
     * Represents a generic command task.
     * @since 0.1.0
     * @version 0.7.3
     */
    public static class Command implements Task {

        private static final long serialVersionUID = -6594560296027009816L;

        private final List<String> commandLine;

        private final String moduleName;

        private final String profileName;

        private final Map<String, String> environment;

        /**
         * Creates a new instance.
         * @param commandLine the command line tokens
         * @param moduleName the target module name
         * @param profileName the target profile name, or {@code null} if it is default
         * @param environment the environment variables
         * @throws IllegalArgumentException if arguments contains {@code null}
         */
        public Command(
                List<String> commandLine,
                String moduleName,
                String profileName,
                Map<String, String> environment) {
            Precondition.checkMustNotBeNull(commandLine, "commandLine"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(moduleName, "moduleName"); //$NON-NLS-1$
            this.commandLine = commandLine;
            this.moduleName = moduleName;
            this.profileName = profileName;
            this.environment = environment;
        }

        @Override
        public TaskKind getTaskKind() {
            return TaskKind.COMMAND;
        }

        /**
         * Returns the command line tokens.
         * @return the command line tokens
         */
        public List<String> getCommandTokens() {
            return commandLine;
        }

        /**
         * Returns the command line string.
         * @return the command line string
         */
        public String getCommandLineString() {
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                buf.append("'" + entry.getKey() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                buf.append("="); //$NON-NLS-1$
                buf.append("'" + entry.getValue() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                buf.append(" "); //$NON-NLS-1$
            }
            Iterator<String> iter = commandLine.iterator();
            if (iter.hasNext()) {
                buf.append(iter.next());
                while (iter.hasNext()) {
                    buf.append(" "); //$NON-NLS-1$
                    buf.append(iter.next());
                }
            }
            return buf.toString();
        }

        /**
         * Returns the target module name.
         * @return the target module name
         */
        public String getModuleName() {
            return moduleName;
        }

        /**
         * Returns the target profile name.
         * @return the target profile name, or {@code null} if it is not set
         */
        public String getProfileName() {
            return profileName;
        }

        /**
         * Returns the environment variables.
         * @return the environment variables
         */
        public Map<String, String> getEnvironment() {
            return environment;
        }
    }
}
