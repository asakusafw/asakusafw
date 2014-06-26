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
package com.asakusafw.runtime.util.lock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link LockObject} for {@link LocalFileLockProvider}.
 * @param <T> the lock target type
 * @since 0.7.0
 */
public class LocalFileLockObject<T> implements LockObject<T> {

    static final Log LOG = LogFactory.getLog(LocalFileLockProvider.class);

    private final T target;

    private final File lockFile;

    private final RandomAccessFile fd;

    private final FileLock lockEntity;

    /**
     * Creates a new instance.
     * @param target lock target
     * @param lockFile the lock file
     * @param fd the lock file descriptor
     * @param lockEntity the internal lock object
     */
    public LocalFileLockObject(T target, File lockFile, RandomAccessFile fd, FileLock lockEntity) {
        this.target = target;
        this.lockFile = lockFile;
        this.fd = fd;
        this.lockEntity = lockEntity;
    }

    @Override
    public T getTarget() {
        return target;
    }

    /**
     * Returns the local lock file.
     * @return the local lock file
     */
    public File getLockFile() {
        return lockFile;
    }

    @Override
    public void close() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Starting to release lock for \"{0}\" ({1})",
                    target,
                    lockFile));
        }
        lockEntity.release();
        fd.close();
        if (lockFile.delete() == false) {
            LOG.warn(MessageFormat.format(
                    "Failed to delete lock file for \"{0}\" ({1})",
                    target,
                    lockFile));
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1}@{2})",
                getClass().getSimpleName(),
                target,
                lockFile);
    }
}
