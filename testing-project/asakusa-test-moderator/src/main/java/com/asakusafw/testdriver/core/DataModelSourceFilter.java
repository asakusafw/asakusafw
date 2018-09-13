/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.util.function.UnaryOperator;

/**
 * Filters {@link DataModelSource}s.
 * @since 0.7.0
 * @version 0.10.2
 */
@FunctionalInterface
public interface DataModelSourceFilter extends UnaryOperator<DataModelSource> {

    /**
     * Applies this filter.
     * @param source the original source
     * @return the filtered source
     */
    @Override
    DataModelSource apply(DataModelSource source);

    /**
     * Applies this filter into the factory.
     * @param factory the target factory
     * @return the applied factory
     * @since 0.10.2
     */
    default DataModelSourceFactory apply(DataModelSourceFactory factory) {
        return new DataModelSourceFactory() {
            @Override
            public <T> DataModelSource createSource(
                    DataModelDefinition<T> definition,
                    TestContext context) throws IOException {
                return DataModelSourceFilter.this.apply(factory.createSource(definition, context));
            }
        };
    }
}
