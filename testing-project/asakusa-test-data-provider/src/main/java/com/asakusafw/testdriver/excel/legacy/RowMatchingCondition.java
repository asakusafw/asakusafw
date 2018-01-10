/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * Kind of predicate about all set of model object.
 */
public enum RowMatchingCondition {

    /**
     * Accepts if all expected/actual data are matched.
     */
    EXACT("完全一致"),

    /**
     * Accepts if all existing actual data matched.
     */
    PARTIAL("部分一致"),

    /**
     * Always accepts.
     */
    NONE("検査しない");

    private String japaneseName;

    RowMatchingCondition(String japaneseName) {
        this.japaneseName = japaneseName;
    }

    /**
     * Returns the Japanese name of this item.
     * @return the Japanese name
     */
    public String getJapaneseName() {
        return japaneseName;
    }

    private static final Map<String, RowMatchingCondition> JAPANESE_NAME_MAP;
    static {
        Map<String, RowMatchingCondition> map = new HashMap<>();
        for (RowMatchingCondition conditon : RowMatchingCondition.values()) {
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
    public static RowMatchingCondition getConditonByJapanseName(String key) {
        return JAPANESE_NAME_MAP.get(key);
    }

    /**
     * Returns the available Japanese names.
     * @return the available Japanese names
     */
    public static String[] getJapaneseNames() {
        RowMatchingCondition[] values = RowMatchingCondition.values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].getJapaneseName();
        }
        return result;
    }
}
