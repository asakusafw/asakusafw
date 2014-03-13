/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.inprocess;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.testdriver.TestExecutionPlan;

/**
 * Mock implementation of {@link TestExecutionPlan} builder.
 */
public class MockTestExecutionPlanBuilder {

    final String definitionId;

    final String executionId;

    final List<TestExecutionPlan.Command> initializers = new ArrayList<TestExecutionPlan.Command>();

    final List<TestExecutionPlan.Command> importers = new ArrayList<TestExecutionPlan.Command>();

    final List<TestExecutionPlan.Job> jobs = new ArrayList<TestExecutionPlan.Job>();

    final List<TestExecutionPlan.Command> exporters = new ArrayList<TestExecutionPlan.Command>();

    final List<TestExecutionPlan.Command> finalizers = new ArrayList<TestExecutionPlan.Command>();

    /**
     * Creates a new instance.
     * @param flowId the flow ID
     * @param executionId the execution ID
     */
    public MockTestExecutionPlanBuilder(String flowId, String executionId) {
        this.definitionId = flowId;
        this.executionId = executionId;
    }

    /**
     * Adds a command.
     * @param command the command
     */
    public void addInitializer(TestExecutionPlan.Command command) {
        initializers.add(command);
    }

    /**
     * Adds a command.
     * @param command the command
     */
    public void addFinalizer(TestExecutionPlan.Command command) {
        finalizers.add(command);
    }

    /**
     * Adds a command.
     * @param command the command
     */
    public void addImporter(TestExecutionPlan.Command command) {
        importers.add(command);
    }

    /**
     * Adds a command.
     * @param command the command
     */
    public void addExporter(TestExecutionPlan.Command command) {
        exporters.add(command);
    }

    /**
     * Adds a job.
     * @param job the job
     */
    public void addHadoopJob(TestExecutionPlan.Job job) {
        jobs.add(job);
    }

    /**
     * Builds a {@link TestExecutionPlan}.
     * @return the built plan
     */
    public TestExecutionPlan build() {
        return new TestExecutionPlan(definitionId, executionId, initializers, importers, jobs, exporters, finalizers);
    }
}
