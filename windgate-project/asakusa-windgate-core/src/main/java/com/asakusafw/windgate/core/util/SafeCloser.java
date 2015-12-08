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
package com.asakusafw.windgate.core.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Closes objects safely.
 * @param <T> the target {@link Closeable}
 * @since 0.8.0
 */
public class SafeCloser<T extends Closeable> implements Closeable {

    private T value;

    /**
     * Creates a new empty instance.
     */
    public SafeCloser() {
        this(null);
    }

    /**
     * Creates a new instance with target {@link Closeable}.
     * @param value the target object (nullable)
     */
    public SafeCloser(T value) {
        this.value = value;
    }

    /**
     * Returns the target object.
     * @return the target object
     * @throws IllegalStateException if no objects were set
     */
    public T get() {
        if (value == null) {
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Sets the target object.
     * @param newValue the target object
     * @throws IllegalStateException if the target object was already set
     */
    public void set(T newValue) {
        if (value != null) {
            throw new IllegalStateException();
        }
        this.value = newValue;
    }

    @Override
    public void close() throws IOException {
        if (value != null) {
            try {
                value.close();
            } catch (IOException e) {
                handle(e);
            }
            value = null;
        }
    }

    /**
     * Handles an exception while closing the target object.
     * @param exception the occurred exception
     * @throws IOException if the exception was re-thrown
     */
    protected void handle(IOException exception) throws IOException {
        throw exception;
    }
}
