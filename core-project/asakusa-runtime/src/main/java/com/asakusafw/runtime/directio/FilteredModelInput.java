/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelInput;

/**
 * An implementation of {@link ModelInput} with {@link DataFilter}.
 * @param <T> the target data model type
 * @since 0.7.3
 */
public class FilteredModelInput<T> implements ModelInput<T> {

    private final ModelInput<T> delegate;

    private final DataFilter<? super T> filter;

    /**
     * Creates a new instance.
     * @param delegate the original {@link ModelInput}
     * @param filter the data filter
     */
    public FilteredModelInput(ModelInput<T> delegate, DataFilter<? super T> filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        while (delegate.readTo(model)) {
            if (filter.acceptsData(model)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
