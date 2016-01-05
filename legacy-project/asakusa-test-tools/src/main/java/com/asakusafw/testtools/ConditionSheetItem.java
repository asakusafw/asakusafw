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
package com.asakusafw.testtools;

/**
 * テスト条件のシート上のアイテムを表す列挙型。
 */
public enum ConditionSheetItem {

    /**
     * カラム番号。
     */
    NO("NO", 2, 0, ItemType.COLUMN_ITEM),

    /**
     * カラム名。
     */
    COLUMN_NAME("カラム名", 2, 1, ItemType.COLUMN_ITEM),

    /**
     * カラムコメント。
     */
    COLUMN_COMMENT("カラムコメント", 2, 2, ItemType.COLUMN_ITEM),

    /**
     * データ型。
     */
    DATA_TYPE("データ型", 2, 3, ItemType.COLUMN_ITEM),

    /**
     * 桁数。
     */
    WIDTH("桁数", 2, 4, ItemType.COLUMN_ITEM),

    /**
     * 制度。
     */
    SCALE("精度", 2, 5, ItemType.COLUMN_ITEM),

    /**
     * キー項目。
     */
    KEY_FLAG("KEY", 2, 6, ItemType.COLUMN_ITEM),

    /**
     * NULL可。
     */
    NULLABLE("NULL可", 2, 7, ItemType.COLUMN_ITEM),

    /**
     * 比較条件。
     */
    MATCHING_CONDITION("比較条件", 2, 8, ItemType.COLUMN_ITEM),

    /**
     * NULL比較条件。
     */
    NULL_VALUE_CONDITION("NULL値", 2, 9, ItemType.COLUMN_ITEM),

    /**
     * テーブル名。
     */
    TABLE_NAME("テーブル名", 0, 1, ItemType.TABLE_ITEM),

    /**
     * 比較条件。
     */
    ROW_MATCHING_CONDITION("比較条件", 1, 1, ItemType.TABLE_ITEM);

    /**
     * セルに表示する名称。
     */
    private String name;

    /**
     * 行位置。
     */
    private int row;

    /**
     * カラム位置。
     */
    private int col;

    private ConditionSheetItem(String name, int row, int col, ItemType itemType) {
        this.name = name;
        this.row = row;
        this.col = col;
    }

    /**
     * アイテムの種別を表す列挙型。
     */
    public enum ItemType {

        /**
         * テーブル。
         */
        TABLE_ITEM,

        /**
         * カラム。
         */
        COLUMN_ITEM,
    }

    /**
     * セルに表示する名称を取得します。
     *
     * @return セルに表示する名称
     */
    public String getName() {
        return name;
    }

    /**
     * 行位置を取得します。
     *
     * @return 行位置
     */
    public int getRow() {
        return row;
    }

    /**
     * カラム位置を取得します。
     *
     * @return カラム位置
     */
    public int getCol() {
        return col;
    }
}
