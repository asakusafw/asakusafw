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
package com.asakusafw.runtime.core.api;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An Asakusa Framework API stub.
 * @param <T> the API type
 * @since 0.9.0
 */
public class ApiStub<T> {

    private final T defaultImplementation;

    private volatile T activeImplementation;

    private int referenceCount;

    /**
     * Creates a new instance without default implementation.
     */
    public ApiStub() {
        this(null);
    }

    /**
     * Creates a new instance.
     * @param defaultImplementation the default implementation (nullable)
     */
    public ApiStub(T defaultImplementation) {
        this.defaultImplementation = defaultImplementation;
    }

    /**
     * Returns the current active implementation on this API stub.
     * @return the active implementation
     * @throws IllegalStateException if there are no available implementations
     */
    public T get() {
        T impl = activeImplementation;
        if (impl != null) {
            return impl;
        }
        if (defaultImplementation != null) {
            return defaultImplementation;
        }
        throw new IllegalStateException(
                "there are no available active Asakusa Framework API implementations (internal error)"); //$NON-NLS-1$
    }

    /**
     * Activates the given API implementation on this stub.
     * @param implementation the target implementation
     * @return the reference of the implementation, must be closed after the API was disposed
     * @throws IllegalStateException if another implementation has been activated
     */
    public Reference<T> activate(T implementation) {
        synchronized (this) {
            if (activeImplementation == null) {
                activeImplementation = implementation;
                referenceCount = 1;
            } else if (activeImplementation == implementation) {
                referenceCount++;
            } else {
                throw new IllegalStateException(MessageFormat.format(
                        "Asakusa Framework API has conflict implementations (internal error): {0} <=> {1}",  //$NON-NLS-1$
                        activeImplementation, implementation));
            }
            return new Reference<>(this, implementation);
        }
    }

    void release(T implementation) {
        synchronized (this) {
            if (activeImplementation == implementation && referenceCount > 0) {
                if (--referenceCount == 0) {
                    activeImplementation = null;
                }
            }
        }
    }

    /**
     * A reference of API implementation.
     * @param <T> the API type
     * @since 0.9.0
     */
    public static final class Reference<T> implements AutoCloseable {

        private final ApiStub<T> owner;

        private final AtomicReference<T> entity;

        Reference(ApiStub<T> owner, T entity) {
            Objects.requireNonNull(owner);
            Objects.requireNonNull(entity);
            this.owner = owner;
            this.entity = new AtomicReference<>(entity);
        }

        /**
         * Returns the entity.
         * @return the entity, or {@code null} if the reference is not available
         */
        public AtomicReference<T> getEntity() {
            return entity;
        }

        @Override
        public void close() {
            T impl = entity.getAndSet(null);
            if (impl != null) {
                owner.release(impl);
            }
        }
    }
}
