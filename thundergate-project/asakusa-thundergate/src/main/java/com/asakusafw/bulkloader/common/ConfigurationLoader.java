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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;

/**
 * 設定を読み込んで保持するクラス。
 * <p>
 * 以下の設定を保持する。
 * </p>
<pre>
・プロパティファイル
・環境変数
・システムプロパティ
</pre>
 * @author yuta.shirai
 * @since 0.1.0
 * @version 0.4.0
 */
public final class ConfigurationLoader {

    private static final Set<String> KEY_PATHS;
    static {
        Set<String> keys = new HashSet<String>();
        keys.add(Constants.PROP_KEY_LOG_CONF_PATH);
        keys.add(Constants.PROP_KEY_SSH_PATH);
        keys.add(Constants.PROP_KEY_IMP_FILE_DIR);
        keys.add(Constants.PROP_KEY_EXP_FILE_DIR);
        KEY_PATHS = Collections.unmodifiableSet(keys);
    }

    /**
     * このクラス。
     */
    private static final Class<ConfigurationLoader> CLASS = ConfigurationLoader.class;

    /**
     * プロパティファイル。
     */
    private static volatile Properties prop = new Properties();
    /**
     * 環境変数。
     */
    private static volatile Map<String, String> env = null;
    /**
     * システムプロパティ。
     */
    private static volatile Properties sysProp = null;

    private ConfigurationLoader() {
        return;
    }
    static {
        // 環境変数を取得
        env = System.getenv();
        // システムプロパティを取得
        sysProp = System.getProperties();
    }
    /**
     * 読み込んでいるプロパティをクリアする。
     */
    public static void cleanProp() {
        prop = new Properties();
    }
    /**
     * 設定の読み込みを行う。
     * @param properties 読み込むプロパティファイルの一覧(絶対パスか「$ASAKUSA_HOME/bulkloader/conf/」のファイル名を指定する)
     * @param doDBPropCheck DBサーバ用プロパティのチェックを行うか
     * @param doHCPropCheck クライアントノード用プロパティのチェックを行うか
     * @throws BulkLoaderSystemException プロパティの中身が不正であった場合
     * @throws IOException ファイル読み込みに失敗した場合
     * @throws IllegalStateException 環境変数が未設定の場合
     */
    public static void init(
            List<String> properties,
            boolean doDBPropCheck,
            boolean doHCPropCheck) throws BulkLoaderSystemException, IOException {

        // 環境変数をチェック
        checkEnv();

        // プロパティを読み込んでチェック・デフォルト値を設定
        loadProperties(properties);
        checkAndSetParam();
        if (doDBPropCheck) {
            checkAndSetParamDB();
        }
        if (doHCPropCheck) {
            checkAndSetParamHC();
        }
    }
    /**
     * 環境変数をチェックする。
     * @throws IllegalStateException 環境変数が不正な場合
     */
    public static void checkEnv() {
        checkDirectory(Constants.ASAKUSA_HOME);
        checkDirectory(Constants.THUNDER_GATE_HOME);
    }

    private static void checkDirectory(String variableName) {
        assert variableName != null;
        String variable = ConfigurationLoader.getEnvProperty(variableName);
        if (isEmpty(variable)) {
            System.err.println(MessageFormat.format("環境変数「{0}」が設定されていません。", variableName));
            throw new IllegalStateException(MessageFormat.format("環境変数「{0}」が設定されていません。", variableName));
        }
        File path = new File(variable);
        if (!path.exists()) {
            System.err.println(MessageFormat.format("環境変数「{0}」に設定されたディレクトリが存在しません。ディレクトリ：{1}", variableName, variable));
            throw new IllegalStateException(
                    MessageFormat.format("環境変数「{0}」に設定されたディレクトリが存在しません。ディレクトリ：{1}", variableName, variable));
        }
    }
    /**
     * HadoopClusterのプロパティの必須チェックとデフォルト値を設定する。
     * @throws BulkLoaderSystemException プロパティの中身が不正であった場合
     */
    protected static void checkAndSetParamHC() throws BulkLoaderSystemException {
        // デフォルト値の設定
        // Exportファイルの圧縮有無
        String strCompType = prop.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_TYPE);
        FileCompType compType = FileCompType.find(strCompType);
        if (isEmpty(strCompType)) {
            prop.setProperty(Constants.PROP_KEY_EXP_FILE_COMP_TYPE, Constants.PROP_DEFAULT_EXP_FILE_COMP_TYPE);
        } else if (compType == null) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "Exportファイルの圧縮有無が不正。値：" + null);
        }
        // エクスポート処理で中間TSVファイルを生成する際にTSVファイルの分割サイズ
        String loadMaxSize = prop.getProperty(Constants.PROP_KEY_EXP_LOAD_MAX_SIZE);
        if (isEmpty(loadMaxSize)) {
            prop.setProperty(
                    Constants.PROP_KEY_EXP_LOAD_MAX_SIZE,
                    Constants.PROP_DEFAULT_EXP_LOAD_MAX_SIZE);
        } else {
            if (!isNumber(loadMaxSize, 1)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "エクスポート処理中間TSVファイルを生成する際にTSVファイルを分割するサイズの設定が不正。設定値：" + loadMaxSize);
            }
        }

        // 出力ファイルの圧縮有無
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_IMP_SEQ_FILE_COMP_TYPE))) {
            prop.setProperty(
                    Constants.PROP_KEY_IMP_SEQ_FILE_COMP_TYPE,
                    Constants.PROP_DEFAULT_IMP_SEQ_FILE_COMP_TYPE);
        }

        // 必須チェック

        // configuration for cache
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_CACHE_BUILDER_PARALLEL))) {
            prop.setProperty(
                    Constants.PROP_KEY_CACHE_BUILDER_PARALLEL,
                    Constants.PROP_DEFAULT_CACHE_BUILDER_PARALLEL);
        }
    }

    // CHECKSTYLE:OFF MethodLengthCheck - FIXME refactoring
    /**
     * DBサーバのプロパティの必須チェックとデフォルト値を設定する。
     * @throws BulkLoaderSystemException プロパティの中身が不正であった場合
     */
    protected static void checkAndSetParamDB() throws BulkLoaderSystemException {
        // デフォルト値の設定
        // Importファイルの圧縮有無
        String strCompType = prop.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_TYPE);
        FileCompType compType = FileCompType.find(strCompType);
        if (isEmpty(strCompType)) {
            prop.setProperty(
                    Constants.PROP_KEY_IMP_FILE_COMP_TYPE,
                    Constants.PROP_DEFAULT_IMP_FILE_COMP_TYPE);
        } else if (compType == null) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "Importファイルの圧縮有無が不正。値：" + null);
        }
        // Importファイルの圧縮時のバッファサイズ
        String impBufSize = prop.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_BUFSIZE);
        if (isEmpty(impBufSize)) {
            prop.setProperty(
                    Constants.PROP_KEY_IMP_FILE_COMP_BUFSIZE,
                    Constants.PROP_DEFAULT_IMP_FILE_COMP_BUFSIZE);
        } else {
            if (!isNumber(impBufSize, 1)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "Importファイルの圧縮時のバッファサイズの設定が不正。設定値：" + impBufSize);
            }
        }
        // Importerのリトライ回数
        String impRetryCount = prop.getProperty(Constants.PROP_KEY_IMP_RETRY_COUNT);
        if (isEmpty(impRetryCount)) {
            prop.setProperty(
                    Constants.PROP_KEY_IMP_RETRY_COUNT,
                    Constants.PROP_DEFAULT_IMP_RETRY_COUNT);
        } else {
            if (!isNumber(impRetryCount, 0)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "Importerのリトライ回数の設定が不正。設定値：" + impRetryCount);
            }
        }
        // Importerのリトライインターバル
        String impRetryInterval = prop.getProperty(Constants.PROP_KEY_IMP_RETRY_INTERVAL);
        if (isEmpty(impRetryInterval)) {
            prop.setProperty(
                    Constants.PROP_KEY_IMP_RETRY_INTERVAL,
                    Constants.PROP_DEFAULT_IMP_RETRY_INTERVAL);
        } else {
            if (!isNumber(impRetryInterval, 0)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "Importerのリトライインターバルの設定が不正。設定値：" + impRetryInterval);
            }
        }
        // Exportファイルの圧縮時のバッファサイズ
        String expBufSize = prop.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_BUFSIZE);
        if (isEmpty(expBufSize)) {
            prop.setProperty(
                    Constants.PROP_KEY_EXP_FILE_COMP_BUFSIZE,
                    Constants.PROP_DEFAULT_EXP_FILE_COMP_BUFSIZE);
        } else {
            if (!isNumber(expBufSize, 1)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "Exportファイルの圧縮時のバッファサイズの設定が不正。設定値：" + expBufSize);
            }
        }
        // Exporterのリトライ回数
        String expRetryCount = prop.getProperty(Constants.PROP_KEY_EXP_RETRY_COUNT);
        if (isEmpty(expRetryCount)) {
            prop.setProperty(
                    Constants.PROP_KEY_EXP_RETRY_COUNT,
                    Constants.PROP_DEFAULT_EXP_RETRY_COUNT);
        } else {
            if (!isNumber(expRetryCount, 0)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "Exporterのリトライ回数の設定が不正。設定値：" + expRetryCount);
            }
        }
        // Exporterのリトライインターバル
        String expRetryInterval = prop.getProperty(Constants.PROP_KEY_EXP_RETRY_INTERVAL);
        if (isEmpty(expRetryInterval)) {
            prop.setProperty(
                    Constants.PROP_KEY_EXP_RETRY_INTERVAL,
                    Constants.PROP_DEFAULT_EXP_RETRY_INTERVAL);
        } else {
            if (!isNumber(expRetryInterval, 0)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "Exporterのリトライインターバルの設定が不正。設定値：" + expRetryInterval);
            }
        }
        // エクスポート処理でExport対象テーブルにデータをコピーする時の最大レコード数
        String copyMaxRecord = prop.getProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD);
        if (isEmpty(copyMaxRecord)) {
            prop.setProperty(
                    Constants.PROP_KEY_EXP_COPY_MAX_RECORD,
                    Constants.PROP_DEFAULT_EXP_COPY_MAX_RECORD);
        } else {
            if (!isNumber(copyMaxRecord, 1)) {
                throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                        "エクスポート処理でExport対象テーブルにデータをコピーする時の最大レコード数の設定が不正。設定値：" + copyMaxRecord);
            }
        }
        // インポート正常終了時のTSVファイル削除有無
        String deleteImportTsv = prop.getProperty(Constants.PROP_KEY_IMPORT_TSV_DELETE);
        TsvDeleteType delImpType = TsvDeleteType.find(deleteImportTsv);
        if (isEmpty(deleteImportTsv)) {
            prop.setProperty(
                    Constants.PROP_KEY_IMPORT_TSV_DELETE,
                    Constants.PROP_DEFAULT_IMPORT_TSV_DELETE);
        } else if (delImpType == null) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "インポート正常終了時のTSVファイル削除有無が不正。値：" + deleteImportTsv);
        }
        // エクスポート正常終了時のTSVファイル削除有無
        String deleteExportTsv = prop.getProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE);
        TsvDeleteType delExpType = TsvDeleteType.find(deleteExportTsv);
        if (isEmpty(deleteExportTsv)) {
            prop.setProperty(
                    Constants.PROP_KEY_EXPORT_TSV_DELETE,
                    Constants.PROP_DEFAULT_EXPORT_TSV_DELETE);
        } else if (delExpType == null) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "エクスポート正常終了時のTSVファイル削除有無が不正。値：" + deleteExportTsv);
        }

        // 必須チェック
        // remote ASAKUSA_HOME
        if (isEmpty(prop.getProperty(Constants.PROP_PREFIX_HC_ENV + Constants.ASAKUSA_HOME))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                MessageFormat.format(
                        "Hadoopクライアントの環境変数{1}が設定されていません ({0}.{1})",
                        Constants.PROP_PREFIX_HC_ENV,
                        Constants.ASAKUSA_HOME));
        }
        // SSHのパス
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_SSH_PATH))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "SSHのパスが設定されていません");
        }
        // HDFSのNameノードのIPアドレス又はホスト名
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_NAMENODE_HOST))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "HDFSのNameノードのホスト名が設定されていません");
        }
        // HDFSのNameノードのユーザー名
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_NAMENODE_USER))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "SSHのパスが設定されていません");
        }
        // Importファイルを置くディレクトリのトップディレクトリ
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_IMP_FILE_DIR))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "Importファイルを置くディレクトリが設定されていません");
        }
        // エクスポートファイルを置くディレクトリのトップディレクトリ
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_EXP_FILE_DIR))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00008",
                    "エクスポートファイルを置くディレクトリが設定されていません");
        }
    }
    // CHECKSTYLE:ON MethodLengthCheck

    /**
     * 共通のプロパティの必須チェックとデフォルト値を設定する。
     */
    protected static void checkAndSetParam() {
        // デフォルト値の設定
        // log4j.xmlのパス
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_LOG_CONF_PATH))) {
            prop.setProperty(Constants.PROP_KEY_LOG_CONF_PATH, resolvePath(Constants.PROP_DEFAULT_LOG_CONF_PATH));
        }
    }

    /**
     * ファイルからプロパティを読み込む。
     * @param propertyPaths 読み込むプロパティファイル
     * @throws FileNotFoundException ファイルが存在しない場合
     * @throws IOException ファイルの読み込みに失敗した場合
     */
    private static void loadProperties(List<String> propertyPaths) throws IOException {
        assert propertyPaths != null;
        Properties properties = loadRawProperties(propertyPaths);
        VariableTable variables = new VariableTable(RedefineStrategy.IGNORE);
        putAll(variables, sysProp);
        putAll(variables, env);
        prop.putAll(resolveProperties(variables, properties));
    }

    private static void putAll(VariableTable variables, Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                variables.defineVariable((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    private static Properties loadRawProperties(List<String> propertyPaths) throws IOException {
        assert propertyPaths != null;
        Properties properties = new Properties();
        for (String strProp : propertyPaths) {
            File propFile = createPropFileName(strProp);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(propFile);
                properties.load(fis);
            } catch (IOException e) {
                System.err.println(
                        "プロパティファイルの読み込みに失敗しました。ファイル名："
                        + propFile.getAbsolutePath());
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
        return properties;
    }

    private static Properties resolveProperties(VariableTable variables, Properties properties) {
        assert variables != null;
        assert properties != null;
        Properties results = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (canResolveVariables(key)) {
                    value = resolveVariables(variables, key, value);
                } else if (canResolvePath(key)) {
                    value = resolvePath(value);
                }
                results.setProperty(key, value);
            }
        }
        return results;
    }

    private static String resolveVariables(VariableTable variables, String key, String value) {
        assert variables != null;
        assert key != null;
        assert value != null;
        try {
            return variables.parse(value, true);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to resolve environment variables in property file: key={0}, value={1}",
                    key,
                    value), e);
        }
    }

    private static boolean canResolveVariables(String key) {
        return true;
    }

    private static boolean canResolvePath(String key) {
        assert key != null;
        return KEY_PATHS.contains(key);
    }

    private static String resolvePath(String value) {
        assert value != null;
        File raw = new File(value);
        if (raw.isAbsolute()) {
            return value;
        }
        String basePath = ConfigurationLoader.getEnvProperty(Constants.ASAKUSA_HOME);
        File base = new File(basePath);
        File target = new File(base, value);
        return target.getAbsolutePath();
    }

    /**
     * DBMSの接続情報を記述したプロパティファイルを読み込む。
     * @param targetName ターゲット名
     * @throws BulkLoaderSystemException 読み込みエラー
     */
    public static void loadJDBCProp(String targetName) throws BulkLoaderSystemException {
        // DBMSの接続情報を記述したプロパティファイルを読み込み
        String propName = targetName + Constants.JDBC_PROP_NAME;
        try {
            loadProperties(Arrays.asList(new String[] { propName }));
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-COMMON-00012",
                    propName);
        }

        // 必須チェック
        // JDBCドライバ
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_JDBC_DRIVER))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00011",
                    "JDBCドライバが設定されていません");
        }
        // DB接続URL
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_DB_URL))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00011",
                    "DB接続URLが設定されていません");
        }
        // DB接続ユーザー
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_DB_USER))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00011",
                    "DB接続ユーザーが設定されていません");
        }
        // DB接続ユーザーに対するパスワード
        if (isEmpty(prop.getProperty(Constants.PROP_KEY_DB_PASSWORD))) {
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00011",
                    "DB接続ユーザーに対するパスワードが設定されていません");
        }
    }

    /**
     * プロパティファイル名を作成する。
     * プロパティファイル名に絶対パスが指定されている場合は絶対パスのファイル名を使用し、
     * 相対パスが指定されている場合は「$ASAKUSA_HOME/bulkloader/conf/」以下のファイルを使用する
     * @param propFileName ファイル名
     * @return ファイル名のフルパス
     */
    private static File createPropFileName(String propFileName) {
        File tempFile = new File(propFileName);
        if (tempFile.isAbsolute()) {
            return tempFile;
        }
        String applHome = ConfigurationLoader.getEnvProperty(Constants.THUNDER_GATE_HOME);
        File file1 = new File(applHome, Constants.PROP_FILE_PATH);
        File file2 = new File(file1, propFileName);
        return file2;
    }
    /**
     * Returns the submap of current properties whose keys have the common prefix.
     * Keys in the resulting map are dropped their common prefix.
     * @param prefix the common prefix
     * @return submap
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Map<String, String> getPropSubMap(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        Map<String, String> results = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                String key = (String) entry.getKey();
                if (key.startsWith(prefix)) {
                    results.put(key.substring(prefix.length()), (String) entry.getValue());
                }
            }
        }
        return results;
    }
    /**
     * プロパティから特定の文字列から開始するKeyのリストを列挙する。
     * @param startString 開始文字列
     * @return 設定に含まれ、かつ指定の開始文字列を持つ設定キーの一覧
     */
    public static List<String> getPropStartWithString(String startString) {
        Set<Object> propSet = prop.keySet();
        List<String> list = new ArrayList<String>();

        for (Object o : propSet) {
            String key = (String) o;
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
    public static List<String> getExistValueList(List<String> list) {
        List<String> resultList = new ArrayList<String>();
        if (list == null || list.size() == 0) {
            return resultList;
        }
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            String key = list.get(i);
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
        if (str.isEmpty()) {
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
            long parsed = Long.parseLong(str);
            return parsed >= min;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the script path.
     * @param relativePath script relative path from thundergate installation home
     * @return the script path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public static File getLocalScriptPath(String relativePath) {
        if (relativePath == null) {
            throw new IllegalArgumentException("relativePath must not be null"); //$NON-NLS-1$
        }
        File base = new File(getEnvProperty(Constants.THUNDER_GATE_HOME));
        return new File(base, relativePath);
    }

    /**
     * Returns the script path.
     * @param relativePath script relative path from framework installation home
     * @return the script path
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public static String getRemoteScriptPath(String relativePath) {
        if (relativePath == null) {
            throw new IllegalArgumentException("relativePath must not be null"); //$NON-NLS-1$
        }
        String remoteHome = getProperty(Constants.PROP_PREFIX_HC_ENV + Constants.ASAKUSA_HOME);
        if (remoteHome.endsWith("/") == false) {
            remoteHome = remoteHome + "/";
        }
        return remoteHome + Constants.PATH_REMOTE_ROOT + relativePath;
    }

    /**
     * 指定のキーに対応するプロパティを返す。
     * @param key キー
     * @return String 対応するプロパティ、存在しない場合は{@code null}
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
     * Returns index name for duplication check about target table.
     * @param batchId current batch ID
     * @param jobflowId current jobflow ID
     * @param tableName target table name
     * @return corresponded index name, or {@code null} if not defined
     * @since 0.2.6
     */
    public static String getForceIndexName(String batchId, String jobflowId, String tableName) {
        StringBuilder buf = new StringBuilder();
        buf.append("dupcheck.index.");
        buf.append(batchId);
        buf.append("|");
        buf.append(jobflowId);
        buf.append("|");
        buf.append(tableName);
        String indexName = prop.getProperty(buf.toString());
        return indexName;
    }

    /**
     * このクラスが提供するプロパティを設定する。
     * @param p 設定するプロパティ
     * @deprecated UT用
     */
    @Deprecated
    public static void setProperty(Properties p) {
        prop = p;
    }
    /**
     * このクラスが提供するプロパティ全体のビューを返す。
     * @return プロパティ全体のビュー
     * @deprecated UT用
     */
    @Deprecated
    public static Properties getProperty() {
        return prop;
    }
    /**
     * このクラスが提供するシステムプロパティを設定する。
     * @param p 設定するプロパティ
     * @deprecated UT用
     */
    @Deprecated
    public static void setSysProp(Properties p) {
        sysProp = p;
    }
    /**
     * このクラスが提供する環境変数を設定する。
     * @param m 設定する環境変数の表
     * @deprecated UT用
     */
    @Deprecated
    public static void setEnv(Map<String, String> m) {
        env = m;
    }

}
