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
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.inspect.Cause;


/**
 * LockReleaseのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class LockReleaseTest {
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
     * 正常系：複数のテーブルのロックを解除するケース
     *         (Import対象とExport対象のテーブルが同一)
     * ・IMPORT_TARGET1：テーブルロック
     * ・IMPORT_TARGET2：行ロック
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void releaseLockTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/LockReleaseTest/releaseLockTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);
        // テンポラリテーブルを作成
        String tempTable1 = "EXPORT_TEMP_TEST_01";
        String dropSql1 = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01";
        String createSql1 = "CREATE TABLE EXPORT_TEMP_TEST_01 (TEXTDATA1 VARCHAR(45) NOT NULL ,INTDATA1 INT NULL,PRIMARY KEY (TEXTDATA1)) ENGINE=InnoDB;";
        String tempTable2 = "EXPORT_TEMP_TEST_02";
        String dropSql2 = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_02";
        String createSql2 = "CREATE TABLE EXPORT_TEMP_TEST_02 (TEXTDATA1 VARCHAR(45) NOT NULL ,INTDATA1 INT NULL,PRIMARY KEY (TEXTDATA1)) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);

        String dropDup1Sql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01_DF";
        String dropDup2Sql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_02_DF";
        StringBuilder dup1Sql = new StringBuilder();
        dup1Sql.append("CREATE  TABLE `EXPORT_TEMP_TEST_01_DF` (");
        dup1Sql.append("  `TEMP_SID` BIGINT,");
        dup1Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");
        StringBuilder dup2Sql = new StringBuilder();
        dup2Sql.append("CREATE  TABLE `EXPORT_TEMP_TEST_02_DF` (");
        dup2Sql.append("  `TEMP_SID` BIGINT,");
        dup2Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");
        UnitTestUtil.executeUpdate(dropDup1Sql);
        UnitTestUtil.executeUpdate(dropDup2Sql);
        UnitTestUtil.executeUpdate(dup1Sql.toString());
        UnitTestUtil.executeUpdate(dup2Sql.toString());


        // ExportBeanを生成
        Map<String, ExportTargetTableBean> exportTargetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setExportTempTableName(tempTable1);
        table1.setDuplicateFlagTableName("EXPORT_TEMP_TEST_01_DF");
        exportTargetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setExportTempTableName(tempTable2);
        table2.setDuplicateFlagTableName("EXPORT_TEMP_TEST_02_DF");
        exportTargetTable.put("IMPORT_TARGET2", table2);
        Map<String, ImportTargetTableBean> importTargetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table3 = new ImportTargetTableBean();
        importTargetTable.put("IMPORT_TARGET1", table3);
        ImportTargetTableBean table4 = new ImportTargetTableBean();
        importTargetTable.put("IMPORT_TARGET2", table4);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setExportTargetTable(exportTargetTable);
        bean.setImportTargetTable(importTargetTable);
        bean.setRetryCount(3);
        bean.setRetryInterval(1);

        // テスト対象クラス実行
        LockRelease lock = new LockRelease();
        boolean result = lock.releaseLock(bean, true);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // テンポラリテーブルが削除されている事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
        assertFalse(UnitTestUtil.isExistTable("EXPORT_TEMP_TEST_01_DF"));
        assertFalse(UnitTestUtil.isExistTable("EXPORT_TEMP_TEST_02_DF"));
    }
    /**
     *
     * <p>
     * 異常系：IMPORT_TABLE_LOCKテーブルのロック取得中にSQLExceptionが発生するケース
     * 　　　　リトライを行った後にリトライオーバーする
     * </p>
     *
     * @throws Exception
     */
    // TODO 実行に3分かかるためCIでは除外中。LockRelease改修時には実施する。
    @Ignore
    @Test
    public void releaseLockTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/LockReleaseTest/releaseLockTest02");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> exportTargetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        Map<String, ImportTargetTableBean> importTargetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table1 = new ImportTargetTableBean();
        importTargetTable.put("IMPORT_TARGET1", table1);
        ImportTargetTableBean table2 = new ImportTargetTableBean();
        importTargetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setExportTargetTable(exportTargetTable);
        bean.setImportTargetTable(importTargetTable);
        bean.setRetryCount(1);
        bean.setRetryInterval(1);

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // コネクションを取得する
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("update IMPORT_TABLE_LOCK set TABLE_NAME='IMPORT_TARGET1' where JOBFLOW_SID=11 and TABLE_NAME='IMPORT_TARGET1'");
            stmt.executeUpdate();

            // テスト対象クラス実行
            LockRelease lock = new LockRelease(){
                @Override
                protected TempTableDelete createTempTableDelete() {
                    return new TempTableDelete(){
                        @Override
                        public boolean delete(
                                List<ExportTempTableBean> exportTempTableBean, boolean copyNotEnd) {
                            return true;
                        }
                        @Override
                        public void deleteTempInfoRecord(String jobflowSid,
                                String tableName, boolean copyNotEnd, Connection conn)
                                throws BulkLoaderSystemException {
                        }
                        @Override
                        public void deleteTempTable(String exportTempName, String duplicateFlagTableName, boolean copyNotEnd, Connection conn) throws BulkLoaderSystemException {
                        }
                    };
                }
            };
            boolean result = lock.releaseLock(bean, true);

            // 実行結果の検証
            assertFalse(result);

            // DBの結果を検証
            util.loadFromDatabase();
            if (!util.inspect()) {
                for (Cause cause : util.getCauses()) {
                    System.out.println(cause.getMessage());
                }
                fail(util.getCauseMessage());
            }
        } finally {
            DBConnection.rollback(conn);
            DBConnection.closePs(stmt);
            DBConnection.closeConn(conn);
        }
    }
    /**
     *
     * <p>
     * 正常系：複数のテーブルのロックを解除するケース
     *         (Import対象とExport対象のテーブルが異なる)
     * ・IMPORT_TARGET1：テーブルロック
     * ・IMPORT_TARGET2：行ロック
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void releaseLockTest04() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/LockReleaseTest/releaseLockTest04");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> exportTargetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        exportTargetTable.put("IMPORT_TARGET1", table1);
        Map<String, ImportTargetTableBean> importTargetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table4 = new ImportTargetTableBean();
        importTargetTable.put("IMPORT_TARGET2", table4);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setExportTargetTable(exportTargetTable);
        bean.setImportTargetTable(importTargetTable);
        bean.setRetryCount(3);
        bean.setRetryInterval(5);

        // テスト対象クラス実行
        LockRelease lock = new LockRelease(){
            @Override
            protected TempTableDelete createTempTableDelete() {
                return new TempTableDelete(){
                    @Override
                    public boolean delete(
                            List<ExportTempTableBean> exportTempTableBean, boolean copyNotEnd) {
                        return true;
                    }
                    @Override
                    public void deleteTempInfoRecord(String jobflowSid,
                            String tableName, boolean copyNotEnd, Connection conn)
                            throws BulkLoaderSystemException {
                    }
                    @Override
                    public void deleteTempTable(String exportTempName, String duplicateFlagTableName, boolean copyNotEnd, Connection conn) throws BulkLoaderSystemException {
                    }
                };
            }
        };
        boolean result = lock.releaseLock(bean, true);

        // 実行結果の検証
        assertTrue(result);

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
     * 正常系：Import対象とExport対象が存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void releaseLockTest05() throws Exception {
        // ExportBeanを生成
        Map<String, ExportTargetTableBean> exportTargetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        Map<String, ImportTargetTableBean> importTargetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(exportTargetTable);
        bean.setImportTargetTable(importTargetTable);
        bean.setRetryCount(3);
        bean.setRetryInterval(5);

        // テスト対象クラス実行
        LockRelease lock = new LockRelease();
        boolean result = lock.releaseLock(bean, true);

        // 実行結果の検証
        assertTrue(result);
    }
    /**
     *
     * <p>
     * 正常系：ジョブフローの終了を記録しないケース
     * ・IMPORT_TARGET1：テーブルロック
     * ・IMPORT_TARGET2：行ロック
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void releaseLockTest06() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/LockReleaseTest/releaseLockTest06");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);
        // テンポラリテーブルを作成
        String tempTable1 = "EXPORT_TEMP_TEST_01";
        String dropSql1 = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01";
        String createSql1 = "CREATE TABLE EXPORT_TEMP_TEST_01 (TEXTDATA1 VARCHAR(45) NOT NULL ,INTDATA1 INT NULL,PRIMARY KEY (TEXTDATA1)) ENGINE=InnoDB;";
        String tempTable2 = "EXPORT_TEMP_TEST_02";
        String dropSql2 = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_02";
        String createSql2 = "CREATE TABLE EXPORT_TEMP_TEST_02 (TEXTDATA1 VARCHAR(45) NOT NULL ,INTDATA1 INT NULL,PRIMARY KEY (TEXTDATA1)) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);

        String dropDup1Sql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_01_DF";
        String dropDup2Sql = "DROP TABLE IF EXISTS EXPORT_TEMP_TEST_02_DF";
        StringBuilder dup1Sql = new StringBuilder();
        dup1Sql.append("CREATE  TABLE `EXPORT_TEMP_TEST_01_DF` (");
        dup1Sql.append("  `TEMP_SID` BIGINT,");
        dup1Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");
        StringBuilder dup2Sql = new StringBuilder();
        dup2Sql.append("CREATE  TABLE `EXPORT_TEMP_TEST_02_DF` (");
        dup2Sql.append("  `TEMP_SID` BIGINT,");
        dup2Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");
        UnitTestUtil.executeUpdate(dropDup1Sql);
        UnitTestUtil.executeUpdate(dropDup2Sql);
        UnitTestUtil.executeUpdate(dup1Sql.toString());
        UnitTestUtil.executeUpdate(dup2Sql.toString());

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> exportTargetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setExportTempTableName(tempTable1);
        table1.setDuplicateFlagTableName("EXPORT_TEMP_TEST_01_DF");
        exportTargetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setExportTempTableName(tempTable2);
        table2.setDuplicateFlagTableName("EXPORT_TEMP_TEST_02_DF");
        exportTargetTable.put("IMPORT_TARGET2", table2);
        Map<String, ImportTargetTableBean> importTargetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean table3 = new ImportTargetTableBean();
        importTargetTable.put("IMPORT_TARGET1", table3);
        ImportTargetTableBean table4 = new ImportTargetTableBean();
        importTargetTable.put("IMPORT_TARGET2", table4);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setExportTargetTable(exportTargetTable);
        bean.setImportTargetTable(importTargetTable);
        bean.setRetryCount(3);
        bean.setRetryInterval(1);

        // テスト対象クラス実行
        LockRelease lock = new LockRelease();
        boolean result = lock.releaseLock(bean, false);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // テンポラリテーブルが削除されている事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
        assertFalse(UnitTestUtil.isExistTable("EXPORT_TEMP_TEST_01_DF"));
        assertFalse(UnitTestUtil.isExistTable("EXPORT_TEMP_TEST_02_DF"));
    }
}
