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
package com.asakusafw.windgate.stream.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.stream.CountingOutputStream;
import com.asakusafw.windgate.stream.OutputStreamProvider;

/**
 * An implementation of {@link OutputStreamProvider} using files.
 * @since 0.2.4
 */
public class FileOutputStreamProvider extends OutputStreamProvider {

    static final Logger LOG = LoggerFactory.getLogger(FileOutputStreamProvider.class);

    private static final int BUFFER_SIZE = 1024 * 64;

    private final Iterator<File> iterator;

    private File current;

    /**
     * Creates a new instance.
     * @param file target file to create
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileOutputStreamProvider(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null"); //$NON-NLS-1$
        }
        this.iterator = Arrays.asList(file).iterator();
    }

    @Override
    public void next() throws IOException {
        current = null;
        if (iterator.hasNext()) {
            current = iterator.next().getCanonicalFile();
        } else {
            throw new IOException();
        }
    }

    @Override
    public String getCurrentPath() {
        return current.getAbsolutePath();
    }

    @Override
    public CountingOutputStream openStream() throws IOException {
        LOG.debug("Opening file to write: {}",
                current);
        boolean succeed = false;
        File parent = current.getParentFile();
        for (int i = 0; i < 10; i++) {
            if (parent == null || parent.exists()) {
                break;
            }
            LOG.debug("Parent directory does not exist, will be created: {}",
                    current);
            if (parent.mkdirs()) {
                break;
            }
        }
        if (parent != null && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create parent directory: {0}",
                    parent.getAbsolutePath()));
        }
        FileOutputStream stream = new FileOutputStream(current);
        try {
            CountingOutputStream result = new CountingOutputStream(new BufferedOutputStream(stream, BUFFER_SIZE));
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }

    @Override
    public void close() {
        return;
    }
}
