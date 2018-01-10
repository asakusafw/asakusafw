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
import java.io.InterruptedIOException;

import com.asakusafw.utils.io.Source;

/**
 * A {@link DataModelSource} implementation via the {@link Source} interface.
 * @param <T> the data model type
 * @since 0.6.0
 */
public class SourceDataModelSource<T> implements DataModelSource {

    private final DataModelDefinition<T> definition;

    private final Source<? extends T> source;

    /**
     * Creates a new instance.
     * @param definition the data model definition
     * @param source the source
     */
    public SourceDataModelSource(DataModelDefinition<T> definition, Source<? extends T> source) {
        this.definition = definition;
        this.source = source;
    }

    @Override
    public DataModelReflection next() throws IOException {
        try {
            if (source.next()) {
                return definition.toReflection(source.get());
            }
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}
