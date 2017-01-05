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
package com.asakusafw.compiler.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.ResourceRepository;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.runtime.io.util.ZipEntryInputStream;

/**
 * An implementation of {@link ResourceRepository} which provides contents in a ZIP archive on the local file system.
 */
public class ZipRepository implements ResourceRepository {

    static final Logger LOG = LoggerFactory.getLogger(ZipRepository.class);

    private final File archive;

    /**
     * Creates a new instance.
     * @param archive the target ZIP archive file
     * @throws IOException if failed to open the target archive file
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ZipRepository(File archive) throws IOException {
        Precondition.checkMustNotBeNull(archive, "archive"); //$NON-NLS-1$
        if (archive.isFile() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("ZipRepository.errorInputNotRegular"), //$NON-NLS-1$
                    archive));
        }
        this.archive = archive.getAbsoluteFile().getCanonicalFile();
    }

    @Override
    public Cursor createCursor() throws IOException {
        FileInputStream input = new FileInputStream(archive);
        boolean success = false;
        try {
            Cursor cursor = new EntryCursor(archive, new ZipInputStream(input));
            success = true;
            return cursor;
        } finally {
            if (success == false) {
                input.close();
            }
        }
    }

    private static class EntryCursor implements Cursor {

        private final File source;

        private final ZipInputStream stream;

        private ZipEntry current;

        private int entries;

        EntryCursor(File source, ZipInputStream stream) {
            assert source != null;
            assert stream != null;
            this.source = source;
            this.stream = stream;
            this.entries = 0;
        }

        @Override
        public boolean next() throws IOException {
            while (true) {
                current = stream.getNextEntry();
                if (current == null) {
                    if (entries == 0) {
                        throw new IOException(MessageFormat.format(
                                Messages.getString("ZipRepository.errorMalformedZipFile"), //$NON-NLS-1$
                                source));
                    }
                    return false;
                }
                entries++;
                if (current.isDirectory() == false) {
                    return true;
                }
            }
        }

        @Override
        public Location getLocation() {
            return Location.fromPath(
                    current.getName().replace('\\', '/'),
                    '/');
        }

        @Override
        public InputStream openResource() throws IOException {
            return new ZipEntryInputStream(stream);
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}
