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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.SequenceFile.CompressionType;

import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;

/**
 * Import処理/Export処理用の定数クラス。
 * @author yuta.shirai
 * @since 0.1.0
 * @version 0.7.1
 */
public final class Constants {

    /*
     * 環境変数名
     */

    /**
     * 環境変数名 ASAKSAF/WのHOMEディレクトリ。
     */
    public static final String ASAKUSA_HOME = "ASAKUSA_HOME";

    /**
     * Environmental variable: the batch applications installation base path.
     * @since 0.6.1
     */
    public static final String BATCHAPPS_PATH = "ASAKUSA_BATCHAPPS_HOME";

    /**
     * 環境変数名 ThunderGateのHOMEディレクトリ。
     */
    public static final String THUNDER_GATE_HOME = "BULKLOADER_HOME";

    /**
     * 変数表の環境変数。
     * <p>
     * {@link VariableTable#toSerialString()}の形式で指定すること。
     * </p>
     */
    public static final String ENV_ARGS = "BULKLOADER_ARGS";

    /*
     * 終了コード
     */
    /**
     * 終了コード 正常終了。
     */
    public static final int EXIT_CODE_SUCCESS = 0;
    /**
     * 終了コード 異常終了。
     */
    public static final int EXIT_CODE_ERROR = 1;
    /**
     * 終了コード 警告終了。
     */
    public static final int EXIT_CODE_WARNING = 2;
    /**
     * Exit code: failed but is retryable.
     */
    public static final int EXIT_CODE_RETRYABLE = 3;

    /**
     * プロパティファイル
     */
    /** DBサーバで読み込むプロパティファイル。 */
    public static final List<String> PROPERTIES_DB = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    /** HadoopClusterで読み込むプロパティファイル。 */
    public static final List<String> PROPERTIES_HC = Arrays.asList(new String[]{"bulkloader-conf-hc.properties"});


    /*
     * プロパティKEY
     */
    /*
     * 共通
     */
    /**
     * プロパティKEY log4j.xmlのパス。
     */
    public static final String PROP_KEY_LOG_CONF_PATH = "log.conf-path";
    /**
     * プロパティKEY HDFSのプロトコルとホスト名。
     * @deprecated use {@link #PROP_KEY_BASE_PATH} instead
     */
    @Deprecated
    public static final String PROP_KEY_HDFS_PROTCOL_HOST = "hdfs-protocol-host";

    /**
     * The property key prefix of remote Hadoop client's environment variables.
     * @since 0.4.0
     */
    public static final String PROP_PREFIX_HC_ENV = "hadoop-cluster.env.";

    /**
     * プロパティKEY HDFSのプロトコルとホスト名。
     */
    public static final String PROP_KEY_BASE_PATH = "base-path";

    /*
     * bulkloader-conf-db.properties
     */
    /**
     * プロパティKEY SSHのパス。
     */
    public static final String PROP_KEY_SSH_PATH = "ssh.path";

    /**
     * プロパティKEY HDFSのNameノードのIPアドレス又はホスト名。
     */
    public static final String PROP_KEY_NAMENODE_HOST = "hadoop-cluster.host";

    /**
     * Value of {@link #PROP_KEY_NAMENODE_HOST} to use local processes for connecting between DB and HC.
     * @since 0.7.1
     */
    public static final String PROP_VALUE_NON_REMOTE_HOST = "-";

    /**
     * プロパティKEY HDFSのNameノードのユーザー名。
     */
    public static final String PROP_KEY_NAMENODE_USER = "hadoop-cluster.user";
    /**
     * プロパティKEY Importファイルを置くディレクトリのトップディレクトリ。
     */
    public static final String PROP_KEY_IMP_FILE_DIR = "import.tsv-create-dir";
    /**
     * プロパティKEY Extractorのシェル名。
     * @deprecated use {@link #PROP_PREFIX_HC_ENV} and {@link #ASAKUSA_HOME} instead
     */
    @Deprecated
    public static final String PROP_KEY_EXT_SHELL_NAME = "import.extractor-shell-name";
    /**
     * The property key of Cache info retriever executable file name.
     * @deprecated use {@link #PROP_PREFIX_HC_ENV} and {@link #ASAKUSA_HOME} instead
     */
    @Deprecated
    public static final String PROP_KEY_CACHE_INFO_SHELL_NAME = "import.cache-info-shell-name";
    /**
     * The property key of Cache storage cleaner file name.
     * @deprecated use {@link #PROP_PREFIX_HC_ENV} and {@link #ASAKUSA_HOME} instead
     */
    @Deprecated
    public static final String PROP_KEY_DELETE_CACHE_SHELL_NAME = "import.delete-cache-shell-name";
    /**
     * プロパティKEY Importファイルの圧縮有無。
     */
    public static final String PROP_KEY_IMP_FILE_COMP_TYPE = "import.zip-comp-type";
    /**
     * プロパティKEY Importファイルの圧縮時のバッファサイズ。
     */
    public static final String PROP_KEY_IMP_FILE_COMP_BUFSIZE = "import.zip-comp-buf-size";
    /**
     * プロパティKEY Importerのリトライ回数。
     */
    public static final String PROP_KEY_IMP_RETRY_COUNT = "import.retry-count";
    /**
     * プロパティKEY Importerのリトライインターバル。
     */
    public static final String PROP_KEY_IMP_RETRY_INTERVAL = "import.retry-interval";
    /**
     * プロパティKEY エクスポートファイルを置くディレクトリのトップディレクトリ。
     */
    public static final String PROP_KEY_EXP_FILE_DIR = "export.tsv-create-dir";
    /**
     * プロパティKEY Collectorのシェル名。
     * @deprecated use {@link #PROP_PREFIX_HC_ENV} and {@link #ASAKUSA_HOME} instead
     */
    @Deprecated
    public static final String PROP_KEY_COL_SHELL_NAME = "export.collector-shell-name";
    /**
     * プロパティKEY Exportファイルの圧縮時のバッファサイズ。
     */
    public static final String PROP_KEY_EXP_FILE_COMP_BUFSIZE = "export.zip-comp-buf-size";
    /**
     * プロパティKEY Exporterのリトライ回数。
     */
    public static final String PROP_KEY_EXP_RETRY_COUNT = "export.retry-count";
    /**
     * プロパティKEY Exporterのリトライインターバル。
     */
    public static final String PROP_KEY_EXP_RETRY_INTERVAL = "export.retry-interval";
    /**
     * プロパティKEY エクスポート処理でExport対象テーブルにデータをコピーする時の最大レコード数。
     */
    public static final String PROP_KEY_EXP_COPY_MAX_RECORD = "export.data-copy-max-count";
    /**
     * システムカラムのカラム名 システムID。
     */
    public static final String PROP_KEY_SYS_COLUMN_SID = "table.sys-column-sid";
    /**
     * システムカラムのカラム名 バージョン番号。
     */
    public static final String PROP_KEY_SYS_COLUMN_VERSION_NO = "table.sys-column-version-no";
    /**
     * システムカラムのカラム名 登録日時。
     */
    public static final String PROP_KEY_SYS_COLUMN_RGST_DATE = "table.sys-column-rgst-date";
    /**
     * システムカラムのカラム名 更新日時。
     */
    public static final String PROP_KEY_SYS_COLUMN_UPDT_DATE = "table.sys-column-updt-date";
    /**
     * システムカラムのカラム名 テンポラリSID。
     */
    public static final String PROP_KEY_SYS_COLUMN_TEMP_SID = "table.sys-column-temp-sid";
    /**
     * プロパティKEY インポート処理が正常終了した場合に、Exporterで生成したインポート中間TSVファイルを削除する。
     */
    public static final String PROP_KEY_IMPORT_TSV_DELETE = "import.delete-tsv";
    /**
     * プロパティKEY エクスポート処理が正常終了した場合に、Exporterで生成したエクスポート中間TSVファイルを削除する。
     */
    public static final String PROP_KEY_EXPORT_TSV_DELETE = "export.delete-tsv";


    /*
     * [ターゲット名]-jdbc.properties
     */
    /**
     * プロパティKEY JDBCドライバ。
     */
    public static final String PROP_KEY_JDBC_DRIVER = "jdbc.driver";
    /**
     * プロパティKEY DB接続URL。
     */
    public static final String PROP_KEY_DB_URL = "jdbc.url";
    /**
     * プロパティKEY DB接続ユーザー。
     */
    public static final String PROP_KEY_DB_USER = "jdbc.user";
    /**
     * プロパティKEY DB接続ユーザーに対するパスワード。
     */
    public static final String PROP_KEY_DB_PASSWORD = "jdbc.password";
    /**
     * プロパティKEY DBMSのコネクション取得時のチューニングパラ-メータを記述したプロパティファイル。
     */
    public static final String PROP_KEY_NAME_DB_PRAM = "jdbc.param-conf-path";

    /*
     * bulkloader-conf-hc.properties
     */
    /**
     * プロパティKEY 出力ファイルの圧縮有無。
     */
    public static final String PROP_KEY_IMP_SEQ_FILE_COMP_TYPE = "import.seq-comp-type";
    /**
     * The property key of Cache Builder executable file name.
     * @since 0.2.3
     */
    @Deprecated
    public static final String PROP_KEY_CACHE_BUILDER_SHELL_NAME = "import.cache-build-shell-name";
    /**
     * The property key of maximim number of parallel Cache Builder.
     * @since 0.2.3
     */
    public static final String PROP_KEY_CACHE_BUILDER_PARALLEL = "import.cache-build-max-parallel";
    /**
     * プロパティKEY Exportファイルの圧縮有無。
     */
    public static final String PROP_KEY_EXP_FILE_COMP_TYPE = "export.zip-comp-type";
    /**
     * プロパティKEY エクスポートファイルをTSVファイルに変換する際のファイル分割サイズ。
     */
    public static final String PROP_KEY_EXP_LOAD_MAX_SIZE = "export.tsv-max-size";
    /**
     * プロパティKEY Extractor,Collectorのファイル入出力位置をワーキングディレクトリベースとするか。
     * trueにすると、スタンドアロンモードでも動作可能となる。開発環境ではtrueにする。
     * @deprecated use {@link #PROP_KEY_BASE_PATH} instead
     */
    @Deprecated
    public static final String PROP_KEY_WORKINGDIR_USE = "hadoop-cluster.workingdir.use";

    /*
     * プロパティのデフォルト値
     */
    /**
     * プロパティデフォルト値 log4j.xmlのパス。
     */
    public static final String PROP_DEFAULT_LOG_CONF_PATH = "bulkloader/conf/log4j.xml";
    /**
     * プロパティデフォルト値 Importファイルの圧縮有無。
     */
    public static final String PROP_DEFAULT_IMP_FILE_COMP_TYPE = FileCompType.STORED.getSymbol();
    /**
     * プロパティデフォルト値 Importファイルの圧縮時のバッファサイズ。
     */
    public static final String PROP_DEFAULT_IMP_FILE_COMP_BUFSIZE = "32768";
    /**
     * プロパティデフォルト値 Importerのリトライ回数。
     */
    public static final String PROP_DEFAULT_IMP_RETRY_COUNT = "3";
    /**
     * プロパティデフォルト値 Importerのリトライインターバル。
     */
    public static final String PROP_DEFAULT_IMP_RETRY_INTERVAL = "10";
    /**
     * プロパティデフォルト値 Exportファイルの圧縮有無。
     */
    public static final String PROP_DEFAULT_EXP_FILE_COMP_TYPE = FileCompType.STORED.getSymbol();
    /**
     * プロパティデフォルト値 Exportファイルの圧縮時のバッファサイズ。
     */
    public static final String PROP_DEFAULT_EXP_FILE_COMP_BUFSIZE = "32768";
    /**
     * プロパティデフォルト値 Exporterのリトライ回数。
     */
    public static final String PROP_DEFAULT_EXP_RETRY_COUNT = "3";
    /**
     * プロパティデフォルト値 Exporterのリトライインターバル。
     */
    public static final String PROP_DEFAULT_EXP_RETRY_INTERVAL = "10";
    /**
     * プロパティデフォルト値 エクスポートファイルをTSVファイルに変換する際のファイル分割サイズ。
     */
    public static final String PROP_DEFAULT_EXP_LOAD_MAX_SIZE = "16777216";
    /**
     * プロパティデフォルト値 エクスポート処理でExport対象テーブルにデータをコピーする時の最大レコード数。
     */
    public static final String PROP_DEFAULT_EXP_COPY_MAX_RECORD = "100000";
    /**
     * プロパティデフォルト値 ファイル入出力位置をワーキングディレクトリベースとするか。
     */
    public static final String PROP_DEFAULT_WORKINGDIR_USE = "false";
    /**
     * プロパティデフォルト値 インポート処理が正常終了した場合に、Exporterで生成したインポート中間TSVファイルを削除するか。
     */
    public static final String PROP_DEFAULT_IMPORT_TSV_DELETE = TsvDeleteType.TRUE.getSymbol();
    /**
     * プロパティデフォルト値 エクスポート処理が正常終了した場合に、Exporterで生成したエクスポート中間TSVファイルを削除するか。
     */
    public static final String PROP_DEFAULT_EXPORT_TSV_DELETE = TsvDeleteType.TRUE.getSymbol();
    /**
     * プロパティデフォルト値 出力ファイルの圧縮有無。
     */
    public static final String PROP_DEFAULT_IMP_SEQ_FILE_COMP_TYPE = CompressionType.NONE.name();
    /**
     * The default property value of maximim number of parallel Cache Builder.
     * @since 0.2.3
     */
    public static final String PROP_DEFAULT_CACHE_BUILDER_PARALLEL = "1";

    /*
     * パス・ファイル名の固定値
     */

    /**
     * Root path of ThunderGate install directory.
     * @since 0.4.0
     */
    public static final String PATH_REMOTE_ROOT = "bulkloader/";

    /**
     * Relative path to the remote extractor script (from framework installation home).
     * @since 0.4.0
     */
    public static final String PATH_REMOTE_EXTRACTOR = "libexec/extractor.sh";

    /**
     * Relative path to the remote collector script (from framework installation home).
     * @since 0.4.0
     */
    public static final String PATH_REMOTE_COLLECTOR = "libexec/collector.sh";

    /**
     * Relative path to the fetching remote cache info script (from framework installation home).
     * @since 0.4.0
     */
    public static final String PATH_REMOTE_CACHE_INFO = "libexec/get-cache-info.sh";

    /**
     * Relative path to the deleting remote cache contents script (from framework installation home).
     * @since 0.4.0
     */
    public static final String PATH_REMOTE_CACHE_DELETE = "libexec/delete-cache-storage.sh";

    /**
     * Relative path to the building cache contents script (from framework installation home).
     * @since 0.4.0
     */
    public static final String PATH_LOCAL_CACHE_BUILD = "libexec/hadoop-build-cache.sh";

    /**
     * DBMSの接続情報を記述したプロパティファイルの接尾辞。
     */
    public static final String JDBC_PROP_NAME = "-jdbc.properties";
    /**
     * ログメッセージプロパティのファイル名。
     */
    public static final String LOG_MESSAGE_FILE = "bulkloader_message.properties";
    /**
     * 設定格納ファイルパス。
     */
    public static final String PROP_FILE_PATH = "conf";
    /**
     * ジョブフローパッケージ格納ファイルパス。
     */
    public static final String JOBFLOW_PACKAGE_PATH_BEFORE = "batchapps";
    /**
     * ジョブフローパッケージ格納ファイルパス。
     */
    public static final String JOBFLOW_PACKAGE_PATH_AFTER = "lib";
    /**
     * Importファイルのプレフィックス。
     */
    public static final String IMPORT_FILE_PREFIX = "IMP";
    /**
     * Importファイルの拡張子。
     */
    public static final String IMPORT_FILE_EXTENSION = ".tsv";
    /**
     * Importファイルの区切り文字。
     */
    public static final String IMPORT_FILE_DELIMITER = "_";
    /**
     * Exportファイルのプレフィックス。
     */
    public static final String EXPORT_FILE_PREFIX = "EXP";
    /**
     * Importファイルの区切り文字。
     */
    public static final String EXPORT_FILE_DELIMITER = "_";
    /**
     * Exportファイルの拡張子。
     */
    public static final String EXPORT_FILE_EXTENSION = ".tsv";
    /**
     * ジョブフローパッケージのプレフィックス。
     */
    public static final String DSL_PROP_PREFIX = "jobflow-";
    /**
     * ジョブフローパッケージの拡張子。
     */
    public static final String DSL_PROP_EXTENSION = ".jar";
    /**
     * HDFSのパス固定値。
     * @deprecated use {@link #PROP_KEY_BASE_PATH} instead
     */
    @Deprecated
    public static final String HDFSFIXED_PATH = "/user";
    /**
     * HDFSのパスの変数名(ユーザー名)。
     */
    public static final String HDFS_PATH_VARIABLE_USER = "user";
    /**
     * HDFSのパスの変数名(実行時ID)。
     */
    public static final String HDFS_PATH_VARIABLE_EXECUTION_ID = "execution_id";


    /*
     * テーブルのカラム名・デフォルト値・設定
     */
    /**
     * Exportテンポラリテーブルのプレフィックス。
     */
    public static final String EXP_TEMP_TABLE_PREIX = "EXPORT_TEMP_";
    /**
     * Exportテンポラリテーブルの区切り文字。
     */
    public static final String EXPORT_TEMP_TABLE_DELIMITER = "_";
    /**
     * 重複フラグテーブル名の末尾の文字。
     */
    public static final String DUPLECATE_FLG_TABLE_END = "_DF";

    /**
     * Export対象テーブルのシステムカラムの配列。
     * @return Export対象テーブルのシステムカラム
     */
    public static List<String> getSystemColumns() {
        List<String> list = new ArrayList<String>();
        list.add(Constants.getSidColumnName());
        list.add(Constants.getVersionColumnName());
        list.add(Constants.getRegisteredDateTimeColumnName());
        list.add(Constants.getUpdatedDateTimeColumnName());
        return list;
    }
    /**
     * 異常データテーブルのシステムカラムの配列。
     * エラーコードのカラムは任意に指定できるためここには含まれない。
     * @return 異常データテーブルのシステムカラム
     */
    public static List<String> getErrorSystemColumns() {
        List<String> list = new ArrayList<String>();
        list.add(Constants.getSidColumnName());
        list.add(Constants.getVersionColumnName());
        list.add(Constants.getRegisteredDateTimeColumnName());
        list.add(Constants.getUpdatedDateTimeColumnName());
        return list;
    }
    /**
     * システムIDのカラム名。
     */
    private static String sidColumnName = "SID";
    /**
     * バージョン番号のカラム名。
     */
    private static String versionColumnName = "VERSION_NO";
    /**
     * 登録日時のカラム名。
     */
    private static String registeredDateTimeColumnName = "RGST_DATETIME";
    /**
     * 更新日時のカラム名。
     */
    private static String updatedDateTimeColumnName = "UPDT_DATETIME";
    /**
     * テンポラリSIDのカラム名。
     */
    private static String temporarySidColumnName = "__TEMP_SID";

    /**
     * バージョン番号の初期値。
     */
    public static final long SYS_COLUMN_DEFAULT_VERSION_NO = 1;
    /**
     * バージョン番号の増分。
     */
    public static final long SYS_COLUMN_INCREMENT_VERSION_NO = 1;

    /*
     * その他
     */

    /**
     * Importer/Exporterがサブプロセスの標準入力・標準出力を読み込む時の文字コード。
     */
    public static final String SUB_PROCESS_CHAR_SET = "UTF-8";


    /**
     * インスタンスの作成を禁止する。
     */
    private Constants() {
        return;
    }

    /**
     * テーブルシステカラムの定数を初期化する。
     */
    public static void setSystemColumn() {
        String sid = ConfigurationLoader.getProperty(PROP_KEY_SYS_COLUMN_SID);
        if (!isEmpty(sid)) {
            setSidColumnName(sid);
        }
        String versionNo = ConfigurationLoader.getProperty(PROP_KEY_SYS_COLUMN_VERSION_NO);
        if (!isEmpty(versionNo)) {
            setVersionColumnName(versionNo);
        }
        String rgstDate = ConfigurationLoader.getProperty(PROP_KEY_SYS_COLUMN_RGST_DATE);
        if (!isEmpty(rgstDate)) {
            setRegisteredDateTimeColumnName(rgstDate);
        }
        String updtDateDate = ConfigurationLoader.getProperty(PROP_KEY_SYS_COLUMN_UPDT_DATE);
        if (!isEmpty(updtDateDate)) {
            setUpdatedDateTimeColumnName(updtDateDate);
        }
        String tempSid = ConfigurationLoader.getProperty(PROP_KEY_SYS_COLUMN_TEMP_SID);
        if (!isEmpty(tempSid)) {
            setTemporarySidColumnName(tempSid);
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
     * 現在の環境で利用する変数表を新しく作成して返す。
     * @return 現在の環境で利用する変数表
     */
    public static VariableTable createVariableTable() {
        VariableTable variables = new VariableTable(RedefineStrategy.OVERWRITE);
        variables.defineVariable("__caller__", String.valueOf(ConfigurationLoader.getEnvProperty("USER")));
        String args = ConfigurationLoader.getEnvProperty(ENV_ARGS);
        if (args != null) {
            variables.defineVariables(args);
        }
        return variables;
    }
    /**
     * システムIDのカラム名を設定する。
     * @param name システムIDのカラム名
     */
    public static void setSidColumnName(String name) {
        sidColumnName = name;
    }
    /**
     * システムIDのカラム名を返す。
     * @return システムIDのカラム名
     */
    public static String getSidColumnName() {
        return sidColumnName;
    }
    /**
     * バージョン番号のカラム名を設定する。
     * @param name バージョン番号のカラム名を返す。
     */
    public static void setVersionColumnName(String name) {
        versionColumnName = name;
    }
    /**
     * バージョン番号のカラム名を返す。
     * @return バージョン番号のカラム名
     */
    public static String getVersionColumnName() {
        return versionColumnName;
    }
    /**
     * 登録日時のカラム名を設定する。
     * @param name 登録日時のカラム名
     */
    public static void setRegisteredDateTimeColumnName(String name) {
        registeredDateTimeColumnName = name;
    }
    /**
     * 登録日時のカラム名を返す。
     * @return 登録日時のカラム名
     */
    public static String getRegisteredDateTimeColumnName() {
        return registeredDateTimeColumnName;
    }
    /**
     * 更新日時のカラム名を設定する。
     * @param name 更新日時のカラム名
     */
    public static void setUpdatedDateTimeColumnName(String name) {
        updatedDateTimeColumnName = name;
    }
    /**
     * 更新日時のカラム名を返す。
     * @return 更新日時のカラム名
     */
    public static String getUpdatedDateTimeColumnName() {
        return updatedDateTimeColumnName;
    }
    /**
     * テンポラリSIDのカラム名を設定する。
     * @param name テンポラリSIDのカラム名
     */
    public static void setTemporarySidColumnName(String name) {
        temporarySidColumnName = name;
    }
    /**
     * テンポラリSIDのカラム名を返す。
     * @return テンポラリSIDのカラム名
     */
    public static String getTemporarySidColumnName() {
        return temporarySidColumnName;
    }
}
