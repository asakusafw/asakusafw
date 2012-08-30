/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages execution locks.
 * @since 0.2.3
 */
public abstract class ExecutionLock implements Closeable {

    static final Logger LOG = LoggerFactory.getLogger(ExecutionLock.class);

    /**
     * Null implementation of {@link ExecutionLock}.
     */
    public static final ExecutionLock NULL = new ExecutionLock() {

        @Override
        public void endFlow(String flowId, String executionId) {
            return;
        }

        @Override
        public void beginFlow(String flowId, String executionId) {
            return;
        }

        @Override
        public void close() throws IOException {
            return;
        }
    };

    /**
     * Begin to process a flow using this lock.
     * @param flowId target flow ID
     * @param executionId  target execution ID
     * @throws IOException if failed to operate
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract void beginFlow(String flowId, String executionId) throws IOException;

    /**
     * End to process a flow using this lock.
     * @param flowId target flow ID
     * @param executionId  target execution ID
     * @throws IOException if failed to operate
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract void endFlow(String flowId, String executionId) throws IOException;

    /**
     * Represents lock scope.
     * @since 0.2.3
     */
    public enum Scope {

        /**
         * World scope.
         * Only single process can execute any batch.
         */
        WORLD,

        /**
         * Batch scope.
         * Only single process can execute particular batch.
         */
        BATCH,

        /**
         * Flow scope.
         * Only single process can execute particular flow.
         */
        FLOW,

        /**
         * Execution scope.
         * Different processes can execute the same flow as long as these execution IDs are different.
         */
        EXECUTION,
        ;

        /**
         * Returns the default scope.
         * @return the default scope
         */
        public static Scope getDefault() {
            return EXECUTION;
        }

        /**
         * Returns the symbol of this scope.
         * @return the symbol of this scope
         */
        public String getSymbol() {
            return name().toLowerCase();
        }

        @Override
        public String toString() {
            return getSymbol();
        }

        /**
         * Returns an {@link Scope} corresponded to the symbol.
         * @param symbol target symbol
         * @return the corresponding scope, or {@code null} if not found
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public static Scope findFromSymbol(String symbol) {
            if (symbol == null) {
                throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
            }
            return Lazy.SYMBOLS.get(symbol);
        }

        private static final class Lazy {

            static final Map<String, Scope> SYMBOLS;
            static {
                Map<String, Scope> map = new HashMap<String, Scope>();
                for (Scope phase : values()) {
                    map.put(phase.getSymbol(), phase);
                }
                SYMBOLS = Collections.unmodifiableMap(map);
            }

            private Lazy() {
                return;
            }
        }
    }
}
