/**
 * Copyright 2011 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the );
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

/**
 * ログ出力で使用されるメッセージIDを一元管理するためのクラスです。
 * このクラスはメッセージファイルにより自動生成されたものなので、直接編集しないでください。
 */
public final class MessageIdConst {

    /**
     * [ERROR] DBコネクションの取得に失敗しました。エラー内容：{0} .
     */
    public static final String CMN_DB_CONN_ERROR = "TG-COMMON-00001";

    /**
     * [ERROR] Import処理用のDSLプロパティの読み込みに失敗しました。エラー内容：{0}、ターゲット名：{1}、ジョブフローID：{2}、ファイル名{3} .
     */
    public static final String CMN_IMP_DSL_LOADERROR = "TG-COMMON-00002";

    /**
     * [ERROR] Export処理用のDSLプロパティの読み込みに失敗しました。エラー内容：{0}、ターゲット名：{1}、ジョブフローID：{2}、ファイル名{3} .
     */
    public static final String CMN_EXP_DSL_LOADERROR = "TG-COMMON-00003";

    /**
     * [ERROR] Import処理用のDSLプロパティのチェックに失敗しました。エラー内容：{0}、ターゲット名：{1}、ジョブフローID：{2}、テーブル名：{3}、ファイル名{4} .
     */
    public static final String CMN_IMP_DSL_CHECKERROR = "TG-COMMON-00004";

    /**
     * [ERROR] Export処理用のDSLプロパティのチェックに失敗しました。エラー内容：{0}、ターゲット名：{1}、ジョブフローID：{2}、テーブル名：{3}、ファイル名{4} .
     */
    public static final String CMN_EXP_DSL_CHECKERROR = "TG-COMMON-00005";

    /**
     * [WARN] Import処理用のDSLプロパティのチェックで不正な設定がありました。不正内容：{0}、ターゲット名：{1}、ジョブフローID：{2}、テーブル名：{3}、ファイル名{4} .
     */
    public static final String CMN_IMP_DSL_CHECKWARN = "TG-COMMON-00006";

    /**
     * [WARN] Export処理用のDSLプロパティのチェックで不正な設定がありました。不正内容：{0}、ターゲット名：{1}、ジョブフローID：{2}、テーブル名：{3}、ファイル名{4} .
     */
    public static final String CMN_EXP_DSL_CHECKWARN = "TG-COMMON-00007";

    /**
     * [ERROR] プロパティファイルの設定値が不正です。不正内容：{0} .
     */
    public static final String CMN_PROP_CHECK_ERROR = "TG-COMMON-00008";

    /**
     * [ERROR] カラム名{0}はExport中間TSVファイルに含まれません。 .
     */
    public static final String CMN_COLUMN_INCLUDE_ERROR = "TG-COMMON-00009";

    /**
     * [ERROR] DBMSの接続情報を記述したプロパティファイルの読み込みに失敗しました。ファイル名：{0} .
     */
    public static final String CMN_JDBCCONF_LOAD_ERROR = "TG-COMMON-00010";

    /**
     * [ERROR] DBMSの接続情報を記述したプロパティファイルの設定値が不正です。不正内容：{0} .
     */
    public static final String CMN_JDBCCONF_CHECK_ERROR = "TG-COMMON-00011";

    /**
     * [ERROR] DBMSの接続情報を記述したプロパティファイルの読み込みに失敗しました。ファイル名：{0} .
     */
    public static final String CMN_JDBCCONF_READ_ERROR = "TG-COMMON-00012";

    /**
     * [ERROR] JDBCドライバのロードに失敗しました。JDBCドライバ名：{0} .
     */
    public static final String CMN_JDBCDRIVER_LOAD_ERROR = "TG-COMMON-00013";

    /**
     * [ERROR] SQLの実行に失敗しました。SQL文：{0} パラメータ：{1} .
     */
    public static final String CMN_DB_SQL_EXEC_ERROR = "TG-COMMON-00014";

    /**
     * [ERROR] トランザクションのコミットに失敗しました。 .
     */
    public static final String CMN_DB_CONN_COMMIT_ERROR = "TG-COMMON-00015";

    /**
     * [ERROR] トランザクションのロールバックに失敗しました。 .
     */
    public static final String CMN_DB_CONN_ROLLBACK_ERROR = "TG-COMMON-00016";

    /**
     * [ERROR] Importファイルを生成するディレクトリが存在しません。ディレクトリ名：{0} .
     */
    public static final String CMN_IMP_DIR_NOT_FIND_ERROR = "TG-COMMON-00017";

    /**
     * [ERROR] HDFSのURIが不正です。URI：{0} .
     */
    public static final String CMN_IMP_HDFS_PATH_ERROR = "TG-COMMON-00018";

    /**
     * [ERROR] HDFSのファイルシステムの取得に失敗しました。URI：{0} .
     */
    public static final String CMN_IMP_HDFS_FILESYS_ERROR = "TG-COMMON-00019";

    /**
     * [INFO] 同一ジョブフロー実行IDで複数プロセスが動作しない為のロックを取得します。SQL：{0} ジョブフロー実行ID：{1} .
     */
    public static final String CMN_EXECUTIONID_LOCK = "TG-COMMON-00020";

    /**
     * [INFO] 同一ジョブフロー実行IDで複数プロセスが動作しない為のロックを解除します。 .
     */
    public static final String CMN_EXECUTIOND_LOCK_RELEASE = "TG-COMMON-00021";

    /**
     * [DEBUG] SQLを実行します。SQL：{0} パラメータ：{1} .
     */
    public static final String CMN_SQL_EXECUTE_BEFORE = "TG-COMMON-00022";

    /**
     * [DEBUG] SQLを実行しました。実行時間(ミリ秒)：{0} 件数：{1} SQL：{2} パラメータ：{3} .
     */
    public static final String CMN_SQL_EXECUTE_AFTER = "TG-COMMON-00023";

    /**
     * [DEBUG] トランザクションをコミットします。 .
     */
    public static final String CMN_COMMIT_EXECUTE_BEFORE = "TG-COMMON-00024";

    /**
     * [DEBUG] トランザクションをコミットしました。コミット時間(ミリ秒)：{0} .
     */
    public static final String CMN_COMMIT_EXECUTE_AFTER = "TG-COMMON-00025";

    /**
     * [DEBUG] トランザクションをロールバックします。 .
     */
    public static final String CMN_ROLLBACK_EXECUTE_BEFORE = "TG-COMMON-00026";

    /**
     * [DEBUG] トランザクションをロールバックしました。コミット時間(ミリ秒)：{0} .
     */
    public static final String CMN_ROLLBACK_EXECUTE_AFTER = "TG-COMMON-00027";

    /**
     * [ERROR] ストリームのリダイレクトに失敗しました。 .
     */
    public static final String CMN_LOG_REDIRECT_ERROR = "TG-COMMON-00028";

    /**
     * [INFO] Importerの処理を開始します。開始時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_START = "TG-IMPORTER-01001";

    /**
     * [INFO] Importerの処理を正常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_EXIT = "TG-IMPORTER-01002";

    /**
     * [ERROR] Importerで初期化処理に失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_INIT_ERROR = "TG-IMPORTER-01003";

    /**
     * [ERROR] Importerでロック取得処理に失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_LOCK_ERROR = "TG-IMPORTER-01004";

    /**
     * [ERROR] ImporterでImport対象ファイル生成処理に失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_CREATEFILE_ERROR = "TG-IMPORTER-01005";

    /**
     * [ERROR] ImporterでImport対象データ送信処理に失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_SENDDATA_ERROR = "TG-IMPORTER-01006";

    /**
     * [ERROR] Importerでキャッシュの取り出し処理に失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_CACHE_ERROR = "TG-IMPORTER-01007";

    /**
     * [ERROR] Importerに指定するパラメータが不正です。不正内容：{0} 値：{1} .
     */
    public static final String IMP_PARAMCHECK_ERROR = "TG-IMPORTER-01008";

    /**
     * [ERROR] Importerでパラメータの入力チェックに失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_PARAM_ERROR = "TG-IMPORTER-01009";

    /**
     * [ERROR] Importerで不明なエラーが発生しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_EXCEPRION = "TG-IMPORTER-01010";

    /**
     * [INFO] ジョブフロー実行テーブルにジョブフローの実行を記録しました。正常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_TARGET_NO_EXIST_SUCCESS = "TG-IMPORTER-01011";

    /**
     * [ERROR] ジョブフロー実行テーブルへジョブフロー実行の記録に失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_INSERT_RNNINGJOBFLOW_ERROR = "TG-IMPORTER-01012";

    /**
     * [ERROR] 指定されたジョブフロー実行IDは他プロセスにより処理中のため、Importerを異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_INSTANCE_ID_LOCKED = "TG-IMPORTER-01013";

    /**
     * [ERROR] ジョブフロー実行IDによる排他制御に失敗しました。異常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_INSTANCE_ID_LOCK_ERROR = "TG-IMPORTER-01014";

    /**
     * [INFO] ジョブフロー実行IDによる排他制御を行います。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_INSTANCE_ID_LOCK = "TG-IMPORTER-01015";

    /**
     * [INFO] ジョブフロー実行IDによる排他制御に成功しました。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_INSTANCE_ID_LOCK_SUCCESS = "TG-IMPORTER-01016";

    /**
     * [INFO] Import対象テーブルのロックを取得します。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_LOCK = "TG-IMPORTER-01017";

    /**
     * [INFO] Import対象テーブルのロック取得に成功しました。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_LOCK_SUCCESS = "TG-IMPORTER-01018";

    /**
     * [INFO] Import対象テーブルが存在しないため、インポートは行わずにジョブフロー実行テーブルにジョブフロー実行の記録のみを行います。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_TARGET_NO_EXIST = "TG-IMPORTER-01019";

    /**
     * [INFO] Import対象テーブルが存在しないため、インポートは行わずにImporterを正常終了します。終了時刻：{0} Import処理区分：{1} ターゲット名：{2} バッチID：{3} ジョブフローID：{4} ジョブフロー実行ID：{5} .
     */
    public static final String IMP_TARGET_NO_EXIST_SECONDARY = "TG-IMPORTER-01020";

    /**
     * [INFO] Import対象ファイルの生成を行います。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_CREATEFILE = "TG-IMPORTER-01021";

    /**
     * [INFO] Import対象ファイルの生成に成功しました。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_CREATEFILE_SUCCESS = "TG-IMPORTER-01022";

    /**
     * [INFO] Import対象ファイルの送信を行います。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_SENDDATA = "TG-IMPORTER-01023";

    /**
     * [INFO] Import対象ファイルの送信をに成功しました。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_SENDDATA_SUCCESS = "TG-IMPORTER-01024";

    /**
     * [INFO] 生成したImport対象ファイル（TSV中間ファイル）を削除します。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_TSV_FILE_DELETE = "TG-IMPORTER-01025";

    /**
     * [INFO] インポート処理が正常終了した場合のTSVファイル削除有無に「0：削除しない」が設定されているため、生成したImport対象ファイル（TSV中間ファイル）を削除しません。Import処理区分：{0} ターゲット名：{1} バッチID：{2} ジョブフローID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String IMP_TSV_FILE_NOT_DELETE = "TG-IMPORTER-01026";

    /**
     * [INFO] 同一ジョブフロー実行IDのレコードが存在するためロック取得処理をスキップします。ターゲット名：{0} ジョブフロー実行ID：{1} ジョブフローSID：{2} .
     */
    public static final String IMP_EXISTS_JOBNET_INSTANCEID = "TG-IMPORTER-02001";

    /**
     * [ERROR] ロック取得処理でリトライを行い、リトライインターバル分スリープ中に例外が発生しました。 .
     */
    public static final String IMP_GET_LOCK_SLEEP_ERROR = "TG-IMPORTER-02002";

    /**
     * [ERROR] ロック取得処理がリトライオーバーしました。異常終了します。 .
     */
    public static final String IMP_GET_LOCK_RETRY_ORVER = "TG-IMPORTER-02003";

    /**
     * [WARN] ロック取得処理でリトライ可能なエラーが発生しました。エラー内容：{0}、テーブル名{1} .
     */
    public static final String IMP_GET_LOCK_RETRY = "TG-IMPORTER-02004";

    /**
     * [INFO] ロック取得のトランザクションを開始します。ターゲット名：{0} ジョブフロー実行ID：{1} .
     */
    public static final String IMP_LOCK__TRAN_START = "TG-IMPORTER-02005";

    /**
     * [INFO] ロック取得のトランザクションを終了します。ターゲット名：{0} ジョブフロー実行ID：{1} .
     */
    public static final String IMP_LOCK__TRAN_END = "TG-IMPORTER-02006";

    /**
     * [INFO] ジョブフロー実行テーブルにジョブフローの実行を記録します。SQL：{0} バッチID{1} ジョブフローID：{2} ターゲット名{3} ジョブフロー実行ID：{4} 終了予定時刻：{5} .
     */
    public static final String IMP_INSERT_RNNINGJOBFLOW = "TG-IMPORTER-02007";

    /**
     * [INFO] テーブルロックテーブルのImport対象テーブル行のTXロックを取得し、ロック取得/解除操作の排他制御を行います。SQL：{0} .
     */
    public static final String IMP_LOCK_EXCLUSIVE = "TG-IMPORTER-02008";

    /**
     * [INFO] ジョブフロー設置に従い、Import対象テーブルに対するロックを取得します。Import対象テーブル名：{0} ロック取得タイプ：{1} ロック済みの場合の挙動：{2} 検索条件：{3} .
     */
    public static final String IMP_IMPORT_TARGET_LOCK = "TG-IMPORTER-02009";

    /**
     * [INFO] Import対象テーブルに対するロック取得を終了します。Import対象テーブル名：{0} ロック取得タイプ：{1} ロック済みの場合の挙動：{2} 検索条件：{3} .
     */
    public static final String IMP_IMPORT_TARGET_LOCK_END = "TG-IMPORTER-02010";

    /**
     * [INFO] ロック取得タイプが「ロックしない」、ロック済みの場合の挙動が「ロック有無に関わらず処理対象とする」のため、当該Import対象テーブルに対するロック取得及びロック済みのチェックは行いません。Import対象テーブル名：{0}  .
     */
    public static final String IMP_NONE_FORCE_DONE = "TG-IMPORTER-02011";

    /**
     * [INFO] ロック取得タイプが「行ロック」、ロック済みの場合の挙動が「処理対象から外す」であり、当該Import対象テーブルに対するテーブルロックが既に取得されているため、ロック取得は行いません。Import対象テーブル名：{0}  .
     */
    public static final String IMP_TABLE_LOCKED = "TG-IMPORTER-02012";

    /**
     * [INFO] ロック取得タイプが「ロックしない」、ロック済みの場合の挙動が「エラーとする」であり、当該Import対象テーブルに対するテーブルロック及びレコードロックが取得されていないため、ロック取得は行わずにロック取得処理を終了します。Import対象テーブル名：{0}  .
     */
    public static final String IMP_NONE_ERROR_DONE = "TG-IMPORTER-02013";

    /**
     * [INFO] Import対象テーブルに対するテーブルロックを取得します。SQL：{0} ジョブフローSID：{1} テーブル名：{2} .
     */
    public static final String IMP_GET_TABLE_LOCK = "TG-IMPORTER-02014";

    /**
     * [INFO] Import対象テーブルに対するレコードロックを取得します。SQL：{0} ジョブフローSID：{1} .
     */
    public static final String IMP_GET_RECORD_LOCK = "TG-IMPORTER-02015";

    /**
     * [ERROR] Importファイル生成処理で既に存在するImportファイルの削除に失敗しました。ファイル名：{0} .
     */
    public static final String IMP_EXISTSFILE_DELETE_ERROR = "TG-IMPORTER-03001";

    /**
     * [ERROR] Importファイル生成処理で対象データが存在せず、0byteのファイル生成に失敗しました。 .
     */
    public static final String IMP_CREATEFILE_EXCEPTION = "TG-IMPORTER-03002";

    /**
     * [INFO] Importファイルを生成します。Import対象テーブル名：{0} ロック取得タイプ：{1} ファイル名：{2} .
     */
    public static final String IMP_CREATE_FILE = "TG-IMPORTER-03003";

    /**
     * [INFO] Importファイルを生成しました。Import対象テーブル名：{0} ロック取得タイプ：{1} ファイル名：{2} .
     */
    public static final String IMP_CREATE_FILE_SUCCESS = "TG-IMPORTER-03004";

    /**
     * [INFO] Importファイルが生成されなかった為、空のImportファイルを生成しました。Import対象テーブル名：{0} ロック取得タイプ：{1} ファイル名：{2} .
     */
    public static final String IMP_CREATE_ZERO_FILE = "TG-IMPORTER-03005";

    /**
     * [INFO] ジョブフローSIDを条件にレコードを抽出してファイルを生成します。SQL：{0} ジョブフローSID：{1} .
     */
    public static final String IMP_CREATE_FILE_WITH_JOBFLOWSID = "TG-IMPORTER-03006";

    /**
     * [INFO] 検索条件でレコードを抽出してファイルを生成します。SQL：{0} .
     */
    public static final String IMP_CREATE_FILE_WITH_CONDITION = "TG-IMPORTER-03007";

    /**
     * [ERROR] Importファイル送信処理でエラーが発生しました。異常終了します。エラー原因：{0} .
     */
    public static final String IMP_SENDFILE_EXCEPTION = "TG-IMPORTER-04001";

    /**
     * [ERROR] Importファイル送信処理で起動したサブプロセスが異常終了しました。終了コード：{0} .
     */
    public static final String IMP_EXTRACTOR_ERROR = "TG-IMPORTER-04002";

    /**
     * [INFO] Importファイル送信の為のサブプロセスを起動します。SSHのパス：{0} マスターノードのホスト：{1} マスターノードのユーザー：{2} Extractorのシェル名：{3} ターゲット名：{4} バッチID：{5} ジョブフローID：{6} ジョブフロー実行ID：{7} .
     */
    public static final String IMP_START_SUB_PROCESS = "TG-IMPORTER-04003";

    /**
     * [INFO] Importファイルを送信します。Import対象テーブル名：{0} Importファイル：{1} ImportファイルのZipEntry名：{2} ZIP圧縮有無：{3} .
     */
    public static final String IMP_FILE_SEND = "TG-IMPORTER-04004";

    /**
     * [INFO] Importファイルを送信しました。Import対象テーブル名：{0} Importファイル：{1} ImportファイルのZipEntry名：{2} ZIP圧縮有無：{3} .
     */
    public static final String IMP_FILE_SEND_END = "TG-IMPORTER-04005";

    /**
     * [WARN] Importファイル削除処理でImportファイルの削除に失敗しました。ファイル名：{0} .
     */
    public static final String IMP_FILEDELETE_ERROR = "TG-IMPORTER-05001";

    /**
     * [WARN] Importファイル削除処理でImportファイル格納ディレクトリの削除に失敗しました。ディレクトリ名：{0} .
     */
    public static final String IMP_DIRDELETE_ERROR = "TG-IMPORTER-05002";

    /**
     * [INFO] Extractorの処理を開始します。開始時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String EXT_START = "TG-EXTRACTOR-01001";

    /**
     * [INFO] Extractorの処理を正常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String EXT_EXIT = "TG-EXTRACTOR-01002";

    /**
     * [ERROR] Extractorで初期化処理に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String EXT_INIT_ERROR = "TG-EXTRACTOR-01003";

    /**
     * [ERROR] ExtractorでImportファイルのHDFSへの書き出しに失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String EXT_CREATEFILE_ERROR = "TG-EXTRACTOR-01004";

    /**
     * [ERROR] Extractorに指定するパラメータが不正です。不正内容：{0}、値：{1} .
     */
    public static final String EXT_PARAMCHECK_ERROR = "TG-EXTRACTOR-01005";

    /**
     * [ERROR] Extractorでパラメータの入力チェックに失敗しました。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String EXT_PARAM_ERROR = "TG-EXTRACTOR-01006";

    /**
     * [ERROR] Extractorで不明なエラーが発生しました。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String EXT_EXCEPRION = "TG-EXTRACTOR-01007";

    /**
     * [INFO] Importファイルを受取り、HDFSに書き出します。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3}、ユーザー名：{4} .
     */
    public static final String EXT_CREATEFILE = "TG-EXTRACTOR-01008";

    /**
     * [INFO] ImportファイルをHDFSへ書き出しました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3}、ユーザー名：{4} .
     */
    public static final String EXT_CREATEFILE_SUCCESS = "TG-EXTRACTOR-01009";

    /**
     * [ERROR] ImportファイルをHDFSに書き出す処理でエラーが発生しました。異常終了します。エラー原因：{0} .
     */
    public static final String EXT_CREATE_HDFSFILE_EXCEPTION = "TG-EXTRACTOR-02001";

    /**
     * [INFO] ImportファイルをHDFSに書き出します。Import対象テーブル名：{0} HDFSのパス：{1} Modelクラス：{2} .
     */
    public static final String EXT_CREATE_HDFSFILE = "TG-EXTRACTOR-02002";

    /**
     * [INFO] ImportファイルをHDFSに書き出しました。Import対象テーブル名：{0} HDFSのパス：{1} Modelクラス：{2} .
     */
    public static final String EXT_CREATE_HDFSFILE_SUCCESS = "TG-EXTRACTOR-02003";

    /**
     * [WARN] HDFSにSequenceFileをインポートする時にSequenceFileを圧縮するかの指定が不正です。「圧縮なし」の設定を適用します。圧縮指定：{0} .
     */
    public static final String EXT_SEQ_COMP_TYPE_FAIL = "TG-EXTRACTOR-02004";

    /**
     * [INFO] Exporterの処理を開始します。開始時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_START = "TG-EXPORTER-01001";

    /**
     * [INFO] Exporterの処理を正常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_EXIT = "TG-EXPORTER-01002";

    /**
     * [ERROR] Exporterで初期化処理に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_INIT_ERROR = "TG-EXPORTER-01003";

    /**
     * [ERROR] Exporterで不明なエラーが発生しました。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_EXCEPRION = "TG-EXPORTER-01004";

    /**
     * [ERROR] Exporterに指定するパラメータが不正です。不正内容：{0} 値：{1} .
     */
    public static final String EXP_PARAMCHECK_ERROR = "TG-EXPORTER-01005";

    /**
     * [ERROR] Exporterでパラメータの入力チェックに失敗しました。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_PARAM_ERROR = "TG-EXPORTER-01006";

    /**
     * [ERROR] ExporterでExportファイルの受信処理に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_FILERECEIVE_ERROR = "TG-EXPORTER-01007";

    /**
     * [ERROR] ExporterでテンポラリテーブルへのExportファイルのロードに失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_FILELOAD_ERROR = "TG-EXPORTER-01008";

    /**
     * [ERROR] Exporterでロックの解除に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_RELEASELOCK_ERROR = "TG-EXPORTER-01009";

    /**
     * [ERROR] ExporterでジョブフローSIDの取得に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_GETJOBFLOWSID_ERROR = "TG-EXPORTER-01010";

    /**
     * [ERROR] 終了したジョブフローに対するExport処理を再実行又は、Importerが実行されていないジョブフローに対するExport処理が実行されました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_JOBFLOW_EXIT_ERROR = "TG-EXPORTER-01011";

    /**
     * [ERROR] Exporterで当該ジョブフローSIDに対応するテンポラリテーブルの情報取得に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_TEMP_INFO_ERROR = "TG-EXPORTER-01012";

    /**
     * [ERROR] Exporterで当該ジョブフローSIDに対応するテンポラリテーブルの削除に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} ジョブフローSID：{5} .
     */
    public static final String EXP_TEMP_DELETE_ERROR = "TG-EXPORTER-01013";

    /**
     * [ERROR] ExporterでテンポラリテーブルからExport対象テーブルへのデータのコピーに失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_DATA_COPY_ERROR = "TG-EXPORTER-01014";

    /**
     * [ERROR] Export対象テーブルに更新対象のレコードが存在しないデータを除いて処理を行いました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_DATA_UPDATE_NOT_EXIT = "TG-EXPORTER-01015";

    /**
     * [ERROR] 指定されたジョブフロー実行IDは他プロセスにより処理中のため、Exporterを異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_INSTANCE_ID_LOCKED = "TG-EXPORTER-01016";

    /**
     * [ERROR] ジョブフロー実行IDによる排他制御に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4} .
     */
    public static final String EXP_INSTANCE_ID_LOCK_ERROR = "TG-EXPORTER-01017";

    /**
     * [INFO] ジョブフロー実行IDによる排他制御を行います。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_INSTANCE_ID_LOCK = "TG-EXPORTER-01018";

    /**
     * [INFO] ジョブフロー実行IDによる排他制御に成功しました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_INSTANCE_ID_LOCK_SUCCESS = "TG-EXPORTER-01019";

    /**
     * [INFO] 当該ジョブフローSIDに対応するテンポラリテーブルを削除します。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} ジョブフローSID：{4} .
     */
    public static final String EXP_TEMP_DELETE = "TG-EXPORTER-01020";

    /**
     * [INFO] 当該ジョブフローSIDに対応するテンポラリテーブルを削除しました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} ジョブフローSID：{4} .
     */
    public static final String EXP_TEMP_DELETE_SUCCESS = "TG-EXPORTER-01021";

    /**
     * [INFO] Exportファイルを受信します。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_FILERECEIVE = "TG-EXPORTER-01022";

    /**
     * [INFO] Exportファイルを受信しました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_FILERECEIVE_SUCCESS = "TG-EXPORTER-01023";

    /**
     * [INFO] ExportテンポラリテーブルへExportファイルをロードします。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_FILELOAD = "TG-EXPORTER-01024";

    /**
     * [INFO] ExportテンポラリテーブルへExportファイルをロードしました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_FILELOAD_SUCCESS = "TG-EXPORTER-01025";

    /**
     * [INFO] ExportテンポラリテーブルからExport対象テーブルへデータをコピーします。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_DATA_COPY = "TG-EXPORTER-01026";

    /**
     * [INFO] ExportテンポラリテーブルからExport対象テーブルへデータをコピーしました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_DATA_COPY_SUCCESS = "TG-EXPORTER-01027";

    /**
     * [INFO] ロックの解除を行います。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_RELEASELOCK = "TG-EXPORTER-01028";

    /**
     * [INFO] ロックの解除を行いました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_RELEASELOCK_SUCCESS = "TG-EXPORTER-01029";

    /**
     * [INFO] 生成したExport対象ファイル（TSV中間ファイル）を削除します。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_TSV_FILE_DELETE = "TG-EXPORTER-01030";

    /**
     * [INFO] Exporterで実行する処理を判断しました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} テンポラリテーブル削除処理：{4} Exportファイル受信処理：{5} Exportファイルロード処理：{6} Exportデータコピー処理：{7} ロック解除処理：{8} 中間ファイル削除処理：{9} .
     */
    public static final String EXP_EXEC_PROCESS_JUDGE = "TG-EXPORTER-01031";

    /**
     * [INFO] エクスポート処理が正常終了した場合のTSVファイル削除有無に「0：削除しない」が設定されているため、エクスポート処理に成功した場合も生成したExport対象ファイル（TSV中間ファイル）を削除しません。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String EXP_TSV_FILE_NOT_DELETE = "TG-EXPORTER-01032";

    /**
     * [ERROR] Exportファイル受信処理でファイルを生成するディレクトリが存在しません。ディレクトリ名：{0} .
     */
    public static final String EXP_DIR_NOT_EXISTS_ERROR = "TG-EXPORTER-02001";

    /**
     * [ERROR] Exportファイル受信処理でエラーが発生しました。異常終了します。エラー原因：{0} .
     */
    public static final String EXP_FILERECEIV_EXCEPTION = "TG-EXPORTER-02002";

    /**
     * [ERROR] Exportファイル受信処理でZIPエントリに対応するテーブルの定義がDSL存在しません。ZIPエントリ名：{0} テーブル名：{1} .
     */
    public static final String EXP_DSL_NOTFOUND = "TG-EXPORTER-02003";

    /**
     * [ERROR] Exportファイル受信処理でExportファイルと同名のファイルが既に存在し、削除に失敗しました。ファイル名：{0} .
     */
    public static final String EXP_DELETEFILE_FAILED = "TG-EXPORTER-02004";

    /**
     * [ERROR] Exportファイル受信処理で起動したサブプロセスが異常終了しました。終了コード：{0} .
     */
    public static final String EXP_COLLECTOR_ERROR = "TG-EXPORTER-02005";

    /**
     * [ERROR] ジョブフロー実行IDからジョブフローSIDを取得する処理で例外が発生しました。ジョブフロー実行ID：{0} .
     */
    public static final String EXP_JOBFLOWSID_ERROR = "TG-EXPORTER-02006";

    /**
     * [INFO] Exportファイル受信の為のサブプロセスを起動します。SSHのパス：{0} マスターノードのホスト：{1} マスターノードのユーザー：{2} Collectorのシェル名：{3} ターゲット名：{4} バッチID：{5} ジョブフローID：{6} ジョブフロー実行ID：{7} .
     */
    public static final String EXP_START_SUB_PROCESS = "TG-EXPORTER-02007";

    /**
     * [INFO] 受信したファイルを生成します。テーブル名：{0} ローカルファイル名：{1} .
     */
    public static final String EXP_FILERECEIV = "TG-EXPORTER-02008";

    /**
     * [INFO] 受信したファイルを生成しました。テーブル名：{0} ローカルファイル名：{1} .
     */
    public static final String EXP_FILERECEIV_SUCCESS = "TG-EXPORTER-02009";

    /**
     * [ERROR] ExportファイルをLOADする処理でエラーが発生しました。異常終了します。エラー原因：{0} .
     */
    public static final String EXP_LOADFILE_EXCEPTION = "TG-EXPORTER-03001";

    /**
     * [INFO] エクスポートテンポラリ管理テーブルに作成予定のエクスポートテンポラリテーブルの情報を登録しました。ジョブフローSID：{0} .
     */
    public static final String EXP_INSERT_TEMP_INFO = "TG-EXPORTER-03002";

    /**
     * [INFO] エクスポートテンポラリテーブルを作成しました。ジョブフローSID：{0} エクスポート対象テーブル名：{1} エクスポートテンポラリテーブル名：{2} SQL：{3} .
     */
    public static final String EXP_CREATE_TEMP_TABLE = "TG-EXPORTER-03003";

    /**
     * [INFO] エクスポート中間TSVファイルをエクスポートテンポラリテーブルにロードしました。ジョブフローSID：{0} エクスポート対象テーブル名：{1} エクスポートテンポラリテーブル名：{2} エクスポート中間TSVファイル：{3} .
     */
    public static final String EXP_TSV_FILE_LOAD = "TG-EXPORTER-03004";

    /**
     * [INFO] エクスポートテンポラリ管理テーブルのステータスを「ロード完了」に更新しました。ジョブフローSID：{0} エクスポート対象テーブル名：{1} エクスポートテンポラリテーブル名：{2} .
     */
    public static final String EXP_LOAD_EXIT = "TG-EXPORTER-03005";

    /**
     * [INFO] 全てのエクスポート中間TSVファイルのロードを終了し、エクスポートテンポラリ管理テーブルのステータスを「コピー開始前」に更新しました。ジョブフローSID：{0}  .
     */
    public static final String EXP_BEFORE_COPY = "TG-EXPORTER-03006";

    /**
     * [ERROR] Export中間TSVファイルのカラムはExport対象テーブル/異常データテーブルの何れかテーブルに含まれるカラムである必要があります。カラム名：{0} .
     */
    public static final String EXP_TSV_COLUMN_NOT_FOUND = "TG-EXPORTER-03007";

    /**
     * [INFO] エクスポートテンポラリテーブルを作成しました。ジョブフローSID：{0} エクスポートテンポラリテーブル名：{1} 重複フラグテーブル名：{2} SQL：{3} .
     */
    public static final String EXP_CREATE_DUPLCATE_TABLE = "TG-EXPORTER-03008";

    /**
     * [ERROR] ロック解放処理でリトライ不可なエラーが発生しました。異常終了します。エラー原因：{0} .
     */
    public static final String EXP_RELEASE_LOCK_ERROR = "TG-EXPORTER-04001";

    /**
     * [ERROR] ロック解放処理がリトライオーバーしました。異常終了します。エラー原因：{0} .
     */
    public static final String EXP_RELEASE_LOCK_RETRY_ORVER = "TG-EXPORTER-04002";

    /**
     * [WARN] ロック解放処理でリトライ可能なエラーが発生しました。リトライします。エラー原因：{0} .
     */
    public static final String EXP_RELEASE_LOCK_RETRY = "TG-EXPORTER-04003";

    /**
     * [INFO] テーブルロックテーブルのImport/Export対象テーブル行のTXロックを取得し、ロック取得/解除操作の排他制御を行います。SQL：{0} .
     */
    public static final String EXP_LOCK_EXCLUSIVE = "TG-EXPORTER-04004";

    /**
     * [INFO] テーブルロックを解除します。SQL：{0} ジョブフローSID：{1} .
     */
    public static final String EXP_TABLE_LOCK_RELEASE = "TG-EXPORTER-04005";

    /**
     * [INFO] レコードロックを解除します。ロック済みレコードのレコード削除SQL：{0} レコードロックのレコード削除SQL：{1} ジョブフローSID：{2} Import/Export対象テーブル名：{3} .
     */
    public static final String EXP_RECORD_LOCK_RELEASE = "TG-EXPORTER-04006";

    /**
     * [INFO] ジョブフロー実行テーブルのレコードを削除します。SQL：{0} ジョブフローSID：{1} .
     */
    public static final String EXP_DELETE_RUNNING_JOBFLOW = "TG-EXPORTER-04007";

    /**
     * [WARN] Exportファイル削除処理でExportファイルの削除に失敗しました。ファイル名：{0} .
     */
    public static final String EXP_FILEDELETE_ERROR = "TG-EXPORTER-05001";

    /**
     * [WARN] Exportファイル削除処理でExportファイル格納ディレクトリの削除に失敗しました。ディレクトリ名：{0} .
     */
    public static final String EXP_DIRDELETE_ERROR = "TG-EXPORTER-05002";

    /**
     * [ERROR] ExporterでテンポラリテーブルからExport対象テーブルへのデータのコピー中に、Export対象テーブルに更新対象のレコードが見つかりませんでした。Export対象テーブル名：{0} Exportテンポラリテーブル名：{1} Export対象テーブルに更新対象が見つからなかったレコード：{2} .
     */
    public static final String EXP_DATA_COPY_UPDATE_ERROR = "TG-EXPORTER-06001";

    /**
     * [INFO] エクスポートテンポラリテーブルからエクスポート対象テーブルへデータをコピーします。ジョブフローSID：{0} Export対象テーブル：{1} Exportテンポラリテーブル：{2} .
     */
    public static final String EXP_COPY_START = "TG-EXPORTER-06002";

    /**
     * [INFO] エクスポートテンポラリテーブルからエクスポート対象テーブルへデータをコピーしました。ジョブフローSID：{0} Export対象テーブル名：{1} Exportテンポラリテーブル名：{2} 全てのデータをコピー完了：{3} .
     */
    public static final String EXP_COPY_END = "TG-EXPORTER-06003";

    /**
     * [INFO] 当該エクスポートテンポラリテーブルは既にコピーが終了しているため、コピーは行いません。ジョブフローSID：{0} Export対象テーブル：{1} Exportテンポラリテーブル：{2} .
     */
    public static final String EXP_COPY_ALREADY_ENDED = "TG-EXPORTER-06004";

    /**
     * [INFO] エクスポート対象テーブルに対応するエクスポートテンポラリテーブルが存在しないため、コピーは行いません。ジョブフローSID：{0} Export対象テーブル名：{1} Exportテンポラリテーブル名：{2} .
     */
    public static final String EXP_TEMP_TABLE_NOT_FOUND = "TG-EXPORTER-06005";

    /**
     * [INFO] 当該エクスポート対象テーブルに対するレコードロックテーブルのレコードをインサートしました。SQL：{0} ジョブフローSID：{1} エクスポート対象テーブル名：{2} .
     */
    public static final String EXP_RECORD_LOCK = "TG-EXPORTER-06006";

    /**
     * [INFO] エクスポートテンポラリテーブルからエクスポート対象テーブルに新規レコードをコピーしました。Export対象テーブル名：{0} Exportテンポラリテーブル名：{1} コピーSQL：{2} レコードロック取得SQL：{3} レコード削除SQL：{4} .
     */
    public static final String EXP_NEW_RECORD_COPY = "TG-EXPORTER-06007";

    /**
     * [INFO] エクスポートテンポラリテーブルから異常データテーブルに重複レコードをコピーしました。異常データテーブル名：{0} Exportテンポラリテーブル名：{1} コピーSQL：{2} レコード削除SQL：{3} .
     */
    public static final String EXP_DUPLICATE_RECORD_COPY = "TG-EXPORTER-06008";

    /**
     * [INFO] エクスポートテンポラリテーブルからエクスポート対象テーブルに更新レコードをコピーしました。Export対象テーブル名：{0} Exportテンポラリテーブル名：{1} コピーSQL：{2} レコード削除SQL：{3} 現在のテンポラリSIDの位置：{4} コピーするテンポラリSIDの最大値：{5} .
     */
    public static final String EXP_UPDATE_RECORD_COPY = "TG-EXPORTER-06009";

    /**
     * [INFO] 全てのエクスポートテンポラリテーブルのレコードをエクスポート対象テーブルにコピーしました。Export対象テーブル名：{0} Exportテンポラリテーブル名：{1}  .
     */
    public static final String EXP_ALL_DATA_COPY = "TG-EXPORTER-06010";

    /**
     * [INFO] エクスポートテンポラリ管理テーブルのステータスを「コピー完了」に更新しました。ジョブフローSID：{0} エクスポート対象テーブル名：{1} .
     */
    public static final String EXP_TABLE_COPY_EXIT = "TG-EXPORTER-06011";

    /**
     * [INFO] エクスポートテンポラリ管理テーブルのレコードを削除します。SQL：{0} ジョブフローSID：{1} テーブル名：{2} .
     */
    public static final String EXP_TEMP_INFO_RECORD_DELETE = "TG-EXPORTER-07001";

    /**
     * [INFO] エクスポートテンポラリテーブルを削除しました。SQL：{0} .
     */
    public static final String EXP_TEMP_TABLE_DROP = "TG-EXPORTER-07002";

    /**
     * [INFO] 当該エクスポートテンポラリテーブルのステータスが「'2'：Export対象テーブルにデータをコピー完了」以外のため、削除を行いません。エクスポートテンポラリテーブル名：{0} ステータス{1} .
     */
    public static final String EXP_TEMP_TABLE_NOT_DROP = "TG-EXPORTER-07003";

    /**
     * [INFO] 重複フラグテーブルを削除しました。SQL：{0} .
     */
    public static final String EXP_DUP_FLG_TABLE_DROP = "TG-EXPORTER-07004";

    /**
     * [INFO] Collectorの処理を開始します。開始時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String COL_START = "TG-COLLECTOR-01001";

    /**
     * [INFO] Collectorの処理を正常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String COL_EXIT = "TG-COLLECTOR-01002";

    /**
     * [ERROR] Collectorで初期化処理に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String COL_INIT_ERROR = "TG-COLLECTOR-01003";

    /**
     * [ERROR] Collectorで不明なエラーが発生しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String COL_EXCEPRION = "TG-COLLECTOR-01004";

    /**
     * [ERROR] Collectorに指定するパラメータが不正です。不正内容：{0} 値：{1} .
     */
    public static final String COL_PARAMCHECK_ERROR = "TG-COLLECTOR-01005";

    /**
     * [ERROR] Collectorでパラメータの入力チェックに失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String COL_PARAM_ERROR = "TG-COLLECTOR-01006";

    /**
     * [ERROR] CollectorでExportファイルの送信処理に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、バッチID：{2}、ジョブフローID：{3}、ジョブフロー実行ID：{4}、ユーザー名：{5} .
     */
    public static final String COL_FILESEND_ERROR = "TG-COLLECTOR-01007";

    /**
     * [INFO] HDFS上のExportファイルをDBサーバに送信します。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3}、ユーザー名：{4} .
     */
    public static final String COL_FILESEND = "TG-COLLECTOR-01008";

    /**
     * [INFO] HDFS上のExportファイルをDBサーバに送信しました。ターゲット名：{0}、バッチID：{1}、ジョブフローID：{2}、ジョブフロー実行ID：{3}、ユーザー名：{4} .
     */
    public static final String COL_FILESEND_SUCCESS = "TG-COLLECTOR-01009";

    /**
     * [ERROR] Exportファイルを送信する処理でエラーが発生しました。異常終了します。エラー原因：{0} .
     */
    public static final String COL_SENDFILE_EXCEPTION = "TG-COLLECTOR-02001";

    /**
     * [INFO] ジョブフロー設定に指定されたパスのExportファイルを送信します。Export対象テーブル名：{0} HDFSのパス：{1} ZIP圧縮有無：{2} Modelクラス：{3} .
     */
    public static final String COL_SEND_HDFSFILE = "TG-COLLECTOR-02002";

    /**
     * [INFO] ジョブフロー設定に指定されたパスのExportファイルを送信しました。Export対象テーブル名：{0} HDFSのパス：{1} ZIP圧縮有無：{2} Modelクラス：{3} .
     */
    public static final String COL_SEND_HDFSFILE_SUCCESS = "TG-COLLECTOR-02003";

    /**
     * [INFO] Exportファイルを送信します。Export対象テーブル名：{0} HDFSのファイルパス：{1} ExportファイルのZipEntry名：{2} .
     */
    public static final String COL_SENDFILE = "TG-COLLECTOR-02004";

    /**
     * [INFO] Exportファイルを送信しました。Export対象テーブル名：{0} HDFSのファイルパス：{1} ExportファイルのZipEntry名：{2} .
     */
    public static final String COL_SENDFILE_SUCCESS = "TG-COLLECTOR-02005";

    /**
     * [INFO] ジョブフロー設定に指定されたパスに該当するExportファイルが存在しませんでした。Export対象テーブル名：{0} HDFSのパス：{1} .
     */
    public static final String COL_EXPORT_FILE_NOT_FOUND = "TG-COLLECTOR-02006";

    /**
     * [INFO] ジョブフロー設定に指定されたパスに{0}件のExportファイルが存在しました。送信を行います。Export対象テーブル名：{1} HDFSのパス：{2} .
     */
    public static final String COL_EXPORT_FILE_FOUND = "TG-COLLECTOR-02007";

    /**
     * [INFO] Recovererの処理を開始します。開始時刻：{0}、ターゲット名：{1}、ジョブフロー実行ID：{2} .
     */
    public static final String RCV_START = "TG-RECOVERER-01001";

    /**
     * [INFO] Recovererの処理を終了します。処理結果：{0}、終了時刻：{1}、ターゲット名：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String RCV_EXIT = "TG-RECOVERER-01002";

    /**
     * [ERROR] Recovererで初期化処理に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、ジョブフロー実行ID：{2} .
     */
    public static final String RCV_INIT_ERROR = "TG-RECOVERER-01003";

    /**
     * [ERROR] Recovererで不明なエラーが発生しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、ジョブフロー実行ID：{2} .
     */
    public static final String RCV_EXCEPRION = "TG-RECOVERER-01004";

    /**
     * [ERROR] Recovererに指定する引数が不正です。異常終了します。引数の数：{0}、終了時刻：{1}、ターゲット名：{2}、ジョブフロー実行ID：{3} .
     */
    public static final String RCV_ARGSCHECK_ERROR = "TG-RECOVERER-01005";

    /**
     * [ERROR] Recovererでジョブフロー実行テーブルの取得に失敗しました。異常終了します。終了時刻：{0}、ターゲット名：{1}、ジョブフロー実行ID：{2} .
     */
    public static final String RCV_GETRUNNUNG_JOBFLOW_ERROR = "TG-RECOVERER-01006";

    /**
     * [ERROR] Recovererに指定するパラメータが不正です。不正内容：{0} 値：{1} ジョブフロー実行ID：{2} .
     */
    public static final String RCV_PARAMCHECK_ERROR = "TG-RECOVERER-01007";

    /**
     * [INFO] 当該ジョブフロー実行IDは他プロセスにより処理中のため、リカバリ対象外とします。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_INSTANCE_ID_LOCK_ERROR = "TG-RECOVERER-01008";

    /**
     * [ERROR] Recovererの処理が開始された後に、当該ジョブフロー実行IDが他プロセスにより処理されました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{3} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_INSTANCE_ID_NOT_FOUND = "TG-RECOVERER-01009";

    /**
     * [INFO] 当該ジョブフロー実行IDは実行中のため、リカバリ対象外とします。ターゲット名：{0}、バッチID：{1} ジョブフローID：{3} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_MM_EXEC_INCTANCE = "TG-RECOVERER-01010";

    /**
     * [ERROR] 当該ジョブフローSIDに対応するテンポラリテーブルの情報取得に失敗しました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{3} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_TEMP_INFO_ERROR = "TG-RECOVERER-01011";

    /**
     * [ERROR] ジョブフローインスタンスに対するロールフォワードでExportテンポラリテーブルからExport対象テーブルへのデータのコピーに失敗しました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_DATA_COPY_ERROR = "TG-RECOVERER-01012";

    /**
     * [ERROR] ジョブフローインスタンスに対するロックの解除に失敗しました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_RELEASELOCK_ERROR = "TG-RECOVERER-01013";

    /**
     * [INFO] ジョブフローインスタンスに対するリカバリ処理を実行しました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}、処理内容：{5} .
     */
    public static final String RCV_JOBFLOW_RECOVERY_EXIT = "TG-RECOVERER-01014";

    /**
     * [ERROR] Export対象テーブルに更新対象のレコードが存在しないため、ジョブフローインスタンスに対するリカバリ処理が不完全に終了しました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}、処理内容：{5} .
     */
    public static final String RCV_COPY_NOT_EXIT = "TG-RECOVERER-01015";

    /**
     * [INFO] ジョブフローインスタンスに対するリカバリ処理を開始します。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4} .
     */
    public static final String RCV_INSTANCE_START = "TG-RECOVERER-01016";

    /**
     * [INFO] ジョブフロー実行IDによる排他制御を行います。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_INSTANCE_ID_LOCK = "TG-RECOVERER-01017";

    /**
     * [INFO] ジョブフロー実行IDによる排他制御に成功しました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_INSTANCE_ID_LOCK_SUCCESS = "TG-RECOVERER-01018";

    /**
     * [INFO] ジョブフローインスタンスに対するロールフォワードでExportテンポラリテーブルからExport対象テーブルへデータをコピーします。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_DATA_COPY = "TG-RECOVERER-01019";

    /**
     * [INFO] ジョブフローインスタンスに対するロールフォワードでExportテンポラリテーブルからExport対象テーブルへデータをコピーしました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_DATA_COPY_SUCCESS = "TG-RECOVERER-01020";

    /**
     * [INFO] ジョブフローインスタンスに対するロックを解除します。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_RELEASELOCK = "TG-RECOVERER-01021";

    /**
     * [INFO] ジョブフローインスタンスに対するロックの解除を行いました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}  .
     */
    public static final String RCV_RELEASELOCK_SUCCESS = "TG-RECOVERER-01022";

    /**
     * [INFO] ジョブフローインスタンスに対するリカバリ処理内容を決定しました。ターゲット名：{0}、バッチID：{1} ジョブフローID：{2} ジョブフローSID：{3} ジョブフロー実行ID：{4}、処理内容：{5} .
     */
    public static final String RCV_JUDGE_PROCESS = "TG-RECOVERER-01023";

    /**
     * [INFO] Import内容を送信しました。ターゲット名：{0} バッチID：{1} ジョブフローID：{2} ジョブフロー実行ID：{3} テーブル：{4} 件数：{5} .
     */
    public static final String PRF_IMPORT_COUNT = "TG-PROFILE-01001";

    /**
     * [INFO] Import内容を受信しました。ターゲット名：{0} バッチID：{1} ジョブフローID：{2} ジョブフロー実行ID：{3} テーブル：{4} 件数：{5} .
     */
    public static final String PRF_EXTRACT_COUNT = "TG-PROFILE-01002";

    /**
     * [INFO] Export内容を受信しました。ターゲット名：{0} バッチID：{1} ジョブフローID：{2} ジョブフロー実行ID：{3} テーブル：{4} 件数：{5} .
     */
    public static final String PRF_EXPORT_COUNT = "TG-PROFILE-01003";

    /**
     * [INFO] Export内容を送信しました。ターゲット名：{0} バッチID：{1} ジョブフローID：{2} ジョブフロー実行ID：{3} テーブル：{4} 件数：{5} .
     */
    public static final String PRF_COLLECT_COUNT = "TG-PROFILE-01004";

    /**
     * インスタンス生成の禁止。
     */
    private MessageIdConst() {
        return;
    }
}
