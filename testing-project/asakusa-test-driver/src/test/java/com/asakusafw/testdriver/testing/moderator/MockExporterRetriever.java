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
package com.asakusafw.testdriver.testing.moderator;

import java.io.IOException;
import java.util.Objects;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.testdriver.core.BaseExporterRetriever;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * Implementation of {@link ExporterRetriever} for {@link MockExporterDescription}s.
 * @since 0.9.0
 */
public class MockExporterRetriever extends BaseExporterRetriever<MockExporterDescription> {

    static final Logger LOG = LoggerFactory.getLogger(MockExporterRetriever.class);

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance with default configurations.
     */
    public MockExporterRetriever() {
        this(ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param configurations the configuration factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public MockExporterRetriever(ConfigurationFactory configurations) {
        this.configurations = Objects.requireNonNull(configurations, "configurations must not be null"); //$NON-NLS-1$
    }

    @Override
    public void truncate(
            MockExporterDescription description,
            TestContext context) throws IOException {
        LOG.debug("deleting output directory: {}", description); //$NON-NLS-1$
        Configuration config = configurations.newInstance();
        FileSystem fs = FileSystem.get(config);
        Path path = new Path(description.getGlob());
        try {
            FileStatus[] stats = fs.globStatus(path);
            for (FileStatus s : stats) {
                fs.delete(s.getPath(), false);
            }
        } catch (IOException e) {
            LOG.debug("exception in truncate", e);
        }
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            MockExporterDescription description,
            TestContext context) throws IOException {
        Configuration conf = configurations.newInstance();
        ModelOutput<V> output = TemporaryStorage.openOutput(
                conf,
                definition.getModelClass(),
                new Path(description.getGlob().replace('*', '_')));
        return output;
    }

    @Override
    public <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            MockExporterDescription description,
            TestContext context) throws IOException {
        Configuration conf = configurations.newInstance();
        return new TemporaryDataModelSource(conf, definition, description.getGlob());
    }
}
