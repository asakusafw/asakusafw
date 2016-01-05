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
import java.util.Set;

import com.asakusafw.dmdl.thundergate.model.Aggregator;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;

/**
 * {@code CREATE VIEW ~}.
 */
public class CreateView {

    /**
     * The name of the VIEW.
     */
    public final Name name;

    /**
     * The projected elemens.
     */
    public final List<Select> selectList;

    /**
     * The source tables.
     */
    public final From from;

    /**
     * The grouping columns.
     */
    public final List<Name> groupBy;

    /**
     * Creates and returns a new instance.
     * @param name the name of this VIEW
     * @param selectList the projected of each element
     * @param from the source tables
     * @param groupBy the grouping columns
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CreateView(
            Name name,
            List<Select> selectList,
            From from,
            List<Name> groupBy) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (selectList == null) {
            throw new IllegalArgumentException("selectList must not be null"); //$NON-NLS-1$
        }
        if (from == null) {
            throw new IllegalArgumentException("from must not be null"); //$NON-NLS-1$
        }
        if (groupBy == null) {
            throw new IllegalArgumentException("groupBy must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.selectList = Lists.freeze(selectList);
        this.from = from;
        this.groupBy = Lists.freeze(groupBy);
    }

    /**
     * Returns the kind of this VIEW.
     * @return the kind of this
     */
    public CreateView.Kind getKind() {
        boolean join = (from.join != null);
        boolean summarize = (groupBy.isEmpty() == false);
        if (summarize == false) {
            for (Select select : selectList) {
                if (select.aggregator != Aggregator.IDENT) {
                    summarize = true;
                    break;
                }
            }
        }
        if (join && summarize == false) {
            return Kind.JOINED;
        }
        if (summarize && join == false) {
            return Kind.SUMMARIZED;
        }
        return Kind.UNKNOWN;
    }

    /**
     * Returns the dependent models from this VIEW.
     * @return the dependent models
     */
    public Set<Name> getDependencies() {
        Set<Name> results = Sets.create();
        results.add(from.table);
        if (from.join != null) {
            results.add(from.join.table);
        }
        return results;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + from.hashCode();
        result = prime * result + groupBy.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + selectList.hashCode();
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
        CreateView other = (CreateView) obj;
        if (!from.equals(other.from)) {
            return false;
        }
        if (!groupBy.equals(other.groupBy)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (!selectList.equals(other.selectList)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (groupBy.isEmpty()) {
            return MessageFormat.format(
                    "CREATE VIEW {0} SELECT {1} {2}",
                    name,
                    selectList,
                    from);
        } else {
            return MessageFormat.format(
                    "CREATE VIEW {0} SELECT {1} {2} GROUP BY {3}",
                    name,
                    selectList,
                    from,
                    groupBy);
        }
    }

    /**
     * The kind of {@link CreateView}.
     */
    public enum Kind {

        /**
         * Joined-model like.
         */
        JOINED,

        /**
         * Summarized-model like.
         */
        SUMMARIZED,

        /**
         * Unknown model.
         */
        UNKNOWN,
    }
}
