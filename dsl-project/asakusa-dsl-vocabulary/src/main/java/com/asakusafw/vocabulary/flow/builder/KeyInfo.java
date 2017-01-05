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
package com.asakusafw.vocabulary.flow.builder;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.model.Key;

/**
 * Represents {@link Key} information.
 * @since 0.9.0
 */
public class KeyInfo {

    private final List<String> group = new ArrayList<>();

    private final List<ShuffleKey.Order> order = new ArrayList<>();

    /**
     * Adds a grouping key.
     * @param name the property name
     * @return this
     */
    public KeyInfo group(String name) {
        group.add(name);
        return this;
    }

    /**
     * Adds an ascendant ordering property.
     * @param name the property name
     * @return this
     */
    public KeyInfo ascendant(String name) {
        order.add(new ShuffleKey.Order(name, ShuffleKey.Direction.ASC));
        return this;
    }

    /**
     * Adds a descendant ordering property.
     * @param name the property name
     * @return this
     */
    public KeyInfo descendant(String name) {
        order.add(new ShuffleKey.Order(name, ShuffleKey.Direction.DESC));
        return this;
    }

    ShuffleKey toShuffleKey() {
        return new ShuffleKey(group, order);
    }
}
