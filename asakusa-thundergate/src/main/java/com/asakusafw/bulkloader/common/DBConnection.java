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
package com.asakusafw.bulkloader.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;

import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;


/**
 * DBのコネクションを取得するクラス。
 * 接続情報はプロパティファイルから読み込む
 * @author yuta.shirai
 *
 */
public final class DBConnection {

    /**
     * このクラス。
     */
    private static final Class<?> CLASS = DBConnection.class;

    /**
     * 初期化済みフラグ。
     */
    private static volatile boolean initialized = false;

    private DBConnection() {
        return;
    }

    /**
     * DBConnectionの初期化メソッド。
     * @param jdbcDriverName ドライバのクラス名
     * @throws BulkLoaderSystemException 初期化に失敗した場合
     */
    public static void init(String jdbcDriverName) throws BulkLoaderSystemException {
        try {
            Class.forName(jdbcDriverName).newInstance();
            initialized = true;
        } catch (NullPointerException e) {
            throw new BulkLoaderSystemException(
                    e,
                    DBConnection.CLASS,
                    MessageIdConst.CMN_JDBCDRIVER_LOAD_ERROR,
                    jdbcDriverName);
        } catch (InstantiationException e) {
            throw new BulkLoaderSystemException(
                    e,
                    DBConnection.CLASS,
                    MessageIdConst.CMN_JDBCDRIVER_LOAD_ERROR,
                    jdbcDriverName);
        } catch (IllegalAccessException e) {
            throw new BulkLoaderSystemException(
                    e,
                    DBConnection.CLASS,
                    MessageIdConst.CMN_JDBCDRIVER_LOAD_ERROR,
                    jdbcDriverName);
        } catch (ClassNotFoundException e) {
            throw new BulkLoaderSystemException(
                    e,
                    DBConnection.CLASS,
                    MessageIdConst.CMN_JDBCDRIVER_LOAD_ERROR,
                    jdbcDriverName);
        }
    }
    /**
     * コネクションを取得する。
     * @return コネクション
     * @throws BulkLoaderSystemException コネクションの取得に失敗した場合
     */
    public static Connection getConnection() throws BulkLoaderSystemException {
        Connection conn = null;
        FileInputStream fis = null;

        // 初期化が行われていない場合例外をスローする。
        if (!initialized) {
            throw new BulkLoaderSystemException(
                    CLASS,
                    MessageIdConst.CMN_DB_CONN_ERROR,
                    "初期化が行われていない");
        }

        // プロパティからDB接続情報を取得
        String url = ConfigurationLoader.getProperty(Constants.PROP_KEY_DB_URL);
        String user = ConfigurationLoader.getProperty(Constants.PROP_KEY_DB_USER);
        String password = ConfigurationLoader.getProperty(Constants.PROP_KEY_DB_PASSWORD);
        String param = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAME_DB_PRAM);

        try {
            if (param != null && !param.isEmpty()) {
                // チューニングパラメータのプロパティが指定されている場合
                fis = new FileInputStream(new File(param));
                Properties prop = new Properties();
                prop.load(fis);
                prop.setProperty("user", user);
                prop.setProperty("password", password);
                conn = DriverManager.getConnection(url, prop);
            } else {
                // チューニングパラメータのプロパティが指定されていない場合
                conn = DriverManager.getConnection(url, user, password);
            }

            // トランザクション分離レベルをREAD_COMMITTEDに設定
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            // オートコミットをFALSEに設定
            conn.setAutoCommit(false);

            return conn;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {
                    // ここで例外が発生した場合は握りつぶす
                    e1.printStackTrace();
                }
            }
            throw new BulkLoaderSystemException(
                    e,
                    CLASS,
                    MessageIdConst.CMN_DB_CONN_ERROR,
                    "コネクション取得中にSQL例外が発生");
        } catch (FileNotFoundException e) {
            throw new BulkLoaderSystemException(
                    e,
                    CLASS,
                    MessageIdConst.CMN_DB_CONN_ERROR,
                    MessageFormat.format("チューニングパラメータのプロパティが見つからない。ファイル名：{0}", param));
        } catch (IOException e) {
            throw new BulkLoaderSystemException(
                    e,
                    CLASS,
                    MessageIdConst.CMN_DB_CONN_ERROR,
                    MessageFormat.format("チューニングパラメータのプロパティの読み込みに失敗。ファイル名：{0}", param));
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // ここで例外が発生した場合は握りつぶす
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * PreparedStatementをクローズする。
     * @param stmt PreparedStatement
     */
    public static void closePs(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }
        }
    }
    /**
     * ResultSetをクローズする。
     * @param rs ResultSet
     */
    public static void closeRs(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }
        }
    }
    /**
     * Connectionをクローズする。
     * @param conn Connection
     */
    public static void closeConn(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }
        }
    }
    /**
     * トランザクションをコミットする。
     * @param conn Connection
     * @throws BulkLoaderSystemException ロールバックに失敗した場合
     */
    public static void commit(Connection conn) throws BulkLoaderSystemException {
        if (conn != null) {
            try {
                // コミット実行前ログを出力
                Log.log(CLASS, MessageIdConst.CMN_COMMIT_EXECUTE_BEFORE);
                Date beforeDate = new Date();

                conn.commit();

                // コミット実行後ログを出力
                Date aferDate = new Date();
                long time = aferDate.getTime() - beforeDate.getTime();
                Log.log(CLASS, MessageIdConst.CMN_COMMIT_EXECUTE_AFTER, time);
            } catch (SQLException e) {
                throw new BulkLoaderSystemException(
                        e,
                        CLASS,
                        MessageIdConst.CMN_DB_CONN_COMMIT_ERROR);
            }
        }
    }
    /**
     * トランザクションをロールバックする。
     * @param conn Connection
     * @throws BulkLoaderSystemException ロールバックに失敗した場合
     */
    public static void rollback(Connection conn) throws BulkLoaderSystemException {
        if (conn != null) {
            try {
                // ロールバック実行前ログを出力
                Log.log(CLASS, MessageIdConst.CMN_ROLLBACK_EXECUTE_BEFORE);
                Date beforeDate = new Date();

                conn.rollback();

                // ロールバック実行後ログを出力
                Date aferDate = new Date();
                long time = aferDate.getTime() - beforeDate.getTime();
                Log.log(CLASS, MessageIdConst.CMN_ROLLBACK_EXECUTE_AFTER, time);
            } catch (SQLException e) {
                throw new BulkLoaderSystemException(
                        e,
                        CLASS,
                        MessageIdConst.CMN_DB_CONN_ROLLBACK_ERROR);
            }
        }
    }
    /**
     * executeUpdateを実行する。
     * @param stmt PreparedStatement
     * @param sql 実行するSQL文
     * @param param SQLのパラメータ
     * @return 更新した件数
     * @throws SQLException SQLの実行に失敗した場合
     */
    public static int executeUpdate(PreparedStatement stmt, String sql, String... param) throws SQLException {
        // SQL実行前ログを出力
        String args = arrayToString(param);
        Log.log(CLASS, MessageIdConst.CMN_SQL_EXECUTE_BEFORE, sql, args);
        Date beforeDate = new Date();

        // SQLを実行
        int result = stmt.executeUpdate();

        // SQL実行後ログを出力
        Date aferDate = new Date();
        long time = aferDate.getTime() - beforeDate.getTime();
        Log.log(CLASS, MessageIdConst.CMN_SQL_EXECUTE_AFTER, time, result, sql, args);

        return result;
    }
    /**
     * executeQueryを実行する。
     * @param stmt PreparedStatement
     * @param sql 実行するSQL文
     * @param param SQLのパラメータ
     * @return ResultSet 問い合わせ結果のセット
     * @throws SQLException SQLの実行に失敗した場合
     */
    public static ResultSet executeQuery(
            PreparedStatement stmt,
            String sql,
            String... param) throws SQLException {
        // SQL実行前ログを出力
        String args = arrayToString(param);
        Log.log(CLASS, MessageIdConst.CMN_SQL_EXECUTE_BEFORE, sql, args);
        Date beforeDate = new Date();

        // SQLを実行
        ResultSet results = stmt.executeQuery();

        // SQL実行後ログを出力
        Date aferDate = new Date();
        long time = aferDate.getTime() - beforeDate.getTime();
        Log.log(CLASS, MessageIdConst.CMN_SQL_EXECUTE_AFTER, time, "-", sql, args);

        return results;
    }
    /**
     * 配列をカンマ区切りの文字列に変換する。
     * @param param 変換する配列
     * @return 変換後の文字列、引数に{@code null}が指定された場合は{@code null}
     */
    private static String arrayToString(String... param) {
        if (param == null) {
            return null;
        }
        StringBuilder strParam = new StringBuilder();
        for (int i = 0; i < param.length; i++) {
            strParam.append(param[i]);
            if (i < param.length - 1) {
                strParam.append(",");
            }
        }
        return strParam.toString();
    }
}
