/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;

/**
 * A file list transfer protocol.
 * @since 0.2.2
 * @version 0.7.4
 */
public final class FileList {

    static final WindGateLogger WGLOG = new HadoopFsLogger(FileList.class);

    static final Logger LOG = LoggerFactory.getLogger(FileList.class);

    static final int PREAMBLE_MARGIN = 128 * 1024;

    static final String FIRST_ENTRY_NAME = ".__FIRST_ENTRY__"; //$NON-NLS-1$

    static final String LAST_ENTRY_NAME = ".__LAST_ENTRY__"; //$NON-NLS-1$

    static final Charset PATH_ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    /**
     * Creates a simple {@link FileStatus}.
     * @param path the target path
     * @return the created file status with the specified path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @deprecated should not use this
     */
    @Deprecated
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
        LOG.debug("Creating a new file list reader");
        byte[] dropped = FileListUtil.dropPreamble(input, PREAMBLE_MARGIN);
        if (dropped.length >= 1) {
            WGLOG.warn("W19002", new String(dropped, Charset.defaultCharset()));
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
        LOG.debug("Creating a new file list writer");
        FileListUtil.putPreamble(output);
        return new Writer(output);
    }

    private FileList() {
        return;
    }

    /**
     * A {@link FileList} read protocol.
     * @since 0.2.2
     * @version 0.7.4
     */
    public static class Reader implements Closeable {

        private final InputStream original;

        private final ZipInputStream input;

        private Path currentPath;

        private boolean sawNext;

        private boolean sawEof;

        Reader(InputStream input) throws IOException {
            assert input != null;
            this.original = input;
            this.input = new ZipInputStream(input);
            ZipEntry first = this.input.getNextEntry();
            if (first == null || first.getName().equals(FIRST_ENTRY_NAME) == false) {
                throw new IOException("file list is broken");
            }
            this.input.closeEntry();
        }

        /**
         * Returns true iff the next temporary file exists,
         * and then the {@link #getCurrentPath()} and {@link #openContent()} method returns it.
         * @return {@code true} if the next data model object exists, otherwise {@code false}
         * @throws IOException if failed to prepare the next data
         */
        public boolean next() throws IOException {
            while (sawEof == false) {
                ZipEntry entry = input.getNextEntry();
                if (entry == null) {
                    throw new IOException("Found unexpected end of file in file list");
                }
                LOG.debug("Opening the next entry in file list: {}", entry.getName());
                if (entry.getName().equals(LAST_ENTRY_NAME)) {
                    sawEof = true;
                    sawNext = false;
                    consume();
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

        private void consume() throws IOException {
            byte[] buf = new byte[1024];
            int rest = 0;
            while (true) {
                int read = original.read(buf);
                if (read < 0) {
                    break;
                }
                rest += read;
            }
            LOG.debug("Consumed tail of file list: {}bytes", rest);
        }

        private boolean restoreExtra(ZipEntry entry) {
            assert entry != null;
            byte[] extra = entry.getExtra();
            if (extra == null) {
                return false;
            }
            try {
                FileInfo info = FileInfo.fromBytes(extra);
                currentPath = new Path(info.getUri());
            } catch (Exception e) {
                WGLOG.warn(e, "W19001",
                        entry.getName());
                return false;
            }
            return true;
        }

        /**
         * Opens the current file path prepared by the {@link #next()} method.
         * @return the current temporary file contents
         * @throws IOException if failed to get status
         * @since 0.7.4
         */
        public Path getCurrentPath() throws IOException {
            checkCurrent();
            return currentPath;
        }

        /**
         * Opens the current file status prepared by the {@link #next()} method.
         * @return the current temporary file contents
         * @throws IOException if failed to get status
         * @deprecated Use {@link #getCurrentPath()} instead
         */
        @Deprecated
        public FileStatus getCurrentFile() throws IOException {
            checkCurrent();
            return createFileStatus(currentPath);
        }

        /**
         * Opens the current file content prepared by the {@link #next()} method.
         * This operation can perform only once for each temporary file.
         * @return the current temporary file contents
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
            LOG.debug("Closing file list reader");
            sawNext = false;
            input.close();
        }
    }

    /**
     * A {@link FileList} write protocol.
     * @since 0.2.2
     * @version 0.7.4
     */
    public static class Writer implements Closeable {

        private final ZipOutputStream output;

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
         * @deprecated Use {@link #openNext(Path)} instead
         */
        @Deprecated
        public OutputStream openNext(FileStatus status) throws IOException {
            if (status == null) {
                throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
            }
            if (status.getPath() == null) {
                throw new IllegalAccessError("status.path must not be null"); //$NON-NLS-1$
            }
            return openNext(status.getPath());
        }

        /**
         * Creates a next file and opens an {@link OutputStream} to write it content.
         * @param path the next file path
         * @return the opened {@link OutputStream}
         * @throws IOException if failed to open the file
         * @throws IllegalArgumentException if the path is {@code null}
         */
        public OutputStream openNext(Path path) throws IOException {
            if (path == null) {
                throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
            }
            ZipEntry entry = createEntry(new FileInfo(path.toString()));
            LOG.debug("Putting next entry: {}", entry.getName());
            output.putNextEntry(entry);
            return new ZipEntryOutputStream(output);
        }

        private ZipEntry createEntry(FileInfo info) {
            ZipEntry entry = new ZipEntry(info.getUri());
            entry.setExtra(info.toBytes());
            return entry;
        }

        @Override
        public void close() throws IOException {
            if (closed == false) {
                LOG.debug("Closing file list writer");
                output.putNextEntry(new ZipEntry(LAST_ENTRY_NAME));
                output.closeEntry();
                output.close();
                LOG.debug("Closed file list writer");
            }
            closed = true;
        }
    }
}
