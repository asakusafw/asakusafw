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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Composition of registered {@link ExporterRetriever} as {@link ServiceLoader services}.
 * @since 0.2.0
 * @version 0.2.2
 */
public class SpiExporterRetriever implements ExporterRetriever<ExporterDescription> {

    @SuppressWarnings("rawtypes")
    private final List<ExporterRetriever> elements;

    /**
     * Creates a new instance.
     * @param serviceClassLoader the class loader to load the registered services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SpiExporterRetriever(ClassLoader serviceClassLoader) {
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.elements = Util.loadService(ExporterRetriever.class, serviceClassLoader);
    }

    /**
     * Creates a new instance.
     * @param elements the elements to be composed
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SpiExporterRetriever(List<? extends ExporterRetriever<?>> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        this.elements = new ArrayList<>(elements);
    }

    @Override
    public Class<ExporterDescription> getDescriptionClass() {
        return ExporterDescription.class;
    }

    @Override
    public void truncate(ExporterDescription description, TestContext context) throws IOException {
        for (ExporterRetriever<?> element : elements) {
            if (element.getDescriptionClass().isAssignableFrom(description.getClass())) {
                truncate0(element, description, context);
                return;
            }
        }
        throw new IOException(MessageFormat.format(
                Messages.getString("SpiExporterRetriever.errorFailedToTruncate"), //$NON-NLS-1$
                description));
    }

    private <T extends ExporterDescription> void truncate0(
            ExporterRetriever<T> preparator,
            ExporterDescription description,
            TestContext context) throws IOException {
        assert preparator != null;
        assert description != null;
        T desc = preparator.getDescriptionClass().cast(description);
        preparator.truncate(desc, context);
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            ExporterDescription description,
            TestContext context) throws IOException {
        for (ExporterRetriever<?> element : elements) {
            if (element.getDescriptionClass().isAssignableFrom(description.getClass())) {
                return createOutput0(definition, element, description, context);
            }
        }
        throw new IOException(MessageFormat.format(
                Messages.getString("SpiExporterRetriever.errorFailedToCreateOutput"), //$NON-NLS-1$
                description));
    }

    private <T extends ExporterDescription, V> ModelOutput<V> createOutput0(
            DataModelDefinition<V> definition,
            ExporterRetriever<T> retriever,
            ExporterDescription description,
            TestContext context) throws IOException {
        assert definition != null;
        assert retriever != null;
        assert description != null;
        T desc = retriever.getDescriptionClass().cast(description);
        return retriever.createOutput(definition, desc, context);
    }

    @Override
    public <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            ExporterDescription description,
            TestContext context) throws IOException {
        for (ExporterRetriever<?> element : elements) {
            if (element.getDescriptionClass().isAssignableFrom(description.getClass())) {
                return createSource0(definition, element, description, context);
            }
        }
        throw new IOException(MessageFormat.format(
                Messages.getString("SpiExporterRetriever.errorFailedToCreateSource"), //$NON-NLS-1$
                description));
    }

    private <T extends ExporterDescription> DataModelSource createSource0(
            DataModelDefinition<?> definition,
            ExporterRetriever<T> retriever,
            ExporterDescription description,
            TestContext context) throws IOException {
        assert retriever != null;
        assert description != null;
        T desc = retriever.getDescriptionClass().cast(description);
        return retriever.createSource(definition, desc, context);
    }
}
