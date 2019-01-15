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
package com.asakusafw.runtime.core;

import java.util.List;

/**
 * Represents a data-flow view, which partitioned by each sorted group.
 * Application developers <em>MUST NOT</em> change the returned lists and their elements.
 * @param <T> the data type
 * @since 0.9.1
 */
public interface GroupView<T> extends View<T> {

    /**
     * Returns a data model list for the empty key.
     * @return the data model list for the empty key, or an empty list there is no such entries for the key
     * @throws IllegalArgumentException if the key structure is not compatible for this view (optional behavior)
     */
    default List<T> find() {
        return find(new Object[0]);
    }

    /**
     * Returns a data model list for the key.
     * @param key the search key element
     * @return the data model list for the key, or an empty list there is no such entries for key
     * @throws IllegalArgumentException if the key structure is not compatible for this view (optional behavior)
     */
    default List<T> find(Object key) {
        return find(new Object[] { key });
    }

    /**
     * Returns a data model list for the key elements.
     * @param a the first key element
     * @param b the second key element
     * @return the data model list for the key, or an empty list there is no such entries for key
     * @throws IllegalArgumentException if the key structure is not compatible for this view (optional behavior)
     */
    default List<T> find(Object a, Object b) {
        return find(new Object[] { a, b });
    }

    /**
     * Returns a data model list for the key elements.
     * @param a the first key element
     * @param b the second key element
     * @param c the third key element
     * @return the data model list for the key, or an empty list there is no such entries for key
     * @throws IllegalArgumentException if the key structure is not compatible for this view (optional behavior)
     */
    default List<T> find(Object a, Object b, Object c) {
        return find(new Object[] { a, b, c });
    }

    /**
     * Returns a data model list for the key elements.
     * @param a the first key element
     * @param b the second key element
     * @param c the third key element
     * @param d the fourth key element
     * @return the data model list for the key, or an empty list there is no such entries for key
     * @throws IllegalArgumentException if the key structure is not compatible for this view (optional behavior)
     */
    default List<T> find(Object a, Object b, Object c, Object d) {
        return find(new Object[] { a, b, c, d });
    }

    /**
     * Returns a data model list for the key elements.
     * @param elements the key elements
     * @return the data model list for the key, or an empty list there is no such entries for key
     * @throws IllegalArgumentException if the key structure is not compatible for this view (optional behavior)
     */
    List<T> find(Object... elements);
}
