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

import com.asakusafw.runtime.core.GroupView;
import com.asakusafw.vocabulary.model.Key;

/**
 * Loads a dataset and organize a set of groups for operator inputs.
 * @param <T> the data type
 * @since 0.9.1
 */
public interface GroupLoader<T> {

    /**
     * Configures the sort order of the organizing groups.
     * The form of each term must be like as {@link Key#order()}.
     * @param terms the ordering term
     * @return this
     * @see Key#order()
     * @throws IllegalStateException if sort order is already configured
     */
    GroupLoader<T> order(String... terms);

    /**
     * Configures the sort order of the organizing dataset.
     * @param comparator the object comparator
     * @return this
     * @throws IllegalStateException if sort order is already configured
     */
    GroupLoader<T> order(Comparator<? super T> comparator);

    /**
     * Returns the loaded data as a group view.
     * @return the organized list
     */
    GroupView<T> asView();
}
