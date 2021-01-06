/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.info.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents grouping spec.
 * @since 0.9.2
 */
public class InputGroup {

    static final char KEY_PARTITION = '=';

    static final char KEY_ASCENDANT = '+';

    static final char KEY_DESCENDANT = '-';

    @JsonIgnore
    private final List<String> keys;

    @JsonIgnore
    private final List<Order> order;

    /**
     * Creates a new instance.
     * @param keys the group keys
     * @param order the ordering information
     */
    public InputGroup(List<String> keys, List<Order> order) {
        this.keys = Util.freeze(keys);
        this.order = Util.freeze(order);
    }

    /**
     * Returns an instance.
     * @param expressions the grouping expressions
     * @return the instance
     */
    @JsonCreator
    public static InputGroup parse(List<String> expressions) {
        List<String> keys = new ArrayList<>();
        List<Order> order = new ArrayList<>();
        for (String element : expressions) {
            assert element.length() >= 1;
            String operand = element.substring(1);
            switch (element.charAt(0)) {
            case KEY_PARTITION:
                keys.add(operand);
                break;
            case KEY_ASCENDANT:
                order.add(new Order(operand, Direction.ASCENDANT));
                break;
            case KEY_DESCENDANT:
                order.add(new Order(operand, Direction.DESCENDANT));
                break;
            default:
                throw new AssertionError(element);
            }
        }
        return new InputGroup(keys, order);
    }

    @JsonValue
    List<String> unparse() {
        List<String> results = new ArrayList<>();
        keys.stream()
            .map(it -> KEY_PARTITION + it)
            .forEachOrdered(results::add);
        order.stream()
            .map(Order::toString)
            .forEachOrdered(results::add);
        return results;
    }

    /**
     * Returns the keys.
     * @return the keys
     */
    public List<String> getKeys() {
        return keys;
    }

    /**
     * Returns the order.
     * @return the order
     */
    public List<Order> getOrder() {
        return order;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(keys);
        result = prime * result + Objects.hashCode(order);
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
        InputGroup other = (InputGroup) obj;
        return Objects.equals(keys, other.keys)
                && Objects.equals(order, other.order);
    }

    @Override
    public String toString() {
        return String.format("Group%s", unparse());
    }

    /**
     * Represents order element.
     * @since 0.9.2
     */
    public static class Order {

        private final String name;

        private final Direction direction;

        /**
         * Creates a new instance.
         * @param name the property name
         * @param direction the property direction
         */
        public Order(String name, Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        /**
         * Returns the name.
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the direction.
         * @return the direction
         */
        public Direction getDirection() {
            return direction;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(direction);
            result = prime * result + Objects.hashCode(name);
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
            return Objects.equals(direction, other.direction)
                    && Objects.equals(name, other.name);
        }

        @Override
        public String toString() {
            return direction.key + name;
        }
    }

    /**
     * Represents direction.
     * @since 0.9.2
     */
    public enum Direction {

        /**
         * Ascendant order.
         */
        ASCENDANT(KEY_ASCENDANT),

        /**
         * Descendant order.
         */
        DESCENDANT(KEY_DESCENDANT),
        ;

        final char key;

        Direction(char key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return String.valueOf(key);
        }
    }
}
