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
package com.asakusafw.runtime.io.json;

import java.io.IOException;

/**
 * Edits properties.
 * @param <T> the property type
 * @since 0.10.3
 */
public interface PropertyAdapter<T> {

    /**
     * Sets "absent" value into the given property.
     * @param property the destination property
     * @throws RuntimeException if "absent" is not a suitable for the destination property (may not recoverable)
     */
    void absent(T property);

    /**
     * Reads a value and set it into the given property.
     * @param reader the source reader
     * @param property the destination property
     * @throws IOException IOException if I/O error was occurred
     * @throws RuntimeException if the property value is not a suitable for the destination property
     */
    void read(ValueReader reader, T property) throws IOException;

    /**
     * Writes a value from the given property.
     * @param property the source property
     * @param writer the destination writer
     * @throws IOException if I/O error was occurred
     */
    void write(T property, ValueWriter writer) throws IOException;
}
