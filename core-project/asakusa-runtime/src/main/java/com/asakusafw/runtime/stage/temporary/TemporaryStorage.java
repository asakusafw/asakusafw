/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.temporary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.CompressionCodec;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.sequencefile.SequenceFileModelInput;
import com.asakusafw.runtime.io.sequencefile.SequenceFileModelOutput;
import com.asakusafw.runtime.io.sequencefile.SequenceFileUtil;

/**
 * Access to the temporary storage.
 * @since 0.2.5
 */
public final class TemporaryStorage {

    static final Log LOG = LogFactory.getLog(TemporaryStorage.class);

    /**
     * Resolves the raw path pattern into the concrete path list.
     * @param conf current configuration
     * @param pathPattern path pattern which describes temporary storage
     * @return the resolved paths
     * @throws IOException if failed to resolve path pattern
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<Path> list(Configuration conf, Path pathPattern) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (pathPattern == null) {
            throw new IllegalArgumentException("pathPattern must not be null"); //$NON-NLS-1$
        }
        FileSystem fs = pathPattern.getFileSystem(conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Listing temporary input: {0} (fs={1})",
                    pathPattern,
                    fs.getUri()));
        }
        FileStatus[] statusList = fs.globStatus(pathPattern);
        if (statusList == null || statusList.length == 0) {
            return Collections.emptyList();
        }
        List<Path> results = new ArrayList<Path>();
        for (FileStatus status : statusList) {
            results.add(status.getPath());
        }
        return results;
    }

    /**
     * Opens a temporary {@link ModelInput} for the specified path.
     * @param <V> data type
     * @param conf configuration
     * @param dataType data type
     * @param path source path (must not contain wildcards)
     * @return the opened {@link ModelInput}
     * @throws IOException if failed to open input
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <V> ModelInput<V> openInput(
            Configuration conf,
            Class<V> dataType,
            Path path) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        FileSystem fs = path.getFileSystem(conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Opening temporary input: {0} (fs={1})",
                    path,
                    fs.getUri()));
        }
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        return (ModelInput<V>) new SequenceFileModelInput<Writable>(reader);
    }

    /**
     * Opens a temporary {@link ModelInput} for the specified path.
     * @param <V> data type
     * @param conf configuration
     * @param dataType data type
     * @param status source file status
     * @param input source file content
     * @return the opened {@link ModelInput}
     * @throws IOException if failed to open input
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <V> ModelInput<V> openInput(
            Configuration conf,
            Class<V> dataType,
            FileStatus status,
            InputStream input) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        SequenceFile.Reader reader = SequenceFileUtil.openReader(input, status, conf);
        return (ModelInput<V>) new SequenceFileModelInput<Writable>(reader, input);
    }

    /**
     * Opens a temporary {@link ModelOutput} for the specified path.
     * @param <V> data type
     * @param conf configuration
     * @param dataType data type
     * @param path target path
     * @return the opened {@link ModelOutput}
     * @throws IOException if failed to open output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <V> ModelOutput<V> openOutput(
            Configuration conf,
            Class<V> dataType,
            Path path) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        FileSystem fs = path.getFileSystem(conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Opening temporary output: {0} (fs={1})",
                    path,
                    fs.getUri()));
        }
        SequenceFile.Writer out = SequenceFile.createWriter(
                fs,
                conf,
                path,
                NullWritable.class,
                dataType);
        return new SequenceFileModelOutput<V>(out);
    }

    /**
     * Opens a temporary {@link ModelOutput} for the specified path.
     * @param <V> data type
     * @param conf configuration
     * @param dataType data type
     * @param path target path
     * @param compressionCodec compression codec, or null if not compressed
     * @return the opened {@link ModelOutput}
     * @throws IOException if failed to open output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <V> ModelOutput<V> openOutput(
            Configuration conf,
            Class<V> dataType,
            Path path,
            CompressionCodec compressionCodec) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        FileSystem fs = path.getFileSystem(conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Opening temporary output: {0} (fs={1})",
                    path,
                    fs.getUri()));
        }
        SequenceFile.Writer out;
        if (compressionCodec == null) {
            out = SequenceFile.createWriter(
                    fs,
                    conf,
                    path,
                    NullWritable.class,
                    dataType,
                    CompressionType.NONE);
        } else {
            out = SequenceFile.createWriter(
                    fs,
                    conf,
                    path,
                    NullWritable.class,
                    dataType,
                    CompressionType.BLOCK,
                    compressionCodec);
        }
        return new SequenceFileModelOutput<V>(out);
    }

    /**
     * Opens a temporary {@link ModelOutput} for the specified output.
     * @param <V> data type
     * @param conf configuration
     * @param dataType data type
     * @param output target output stream
     * @return the opened {@link ModelOutput}
     * @throws IOException if failed to open output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <V> ModelOutput<V> openOutput(
            Configuration conf,
            Class<V> dataType,
            OutputStream output) throws IOException {
        return openOutput(conf, dataType, output, null);
    }

    /**
     * Opens a temporary {@link ModelOutput} for the specified output.
     * @param <V> data type
     * @param conf configuration
     * @param dataType data type
     * @param output target output stream
     * @param compressionCodec compression codec, or null if not compressed
     * @return the opened {@link ModelOutput}
     * @throws IOException if failed to open output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <V> ModelOutput<V> openOutput(
            Configuration conf,
            Class<V> dataType,
            OutputStream output,
            CompressionCodec compressionCodec) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        SequenceFile.Writer out = SequenceFileUtil.openWriter(
                output, conf, NullWritable.class, dataType, compressionCodec);
        return new SequenceFileModelOutput<V>(out);
    }

    private TemporaryStorage() {
        return;
    }
}
