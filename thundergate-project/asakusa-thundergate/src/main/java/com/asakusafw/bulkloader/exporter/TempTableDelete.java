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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.ExportTempTableStatus;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * テンポラリテーブルを削除するクラス。
 * @author yuta.shirai
 */
public class TempTableDelete {

    static final Log LOG = new Log(TempTableDelete.class);

    /**
     * テンポラリテーブルとエクスポートテンポラリ管理テーブルのレコードを削除する。
     * @param exportTempTableBean エクスポートテンポラリ管理テーブルの情報を保持するBean
     * @param isDeleteCopyIncomplete エクスポート対象テーブルへのコピーが終了していない場合も削除するか
     * @return テンポラリテーブル削除結果
     */
    public boolean delete(List<ExportTempTableBean> exportTempTableBean, boolean isDeleteCopyIncomplete) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // テンポラリテーブルを削除
            int beanSize = exportTempTableBean.size();
            for (int i = 0; i < beanSize; i++) {
                deleteTempTable(
                        exportTempTableBean.get(i).getTemporaryTableName(),
                        exportTempTableBean.get(i).getDuplicateFlagTableName(),
                        isDeleteCopyIncomplete,
                        conn);
            }
            // エクスポートテンポラリ管理テーブルのレコードを削除
            for (int i = 0; i < beanSize; i++) {
                deleteTempInfoRecord(
                        exportTempTableBean.get(i).getJobflowSid(),
                        exportTempTableBean.get(i).getExportTableName(),
                        isDeleteCopyIncomplete,
                        conn);
            }
            DBConnection.commit(conn);
            return true;
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            try {
                DBConnection.rollback(conn);
            } catch (BulkLoaderSystemException e1) {
                e1.printStackTrace();
            }
            return false;
        } finally {
            DBConnection.closeConn(conn);
        }
    }

    /**
     * エクスポートテンポラリ管理テーブルのレコードを削除する。
     * @param jobflowSid ジョブフローSID
     * @param tableName テーブル名
     * @param isDeleteCopyIncomplete エクスポート対象テーブルへのコピーが終了していない場合も削除するか
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    public void deleteTempInfoRecord(
            String jobflowSid,
            String tableName,
            boolean isDeleteCopyIncomplete,
            Connection conn) throws BulkLoaderSystemException {
        StringBuilder sql = new StringBuilder("DELETE FROM EXPORT_TEMP_TABLE "
                + "WHERE JOBFLOW_SID=? AND TABLE_NAME=?");
        if (!isDeleteCopyIncomplete) {
            sql.append("AND TEMP_TABLE_STATUS=");
            sql.append(ExportTempTableStatus.COPY_EXIT.getStatus());
        }

        PreparedStatement stmt = null;
        try {
            LOG.info("TG-EXPORTER-07001",
                    sql.toString(), jobflowSid, tableName);
            stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, jobflowSid);
            stmt.setString(2, tableName);
            DBConnection.executeUpdate(
                    stmt,
                    sql.toString(),
                    new String[]{ jobflowSid, tableName });
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    DBAccessUtil.class,
                    sql.toString(),
                    new String[] { jobflowSid, tableName });
        } finally {
            DBConnection.closePs(stmt);
        }
    }

    /**
     * テンポラリテーブルを削除する。
     * 指定されたテーブルが存在しない場合は削除されない
     * @param exportTempName テンポラリテーブル名
     * @param duplicateFlagTableName 重複フラグテーブル名
     * @param isDeleteCopyIncomplete エクスポート対象テーブルへのコピーが終了していない場合も削除するか
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    public void deleteTempTable(
            String exportTempName,
            String duplicateFlagTableName,
            boolean isDeleteCopyIncomplete,
            Connection conn) throws BulkLoaderSystemException {
        String checkSql = "SELECT TEMP_TABLE_STATUS "
            + "FROM EXPORT_TEMP_TABLE "
            + "WHERE EXPORT_TEMP_NAME=?";
        StringBuilder tempDelSql = new StringBuilder("DROP TABLE IF EXISTS ");
        tempDelSql.append(exportTempName);
        StringBuilder dupDelSql = new StringBuilder("DROP TABLE IF EXISTS ");
        dupDelSql.append(duplicateFlagTableName);

        if (!isDeleteCopyIncomplete) {
            // ステータスが「'2'：Export対象テーブルにデータをコピー完了」以外の場合削除を行わない
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement(checkSql);
                stmt.setString(1, exportTempName);
                rs = DBConnection.executeQuery(
                        stmt,
                        checkSql,
                        new String[]{ exportTempName });
                if (rs.next()) {
                    ExportTempTableStatus status = ExportTempTableStatus.find(rs.getString("TEMP_TABLE_STATUS"));
                    if (!ExportTempTableStatus.COPY_EXIT.equals(status)) {
                        LOG.info("TG-EXPORTER-07003",
                                exportTempName, status.getStatus());
                        return;
                    }
                }
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        DBAccessUtil.class,
                        checkSql,
                        new String[] { exportTempName });
            } finally {
                DBConnection.closePs(stmt);
                DBConnection.closeRs(rs);
            }
        }

        // テンポラリテーブルを削除
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(tempDelSql.toString());
            DBConnection.executeUpdate(stmt, tempDelSql.toString(), new String[0]);
            LOG.info("TG-EXPORTER-07002", tempDelSql);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    DBAccessUtil.class,
                    tempDelSql.toString(),
                    new String[]{ exportTempName });
        } finally {
            DBConnection.closePs(stmt);
        }
        // 重複フラグテーブルを削除
        try {
            stmt = conn.prepareStatement(dupDelSql.toString());
            DBConnection.executeUpdate(stmt, dupDelSql.toString(), new String[0]);
            LOG.info("TG-EXPORTER-07004",
                    dupDelSql);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    DBAccessUtil.class,
                    dupDelSql.toString(),
                    new String[] { duplicateFlagTableName });
        } finally {
            DBConnection.closePs(stmt);
        }
    }
}
