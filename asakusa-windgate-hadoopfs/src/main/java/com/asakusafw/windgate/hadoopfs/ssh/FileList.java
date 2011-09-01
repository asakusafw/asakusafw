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
package com.asakusafw.windgate.hadoopfs.ssh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file list transfer protocol.
 * @since 0.2.3
 */
public final class FileList {

    static final Logger LOG = LoggerFactory.getLogger(FileList.class);

    static final String FIRST_ENTRY_NAME = ".__FIRST_ENTRY__"; //$NON-NLS-1$

    static final String LAST_ENTRY_NAME = ".__LAST_ENTRY__"; //$NON-NLS-1$

    /**
     * Creates a simple {@link FileStatus}.
     * @param path the target path
     * @return the created file status with the specified path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FileStatus createFileStatus(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        return new FileStatus(0, false, 0, 0, 0, 0, null, null, null, path);
    }

    /**
     * Creates a new reaer.
     * @param input the input stream which contains a file list
     * @return the created reader
     * @throws IOException if failed to open the file list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FileList.Reader createReader(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        return new Reader(input);
    }

    /**
     * Creates a new writer.
     * @param output the output stream to write a file list
     * @return the created writer
     * @throws IOException if failed to prepare a file list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FileList.Writer createWriter(OutputStream output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        return new Writer(output);
    }

    private FileList() {
        return;
    }

    /**
     * A {@link FileList} read protocol.
     * @since 0.2.3
     */
    public static class Reader implements Closeable {

        private final ZipInputStream input;

        private final FileStatus current = new FileStatus();

        private final DataInputBuffer buffer = new DataInputBuffer();

        private boolean sawNext;

        private boolean sawEof;

        Reader(InputStream input) throws IOException {
            assert input != null;
            this.input = new ZipInputStream(input);
            ZipEntry first = this.input.getNextEntry();
            if (first == null || first.getName().equals(FIRST_ENTRY_NAME) == false) {
                throw new IOException("file list is broken");
            }
        }

        /**
         * Returns true iff the next sequence file exists,
         * and then the {@link #getCurrentFile()} and {@link #openContent()} method returns it.
         * @return {@code true} if the next data model object exists, otherwise {@code false}
         * @throws IOException if failed to prepare the next data
         */
        public boolean next() throws IOException {
            while (sawEof == false) {
                ZipEntry entry = input.getNextEntry();
                if (entry == null) {
                    throw new IOException("Found unexpected end of file in file list");
                }
                if (entry.getName().equals(LAST_ENTRY_NAME)) {
                    sawEof = true;
                    sawNext = false;
                    return false;
                }
                if (entry.isDirectory()) {
                    // may not come here
                    continue;
                }
                LOG.debug("opening {}", entry.getName());
                if (restoreExtra(entry) == false) {
                    throw new IOException(MessageFormat.format(
                            "Invalid file list format: {0}",
                            entry.getName()));
                }
                sawNext = true;
                return true;
            }
            return false;
        }

        private boolean restoreExtra(ZipEntry entry) {
            assert entry != null;
            byte[] extra = entry.getExtra();
            if (extra == null) {
                return false;
            }
            buffer.reset(extra, extra.length);
            try {
                current.readFields(buffer);
            } catch (Exception e) {
                // TODO logging
                LOG.error(MessageFormat.format(
                        "File list is broken (missing file status): {}",
                        entry.getName()), e);
                return false;
            }
            return true;
        }

        /**
         * Opens the current file status prepared by the {@link #next()} method.
         * @return the current sequence file contents
         * @throws IOException if failed to get status
         */
        public FileStatus getCurrentFile() throws IOException {
            checkCurrent();
            return current;
        }

        /**
         * Opens the current file content prepared by the {@link #next()} method.
         * This operation can perform only once for each sequence file.
         * @return the current sequence file contents
         * @throws IOException if failed to open the file
         */
        public InputStream openContent() throws IOException {
            checkCurrent();
            return new ZipEntryInputStream(input);
        }

        private void checkCurrent() throws IOException {
            if (sawNext == false) {
                throw new IOException("current content is not prepared");
            }
        }

        @Override
        public void close() throws IOException {
            sawNext = false;
            input.close();
        }
    }

    /**
     * A {@link FileList} write protocol.
     * @since 0.2.3
     */
    public static class Writer implements Closeable {

        private final ZipOutputStream output;

        private final DataOutputBuffer buffer = new DataOutputBuffer(65536);

        private boolean closed = false;

        Writer(OutputStream output) throws IOException {
            if (output == null) {
                throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
            }
            this.output = new ZipOutputStream(output);
            this.output.setMethod(ZipOutputStream.DEFLATED);
            this.output.setLevel(0);
            this.output.putNextEntry(new ZipEntry(FIRST_ENTRY_NAME));
            this.output.closeEntry();
        }

        /**
         * Creates a next file and opens an {@link OutputStream} to write it content.
         * @param status the status of next file
         * @return the opened {@link OutputStream}
         * @throws IOException if failed to open the file
         * @throws IllegalArgumentException if the status or its path is {@code null}
         */
        public OutputStream openNext(FileStatus status) throws IOException {
            if (status == null) {
                throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
            }
            if (status.getPath() == null) {
                throw new IllegalAccessError("status.path must not be null"); //$NON-NLS-1$
            }
            ZipEntry entry = createEntryFromStatus(status);
            output.putNextEntry(entry);
            return new ZipEntryOutputStream(output);
        }

        private ZipEntry createEntryFromStatus(FileStatus status) throws IOException {
            assert status != null;
            buffer.reset();
            status.write(buffer);
            ZipEntry entry = new ZipEntry(status.getPath().toString());
            entry.setExtra(Arrays.copyOfRange(buffer.getData(), 0, buffer.getLength()));
            return entry;
        }

        @Override
        public void close() throws IOException {
            if (closed == false) {
                output.putNextEntry(new ZipEntry(LAST_ENTRY_NAME));
                output.closeEntry();
                output.close();
            }
            closed = true;
        }
    }
}
