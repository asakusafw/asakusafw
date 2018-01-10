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
package com.asakusafw.vocabulary.flow.builder;

import java.util.Objects;

import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.Import;

/**
 * Represents {@link Import} or {@link Export} information.
 * @since 0.9.0
 */
public class ExternInfo {

    private final String name;

    private final Class<?> description;

    /**
     * Creates a new instance.
     * @param name the external port name
     * @param description the external port description class
     */
    public ExternInfo(String name, Class<?> description) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
    }

    /**
     * Returns the external port name.
     * @return the external port name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the external port description class.
     * @return the external port description class
     */
    public Class<?> getDescription() {
        return description;
    }
}
