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
package com.asakusafw.runtime.flow.join;

import java.io.IOException;
import java.util.List;

/**
 * An abstract super interface of looking up a list of objects.
 * @param <T> the element type
 */
public interface LookUpTable<T> {

    /**
     * Returns a list of objects for the corresponding key.
     * @param key the lookup key
     * @return the related list of objects, or an empty list if there are no corresponding objects
     * @throws IOException if error occurred while looking up objects
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    List<T> get(LookUpKey key) throws IOException;

    /**
     * A builder for building {@link LookUpTable}.
     * @param <T> the element type
     */
    interface Builder<T> {

        /**
         * Puts a key and value pair.
         * @param key the key
         * @param value the value
         * @throws IOException if error occurred while adding the key value pair
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        void add(LookUpKey key, T value) throws IOException;

        /**
         * Returns a {@link LookUpTable} from previously added key value pairs.
         * @throws IOException if error occurred while building the table
         * @return the created table
         */
        LookUpTable<T> build() throws IOException;
    }
}
