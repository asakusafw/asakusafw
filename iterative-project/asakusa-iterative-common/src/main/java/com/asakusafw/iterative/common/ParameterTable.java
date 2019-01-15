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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a parameter table.
 * @since 0.8.0
 */
public interface ParameterTable extends Iterable<ParameterSet> {

    /**
     * Returns whether this table is empty or not.
     * @return {@code true} if this table is empty, otherwise {@code false}
     */
    boolean isEmpty();

    /**
     * Returns the number of rows in this table.
     * @return the number of rows, or {@code 0} if this table is empty
     */
    int getRowCount();

    /**
     * Returns a new cursor for iterate each row.
     * @return the created cursor
     */
    ParameterTable.Cursor newCursor();

    /**
     * Returns the all available parameter names in this set.
     * @return the all available parameter names
     */
    Set<String> getAvailable();

    /**
     * Returns the partial parameter names in this set.
     * Partial parameters will not available in some rounds.
     * @return the partial parameter names
     */
    Set<String> getPartial();

    /**
     * Returns all rows in this table.
     * @return all rows
     */
    List<ParameterSet> getRows();

    /**
     * A cursor over {@link ParameterTable}.
     * @since 0.8.0
     */
    public interface Cursor extends BaseCursor<ParameterSet> {

        @Override
        boolean next();

        @Override
        ParameterSet get();

        /**
         * Returns the changed parameter names from the previous element. If this point to the first element, the
         * returned set is equivalent to the set of available parameters in the first element.
         * @return the changed parameter names
         * @throws IllegalStateException if the cursor does not point to any elements
         */
        Set<String> getDifferences();
    }

    /**
     * A builder for {@link ParameterTable}.
     * @since 0.8.0
     */
    public interface Builder {

        /**
         * Starts building the next row of {@link ParameterTable}.
         * @return this
         */
        Builder next();

        /**
         * Adds a parameter name and its value into the current row.
         * @param name the parameter name
         * @param value the parameter value
         * @return this
         * @throws IllegalStateException if building the row is not started
         * @see #next()
         */
        Builder put(String name, String value);

        /**
         * Adds a parameter map into the current row.
         * @param parameters the parameter map
         * @return this
         * @throws IllegalStateException if building the row is not started
         * @see #next()
         */
        Builder put(Map<String, String> parameters);

        /**
         * Builds a {@link ParameterTable} from the previously added rows.
         * @return the built object
         */
        ParameterTable build();
    }
}
