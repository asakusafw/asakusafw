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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.db.DbUtils;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * asakusa-test-toolsが提供するAPIを使って実装されたテストドライバの基底クラス。
 *
 */
@SuppressWarnings("deprecation")
public class TestDriverTestToolsBase extends TestDriverBase {

    private static final Logger LOG = LoggerFactory.getLogger(TestDriverTestToolsBase.class);

    private static final String BUILD_PROPERTIES_FILE = "build.properties";

    /** テストデータ格納先のデフォルト値。 */
    protected static final String TESTDATA_DIR_DEFAULT = "src/test/data/excel";

    /** テストデータ生成・検証ツールオブジェクト。 */
    protected TestUtils testUtils;
    /** TestUtils生成時に指定するテストデータ定義シートのファイルリスト。 */
    protected List<File> testDataFileList;
    /** TestUtils生成時に指定するテストデータ定義シートのディレクトリ（testDataFileListと排他)。 */
    protected File testDataDir;

    /** {@code build.properties}. */
    protected Properties buildProperties;

    /**
     * コンストラクタ。
     *
     * @throws RuntimeException 初期化に失敗した場合
     */
    public TestDriverTestToolsBase() {
        super(findCaller().getDeclaringClass());
        initialize(findCaller());
    }

    /**
     * コンストラクタ。
     *
     * @param testDataFileList テストデータ定義シートのパスを示すFileのリスト
     * @throws RuntimeException 初期化に失敗した場合
     */
    public TestDriverTestToolsBase(List<File> testDataFileList) {
        super(findCaller().getDeclaringClass());
        this.testDataFileList = testDataFileList;
        initialize(findCaller());
    }

    private static Method findCaller() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        for (StackTraceElement element : trace) {
            try {
                Class<?> aClass = Class.forName(element.getClassName());
                if (TestDriverTestToolsBase.class.isAssignableFrom(aClass)) {
                    continue;
                }
                Method method = aClass.getDeclaredMethod(element.getMethodName());
                return method;
            } catch (Exception e) {
                continue;
            }
        }
        throw new IllegalStateException("テストドライバのインスタンスを作成したテストを特定できませんでした");
    }

    private void initialize(Method caller) {
        assert caller != null;
        try {
            File buildPropertiesFile = new File(BUILD_PROPERTIES_FILE);
            if (buildPropertiesFile.exists()) {
                LOG.info("ビルド設定情報をロードしています: {}", buildPropertiesFile);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(buildPropertiesFile);
                    buildProperties = new Properties();
                    buildProperties.load(fis);
                    System.setProperty(
                            "ASAKUSA_MODELGEN_PACKAGE",
                            buildProperties.getProperty("asakusa.modelgen.package"));
                    System.setProperty(
                            "ASAKUSA_MODELGEN_OUTPUT",
                            buildProperties.getProperty("asakusa.modelgen.output"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            } else {
                LOG.info("ビルド設定情報が存在しないため、スキップします: {}", BUILD_PROPERTIES_FILE);
            }
            System.setProperty("ASAKUSA_TESTTOOLS_CONF",
                    String.format(
                            "%s/bulkloader/conf/%s-jdbc.properties",
                            System.getenv("ASAKUSA_HOME"),
                            buildProperties.getProperty("asakusa.database.target")));
            System.setProperty(
                    "ASAKUSA_TEMPLATEGEN_OUTPUT_DIR",
                    buildProperties.getProperty("asakusa.testdatasheet.output"));
            String testDataDirPath = buildProperties.getProperty("asakusa.testdriver.testdata.dir");
            if (testDataDirPath == null) {
                testDataDirPath = TESTDATA_DIR_DEFAULT;
            }
            if (testDataFileList == null) {
                testDataDir = new File(
                        testDataDirPath
                        + File.separatorChar
                        + caller.getDeclaringClass().getSimpleName()
                        + File.separatorChar
                        + caller.getName());
                testUtils = new TestUtils(testDataDir);
            } else {
                testUtils = new TestUtils(testDataFileList);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets timestamp value to the last modified timestamp columns if they are known.
     * @param timestamp target timestamp
     */
    protected void setLastModifiedTimestamp(Timestamp timestamp) {
        for (String tableName : testUtils.getTablenames()) {
            String timestampColumn = findTimestampColumn(tableName);
            if (timestampColumn != null) {
                updateTimestamp(tableName, timestampColumn, timestamp);
            }
        }
    }

    private String findTimestampColumn(String tableName) {
        Class<?> tableClass = testUtils.getClassByTablename(tableName);
        if (tableClass == null) {
            return null;
        }
        if (ThunderGateCacheSupport.class.isAssignableFrom(tableClass)) {
            try {
                ThunderGateCacheSupport instance = tableClass
                    .asSubclass(ThunderGateCacheSupport.class)
                    .newInstance();
                return instance.__tgc__TimestampColumn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void updateTimestamp(String tableName, String timestampColumn, Timestamp timestamp) {
        LOG.info("テーブル{}のカラム{}を初期化しています", tableName, timestampColumn);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DbUtils.getConnection();
            stmt = conn.prepareStatement(MessageFormat.format(
                    "UPDATE {0} SET {1} = ? WHERE {1} IS NULL",
                    tableName,
                    timestampColumn));
            stmt.setTimestamp(1, timestamp);
            int rows = stmt.executeUpdate();
            LOG.info("テーブル{}のカラム{}を初期化しました: {}件", new Object[] {
                tableName,
                timestampColumn,
                rows
            });
            if (conn.getAutoCommit() == false) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }
}
