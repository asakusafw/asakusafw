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
package com.asakusafw.bulkloader.transfer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.InputBuffer;
import org.apache.hadoop.io.OutputBuffer;

import com.asakusafw.runtime.io.ZipEntryInputStream;
import com.asakusafw.runtime.io.ZipEntryOutputStream;

/**
 * A cache list transfer protocol.
 * @since 0.2.3
 */
public final class FileList {

    static final Log LOG = LogFactory.getLog(FileList.class);

    static final String FIRST_ENTRY_NAME = ".__FIRST_ENTRY__"; //$NON-NLS-1$

    static final String LAST_ENTRY_NAME = ".__LAST_ENTRY__"; //$NON-NLS-1$

    /**
     * Creates a protocol object for send plain contents.
     * @param name target file name
     * @return the created object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FileProtocol content(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return new FileProtocol(FileProtocol.Kind.CONTENT, name, null);
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
        return new Reader(input);
    }

    /**
     * Creates a new writer.
     * @param output the output stream to write a file list
     * @param compress {@code true} to compress stream
     * @return the created writer
     * @throws IOException if failed to prepare a file list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static FileList.Writer createWriter(OutputStream output, boolean compress) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating a new file list writer");
        return new Writer(output, compress);
    }

    private FileList() {
        return;
    }

    /**
     * A {@link FileList} read protocol.
     * @since 0.2.2
     */
    public static class Reader implements Closeable {

        private final CountingInputStream counter;

        private final ZipInputStream input;

        private FileProtocol current;

        private final InputBuffer buffer = new InputBuffer();

        private boolean sawNext;

        private boolean sawEof;

        Reader(InputStream input) throws IOException {
            assert input != null;
            this.counter = new CountingInputStream(input);
            this.input = new ZipInputStream(counter);
            ZipEntry first = this.input.getNextEntry();
            if (first == null || first.getName().equals(FIRST_ENTRY_NAME) == false) {
                throw new IOException("file list is broken");
            }
        }

        /**
         * Returns true iff the next sequence file exists,
         * and then the {@link #getCurrentProtocol()} and {@link #openContent()} method returns it.
         * @return {@code true} if the next data model object exists, otherwise {@code false}
         * @throws IOException if failed to prepare the next data
         */
        public boolean next() throws IOException {
            while (sawEof == false) {
                ZipEntry entry = input.getNextEntry();
                if (entry == null) {
                    throw new IOException("Found unexpected end of file in file list");
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("Opening the next entry in file list: {0}", entry.getName()));
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("opening {0}", entry.getName()));
                }
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
                Properties properties = new Properties();
                properties.load(buffer);
                current = FileProtocol.loadFrom(properties);
            } catch (Exception e) {
                // TODO logging
                LOG.warn("Failed to restore protocol information", e);
                return false;
            }
            return true;
        }

        /**
         * Returns the cache protocol for current file prepared by the {@link #next()} method.
         * @return the cache protocol for current file
         * @throws IOException if failed to extract protocol information
         */
        public FileProtocol getCurrentProtocol() throws IOException {
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

        /**
         * Returns number of bytes read.
         * @return number of bytes read
         */
        public long getByteCount() {
            return counter.getByteCount();
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
     */
    public static class Writer implements Closeable {

        private final CountingOutputStream counter;

        private final ZipOutputStream output;

        private final OutputBuffer buffer = new OutputBuffer();

        private boolean closed = false;

        Writer(OutputStream output, boolean compress) throws IOException {
            if (output == null) {
                throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
            }
            this.counter = new CountingOutputStream(output);
            this.output = new ZipOutputStream(counter);
            this.output.setMethod(ZipOutputStream.DEFLATED);
            if (compress == false) {
                this.output.setLevel(0);
            }
            this.output.putNextEntry(new ZipEntry(FIRST_ENTRY_NAME));
            this.output.closeEntry();
        }

        /**
         * Creates a next file and opens an {@link OutputStream} to write it content.
         * @param protocol the protocol of next content
         * @return the opened {@link OutputStream}
         * @throws IOException if failed to open the file
         * @throws IllegalArgumentException if the status or its path is {@code null}
         */
        public OutputStream openNext(FileProtocol protocol) throws IOException {
            if (protocol == null) {
                throw new IllegalArgumentException("protocol must not be null"); //$NON-NLS-1$
            }
            ZipEntry entry = createEntryFromProtocol(protocol);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Putting next entry: {0}", entry.getName()));
            }
            output.putNextEntry(entry);
            return new ZipEntryOutputStream(output);
        }

        private ZipEntry createEntryFromProtocol(FileProtocol protocol) throws IOException {
            assert protocol != null;
            Properties properties = new Properties();
            protocol.storeTo(properties);
            buffer.reset();
            properties.store(buffer, protocol.getLocation());
            ZipEntry entry = new ZipEntry(protocol.getLocation());
            entry.setExtra(Arrays.copyOfRange(buffer.getData(), 0, buffer.getLength()));
            return entry;
        }

        /**
         * Returns number of bytes written.
         * @return number of bytes written
         */
        public long getByteCount() {
            return counter.getByteCount();
        }

        @Override
        public void close() throws IOException {
            if (closed == false) {
                LOG.debug("Closing file list writer");
                output.putNextEntry(new ZipEntry(LAST_ENTRY_NAME));
                output.closeEntry();
                output.close();
            }
            closed = true;
        }
    }
}
