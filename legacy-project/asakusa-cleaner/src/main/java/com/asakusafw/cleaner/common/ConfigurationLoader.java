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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.asakusafw.cleaner.exception.CleanerSystemException;


/**
 * 設定を読み込んで保持するクラス。
 * 以下の設定を保持する。
 * ・プロパティファイル
 * ・環境変数
 * ・システムプロパティ
 *
 * @author yuta.shirai
 *
 */
public final class ConfigurationLoader {
    /** プロパティファイル。 */
    private static Properties prop = new Properties();
    /** 環境変数。 */
    private static Map<String, String> env = null;
    /** システムプロパティ。 */
    private static Properties sysProp = null;

    private ConfigurationLoader() {
    	return;
    }

    /**
     * 読み込んでいるプロパティをクリアする。
     */
    public static void cleanProp() {
        prop = new Properties();
    }
    /**
     * 設定の読み込みを行う。
     * @param propertys 読み込むプロパティファイルの一覧(絶対パスか「$CLEAN_HOME/conf/」のファイル名を指定する)
     * @param doLocalCleanPropCheck LocalFileCleaner用プロパティのチェックを行うか
     * @param doHDFSCleanPropCheck HDFSCleaner用プロパティのチェックを行うか
     * @throws CleanerSystemException プロパティの中身が不正
     * @throws Exception 環境変数未設定又はファイル読み込み失敗
     */
    public static void init(
            String[] propertys,
            boolean doLocalCleanPropCheck,
            boolean doHDFSCleanPropCheck) throws CleanerSystemException, Exception {

        // 環境変数を取得してチェック
        env = System.getenv();

        // システムプロパティを取得
        sysProp = System.getProperties();

        checkEnv();

        // プロパティを読み込んでチェック・デフォルト値を設定
        loadPropertyes(propertys);
        if (doLocalCleanPropCheck) {
            checkAndSetParamLocalFileClean();
        }
        if (doHDFSCleanPropCheck) {
            checkAndSetParamHDFSClean();
        }
    }
    /**
     * 環境変数をチェックする。
     * @throws Exception 環境変数が不正な場合
     */
    protected static void checkEnv() throws Exception {
        String cleanHome = getEnvProperty(Constants.CLEAN_HOME);
        if (isEmpty(cleanHome)) {
            System.err.println(MessageFormat.format("環境変数「{0}」が設定されていません。", Constants.CLEAN_HOME));
            throw new Exception(MessageFormat.format("環境変数「{0}」が設定されていません。", Constants.CLEAN_HOME));
        }
        File cleanHomeDir = new File(cleanHome);
        if (!cleanHomeDir.exists()) {
            System.err.println(
                    MessageFormat.format("環境変数「{0}」に設定されたディレクトリが存在しません。ディレクトリ：{1}", Constants.CLEAN_HOME, cleanHome));
            throw new Exception(MessageFormat.format(
                    "環境変数「{0}」に設定されたディレクトリが存在しません。ディレクトリ：", Constants.CLEAN_HOME, cleanHome));
        }
    }
    /**
     * LocalFileCleanerのプロパティの必須チェックとデフォルト値を設定する。
     * @throws CleanerSystemException Cleanerのシステム例外
     */
    protected static void checkAndSetParamLocalFileClean() throws CleanerSystemException {
        // デフォルト値の設定
        // クリーニング対象を保持する期間
        String keepDate = prop.getProperty(Constants.PROP_KEY_LOCAL_FILE_KEEP_DATE);
        if (isEmpty(keepDate)) {
            prop.setProperty(Constants.PROP_KEY_LOCAL_FILE_KEEP_DATE, Constants.PROP_DEFAULT_LOCAL_FILE_KEEP_DATE);
        } else {
            if (!isNumber(keepDate, 0)) {
                throw new CleanerSystemException(
                        ConfigurationLoader.class,
                        MessageIdConst.CMN_PROP_CHECK_ERROR,
                        "LocalFileCleanerでクリーニング対象を保持する期間の設定が不正。設定値：" + keepDate);
            }
        }

        // 必須チェック
        // クリーニング対象ディレクトリ
        List<String> cleanDirList = getPropStartWithString(Constants.PROP_KEY_LOCAL_FILE_CLEAN_DIR + ".");
        List<String> noEmptyList = getNoEmptyList(cleanDirList);
        if (noEmptyList.size() == 0) {
            throw new CleanerSystemException(
                    ConfigurationLoader.class,
                    MessageIdConst.CMN_PROP_CHECK_ERROR,
                    "LocalFileCleanerでクリーニング対象ディレクトリが設定されていない。");
        }
    }
    /**
     * HDFSCleanerのプロパティの必須チェックとデフォルト値を設定する。
     * @throws CleanerSystemException Cleanerのシステム例外
     */
    protected static void checkAndSetParamHDFSClean() throws CleanerSystemException {
        // デフォルト値の設定
        // クリーニング対象を保持する期間
        String keepDate = prop.getProperty(Constants.PROP_KEY_HDFS_FILE_KEEP_DATE);
        if (isEmpty(keepDate)) {
            prop.setProperty(Constants.PROP_KEY_HDFS_FILE_KEEP_DATE, Constants.PROP_DEFAULT_HDFS_FILE_KEEP_DATE);
        } else {
            if (!isNumber(keepDate, 0)) {
                throw new CleanerSystemException(
                        ConfigurationLoader.class,
                        MessageIdConst.CMN_PROP_CHECK_ERROR,
                        "HDFSCleanerでクリーニング対象を保持する期間の設定が不正。設定値：" + keepDate);
            }
        }

        // 必須チェック
        // HDFSのプロトコルとホスト名
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST))) {
            throw new CleanerSystemException(
                    ConfigurationLoader.class,
                    MessageIdConst.CMN_PROP_CHECK_ERROR,
                    "HDFSのプロトコルとホスト名が設定されていない。");
        }
        // クリーニング対象ディレクトリ
        List<String> cleanDirList = getPropStartWithString(Constants.PROP_KEY_HDFS_FILE_CLEAN_DIR + ".");
        List<String> noEmptyList = getNoEmptyList(cleanDirList);
        if (noEmptyList.size() == 0) {
            throw new CleanerSystemException(
                    ConfigurationLoader.class,
                    MessageIdConst.CMN_PROP_CHECK_ERROR,
                    "HDFSCleanerでクリーニング対象ディレクトリが設定されていない。");
        }
    }
    /**
     * 共通のプロパティの必須チェックとデフォルト値を設定する。
     */
    protected static void checkAndSetParam() {
        // デフォルト値の設定
        // log4j.xmlのパス
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_LOG_CONF_PATH))) {
            prop.setProperty(Constants.PROP_KEY_LOG_CONF_PATH, Constants.PROP_DEFAULT_LOG_CONF_PATH);
        }
    }
    /**
     * ファイルからプロパティを読み込みます。
     *
     * @param propertys 読み込むプロパティファイル
     * @throws FileNotFoundException ファイルが存在しない
     * @throws IOException ファイル読み込み失敗
     */
    private static void loadPropertyes(String[] propertys) throws IOException {
        FileInputStream fis = null;
        for (String strProp : propertys) {
            File propFile = createPropFileName(strProp);
            try {
                fis = new FileInputStream(propFile);
                prop.load(fis);
            } catch (IOException e) {
                System.err.println("プロパティファイルの読み込みに失敗しました。ファイル名：" + propFile.getAbsolutePath());
                e.printStackTrace();
                throw e;
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
    }
    /**
     * プロパティファイル名を作成する。
     * 「$CLEANER_HOME/conf」以下のファイルを使用する。
     * @param propFileName ファイル名
     * @return ファイル名のフルパス
     */
    private static File createPropFileName(String propFileName) {
        String cleanHome = ConfigurationLoader.getEnvProperty(Constants.CLEAN_HOME);
        File file1 = new File(cleanHome, Constants.PROP_FILE_PATH);
        File file2 = new File(file1, propFileName);
        return file2;
    }
    /**
     * プロパティから特定の文字列から開始するKeyのリストを検索します。
     * @param startString 開始文字列
     * @return keyのリスト
     */
    public static List<String> getPropStartWithString(String startString) {
        Set<Object> propSet = prop.keySet();
        List<String> list = new ArrayList<String>();
        for (Object strKey : propSet) {
            String key = (String) strKey;
            if (key.startsWith(startString)) {
                list.add(key);
            }
        }

        return list;
    }
    /**
     * 引数のkeyリストのうち、プロパティのvalueが空でないkeyを返す。
     * @param list keyのリスト
     * @return プロパティのvalueが空でないkeyのリスト
     */
    public static List<String> getNoEmptyList(List<String> list) {
        List<String> resultList = new ArrayList<String>();
        if (list == null || list.size() == 0) {
            return resultList;
        }
        for (String key : list) {
            String value = prop.getProperty(key);
            if (!isEmpty(value)) {
                resultList.add(key);
            }
        }
        return resultList;
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
        if ("".equals(str)) {
            return true;
        }
        return false;
    }
    /**
     * 引数が数値である場合trueを返す。
     * @param str 文字列
     * @param min 数値の最小値(最小値より小さい場合NGとする)
     * @return 結果
     */
    private static boolean isNumber(String str, int min) {
        try {
            long l = Long.parseLong(str);
            return l >= min;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    /**
     * プロパティを返します。
     *
     * @param key キー
     * @return String 値
     */
    public static String getProperty(String key) {
        return prop.getProperty(key);
    }
    /**
     * 指定のキーに対応するシステムプロパティ又は環境変数を返す。
     * システムプロパティに存在する場合はシステムプロパティの値を返し、
     * 存在しない場合は環境変数を検索する。
     * 何れにも存在しない場合はnullを返す。
     *
     * @param key キー
     * @return String 対応するシステムプロパティ又は環境変数、存在しない場合は{@code null}
     */
    public static String getEnvProperty(String key) {
        String strSysProp = sysProp.getProperty(key);
        String strEnv = env.get(key);
        if (strSysProp != null) {
            return strSysProp;
        } else if (strEnv != null) {
            return strEnv;
        } else {
            return null;
        }
    }
    /**
     * プロパティを設定します。
     * @param p Properties
     * @deprecated UT用
     */
    @Deprecated
    public static void setProperty(Properties p) {
        prop = p;
    }
    /**
     * プロパティを返します。
     * @return Properties
     * @deprecated UT用
     */
    @Deprecated
    public static Properties getProperty() {
        return prop;
    }
    /**
     * システムプロパティを設定します。
     * @param p Properties
     * @deprecated UT用
     */
    @Deprecated
    public static void setSysProp(Properties p) {
        sysProp = p;
    }
    /**
     * 環境変数を設定します。
     * @param m Map&lt;String, String&gt;
     * @deprecated UT用
     */
    @Deprecated
    public static void setEnv(Map<String, String> m) {
        env = m;
    }
}
