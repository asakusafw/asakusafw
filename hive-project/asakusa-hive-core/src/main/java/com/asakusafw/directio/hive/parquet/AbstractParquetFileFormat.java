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
package com.asakusafw.directio.hive.parquet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parquet.column.ParquetProperties.WriterVersion;
import parquet.hadoop.Footer;
import parquet.hadoop.ParquetFileReader;
import parquet.hadoop.metadata.BlockMetaData;
import parquet.hadoop.metadata.ColumnChunkMetaData;
import parquet.hadoop.metadata.CompressionCodecName;

import com.asakusafw.directio.hive.common.HiveFieldInfo;
import com.asakusafw.directio.hive.common.HiveTableInfo;
import com.asakusafw.directio.hive.common.RowFormatInfo;
import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.BlockMap;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An abstract implementation of {@link HadoopFileFormat} for Parquet.
 * @param <T> the data model type
 * @since 0.7.0
 */
public abstract class AbstractParquetFileFormat<T> extends HadoopFileFormat<T>
        implements StripedDataFormat<T>, HiveTableInfo {

    static final Logger LOG = LoggerFactory.getLogger(AbstractParquetFileFormat.class);

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

    @Override
    public Class<?> getDataModelClass() {
        return getDataModelDescriptor().getDataModelClass();
    }

    @Override
    public String getTableComment() {
        return getDataModelDescriptor().getDataModelComment();
    }

    @Override
    public List<? extends HiveFieldInfo> getFields() {
        return getDataModelDescriptor().getPropertyDescriptors();
    }

    @Override
    public RowFormatInfo getRowFormat() {
        return null;
    }

    @Override
    public String getFormatName() {
        return "PARQUET";
    }

    @Override
    public Map<String, String> getTableProperties() {
        Map<String, String> results = new HashMap<String, String>();
        // no special items
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getSupportedType() {
        return (Class<T>) getDataModelDescriptor().getDataModelClass();
    }

    @Override
    public List<DirectInputFragment> computeInputFragments(
            InputContext context) throws IOException, InterruptedException {
        List<DirectInputFragment> results = new ArrayList<DirectInputFragment>();
        for (FileStatus status : context.getInputFiles()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Computing parquet blocks: {0}",
                        status.getPath()));
            }
            BlockMap blockMap = BlockMap.create(
                    status.getPath().toString(),
                    status.getLen(),
                    BlockMap.computeBlocks(context.getFileSystem(), status),
                    false);
            List<Footer> footers = ParquetFileReader.readFooters(getConf(), status);
            for (Footer footer : footers) {
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
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "Found parquet block: path={0}, begin={1}, end={2}",
                                status.getPath(),
                                begin,
                                end));
                    }
                    DirectInputFragment fragment = blockMap.get(begin, end);
                    results.add(fragment);
                }
            }
        }
        return results;
    }

    @Override
    public long getPreferredFragmentSize() throws IOException, InterruptedException {
        return -1L;
    }

    @Override
    public long getMinimumFragmentSize() throws IOException, InterruptedException {
        return -1L;
    }

    @Override
    public ModelInput<T> createInput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            long offset, long fragmentSize,
            Counter counter) throws IOException, InterruptedException {
        DataModelMapping driverConf = new DataModelMapping();
        ParquetFormatConfiguration conf = getFormatConfiguration();
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
        return new ParquetFileInput<T>(
                getDataModelDescriptor(),
                driverConf,
                getConf(), path,
                offset, fragmentSize,
                counter);
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            Counter counter) throws IOException, InterruptedException {
        ParquetFileOutput.Options options = new ParquetFileOutput.Options();
        ParquetFormatConfiguration conf = getFormatConfiguration();
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
        return new ParquetFileOutput<T>(
                getDataModelDescriptor(),
                getConf(), path,
                options,
                counter);
    }

}
