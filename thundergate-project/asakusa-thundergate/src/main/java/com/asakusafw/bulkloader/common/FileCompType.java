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
 * ファイル圧縮有無を表すENUM。
 * @author yuta.shirai
 * @since 0.1.0
 * @version 0.4.0
 */
public enum FileCompType {

    /**
     * ファイル圧縮有無-圧縮する。
     */
    DEFLATED("COMPRESS", "1", "DEFLATE"),

    /**
     * ファイル圧縮有無-圧縮しない。
     */
    STORED("NONE", "0", "STORE"),
    ;

    private String symbol;

    Set<String> keys;

    private FileCompType(String symbol, String... alternatives) {
        this.symbol = symbol;
        this.keys = new HashSet<String>();
        this.keys.add(symbol);
        Collections.addAll(this.keys, alternatives);
    }

    /**
     * ファイル圧縮有無を返す。
     * @return lockType ファイル圧縮有無
     */
    public String getSymbol() {
        return symbol;
    }
    /**
     * {@code String}に対する定数を返す。
     * @param key String
     * @return 対応する定数、存在しない場合は{@code null}
     */
    public static FileCompType find(String key) {
        return CompTypeToFileCompType.REVERSE_DICTIONARY.get(key);
    }

    private static class CompTypeToFileCompType {
        static final Map<String, FileCompType> REVERSE_DICTIONARY;
        static {
            Map<String, FileCompType> map = new TreeMap<String, FileCompType>(String.CASE_INSENSITIVE_ORDER);
            for (FileCompType elem : FileCompType.values()) {
                for (String key : elem.keys) {
                    map.put(key, elem);
                }
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
