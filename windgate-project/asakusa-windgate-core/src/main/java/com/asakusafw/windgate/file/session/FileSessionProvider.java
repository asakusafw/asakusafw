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
package com.asakusafw.windgate.file.session;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.session.SessionException;
import com.asakusafw.windgate.core.session.SessionException.Reason;
import com.asakusafw.windgate.core.session.SessionMirror;
import com.asakusafw.windgate.core.session.SessionProfile;
import com.asakusafw.windgate.core.session.SessionProvider;

/**
 * An implementation of {@link SessionProvider} using the local file system and file lock.
 * @since 0.2.2
 */
public class FileSessionProvider extends SessionProvider {

    static final FileSessionLogger WGLOG = new FileSessionLogger(FileSessionProvider.class);

    static final Logger LOG = LoggerFactory.getLogger(FileSessionProvider.class);

    private static final String VALID_STRING = "VALID";

    private static final String DISPOSED_STRING = "DISPOSED";

    private static final byte[] VALID = String.format(
            "%-10s%n", //$NON-NLS-1$
            VALID_STRING).getBytes(StandardCharsets.US_ASCII);

    private static final byte[] DISPOSED = String.format(
            "%-10s%n", //$NON-NLS-1$
            DISPOSED_STRING).getBytes(StandardCharsets.US_ASCII);

    /**
     * Profile key name of session storage directory.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_DIRECTORY = "directory";

    private volatile File directory;

    @Override
    protected void configure(SessionProfile profile) throws IOException {
        LOG.debug("Configuring file sessions: {}",
                profile.getProviderClass().getName());
        directory = prepareDirectory(profile);
        LOG.debug("Configured file sessions: {}",
                directory);
    }

    /**
     * Returns the session storage directory.
     * @return the session storage directory,
     *     or {@code null} if this have not been {@link #configure(SessionProfile) configured}
     */
    File getDirectory() {
        return directory;
    }

    private File prepareDirectory(SessionProfile profile) throws IOException {
        assert profile != null;
        String rawPath = profile.getConfiguration().get(KEY_DIRECTORY);
        if (rawPath == null || rawPath.isEmpty()) {
            WGLOG.error("E00001",
                    KEY_DIRECTORY,
                    rawPath);
            throw new IOException(MessageFormat.format(
                    "The session config \"{0}\" was not defined",
                    KEY_DIRECTORY));
        }
        String path;
        try {
            LOG.debug("Resolving session directory path: {}", rawPath);
            path = profile.getContext().getContextParameters().replace(rawPath, true);
        } catch (IllegalArgumentException e) {
            WGLOG.error(e, "E00001",
                    KEY_DIRECTORY,
                    rawPath);
            throw new IOException(MessageFormat.format(
                    "Failed to resolve the session config \"{0}\": {1}",
                    KEY_DIRECTORY,
                    rawPath), e);
        }
        File dir = new File(path);
        if (dir.isDirectory() == false && dir.mkdirs() == false) {
            WGLOG.error("E00002",
                    dir.getAbsolutePath());
            throw new IOException(MessageFormat.format(
                    "Failed to prepare session directory: {0}",
                    dir.getAbsolutePath()));
        }
        return dir;
    }

    @Override
    public List<String> getCreatedIds() throws IOException {
        LOG.debug("Collecting sessions: {}", directory);
        return list(directory, pathname -> {
            if (pathname.isFile() == false) {
                return false;
            }
            if (pathname.getName().startsWith(".")) {
                return false;
            }
            return true;
        }).stream().map(f -> fileToId(f)).collect(Collectors.toList());
    }

    private static List<File> list(File file, FileFilter filter) {
        return Optional.ofNullable(file.listFiles(filter))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    @Override
    public SessionMirror create(String id) throws SessionException, IOException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating session: {}", id);
        return attach(id, true, false);
    }

    @Override
    public SessionMirror open(String id) throws SessionException, IOException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Opening session: {}", id);
        return attach(id, false, false);
    }

    @Override
    public void delete(String id) throws IOException {
        assert id != null;
        LOG.debug("Deleting session: {}", id);
        SessionMirror session = attach(id, false, true);
        session.abort();
    }

    private SessionMirror attach(String id, boolean create, boolean force) throws IOException {
        assert id != null;
        boolean completed = false;
        boolean delete = false;
        File path = idToFile(id);
        RandomAccessFile file = null;
        FileLock lock = null;
        try {
            LOG.debug("Opening session file: {}", path);
            file = new RandomAccessFile(path, "rw");
            lock = acquireLock(id, path, file);
            State state = getSessionState(id, path, file);
            switch (state) {
            case INIT:
                if (create == false) {
                    delete = true;
                    throw new SessionException(id, Reason.NOT_EXIST);
                } else {
                    createSession(path, file);
                }
                break;
            case CREATED:
                if (create) {
                    throw new SessionException(id, Reason.ALREADY_EXIST);
                }
                break;
            case INVALID:
                if (force == false) {
                    WGLOG.error("W01001",
                            id,
                            path);
                    throw new SessionException(id, Reason.BROKEN);
                }
                break;
            default:
                throw new AssertionError(MessageFormat.format(
                        "Invalid state: {2} (id={0}, path={1})",
                        id,
                        path,
                        state));
            }
            completed = true;
            return new FileSessionMirror(id, path, file, lock);
        } catch (SessionException e) {
            throw e;
        } catch (IOException e) {
            WGLOG.error(e, "E01001",
                    id,
                    path);
            throw e;
        } finally {
            if (completed == false) {
                if (delete) {
                    try {
                        invalidate(path, file);
                    } catch (IOException e) {
                        WGLOG.warn(e, "W02002",
                                id,
                                path);
                    }
                }
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e) {
                        WGLOG.warn(e, "W02001",
                                id,
                                path);
                    }
                }
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e) {
                        WGLOG.warn(e, "W02002",
                                id,
                                path);
                    }
                }
                if (delete) {
                    if (path.delete() == false) {
                        WGLOG.warn("W02002",
                                id,
                                path);
                    }
                }
            }
        }
    }

    private FileLock acquireLock(String id, File path, RandomAccessFile file) throws IOException {
        assert id != null;
        assert path != null;
        assert file != null;
        LOG.debug("Acquiring lock: {}", id);
        try {
            FileLock lock = file.getChannel().tryLock();
            if (lock != null) {
                return lock;
            }
            LOG.debug("Failed to acquire lock: {}", id);
        } catch (OverlappingFileLockException e) {
            // fall through
            LOG.debug(MessageFormat.format(
                    "Failed to acquire lock: {0}",
                    id), e);
        }
        throw new SessionException(id, Reason.ACQUIRED);
    }

    private void createSession(File path, RandomAccessFile file) throws IOException {
        assert path != null;
        assert file != null;
        assert file.getFilePointer() == 0L;
        LOG.debug("Initializing session file: {}", path);
        file.write(VALID);
        file.getFD().sync();
    }

    private State getSessionState(String id, File path, RandomAccessFile file) throws IOException {
        assert id != null;
        assert path != null;
        assert file != null;
        file.seek(0L);
        LOG.debug("Loading session file: {}", path);
        byte[] buf = new byte[VALID.length];
        int length = file.read(buf);
        file.seek(file.length());
        if (length <= 0) {
            return State.INIT;
        } else if (Arrays.equals(buf, VALID)) {
            return State.CREATED;
        }
        return State.INVALID;
    }

    static void invalidate(File path, RandomAccessFile file) throws IOException {
        assert path != null;
        assert file != null;
        LOG.debug("Disposing session file: {}", path);
        file.seek(0L);
        file.write(DISPOSED);
    }

    private File idToFile(String id) {
        assert id != null;
        return new File(directory, id);
    }

    private String fileToId(File file) {
        assert file != null;
        return file.getName();
    }

    private enum State {

        INIT,

        CREATED,

        INVALID,
    }
}
