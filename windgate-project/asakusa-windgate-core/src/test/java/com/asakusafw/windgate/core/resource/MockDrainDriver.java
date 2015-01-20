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
package com.asakusafw.windgate.core.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock {@link DrainDriver}.
 * @param <T> the type of contents
 */
public class MockDrainDriver<T> implements DrainDriver<T> {

    final String name;

    final List<T> results = new ArrayList<T>();

    /**
     * Creates a new instance.
     * @param name the name
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public MockDrainDriver(String name) {
        this.name = name;
    }

    /**
     * Returns the put data.
     * @return the results
     */
    public List<T> getResults() {
        return results;
    }

    @Override
    public void prepare() throws IOException {
        results.clear();
    }

    @Override
    public void put(T object) throws IOException {
        results.add(object);
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
