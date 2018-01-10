/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.orc.OrcFile.WriterOptions;
import org.apache.hadoop.hive.ql.io.orc.Writer;

import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An implementation of {@link ModelOutput} for creating ORCFile.
 * @param <T> the data model type
 * @since 0.7.0
 */
public class OrcFileOutput<T> implements ModelOutput<T> {

    static final Log LOG = LogFactory.getLog(OrcFileOutput.class);

    private final DataModelDescriptor descriptor;

    private final Path path;

    private final OrcFile.WriterOptions options;

    private final Counter counter;

    private Writer currentWriter;

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param path the path to the target file
     * @param options the ORCFile writer options
     * @param counter the current counter
     */
    public OrcFileOutput(
            DataModelDescriptor descriptor,
            Path path,
            WriterOptions options,
            Counter counter) {
        this.descriptor = descriptor;
        this.path = path;
        this.options = options;
        this.counter = counter;
    }

    @Override
    public void write(T model) throws IOException {
        Writer writer = prepare();
        writer.addRow(model);

        // not sure
        counter.add(1);
    }

    private Writer prepare() throws IOException {
        Writer writer = currentWriter;
        if (writer == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("OrcFileOutput.infoCreate"), //$NON-NLS-1$
                        descriptor.getDataModelClass().getSimpleName(),
                        path));
            }
            writer = OrcFile.createWriter(path, options);
            currentWriter = writer;
        }
        return writer;
    }

    @Override
    public void close() throws IOException {
        if (currentWriter != null) {
            currentWriter.close();
        }
    }
}
