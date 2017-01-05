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
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.testing.TemporaryInputDescription;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.testdriver.core.BaseImporterPreparator;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.ImporterPreparator;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * Implementation of {@link ImporterPreparator} for {@link TemporaryInputDescription}s.
 * @since 0.2.5
 */
public class TemporaryInputPreparator extends BaseImporterPreparator<TemporaryInputDescription> {

    static final Logger LOG = LoggerFactory.getLogger(TemporaryInputPreparator.class);

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance with default configurations.
     */
    public TemporaryInputPreparator() {
        this(ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param configurations the configuration factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TemporaryInputPreparator(ConfigurationFactory configurations) {
        if (configurations == null) {
            throw new IllegalArgumentException("configurations must not be null"); //$NON-NLS-1$
        }
        this.configurations = configurations;
    }

    @Override
    public void truncate(TemporaryInputDescription description, TestContext context) throws IOException {
        LOG.debug("Deleting input: {}", description); //$NON-NLS-1$
        VariableTable variables = createVariables(context);
        Configuration config = configurations.newInstance();
        FileSystem fs = FileSystem.get(config);
        for (String path : description.getPaths()) {
            String resolved = variables.parse(path, false);
            Path target = fs.makeQualified(new Path(resolved));
            delete(fs, target);
        }
        return;
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            TemporaryInputDescription description,
            TestContext context) throws IOException {
        LOG.debug("Preparing input: {}", description); //$NON-NLS-1$
        checkType(definition, description);
        Set<String> path = description.getPaths();
        if (path.isEmpty()) {
            return new ModelOutput<V>() {
                @Override
                public void close() throws IOException {
                    return;
                }
                @Override
                public void write(V model) throws IOException {
                    return;
                }
            };
        }
        VariableTable variables = createVariables(context);
        String destination = path.iterator().next();
        String resolved = variables.parse(destination, false);
        Configuration conf = configurations.newInstance();
        ModelOutput<V> output = TemporaryStorage.openOutput(conf, definition.getModelClass(), new Path(resolved));
        return output;
    }

    private VariableTable createVariables(TestContext context) {
        assert context != null;
        VariableTable result = new VariableTable();
        result.defineVariables(context.getArguments());
        return result;
    }

    private <V> void checkType(DataModelDefinition<V> definition,
            TemporaryInputDescription description) throws IOException {
        if (definition.getModelClass() != description.getModelType()) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("TemporaryInputPreparator.errorInconsistentDataType"), //$NON-NLS-1$
                    definition.getModelClass().getName(),
                    description.getModelType().getName(),
                    description));
        }
    }

    static void delete(FileSystem fs, Path target) throws IOException {
        FileStatus[] stats = fs.globStatus(target);
        if (stats == null || stats.length == 0) {
            return;
        }
        for (FileStatus s : stats) {
            Path path = s.getPath();
            LOG.debug("deleting file: {}", path); //$NON-NLS-1$
            boolean succeed = fs.delete(path, true);
            LOG.debug("deleted file (succeed={}): {}", succeed, path); //$NON-NLS-1$
        }
    }
}
