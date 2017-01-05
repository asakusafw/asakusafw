/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdriver.mapreduce.io;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.testdriver.core.BaseExporterRetriever;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * Implementation of {@link ExporterRetriever} for {@link TemporaryOutputDescription}s.
 * @since 0.2.5
 */
public class TemporaryOutputRetriever extends BaseExporterRetriever<TemporaryOutputDescription> {

    static final Logger LOG = LoggerFactory.getLogger(TemporaryOutputRetriever.class);

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance with default configurations.
     */
    public TemporaryOutputRetriever() {
        this(ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param configurations the configuration factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TemporaryOutputRetriever(ConfigurationFactory configurations) {
        if (configurations == null) {
            throw new IllegalArgumentException("configurations must not be null"); //$NON-NLS-1$
        }
        this.configurations = configurations;
    }

    @Override
    public void truncate(
            TemporaryOutputDescription description,
            TestContext context) throws IOException {
        LOG.debug("Deleting output directory: {}", description); //$NON-NLS-1$
        VariableTable variables = createVariables(context);
        Configuration config = configurations.newInstance();
        FileSystem fs = FileSystem.get(config);
        String resolved = variables.parse(description.getPathPrefix(), false);
        Path path = new Path(resolved);
        Path output = path.getParent();
        Path target;
        if (output == null) {
            LOG.warn(MessageFormat.format(
                    Messages.getString("TemporaryOutputRetriever.warnDeleteBaseDirectory"), //$NON-NLS-1$
                    path));
            target = fs.makeQualified(path);
        } else {
            LOG.debug("output directory will be deleted: {}", output); //$NON-NLS-1$
            target = fs.makeQualified(output);
        }
        TemporaryInputPreparator.delete(fs, target);
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            TemporaryOutputDescription description,
            TestContext context) throws IOException {
        LOG.debug("Preparing initial output: {}", description); //$NON-NLS-1$
        checkType(definition, description);
        VariableTable variables = createVariables(context);
        String destination = description.getPathPrefix().replace('*', '_');
        String resolved = variables.parse(destination, false);
        Configuration conf = configurations.newInstance();
        ModelOutput<V> output = TemporaryStorage.openOutput(conf, definition.getModelClass(), new Path(resolved));
        return output;
    }

    @Override
    public <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            TemporaryOutputDescription description,
            TestContext context) throws IOException {
        LOG.debug("Retrieving output: {}", description); //$NON-NLS-1$
        VariableTable variables = createVariables(context);
        checkType(definition, description);
        Configuration conf = configurations.newInstance();
        String resolved = variables.parse(description.getPathPrefix(), false);
        return new TemporaryDataModelSource(conf, definition, resolved);
    }

    private VariableTable createVariables(TestContext context) {
        assert context != null;
        VariableTable result = new VariableTable();
        result.defineVariables(context.getArguments());
        return result;
    }

    private <V> void checkType(DataModelDefinition<V> definition,
            TemporaryOutputDescription description) throws IOException {
        if (definition.getModelClass() != description.getModelType()) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("TemporaryOutputRetriever.errorInconsistentDataType"), //$NON-NLS-1$
                    definition.getModelClass().getName(),
                    description.getModelType().getName(),
                    description));
        }
    }
}
