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
/*
 * Copyright 2008 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.asakusafw.utils.java.internal.model.util;

import com.asakusafw.utils.java.model.syntax.ArrayCreationExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.InfixExpression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;

/**
 * 式の優先順位。
 */
public enum ExpressionPriority {

    /**
     * 一次式。
     */
    PRIMARY,

    /**
     * 配列初期化子。
     */
    ARRAY_INITIALIZER,

    /**
     * 単項演算子。
     */
    UNARY,

    /**
     * キャスト演算子。
     */
    CAST,

    /**
     * 乗除演算子。
     */
    MULTIPLICATIVE,

    /**
     * 加減演算子。
     */
    ADDITIVE,

    /**
     * シフト演算子。
     */
    SHIFT,

    /**
     * 比較演算子。
     */
    RELATIONAL,

    /**
     * 等価演算子。
     */
    EQUALITY,

    /**
     * 論理演算子。
     */
    LOGICAL,

    /**
     * 条件And演算子。
     */
    CONDITIONAL_AND,

    /**
     * 条件Or演算子。
     */
    CONDITIONAL_OR,

    /**
     * 三項演算子。
     */
    CONDITIONAL,

    /**
     * 代入演算子。
     */
    ASSIGNMENT,

    ;

    /**
     * 二項演算子をこの表現に変換して返す。
     * @param operator 二項演算子
     * @return 対応する表現
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * 式をこの表現に変換して返す。
     * @param expression 対象の式
     * @return 対応する表現
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * 指定の優先度比較で、かっこが必要となる場合のみ{@code true}を返す。
     * @param required 要求された優先度
     * @param requiredInRight 要求された優先度が二項演算子の右に出現する場合のみ{@code true}
     * @param priority 検査する優先度
     * @return かっこが必要となる場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
