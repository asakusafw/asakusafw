/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Phases each execution replies on.
 * @since 0.2.3
 */
public enum ExecutionPhase {

    /**
     * Setup working space.
     */
    SETUP,

    /**
     * Initialization.
     */
    INITIALIZE,

    /**
     * Importing input data.
     */
    IMPORT,

    /**
     * Preprocessing input data.
     */
    PROLOGUE,

    /**
     * Processing data.
     */
    MAIN,

    /**
     * Postprocessing output data.
     */
    EPILOGUE,

    /**
     * Exporting output data.
     */
    EXPORT,

    /**
     * Finalization.
     */
    FINALIZE,

    /**
     * Cleanup working space.
     */
    CLEANUP,
    ;

    /**
     * Returns the symbol of this phase.
     * This symbol is used in {@link ExecutionScript}s.
     * @return the symbol of this phase
     */
    public String getSymbol() {
        return name().toLowerCase();
    }

    /**
     * Returns an {@link ExecutionPhase} corresponded to the symbol.
     * @param symbol target symbol
     * @return the corresponding phase, or {@code null} if not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ExecutionPhase findFromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        return Lazy.SYMBOLS.get(symbol);
    }

    @Override
    public String toString() {
        return getSymbol();
    }

    private static final class Lazy {

        static final Map<String, ExecutionPhase> SYMBOLS;
        static {
            Map<String, ExecutionPhase> map = new HashMap<>();
            for (ExecutionPhase phase : values()) {
                map.put(phase.getSymbol(), phase);
            }
            SYMBOLS = Collections.unmodifiableMap(map);
        }

        private Lazy() {
            return;
        }
    }
}
