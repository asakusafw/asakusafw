/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.session.SessionMirror;

/**
 * An implementation of {@link SessionMirror} using file locking.
 * @since 0.2.2
 */
class FileSessionMirror extends SessionMirror {

    static final FileSessionLogger WGLOG = new FileSessionLogger(FileSessionMirror.class);

    static final Logger LOG = LoggerFactory.getLogger(FileSessionMirror.class);

    private final String id;

    private final File path;

    private final RandomAccessFile file;

    private final FileLock lock;

    private volatile boolean closed;

    /**
     * Creates a new instance.
     * @param id the session ID
     * @param path the target file path
     * @param file the target file
     * @param lock the acquired lock
     */
    FileSessionMirror(String id, File path, RandomAccessFile file, FileLock lock) {
        assert id != null;
        assert path != null;
        assert file != null;
        assert lock != null;
        this.id = id;
        this.path = path;
        this.file = file;
        this.lock = lock;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void complete() throws IOException {
        LOG.debug("Completing session: {}", id);
        delete();
    }

    @Override
    public void abort() throws IOException {
        LOG.debug("Aborting session: {}", id);
        delete();
    }

    private void delete() throws IOException {
        try {
            FileSessionProvider.invalidate(path, file);
        } catch (IOException e) {
            WGLOG.error(e, "E02001",
                    id,
                    path);
            throw e;
        }
        close();
        LOG.debug("Deleting session file: {}", path);
        if (path.delete() == false) {
            WGLOG.error("E02002",
                    id,
                    path);
            throw new IOException(MessageFormat.format(
                    "Failed to delete session object (id=\"{0}\", path=\"{1}\")",
                    id,
                    path));
        }
    }

    @Override
    public synchronized void close() {
        if (closed == false) {
            LOG.debug("Closing session file: {}", path);
            try {
                lock.release();
            } catch (IOException e) {
                WGLOG.warn(e, "W01005",
                        id,
                        path);
            }
            try {
                file.close();
            } catch (IOException e) {
                WGLOG.warn(e, "W01006",
                        id,
                        path);
            }
        }
        closed = true;
    }
}
