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
/**
 *
 */
package com.asakusafw.testtools;

import java.util.HashMap;
import java.util.Map;

/**
 * カラム毎に指定するテスト条件を表す列挙型。
 */
public enum ColumnMatchingCondition {

    /**
     * 検査対象外。
     */
    NONE("検査対象外"),

    /**
     * 完全一致。
     */
    EXACT("完全一致"),

    /**
     * 部分一致。
     */
    PARTIAL("部分一致"),

    /**
     * 現在時刻。
     */
    NOW("現在時刻"),

    /**
     * 現在日。
     */
    TODAY("現在日");

    /**
     * 日本語名。
     */
    private String japaneseName;

    private ColumnMatchingCondition(String japaneseName) {
        this.japaneseName = japaneseName;
    }

    /**
     * 日本語名を取得します。
     * @return 日本語名
     */
    public String getJapaneseName() {
        return japaneseName;
    }

    /**
     * 日本語名と条件のマップ。
     */
    private static Map<String, ColumnMatchingCondition> japaneseNameMap
        = new HashMap<String, ColumnMatchingCondition>();
    static {
        for (ColumnMatchingCondition conditon : ColumnMatchingCondition.values()) {
            String key = conditon.getJapaneseName();
            if (japaneseNameMap.containsKey(key)) {
                throw new RuntimeException("日本語名に重複があります");
            }
            japaneseNameMap.put(key, conditon);
        }
    }

    /**
     * 日本語名から条件を返す。
     * @param key 日本語名
     * @return 条件
     */
    public static ColumnMatchingCondition getConditonByJapanseName(String key) {
        return japaneseNameMap.get(key);
    }

    /**
     * 日本語名の配列を返す。
     * @return 日本語名の配列
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
