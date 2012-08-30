/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Data model format of {@link InputStream} / {@link OutputStream} .
 * This implementation class must have a public constructor without any parameters.
 * @param <K> the type of raw sequence file key
 * @param <V> the type of raw sequence file value
 * @param <T> the type of target data model
 * @since 0.2.6
 * @version 0.4.0
 */
public abstract class SequenceFileFormat<K, V, T> extends HadoopFileFormat<T> {

    static final Log LOG = LogFactory.getLog(SequenceFileFormat.class);

    static final String KEY_COMPRESSION_CODEC = "com.asakusafw.output.sequencefile.compression.codec";

    static final String VALUE_COMPRESSION_AUTO = "auto";

    @Override
    public long getPreferredFragmentSize() throws IOException, InterruptedException {
        return -1L;
    }

    @Override
    public long getMinimumFragmentSize() throws IOException, InterruptedException {
        return SequenceFile.SYNC_INTERVAL;
    }

    /**
     * Returns a key object.
     * @return a key object
     */
    protected abstract K createKeyObject();

    /**
     * Returns a value object.
     * @return a value object
     */
    protected abstract V createValueObject();

    /**
     * Copy key and value into the target data model.
     * @param key the source key object
     * @param value the source value object
     * @param model the target data model
     * @throws IOException if failed to copy
     */
    protected abstract void copyToModel(K key, V value, T model) throws IOException;

    /**
     * Copy the data model into the key and value.
     * @param model the source data model
     * @param key the target key object
     * @param value the target value object
     * @throws IOException if failed to copy
     */
    protected abstract void copyFromModel(T model, K key, V value) throws IOException;

    @Override
    public ModelInput<T> createInput(
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            long offset,
            long fragmentSize,
            final Counter counter) throws IOException, InterruptedException {
        final long end = offset + fragmentSize;
        final K keyBuffer = createKeyObject();
        final V valueBuffer = createValueObject();
        final SequenceFile.Reader reader;
        try {
            reader = new SequenceFile.Reader(fileSystem, path, getConf());
        } catch (EOFException e) {
            FileStatus status = fileSystem.getFileStatus(path);
            if (status.getLen() == 0L) {
                LOG.warn(MessageFormat.format(
                        "Target sequence file is empty: {0}",
                        path));
                return new ModelInput<T>() {
                    @Override
                    public boolean readTo(T model) throws IOException {
                        return false;
                    }
                    @Override
                    public void close() throws IOException {
                        return;
                    }
                };
            }
            throw e;
        }
        boolean succeed = false;
        try {
            if (offset > reader.getPosition()) {
                reader.sync(offset);
            }
            ModelInput<T> result = new ModelInput<T>() {

                private boolean next = reader.getPosition() < end;

                private long lastPosition = reader.getPosition();

                @Override
                public boolean readTo(T model) throws IOException {
                    if (next == false) {
                        return false;
                    }
                    long current = reader.getPosition();
                    @SuppressWarnings("unchecked")
                    K key = (K) reader.next(keyBuffer);
                    if (key == null || (current >= end && reader.syncSeen())) {
                        next = false;
                        return false;
                    } else {
                        reader.getCurrentValue(valueBuffer);
                        SequenceFileFormat.this.copyToModel(keyBuffer, valueBuffer, model);
                        long nextPosition = reader.getPosition();
                        counter.add(nextPosition - lastPosition);
                        lastPosition = nextPosition;
                        return true;
                    }
                }

                @Override
                public void close() throws IOException {
                    reader.close();
                }
            };
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                reader.close();
            }
        }
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            final Counter counter) throws IOException, InterruptedException {
        final K keyBuffer = createKeyObject();
        final V valueBuffer = createValueObject();
        CompressionCodec codec = getCompressionCodec(path);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Creating sequence file (path={0}, type={1}, codec={2})",
                    path,
                    dataType.getName(),
                    codec));
        }
        configure(codec);
        final SequenceFile.Writer writer = SequenceFile.createWriter(
                fileSystem,
                getConf(),
                path,
                keyBuffer.getClass(),
                valueBuffer.getClass(),
                codec == null ? CompressionType.NONE : CompressionType.BLOCK,
                codec);
        boolean succeed = false;
        try {
            ModelOutput<T> output = new ModelOutput<T>() {

                private long lastPosition = 0;

                @Override
                public void write(T model) throws IOException {
                    copyFromModel(model, keyBuffer, valueBuffer);
                    writer.append(keyBuffer, valueBuffer);
                    long nextPosition = writer.getLength();
                    counter.add(nextPosition - lastPosition);
                    lastPosition = nextPosition;
                }

                @Override
                public void close() throws IOException {
                    writer.close();
                }
            };
            succeed = true;
            return output;
        } finally {
            if (succeed == false) {
                writer.close();
            }
        }
    }

    private void configure(Object object) {
        if (object instanceof Configurable) {
            Configurable configurable = (Configurable) object;
            if (configurable.getConf() == null) {
                configurable.setConf(getConf());
            }
        }
    }

    /**
     * Returns a compression codec for output sequence files.
     * Clients can override this method in subclasses, and return the suitable {@link CompressionCodec} object.
     * @param path target path
     * @return a compression codec used to output, or {@code null} if output will not be compressed
     * @throws IOException if failed to create a compression codec
     * @throws InterruptedException if interrupted
     */
    public CompressionCodec getCompressionCodec(Path path) throws IOException, InterruptedException {
        String codecClassName = getConf().get(KEY_COMPRESSION_CODEC);
        if (codecClassName != null && codecClassName.isEmpty() == false) {
            try {
                Class<?> codecClass = getConf().getClassByName(codecClassName);
                return ReflectionUtils.newInstance(codecClass.asSubclass(CompressionCodec.class), getConf());
            } catch (Exception e) {
                LOG.warn(MessageFormat.format(
                        "Failed to load compression codec ({0}={1})",
                        KEY_COMPRESSION_CODEC,
                        codecClassName), e);
                return null;
            }
        }
        return null;
    }
}
