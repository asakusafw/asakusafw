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
 * Provides values from data sources.
 * @since 0.10.3
 */
public interface ValueReader {

    /**
     * Provides null value.
     * @see #isNull()
     */
    ValueReader NULL = new ValueReader() {

        @Override
        public boolean isNull() throws IOException {
            return true;
        }

        @Override
        public void readString(StringBuilder buffer) throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public long readLong() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public int readInt() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public float readFloat() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public double readDouble() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public BigDecimal readDecimal() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public boolean readBoolean() throws IOException {
            throw new IllegalStateException();
        }
    };

    /**
     * Reads the next value and tests whether or not it exists.
     * @return {@code true} if it exists, or false otherwise
     * @throws IOException if I/O error was occurred
     */
    boolean isNull() throws IOException;

    /**
     * Reads the next value as a string, and put it into the given buffer.
     * @param buffer the destination buffer
     * @throws IOException if I/O error was occurred
     * @throws IllegalStateException if the next value is invalid
     * @see #isNull()
     */
    void readString(StringBuilder buffer) throws IOException;

    /**
     * Reads the next value as a string.
     * @return the read value, never {@code null}
     * @throws IOException if I/O error was occurred
     * @see #isNull()
     */
    default String readString() throws IOException {
        StringBuilder buf = new StringBuilder();
        readString(buf);
        return buf.toString();
    }

    /**
     * Reads the next value as a decimal.
     * @return the read value, never {@code null}
     * @throws IOException if I/O error was occurred
     * @throws IllegalStateException if the next value is invalid
     * @throws NumberFormatException if the next value is invalid
     * @throws ArithmeticException if the next value is invalid
     * @see #isNull()
     */
    default BigDecimal readDecimal() throws IOException {
        String value = readString();
        return new BigDecimal(value);
    }

    /**
     * Reads the next value as an integer.
     * @return the read value
     * @throws IOException if I/O error was occurred
     * @throws IllegalStateException if the next value is invalid
     * @throws NumberFormatException if the next value is invalid
     * @throws ArithmeticException if the next value is invalid
     * @see #isNull()
     */
    default int readInt() throws IOException {
        long value = readLong();
        int result = (int) value;
        if (value != result) {
            throw new ArithmeticException(String.format("int overflow: %,d", value));
        }
        return result;
    }

    /**
     * Reads the next value as an integer.
     * @return the read value
     * @throws IOException if I/O error was occurred
     * @throws IllegalStateException if the next value is invalid
     * @throws NumberFormatException if the next value is invalid
     * @throws ArithmeticException if the next value is invalid
     * @see #isNull()
     */
    long readLong() throws IOException;

    /**
     * Reads the next value as a floating point number value.
     * @return the read value
     * @throws IOException if I/O error was occurred
     * @throws IllegalStateException if the next value is invalid
     * @throws NumberFormatException if the next value is invalid
     * @throws ArithmeticException if the next value is invalid
     * @see #isNull()
     */
    default float readFloat() throws IOException {
        return (float) readDouble();
    }

    /**
     * Reads the next value as a floating point number value.
     * @return the read value
     * @throws IOException if I/O error was occurred
     * @throws IllegalStateException if the next value is invalid
     * @throws NumberFormatException if the next value is invalid
     * @throws ArithmeticException if the next value is invalid
     * @see #isNull()
     */
    double readDouble() throws IOException;

    /**
     * Reads the next value as a boolean.
     * @return the read value
     * @throws IOException if I/O error was occurred
     * @throws IllegalStateException if the next value is invalid
     * @see #isNull()
     */
    boolean readBoolean() throws IOException;
}
