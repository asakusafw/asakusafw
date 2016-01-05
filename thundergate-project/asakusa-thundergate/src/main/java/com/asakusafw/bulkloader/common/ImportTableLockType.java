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
import java.util.HashMap;
import java.util.Map;

/**
 * ロック取得タイプを表すENUM。
 * @author yuta.shirai
 *
 */
public enum ImportTableLockType {
    /**
     * ロック取得タイプ-テーブルロックを取得。
     */
    TABLE("1"),

    /**
     * ロック取得タイプ-行ロックを取得。
     */
    RECORD("2"),

    /**
     * ロック取得タイプ-ロックを取得しない。
     */
    NONE("3")
    ;

    /**
     * ロック取得タイプ。
     */
    private String lockType;
    /**
     * ロック取得タイプを返す。
     * @return lockType ロック取得タイプ
     */
    public String getLockType() {
        return lockType;
    }
    /**
     * コンストラクタ。
     * @param type ロック取得タイプ
     */
    private ImportTableLockType(String type) {
        this.lockType = type;
    }
    /**
     * {@code String}に対する定数を返す。
     * @param key String
     * @return 対応する定数、存在しない場合は{@code null}
     */
    public static ImportTableLockType find(String key) {
        return LockTypeToImportTableLockType.REVERSE_DICTIONARY.get(key);
    }

    private static class LockTypeToImportTableLockType {
        static final Map<String, ImportTableLockType> REVERSE_DICTIONARY;
        static {
            Map<String, ImportTableLockType> map = new HashMap<String, ImportTableLockType>();
            for (ImportTableLockType elem : ImportTableLockType.values()) {
                map.put(elem.getLockType(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
