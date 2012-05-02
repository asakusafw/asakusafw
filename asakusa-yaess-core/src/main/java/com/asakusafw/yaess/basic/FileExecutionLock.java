/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionLock;
import com.asakusafw.yaess.core.YaessLogger;

/**
 * An implementation of {@link ExecutionLock} using local file system locks.
 * @since 0.2.3
 */
class FileExecutionLock extends ExecutionLock {

    static final YaessLogger YSLOG = new YaessBasicLogger(FileExecutionLock.class);

    static final Logger LOG = LoggerFactory.getLogger(FileExecutionLock.class);

    private static final String NAME_WORLD = "world.lck";

    private static final String NAME_BATCH = "batch-{0}.lck";

    private static final String NAME_FLOW = "flow-{0}-{1}.lck";

    private static final String NAME_EXECUTION = "execution-{0}-{1}-{2}.lck";

    private final Scope lockScope;

    private final String batchId;

    private final File directory;

    private final LockObject batchLock;

    private final Map<String, LockObject> flowLocks;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param lockScope lock scope
     * @param batchId target batch ID
     * @param directory base directory for lock files, must exist
     * @throws IOException if failed to create instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileExecutionLock(Scope lockScope, String batchId, File directory) throws IOException {
        if (lockScope == null) {
            throw new IllegalArgumentException("lockScope must not be null"); //$NON-NLS-1$
        }
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        if (directory == null) {
            throw new IllegalArgumentException("directory must not be null"); //$NON-NLS-1$
        }
        this.lockScope = lockScope;
        this.batchId = batchId;
        this.directory = directory;
        this.flowLocks = new HashMap<String, LockObject>();
        try {
            this.batchLock = acquireForBatch();
        } catch (IOException e) {
            YSLOG.error("E41001", batchId, lockScope);
            throw e;
        }
    }

    @Override
    public synchronized void beginFlow(String flowId, String executionId) throws IOException {
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        if (closed) {
            throw new IOException("Lock manager is already closed");
        }
        LockObject other = flowLocks.get(flowId);
        if (other != null) {
            YSLOG.error("E41002", batchId, flowId, executionId, lockScope);
            throw new IOException(MessageFormat.format(
                    "Failed to lock target flow (re-entrant): {0} ({1})",
                    flowId,
                    other.label));
        }
        try {
            LockObject lock = acquireForFlow(flowId, executionId);
            if (lock != null) {
                flowLocks.put(flowId, lock);
            }
        } catch (IOException e) {
            YSLOG.error("E41002", batchId, flowId, executionId, lockScope);
            throw e;
        }
    }

    @Override
    public synchronized void endFlow(String flowId, String executionId) throws IOException {
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        if (closed) {
            throw new IOException("Lock manager is already closed");
        }
        LockObject lock = flowLocks.remove(flowId);
        closeQuiet(lock);
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closeQuiet(batchLock);
        for (LockObject lock : flowLocks.values()) {
            closeQuiet(lock);
        }
        closed = true;
    }

    private void closeQuiet(LockObject lock) {
        if (lock == null) {
            return;
        }
        lock.close();
    }

    private LockObject acquireForBatch() throws IOException {
        assert directory != null;
        assert batchId != null;
        switch (lockScope) {
        case WORLD:
            return new LockObject(
                    "world lock",
                    new File(directory, NAME_WORLD));
        case BATCH:
            return new LockObject(
                    MessageFormat.format("batch lock - {0}", batchId),
                    new File(directory, MessageFormat.format(NAME_BATCH, batchId)));
        default:
            return null;
        }
    }

    private LockObject acquireForFlow(String flowId, String executionId) throws IOException {
        assert flowId != null;
        assert executionId != null;
        switch (lockScope) {
        case WORLD:
        case BATCH:
        case FLOW:
            return new LockObject(
                    MessageFormat.format("flow lock - {0}/{1}", batchId, flowId),
                    new File(directory, MessageFormat.format(NAME_FLOW, batchId, flowId, executionId)));
        case EXECUTION:
            return new LockObject(
                    MessageFormat.format("execution lock - {2} ({0}/{1})", batchId, flowId, executionId),
                    new File(directory, MessageFormat.format(NAME_EXECUTION, batchId, flowId, executionId)));
        default:
            return null;
        }
    }

    private static class LockObject implements Closeable {

        final String label;

        final File path;

        private final RandomAccessFile file;

        private final FileLock lock;

        private boolean closed;

        LockObject(String label, File path) throws IOException {
            if (path == null) {
                throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
            }
            if (label == null) {
                throw new IllegalArgumentException("label must not be null"); //$NON-NLS-1$
            }
            this.label = label;
            this.path = path;
            this.file = new RandomAccessFile(path, "rw");
            boolean succeed = false;
            try {
                this.lock = acquireFileLock();
                succeed = true;
            } finally {
                if (succeed == false) {
                    closeFile();
                }
            }
        }

        private FileLock acquireFileLock() throws IOException {
            assert file != null;
            LOG.debug("Acquiring lock: {}", label);
            FileLock flock;
            try {
                flock = file.getChannel().tryLock();
                if (flock != null) {
                    return flock;
                }
                LOG.debug("Failed to acquire lock: {}", path);
            } catch (OverlappingFileLockException e) {
                // fall through
                LOG.debug(MessageFormat.format(
                        "Lock may be overlapped: {0} ({1})",
                        label,
                        path), e);
            }
            throw new IOException(MessageFormat.format(
                    "Failed to acquire lock: {0}",
                    path));
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            releaseLock();
            closeFile();
            deleteFile();
        }

        private void closeFile() {
            try {
                this.file.close();
            } catch (IOException e) {
                YSLOG.warn(e, "W41002", label, path);
            }
        }

        private void releaseLock() {
            try {
                this.lock.release();
            } catch (IOException e) {
                YSLOG.warn(e, "W41001", label, path);
            }
        }

        private void deleteFile() {
            // FIXME more safe
            if (path.delete() == false) {
                YSLOG.warn("W41003", label, path);
            }
        }
    }
}
