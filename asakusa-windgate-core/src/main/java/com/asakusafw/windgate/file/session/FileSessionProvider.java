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
package com.asakusafw.windgate.file.session;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.session.SessionException;
import com.asakusafw.windgate.core.session.SessionException.Reason;
import com.asakusafw.windgate.core.session.SessionMirror;
import com.asakusafw.windgate.core.session.SessionProfile;
import com.asakusafw.windgate.core.session.SessionProvider;

/**
 * An implementation of {@link SessionProvider} using the local file system and file lock.
 * @since 0.2.3
 */
public class FileSessionProvider extends SessionProvider {

    private static final String VALID_STRING = "VALID";

    private static final String DISPOSED_STRING = "DISPOSED";

    private static final byte[] VALID = String.format("%-10s%n", VALID_STRING).getBytes(); //$NON-NLS-1$

    private static final byte[] DISPOSED = String.format("%-10s%n", DISPOSED_STRING).getBytes(); //$NON-NLS-1$

    /**
     * Profile key name of session storage directory.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_DIRECTORY = "directory";

    private volatile File directory;

    @Override
    protected void configure(SessionProfile profile) throws IOException {
        directory = prepareDirectory(profile);
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
            throw new IOException(MessageFormat.format(
                    "The session config \"{0}\" was not defined",
                    KEY_DIRECTORY));
        }
        String path;
        try {
            ParameterList environment = new ParameterList(System.getenv());
            path = environment.replace(rawPath, true);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to resolve the session config \"{0}\": {1}",
                    KEY_DIRECTORY,
                    rawPath), e);
        }
        File dir = new File(path);
        if (dir.isDirectory() == false && dir.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to prepare session directory: {0}",
                    dir.getAbsolutePath()));
        }
        return dir;
    }

    @Override
    public List<String> getCreatedIds() throws IOException {
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() == false) {
                    return false;
                }
                if (pathname.getName().startsWith(".")) {
                    return false;
                }
                return true;
            }
        });
        List<String> results = new ArrayList<String>();
        for (File file : files) {
            results.add(fileToId(file));
        }
        return results;
    }

    @Override
    public SessionMirror create(String id) throws SessionException, IOException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        return attach(id, true, false);
    }

    @Override
    public SessionMirror open(String id) throws SessionException, IOException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        return attach(id, false, false);
    }

    @Override
    public void delete(String id) throws IOException {
        assert id != null;
        SessionMirror session = attach(id, false, true);
        session.abort();
    }

    private SessionMirror attach(String id, boolean create, boolean ignoreBroken) throws IOException {
        assert id != null;
        boolean completed = false;
        File path = idToFile(id);
        RandomAccessFile file = null;
        FileLock lock = null;
        try {
            file = new RandomAccessFile(path, "rw");
            lock = acquireLock(id, file);
            State state = getSessionState(id, file);
            switch (state) {
            case INIT:
                if (create == false) {
                    throw new SessionException(id, Reason.NOT_EXIST);
                } else {
                    createSession(file);
                }
                break;
            case CREATED:
                if (create) {
                    throw new SessionException(id, Reason.ALREADY_EXIST);
                }
                break;
            case INVALID:
                if (ignoreBroken == false) {
                    throw new SessionException(id, Reason.BROKEN);
                }
            }
            completed = true;
            return new FileSessionMirror(id, path, file, lock);
        } finally {
            if (completed == false) {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e) {
                        // TODO logging
                    }
                }
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e) {
                        // TODO logging
                    }
                }
            }
        }
    }
    private FileLock acquireLock(String id, RandomAccessFile file) throws IOException {
        assert id != null;
        assert file != null;
        try {
            FileLock lock = file.getChannel().tryLock();
            if (lock != null) {
                return lock;
            }
        } catch (OverlappingFileLockException e) {
            // fall through
        }
        throw new SessionException(id, Reason.ACQUIRED);
    }

    private void createSession(RandomAccessFile file) throws IOException {
        assert file != null;
        assert file.getFilePointer() == 0L;
        file.write(VALID);
        file.getFD().sync();
    }

    private State getSessionState(String id, RandomAccessFile file) throws IOException {
        assert file != null;
        file.seek(0L);
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

    static void invalidate(RandomAccessFile file) throws IOException {
        assert file != null;
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
