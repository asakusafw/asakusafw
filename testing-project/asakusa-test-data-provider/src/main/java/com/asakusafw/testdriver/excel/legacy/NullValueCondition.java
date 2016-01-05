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
package com.asakusafw.testdriver.excel.legacy;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Kind of nullity predicate represented in each cell.
 */
public enum NullValueCondition {

    /**
     * Accepts like that {@code null} is a regular value.
     */
    NORMAL("通常比較"),

    /**
     * Accepts if actual value is {@code null}.
     */
    NULL_IS_OK("常にOK"),

    /**
     * Denies if actual value is {@code null}.
     */
    NULL_IS_NG("常にNG"),

    /**
     * Accepts if actual value is not {@code null}.
     */
    NOT_NULL_IS_OK("NULLでなければ常にOK"),

    /**
     * Denies if actual value is not {@code null}.
     */
    NOT_NULL_IS_NG("NULLでなければ常にNG");

    private String japaneseName;

    private NullValueCondition(String japaneseName) {
        this.japaneseName = japaneseName;
    }

    /**
     * Returns the Japanese name of this item.
     * @return the Japanese name
     */
    public String getJapaneseName() {
        return japaneseName;
    }

    private static final Map<String, NullValueCondition> JAPANESE_NAME_MAP;
    static {
        Map<String, NullValueCondition> map = new HashMap<>();
        for (NullValueCondition conditon : NullValueCondition.values()) {
            String key = conditon.getJapaneseName();
            if (map.containsKey(key)) {
                throw new RuntimeException(MessageFormat.format(
                        "duplicate Japanese name: {0}", //$NON-NLS-1$
                        key));
            }
            map.put(key, conditon);
        }
        JAPANESE_NAME_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Returns an item about the {@link #getJapaneseName() Japanese name}.
     * @param key the Japanese name
     * @return the related item, or {@code null} if there is no such the item
     */
    public static NullValueCondition getConditonByJapanseName(String key) {
        return JAPANESE_NAME_MAP.get(key);
    }

    /**
     * Returns the available Japanese names.
     * @return the available Japanese names
     */
    public static String[] getJapaneseNames() {
        NullValueCondition[] values = NullValueCondition.values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].getJapaneseName();
        }
        return result;
    }
}
