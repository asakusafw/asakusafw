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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.loader.BasicDataLoader;
import com.asakusafw.testdriver.loader.DataLoader;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * An abstract super class of testers.
 * @since 0.7.3
 * @version 0.10.2
 */
public class TesterBase extends TestDriverBase {

    private static final Logger LOG = LoggerFactory.getLogger(TesterBase.class);

    private final Map<ImporterDescription, DataModelSourceFactory> externalResources = new HashMap<>();

    /**
     * Creates a new instance.
     * @param callerClass the original test class
     */
    protected TesterBase(Class<?> callerClass) {
        super(callerClass);
    }

    /**
     * Puts initial data for the external resource.
     * This may be invoked from {@link DriverToolBase other collaborators}.
     * @param importer the importer description for accessing the target resource
     * @param source the source, or {@code null} to reset it
     */
    public final void putExternalResource(ImporterDescription importer, DataModelSourceFactory source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Prepare: importer={}, source={}", new Object[] { //$NON-NLS-1$
                    importer,
                    source,
            });
        }
        if (source == null) {
            externalResources.remove(importer);
        } else {
            externalResources.put(importer, source);
        }
    }

    /**
     * Returns initial data providers for the external resources.
     * @return the resources
     */
    protected Map<ImporterDescription, DataModelSourceFactory> getExternalResources() {
        return Collections.unmodifiableMap(externalResources);
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param sourcePath the path to test data set (relative from the current test case class)
     * @return the created loader
     * @since 0.10.2
     */
    public <T> DataLoader<T> loader(Class<T> dataType, String sourcePath) {
        Objects.requireNonNull(sourcePath);
        return loader(dataType, toDataModelSourceFactory(sourcePath));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param objects the test data objects
     * @return the created loader
     * @since 0.10.2
     */
    public <T> DataLoader<T> loader(Class<T> dataType, Iterable<? extends T> objects) {
        Objects.requireNonNull(objects);
        return loader(dataType, toDataModelSourceFactory(toDataModelDefinition(dataType), objects));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param provider the test data set provider
     * @return the created loader
     * @since 0.10.2
     */
    public <T> DataLoader<T> loader(Class<T> dataType, Provider<? extends Source<? extends T>> provider) {
        Objects.requireNonNull(provider);
        return loader(dataType, toDataModelSourceFactory(provider));
    }

    /**
     * Returns a new data loader.
     * Note that, the original source path may be changed if tracking source file name.
     * To keep the source file path information, please use {@link #loader(Class, Class, File)} instead.
     * @param <T> the data type
     * @param dataType the data type
     * @param formatClass the data format class
     * @param sourcePath the input file path on the class path
     * @return the created loader
     * @since 0.10.2
     */
    public <T> DataLoader<T> loader(
            Class<T> dataType, Class<? extends DataFormat<? super T>> formatClass, String sourcePath) {
        return loader(dataType, toDataModelSourceFactory(toDataModelDefinition(dataType), formatClass, sourcePath));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param formatClass the data format class
     * @param file the input file path on the class path
     * @return the created loader
     * @since 0.10.2
     */
    public <T> DataLoader<T> loader(
            Class<T> dataType, Class<? extends DataFormat<? super T>> formatClass, File file) {
        return loader(dataType, toDataModelSourceFactory(toDataModelDefinition(dataType), formatClass, file));
    }

    /**
     * Returns a new data loader.
     * @param <T> the data type
     * @param dataType the data type
     * @param factory factory which provides test data set
     * @return the created loader
     * @since 0.10.2
     */
    public <T> DataLoader<T> loader(Class<T> dataType, DataModelSourceFactory factory) {
        Objects.requireNonNull(factory);
        return new BasicDataLoader<>(getDriverContext(), toDataModelDefinition(dataType), factory);
    }

    private <T> DataModelDefinition<T> toDataModelDefinition(Class<T> dataType) {
        try {
            return getTestTools().toDataModelDefinition(dataType);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "failed to analyze the data model type: {0}",
                    dataType.getName()), e);
        }
    }
}
