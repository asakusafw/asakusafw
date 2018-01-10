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
package com.asakusafw.yaess.core.task;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.yaess.basic.ExitCodeException;
import com.asakusafw.yaess.core.BatchScript;
import com.asakusafw.yaess.core.Blob;
import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.CommandScriptHandler;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionLock;
import com.asakusafw.yaess.core.ExecutionLockProvider;
import com.asakusafw.yaess.core.ExecutionMonitorProvider;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.FlowScript;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.Job;
import com.asakusafw.yaess.core.JobScheduler;
import com.asakusafw.yaess.core.JobScheduler.ErrorHandler;
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.YaessCoreLogger;
import com.asakusafw.yaess.core.YaessLogger;
import com.asakusafw.yaess.core.YaessProfile;

/**
 * Task to execute target batch, flow, or phase.
 * @since 0.2.3
 * @version 0.8.0
 */
public class ExecutionTask {

    static final String KEY_SKIP_FLOWS = "skipFlows";

    static final String KEY_SERIALIZE_FLOWS = "serializeFlows";

    static final String KEY_VERIFY_APPLICATION = "verifyApplication";

    static final String KEY_VERIFY_DRYRUN = "dryRun";

    static final YaessLogger YSLOG = new YaessCoreLogger(ExecutionTask.class);

    static final Logger LOG = LoggerFactory.getLogger(ExecutionTask.class);

    private final ExecutionMonitorProvider monitors;

    private final ExecutionLockProvider locks;

    private final JobScheduler scheduler;

    private final HadoopScriptHandler hadoopHandler;

    private final Map<String, CommandScriptHandler> commandHandlers;

    private final BatchScript script;

    private final Map<String, String> batchArguments;

    private final Map<String, String> environmentVaritables = new ConcurrentHashMap<>();

    private final Map<String, Blob> extensions;

    private final Set<String> skipFlows = Collections.synchronizedSet(new HashSet<String>());

    private volatile RuntimeContext runtimeContext;

    private volatile boolean serializeFlows = false;

    /**
     * Creates a new instance.
     * @param monitors monitor provider
     * @param locks lock provider
     * @param scheduler job scheduler
     * @param hadoopHandler Hadoop script handler
     * @param commandHandlers command script handlers and its profile names
     * @param script target script
     * @param batchArguments current batch arguments
     * @param extensions extension BLOBs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    ExecutionTask(
            ExecutionMonitorProvider monitors,
            ExecutionLockProvider locks,
            JobScheduler scheduler,
            HadoopScriptHandler hadoopHandler,
            Map<String, CommandScriptHandler> commandHandlers,
            BatchScript script,
            Map<String, String> batchArguments,
            Map<String, Blob> extensions) {
        if (monitors == null) {
            throw new IllegalArgumentException("monitors must not be null"); //$NON-NLS-1$
        }
        if (locks == null) {
            throw new IllegalArgumentException("locks must not be null"); //$NON-NLS-1$
        }
        if (scheduler == null) {
            throw new IllegalArgumentException("scheduler must not be null"); //$NON-NLS-1$
        }
        if (hadoopHandler == null) {
            throw new IllegalArgumentException("hadoopHandler must not be null"); //$NON-NLS-1$
        }
        if (commandHandlers == null) {
            throw new IllegalArgumentException("commandHandlers must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        if (batchArguments == null) {
            throw new IllegalArgumentException("batchArguments must not be null"); //$NON-NLS-1$
        }
        if (extensions == null) {
            throw new IllegalArgumentException("extensions must not be null"); //$NON-NLS-1$
        }
        this.monitors = monitors;
        this.locks = locks;
        this.scheduler = scheduler;
        this.hadoopHandler = hadoopHandler;
        this.commandHandlers = Collections.unmodifiableMap(new HashMap<>(commandHandlers));
        this.script = script;
        this.batchArguments = Collections.unmodifiableMap(new LinkedHashMap<>(batchArguments));
        this.extensions = Collections.unmodifiableMap(new LinkedHashMap<>(extensions));
        this.runtimeContext = RuntimeContext.get();
    }

    /**
     * Loads profile and create a new {@link ExecutionTask}.
     * @param profile target profile
     * @param script target script
     * @param batchArguments current batch arguments
     * @return the created task
     * @throws InterruptedException if interrupted while configuring services
     * @throws IOException if failed to configure services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ExecutionTask load(
            YaessProfile profile,
            Properties script,
            Map<String, String> batchArguments) throws InterruptedException, IOException {
        return load(profile, script, batchArguments, Collections.emptyMap());
    }

    /**
     * Loads profile and create a new {@link ExecutionTask}.
     * @param profile target profile
     * @param script target script
     * @param batchArguments current batch arguments
     * @param yaessArguments current control arguments
     * @return the created task
     * @throws InterruptedException if interrupted while configuring services
     * @throws IOException if failed to configure services
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.6
     */
    public static ExecutionTask load(
            YaessProfile profile,
            Properties script,
            Map<String, String> batchArguments,
            Map<String, String> yaessArguments) throws InterruptedException, IOException {
        return load(profile, script, batchArguments, yaessArguments, Collections.emptyMap());
    }

    /**
     * Loads profile and create a new {@link ExecutionTask}.
     * @param profile target profile
     * @param script target script
     * @param batchArguments current batch arguments
     * @param yaessArguments current control arguments
     * @param extensions extension BLOBs
     * @return the created task
     * @throws InterruptedException if interrupted while configuring services
     * @throws IOException if failed to configure services
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.8.0
     */
    public static ExecutionTask load(
            YaessProfile profile,
            Properties script,
            Map<String, String> batchArguments,
            Map<String, String> yaessArguments,
            Map<String, Blob> extensions) throws InterruptedException, IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        if (batchArguments == null) {
            throw new IllegalArgumentException("batchArguments must not be null"); //$NON-NLS-1$
        }
        if (yaessArguments == null) {
            throw new IllegalArgumentException("yaessArguments must not be null"); //$NON-NLS-1$
        }
        if (extensions == null) {
            throw new IllegalArgumentException("extensions must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Loading execution monitor feature");
        ExecutionMonitorProvider monitors = profile.getMonitors().newInstance();

        LOG.debug("Loading execution lock feature");
        ExecutionLockProvider locks = profile.getLocks().newInstance();

        LOG.debug("Loading job scheduling feature");
        JobScheduler scheduler = profile.getScheduler().newInstance();

        LOG.debug("Loading hadoop execution feature");
        HadoopScriptHandler hadoopHandler = profile.getHadoopHandler().newInstance();

        LOG.debug("Loading command execution features");
        Map<String, CommandScriptHandler> commandHandlers = new HashMap<>();
        for (Map.Entry<String, ServiceProfile<CommandScriptHandler>> entry
                : profile.getCommandHandlers().entrySet()) {
            commandHandlers.put(entry.getKey(), entry.getValue().newInstance());
        }

        LOG.debug("Extracting batch script");
        BatchScript batch = BatchScript.load(script);

        ExecutionTask result = new ExecutionTask(
                monitors,
                locks,
                scheduler,
                hadoopHandler,
                commandHandlers,
                batch,
                batchArguments,
                extensions);

        LOG.debug("Applying definitions");
        Map<String, String> copyDefinitions = new TreeMap<>(yaessArguments);
        consumeRuntimeContext(result, copyDefinitions, batch);
        consumeSkipFlows(result, copyDefinitions, batch);
        consumeSerializeFlows(result, copyDefinitions, batch);
        checkRest(copyDefinitions);

        return result;
    }

    private static void consumeRuntimeContext(
            ExecutionTask result,
            Map<String, String> copyDefinitions,
            BatchScript script) {
        assert result != null;
        assert copyDefinitions != null;
        assert script != null;
        RuntimeContext rc = RuntimeContext.get().batchId(script.getId()).buildId(script.getBuildId());

        Ternary dryRunResult = consumeBoolean(copyDefinitions, KEY_VERIFY_DRYRUN);
        if (dryRunResult == Ternary.TRUE) {
            rc = rc.mode(ExecutionMode.SIMULATION);
        } else if (dryRunResult == Ternary.FALSE) {
            rc = rc.mode(ExecutionMode.PRODUCTION);
        }

        Ternary verify = consumeBoolean(copyDefinitions, KEY_VERIFY_APPLICATION);
        if (verify == Ternary.FALSE) {
            rc = rc.buildId(null);
        }

        result.runtimeContext = rc;
        result.getEnv().putAll(rc.unapply());
    }

    private static void consumeSkipFlows(
            ExecutionTask task,
            Map<String, String> copyDefinitions,
            BatchScript script) {
        assert task != null;
        assert copyDefinitions != null;
        assert script != null;
        String flows = copyDefinitions.remove(KEY_SKIP_FLOWS);
        if (flows == null || flows.trim().isEmpty()) {
            return;
        }
        LOG.debug("Definition: {}={}", KEY_SKIP_FLOWS, flows);
        for (String flowIdCandidate : flows.split(",")) {
            String flowId = flowIdCandidate.trim();
            if (flowId.isEmpty() == false) {
                FlowScript flow = script.findFlow(flowId);
                if (flow == null) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Unknown flowId in definition {0} : {1}",
                            KEY_SKIP_FLOWS,
                            flowId));
                }
                task.skipFlows.add(flowId);
            }
        }
    }

    private static void consumeSerializeFlows(
            ExecutionTask task,
            Map<String, String> copyDefinitions,
            BatchScript script) {
        assert task != null;
        assert copyDefinitions != null;
        assert script != null;

        Ternary serialize = consumeBoolean(copyDefinitions, KEY_SERIALIZE_FLOWS);
        if (serialize == null) {
            return;
        }
        task.serializeFlows = serialize == Ternary.TRUE;
    }

    private static Ternary consumeBoolean(Map<String, String> copyDefinitions, String key) {
        assert copyDefinitions != null;
        String value = copyDefinitions.remove(key);
        if (value == null) {
            return Ternary.UNDEF;
        }
        value = value.trim();
        LOG.debug("Definition: {}={}", key, value);
        if (value.equalsIgnoreCase("true")) {
            return Ternary.TRUE;
        } else if (value.equalsIgnoreCase("false")) {
            return Ternary.FALSE;
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid option value in definition {0} : {1}",
                    key,
                    value));
        }
    }

    private static void checkRest(Map<String, String> copyDefinitions) {
        assert copyDefinitions != null;
        if (copyDefinitions.isEmpty() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unknown definitions: {0}",
                    copyDefinitions.keySet()));
        }
    }

    /**
     * Returns the view of flow IDs which should be skipped.
     * @return the view of skip targets
     * @since 0.2.6
     */
    Set<String> getSkipFlows() {
        return skipFlows;
    }

    /**
     * Sets whether the flow execution was serialized in {@link #executeBatch(String)}.
     * @param serialize {@code true} to serialize, otherwise {@code false}
     * @since 0.2.6
     */
    void setSerializeFlows(boolean serialize) {
        this.serializeFlows = serialize;
    }

    /**
     * Sets runtime context.
     * Note that the
     * @param runtimeContext the runtime context
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    void setRuntimeContext(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    /**
     * Returns subprocesses' environment variables.
     * @return subprocesses' environment variables
     * @since 0.4.0
     */
    Map<String, String> getEnv() {
        return this.environmentVaritables;
    }

    /**
     * Executes a target flow.
     * If execution is failed except on {@link ExecutionPhase#SETUP setup}, {@link ExecutionPhase#FINALIZE finalize},
     * or {@link ExecutionPhase#CLEANUP cleanup}, then the {@code finalize} phase will be executed for recovery
     * before the target flow execution is aborted.
     * Execution ID for each flow will automatically generated.
     * @param batchId target batch ID
     * @throws InterruptedException if interrupted during this execution
     * @throws IOException if failed to execute target batch
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void executeBatch(String batchId) throws InterruptedException, IOException {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        ExecutorService executor = createJobflowExecutor(batchId);
        YSLOG.info("I01000", batchId);
        long start = System.currentTimeMillis();
        try {
            try (ExecutionLock lock = acquireExecutionLock(batchId)) {
                BatchScheduler batchScheduler = new BatchScheduler(batchId, script, lock, executor);
                batchScheduler.run();
            }
            YSLOG.info("I01001", batchId);
        } catch (ExitCodeException e) {
            YSLOG.error("E01001", batchId);
            throw e;
        } catch (IOException e) {
            YSLOG.error(e, "E01001", batchId);
            throw e;
        } catch (InterruptedException e) {
            YSLOG.warn(e, "W01001", batchId);
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            YSLOG.info("I01999", batchId, end - start);
        }
    }

    private ExecutionLock acquireExecutionLock(String batchId) throws IOException {
        assert batchId != null;
        if (runtimeContext.canExecute(locks)) {
            return locks.newInstance(batchId);
        } else {
            return ExecutionLock.NULL;
        }
    }

    private PhaseMonitor obtainPhaseMonitor(ExecutionContext context) throws InterruptedException, IOException {
        assert context != null;
        if (runtimeContext.canExecute(monitors)) {
            return monitors.newInstance(context);
        } else {
            return PhaseMonitor.NULL;
        }
    }

    private ExecutorService createJobflowExecutor(String batchId) {
        assert batchId != null;
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName(MessageFormat.format(
                    "JobflowExecutor-{0}",
                    batchId));
            thread.setDaemon(true);
            return thread;
        };
        if (serializeFlows) {
            return Executors.newFixedThreadPool(1, threadFactory);
        } else {
            return Executors.newCachedThreadPool(threadFactory);
        }
    }

    /**
     * Executes a target flow.
     * If execution is failed except on {@link ExecutionPhase#SETUP setup}, {@link ExecutionPhase#FINALIZE finalize},
     * or {@link ExecutionPhase#CLEANUP cleanup}, then the {@code finalize} phase will be executed for recovery
     * before execution is aborted.
     * @param batchId target batch ID
     * @param flowId target flow ID
     * @param executionId target execution ID
     * @throws InterruptedException if interrupted during this execution
     * @throws IOException if failed to execute target flow
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void executeFlow(
            String batchId,
            String flowId,
            String executionId) throws InterruptedException, IOException {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        FlowScript flow = script.findFlow(flowId);
        if (flow == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Flow is undefined: batchId={0}, flowId={1}, executionId={2}",
                    batchId,
                    flowId,
                    executionId));
        }
        try (ExecutionLock lock = acquireExecutionLock(batchId)) {
            lock.beginFlow(flowId, executionId);
            executeFlow(batchId, flow, executionId);
            lock.endFlow(flowId, executionId);
        }
    }

    /**
     * Executes a target phase.
     * @param batchId target batch ID
     * @param flowId target flow ID
     * @param executionId target execution ID
     * @param phase target phase
     * @throws InterruptedException if interrupted during this execution
     * @throws IOException if failed to execute target phase
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void executePhase(
            String batchId,
            String flowId,
            String executionId,
            ExecutionPhase phase) throws InterruptedException, IOException {
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
        ExecutionContext context = newContext(batchId, flowId, executionId, phase);
        executePhase(context);
    }

    private ExecutionContext newContext(String batchId, String flowId, String executionId, ExecutionPhase phase) {
        return new ExecutionContext(
                batchId, flowId, executionId, phase,
                batchArguments, environmentVaritables, extensions);
    }

    /**
     * Executes a target phase.
     * @param context the current context
     * @throws InterruptedException if interrupted during this execution
     * @throws IOException if failed to execute target phase
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public void executePhase(ExecutionContext context) throws InterruptedException, IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        FlowScript flow = script.findFlow(context.getFlowId());
        if (flow == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Flow is undefined: batchId={0}, flowId={1}, executionId={2}",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId()));
        }
        Set<ExecutionScript> executions = flow.getScripts().get(context.getPhase());
        try (ExecutionLock lock = acquireExecutionLock(context.getBatchId())) {
            lock.beginFlow(context.getFlowId(), context.getExecutionId());
            executePhase(context, executions, flow.getEnabledScriptKinds());
            lock.endFlow(context.getFlowId(), context.getExecutionId());
        }
    }

    void executeFlow(String batchId, FlowScript flow, String executionId) throws InterruptedException, IOException {
        assert batchId != null;
        assert flow != null;
        assert executionId != null;
        YSLOG.info("I02000", batchId, flow.getId(), executionId);
        long start = System.currentTimeMillis();
        try {
            if (skipFlows.contains(flow.getId())) {
                YSLOG.info("I02002", batchId, flow.getId(), executionId);
                return;
            }
            executePhase(batchId, flow, executionId, ExecutionPhase.SETUP);
            boolean succeed = false;
            try {
                executePhase(batchId, flow, executionId, ExecutionPhase.INITIALIZE);
                executePhase(batchId, flow, executionId, ExecutionPhase.IMPORT);
                executePhase(batchId, flow, executionId, ExecutionPhase.PROLOGUE);
                executePhase(batchId, flow, executionId, ExecutionPhase.MAIN);
                executePhase(batchId, flow, executionId, ExecutionPhase.EPILOGUE);
                executePhase(batchId, flow, executionId, ExecutionPhase.EXPORT);
                succeed = true;
            } finally {
                if (succeed) {
                    executePhase(batchId, flow, executionId, ExecutionPhase.FINALIZE);
                } else {
                    YSLOG.info("I02003", batchId, flow.getId(), executionId);
                    try {
                        executePhase(batchId, flow, executionId, ExecutionPhase.FINALIZE);
                    } catch (Exception e) {
                        YSLOG.warn(e, "W02002", batchId, flow.getId(), executionId);
                    }
                }
            }
            try {
                executePhase(batchId, flow, executionId, ExecutionPhase.CLEANUP);
            } catch (Exception e) {
                YSLOG.warn(e, "W02003", batchId, flow.getId(), executionId);
            }
            YSLOG.info("I02001", batchId, flow.getId(), executionId);
        } catch (ExitCodeException e) {
            YSLOG.error("E02001", batchId, flow.getId(), executionId);
            throw e;
        } catch (IOException e) {
            YSLOG.error(e, "E02001", batchId, flow.getId(), executionId);
            throw e;
        } catch (InterruptedException e) {
            YSLOG.warn(e, "W02001", batchId, flow.getId(), executionId);
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            YSLOG.info("I02999", batchId, flow.getId(), executionId, end - start);
        }
    }

    private void executePhase(
            String batchId,
            FlowScript flow,
            String executionId,
            ExecutionPhase phase) throws InterruptedException, IOException {
        ExecutionContext context = newContext(batchId, flow.getId(), executionId, phase);
        Set<ExecutionScript> scripts = flow.getScripts().get(phase);
        assert scripts != null;
        executePhase(context, scripts, flow.getEnabledScriptKinds());
    }

    private void executePhase(
            ExecutionContext context,
            Set<ExecutionScript> executions,
            Set<ExecutionScript.Kind> enables) throws InterruptedException, IOException {
        assert context != null;
        assert executions != null;
        YSLOG.info("I03000",
                context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase());
        long start = System.currentTimeMillis();
        try {
            if (skipFlows.contains(context.getFlowId())) {
                YSLOG.info("I03002",
                        context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase());
                return;
            }
            List<? extends Job> jobs;
            ErrorHandler handler;
            switch (context.getPhase()) {
            case SETUP:
                jobs = buildSetupJobs(context, enables);
                handler = JobScheduler.STRICT;
                break;
            case CLEANUP:
                jobs = buildCleanupJobs(context, enables);
                handler = JobScheduler.BEST_EFFORT;
                break;
            case FINALIZE:
                jobs = buildExecutionJobs(context, executions, enables);
                handler = JobScheduler.BEST_EFFORT;
                break;
            default:
                jobs = buildExecutionJobs(context, executions, enables);
                handler = JobScheduler.STRICT;
                break;
            }
            try (PhaseMonitor monitor = obtainPhaseMonitor(context)) {
                scheduler.execute(monitor, context, jobs, handler);
            }
            YSLOG.info("I03001",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase());
        } catch (ExitCodeException e) {
            YSLOG.error("E03001",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase());
            throw e;
        } catch (IOException e) {
            YSLOG.error(e, "E03001",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase());
            throw e;
        } catch (InterruptedException e) {
            YSLOG.warn(e, "W03001",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase());
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            YSLOG.info("I03999",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase(),
                    end - start);
        }
    }

    private List<SetupJob> buildSetupJobs(ExecutionContext context, Set<ExecutionScript.Kind> enables) {
        assert context != null;
        List<SetupJob> results = new ArrayList<>();
        if (enables.contains(ExecutionScript.Kind.HADOOP)) {
            results.add(new SetupJob(hadoopHandler));
        }
        if (enables.contains(ExecutionScript.Kind.COMMAND)) {
            for (CommandScriptHandler commandHandler : commandHandlers.values()) {
                results.add(new SetupJob(commandHandler));
            }
        }
        return results;
    }

    private List<CleanupJob> buildCleanupJobs(ExecutionContext context, Set<ExecutionScript.Kind> enables) {
        assert context != null;
        List<CleanupJob> results = new ArrayList<>();
        if (enables.contains(ExecutionScript.Kind.HADOOP)) {
            results.add(new CleanupJob(hadoopHandler));
        }
        if (enables.contains(ExecutionScript.Kind.COMMAND)) {
            for (CommandScriptHandler commandHandler : commandHandlers.values()) {
                results.add(new CleanupJob(commandHandler));
            }
        }
        return results;
    }

    private List<ScriptJob<?>> buildExecutionJobs(
            ExecutionContext context,
            Set<ExecutionScript> executions,
            Set<ExecutionScript.Kind> enables) throws IOException, InterruptedException {
        assert context != null;
        assert executions != null;
        List<ScriptJob<?>> results = new ArrayList<>();
        for (ExecutionScript execution : executions) {
            if (enables.contains(execution.getKind()) == false) {
                throw new IllegalStateException(MessageFormat.format(
                        "job kind {2} is not enabled in this flow (batch={0}, flow={1})",
                        context.getBatchId(),
                        context.getFlowId(),
                        execution.getKind()));
            }
            switch (execution.getKind()) {
            case COMMAND: {
                CommandScript exec = (CommandScript) execution;
                String profileName = exec.getProfileName();
                CommandScriptHandler handler = profileName == null ? null : commandHandlers.get(profileName);
                if (handler == null) {
                    LOG.debug("Profile {} is not defined in comand script handlers, try wildcard: {}",
                            profileName,
                            exec.getId());
                    handler = commandHandlers.get(CommandScriptHandler.PROFILE_WILDCARD);
                }
                if (handler == null) {
                    throw new IOException(MessageFormat.format(
                            "Profile \"{5}\" is not defined (batch={0}, flow={1}, phase={2}, module={3}, id={4})",
                            context.getBatchId(),
                            context.getFlowId(),
                            context.getPhase().getSymbol(),
                            exec.getModuleName(),
                            exec.getId(),
                            profileName));
                }
                results.add(new ScriptJob<>(exec.resolve(context, handler), handler));
                break;
            }
            case HADOOP: {
                HadoopScript exec = (HadoopScript) execution;
                results.add(new ScriptJob<>(exec.resolve(context, hadoopHandler), hadoopHandler));
                break;
            }
            default:
                throw new AssertionError(MessageFormat.format(
                        "Unknown execution script: {0}",
                        execution));
            }
        }
        return results;
    }

    private class BatchScheduler {

        final String batchId;

        final LinkedList<FlowScript> flows;

        final ExecutionLock lock;

        final ExecutorService executor;

        final Map<String, FlowScriptTask> running;

        final Set<String> blocking;

        final BlockingQueue<FlowScriptTask> doneQueue;

        BatchScheduler(String batchId, BatchScript batchScript, ExecutionLock lock, ExecutorService executor) {
            assert batchId != null;
            assert batchScript != null;
            assert lock != null;
            assert executor != null;
            this.batchId = batchId;
            this.flows = new LinkedList<>(batchScript.getAllFlows());
            this.lock = lock;
            this.executor = executor;
            this.running = new HashMap<>();
            this.blocking = new HashSet<>();
            for (FlowScript flow : flows) {
                blocking.add(flow.getId());
            }
            this.doneQueue = new LinkedBlockingQueue<>();
        }

        public void run() throws InterruptedException, IOException {
            try {
                while (flows.isEmpty() == false) {
                    boolean submitted = submit();
                    if (submitted == false) {
                        if (running.isEmpty()) {
                            throw new IOException(MessageFormat.format(
                                    "Deadlock was detected in \"{0}\": {1}",
                                    batchId,
                                    blocking));
                        }
                        waitForComplete();
                    }
                }
                while (running.isEmpty() == false) {
                    waitForComplete();
                }
            } finally {
                YSLOG.info("I01004", batchId);
                for (FlowScriptTask task : running.values()) {
                    task.cancel(true);
                }
                while (running.isEmpty() == false) {
                    try {
                        waitForComplete();
                    } catch (IOException e) {
                        YSLOG.warn(e, "W01002", batchId);
                    }
                }
            }
        }

        private boolean submit() {
            LOG.debug("Submitting waiting jobflows: {}", batchId);
            boolean submitted = false;
            for (Iterator<FlowScript> iter = flows.iterator(); iter.hasNext();) {
                FlowScript flow = iter.next();
                boolean blocked = false;
                for (String blockerId : flow.getBlockerIds()) {
                    if (blocking.contains(blockerId)) {
                        blocked = true;
                        break;
                    }
                }
                if (blocked == false) {
                    submit(flow);
                    iter.remove();
                    submitted = true;
                }
            }
            return submitted;
        }

        private void submit(FlowScript flow) {
            LOG.debug("Submitting jobflow \"{}\": {}", flow.getId(), batchId);
            FlowScriptTask task = new FlowScriptTask(flow, doneQueue, new Callable<Void>() {
                @Override
                public Void call() throws InterruptedException, IOException {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    String executionId = UUID.randomUUID().toString();
                    LOG.debug("Generated execution ID for \"{}\": {}", flow.getId(), executionId);
                    lock.beginFlow(flow.getId(), executionId);
                    executeFlow(batchId, flow, executionId);
                    lock.endFlow(flow.getId(), executionId);
                    LOG.debug("Completing jobflow \"{}\": {}", flow.getId(), batchId);
                    return null;
                }
            });
            YSLOG.info("I01003", batchId, flow.getId());
            executor.execute(task);
            running.put(flow.getId(), task);
        }

        private void waitForComplete() throws InterruptedException, IOException {
            LOG.debug("Waiting for running jobflows complete: {}", batchId);
            FlowScriptTask done = doneQueue.take();
            assert done.isDone();
            FlowScript flow = done.script;
            try {
                done.get();
                boolean blocked = blocking.remove(flow.getId());
                assert blocked;
            } catch (CancellationException e) {
                YSLOG.info(e, "I01005", batchId, flow.getId());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else if (e.getCause() instanceof InterruptedException) {
                    throw (InterruptedException) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                } else {
                    throw new IOException("Flow execution failed by unknown error", e);
                }
            } finally {
                FlowScriptTask ran = running.remove(flow.getId());
                assert ran != null;
            }
        }
    }

    private static class FlowScriptTask extends FutureTask<Void> {

        final FlowScript script;

        private final BlockingQueue<FlowScriptTask> doneQueue;

        FlowScriptTask(FlowScript script, BlockingQueue<FlowScriptTask> doneQueue, Callable<Void> callable) {
            super(callable);
            assert script != null;
            assert doneQueue != null;
            this.script = script;
            this.doneQueue = doneQueue;
        }

        @Override
        protected void done() {
            try {
                doneQueue.put(this);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        }
    }

    private enum Ternary {

        TRUE,

        FALSE,

        UNDEF,
    }
}
