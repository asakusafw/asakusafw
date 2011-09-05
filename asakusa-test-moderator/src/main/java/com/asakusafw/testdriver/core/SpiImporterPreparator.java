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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Composition of registered {@link ImporterPreparator} as {@link ServiceLoader services}.
 * @since 0.2.0
 * @version 0.2.2
 */
public class SpiImporterPreparator implements ImporterPreparator<ImporterDescription> {

    @SuppressWarnings("rawtypes")
    private final List<ImporterPreparator> elements;

    /**
     * Creates a new instance.
     * @param serviceClassLoader the class loader to load the registered services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SpiImporterPreparator(ClassLoader serviceClassLoader) {
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.elements = Util.loadService(ImporterPreparator.class, serviceClassLoader);
    }

    /**
     * Creates a new instance.
     * @param elements the elements to be composited
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings("rawtypes")
    public SpiImporterPreparator(List<? extends ImporterPreparator<?>> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        this.elements = new ArrayList<ImporterPreparator>(elements);
    }

    @Override
    public Class<ImporterDescription> getDescriptionClass() {
        return ImporterDescription.class;
    }

    @Override
    public void truncate(ImporterDescription description, TestContext context) throws IOException {
        for (ImporterPreparator<?> element : elements) {
            if (element.getDescriptionClass().isAssignableFrom(description.getClass())) {
                truncate0(element, description, context);
                return;
            }
        }
        throw new IOException(MessageFormat.format(
                "Failed to truncate {0} (does not supported)",
                description));
    }

    private <T extends ImporterDescription> void truncate0(
            ImporterPreparator<T> preparator,
            ImporterDescription description,
            TestContext context) throws IOException {
        assert preparator != null;
        assert description != null;
        T desc = preparator.getDescriptionClass().cast(description);
        preparator.truncate(desc, context);
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            ImporterDescription description,
            TestContext context) throws IOException {
        for (ImporterPreparator<?> element : elements) {
            if (element.getDescriptionClass().isAssignableFrom(description.getClass())) {
                return createOutput0(definition, element, description, context);
            }
        }
        throw new IOException(MessageFormat.format(
                "Failed to open results of {0} (does not supported)",
                description));
    }

    private <T extends ImporterDescription, V> ModelOutput<V> createOutput0(
            DataModelDefinition<V> definition,
            ImporterPreparator<T> preparator,
            ImporterDescription description,
            TestContext context) throws IOException {
        assert definition != null;
        assert preparator != null;
        assert description != null;
        T desc = preparator.getDescriptionClass().cast(description);
        return preparator.createOutput(definition, desc, context);
    }
}
