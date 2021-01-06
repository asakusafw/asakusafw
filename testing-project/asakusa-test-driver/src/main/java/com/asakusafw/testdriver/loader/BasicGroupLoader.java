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
package com.asakusafw.testdriver.loader;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import com.asakusafw.runtime.core.GroupView;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.TestContext;

/**
 * A basic implementation of {@link GroupLoader}.
 * @param <T> the data type
 * @since 0.9.1
 * @see BasicDataLoader
 */
public class BasicGroupLoader<T> implements GroupLoader<T> {

    private final TestContext context;

    private final DataModelDefinition<T> definition;

    private final DataModelSourceFactory factory;

    private final List<PropertyName> grouping;

    private Comparator<DataModelReflection> refComparator;

    BasicGroupLoader(
            TestContext context,
            DataModelDefinition<T> definition,
            DataModelSourceFactory factory,
            List<PropertyName> grouping,
            Comparator<DataModelReflection> comparator) {
        this.context = context;
        this.definition = definition;
        this.factory = factory;
        this.grouping = grouping;
        this.refComparator = comparator;
    }

    @Override
    public GroupLoader<T> order(String... terms) {
        if (refComparator != null) {
            throw new IllegalStateException("order is already defined"); //$NON-NLS-1$
        }
        refComparator = Util.toComparator(definition, terms);
        return this;
    }

    @Override
    public GroupLoader<T> order(Comparator<? super T> comparator) {
        if (refComparator != null) {
            throw new IllegalStateException("order is already defined"); //$NON-NLS-1$
        }
        refComparator = Util.toComparator(definition, comparator);
        return this;
    }

    @Override
    public GroupView<T> asView() {
        Map<List<Object>, List<DataModelReflection>> map = asRefMap();
        Map<List<Object>, List<T>> results = new LinkedHashMap<>();
        for (Map.Entry<List<Object>, List<DataModelReflection>> entry : map.entrySet()) {
            List<Object> key = entry.getKey();
            List<DataModelReflection> refs = entry.getValue();
            List<T> resolved = new ArrayList<>(refs.size());
            for (DataModelReflection ref : refs) {
                T object = definition.toObject(ref);
                resolved.add(object);
            }
            results.put(key, resolved);
        }
        return new MapGroupView<>(definition, grouping, results);
    }

    private Map<List<Object>, List<DataModelReflection>> asRefMap() {
        Map<List<Object>, List<DataModelReflection>> results = new LinkedHashMap<>();
        try (DataModelSource source = factory.createSource(definition, context)) {
            while (true) {
                DataModelReflection ref = source.next();
                if (ref == null) {
                    break;
                }
                List<Object> key = new ArrayList<>(grouping.size());
                for (PropertyName name : grouping) {
                    Object value = ref.getValue(name);
                    key.add(value);
                }
                results.computeIfAbsent(key, k -> new ArrayList<>()).add(ref);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (refComparator != null) {
            for (List<DataModelReflection> refs : results.values()) {
                refs.sort(refComparator);
            }
        }
        return results;
    }

    private static class MapGroupView<T> implements GroupView<T> {

        private final DataModelDefinition<T> definition;

        private final PropertyType[] types;

        private final Map<List<Object>, List<T>> entity;

        MapGroupView(
                DataModelDefinition<T> definition,
                List<PropertyName> names,
                Map<List<Object>, List<T>> entity) {
            this.definition = definition;
            this.types = names.stream()
                    .sequential()
                    .map(definition::getType)
                    .toArray(PropertyType[]::new);
            this.entity = entity;
        }

        @Override
        public Iterator<T> iterator() {
            Iterator<? extends List<T>> partitions = entity.values().iterator();
            return new Iterator<T>() {
                private Iterator<T> nextPartition;
                @Override
                public boolean hasNext() {
                    while (true) {
                        if (nextPartition == null) {
                            if (partitions.hasNext()) {
                                nextPartition = partitions.next().iterator();
                            } else {
                                return false;
                            }
                        }
                        if (nextPartition.hasNext()) {
                            return true;
                        } else {
                            nextPartition = null;
                        }
                    }
                }
                @Override
                public T next() {
                    if (nextPartition == null) {
                        throw new NoSuchElementException();
                    }
                    return nextPartition.next();
                }
            };
        }

        @Override
        public List<T> find(Object... elements) {
            List<Object> key = toKey(elements);
            return entity.getOrDefault(key, Collections.emptyList());
        }

        private List<Object> toKey(Object[] elements) {
            if (elements.length != types.length) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "invalid number of key elements: must be ''find({0})''",
                        Arrays.stream(types)
                            .map(it -> nameOf(it))
                            .collect(Collectors.joining(", "))));
            }
            List<Object> key = new ArrayList<>(elements.length);
            for (int i = 0; i < elements.length; i++) {
                Object value;
                Object input = elements[i];
                try {
                    value = definition.resolveRawValue(input);
                } catch (IllegalArgumentException e) {
                    throw invalidType(i, input, e);
                }
                if (input != null && types[i].getImplementation().isPresent()) {
                    Class<?> expect = types[i].getImplementation().get();
                    Class<?> actual = input.getClass();
                    if (expect.isAssignableFrom(actual) == false) {
                        throw invalidType(i, input, null);
                    }
                }
                if (value != null) {
                    Class<?> expect = types[i].getRepresentation();
                    Class<?> actual = value.getClass();
                    if (expect.isAssignableFrom(actual) == false) {
                        throw invalidType(i, input, null);
                    }
                }
                key.add(value);
            }
            return key;
        }

        private IllegalArgumentException invalidType(int index, Object value, Exception cause) {
            return new IllegalArgumentException(MessageFormat.format(
                    "invalid key element type at parameter #{0}: required={1}, specified={2}",
                    index,
                    nameOf(types[index]),
                    Optional.ofNullable(value)
                            .map(it -> it.getClass().getSimpleName())
                            .orElse("(null)")),
                    cause);
        }

        private static String nameOf(PropertyType type) {
            return type.getImplementation()
                    .map(Class::getSimpleName)
                    .orElseGet(() -> type.name());
        }
    }
}
