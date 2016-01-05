/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.util.NoSuchElementException;

/**
 * An abstract super interface of object sources.
 * @param <T> object type
 * @since 0.6.0
 */
public interface Source<T> extends Closeable {

    /**
     * Moves this source to the next object, and then returns {@code true}
     * if and only if the next object exists.
     * @return {@code true} if the next object exists, otherwise {@code false}
     * @throws IOException if failed to move this cursor
     * @throws InterruptedException if interrupted while searching for the next object
     */
    boolean next() throws IOException, InterruptedException;

    /**
     * Returns an object on the current source position.
     * @return the current object
     * @throws NoSuchElementException if this source has no objects on the current position
     * @throws IOException if failed to load an object by I/O error
     * @throws InterruptedException if interrupted while loading the current object
     */
    T get() throws IOException, InterruptedException;
}
