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
package com.asakusafw.testdriver.compiler.basic;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.asakusafw.testdriver.compiler.GraphElement;

/**
 * A skeletal implementation of {@link GraphElement}.
 * @param <S> this element type
 * @since 0.8.0
 */
public abstract class AbstractGraphElement<S extends GraphElement<S>> implements GraphElement<S> {

    private final Set<S> blockers = new LinkedHashSet<>();

    /**
     * Adds a blocker element.
     * @param blocker the blocker element
     */
    public void addBlocker(S blocker) {
        Objects.requireNonNull(blocker);
        this.blockers.add(blocker);
    }

    @Override
    public Set<S> getBlockers() {
        return blockers;
    }
}
