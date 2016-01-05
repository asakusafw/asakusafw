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
package com.asakusafw.bulkloader.exporter;


import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.ExportTempTableStatus;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.inspect.Cause;

/**
 * TempTableDeleteのテストクラス
 * @author yuta.shirai
 *
 */
public class TempTableDeleteTest {
    /** Importerで読み込むプロパティファイル */
    private static List<String> propertys = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    /** ジョブフローID */
    private static String jobflowId = "JOB_FLOW01";
    /** ジョブフロー実行ID */
    private static String executionId = "JOB_FLOW01-001";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, "target1");
        UnitTestUtil.setUpDB();
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }

    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, "target1");
        UnitTestUtil.startUp();
    }

    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
    }

    /**
     *
     * <p>
     * deleteのテストケース
     * 正常系：テンポラリテーブルと、エクスポートテンポラリ管理テーブルのレコードの削除に成功するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTest01() throws Exception {
        // 入力値の生成
        ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
        tempBean[0] = new ExportTempTableBean();
        tempBean[0].setJobflowSid("11");
        tempBean[0].setExportTableName("table1");
        tempBean[0].setTemporaryTableName("teble1_tmp");
        tempBean[0].setTempTableStatus(ExportTempTableStatus.find("1"));
        tempBean[1] = new ExportTempTableBean();
        tempBean[1].setJobflowSid("11");
        tempBean[1].setExportTableName("table2");
        tempBean[1].setTemporaryTableName("table2_temp");
        tempBean[1].setTempTableStatus(ExportTempTableStatus.find("setTempTableStatus"));

        // 処理の実行
        TempTableDelete delete = new TempTableDelete() {
            /**
             * @see com.asakusafw.bulkloader.exporter.TempTableDelete#deleteTempInfoRecord(java.lang.String, java.lang.String, java.sql.Connection)
             */
            @Override
            public void deleteTempInfoRecord(String jobflowSid,
                    String tableName, boolean copyNotEnd, Connection conn) throws BulkLoaderSystemException {
            }

            /**
             * @see com.asakusafw.bulkloader.exporter.TempTableDelete#deleteTempTable(java.lang.String, java.sql.Connection)
             */
            @Override
            public void deleteTempTable(String exportTempName, String duplicateFlagTableName, boolean copyNotEnd, Connection conn)
                    throws BulkLoaderSystemException {
            }
        };
        boolean resutlt = delete.delete(Arrays.asList(tempBean), true);

        // 結果の検証
        assertTrue(resutlt);
    }
    /**
     *
     * <p>
     * deleteのテストケース
     * 正常系：削除するテンポラリテーブルが存在しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTest02() throws Exception {
        // 入力値の生成
        ExportTempTableBean[] tempBean = new ExportTempTableBean[0];

        // 処理の実行
        TempTableDelete delete = new TempTableDelete() {
            /**
             * @see com.asakusafw.bulkloader.exporter.TempTableDelete#deleteTempInfoRecord(java.lang.String, java.lang.String, java.sql.Connection)
             */
            @Override
            public void deleteTempInfoRecord(String jobflowSid,
                    String tableName, boolean copyNotEnd, Connection conn) throws BulkLoaderSystemException {
            }

            /**
             * @see com.asakusafw.bulkloader.exporter.TempTableDelete#deleteTempTable(java.lang.String, java.sql.Connection)
             */
            @Override
            public void deleteTempTable(String exportTempName, String duplicateFlagTableName, boolean copyNotEnd, Connection conn)
                    throws BulkLoaderSystemException {
            }
        };
        boolean resutlt =delete.delete(Arrays.asList(tempBean), true);

        // 結果の検証
        assertTrue(resutlt);
    }
    /**
     *
     * <p>
     * deleteのテストケース
     * 異常系：テンポラリテーブルの削除に失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTest03() throws Exception {
        // 入力値の生成
        ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
        tempBean[0] = new ExportTempTableBean();
        tempBean[0].setJobflowSid("11");
        tempBean[0].setExportTableName("table1");
        tempBean[0].setTemporaryTableName("teble1_tmp");
        tempBean[0].setTempTableStatus(ExportTempTableStatus.find("1"));
        tempBean[1] = new ExportTempTableBean();
        tempBean[1].setJobflowSid("11");
        tempBean[1].setExportTableName("table2");
        tempBean[1].setTemporaryTableName("table2_temp");
        tempBean[1].setTempTableStatus(ExportTempTableStatus.find("1"));

        // 処理の実行
        TempTableDelete delete = new TempTableDelete() {
            @Override
            public void deleteTempInfoRecord(String jobflowSid,
                    String tableName, boolean copyNotEnd, Connection conn) throws BulkLoaderSystemException {
            }

            @Override
            public void deleteTempTable(String exportTempName, String duplicateFlagTableName, boolean copyNotEnd, Connection conn)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(this.getClass(), "TG-EXPORTER-01001");
            }
        };
        boolean resutlt =delete.delete(Arrays.asList(tempBean), true);

        // 結果の検証
        assertFalse(resutlt);
    }
    /**
     *
     * <p>
     * deleteのテストケース
     * 異常系：エクスポートテンポラリ管理テーブルのレコードの削除に失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTest04() throws Exception {
        // 入力値の生成
        ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
        tempBean[0] = new ExportTempTableBean();
        tempBean[0].setJobflowSid("11");
        tempBean[0].setExportTableName("table1");
        tempBean[0].setTemporaryTableName("teble1_tmp");
        tempBean[0].setTempTableStatus(ExportTempTableStatus.find("1"));
        tempBean[1] = new ExportTempTableBean();
        tempBean[1].setJobflowSid("11");
        tempBean[1].setExportTableName("table2");
        tempBean[1].setTemporaryTableName("table2_temp");
        tempBean[1].setTempTableStatus(ExportTempTableStatus.find("1"));

        // 処理の実行
        TempTableDelete delete = new TempTableDelete() {
            @Override
            public void deleteTempInfoRecord(String jobflowSid,
                    String tableName, boolean copyNotEnd, Connection conn) throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(this.getClass(), "TG-EXPORTER-01001");
            }
            @Override
            public void deleteTempTable(String exportTempName, String duplicateFlagTableName, boolean copyNotEnd, Connection conn)
                    throws BulkLoaderSystemException {
            }
        };
        boolean resutlt =delete.delete(Arrays.asList(tempBean), true);

        // 結果の検証
        assertFalse(resutlt);
    }

    /**
     *
     * <p>
     * deleteTempInfoRecordのテストケース
     * 正常系：エクスポートテンポラリ管理テーブルのレコードを削除するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTempInfoRecordTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/TempTableDeleteTest/deleteTempInfoRecordTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        TempTableDelete delete = new TempTableDelete();
        Connection conn =DBConnection.getConnection();
        try {
            delete.deleteTempInfoRecord("1", "Table2", true, conn);
            DBConnection.commit(conn);
        } catch (Exception e) {
            DBConnection.rollback(conn);
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }
    /**
     *
     * <p>
     * deleteTempInfoRecordのテストケース
     * 正常系：エクスポートテンポラリ管理テーブルのレコードが存在しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTempInfoRecordTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/TempTableDeleteTest/deleteTempInfoRecordTest02");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        TempTableDelete delete = new TempTableDelete();
        Connection conn =DBConnection.getConnection();
        try {
            delete.deleteTempInfoRecord("11", "Table2", true, conn);
            DBConnection.commit(conn);
        } catch (Exception e) {
            DBConnection.rollback(conn);
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }
    /**
     *
     * <p>
     * deleteTempInfoRecordのテストケース
     * 正常系：コピーが完了していないレコードを処理しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTempInfoRecordTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/TempTableDeleteTest/deleteTempInfoRecordTest03");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        TempTableDelete delete = new TempTableDelete();
        Connection conn =DBConnection.getConnection();
        try {
            delete.deleteTempInfoRecord("11", "Table2", false, conn);
            DBConnection.commit(conn);
        } catch (Exception e) {
            DBConnection.rollback(conn);
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }
    /**
     *
     * <p>
     * deleteTempTableのテストケース
     * 正常系：テンポラリテーブルを削除するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTempTableTest01() throws Exception {
        // テンポラリテーブルを作成
        String tempTable = "EXPORT_TEMP_TEST_01";
        String dropSql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01";
        String createSql = "CREATE TABLE EXPORT_TEMP_TEST_01 (TEXTDATA1 VARCHAR(45) NOT NULL ,INTDATA1 INT NULL,PRIMARY KEY (TEXTDATA1)) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql);
        UnitTestUtil.executeUpdate(createSql);

        String dropDup1Sql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01_DF";
        StringBuilder dup1Sql = new StringBuilder();
        dup1Sql.append("CREATE  TABLE `EXPORT_TEMP_TEST_01_DF` (");
        dup1Sql.append("  `TEMP_SID` BIGINT,");
        dup1Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");
        UnitTestUtil.executeUpdate(dropDup1Sql);
        UnitTestUtil.executeUpdate(dup1Sql.toString());

        // 処理の実行
        TempTableDelete delete = new TempTableDelete();
        Connection conn =DBConnection.getConnection();
        try {
            delete.deleteTempTable(tempTable, "EXPORT_TEMP_TEST_01_DF", true, conn);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }

        // 結果を検証
        assertFalse(UnitTestUtil.isExistTable(tempTable));
        assertFalse(UnitTestUtil.isExistTable("EXPORT_TEMP_TEST_01_DF"));

        // テーブルを削除
        UnitTestUtil.executeUpdate(dropSql);
        UnitTestUtil.executeUpdate(dropDup1Sql);
    }
    /**
     *
     * <p>
     * deleteTempTableのテストケース
     * 正常系：テンポラリテーブルが存在しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTempTableTest02() throws Exception {
        String tempTable = "EXPORT_TEMP_TEST_01";
        String dropSql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01";
        String dropDup1Sql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01_DF";
        UnitTestUtil.executeUpdate(dropSql);

        // 処理の実行
        TempTableDelete delete = new TempTableDelete();
        Connection conn =DBConnection.getConnection();
        try {
            delete.deleteTempTable(tempTable, "EXPORT_TEMP_TEST_01_DF", true, conn);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }

        // 結果を検証
        assertFalse(UnitTestUtil.isExistTable(tempTable));
        assertFalse(UnitTestUtil.isExistTable("EXPORT_TEMP_TEST_01_DF"));

        // テーブルを削除
        UnitTestUtil.executeUpdate(dropSql);
        UnitTestUtil.executeUpdate(dropDup1Sql);
    }
    /**
     *
     * <p>
     * deleteTempTableのテストケース
     * 正常系：コピーが完了していないテーブルを処理しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void deleteTempTableTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/TempTableDeleteTest/deleteTempTableTest03");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テンポラリテーブルを作成
        String tempTable = "EXPORT_TEMP_TEST_01";
        String dropSql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01";
        String createSql = "CREATE TABLE EXPORT_TEMP_TEST_01 (TEXTDATA1 VARCHAR(45) NOT NULL ,INTDATA1 INT NULL,PRIMARY KEY (TEXTDATA1)) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql);
        UnitTestUtil.executeUpdate(createSql);

        // 処理の実行
        TempTableDelete delete = new TempTableDelete();
        Connection conn =DBConnection.getConnection();
        try {
            delete.deleteTempTable(tempTable, "", false, conn);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            DBConnection.closeConn(conn);
        }

        // 結果を検証
        assertTrue(UnitTestUtil.isExistTable(tempTable));

        // テーブルを削除
        UnitTestUtil.executeUpdate(dropSql);
    }
}
