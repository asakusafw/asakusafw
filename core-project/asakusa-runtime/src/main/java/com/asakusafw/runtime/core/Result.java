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
package com.asakusafw.runtime.core;

/**
 * Represents a result sink of operators.
 * @param <T> the data type
 */
@FunctionalInterface
public interface Result<T> {

    /**
     * Adds an object into this result.
     * Generally, this object accepts two or more objects per an operation.
     * This method will modify properties in the added object, so that clients should create a copy the object
     * if the object will be continuously used.
     * @param result the target object
     * @throws Result.OutputException if error was occurred while processing the added object
     */
    void add(T result);

    /**
     * An {@link OutputException} is thrown when a {@link Result} was failed to output data model objects.
     */
    class OutputException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance.
         */
        public OutputException() {
            super();
        }

        /**
         * Creates a new instance.
         * @param message message (nullable)
         */
        public OutputException(String message) {
            super(message);
        }

        /**
         * Creates a new instance.
         * @param cause original cause (nullable)
         */
        public OutputException(Throwable cause) {
            super(cause);
        }

        /**
         * Creates a new instance.
         * @param message message (nullable)
         * @param cause original cause (nullable)
         */
        public OutputException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
