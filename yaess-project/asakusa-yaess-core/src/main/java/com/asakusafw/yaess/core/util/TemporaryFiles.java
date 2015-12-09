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
package com.asakusafw.yaess.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages temporary files to be deleted.
 * @since 0.8.0
 */
public class TemporaryFiles implements AutoCloseable {

    static final Logger LOG = LoggerFactory.getLogger(TemporaryFiles.class);

    private final LinkedList<File> files = new LinkedList<>();

    /**
     * Adds a file to be deleted.
     * @param file the target file
     * @return the added file
     */
    public File add(File file) {
        files.add(file);
        return file;
    }

    /**
     * Creates a new temporary file.
     * @param prefix the file prefix
     * @param suffix the file suffix (nullable)
     * @return the created file
     * @throws IOException if I/O error was occurred while creating the file
     */
    public File create(String prefix, String suffix) throws IOException {
        return add(File.createTempFile(prefix, suffix));
    }

    /**
     * Creates a new temporary file.
     * @param prefix the file prefix
     * @param suffix the file suffix (nullable)
     * @param contents the file contents
     * @return the created file
     * @throws IOException if I/O error was occurred while creating the file
     */
    public File create(String prefix, String suffix, InputStream contents) throws IOException {
        File file = create(prefix, suffix);
        try (OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[256];
            while (true) {
                int read = contents.read(buf);
                if (read < 0) {
                    break;
                }
                out.write(buf, 0, read);
            }
        }
        return file;
    }

    @Override
    public void close() {
        while (files.isEmpty() == false) {
            File file = files.removeFirst();
            if (file.delete() == false && file.exists()) {
                LOG.warn(MessageFormat.format(
                        "failed to delete a temporary file: {0}",
                        file));
            }
        }
    }
}
