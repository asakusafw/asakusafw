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
package com.asakusafw.testdriver.core;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Composition of registered {@link DataModelAdapter}s as {@link ServiceLoader services}.
 * @since 0.2.0
 */
public class SpiDataModelAdapter implements DataModelAdapter {

    private final List<DataModelAdapter> elements;

    /**
     * Creates a new instance.
     * @param serviceClassLoader the class loader to load the registered services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SpiDataModelAdapter(ClassLoader serviceClassLoader) {
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.elements = Util.loadService(DataModelAdapter.class, serviceClassLoader);
    }

    @Override
    public <T> DataModelDefinition<T> get(Class<T> modelClass) {
        for (DataModelAdapter service : elements) {
            DataModelDefinition<T> result = service.get(modelClass);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
