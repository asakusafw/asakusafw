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
package com.asakusafw.runtime.io.text;

import java.io.Closeable;
import java.io.IOException;

import com.asakusafw.runtime.io.text.driver.FieldOutput;

/**
 * An abstract super interface which writing records and their fields.
 * @since 0.9.1
 */
public interface FieldWriter extends Closeable {

    /**
     * Puts a next field into the current writing record.
     * @param output the field contents
     * @throws IOException if I/O error was occurred while writing the next field
     */
    void putField(FieldOutput output) throws IOException;

    /**
     * Puts an end-of-record mark and starts writing the next record.
     * @throws IOException if I/O error was occurred while writing the end-of-record mark
     * @throws UnmappableOutputException if the current record contained some unmappable sequences
     */
    void putEndOfRecord() throws IOException;
}
