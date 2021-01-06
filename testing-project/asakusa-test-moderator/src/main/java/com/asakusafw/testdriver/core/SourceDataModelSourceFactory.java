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
import java.io.InterruptedIOException;
import java.text.MessageFormat;

import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;

/**
 * A {@link DataModelSourceFactory} implementation for {@link SourceDataModelSource}.
 * @since 0.6.0
 */
public class SourceDataModelSourceFactory extends DataModelSourceFactory {

    private final Class<?> sourceType;

    private final Provider<? extends Source<?>> sourceProvider;

    /**
     * Creates a new instance.
     * @param sourceType the source object class
     * @param sourceProvider the source provider
     * @param <T> the source data model type
     */
    public <T> SourceDataModelSourceFactory(
            Class<T> sourceType, Provider<? extends Source<? extends T>> sourceProvider) {
        if (sourceProvider == null) {
            throw new IllegalArgumentException("sourceProvider must not be null"); //$NON-NLS-1$
        }
        this.sourceType = sourceType;
        this.sourceProvider = sourceProvider;
    }

    /**
     * Creates a new instance.
     * @param sourceProvider the source provider
     */
    public SourceDataModelSourceFactory(Provider<? extends Source<?>> sourceProvider) {
        this(null, sourceProvider);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> DataModelSource createSource(DataModelDefinition<T> definition, TestContext context) throws IOException {
        if (sourceType != null && definition.getModelClass() != sourceType) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("SourceDataModelSourceFactory.errorInconsistentDataType"), //$NON-NLS-1$
                    sourceType.getName(),
                    definition.getModelClass().getName()));
        }
        try {
            return new SourceDataModelSource(definition, sourceProvider.open());
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }
}
