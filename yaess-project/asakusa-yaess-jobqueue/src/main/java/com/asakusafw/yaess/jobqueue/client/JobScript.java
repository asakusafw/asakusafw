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
package com.asakusafw.yaess.jobqueue.client;

import java.util.Map;

import com.asakusafw.yaess.core.ExecutionPhase;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a job script to be sent to remote server.
 * @since 0.2.6
 */
public class JobScript {

    private String batchId;

    private String flowId;

    private String executionId;

    @SerializedName("phaseId")
    private ExecutionPhase phase;

    private String stageId;

    @SerializedName("mainClass")
    private String mainClassName;

    private Map<String, String> arguments;

    private Map<String, String> properties;

    @SerializedName("env")
    private Map<String, String> environmentVariables;

    /**
     * Returns the batch ID for target job.
     * @return the batch ID
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the batch ID of target job.
     * @param id the ID
     */
    public void setBatchId(String id) {
        this.batchId = id;
    }

    /**
     * Returns the flow ID for target job.
     * @return the flow ID
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Sets the flow ID of target job.
     * @param id the ID
     */
    public void setFlowId(String id) {
        this.flowId = id;
    }

    /**
     * Returns the execution ID for target job.
     * @return the execution ID
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Sets the execution ID of target job.
     * @param id the ID
     */
    public void setExecutionId(String id) {
        this.executionId = id;
    }

    /**
     * Returns the phase kind for target job.
     * @return the phase kind
     */
    public ExecutionPhase getPhase() {
        return phase;
    }

    /**
     * Sets the phase kind of target job.
     * @param phase the phase kind
     */
    public void setPhase(ExecutionPhase phase) {
        this.phase = phase;
    }

    /**
     * Returns the stage ID for target job.
     * @return the stage ID
     */
    public String getStageId() {
        return stageId;
    }

    /**
     * Sets the stage ID of target job.
     * @param id the ID
     */
    public void setStageId(String id) {
        this.stageId = id;
    }

    /**
     * Returns the main class name for the target job.
     * @return the main class name
     */
    public String getMainClassName() {
        return mainClassName;
    }

    /**
     * Sets the main class name of target job.
     * @param name the fqn of the target class
     */
    public void setMainClassName(String name) {
        this.mainClassName = name;
    }

    /**
     * Returns the batch arguments map for the target job.
     * @return the arguments
     */
    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * Sets the batch arguments map for the target job.
     * @param arguments the arguments map
     */
    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    /**
     * Returns the Hadoop properties map for the target job.
     * @return the properties map
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets the Hadoop properties map of target job.
     * @param map the map
     */
    public void setProperties(Map<String, String> map) {
        this.properties = map;
    }

    /**
     * Returns the environment variables for the target job.
     * @return the variables map
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Sets the environment variables map of target job.
     * @param map the map
     */
    public void setEnvironmentVariables(Map<String, String> map) {
        this.environmentVariables = map;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JobScript [batchId=");
        builder.append(batchId);
        builder.append(", flowId=");
        builder.append(flowId);
        builder.append(", executionId=");
        builder.append(executionId);
        builder.append(", phase=");
        builder.append(phase);
        builder.append(", stageId=");
        builder.append(stageId);
        builder.append(", mainClassName=");
        builder.append(mainClassName);
        builder.append(", arguments=");
        builder.append(arguments);
        builder.append(", properties=");
        builder.append(properties);
        builder.append(", environmentVariables=");
        builder.append(environmentVariables);
        builder.append("]");
        return builder.toString();
    }
}
