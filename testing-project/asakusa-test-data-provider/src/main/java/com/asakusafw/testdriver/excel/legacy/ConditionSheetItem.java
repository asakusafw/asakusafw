/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

/**
 * Represents a kind of cell on the Excel rule sheets.
 */
public enum ConditionSheetItem {

    /**
     * The column number.
     */
    NO("NO", 2, 0, ItemType.COLUMN_ITEM),

    /**
     * The column name.
     */
    COLUMN_NAME("カラム名", 2, 1, ItemType.COLUMN_ITEM),

    /**
     * The column comments.
     */
    COLUMN_COMMENT("カラムコメント", 2, 2, ItemType.COLUMN_ITEM),

    /**
     * The data type.
     */
    DATA_TYPE("データ型", 2, 3, ItemType.COLUMN_ITEM),

    /**
     * The number of digits.
     */
    WIDTH("桁数", 2, 4, ItemType.COLUMN_ITEM),

    /**
     * The degree of precision.
     */
    SCALE("精度", 2, 5, ItemType.COLUMN_ITEM),

    /**
     * The key item.
     */
    KEY_FLAG("KEY", 2, 6, ItemType.COLUMN_ITEM),

    /**
     * The nullability.
     */
    NULLABLE("NULL可", 2, 7, ItemType.COLUMN_ITEM),

    /**
     * The matching condition.
     */
    MATCHING_CONDITION("比較条件", 2, 8, ItemType.COLUMN_ITEM),

    /**
     * The nullity condition.
     */
    NULL_VALUE_CONDITION("NULL値", 2, 9, ItemType.COLUMN_ITEM),

    /**
     * The table name.
     */
    TABLE_NAME("テーブル名", 0, 1, ItemType.TABLE_ITEM),

    /**
     * The row matching condition.
     */
    ROW_MATCHING_CONDITION("比較条件", 1, 1, ItemType.TABLE_ITEM);

    private String name;

    private int row;

    private int col;

    private ConditionSheetItem(String name, int row, int col, ItemType itemType) {
        assert name != null;
        assert itemType != null;
        this.name = name;
        this.row = row;
        this.col = col;
    }

    /**
     * Represents a kind of item.
     */
    public enum ItemType {

        /**
         * The table item.
         */
        TABLE_ITEM,

        /**
         * The column item.
         */
        COLUMN_ITEM,
    }

    /**
     * Returns the cell name.
     * @return the cell name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the row number.
     * @return the row number (0-origin)
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column number.
     * @return the column number (0-origin)
     */
    public int getCol() {
        return col;
    }
}
