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
package com.asakusafw.workflow.model;

import java.util.Collection;

/**
 * An abstract super interface for execution graph elements.
 * @param <S> this element type
 * @since 0.10.0
 */
public interface GraphElement<S extends GraphElement<S>> extends Element {

    /**
     * Returns the blocker elements.
     * @return the blocker elements
     */
    Collection<? extends S> getBlockers();
}
