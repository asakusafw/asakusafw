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
package com.asakusafw.compiler.bulkloader;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.asakusafw.compiler.flow.Location;
import com.asakusafw.utils.collections.Lists;


/**
 * バルクローダーの処理に関する情報。
 */
public class BulkLoaderScript {

    private final List<ImportTable> importTargetTables;

    private final List<ExportTable> exportTargetTables;

    /**
     * インスタンスを生成する。
     * @param importTargetTables インポート対象の情報一覧
     * @param exportTargetTables エクスポート対象の情報一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public BulkLoaderScript(
            List<ImportTable> importTargetTables,
            List<ExportTable> exportTargetTables) {
        if (importTargetTables == null) {
            throw new IllegalArgumentException("importTargetTables must not be null"); //$NON-NLS-1$
        }
        if (exportTargetTables == null) {
            throw new IllegalArgumentException("exportTargetTables must not be null"); //$NON-NLS-1$
        }
        this.importTargetTables = importTargetTables;
        this.exportTargetTables = exportTargetTables;
    }

    /**
     * インポーターの設定情報を返す。
     * @return インポーターの設定情報
     */
    public List<ImportTable> getImportTargetTables() {
        return importTargetTables;
    }

    /**
     * エクスポーターの設定情報を返す。
     * @return エクスポーターの設定情報
     */
    public List<ExportTable> getExportTargetTables() {
        return exportTargetTables;
    }

    /**
     * インポーターの設定情報を返す。
     * @return インポーターの設定情報
     */
    public Properties getImporterProperties() {
        return ImportTable.toProperties(importTargetTables);
    }

    /**
     * エクスポーターの設定情報を返す。
     * @return エクスポーターの設定情報
     */
    public Properties getExporterProperties() {
        return ExportTable.toProperties(exportTargetTables);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + exportTargetTables.hashCode();
        result = prime * result + importTargetTables.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BulkLoaderScript other = (BulkLoaderScript) obj;
        if (exportTargetTables.equals(other.exportTargetTables) == false) {
            return false;
        }
        if (importTargetTables.equals(other.importTargetTables) == false) {
            return false;
        }
        return true;
    }

    static List<String> toNames(List<? extends Table> tables) {
        assert tables != null;
        List<String> results = Lists.create();
        for (Table table : tables) {
            results.add(table.getName());
        }
        return results;
    }

    static String toPath(Location location) {
        assert location != null;
        return location.toPath('/');
    }

    static Location fromPath(String path) {
        assert path != null;
        return Location.fromPath(path, '/');
    }

    static List<String> toPaths(List<Location> locations) {
        assert locations != null;
        List<String> results = Lists.create();
        for (Location location : locations) {
            results.add(toPath(location));
        }
        return results;
    }

    static List<Location> fromPaths(List<String> paths) {
        assert paths != null;
        List<Location> results = Lists.create();
        for (String path : paths) {
            results.add(fromPath(path));
        }
        return results;
    }

    static String join(List<String> fields) {
        assert fields != null;
        if (fields.isEmpty()) {
            return "";
        }
        if (fields.size() == 1) {
            return fields.get(0);
        }
        StringBuilder buf = new StringBuilder();
        Iterator<String> iter = fields.iterator();
        assert iter.hasNext();
        buf.append(iter.next());
        while (iter.hasNext()) {
            buf.append(',');
            buf.append(iter.next());
        }
        return buf.toString();
    }

    static List<String> split(String fields) {
        assert fields != null;
        if (fields.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> results = Lists.create();
        int start = 0;
        while (true) {
            int end = fields.indexOf(',', start);
            if (end < 0) {
                break;
            }
            results.add(fields.substring(start, end));
            start = end + 1;
        }
        results.add(fields.substring(start));
        return results;
    }

    static String get(Properties properties, String keyName, boolean mandatory) {
        assert properties != null;
        assert keyName != null;
        String value = properties.getProperty(keyName);
        if (value == null && mandatory) {
            throw new IllegalArgumentException(keyName);
        }
        return value;
    }

    /**
     * インポートまたはエクスポート対象のテーブル情報。
     */
    public abstract static class Table {

        private final Class<?> modelClass;

        private final String name;

        private final List<String> targetColumns;

        /**
         * インスタンスを生成する。
         * @param modelClass テーブルと対応づけられたモデルクラス
         * @param name 対象テーブルの名前
         * @param targetColumns 処理対象のカラム一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        protected Table(
                Class<?> modelClass,
                String name,
                List<String> targetColumns) {
            if (modelClass == null) {
                throw new IllegalArgumentException(
                        "modelClass must not be null"); //$NON-NLS-1$
            }
            if (name == null) {
                throw new IllegalArgumentException(
                        "name must not be null"); //$NON-NLS-1$
            }
            if (targetColumns == null) {
                throw new IllegalArgumentException(
                        "targetColumns must not be null"); //$NON-NLS-1$
            }
            this.modelClass = modelClass;
            this.name = name;
            this.targetColumns = targetColumns;
        }

        /**
         * このテーブルの名前を返す。
         * @return このテーブルの名前
         */
        public String getName() {
            return name;
        }

        /**
         * このテーブルに対応するモデルクラスを返す。
         * @return このテーブルに対応するモデルクラス
         */
        public Class<?> getModelClass() {
            return modelClass;
        }

        /**
         * 処理対象とするカラム名の一覧を返す。
         * @return 処理対象とするカラム名の一覧
         */
        public List<String> getTsvColumns() {
            return targetColumns;
        }

        /**
         * このオブジェクトに対するプロパティ一覧を返す。
         * @return このオブジェクトに対するプロパティ一覧
         */
        public abstract Properties toProperties();
    }

    /**
     * インポート対象のテーブル情報。
     */
    public static class ImportTable extends Table {

        private static final String K_TARGET_TABLES = "import.target-table";

        private static final String P_TSV_COLUMNS = ".target-column";

        private static final String P_SEARCH_CONDITION = ".search-condition";

        private static final String P_CACHE_ID = ".cache-id";

        private static final String P_LOCK_TYPE = ".lock-type";

        private static final String P_LOCKED_OPERATION = ".locked-operation";

        private static final String P_BEAN_NAME = ".bean-name";

        private static final String P_DESTINATION = ".hdfs-import-file";

        private final String searchConditionOrNull;

        private final String cacheId;

        private final LockType lockType;

        private final LockedOperation lockedOperation;

        private final Location destination;

        /**
         * インスタンスを生成する。
         * @param modelClass テーブルと対応づけられたモデルクラス
         * @param name 対象テーブルの名前
         * @param targetColumns 処理対象のカラム一覧
         * @param searchConditionOrNull 検索条件、利用しない場合は{@code null}
         * @param cacheId キャッシュID (利用しない場合は{@code null})
         * @param lockType ロックの種類
         * @param lockedOperation 処理対象がロックされていた際の動作
         * @param destination 出力先の位置
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ImportTable(
                Class<?> modelClass,
                String name,
                List<String> targetColumns,
                String searchConditionOrNull,
                String cacheId,
                LockType lockType,
                LockedOperation lockedOperation,
                Location destination) {
            super(modelClass, name, targetColumns);
            if (lockType == null) {
                throw new IllegalArgumentException(
                        "lockType must not be null"); //$NON-NLS-1$
            }
            if (lockedOperation == null) {
                throw new IllegalArgumentException(
                        "lockedOperation must not be null"); //$NON-NLS-1$
            }
            if (destination == null) {
                throw new IllegalArgumentException(
                        "destination must not be null"); //$NON-NLS-1$
            }
            this.searchConditionOrNull = searchConditionOrNull;
            this.cacheId = cacheId;
            this.lockType = lockType;
            this.lockedOperation = lockedOperation;
            this.destination = destination;
        }

        /**
         * 出力先の位置を返す。
         * @return 出力先の位置
         */
        public Location getDestination() {
            return destination;
        }

        static Properties toProperties(List<ImportTable> list) {
            assert list != null;
            Properties properties = new Properties();
            properties.setProperty(K_TARGET_TABLES, join(toNames(list)));
            for (ImportTable table : list) {
                properties.putAll(table.toProperties());
            }
            return properties;
        }

        @Override
        public Properties toProperties() {
            String prefix = getName();
            Properties p = new Properties();
            p.setProperty(prefix + P_TSV_COLUMNS, join(getTsvColumns()));
            if (searchConditionOrNull != null) {
                p.setProperty(prefix + P_SEARCH_CONDITION, searchConditionOrNull);
            }
            if (cacheId != null) {
                p.setProperty(prefix + P_CACHE_ID, String.valueOf(cacheId));
            }
            p.setProperty(prefix + P_LOCK_TYPE, String.valueOf(lockType.id));
            p.setProperty(prefix + P_LOCKED_OPERATION, String.valueOf(lockedOperation.id));
            p.setProperty(prefix + P_BEAN_NAME, String.valueOf(getModelClass().getName()));
            p.setProperty(prefix + P_DESTINATION, String.valueOf(toPath(destination)));
            return p;
        }

        /**
         * 指定のプロパティに対するこのクラスのインスタンス一覧を返す。
         * @param properties 対象のプロパティ
         * @param loaderOrNull モデルをロードする際のクラスローダー
         * @return 対応するインスタンス一覧
         * @throws IllegalArgumentException プロパティを解析できなかった場合
         */
        public static List<ImportTable> fromProperties(
                Properties properties,
                ClassLoader loaderOrNull) {
            if (properties == null) {
                throw new IllegalArgumentException(
                        "properties must not be null"); //$NON-NLS-1$
            }
            ClassLoader loader = (loaderOrNull == null)
                    ? BulkLoaderScript.class.getClassLoader()
                    : loaderOrNull;
            List<ImportTable> results = Lists.create();
            for (String prefix : split(get(properties, K_TARGET_TABLES, true))) {
                results.add(fromProperties(properties, prefix, loader));
            }
            return results;
        }

        private static ImportTable fromProperties(
                Properties p,
                String name,
                ClassLoader loader) {
            assert p != null;
            assert name != null;
            assert loader != null;
            Class<?> modelClass;
            try {
                modelClass = Class.forName(
                        get(p, name + P_BEAN_NAME, true),
                        false,
                        loader);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
            List<String> targetColumns = split(get(p, name + P_TSV_COLUMNS, true));
            String searchConditionOrNull = get(p, name + P_SEARCH_CONDITION, false);
            String cacheId = get(p, name + P_CACHE_ID, false);
            LockType lockType = LockType.idOf(get(p, name + P_LOCK_TYPE, true));
            LockedOperation lockedOperation = LockedOperation.idOf(get(p, name + P_LOCKED_OPERATION, true));
            Location destination = fromPath(get(p, name + P_DESTINATION, true));
            return new ImportTable(
                    modelClass,
                    name,
                    targetColumns,
                    searchConditionOrNull,
                    cacheId,
                    lockType,
                    lockedOperation,
                    destination);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getModelClass().hashCode();
            result = prime * result + getName().hashCode();
            result = prime * result + getTsvColumns().hashCode();
            result = prime * result + destination.hashCode();
            result = prime * result + lockType.hashCode();
            result = prime * result + lockedOperation.hashCode();
            result = prime * result + ((searchConditionOrNull == null) ? 0 : searchConditionOrNull.hashCode());
            result = prime * result + ((cacheId == null) ? 0 : cacheId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ImportTable other = (ImportTable) obj;
            if (getModelClass() != other.getModelClass()) {
                return false;
            }
            if (getName().equals(other.getName()) == false) {
                return false;
            }
            if (getTsvColumns().equals(other.getTsvColumns()) == false) {
                return false;
            }
            if (destination.equals(other.destination) == false) {
                return false;
            }
            if (lockType != other.lockType) {
                return false;
            }
            if (lockedOperation != other.lockedOperation) {
                return false;
            }
            if (searchConditionOrNull == null) {
                if (other.searchConditionOrNull != null) {
                    return false;
                }
            } else if (searchConditionOrNull.equals(other.searchConditionOrNull) == false) {
                return false;
            }
            if (cacheId == null) {
                if (other.cacheId != null) {
                    return false;
                }
            } else if (cacheId.equals(other.cacheId) == false) {
                return false;
            }
            return true;
        }
    }

    /**
     * エクスポート対象のテーブル情報。
     */
    public static class ExportTable extends Table {

        private static final String K_TARGET_TABLES = "export.target-table";

        private static final String P_BEAN_NAME = ".bean-name";

        private static final String P_TSV_COLUMNS = ".tsv-column";

        private static final String P_TARGET_COLUMNS = ".export-table-column";

        private static final String P_SOURCES = ".hdfs-export-file";

        private final List<String> exportColumns;

        private final List<Location> sources;

        private final DuplicateRecordErrorTable duplicateRecordError;

        /**
         * インスタンスを生成する。
         * @param modelClass テーブルと対応づけられたモデルクラス
         * @param name 対象テーブルの名前
         * @param tsvColumns 処理対象のカラム一覧
         * @param exportColumns 正常エクスポート時に利用するカラム一覧
         * @param duplicateRecordError ユニーク制約のエラー、不要の場合は{@code null}
         * @param sources 読み出し元のディレクトリ一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ExportTable(
                Class<?> modelClass,
                String name,
                List<String> tsvColumns,
                List<String> exportColumns,
                DuplicateRecordErrorTable duplicateRecordError,
                List<Location> sources) {
            super(modelClass, name, tsvColumns);
            this.sources = sources;
            this.exportColumns = exportColumns;
            this.duplicateRecordError = duplicateRecordError;
        }

        /**
         * 読み出し元のディレクトリ一覧を返す。
         * @return 読み出し元のディレクトリ一覧
         */
        public List<Location> getSources() {
            return sources;
        }

        static Properties toProperties(List<ExportTable> list) {
            assert list != null;
            Properties properties = new Properties();
            properties.setProperty(K_TARGET_TABLES, join(toNames(list)));
            for (ExportTable table : list) {
                properties.putAll(table.toProperties());
            }
            return properties;
        }

        @Override
        public Properties toProperties() {
            String prefix = getName();
            Properties p = new Properties();
            p.setProperty(prefix + P_TSV_COLUMNS, join(getTsvColumns()));
            p.setProperty(prefix + P_TARGET_COLUMNS, join(exportColumns));
            p.setProperty(prefix + P_BEAN_NAME, String.valueOf(getModelClass().getName()));
            p.setProperty(prefix + P_SOURCES, String.valueOf(join(toPaths(sources))));
            if (duplicateRecordError != null) {
                Properties sub = duplicateRecordError.toProperties(prefix);
                p.putAll(sub);
            }
            return p;
        }

        /**
         * 指定のプロパティに対するこのクラスのインスタンス一覧を返す。
         * @param properties 対象のプロパティ
         * @param loaderOrNull モデルをロードする際のクラスローダー
         * @return 対応するインスタンス一覧
         * @throws IllegalArgumentException プロパティを解析できなかった場合
         */
        public static List<ExportTable> fromProperties(
                Properties properties,
                ClassLoader loaderOrNull) {
            if (properties == null) {
                throw new IllegalArgumentException(
                        "properties must not be null"); //$NON-NLS-1$
            }
            ClassLoader loader = (loaderOrNull == null)
                    ? BulkLoaderScript.class.getClassLoader()
                    : loaderOrNull;
            List<ExportTable> results = Lists.create();
            for (String prefix : split(get(properties, K_TARGET_TABLES, true))) {
                results.add(fromProperties(properties, prefix, loader));
            }
            return results;
        }

        private static ExportTable fromProperties(
                Properties p,
                String name,
                ClassLoader loader) {
            assert p != null;
            assert name != null;
            assert loader != null;
            Class<?> modelClass;
            try {
                modelClass = Class.forName(
                        get(p, name + P_BEAN_NAME, true),
                        false,
                        loader);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
            List<String> tsvColumns = split(get(p, name + P_TSV_COLUMNS, true));
            List<String> exportColumns = split(get(p, name + P_TARGET_COLUMNS, true));
            List<Location> sources = fromPaths(split(get(p, name + P_SOURCES, true)));
            DuplicateRecordErrorTable ucError = DuplicateRecordErrorTable.fromProperties(p, name);
            return new ExportTable(
                    modelClass,
                    name,
                    tsvColumns,
                    exportColumns,
                    ucError,
                    sources);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getModelClass().hashCode();
            result = prime * result + getName().hashCode();
            result = prime * result + getTsvColumns().hashCode();
            result = prime * result + exportColumns.hashCode();
            result = prime * result
                    + (duplicateRecordError == null ? 0 : duplicateRecordError.hashCode());
            result = prime * result + sources.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ExportTable other = (ExportTable) obj;
            if (getModelClass() != other.getModelClass()) {
                return false;
            }
            if (getName().equals(other.getName()) == false) {
                return false;
            }
            if (getTsvColumns().equals(other.getTsvColumns()) == false) {
                return false;
            }
            if (exportColumns.equals(other.exportColumns) == false) {
                return false;
            }
            if (duplicateRecordError == null) {
                if (other.duplicateRecordError != null) {
                    return false;
                }
            } else if (duplicateRecordError.equals(other.duplicateRecordError) == false) {
                return false;
            }
            if (sources.equals(other.sources) == false) {
                return false;
            }
            return true;
        }
    }

    /**
     * エクスポート時の重複レコードエラーに関する情報。
     */
    public static class DuplicateRecordErrorTable {

        private static final String P_TABLE_NAME = ".error-table";

        private static final String P_TARGET_COLUMNS = ".error-table-column";

        private static final String P_KEY_COLUMNS = ".key-column";

        private static final String P_ERROR_CODE_COLUMN = ".error-column";

        private static final String P_ERROR_CODE_VALUE = ".error-code";

        private final List<String> targetColumns;

        private final List<String> keyColumns;

        private final String errorCodeColumn;

        private final String errorCodeValue;

        private final String tableName;

        /**
         * インスタンスを生成する。
         * @param tableName エラー情報のテーブル名
         * @param targetColumns エラー情報のカラム一覧
         * @param keyColumns ユニーク制約に利用するカラムの一覧
         * @param errorCodeColumn エラーコードのカラム名
         * @param errorCodeValue エラーコードの値
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public DuplicateRecordErrorTable(
                String tableName,
                List<String> targetColumns,
                List<String> keyColumns,
                String errorCodeColumn,
                String errorCodeValue) {
            if (tableName == null) {
                throw new IllegalArgumentException(
                        "tableName must not be null"); //$NON-NLS-1$
            }
            if (targetColumns == null) {
                throw new IllegalArgumentException("targetColumns must not be null"); //$NON-NLS-1$
            }
            if (keyColumns == null) {
                throw new IllegalArgumentException("keyColumns must not be null"); //$NON-NLS-1$
            }
            if (errorCodeColumn == null) {
                throw new IllegalArgumentException("errorCodeColumn must not be null"); //$NON-NLS-1$
            }
            if (errorCodeValue == null) {
                throw new IllegalArgumentException("errorCodeValue must not be null"); //$NON-NLS-1$
            }
            this.tableName = tableName;
            this.targetColumns = targetColumns;
            this.keyColumns = keyColumns;
            this.errorCodeColumn = errorCodeColumn;
            this.errorCodeValue = errorCodeValue;
        }

        /**
         * このオブジェクトに対するプロパティ一覧を返す。
         * @param prefix 設定項目の接頭辞
         * @return このオブジェクトに対するプロパティ一覧
         */
        public Properties toProperties(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
            }
            Properties p = new Properties();
            p.setProperty(prefix + P_TABLE_NAME, tableName);
            p.setProperty(prefix + P_TARGET_COLUMNS, join(targetColumns));
            p.setProperty(prefix + P_KEY_COLUMNS, join(keyColumns));
            p.setProperty(prefix + P_ERROR_CODE_COLUMN, errorCodeColumn);
            p.setProperty(prefix + P_ERROR_CODE_VALUE, errorCodeValue);
            return p;
        }

        static DuplicateRecordErrorTable fromProperties(Properties p, String name) {
            assert p != null;
            assert name != null;
            String tableName = get(p, name + P_TABLE_NAME, false);
            String rawTargetColumns = get(p, name + P_TARGET_COLUMNS, false);
            String rawKeyColumns = get(p, name + P_KEY_COLUMNS, false);
            String errorColumn = get(p, name + P_ERROR_CODE_COLUMN, false);
            String errorCode = get(p, name + P_ERROR_CODE_VALUE, false);
            if (tableName == null || tableName.isEmpty()) {
                return null;
            }
            checkProperties(name, rawTargetColumns, rawKeyColumns, errorColumn, errorCode);
            List<String> targetColumns = split(rawTargetColumns);
            List<String> keyColumns = split(rawKeyColumns);
            return new DuplicateRecordErrorTable(
                    tableName,
                    targetColumns,
                    keyColumns,
                    errorColumn,
                    errorCode);
        }

        private static void checkProperties(
                String name,
                String rawTargetColumns,
                String rawKeyColumns,
                String errorColumn,
                String errorCode) {
            if (rawTargetColumns == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0}.{1}の指定がありません",
                        name,
                        P_TARGET_COLUMNS));
            }
            if (rawKeyColumns == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0}.{1}の指定がありません",
                        name,
                        P_KEY_COLUMNS));
            }
            if (errorColumn == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0}.{1}の指定がありません",
                        name,
                        P_ERROR_CODE_COLUMN));
            }
            if (errorCode == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0}.{1}の指定がありません",
                        name,
                        P_ERROR_CODE_VALUE));
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + tableName.hashCode();
            result = prime * result + targetColumns.hashCode();
            result = prime * result + keyColumns.hashCode();
            result = prime * result + errorCodeColumn.hashCode();
            result = prime * result + errorCodeValue.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DuplicateRecordErrorTable other = (DuplicateRecordErrorTable) obj;
            if (!tableName.equals(other.tableName)) {
                return false;
            }
            if (!targetColumns.equals(other.targetColumns)) {
                return false;
            }
            if (!keyColumns.equals(other.keyColumns)) {
                return false;
            }
            if (!errorCodeColumn.equals(other.errorCodeColumn)) {
                return false;
            }
            if (!errorCodeValue.equals(other.errorCodeValue)) {
                return false;
            }
            return true;
        }
    }

    /**
     * ロック取得のタイプ。
     */
    public enum LockType {

        /**
         * テーブルロック。
         */
        TABLE(1),

        /**
         * 行ロック。
         */
        ROW(2),

        /**
         * ロックしない。
         */
        UNLOCKED(3),
        ;

        /**
         * この項目の識別子。
         */
        public final int id;

        private LockType(int identifier) {
            this.id = identifier;
        }

        static LockType idOf(String id) {
            assert id != null;
            int idNum;
            try {
                idNum = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(id);
            }
            for (LockType constant : values()) {
                if (constant.id == idNum) {
                    return constant;
                }
            }
            throw new IllegalArgumentException(id);
        }
    }

    /**
     * 対象のレコードがロック済みの場合の取り扱い。
     */
    public enum LockedOperation {

        /**
         * 処理対象から外す。
         */
        SKIP(1),

        /**
         * ロックの有無にかかわらず処理対象とする。
         */
        FORCE(2),

        /**
         * エラーとする。
         */
        ERROR(3),
        ;

        /**
         * この項目の識別子。
         */
        public final int id;

        private LockedOperation(int identifier) {
            this.id = identifier;
        }

        static LockedOperation idOf(String id) {
            assert id != null;
            int idNum;
            try {
                idNum = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(id);
            }
            for (LockedOperation constant : values()) {
                if (constant.id == idNum) {
                    return constant;
                }
            }
            throw new IllegalArgumentException(id);
        }
    }
}
