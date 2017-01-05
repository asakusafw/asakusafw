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
package com.asakusafw.runtime.directio.api;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.ResourcePattern;
import com.asakusafw.runtime.directio.SimpleDataDefinition;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.io.ModelInput;

/**
 * Delegating object for {@link DirectIo}.
 * @since 0.7.3
 * @version 0.9.0
 */
public class DirectIoDelegate extends Configured implements DirectIoApi {

    private final AtomicReference<DirectDataSourceRepository> repository = new AtomicReference<>();

    /**
     * Creates a new instance.
     * @param configuration the current configuration
     */
    public DirectIoDelegate(Configuration configuration) {
        super(configuration);
    }

    @Override
    public <T> ModelInput<T> open(
            Class<? extends DataFormat<T>> formatClass,
            String basePath,
            String resourcePattern) throws IOException {
        FilePattern bPattern = FilePattern.compile(basePath);
        if (bPattern.containsVariables()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "base path must not contain variables: {0}",
                    basePath));
        }
        FilePattern rPattern = FilePattern.compile(resourcePattern);
        if (rPattern.containsVariables()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "resource pattern must not contain variables: {0}",
                    resourcePattern));
        }
        try {
            return open0(formatClass, basePath, rPattern);
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException().initCause(e);
        }
    }

    private <T> ModelInput<T> open0(
            Class<? extends DataFormat<T>> formatClass,
            String originalBasePath,
            ResourcePattern resourcePattern) throws IOException, InterruptedException {
        DirectDataSourceRepository repo = prepareRepository();
        String basePath = repo.getComponentPath(originalBasePath);
        DirectDataSource source = repo.getRelatedDataSource(originalBasePath);
        DataDefinition<T> definition = createDataDefinition(formatClass);
        List<DirectInputFragment> fragments = source.findInputFragments(definition, basePath, resourcePattern);
        return new DirectInputFragmentInput<>(source, definition, fragments.iterator(), new Counter());
    }

    private <T> DataDefinition<T> createDataDefinition(Class<? extends DataFormat<T>> formatClass) {
        DataFormat<T> format = ReflectionUtils.newInstance(formatClass, getConf());
        return SimpleDataDefinition.newInstance(format.getSupportedType(), format);
    }

    private DirectDataSourceRepository prepareRepository() {
        DirectDataSourceRepository repo = repository.get();
        if (repo == null) {
            this.repository.compareAndSet(null, HadoopDataSourceUtil.loadRepository(getConf()));
            repo = repository.get();
            assert repo != null;
        }
        return repo;
    }
}
