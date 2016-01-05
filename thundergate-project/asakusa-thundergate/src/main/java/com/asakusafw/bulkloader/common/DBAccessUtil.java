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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * Import処理/Export処理で共通に行うDB操作及びSQL文作成を纏めたユーティリティクラス。
 * トランザクションの管理は当クラスの外で行う。
 * @author yuta.shirai
 */
public final class DBAccessUtil {

    static final Log LOG = new Log(DBAccessUtil.class);

    // TODO 各種定数の括り出し

    private DBAccessUtil() {
        return;
    }

    /**
     * Import/Export対象テーブル名からロック済みレコードテーブル名を組み立てる。
     * @param tableName Import/Export対象テーブル名
     * @return ロック済みレコードテーブル名
     */
    public static String createRecordLockTableName(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.append("_RL");
        return sb.toString();
    }
    /**
     * Exportテンポラリテーブル名から重複フラグテーブル名を組み立てる。
     * @param tempTableName Exportテンポラリテーブル名
     * @return 重複フラグテーブル名
     */
    public static String createDuplicateFlgTableName(String tempTableName) {
        StringBuilder sb = new StringBuilder(tempTableName);
        sb.append(Constants.DUPLECATE_FLG_TABLE_END);
        return sb.toString();
    }
    /**
     * ジョブフロー実行IDからジョブフローSIDを検索する。
     * @param executionId ジョブフロー実行ID
     * @return String ジョブフローSID
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    public static String selectJobFlowSid(String executionId) throws BulkLoaderSystemException {
        String sql = "SELECT JOBFLOW_SID "
            + "FROM RUNNING_JOBFLOWS "
            + "WHERE EXECUTION_ID=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String jobflowSid = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, executionId);
            rs = DBConnection.executeQuery(stmt, sql, new String[]{executionId});
            if (rs.next()) {
                jobflowSid = rs.getString("JOBFLOW_SID");
            }
            return jobflowSid;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    DBAccessUtil.class,
                    sql,
                    executionId);
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
            DBConnection.closeConn(conn);
        }
    }
    /**
     * ジョブフロー実行テーブルを検索する。
     * 引数にジョブフロー実行IDが指定されている場合は
     * ジョブフロー実行IDを条件に検索を行い、
     * 指定されていない場合は全件を検索する。
     * @param executionId ジョブフロー実行ID
     * @return ジョブフロー実行テーブル, never {@code null}
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    public static List<ExporterBean> selectRunningJobFlow(String executionId) throws BulkLoaderSystemException {
        boolean hasCondition = false;
        if (executionId != null) {
            hasCondition = true;
        }
        String sql = "SELECT JOBFLOW_SID,BATCH_ID,JOBFLOW_ID,TARGET_NAME,EXECUTION_ID "
            + "FROM RUNNING_JOBFLOWS";
        if (hasCondition) {
            sql = sql + " WHERE EXECUTION_ID=?";
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            if (hasCondition) {
                stmt.setString(1, executionId);
            }
            rs = DBConnection.executeQuery(stmt, sql, new String[] { executionId });
            List<ExporterBean> beanList = new ArrayList<ExporterBean>();
            while (rs.next()) {
                ExporterBean bean = new ExporterBean();
                bean.setJobflowSid(rs.getString("JOBFLOW_SID"));
                bean.setBatchId(rs.getString("BATCH_ID"));
                bean.setJobflowId(rs.getString("JOBFLOW_ID"));
                bean.setTargetName(rs.getString("TARGET_NAME"));
                bean.setExecutionId(rs.getString("EXECUTION_ID"));
                beanList.add(bean);
            }
            return beanList;
        } catch (SQLException e) {
            if (hasCondition) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        DBAccessUtil.class,
                        sql,
                        executionId);
            } else {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        DBAccessUtil.class,
                        sql);
            }
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
            DBConnection.closeConn(conn);
        }
    }
    /**
     * ジョブフローSIDをもとにエクスポートテンポラリ管理テーブルの情報を取得して返す。
     * @param jobflowSid ジョブフローSID
     * @return エクスポートテンポラリ管理テーブルの情報
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    public static List<ExportTempTableBean> getExportTempTable(String jobflowSid) throws BulkLoaderSystemException {
        String sql = "SELECT "
            + "TABLE_NAME,EXPORT_TEMP_NAME,DUPLICATE_FLG_NAME,TEMP_TABLE_STATUS "
            + "FROM EXPORT_TEMP_TABLE WHERE JOBFLOW_SID=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<ExportTempTableBean> beanList = new ArrayList<ExportTempTableBean>();
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, jobflowSid);
            rs = DBConnection.executeQuery(stmt, sql, new String[]{jobflowSid});
            while (rs.next()) {
                ExportTempTableBean bean = new ExportTempTableBean();
                bean.setJobflowSid(jobflowSid);
                bean.setExportTableName(rs.getString("TABLE_NAME"));
                bean.setTemporaryTableName(rs.getString("EXPORT_TEMP_NAME"));
                bean.setDuplicateFlagTableName(rs.getString("DUPLICATE_FLG_NAME"));
                bean.setTempTableStatus(ExportTempTableStatus.find(rs.getString("TEMP_TABLE_STATUS")));
                beanList.add(bean);
            }
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    DBAccessUtil.class,
                    sql,
                    jobflowSid);
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
            DBConnection.closeConn(conn);
        }

        return beanList;
    }
    /**
     * カラム名のリストからExport対象テーブルのシステムカラム名を取り除く。
     * @param tableColumn カラム名のリスト
     * @return システムカラムを取り除いたカラム名のリスト
     */
    public static List<String> delSystemColumn(List<String> tableColumn) {
        return delColumn(tableColumn, Constants.getSystemColumns());
    }
    /**
     * カラム名のリストから異常データテーブルのシステムカラム名とエラーコードを格納するカラム名を取り除く。
     * @param tableColumn カラム名のリスト
     * @param errorCodeColumn エラーコードを格納するカラム名
     * @return システムカラムを取り除いたカラム名のリスト
     */
    public static List<String> delErrorSystemColumn(List<String> tableColumn, String errorCodeColumn) {
        List<String> errorSystemColumns = Constants.getErrorSystemColumns();
        List<String> sysColumn = new ArrayList<String>(errorSystemColumns);
        sysColumn.add(errorCodeColumn);
        return delColumn(tableColumn, sysColumn);
    }
    /**
     * 配列から特定の要素を取り除く。
     * @param tableColumn 配列
     * @param delColumn 取り除く要素
     * @return 特定の要素を取り除いた配列
     */
    private static List<String> delColumn(List<String> tableColumn, List<String> delColumn) {
        List<String> resultList = new ArrayList<String>();

        // カラム名のリストからシステムカラムを取り除く
        int tableCount = tableColumn.size();
        for (int i = 0; i < tableCount; i++) {
            boolean isSystemColumn = false;
            int columnCount = delColumn.size();
            for (int j = 0; j < columnCount; j++) {
                if (delColumn.get(j).equals(tableColumn.get(i))) {
                    isSystemColumn = true;
                    break;
                }
            }
            if (!isSystemColumn) {
                resultList.add(tableColumn.get(i));
            }
        }
        return resultList;
    }
    /**
     * 配列をカンマ区切りにして返す。
     * 配列がnull又は0配列の場合はnullを返す
     * @param columnArray カラム名の配列
     * @return カンマ区切りにしたカラム名の一覧、空の配列を指定した場合は{@code null}
     */
    public static String joinColumnArray(List<String> columnArray) {
        if (columnArray == null || columnArray.size() == 0) {
            return null;
        }
        StringBuilder column = new StringBuilder();
        int columnSize = columnArray.size();
        for (int i = 0; i < columnSize; i++) {
            column.append(columnArray.get(i));
            if (i + 1 < columnSize) {
                column.append(",");
            }
        }
        return column.toString();
    }
    /**
     * 同一ジョブフロー実行IDでImporter/Exporter/Recovererが同時に動作しない為のロックを取得する。
     * 当メソッドに渡すコネクションは、ロック専用のコネクションでなくてはならない。
     * @param executionId ジョブフロー実行ID
     * @param conn コネクション
     * @return 排他ロックを取得できたかどうか
     */
    public static boolean getJobflowInstanceLock(String executionId, Connection conn) {
        String sql = "INSERT INTO JOBFLOW_INSTANCE_LOCK (EXECUTION_ID) VALUES(?)";
        PreparedStatement stmt = null;
        LOG.info("TG-COMMON-00020", sql, executionId);
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, executionId);
            DBConnection.executeUpdate(stmt, sql, executionId);
            return true;
        } catch (SQLException e) {
            try {
                DBConnection.rollback(conn);
            } catch (BulkLoaderSystemException e1) {
                e1.printStackTrace();
            }
            return false;
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * 同一ジョブフロー実行IDでImporter/Exporter/Recovererが同時に動作しない為の
     * ロックを解除する。
     * 当メソッドに渡すコネクションは、ロック専用のコネクションでなくてはならない。
     * @param conn コネクション
     */
    public static void releaseJobflowInstanceLock(Connection conn) {
        LOG.info("TG-COMMON-00021");
        try {
            DBConnection.rollback(conn);
        } catch (BulkLoaderSystemException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConn(conn);
        }
    }
    /**
     * SELECT INTO OUTFILE文 及び LOAD DATA INFILE文のTSVファイルフォーマット仕様を返す。
     * @return TSV仕様
     */
    public static String getTSVFileFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(" FIELDS TERMINATED BY '\\t'");
        sb.append(" ENCLOSED BY ''");
        sb.append(" ESCAPED BY '\\\\'");
        sb.append(" LINES STARTING BY ''");
        sb.append(" TERMINATED BY '\\n'");
        return sb.toString();
    }
}
