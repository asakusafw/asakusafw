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

/**
 * Import処理/Export処理用の定数クラス。
 * @author yuta.shirai
 *
 */
public final class Constants {
    /**
     * 終了コード。
     */
    /** 終了コード 正常終了。 */
    public static final int EXIT_CODE_SUCCESS = 0;
    /** 終了コード 異常終了。 */
    public static final int EXIT_CODE_ERROR = 1;
    /** 終了コード 警告終了。 */
    public static final int EXIT_CODE_WARNING = 2;

    /**
     * クリーナーの動作モード
     */
    /** クリーニング対象ディレクトリのみクリーニングを行う。 */
    public static final String CLEAN_MODE_NOMAL = "normal";
    /** 再帰的にクリーニングを行う。 */
    public static final String CLEAN_MODE_RECURSIVE = "recursive";

    /**
     * 環境変数名
     */
    /** 環境変数名 CLEANERのHOMEディレクトリ。 */
    public static final String CLEAN_HOME = "CLEANER_HOME";

    /**
     * プロパティKEY
     */
    /** 共通 */
    /** プロパティKEY log4j.xmlのパス。 */
    public static final String PROP_KEY_LOG_CONF_PATH = "log.conf-path";

    /** clean-localfs-conf.properties */
    /** プロパティKEY クリーニング対象ディレクトリ。 */
    public static final String PROP_KEY_LOCAL_FILE_CLEAN_DIR = "clean.local-dir";
    /** プロパティKEY クリーニング対象ファイルのパターン。 */
    public static final String PROP_KEY_LOCAL_FILE_CLEAN_PATTERN = "clean.local-pattern";
    /** プロパティKEY クリーニング対象を保持する期間。 */
    public static final String PROP_KEY_LOCAL_FILE_KEEP_DATE = "clean.local-keep-date";

    /** clean-hdfs-conf.properties */
    /** プロパティKEY HDFSのプロトコルとホスト名。 */
    public static final String PROP_KEY_HDFS_PROTCOL_HOST = "hdfs-protocol-host";
    /** プロパティKEY HDFS上のクリーニング対象ディレクトリ。 */
    public static final String PROP_KEY_HDFS_FILE_CLEAN_DIR = "clean.hdfs-dir";
    /** プロパティKEY クリーニング対象ファイルのパターン。 */
    public static final String PROP_KEY_HDFS_FILE_CLEAN_PATTERN = "clean.hdfs-pattern";
    /** プロパティKEY HDFS上のクリーニング対象を保持する期間。 */
    public static final String PROP_KEY_HDFS_FILE_KEEP_DATE = "clean.hdfs-keep-date";

    /**
     * プロパティのデフォルト値
     */
    /** プロパティデフォルト値 log4j.xmlのパス。 */
    public static final String PROP_DEFAULT_LOG_CONF_PATH = "bulkloader/conf/log4j.xml";
    /** プロパティデフォルト値 クリーニング対象を保持する期間。 */
    public static final String PROP_DEFAULT_LOCAL_FILE_KEEP_DATE = "14";
    /** プロパティデフォルト値 HDFS上のクリーニング対象を保持する期間。 */
    public static final String PROP_DEFAULT_HDFS_FILE_KEEP_DATE = "14";

    /**
     * パス・ファイル名の固定値
     */
    /** ログメッセージプロパティのファイル名。 */
    public static final String LOG_MESSAGE_FILE = "cleaner_message.properties";
    /** 設定格納ファイルパス。 */
    public static final String PROP_FILE_PATH = "conf";
    /** HDFSのパス固定値。 */
    public static final String HDFSFIXED_PATH = "/user";
    /** HDFSのパスの置換文字列(ユーザー名)。 */
    public static final String HDFS_PATH_REPLACE_STR_USER = "${user}";
    /** HDFSのパスの置換文字列(ジョブフロー実行ID)。 */
    public static final String HDFS_PATH_REPLACE_STR_ID = "${execution_id}";

    /**
     * <p>インスタンスの作成を禁止する。</p>
     */
    private Constants() {
        return;
    }
}
