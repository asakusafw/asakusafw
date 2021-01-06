/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.windgate.bootstrap;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a bootstrap context.
 * @since 0.10.0
 */
public class Context {

    private static final int IDX_PROFILE_NAME = 0;

    private static final int IDX_PROCCESS_SESSION_KIND = 1;

    private static final int IDX_PROCCESS_SCRIPT_PATH = 2;

    private static final int IDX_PROCCESS_BATCH_ID = 3;

    private static final int IDX_PROCCESS_FLOW_ID = 4;

    private static final int IDX_PROCCESS_EXECUTION_ID = 5;

    private static final int IDX_PROCCESS_BATCH_ARGUMENTS = 6;

    private static final int IDX_FINALIZE_FULL_BATCH_ID = 1;

    private static final int IDX_FINALIZE_FULL_FLOW_ID = 2;

    private static final int IDX_FINALIZE_FULL_EXECUTION_ID = 3;

    private static final int IDX_FINALIZE_SHORT_EXECUTION_ID = 1;

    private static final Set<String> AVAILABLE_SESSION_KINDS = Arrays.stream(new String[] {
            "begin",
            "end",
            "oneshot",
    }).collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

    private final String profileName;

    private final String scriptPath;

    private String sessionKind;

    private final String batchId;

    private final String flowId;

    private final String executionId;

    private final String batchArguments;

    Context(
            String profileName, String sessionKind, String scriptPath,
            String batchId, String flowId, String executionId,
            String batchArguments) {
        if (AVAILABLE_SESSION_KINDS.contains(sessionKind) == false) {
            throw new IllegalStateException(sessionKind);
        }
        this.profileName = profileName;
        this.scriptPath = scriptPath;
        this.sessionKind = sessionKind;
        this.batchId = batchId;
        this.flowId = flowId;
        this.executionId = executionId;
        this.batchArguments = batchArguments;
    }

    Context(
            String profileName,
            String batchId, String flowId, String executionId) {
        this.profileName = profileName;
        this.scriptPath = null;
        this.batchId = batchId;
        this.flowId = flowId;
        this.executionId = executionId;
        this.batchArguments = "";
    }

    /**
     * Parses the program arguments.
     * @param args the program arguments
     * @return this
     */
    public static Context parseForProcess(String... args) {
        if (args.length != IDX_PROCCESS_BATCH_ARGUMENTS + 1) {
            throw new IllegalStateException(MessageFormat.format(
                    "invalid arguments: {0}",
                    String.join(", ", args)));
        }
        return new Context(
                args[IDX_PROFILE_NAME],
                args[IDX_PROCCESS_SESSION_KIND],
                args[IDX_PROCCESS_SCRIPT_PATH],
                args[IDX_PROCCESS_BATCH_ID],
                args[IDX_PROCCESS_FLOW_ID],
                args[IDX_PROCCESS_EXECUTION_ID],
                args[IDX_PROCCESS_BATCH_ARGUMENTS]);
    }

    /**
     * Parses the program arguments.
     * @param args the program arguments
     * @return this
     */
    public static Context parseForFinalize(String... args) {
        switch (args.length) {
        case IDX_PROFILE_NAME + 1:
            return new Context(
                    args[IDX_PROFILE_NAME],
                    null,
                    null,
                    null);
        case IDX_FINALIZE_SHORT_EXECUTION_ID + 1:
            return new Context(
                    args[IDX_PROFILE_NAME],
                    null,
                    null,
                    args[IDX_FINALIZE_SHORT_EXECUTION_ID]);
        case IDX_FINALIZE_FULL_EXECUTION_ID + 1:
            return new Context(
                    args[IDX_PROFILE_NAME],
                    args[IDX_FINALIZE_FULL_BATCH_ID],
                    args[IDX_FINALIZE_FULL_FLOW_ID],
                    args[IDX_FINALIZE_FULL_EXECUTION_ID]);
        default:
            throw new IllegalStateException(MessageFormat.format(
                    "invalid arguments: {0}",
                    String.join(", ", args)));
        }
    }

    /**
     * Returns the profile name.
     * @return the profile name
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the script path.
     * @return the script path
     */
    public String getScriptPath() {
        return scriptPath;
    }

    /**
     * Returns the session kind.
     * @return the session kind
     */
    public String getSessionKind() {
        return sessionKind;
    }

    /**
     * Returns the batch ID.
     * @return the batch ID
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Returns the flow ID.
     * @return the flow ID
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Returns the execution ID.
     * @return the execution ID
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Returns the serialized batch arguments.
     * @return the serialized batch arguments
     */
    public String getBatchArguments() {
        return batchArguments;
    }

    /**
     * Returns the serialized WindGate arguments.
     * @return the serialized WindGate arguments
     */
    public String getWindGateArguments() {
        StringBuilder buf = new StringBuilder();
        buf.append(getBatchArguments());
        append(buf, "user", System.getProperty("user.name"));
        append(buf, "batch_id", getBatchId());
        append(buf, "flow_id", getFlowId());
        append(buf, "execution_id", getExecutionId());
        return buf.toString();
    }

    private static void append(StringBuilder buf, String key, String value) {
        buf.append(",");
        buf.append(key);
        buf.append("=");
        if (value != null) {
            buf.append(value);
        }
    }

    /**
     * Installs WindGate logger settings.
     */
    public void installLogSettings() {
        AccessController.doPrivileged((PrivilegedAction<?>) () -> {
            System.setProperty("com.asakusafw.windgate.log.batchId", orUnknown(getBatchId()));
            System.setProperty("com.asakusafw.windgate.log.flowId", orUnknown(getFlowId()));
            System.setProperty("com.asakusafw.windgate.log.executionId", orUnknown(getExecutionId()));
            return null;
        });
    }

    String orUnknown(String value) {
        return Optional.ofNullable(value).orElse("(unknown)");
    }
}
