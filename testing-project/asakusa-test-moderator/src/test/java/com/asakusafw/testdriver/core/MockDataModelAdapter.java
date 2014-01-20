/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Mock implemantation of {@link DataModelAdapter}.
 * @since 0.2.0
 */
public class MockDataModelAdapter implements DataModelAdapter {

    private final Set<Class<?>> accepts;

    /**
     * Creates a new instance that only accepts String models.
     */
    public MockDataModelAdapter() {
        this(String.class);
    }

    /**
     * Creates a new instance.
     * @param accepts acceptable classes
     */
    public MockDataModelAdapter(Class<?>... accepts) {
        this.accepts = new HashSet<Class<?>>(Arrays.asList(accepts));
    }

    @Override
    public <T> DataModelDefinition<T> get(Class<T> modelClass) {
        if (accepts == null || accepts.contains(modelClass)) {
            return new ValueDefinition<T>(modelClass);
        }
        return null;
    }
}
