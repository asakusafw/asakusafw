/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.parquet.v2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties.WriterVersion;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An implementation of {@link ModelOutput} for writing Parquet files.
 * @param <T> the data model type
 * @since 0.7.0
 * @version 0.10.3
 */
public class ParquetFileOutput<T> implements ModelOutput<T> {

    static final Log LOG = LogFactory.getLog(ParquetFileOutput.class);

    private final DataModelDescriptor descriptor;

    private final Configuration configuration;

    private final DataModelWriteSupport writeSupport;

    private final Path path;

    private final ParquetFileOutput.Options options;

    private final Counter counter;

    private ParquetWriter<T> currentWriter;

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param configuration the hadoop configuration
     * @param path the path to the target file
     * @param options the parquet file output options
     * @param counter the current counter
     */
    public ParquetFileOutput(
            DataModelDescriptor descriptor,
            Configuration configuration,
            Path path,
            ParquetFileOutput.Options options,
            Counter counter) {
        this.writeSupport = new DataModelWriteSupport(descriptor);
        this.descriptor = descriptor;
        this.configuration = configuration;
        this.path = path;
        this.options = options;
        this.counter = counter;
    }

    @Override
    public void write(T model) throws IOException {
        ParquetWriter<T> writer = prepareWriter();
        writer.write(model);

        // NOTE: only tell this is alive
        counter.add(0);
    }

    @SuppressWarnings("unchecked")
    private ParquetWriter<T> prepareWriter() throws IOException {
        ParquetWriter<T> writer = currentWriter;
        if (writer == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("ParquetFileOutput.infoCreate"), //$NON-NLS-1$
                        descriptor.getDataModelClass().getSimpleName(),
                        path));
            }
            writer = new WriterBuilder<>(path, (WriteSupport<T>) writeSupport)
                    .withCompressionCodec(options.getCompressionCodecName())
                    .withRowGroupSize(options.getBlockSize())
                    .withPageSize(options.getDataPageSize())
                    .withDictionaryPageSize(options.getDictionaryPageSize())
                    .withDictionaryEncoding(options.isEnableDictionary())
                    .withValidation(options.isEnableValidation())
                    .withWriterVersion(options.getWriterVersion())
                    .withConf(configuration)
                    .build();

            currentWriter = writer;
        }
        return writer;
    }

    @Override
    public void close() throws IOException {
        if (currentWriter != null) {
            currentWriter.close();
            currentWriter = null;
            counter.add(getFileSize());
        }
    }

    private long getFileSize() {
        try {
            FileSystem fs = path.getFileSystem(configuration);
            FileStatus status = fs.getFileStatus(path);
            return status.getLen();
        } catch (FileNotFoundException e) {
            LOG.debug(MessageFormat.format(
                    "cannot obtain the Parquet file size: {0}",
                    path), e);
        } catch (IOException e) {
            LOG.warn(MessageFormat.format(
                    "cannot obtain the Parquet file size: {0}",
                    path), e);
        }
        return 0;
    }

    private static final class WriterBuilder<T> extends ParquetWriter.Builder<T, WriterBuilder<T>> {

        WriteSupport<T> support;

        WriterBuilder(Path file, WriteSupport<T> support) {
            super(file);
            this.support = support;
        }

        @Override
        protected WriterBuilder<T> self() {
            return this;
        }

        @Override
        protected WriteSupport<T> getWriteSupport(Configuration conf) {
            return support;
        }
    }

    /**
     * The parquet file writing options.
     * @since 0.7.0
     */
    public static final class Options {

        private CompressionCodecName compressionCodecName = CompressionCodecName.SNAPPY;

        private int blockSize = ParquetWriter.DEFAULT_BLOCK_SIZE;

        private int dataPageSize = ParquetWriter.DEFAULT_PAGE_SIZE;

        private int dictionaryPageSize = ParquetWriter.DEFAULT_PAGE_SIZE;

        private boolean enableDictionary = true;

        private boolean enableValidation = false;

        private WriterVersion writerVersion = WriterVersion.PARQUET_1_0;

        /**
         * Returns the compression codec name.
         * @return the compression codec name
         */
        public CompressionCodecName getCompressionCodecName() {
            return compressionCodecName;
        }

        /**
         * Sets the compression codec name.
         * @param value the value
         */
        public void setCompressionCodecName(CompressionCodecName value) {
            this.compressionCodecName = value;
        }

        /**
         * Returns the block size (in bytes).
         * @return the block size
         */
        public int getBlockSize() {
            return blockSize;
        }

        /**
         * Sets the block size (in bytes).
         * @param value the value
         */
        public void setBlockSize(int value) {
            this.blockSize = value;
        }

        /**
         * Returns the data page size (in bytes).
         * @return the data page size
         */
        public int getDataPageSize() {
            return dataPageSize;
        }

        /**
         * Sets the data page size (in bytes).
         * @param value the value
         */
        public void setDataPageSize(int value) {
            this.dataPageSize = value;
        }

        /**
         * Returns the dictionary page size (in bytes).
         * @return the dictionary page size
         */
        public int getDictionaryPageSize() {
            return dictionaryPageSize;
        }

        /**
         * Sets the dictionary page size (in bytes).
         * @param value the value
         */
        public void setDictionaryPageSize(int value) {
            this.dictionaryPageSize = value;
        }

        /**
         * Returns whether the dictionary is enabled or not.
         * @return {@code true} if enabled, otherwise {@code false}
         */
        public boolean isEnableDictionary() {
            return enableDictionary;
        }

        /**
         * Sets whether the dictionary is enabled or not.
         * @param value the value
         */
        public void setEnableDictionary(boolean value) {
            this.enableDictionary = value;
        }

        /**
         * Returns whether the schema validation is enabled or not.
         * @return {@code true} if enabled, otherwise {@code false}
         */
        public boolean isEnableValidation() {
            return enableValidation;
        }

        /**
         * Sets whether the schema validation is enabled or not.
         * @param value the value
         */
        public void setEnableValidation(boolean value) {
            this.enableValidation = value;
        }

        /**
         * Returns the writer version.
         * @return the writer version
         */
        public WriterVersion getWriterVersion() {
            return writerVersion;
        }

        /**
         * Sets the writer version.
         * @param value the value
         */
        public void setWriterVersion(WriterVersion value) {
            this.writerVersion = value;
        }
    }
}
