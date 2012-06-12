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
package com.asakusafw.bulkloader.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TSVファイル削除有無を表すENUM。
 * @author yuta.shirai
 *
 */
public enum TsvDeleteType {
    /**
     * TSVファイル削除有無-削除しない。
     */
    FALSE("0"),

    /**
     * TSVファイル削除有無-削除する。
     */
    TRUE("1");

    /**
     * TSVファイル削除有無。
     */
    private String deleteType;
    /**
     * TSVファイル削除有無を返す。
     * @return lockType TSVファイル削除有無
     */
    public String getDeleteType() {
        return deleteType;
    }
    /**
     * コンストラクタ。
     * @param type TSVファイル削除有無
     */
    private TsvDeleteType(String type) {
        this.deleteType = type;
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
            Map<String, TsvDeleteType> map = new HashMap<String, TsvDeleteType>();
            for (TsvDeleteType elem : TsvDeleteType.values()) {
                map.put(elem.getDeleteType(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
