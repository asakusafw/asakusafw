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
package com.asakusafw.yaess.basic;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.basic.JobExecutor.Executing;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.Job;
import com.asakusafw.yaess.core.JobScheduler;
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.PhaseMonitor.JobStatus;
import com.asakusafw.yaess.core.YaessLogger;

/**
 * An abstract implementation of {@link JobScheduler}.
 * @since 0.2.3
 */
public abstract class AbstractJobScheduler extends JobScheduler {

    static final YaessLogger YSLOG = new YaessBasicLogger(AbstractJobScheduler.class);

    static final Logger LOG = LoggerFactory.getLogger(AbstractJobScheduler.class);

    /**
     * Returns a {@link JobExecutor} for this scheduler.
     * @return the {@link JobExecutor}
     */
    protected abstract JobExecutor getJobExecutor();

    @Override
    public final void execute(
            PhaseMonitor monitor,
            ExecutionContext context,
            List<? extends Job> jobs,
            ErrorHandler errorHandler) throws InterruptedException, IOException {
        if (monitor == null) {
            throw new IllegalArgumentException("monitor must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (jobs == null) {
            throw new IllegalArgumentException("jobs must not be null"); //$NON-NLS-1$
        }
        if (errorHandler == null) {
            throw new IllegalArgumentException("errorHandler must not be null"); //$NON-NLS-1$
        }
        monitor.open(jobs.size());
        try {
            monitor.checkCancelled();
            Engine engine = new Engine(getJobExecutor(), monitor, context, errorHandler, jobs);
            engine.run();
        } finally {
            monitor.close();
        }
    }

    private static final class Engine {

        private final JobExecutor executor;

        final PhaseMonitor monitor;

        final ExecutionContext context;

        private final ErrorHandler handler;

        private final LinkedList<Job> waiting;

        private final Map<String, Executing> executing;

        private final BlockingQueue<Executing> doneQueue;

        private final Set<String> blockers;

        private boolean sawError;

        Engine(
                JobExecutor executor,
                PhaseMonitor monitor,
                ExecutionContext context,
                ErrorHandler handler,
                List<? extends Job> waiting) {
            assert executor != null;
            assert monitor != null;
            assert context != null;
            assert handler != null;
            assert waiting != null;
            this.executor = executor;
            this.monitor = monitor;
            this.context = context;
            this.handler = handler;
            this.waiting = new LinkedList<Job>(waiting);
            this.executing = new HashMap<String, Executing>();
            this.doneQueue = new LinkedBlockingQueue<Executing>();
            this.blockers = new TreeSet<String>();
            for (Job job : waiting) {
                blockers.add(job.getId());
            }
            this.sawError = false;
        }

        void run() throws IOException, InterruptedException {
            while (waiting.isEmpty() == false) {
                boolean submitted = submitAllWaiting();
                if (submitted == false && executing.isEmpty()) {
                    assert waiting.isEmpty() == false;
                    if (sawError) {
                        waiting.clear();
                    } else {
                        throw new IOException(MessageFormat.format(
                                "Jobnet execution was deadlocked: {4} (batch={0}, flow={1}, phase={2}, execution={3})",
                                context.getBatchId(),
                                context.getFlowId(),
                                context.getPhase(),
                                context.getExecutionId(),
                                blockers));
                    }
                } else {
                    waitForDone();
                }
            }
            while (executing.isEmpty() == false) {
                waitForDone();
            }
            if (sawError) {
                throw new IOException(MessageFormat.format(
                        "Jobnet was failed by preceding errors (batch={0}, flow={1}, phase={2}, execution={3})",
                        context.getBatchId(),
                        context.getFlowId(),
                        context.getPhase(),
                        context.getExecutionId()));
            }
        }

        private boolean submitAllWaiting() throws IOException, InterruptedException {
            boolean sawSubmit = false;
            for (Iterator<Job> iter = waiting.iterator(); iter.hasNext();) {
                Job next = iter.next();
                LOG.debug("Attemps to submit job: {}", next.getId());
                if (isBlocked(next)) {
                    LOG.debug("Job is currently blocked: {}", next.getId());
                    continue;
                }
                iter.remove();
                if (submit(next)) {
                    sawSubmit = true;
                }
            }
            return sawSubmit;
        }

        private void waitForDone() throws InterruptedException, IOException {
            assert executing.isEmpty() == false;
            monitor.checkCancelled();
            Executing done = doneQueue.take();
            assert done.isDone();
            handleDone(done);
            while (true) {
                monitor.checkCancelled();
                Executing rest = doneQueue.poll();
                if (rest == null) {
                    break;
                }
                assert rest.isDone();
                handleDone(rest);
            }
        }

        private void handleDone(Executing done) throws InterruptedException, IOException {
            assert done != null;
            assert done.isDone();
            Executing removed = executing.remove(done.getJob().getId());
            assert removed != null;
            try {
                done.get();
                done(done.getJob());
                monitor.reportJobStatus(done.getJob().getId(), JobStatus.SUCCESS, null);
            } catch (CancellationException e) {
                sawError = true;
                monitor.reportJobStatus(done.getJob().getId(), JobStatus.CANCELLED, e);
            } catch (ExecutionException e) {
                sawError = true;
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    monitor.reportJobStatus(done.getJob().getId(), JobStatus.CANCELLED, cause);
                    cancelExecution();
                    throw (InterruptedException) cause;
                } else if (cause instanceof IOException) {
                    monitor.reportJobStatus(done.getJob().getId(), JobStatus.FAILED, cause);
                    handleException(done.getJob(), (IOException) cause);
                } else if (cause instanceof Error) {
                    monitor.reportJobStatus(done.getJob().getId(), JobStatus.FAILED, cause);
                    cancelExecution();
                    throw (Error) cause;
                } else if (cause instanceof RuntimeException) {
                    monitor.reportJobStatus(done.getJob().getId(), JobStatus.FAILED, cause);
                    cancelExecution();
                    throw (RuntimeException) cause;
                } else {
                    monitor.reportJobStatus(done.getJob().getId(), JobStatus.FAILED, cause);
                    cancelExecution();
                    throw new AssertionError(cause);
                }
            }
        }

        private boolean submit(Job job) throws InterruptedException, IOException {
            assert job != null;
            monitor.checkCancelled();
            try {
                Executing execution = executor.submit(
                        monitor.createJobMonitor(job.getId(), 1), context, job, doneQueue);
                executing.put(execution.getJob().getId(), execution);
                return true;
            } catch (IOException e) {
                sawError = true;
                handleException(job, e);
                return false;
            }
        }

        private void handleException(Job job, IOException exception) throws IOException {
            assert job != null;
            assert exception != null;
            YSLOG.error(exception, "E21001",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId(),
                    context.getPhase(),
                    job.getJobLabel(),
                    job.getServiceLabel());
            if (handler.handle(context, exception) == false) {
                cancelExecution();
                throw exception;
            }
        }

        private void cancelExecution() {
            for (Executing exec : executing.values()) {
                exec.cancel(true);
            }
        }

        private boolean isBlocked(Job job) {
            assert job != null;
            for (String blocker : job.getBlockerIds()) {
                if (blockers.contains(blocker)) {
                    return true;
                }
            }
            return false;
        }

        private void done(Job job) {
            assert job != null;
            blockers.remove(job.getId());
        }
    }
}
