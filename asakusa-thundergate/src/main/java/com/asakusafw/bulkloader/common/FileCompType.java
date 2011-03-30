/**
 * Copyright 2011 Asakusa Framework Team.
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
 * ファイル圧縮有無を表すENUM。
 * @author yuta.shirai
 *
 */
public enum FileCompType {
    /**
     * ファイル圧縮有無-圧縮する。
     */
    DEFLATED("1"),

    /**
     * ファイル圧縮有無-圧縮しない。
     */
    STORED("0");

    /**
     * ファイル圧縮有無。
     */
    private String compType;
    /**
     * ファイル圧縮有無を返す。
     * @return lockType ファイル圧縮有無
     */
    public String getCompType() {
        return compType;
    }
    /**
     * コンストラクタ。
     * @param type ファイル圧縮有無
     */
    private FileCompType(String type) {
        this.compType = type;
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
            Map<String, FileCompType> map = new HashMap<String, FileCompType>();
            for (FileCompType elem : FileCompType.values()) {
                map.put(elem.getCompType(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
