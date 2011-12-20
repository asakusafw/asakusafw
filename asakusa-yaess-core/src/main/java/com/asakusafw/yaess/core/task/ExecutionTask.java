/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.BatchScript;
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
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.JobScheduler.ErrorHandler;
import com.asakusafw.yaess.core.YaessProfile;

/**
 * Task to execute target batch, flow, or phase.
 * @since 0.2.3
 */
public class ExecutionTask {

    static final Logger LOG = LoggerFactory.getLogger(ExecutionTask.class);

    private final ExecutionMonitorProvider monitors;

    private final ExecutionLockProvider locks;

    private final JobScheduler scheduler;

    private final HadoopScriptHandler hadoopHandler;

    private final Map<String, CommandScriptHandler> commandHandlers;

    private final Properties script;

    private final Map<String, String> arguments;

    /**
     * Creates a new instance.
     * @param monitors monitor provider
     * @param locks lock provider
     * @param scheduler job scheduler
     * @param hadoopHandler Hadoop script handler
     * @param commandHandlers command script handlers and its profile names
     * @param script target script
     * @param arguments current arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExecutionTask(
            ExecutionMonitorProvider monitors,
            ExecutionLockProvider locks,
            JobScheduler scheduler,
            HadoopScriptHandler hadoopHandler,
            Map<String, CommandScriptHandler> commandHandlers,
            Properties script,
            Map<String, String> arguments) {
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
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.monitors = monitors;
        this.locks = locks;
        this.scheduler = scheduler;
        this.hadoopHandler = hadoopHandler;
        this.commandHandlers = Collections.unmodifiableMap(new HashMap<String, CommandScriptHandler>(commandHandlers));
        this.script = script;
        this.arguments = Collections.unmodifiableMap(new LinkedHashMap<String, String>(arguments));
    }

    /**
     * Loads profile and create a new {@link ExecutionTask}.
     * @param profile target profile
     * @param script target script
     * @param arguments execution arguments
     * @return the created task
     * @throws InterruptedException if interrupted while configuring services
     * @throws IOException if failed to configure services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ExecutionTask load(
            YaessProfile profile,
            Properties script,
            Map<String, String> arguments) throws InterruptedException, IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
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
        Map<String, CommandScriptHandler> commandHandlers = new HashMap<String, CommandScriptHandler>();
        for (Map.Entry<String, ServiceProfile<CommandScriptHandler>> entry
                : profile.getCommandHandlers().entrySet()) {
            commandHandlers.put(entry.getKey(), entry.getValue().newInstance());
        }
        return new ExecutionTask(monitors, locks, scheduler, hadoopHandler, commandHandlers, script, arguments);
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
    public void executeBatch(final String batchId) throws InterruptedException, IOException {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(MessageFormat.format(
                        "JobflowExecutor-{0}",
                        batchId));
                return thread;
            }
        });
        BatchScript batchScript = BatchScript.load(script);
        ExecutionLock lock = locks.newInstance(batchId);
        try {
            BatchScheduler batchScheduler = new BatchScheduler(batchId, batchScript, lock, executor);
            batchScheduler.run();
        } finally {
            lock.close();
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
        FlowScript flow = FlowScript.load(script, flowId);
        ExecutionLock lock = locks.newInstance(batchId);
        try {
            lock.beginFlow(flowId, executionId);
            executeFlow(batchId, flow, executionId);
            lock.endFlow(flowId, executionId);
        } finally {
            lock.close();
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
        ExecutionContext context = new ExecutionContext(batchId, flowId, executionId, phase, arguments);
        Set<ExecutionScript> executions = FlowScript.load(script, flowId, phase);
        ExecutionLock lock = locks.newInstance(batchId);
        try {
            lock.beginFlow(flowId, executionId);
            executePhase(context, executions);
            lock.endFlow(flowId, executionId);
        } finally {
            lock.close();
        }
    }

    void executeFlow(String batchId, FlowScript flow, String executionId) throws InterruptedException, IOException {
        assert batchId != null;
        assert flow != null;
        assert executionId != null;
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
                try {
                    executePhase(batchId, flow, executionId, ExecutionPhase.FINALIZE);
                } catch (Exception e) {
                    // TODO logging WARN
                    LOG.warn("Finalize failed", e);
                }
            }
        }
        try {
            executePhase(batchId, flow, executionId, ExecutionPhase.CLEANUP);
        } catch (Exception e) {
            // TODO logging WARN
            LOG.warn("Cleanup failed", e);
        }
    }

    private void executePhase(
            String batchId,
            FlowScript flow,
            String executionId,
            ExecutionPhase phase) throws InterruptedException, IOException {
        ExecutionContext context = new ExecutionContext(batchId, flow.getId(), executionId, phase, arguments);
        Set<ExecutionScript> scripts = flow.getScripts().get(phase);
        assert scripts != null;
        executePhase(context, scripts);
    }

    private void executePhase(
            ExecutionContext context,
            Set<ExecutionScript> executions) throws InterruptedException, IOException {
        assert context != null;
                assert executions != null;
        List<? extends Job> jobs;
        ErrorHandler handler;
        switch (context.getPhase()) {
        case SETUP:
            jobs = buildSetupJobs(context);
            handler = JobScheduler.STRICT;
            break;
        case CLEANUP:
            jobs = buildCleanupJobs(context);
            handler = JobScheduler.BEST_EFFORT;
            break;
        case FINALIZE:
            jobs = buildExecutionJobs(context, executions);
            handler = JobScheduler.BEST_EFFORT;
            break;
        default:
            jobs = buildExecutionJobs(context, executions);
            handler = JobScheduler.STRICT;
            break;
        }
        PhaseMonitor monitor = monitors.newInstance(context);
        try {
            scheduler.execute(monitor, context, jobs, handler);
        } finally {
            monitor.close();
        }
    }

    private List<SetupJob> buildSetupJobs(ExecutionContext context) {
        assert context != null;
        List<SetupJob> results = new ArrayList<SetupJob>();
        results.add(new SetupJob(hadoopHandler));
        for (CommandScriptHandler commandHandler : commandHandlers.values()) {
            results.add(new SetupJob(commandHandler));
        }
        return results;
    }

    private List<CleanupJob> buildCleanupJobs(ExecutionContext context) {
        assert context != null;
        List<CleanupJob> results = new ArrayList<CleanupJob>();
        results.add(new CleanupJob(hadoopHandler));
        for (CommandScriptHandler commandHandler : commandHandlers.values()) {
            results.add(new CleanupJob(commandHandler));
        }
        return results;
    }

    private List<ScriptJob<?>> buildExecutionJobs(
            ExecutionContext context,
            Set<ExecutionScript> executions) throws IOException, InterruptedException {
        assert context != null;
        assert executions != null;
        List<ScriptJob<?>> results = new ArrayList<ScriptJob<?>>();
        for (ExecutionScript execution : executions) {
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
                results.add(new ScriptJob<CommandScript>(exec.resolve(context, handler), handler));
                break;
            }
            case HADOOP: {
                HadoopScript exec = (HadoopScript) execution;
                results.add(new ScriptJob<HadoopScript>(exec.resolve(context, hadoopHandler), hadoopHandler));
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
            this.flows = new LinkedList<FlowScript>(batchScript.getAllFlows());
            this.lock = lock;
            this.executor = executor;
            this.running = new HashMap<String, FlowScriptTask>();
            this.blocking = new HashSet<String>();
            for (FlowScript flow : flows) {
                blocking.add(flow.getId());
            }
            this.doneQueue = new LinkedBlockingQueue<FlowScriptTask>();
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
                for (FlowScriptTask task : running.values()) {
                    task.cancel(true);
                }
                while (running.isEmpty() == false) {
                    try {
                        waitForComplete();
                    } catch (IOException e) {
                        // TODO logging
                        LOG.warn("Error has occurred while shutting down", e);
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

        private void submit(final FlowScript flow) {
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
                LOG.info(MessageFormat.format(
                        "Flow execution is cancelled (batch={0}, flow={1})",
                        flow.getId()), e);
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
}
