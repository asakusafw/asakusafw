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
package com.asakusafw.compiler.flow;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.IoContext;
import com.asakusafw.runtime.util.VariableTable;

/**
 * Provides commands for processing external I/Os.
 */
public class ExternalIoCommandProvider {

    /**
     * Returns the name of this command provider.
     * @return the command provider name
     */
    public String getName() {
        return "default"; //$NON-NLS-1$
    }

    /**
     * Returns command list for launching the importer.
     * @param context the command context
     * @return the corresponding command list
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Command> getImportCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * Returns command list for launching the exporter.
     * @param context the command context
     * @return the corresponding command list
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Command> getExportCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * Returns command list for recovering the target external I/O component.
     * @param context the command context
     * @return the corresponding command list
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @deprecated Please use {@link #getFinalizeCommand(CommandContext)} instead
     */
    @Deprecated
    public List<Command> getRecoverCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * Returns command list for initializing the target external I/O component.
     * @param context the command context
     * @return the corresponding command list
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Command> getInitializeCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * Returns command list for finalizing the target external I/O component.
     * @param context the command context
     * @return the corresponding command list
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Command> getFinalizeCommand(CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * Represents a context for {@link ExternalIoCommandProvider}.
     */
    public static class CommandContext {

        private final String homePathPrefix;

        private final String executionId;

        private final String variableList;

        /**
         * Creates a new instance.
         * @param homePathPrefix the command path prefix (may be the framework installation path)
         * @param executionId the target execution ID
         * @param variableList the serialized batch arguments
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public CommandContext(String homePathPrefix, String executionId, String variableList) {
            Precondition.checkMustNotBeNull(homePathPrefix, "homePathPrefix"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(executionId, "executionId"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(variableList, "variableList"); //$NON-NLS-1$
            this.homePathPrefix = homePathPrefix;
            this.executionId = executionId;
            this.variableList = variableList;
        }

        /**
         * Creates a new instance.
         * @param homePathPrefix the command path prefix (may be the framework installation path)
         * @param executionId the target execution ID
         * @param variables the batch arguments
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public CommandContext(String homePathPrefix, String executionId, Map<String, String> variables) {
            Precondition.checkMustNotBeNull(homePathPrefix, "homePathPrefix"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(executionId, "executionId"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(variables, "variables"); //$NON-NLS-1$
            this.homePathPrefix = homePathPrefix;
            this.executionId = executionId;
            VariableTable table = new VariableTable();
            table.defineVariables(variables);
            this.variableList = table.toSerialString();
        }

        /**
         * Returns the command path prefix (may be the framework installation path).
         * @return the command path prefix
         */
        public String getHomePathPrefix() {
            return homePathPrefix;
        }

        /**
         * Returns the target execution ID.
         * @return the target execution ID
         */
        public String getExecutionId() {
            return executionId;
        }

        /**
         * Returns the serialized batch arguments.
         * @return the serialized batch arguments
         */
        public String getVariableList() {
            return variableList;
        }
    }

    /**
     * Represents a command for launching external I/O components.
     * @since 0.1.0
     * @version 0.5.1
     */
    public static class Command {

        private final String id;

        private final List<String> commandLine;

        private final String moduleName;

        private final String profileName;

        private final Map<String, String> environment;

        private final IoContext context;

        /**
         * Creates a new instance.
         * @param commandLine the command line tokens
         * @param moduleName target module ID
         * @param profileName target profile ID, or {@code null} for the default profile
         * @param environment target environment variables
         * @throws IllegalArgumentException if some parameters were {@code null}
         * @deprecated Use {@link IoContext} instead
         */
        @Deprecated
        public Command(
                List<String> commandLine,
                String moduleName,
                String profileName,
                Map<String, String> environment) {
            this(UUID.randomUUID().toString(), commandLine, moduleName, profileName, environment, IoContext.EMPTY);
        }

        /**
         * Creates a new instance.
         * @param id the command ID
         * @param commandLine the command line tokens
         * @param moduleName target module ID
         * @param profileName target profile ID, or {@code null} for the default profile
         * @param environment target environment variables
         * @param context I/O information for target command
         * @throws IllegalArgumentException if some parameters were {@code null}
         * @since 0.5.1
         */
        public Command(
                String id,
                List<String> commandLine,
                String moduleName,
                String profileName,
                Map<String, String> environment,
                IoContext context) {
            Precondition.checkMustNotBeNull(id, "id"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(commandLine, "commandLine"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(moduleName, "moduleName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
            this.id = id;
            this.commandLine = commandLine;
            this.moduleName = moduleName;
            this.profileName = profileName;
            this.environment = environment;
            this.context = context;
        }

        /**
         * Returns the command ID.
         * @return the ID
         * @since 0.5.1
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the command line tokens.
         * @return the command line tokens
         */
        public List<String> getCommandTokens() {
            return commandLine;
        }

        /**
         * Returns the concatenated command line (for b-sh).
         * @return the concatenated command line
         * @deprecated use {@link #getCommandTokens()} instead
         */
        @Deprecated
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
         * Returns the module ID of the target external I/O component.
         * @return the module ID
         */
        public String getModuleName() {
            return moduleName;
        }

        /**
         * Returns the profile ID for using this command.
         * @return the profile ID, or {@code null} for using the default profile
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

        /**
         * Returns I/O context for this command.
         * @return the I/O context
         * @since 0.5.1
         */
        public IoContext getContext() {
            return context;
        }
    }
}
