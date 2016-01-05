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
package com.asakusafw.cleaner.common;

import com.asakusafw.cleaner.exception.CleanerSystemException;
import com.asakusafw.cleaner.log.Log;
import com.asakusafw.cleaner.log.LogInitializer;

/**
 * Cleanerの初期化クラス。
 * コンフィグレーションとログの初期化を行う。
 * @author yuta.shirai
 *
 */
public final class CleanerInitializer {
    private CleanerInitializer() {
    	return;
    }

    /**
     * 初期処理。
     * ログの初期化とコンフィグレーションの読み込みを行う
     * LocalFileCleanerは当メソッドを指定する。
     * @param properties 読み込むプロパティファイルの一覧(絶対パスか「$CLEAN_HOME/conf/」のファイル名を指定する)
     * @return 初期化処理結果
     */
    public static boolean initLocalFileCleaner(String[] properties) {
        return initialize(properties, true, false);
    }
    /**
     * 初期処理。
     * ログの初期化とコンフィグレーションの読み込みを行う
     * HDFSCleanerは当メソッドを指定する。
     * @param properties 読み込むプロパティファイルの一覧(絶対パスか「$CLEAN_HOME/conf/」のファイル名を指定する)
     * @return 初期化処理結果
     */
    public static boolean initDFSCleaner(String[] properties) {
        return initialize(properties, false, true);
    }
    /**
     * 初期処理を実施する。
     * @param properties 読み込むプロパティ
     * @param doLocalCleanPropCheck LocalFileCleaner用プロパティのチェック要否
     * @param doDFSCleanPropCheck HDFSCleaner用プロパティのチェック要否
     * @return 初期化処理結果
     */
    private static boolean initialize(
            String[] properties,
            boolean doLocalCleanPropCheck,
            boolean doDFSCleanPropCheck) {

        try {
            // コンフィグレーションの読み込み
            ConfigurationLoader.init(properties, doLocalCleanPropCheck, doDFSCleanPropCheck);
        } catch (CleanerSystemException e) {
            // ログの初期化が可能な場合はログを出力する。
            if (initLog()) {
                Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
                return false;
            } else {
                printPropLoadError(properties, e);
                return false;
            }
        } catch (Exception e) {
            printPropLoadError(properties, e);
            return false;
        }

        // ログの初期化
        if (!initLog()) {
            return false;
        }

        return true;
    }
    /**
     * ログの初期化を行う。
     * @return ログ初期化結果
     */
    private static boolean initLog() {
        // ログの初期化
        String logConfFilePath = null;
        try {
            logConfFilePath = ConfigurationLoader.getProperty(Constants.PROP_KEY_LOG_CONF_PATH);
            LogInitializer.execute(logConfFilePath);
            return true;
        } catch (Exception e) {
            System.err.println("ERROR：ログの初期化に失敗しました [" + e.getMessage() + "]");
            System.err.println("ログ設定ファイル：" + logConfFilePath);
            e.printStackTrace();
            return false;
        }
    }
    /**
     * ログを出力できない場合にプロパティファイル読み込みエラーを出力する。
     * @param properties プロパティ
     * @param e 発生した例外
     */
    private static void printPropLoadError(String[] properties, Exception e) {

        System.err.println("ERROR：コンフィグレーションの初期化に失敗しました [" + e.getMessage() + "]");
        System.err.println("環境変数：" + System.getenv());
        if (properties == null) {
            System.err.println("プロパティファイル：null");
        } else {
            for (int i = 0; i < properties.length; i++) {
                System.err.println("プロパティファイル[" + i + "]：" + properties[i]);
            }
        }
        e.printStackTrace();
    }
}
