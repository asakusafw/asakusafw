/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.dmdl.source;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.NoSuchElementException;

/**
 * DMDL source file repository.
 */
public interface DmdlSourceRepository {

    /**
     * Creates and returns a new cursor which can traverse in this repository.
     * @return the created cursor
     * @throws IOException if failed to create a cursor
     */
    Cursor createCursor() throws IOException;

    /**
     * Cursors in repository.
     */
    public interface Cursor extends Closeable {

        /**
         * Moves this cursor to next resource, and then returns {@code true}
         * iff the next resource exists.
         * @return {@code true} iff the next resource exists
         * @throws IOException if failed to move this cursor
         */
        boolean next() throws IOException;

        /**
         * Returns the identifier of the current resource.
         * @return the identifier
         * @throws NoSuchElementException if the current cursor is not on any element
         * @throws IOException if failed to identify the current resource
         */
        URI getIdentifier() throws IOException;

        /**
         * Open the current resource to read contents.
         * <p>
         * This method can be invoked up to once.
         * </p>
         * @return the opened stream
         * @throws NoSuchElementException if the current cursor is not on any element
         * @throws IOException if failed to open the current resource
         */
        Reader openResource() throws IOException;
    }
}
