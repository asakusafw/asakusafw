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
package com.asakusafw.utils.java.model.syntax;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents postfix operators.
 */
public enum PostfixOperator {

    /**
     * Postfix increment.
     */
    INCREMENT("++"), //$NON-NLS-1$

    /**
     * Postfix decrement.
     */
    DECREMENT("--"), //$NON-NLS-1$

    ;

    private final String symbol;

    /**
     * Creates a new instance.
     * @param symbol the operator symbol
     */
    private PostfixOperator(String symbol) {
        assert symbol != null;
        this.symbol = symbol;
    }

    /**
     * Returns the operator symbol.
     * @return the operator symbol
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Returns an operator from its symbol.
     * @param symbol the target operator symbol
     * @return the corresponded operator, or {@code null} if there is no such the operator
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static PostfixOperator fromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        return SymbolToPostfixOperator.get(symbol);
    }

    private static class SymbolToPostfixOperator {

        private static final Map<String, PostfixOperator> REVERSE_DICTIONARY;
        static {
            Map<String, PostfixOperator> map = new HashMap<>();
            for (PostfixOperator elem : PostfixOperator.values()) {
                map.put(elem.getSymbol(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }

        static PostfixOperator get(String key) {
            return REVERSE_DICTIONARY.get(key);
        }
    }
}
