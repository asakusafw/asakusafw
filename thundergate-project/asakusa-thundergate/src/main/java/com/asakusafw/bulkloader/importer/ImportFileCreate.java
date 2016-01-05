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
package com.asakusafw.bulkloader.importer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.ImportTableLockType;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Importファイルを生成するクラス。
 * @author yuta.shirai
 */
public class ImportFileCreate {

    static final Log LOG = new Log(ImportFileCreate.class);

    private static final String[] EMPTY = new String[0];

    /**
     * Importファイルを生成する。
     * Importファイルはテーブル毎に以下の通り出力される
<pre>
・出力ディレクトリ：[プロパティのimport.tsv-create-dir]
・ファイル名：IMP_[ターゲット名]_[ジョブフローID]_[ジョブフロー実行ID]_[インポート対象テーブル名].tsv
</pre>
     * @param bean パラメータを保持するBean
     * @param jobflowSid ジョブフローID (レコードロックを一つも行わない場合は{@code null}でもよい)
     * @return ファイル生成結果（成功した場合：true、失敗した場合：false）
     */
    public boolean createImportFile(ImportBean bean, String jobflowSid) {
        Connection conn = null;
        try {
            // コネクションを取得する
            conn = DBConnection.getConnection();

            // import対象テーブルの分繰り返してファイル作成を行う。
            List<String> list = bean.getImportTargetTableList();
            for (String tableName : list) {
                ImportTargetTableBean targetTable = bean.getTargetTable(tableName);
                ImportTableLockType lockType = targetTable.getLockType();

                // ファイル名を生成
                File importFile = FileNameUtil.createImportFilePath(
                        bean.getTargetName(), bean.getJobflowId(), bean.getExecutionId(), tableName);

                LOG.info("TG-IMPORTER-03003",
                        tableName,
                        lockType,
                        importFile.getAbsolutePath());

                // ファイルが既に存在する場合はファイルを削除する。
                if (importFile.exists()) {
                    if (!importFile.delete()) {
                        // ファイルの削除に失敗した場合は異常終了する
                        throw new BulkLoaderSystemException(getClass(), "TG-IMPORTER-03001",
                                importFile.getName());
                    }
                }

                // ロック取得有無に応じてレコードを抽出し、ファイルを生成する
                if (ImportTableLockType.TABLE.equals(lockType)) {
                    // ロック取得有無が「テーブルロック」の場合、検索条件でレコードを抽出する
                    createFileWithCondition(
                            conn,
                            tableName,
                            targetTable,
                            importFile);
                } else if (ImportTableLockType.RECORD.equals(lockType)) {
                    // ロック取得有無が「行ロック」の場合、ジョブフローIDを条件にレコードを抽出する
                    createFileWithJobFlowSid(
                            conn,
                            tableName,
                            targetTable,
                            jobflowSid,
                            importFile);
                } else if (ImportTableLockType.NONE.equals(lockType)) {
                    // ロック取得有無が「ロックを取得しない」の場合、検索条件でレコードを抽出する
                    createFileWithCondition(
                            conn,
                            tableName,
                            targetTable,
                            importFile);
                }
                // ファイルが生成出来なかった場合は0byteのファイルを作成する。
                if (!importFile.exists()) {
                    try {
                        if (!importFile.createNewFile()) {
                            throw new BulkLoaderSystemException(getClass(), "TG-IMPORTER-03002");
                        }
                        LOG.info("TG-IMPORTER-03005",
                                tableName,
                                lockType,
                                importFile.getAbsolutePath());
                    } catch (IOException e) {
                        throw new BulkLoaderSystemException(getClass(), "TG-IMPORTER-03002");
                    }
                } else {
                    LOG.info("TG-IMPORTER-03004",
                            tableName,
                            lockType,
                            importFile.getAbsolutePath());
                }

                // 生成したファイル名を追加
                targetTable.setImportFile(importFile);
            }

            // 正常終了
            return true;

        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            return false;
        } finally {
            DBConnection.closeConn(conn);
        }
    }

    /**
     * ジョブフローSIDを条件にレコードを抽出してファイルを生成する。
     * @param conn コネクション
     * @param tableName target table name
     * @param tableInfo target table information
     * @param jobflowSid ジョブフローID
     * @param importFileName importファイル
     * @throws BulkLoaderSystemException 処理に失敗した場合
     */
    private void createFileWithJobFlowSid(
            Connection conn,
            String tableName,
            ImportTargetTableBean tableInfo,
            String jobflowSid,
            File importFileName) throws BulkLoaderSystemException {
        String sql = createSQLWithJobFlowSid(tableName, tableInfo, importFileName);
        PreparedStatement stmt = null;

        String[] parameters = EMPTY;
        LOG.info("TG-IMPORTER-03006", sql, jobflowSid);
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, jobflowSid);
            if (tableInfo.getStartTimestamp() != null) {
                Calendar beginning = tableInfo.getStartTimestamp();
                Timestamp timestamp = new Timestamp(beginning.getTimeInMillis());
                LOG.info("TG-IMPORTER-13001", tableName, tableInfo.getCacheId(), timestamp);
                stmt.setTimestamp(2, timestamp, beginning);
                parameters = new String[] { jobflowSid, String.valueOf(timestamp) };
            } else {
                parameters = new String[] { jobflowSid };
            }
            DBConnection.executeQuery(stmt, sql, parameters);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(e, getClass(), sql, parameters);
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * ジョブフローIDを条件にレコードを抽出する場合のSQLを組み立てる。
     * @param tableName target table name
     * @param tableInfo target table information
     * @param importFileName importファイル
     * @return 生成したSQL文
     * @throws BulkLoaderSystemException if failed to build SQL
     */
    private String createSQLWithJobFlowSid(
            String tableName,
            ImportTargetTableBean tableInfo,
            File importFileName) throws BulkLoaderSystemException {
        String rlTableName = DBAccessUtil.createRecordLockTableName(tableName);

        String baseSearchCondition = MessageFormat.format(
                "WHERE EXISTS (SELECT SID FROM {1} WHERE {1}.SID = {0}.{2} AND {1}.JOBFLOW_SID = ?)",
                tableName,
                rlTableName,
                Constants.getSidColumnName());
        String searchCondition = resolveSearchCondition(tableName, tableInfo, baseSearchCondition);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(DBAccessUtil.joinColumnArray(tableInfo.getImportTargetColumns()));
        sql.append(" FROM ");
        sql.append(tableName);
        sql.append(" ");
        sql.append(searchCondition);
        sql.append(" INTO OUTFILE ");
        sql.append("'");
        sql.append(importFileName.getAbsolutePath().replace(File.separatorChar, '/'));
        sql.append("'");
        sql.append(DBAccessUtil.getTSVFileFormat());

        return sql.toString();
    }
    /**
     * 検索条件でレコードを抽出してファイルを生成する。
     * @param conn コネクション
     * @param tableName target table name
     * @param tableInfo target table information
     * @param importFileName importファイル
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void createFileWithCondition(
            Connection conn,
            String tableName,
            ImportTargetTableBean tableInfo,
            File importFileName) throws BulkLoaderSystemException {
        String sql = createSQLWithCondition(tableName, tableInfo, importFileName);
        PreparedStatement stmt = null;

        LOG.info("TG-IMPORTER-03007", sql);
        String[] parameters = EMPTY;
        try {
            stmt = conn.prepareStatement(sql);
            if (tableInfo.getStartTimestamp() != null) {
                Calendar beginning = tableInfo.getStartTimestamp();
                Timestamp timestamp = new Timestamp(beginning.getTimeInMillis());
                LOG.info("TG-IMPORTER-13001", tableName, tableInfo.getCacheId(), timestamp);
                stmt.setTimestamp(1, timestamp, beginning);
                parameters = new String[] { String.valueOf(timestamp) };
            }
            DBConnection.executeQuery(stmt, sql, parameters);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(e, getClass(), sql, parameters);
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * 検索条件でレコードを抽出する場合のSQLを組み立てる。
     * @param tableName target table name
     * @param tableInfo target table information
     * @param importFileName importファイル
     * @return 生成したSQL
     * @throws BulkLoaderSystemException if failed to build SQL
     */
    private String createSQLWithCondition(
            String tableName,
            ImportTargetTableBean tableInfo,
            File importFileName) throws BulkLoaderSystemException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(DBAccessUtil.joinColumnArray(tableInfo.getImportTargetColumns()));
        sql.append(" FROM ");
        sql.append(tableName);
        String searchCondition = resolveSearchCondition(tableName, tableInfo, tableInfo.getSearchCondition());
        if (searchCondition != null && !searchCondition.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(searchCondition);
        }
        sql.append(" INTO OUTFILE ");
        sql.append("'");
        sql.append(importFileName.getAbsolutePath().replace(File.separatorChar, '/'));
        sql.append("'");
        sql.append(DBAccessUtil.getTSVFileFormat());

        return sql.toString();
    }

    /**
     * Creates condition expression for the target table.
     * If {@link ImportTargetTableBean#getStartTimestamp() cache is valid},
     * the search condition will include a placeholder ({@code ?}) in its tail.
     * @param tableName target table name (or alias name)
     * @param tableInfo target table information
     * @param expression the original condition expression (nullable)
     * @return the built string, or {@code null} if unconditioned
     * @throws BulkLoaderSystemException if failed to resolve search condition
     */
    private String resolveSearchCondition(
            String tableName,
            ImportTargetTableBean tableInfo,
            String expression) throws BulkLoaderSystemException {
        assert tableName != null;
        assert tableInfo != null;
        String original = expression;
        if (original == null || original.trim().isEmpty()) {
            original = null;
        }
        if (tableInfo.getStartTimestamp() == null) {
            return original;
        } else {
            ThunderGateCacheSupport support;
            try {
                support = tableInfo
                    .getImportTargetType()
                    .asSubclass(ThunderGateCacheSupport.class)
                    .newInstance();
            } catch (Exception e) {
                throw new BulkLoaderSystemException(e, getClass(), "TG-IMPORTER-13002",
                        tableName,
                        tableInfo.getCacheId(),
                        tableInfo.getImportTargetType().getName());
            }
            String timestampColumn = support.__tgc__TimestampColumn();
            if (original == null) {
                return MessageFormat.format(
                        "{0}.{1} >= ?",
                        tableName,
                        timestampColumn);
            } else {
                return MessageFormat.format(
                        "({0}) AND {1}.{2} >= ?",
                        original,
                        tableName,
                        timestampColumn);
            }
        }
    }
}
