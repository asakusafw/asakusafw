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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.ExportTempTableStatus;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;


/**
 * ExportファイルをDBにロードするクラス。
 * @author yuta.shirai
 */
public class ExportFileLoad {

    static final Log LOG = new Log(ExportFileLoad.class);

    /** テーブル名の最大長。 */
    private static final int MAX_TABLE_NAME_LENGTH = 64;
    /** テンポラリSID(BIGINT)の最大長。 */
    private static final int MAX_TEMP_SID_LENGTH = 19;
    /**
     * ExportファイルをDBにロードする。
     * ファイルのLoadに失敗した場合、全てのファイルをロードした後にfalseを返す。
     * @param bean パラメータを保持するBean
     * @return Exportファイルロード結果（true:成功、false:失敗）
     */
    public boolean loadFile(ExporterBean bean) {
        Connection conn = null;
        try {
            // コネクションを取得する
            conn = DBConnection.getConnection();

            // テンポラリ管理テーブルに作成予定のエクスポートテンポラリテーブルのレコードをInsert
            insertTempInfo(bean, conn);

            // エクスポートテンポラリテーブルを作成
            createTempTable(bean, conn);

            // TSV中間ファイルをLoad
            loadFile(bean, conn);

            // ロードが完了し、コピー前である事を記録
            updateStatus(bean.getJobflowSid(), conn);

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
     * ロードが完了し、コピー前である事をテンポラリ管理テーブルに記録する。
     * @param jobflowSid ジョブフローSID
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void updateStatus(String jobflowSid, Connection conn) throws BulkLoaderSystemException {
        // ロード完了を記録するSQL
        String beforeCopySql = "UPDATE EXPORT_TEMP_TABLE "
            + "SET TEMP_TABLE_STATUS=? "
            + "WHERE JOBFLOW_SID=? AND TEMP_TABLE_STATUS=?";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(beforeCopySql);
            stmt.setString(1, ExportTempTableStatus.BEFORE_COPY.getStatus());
            stmt.setString(2, jobflowSid);
            stmt.setString(3, ExportTempTableStatus.LOAD_EXIT.getStatus());
            DBConnection.executeUpdate(
                    stmt,
                    beforeCopySql,
                    new String[]{
                            ExportTempTableStatus.BEFORE_COPY.getStatus(),
                            jobflowSid,
                            ExportTempTableStatus.LOAD_EXIT.getStatus()
                    });
            DBConnection.commit(conn);
            LOG.info("TG-EXPORTER-03006", jobflowSid);
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    beforeCopySql,
                    new String[]{
                        ExportTempTableStatus.BEFORE_COPY.getStatus(),
                        jobflowSid,
                        ExportTempTableStatus.LOAD_EXIT.getStatus()
                    });
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * 中間TSVファイルをエクスポートテンポラリテーブルにLoadする。
     * Loadが終わったテーブルに対して重複チェックを行い、ロード完了を記録する
     * @param bean パラメータを保持するBean
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void loadFile(ExporterBean bean, Connection conn) throws BulkLoaderSystemException {
        // ロード完了を記録するSQL
        String loadExitSql = "UPDATE EXPORT_TEMP_TABLE "
            + "SET TEMP_TABLE_STATUS=? "
            + "WHERE JOBFLOW_SID=? AND TABLE_NAME=?";

        // 中間TSVファイルをロードする
        List<String> list = bean.getExportTargetTableList();
        for (String tableName : list) {
            ExportTargetTableBean tableBean = bean.getExportTargetTable(tableName);
            List<File> exportFileList = tableBean.getExportFiles();

            long recordCount = 0;
            // Export対象テーブルに該当するファイル数分繰り返す
            for (File file : exportFileList) {
                // ファイルをDBにロードする
                recordCount += load(
                        tableBean.getExportTempTableName(),
                        file,
                        tableBean.getExportTsvColumn(),
                        conn);
                LOG.info("TG-EXPORTER-03004",
                        bean.getJobflowSid(),
                        tableName,
                        tableBean.getExportTempTableName(),
                        file.getAbsolutePath());
            }
            LOG.info("TG-PROFILE-01003",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    tableName,
                    recordCount);

            PreparedStatement stmt = null;
            if (tableBean.isDuplicateCheck()) {
                // 重複フラグを立てるSQL
                StringBuilder duplicateCheckSql = new StringBuilder("INSERT INTO ");
                duplicateCheckSql.append(tableBean.getDuplicateFlagTableName());
                duplicateCheckSql.append("(");
                duplicateCheckSql.append(Constants.getTemporarySidColumnName());
                duplicateCheckSql.append(")");
                duplicateCheckSql.append(" SELECT ");
                duplicateCheckSql.append(Constants.getTemporarySidColumnName());
                duplicateCheckSql.append(" FROM ");
                duplicateCheckSql.append(tableBean.getExportTempTableName());
                duplicateCheckSql.append(" WHERE ");
                duplicateCheckSql.append("EXISTS(SELECT * FROM ");
                duplicateCheckSql.append(tableName);

                String forceIndex = ConfigurationLoader.getForceIndexName(
                        bean.getBatchId(), bean.getJobflowId(), tableName);
                if (forceIndex != null) {
                    duplicateCheckSql.append(" FORCE INDEX (");
                    duplicateCheckSql.append(forceIndex);
                    duplicateCheckSql.append(")");
                }

                duplicateCheckSql.append(" WHERE ");
                List<String> key = tableBean.getKeyColumns();

                int keySize = key.size();
                for (int i = 0; i < keySize; i++) {
                    duplicateCheckSql.append(tableName);
                    duplicateCheckSql.append(".");
                    duplicateCheckSql.append(key.get(i));
                    duplicateCheckSql.append("=");
                    duplicateCheckSql.append(tableBean.getExportTempTableName());
                    duplicateCheckSql.append(".");
                    duplicateCheckSql.append(key.get(i));
                    if (i < keySize - 1) {
                        duplicateCheckSql.append(" AND ");
                    }
                }
                duplicateCheckSql.append(") AND ");
                duplicateCheckSql.append(tableBean.getExportTempTableName());
                duplicateCheckSql.append(".");
                duplicateCheckSql.append(Constants.getSidColumnName());
                duplicateCheckSql.append(" IS NULL");

                // 重複チェックを行い、重複しているレコードには重複フラグを立てる
                try {
                    stmt = conn.prepareStatement(duplicateCheckSql.toString());
                    DBConnection.executeUpdate(stmt, duplicateCheckSql.toString(), new String[0]);
                } catch (SQLException e) {
                    throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                            e,
                            this.getClass(),
                            duplicateCheckSql.toString(),
                            new String[0]);
                } finally {
                    DBConnection.closePs(stmt);
                }
            }

            // エクスポートテンポラリ管理テーブルにロード完了を記録する
            try {
                stmt = conn.prepareStatement(loadExitSql);
                stmt.setString(1, ExportTempTableStatus.LOAD_EXIT.getStatus());
                stmt.setString(2, bean.getJobflowSid());
                stmt.setString(3, tableName);
                int updateCount = DBConnection.executeUpdate(
                        stmt,
                        loadExitSql,
                        new String[] {
                                ExportTempTableStatus.LOAD_EXIT.getStatus(),
                                bean.getJobflowSid(),
                                tableName
                        });
                if (updateCount == 0) {
                    throw new BulkLoaderSystemException(getClass(), "TG-EXPORTER-03001",
                            // TODO MessageFormat.formatの検討
                            "テンポラリ管理テーブルのレコードを更新できませんでした。ジョブフローSID：" + bean.getJobflowSid(),
                            " Export対象テーブル名：" + tableName);
                }
                DBConnection.commit(conn);
                LOG.info("TG-EXPORTER-03005",
                        bean.getJobflowSid(), tableName, tableBean.getExportTempTableName());
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        loadExitSql,
                        new String[]{
                            ExportTempTableStatus.LOAD_EXIT.getStatus(),
                            bean.getJobflowSid(),
                            tableName
                        });
            } finally {
                DBConnection.closePs(stmt);
            }
        }
    }
    /**
     * エクスポートテンポラリテーブルを作成する。
     * @param bean パラメータを保持するBean
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void createTempTable(ExporterBean bean, Connection conn) throws BulkLoaderSystemException {
        List<String> list = bean.getExportTargetTableList();
        for (String tableName : list) {
            // Exportテンポラリテーブル名
            String tempTableName = bean.getExportTargetTable(tableName).getExportTempTableName();
            // 重複チェックテーブル名
            String duplicateTableName = bean.getExportTargetTable(tableName).getDuplicateFlagTableName();

            // テンポラリテーブル作成のSQLを作成
            String createSql = createTableSql(tableName, tempTableName, bean.getExportTargetTable(tableName));

            // 重複チェックテーブル作成のSQL
            StringBuilder dupSql = new StringBuilder();
            dupSql.append("CREATE TABLE ");
            dupSql.append(duplicateTableName);
            dupSql.append(" (");
            dupSql.append(Constants.getTemporarySidColumnName());
            dupSql.append(" BIGINT NOT NULL,");
            dupSql.append("PRIMARY KEY (");
            dupSql.append(Constants.getTemporarySidColumnName());
            dupSql.append("))");

            // テンポラリテーブルを作成
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement(createSql.toString());
                DBConnection.executeUpdate(stmt, createSql.toString(), new String[0]);
                LOG.info("TG-EXPORTER-03003",
                        bean.getJobflowSid(), tableName, tempTableName, createSql.toString());
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        createSql.toString(),
                        new String[0]);
            } finally {
                DBConnection.closePs(stmt);
            }
            // 重複フラグテーブルを作成
            try {
                stmt = conn.prepareStatement(dupSql.toString());
                DBConnection.executeUpdate(stmt, dupSql.toString(), new String[0]);
                LOG.info("TG-EXPORTER-03008",
                        bean.getJobflowSid(), tempTableName, duplicateTableName, dupSql.toString());
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        dupSql.toString(), new String[0]);
            } finally {
                DBConnection.closePs(stmt);
            }
        }
    }
    /**
     * テンポラリテーブル名を作成する。
     * @param tableName Export対象テーブル名
     * @param jobflowSid ジョブフローSID
     * @param conn コネクション
     * @return テンポラリテーブル名
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    protected String createTempTableName(
            String tableName,
            String jobflowSid,
            Connection conn) throws BulkLoaderSystemException {
        // テンポラリシーケンス番号を取得
        long seq = getTempSeq(jobflowSid, tableName, conn);

        // テーブル名を組み立てる
        int maxLength = MAX_TABLE_NAME_LENGTH
                        - Constants.EXP_TEMP_TABLE_PREIX.length()
                        - Constants.EXPORT_TEMP_TABLE_DELIMITER.length()
                        - MAX_TEMP_SID_LENGTH
                        - Constants.DUPLECATE_FLG_TABLE_END.length();

        StringBuilder tempTableName = new StringBuilder(Constants.EXP_TEMP_TABLE_PREIX);
        if (tableName.length() > maxLength) {
            tempTableName.append(tableName.substring(0, maxLength));
        } else {
            tempTableName.append(tableName);
        }
        tempTableName.append(Constants.EXPORT_TEMP_TABLE_DELIMITER);
        tempTableName.append(seq);

        return tempTableName.toString();
    }
    /**
     * テンポラリシーケンス番号を取得する。
     * @param jobflowSid ジョブフローSID
     * @param tableName テーブル名
     * @param conn コネクション
     * @return テンポラリシーケンス番号
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    protected long getTempSeq(
            String jobflowSid,
            String tableName,
            Connection conn) throws BulkLoaderSystemException {
        // テンポラリシーケンス番号を取得するSQL
        String selectSql = "SELECT EXPORT_TEMP_SEQ "
            + "FROM EXPORT_TEMP_TABLE "
            + "WHERE JOBFLOW_SID=? AND TABLE_NAME=?";

        PreparedStatement stmt = null;
        ResultSet rs = null;
        long seq = 0;
        try {
            stmt = conn.prepareStatement(selectSql);
            stmt.setString(1, jobflowSid);
            stmt.setString(2, tableName);
            rs = DBConnection.executeQuery(stmt, selectSql, new String[]{jobflowSid, tableName});
            if (rs.next()) {
                seq = rs.getLong("EXPORT_TEMP_SEQ");
            } else {
                throw new BulkLoaderSystemException(getClass(), "TG-EXPORTER-03001",
                        // TODO MessageFormat.formatの検討
                        "テンポラリ管理テーブルのレコードを取得できませんでした。ジョブフローSID：" + jobflowSid,
                        " Export対象テーブル名：" + tableName);
            }
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    selectSql,
                    new String[]{ jobflowSid, tableName });
        } finally {
            DBConnection.closeRs(rs);
            DBConnection.closePs(stmt);
        }
        return seq;
    }
    /**
     * テンポラリテーブル作成のSQLを作成する。
     * @param tableName Export対象テーブル名
     * @param tempTableName Exportテンポラリテーブル名
     * @param tableBean Export対象テーブルの設定を保持するBean
     * @return テンポラリテーブル作成のSQL
     * @throws BulkLoaderSystemException SQLの構築に失敗した場合
     */
    protected String createTableSql(
            String tableName,
            String tempTableName,
            ExportTargetTableBean tableBean) throws BulkLoaderSystemException {

        // テンポラリテーブのソースとなるテーブル名とカラム名の一覧を取得
        /*
         * カラムをしぼるときのためのメモ。
         * 現在は TSV > (NORMAL_TABLE + ERROR_TABLE) - { ERROR_COLUMN }
         * となったときに、CREATE TABLE (...) SELECT ... で
         * 元のテーブルカラムが推測できないためにエラーとなってしまう可能性がある
         * (両者のテーブルにデータが存在する場合にambiguous)。
         */
        List<String> sourceTables = computeSourceTables(tableName, tableBean);
        List<String> sourceColumns = computeCopyColumns(tableName, tableBean);

        // テンポラリテーブルを作成するSQL
        StringBuilder createSql = new StringBuilder();
        createSql.append(createTempTableSqlHead(tableName, tempTableName));
        createSql.append(DBAccessUtil.joinColumnArray(sourceColumns));
        createSql.append(" FROM ");
        createSql.append(DBAccessUtil.joinColumnArray(sourceTables));
        createSql.append(" LIMIT 0");

        return createSql.toString();
    }

    private List<String> computeSourceTables(String tableName, ExportTargetTableBean tableBean) {
        assert tableName != null;
        assert tableBean != null;
        if (tableBean.isDuplicateCheck()) {
            return Arrays.asList(new String[] { tableName, tableBean.getErrorTableName() });
        } else {
            return Arrays.asList(new String[] { tableName });
        }
    }

    private List<String> computeCopyColumns(
            String tableName,
            ExportTargetTableBean tableBean) throws BulkLoaderSystemException {
        assert tableName != null;
        assert tableBean != null;
        List<String> columns = tableBean.getExportTsvColumn();
        columns = DBAccessUtil.delSystemColumn(columns);
        columns = new ArrayList<String>(columns);

        if (tableBean.isDuplicateCheck() == false) {
            int columnSize = columns.size();
            for (int i = 0; i < columnSize; i++) {
                // <テーブル名>.<カラム名> AS <カラム名>" の形式に書き換え
                columns.set(i, String.format("%s.%s AS %s", tableName, columns.get(i), columns.get(i)));
            }
        } else {
            // カラム->テーブル表を作成
            Map<String, String> columnMap = new HashMap<String, String>();
            for (String columnName : tableBean.getErrorTableColumns()) {
                columnMap.put(columnName, tableBean.getErrorTableName());
            }
            columnMap.put(tableBean.getErrorCodeColumn(), tableBean.getErrorTableName());
            for (String columnName : tableBean.getExportTableColumns()) {
                columnMap.put(columnName, tableName);
            }
            int columnSize = columns.size();
            for (int i = 0; i < columnSize; i++) {
                // テーブル名が判明すれば "<テーブル名>.<カラム名> AS <カラム名>" の形式に書き換え
                String owner = columnMap.get(columns.get(i));
                if (owner != null) {
                    columns.set(i, String.format("%s.%s AS %s", owner, columns.get(i), columns.get(i)));
                } else {
                    // カラムのソースが判明しないときにはどこからか型情報を補足する必要がある
                    throw new BulkLoaderSystemException(getClass(), "TG-EXPORTER-03007",
                            columns.get(i));
                }
            }
        }
        return columns;
    }

    private String createTempTableSqlHead(String tableName, String tempTableName) {
        StringBuilder buf = new StringBuilder();
        buf.append("CREATE TABLE ");
        buf.append(tempTableName);
        buf.append(" (");
        buf.append(Constants.getTemporarySidColumnName());
        buf.append(" BIGINT NOT NULL AUTO_INCREMENT,");
        buf.append(Constants.getSidColumnName());
        buf.append(" BIGINT NULL,");
        buf.append(Constants.getVersionColumnName());
        buf.append(" BIGINT NULL,");
        buf.append(Constants.getRegisteredDateTimeColumnName());
        buf.append(" DATETIME NULL,");
        buf.append(Constants.getUpdatedDateTimeColumnName());
        buf.append(" DATETIME NULL,");
        buf.append("PRIMARY KEY (");
        buf.append(Constants.getTemporarySidColumnName());
        buf.append(")) ");
        buf.append("SELECT ");

        buf.append("NULL AS ");
        buf.append(Constants.getTemporarySidColumnName());
        buf.append(",");

        // システムカラムは正常テーブルのスキーマを利用する
        buf.append(createSystemColumn(tableName, Constants.getSidColumnName()));
        buf.append(",");
        buf.append(createSystemColumn(tableName, Constants.getVersionColumnName()));
        buf.append(",");
        buf.append(createSystemColumn(tableName, Constants.getRegisteredDateTimeColumnName()));
        buf.append(",");
        buf.append(createSystemColumn(tableName, Constants.getUpdatedDateTimeColumnName()));
        buf.append(", ");

        return buf.toString();
    }

    private CharSequence createSystemColumn(String tableName, String columnName) {
        assert tableName != null;
        assert columnName != null;
        StringBuilder buf = new StringBuilder();
        buf.append(tableName);
        buf.append(".");
        buf.append(columnName);
        buf.append(" AS ");
        buf.append(columnName);
        return buf;
    }

    /**
     * エクスポートテンポラリ管理テーブルにレコードをインサートする。
     * @param bean パラメータを保持するBean
     * @param conn コネクション
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private void insertTempInfo(ExporterBean bean, Connection conn) throws BulkLoaderSystemException {
        // エクスポートテンポラリ管理テーブルにInsertするSQL
        String insertSql = "INSERT INTO EXPORT_TEMP_TABLE(JOBFLOW_SID,TABLE_NAME,EXPORT_TEMP_SEQ) "
            + "VALUES(?,?,NULL)";
        // エクスポートテンポラリ管理テーブルのテンポラリテーブル名を更新するSQL
        String updateSql = "UPDATE EXPORT_TEMP_TABLE "
            + "SET EXPORT_TEMP_NAME=?,DUPLICATE_FLG_NAME=? "
            + "WHERE JOBFLOW_SID=? AND TABLE_NAME=?";

        PreparedStatement stmt = null;
        String jobflowSid = null;

        jobflowSid = bean.getJobflowSid();
        List<String> list = bean.getExportTargetTableList();
        for (String tableName : list) {
            // レコードをインサート
            try {
                stmt = conn.prepareStatement(insertSql);
                stmt.setString(1, jobflowSid);
                stmt.setString(2, tableName);
                DBConnection.executeUpdate(stmt, insertSql, new String[]{ jobflowSid, tableName });
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        insertSql,
                        new String[]{ jobflowSid, tableName });
            } finally {
                DBConnection.closePs(stmt);
            }

            // テンポラリテーブル名を作成
            String tempTableName = createTempTableName(tableName, bean.getJobflowSid(), conn);
            bean.getExportTargetTable(tableName).setExportTempTableName(tempTableName);

            // 重複フラグテーブル名を作成
            String duplicateFlgTableName = DBAccessUtil.createDuplicateFlgTableName(tempTableName);
            bean.getExportTargetTable(tableName).setDuplicateFlagTableName(duplicateFlgTableName);

            // テンポラリテーブル名を更新
            try {
                stmt = conn.prepareStatement(updateSql);
                stmt.setString(1, tempTableName);
                stmt.setString(2, duplicateFlgTableName);
                stmt.setString(3, jobflowSid);
                stmt.setString(4, tableName);
                DBConnection.executeUpdate(
                        stmt,
                        updateSql,
                        new String[]{ tempTableName, jobflowSid, tableName });
            } catch (SQLException e) {
                throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                        e,
                        this.getClass(),
                        updateSql,
                        new String[]{ tempTableName, jobflowSid, tableName });
            } finally {
                DBConnection.closePs(stmt);
            }
        }
        DBConnection.commit(conn);
        LOG.info("TG-EXPORTER-03002", bean.getJobflowSid());
    }
    /**
     * Exportファイルのロードを実行する。
     * @param tempTableName テーブル名
     * @param file エクスポートファイル
     * @param exportTsvColumn TSVのカラム名一覧
     * @param conn コネクション
     * @return 更新した件数
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    private long load(
            String tempTableName,
            File file,
            List<String> exportTsvColumn,
            Connection conn) throws BulkLoaderSystemException {
        // ロードするファイルが空の場合はSQLを発行しない
        if (isEmpty(file)) {
            return 0;
        }

        // Load用のSQLを作成
        StringBuilder sql = new StringBuilder("LOAD DATA INFILE '");
        sql.append(file.getAbsolutePath().replace(File.separatorChar, '/'));
        sql.append("' INTO TABLE ");
        sql.append(tempTableName);
        sql.append(DBAccessUtil.getTSVFileFormat());
        sql.append(" (");
        sql.append(DBAccessUtil.joinColumnArray(exportTsvColumn));
        sql.append(")");

        PreparedStatement stmt = null;
        try {
            // LOADを実行
            stmt = conn.prepareStatement(sql.toString());
            long count = DBConnection.executeUpdate(stmt, sql.toString(), new String[0]);
            DBConnection.commit(conn);
            return count;
        } catch (SQLException e) {
            throw BulkLoaderSystemException.createInstanceCauseBySQLException(
                    e,
                    this.getClass(),
                    sql.toString(),
                    new String[0]);
        } finally {
            DBConnection.closePs(stmt);
        }
    }
    /**
     * ファイルが空かどうか判定して返す。
     * ファイルが存在して0byteの場合はtrueを返す
     * @param file ファイル
     * @return 判定結果
     */
    private boolean isEmpty(File file) {
        return file.exists() && file.length() == 0;
    }
}
