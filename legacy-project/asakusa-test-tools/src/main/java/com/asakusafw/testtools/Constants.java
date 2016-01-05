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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 定数表。
 */
public final class Constants {
    /**
     * 入力データのシートに取り込むデータの最大行数。
     */
    public static final int MAX_ROWS = 10000;

    /**
     * 利用する全ての環境変数名の接頭辞。
     */
    private static final String[] ENV_PREFIX = { "ASAKUSA_", "NS_" };

    /**
     * 設定ファイルの位置を示す環境変数名。
     */
    public static final List<String> ENV_PROPERTIES = buildEnvProperties("TESTTOOLS_CONF");

    /**
     * モデルクラスのパッケージを示す環境変数名。
     */
    public static final List<String> ENV_BASE_PACKAGE = buildEnvProperties("MODELGEN_PACKAGE");

    /**
     * テンプレートジェネレータの出力ディレクトリを示す環境変数名。
     */
    public static final List<String> ENV_TEMPLATEGEN_OUTPUT_DIR = buildEnvProperties("TEMPLATEGEN_OUTPUT_DIR");
    
    /**
     * JDBCの設定ファイル内に記述する、JDBC Driverのキー。
     */
    public static final String K_JDBC_DRIVER = "jdbc.driver";

    /**
     * 設定ファイル内に記述する、JDBC URLのキー。
     */
    public static final String K_JDBC_URL = "jdbc.url";

    /**
     * 設定ファイル内に記述する、ログインユーザー名のキー。
     */
    public static final String K_JDBC_USER = "jdbc.user";

    /**
     * 設定ファイル内に記述する、パスワードのキー。
     */
    public static final String K_JDBC_PASSWORD = "jdbc.password";

    /**
     * 設定ファイル内に記述するデータベース名。
     */
    public static final String K_DATABASE_NAME = "database.name";

    /**
     * 設定ファイル内に記述する出力ディレクトリ名。
     */
    public static final String K_OUTPUT_DIR = "output.directory";

    /**
     * 入力データを定義するExcelシートのシート名。
     */
    public static final String INPUT_DATA_SHEET_NAME = "入力データ";

    /**
     * 出力データを定義するExcelシートのシート名。
     */
    public static final String OUTPUT_DATA_SHEET_NAME = "出力データ";

    /**
     * テスト条件を定義するExcelシートのシート名。
     */
    public static final String TEST_CONDITION_SHEET_NAME = "テスト条件";

    private static List<String> buildEnvProperties(String suffix) {
        assert suffix != null;
        List<String> properties = new ArrayList<String>(ENV_PREFIX.length);
        for (String prefix : ENV_PREFIX) {
            properties.add(prefix + suffix);
        }
        return Collections.unmodifiableList(properties);
    }

    private Constants() {
        return;
    }
}
