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
package com.asakusafw.utils.java.internal.model.util;

import com.asakusafw.utils.java.model.syntax.ArrayCreationExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.InfixExpression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;

/**
 * Represents an association priorities in expressions.
 */
public enum ExpressionPriority {

    /**
     * The primary expressions.
     */
    PRIMARY,

    /**
     * The array initializers.
     */
    ARRAY_INITIALIZER,

    /**
     * The unary operators.
     */
    UNARY,

    /**
     * The cast operators.
     */
    CAST,

    /**
     * The multiplicative (multiply, division, and remainder) operators.
     */
    MULTIPLICATIVE,

    /**
     * The additive (add and subtract) operators.
     */
    ADDITIVE,

    /**
     * The shift operators.
     */
    SHIFT,

    /**
     * The comparison operators.
     */
    RELATIONAL,

    /**
     * The equality operators.
     */
    EQUALITY,

    /**
     * The logical operators.
     */
    LOGICAL,

    /**
     * The conditional {@code and} operators.
     */
    CONDITIONAL_AND,

    /**
     * The conditional {@code or} operators.
     */
    CONDITIONAL_OR,

    /**
     * The conditional operators.
     */
    CONDITIONAL,

    /**
     * The assignment operators.
     */
    ASSIGNMENT,
    ;

    /**
     * Returns the priority about the target infix operator.
     * @param operator the infix operator
     * @return the corresponded priority
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static ExpressionPriority valueOf(InfixOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        switch (operator) {
        case TIMES:
        case DIVIDE:
        case REMAINDER:
            return MULTIPLICATIVE;

        case PLUS:
        case MINUS:
            return ADDITIVE;

        case LEFT_SHIFT:
        case RIGHT_SHIFT_SIGNED:
        case RIGHT_SHIFT_UNSIGNED:
            return SHIFT;

        case GREATER:
        case GREATER_EQUALS:
        case LESS:
        case LESS_EQUALS:
            return RELATIONAL;

        case EQUALS:
        case NOT_EQUALS:
            return EQUALITY;

        case AND:
        case OR:
        case XOR:
            return LOGICAL;

        case CONDITIONAL_AND:
            return CONDITIONAL_AND;

        case CONDITIONAL_OR:
            return CONDITIONAL_OR;

        default:
            throw new IllegalArgumentException(operator.toString());
        }
    }

    /**
     * Returns the priority about the target expression.
     * @param expression the target expression
     * @return the corresponded priority
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static ExpressionPriority valueOf(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("expression must not be null"); //$NON-NLS-1$
        }
        switch (expression.getModelKind()) {
        case ARRAY_CREATION_EXPRESSION:
            if (((ArrayCreationExpression) expression).getArrayInitializer() == null) {
                return PRIMARY;
            } else {
                return ARRAY_INITIALIZER;
            }
        case ASSIGNMENT_EXPRESSION:
            return ASSIGNMENT;
        case CAST_EXPRESSION:
            return CAST;
        case CONDITIONAL_EXPRESSION:
            return CONDITIONAL;
        case INFIX_EXPRESSION:
            return valueOf(((InfixExpression) expression).getOperator());
        case INSTANCEOF_EXPRESSION:
            return RELATIONAL;
        case POSTFIX_EXPRESSION:
            return UNARY;
        case UNARY_EXPRESSION:
            return UNARY;
        default:
            return PRIMARY;
        }
    }

    /**
     * Returns whether parentheses are required for comparing the priorities or not.
     * @param required the required priority
     * @param requiredInRight {@code true} if the required priority appears in the right term of infix expressions,
     *     otherwise {@code false}
     * @param priority the target priority
     * @return {@code true} if parentheses are required, otherwise {@code false}
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static boolean isParenthesesRequired(
            ExpressionPriority required,
            boolean requiredInRight,
            ExpressionPriority priority) {
        if (required == null) {
            throw new IllegalArgumentException("required must not be null"); //$NON-NLS-1$
        }
        if (priority == null) {
            throw new NullPointerException("required must not be null"); //$NON-NLS-1$
        }
        int contextOrder = required.ordinal() * 2;
        int priorityOrder = priority.ordinal() * 2;
        if (requiredInRight) {
            contextOrder--;
        }
        return (contextOrder < priorityOrder);
    }
}
