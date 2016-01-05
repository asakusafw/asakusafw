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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.ImportTableLockType;
import com.asakusafw.bulkloader.common.ImportTableLockedOperation;
import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * Import対象テーブルのロックを取得するクラス。
 * @author yuta.shirai
 */
public class TargetDataLock {

    static final Log LOG = new Log(TargetDataLock.class);

    private static final String NOT_EXISTS_JOBFLOW_SID = "-1";
    /**
     * ジョブフローSID。
     */
    private String jobflowSid;

    /**
     * Import対象テーブルのロックを取得する。
     * テーブルロックの場合はIMPORT_TABLE_LOCKにジョブフローSIDを登録する。
     * レコードロックの場合はImport対象テーブルにジョブフローSIDを登録する。
     * @param bean パラメータを保持するBean
     * @return ロック取得結果（成功した場合：true、失敗した場合：false）
     * @throws BulkLoaderReRunnableException if failed to acquire data lock but is retryable
     */
    public boolean lock(ImportBean bean) throws BulkLoaderReRunnableException {
        // リトライ回数
        int retryCount = bean.getRetryCount();
        // リトライインターバル
        int retryInterval = bean.getRetryInterval();
        // 実行回数を示すカウンタ
        int retry = 0;

        Connection conn = null;
        try {
            // コネクションを取得する
            conn = DBConnection.getConnection();

            // RUNNING_JOBFLOWSテーブルに同一ジョブフロー実行IDが無いことを確認する
            // 同一ジョブフロー実行IDがある場合、再実行とみなしてロック取得処理を正常終了する。
            // (RUNNING_JOBFLOWSがコミットされているということは、ロック取得に成功している)
            jobflowSid = checkExecutionId(conn, bean.getExecutionId());

            if (!jobflowSid.equals(NOT_EXISTS_JOBFLOW_SID)) {
                LOG.info("TG-IMPORTER-02001",
                        bean.getTargetName(), bean.getExecutionId(), jobflowSid);
                return true;
            }

            // リトライのループ
            while (true) {
                retry++;
                try {
                    LOG.info("TG-IMPORTER-02005",
                            bean.getTargetName(), bean.getExecutionId());
                    // ロック取得のトランザクションを実行する。
                    execTran(conn, bean);
                    // コミットして正常終了する
                    DBConnection.commit(conn);
                    LOG.info("TG-IMPORTER-02006",
                            bean.getTargetName(), bean.getExecutionId());
                    return true;
                } catch (BulkLoaderReRunnableException e) {
                    LOG.log(e);
                    if (retry <= retryCount) {
                        // リトライ可能な場合、ロールバックしてリトライする
                        try {
                            DBConnection.rollback(conn);
                            Thread.sleep(TimeUnit.SECONDS.toMillis(retryInterval));
                            continue;
                        } catch (InterruptedException e2) {
                            throw new BulkLoaderSystemException(e2, getClass(), "TG-IMPORTER-02002");
                        }
                    } else {
                        // リトライ不可の場合、異常終了する。
                        throw new BulkLoaderReRunnableException(e, getClass(), "TG-IMPORTER-02003");
                    }
                }
            }
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            try {
                DBConnection.rollback(conn);
            } catch (BulkLoaderSystemException e1) {
                // ここで例外が発生した場合は握りつぶす
                e1.printStackTrace();
            }
            return false;
        } finally {
            DBConnection.closeConn(conn);
        }
    }
    /**
     * ロック取得のトランザクション処理を実行する。
     * @param conn Connection
     * @param bean ImportBean
     * @throws BulkLoaderReRunnableException リトライ可能エラー
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private void execTran(
            Connection conn,
            ImportBean bean) throws BulkLoaderReRunnableException, BulkLoaderSystemException {

        // TODO 見通しが悪い

        // RUNNING_JOBFLOWSテーブルにレコードを挿入する
        jobflowSid = insertRunningJobFlow(
                conn,
                bean.getTargetName(),
                bean.getBatchId(),
                bean.getJobflowId(),
                bean.getExecutionId(),
                bean.getJobnetEndTime());

        // IMPORT_TABLE_LOCKテーブルのロックを取得
        Map<String, String> tableLock;
        try {
            tableLock = getImportTableLock(conn, bean.getImportTargetTableList().iterator());
        } catch (BulkLoaderSystemException e) {
            // リトライする
            throw new BulkLoaderReRunnableException(e.getCause(), getClass(), "TG-IMPORTER-02004",
                    "ロック取得処理の排他制御に失敗",
                    "IMPORT_TABLE_LOCK");
        }

        // Import対象テーブル毎に処理し、指定に応じてロックフラグを立てる
        List<String> list = bean.getImportTargetTableList();
        for (String tableName : list) {
            ImportTargetTableBean targetTable = bean.getTargetTable(tableName);
            ImportTableLockType lockType = targetTable.getLockType();
            ImportTableLockedOperation operation = targetTable.getLockedOperation();
            String serchCondition = targetTable.getSearchCondition();

            LOG.info("TG-IMPORTER-02009",
                    tableName, lockType, operation, serchCondition);

            // 「ロックしない」かつ「ロック有無に関わらず処理対象とする」の場合、ロックフラグは立てずに終了する。
            if (ImportTableLockType.NONE.equals(lockType)
                    && ImportTableLockedOperation.FORCE.equals(operation)) {
                LOG.info("TG-IMPORTER-02011", tableName);
                continue;
            }

            // テーブルにロックフラグが立っていないかチェックする
            String targetSid = tableLock.get(tableName);
            if (targetSid != null && !targetSid.isEmpty()) {
                // 「行ロック」「処理対象から外す」の場合、ロックを取得しないことで当該テーブルを処理対象としない
                if (ImportTableLockType.RECORD.equals(lockType)
                        && ImportTableLockedOperation.OFF.equals(operation)) {
                    LOG.info("TG-IMPORTER-02012", tableName);
                    continue;
                } else {
                    // ロック済みの動作が「エラーとする」の場合、リトライを行う。
                    // テーブルにロックフラグが立っている場合、リトライする
                    throw new BulkLoaderReRunnableException(getClass(), "TG-IMPORTER-02004",
                            "ロック対象テーブルのテーブルロックが取得されている",
                            tableName);
                }
            }

            // ロック取得範囲に応じてロックフラグを立てる
            if (ImportTableLockType.TABLE.equals(lockType)) {
                // テーブルロックの場合
                // 対象テーブルのレコードにロックフラグが立っていないかチェック
                if (!checkRecordLock(conn, tableName)) {
                    // リトライする
                    throw new BulkLoaderReRunnableException(getClass(), "TG-IMPORTER-02004",
                            "ロック対象テーブルのレコードロックが取得されている",
                            tableName);
                } else {
                    // IMPORT_TABLE_LOCKにロックフラグを立てる
                    tableLock(conn, tableName, jobflowSid);
                    continue;
                }
            } else if (ImportTableLockType.RECORD.equals(lockType)) {
                // 行ロックの場合
                // ロック対象のロックを取得できなかった場合の動作に応じて処理を行う
                if (ImportTableLockedOperation.OFF.equals(operation)) {
                    // 処理対象から外す場合、ロックフラグが立っていないレコードのみロックフラグを立てる
                    recordLock(conn, tableName, serchCondition, jobflowSid);
                    continue;
                } else if (ImportTableLockedOperation.ERROR.equals(operation)) {
                    // エラーとする場合、対象レコードにロックフラグが立っていないかチェックする
                    if (!checkRecordLock(conn, tableName, serchCondition)) {
                        // リトライする
                        throw new BulkLoaderReRunnableException(getClass(), "TG-IMPORTER-02004",
                                "ロック対象テーブルのレコードロックが取得されている",
                                tableName);
                    } else {
                        // 対象レコードにロックフラグを立てる
                        recordLock(conn, tableName, serchCondition, jobflowSid);
                        continue;
                    }
                }
            } else if (ImportTableLockType.NONE.equals(lockType)) {
                // ロックを取得しない場合
                // ロック対象のロックを取得できなかった場合の動作に応じて処理を行う
                // ここに到達するのは「エラーとする」のみ
                // 対象レコードにロックフラグが立っていないかチェックする
                if (!checkRecordLock(conn, tableName, serchCondition)) {
                    // リトライする
                    throw new BulkLoaderReRunnableException(getClass(), "TG-IMPORTER-02004",
                            "ロック対象テーブルのレコードロックが取得されている",
                            tableName);
                } else {
                    // ロックフラグは立てずに終了する。
                    LOG.info("TG-IMPORTER-02013", tableName);
                    continue;
                }
            }
            LOG.info("TG-IMPORTER-02010",
                    tableName, lockType, operation, serchCondition);
        }
    }
    /**
     * RUNNING_JOBFLOWSに同一ジョブフロー実行IDが存在するかチェックする。
     * @param conn コネクション
     * @param executionId ジョブフロー実行ID
     * @return レコードが存在しない場合：-1、存在する場合：ジョブフローSID
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private String checkExecutionId(
            Connection conn,
            String executionId) throws BulkLoaderSystemException {
        String sql = "SELECT JOBFLOW_SID "
            + "FROM RUNNING_JOBFLOWS "
            + "WHERE EXECUTION_ID=?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, executionId);
            rs = DBConnection.executeQuery(stmt, sql, new String[] { executionId });
            if (rs.next()) {
                return rs.getString("JOBFLOW_SID");
            } else {
                return NOT_EXISTS_JOBFLOW_SID;
            }
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql,
                    new String[] { executionId });
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }
    }
    /**
     * RUNNING_JOBFLOWSテーブルにレコードをインサートする。
     * @param conn コネクション
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobflowId ジョブフローID
     * @param executionId ジョブフロー実行ID
     * @param jobnetEndTime 終了予定時刻
     * @return ジョブフローSID
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private String insertRunningJobFlow(
            Connection conn,
            String targetName,
            String batchId,
            String jobflowId,
            String executionId,
            Date jobnetEndTime) throws BulkLoaderSystemException {
        String insertSql = "INSERT INTO RUNNING_JOBFLOWS "
            + "(BATCH_ID,JOBFLOW_ID,TARGET_NAME,EXECUTION_ID,EXPECTED_COMPLETION_DATETIME) "
            + "VALUES(?,?,?,?,?)";
        String selectSql = "SELECT LAST_INSERT_ID()";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        LOG.info("TG-IMPORTER-02007",
                insertSql,
                batchId,
                jobflowId,
                targetName,
                executionId,
                jobnetEndTime);
        try {
            try {
                stmt = conn.prepareStatement(insertSql);
                stmt.setString(1, batchId);
                stmt.setString(2, jobflowId);
                stmt.setString(3, targetName);
                stmt.setString(4, executionId);
                stmt.setTimestamp(5, new java.sql.Timestamp(jobnetEndTime.getTime()));
                DBConnection.executeUpdate(
                        stmt,
                        insertSql,
                        new String[] { batchId, jobflowId, targetName, executionId });
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        insertSql,
                        new String[] { batchId, jobflowId, executionId, jobnetEndTime.toString() });
            }

            try {
                stmt = conn.prepareStatement(selectSql);
                rs = DBConnection.executeQuery(stmt, selectSql, new String[0]);
                rs.next();
                return rs.getString(1);
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        selectSql,
                        new String[0]);
            }
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }
    }
    /**
     * IMPORT_TABLE_LOCKテーブルからImport対象テーブルの行のロックを取得する。
     * @param conn コネクション
     * @param importTargetTable Import対象テーブル
     * @return テーブルロック状態(key:テーブル名、value:ジョブフローSID)
     * @throws BulkLoaderSystemException リトライ可能エラー
     */
    private Map<String, String> getImportTableLock(
            Connection conn,
            Iterator<String> importTargetTable) throws BulkLoaderSystemException {
        String selectSql1 = "SELECT TABLE_NAME,JOBFLOW_SID "
            + "FROM IMPORT_TABLE_LOCK "
            + "WHERE TABLE_NAME IN(";
        String selectSql2 = ") FOR UPDATE";
        StringBuilder selectSql = new StringBuilder(selectSql1);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tableCount = 0;

        try {
            while (importTargetTable.hasNext()) {
                // ロック取得のSQLを組み立てる
                selectSql.append("\"");
                selectSql.append(importTargetTable.next());
                selectSql.append("\"");
                tableCount++;
                if (importTargetTable.hasNext()) {
                    selectSql.append(",");
                }
            }
            selectSql.append(selectSql2);

            // トランザクションのロックを取得する
            LOG.info("TG-IMPORTER-02008", selectSql.toString());
            stmt = conn.prepareStatement(selectSql.toString());
            rs = DBConnection.executeQuery(stmt, selectSql.toString(), new String[0]);
            Map<String, String> tableLockStatus = new HashMap<String, String>();
            while (rs.next()) {
                String key = rs.getString("TABLE_NAME");
                String value = rs.getString("JOBFLOW_SID");
                tableLockStatus.put(key, value);
            }

            // IMPORT_TABLE_LOCKのロックをテーブル数分取得できなかった場合は例外をthrowする
            if (tableLockStatus.size() != tableCount) {
                // TODO MessageFormat.formatの検討
                throw new SQLException(
                        "IMPORT_TABLE_LOCKから取得したロック件数がテーブル数と異なる。テーブル数："
                        + tableCount
                        + "、ロック取得数："
                        + tableLockStatus.size());
            }

            return tableLockStatus;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    selectSql.toString(),
                    new String[0]);
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }
    }
    /**
     * Import対象テーブルの一部のレコードにロックフラグが立っているかチェックする。
     * IMPORT_RECORD_LOCKにImport対象テーブルのレコードが存在するか確認する事でチェックを行う
     * @param conn コネクション
     * @param tableName Import 対象テーブル
     * @return チェック結果（ロックフラグが立っていない：true、ロックフラグが立っている：false）
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private boolean checkRecordLock(
            Connection conn,
            String tableName) throws BulkLoaderSystemException {
        String sql = "SELECT JOBFLOW_SID "
            + "FROM IMPORT_RECORD_LOCK "
            + "WHERE TABLE_NAME=?";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableName);
            rs = DBConnection.executeQuery(stmt, sql, new String[] { tableName });
            if (rs.next()) {
                String targetJobflowSid = rs.getString("JOBFLOW_SID");
                return targetJobflowSid == null || targetJobflowSid.isEmpty();
            } else {
                return true;
            }
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql,
                    new String[] { tableName });
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }
    }
    /**
     * Import対象テーブルの対象レコードにロックフラグが立っているかチェックする。
     * @param conn コネクション
     * @param tableName Import対象テーブル
     * @param serchCondition 検索条件
     * @return チェック結果（ロックフラグが立っていない：true、ロックフラグが立っている：false）
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private boolean checkRecordLock(
            Connection conn,
            String tableName,
            String serchCondition) throws BulkLoaderSystemException {
        String rlTableName = DBAccessUtil.createRecordLockTableName(tableName);
        StringBuilder sql = new StringBuilder("SELECT SID FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        if (serchCondition != null && !serchCondition.isEmpty()) {
            sql.append(" (");
            sql.append(serchCondition);
            sql.append(") ");
            sql.append(" AND ");
        }
        sql.append(" EXISTS (SELECT SID FROM ");
        sql.append(rlTableName);
        sql.append(" WHERE ");
        sql.append(rlTableName);
        sql.append(".SID=");
        sql.append(tableName);
        sql.append(".SID)");

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement(sql.toString());
            rs = DBConnection.executeQuery(stmt, sql.toString(), new String[0]);
            boolean hasResult = rs.next();
            return hasResult == false;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql.toString(),
                    new String[0]);
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }
    }
    /**
     * IMPORT_TABLE_LOCKテーブルのImport対象テーブルレコードにロックフラグを立てる。
     * IMPORT_TABLE_LOCKテーブルの指定されたテーブル名のレコードに対して、
     * ジョブフローSIDがnullの場合にのみロックフラグを立てる
     * @param conn コネクション
     * @param tableName Import対象テーブル
     * @param targetJobflowSid ジョブフローSID
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private void tableLock(
            Connection conn,
            String tableName,
            String targetJobflowSid) throws BulkLoaderSystemException {
        String sql = "UPDATE IMPORT_TABLE_LOCK "
            + "SET JOBFLOW_SID=? "
            + "WHERE TABLE_NAME=? AND JOBFLOW_SID IS NULL";
        PreparedStatement stmt = null;

        LOG.info("TG-IMPORTER-02014", sql, targetJobflowSid, tableName);
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, targetJobflowSid);
            stmt.setString(2, tableName);
            DBConnection.executeUpdate(stmt, sql, new String[] { targetJobflowSid, tableName });
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql,
                    new String[] { targetJobflowSid, tableName });
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * Import対象テーブルの対象レコードにロックフラグを立てる。
     * また、LOCKED_TABLEにロックを記録する。
     *
     * @param conn コネクション
     * @param tableName Import対象テーブル
     * @param searchCondition 検索条件
     * @param targetJobflowSid ジョブフローSID
     * @throws BulkLoaderSystemException リトライ不可エラー
     */
    private void recordLock(
            Connection conn,
            String tableName,
            String searchCondition,
            String targetJobflowSid) throws BulkLoaderSystemException {
        String rlTableName = DBAccessUtil.createRecordLockTableName(tableName);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(rlTableName);
        sql.append(" SELECT SID, ? FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        if (searchCondition != null && !searchCondition.equals("")) {
            sql.append("(");
            sql.append(searchCondition);
            sql.append(") AND ");
        }
        sql.append("NOT EXISTS (SELECT SID FROM ");
        sql.append(rlTableName);
        sql.append(" WHERE ");
        sql.append(rlTableName);
        sql.append(".SID=");
        sql.append(tableName);
        sql.append(".SID)");

        LOG.info("TG-IMPORTER-02015", sql.toString(), targetJobflowSid);
        PreparedStatement stmt = null;
        try {
            int count = 0;
            try {
                stmt = conn.prepareStatement(sql.toString());
                stmt.setString(1, targetJobflowSid);
                count = DBConnection.executeUpdate(stmt, sql.toString(), new String[] { targetJobflowSid });
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        sql.toString(),
                        new String[] { targetJobflowSid });
            }

            if (count > 0) {
                String updateSql = null;
                try {
                    // 0件以上レコードロックを取得した場合はIMPORT_RECORD_LOCKにロックを記録する
                    updateSql = "INSERT INTO IMPORT_RECORD_LOCK (JOBFLOW_SID, TABLE_NAME) VALUES(?, ?)";
                    stmt = conn.prepareStatement(updateSql);
                    stmt.setString(1, targetJobflowSid);
                    stmt.setString(2, tableName);
                    DBConnection.executeUpdate(stmt, updateSql, new String[] { targetJobflowSid, tableName });
                } catch (SQLException e) {
                    throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                            e,
                            this.getClass(),
                            updateSql,
                            new String[] { targetJobflowSid, tableName });
                }
            }
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * RUNNING_JOBFLOWSテーブルにレコードをインサートする。
     * Import対象テーブルが存在しない場合に使用する
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobflowId ジョブフローID
     * @param executionId ジョブフロー実行ID
     * @param jobnetEndTime 終了予定時刻
     * @return ジョブフローSID
     */
    public String insertRunningJobFlow(
            String targetName,
            String batchId,
            String jobflowId,
            String executionId,
            Date jobnetEndTime) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            jobflowSid = insertRunningJobFlow(conn, targetName, batchId, jobflowId, executionId, jobnetEndTime);
            DBConnection.commit(conn);
            return jobflowSid;
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            try {
                DBConnection.rollback(conn);
            } catch (BulkLoaderSystemException e1) {
                // ここで例外が発生した場合は握りつぶす
                e1.printStackTrace();
            }
            return null;
        } finally {
            DBConnection.closeConn(conn);
        }
    }

    /**
     * ロック取得時に生成した一連のトランザクションに関するSIDを返す。
     * 当メソッドは以下の何れかのメソッドを実行した後でないと有効な値を返さない。
     * ・lock
     * ・insertRunningJobFlow
     * @return 一連のトランザクションに関するSID (ジョブフローシステムID)
     */
    public String getJobFlowSid() {
        return jobflowSid;
    }
}