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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;
import com.asakusafw.thundergate.runtime.property.PropertyLoader;

/**
 * プロパティファイルからジョブフロー設定を読み取るクラス。
 * @author yuta.shirai
 */
public class JobFlowParamLoader {

    static final Log LOG = new Log(JobFlowParamLoader.class);

    /**
     * ジョブフローインポート設定のKEY Import対象テーブル。
     */
    private static final String IMP_TARGET_TABLE = "import.target-table";
    /**
     * ジョブフローインポート設定のKEY Import対象カラム。
     */
    private static final String IMP_TARGET_COLUMN = "target-column";
    /**
     * ジョブフローインポート設定のKEY 検索条件。
     */
    private static final String IMP_SEARCH_CONDITION = "search-condition";
    /**
     * Cache ID for import.
     * @since 0.2.3
     */
    private static final String IMP_CACHE_ID = "cache-id";
    /**
     * ジョブフローインポート設定のKEY ロック取得タイプ。
     */
    private static final String IMP_LOCK_TYPE = "lock-type";
    /**
     * ジョブフローインポート設定のKEY ロック済みの場合の取り扱い。
     */
    private static final String IMP_LOCKED_OPERATION = "locked-operation";
    /**
     * ジョブフローインポート設定のKEY JavaBeansクラス名。
     */
    private static final String IMP_BEAN_NAME = "bean-name";
    /**
     * ジョブフローインポート設定のKEY HDFS上の出力パス。
     */
    private static final String IMP_HDFS_IMPORT_FILE = "hdfs-import-file";

    /**
     * ジョブフローエクスポート設定のKEY Export対象テーブル。
     */
    private static final String EXP_TARGET_TABLE = "export.target-table";
    /**
     * ジョブフローエクスポート設定のKEY Export対象テーブルに対応する異常データテーブル。
     */
    private static final String EXP_ERROR_TABLE = "error-table";
    /**
     * Export中間TSVファイルに対応するカラム名。
     */
    private static final String EXP_TSV_COLUMN = "tsv-column";
    /**
     * Export対象テーブルのカラム名。
     */
    private static final String EXP_EXPORT_TABLE_COLUMN = "export-table-column";
    /**
     * 異常データテーブルのカラム名。
     */
    private static final String EXP_ERROR_TABLE_COLUMN = "error-table-column";
    /**
     * キー項目のカラム名。
     */
    private static final String EXP_KEY_COLUMN = "key-column";
    /**
     * エラーコードを格納するカラム名。
     */
    private static final String EXP_ERROR_COLUMN = "error-column";
    /**
     * 重複チェックエラーのエラーコードの値。
     */
    private static final String EXP_ERROR_CODE = "error-code";
    /**
     *  ジョブフローエクスポート設定のKEY JavaBeansクラス名。
     */
    private static final String EXP_BEAN_NAME = "bean-name";
    /**
     *  ジョブフローエクスポート設定のKEY HDFS上の出力パス。
     */
    private static final String EXP_HDFS_EXPORT_FILE = "hdfs-export-file";

    /**
     * インポート対象テーブルの設定（key:テーブル名、value:Import対象テーブルの設定）。
     */
    private Map<String, ImportTargetTableBean> importTargetTables;
    /**
     * エクスポート対象テーブルの設定（key:テーブル名、value:Export対象テーブルの設定）。
     */
    private Map<String, ExportTargetTableBean> exportTargetTables;

    /**
     * インポート処理で使用するパラメータを読み取る。
     * getImportTargetTable()を呼出す前には必ず当メソッドを実行する必要がある。
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobflowId ジョブフローID
     * @param isPrimary 通常起動かどうか
     * @return 読み出しに成功した場合に{@code true}、失敗した場合に{@code false}
     */
    public boolean loadImportParam(
            String targetName,
            String batchId,
            String jobflowId,
            boolean isPrimary) {
        File propFile = createJobFlowConfFile(jobflowId, batchId);
        if (fetchImporterParams(targetName, jobflowId, propFile) == false) {
            return false;
        }
        if (importTargetTables.isEmpty()) {
            return true;
        }
        return checkImportParam(importTargetTables, targetName, jobflowId, propFile.getPath(), isPrimary);
    }

    /**
     * Loads the jobflow parameters in extractor.
     * @param targetName target name
     * @param batchId the batch ID
     * @param jobflowId the jobflow ID
     * @return {@code true} if the parameters are valid, otherwise {@code false}
     * @since 0.2.6
     */
    public boolean loadExtractParam(String targetName, String batchId, String jobflowId) {
        File propFile = createJobFlowConfFile(jobflowId, batchId);
        if (fetchImporterParams(targetName, jobflowId, propFile) == false) {
            return false;
        }
        if (importTargetTables.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, ImportTargetTableBean> entry : importTargetTables.entrySet()) {
            ImportTargetTableBean tableInfo = entry.getValue();
            tableInfo.setSearchCondition(null);
        }
        boolean result = checkImportParam(importTargetTables, targetName, jobflowId, propFile.getPath(), true);
        return result;
    }

    /**
     * Loads the jobflow parameters in cache building.
     * @param targetName target name
     * @param batchId the batch ID
     * @param jobflowId the jobflow ID
     * @return {@code true} if the parameters are valid, otherwise {@code false}
     * @since 0.2.6
     */
    public boolean loadCacheBuildParam(String targetName, String batchId, String jobflowId) {
        File propFile = createJobFlowConfFile(jobflowId, batchId);
        if (fetchImporterParams(targetName, jobflowId, propFile) == false) {
            return false;
        }
        Map<String, ImportTargetTableBean> cacheTables = new HashMap<String, ImportTargetTableBean>();
        for (Map.Entry<String, ImportTargetTableBean> entry : importTargetTables.entrySet()) {
            String tableName = entry.getKey();
            ImportTargetTableBean tableInfo = entry.getValue();
            if (tableInfo.getCacheId() == null) {
                LOG.debugMessage("Table \"{0}\" does not use cache mechanism", tableName);
                continue;
            }
            tableInfo.setLockType(ImportTableLockType.NONE);
            tableInfo.setLockedOperation(ImportTableLockedOperation.FORCE);
            cacheTables.put(tableName, tableInfo);
        }
        if (cacheTables.isEmpty()) {
            return true;
        }
        boolean result = checkImportParam(cacheTables, targetName, jobflowId, propFile.getPath(), false);
        return result;
    }

    private boolean fetchImporterParams(String targetName, String jobflowId, File propFile) {
        Properties properties;
        try {
            properties = getImportProp(propFile, targetName);
        } catch (IOException e) {
            LOG.error(
                    e,
                    "TG-COMMON-00002",
                    "ファイルの読み込みに失敗",
                    targetName,
                    jobflowId,
                    propFile.getPath());
            return false;
        }
        if (!createImportTargetTableBean(properties, targetName, jobflowId, propFile.getPath())) {
            return false;
        }
        return true;
    }

    /**
     * プロパティを解析してimportTargetTableを作成する。
     * @param importProp プロパティ
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param propFilePath プロパティファイルのパス
     * @return importTargetTable作成に成功したか
     */
    private boolean createImportTargetTableBean(
            Properties importProp,
            String targetName,
            String jobflowId,
            String propFilePath) {
        importTargetTables = new TreeMap<String, ImportTargetTableBean>();
        String strTargetTable = importProp.getProperty(IMP_TARGET_TABLE);
        if (strTargetTable == null || strTargetTable.isEmpty()) {
            // Import対象テーブルがない場合は中身を生成せずに終了する
            return true;
        }
        List<String> targetTable = spritComma(strTargetTable);

        // Import対象テーブル分のImportTargetTableBeanを作成する
        for (String element : targetTable) {
            importTargetTables.put(element, new ImportTargetTableBean());
        }

        // ImportTargetTableBeanに値を詰める
        Set<?> set = importProp.keySet();
        for (Object objKey : set) {
            String key = (String) objKey;
            // KEYが対象テーブルの設定でない場合にその行を解析する
            if (!IMP_TARGET_TABLE.equals(key)) {

                // TODO 一連の文字列を解析する処理の見通しが悪い
                // テーブル名を取得
                String tableName = key.substring(0, key.indexOf('.'));

                // 当該行がテーブルに対する何の設定か取得
                int start = tableName.length() + 3;
                int end = key.indexOf('=', start) - 1;
                String keyMeans = key.substring(start + end);

                // テーブル名がImport対象テーブルとして定義されていない場合は読み飛ばす
                ImportTargetTableBean bean = importTargetTables.get(tableName);
                if (bean == null) {
                    continue;
                }
                String value = importProp.getProperty(key);
                // KEYに対するVALUEがない場合は読み飛ばす
                if (value == null || value.equals("")) {
                    continue;
                }

                // 当該行の設定をImportTargetTableBeanに設定する
                if (IMP_TARGET_COLUMN.equals(keyMeans)) {
                    // Import対象カラム
                    bean.setImportTargetColumns(spritComma(value));
                } else if (IMP_SEARCH_CONDITION.equals(keyMeans)) {
                    bean.setSearchCondition(value);
                } else if (IMP_CACHE_ID.equals(keyMeans)) {
                    // enables cache
                    if (value.trim().isEmpty() == false) {
                        bean.setCacheId(value);
                    }
                } else if (IMP_LOCK_TYPE.equals(keyMeans)) {
                    // ロック取得タイプ
                    bean.setLockType(ImportTableLockType.find(value));
                } else if (IMP_LOCKED_OPERATION.equals(keyMeans)) {
                    // ロック済みの場合の取り扱い
                    bean.setLockedOperation(ImportTableLockedOperation.find(value));
                } else if (IMP_BEAN_NAME.equals(keyMeans)) {
                    // JavaBeansクラス名
                    try {
                        bean.setImportTargetType(loadClass(value));
                    } catch (ClassNotFoundException e) {
                        LOG.error(e, "TG-COMMON-00002",
                                "Import対象テーブルに対応するJavaBeanのクラスが存在しない",
                                targetName, jobflowId, propFilePath);
                        return false;
                    }
                } else if (IMP_HDFS_IMPORT_FILE.equals(keyMeans)) {
                    // HDFS上の出力パス
                    bean.setDfsFilePath(value);
                } else {
                    // 設定が不明の場合は読み飛ばす
                    LOG.warn("TG-COMMON-00006",
                            "不明な設定。 key：" + key,
                            targetName, jobflowId, tableName, propFilePath);
                    continue;
                }
            }
        }
        for (Map.Entry<String, ImportTargetTableBean> entry : importTargetTables.entrySet()) {
            ImportTargetTableBean bean = entry.getValue();
            if (bean.getCacheId() != null) {
                if (ThunderGateCacheSupport.class.isAssignableFrom(bean.getImportTargetType()) == false) {
                    LOG.error("TG-COMMON-00002",
                            MessageFormat.format(
                                    "データモデルクラス\"{1}\"がキャッシュをサポートしていない ({0})",
                                    ThunderGateCacheSupport.class.getName(),
                                    bean.getImportTargetType().getName()),
                            targetName, jobflowId, propFilePath);
                    return false;
                }
                if (bean.getSearchCondition() != null && bean.getSearchCondition().trim().isEmpty() == false) {
                    LOG.error("TG-COMMON-00002",
                            MessageFormat.format(
                                    "キャッシュ利用時に条件式を指定している ({0})",
                                    bean.getLockedOperation()),
                            targetName, jobflowId, propFilePath);
                    return false;
                }
                if (bean.getLockedOperation() == ImportTableLockedOperation.OFF) {
                    LOG.error("TG-COMMON-00002",
                            MessageFormat.format(
                                    "キャッシュ利用時にロック箇所を読み飛ばす設定がされている ({0})",
                                    bean.getLockedOperation()),
                            targetName, jobflowId, propFilePath);
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * エクスポート処理で使用するパラメータを読み取る。
     * getExportTargetTable()を呼出す前には必ず当メソッドを実行する必要がある。
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobflowId ジョブフローID
     * @return 読み出しに成功した場合に{@code true}、失敗した場合に{@code false}
     */
    public boolean loadExportParam(String targetName, String batchId, String jobflowId) {
        // ジョブフロー設定のファイル名を作成
        File propFile = createJobFlowConfFile(jobflowId, batchId);

        // プロパティを取得
        Properties exportProp = null;
        try {
            exportProp = getExportProp(propFile, targetName);
        } catch (IOException e) {
            LOG.error(e, "TG-COMMON-00003",
                    "ファイルの読み込みに失敗",
                    targetName, jobflowId, propFile.getPath());
            return false;
        }

        // エクスポート対象テーブルの設定を作成する
        if (!createExportTargetTableBean(exportProp, targetName, jobflowId, propFile.getPath())) {
            return false;
        } else {
            if (exportTargetTables.isEmpty()) {
                // Export対象テーブルがない場合は正常終了する。
                return true;
            }
        }

        return checkExportParam(exportTargetTables, targetName, jobflowId, propFile.getPath());
    }
    /**
     * プロパティを解析してexportTargetTableを作成する。
     * @param exportProp プロパティ
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param propFilePath プロパティファイルのパス
     * @return exportTargetTable作成に成功したか
     */
    private boolean createExportTargetTableBean(
            Properties exportProp,
            String targetName,
            String jobflowId,
            String propFilePath) {
        exportTargetTables = new TreeMap<String, ExportTargetTableBean>();
        String strTargetTable = exportProp.getProperty(EXP_TARGET_TABLE);
        if (strTargetTable == null || strTargetTable.equals("")) {
            // Export対象テーブルがない場合は中身を生成せずに終了する
            return true;
        }
        List<String> targetTable = spritComma(strTargetTable);

        // Export対象テーブル分のExportTargetTableBeanを作成する
        for (String element : targetTable) {
            exportTargetTables.put(element, new ExportTargetTableBean());
        }

        // ExportTargetTableBeanに値を詰める
        Iterator<?> it = exportProp.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            // KEYが対象テーブルの設定でない場合にその行を解析する
            if (!EXP_TARGET_TABLE.equals(key)) {
                // TODO 一連の文字列を解析する処理の見通しが悪い
                // テーブル名を取得
                String tableName = key.substring(0, key.indexOf('.'));

                // 当該行がテーブルに対する何の設定か取得
                int start = tableName.length() + 3;
                int end = key.indexOf('=', start) - 1;
                String keyMeans = key.substring(start + end);

                // テーブル名がExport対象テーブルとして定義されていない場合は読み飛ばす
                ExportTargetTableBean bean = exportTargetTables.get(tableName);
                if (bean == null) {
                    continue;
                }
                // KEYに対するVALUEがない場合は読み飛ばす
                String value = exportProp.getProperty(key);
                if (value == null || value.equals("")) {
                    continue;
                }

                // 当該行の設定をImportTargetTableBeanに設定する
                if (EXP_ERROR_TABLE.equals(keyMeans)) {
                    // Export対象テーブルに対応する異常データテーブル
                    bean.setErrorTableName(value);
                    if (!value.isEmpty()) {
                        bean.setDuplicateCheck(true);
                    }
                } else if (EXP_TSV_COLUMN.equals(keyMeans)) {
                    // Export中間TSVファイルに対応するカラム名
                    bean.setExportTsvColumns(spritComma(value));
                } else if (EXP_EXPORT_TABLE_COLUMN.equals(keyMeans)) {
                    // Export対象テーブルのカラム名
                    bean.setExportTableColumns(spritComma(value));
                } else if (EXP_ERROR_TABLE_COLUMN.equals(keyMeans)) {
                    // 異常データテーブルのカラム名
                    bean.setErrorTableColumns(spritComma(value));
                } else if (EXP_KEY_COLUMN.equals(keyMeans)) {
                    // キー項目のカラム名
                    bean.setKeyColumns(spritComma(value));
                } else if (EXP_ERROR_COLUMN.equals(keyMeans)) {
                    // エラーコードを格納するカラム名
                    bean.setErrorCodeColumn(value);
                } else if (EXP_ERROR_CODE.equals(keyMeans)) {
                    // 重複チェックエラーのエラーコードの値
                    bean.setErrorCode(value);
                } else if (EXP_BEAN_NAME.equals(keyMeans)) {
                    // JavaBeansクラス名
                    try {
                        bean.setExportTargetType(loadClass(value));
                    } catch (ClassNotFoundException e) {
                        LOG.error(e, "TG-COMMON-00003",
                                "Export対象テーブルに対応するJavaBeanのクラスが存在しない",
                                targetName, jobflowId, propFilePath);
                        return false;
                    }
                } else if (EXP_HDFS_EXPORT_FILE.equals(keyMeans)) {
                    // HDFS上の出力パス
                    List<String> path = spritComma(value);
                    List<String> pathList = new ArrayList<String>(path);
                    bean.setDfsFilePaths(pathList);
                } else {
                    // 設定が不明の場合は読み飛ばす
                    LOG.warn("TG-COMMON-00007",
                            "不明な設定。 key：" + key,
                            targetName, jobflowId, tableName, propFilePath);
                    continue;
                }
            }
        }

        return true;
    }

    private Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return Class.forName(className, false, classLoader);
        } else {
            return Class.forName(className);
        }
    }

    /**
     * リカバリ処理で使用するパラメータを読み取る。
     * 当メソッドを呼出した後は以下のメソッドを使用できるようになる。
<pre>
・getImportTargetTable()
・getExportTargetTable()
</pre>
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobflowId ジョブフローID
     * @return ジョブフローインポート設定ロード結果
     */
    public boolean loadRecoveryParam(String targetName, String batchId, String jobflowId) {
        // ジョブフロー設定のファイル名を作成
        File propFile = createJobFlowConfFile(jobflowId, batchId);
        // プロパティを取得
        Properties importProp = null;
        Properties exportProp = null;
        try {
            importProp = getImportProp(propFile, targetName);
            exportProp = getExportProp(propFile, targetName);
        } catch (IOException e) {
            LOG.error(e, "TG-COMMON-00002",
                    "ファイルの読み込みに失敗",
                    targetName, jobflowId, propFile.getPath());
            return false;
        }

        ClassLoader jobflowLoader;
        try {
            final URL jarLocation = propFile.getAbsoluteFile().getCanonicalFile().toURI().toURL();
            jobflowLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    URLClassLoader loader = new URLClassLoader(
                            new URL[] { jarLocation },
                            getClass().getClassLoader());
                    return loader;
                }
            });
        } catch (IOException e) {
            LOG.debugMessage("Failed to load the jobflow library: {0}",
                    propFile);
            jobflowLoader = getClass().getClassLoader();
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(jobflowLoader);
            // インポート対象テーブルの設定を作成する
            if (!createImportTargetTableBean(importProp, targetName, jobflowId, propFile.getPath())) {
                return false;
            }

            // エクスポート対象テーブルの設定を作成する
            if (!createExportTargetTableBean(exportProp, targetName, jobflowId, propFile.getPath())) {
                return false;
            }

            return checkRecoveryParam(exportTargetTables, targetName, jobflowId, propFile.getPath());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
    /**
     * リカバリ処理で使用する設定のチェックを行う。
     * @param tables エクスポート対象テーブルの設定
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param fileName ジョブフローエクスポート設定ファイル名
     * @return チェック結果
     */
    private boolean checkRecoveryParam(
            Map<String, ExportTargetTableBean> tables,
            String targetName, String jobflowId, String fileName) {
        // Export設定のチェックを行う
        for (Map.Entry<String, ExportTargetTableBean> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            ExportTargetTableBean bean = entry.getValue();

            // Export中間TSVファイルに対応するカラム名のチェック
            if (isEmptyOrHasEmptyString(bean.getExportTsvColumn())) {
                LOG.error("TG-COMMON-00005",
                        "Export中間TSVファイルに対応するカラム名が設定されていない",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // Export対象テーブルのカラム名のチェック
            if (isEmptyOrHasEmptyString(bean.getExportTableColumns())) {
                LOG.error("TG-COMMON-00005",
                        "Export対象テーブルのカラム名が設定されていない",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }
            if (columnCheck(bean.getExportTableColumns(), Constants.getTemporarySidColumnName())) {
                LOG.error("TG-COMMON-00005",
                        "テンポラリSIDと同一のカラム名がExport対象テーブルのカラム名に設定されている",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // Export時に重複チェックを行う場合のみチェックを行う
            if (bean.isDuplicateCheck()) {
                // 異常データテーブルのカラム名のチェック
                if (isEmptyOrHasEmptyString(bean.getErrorTableColumns())) {
                    LOG.error("TG-COMMON-00005",
                            "異常データテーブルのカラム名が設定されていない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }

                // エラーコードを格納するカラム名のチェック
                String errCodeColumn = bean.getErrorCodeColumn();
                if (isEmpty(errCodeColumn)) {
                    LOG.error("TG-COMMON-00005",
                            "エラーコードを格納するカラム名が設定されていない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                } else {
                    // エラーコードのカラムがExport対象テーブルにコピーするカラムに含まれている場合はエラーとする
                    if (findArray(errCodeColumn, bean.getExportTableColumns())) {
                        LOG.error("TG-COMMON-00005",
                                "エラーコードを格納するカラム名がExport対象テーブルにデータをコピーするカラムに含まれている",
                                targetName, jobflowId, tableName, fileName);
                        return false;
                    }
                    // エラーコードのカラムが異常データテーブルにコピーするカラムに含まれている場合はエラーとする
                    if (findArray(errCodeColumn, bean.getErrorTableColumns())) {
                        LOG.error("TG-COMMON-00005",
                                "エラーコードを格納するカラム名が異常データテーブルにデータをコピーするカラムに含まれている",
                                targetName, jobflowId, tableName, fileName);
                        return false;
                    }
                }

                // 重複チェックエラーのエラーコードの値のチェック
                if (isEmpty(bean.getErrorCode())) {
                    LOG.error("TG-COMMON-00005",
                            "重複チェックエラーのエラーコードの値が設定されていない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }
            }
        }

        return true;
    }
    /**
     * カンマで区切られた文字列をリストにして返す。
     * @param str カンマで区切られた文字列
     * @return 文字列のリスト
     */
    private List<String> spritComma(String str) {
        String[] array = str.split(",");
        return Arrays.asList(array);
    }
    /**
     * 検索条件を作成する。
     * 文字列中の${javaの環境変数名}を環境変数から取得した値に置換する
     * @param serchCondition 検索条件
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param fileName ジョブフロー設定ファイル名
     * @return 置換済みの検索条件(置換文字列が環境変数にない場合nullを返す)
     */
    private String createSearchCondition(
            String serchCondition,
            String targetName,
            String jobflowId,
            String fileName) {
        VariableTable variables = Constants.createVariableTable();
        try {
            return variables.parse(serchCondition, true);
        } catch (IllegalArgumentException e) {
            LOG.error("TG-COMMON-00002",
                    "検索条件の置換文字に対応する変数が存在しない。検索条件:" + serchCondition,
                    targetName, jobflowId, fileName);
            return null;
        }
    }
    /**
     * 生成したインポート対象テーブルの設定のチェックを行う。
     * @param tables インポート対象テーブルの設定
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param fileName ジョブフローインポート設定ファイル名
     * @param isPrimary  通常起動かどうか
     * @return チェック結果
     */
    public boolean checkImportParam(
            Map<String, ImportTargetTableBean> tables,
            String targetName,
            String jobflowId,
            String fileName,
            boolean isPrimary) {
        for (Map.Entry<String, ImportTargetTableBean> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            ImportTargetTableBean bean = entry.getValue();

            // Import対象カラムのチェック
            if (isEmptyOrHasEmptyString(bean.getImportTargetColumns())) {
                LOG.error("TG-COMMON-00004",
                        "Import対象カラムが設定されていない",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // ロック取得のタイプのチェック
            ImportTableLockType lockType = bean.getLockType();
            if (lockType == null) {
                LOG.error("TG-COMMON-00004",
                        "ロック取得タイプの設定が不正",
                        targetName, jobflowId, tableName, fileName);
                return false;
            } else {
                if (!isPrimary && !ImportTableLockType.NONE.equals(lockType)) {
                    LOG.error("TG-COMMON-00004",
                            "サブ起動のImporterに対してロック取得タイプ「3：ロックしない」以外が指定されている。",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }
            }

            // ロック済みの場合の取り扱いのチェック
            ImportTableLockedOperation operation = bean.getLockedOperation();
            if (operation == null) {
                LOG.error("TG-COMMON-00004",
                        "ロック済みの場合の取り扱いの設定が不正",
                        targetName, jobflowId, tableName, fileName);
                return false;
            } else {
                if (!isPrimary && !ImportTableLockedOperation.FORCE.equals(operation)) {
                    LOG.error("TG-COMMON-00004",
                            "サブ起動のImporterに対してロック済みの場合の取り扱い「2：ロックの有無にかかわらず処理対象とする」以外が指定されている。",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }
            }

            // ロック取得のタイプとロック済みの場合の取り扱いの組合せのチェック
            if (lockType.equals(ImportTableLockType.TABLE) && operation.equals(ImportTableLockedOperation.OFF)
                    || lockType.equals(ImportTableLockType.TABLE) && operation.equals(ImportTableLockedOperation.FORCE)
                    || lockType.equals(ImportTableLockType.RECORD) && operation.equals(ImportTableLockedOperation.FORCE)
                    || lockType.equals(ImportTableLockType.NONE) && operation.equals(ImportTableLockedOperation.OFF)) {
                LOG.error("TG-COMMON-00004",
                        "ロック取得のタイプとロック済みの場合の取り扱いの組合せが不正",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // JavaBeansのクラス名のチェック
            Class<?> beanClass = bean.getImportTargetType();
            if (beanClass == null) {
                LOG.error("TG-COMMON-00004",
                        "Import対象テーブルに対応するJavaBeanのクラスが未設定",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // HDFS上のファイルパスをチェック
            String path = bean.getDfsFilePath();
            if (isEmpty(path)) {
                LOG.error("TG-COMMON-00004",
                        "Import対象テーブルのデータをHDFS上に書き出す際のファイルパスが未設定",
                        targetName, jobflowId, tableName, fileName);
                return false;
            } else {
                try {
                    // 有効なパスかチェック
                    VariableTable variables = Constants.createVariableTable();
                    variables.defineVariable(Constants.HDFS_PATH_VARIABLE_USER, "dummyuser");
                    variables.defineVariable(Constants.HDFS_PATH_VARIABLE_EXECUTION_ID, "dummyid");
                    String dummyPath = variables.parse(path, false);
                    new URI(dummyPath).normalize();
                } catch (URISyntaxException e) {
                    LOG.error(e, "TG-COMMON-00004",
                            "HDFS上に書き出す際のファイルパスが有効でない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }
            }

            // 検索条件（検索条件はcreateSerchConditionを使って置換を行う）
            String condition = bean.getSearchCondition();
            if (condition != null) {
                condition = createSearchCondition(condition, targetName, jobflowId, fileName);
                if (condition == null) {
                    return false;
                } else {
                    bean.setSearchCondition(condition);
                }
            }
        }
        return true;
    }
    /**
     * 生成したエクスポート対象テーブルの設定のチェックを行う。
     * @param tables エクスポート対象テーブルの設定
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param fileName ジョブフローエクスポート設定ファイル名
     * @return チェック結果
     */
    public boolean checkExportParam(
            Map<String, ExportTargetTableBean> tables,
            String targetName,
            String jobflowId,
            String fileName) {

        // TODO 長いメソッド

        for (Map.Entry<String, ExportTargetTableBean> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            ExportTargetTableBean bean = entry.getValue();

            // Export中間TSVファイルに対応するカラム名のチェック
            if (isEmptyOrHasEmptyString(bean.getExportTsvColumn())) {
                LOG.error("TG-COMMON-00005",
                        "Export中間TSVファイルに対応するカラム名が設定されていない",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // Export対象テーブルのカラム名のチェック
            if (isEmptyOrHasEmptyString(bean.getExportTableColumns())) {
                LOG.error("TG-COMMON-00005",
                        "Export対象テーブルのカラム名が設定されていない",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }
            if (columnCheck(bean.getExportTableColumns(), Constants.getTemporarySidColumnName())) {
                LOG.error("TG-COMMON-00005",
                        "テンポラリSIDと同一のカラム名がExport対象テーブルのカラム名に設定されている",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // Export対象テーブルのカラムがExport中間TSVファイルに含まれる事を確認
            List<String> systemColumns = Constants.getSystemColumns();
            List<String> allColumn = new ArrayList<String>(bean.getExportTsvColumn());
            allColumn.addAll(systemColumns);
            if (!includeColumnCheck(bean.getExportTableColumns(), allColumn)) {
                LOG.error("TG-COMMON-00005",
                        "Export対象テーブルのカラム名にExport中間TSVファイルに含まれないカラム名が存在します",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // Export時に重複チェックを行う場合のみチェックを行う
            if (bean.isDuplicateCheck()) {
                // 異常データテーブルのカラム名のチェック
                if (isEmptyOrHasEmptyString(bean.getErrorTableColumns())) {
                    LOG.error("TG-COMMON-00005",
                            "異常データテーブルのカラム名が設定されていない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }

                // 異常データテーブルのカラムがExport中間TSVファイルに含まれる事を確認
                if (!includeColumnCheck(bean.getErrorTableColumns(), allColumn)) {
                    LOG.error("TG-COMMON-00005",
                            "異常データテーブルのカラム名にExport中間TSVファイルに含まれないカラム名が存在します",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }

                // キー項目のカラム名のチェック
                if (isEmptyOrHasEmptyString(bean.getKeyColumns())) {
                    LOG.error("TG-COMMON-00005",
                            "キー項目のカラム名が設定されていない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }

                // エラーコードを格納するカラム名のチェック
                String errCodeColumn = bean.getErrorCodeColumn();
                if (isEmpty(errCodeColumn)) {
                    LOG.error("TG-COMMON-00005",
                            "エラーコードを格納するカラム名が設定されていない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                } else {
                    // エラーコードのカラムがExport対象テーブルにコピーするカラムに含まれている場合はエラーとする
                    if (findArray(errCodeColumn, bean.getExportTableColumns())) {
                        LOG.error("TG-COMMON-00005",
                                "エラーコードを格納するカラム名がExport対象テーブルにデータをコピーするカラムに含まれている",
                                targetName, jobflowId, tableName, fileName);
                        return false;
                    }
                    // エラーコードのカラムが異常データテーブルにコピーするカラムに含まれている場合はエラーとする
                    if (findArray(errCodeColumn, bean.getErrorTableColumns())) {
                        LOG.error("TG-COMMON-00005",
                                "エラーコードを格納するカラム名が異常データテーブルにデータをコピーするカラムに含まれている",
                                targetName, jobflowId, tableName, fileName);
                        return false;
                    }
                }

                // 重複チェックエラーのエラーコードの値のチェック
                if (isEmpty(bean.getErrorCode())) {
                    LOG.error("TG-COMMON-00005",
                            "重複チェックエラーのエラーコードの値が設定されていない",
                            targetName, jobflowId, tableName, fileName);
                    return false;
                }
            }

            // Export対象テーブルに対応するJavaBeansのクラス名のチェック
            Class<?> beanClass = bean.getExportTargetType();
            if (beanClass == null) {
                LOG.error("TG-COMMON-00005",
                        "Export対象テーブルに対応するJavaBeanのクラスが未設定",
                        targetName, jobflowId, tableName, fileName);
                return false;
            }

            // Export対象データのHDFS上のパスのチェック
            List<String> path = bean.getDfsFilePaths();
            if (path == null || path.isEmpty()) {
                LOG.error("TG-COMMON-00005",
                        "Export対象データのHDFS上のパスが設定されていない",
                        targetName, jobflowId, tableName, fileName);
                return false;
            } else {
                for (String element : path) {
                    if (isEmpty(element)) {
                        LOG.error("TG-COMMON-00005",
                                "Export対象データのHDFS上のパスの設定が不正",
                                targetName, jobflowId, tableName, fileName);
                        return false;
                    } else {
                        try {
                            // 有効なパスかチェック
                            VariableTable variables = Constants.createVariableTable();
                            variables.defineVariable(Constants.HDFS_PATH_VARIABLE_USER, "dummyuser");
                            variables.defineVariable(Constants.HDFS_PATH_VARIABLE_EXECUTION_ID, "dummyid");
                            String dummyPath = variables.parse(element, false);
                            new URI(dummyPath).normalize();
                        } catch (URISyntaxException e) {
                            LOG.error(e, "TG-COMMON-00005",
                                    "HDFS上のディレクトリが有効でない",
                                    targetName, jobflowId, tableName, fileName);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    /**
     * 配列の各要素が他の配列に含まれている事を確認する。
     * @param includeColumn 含まれるリスト
     * @param allColumn 含むリスト
     * @return 全ての要素が含まれている場合はtrueを返す
     */
    private boolean includeColumnCheck(List<String> includeColumn, List<String> allColumn) {
        boolean result = true;
        for (String including : includeColumn) {
            boolean search = false;
            for (String column : allColumn) {
                if (including.equals(column)) {
                    search = true;
                    break;
                }
            }
            if (!search) {
                LOG.error("TG-COMMON-00009",
                        including);
                result = false;
            }
        }
        return result;
    }
    /**
     * 指定されたカラム名が配列に含まれないことを確認する。
     * @param exportTableColumn カラム名のリスト
     * @param propKeySysColumnTempSid チェックするカラム名
     * @return 含まれる場合にtrueを返す
     */
    private boolean columnCheck(List<String> exportTableColumn, String propKeySysColumnTempSid) {
        if (exportTableColumn == null || exportTableColumn.size() == 0) {
            return false;
        }
        return exportTableColumn.contains(propKeySysColumnTempSid);
    }
    /**
     * 配列に文字列が存在する場合trueを返す。
     * 配列及び文字列がnullの場合の動作は規定されていない。
     * @param str 検索する文字列
     * @param list 配列
     * @return 指定の配列に指定の文字列が含まれていれば{@code true}、そうでなければ{@code false}
     */
    private boolean findArray(String str, List<String> list) {
        return list.contains(str);
    }
    /**
     * 配列が空か配列の要素が空の場合にtrueを返す。
     * @param tsvColumn リスト
     * @return 結果
     */
    // TODO メソッドの分割
    private boolean isEmptyOrHasEmptyString(List<String> tsvColumn) {
        if (tsvColumn == null || tsvColumn.size() == 0) {
            return true;
        } else {
            for (String element : tsvColumn) {
                if (isEmpty(element)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 引数の文字列が空かnullの場合trueを返す。
     * @param str 文字列
     * @return 結果
     */
    private boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        if (str.isEmpty()) {
            return true;
        }
        return false;
    }
    /**
     * ロードしたインポート対象テーブル一覧の情報を返す。
     * @return テーブル名とテーブル情報のペア一覧
     * @see #loadImportParam(String, String, String, boolean)
     */
    public Map<String, ImportTargetTableBean> getImportTargetTables() {
        return importTargetTables;
    }
    /**
     * ロードしたエクスポート対象テーブル一覧の情報を返す。
     * @return テーブル名とテーブル情報のペア一覧
     * @see #loadExportParam(String, String, String)
     */
    public Map<String, ExportTargetTableBean> getExportTargetTables() {
        return exportTargetTables;
    }

    /**
     * Import処理用のプロパティを返す。
     * @param file プロパティファイル
     * @param targetName ターゲット名
     * @return プロパティオブジェクト
     * @throws IOException プロパティの取得に失敗した場合
     */
    protected Properties getImportProp(File file, String targetName) throws IOException {
        PropertyLoader loader = new PropertyLoader(file, targetName);
        try {
            return loader.loadImporterProperties();
        } finally {
            loader.close();
        }
    }
    /**
     * Export処理用のプロパティを返す。
     * @param file プロパティファイル
     * @param targetName ターゲット名
     * @return プロパティオブジェクト
     * @throws IOException プロパティの取得に失敗した場合
     */
    protected Properties getExportProp(File file, String targetName) throws IOException {
        PropertyLoader loader = new PropertyLoader(file, targetName);
        try {
            return loader.loadExporterProperties();
        } finally {
            loader.close();
        }
    }

    /**
     * ジョブフロー設定のフルパスのファイル名を作成する。
     * @param jobflowId ジョブフローID
     * @param batchId バッチID
     * @return ジョブフロー設定のフルパス
     */
    protected static File createJobFlowConfFile(String jobflowId, String batchId) {
        StringBuffer fileName = new StringBuffer(Constants.DSL_PROP_PREFIX);
        fileName.append(jobflowId);
        fileName.append(Constants.DSL_PROP_EXTENSION);

        File appsHome;
        String explicitBatchappsHome = ConfigurationLoader.getEnvProperty(Constants.BATCHAPPS_PATH);
        if (explicitBatchappsHome != null) {
            appsHome = new File(explicitBatchappsHome);
        } else {
            String frameworkHome = ConfigurationLoader.getEnvProperty(Constants.ASAKUSA_HOME);
            appsHome = new File(frameworkHome, Constants.JOBFLOW_PACKAGE_PATH_BEFORE);
        }
        File appPath = new File(appsHome, batchId);
        File appLibs = new File(appPath, Constants.JOBFLOW_PACKAGE_PATH_AFTER);
        File jobflowLib = new File(appLibs, fileName.toString());

        return jobflowLib;
    }
}
