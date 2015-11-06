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
package com.asakusafw.utils.java.model.syntax;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents infix operators.
 */
public enum InfixOperator {

    /**
     * Assignment operator.
     */
    ASSIGN(
        "=", //$NON-NLS-1$
        EnumSet.of(Context.ASSIGNMENT),
        Category.ASSIGNMENT),

    /**
     * Add operator.
     */
    PLUS(
        "+", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.ADDITIVE),

    /**
     * Subtract operator.
     */
    MINUS(
        "-", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.ADDITIVE),

    /**
     * Multiply operator.
     */
    TIMES(
        "*", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.MULTIPLICATIVE),

    /**
     * Division operator.
     */
    DIVIDE(
        "/", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.MULTIPLICATIVE),

    /**
     * Remainder operator.
     */
    REMAINDER(
        "%", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.MULTIPLICATIVE),

    /**
     * Left shift operator.
     */
    LEFT_SHIFT(
        "<<", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.SHIFT),

    /**
     * Signed right shift operator.
     */
    RIGHT_SHIFT_SIGNED(
        ">>", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.SHIFT),

    /**
     * Unsigned right shift operator.
     */
    RIGHT_SHIFT_UNSIGNED(
        ">>>", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.SHIFT),

    /**
     * Bitwise/logical or operator.
     */
    OR(
        "|", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.BITWISE),

    /**
     * Bitwise/logical and operator.
     */
    AND(
        "&", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.BITWISE),

    /**
     * Bitwise/logical exclusive or operator.
     */
    XOR(
        "^", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.BITWISE),

    /**
     * Equal operator.
     */
    EQUALS(
        "==", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.EQUALITY),

    /**
     * Not equal operator.
     */
    NOT_EQUALS(
        "!=", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.EQUALITY),

    /**
     * Greater than operator.
     */
    GREATER(
        ">", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * Less than operator.
     */
    LESS(
        "<", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * Greater than or equal to operator.
     */
    GREATER_EQUALS(
        ">=", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * Less than or equal to operator.
     */
    LESS_EQUALS(
        "<=", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * Conditional or operator.
     */
    CONDITIONAL_OR(
        "||", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.CONDITIONAL),

    /**
     * Conditional and operator.
     */
    CONDITIONAL_AND(
        "&&", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.CONDITIONAL),

    ;

    private final String symbol;

    private final Set<Context> permittedContexts;

    private final Category category;

    /**
     * Creates a new instance.
     * @param symbol the operator symbol
     * @param permitted set of the permitted contexts
     * @param category the operator category
     */
    private InfixOperator(String symbol, EnumSet<Context> permitted, Category category) {
        assert symbol != null;
        assert permitted != null;
        assert category != null;
        this.symbol = symbol;
        this.permittedContexts = Collections.unmodifiableSet(permitted);
        this.category = category;
    }

    /**
     * Returns the infix operator symbol.
     * @return the infix operator symbol
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Returns the assignment operator symbol.
     * @return the assignment operator symbol
     */
    public String getAssignmentSymbol() {
        if (this == ASSIGN) {
            return ASSIGN.getSymbol();
        } else {
            return getSymbol() + ASSIGN.getSymbol();
        }
    }

    /**
     * Returns an operator from its symbol.
     * @param symbol the target operator symbol
     * @return the corresponded operator, or {@code null} if there is no such the operator
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static InfixOperator fromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        return SymbolToInfixOperator.get(symbol);
    }

    /**
     * Returns whether this operator can be used in the specified context or not.
     * @param context the target context
     * @return {@code true} if this operator can be used in the specified context, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public boolean isPermitted(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return permittedContexts.contains(context);
    }

    /**
     * Returns the operator category.
     * @return the operator category
     */
    public InfixOperator.Category getCategory() {
        return this.category;
    }

    /**
     * Represents a kind of permissive context.
     */
    public static enum Context {

        /**
         * The operator can be used as infix operator.
         */
        INFIX,

        /**
         *
         * The operator can be used as assignment operator.
         */
        ASSIGNMENT,
    }

    /**
     * Represents an operator kind.
     */
    public static enum Category {

        /**
         * multiplicative operators ({@literal 15.17}).
         */
        MULTIPLICATIVE,

        /**
         * additive operators ({@literal 15.18}).
         */
        ADDITIVE,

        /**
         * shift operators ({@literal 15.19}).
         */
        SHIFT,

        /**
         * relational operators ({@literal 15.20}).
         */
        RELATIONAL,

        /**
         * equality operators ({@literal 15.21}).
         */
        EQUALITY,

        /**
         * bitwise/logical operators ({@literal 15.22}).
         */
        BITWISE,

        /**
         * conditional operator ({@literal 15.23}, {@literal 15.24}).
         */
        CONDITIONAL,

        /**
         * assignment operators ({@literal 15.23}, {@literal 15.26}).
         */
        ASSIGNMENT,
    }

    private static class SymbolToInfixOperator {

        private static final Map<String, InfixOperator> REVERSE_DICTIONARY;
        static {
            Map<String, InfixOperator> map = new HashMap<String, InfixOperator>();
            for (InfixOperator elem : InfixOperator.values()) {
                map.put(elem.getSymbol(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }

        static InfixOperator get(String key) {
            return REVERSE_DICTIONARY.get(key);
        }
    }
}
