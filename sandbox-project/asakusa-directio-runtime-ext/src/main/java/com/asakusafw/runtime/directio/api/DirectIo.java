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
package com.asakusafw.runtime.directio.api;

import java.io.IOException;

import com.asakusafw.runtime.core.api.ApiStub;
import com.asakusafw.runtime.core.util.Shared;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.api.legacy.LegacyDirectIo;
import com.asakusafw.runtime.io.ModelInput;

/**
 * A framework API for accessing Direct I/O.
 * @see Shared
 * @since 0.7.3
 * @version 0.9.0
 */
public final class DirectIo {

    private static final ApiStub<DirectIoApi> STUB = new ApiStub<>(LegacyDirectIo.API);

    static final ThreadLocal<DirectIoDelegate> DELEGATE = ThreadLocal.withInitial(() -> {
        throw new IllegalStateException("Direct I/O API is not yet initialized");
    });

    private DirectIo() {
        return;
    }

    /**
     * Returns data model objects from Direct I/O data sources.
     * <p>
     * Clients can obtain each data model object:
     * </p>
<pre><code>
try (ModelInput&lt;Hoge&gt; input = DirectIo.open(...)) {
    Hoge object = new Hoge();
    while (input.readTo(object)) {
        // process object
        System.out.println(object);
    }
}
</code></pre>
     * , or can build a list of data model objects:
<pre><code>
List&lt;Hoge&gt; list = new ArrayList&lt;&gt;();
try (ModelInput&lt;Hoge&gt; input = DirectIo.open(...)) {
    while (true) {
        // create a new object in each iteration
        Hoge object = new Hoge();
        if (!input.readTo(object)) {
            break;
        }
        list.add(object);
    }
}
</code></pre>
     * @param <T> the data model object type
     * @param formatClass the Direct I/O data format class
     * @param basePath the base path (must not contain variables)
     * @param resourcePattern the resource pattern (must not contain variables)
     * @return the data model objects
     * @throws IOException if failed to open data model objects on the data source
     */
    public static <T> ModelInput<T> open(
            Class<? extends DataFormat<T>> formatClass,
            String basePath,
            String resourcePattern) throws IOException {
        return STUB.get().open(formatClass, basePath, resourcePattern);
    }

    /**
     * Returns the API stub.
     * Application developer must not use this directly.
     * @return the API stub
     * @since 0.9.0
     */
    public static ApiStub<DirectIoApi> getStub() {
        return STUB;
    }
}
