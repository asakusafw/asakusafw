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

/**
 * An abstract super interface which extracting records and their fields.
 * @since 0.9.1
 */
public interface FieldReader extends Closeable {

    /**
     * Advances the cursor and returns whether or not the next record exists.
     * If the current record contains any unread fields, they will be ignored.
     * This method may change the previous {@link #getContent()} result object.
     * @return {@code true} if the next record exists, otherwise {@code false}
     * @throws IOException if I/O error occurred while reading the next record
     * @throws TextFormatException if text format is not valid
     */
    boolean nextRecord() throws IOException;

    /**
     * Advances the cursor and returns whether or not the next field exists in the current record.
     * This method may change the previous {@link #getContent()} result object.
     * @return {@code true} if the next field exists, otherwise {@code false}
     * @throws IOException if I/O error occurred while reading the next field
     * @throws TextFormatException if text format is not valid
     */
    boolean nextField() throws IOException;

    /**
     * Rewinds the cursor to the head of the current record.
     * @throws IOException if I/O error occurred while rewinding fields
     * @throws TextFormatException if text format is not valid
     */
    void rewindFields() throws IOException;

    /**
     * Returns the content of the current field.
     * Note that the content may be <em>raw</em>, it can contain quoting or escape sequences.
     * @return the content, or {@code null} if this represents {@code NULL}
     * @throws TextFormatException if text format is not valid
     */
    CharSequence getContent();

    /**
     * Returns the number of line where the current <em>RECORD</em> starts.
     * @return the line number (0-origin), or {@code -1} if it is not sure
     */
    default long getRecordLineNumber() {
        return -1L;
    }

    /**
     * Returns the record index of the current field.
     * @return the record index (0-origin), or {@code -1} if it is not sure
     */
    default long getRecordIndex() {
        return -1L;
    }

    /**
     * Returns the field index of the current field.
     * @return the field index (0-origin), or {@code -1} if it is not sure
     */
    default long getFieldIndex() {
        return -1L;
    }
}
