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
import java.util.HashMap;
import java.util.Map;

/**
 * 後置演算子。
 */
public enum PostfixOperator {

    /**
     * 後置インクリメント。
     */
    INCREMENT("++"), //$NON-NLS-1$

    /**
     * 後置デクリメント。
     */
    DECREMENT("--"), //$NON-NLS-1$

    ;

    private final String symbol;

    /**
     * インスタンスを生成する。
     * @param symbol シンボル
     */
    private PostfixOperator(String symbol) {
        assert symbol != null;
        this.symbol = symbol;
    }

    /**
     * この演算子のシンボルを返す。
     * @return シンボル
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * 指定のシンボルに対応するこの列挙の定数を返す。
     * @param symbol 対象のシンボル
     * @return 対応する定数、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
            Map<String, PostfixOperator> map = new HashMap<String, PostfixOperator>();
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
