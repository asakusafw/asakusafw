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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.MessageFormat;

import com.asakusafw.windgate.core.session.SessionMirror;

/**
 * An implementation of {@link SessionMirror} using file locking.
 * @since 0.2.3
 */
class FileSessionMirror extends SessionMirror {

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
        delete();
    }

    @Override
    public void abort() throws IOException {
        delete();
    }

    private void delete() throws IOException {
        FileSessionProvider.invalidate(file);
        close();
        if (path.delete() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to delete session object (id=\"{0}\", path=\"{1}\")",
                    id,
                    path));
        }
    }

    @Override
    public synchronized void close() {
        if (closed == false) {
            try {
                lock.release();
            } catch (IOException e) {
                // TODO logging
            }
            try {
                file.close();
            } catch (IOException e) {
                // TODO logging
            }
        }
        closed = true;
    }
}
