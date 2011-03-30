/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.List;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.ImportTableLockType;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * Importファイルを生成するクラス。
 * @author yuta.shirai
 */
public class ImportFileCreate {
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

                Log.log(
                        this.getClass(),
                        MessageIdConst.IMP_CREATE_FILE,
                        tableName,
                        lockType,
                        importFile.getAbsolutePath());

                // ファイルが既に存在する場合はファイルを削除する。
                if (importFile.exists()) {
                    if (!importFile.delete()) {
                        // ファイルの削除に失敗した場合は異常終了する
                        throw new BulkLoaderSystemException(
                                this.getClass(),
                                MessageIdConst.IMP_EXISTSFILE_DELETE_ERROR,
                                importFile.getName());
                    }
                }

                // ロック取得有無に応じてレコードを抽出し、ファイルを生成する
                if (ImportTableLockType.TABLE.equals(lockType)) {
                    // ロック取得有無が「テーブルロック」の場合、検索条件でレコードを抽出する
                    createFileWithCondition(
                            conn,
                            tableName,
                            targetTable.getImportTargetColumns(),
                            targetTable.getSearchCondition(),
                            importFile);
                } else if (ImportTableLockType.RECORD.equals(lockType)) {
                    // ロック取得有無が「行ロック」の場合、ジョブフローIDを条件にレコードを抽出する
                    createFileWithJobFlowSid(
                            conn,
                            tableName,
                            targetTable.getImportTargetColumns(),
                            jobflowSid,
                            importFile);
                } else if (ImportTableLockType.NONE.equals(lockType)) {
                    // ロック取得有無が「ロックを取得しない」の場合、検索条件でレコードを抽出する
                    createFileWithCondition(
                            conn,
                            tableName,
                            targetTable.getImportTargetColumns(),
                            targetTable.getSearchCondition(),
                            importFile);
                }
                // ファイルが生成出来なかった場合は0byteのファイルを作成する。
                if (!importFile.exists()) {
                    try {
                        if (!importFile.createNewFile()) {
                            throw new BulkLoaderSystemException(
                                    this.getClass(),
                                    MessageIdConst.IMP_CREATEFILE_EXCEPTION);
                        }
                        Log.log(
                                this.getClass(),
                                MessageIdConst.IMP_CREATE_ZERO_FILE,
                                tableName,
                                lockType,
                                importFile.getAbsolutePath());
                    } catch (IOException e) {
                        throw new BulkLoaderSystemException(
                                this.getClass(),
                                MessageIdConst.IMP_CREATEFILE_EXCEPTION);
                    }
                } else {
                    Log.log(
                            this.getClass(),
                            MessageIdConst.IMP_CREATE_FILE_SUCCESS,
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
            Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
            return false;
        } finally {
            DBConnection.closeConn(conn);
        }
    }

    /**
     * ジョブフローSIDを条件にレコードを抽出してファイルを生成する。
     * @param conn コネクション
     * @param tableName import対象テーブル
     * @param columns import対象カラム
     * @param jobflowSid ジョブフローID
     * @param importFileName importファイル
     * @throws BulkLoaderSystemException 処理に失敗した場合
     */
    private void createFileWithJobFlowSid(
            Connection conn,
            String tableName,
            List<String> columns,
            String jobflowSid,
            File importFileName) throws BulkLoaderSystemException {
        String sql = createSQLWithJobFlowSid(tableName, columns, importFileName);
        PreparedStatement stmt = null;

        Log.log(this.getClass(), MessageIdConst.IMP_CREATE_FILE_WITH_JOBFLOWSID, sql, jobflowSid);
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, jobflowSid);
            DBConnection.executeQuery(stmt, sql, new String[] { jobflowSid });
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql,
                    new String[] { jobflowSid });
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * ジョブフローIDを条件にレコードを抽出する場合のSQLを組み立てる。
     * @param tableName import対象テーブル
     * @param columns import対象カラム
     * @param importFileName importファイル
     * @return 生成したSQL文
     */
    protected String createSQLWithJobFlowSid(String tableName, List<String> columns, File importFileName) {
        String rlTableName = DBAccessUtil.createRecordLockTableName(tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(DBAccessUtil.joinColumnArray(columns));
        sql.append(" FROM ");
        sql.append(tableName);
        sql.append(" WHERE EXISTS (SELECT SID FROM ");
        sql.append(rlTableName);
        sql.append(" WHERE ");
        sql.append(rlTableName);
        sql.append(".SID=");
        sql.append(tableName);
        sql.append(".SID AND JOBFLOW_SID=?)");
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
     * @param tableName import対象テーブル
     * @param columns import対象カラム
     * @param serchCondition 検索条件
     * @param importFileName importファイル
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void createFileWithCondition(
            Connection conn,
            String tableName,
            List<String> columns,
            String serchCondition,
            File importFileName) throws BulkLoaderSystemException {
        String sql = createSQLWithCondition(tableName, columns, serchCondition, importFileName);
        PreparedStatement stmt = null;

        Log.log(this.getClass(), MessageIdConst.IMP_CREATE_FILE_WITH_CONDITION, sql);
        try {
            stmt = conn.prepareStatement(sql);
            DBConnection.executeQuery(stmt, sql, new String[0]);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e, this.getClass(), sql, new String[0]);
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * 検索条件でレコードを抽出する場合のSQLを組み立てる。
     * @param tableName import対象テーブル
     * @param columns import対象カラム
     * @param serchCondition 検索条件
     * @param importFileName importファイル
     * @return 生成したSQL
     */
    protected String createSQLWithCondition(
            String tableName,
            List<String> columns,
            String serchCondition,
            File importFileName) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(DBAccessUtil.joinColumnArray(columns));
        sql.append(" FROM ");
        sql.append(tableName);
        if (serchCondition != null && !serchCondition.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(serchCondition);
        }

        sql.append(" INTO OUTFILE ");
        sql.append("'");
        sql.append(importFileName.getAbsolutePath().replace(File.separatorChar, '/'));
        sql.append("'");
        sql.append(DBAccessUtil.getTSVFileFormat());

        return sql.toString();
    }
}
