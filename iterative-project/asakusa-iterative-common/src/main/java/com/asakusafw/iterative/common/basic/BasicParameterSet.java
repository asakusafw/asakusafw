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
package com.asakusafw.iterative.common.basic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.asakusafw.iterative.common.ParameterSet;

/**
 * A basic implementation of {@link ParameterSet}.
 * @since 0.8.0
 */
public class BasicParameterSet implements ParameterSet {

    private final Map<String, String> entity;

    /**
     * Creates a new instance.
     * @param entity the entity
     */
    public BasicParameterSet(Map<String, String> entity) {
        Objects.requireNonNull(entity);
        for (Map.Entry<String, String> entry : entity.entrySet()) {
            Objects.requireNonNull(entry.getKey());
            Objects.requireNonNull(entry.getValue());
        }
        this.entity = Collections.unmodifiableMap(new LinkedHashMap<>(entity));
    }

    @Override
    public boolean isAvailable(String name) {
        Objects.requireNonNull(name);
        return toMap().containsKey(name);
    }

    @Override
    public String get(String name) {
        Objects.requireNonNull(name);
        return toMap().get(name);
    }

    @Override
    public Set<String> getAvailable() {
        return toMap().keySet();
    }

    @Override
    public Map<String, String> toMap() {
        return entity;
    }

    @Override
    public String toString() {
        return toMap().toString();
    }
}
