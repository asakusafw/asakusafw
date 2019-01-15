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
package com.asakusafw.iterative.common.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.asakusafw.iterative.common.ParameterSet;
import com.asakusafw.iterative.common.ParameterTable;

/**
 * A basic implementation of {@link ParameterTable}.
 * @since 0.8.0
 */
public class BasicParameterTable implements ParameterTable {

    private final List<Map<String, String>> entity;

    /**
     * Creates a new instance.
     * @param entity the list of parameter map
     */
    public BasicParameterTable(List<? extends Map<String, String>> entity) {
        Objects.requireNonNull(entity);
        this.entity = Collections.unmodifiableList(new ArrayList<>(entity));
    }

    @Override
    public Iterator<ParameterSet> iterator() {
        return CursorUtil.toIterator(newCursor());
    }

    @Override
    public boolean isEmpty() {
        return entity.isEmpty();
    }

    @Override
    public int getRowCount() {
        return entity.size();
    }

    @Override
    public Cursor newCursor() {
        return new BasicCursor(entity);
    }

    @Override
    public Set<String> getAvailable() {
        Set<String> results = new HashSet<>();
        for (Map<String, String> map : entity) {
            results.addAll(map.keySet());
        }
        return results;
    }

    @Override
    public Set<String> getPartial() {
        Set<String> available = getAvailable();
        Set<String> results = new HashSet<>();
        for (Map<String, String> map : entity) {
            for (String key : available) {
                if (map.get(key) == null) {
                    results.add(key);
                }
            }
            if (available.size() == results.size()) {
                break;
            }
        }
        return results;
    }

    @Override
    public List<ParameterSet> getRows() {
        List<ParameterSet> results = new ArrayList<>();
        for (ParameterSet element : this) {
            results.add(element);
        }
        return results;
    }

    /**
     * A basic implementation of {@link com.asakusafw.iterative.common.ParameterTable.Cursor}.
     * @since 0.8.0
     */
    public static class BasicCursor implements Cursor {

        private final Iterator<? extends Map<String, String>> entity;

        private Map<String, String> previous;

        private Map<String, String> current;

        /**
         * Creates a new instance.
         * @param table the list of parameter map
         */
        public BasicCursor(List<? extends Map<String, String>> table) {
            this(Objects.requireNonNull(table).iterator());
        }

        /**
         * Creates a new instance.
         * @param entity the iterator of parameter map
         */
        public BasicCursor(Iterator<? extends Map<String, String>> entity) {
            Objects.requireNonNull(entity);
            this.entity = entity;
        }

        @Override
        public boolean next() {
            if (entity.hasNext() == false) {
                current = null;
                return false;
            }
            previous = current != null ? current : Collections.emptyMap();
            current = entity.next();
            return true;
        }

        @Override
        public ParameterSet get() {
            if (current == null) {
                throw new IllegalStateException();
            }
            return new BasicParameterSet(current);
        }

        @Override
        public Set<String> getDifferences() {
            if (current == null) {
                throw new IllegalStateException();
            }
            assert previous != null;
            Set<String> keys = new HashSet<>();
            keys.addAll(previous.keySet());
            keys.addAll(current.keySet());
            for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
                String key = iter.next();
                if (Objects.equals(previous.get(key), current.get(key))) {
                    iter.remove();
                }
            }
            return keys;
        }
    }

    /**
     * A basic implementation of {@link com.asakusafw.iterative.common.ParameterTable.Builder}.
     * @since 0.8.0
     */
    public static class BasicBuilder implements Builder {

        private final List<Map<String, String>> entity = new ArrayList<>();

        private Map<String, String> row;

        @Override
        public Builder next() {
            if (row != null) {
                entity.add(row);
            }
            row = new LinkedHashMap<>();
            return this;
        }

        @Override
        public Builder put(String name, String value) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(value);
            if (row == null) {
                throw new IllegalStateException();
            }
            row.put(name, value);
            return this;
        }

        @Override
        public Builder put(Map<String, String> parameters) {
            Objects.requireNonNull(parameters);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        @Override
        public BasicParameterTable build() {
            if (row != null) {
                entity.add(row);
                row = null;
            }
            return new BasicParameterTable(entity);
        }

    }
}
