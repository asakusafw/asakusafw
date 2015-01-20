/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.resource;

import java.io.IOException;

import com.asakusafw.windgate.core.ProcessScript;

/**
 * An abstract implementation of logical data store manipulator.
 * Generally this is used for testing that the application uses the corresponding {@link ResourceMirror}.
 * @since 0.2.2
 */
public abstract class ResourceManipulator {

    /**
     * Returns the name of this resource.
     * @return the name of this resource
     */
    public abstract String getName();

    /**
     * Cleanups the source of this resource.
     * @param script target process
     * @throws IOException if failed to cleanup this resource
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract void cleanupSource(ProcessScript<?> script) throws IOException;

    /**
     * Cleanups the drain of this resource.
     * @param script target process
     * @throws IOException if failed to cleanup this resource
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract void cleanupDrain(ProcessScript<?> script) throws IOException;

    /**
     * Creates a new source driver for the source of this resource.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to open this resource
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract <T> SourceDriver<T> createSourceForSource(ProcessScript<T> script) throws IOException;

    /**
     * Creates a new drain driver for the source of this resource.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to open this resource
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract <T> DrainDriver<T> createDrainForSource(ProcessScript<T> script) throws IOException;

    /**
     * Creates a new source driver for the drain of this resource.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to open this resource
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract <T> SourceDriver<T> createSourceForDrain(ProcessScript<T> script) throws IOException;

    /**
     * Creates a new drain driver for the drain of this resource.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to open this resource
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract <T> DrainDriver<T> createDrainForDrain(ProcessScript<T> script) throws IOException;
}
