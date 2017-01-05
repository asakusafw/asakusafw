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
package com.asakusafw.runtime.flow.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link LookUpTable} that holds objects on the memory.
 * @param <T> the element type
 */
public class VolatileLookUpTable<T> implements LookUpTable<T> {

    private final Map<LookUpKey.View, List<T>> entity;

    VolatileLookUpTable(Map<LookUpKey.View, List<T>> entity) {
        this.entity = entity;
    }

    @Override
    public List<T> get(LookUpKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        List<T> list = entity.get(key.getDirectView());
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * A builder for {@link VolatileLookUpTable}.
     * @param <T> the element type
     */
    public static class Builder<T> implements LookUpTable.Builder<T> {

        private final Map<LookUpKey.View, List<T>> entity = new HashMap<>();

        @Override
        public void add(LookUpKey key, T value) throws IOException {
            if (key == null) {
                throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
            }
            List<T> list = entity.get(key.getDirectView());
            if (list == null) {
                list = new ArrayList<>(1);
                entity.put(key.getFrozenView(), list);
            }
            list.add(value);
        }

        @Override
        public LookUpTable<T> build() throws IOException {
            return new VolatileLookUpTable<>(entity);
        }
    }
}
