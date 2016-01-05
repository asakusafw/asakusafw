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
package com.asakusafw.testtools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * このアプリケーションの設定情報。
 */
public final class Configuration {

    private static Configuration conf;

    /**
     * このクラスの唯一のインスタンスを返す。
     * @return このクラスの唯一のインスタンス
     */
    public static synchronized Configuration  getInstance() {
        if (conf == null) {
            loadConfigurationFromEnvironment();
        }
        return conf;
    }

    private Configuration() {
        // コンストラクタを使わせない
    }

    /**
     * JDBCドライバ名。
     */
    private String jdbcDriver;

    /**
     * JDBC URL。
     */
    private String jdbcUrl;

    /**
     * DBユーザ。
     */
    private String jdbcUser;

    /**
     * DBのパスワード。
     */
    private String jdbcPassword;

    /**
     * データベース名。
     */
    private String databaseName;

    /**
     * 出力ディレクトリ。
     */
    private String outputDirectory;

    /**
     * モデルクラスのパッケージ名。
     */
    private String modelPackage;

    /**
     * 出力ディレクトリを取得します。
     * @return outputDirectory
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * 出力ディレクトリを設定します。
     * @param outputDirectory outputDirectory
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * 使用するJDBC Driverを取得します。
     * @return jdbcDriver
     */
    public String getJdbcDriver() {
        return jdbcDriver;
    }

    /**
     * 使用するJDBC Driverを設定します。
     * @param jdbcDriver jdbcDriver
     */
    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    /**
     * JDBCでの接続先URLを返す。
     * @return JDBCでの接続先URL
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * JDBCでの接続先URLを変更する。
     * @param jdbcUrl 設定するURL
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * JDBCでの接続ユーザーを返す。
     * @return JDBCでの接続ユーザー
     */
    public String getJdbcUser() {
        return jdbcUser;
    }

    /**
     * JDBCでの接続ユーザーを変更する。
     * @param jdbcUser 設定するユーザー名
     */
    public void setJdbcUser(String jdbcUser) {
        this.jdbcUser = jdbcUser;
    }

    /**
     * JDBCでの接続パスワードを返す。
     * @return JDBCでの接続パスワード
     */
    public String getJdbcPassword() {
        return jdbcPassword;
    }

    /**
     * JDBCでの接続パスワードを変更する。
     * @param jdbcPassword 設定するパスワード
     */
    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }

    /**
     * 処理対象のDatabase名を取得します。
     * @return databaseName
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 処理対象のDatabase名を設定します。
     * @param databaseName databaseName
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * 環境変数から設定情報を復元する。
     * @throws IllegalStateException 復元に失敗した場合
     */
    private static void loadConfigurationFromEnvironment() {
        conf = new Configuration();

        String path = findVariable(Constants.ENV_PROPERTIES, true);
        try {
            Properties props = loadProperties(path);
            conf.setJdbcDriver(findProperty(props, Constants.K_JDBC_DRIVER));
            conf.setJdbcUrl(findProperty(props, Constants.K_JDBC_URL));
            conf.setJdbcUser(findProperty(props, Constants.K_JDBC_USER));
            conf.setJdbcPassword(findProperty(props, Constants.K_JDBC_PASSWORD));
            conf.setDatabaseName(findProperty(props, Constants.K_DATABASE_NAME));
            
            String outputDir = findVariable(Constants.ENV_TEMPLATEGEN_OUTPUT_DIR, false);
            if (outputDir == null) {
                outputDir = findProperty(props, Constants.K_OUTPUT_DIR);
            }
            conf.setOutputDirectory(outputDir);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "設定ファイルの読み出しに失敗しました: {0}={1}",
                    Constants.ENV_PROPERTIES,
                    path),
                    e);
        }
        String modelPackage = findVariable(Constants.ENV_BASE_PACKAGE, true);
        conf.setModelPackage(modelPackage);
    }

    private static String findProperty(Properties properties, String key) {
        assert properties != null;
        assert key != null;
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "プロパティ\"{0}\"が設定されていません",
                    key));
        }
        return value;
    }


    private static String findVariable(List<String> variableNames, boolean mandatory) {
        assert variableNames != null;
        assert variableNames.isEmpty() == false;
        String value = null;
        for (String var : variableNames) {
            value = System.getProperty(var);
            if (value == null) {
                value = System.getenv(var);
            }
            if (value != null) {
                break;
            }
        }
        if (mandatory && value == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "環境変数\"{0}\"が設定されていません",
                    variableNames.get(0)));
        }
        return value;
    }

    private static Properties loadProperties(String path) throws IOException {
        assert path != null;
        InputStream in = new FileInputStream(path);
        try {
            Properties result = new Properties();
            result.load(in);
            return result;
        } finally {
            in.close();
        }
    }

    /**
     * モデルクラスのパッケージ名を取得します。
     * @return モデルクラスのパッケージ名
     */
    public String getModelPackage() {
        return modelPackage;
    }

    /**
     * モデルクラスのパッケージ名を設定します。
     * @param modelPackage モデルクラスのパッケージ名
     */
    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }
}
