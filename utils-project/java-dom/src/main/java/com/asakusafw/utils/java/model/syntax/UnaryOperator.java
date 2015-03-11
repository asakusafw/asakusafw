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
import java.util.HashMap;
import java.util.Map;

/**
 * 単項演算子。
 */
public enum UnaryOperator {

    /**
     * 単項プラス。
     */
    PLUS("+", Category.SIGN), //$NON-NLS-1$

    /**
     * 単項マイナス。
     */
    MINUS("-", Category.SIGN), //$NON-NLS-1$

    /**
     * 単項ビット反転。
     */
    COMPLEMENT("~", Category.BITWISE), //$NON-NLS-1$

    /**
     * 単項論理反転。
     */
    NOT("!", Category.LOGICAL), //$NON-NLS-1$

    /**
     * 前置インクリメント。
     */
    INCREMENT("++", Category.INCREMENT_DECREMENT), //$NON-NLS-1$

    /**
     * 前置デクリメント。
     */
    DECREMENT("--", Category.INCREMENT_DECREMENT), //$NON-NLS-1$

    ;

    private final String symbol;

    private final Category category;

    /**
     * インスタンスを生成する。
     * @param symbol シンボル
     * @param category 演算子のカテゴリ
     */
    private UnaryOperator(String symbol, Category category) {
        assert symbol != null;
        assert category != null;
        this.symbol = symbol;
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
     * この演算子のカテゴリを返す。
     * @return この演算子のカテゴリ
     */
    public Category getCategory() {
        return category;
    }

    /**
     * 指定のシンボルに対応するこの列挙の定数を返す。
     * @param symbol 対象のシンボル
     * @return 対応する定数、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static UnaryOperator fromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        return SymbolToUnaryOperator.get(symbol);
    }

    /**
     * 演算子のカテゴリ。
     */
    public enum Category {

        /**
         * 前置インクリメント/デクリメント。
         */
        INCREMENT_DECREMENT,

        /**
         * 符号演算。
         */
        SIGN,

        /**
         * ビット演算。
         */
        BITWISE,

        /**
         * 論理演算。
         */
        LOGICAL,
    }

    private static class SymbolToUnaryOperator {

        private static final Map<String, UnaryOperator> REVERSE_DICTIONARY;
        static {
            Map<String, UnaryOperator> map = new HashMap<String, UnaryOperator>();
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
