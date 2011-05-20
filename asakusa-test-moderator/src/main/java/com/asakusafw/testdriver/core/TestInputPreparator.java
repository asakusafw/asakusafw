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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Prepares each test input data.
 * @since 0.2.0
 */
public class TestInputPreparator {

    private final DataModelAdapter adapter;

    private final SourceProvider sources;

    private final ImporterPreparator<ImporterDescription> targets;

    /**
     * Creates a new instance which uses registerd services.
     * @param serviceClassLoader class loader to load services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestInputPreparator(ClassLoader serviceClassLoader) {
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.adapter = new SpiDataModelAdapter(serviceClassLoader);
        this.sources = new SpiSourceProvider(serviceClassLoader);
        this.targets = new SpiImporterPreparator(serviceClassLoader);
    }

    /**
     * Creates a new instance which uses the specified services.
     * @param adapter data model adapter
     * @param sources test data provider
     * @param targets test data deployer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestInputPreparator(
            DataModelAdapter adapter,
            SourceProvider sources,
            ImporterPreparator<ImporterDescription> targets) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter must not be null"); //$NON-NLS-1$
        }
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null"); //$NON-NLS-1$
        }
        if (targets == null) {
            throw new IllegalArgumentException("targets must not be null"); //$NON-NLS-1$
        }
        this.adapter = adapter;
        this.sources = sources;
        this.targets = targets;
    }

    /**
     * Opens the target importer's input to prepare it.
     * @param <T> type of data model
     * @param type class of data model
     * @param description target importer
     * @return model object sink to prepare the importer's input
     * @throws IOException if failed to open the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> ModelOutput<T> prepare(Class<T> type, ImporterDescription description) throws IOException {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (type != description.getModelType()) {
            throw new IllegalArgumentException("invalid model type: type must be = description.getModelType()"); //$NON-NLS-1$
        }
        DataModelDefinition<T> definition = findDefinition(type);
        return targets.createOutput(definition, description);
    }

    /**
     * Prepares the target importer's input using the specified source.
     * @param type class of data model
     * @param description target importer
     * @param source test data
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepare(Class<?> type, ImporterDescription description, URI source) throws IOException {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = findDefinition(type);
        prepare(definition, description, source);
    }

    /**
     * Truncate the target importer's input.
     * @param type class of data model
     * @param description target importer
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void truncate(Class<?> type, ImporterDescription description) throws IOException {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = findDefinition(type);
        targets.truncate(definition, description);
    }
    
    private <T> DataModelDefinition<T> findDefinition(Class<T> type) throws IOException {
        assert type != null;
        DataModelDefinition<T> definition = adapter.get(type);
        if (definition == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to adapt {0}: (adaptor not found)",
                    type.getName()));
        }
        return definition;
    }

    private <T> void prepare(
            DataModelDefinition<T> definition,
            ImporterDescription desctipion,
            URI source) throws IOException {
        DataModelSource input = sources.open(definition, source);
        if (input == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to open source: {0} (handler not found)",
                    source));
        }
        try {
            ModelOutput<T> output = targets.createOutput(definition, desctipion);
            try {
                while (true) {
                    DataModelReflection next = input.next();
                    if (next == null) {
                        break;
                    }
                    T object = definition.toObject(next);
                    output.write(object);
                }
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }
}
