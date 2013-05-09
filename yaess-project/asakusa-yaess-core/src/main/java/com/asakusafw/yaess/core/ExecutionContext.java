/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contextual information of jobflow executions.
 * @since 0.2.3
 * @version 0.4.0
 */
public class ExecutionContext {

    static final Logger LOG = LoggerFactory.getLogger(ExecutionContext.class);

    private final String batchId;

    private final String flowId;

    private final String executionId;

    private final ExecutionPhase phase;

    private final Map<String, String> arguments;

    private final Map<String, String> environmentVariables;

    /**
     * Creates a new instance.
     * @param batchId current batch ID
     * @param flowId current flow ID
     * @param executionId current execution ID
     * @param phase current execution phase
     * @param arguments current argument pairs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExecutionContext(
            String batchId,
            String flowId,
            String executionId,
            ExecutionPhase phase,
            Map<String, String> arguments) {
        this(batchId, flowId, executionId, phase, arguments, Collections.<String, String>emptyMap());
    }

    /**
     * Creates a new instance.
     * @param batchId current batch ID
     * @param flowId current flow ID
     * @param executionId current execution ID
     * @param phase current execution phase
     * @param arguments current argument pairs
     * @param environmentVariables environment variables to be inherited
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public ExecutionContext(
            String batchId,
            String flowId,
            String executionId,
            ExecutionPhase phase,
            Map<String, String> arguments,
            Map<String, String> environmentVariables) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        if (phase == null) {
            throw new IllegalArgumentException("phase must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.batchId = batchId;
        this.flowId = flowId;
        this.executionId = executionId;
        this.phase = phase;
        this.arguments = Collections.unmodifiableMap(new HashMap<String, String>(arguments));
        this.environmentVariables = Collections.unmodifiableMap(new HashMap<String, String>(environmentVariables));
    }

    /**
     * Returns current batch ID.
     * @return the batch ID
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Returns current flow ID.
     * @return the flow ID
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Returns current execution ID.
     * @return the execution ID
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Returns current execution phase.
     * @return the phase
     */
    public ExecutionPhase getPhase() {
        return phase;
    }

    /**
     * Returns current argument pairs.
     * @return the execution arguments
     */
    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * Returns current environment variables for sub processes.
     * @return the environment variables
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Returns currnt argument pairs as string format.
     * @return string format of the execution arguments
     */
    public String getArgumentsAsString() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            buf.append(escape(entry.getKey()));
            buf.append("=");
            buf.append(escape(entry.getValue()));
            buf.append(",");
        }
        if (buf.length() >= 1) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    private static final Pattern TO_ESCAPED = Pattern.compile("[=,\\\\]");
    private String escape(String string) {
        assert string != null;
        return TO_ESCAPED.matcher(string).replaceAll("\\\\$0");
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Context'{'batchId={0}, flowId={1}, executionId={2}, phase={3}, arguments={4}, env={5}'}'",
                batchId,
                flowId,
                executionId,
                phase,
                arguments,
                environmentVariables);
    }
}
