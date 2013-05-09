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
package com.asakusafw.runtime.io.sequencefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;

import com.asakusafw.runtime.compatibility.SequenceFileCompatibility;

/**
 * Accessors for {@link SequenceFile}.
 * @since 0.2.5
 */
public final class SequenceFileUtil {

    static final Log LOG = LogFactory.getLog(SequenceFileUtil.class);

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
                    "Creating sequence file reader for {0}",
                    status.getPath()));
        }
        return SequenceFileCompatibility.openReader(in, status.getLen(), conf);
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
        return SequenceFileCompatibility.openReader(in, length, conf);
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
                    "Creating sequence file writer for output (key={0}, value={0})",
                    keyClass.getName(),
                    valueClass.getName()));
        }
        FSDataOutputStream output = new FSDataOutputStream(out, null);
        if (codec != null) {
            return SequenceFile.createWriter(conf, output, keyClass, valueClass, CompressionType.BLOCK, codec);
        } else {
            return SequenceFile.createWriter(conf, output, keyClass, valueClass, CompressionType.NONE, null);
        }
    }

    private SequenceFileUtil() {
        return;
    }
}
