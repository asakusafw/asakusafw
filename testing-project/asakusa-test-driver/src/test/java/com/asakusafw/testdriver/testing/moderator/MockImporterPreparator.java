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
package com.asakusafw.testdriver.testing.moderator;

import java.io.IOException;
import java.util.Objects;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.testdriver.core.BaseImporterPreparator;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.ImporterPreparator;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * Implementation of {@link ImporterPreparator} for {@link MockImporterDescription}s.
 * @since 0.9.0
 */
public class MockImporterPreparator extends BaseImporterPreparator<MockImporterDescription> {

    static final Logger LOG = LoggerFactory.getLogger(MockImporterPreparator.class);

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance with default configurations.
     */
    public MockImporterPreparator() {
        this(ConfigurationFactory.getDefault());
    }

    /**
     * Creates a new instance.
     * @param configurations the configuration factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public MockImporterPreparator(ConfigurationFactory configurations) {
        this.configurations = Objects.requireNonNull(configurations, "configurations must not be null"); //$NON-NLS-1$
    }

    @Override
    public void truncate(MockImporterDescription description, TestContext context) throws IOException {
        Configuration config = configurations.newInstance();
        FileSystem fs = FileSystem.get(config);
        Path target = fs.makeQualified(new Path(description.getDirectory()));
        if (fs.exists(target)) {
            fs.delete(target, true);
        }
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            MockImporterDescription description,
            TestContext context) throws IOException {
        Configuration conf = configurations.newInstance();
        ModelOutput<V> output = TemporaryStorage.openOutput(
                conf,
                definition.getModelClass(),
                new Path(description.getDirectory(), "input.bin"));
        return output;
    }
}
