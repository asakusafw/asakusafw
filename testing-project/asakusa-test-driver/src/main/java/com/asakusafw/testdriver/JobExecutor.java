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
package com.asakusafw.testdriver;

import java.io.IOException;
import java.util.Map;

import com.asakusafw.workflow.executor.TaskExecutor;

/**
 * Executes a job.
 * @deprecated replaced with {@link TaskExecutor}
 */
@Deprecated
public abstract class JobExecutor {

    /**
     * Validates the target job execution environment.
     * @throws AssertionError if the current execution environment is invalid
     */
    public void validateEnvironment() {
        return;
    }

    /**
     * Validates the target test execution plan for this executor.
     * @param plan the target execution plan
     * @throws AssertionError if the target plan is invalid for the environment
     */
    public void validatePlan(TestExecutionPlan plan) {
        return;
    }

    /**
     * Executes a Hadoop job.
     * @param job the job descriptor
     * @param environmentVariables recommended environment variables
     * @throws IOException if failed to execute the job
     */
    public abstract void execute(
            TestExecutionPlan.Job job,
            Map<String, String> environmentVariables) throws IOException;

    /**
     * Executes a command job.
     * @param command the command descriptor
     * @param environmentVariables recommended environment variables
     * @throws IOException if failed to execute the job
     */
    public abstract void execute(
            TestExecutionPlan.Command command,
            Map<String, String> environmentVariables) throws IOException;
}
