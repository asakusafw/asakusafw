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
 * ログ出力で使用されるメッセージIDを一元管理するためのクラスです。
 * このクラスはメッセージファイルにより自動生成されたものなので、直接編集しないでください。
 *
 * @author logmessage.xls
 * @version $Id$
 */
public final class MessageIdConst {

    /** [ERROR] プロパティファイルの設定値が不正です。不正内容：{0}。 */
    public static final String CMN_PROP_CHECK_ERROR = "CL-COMMON-00001";

    /** [INFO] LocalFileCleanerの処理を開始します。開始時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String LCLN_START = "CL-LOCALCLEAN-01001";

    /** [INFO] LocalFileCleanerの処理を正常終了します。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String LCLN_EXIT_SUCCESS = "CL-LOCALCLEAN-01002";

    /** [INFO] LocalFileCleanerの処理を警告終了します。一部ディレクトリ又はファイルの削除に失敗しました。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String LCLN_EXIT_WARNING = "CL-LOCALCLEAN-01003";

    /** [ERROR] LocalFileCleanerで初期化処理に失敗しました。異常終了します。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String LCLN_INIT_ERROR = "CL-LOCALCLEAN-01004";

    /** [ERROR] LocalFileCleanerで不明なエラーが発生しました。異常終了します。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String LCLN_EXCEPRION = "CL-LOCALCLEAN-01005";

    /** [ERROR] LocalFileCleanerに指定するパラメータが不正です。異常終了します。不正内容：{0} 値：{1} 終了時刻：{2} 動作モード：{3} プロパティ：{4}。 */
    public static final String LCLN_PARAMCHECK_ERROR = "CL-LOCALCLEAN-01006";

    /** [INFO] クリーニング対象ディレクトリに対するクリーニング処理に成功しました。クリーニング対象ディレクトリ：{0} 保持期間：{1} 動作モード：{2}。 */
    public static final String LCLN_CLEN_DIR_SUCCESS = "CL-LOCALCLEAN-01007";

    /** [WARN] クリーニング対象ディレクトリに対するクリーニング処理に失敗しました。クリーニング対象ディレクトリ：{0} 保持期間：{1} 動作モード：{2}。 */
    public static final String LCLN_CLEN_DIR_FAIL = "CL-LOCALCLEAN-01008";

    /** [ERROR] LocalFileCleanerに指定するクリーニング対象ディレクトリが不正です。不正内容：{0} 値：{1}。 */
    public static final String LCLN_CLEN_DIR_ERROR = "CL-LOCALCLEAN-01009";

    /** [WARN] LocalFileCleanerで削除処理に失敗しました。削除対象：{0} パス：{1}。 */
    public static final String LCLN_CLEN_FAIL = "CL-LOCALCLEAN-01010";

    /** [INFO] クリーニング対象ディレクトリに対するクリーニングを実施します。クリーニング対象ディレクトリ：{0} 削除パターン：{1} 保持期間：{2} 動作モード：{3} クリーニング開始時刻：{4}。 */
    public static final String LCLN_CLEN_FILE = "CL-LOCALCLEAN-01011";

    /** [DEBUG] ファイルをクリーニングします。ディレクトリ：{0}。 */
    public static final String LCLN_FILE_DELETE = "CL-LOCALCLEAN-01012";

    /** [DEBUG] ディレクトリ内をクリーニングしました。ディレクトリ：{0} ディレクトリ削除件数：{1} ファイル削除件数：{2}。 */
    public static final String LCLN_FILE_DELETE_SUCCESS = "CL-LOCALCLEAN-01013";

    /** [DEBUG] ディレクトリを削除しました。ディレクトリ：{0}。 */
    public static final String LCLN_DIR_DELETE = "CL-LOCALCLEAN-01014";

    /** [ERROR] ファイルの削除指定パターンの正規表現が不正です。パターン：{0}。 */
    public static final String LCLN_PATTERN_FAIL = "CL-LOCALCLEAN-01015";

    /** [DEBUG] ファイルを削除しました。ファイル：{0}。 */
    public static final String LCLN_DELETE_FILE = "CL-LOCALCLEAN-01016";

    /** [ERROR] クリーニング対象ディレクトリに対する削除パターンが指定されていません。クリーニング対象ディレクトリのkey：{0} クリーニング対象ディレクトリ：{1} 削除パターンのkey：{2}。 */
    public static final String LCLN_PATTERN_NOT_FOUND = "CL-LOCALCLEAN-01017";

    /** [INFO] HDFSCleanerの処理を開始します。開始時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String HCLN_START = "CL-HDFSCLEAN-01001";

    /** [INFO] HDFSCleanerの処理を正常終了します。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String HCLN_EXIT_SUCCESS = "CL-HDFSCLEAN-01002";

    /** [INFO] HDFSCleanerの処理を警告終了します。一部ディレクトリ又はファイルの削除に失敗しました。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String HCLN_EXIT_WARNING = "CL-HDFSCLEAN-01003";

    /** [ERROR] HDFSCleanerで初期化処理に失敗しました。異常終了します。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String HCLN_INIT_ERROR = "CL-HDFSCLEAN-01004";

    /** [ERROR] HDFSCleanerで不明なエラーが発生しました。異常終了します。終了時刻：{0} 動作モード：{1} プロパティ：{2}。 */
    public static final String HCLN_EXCEPRION = "CL-HDFSCLEAN-01005";

    /** [ERROR] HDFSCleanerに指定するパラメータが不正です。異常終了します。不正内容：{0} 値：{1} 終了時刻：{2} 動作モード：{3} プロパティ：{4}。 */
    public static final String HCLN_PARAMCHECK_ERROR = "CL-HDFSCLEAN-01006";

    /** [INFO] クリーニング対象ディレクトリに対するクリーニング処理に成功しました。クリーニング対象ディレクトリ：{0} 保持期間：{1} 動作モード：{2}。 */
    public static final String HCLN_CLEN_DIR_SUCCESS = "CL-HDFSCLEAN-01007";

    /** [WARN] クリーニング対象ディレクトリに対するクリーニング処理に失敗しました。クリーニング対象ディレクトリ：{0} 保持期間：{1} 動作モード：{2}。 */
    public static final String HCLN_CLEN_DIR_FAIL = "CL-HDFSCLEAN-01008";

    /** [ERROR] HDFSCleanerに指定するクリーニング対象ディレクトリが不正です。不正内容：{0} 値：{1}。 */
    public static final String HCLN_CLEN_DIR_ERROR = "CL-HDFSCLEAN-01009";

    /** [WARN] HDFSCleanerで削除処理に失敗しました。削除対象：{0} パス：{1}。 */
    public static final String HCLN_CLEN_FAIL = "CL-HDFSCLEAN-01010";

    /** [ERROR] HDFSCleanerでクリーニング処理中にIO例外が発生しました。削除対象ディレクトリ：{0}。 */
    public static final String HCLN_CLEN_DIR_EXCEPTION = "CL-HDFSCLEAN-01011";

    /** [INFO] クリーニング対象ディレクトリが処理中のため、クリーニング対象外とします。クリーニング対象ディレクトリ：{0}。 */
    public static final String HCLN_CLEN_DIR_EXEC = "CL-HDFSCLEAN-01012";

    /**
     * [INFO] クリーニング対象ディレクトリに対するクリーニングを実施します。
     * クリーニング対象ディレクトリ：{0} 削除パターン：{1} 保持期間：{2} 動作モード：{3}
     * ジョブフローインスタンス実行中の問い合わせを行うか：{4} クリーニング開始時刻：{5}。
     */
    public static final String HCLN_CLEN_FILE = "CL-HDFSCLEAN-01013";

    /** [DEBUG] ファイルをクリーニングします。ディレクトリ：{0}。 */
    public static final String HCLN_FILE_DELETE = "CL-HDFSCLEAN-01014";

    /** [DEBUG] ディレクトリ内をクリーニングしました。ディレクトリ：{0} ディレクトリ削除件数：{1} ファイル削除件数：{2}。 */
    public static final String HCLN_FILE_DELETE_SUCCESS = "CL-HDFSCLEAN-01015";

    /** [DEBUG] ディレクトリを削除しました。ディレクトリ：{0}。 */
    public static final String HCLN_DIR_DELETE = "CL-HDFSCLEAN-01016";

    /** [ERROR] ファイルの削除指定パターンの正規表現が不正です。パターン：{0}。 */
    public static final String HCLN_PATTERN_FAIL = "CL-HDFSCLEAN-01017";

    /** [DEBUG] ファイルを削除しました。ファイル：{0}。 */
    public static final String HCLN_DELETE_FILE = "CL-HDFSCLEAN-01018";

    /** [ERROR] クリーニング対象ディレクトリに対する削除パターンが指定されていません。クリーニング対象ディレクトリのkey：{0} クリーニング対象ディレクトリ：{1} 削除パターンのkey：{2}。 */
    public static final String HCLN_PATTERN_NOT_FOUND = "CL-HDFSCLEAN-01019";

    /**
     * コンストラクタ。
     */
    private MessageIdConst() {
        return;
    }
}
