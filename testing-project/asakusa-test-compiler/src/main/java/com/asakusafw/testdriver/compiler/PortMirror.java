/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.testdriver.compiler;

/**
 * Represents an external I/O port.
 * @param <T> the importer/exporter description object type
 * @since 0.8.0
 */
public interface PortMirror<T> extends Mirror {

    /**
     * Returns the port name.
     * @return the port name
     */
    String getName();

    /**
     * Returns the data type.
     * @return the data type
     */
    Class<?> getDataType();

    /**
     * Returns the importer/exporter description object.
     * @return the description object
     */
    T getDescription();
}
