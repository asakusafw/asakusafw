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

import java.io.File;
import java.util.List;

import org.apache.hadoop.io.nativeio.NativeIO;

import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.log.LogInitializer;

/**
 * ノードごとの初期化処理。
 */
public final class BulkLoaderInitializer {

    static final Log LOG = new Log(BulkLoaderInitializer.class);

    private BulkLoaderInitializer() {
        return;
    }

    /**
     * DBサーバでの初期処理。
     * ログの初期化とコンフィグレーションの読み込みを行う
     * DBサーバ上で動作するプロセスは当メソッドを指定する。
     * @param jobflowId ジョブフローID
     * @param executionId 実行ID
     * @param properties 読み込むプロパティファイルの一覧(絶対パスか「$ASAKUSA_HOME/bulkloader/conf/」のファイル名を指定する)
     * @param targetName ターゲット名
     * @return 処理結果
     */
    public static boolean initDBServer(
            String jobflowId,
            String executionId,
            List<String> properties,
            String targetName) {
        return initialize(jobflowId, executionId, properties, true, false, true, targetName);
    }
    /**
     * Hadoop Clusterでの初期処理。
     * ログの初期化とコンフィグレーションの読み込みを行う
     * HadoopCluster上で動作するプロセスは当メソッドを指定する。
     * @param jobflowId ジョブフローID
     * @param executionId 実行ID
     * @param properties 読み込むプロパティファイルの一覧(絶対パスか「$ASAKUSA_HOME/bulkloader/conf/」のファイル名を指定する)
     * @return 処理結果
     */
    public static boolean initHadoopCluster(
            String jobflowId,
            String executionId,
            List<String> properties) {
        // Eagerly initializes Hadoop native libraries
        NativeIO.isAvailable();
        return initialize(jobflowId, executionId, properties, false, true, false, null);
    }

    /**
     * 初期処理を実施する。
     * @param jobflowId ジョブフローID
     * @param executionId 実行ID
     * @param properties 読み込むプロパティ
     * @param doDBPropCheck DBサーバ用プロパティのチェック要否
     * @param doHCPropCheck マスターノード用プロパティのチェック要否
     * @param doDbConnInit DBコネクション初期化要否
     * @param targetName ターゲット名（DBコネクションを初期化する場合のみ使用する）
     * @return 初期化に成功した場合に{@code true}、失敗した場合は{@code false}
     */
    private static boolean initialize(
            String jobflowId,
            String executionId,
            List<String> properties,
            boolean doDBPropCheck,
            boolean doHCPropCheck,
            boolean doDbConnInit,
            String targetName) {

        try {
            // コンフィグレーションの読み込み
            ConfigurationLoader.init(properties, doDBPropCheck, doHCPropCheck);
            // テーブルシステカラムの定数を初期化する
            Constants.setSystemColumn();
        } catch (BulkLoaderSystemException e) {
            // ログの初期化が可能な場合はログを出力する。
            if (initLog(jobflowId, executionId, targetName)) {
                LOG.log(e);
                return false;
            } else {
                printPropLoadError(jobflowId, executionId, targetName, properties, e);
                return false;
            }
        } catch (Exception e) {
            printPropLoadError(jobflowId, executionId, targetName, properties, e);
            return false;
        }

        // ログの初期化
        if (!initLog(jobflowId, executionId, targetName)) {
            return false;
        }

        // DBConnectionの初期化
        if (doDbConnInit) {
            try {
                // JDBCプロパティを読み込む
                ConfigurationLoader.loadJDBCProp(targetName);
                // DBConnectionを初期化
                DBConnection.init(ConfigurationLoader.getProperty(Constants.PROP_KEY_JDBC_DRIVER));
            } catch (BulkLoaderSystemException e) {
                LOG.log(e);
                return false;
            }
        }

        return true;
    }

    /**
     * ログの初期化を行う。
     * @param jobflowId ジョブフローID
     * @param executionId 実行ID
     * @param targetName ターゲット名
     * @return 初期化に成功した場合は{@code true}、失敗した場合は{@code false}
     */
    private static boolean initLog(String jobflowId, String executionId, String targetName) {
        // ログの初期化
        String logConfFilePath = null;
        try {
            logConfFilePath = ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH);
            if (new File(logConfFilePath).exists() == false) {
                String home = ConfigurationLoader.getEnvProperty(Constants.ASAKUSA_HOME);
                if (home != null) {
                    File file = new File(new File(home), logConfFilePath);
                    if (file.exists()) {
                        logConfFilePath = file.getPath();
                    }
                }
            }
            LogInitializer.execute(logConfFilePath);
            return true;
        } catch (Exception e) {
            // TODO MessageFormat.formatの検討
            System.err.println(
                    "ERROR：ログの初期化に失敗しました ["
                    + e.getMessage()
                    + "] ジョブフローID："
                    + jobflowId
                    + " ジョブフロー実行ID："
                    + executionId
                    + "ターゲット名"
                    + targetName);
            System.err.println("ログ設定ファイル：" + logConfFilePath);
            e.printStackTrace();
            return false;
        }
    }
    /**
     * ログを出力できない場合にプロパティファイル読み込みエラーを出力する。
     * @param jobflowId ジョブフローID
     * @param executionId 実行ID
     * @param targetName ターゲット名
     * @param properties プロパティファイルの一覧
     * @param e 発生した例外
     */
    private static void printPropLoadError(
            String jobflowId,
            String executionId,
            String targetName,
            List<String> properties,
            Exception e) {
        // TODO MessageFormat.formatの検討
        System.err.println(
                "ERROR：コンフィグレーションの初期化に失敗しました ["
                + e.getMessage()
                + "] ジョブフローID："
                + jobflowId
                + " ジョブフロー実行ID："
                + executionId
                + "ターゲット名"
                + targetName);
        System.err.println("環境変数：" + System.getenv());
        if (properties == null) {
            System.err.println("プロパティファイル：null");
        } else {
            for (int i = 0; i < properties.size(); i++) {
                System.err.println("プロパティファイル[" + i + "]：" + properties.get(i));
            }
        }
        e.printStackTrace();
    }
}
