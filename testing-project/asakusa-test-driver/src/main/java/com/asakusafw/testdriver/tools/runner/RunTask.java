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
package com.asakusafw.testdriver.tools.runner;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;
import com.asakusafw.testdriver.JobExecutor;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.yaess.core.BatchScript;
import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScript.Kind;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.FlowScript;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Runs an Asakusa batch application using test driver facilities.
 * @since 0.6.0
 */
public class RunTask {

    static final Logger LOG = LoggerFactory.getLogger(RunTask.class);

    private final Configuration configuration;

    private final ExecutionScriptHandler<?> handler = new ExecutionScriptHandler<ExecutionScript>() {
        @Override
        public void configure(ServiceProfile<?> profile) {
            return;
        }
        @Override
        public String getHandlerId() {
            return "testing";
        }
        @Override
        public String getResourceId(ExecutionContext context, ExecutionScript script) {
            return "testing";
        }
        @Override
        public Map<String, String> getProperties(ExecutionContext context, ExecutionScript script) {
            if (script.getKind() == Kind.HADOOP) {
                return getHadoopProperties();
            } else {
                return Collections.emptyMap();
            }
        }
        @Override
        public Map<String, String> getEnvironmentVariables(ExecutionContext context, ExecutionScript script) {
            return context.getEnvironmentVariables();
        }
        @Override
        public void setUp(ExecutionMonitor monitor, ExecutionContext context) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void execute(ExecutionMonitor monitor, ExecutionContext context, ExecutionScript script) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void cleanUp(ExecutionMonitor monitor, ExecutionContext context) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Creates a new instance.
     * @param configuration the task configuration
     */
    public RunTask(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Executes an Asakusa batch application.
     * @throws IOException if failed to prepare execution plan
     * @throws AssertionError if failed to execute each job
     */
    public void perform() throws IOException {
        TestDriverContext context = configuration.context;
        for (FlowScript flow : configuration.script.getAllFlows()) {
            context.setCurrentBatchId(configuration.script.getId());
            context.setCurrentFlowId(flow.getId());
            context.setCurrentExecutionId(getExecutionId(flow));

            TestExecutionPlan plan = toPlan(flow);
            executePlan(plan);
        }
    }

    private TestExecutionPlan toPlan(FlowScript flow) throws IOException {
        assert flow != null;
        List<TestExecutionPlan.Command> initializers = resolveCommands(flow, ExecutionPhase.INITIALIZE);
        List<TestExecutionPlan.Command> importers = resolveCommands(flow, ExecutionPhase.IMPORT);
        List<TestExecutionPlan.Job> jobs = new ArrayList<TestExecutionPlan.Job>();
        jobs.addAll(resolveJobs(flow, ExecutionPhase.PROLOGUE));
        jobs.addAll(resolveJobs(flow, ExecutionPhase.MAIN));
        jobs.addAll(resolveJobs(flow, ExecutionPhase.EPILOGUE));
        List<TestExecutionPlan.Command> exporters = resolveCommands(flow, ExecutionPhase.EXPORT);
        List<TestExecutionPlan.Command> finalizers = resolveCommands(flow, ExecutionPhase.FINALIZE);
        return new TestExecutionPlan(
                flow.getId(),
                configuration.context.getExecutionId(),
                initializers,
                importers,
                jobs,
                exporters,
                finalizers);
    }

    private List<TestExecutionPlan.Job> resolveJobs(FlowScript flow, ExecutionPhase phase) throws IOException {
        ExecutionContext context = createExecutionContext(flow, phase);
        List<TestExecutionPlan.Job> results = new ArrayList<TestExecutionPlan.Job>();
        for (ExecutionScript script : flow.getScripts().get(phase)) {
            HadoopScript resolved = (HadoopScript) resolveScript(script, context);
            Map<String, String> props = new TreeMap<String, String>();
            props.putAll(getHadoopProperties());
            props.putAll(resolved.getHadoopProperties());
            results.add(new TestExecutionPlan.Job(
                    resolved.getClassName(),
                    context.getExecutionId(),
                    props));
        }
        return results;
    }

    private List<TestExecutionPlan.Command> resolveCommands(FlowScript flow, ExecutionPhase phase) throws IOException {
        ExecutionContext context = createExecutionContext(flow, phase);
        List<TestExecutionPlan.Command> results = new ArrayList<TestExecutionPlan.Command>();
        for (ExecutionScript script : flow.getScripts().get(phase)) {
            CommandScript resolved = (CommandScript) resolveScript(script, context);
            results.add(new TestExecutionPlan.Command(
                    resolved.getCommandLineTokens(),
                    resolved.getModuleName(),
                    resolved.getProfileName(),
                    resolved.getEnvironmentVariables()));
        }
        return results;
    }

    private ExecutionScript resolveScript(ExecutionScript script, ExecutionContext context) throws IOException {
        try {
            return script.resolve(context, handler);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to resolve a job: id={1}, context={0}",
                    context,
                    script.getId()), e);
        }
    }

    Map<String, String> getHadoopProperties() {
        TestDriverContext context = configuration.context;
        VariableTable resolver = new VariableTable(RedefineStrategy.ERROR);
        resolver.defineVariables(context.getBatchArgs());
        Map<String, String> dPropMap = Maps.create();
        dPropMap.put(StageConstants.PROP_USER, context.getOsUser());
        dPropMap.put(StageConstants.PROP_EXECUTION_ID, context.getExecutionId());
        dPropMap.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS, resolver.toSerialString());
        dPropMap.putAll(context.getExtraConfigurations());
        return dPropMap;
    }

    private ExecutionContext createExecutionContext(FlowScript flow, ExecutionPhase phase) {
        return new ExecutionContext(
                configuration.script.getId(),
                flow.getId(),
                getExecutionId(flow),
                phase,
                configuration.context.getBatchArgs(),
                configuration.context.getEnvironmentVariables());
    }

    private String getExecutionId(FlowScript flow) {
        assert flow != null;
        return String.format("%s-%s-%s",
                configuration.executionIdPrefix,
                configuration.script.getId(),
                flow.getId());
    }

    private void executePlan(TestExecutionPlan plan) throws IOException {
        assert plan != null;
        TestDriverContext context = configuration.context;
        JobExecutor executor = context.getJobExecutor();
        LOG.info("Executing plan: batchId={}, flowId={}, execId={}, args={}, executor={}", new Object[] {
                context.getCurrentBatchId(),
                context.getCurrentFlowId(),
                context.getExecutionId(),
                context.getBatchArgs(),
                executor.getClass().getName(),
        });
        try {
            runJobFlowCommands(executor, plan.getInitializers());
            runJobFlowCommands(executor, plan.getImporters());
            runJobflowJobs(executor, plan.getJobs());
            runJobFlowCommands(executor, plan.getExporters());
        } finally {
            runJobFlowCommands(executor, plan.getFinalizers());
        }
    }

    private void runJobflowJobs(JobExecutor executor, List<TestExecutionPlan.Job> jobs) throws IOException {
        assert jobs != null;
        for (TestExecutionPlan.Job job : jobs) {
            executor.execute(job, getEnvironmentVariables());
        }
    }

    private void runJobFlowCommands(JobExecutor executor, List<TestExecutionPlan.Command> cmdList) throws IOException {
        assert cmdList != null;
        for (TestExecutionPlan.Command command : cmdList) {
            executor.execute(command, getEnvironmentVariables());
        }
    }

    private Map<String, String> getEnvironmentVariables() {
        Map<String, String> variables = Maps.from(configuration.context.getEnvironmentVariables());
        return variables;
    }

    /**
     * Represents a configuration for {@link RunTask}.
     * @since 0.6.0
     */
    public static final class Configuration {

        final TestDriverContext context;

        final BatchScript script;

        final String executionIdPrefix;

        /**
         * Creates a new instance.
         * @param context the current test driver context
         * @param script the target execution script
         * @param executionIdPrefix the execution ID prefix for each jobflow execution
         */
        public Configuration(TestDriverContext context, BatchScript script, String executionIdPrefix) {
            this.context = context;
            this.script = script;
            this.executionIdPrefix = executionIdPrefix;
        }
    }
}
