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
package com.asakusafw.modelgen;

import java.io.File;
import java.util.List;


/**
 * このアプリケーションの設定情報。
 */
public class Configuration {

    private String jdbcDriver;

    private String jdbcUrl;

    private String jdbcUser;

    private String jdbcPassword;

    private String databaseName;

    private String basePackage;

    private File output;

    private List<String> headerComments;

    private ModelMatcher matcher;

    /**
     * 使用するJDBC Driverを返す。
     * @return JDBC Driverクラスの完全限定名
     */
    public String getJdbcDriver() {
        return jdbcDriver;
    }

    /**
     * 使用するJDBC Driverを変更する。
     * @param jdbcDriver JDBC Driverクラスの完全限定名
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
     * 出力時のルートパッケージを返す。
     * @return 出力時のルートパッケージ
     */
    public String getBasePackage() {
        return basePackage;
    }

    /**
     * 出力時のルートパッケージを変更する。
     * @param basePackage 設定するパッケージ名
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * 出力先のディレクトリを返す。
     * @return 出力先のディレクトリ
     */
    public File getOutput() {
        return output;
    }

    /**
     * 出力先のディレクトリを変更する。
     * @param output 設定するパス
     */
    public void setOutput(File output) {
        this.output = output;
    }

    /**
     * 解析対象から除外するモデルのマッチャを返す。
     * @return モデルのマッチャ
     */
    public ModelMatcher getMatcher() {
        return matcher;
    }

    /**
     * 解析対象から除外するモデルのマッチャを設定する。
     * @param matcher 設定するマッチャ
     */
    public void setMatcher(ModelMatcher matcher) {
        this.matcher = matcher;
    }

    /**
     * ヘッダコメントの内容を返す。
     * @return ヘッダコメントの内容、存在しない場合は{@code null}
     */
    public List<String> getHeaderComments() {
        return headerComments;
    }

    /**
     * ヘッダコメントの内容を設定する。
     * @param headerComments 設定する内容
     */
    public void setHeaderComments(List<String> headerComments) {
        this.headerComments = headerComments;
    }
}
