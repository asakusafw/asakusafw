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
package com.asakusafw.testdriver.rule;

/**
 * Represents a compare operator.
 * @since 0.7.0
 */
public enum CompareOperator {

    /**
     * {@code <}.
     */
    LESS_THAN("<") { //$NON-NLS-1$
        @Override
        public <T extends Comparable<T>> boolean satisfies(T a, T b) {
            if (a == null || b == null) {
                throw new IllegalArgumentException();
            }
            return a.compareTo(b) < 0;
        }
    },

    /**
     * {@code >}.
     */
    GREATER_THAN(">") { //$NON-NLS-1$
        @Override
        public <T extends Comparable<T>> boolean satisfies(T a, T b) {
            if (a == null || b == null) {
                throw new IllegalArgumentException();
            }
            return a.compareTo(b) > 0;
        }
    },

    /**
     * {@code <=}.
     */
    LESS_THAN_OR_EQUAL("<=") { //$NON-NLS-1$
        @Override
        public <T extends Comparable<T>> boolean satisfies(T a, T b) {
            if (a == null || b == null) {
                throw new IllegalArgumentException();
            }
            return a.compareTo(b) <= 0;
        }
    },

    /**
     * {@code >=}.
     */
    GREATER_THAN_OR_EQUAL(">=") { //$NON-NLS-1$
        @Override
        public <T extends Comparable<T>> boolean satisfies(T a, T b) {
            if (a == null || b == null) {
                throw new IllegalArgumentException();
            }
            return a.compareTo(b) >= 0;
        }
    },
    ;

    private final String symbol;

    private CompareOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the symbol.
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns whether {@code a <operator> b} is satisfied or not.
     * @param <T> the data type
     * @param a the first term
     * @param b the second term
     * @return {@code true} if it is satisfied, or {@code false} otherwise
     */
    public abstract <T extends Comparable<T>> boolean satisfies(T a, T b);
}
