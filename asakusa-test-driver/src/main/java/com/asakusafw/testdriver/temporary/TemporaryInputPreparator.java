/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.temporary;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
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
        LOG.info("Deleting input: {}", description);
        VariableTable variables = createVariables(context);
        Configuration config = configurations.newInstance();
        FileSystem fs = FileSystem.get(config);
        for (String path : description.getPaths()) {
            String resolved = variables.parse(path, false);
            Path target = fs.makeQualified(new Path(resolved));
            LOG.debug("Deleting file: {}", target);
            boolean succeed = fs.delete(target, true);
            LOG.debug("Deleted file (succeed={}): {}", succeed, target);
        }
        return;
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            TemporaryInputDescription description,
            TestContext context) throws IOException {
        LOG.info("Preparing input: {}", description);
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
                    "型が一致しません: モデルの型={0}, 入力の型={1} ({2})",
                    definition.getModelClass().getName(),
                    description.getModelType().getName(),
                    description));
        }
    }
}
