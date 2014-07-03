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
package com.asakusafw.directio.hive.orc;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.orc.Reader;
import org.apache.hadoop.hive.ql.io.orc.RecordReader;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;

import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelDriver;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.io.ModelInput;

/**
 * An implementation of {@link ModelInput} for reading ORCFile.
 * @param <T> the data model type
 * @since 0.7.0
 */
public class OrcFileInput<T> implements ModelInput<T> {

    private final DataModelDescriptor descriptor;

    private final DataModelMapping configuration;

    private final FileSystem fileSystem;

    private final Path path;

    private final long offset;

    private final long fragmentSize;

    private final Counter counter;

    private DataModelDriver driver;

    private RecordReader currentReader;

    private Object structBuffer;

    private long lastCount;

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param fileSystem the file system to open the target path
     * @param path the path to the target file
     * @param offset starting stream offset
     * @param fragmentSize suggested fragment bytes count, or {@code -1} as infinite
     * @param counter the current counter
     */
    public OrcFileInput(
            DataModelDescriptor descriptor,
            FileSystem fileSystem, Path path,
            long offset, long fragmentSize,
            Counter counter) {
        this(descriptor, new DataModelMapping(), fileSystem, path, offset, fragmentSize, counter);
    }

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param configuration the {@link DataModelDriver} configuration
     * @param fileSystem the file system to open the target path
     * @param path the path to the target file
     * @param offset starting stream offset
     * @param fragmentSize suggested fragment bytes count, or {@code -1} as infinite
     * @param counter the current counter
     */
    public OrcFileInput(
            DataModelDescriptor descriptor,
            DataModelMapping configuration,
            FileSystem fileSystem, Path path,
            long offset, long fragmentSize,
            Counter counter) {
        this.descriptor = descriptor;
        this.configuration = configuration;
        this.fileSystem = fileSystem;
        this.path = path;
        this.offset = offset;
        this.fragmentSize = fragmentSize;
        this.counter = counter;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        RecordReader reader = prepare();
        if (reader.hasNext() == false) {
            return false;
        }
        Object buf = reader.next(structBuffer);
        driver.set(model, buf);
        structBuffer = buf;

        advanceCounter((long) (fragmentSize * reader.getProgress()));
        return true;
    }

    private RecordReader prepare() throws IOException {
        RecordReader reader = currentReader;
        if (reader == null) {
            Reader orc = OrcFile.createReader(fileSystem, path);
            StructObjectInspector sourceInspector = (StructObjectInspector) orc.getObjectInspector();
            driver = new DataModelDriver(descriptor, sourceInspector, configuration);
            reader = orc.rows(offset, fragmentSize, null);
            currentReader = reader;
        }
        return reader;
    }

    private void advanceCounter(long nextCount) {
        long deltaCount = nextCount - lastCount;
        if (deltaCount > 0) {
            counter.add(deltaCount);
            lastCount = nextCount;
        }
    }

    @Override
    public void close() throws IOException {
        if (currentReader != null) {
            currentReader.close();
        }
    }
}
