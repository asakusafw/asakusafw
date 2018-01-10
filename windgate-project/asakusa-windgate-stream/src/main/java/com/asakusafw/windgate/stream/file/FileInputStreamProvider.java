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
package com.asakusafw.windgate.stream.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.stream.CountingInputStream;
import com.asakusafw.windgate.stream.InputStreamProvider;

/**
 * An implementation of {@link InputStreamProvider} using files.
 * @since 0.2.4
 */
public class FileInputStreamProvider extends InputStreamProvider {

    static final Logger LOG = LoggerFactory.getLogger(FileInputStreamProvider.class);

    private static final int BUFFER_SIZE = 1024 * 64;

    private final Iterator<File> iterator;

    private File current;

    /**
     * Creates a new instance.
     * @param file target file to open
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileInputStreamProvider(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null"); //$NON-NLS-1$
        }
        this.iterator = Arrays.asList(file).iterator();
    }

    @Override
    public boolean next() throws IOException {
        current = null;
        if (iterator.hasNext()) {
            current = iterator.next().getCanonicalFile();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getCurrentPath() {
        return current.getAbsolutePath();
    }

    @Override
    public CountingInputStream openStream() throws IOException {
        LOG.debug("Opening file to read: {}",
                current);
        boolean succeed = false;
        FileInputStream stream = new FileInputStream(current);
        try {
            CountingInputStream result = new CountingInputStream(new BufferedInputStream(stream, BUFFER_SIZE));
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                try {
                    stream.close();
                } catch (IOException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "Failed to dispose input: {0}",
                                current), e);
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        return;
    }
}
