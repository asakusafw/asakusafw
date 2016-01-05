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
package com.asakusafw.dmdl.thundergate.view.model;

import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.utils.collections.Lists;

/**
 * {@code JOIN ~ ON ~}.
 */
public class Join {

    /**
     * The table name to join.
     */
    public final Name table;

    /**
     * The alias name to the table, or {@code null} if not specified.
     */
    public final String alias;

    /**
     * The (eq) join conditions.
     */
    public final List<On> condition;

    /**
     * Creates and returns a new instance.
     * @param table the table name to join
     * @param alias the alias name to the table, or {@code null} if not specified
     * @param condition the (eq) join conditions
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Join(Name table, String alias, List<On> condition) {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null"); //$NON-NLS-1$
        }
        if (condition == null) {
            throw new IllegalArgumentException("condition must not be null"); //$NON-NLS-1$
        }
        this.table = table;
        this.alias = alias;
        this.condition = Lists.freeze(condition);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + condition.hashCode();
        result = prime * result + table.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Join other = (Join) obj;
        if (!table.equals(other.table)) {
            return false;
        }
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (!condition.equals(other.condition)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (alias != null) {
            return MessageFormat.format(
                    "JOIN {0} {2} ON {1}",
                    table,
                    condition,
                    alias);
        } else {
            return MessageFormat.format(
                    "JOIN {0} ON {1}",
                    table,
                    condition);
        }
    }
}
