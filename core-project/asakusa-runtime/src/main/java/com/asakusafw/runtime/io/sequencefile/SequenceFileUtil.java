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
package com.asakusafw.runtime.io.sequencefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;

/**
 * Accessors for {@link SequenceFile}.
 * @since 0.2.5
 */
public final class SequenceFileUtil {

    static final Log LOG = LogFactory.getLog(SequenceFileUtil.class);

    private SequenceFileUtil() {
        return;
    }

    /**
     * Creates a new reader.
     * @param in the source
     * @param status target file status
     * @param conf current configuration
     * @return the created sequence file reader
     * @throws IOException if failed to open the sequence file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static SequenceFile.Reader openReader(
            InputStream in,
            FileStatus status,
            Configuration conf) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Creating sequence file reader for {0}", //$NON-NLS-1$
                    status.getPath()));
        }
        return new SequenceFile.Reader(
                conf,
                SequenceFile.Reader.stream(new FSDataInputStream(new WrappedInputStream(in))),
                SequenceFile.Reader.length(status.getLen()));
    }

    /**
     * Creates a new reader.
     * @param in the source
     * @param length the stream length
     * @param conf current configuration
     * @return the created sequence file reader
     * @throws IOException if failed to open the sequence file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static SequenceFile.Reader openReader(
            InputStream in,
            long length,
            Configuration conf) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        return new SequenceFile.Reader(
                conf,
                SequenceFile.Reader.stream(new FSDataInputStream(new WrappedInputStream(in))),
                SequenceFile.Reader.length(length));
    }

    /**
     * Creates a new writer.
     * @param out the drain
     * @param conf current configuration
     * @param keyClass the key type
     * @param valueClass the value type
     * @param codec the compression codec to block compression, or {@code null} to uncompressed
     * @return the created sequence file writer
     * @throws IOException if failed to create a sequence file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static SequenceFile.Writer openWriter(
            OutputStream out, Configuration conf,
            Class<?> keyClass, Class<?> valueClass,
            CompressionCodec codec) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out must not be null"); //$NON-NLS-1$
        }
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (keyClass == null) {
            throw new IllegalArgumentException("keyClass must not be null"); //$NON-NLS-1$
        }
        if (valueClass == null) {
            throw new IllegalArgumentException("valueClass must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Creating sequence file writer for output (key={0}, value={0})", //$NON-NLS-1$
                    keyClass.getName(),
                    valueClass.getName()));
        }
        return SequenceFile.createWriter(conf,
                SequenceFile.Writer.stream(new FSDataOutputStream(out, null)),
                SequenceFile.Writer.keyClass(keyClass),
                SequenceFile.Writer.valueClass(valueClass),
                SequenceFile.Writer.compression(codec == null ? CompressionType.NONE : CompressionType.BLOCK, codec));
    }

    private static class WrappedInputStream extends InputStream implements Seekable, PositionedReadable {

        private final InputStream input;

        private long current;

        WrappedInputStream(InputStream input) {
            assert input != null;
            this.input = input;
            this.current = 0L;
        }

        @Override
        public int read(long position, byte[] buffer, int offset, int length) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void readFully(long position, byte[] buffer) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void seek(long pos) throws IOException {
            if (pos < current) {
                throw new UnsupportedOperationException();
            }
            while (pos > current) {
                skip0(pos - current);
            }
        }

        @Override
        public long getPos() throws IOException {
            return current;
        }

        @Override
        public boolean seekToNewSource(long targetPos) throws IOException {
            return false;
        }

        @Override
        public int read() throws IOException {
            int result = input.read();
            if (result >= 0) {
                current++;
            }
            return result;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int result = input.read(b);
            if (result >= 0) {
                current += result;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = input.read(b, off, len);
            if (result >= 0) {
                current += result;
            }
            return result;
        }

        @Override
        public long skip(long n) throws IOException {
            return skip0(n);
        }

        private long skip0(long n) throws IOException {
            long result = input.skip(n);
            if (result >= 0) {
                current += result;
            }
            return result;
        }

        @Override
        public int available() throws IOException {
            return input.available();
        }

        @Override
        public void close() throws IOException {
            input.close();
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public synchronized void mark(int readlimit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void reset() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
