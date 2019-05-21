/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.hadoop.metadata.FileMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;

import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelDriver;
import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.io.ModelInput;

/**
 * An implementation of {@link ModelInput} for reading Parquet files.
 * @param <T> the data model type
 * @since 0.7.0
 */
public class ParquetFileInput<T> implements ModelInput<T> {

    static final Log LOG = LogFactory.getLog(ParquetFileInput.class);

    private static final Constructor<ParquetFileReader> FILE_READER_NEWER_CTOR;
    static {
        LOG.debug("loading newer ParquetFileReader.<init>");
        Constructor<ParquetFileReader> ctor = null;
        try {
            ctor = ParquetFileReader.class.getConstructor(
                    Configuration.class,
                    FileMetaData.class,
                    Path.class,
                    List.class,
                    List.class);
        } catch (ReflectiveOperationException e) {
            LOG.debug("cannot load newer ParquetFileReader.<init>", e);
        }
        FILE_READER_NEWER_CTOR = ctor;
    }

    private final DataModelDescriptor descriptor;

    private final DataModelMapping mappingConfiguration;

    private final Configuration hadoopConfiguration;

    private final Path path;

    private final long offset;

    private final long fragmentSize;

    private final Counter counter;

    private long rowRest = -1;

    private ParquetFileReader fileReader;

    private DataModelMaterializer materializer;

    private RecordReader<Object> currentRecordReader;

    private MessageColumnIO columnIo;

    private double averageBytesPerRecord;

    private double lastBytes;

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param mappingConfiguration the {@link DataModelDriver} configuration
     * @param hadoopConfiguration the hadoop configuration
     * @param path the path to the target file
     * @param offset starting stream offset
     * @param fragmentSize suggested fragment bytes count
     * @param counter the current counter
     */
    public ParquetFileInput(
            DataModelDescriptor descriptor,
            DataModelMapping mappingConfiguration,
            Configuration hadoopConfiguration, Path path,
            long offset, long fragmentSize,
            Counter counter) {
        this.descriptor = descriptor;
        this.mappingConfiguration = mappingConfiguration;
        this.hadoopConfiguration = hadoopConfiguration;
        this.path = path;
        this.offset = offset;
        this.fragmentSize = fragmentSize;
        this.counter = counter;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        RecordReader<Object> reader = prepareReader(model);
        if (reader == null) {
            return false;
        }
        rowRest--;
        reader.read();
        advanceCounter();
        return true;
    }

    private void advanceCounter() {
        double last = lastBytes;
        double next = last + averageBytesPerRecord;
        long delta = (long) (next - last);
        counter.add(delta);
        lastBytes = next;
    }

    private RecordReader<Object> prepareReader(T model) throws IOException {
        while (rowRest <= 0) {
            PageReadStore next = fetchRowGroup();
            if (next == null) {
                return null;
            }
            currentRecordReader = createRecordReader(next);
        }
        assert currentRecordReader != null;
        assert materializer != null;
        materializer.setNextRecord(model);
        return currentRecordReader;
    }

    private PageReadStore fetchRowGroup() throws IOException {
        if (fileReader == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("ParquetFileInput.infoLoadMetadata"), //$NON-NLS-1$
                        descriptor.getDataModelClass().getSimpleName(),
                        path));
            }
            ParquetMetadata footer = ParquetFileReader.readFooter(
                    hadoopConfiguration, path, ParquetMetadataConverter.NO_FILTER);
            List<BlockMetaData> blocks = filterBlocks(footer.getBlocks());
            if (blocks.isEmpty()) {
                return null;
            }
            long totalRecords = computeTotalRecords(blocks);
            this.averageBytesPerRecord = (double) fragmentSize / totalRecords;
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("ParquetFileInput.infoLoadContents"), //$NON-NLS-1$
                        descriptor.getDataModelClass().getSimpleName(),
                        path,
                        offset,
                        fragmentSize));
            }
            this.fileReader = createFileReader(footer, blocks);
            this.materializer = new DataModelMaterializer(
                    descriptor,
                    footer.getFileMetaData().getSchema(),
                    mappingConfiguration);
            this.columnIo = new ColumnIOFactory().getColumnIO(
                    materializer.getMaterializeSchema(),
                    footer.getFileMetaData().getSchema());
        }
        return fileReader.readNextRowGroup();
    }

    private ParquetFileReader createFileReader(ParquetMetadata meta, List<BlockMetaData> blocks) throws IOException {
        FileMetaData fileMetaData = meta.getFileMetaData();
        if (FILE_READER_NEWER_CTOR != null) {
            try {
                return FILE_READER_NEWER_CTOR.newInstance(
                        hadoopConfiguration,
                        fileMetaData,
                        path,
                        blocks,
                        fileMetaData.getSchema().getColumns());
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
                LOG.debug("failed ParquetFileReader.<init>", e);
            }
        }
        return new ParquetFileReader(
                hadoopConfiguration,
                fileMetaData,
                path,
                blocks,
                fileMetaData.getSchema().getColumns());
    }

    private static long computeTotalRecords(List<BlockMetaData> blocks) {
        long result = 0L;
        for (BlockMetaData block : blocks) {
            result += block.getRowCount();
        }
        return result;
    }

    private List<BlockMetaData> filterBlocks(List<BlockMetaData> blocks) {
        if (fragmentSize < 0L) {
            return blocks;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Detecting target parquet blocks: {0} ({1}+{2})", //$NON-NLS-1$
                    path,
                    this.offset,
                    this.fragmentSize));
        }
        List<BlockMetaData> results = new ArrayList<>();
        for (BlockMetaData block : blocks) {
            List<ColumnChunkMetaData> columns = block.getColumns();
            if (columns.isEmpty()) {
                return Collections.emptyList();
            }
            long begin = Long.MAX_VALUE;
            long end = -1L;
            for (ColumnChunkMetaData column : block.getColumns()) {
                long off = column.getFirstDataPageOffset();
                long len = column.getTotalSize();
                begin = Math.min(begin, off);
                end = Math.max(end, off + len);
            }
            assert begin >= 0L;
            assert end >= 0L;
            if (this.offset <= begin && end <= this.offset + this.fragmentSize && block.getRowCount() != 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Detected a target parquet block: {0} ({1}+{2})", //$NON-NLS-1$
                            path,
                            begin,
                            end - begin));
                }
                results.add(block);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Filter parquet block: {0} ({1}+{2})", //$NON-NLS-1$
                            path,
                            begin,
                            end - begin));
                }
            }
        }
        return results;
    }

    private RecordReader<Object> createRecordReader(PageReadStore store) {
        assert materializer != null;
        this.currentRecordReader = columnIo.getRecordReader(store, materializer);
        this.rowRest = store.getRowCount();
        return currentRecordReader;
    }

    @Override
    public void close() throws IOException {
        if (fileReader != null) {
            fileReader.close();
        }
    }
}
