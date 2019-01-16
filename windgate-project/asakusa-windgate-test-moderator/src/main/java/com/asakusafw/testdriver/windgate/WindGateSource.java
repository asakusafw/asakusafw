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
package com.asakusafw.testdriver.windgate;

import java.io.IOException;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * An implementation of {@link DataModelSource} using {@link SourceDriver}.
 * @param <T> the target model
 * @since 0.2.2
 */
public class WindGateSource<T> implements DataModelSource {

    private final SourceDriver<T> driver;

    private final DataModelDefinition<T> definition;

    /**
     * Creates a new instance.
     * @param driver the drain driver, this must be prepared
     * @param definition target data model definition
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WindGateSource(SourceDriver<T> driver, DataModelDefinition<T> definition) {
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        this.driver = driver;
        this.definition = definition;
    }

    @Override
    public DataModelReflection next() throws IOException {
        if (driver.next() == false) {
            return null;
        }
        T object = driver.get();
        return definition.toReflection(object);
    }

    @Override
    public void close() throws IOException {
        driver.close();
    }
}
