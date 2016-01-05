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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 定数表。
 */
public final class Constants {

    /**
     * バージョン番号。
     */
    public static final String VERSION = "0.0.1";

    /**
     * ファイルを出力する際のエンコーディング。
     */
    public static final Charset OUTPUT_ENCODING = Charset.forName("UTF-8");

    /**
     * テーブルのソース名。
     */
    public static final String SOURCE_TABLE = "table";

    /**
     * ビューのソース名。
     */
    public static final String SOURCE_VIEW = "view";

    /**
     * モデルのカテゴリ名。
     */
    public static final String CATEGORY_MODEL = "model";

    /**
     * IOのカテゴリ名。
     */
    public static final String CATEGORY_IO = "io";

    /**
     * 利用する全ての環境変数名の接頭辞。
     */
    private static final String[] ENV_PREFIX = { "ASAKUSA_MODELGEN_", "NS_MODELGEN_" };

    /**
     * JDBCの接続に関する設定ファイルの位置を示す環境変数名。
     * @see #K_JDBC_URL
     * @see #K_JDBC_USER
     * @see #K_JDBC_PASSWORD
     */
    public static final List<String> ENV_JDBC_PROPERTIES = buildEnvProperties("JDBC");

    /**
     * 出力先のルートパッケージ。
     * <p>
     * 実際の出力は、このパッケージを基点として名前空間を考慮して出力する。
     * </p>
     */
    public static final List<String> ENV_BASE_PACKAGE = buildEnvProperties("PACKAGE");

    /**
     * 出力先のディレクトリ。
     * <p>
     * 実際の出力は、このディレクトリを基点にJavaのパッケージ構成に従って出力する。
     * </p>
     */
    public static final List<String> ENV_OUTPUT = buildEnvProperties("OUTPUT");

    /**
     * 解析対象に含めるテーブルやビュー名の正規表現。
     * <p>
     * これを指定しない場合、全てのテーブルとビューを許可する。
     * </p>
     */
    public static final List<String> ENV_MODEL_INCLUDES = buildEnvProperties("MODEL_INCLUDES");

    /**
     * 解析対象から除外するテーブルやビュー名の正規表現。
     * <p>
     * これを指定しない場合、全てのテーブルとビューを許可する。
     * </p>
     */
    public static final List<String> ENV_MODEL_EXCLUDES = buildEnvProperties("MODEL_EXCLUDES");

    /**
     * 出力するソースプログラムのヘッダに指定するコメントの内容を記録したファイルへのパス。
     * <p>
     * 対象のファイルは、Javaのコメントの形式<em>ではなく</em>、
     * ライセンスの内容等をプレーンに記述したものである必要がある。
     * また、それらの内容はUTF-8エンコードで記述される必要がある。
     * </p>
     * <p>
     * これを指定しない場合、ヘッダコメントは付けられない。
     * </p>
     */
    public static final List<String> ENV_HEADER_COMENT = buildEnvProperties("HEADER_COMMENT");

    /**
     * JDBCの設定ファイル内に記述する、JDBC Driverのキー。
     */
    public static final String K_JDBC_DRIVER = "jdbc.driver";

    /**
     * JDBCの設定ファイル内に記述する、JDBC URLのキー。
     */
    public static final String K_JDBC_URL = "jdbc.url";

    /**
     * JDBCの設定ファイル内に記述する、ログインユーザー名のキー。
     */
    public static final String K_JDBC_USER = "jdbc.user";

    /**
     * JDBCの設定ファイル内に記述する、パスワードのキー。
     */
    public static final String K_JDBC_PASSWORD = "jdbc.password";

    /**
     * JDBC設定ファイル内に記述するデータベース名。
     */
    public static final String K_DATABASE_NAME = "database.name";

    /**
     * プロパティの内容をコピーするメソッドの名前。
     */
    public static final String NAME_OPTION_COPIER = "copyFrom";

    /**
     * プロパティの内容を変更するメソッドの名前。
     */
    public static final String NAME_OPTION_MODIFIER = "modify";

    /**
     * プロパティの内容を変更するメソッドの名前。
     */
    public static final String NAME_OPTION_EXTRACTOR = "get";

    /**
     * プロパティの内容をクリアするメソッドの名前。
     */
    public static final String NAME_OPTION_ERASER = "setNull";

    /**
     * プロパティの内容を加算するメソッドの名前。
     */
    public static final String NAME_OPTION_ADDER = "add";

    /**
     * プロパティの内容のうち最大のものを利用するメソッドの名前。
     */
    public static final String NAME_OPTION_MAX = "max";

    /**
     * プロパティの内容のうち最小のものを利用するメソッドの名前。
     */
    public static final String NAME_OPTION_MIN = "min";

    /**
     * ModelInputの単純名フォーマット。
     */
    public static final String FORMAT_NAME_MODEL_INPUT = "{0}ModelInput";

    /**
     * ModelOutputの単純名フォーマット。
     */
    public static final String FORMAT_NAME_MODEL_OUTPUT = "{0}ModelOutput";

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
