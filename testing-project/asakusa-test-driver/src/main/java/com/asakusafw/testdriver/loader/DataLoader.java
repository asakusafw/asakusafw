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
package com.asakusafw.testdriver.loader;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.asakusafw.runtime.core.View;
import com.asakusafw.vocabulary.model.Key;

/**
 * Loads a dataset and organize it for operator inputs.
 * @param <T> the data type
 * @since 0.9.1
 * @version 0.10.2
 */
public interface DataLoader<T> {

    /**
     * Configures the grouping key of the organizing dataset.
     * The form of each term must be like as {@link Key#group()}.
     * @param terms the property name of each key element
     * @return the group loader
     * @see Key#group()
     */
    GroupLoader<T> group(String... terms);

    /**
     * Configures the sort order of the organizing dataset.
     * The form of each term must be like as {@link Key#order()}.
     * @param terms the ordering term
     * @return this
     * @see Key#order()
     * @throws IllegalStateException if sort order is already configured
     */
    DataLoader<T> order(String... terms);

    /**
     * Configures the sort order of the organizing dataset.
     * @param comparator the object comparator
     * @return this
     * @throws IllegalStateException if sort order is already configured
     */
    DataLoader<T> order(Comparator<? super T> comparator);

    /**
     * Returns the loaded data as a stream.
     * @return the organized stream
     * @since 0.10.2
     */
    default Stream<T> asStream() {
        return asList().stream();
    }

    /**
     * Returns the loaded data as a list.
     * @return the organized list
     */
    List<T> asList();

    /**
     * Returns the loaded data as a view.
     * @return the organized list
     */
    View<T> asView();
}
