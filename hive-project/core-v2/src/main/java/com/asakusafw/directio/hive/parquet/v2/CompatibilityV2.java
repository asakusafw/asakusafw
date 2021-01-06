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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.Footer;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import com.asakusafw.directio.hive.parquet.AbstractParquetFileFormat;
import com.asakusafw.directio.hive.parquet.Compatibility;
import com.asakusafw.directio.hive.parquet.ParquetFormatConfiguration;
import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.directio.hive.util.CompatibilityUtil;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.BlockMap;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat.InputContext;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * compatibility layer for Direct I/O Parquet support.
 * @since 0.10.3
 */
public class CompatibilityV2 extends Compatibility {

    static final Log LOG = LogFactory.getLog(CompatibilityV2.class);

    @Override
    protected int getPriority() {
        OptionalInt version = CompatibilityUtil.getHiveMajorVersion();
        if (version.isPresent()) {
            int v = version.getAsInt();
            return v == 2 ? 2 : -1;
        }
        try {
            Class.forName("org.apache.parquet.column.ParquetProperties"); //$NON-NLS-1$
            return 2;
        } catch (ClassNotFoundException | LinkageError e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error occurred while initializing compatibility class", e);  //$NON-NLS-1$
            }
            return -1;
        }
    }

    @Override
    public List<DirectInputFragment> computeInputFragments(
            AbstractParquetFileFormat<?> format,
            InputContext context) throws IOException, InterruptedException {
        List<DirectInputFragment> results = new ArrayList<>();
        List<FileStatus> files = new ArrayList<>(context.getInputFiles());
        Map<Path, FileStatus> pathMap = new HashMap<>();
        for (FileStatus status : files) {
            pathMap.put(status.getPath(), status);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(MessageFormat.format(
                    Messages.getString("Compatibility.infoLoadMetadata"), //$NON-NLS-1$
                    context.getDataType().getSimpleName(),
                    files.size()));
        }
        // NOTE: BlockMetaData requires skipRowGroup=false
        List<Footer> footers = ParquetFileReader.readAllFootersInParallel(format.getConf(), files, false);
        for (Footer footer : footers) {
            Path path = footer.getFile();
            FileStatus status = pathMap.get(path);
            if (status == null) {
                // may not occur
                status = context.getFileSystem().getFileStatus(path);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("Compatibility.infoAnalyzeMetadata"), //$NON-NLS-1$
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
    public <T> ModelInput<T> createInput(
            AbstractParquetFileFormat<T> format,
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            long offset,
            long fragmentSize,
            Counter counter) throws IOException, InterruptedException {
        ParquetFormatConfiguration conf = format.getFormatConfiguration();
        DataModelMapping driverConf = new DataModelMapping();
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
                format.getDataModelDescriptor(),
                driverConf,
                format.getConf(), path,
                offset, size,
                counter);
    }

    @Override
    public <T> ModelOutput<T> createOutput(
            AbstractParquetFileFormat<T> format,
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            Counter counter) throws IOException, InterruptedException {
        ParquetFormatConfiguration conf = format.getFormatConfiguration();
        ParquetFileOutput.Options options = new ParquetFileOutput.Options();
        CompressionCodecName compress = conf.getCompressionCodecName(getCompressionCodecNameClass());
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
        ParquetProperties.WriterVersion version = conf.getWriterVersion(ParquetProperties.WriterVersion.class);
        if (version != null) {
            options.setWriterVersion(version);
        }
        return new ParquetFileOutput<>(
                format.getDataModelDescriptor(),
                format.getConf(), path,
                options,
                counter);
    }

    @Override
    public Optional<ParquetProperties.WriterVersion> findVersionId(String name) {
        try {
            return Optional.of(ParquetProperties.WriterVersion.fromString(name));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public Class<CompressionCodecName> getCompressionCodecNameClass() {
        return CompressionCodecName.class;
    }

    @Override
    public Optional<ParquetValueDriver> findValueDriver(TypeInfo typeInfo, Class<?> valueClass) {
        return Optional.ofNullable(ParquetValueDrivers.find(typeInfo, valueClass));
    }
}
