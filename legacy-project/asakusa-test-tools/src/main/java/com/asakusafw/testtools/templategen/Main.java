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
package com.asakusafw.testtools.templategen;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.testtools.Configuration;
import com.asakusafw.testtools.db.DbUtils;

/**
 * プログラムエントリ。
 */
public final class Main {

    private Connection conn;
    private List<String> tableList;
    private String databaseName;
    private File outputDirectory;

    /**
     * プログラムエントリ。
     * @param args Excelブックを生成する対象テーブル名の一覧
     * @throws IOException Excelファイルの生成に失敗した場合
     * @throws SQLException データベースからテーブル情報を取得できなかった場合
     */
    public static void main(String[] args) throws IOException, SQLException {
        Main main = new Main(args);
        main.run();
    }

    private Main(String[] args) throws SQLException {
        if (args.length == 0) {
            throw new IllegalArgumentException("No tabble names in argments.");
        }
        tableList = new ArrayList<String>(args.length);
        for (String tablename : args) {
            tableList.add(tablename);
        }
        Configuration conf = Configuration.getInstance();
        String outputDirectoryName = conf.getOutputDirectory();
        if (outputDirectoryName == null) {
            throw new IllegalArgumentException("output directory must not be null");
        }
        outputDirectory = new File(outputDirectoryName);
        if (!outputDirectory.exists()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "出力ディレクトリ({0})が存在しません",
                    outputDirectoryName));
        }
        if (!outputDirectory.isDirectory()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "指定された出力ディレクトリ({0})はディレクトリでありません",
                    outputDirectoryName));
        }
        if (!outputDirectory.canWrite()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "指定された出力ディレクトリ({0})の書き込み権が存在しません",
                    outputDirectoryName));
        }
        databaseName = conf.getDatabaseName();
        if (databaseName == null) {
            throw new IllegalArgumentException("database name must not be null");
        }
        conn = DbUtils.getConnection();
    }

    private void run() throws IOException, SQLException {
        for (String tableName : tableList) {
            ExcelBookBuilder ebb = new ExcelBookBuilder(conn, tableName, databaseName);
            ebb.build(outputDirectory);
        }
    }
}
