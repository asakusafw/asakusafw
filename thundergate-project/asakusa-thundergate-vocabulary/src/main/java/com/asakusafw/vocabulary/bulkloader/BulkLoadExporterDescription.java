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
package com.asakusafw.vocabulary.bulkloader;

import java.util.List;

import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * バルクローダー(インポート)の処理内容を記述するクラスの基底。
 * <p>
 * このクラスを継承するクラスは次のような要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> {@code public}で宣言されている </li>
 * <li> {@code abstract}で宣言されていない </li>
 * <li> 型引数が宣言されていない </li>
 * <li> 明示的なコンストラクターが宣言されていない </li>
 * </ul>
 */
public abstract class BulkLoadExporterDescription implements ExporterDescription {

    /**
     * エクスポーターの接続先データベースを表す識別子を返す。
     * <p>
     * 通常、これはデータベース名と同じ文字列である。
     * </p>
     * @return エクスポーターの接続先データベースを表す識別子
     */
    public abstract String getTargetName();

    /**
     * エクスポート対象のテーブルモデルを表すクラスを返す。
     * @return エクスポート対象のテーブルモデルを表すクラス
     */
    public abstract Class<?> getTableModelClass();

    /**
     * エクスポート対象のテーブル名を返す。
     * @return エクスポート対象のテーブル名
     */
    public abstract String getTableName();

    /**
     * エクスポート対象のカラム列を返す。
     * <p>
     * このカラム列は、バルクローダーに転送する際のデータ構造を特定するためのものである。
     * 実際にバルクローダーが利用するカラム名は{@link #getTargetColumnNames()}で指定する。
     * </p>
     * @return エクスポート対象のカラム列
     */
    public abstract List<String> getColumnNames();

    /**
     * 実際にエクスポートする対象のカラム列を返す。
     * <p>
     * このカラム列に含まれる項目は、{@link #getColumnNames()}が返す項目に
     * すべて含まれる必要がある。
     * ここに指定されなかった項目は、{@link #getColumnNames()}
     * で指定されたとしても実際には反映されない。
     * </p>
     * @return 正常時のみエクスポートする対象のカラム列
     */
    public abstract List<String> getTargetColumnNames();

    /**
     * 主キーとなるプロパティ名の一覧を返す。
     * @return 主キーとなるプロパティ名の一覧
     */
    public abstract List<String> getPrimaryKeyNames();

    /**
     * エクスポート時の重複レコードチェック情報を返す。
     * @return エクスポート時の重複レコードチェック情報、利用しない場合は{@code null}
     */
    public DuplicateRecordCheck getDuplicateRecordCheck() {
        return null;
    }

    /**
     * 重複レコードチェック。
     */
    public static class DuplicateRecordCheck {

        private final Class<?> tableModelClass;

        private final String tableName;

        private final List<String> columnNames;

        private final List<String> checkColumnNames;

        private final String errorCodeColumnName;

        private final String errorCodeValue;

        /**
         * インスタンスを生成する。
         * @param tableModelClass 重複チェックに失敗した際に出力先とするテーブルの構造を表すモデルクラス
         * @param tableName 重複チェックに失敗した際に出力先とするテーブル名
         * @param columnNames 重複チェックに失敗した際に出力するカラム一覧
         * @param checkColumnNames 重複チェックに利用するカラム名の一覧
         * @param errorCodeColumnName 重複チェックに失敗した際にエラーコードを出力するカラム名
         * @param errorCodeValue 重複チェックに失敗した際に設定されるエラーコード
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public DuplicateRecordCheck(
                Class<?> tableModelClass,
                String tableName,
                List<String> columnNames,
                List<String> checkColumnNames,
                String errorCodeColumnName,
                String errorCodeValue) {
            if (tableModelClass == null) {
                throw new IllegalArgumentException("tableModelClass must not be null"); //$NON-NLS-1$
            }
            if (tableName == null) {
                throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
            }
            if (columnNames == null) {
                throw new IllegalArgumentException("columnNames must not be null"); //$NON-NLS-1$
            }
            if (checkColumnNames == null) {
                throw new IllegalArgumentException("checkColumnNames must not be null"); //$NON-NLS-1$
            }
            if (errorCodeColumnName == null) {
                throw new IllegalArgumentException("errorCodeColumnName must not be null"); //$NON-NLS-1$
            }
            if (errorCodeValue == null) {
                throw new IllegalArgumentException("errorCodeValue must not be null"); //$NON-NLS-1$
            }
            this.tableModelClass = tableModelClass;
            this.tableName = tableName;
            this.columnNames = columnNames;
            this.checkColumnNames = checkColumnNames;
            this.errorCodeColumnName = errorCodeColumnName;
            this.errorCodeValue = errorCodeValue;
        }

        /**
         * 重複チェックに失敗した際に出力先とするテーブルの構造を表すモデルクラスを返す。
         * @return モデルクラス
         */
        public Class<?> getTableModelClass() {
            return tableModelClass;
        }

        /**
         * 重複チェックに失敗した際に出力先とするテーブル名を返す。
         * @return 重複チェックに失敗した際に出力先とするテーブル名
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * 重複チェックに失敗した際に出力するカラム一覧を返す。
         * <p>
         * このカラム列に含まれる項目は、{@link BulkLoadExporterDescription#getColumnNames()}が返す項目に
         * すべて含まれる必要がある。
         * </p>
         * @return 重複チェックに失敗した際に出力するカラム一覧
         */
        public List<String> getColumnNames() {
            return columnNames;
        }

        /**
         * 重複チェックに利用するカラム名の一覧を返す。
         * <p>
         * このカラム列に含まれる項目は、{@link BulkLoadExporterDescription#getColumnNames()}が返す項目に
         * すべて含まれる必要がある。
         * </p>
         * @return 重複チェックに利用するカラム名の一覧
         */
        public List<String> getCheckColumnNames() {
            return checkColumnNames;
        }

        /**
         * 重複チェックに失敗した際にエラーコードを出力するカラム名を返す。
         * <p>
         * このカラムに含まれる項目は、{@link #getColumnNames()}が返す項目に含まれる必要はない。
         * </p>
         * @return 重複チェックに失敗した際にエラーコードを出力するカラム名
         */
        public String getErrorCodeColumnName() {
            return errorCodeColumnName;
        }

        /**
         * 重複チェックに失敗した際に設定されるエラーコードを返す。
         * @return 重複チェックに失敗した際に設定されるエラーコード
         */
        public String getErrorCodeValue() {
            return errorCodeValue;
        }
    }
}
