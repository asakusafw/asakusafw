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
package com.asakusafw.utils.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * An abstract super interface which provides I/O resources.
 * @param <T> resource type which this provides
 * @since 0.6.0
 */
public interface Provider<T> extends Closeable {

    /**
     * Returns a new resource.
     * @return the provided resource
     * @throws IOException if failed to provide a new resource
     * @throws InterruptedException if interrupted while preparing the resource
     */
    T open() throws IOException, InterruptedException;
}
