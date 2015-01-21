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
package com.asakusafw.runtime.directio.hadoop;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.util.CountInputStream;
import com.asakusafw.runtime.directio.util.CountOutputStream;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Adapts {@link BinaryStreamFormat} into {@link HadoopFileFormat}.
 * @param <T> the type of target data model
 * @since 0.2.6
 */
public class HadoopFileFormatAdapter<T> extends HadoopFileFormat<T> {

    static final Log LOG = LogFactory.getLog(HadoopFileFormatAdapter.class);

    private final BinaryStreamFormat<T> streamFormat;

    /**
     * Creates a new instance.
     * @param streamFormat adaption target
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HadoopFileFormatAdapter(BinaryStreamFormat<T> streamFormat) {
        super();
        if (streamFormat == null) {
            throw new IllegalArgumentException("streamFormat must not be null"); //$NON-NLS-1$
        }
        this.streamFormat = streamFormat;
    }

    /**
     * Creates a new instance.
     * @param streamFormat adaption target
     * @param configuration initial configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HadoopFileFormatAdapter(BinaryStreamFormat<T> streamFormat, Configuration configuration) {
        super(configuration);
        if (streamFormat == null) {
            throw new IllegalArgumentException("streamFormat must not be null"); //$NON-NLS-1$
        }
        this.streamFormat = streamFormat;
    }

    @Override
    public Class<T> getSupportedType() {
        return streamFormat.getSupportedType();
    }

    @Override
    public long getPreferredFragmentSize() throws IOException, InterruptedException {
        return streamFormat.getPreferredFragmentSize();
    }

    @Override
    public long getMinimumFragmentSize() throws IOException, InterruptedException {
        return streamFormat.getMinimumFragmentSize();
    }

    @Override
    public ModelInput<T> createInput(
            Class<? extends T> dataType,
            FileSystem fileSystem,
            final Path path,
            final long offset,
            final long fragmentSize,
            Counter counter) throws IOException, InterruptedException {
        FSDataInputStream stream = fileSystem.open(path);
        boolean succeed = false;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Process opening input [stream opened] (path={0}, offset={1}, size={2})", //$NON-NLS-1$
                        path,
                        offset,
                        fragmentSize));
            }
            if (offset != 0) {
                stream.seek(offset);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Process opening input [sought to offset] (path={0}, offset={1}, size={2})", //$NON-NLS-1$
                            path,
                            offset,
                            fragmentSize));
                }
            }
            CountInputStream cstream;
            if (LOG.isDebugEnabled()) {
                cstream = new CountInputStream(stream, counter) {
                    @Override
                    public void close() throws IOException {
                        LOG.debug(MessageFormat.format(
                                "Start closing input (path={0}, offset={1}, size={2})", //$NON-NLS-1$
                                path,
                                offset,
                                fragmentSize));
                        super.close();
                        LOG.debug(MessageFormat.format(
                                "Finish closing input (path={0}, offset={1}, size={2})", //$NON-NLS-1$
                                path,
                                offset,
                                fragmentSize));
                    }
                };
            } else {
                cstream = new CountInputStream(stream, counter);
            }
            ModelInput<T> input = streamFormat.createInput(dataType, path.toString(), cstream, offset, fragmentSize);
            succeed = true;
            return input;
        } finally {
            if (succeed == false) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to close input (path={0}, offset={1}, size={2})",
                            path,
                            offset,
                            fragmentSize), e);
                }
            }
        }
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            FileSystem fileSystem,
            final Path path,
            Counter counter) throws IOException, InterruptedException {
        FSDataOutputStream stream = fileSystem.create(path);
        boolean succeed = false;
        try {
            CountOutputStream cstream;
            if (LOG.isDebugEnabled()) {
                cstream = new CountOutputStream(stream, counter) {
                    @Override
                    public void close() throws IOException {
                        LOG.debug(MessageFormat.format(
                                "Start closing output (file={0})", //$NON-NLS-1$
                                path));
                        super.close();
                        LOG.debug(MessageFormat.format(
                                "Finish closing output (file={0})", //$NON-NLS-1$
                                path));
                    }
                };
            } else {
                cstream = new CountOutputStream(stream, counter);
            }
            ModelOutput<T> output = streamFormat.createOutput(dataType, path.toString(), cstream);
            succeed = true;
            return output;
        } finally {
            if (succeed == false) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to close output (path={0})",
                            path), e);
                }
            }
        }
    }
}
