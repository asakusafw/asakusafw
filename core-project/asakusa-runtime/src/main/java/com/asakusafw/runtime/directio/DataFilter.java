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
package com.asakusafw.runtime.directio;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract super class of filter for Direct I/O file inputs.
 * Clients should not use framework APIs in this sub-classes.
 * To use batch arguments, please implement {@link #initialize(Context)} and obtain them from the context object.
 * @param <T> the target data model type
 * @since 0.7.3
 */
public abstract class DataFilter<T> {

    /**
     * Initializes this object.
     * @param context the current context
     */
    public void initialize(Context context) {
        return;
    }

    /**
     * Returns whether this filter accepts the target file or not.
     * @param path the target file path
     * @return {@code true} to accepts the target file, otherwise {@code false}
     */
    public boolean acceptsPath(String path) {
        return true;
    }

    /**
     * Returns whether this filter accepts the target data or not.
     * @param data the target data
     * @return {@code true} to accepts the target data, otherwise {@code false}
     */
    public boolean acceptsData(T data) {
        return true;
    }

    /**
     * Context object for {@link DataFilter}.
     */
    public static class Context {

        private final Map<String, String> batchArguments;

        /**
         * Creates a new instance without batch arguments.
         */
        public Context() {
            this(Collections.emptyMap());
        }

        /**
         * Creates a new instance.
         * @param batchArguments the target batch arguments
         */
        public Context(Map<String, String> batchArguments) {
            this.batchArguments = Collections.unmodifiableMap(new HashMap<>(batchArguments));
        }

        /**
         * Returns the batch arguments.
         * @return the batch arguments
         */
        public Map<String, String> getBatchArguments() {
            return batchArguments;
        }
    }
}
