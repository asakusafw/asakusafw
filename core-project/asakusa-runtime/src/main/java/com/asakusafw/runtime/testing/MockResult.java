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
package com.asakusafw.runtime.testing;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.runtime.core.Result;

/**
 * Mock implementation of {@link Result}.
 * @param <T> the data type
 */
public class MockResult<T> implements Result<T> {

    private final List<T> results = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param <T> the target data type
     * @return the created instance
     */
    public static <T> MockResult<T> create() {
        return new MockResult<>();
    }

    @Override
    public void add(T result) {
        T blessed = bless(result);
        results.add(blessed);
    }

    /**
     * Handles the {@link #add(Object) added} object.
     * Clients can override this method, and replace the added object with another object.
     * @param result the original added object
     * @return the blessed object
     */
    protected T bless(T result) {
        return result;
    }

    /**
     * Returns the previously {@link #add(Object) added} objects.
     * Clients can edit the result objects by overriding {@link #bless(Object)} method.
     * @return the previously added objects
     */
    public List<T> getResults() {
        return results;
    }
}
