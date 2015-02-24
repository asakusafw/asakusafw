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
package com.asakusafw.runtime.core.util;

import java.io.IOException;

/**
 * An abstract super class of shared object holder for operator classes.
 * Clients can inherit this class and implement
 * {@link #initialValue()} to prepare shared value.
 * <p>
 * Example:
 * </p>
<pre><code>
// operator class
public abstract class SomeOperator {
    // shared value container
    static final Shared&lt;Hoge&gt; SHARED = new Shared&lt;Hoge&gt;() {
        &#64;Override protected Hoge initialValue() throws IOException {
            // initializes shared value
            Hoge result = ...;
            return result;
        }
    }
    // operator
    &#64;Update
    public void some(Foo foo) {
        // obtains shared value
        Hoge hoge = SHARED.get();
        ...
    }
}
</code></pre>
 * @param <T> the value type
 * @since 0.7.3
 */
public abstract class Shared<T> {

    private T shared;

    private boolean initialized;

    /**
     * Returns the shared value.
     * If this container is not {@link #isInitialzed() initialized} yet,
     * this will prepare an initial value and returns it.
     * @return the shared value
     * @throws Shared.InitializationException if failed to initialize the shared value
     */
    public final synchronized T get() {
        synchronized (this) {
            if (initialized == false) {
                try {
                    this.shared = initialValue();
                    this.initialized = true;
                } catch (IOException e) {
                    throw new InitializationException("failed to initialize shared value", e);
                }
            }
            return shared;
        }
    }

    /**
     * Discards the current shared value.
     * After this is invoked, {@link #isInitialzed()} will return {@code false} until
     * this container is re-initialized.
     * If this does not hold a shared value, this does nothing.
     */
    public final void remove() {
        synchronized (this) {
            this.shared = null;
            this.initialized = false;
        }
    }

    /**
     * Manually sets a shared value.
     * The {@link #get()} method will return the value until {@link #remove()} is invoked.
     * This forcibly replaces the shared value even if this container is already initialized.
     * @param value the shared value
     * @see #isInitialzed()
     */
    public final void set(T value) {
        synchronized (this) {
            this.shared = value;
            this.initialized = true;
        }
    }

    /**
     * Returns whether this container is initialized or not.
     * @return {@code true} if this is initialized, or {@code false} otherwise
     */
    public final boolean isInitialzed() {
        synchronized (this) {
            return this.initialized;
        }
    }

    /**
     * Returns the initial shared value.
     * @return the shared value
     * @throws IOException if initialization was failed
     */
    protected abstract T initialValue() throws IOException;

    /**
     * Represents an exception occurred while {@link Shared#initialValue() initializing} shared values.
     * @since 0.7.3
     */
    public static final class InitializationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance.
         * @param message the exception message
         * @param cause the original cause
         */
        public InitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
