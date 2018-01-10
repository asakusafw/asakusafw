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
package com.asakusafw.utils.java.model.syntax;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents unary operators.
 */
public enum UnaryOperator {

    /**
     * Unary plus.
     */
    PLUS("+", Category.ARITHMETIC), //$NON-NLS-1$

    /**
     * Unary minus.
     */
    MINUS("-", Category.ARITHMETIC), //$NON-NLS-1$

    /**
     * Bit complement.
     */
    COMPLEMENT("~", Category.BITWISE), //$NON-NLS-1$

    /**
     * Logical not.
     */
    NOT("!", Category.LOGICAL), //$NON-NLS-1$

    /**
     * Prefix increment.
     */
    INCREMENT("++", Category.INCREMENT_DECREMENT), //$NON-NLS-1$

    /**
     * Prefix decrement.
     */
    DECREMENT("--", Category.INCREMENT_DECREMENT), //$NON-NLS-1$

    ;

    private final String symbol;

    private final Category category;

    /**
     * Creates a new instance.
     * @param symbol the operator symbol
     * @param category the operator category
     */
    UnaryOperator(String symbol, Category category) {
        assert symbol != null;
        assert category != null;
        this.symbol = symbol;
        this.category = category;
    }

    /**
     * Returns the operator symbol.
     * @return the operator symbol
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Returns the operator category.
     * @return the operator category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns an operator from its symbol.
     * @param symbol the target operator symbol
     * @return the corresponded operator, or {@code null} if there is no such the operator
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static UnaryOperator fromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        return SymbolToUnaryOperator.get(symbol);
    }

    /**
     * Represents an operator kind.
     */
    public enum Category {

        /**
         * Prefix increment/decrement.
         */
        INCREMENT_DECREMENT,

        /**
         * Arithmetic operations.
         */
        ARITHMETIC,

        /**
         * Bitwise operations.
         */
        BITWISE,

        /**
         * Logical operations.
         */
        LOGICAL,
    }

    private static class SymbolToUnaryOperator {

        private static final Map<String, UnaryOperator> REVERSE_DICTIONARY;
        static {
            Map<String, UnaryOperator> map = new HashMap<>();
            for (UnaryOperator elem : UnaryOperator.values()) {
                map.put(elem.getSymbol(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }

        static UnaryOperator get(String key) {
            return REVERSE_DICTIONARY.get(key);
        }
    }
}
