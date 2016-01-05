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

import com.asakusafw.modelgen.emitter.JavaName;
import com.asakusafw.modelgen.source.MySqlDataType;

/**
 * データベースのカラム情報。
 */
public class ColumnInfo {

// CHECKSTYLE:OFF ParameterNumberCheck
    /**
     * インスタンスを生成する。
     * @param tableName テーブル名
     * @param columnName カラム名
     * @param columnComment カラムコメント
     * @param dataType データ型
     * @param characterMaximumLength 文字列の最大長、文字列以外では無視
     * @param numericPrecision 10進数の精度、10進数以外では無視
     * @param numericScale 10進数のスケール、10進数以外では無視
     * @param nullable {@code true}ならばNULLを許可する
     * @param key {@code true}ならば比較の同一カラム判定キー項目に利用する
     * @param columnMatchingCondition カラム値の比較条件
     * @param nullValueCondition NULLの比較条件
     */
    public ColumnInfo(String tableName, String columnName,
            String columnComment, MySqlDataType dataType,
            long characterMaximumLength, int numericPrecision,
            int numericScale, boolean nullable, boolean key,
            ColumnMatchingCondition columnMatchingCondition,
            NullValueCondition nullValueCondition) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnComment = columnComment;
        this.dataType = dataType;
        this.characterMaximumLength = characterMaximumLength;
        this.numericPrecision = numericPrecision;
        this.numericScale = numericScale;
        this.nullable = nullable;
        this.key = key;
        this.columnMatchingCondition = columnMatchingCondition;
        this.nullValueCondition = nullValueCondition;

        // FIXME modelgenの方にロジックを持たせる
        JavaName javaName = JavaName.of(columnName);
        getterName = "get" + javaName.toTypeName() + "Option";
        setterName = "set" + javaName.toTypeName() + "Option";
    }
// CHECKSTYLE:ON ParameterNumberCheck

    /**
     * テーブル名。
     */
    private final String tableName;

    /**
     * カラム名。
     */
    private final String columnName;

    /**
     * カラムコメント。
     */
    private final String columnComment;

    /**
     * カラムのデータ型。
     */
    private final MySqlDataType dataType;

    /**
     * 文字列型カラムの最大長。
     */
    private final long characterMaximumLength;

    /**
     * Decimal型カラムの桁数。
     */
    private final int numericPrecision;

    /**
     * Decimal型カラムの精度。
     */
    private final int numericScale;

    /**
     * NULL可フラグ。
     */
    private final boolean nullable;

    /**
     * Key項目フラグ。
     */
    private final boolean key;

    /**
     * テスト条件。
     */
    private final ColumnMatchingCondition columnMatchingCondition;


    /**
     * NULL値の扱い。
     */
    private final NullValueCondition nullValueCondition;

    /**
     * JavaBeansのgetter名。
     */
    private final String getterName;

    /**
     * JavaBeansのsetter名。
     */
    private final String setterName;


    /**
     * テーブル名を取得します。
     * @return テーブル名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * カラム名を取得します。
     * @return カラム名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * カラムコメントを取得します。
     * @return カラムコメント
     */
    public String getColumnComment() {
        return columnComment;
    }

    /**
     * カラムのデータ型を取得します。
     * @return カラムのデータ型
     */
    public MySqlDataType getDataType() {
        return dataType;
    }

    /**
     * 文字列型カラムの最大長を取得します。
     * @return 文字列型カラムの最大長
     */
    public long getCharacterMaximumLength() {
        return characterMaximumLength;
    }

    /**
     * Decimal型カラムの桁数を取得します。
     * @return Decimal型カラムの桁数
     */
    public int getNumericPrecision() {
        return numericPrecision;
    }

    /**
     * Decimal型カラムの精度を取得します。
     * @return Decimal型カラムの精度
     */
    public int getNumericScale() {
        return numericScale;
    }

    /**
     * NULL可フラグを取得します。
     * @return NULL可フラグ
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * Key項目フラグを取得します。
     * @return Key項目フラグ
     */
    public boolean isKey() {
        return key;
    }

    /**
     * テスト条件を取得します。
     * @return テスト条件
     */
    public ColumnMatchingCondition getColumnMatchingCondition() {
        return columnMatchingCondition;
    }

    /**
     * NULL値の扱いを取得します。
     * @return NULL値の扱い
     */
    public NullValueCondition getNullValueCondition() {
        return nullValueCondition;
    }

    /**
     * JavaBeansのgetter名を取得します。
     * @return JavaBeansのgetter名
     */
    public String getGetterName() {
        return getterName;
    }

    /**
     * JavaBeansのsetter名を取得します。
     * @return JavaBeansのsetter名
     */
    public String getSetterName() {
        return setterName;
    }
}
