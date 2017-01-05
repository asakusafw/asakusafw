/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.asakusafw.compiler.flow.Location;

/**
 * A repository for providing resources.
 */
public interface ResourceRepository {

    /**
     * Creates a new cursor for iterating resources in this repository.
     * @return the created cursor
     * @throws IOException if failed to create a cursor
     */
    Cursor createCursor() throws IOException;

    /**
     * A cursor for iterating resources in {@link ResourceRepository}.
     */
    interface Cursor extends Closeable {

        /**
         * Advances this cursor and returns whether the next item exists or not.
         * @return {@code true} if the next item exists, otherwise {@code false}
         * @throws IOException if failed to advance this cursor
         */
        boolean next() throws IOException;

        /**
         * Returns the location of the current item.
         * @return the location of the current item
         */
        Location getLocation();

        /**
         * Returns the byte contents of the current item.
         * @return the input stream for obtaining the contents of the current item
         * @throws IOException if failed to open the resource
         */
        InputStream openResource() throws IOException;
    }
}
