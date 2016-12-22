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
package com.asakusafw.directio.hive.parquet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.directio.hive.info.BuiltinStorageFormatInfo;
import com.asakusafw.directio.hive.info.StorageFormatInfo;
import com.asakusafw.directio.hive.info.TableInfo;
import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.directio.hive.serde.PropertyDescriptor;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.BlockMap;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

import parquet.column.ParquetProperties.WriterVersion;
import parquet.hadoop.Footer;
import parquet.hadoop.ParquetFileReader;
import parquet.hadoop.metadata.BlockMetaData;
import parquet.hadoop.metadata.ColumnChunkMetaData;
import parquet.hadoop.metadata.CompressionCodecName;

/**
 * An abstract implementation of {@link HadoopFileFormat} for Parquet.
 * @param <T> the data model type
 * @since 0.7.0
 */
public abstract class AbstractParquetFileFormat<T> extends HadoopFileFormat<T>
        implements StripedDataFormat<T>, TableInfo.Provider {

    static final Log LOG = LogFactory.getLog(AbstractParquetFileFormat.class);

    /**
     * Returns the format configuration.
     * @return the format configuration
     */
    public abstract ParquetFormatConfiguration getFormatConfiguration();

    /**
     * Returns the target data model descriptor.
     * @return the target data model descriptor
     */
    public abstract DataModelDescriptor getDataModelDescriptor();

    /**
     * Returns the table name.
     * @return the table name
     */
    public abstract String getTableName();

    @Override
    public TableInfo getSchema() {
        DataModelDescriptor desc = getDataModelDescriptor();
        TableInfo.Builder builder = new TableInfo.Builder(getTableName());
        for (PropertyDescriptor property : desc.getPropertyDescriptors()) {
            builder.withColumn(property.getSchema());
        }
        builder.withComment(desc.getDataModelComment());
        builder.withStorageFormat(BuiltinStorageFormatInfo.of(StorageFormatInfo.FormatKind.PARQUET));
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getSupportedType() {
        return (Class<T>) getDataModelDescriptor().getDataModelClass();
    }

    @Override
    public List<DirectInputFragment> computeInputFragments(
            InputContext context) throws IOException, InterruptedException {
        List<DirectInputFragment> results = new ArrayList<>();
        List<FileStatus> files = new ArrayList<>(context.getInputFiles());
        Map<Path, FileStatus> pathMap = new HashMap<>();
        for (FileStatus status : files) {
            pathMap.put(status.getPath(), status);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(MessageFormat.format(
                    Messages.getString("AbstractParquetFileFormat.infoLoadMetadata"), //$NON-NLS-1$
                    context.getDataType().getSimpleName(),
                    files.size()));
        }
        List<Footer> footers = ParquetFileReader.readAllFootersInParallel(getConf(), files);
        for (Footer footer : footers) {
            Path path = footer.getFile();
            FileStatus status = pathMap.get(path);
            if (status == null) {
                // may not occur
                status = context.getFileSystem().getFileStatus(path);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("AbstractParquetFileFormat.infoAnalyzeMetadata"), //$NON-NLS-1$
                        context.getDataType().getSimpleName(),
                        status.getPath()));
            }
            BlockMap blockMap = BlockMap.create(
                    status.getPath().toString(),
                    status.getLen(),
                    BlockMap.computeBlocks(context.getFileSystem(), status),
                    false);
            for (BlockMetaData block : footer.getParquetMetadata().getBlocks()) {
                if (block.getColumns().isEmpty()) {
                    continue;
                }
                long begin = Long.MAX_VALUE;
                long end = -1L;
                for (ColumnChunkMetaData column : block.getColumns()) {
                    long offset = column.getFirstDataPageOffset();
                    long size = column.getTotalSize();
                    begin = Math.min(begin, offset);
                    end = Math.max(end, offset + size);
                }
                assert begin >= 0;
                assert end >= 0;
                DirectInputFragment fragment = blockMap.get(begin, end);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Detect Parquet file block: " //$NON-NLS-1$
                            + "path={0}, rows={1}, range={2}+{3}, allocation={4}", //$NON-NLS-1$
                            status.getPath(),
                            block.getRowCount(),
                            begin,
                            end - begin,
                            fragment.getOwnerNodeNames()));
                }
                results.add(fragment);
            }
        }
        return results;
    }

    @Override
    public ModelInput<T> createInput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            long offset, long fragmentSize,
            Counter counter) throws IOException, InterruptedException {
        DataModelMapping driverConf = new DataModelMapping();
        ParquetFormatConfiguration conf = getFormatConfiguration();
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Parquet file input ({0}): {1}", //$NON-NLS-1$
                    path,
                    conf));
        }
        if (conf.getFieldMappingStrategy() != null) {
            driverConf.setFieldMappingStrategy(conf.getFieldMappingStrategy());
        }
        if (conf.getOnMissingSource() != null) {
            driverConf.setOnMissingSource(conf.getOnMissingSource());
        }
        if (conf.getOnMissingTarget() != null) {
            driverConf.setOnMissingTarget(conf.getOnMissingTarget());
        }
        if (conf.getOnIncompatibleType() != null) {
            driverConf.setOnIncompatibleType(conf.getOnIncompatibleType());
        }
        long size = fragmentSize;
        if (size < 0L) {
            FileStatus stat = fileSystem.getFileStatus(path);
            size = stat.getLen();
        }
        return new ParquetFileInput<>(
                getDataModelDescriptor(),
                driverConf,
                getConf(), path,
                offset, size,
                counter);
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            Counter counter) throws IOException, InterruptedException {
        ParquetFileOutput.Options options = new ParquetFileOutput.Options();
        ParquetFormatConfiguration conf = getFormatConfiguration();
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Parquet file output ({0}): {1}", //$NON-NLS-1$
                    path,
                    conf));
        }
        CompressionCodecName compress = conf.getCompressionCodecName();
        if (compress != null) {
            options.setCompressionCodecName(compress);
        }
        Integer block = conf.getBlockSize();
        if (block != null) {
            options.setBlockSize(block);
        }
        Integer dataPage = conf.getDataPageSize();
        if (dataPage != null) {
            options.setDataPageSize(dataPage);
        }
        Integer dictPage = conf.getDictionaryPageSize();
        if (dictPage != null) {
            options.setDictionaryPageSize(dictPage);
        }
        Boolean useDict = conf.getEnableDictionary();
        if (useDict != null) {
            options.setEnableDictionary(useDict);
        }
        Boolean validate = conf.getEnableValidation();
        if (validate != null) {
            options.setEnableValidation(validate);
        }
        WriterVersion version = conf.getWriterVersion();
        if (version != null) {
            options.setWriterVersion(version);
        }
        return new ParquetFileOutput<>(
                getDataModelDescriptor(),
                getConf(), path,
                options,
                counter);
    }

}
