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

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.ExportTempTableStatus;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;


/**
 * エクスポートテンポラリテーブルからデータをコピーするクラス。
 * @author yuta.shirai
 *
 */
public class ExportDataCopy {

    static final Log LOG = new Log(ExportDataCopy.class);

    /**
     * 更新レコードのコピーが全て終了したかを表すフラグ。
     */
    private boolean copyEnd = true;

    /**
     * エクスポートテンポラリテーブルからデータをコピーする。
     * @param bean パラメータを保持するBean
     * @return コピー結果（true:成功、false:失敗（UPDATEが全て成功していなくてもtrueを返す。UPDATEの結果はupdateEndを参照する））
     */
    public boolean copyData(ExporterBean bean) {
        long maxRecord = Long.parseLong(ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD));
        Connection conn = null;
        try {
            // コネクションを取得する
            conn = DBConnection.getConnection();

            // テンポラリ管理テーブルの情報を取得する。
            List<ExportTempTableBean> tempBean = DBAccessUtil.getExportTempTable(bean.getJobflowSid());

            // Export対象テーブル分繰り返す
            List<String> l = bean.getExportTargetTableList();
            for (String tableName : l) {
                ExportTargetTableBean expTableBean = bean.getExportTargetTable(tableName);

                LOG.info("TG-EXPORTER-06002",
                        bean.getJobflowSid(), tableName, expTableBean.getExportTempTableName());

                // TODO 外側からO(N^2)となるのでやや気になる
                if  (isCopyEnd(tempBean, expTableBean, tableName)) {
                    // 当該テーブルのコピーが完了している場合はコピーを行わない。
                    LOG.info("TG-EXPORTER-06004",
                            bean.getJobflowSid(), tableName, expTableBean.getExportTempTableName());
                    continue;
                }
                if (expTableBean.getExportTempTableName() == null) {
                    // エクスポートテンポラリテーブルが存在しない場合はコピーを行わない
                    LOG.info("TG-EXPORTER-06005",
                            bean.getJobflowSid(), tableName, expTableBean.getExportTempTableName());
                    continue;
                }

                // 新規レコードにレコードロックを取得するかを判定
                boolean isGetRecordLock = getRecordLock(bean.getJobflowSid(), tableName, conn);

                // 新規レコードのコピー（重複していないデータ）
                copyNonDuplicateData(expTableBean, tableName, maxRecord, bean.getJobflowSid(), isGetRecordLock, conn);
                if (expTableBean.isDuplicateCheck()) {
                    // 新規レコードのコピー（重複していいるデータ（重複チェックを行う場合のみ））
                    copyDuplicateData(expTableBean, maxRecord, conn);
                }
                // 更新レコードのコピー
                boolean tableCopyEnd = copyUpdateData(expTableBean, tableName, maxRecord, bean.getJobflowSid(), conn);
                if (tableCopyEnd) {
                    // コピー完了を記録
                    copyExit(bean.getJobflowSid(), tableName, conn);
                } else {
                    copyEnd = false;
                }
                LOG.info("TG-EXPORTER-06003",
                        bean.getJobflowSid(), tableName, expTableBean.getExportTempTableName(), tableCopyEnd);
            }
            return true;
        } catch (BulkLoaderSystemException e) {
            try {
                DBConnection.rollback(conn);
            } catch (BulkLoaderSystemException e1) {
                LOG.log(e);
            }
            LOG.log(e);
            return false;
        } finally {
            DBConnection.closeConn(conn);
        }
    }
    /**
     * 当該テーブルのコピーが終了しているかを判断する。
     * @param tempBeans テンポラリ管理テーブルの情報
     * @param tableBean パラメータを保持するBean
     * @param tableName テーブル名
     * @return コピーが完了している場合trueを返す
     */
    private boolean isCopyEnd(
            List<ExportTempTableBean> tempBeans,
            ExportTargetTableBean tableBean,
            String tableName) {
        if (tempBeans == null || tempBeans.size() == 0) {
            return false;
        } else {
            for (ExportTempTableBean tempBean : tempBeans) {
                if (tempBean.getExportTableName().equals(tableName)) {
                    if (tableBean.getExportTempTableName() == null) {
                        // 再実行でLoadを行っていない場合はテンポラリテーブル名をセットする
                        tableBean.setExportTempTableName(tempBean.getTemporaryTableName());
                        tableBean.setDuplicateFlagTableName(tempBean.getDuplicateFlagTableName());
                    }
                    boolean completed = ExportTempTableStatus.COPY_EXIT.equals(
                            tempBean.getTempTableStatus());
                    return completed;
                }
            }
        }
        return false;
    }
    /**
     * テンポラリ管理テーブルにコピー完了を記録する。
     * @param jobflowSid ジョブフローSID
     * @param tableName テーブル名
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void copyExit(
            String jobflowSid,
            String tableName,
            Connection conn) throws BulkLoaderSystemException {
        // ロード完了を記録するSQL
        String loadExitSql = "UPDATE EXPORT_TEMP_TABLE "
            + "SET TEMP_TABLE_STATUS=? "
            + "WHERE JOBFLOW_SID=? AND TABLE_NAME=?";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(loadExitSql);
            stmt.setString(1, ExportTempTableStatus.COPY_EXIT.getStatus());
            stmt.setString(2, jobflowSid);
            stmt.setString(3, tableName);
            DBConnection.executeUpdate(
                    stmt,
                    loadExitSql,
                    new String[] { ExportTempTableStatus.COPY_EXIT.getStatus(), jobflowSid, tableName });
            DBConnection.commit(conn);
            LOG.info("TG-EXPORTER-06011", jobflowSid, tableName);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e, this.getClass(), loadExitSql,
                    new String[] { ExportTempTableStatus.COPY_EXIT.getStatus(), jobflowSid, tableName });
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * 重複データを異常データテーブルにコピー（Insert）する。
     * コピーしたデータはエクスポートテンポラリテーブルから削除する。
     * @param expTableBean Export対象テーブルの設定を保持するBean
     * @param maxRecord コピーの最大レコード件数
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void copyDuplicateData(
            ExportTargetTableBean expTableBean,
            long maxRecord,
            Connection conn) throws BulkLoaderSystemException {

        // 検索条件を作成
        String selectCondition = createDupSelectCondition(expTableBean, maxRecord);
        // コピーのSQLを作成
        String copySql = createDupInsertSql(expTableBean, selectCondition);
        // 削除のSQLを作成
        String delSql = createDupCopyDelSql(expTableBean, selectCondition);

        PreparedStatement stmt = null;

        while (true) {
            // データをコピー
            int copyCount = 0;
            try {
                stmt = conn.prepareStatement(copySql.toString());
                copyCount = DBConnection.executeUpdate(stmt, copySql.toString());
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        copySql.toString(),
                        new String[0]);
            } finally {
                DBConnection.closePs(stmt);
            }

            // コピーするレコードが存在しなくなった場合はコミットして終了
            if (copyCount == 0) {
                DBConnection.commit(conn);
                break;
            }

            // コピーしたレコードを削除
            try {
                stmt = conn.prepareStatement(delSql);
                DBConnection.executeUpdate(stmt, delSql, new String[0]);
                DBConnection.commit(conn);
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        delSql,
                        new String[0]);
            } finally {
                DBConnection.closePs(stmt);
            }

            LOG.info("TG-EXPORTER-06008",
                    expTableBean.getErrorTableName(),
                    expTableBean.getExportTempTableName(),
                    copySql.toString(),
                    delSql);
        }
    }
    /**
     * コピーした重複データを削除のSQLを作成する。
     * @param expTableBean Export対象テーブルの設定を保持する
     * @param selectCondition 検索条件
     * @return 削除のSQL
     */
    private String createDupCopyDelSql(ExportTargetTableBean expTableBean,
            String selectCondition) {
        StringBuilder delSqll = new StringBuilder("DELETE FROM ");
        delSqll.append(expTableBean.getExportTempTableName());
        delSqll.append(selectCondition);
        return delSqll.toString();
    }
    /**
     * 重複データをコピーするSQLを生成する。
     * @param expTableBean Export対象テーブルの設定を保持する
     * @param selectCondition 検索条件
     * @return 重複データをコピーするSQL
     */
    private String createDupInsertSql(ExportTargetTableBean expTableBean,
            String selectCondition) {
        List<String> columnList = DBAccessUtil.delErrorSystemColumn(
                expTableBean.getErrorTableColumns(),
                expTableBean.getErrorCodeColumn());
        String column = DBAccessUtil.joinColumnArray(columnList);

        StringBuilder copySql = new StringBuilder("INSERT INTO ");
        copySql.append(expTableBean.getErrorTableName());
        copySql.append(" (");
        copySql.append(column);
        copySql.append(",");
        copySql.append(Constants.getRegisteredDateTimeColumnName());
        copySql.append(",");
        copySql.append(Constants.getUpdatedDateTimeColumnName());
        copySql.append(",");
        copySql.append(expTableBean.getErrorCodeColumn());
        copySql.append(") SELECT ");
        copySql.append(column);
        copySql.append(",NOW(),NOW(),'");
        copySql.append(expTableBean.getErrorCode());
        copySql.append("' FROM ");
        copySql.append(expTableBean.getExportTempTableName());
        copySql.append(selectCondition);
        return copySql.toString();
    }
    /**
     * 重複データコピーの検索条件を作成する。
     * @param expTableBean Export対象テーブルの設定を保持するBean
     * @param maxRecord コピーの最大レコード件数
     * @return 検索条件
     */
    private String createDupSelectCondition(ExportTargetTableBean expTableBean,
            long maxRecord) {
        StringBuilder selectCondition = new StringBuilder(" WHERE ");
        selectCondition.append(Constants.getSidColumnName());
        selectCondition.append(" IS NULL AND EXISTS (SELECT ");
        selectCondition.append(expTableBean.getDuplicateFlagTableName());
        selectCondition.append(".");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append(" FROM ");
        selectCondition.append(expTableBean.getDuplicateFlagTableName());
        selectCondition.append(" WHERE ");
        selectCondition.append(expTableBean.getDuplicateFlagTableName());
        selectCondition.append(".");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append("=");
        selectCondition.append(expTableBean.getExportTempTableName());
        selectCondition.append(".");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append(")");
        selectCondition.append(" ORDER BY ");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append(" LIMIT ");
        selectCondition.append(maxRecord);
        return selectCondition.toString();
    }

    // CHECKSTYLE:OFF MethodLengthCheck - FIXME refactoring
    /**
     * 更新データをExport対象テーブルにコピーする。
     * テーブルロックを取得していない場合、コピーしたデータは行ロック状態に更新する。
     * また、コピーしたデータはエクスポートテンポラリテーブルから削除する。
     * @param tableBean Export対象テーブルの設定を保持するBean
     * @param tableName Export対象テーブル名
     * @param conn コネクション
     * @param maxRecord 毎回のコピーの最大レコード件数
     * @param jobflowSid ジョブフローSID
     * @return 全てのレコードをコピーした場合：true、更新対象レコードが見つからなかった場合：false
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private boolean copyUpdateData(
            ExportTargetTableBean tableBean,
            String tableName,
            long maxRecord,
            String jobflowSid,
            Connection conn) throws BulkLoaderSystemException {

        String tempTableName = tableBean.getExportTempTableName();

        // テンポラリSIDの最小値を調べるSQL
        String minTempSidSql = createMinTempSidSql(Constants.getTemporarySidColumnName(), tempTableName);
        // テンポラリSIDの最大値を調べるSQL
        String maxTempSidSql = createMaxTempSidSql(Constants.getTemporarySidColumnName(), tempTableName);
        // テンポラリテーブルのレコード件数を調べるSQL
        String countTempSql = createCountTempSql(tempTableName);
        // 検索条件を作成
        String selectCondition = createUpdateSelectCondition(tableName, tempTableName);
        // コピーのSQLを作成
        String copySql = createUpdateCopySql(
                tableName, tempTableName, selectCondition, tableBean.getExportTableColumns());
        // 削除のSQLを作成
        String delSql = createUpdateRecordDelSql(tableName, tempTableName);
        // エラーになったレコードを取得するSQLを作成
        String errTempSql = createSelectErrTempRecordSql(tempTableName);

        PreparedStatement stmt = null;

        // テンポラリSIDの最小値を取得
        long minTempSid = 0;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(minTempSidSql);
            rs = DBConnection.executeQuery(stmt, minTempSidSql, new String[0]);
            rs.next();
            minTempSid =  rs.getLong(1);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    minTempSidSql,
                    new String[0]);
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }

        // テンポラリSIDの最大値を取得
        long maxTempSid = 0;
        try {
            stmt = conn.prepareStatement(maxTempSidSql);
            rs = DBConnection.executeQuery(stmt, maxTempSidSql, new String[0]);
            rs.next();
            maxTempSid =  rs.getLong(1);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    maxTempSidSql,
                    new String[0]);
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }

        // 現在のテンポラリSIDの位置を表す変数
        long currentCount = minTempSid;

        while (true) {
            Long maxCount = currentCount + maxRecord;

            // データをコピー
            int copyCount = 0;
            try {
                stmt = conn.prepareStatement(copySql);
                stmt.setLong(1, currentCount);
                stmt.setLong(2, maxCount);
                copyCount = DBConnection.executeUpdate(
                        stmt,
                        copySql,
                        new String[] { String.valueOf(currentCount), String.valueOf(maxCount) });
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        copySql,
                        new String[] { String.valueOf(currentCount), String.valueOf(maxCount) });
            } finally {
                DBConnection.closePs(stmt);
            }

            if (copyCount > 0) {
                // コピーしたレコードを削除
                try {
                    stmt = conn.prepareStatement(delSql);
                    stmt.setLong(1, currentCount);
                    stmt.setLong(2, maxCount);
                    DBConnection.executeUpdate(
                            stmt,
                            delSql,
                            new String[] { String.valueOf(currentCount), String.valueOf(maxCount) });
                    DBConnection.commit(conn);
                } catch (SQLException e) {
                    throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                            e,
                            this.getClass(),
                            delSql,
                            new String[] { String.valueOf(currentCount), String.valueOf(maxCount) });
                } finally {
                    DBConnection.closePs(stmt);
                }
            }
            LOG.info("TG-EXPORTER-06009",
                    tableName,
                    tableBean.getExportTempTableName(),
                    copySql,
                    delSql,
                    currentCount,
                    maxCount);

            // カレントのテンポラリSIDがテンポラリSIDの最大値を超えた場合終了する。
            currentCount = maxCount + 1;
            if (currentCount > maxTempSid) {
                DBConnection.commit(conn);
                break;
            }
        }

        // テンポラリテーブルの件数を取得
        long tempCount = 0;
        try {
            stmt = conn.prepareStatement(countTempSql);
            rs = DBConnection.executeQuery(stmt, countTempSql, new String[0]);
            rs.next();
            tempCount = rs.getLong(1);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    countTempSql,
                    new String[0]);
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }

        if (tempCount == 0) {
            LOG.info("TG-EXPORTER-06010",
                    tableName,
                    tableBean.getExportTempTableName());
            return true;
        } else {
            // エラーになったレコードをログに出力する
            StringBuilder errSid = new StringBuilder();
            try {
                stmt = conn.prepareStatement(errTempSql);
                rs = DBConnection.executeQuery(stmt, errTempSql, new String[0]);
                while (rs.next()) {
                    errSid.append(Constants.getSidColumnName());
                    errSid.append("=");
                    errSid.append(rs.getLong(Constants.getSidColumnName()));
                    errSid.append(",");
                    errSid.append(Constants.getTemporarySidColumnName());
                    errSid.append("=");
                    errSid.append(rs.getLong(Constants.getTemporarySidColumnName()));
                    errSid.append(" ");
                }
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        errTempSql.toString(),
                        new String[0]);
            } finally {
                DBConnection.closeRs(rs);
                DBConnection.closePs(stmt);
            }
            LOG.error("TG-EXPORTER-06001",
                    tableName,
                    tempTableName,
                    errSid.toString());
            return false;
        }
    }
    // CHECKSTYLE:ON MethodLengthCheck

    /**
     * 更新レコードコピーでエラーになったレコードを取得するSQLを作成する。
     * @param tempTableName テンポラリテーブル名
     * @return エラーになったレコードを取得するSQL
     */
    private String createSelectErrTempRecordSql(String tempTableName) {
        StringBuilder errTempSql = new StringBuilder("SELECT ");
        errTempSql.append(Constants.getTemporarySidColumnName());
        errTempSql.append(",");
        errTempSql.append(Constants.getSidColumnName());
        errTempSql.append(" FROM ");
        errTempSql.append(tempTableName);
        return errTempSql.toString();
    }
    /**
     * コピーした更新レコードを削除するSQLを作成する。
     * @param tableName テーブル名
     * @param tempTableName テンポラリテーブル名
     * @return 削除のSQL
     */
    private String createUpdateRecordDelSql(String tableName, String tempTableName) {
        StringBuilder delSql = new StringBuilder("DELETE FROM ");
        delSql.append(tempTableName);
        delSql.append(" WHERE EXISTS (SELECT ");
        delSql.append(Constants.getTemporarySidColumnName());
        delSql.append(" FROM ");
        delSql.append(tableName);
        delSql.append(" WHERE ");
        delSql.append(tableName);
        delSql.append(".");
        delSql.append(Constants.getSidColumnName());
        delSql.append("=");
        delSql.append(tempTableName);
        delSql.append(".");
        delSql.append(Constants.getSidColumnName());
        delSql.append(") AND ");
        delSql.append(Constants.getSidColumnName());
        delSql.append(" IS NOT NULL AND ");
        delSql.append(Constants.getTemporarySidColumnName());
        delSql.append(" BETWEEN ? AND ?");
        return delSql.toString();
    }
    /**
     * 更新レコードをコピーするSQLを作成する。
     * @param tableName テーブル名
     * @param tempTableName テンポラリテーブル名
     * @param selectCondition 検索条件
     * @param exportTableColumns エクスポートするカラム名
     * @return コピーのSQL
     */
    private String createUpdateCopySql(
            String tableName,
            String tempTableName,
            String selectCondition,
            List<String> exportTableColumns) {
        List<String> updateColumnList = DBAccessUtil.delSystemColumn(exportTableColumns);
        StringBuilder copySql = new StringBuilder("UPDATE ");
        copySql.append(tableName);
        copySql.append(",");
        copySql.append(tempTableName);
        copySql.append(" SET ");
        // SIDを設定
        copySql.append(tableName);
        copySql.append(".");
        copySql.append(Constants.getSidColumnName());
        copySql.append("=");
        copySql.append(tempTableName);
        copySql.append(".");
        copySql.append(Constants.getSidColumnName());
        copySql.append(",");
        // バージョン番号を設定
        copySql.append(tableName);
        copySql.append(".");
        copySql.append(Constants.getVersionColumnName());
        copySql.append("=");
        copySql.append(tableName);
        copySql.append(".");
        copySql.append(Constants.getVersionColumnName());
        copySql.append("+");
        copySql.append(Constants.SYS_COLUMN_INCREMENT_VERSION_NO);
        copySql.append(",");
        // 更新日時を設定
        copySql.append(tableName);
        copySql.append(".");
        copySql.append(Constants.getUpdatedDateTimeColumnName());
        copySql.append("=NOW(),");
        // 業務カラムを設定
        int updateColumnSize = updateColumnList.size();
        for (int i = 0; i < updateColumnSize; i++) {
            copySql.append(tableName);
            copySql.append(".");
            copySql.append(updateColumnList.get(i));
            copySql.append("=");
            copySql.append(tempTableName);
            copySql.append(".");
            copySql.append(updateColumnList.get(i));
            if (i + 1 < updateColumnSize) {
                copySql.append(",");
            }
        }
        copySql.append(selectCondition);
        return copySql.toString();
    }
    /**
     * 更新レコードコピーの検索条件を作成する。
     * @param tableName テーブル名
     * @param tempTableName テンポラリテーブル名
     * @return 検索条件
     */
    private String createUpdateSelectCondition(String tableName, String tempTableName) {
        StringBuilder selectCondition = new StringBuilder(" WHERE ");
        selectCondition.append(tableName);
        selectCondition.append(".");
        selectCondition.append(Constants.getSidColumnName());
        selectCondition.append("=");
        selectCondition.append(tempTableName);
        selectCondition.append(".");
        selectCondition.append(Constants.getSidColumnName());
        selectCondition.append(" AND ");
        selectCondition.append(tempTableName);
        selectCondition.append(".");
        selectCondition.append(Constants.getSidColumnName());
        selectCondition.append(" IS NOT NULL AND ");
        selectCondition.append(tempTableName);
        selectCondition.append(".");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append(" BETWEEN ? AND ?");
        return selectCondition.toString();
    }
    /**
     * テンポラリテーブルのレコード件数を調べるSQLを生成する。
     * @param tempTableName テンポラリテーブル名
     * @return テンポラリテーブルのレコード件数を調べるSQL
     */
    private String createCountTempSql(String tempTableName) {
        StringBuilder countTempSql = new StringBuilder("SELECT COUNT(*) FROM ");
        countTempSql.append(tempTableName);
        return countTempSql.toString();
    }
    /**
     * テンポラリSIDの最大値を調べるSQLを生成する。
     * @param temporarySidColumnName テンポラリSIDのカラム名
     * @param tempTableName テンポラリテーブル名
     * @return テンポラリSIDの最大値を調べるSQL
     */
    private String createMaxTempSidSql(String temporarySidColumnName,
            String tempTableName) {
        StringBuilder maxTempSidSql = new StringBuilder("SELECT MAX(");
        maxTempSidSql.append(Constants.getTemporarySidColumnName());
        maxTempSidSql.append(") FROM ");
        maxTempSidSql.append(tempTableName);
        return maxTempSidSql.toString();
    }
    /**
     * テンポラリSIDの最小値を調べるSQLを生成する。
     * @param temporarySidColumnName テンポラリSIDのカラム名
     * @param tempTableName テンポラリテーブル名
     * @return テンポラリSIDの最小値を調べるSQL
     */
    private String createMinTempSidSql(String temporarySidColumnName, String tempTableName) {
        StringBuilder minTempSidSql = new StringBuilder("SELECT MIN(");
        minTempSidSql.append(Constants.getTemporarySidColumnName());
        minTempSidSql.append(") FROM ");
        minTempSidSql.append(tempTableName);
        return minTempSidSql.toString();
    }
    /**
     * 新規データをExport対象テーブルにコピー（Insert）する。
     * テーブルロックを取得していない場合、コピーしたデータは行ロック状態に更新する。
     * また、コピーしたデータはエクスポートテンポラリテーブルから削除する。
     * @param expTableBean Export対象テーブルの設定を保持するBean
     * @param tableName Export対象テーブル名
     * @param conn コネクション
     * @param maxRecord コピーの最大レコード件数
     * @param jobflowSid ジョブフローSID
     * @param isGetRecordLock レコードロックを取得するか
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void copyNonDuplicateData(
            ExportTargetTableBean expTableBean,
            String tableName,
            long maxRecord,
            String jobflowSid,
            boolean isGetRecordLock,
            Connection conn) throws BulkLoaderSystemException {

        String recordLockSql = null;
        // 検索条件を作成
        String selectCondition = createInsertselectcondition(expTableBean, maxRecord);
        // コピーのSQLを作成
        String copySql = createInsertCopySql(tableName, expTableBean, selectCondition);

        // 削除のSQLを作成
        String delSql = createInsertDelSql(expTableBean, selectCondition);

        PreparedStatement stmt = null;

        while (true) {
            // データをコピー
            int copyCount = 0;
            try {
                stmt = conn.prepareStatement(copySql.toString());
                copyCount = DBConnection.executeUpdate(stmt, copySql.toString(), new String[0]);
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        copySql.toString(),
                        new String[0]);
            } finally {
                DBConnection.closePs(stmt);
            }

            // コピーするレコードが存在しなくなった場合はコミットして終了
            if (copyCount == 0) {
                DBConnection.commit(conn);
                break;
            }

            // コピーしたレコードにレコードロックフラグを立てる
            if (isGetRecordLock) {
                // コピーしたデータのSIDを取得するSQL
                String selectSidSql = "SELECT LAST_INSERT_ID()";

                // ユーザー変数を設定するSQL
                StringBuilder userParam = new StringBuilder("@EXPORT_");
                userParam.append(tableName);
                userParam.append("_SID");
                String setUserParamSql = createSetUserParamSql(tableName, userParam);

                // レコードロックフラグを立てるSQL
                recordLockSql = createRecordLockSql(tableName, expTableBean, jobflowSid, userParam, selectCondition);

                // コピーしたレコードのSIDを取得
                ResultSet rs = null;
                String sid = null;
                try {
                    stmt = conn.prepareStatement(selectSidSql);
                    rs = DBConnection.executeQuery(stmt, selectSidSql, new String[0]);
                    rs.next();
                    sid =  rs.getString(1);
                } catch (SQLException e) {
                    throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                            e, this.getClass(), selectSidSql, new String[0]);
                } finally {
                    DBConnection.closeRs(rs);
                    DBConnection.closePs(stmt);
                }

                // ユーザー変数をセットする
                try {
                    stmt = conn.prepareStatement(setUserParamSql);
                    long param = Long.parseLong(sid) - 1L;
                    stmt.setLong(1, param);
                    copyCount = DBConnection.executeUpdate(
                            stmt,
                            setUserParamSql,
                            new String[]{ String.valueOf(param) });
                } catch (SQLException e) {
                    throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                            e, this.getClass(), setUserParamSql, new String[] { sid });
                } finally {
                    DBConnection.closePs(stmt);
                }
                // ロックフラグを立てる
                // Import時にレコードロックを取得している場合のみレコードロックを取得する
                try {
                    stmt = conn.prepareStatement(recordLockSql.toString());
                    copyCount = DBConnection.executeUpdate(
                            stmt,
                            recordLockSql.toString(),
                            new String[0]);
                } catch (SQLException e) {
                    throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                            e, this.getClass(), recordLockSql.toString(), new String[0]);
                } finally {
                    DBConnection.closePs(stmt);
                }
            }

            // コピーしたレコードを削除
            try {
                stmt = conn.prepareStatement(delSql);
                DBConnection.executeUpdate(stmt, delSql, new String[0]);
                DBConnection.commit(conn);
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e, this.getClass(), delSql, new String[0]);
            } finally {
                DBConnection.closePs(stmt);
            }
            LOG.info("TG-EXPORTER-06007",
                    tableName,
                    expTableBean.getExportTempTableName(),
                    copySql,
                    recordLockSql,
                    delSql);
        }
    }
    /**
     * レコードロックフラグを立てるSQLを生成する。
     * @param tableName テーブル名
     * @param expTableBean Export対象テーブルの設定を保持するBean
     * @param jobflowSid ジョブフローSID
     * @param userParam ユーザー変数
     * @param selectCondition 検索条件
     * @return レコードロックフラグを立てるSQL
     */
    private String createRecordLockSql(String tableName,
            ExportTargetTableBean expTableBean, String jobflowSid, StringBuilder userParam, String selectCondition) {
        String rlTableName = DBAccessUtil.createRecordLockTableName(tableName);
        StringBuilder recordLockSql = new StringBuilder();
        recordLockSql.append("INSERT INTO ");
        recordLockSql.append(rlTableName);
        recordLockSql.append(" (");
        recordLockSql.append(Constants.getSidColumnName());
        recordLockSql.append(",JOBFLOW_SID) SELECT ");
        recordLockSql.append(userParam);
        recordLockSql.append(":=");
        recordLockSql.append(userParam);
        recordLockSql.append("+1,");
        recordLockSql.append(jobflowSid);
        recordLockSql.append(" FROM ");
        recordLockSql.append(expTableBean.getExportTempTableName());
        recordLockSql.append(selectCondition);
        return recordLockSql.toString();
    }
    /**
     * ユーザー変数を設定するSQLを生成する。
     * @param tableName テーブル名
     * @param userParam ユーザー変数
     * @return ユーザー変数を設定するSQL
     */
    private String createSetUserParamSql(String tableName,
            StringBuilder userParam) {
        StringBuilder setUserParamSql = new StringBuilder("SET ");
        setUserParamSql.append(userParam);
        setUserParamSql.append("=?");
        return setUserParamSql.toString();
    }
    /**
     * コピーした新規データを削除するSQLを作成する。
     * @param expTableBean Export対象テーブルの設定を保持するBean
     * @param selectCondition 検索条件
     * @return コピーした新規データを削除するSQL
     */
    private String createInsertDelSql(ExportTargetTableBean expTableBean,
            String selectCondition) {
        StringBuilder delSql = new StringBuilder("DELETE FROM ");
        delSql.append(expTableBean.getExportTempTableName());
        delSql.append(selectCondition);
        return delSql.toString();
    }
    /**
     * 新規データコピーのSQLを作成する。
     * @param tableName テーブル名
     * @param expTableBean Export対象テーブルの設定を保持するBean
     * @param selectCondition 検索条件
     * @return 新規データコピーのSQL
     */
    private String createInsertCopySql(String tableName,
            ExportTargetTableBean expTableBean, String selectCondition) {
        List<String> columnList = DBAccessUtil.delSystemColumn(expTableBean.getExportTableColumns());
        String column = DBAccessUtil.joinColumnArray(columnList);

        StringBuilder copySql = new StringBuilder("INSERT INTO ");
        copySql.append(tableName);
        copySql.append(" (");
        copySql.append(column);
        copySql.append(",");
        copySql.append(Constants.getRegisteredDateTimeColumnName());
        copySql.append(",");
        copySql.append(Constants.getUpdatedDateTimeColumnName());
        copySql.append(") SELECT ");
        copySql.append(column);
        copySql.append(",NOW(),NOW() FROM ");
        copySql.append(expTableBean.getExportTempTableName());
        copySql.append(selectCondition);
        return copySql.toString();
    }
    /**
     * 新規レコードコピーの検索条件を作成する。
     * @param expTableBean Export対象テーブルの設定を保持するBean
     * @param maxRecord コピー最大件数
     * @return 検索条件
     */
    private String createInsertselectcondition(
            ExportTargetTableBean expTableBean, long maxRecord) {
        StringBuilder selectCondition = new StringBuilder(" WHERE ");
        selectCondition.append(Constants.getSidColumnName());
        selectCondition.append(" IS NULL AND NOT EXISTS (SELECT ");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append(" FROM ");
        selectCondition.append(expTableBean.getDuplicateFlagTableName());
        selectCondition.append(" WHERE ");
        selectCondition.append(expTableBean.getDuplicateFlagTableName());
        selectCondition.append(".");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append("=");
        selectCondition.append(expTableBean.getExportTempTableName());
        selectCondition.append(".");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append(")");
        selectCondition.append(" ORDER BY ");
        selectCondition.append(Constants.getTemporarySidColumnName());
        selectCondition.append(" LIMIT ");
        selectCondition.append(maxRecord);
        return selectCondition.toString();
    }
    /**
     * 新規レコードに対してレコードロックを取得するか判定する。
     *
     * Import時にレコードロックが取得されている場合にのみ、
     * 新規レコードに対してレコードロックを取得する。
     *
     * @param jobflowSid ジョブフローSID
     * @param tableName テーブル名
     * @param conn コネクション
     * @return レコードロック取得要否
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private boolean getRecordLock(
            String jobflowSid,
            String tableName,
            Connection conn) throws BulkLoaderSystemException {
        String checkRecordLockSql = "SELECT COUNT(*) "
            + "FROM IMPORT_RECORD_LOCK "
            + "WHERE JOBFLOW_SID=? AND TABLE_NAME=?";
        boolean isRecordLock = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        // レコードロック有無を確認
        try {
            stmt = conn.prepareStatement(checkRecordLockSql);
            stmt.setString(1, jobflowSid);
            stmt.setString(2, tableName);
            rs = DBConnection.executeQuery(
                    stmt, checkRecordLockSql, new String[] { jobflowSid, tableName });
            rs.next();
            int count = rs.getInt("COUNT(*)");
            if (count > 0) {
                isRecordLock = true;
            }
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e, this.getClass(), checkRecordLockSql, new String[] { jobflowSid, tableName });
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }

        return isRecordLock;
    }
    /**
     * 更新が終了している場合のみ{@code true}を返す。
     * @return 更新が終了している場合に{@code true}、そうでない場合は{@code false}
     */
    public boolean isUpdateEnd() {
        return copyEnd;
    }
}
