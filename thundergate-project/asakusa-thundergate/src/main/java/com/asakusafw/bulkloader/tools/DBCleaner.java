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
package com.asakusafw.bulkloader.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * DBをクリーニングするツール。
 * <p>
 * 実行すると以下の処理を行う。
 * </p>
 * <ul>
 * <li> ジョブフロー実行テーブル(RUNNING_JOBFLOWS)のレコードを全件削除 </li>
 * <li> ジョブフロー排他テーブル(JOBFLOW_INSTANCE_LOCK)のレコードを全件削除 </li>
 * <li> ロック(テーブルロック・レコードロック)を全て解除 </li>
 * <li> エクスポートテンポラリテーブルを全て削除 </li>
 * <li> エクスポートテンポラリ管理のレコードを全件削除 </li>
 * </ul>
 * @author yuta.shirai
 */
public final class DBCleaner {
    /**
     * プログラムエントリ。
<pre>
・args[0]：必須：ターゲット名
</pre>
     * @param args 起動引数
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);
        System.exit(result);
    }
    /**
     * DBCleanerの処理を実行する。
     * @param args コマンドライン引数
     * @return 終了コード
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_WARNING
     * @see Constants#EXIT_CODE_ERROR
     */
    protected int execute(String[] args) {
        String targetName = null;
        printLog("DBのクリーニングを開始します。");

        // 入力チェック
        if (args.length == 1) {
            targetName = args[0];
            if (isEmpty(targetName)) {
                printErr("引数にターゲット名が指定されていません。");
                return Constants.EXIT_CODE_ERROR;
            }
        } else {
            printErr("引数にターゲット名が指定されていません。");
            return Constants.EXIT_CODE_ERROR;
        }

        Connection conn = null;
        try {
            // JDBCプロパティを読み込む
            try {
                ConfigurationLoader.checkEnv();
                ConfigurationLoader.loadJDBCProp(targetName);
            } catch (IllegalStateException e) {
                // 環境変数が適切に設定されていない場合
                throw new SystemException(e, e.getMessage());
            } catch (BulkLoaderSystemException e) {
                throw new SystemException(e.getCause(),
                        MessageFormat.format("JDBCプロパティの読み込みに失敗しました。ターゲット名：{0}", targetName));
            }
            // DBConnectionを初期化
            try {
                DBConnection.init(ConfigurationLoader.getProperty(Constants.PROP_KEY_JDBC_DRIVER));
            } catch (BulkLoaderSystemException e) {
                throw new SystemException(e.getCause(),
                        MessageFormat.format("DBConnectionの初期化に失敗しました。ターゲット名：{0}", targetName));
            }
            // コネクションを取得
            try {
                conn = DBConnection.getConnection();
            } catch (BulkLoaderSystemException e) {
                throw new SystemException(e.getCause(),
                        MessageFormat.format("DBConnectionの取得に失敗しました。ターゲット名：{0}", targetName));
            }

            if (RuntimeContext.get().isSimulation()) {
                return Constants.EXIT_CODE_SUCCESS;
            }

            // テンポラリテーブルとエクスポートテンポラリ管理のレコードを削除
            deleteTempTable(conn);

            // ロックを解除
            lockRelease(conn);

            // ジョブフロー実行テーブルとジョブフロー排他テーブルのレコードを削除
            deleteRunningJobflows(conn);

            // releases cache locks
            releaseCacheLock(conn);

            // 正常終了
            printLog("DBのクリーニングを正常終了します。");
            return Constants.EXIT_CODE_SUCCESS;

        } catch (SystemException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            printErr(e.getCause(), e.getMessage());
            printErr("DBのクリーニングを異常終了します。");
            return Constants.EXIT_CODE_ERROR;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            printErr(e, "不明なエラーが発生しました。");
            printErr("DBのクリーニングを異常終了します。");
            return Constants.EXIT_CODE_ERROR;
        }
    }
    /**
     * ジョブフロー実行テーブルとジョブフロー排他テーブルのレコードを削除する。
     * @param conn コネクション
     * @throws SystemException 続行不能なエラー
     */
    private static void deleteRunningJobflows(Connection conn) throws SystemException {
        String delRunningJobflowSql = "DELETE FROM RUNNING_JOBFLOWS";
        String delInstanceLockSql = "DELETE FROM JOBFLOW_INSTANCE_LOCK";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(delRunningJobflowSql);
            int delCount = stmt.executeUpdate();
            printLog(
                    MessageFormat.format("ジョブフロー実行テーブルのレコードを全て削除しました。件数：{0}", delCount));
        } catch (SQLException e) {
            throw new SystemException(
                    e,
                    MessageFormat.format("ジョブフロー実行テーブルのレコードの削除に失敗しました。SQL：{0}", delRunningJobflowSql));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            stmt = conn.prepareStatement(delInstanceLockSql);
            int delCount = stmt.executeUpdate();
            printLog(
                    MessageFormat.format("ジョブフロー排他テーブルのレコードを全て削除しました。件数：{0}", delCount));
        } catch (SQLException e) {
            throw new SystemException(
                    e,
                    MessageFormat.format("ジョブフロー排他テーブルのレコードの削除に失敗しました。SQL：{0}", delRunningJobflowSql));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new SystemException(
                    e,
                    "トランザクションのコミットに失敗しました。");
        }
    }
    /**
     * ロックを解除する。
     * @param conn コネクション
     * @throws SystemException 続行不能なエラー
     */
    private static void lockRelease(Connection conn) throws SystemException {
        String tableLockSql = "UPDATE IMPORT_TABLE_LOCK SET JOBFLOW_SID=NULL WHERE JOBFLOW_SID IS NOT NULL";
        String selSql = "SELECT DISTINCT TABLE_NAME FROM IMPORT_RECORD_LOCK";
        String recordLockSql = "DELETE FROM IMPORT_RECORD_LOCK";
        String rlSql = "DELETE FROM ";

        // レコードロックテーブルを検索
        List<String> rlList = new ArrayList<String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(selSql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                rlList.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new SystemException(e,
                    MessageFormat.format("レコードロックテーブルの検索に失敗しました。SQL：{0}", selSql));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // ロック済みレコードのレコードを削除
        StringBuilder sql = null;
        try {
            for (String tableName : rlList) {
                String lockedTable = DBAccessUtil.createRecordLockTableName(tableName);
                sql = new StringBuilder(rlSql);
                sql.append(lockedTable);
                stmt = conn.prepareStatement(sql.toString());
                int delCount = stmt.executeUpdate();
                printLog(
                        MessageFormat.format("ロック済みレコードテーブルのレコードを全て削除しました。ロック済みレコードテーブル名：{0} 件数：{1}",
                                lockedTable, delCount));
            }
        } catch (SQLException e) {
            throw new SystemException(e,
                    MessageFormat.format("ロック済みレコードテーブルの削除に失敗しました。SQL：{0}", sql));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // レコードロックのレコードを削除
        try {
            stmt = conn.prepareStatement(recordLockSql);
            int delCount = stmt.executeUpdate();
            printLog(
                    MessageFormat.format("レコードロックテーブルのレコードを全て削除しました。件数：{0}", delCount));
        } catch (SQLException e) {
            throw new SystemException(e,
                    MessageFormat.format("レコードロックテーブルのレコードの削除に失敗しました。SQL：{0}", recordLockSql));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // テーブルロックを解除
        try {
            stmt = conn.prepareStatement(tableLockSql);
            int upCount = stmt.executeUpdate();
            printLog(
                    MessageFormat.format("テーブルロックを全て解除しました。件数：{0}", upCount));
        } catch (SQLException e) {
            throw new SystemException(e,
                    MessageFormat.format("レコードロックテーブルのレコードの削除に失敗しました。SQL：{0}", tableLockSql));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new SystemException(
                    e,
                    "トランザクションのコミットに失敗しました。");
        }
    }
    /**
     * テンポラリテーブルとエクスポートテンポラリ管理のレコードを削除する。
     * @param conn コネクション
     * @throws SystemException 続行不能なエラー
     */
    private static void deleteTempTable(Connection conn) throws SystemException {
        String selSql = "SELECT EXPORT_TEMP_NAME,DUPLICATE_FLG_NAME FROM EXPORT_TEMP_TABLE";
        String dropSql = "DROP TABLE IF EXISTS ";
        String delSql = "DELETE FROM EXPORT_TEMP_TABLE";

        // エクスポートテンポラリ管理テーブルを検索
        List<String> tempTableList = new ArrayList<String>();
        List<String> dupTableList = new ArrayList<String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(selSql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                tempTableList.add(rs.getString("EXPORT_TEMP_NAME"));
                dupTableList.add(rs.getString("DUPLICATE_FLG_NAME"));
            }
        } catch (SQLException e) {
            throw new SystemException(e, "エクスポートテンポラリ管理テーブルの検索に失敗しました。SQL：" + selSql);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // テンポラリテーブルを削除
        StringBuilder sql = null;
        try {
            for (int i = 0, n = tempTableList.size(); i < n; i++) {
                String tempTableName = tempTableList.get(i);
                if (!isEmpty(tempTableName)) {
                    sql = new StringBuilder(dropSql);
                    sql.append(tempTableName);
                    stmt = conn.prepareStatement(sql.toString());
                    stmt.executeUpdate();
                    printLog(
                            MessageFormat.format("テンポラリテーブルを削除しました。テンポラリテーブル名：{0}", tempTableName));
                }
                String dupTableName = dupTableList.get(i);
                if (!isEmpty(dupTableName)) {
                    sql = new StringBuilder(dropSql);
                    sql.append(dupTableName);
                    stmt = conn.prepareStatement(sql.toString());
                    stmt.executeUpdate();
                    printLog(
                            MessageFormat.format("重複フラグテーブルを削除しました。テンポラリテーブル名：{0}", dupTableName));
                }
            }
        } catch (SQLException e) {
            throw new SystemException(e,
                    MessageFormat.format("テンポラリテーブルの削除に失敗しました。SQL：{0}", sql));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // エクスポートテンポラリ管理のレコードを削除
        try {
            stmt = conn.prepareStatement(delSql);
            int delCount = stmt.executeUpdate();
            printLog(
                    MessageFormat.format("エクスポートテンポラリ管理テーブルのレコードを全て削除しました。件数：{0}", delCount));
        } catch (SQLException e) {
            throw new SystemException(e,
                    MessageFormat.format("エクスポートテンポラリ管理テーブルのレコードの削除に失敗しました。SQL：{0}", delSql));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new SystemException(
                    e,
                    "トランザクションのコミットに失敗しました。");
        }
    }

    private void releaseCacheLock(Connection conn) {
        assert conn != null;
        Statement stmt = null;
        boolean committed = false;
        try {
            stmt = conn.createStatement();
            int count = stmt.executeUpdate("DELETE FROM __TG_CACHE_LOCK WHERE 1 > 0");
            conn.commit();
            printLog(MessageFormat.format(
                    "キャッシュロックを削除しました。件数：{0}件",
                    count));
            committed = false;
        } catch (SQLException e) {
            printLog("キャッシュロックの削除に失敗しました。スキップします。");
            e.printStackTrace();
        } finally {
            if (committed == false) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 引数の文字列が空かnullの場合trueを返す。
     * @param str 文字列
     * @return 結果
     */
    private static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        if (str.isEmpty()) {
            return true;
        }
        return false;
    }
    /**
     * エラーメッセージを出力する。
     * @param e Exception
     * @param message エラーメッセージ
     */
    private static void printErr(Throwable e, String message) {
        printErr(message);
        e.printStackTrace();
    }
    /**
     * エラーメッセージを出力する。
     * @param message エラーメッセージ
     */
    private static void printErr(String message) {
        String strDate = getDate();
        System.out.println("[ERR] [" + strDate + "]：" + message);
    }
    /**
     * ログを出力する。
     * @param message ログメッセージ
     */
    private static void printLog(String message) {
        String strDate = getDate();
        System.out.println("[LOG] [" + strDate + "]：" + message);
    }
    /**
     * 現在の日付時刻を表す文字列を返す。
     * @return 現在の日付時刻を表す文字列
     */
    private static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
}
