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
package com.asakusafw.bulkloader.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * TSVファイル削除有無を表すENUM。
 * @author yuta.shirai
 * @since 0.1.0
 * @version 0.4.0
 */
public enum TsvDeleteType {
    /**
     * TSVファイル削除有無-削除しない。
     */
    FALSE("FALSE", "0", "KEEP"),

    /**
     * TSVファイル削除有無-削除する。
     */
    TRUE("TRUE", "1", "DELETE")
    ;

    private String symbol;

    Set<String> keys;

    private TsvDeleteType(String symbol, String... alternatives) {
        this.symbol = symbol;
        this.keys = new HashSet<String>();
        this.keys.add(symbol);
        Collections.addAll(this.keys, alternatives);
    }

    /**
     * TSVファイル削除有無を返す。
     * @return lockType TSVファイル削除有無
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * {@code String}に対する定数を返す。
     * @param key String
     * @return 対応する定数、存在しない場合は{@code null}
     */
    public static TsvDeleteType find(String key) {
        return DeleteTypeToTsvDeleteType.REVERSE_DICTIONARY.get(key);
    }

    private static class DeleteTypeToTsvDeleteType {
        static final Map<String, TsvDeleteType> REVERSE_DICTIONARY;
        static {
            Map<String, TsvDeleteType> map = new TreeMap<String, TsvDeleteType>(String.CASE_INSENSITIVE_ORDER);
            for (TsvDeleteType elem : TsvDeleteType.values()) {
                for (String key : elem.keys) {
                    map.put(key, elem);
                }
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
