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
package com.asakusafw.utils.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * An abstract super interface of object sinks.
 * @param <T> object type
 * @since 0.6.0
 */
public interface Sink<T> extends Flushable, Closeable {

    /**
     * Puts an object to this sink.
     * @param object the target object
     * @throws IOException if failed to put the object by I/O error
     * @throws InterruptedException if interrupted while putting the object
     */
    void put(T object) throws IOException, InterruptedException;
}
