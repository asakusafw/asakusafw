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
package com.asakusafw.workflow.model.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.asakusafw.workflow.model.GraphElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A skeletal implementation of {@link GraphElement}.
 * @param <S> this element type
 * @since 0.10.0
 */
public abstract class AbstractGraphElement<S extends GraphElement<S>>
        extends AbstractElement implements GraphElement<S> {

    @JsonProperty("blockers")
    @JsonInclude(Include.NON_EMPTY)
    private final List<S> blockers = new ArrayList<>();

    @Override
    public List<S> getBlockers() {
        return blockers;
    }

    /**
     * Adds a blocker element.
     * @param blocker the blocker element
     */
    public void addBlocker(S blocker) {
        Objects.requireNonNull(blocker);
        this.blockers.add(blocker);
    }

    /**
     * Sets blocker elements.
     * @param blockers the blocker elements
     */
    protected void setBlockers(Collection<? extends S> blockers) {
        this.blockers.clear();
        if (blockers != null) {
            this.blockers.addAll(blockers);
        }
    }
}
