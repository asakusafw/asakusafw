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
package com.asakusafw.windgate.cli;


/**
 * Execution mode.
 */
public enum ExecutionKind {

    /**
     * Creates and keeps a session.
     */
    BEGIN("begin", true, false),

    /**
     * Attaches and keeps a session .
     */
    CONTINUE("continue", false, false),

    /**
     * Attaches and completes a session.
     */
    END("end", false, true),

    /**
     * Creates and completes a session.
     */
    ONESHOT("oneshot", true, true),
    ;

    /**
     * The symbol of this kind.
     */
    public final String symbol;

    /**
     * Whether this execution creates a new session.
     */
    public final boolean createsSession;

    /**
     * Whether this execution completes a session.
     */
    public final boolean completesSession;

    ExecutionKind(String symbol, boolean createsSession, boolean completesSession) {
        assert symbol != null;
        this.symbol = symbol;
        this.createsSession = createsSession;
        this.completesSession = completesSession;
    }

    /**
     * Returns a constant corresponded to the specified symbol.
     * @param symbol the symbol of kind
     * @return the corresponding constant, or {@code null} if does not exist such a constant
     */
    public static ExecutionKind parse(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        for (ExecutionKind kind : ExecutionKind.values()) {
            if (symbol.equals(kind.symbol)) {
                return kind;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
