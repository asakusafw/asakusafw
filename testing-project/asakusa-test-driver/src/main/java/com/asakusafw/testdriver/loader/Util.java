/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;

final class Util {

    private static final Pattern PATTERN_ORDER = Pattern.compile(
            "(\\w+)"                    // 1 - asc //$NON-NLS-1$
            + "|" + "(\\+\\s*(\\w+))"   // 3 - asc //$NON-NLS-1$ //$NON-NLS-2$
            + "|" + "(-\\s*(\\w+))"     // 5 - desc //$NON-NLS-1$ //$NON-NLS-2$
            + "|" + "((\\w+)\\s+ASC)"   // 7 - asc //$NON-NLS-1$ //$NON-NLS-2$
            + "|" + "((\\w+)\\s+DESC)"  // 9 - desc //$NON-NLS-1$ //$NON-NLS-2$
            + "", //$NON-NLS-1$
            Pattern.CASE_INSENSITIVE);

    private static final Map<Integer, Direction> ORDER_GROUP_DIRECTIONS;
    static {
        Map<Integer, Direction> map = new LinkedHashMap<>();
        map.put(1, Direction.ASCENDANT);
        map.put(3, Direction.ASCENDANT);
        map.put(5, Direction.DESCENDANT);
        map.put(7, Direction.ASCENDANT);
        map.put(9, Direction.DESCENDANT);
        ORDER_GROUP_DIRECTIONS = map;
    }

    private Util() {
        return;
    }

    static void checkProperty(DataModelDefinition<?> definition, PropertyName name) {
        PropertyType type = definition.getType(name);
        if (type == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "property \"{1}\" is not defined in {0}",
                    definition.getClass().getName(),
                    name));
        }
    }

    static <T> Comparator<DataModelReflection> toComparator(
            DataModelDefinition<T> definition,
            String... terms) {
        if (terms.length == 0) {
            return null;
        }
        List<Comparator<DataModelReflection>> subs = new ArrayList<>();
        for (String term : terms) {
            subs.add(toComparator(definition, term));
        }
        return (a, b) -> {
            for (Comparator<DataModelReflection> sub : subs) {
                int diff = sub.compare(a, b);
                if (diff != 0) {
                    return diff;
                }
            }
            return 0;
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparator<DataModelReflection> toComparator(DataModelDefinition<T> definition, String term) {
        if (term.isEmpty()) {
            throw new IllegalArgumentException("order term must not be empty"); //$NON-NLS-1$
        }
        Ordering order = parseOrder(term);
        checkProperty(definition, order.propertyName);
        Comparator<DataModelReflection> comparator = (a, b) -> {
            Comparable<Object> aValue = (Comparable<Object>) a.getValue(order.propertyName);
            Comparable<Object> bValue = (Comparable<Object>) b.getValue(order.propertyName);
            if (aValue == null) {
                if (bValue == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (bValue == null) {
                return +1;
            }
            return aValue.compareTo(bValue);
        };
        if (order.direction == Direction.ASCENDANT) {
            return comparator;
        } else {
            return comparator.reversed();
        }
    }

    static <T> Comparator<DataModelReflection> toComparator(
            DataModelDefinition<T> definition,
            Comparator<? super T> objectComparator) {
        if (objectComparator == null) {
            return null;
        }
        return (a, b) -> {
            T aObj = definition.toObject(a);
            T bObj = definition.toObject(b);
            return objectComparator.compare(aObj, bObj);
        };
    }

    static Ordering parseOrder(String expression) {
        Matcher matcher = PATTERN_ORDER.matcher(expression);
        if (matcher.matches() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid group ordering expression: {0}",
                    expression));
        }
        String name = null;
        Direction direction = null;
        for (Map.Entry<Integer, Direction> entry : ORDER_GROUP_DIRECTIONS.entrySet()) {
            int index = entry.getKey();
            String s = matcher.group(index);
            if (s != null) {
                name = s;
                direction = entry.getValue();
                break;
            }
        }
        assert name != null;
        assert direction != null;
        return new Ordering(PropertyName.parse(name), direction);
    }

    private static final class Ordering {
        final PropertyName propertyName;
        final Direction direction;
        Ordering(PropertyName propertyName, Direction direction) {
            this.propertyName = propertyName;
            this.direction = direction;
        }
    }

    private enum Direction {
        ASCENDANT,
        DESCENDANT,
    }
}
