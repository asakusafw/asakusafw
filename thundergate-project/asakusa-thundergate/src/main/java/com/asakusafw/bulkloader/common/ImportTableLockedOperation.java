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
 * ロックを取得できなかった時の動作を表すENUM。
 * @author yuta.shirai
 *
 */
public enum ImportTableLockedOperation {
    /**
     * ロックを取得できなかった時の動作-処理対象外とする。
     */
    OFF("1"),

    /**
     * ロックを取得できなかった時の動作-無視して処理する。
     */
    FORCE("2"),

    /**
     * ロックを取得できなかった時の動作-エラーとする。
     */
    ERROR("3")
    ;

    /**
     * ロックを取得できなかった時の動作。
     */
    private String lockedOperation;
    /**
     * ロックを取得できなかった時の動作を返す。
     * @return lockType ロックを取得できなかった時の動作
     */
    public String getLockedOperation() {
        return lockedOperation;
    }
    /**
     * コンストラクタ。
     * @param ope ロックを取得できなかった時の動作
     */
    private ImportTableLockedOperation(String ope) {
        this.lockedOperation = ope;
    }

    /**
     * {@code String}に対する定数を返す。
     * @param key String
     * @return 対応する定数、存在しない場合は{@code null}
     */
    public static ImportTableLockedOperation find(String key) {
        return LockedOperationToImportTableLockedOperation.REVERSE_DICTIONARY.get(key);
    }

    private static class LockedOperationToImportTableLockedOperation {
        static final Map<String, ImportTableLockedOperation> REVERSE_DICTIONARY;
        static {
            Map<String, ImportTableLockedOperation> map = new HashMap<String, ImportTableLockedOperation>();
            for (ImportTableLockedOperation elem : ImportTableLockedOperation
                    .values()) {
                map.put(elem.getLockedOperation(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
