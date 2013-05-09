/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * 二項演算子。
 */
public enum InfixOperator {

    /**
     * 単一代入演算子。
     */
    ASSIGN(
        "=", //$NON-NLS-1$
        EnumSet.of(Context.ASSIGNMENT),
        Category.ASSIGNMENT),

    /**
     * 加算。
     */
    PLUS(
        "+", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.ADDITIVE),

    /**
     * 減算。
     */
    MINUS(
        "-", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.ADDITIVE),

    /**
     * 乗算。
     */
    TIMES(
        "*", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.MULTIPLICATIVE),

    /**
     * 除算。
     */
    DIVIDE(
        "/", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.MULTIPLICATIVE),

    /**
     * 剰余算。
     */
    REMAINDER(
        "%", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.MULTIPLICATIVE),

    /**
     * 左シフト。
     */
    LEFT_SHIFT(
        "<<", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.SHIFT),

    /**
     * 右算術シフト。
     */
    RIGHT_SHIFT_SIGNED(
        ">>", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.SHIFT),

    /**
     * 右論理シフト。
     */
    RIGHT_SHIFT_UNSIGNED(
        ">>>", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.SHIFT),

    /**
     * 論理和。
     */
    OR(
        "|", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.BITWISE),

    /**
     * 論理積。
     */
    AND(
        "&", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.BITWISE),

    /**
     * 排他的論理和。
     */
    XOR(
        "^", //$NON-NLS-1$
        EnumSet.of(Context.INFIX, Context.ASSIGNMENT),
        Category.BITWISE),

    /**
     * 同一比較。
     */
    EQUALS(
        "==", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.EQUALITY),

    /**
     * 非同一比較。
     */
    NOT_EQUALS(
        "!=", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.EQUALITY),

    /**
     * 超過。
     */
    GREATER(
        ">", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * 未満。
     */
    LESS(
        "<", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * 以上。
     */
    GREATER_EQUALS(
        ">=", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * 以下。
     */
    LESS_EQUALS(
        "<=", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.RELATIONAL),

    /**
     * 短絡的論理和。
     */
    CONDITIONAL_OR(
        "||", //$NON-NLS-1$
        EnumSet.of(Context.INFIX),
        Category.CONDITIONAL),

    /**
     * 短絡的論理積。
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
     * インスタンスを生成する。
     * @param symbol シンボル
     * @param permitted 許可されたコンテキストの一覧
     * @param category カテゴリ
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
     * この演算子のシンボルを返す。
     * @return この演算子のシンボル
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * この演算子の代入シンボルを返す。
     * @return 代入シンボル
     */
    public String getAssignmentSymbol() {
        if (this == ASSIGN) {
            return ASSIGN.getSymbol();
        } else {
            return getSymbol() + ASSIGN.getSymbol();
        }
    }

    /**
     * 指定のシンボルに対応するこの列挙の定数を返す。
     * @param symbol 対象のシンボル
     * @return 対応する定数、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static InfixOperator fromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        return SymbolToInfixOperator.get(symbol);
    }

    /**
     * 指定したコンテキストでこの演算子を利用することが許可されている場合のみ{@code true}を返す。
     * @param context コンテキストの種類
     * @return この演算子を利用することが許可されている場合のみ{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public boolean isPermitted(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return permittedContexts.contains(context);
    }

    /**
     * この演算子が属するカテゴリを返す。
     * @return カテゴリ
     */
    public InfixOperator.Category getCategory() {
        return this.category;
    }

    /**
     * 演算子を利用可能なコンテキストの種類。
     */
    public static enum Context {

        /**
         * 二項演算子として使用可能。
         */
        INFIX,

        /**
         * 単純代入および複合代入演算子として使用可能。
         */
        ASSIGNMENT,
    }

    /**
     * 演算子のカテゴリ。
     */
    public static enum Category {

        /**
         * 乗除演算子 ({@literal 15.17}).
         */
        MULTIPLICATIVE,

        /**
         * 加減演算子 ({@literal 15.18}).
         */
        ADDITIVE,

        /**
         * シフト演算子 ({@literal 15.19}).
         */
        SHIFT,

        /**
         * 関係演算子 ({@literal 15.20}).
         */
        RELATIONAL,

        /**
         * 等値演算子 ({@literal 15.21}).
         */
        EQUALITY,

        /**
         * ビット演算子, 論理演算子 ({@literal 15.22}).
         */
        BITWISE,

        /**
         * 条件演算子 ({@literal 15.23}, {@literal 15.24}).
         */
        CONDITIONAL,

        /**
         * 代入演算子 ({@literal 15.23}, {@literal 15.26}).
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
