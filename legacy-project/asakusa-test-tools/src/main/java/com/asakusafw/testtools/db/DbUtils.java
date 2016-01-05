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
package com.asakusafw.testtools.db;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.asakusafw.testtools.ColumnInfo;
import com.asakusafw.testtools.Configuration;

/**
 * データベースに関するユーティリティ群。
 */
public final class DbUtils {

    /**
     * 指定されたカラム情報のリストからテーブルを作成する。
     * @param conn 接続
     * @param list DDL生成の基となるテーブル、カラムの情報
     * @throws SQLException テーブルの作成に失敗した場合
     */
    public static void createTable(Connection conn, List<ColumnInfo> list) throws SQLException {
        if (list == null || list.size() == 0) {
            throw new RuntimeException("カラム情報のリストが空");
        }

        StringBuilder sb = new StringBuilder();
        boolean firstElement = true;
        for (ColumnInfo info : list) {
            if (firstElement) {
                firstElement = false;
                sb.append("CREATE TABLE ");
                sb.append(info.getTableName());
                sb.append("(\n");
            } else {
                sb.append(",\n");
            }
            sb.append("  ");
            sb.append(String.format("  %-32s %s",
                    info.getColumnName(),
                    info.getDataType().getDataTypeString()));
            switch(info.getDataType()) {
            case CHAR:
            case VARCHAR:
                sb.append(String.format("(%d)",
                        info.getCharacterMaximumLength()));
                break;
            case DECIMAL:
                sb.append(String.format("(%d,%d)",
                        info.getNumericPrecision(),
                        info.getNumericScale()));
                break;
            default:
                break;
            }
            if (info.getColumnComment() != null
                    && info.getColumnComment().length() != 0) {
                sb.append(" COMMENT ");
                sb.append("'");
                sb.append(info.getColumnComment());
                sb.append("'");
            }
        }
        sb.append("\n) engine=innodb");
        String sql = sb.toString();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            closeQuietly(stmt);
        }
    }

    /**
     * 指定のテーブルをDropする。
     * @param conn JDBCコネクション
     * @param tablename テーブル名
     * @throws SQLException テーブルの削除に失敗した場合
     */
    public static void dropTable(Connection conn, String tablename) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS ");
        sb.append(tablename);
        String sql = sb.toString();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            closeQuietly(stmt);
        }
    }

    /**
     * 指定のテーブルをTruncateする。
     * @param conn JDBCコネクション
     * @param tablename テーブル名
     * @throws SQLException テーブルの切り詰めに失敗した場合
     */
    public static void truncateTable(Connection conn, String tablename) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("TRUNCATE TABLE ");
        sb.append(tablename);
        String sql = sb.toString();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            closeQuietly(stmt);
        }
    }

    /**
     * Exceptionを発生させずにConnectionをクローズする。
     * @param conn クローズするオブジェクト
     */
    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Exceptionを発生させずにStatementをクローズする。
     * @param stmt クローズするオブジェクト
     */
    public static void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Exceptionを発生させずにResultSetをクローズする。
     * @param rs クローズするオブジェクト
     */
    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Exceptionを発生させずにCloseableをクローズする。
     * @param closeable クローズするオブジェクト
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * コンフィグレーションファイルに従いDBコネクションを取得する。
     * @return DBコネクション
     * @throws SQLException コネクションの取得に失敗した場合
     */
    public static Connection getConnection() throws SQLException {

        Configuration conf = Configuration.getInstance();
        String driver = conf.getJdbcDriver();
        String user = conf.getJdbcUser();
        String password = conf.getJdbcPassword();
        String url = conf.getJdbcUrl();
        if (driver == null) {
            throw new IllegalArgumentException("jdbcDriver must not be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("jdbcUrl must not be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Drivr not found", e);
        }
        return  DriverManager.getConnection(url, user, password);
    }

    private DbUtils() {
        return;
    }
}
