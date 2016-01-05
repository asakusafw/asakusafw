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
package com.asakusafw.bulkloader.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * Importerで設定したロックを解除するクラス。
 * @author yuta.shirai
 */
public class LockRelease {

    static final Log LOG = new Log(LockRelease.class);

    /**
     * ロックを解除する。
     * @param bean パラメータを保持するBean
     * @param isEndJobFlow ジョブフローの処理がが全て正常終了したか
     * @return ロック解除結果（成功した場合：true、失敗した場合：false）
     */
    public boolean releaseLock(ExporterBean bean, boolean isEndJobFlow) {
        // リトライ回数
        int retryCount = bean.getRetryCount();
        // リトライインターバル
        int retryInterval = bean.getRetryInterval();
        // 実行回数を示すカウンタ
        int retry = 0;

        // Import対象テーブルとExport対象テーブルをマージ
        Set<String> tableSet = mergingIOTable(bean.getExportTargetTableList(), bean.getImportTargetTableList());

        Connection conn = null;
        try {
            // コネクションを取得する
            conn = DBConnection.getConnection();

            // Import/Export対象テーブルがない場合はジョブフロー実行テーブルのレコードを削除して終了する
            if (tableSet.isEmpty()) {
                if (isEndJobFlow) {
                    endJobFlow(conn, bean.getJobflowSid());
                    DBConnection.commit(conn);
                }
                return true;
            }

            // テンポラリテーブルを削除する
            deleteTempTable(bean, isEndJobFlow, conn);

            // テンポラリ管理テーブルのレコードを削除する。
            deleteTempInfoRecord(bean, isEndJobFlow, conn);

            // リトライのループ(IMPORT_TABLE_LOCKのロック取得に失敗した時のみリトライする)
            while (true) {
                retry++;
                // IMPORT_TABLE_LOCKテーブルのロックを取得する
                try {
                    getImportTableLock(conn, tableSet.iterator());
                    break;
                } catch (BulkLoaderReRunnableException e) {
                    LOG.log(e);
                    if (retry <= retryCount) {
                        // リトライ可能な場合、ロールバックしてリトライする
                        try {
                            DBConnection.rollback(conn);
                            Thread.sleep(TimeUnit.SECONDS.toMillis(retryInterval));
                            continue;
                        } catch (InterruptedException e1) {
                            throw new BulkLoaderSystemException(e1, getClass(), "TG-EXPORTER-04001",
                                    "IMPORT_TABLE_LOCKテーブルのロック取得に失敗");
                        }
                    } else {
                        // リトライ不可の場合、異常終了する。
                        throw new BulkLoaderSystemException(getClass(), "TG-EXPORTER-04002",
                                "IMPORT_TABLE_LOCKテーブルのロック取得に失敗");
                    }
                }
            }

            // テーブルロックを解除する
            releaseTableLock(conn, bean.getJobflowSid());

            // 行ロックを解除する
            for (String tableName : tableSet) {
                releaseLineLock(conn, tableName, bean.getJobflowSid());
            }

            // ジョブフロー実行テーブルのレコードを削除する
            if (isEndJobFlow) {
                endJobFlow(conn, bean.getJobflowSid());
            }

            // 正常終了
            DBConnection.commit(conn);
            return true;

        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            try {
                DBConnection.rollback(conn);
            } catch (BulkLoaderSystemException e1) {
                LOG.log(e1);
            }
            return false;
        } finally {
            DBConnection.closeConn(conn);
        }
    }
    /**
     * ジョブフロー実行テーブルのレコードを削除する。
     * @param conn コネクション
     * @param jobflowSid ジョブフローSID
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void endJobFlow(Connection conn, String jobflowSid) throws BulkLoaderSystemException {
        String sql = "DELETE FROM RUNNING_JOBFLOWS WHERE JOBFLOW_SID=?";
        PreparedStatement stmt = null;

        LOG.info("TG-EXPORTER-04007", sql, jobflowSid);
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, jobflowSid);
            DBConnection.executeUpdate(stmt, sql, new String[]{ jobflowSid });
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql,
                    new String[]{ jobflowSid });
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * テンポラリ管理テーブルのレコードを削除する。
     * @param bean パラメータを保持するBean
     * @param deleteTempTableForce コピーが終了していないテンポラリテーブルも強制的に削除するか
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void deleteTempInfoRecord(
            ExporterBean bean,
            boolean deleteTempTableForce,
            Connection conn) throws BulkLoaderSystemException {
        List<String> list = bean.getExportTargetTableList();
        TempTableDelete delete = createTempTableDelete();
        for (String tableName : list) {
            delete.deleteTempInfoRecord(bean.getJobflowSid(), tableName, deleteTempTableForce, conn);
        }
    }
    /**
     * テンポラリテーブルを削除する。
     * @param bean パラメータを保持するBean
     * @param deleteTempTableForce コピーが終了していないテンポラリテーブルも強制的に削除するか
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void deleteTempTable(
            ExporterBean bean,
            boolean deleteTempTableForce,
            Connection conn) throws BulkLoaderSystemException {
        List<String> list = bean.getExportTargetTableList();
        TempTableDelete delete = createTempTableDelete();
        for (String tableName : list) {
            ExportTargetTableBean table = bean.getExportTargetTable(tableName);
            String tempTable = table.getExportTempTableName();
            String dupTalbe = table.getDuplicateFlagTableName();
            if (tempTable != null) {
                delete.deleteTempTable(tempTable, dupTalbe, deleteTempTableForce, conn);
            }
        }
    }
    /**
     * Export対象テーブルとImport対象テーブルのテーブル名をマージして返す。
     * @param exportTargetTableList Export対象テーブル
     * @param importTargetTableList Import対象テーブル
     * @return マージしたテーブルのセット
     */
    private Set<String> mergingIOTable(
            List<String> exportTargetTableList,
            List<String> importTargetTableList) {
        Set<String> set = new LinkedHashSet<String>();
        for (String exportTable : exportTargetTableList) {
            set.add(exportTable);
        }
        for (String importTable : importTargetTableList) {
            set.add(importTable);
        }
        return set;
    }
    /**
     * IMPORT_TABLE_LOCKテーブルからExport対象テーブルのレコードのロックを取得する。
     * @param conn コネクション
     * @param importTargetTable Export対象テーブル
     * @throws BulkLoaderReRunnableException リトライ可能エラー
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private void getImportTableLock(
            Connection conn,
            Iterator<String> importTargetTable) throws BulkLoaderReRunnableException, BulkLoaderSystemException {
        String selectSql1 = "SELECT JOBFLOW_SID FROM IMPORT_TABLE_LOCK WHERE TABLE_NAME IN(";
        String selectSql2 = ") FOR UPDATE";
        StringBuffer selectSql = new StringBuffer(selectSql1);
        PreparedStatement stmt = null;

        try {
            while (importTargetTable.hasNext()) {
                // ロック取得のSQLを組み立てる
                selectSql.append("\"");
                selectSql.append(importTargetTable.next());
                selectSql.append("\"");
                if (importTargetTable.hasNext()) {
                    selectSql.append(",");
                }
            }
            selectSql.append(selectSql2);

            // トランザクションのロックを取得する
            LOG.info("TG-EXPORTER-04004", selectSql.toString());
            stmt = conn.prepareStatement(selectSql.toString());
            DBConnection.executeQuery(stmt, selectSql.toString(), new String[0]);

        } catch (SQLException e) {
            throw new BulkLoaderReRunnableException(
                    e,
                    this.getClass(),
                    "TG-EXPORTER-04003",
                    "IMPORT_TABLE_LOCKテーブルのロック取得に失敗");
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * テーブルロックを解除する。
     * IMPORT_TABLE_LOCKテーブルのジョブフローIDが一致するレコードに対して、
     * ジョブフローIDにnullを設定することでロックを解除する
     * @param conn コネクション
     * @param jobFlowSid ジョブフローSID
     * @throws BulkLoaderSystemException SQL例外した場合
     */
    private void releaseTableLock(Connection conn, String jobFlowSid) throws BulkLoaderSystemException {
        String sql = "UPDATE IMPORT_TABLE_LOCK "
            + "SET JOBFLOW_SID=NULL "
            + "WHERE JOBFLOW_SID=?";
        PreparedStatement stmt = null;

        LOG.info("TG-EXPORTER-04005", sql, jobFlowSid);
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, jobFlowSid);
            DBConnection.executeUpdate(stmt, sql, new String[]{jobFlowSid});
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql,
                    new String[]{ jobFlowSid });
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * 行ロックを解除する。
     * ロック済みレコードテーブル及びレコードロックテーブルからレコードを削除する。
     * @param conn コネクション
     * @param tableName テーブル名
     * @param jobflowSid ジョブフローSID
     * @throws BulkLoaderSystemException SQL例外した場合
     */
    private void releaseLineLock(
            Connection conn,
            String tableName,
            String jobflowSid) throws BulkLoaderSystemException {
        String recordLockSql = "DELETE FROM IMPORT_RECORD_LOCK "
            + "WHERE JOBFLOW_SID=? AND TABLE_NAME=?";
        PreparedStatement stmt = null;

        int count = 0;
        try {
            // レコードロックのレコードを削除
            stmt = conn.prepareStatement(recordLockSql);
            stmt.setString(1, jobflowSid);
            stmt.setString(2, tableName);
            count = DBConnection.executeUpdate(
                    stmt,
                    recordLockSql,
                    new String[] { jobflowSid, tableName });
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    recordLockSql,
                    new String[] { jobflowSid, tableName });
        } finally {
            DBConnection.closePs(stmt);
        }

        if (count > 0) {
            String rlTableName = DBAccessUtil.createRecordLockTableName(tableName);
            StringBuffer rlSql = new StringBuffer("DELETE FROM ");
            rlSql.append(rlTableName);
            rlSql.append(" WHERE JOBFLOW_SID=?");
            LOG.info("TG-EXPORTER-04006",
                    rlSql.toString(), recordLockSql, jobflowSid, tableName);
            try {
                // ロック済みレコードのレコードを削除
                stmt = conn.prepareStatement(rlSql.toString());
                stmt.setString(1, jobflowSid);
                DBConnection.executeUpdate(stmt, rlSql.toString(), new String[]{ jobflowSid });
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        rlSql.toString(),
                        new String[]{ jobflowSid });
            } finally {
                DBConnection.closePs(stmt);
            }
        }
    }
    /**
     * TempTableDeleteを生成して返す。
     * @return 生成したオブジェクト
     */
    protected TempTableDelete createTempTableDelete() {
        return new TempTableDelete();
    }
}
