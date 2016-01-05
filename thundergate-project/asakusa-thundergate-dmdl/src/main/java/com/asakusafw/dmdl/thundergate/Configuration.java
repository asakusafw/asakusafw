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
package com.asakusafw.dmdl.thundergate;

import java.io.File;
import java.nio.charset.Charset;

import com.asakusafw.dmdl.model.AstLiteral;

/**
 * このアプリケーションの設定情報。
 * @since 0.2.0
 * @version 0.6.1
 */
public class Configuration {

    private String jdbcDriver;

    private String jdbcUrl;

    private String jdbcUser;

    private String jdbcPassword;

    private String databaseName;

    private File output;

    private ModelMatcher matcher;

    private Charset encoding;

    private String sidColumn;

    private String timestampColumn;

    private String deleteFlagColumn;

    private AstLiteral deleteFlagValue;

    private File recordLockDdlOutput;

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
     * Returns the output DMDL encoding.
     * @return the output DMDL encoding
     */
    public Charset getEncoding() {
        return encoding;
    }

    /**
     * Sets the output DMDL encoding.
     * @param encoding the encoding to set
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the column name of System ID.
     * @return the column name of System ID, or {@code null} if not defined
     * @since 0.2.3
     */
    public String getSidColumn() {
        return sidColumn;
    }

    /**
     * Sets the column name of System ID.
     * @param sidColumn the column name, or {@code null} to clear
     * @since 0.2.3
     */
    public void setSidColumn(String sidColumn) {
        this.sidColumn = sidColumn;
    }

    /**
     * Returns the column name of Modification Timestamp.
     * @return the column name of Modification Timestamp, or {@code null} if not defined
     * @since 0.2.3
     */
    public String getTimestampColumn() {
        return timestampColumn;
    }

    /**
     * Sets the column name of System ID.
     * @param timestampColumn the column name, or {@code null} to clear
     * @since 0.2.3
     */
    public void setTimestampColumn(String timestampColumn) {
        this.timestampColumn = timestampColumn;
    }

    /**
     * Returns the column name of Logical Delete Flag.
     * @return the column name of Logical Delete Flag, or {@code null} if not defined
     * @since 0.2.3
     */
    public String getDeleteFlagColumn() {
        return deleteFlagColumn;
    }

    /**
     * Sets the column name of Delete Flag.
     * @param deleteFlagColumn the column name, or {@code null} to clear
     * @since 0.2.3
     */
    public void setDeleteFlagColumn(String deleteFlagColumn) {
        this.deleteFlagColumn = deleteFlagColumn;
    }

    /**
     * Returns the column value of logical delete flag is true.
     * @return the column value in Java literal, or {@code null} if not defined
     * @since 0.2.3
     */
    public AstLiteral getDeleteFlagValue() {
        return deleteFlagValue;
    }

    /**
     * Sets the column value of logical delete flag is true.
     * @param deleteFlagValue the column value in Java literal, or {@code null} to clear
     * @since 0.2.3
     */
    public void setDeleteFlagValue(AstLiteral deleteFlagValue) {
        this.deleteFlagValue = deleteFlagValue;
    }

    /**
     * Returns the output target path of record lock tables DDL file.
     * @return the output target path, or {@code null} if it is not required
     * @since 0.6.1
     */
    public File getRecordLockDdlOutput() {
        return recordLockDdlOutput;
    }

    /**
     * Sets the output target path of record lock tables DDL file.
     * @param file the output target path, or {@code null} if it is not required
     * @since 0.6.1
     */
    public void setRecordLockDdlOutput(File file) {
        this.recordLockDdlOutput = file;
    }
}
