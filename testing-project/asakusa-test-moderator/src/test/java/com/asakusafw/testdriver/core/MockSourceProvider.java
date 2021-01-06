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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock {@link DataModelSourceProvider}.
 * @since 0.2.0
 */
public class MockSourceProvider implements DataModelSourceProvider {

    private final Map<URI, DataModelSource> sources = new HashMap<>();

    /**
     * Creates a new instance only includes {@code default:source=MOCK}.
     */
    public MockSourceProvider() {
        try {
            add(new URI("default:source"), "MOCK");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Addes contents to be provided.
     * @param <T> type of data model
     * @param uri target URI
     * @param def data model definition
     * @param iter contents
     * @return this object (for method chain)
     */
    public final <T> MockSourceProvider add(URI uri, DataModelDefinition<T> def, Iterable<? extends T> iter) {
        sources.put(uri, new IteratorDataModelSource(def, iter.iterator()));
        return this;
    }

    /**
     * Addes contents to be provided.
     * @param uri target URI
     * @param lines contents
     * @return this object (for method chain)
     */
    public final MockSourceProvider add(URI uri, String... lines) {
        return add(uri, ValueDefinition.of(String.class), Arrays.asList(lines));
    }

    @Override
    public <T> DataModelSource open(
            DataModelDefinition<T> definition,
            URI source,
            TestContext context) throws IOException {
        return sources.get(source);
    }
}
