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
package com.asakusafw.vocabulary.flow.builder;

import java.util.Objects;

/**
 * Represents operator data information.
 * @since 0.9.0
 */
public class DataInfo extends EdgeInfo<DataInfo> {

    private final String name;

    private final Data data;

    /**
     * Creates a new instance.
     * @param name the target name
     * @param data the target data
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataInfo(String name, Data data) {
        this.name = Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        this.data = Objects.requireNonNull(data, "data must not be null"); //$NON-NLS-1$
    }

    @Override
    protected DataInfo getSelf() {
        return this;
    }

    /**
     * Returns the target name.
     * @return the target name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the data.
     * @return the data
     */
    public Data getData() {
        return data;
    }
}
