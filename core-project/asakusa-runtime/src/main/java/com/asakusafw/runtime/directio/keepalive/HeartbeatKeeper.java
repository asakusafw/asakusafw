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
package com.asakusafw.runtime.directio.keepalive;

import java.io.Closeable;
import java.io.IOException;
import java.lang.Thread.State;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.directio.Counter;

/**
 * Keep alive using heartbeat.
 * @since 0.2.6
 */
class HeartbeatKeeper implements Closeable {

    static final Log LOG = LogFactory.getLog(HeartbeatKeeper.class);

    static final AtomicInteger THREAD_SERIAL = new AtomicInteger();

    final List<Counter> counters = new CopyOnWriteArrayList<Counter>();

    private final long interval;

    private final Thread daemon;

    /**
     * Creates a new instance.
     * @param interval heartbeat interval (ms)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HeartbeatKeeper(final long interval) {
        this.interval = interval;
        this.daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                keepAlive();
            }
        });
        daemon.setName(String.format("directio-keepalive-%02d", THREAD_SERIAL.incrementAndGet()));
        daemon.setDaemon(true);
    }

    /**
     * Registers a counter into this keeper.
     * @param counter target counter
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void register(Counter counter) {
        if (counter == null) {
            throw new IllegalArgumentException("counter must not be null"); //$NON-NLS-1$
        }
        counter.add(0);
        counters.add(counter);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Registered counter: {2} ({0}@{1})",
                    daemon.getName(),
                    daemon.getState(),
                    counter));
        }
        synchronized (daemon) {
            if (daemon.getState() == State.NEW) {
                LOG.info(MessageFormat.format(
                        "Starting Heartbeat Keeper: {0})",
                        daemon.getName()));
                daemon.start();
            }
        }
    }

    /**
     * Unregisters the previously registered counter from this keeper.
     * @param counter target counter
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void unregister(Counter counter) {
        if (counter == null) {
            throw new IllegalArgumentException("counter must not be null"); //$NON-NLS-1$
        }
        boolean removed = counters.remove(counter);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Unregistered counter: {2} ({0}@{1})",
                    daemon.getName(),
                    daemon.getState(),
                    counter));
        }
        if (removed == false) {
            LOG.warn(MessageFormat.format(
                    "Failed to delete the registered counter: {0}",
                    counter));
        }
    }

    void keepAlive() {
        if (interval <= 0) {
            return;
        }
        try {
            while (true) {
                if (LOG.isDebugEnabled() && counters.isEmpty() == false) {
                    LOG.debug(MessageFormat.format(
                            "Heartbeat by HearbeatKeeper: {1}counter(s) ({0})",
                            Thread.currentThread().getName(),
                            counters.size()));
                }
                for (Counter counter : counters) {
                    counter.add(0);
                }
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            LOG.info(MessageFormat.format(
                    "Direct I/O KeepAlive Thread is going to shut down: {1}counter(s) ({0})",
                    Thread.currentThread().getName(),
                    counters.size()));
        }
    }

    boolean isEmpty() {
        return counters.isEmpty();
    }

    @Override
    public void close() throws IOException {
        synchronized (daemon) {
            if (daemon.isAlive()) {
                daemon.interrupt();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
