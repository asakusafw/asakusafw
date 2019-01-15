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
package com.asakusafw.iterative.common;

/**
 * An abstract super interface of cursors.
 * @param <T> the element type
 * @since 0.8.0
 */
public interface BaseCursor<T> {

    /**
     * Advances this cursor and returns whether the next element exists or not.
     * @return {@code true} if the next element exists, otherwise {@code false}
     */
    boolean next();

    /**
     * Returns the current element on this cursor.
     * @return the current element
     * @throws IllegalStateException if the cursor does not point to any elements
     */
    T get();
}
