/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Prepares each test input data.
 * @since 0.2.0
 * @deprecated Use {@link TestModerator} instead
 */
@Deprecated
public class TestDataPreparator {

    private final DataModelAdapter adapter;

    private final DataModelSourceProvider sources;

    private final ImporterPreparator<ImporterDescription> importers;

    private final ExporterRetriever<ExporterDescription> exporters;

    private final TestContext context;

    /**
     * Creates a new instance which uses registerd services.
     * @param serviceClassLoader class loader to load services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDataPreparator(ClassLoader serviceClassLoader) {
        this(new TestContext.Empty(), serviceClassLoader);
    }

    /**
     * Creates a new instance which uses the specified services.
     * @param adapter data model adapter
     * @param sources test data provider
     * @param importers test data deployer for import source
     * @param exporters test data deployer for export target
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDataPreparator(
            DataModelAdapter adapter,
            DataModelSourceProvider sources,
            ImporterPreparator<ImporterDescription> importers,
            ExporterRetriever<ExporterDescription> exporters) {
        this(new TestContext.Empty(), adapter, sources, importers, exporters);
    }

    /**
     * Creates a new instance which uses registerd services.
     * @param context the current context
     * @param serviceClassLoader class loader to load services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDataPreparator(TestContext context, ClassLoader serviceClassLoader) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.adapter = new SpiDataModelAdapter(serviceClassLoader);
        this.sources = new SpiDataModelSourceProvider(serviceClassLoader);
        this.importers = new SpiImporterPreparator(serviceClassLoader);
        this.exporters = new SpiExporterRetriever(serviceClassLoader);
    }

    /**
     * Creates a new instance which uses the specified services.
     * @param context the current context
     * @param adapter data model adapter
     * @param sources test data provider
     * @param importers test data deployer for import source
     * @param exporters test data deployer for export target
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDataPreparator(
            TestContext context,
            DataModelAdapter adapter,
            DataModelSourceProvider sources,
            ImporterPreparator<ImporterDescription> importers,
            ExporterRetriever<ExporterDescription> exporters) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (adapter == null) {
            throw new IllegalArgumentException("adapter must not be null"); //$NON-NLS-1$
        }
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null"); //$NON-NLS-1$
        }
        if (importers == null) {
            throw new IllegalArgumentException("importers must not be null"); //$NON-NLS-1$
        }
        if (exporters == null) {
            throw new IllegalArgumentException("exporters must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.adapter = adapter;
        this.sources = sources;
        this.importers = importers;
        this.exporters = exporters;
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
            throw new IllegalArgumentException(
                    "invalid model type: type must be = description.getModelType()"); //$NON-NLS-1$
        }
        DataModelDefinition<T> definition = findDefinition(type);
        return importers.createOutput(definition, description, context);
    }

    /**
     * Opens the target exporter's output to prepare it.
     * @param <T> type of data model
     * @param type class of data model
     * @param description target exporter
     * @return model object sink to prepare the importer's input
     * @throws IOException if failed to open the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> ModelOutput<T> prepare(Class<T> type, ExporterDescription description) throws IOException {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (type != description.getModelType()) {
            throw new IllegalArgumentException(
                    "invalid model type: type must be = description.getModelType()"); //$NON-NLS-1$
        }
        DataModelDefinition<T> definition = findDefinition(type);
        return exporters.createOutput(definition, description, context);
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
     * Prepares the target exporter's output using the specified source.
     * @param type class of data model
     * @param description target importer
     * @param source test data
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepare(Class<?> type, ExporterDescription description, URI source) throws IOException {
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
     * Truncates the target importer's input.
     * @param description target importer
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void truncate(ImporterDescription description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        importers.truncate(description, context);
    }

    /**
     * Truncates the target exporter's output.
     * @param description target importer
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void truncate(ExporterDescription description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        exporters.truncate(description, context);
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
        assert definition != null;
        assert desctipion != null;
        assert source != null;
        ModelOutput<T> output = importers.createOutput(definition, desctipion, context);
        prepare(definition, output, source);
    }

    private <T> void prepare(
            DataModelDefinition<T> definition,
            ExporterDescription desctipion,
            URI source) throws IOException {
        assert definition != null;
        assert desctipion != null;
        assert source != null;
        ModelOutput<T> output = exporters.createOutput(definition, desctipion, context);
        prepare(definition, output, source);
    }

    private <T> void prepare(
            DataModelDefinition<T> definition,
            ModelOutput<T> output,
            URI source) throws IOException {
        try {
            DataModelSource input = sources.open(definition, source, context);
            if (input == null) {
                throw new IOException(MessageFormat.format(
                        "Failed to open source: {0} (handler not found)",
                        source));
            }
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
                input.close();
            }
        } finally {
            output.close();
        }
    }
}
