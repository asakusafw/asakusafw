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
 * Importer処理区分を表すENUM。
 * @author yuta.shirai
 *
 */
public enum ImportType {
    /**
     * Importer処理区分(primary：通常起動)。
     */
    PRIMARY("primary"),

    /**
     * Importer処理区分(secondary：サブ起動)。
     */
    SECONDARY("secondary")
    ;

    /**
     * Importer処理区分。
     */
    private String importType;
    /**
     * Importer処理区分を返す。
     * @return importType Importer処理区分
     */
    public String getImportType() {
        return importType;
    }
    /**
     * コンストラクタ。
     * @param type Importer処理区分
     */
    private ImportType(String type) {
        this.importType = type;
    }
    /**
     * {@code String}に対する定数を返す。
     * @param key String
     * @return 対応する定数、存在しない場合は{@code null}
     */
    public static ImportType find(String key) {
        return ImportTypeToImportType.REVERSE_DICTIONARY.get(key);
    }

    @Override
    public String toString() {
        return importType;
    }

    private static class ImportTypeToImportType {
        static final Map<String, ImportType> REVERSE_DICTIONARY;
        static {
            Map<String, ImportType> map = new HashMap<String, ImportType>();
            for (ImportType elem : ImportType.values()) {
                map.put(elem.getImportType(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
