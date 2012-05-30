/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.jobqueue;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.basic.ExitCodeException;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandlerBase;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.YaessLogger;
import com.asakusafw.yaess.jobqueue.client.JobClient;
import com.asakusafw.yaess.jobqueue.client.JobId;
import com.asakusafw.yaess.jobqueue.client.JobScript;
import com.asakusafw.yaess.jobqueue.client.JobStatus;

/**
 * An implementation of {@link HadoopScript} using Job Queue client.
 * @since 0.2.6
 */
public class QueueHadoopScriptHandler extends ExecutionScriptHandlerBase implements HadoopScriptHandler {

    /**
     * This is a copy from asakusa-runtime.
     */
    static final String CLEANUP_STAGE_CLASS = "com.asakusafw.runtime.stage.CleanupStageClient";

    static final YaessLogger YSLOG = new YaessJobQueueLogger(QueueHadoopScriptHandler.class);

    static final Logger LOG = LoggerFactory.getLogger(QueueHadoopScriptHandler.class);

    private static final ExecutorService TIMEOUT_THREAD = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, String.format("job-requestor-%04d", counter.incrementAndGet()));
            thread.setDaemon(true);
            return thread;
        }
    });

    private volatile JobClientProvider clients;

    private volatile long timeout;

    private volatile long pollingInterval;

    @Override
    protected void doConfigure(
            ServiceProfile<?> profile,
            Map<String, String> desiredProperties,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException {
        if (desiredEnvironmentVariables.isEmpty() == false) {
            throw new IOException(MessageFormat.format(
                    "{0} can not have environment variables: {1}.{2} = {3}",
                    getClass().getName(),
                    profile.getPrefix(),
                    KEY_ENV_PREFIX,
                    desiredEnvironmentVariables));
        }

        desiredEnvironmentVariables.put(ExecutionScript.ENV_ASAKUSA_HOME, "DUMMY");
        JobClientProfile p;
        try {
            p = JobClientProfile.convert(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure a hadoop script handler: {0}",
                    profile.getPrefix()), e);
        }
        doConfigure(p);
    }

    void doConfigure(JobClientProfile p) {
        this.timeout = p.getTimeout();
        this.pollingInterval = p.getPollingInterval();
        this.clients = new JobClientProvider(p.getClients());
    }

    @Override
    public void execute(
            ExecutionMonitor monitor,
            ExecutionContext context,
            HadoopScript script) throws InterruptedException, IOException {
        run(monitor, context, script);
    }

    @Override
    public void cleanUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        HadoopScript script = new HadoopScript(
                context.getPhase().getSymbol(),
                Collections.<String>emptySet(),
                CLEANUP_STAGE_CLASS,
                Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap());
        run(monitor, context, script);
    }

    private void run(
            ExecutionMonitor monitor,
            ExecutionContext context,
            HadoopScript script) throws InterruptedException, IOException {
        assert monitor != null;
        assert context != null;
        assert script != null;
        monitor.open(100);
        try {
            monitor.checkCancelled();
            JobInfo info = registerScript(monitor, context, script);
            monitor.progressed(5);

            monitor.checkCancelled();
            submitScript(context, info);
            monitor.progressed(5);

            monitor.checkCancelled();
            YSLOG.info("I01005",
                    info.script.getBatchId(),
                    info.script.getFlowId(),
                    info.script.getPhase(),
                    info.script.getExecutionId(),
                    info.script.getStageId(),
                    info.client,
                    info.id);
            long start = System.currentTimeMillis();
            try {
                JobStatus.Kind lastKind = JobStatus.Kind.INITIALIZED;
                while (true) {
                    JobStatus status = poll(context, info);
                    JobStatus.Kind currentKind = status.getKind();
                    if (lastKind.compareTo(currentKind) < 0) {
                        // progressed
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Job status was changed: {} -> {} ({})", new Object[] {
                                    lastKind,
                                    currentKind,
                                    info,
                            });
                        }
                        switch (currentKind) {
                        case WAITING:
                            break;
                        case RUNNING:
                            monitor.progressed(10);
                            break;
                        case COMPLETED:
                        case ERROR:
                            checkError(context, info, status);
                            return;
                        default:
                            throw new AssertionError(currentKind);
                        }
                    }
                    lastKind = currentKind;
                    monitor.checkCancelled();
                    Thread.sleep(pollingInterval);
                }
            } finally {
                long end = System.currentTimeMillis();
                YSLOG.info("I01006",
                        info.script.getBatchId(),
                        info.script.getFlowId(),
                        info.script.getPhase(),
                        info.script.getExecutionId(),
                        info.script.getStageId(),
                        info.client,
                        info.id,
                        end - start);
            }
        } finally {
            monitor.close();
        }
    }

    private JobInfo registerScript(
            ExecutionMonitor monitor,
            ExecutionContext context,
            HadoopScript script) throws InterruptedException, IOException {
        assert monitor != null;
        assert context != null;
        assert script != null;
        JobScript job = convert(context, script);
        for (int i = 1, n = clients.count() * 2; i <= n; i++) {
            monitor.checkCancelled();
            JobClient client = clients.get();
            try {
                JobId id = registerWithTimeout(job, client);
                return new JobInfo(job, id, client);
            } catch (IOException e) {
                clients.setError(client);
                YSLOG.warn(e, "W01001",
                        job.getBatchId(),
                        job.getFlowId(),
                        job.getPhase(),
                        job.getExecutionId(),
                        job.getStageId(),
                        client);
            }
        }
        YSLOG.warn("E01001",
                job.getBatchId(),
                job.getFlowId(),
                job.getPhase(),
                job.getExecutionId(),
                job.getStageId(),
                clients.count());
        throw new IOException(MessageFormat.format(
                "Failed to request Hadoop job "
                + "(batchId={0}, flowId={1}, phase={2}, stageId={4}, executionId={3})",
                job.getBatchId(),
                job.getFlowId(),
                job.getPhase(),
                job.getExecutionId(),
                job.getStageId()));
    }

    private JobId registerWithTimeout(
            final JobScript job,
            final JobClient client) throws IOException, InterruptedException {
        assert job != null;
        assert client != null;
        Future<JobId> future = TIMEOUT_THREAD.submit(new Callable<JobId>() {
            @Override
            public JobId call() throws Exception {
                YSLOG.info("I01001",
                        job.getBatchId(),
                        job.getFlowId(),
                        job.getPhase(),
                        job.getExecutionId(),
                        job.getStageId(),
                        client);
                long start = System.currentTimeMillis();
                JobId id = client.register(job);
                long end = System.currentTimeMillis();
                YSLOG.info("I01002",
                        job.getBatchId(),
                        job.getFlowId(),
                        job.getPhase(),
                        job.getExecutionId(),
                        job.getStageId(),
                        client,
                        id,
                        end - start);
                return id;
            }
        });
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            } else if (cause instanceof CancellationException) {
                throw (CancellationException) cause;
            } else {
                throw new IOException(MessageFormat.format(
                        "Request was failed cause of unknown exception: {5} "
                        + "(batchId={0}, flowId={1}, phase={2}, stageId={4}, executionId={3})",
                        job.getBatchId(),
                        job.getFlowId(),
                        job.getPhase(),
                        job.getExecutionId(),
                        job.getStageId(),
                        client), cause);
            }
        } catch (TimeoutException e) {
            throw new IOException(MessageFormat.format(
                    "Request was timeout: {5} "
                    + "(batchId={0}, flowId={1}, phase={2}, stageId={4}, executionId={3})",
                    job.getBatchId(),
                    job.getFlowId(),
                    job.getPhase(),
                    job.getExecutionId(),
                    job.getStageId(),
                    client), e);
        }
    }

    private JobScript convert(ExecutionContext context, HadoopScript script) throws InterruptedException, IOException {
        assert context != null;
        assert script != null;
        JobScript result = new JobScript();
        result.setBatchId(context.getBatchId());
        result.setFlowId(context.getFlowId());
        result.setExecutionId(context.getExecutionId());
        result.setPhase(context.getPhase());
        result.setArguments(new HashMap<String, String>(context.getArguments()));
        result.setStageId(script.getId());
        result.setMainClassName(script.getClassName());
        result.setProperties(merge(getProperties(context, script), script.getHadoopProperties()));
        result.setEnvironmentVariables(new HashMap<String, String>(script.getEnvironmentVariables()));
        return result;
    }

    private Map<String, String> merge(Map<String, String> base, Map<String, String> override) {
        assert base != null;
        assert override != null;
        Map<String, String> results = new TreeMap<String, String>();
        results.putAll(base);
        results.putAll(override);
        return results;
    }

    private void submitScript(ExecutionContext context, JobInfo info) throws IOException, InterruptedException {
        assert context != null;
        assert info != null;
        YSLOG.info("I01003",
                info.script.getBatchId(),
                info.script.getFlowId(),
                info.script.getPhase(),
                info.script.getExecutionId(),
                info.script.getStageId(),
                info.client,
                info.id);
        long start = System.currentTimeMillis();
        try {
            info.client.submit(info.id);
        } catch (IOException e) {
            YSLOG.error(e, "E01002",
                    info.script.getBatchId(),
                    info.script.getFlowId(),
                    info.script.getPhase(),
                    info.script.getExecutionId(),
                    info.script.getStageId(),
                    info.client,
                    info.id);
            throw e;
        }
        long end = System.currentTimeMillis();
        YSLOG.info("I01004",
                info.script.getBatchId(),
                info.script.getFlowId(),
                info.script.getPhase(),
                info.script.getExecutionId(),
                info.script.getStageId(),
                info.client,
                info.id,
                end - start);
    }

    private JobStatus poll(ExecutionContext context, JobInfo info) throws IOException, InterruptedException {
        assert context != null;
        assert info != null;
        try {
            return info.client.getStatus(info.id);
        } catch (IOException e) {
            YSLOG.error(e, "E01003",
                    info.script.getBatchId(),
                    info.script.getFlowId(),
                    info.script.getPhase(),
                    info.script.getExecutionId(),
                    info.script.getStageId(),
                    info.client,
                    info.id);
            throw e;
        }
    }

    private void checkError(ExecutionContext context, JobInfo info, JobStatus status) throws IOException {
        assert context != null;
        assert info != null;
        assert status != null;
        JobStatus.Kind kind = status.getKind();
        switch (kind) {
        case COMPLETED:
            if (status.getExitCode() != 0) {
                throw new ExitCodeException(MessageFormat.format(
                        "Unexpected exit code from Hadoop job: code={1} ({0})",
                        info,
                        String.valueOf(status.getExitCode())), status.getExitCode());
            }
            return;
        case ERROR:
            throw new IOException(MessageFormat.format(
                    "{1}: ({0})",
                    info,
                    status.getErrorMessage() == null
                        ? status.getErrorCode() == null
                            ? "Unknown error"
                            : MessageFormat.format("Unknown error code={0}", status.getErrorCode())
                        : status.getErrorMessage()));
        default:
            throw new AssertionError(kind);
        }
    }

    private static final class JobInfo {

        final JobScript script;

        final JobId id;

        final JobClient client;

        JobInfo(JobScript script, JobId id, JobClient client) {
            assert script != null;
            assert id != null;
            assert client != null;
            this.script = script;
            this.id = id;
            this.client = client;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Job(id={0}, client={1}, batchId={2}, flowId={3}, phase={4}, stageId={6}, executionId={5})",
                    id,
                    client,
                    script.getBatchId(),
                    script.getFlowId(),
                    script.getPhase(),
                    script.getExecutionId(),
                    script.getStageId());
        }
    }
}
