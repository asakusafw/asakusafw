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
 * キャッシュ利用有無を表すENUM。
 * @author yuta.shirai
 * @deprecated no longer be used in ThunderGate
 */
@Deprecated
public enum CacheUseType {
    /**
     * キャッシュ利用有無-利用する。
     */
    USE("1"),

    /**
     * キャッシュ利用有無-利用しない。
     */
    NONE("2");

    /**
     * キャッシュ利用有無。
     */
    private String cacheUseType;
    /**
     * キャッシュ利用有無を返す。
     * @return lockType キャッシュ利用有無
     */
    public String getCacheUseType() {
        return cacheUseType;
    }
    /**
     * コンストラクタ。
     * @param type キャッシュ利用有無
     */
    private CacheUseType(String type) {
        this.cacheUseType = type;
    }
    /**
     * {@code String}に対する定数を返す。
     * @param key String
     * @return 対応する定数、存在しない場合は{@code null}
     */
    public static CacheUseType find(String key) {
        return CacheUseTypeToCacheUseType.REVERSE_DICTIONARY.get(key);
    }

    private static class CacheUseTypeToCacheUseType {
        static final Map<String, CacheUseType> REVERSE_DICTIONARY;
        static {
            Map<String, CacheUseType> map = new HashMap<String, CacheUseType>();
            for (CacheUseType elem : CacheUseType.values()) {
                map.put(elem.getCacheUseType(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }
    }
}
