/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
/**
 *
 */
package com.asakusafw.testdriver.excel.legacy;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a kind of column verification rule.
 */
public enum ColumnMatchingCondition {

    /**
     * Accepts anything.
     */
    NONE("検査対象外"),

    /**
     * Accepts equivalent values.
     */
    EXACT("完全一致"),

    /**
     * Accepts if expected data appears in the actual data.
     */
    PARTIAL("部分一致"),

    /**
     * Accepts if actual date/time is between the test started time and its finished time.
     */
    NOW("現在時刻"),

    /**
     * Accepts if actual date/time is between the test started date and its finished date.
     */
    TODAY("現在日");

    private String japaneseName;

    ColumnMatchingCondition(String japaneseName) {
        this.japaneseName = japaneseName;
    }

    /**
     * Returns the Japanese name of this item.
     * @return the Japanese name
     */
    public String getJapaneseName() {
        return japaneseName;
    }

    private static final Map<String, ColumnMatchingCondition> JAPANESE_NAME_MAP;
    static {
        Map<String, ColumnMatchingCondition> map = new HashMap<>();
        for (ColumnMatchingCondition conditon : ColumnMatchingCondition.values()) {
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
    public static ColumnMatchingCondition getConditonByJapanseName(String key) {
        return JAPANESE_NAME_MAP.get(key);
    }

    /**
     * Returns the available Japanese names.
     * @return the available Japanese names
     */
    public static String[] getJapaneseNames() {
        ColumnMatchingCondition[] values = ColumnMatchingCondition.values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].getJapaneseName();
        }
       return result;
    }
}
