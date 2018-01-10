/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.trace.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a list of {@link TraceSetting}s.
 * @since 0.8.1
 */
public class TraceSettingList {

    private final List<TraceSetting> elements;

    /**
     * Creates a new instance.
     * @param elements the setting elements
     */
    public TraceSettingList(Collection<? extends TraceSetting> elements) {
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    /**
     * Returns the setting elements.
     * @return the elements
     */
    public List<TraceSetting> getElements() {
        return elements;
    }
}
