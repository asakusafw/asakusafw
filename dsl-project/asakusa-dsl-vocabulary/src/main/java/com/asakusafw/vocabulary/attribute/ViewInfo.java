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
package com.asakusafw.vocabulary.attribute;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents information of data-flow views.
 * @since 0.9.1
 */
public final class ViewInfo implements Attribute {

    private static final ViewInfo FLAT_INSTANCE =
            new ViewInfo(ViewKind.FLAT, Collections.emptyList(), Collections.emptyList());

    private final ViewKind kind;

    private final List<String> grouping;

    private final List<Ordering> ordering;

    private ViewInfo(ViewKind kind, List<String> grouping, List<Ordering> ordering) {
        this.kind = kind;
        this.grouping = Collections.unmodifiableList(new ArrayList<>(grouping));
        this.ordering = Collections.unmodifiableList(new ArrayList<>(ordering));
    }

    /**
     * Returns an instance of flat view information.
     * @return the instance
     */
    public static ViewInfo flat() {
        return FLAT_INSTANCE;
    }

    /**
     * Creates a new group view information instance from the given grouping term list.
     * Each term must start with either {@code =} (grouping), {@code +} (ascendant order), or {@code -} (descendant
     * order), and follow its property name.
     * @param terms the property terms
     * @return the created instance
     * @see #getTerms()
     */
    public static ViewInfo groupOf(String... terms) {
        List<String> grouping = new ArrayList<>();
        List<Ordering> ordering = new ArrayList<>();
        for (String term : terms) {
            if (term.isEmpty()) {
                throw new IllegalArgumentException(term);
            }
            char operator = term.charAt(0);
            String name = term.substring(1);
            switch (operator) {
            case '=':
                grouping.add(name);
                break;
            case '+':
                ordering.add(new Ordering(name, Direction.ASCENDANT));
                break;
            case '-':
                ordering.add(new Ordering(name, Direction.DESCENDANT));
                break;
            default:
                throw new IllegalArgumentException(term);
            }
        }
        return new ViewInfo(ViewKind.GROUP, grouping, ordering);
    }

    /**
     * Returns the view kind.
     * @return the view kind
     */
    public ViewKind getKind() {
        return kind;
    }

    /**
     * Returns the property terms.
     * Each term must start with either {@code =} (grouping), {@code +} (ascendant order), or {@code -} (descendant
     * order), and follow its property name.
     * @return the property terms
     * @see #groupOf(String...)
     */
    public List<String> getTerms() {
        List<String> results = new ArrayList<>();
        results.addAll(getGrouping());
        results.addAll(getOrdering().stream()
                .map(o -> o.getDirection().getOperator() + o.getPropertyName())
                .collect(Collectors.toList()));
        return results;
    }

    /**
     * Returns the grouping properties.
     * @return the grouping properties
     */
    public List<String> getGrouping() {
        return grouping;
    }

    /**
     * Returns the sort expression in each group.
     * @return the sort expression
     */
    public List<Ordering> getOrdering() {
        return ordering;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(kind);
        result = prime * result + Objects.hashCode(grouping);
        result = prime * result + Objects.hashCode(ordering);
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
        ViewInfo other = (ViewInfo) obj;
        if (!Objects.equals(kind, other.kind)) {
            return false;
        }
        if (!Objects.equals(grouping, other.grouping)) {
            return false;
        }
        if (!Objects.equals(ordering, other.ordering)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        switch (kind) {
        case FLAT:
            return "PlainView"; //$NON-NLS-1$
        case GROUP: {
            List<String> elements = new ArrayList<>();
            for (String name : grouping) {
                elements.add(String.format("=%s", name)); //$NON-NLS-1$
            }
            for (Ordering order : ordering) {
                elements.add(order.toString());
            }
            return MessageFormat.format(
                    "GroupView{0}", //$NON-NLS-1$
                    elements);
        }
        default:
            return "UnknownView"; //$NON-NLS-1$
        }
    }

    /**
     * Represents a kind of view.
     * @since 0.9.1
     */
    public enum ViewKind {

        /**
         * Represents a plain {@code View}.
         */
        FLAT,

        /**
         * Represents a {@code GroupView}.
         */
        GROUP,
    }

    /**
     * Represents an ordering atom.
     * @since 0.9.1
     */
    public static final class Ordering {

        private final String propertyName;

        private final Direction direction;

        /**
         * Creates new instance.
         * @param propertyName the property name
         * @param direction the ordering direction
         */
        public Ordering(String propertyName, Direction direction) {
            this.propertyName = propertyName;
            this.direction = direction;
        }

        /**
         * Returns the property name.
         * @return the property name
         */
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * Returns the ordering direction.
         * @return the ordering direction
         */
        public Direction getDirection() {
            return direction;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(direction);
            result = prime * result + Objects.hashCode(propertyName);
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
            Ordering other = (Ordering) obj;
            if (!Objects.equals(direction, other.direction)) {
                return false;
            }
            if (!Objects.equals(propertyName, other.propertyName)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{1}{0}", //$NON-NLS-1$
                    propertyName,
                    direction);
        }
    }

    /**
     * Represents a kind of ordering direction.
     * @since 0.9.1
     */
    public enum Direction {

        /**
         * Ascendant order.
         */
        ASCENDANT("+"), //$NON-NLS-1$

        /**
         * Descendant order.
         */
        DESCENDANT("-"), //$NON-NLS-1$
        ;

        private final String operator;

        Direction(String symbol) {
            this.operator = symbol;
        }

        /**
         * Returns the direction operator.
         * @return the direction operator
         */
        public String getOperator() {
            return operator;
        }

        @Override
        public String toString() {
            return getOperator();
        }
    }
}
