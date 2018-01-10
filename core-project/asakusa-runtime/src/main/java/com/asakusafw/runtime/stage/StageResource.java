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
package com.asakusafw.runtime.stage;

/**
 * Represents a stage resource.
 * @since 0.1.0
 */
public class StageResource {

    private final String location;

    private final String name;

    /**
     * Creates a new instance.
     * @param location the path expression for resources
     * @param name the resource name
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public StageResource(String location, String name) {
        if (location == null) {
            throw new IllegalArgumentException("location must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.location = location;
        this.name = name;
    }

    /**
     * Returns the path expression for the resources.
     * @return the path expression
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the resource name.
     * @return the resource name
     */
    public String getName() {
        return name;
    }
}
