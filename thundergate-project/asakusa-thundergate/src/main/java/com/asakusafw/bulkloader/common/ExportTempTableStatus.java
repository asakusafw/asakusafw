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
 * エクスポートテンポラリテーブルステータスを表すENUM。
 * @author yuta.shirai
 *
 */
public enum ExportTempTableStatus {
    /**
     * エクスポートテンポラリテーブルステータス TSVファイルをロード完了。
     */
    LOAD_EXIT("1"),

    /**
     * エクスポートテンポラリテーブルステータス Export対象テーブルにデータをコピー開始前。
     */
    BEFORE_COPY("2"),

    /**
     * エクスポートテンポラリテーブルステータス Export対象テーブルにデータをコピー完了。
     */
    COPY_EXIT("3");

    /**
     * エクスポートテンポラリテーブルステータス。
     */
    private String status;
    /**
     * エクスポートテンポラリテーブルステータスを返す。
     * @return lockType エクスポートテンポラリテーブルステータス
     */
    public String getStatus() {
        return status;
    }
    /**
     * コンストラクタ。
     * @param status エクスポートテンポラリテーブルステータス
     */
    private ExportTempTableStatus(String status) {
        this.status = status;
    }
    /**
     * {@code String}に対する定数を返す。
     * @param key String
     * @return 対応する定数、存在しない場合は{@code null}
     */
    public static ExportTempTableStatus find(String key) {
        return StatusToExportTempTableStatus.REVERSE_DICTIONARY.get(key);
    }

    private static class StatusToExportTempTableStatus {
        static final Map<String, ExportTempTableStatus> REVERSE_DICTIONARY;
        static {
            Map<String, ExportTempTableStatus> map = new HashMap<String, ExportTempTableStatus>();
            for (ExportTempTableStatus elem : ExportTempTableStatus.values()) {
                map.put(elem.getStatus(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
