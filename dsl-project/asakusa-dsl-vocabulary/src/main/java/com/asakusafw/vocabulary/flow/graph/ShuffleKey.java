/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents shuffle key properties.
 */
public class ShuffleKey {

    static final Pattern ORDER_PATTERN = Pattern.compile(
            "(\\S+)(\\s+(ASC|DESC))?", //$NON-NLS-1$
            Pattern.CASE_INSENSITIVE);

    private final List<String> groupProperties;

    private final List<Order> orderings;

    /**
     * Creates a new instance.
     * @param groupProperties grouping properties
     * @param orderings ordering information
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public ShuffleKey(List<String> groupProperties, List<Order> orderings) {
        if (groupProperties == null) {
            throw new IllegalArgumentException("groupProperties must not be null"); //$NON-NLS-1$
        }
        if (orderings == null) {
            throw new IllegalArgumentException("orderings must not be null"); //$NON-NLS-1$
        }
        this.groupProperties = Collections.unmodifiableList(new ArrayList<>(groupProperties));
        this.orderings = Collections.unmodifiableList(new ArrayList<>(orderings));
    }

    /**
     * Returns the grouping properties.
     * @return the grouping properties
     */
    public List<String> getGroupProperties() {
        return groupProperties;
    }

    /**
     * Returns ordering information.
     * @return ordering information
     */
    public List<Order> getOrderings() {
        return orderings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupProperties.hashCode();
        result = prime * result + orderings.hashCode();
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
        ShuffleKey other = (ShuffleKey) obj;
        if (!groupProperties.equals(other.groupProperties)) {
            return false;
        }
        if (!orderings.equals(other.orderings)) {
            return false;
        }
        return true;
    }

    /**
     * Represents ordering information.
     */
    public static class Order {

        private final String property;

        private final Direction direction;

        /**
         * Creates a new instance.
         * @param property the property name
         * @param direction the ordering direction
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Order(String property, Direction direction) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
            }
            if (direction == null) {
                throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
            }
            this.property = property;
            this.direction = direction;
        }

        /**
         * Returns the property name of ordering key.
         * @return the property name
         */
        public String getProperty() {
            return property;
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
            result = prime * result + direction.hashCode();
            result = prime * result + property.hashCode();
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
            Order other = (Order) obj;
            if (direction != other.direction) {
                return false;
            }
            if (!property.equals(other.property)) {
                return false;
            }
            return true;
        }

        /**
         * Parses a string form of {@code <property-name> <direction>} (as same as {@link Order#toString()}),
         * and returns the corresponded {@link Order} object.
         * @param string a string of {@link Order#toString()}
         * @return the corresponded object, or {@code null} if the string is not valid form
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public static Order parse(String string) {
            if (string == null) {
                throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
            }
            Matcher matcher = ORDER_PATTERN.matcher(string.trim());
            if (matcher.matches() == false) {
                return null;
            }
            String property = matcher.group(1);
            String directionString = matcher.group(3);
            if (directionString == null) {
                return new Order(property, Direction.ASC);
            }
            directionString = directionString.trim();
            if (directionString.equalsIgnoreCase(Direction.ASC.name())) {
                return new Order(property, Direction.ASC);
            }
            if (directionString.equalsIgnoreCase(Direction.DESC.name())) {
                return new Order(property, Direction.DESC);
            }
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0} {1}", //$NON-NLS-1$
                    getProperty(),
                    getDirection().name());
        }
    }

    /**
     * Represents ordering direction.
     */
    public enum Direction {

        /**
         * Ascendant.
         */
        ASC,

        /**
         * Descendant.
         */
        DESC,
    }
}
