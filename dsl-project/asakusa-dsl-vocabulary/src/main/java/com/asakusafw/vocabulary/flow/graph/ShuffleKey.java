/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
 * シャッフル時に利用されるキープロパティの情報。
 */
public class ShuffleKey {

    static final Pattern ORDER_PATTERN = Pattern.compile(
            "(\\S+)(\\s+(ASC|DESC))?", //$NON-NLS-1$
            Pattern.CASE_INSENSITIVE);

    private final List<String> groupProperties;

    private final List<Order> orderings;

    /**
     * インスタンスを生成する。
     * @param groupProperties グループ化のためのプロパティ一覧
     * @param orderings 整列順序
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleKey(List<String> groupProperties, List<Order> orderings) {
        if (groupProperties == null) {
            throw new IllegalArgumentException("groupProperties must not be null"); //$NON-NLS-1$
        }
        if (orderings == null) {
            throw new IllegalArgumentException("orderings must not be null"); //$NON-NLS-1$
        }
        this.groupProperties = Collections.unmodifiableList(new ArrayList<String>(groupProperties));
        this.orderings = Collections.unmodifiableList(new ArrayList<Order>(orderings));
    }

    /**
     * グループ化のためのプロパティ一覧を返す。
     * @return グループ化のためのプロパティ一覧
     */
    public List<String> getGroupProperties() {
        return groupProperties;
    }

    /**
     * 整列順序を返す。
     * @return 整列順序
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
     * 整列を表現する。
     */
    public static class Order {

        private final String property;

        private final Direction direction;

        /**
         * インスタンスを生成する。
         * @param property プロパティの名称
         * @param direction 整列方向
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
         * 整列のキーに利用するプロパティの名称を返す。
         * @return 整列のキーに利用するプロパティの名称
         */
        public String getProperty() {
            return property;
        }

        /**
         * 整列方向を返す。
         * @return 整列方向
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
         * {@code プロパティ名, 整列方向} の形式の文字列から、相応する{@link Order}オブジェクトを生成して返す。
         * @param string {@link Order#toString()}の文字列
         * @return 対応するオブジェクト、解析に失敗した場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * 整列順序。
     */
    public enum Direction {

        /**
         * 昇順。
         */
        ASC,

        /**
         * 降順。
         */
        DESC,
    }
}
