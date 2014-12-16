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
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of {@link LockProvider} using local file lock.
 * @param <T> the lock target type
 * @since 0.7.0
 */
public class LocalFileLockProvider<T> implements LockProvider<T> {

    static final Log LOG = LogFactory.getLog(LocalFileLockProvider.class);

    private static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    private final File baseDirectory;

    /**
     * Creates a new instance.
     * @param baseDirectory the local lock file base directory
     */
    public LocalFileLockProvider(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public LocalFileLockObject<T> tryLock(T target) throws IOException {
        if (baseDirectory.mkdirs() == false && baseDirectory.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create lock directory: {0}",
                    baseDirectory));
        }
        String fileName = String.format("%08x.lck", target == null ? -1 : target.hashCode()); //$NON-NLS-1$
        File lockFile = new File(baseDirectory, fileName);
        RandomAccessFile fd = new RandomAccessFile(lockFile, "rw"); //$NON-NLS-1$
        boolean success = false;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Start to acquire lock for \"{0}\" ({1})", //$NON-NLS-1$
                        target,
                        lockFile));
            }
            FileLock lockEntity = getLock(target, lockFile, fd);
            if (lockEntity == null) {
                return null;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Finished to acquire lock for \"{0}\" ({1})", //$NON-NLS-1$
                            target,
                            lockFile));
                }
                try {
                    fd.seek(0L);
                    fd.setLength(0L);
                    fd.write(String.valueOf(target).getBytes(ENCODING));
                    success = true;
                    return new LocalFileLockObject<T>(target, lockFile, fd, lockEntity);
                } finally {
                    if (success == false) {
                        lockEntity.release();
                    }
                }
            }
        } finally {
            if (success == false) {
                fd.close();
            }
        }
    }

    private FileLock getLock(T target, File lockFile, RandomAccessFile fd) throws IOException {
        try {
            FileLock result = fd.getChannel().tryLock();
            if (result == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Failed to acquire lock for \"{0}\" ({1})", //$NON-NLS-1$
                            target,
                            lockFile));
                }
            }
            return result;
        } catch (OverlappingFileLockException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Failed to acquire lock for \"{0}\" ({1})", //$NON-NLS-1$
                        target,
                        lockFile)/*, e*/);
            }
            return null;
        }
    }
}
