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
package com.asakusafw.runtime.io.json;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Writes values into data sources.
 * @since 0.10.3
 */
public interface ValueWriter {

    /**
     * Puts {@code null}.
     * @throws IOException if I/O error was occurred
     */
    void writeNull() throws IOException;

    /**
     * Puts a string.
     * @param sequence the character sequence
     * @param offset the character offset
     * @param length the character length (in UCS-16 characters)
     * @throws IOException if I/O error was occurred
     */
    void writeString(CharSequence sequence, int offset, int length) throws IOException;

    /**
     * Puts a string value.
     * @param value the value
     * @throws IOException if I/O error was occurred
     */
    default void writeString(CharSequence value) throws IOException {
        writeString(value, 0, value.length());
    }

    /**
     * Puts a decimal value.
     * @param value the value
     * @throws IOException if I/O error was occurred
     */
    void writeDecimal(BigDecimal value) throws IOException;

    /**
     * Puts an integer value.
     * @param value the value
     * @throws IOException if I/O error was occurred
     */
    default void writeInt(int value) throws IOException {
        writeLong(value);
    }

    /**
     * Puts an integer value.
     * @param value the value
     * @throws IOException if I/O error was occurred
     */
    void writeLong(long value) throws IOException;

    /**
     * Puts a floating point number value.
     * @param value the value
     * @throws IOException if I/O error was occurred
     */
    default void writeFloat(float value) throws IOException {
        writeDouble(value);
    }

    /**
     * Puts a floating point number value.
     * @param value the value
     * @throws IOException if I/O error was occurred
     */
    void writeDouble(double value) throws IOException;

    /**
     * Puts a boolean value.
     * @param value the value
     * @throws IOException if I/O error was occurred
     */
    void writeBoolean(boolean value) throws IOException;
}
