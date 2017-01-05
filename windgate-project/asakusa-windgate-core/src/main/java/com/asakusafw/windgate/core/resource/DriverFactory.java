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
package com.asakusafw.windgate.core.resource;

import java.io.IOException;

import com.asakusafw.windgate.core.ProcessScript;

/**
 * An interface to provide {@link SourceDriver} and {@link DrainDriver}.
 * @since 0.2.2
 */
public interface DriverFactory {

    /**
     * Creates a new source driver.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to create a driver
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException;

    /**
     * Creates a new drain driver.
     * @param <T> target data model type
     * @param script target process
     * @return the created driver
     * @throws IOException if failed to create a driver
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException;
}
