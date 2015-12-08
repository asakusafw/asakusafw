/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.windgate.core;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.resource.DriverRepository;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.resource.ResourceProvider;
import com.asakusafw.windgate.core.session.SessionMirror;
import com.asakusafw.windgate.core.session.SessionProfile;
import com.asakusafw.windgate.core.session.SessionProvider;
import com.asakusafw.windgate.core.util.SafeCloser;

/**
 * Executes WindGate.
 * @since 0.2.2
 * @version 0.4.0
 */
@SimulationSupport
public class GateTask implements Closeable {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(GateTask.class);

    static final Logger LOG = LoggerFactory.getLogger(GateTask.class);

    private final ExecutorService executor;

    private final SessionProvider sessionProvider;

    private final List<ResourceProvider> resourceProviders;

    private final Map<String, ProcessProvider> processProviders;

    final GateProfile profile;

    final GateScript script;

    final String sessionId;

    private final boolean createSession;

    private final boolean completeSession;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param profile the gate profile
     * @param script the gate script
     * @param sessionId current session ID
     * @param createSession {@code true} to create a new session, otherwise {@code false}
     * @param completeSession {@code true} to complete the session, otherwise {@code false}
     * @param arguments execution arguments (argument name =&gt; value)
     * @throws IOException if failed to initialize the task
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public GateTask(
            GateProfile profile,
            GateScript script,
            String sessionId,
            boolean createSession,
            boolean completeSession,
            ParameterList arguments) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        this.script = script;
        this.sessionId = sessionId;
        this.createSession = createSession;
        this.completeSession = completeSession;
        this.arguments = arguments;
        this.sessionProvider = loadSessionProvider(profile.getSession());
        this.resourceProviders = loadResourceProviders(profile.getResources());
        this.processProviders = loadProcessProviders(profile.getProcesses());
        this.executor = Executors.newFixedThreadPool(profile.getCore().getMaxProcesses(), new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, String.format("WindGate-%d", counter.incrementAndGet()));
                t.setDaemon(true);
                return t;
            }
        });
    }

    private SessionProvider loadSessionProvider(SessionProfile session) throws IOException {
        assert session != null;
        LOG.debug("Loading session provider: {}",
                session.getProviderClass().getName());
        SessionProvider result = session.createProvider();
        return result;
    }

    private List<ResourceProvider> loadResourceProviders(
            List<ResourceProfile> resources) throws IOException {
        assert resources != null;
        List<ResourceProvider> results = new ArrayList<>();
        for (ResourceProfile resourceProfile : resources) {
            LOG.debug("Loading resource provider \"{}\": {}",
                    resourceProfile.getName(),
                    resourceProfile.getProviderClass().getName());
            ResourceProvider provider = resourceProfile.createProvider();
            results.add(provider);
        }
        return results;
    }

    private Map<String, ProcessProvider> loadProcessProviders(
            List<ProcessProfile> processes) throws IOException {
        assert processes != null;
        Map<String, ProcessProvider> results = new TreeMap<>();
        for (ProcessProfile processProfile : processes) {
            LOG.debug("Loading process provider \"{}\": {}",
                    processProfile.getName(),
                    processProfile.getProviderClass().getName());
            assert results.containsKey(processProfile.getName()) == false;
            ProcessProvider provider = processProfile.createProvider();
            results.put(processProfile.getName(), provider);
        }
        return results;
    }

    /**
     * Executes WindGate process.
     * @throws IOException if failed
     * @throws InterruptedException if interrupted
     */
    public void execute() throws IOException, InterruptedException {
        WGLOG.info("I00000",
                sessionId,
                profile.getName(),
                script.getName());
        long start = System.currentTimeMillis();
        WGLOG.info("I00001",
                sessionId,
                profile.getName(),
                script.getName());
        try {
            try (SafeCloser<SessionMirror> session = new SafeCloser<SessionMirror>() {
                @Override
                protected void handle(IOException exception) throws IOException {
                    WGLOG.warn(exception, "W00001",
                            sessionId,
                            profile.getName(),
                            script.getName());
                }
            }) {
                session.set(attachSession(createSession));
                WGLOG.info("I00002",
                        sessionId,
                        profile.getName(),
                        script.getName());
                List<ResourceMirror> resources = createResources();
                if (createSession) {
                    WGLOG.info("I00003",
                            sessionId,
                            profile.getName(),
                            script.getName());
                    fireSessionCreated(resources);
                }
                WGLOG.info("I00004",
                        sessionId,
                        profile.getName(),
                        script.getName());
                prepareResources(resources);
                WGLOG.info("I00005",
                        sessionId,
                        profile.getName(),
                        script.getName());
                runGateProcesses(resources);
                if (completeSession) {
                    WGLOG.info("I00006",
                            sessionId,
                            profile.getName(),
                            script.getName());
                    fireSessionCompleted(resources);
                    WGLOG.info("I00007",
                            sessionId,
                            profile.getName(),
                            script.getName());
                    session.get().complete();
                }
            }
            WGLOG.info("I00008",
                    sessionId,
                    profile.getName(),
                    script.getName());
        } finally {
            long end = System.currentTimeMillis();
            WGLOG.info("I00999",
                    sessionId,
                    profile.getName(),
                    script.getName(),
                    end - start);
        }
    }

    private SessionMirror attachSession(boolean create) throws IOException {
        if (create) {
            LOG.debug("Creating session: {}",
                    sessionId);
            if (RuntimeContext.get().canExecute(sessionProvider)) {
                return sessionProvider.create(sessionId);
            } else {
                return new SessionMirror.Null(sessionId);
            }
        } else {
            LOG.debug("Opening session: {}",
                    sessionId);
            if (RuntimeContext.get().canExecute(sessionProvider)) {
                return sessionProvider.open(sessionId);
            } else {
                return new SessionMirror.Null(sessionId);
            }
        }
    }

    private List<ResourceMirror> createResources() throws IOException {
        List<ResourceMirror> results = new ArrayList<>();
        for (ResourceProvider provider : resourceProviders) {
            LOG.debug("Creating resource: {}",
                    provider.getClass().getName());
            // NOTE each initialization can be done in multi-threaded
            ResourceMirror resource = provider.create(sessionId, arguments);
            results.add(resource);
        }
        return results;
    }

    private void fireSessionCreated(List<ResourceMirror> resources) throws IOException {
        assert resources != null;
        for (final ResourceMirror resource : resources) {
            if (resource.isTransactional()) {
                try {
                    LOG.debug("Initializing transactional resource \"{}\" for session \"{}\"",
                            resource.getName(),
                            sessionId);
                    if (RuntimeContext.get().canExecute(resource)) {
                        resource.onSessionCreated();
                    }
                } catch (IOException e) {
                    WGLOG.error(e, "E00001",
                            sessionId,
                            profile.getName(),
                            script.getName(),
                            resource.getName());
                    throw new IOException(MessageFormat.format(
                            "Failed to initializing resource \"{0}\" (session={1})",
                            resource.getName(),
                            sessionId), e);
                }
            }
        }
        LinkedList<Future<?>> futures = new LinkedList<>();
        for (final ResourceMirror resource : resources) {
            if (resource.isTransactional() == false) {
                Future<?> future = executor.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws IOException {
                        LOG.debug("Initializing resource \"{}\" for session \"{}\"",
                                resource.getName(),
                                sessionId);
                        try {
                            if (RuntimeContext.get().canExecute(resource)) {
                                resource.onSessionCreated();
                            }
                        } catch (IOException e) {
                            WGLOG.error(e, "E00001",
                                    sessionId,
                                    profile.getName(),
                                    script.getName(),
                                    resource.getName());
                            throw new IOException(MessageFormat.format(
                                    "Failed to initializing resource \"{0}\" (session={1})",
                                    resource.getName(),
                                    sessionId), e);
                        }
                        return null;
                    }
                });
                futures.add(future);
            }
        }
        int failureCount = waitForComplete(futures);
        if (failureCount > 0) {
            throw new IOException(MessageFormat.format(
                    "Failed to initialize session: {0}",
                    sessionId));
        }
    }

    private void prepareResources(List<ResourceMirror> resources) throws IOException {
        assert resources != null;
        LinkedList<Future<?>> futures = new LinkedList<>();
        for (final ResourceMirror resource : resources) {
            Future<?> future = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws IOException {
                    LOG.debug("Preparing resource \"{}\"",
                            resource.getName());
                    try {
                        if (RuntimeContext.get().canExecute(resource)) {
                            resource.prepare(script);
                        }
                    } catch (IOException e) {
                        WGLOG.error(e, "E00002",
                                sessionId,
                                profile.getName(),
                                script.getName(),
                                resource.getName());
                        throw new IOException(MessageFormat.format(
                                "Preparing {0} failed (session={1})",
                                resource.getName(),
                                sessionId), e);
                    }
                    return null;
                }
            });
            futures.add(future);
        }
        int failureCount = waitForComplete(futures);
        if (failureCount > 0) {
            throw new IOException("Failed to prepare some resources");
        }
    }

    private void runGateProcesses(List<ResourceMirror> resources) throws IOException {
        assert resources != null;
        final DriverRepository drivers = new DriverRepository(resources);
        LinkedList<Future<?>> futures = new LinkedList<>();
        for (final ProcessScript<?> process : script.getProcesses()) {
            final ProcessProvider processProvider = processProviders.get(process.getProcessType());
            assert processProvider != null;
            Future<?> future = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws IOException {
                    LOG.debug("Starting gate process: {}",
                            process.getName(),
                            sessionId);
                    try {
                        if (RuntimeContext.get().canExecute(processProvider)) {
                            processProvider.execute(drivers, process);
                        } else {
                            LOG.info("Skipped process execution (simulated)");
                        }
                    } catch (IOException e) {
                        WGLOG.error(e, "E00003",
                                sessionId,
                                profile.getName(),
                                script.getName(),
                                process.getName());
                        throw new IOException(MessageFormat.format(
                                "Process {0} failed: source={1} -> drain={2} (session={3})",
                                process.getName(),
                                process.getSourceScript().getResourceName(),
                                process.getDrainScript().getResourceName(),
                                sessionId), e);
                    }
                    return null;
                }
            });
            futures.add(future);
        }
        int failureCount = waitForComplete(futures);
        if (failureCount > 0) {
            throw new IOException(MessageFormat.format(
                    "Failed to execute some gate processes in session \"{0}\"",
                    sessionId));
        }
    }

    private void fireSessionCompleted(List<ResourceMirror> resources) throws IOException {
        assert resources != null;
        LinkedList<Future<?>> futures = new LinkedList<>();
        for (final ResourceMirror resource : resources) {
            if (resource.isTransactional() == false) {
                Future<?> future = executor.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws IOException {
                        LOG.debug("Finalizing resource \"{}\" for session \"{}\"",
                                resource.getName(),
                                sessionId);
                        try {
                            if (RuntimeContext.get().canExecute(resource)) {
                                resource.onSessionCompleting();
                            }
                        } catch (IOException e) {
                            WGLOG.error(e, "E00004",
                                    sessionId,
                                    profile.getName(),
                                    script.getName(),
                                    resource.getName());
                            throw new IOException(MessageFormat.format(
                                    "Failed to finalizing resource \"{0}\" (session={1})",
                                    resource.getName(),
                                    sessionId), e);
                        }
                        return null;
                    }
                });
                futures.add(future);
            }
        }
        int failureCount = waitForComplete(futures);
        if (failureCount > 0) {
            throw new IOException(MessageFormat.format(
                    "Failed to complete session: {0}",
                    sessionId));
        }
        for (final ResourceMirror resource : resources) {
            if (resource.isTransactional()) {
                LOG.debug("Finalizing transactional resource \"{}\" for session \"{}\"",
                        resource.getName(),
                        sessionId);
                try {
                    if (RuntimeContext.get().canExecute(resource)) {
                        resource.onSessionCompleting();
                    }
                } catch (IOException e) {
                    WGLOG.error(e, "E00004",
                            sessionId,
                            profile.getName(),
                            script.getName(),
                            resource.getName());
                    throw new IOException(MessageFormat.format(
                            "Failed to finalizing resource \"{0}\" (session={1})",
                            resource.getName(),
                            sessionId), e);
                }
            }
        }
    }

    private int waitForComplete(LinkedList<Future<?>> futures) {
        assert futures != null;
        int failureCount = 0;
        while (futures.isEmpty() == false) {
            Future<?> future = futures.removeFirst();
            try {
                future.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                futures.addLast(future);
                // continue
            } catch (InterruptedException e) {
                WGLOG.warn(e, "W00002",
                        sessionId,
                        profile.getName(),
                        script.getName());
                futures.addLast(future);
                cancelAll(futures);
                // continue
            } catch (ExecutionException e) {
                failureCount++;
                WGLOG.warn(e, "W00003",
                        sessionId,
                        profile.getName(),
                        script.getName());
                cancelAll(futures);
                if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            } catch (CancellationException e) {
                failureCount++;
                WGLOG.warn(e, "W00004",
                        sessionId,
                        profile.getName(),
                        script.getName());
            }
        }
        return failureCount;
    }

    private void cancelAll(LinkedList<Future<?>> futures) {
        assert futures != null;
        for (Future<?> future : futures) {
            future.cancel(true);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
