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
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.inspect.Cause;

/**
 * ExportFileLoadのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExportFileLoadTest {
    /** 読み込むプロパティファイル */
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
     * loadFileのテストケース
     * 正常系：複数のファイルをロードするケース(重複チェックあり)
     * ファイルパスは相対パスを指定する。
     * ・テーブル：IMPORT_TARGET1
     * 　-ファイル：/home/mysql/EXP_EXP_TARGET1_1.tsv
     * 　-ファイル：/home/mysql/EXP_EXP_TARGET1_2.tsv
     * ・テーブル：IMPORT_TARGET2
     * 　-ファイル：/home/mysql/EXP_EXP_TARGET2_1.tsv
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadFileTest01() throws Exception {
        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_1.tsv").getAbsolutePath()));
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_2.tsv").getAbsolutePath()));
        table1.setExportTsvColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setExportTableColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(true);
        table2.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET2_1.tsv").getAbsolutePath()));
        table2.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA2"}));
        table2.setErrorTableName("IMPORT_TARGET2_ERROR");
        table2.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportFileLoad load = new ExportFileLoad() {
            @Override
            protected long getTempSeq(String jobflowSid, String tableName,
                    Connection conn) throws BulkLoaderSystemException {
                return 1;
            }
        };
        try {
            // テンポラリテーブルを削除
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1_DF");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1_DF");

            // テストデータを指定
            TestUtils util1 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest01_load"));
            // テストデータをセット
            util1.storeToDatabase(false);

            // テスト対象クラス実行
            boolean result = load.loadFile(bean);

            // 実行結果の検証
            assertTrue(result);

            // DBの結果を検証
            TestUtils util2 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest01_assert"));
            util2.loadFromDatabase();
            if (!util2.inspect()) {
                for (Cause cause : util2.getCauses()) {
                    System.out.println(cause.getMessage());
                }
                fail(util2.getCauseMessage());
            }
            // 重複チェック件数を確認
            assertTrue(UnitTestUtil.countAssert("EXPORT_TEMP_IMPORT_TARGET1_1_DF", 2));
            assertTrue(UnitTestUtil.countAssert("EXPORT_TEMP_IMPORT_TARGET2_1_DF", 3));
        } finally {
            // テンポラリテーブルを削除
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1_DF");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1_DF");
        }
    }
    /**
     *
     * <p>
     * loadFileのテストケース
     * 正常系：複数のファイルをロードするケース(重複チェックなし)
     * ファイルパスは相対パスを指定する。
     * ・テーブル：IMPORT_TARGET1
     * 　-ファイル：/home/mysql/EXP_EXP_TARGET1_1.tsv
     * 　-ファイル：/home/mysql/EXP_EXP_TARGET1_2.tsv
     * ・テーブル：IMPORT_TARGET2
     * 　-ファイル：/home/mysql/EXP_EXP_TARGET2_1.tsv
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadFileTest02() throws Exception {
        // テストデータを指定
        TestUtils util1 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest02_load"));
        // テストデータをセット
        util1.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(false);
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_1.tsv").getAbsolutePath()));
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_2.tsv").getAbsolutePath()));
        table1.setExportTsvColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(false);
        table2.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET2_1.tsv").getAbsolutePath()));
        table2.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportFileLoad load = new ExportFileLoad() {
            @Override
            protected long getTempSeq(String jobflowSid, String tableName,
                    Connection conn) throws BulkLoaderSystemException {
                return 1;
            }
        };
        try {
            // テンポラリテーブルを削除
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1_DF");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1_DF");


            // テスト対象クラス実行
            boolean result = load.loadFile(bean);

            // 実行結果の検証
            assertTrue(result);

            // DBの結果を検証
            TestUtils util2 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest02_assert"));
            util2.loadFromDatabase();
            if (!util2.inspect()) {
                for (Cause cause : util2.getCauses()) {
                    System.out.println(cause.getMessage());
                }
                fail(util2.getCauseMessage());
            }
            // 重複チェック件数を確認
            assertTrue(UnitTestUtil.countAssert("EXPORT_TEMP_IMPORT_TARGET1_1_DF", 0));
            assertTrue(UnitTestUtil.countAssert("EXPORT_TEMP_IMPORT_TARGET2_1_DF", 0));
        } finally {
            // テンポラリテーブルを削除
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1_DF");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1_DF");

        }
    }

    /**
     *
     * <p>
     * loadFileのテストケース
     * 異常系：処理中にSQLExceptionが発生するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadFileTest03() throws Exception {
        // テストデータを指定
        TestUtils util1 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest01_load"));
        // テストデータをセット
        util1.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(false);
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_1.tsv").getAbsolutePath()));
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_2.tsv").getAbsolutePath()));
        table1.setExportTsvColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(false);
        table2.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET2_1.tsv").getAbsolutePath()));
        table2.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA2", "DATEDATA2"}));
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportFileLoad load = new ExportFileLoad() {
            @Override
            protected long getTempSeq(String jobflowSid, String tableName,
                    Connection conn) throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(this.getClass(), "TG-EXTRACTOR-02001");
            }
        };

        // テスト対象クラス実行
        boolean result = load.loadFile(bean);

        // 実行結果の検証
        assertFalse(result);
    }
    /**
     *
     * <p>
     * loadFileのテストケース
     * 正常系：ロードするファイルが0byteのケース
     * ・テーブル：IMPORT_TARGET1
     * 　-ファイル：/home/mysql/EXP_EXP_TARGET1_4.tsv
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadFileTest04() throws Exception {
        // テストデータを指定
        TestUtils util1 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest04_load"));
        // テストデータをセット
        util1.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_4.tsv").getAbsolutePath()));
        table1.setExportTsvColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        targetTable.put("IMPORT_TARGET1", table1);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportFileLoad load = new ExportFileLoad() {
            @Override
            protected long getTempSeq(String jobflowSid, String tableName,
                    Connection conn) throws BulkLoaderSystemException {
                return 1;
            }
        };
        try {
            // テンポラリテーブルを削除
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1_DF");


            // テスト対象クラス実行
            boolean result = load.loadFile(bean);

            // 実行結果の検証
            assertTrue(result);

            // DBの結果を検証
            TestUtils util2 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest04_assert"));
            util2.loadFromDatabase();
            if (!util2.inspect()) {
                for (Cause cause : util2.getCauses()) {
                    System.out.println(cause.getMessage());
                }
                fail(util2.getCauseMessage());
            }
            // 重複チェック件数を確認
            assertTrue(UnitTestUtil.countAssert("EXPORT_TEMP_IMPORT_TARGET1_1_DF", 0));
        } finally {
            // テンポラリテーブルを削除
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1_DF");

        }
    }
    /**
     *
     * <p>
     * loadFileのテストケース
     * 異常系：Export中間TSVファイルのカラムがExport対象テーブル/異常データテーブルのカラムに含まれないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadFileTest05() throws Exception {
        // テストデータを指定
        TestUtils util1 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest02_load"));
        // テストデータをセット
        util1.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_1.tsv").getAbsolutePath()));
        table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_2.tsv").getAbsolutePath()));
        table1.setExportTsvColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setExportTableColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1"}));
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        targetTable.put("IMPORT_TARGET1", table1);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportFileLoad load = new ExportFileLoad() {
            @Override
            protected long getTempSeq(String jobflowSid, String tableName,
                    Connection conn) throws BulkLoaderSystemException {
                return 1;
            }
        };
        try {
            // テスト対象クラス実行
            boolean result = load.loadFile(bean);

            // 実行結果の検証
            assertFalse(result);

        } finally {
            // テンポラリテーブルを削除
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_1_DF");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET2_1_DF");

        }
    }
    /**
    *
    * <p>
    * loadFileのテストケース
    * 正常系：複数のファイルをロードするケース(重複チェックあり)
    * ファイルパスは相対パスを指定する。
    * ・テーブル：IMPORT_TARGET1
    * 　-ファイル：/home/mysql/EXP_EXP_TARGET1_1.tsv
    * 　-ファイル：/home/mysql/EXP_EXP_TARGET1_2.tsv
    * ・テーブル：IMPORT_TARGET2
    * 　-ファイル：/home/mysql/EXP_EXP_TARGET2_1.tsv
    * </p>
    *
    * @throws Exception
    */
   @Test
   public void loadFileTest_DupCheckTableUnion() throws Exception {
       // テストデータを指定
       TestUtils util1 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest_DupCheckTableUnion_load"));
       // テストデータをセット
       util1.storeToDatabase(false);

       // ExportBeanを生成
       Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
       ExportTargetTableBean table1 = new ExportTargetTableBean();
       table1.setDuplicateCheck(true);
       table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_1.tsv").getAbsolutePath()));
       table1.addExportFile(new File(new File ("src/test/data/exporter/EXP_EXP_TARGET1_2.tsv").getAbsolutePath()));
       table1.setExportTsvColumns(Arrays.asList(new String[]{"SID", "VERSION_NO", "TEXTDATA1", "INTDATA2", "DATEDATA2"}));
       table1.setExportTableColumns(Arrays.asList(new String[] {"SID", "VERSION_NO", "TEXTDATA1"}));
       table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
       table1.setErrorTableName("IMPORT_TARGET2_ERROR");
       table1.setErrorTableColumns(Arrays.asList(new String[] {"SID", "VERSION_NO", "INTDATA2", "DATEDATA2"}));
       targetTable.put("IMPORT_TARGET1", table1);
       ExporterBean bean = new ExporterBean();
       bean.setExportTargetTable(targetTable);
       bean.setJobflowSid("11");
       bean.setJobflowId(jobflowId);
       bean.setExecutionId(executionId);

       // テスト対象クラスを生成
       ExportFileLoad load = new ExportFileLoad() {
           @Override
           protected long getTempSeq(String jobflowSid, String tableName,
                   Connection conn) throws BulkLoaderSystemException {
               return 9;
           }
       };
       try {
           // テンポラリテーブルを削除
           UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_9");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_9_DF");


           // テスト対象クラス実行
           boolean result = load.loadFile(bean);

           // 実行結果の検証
           assertTrue(result);

           // DBの結果を検証
           TestUtils util2 = new TestUtils(new File("src/test/data/exporter/ExportFileLoadTest/loadFileTest_DupCheckTableUnion_assert"));
           util2.loadFromDatabase();
           if (!util2.inspect()) {
               for (Cause cause : util2.getCauses()) {
                   System.out.println(cause.getMessage());
               }
               fail(util2.getCauseMessage());
           }
            // 重複チェック件数を確認
            assertTrue(UnitTestUtil.countAssert("EXPORT_TEMP_IMPORT_TARGET1_9_DF", 2));
       } finally {
           // テンポラリテーブルを削除
           UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_9");
            UnitTestUtil.executeUpdate("DROP TABLE IF EXISTS EXPORT_TEMP_IMPORT_TARGET1_9_DF");

       }
   }
    /**
     *
     * <p>
     * createTempTableNameのテストケース
     * 正常系：テーブル名の作成に成功するケース(Export対象テーブル名28桁以下)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createTempTableName01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileLoadTest/createTempTableName01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        Connection conn = null;
        String result = null;
        try {
            conn = DBConnection.getConnection();
            ExportFileLoad load = new ExportFileLoad();
            result = load.createTempTableName("1111111111222222222233333333", "11", conn);
        } finally {
            DBConnection.closeConn(conn);
        }

        // 実行結果の検証
        assertEquals("EXPORT_TEMP_1111111111222222222233333333_6", result);
    }
    /**
     *
     * <p>
     * createTempTableNameのテストケース
     * 正常系：テーブル名の作成に成功するケース(Export対象テーブル名29桁以上)
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createTempTableName02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileLoadTest/createTempTableName01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        Connection conn = null;
        String result = null;
        try {
            conn = DBConnection.getConnection();
            ExportFileLoad load = new ExportFileLoad();
            result = load.createTempTableName("11111111112222222222333333333", "11", conn);
        } finally {
            DBConnection.closeConn(conn);
        }

        // 実行結果の検証
        assertEquals("EXPORT_TEMP_11111111112222222222333333333_7", result);
    }
    /**
     *
     * <p>
     * createTempTableNameのテストケース
     * 異常系：テンポラリ管理テーブルのレコード取得に失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createTempTableName03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileLoadTest/createTempTableName01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            ExportFileLoad load = new ExportFileLoad();
            load.createTempTableName("IMPORT_TARGET4", "11", conn);
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BulkLoaderSystemException);
        } finally {
            DBConnection.closeConn(conn);
        }
    }
    /**
     *
     * <p>
     * createTableSqlのテストケース
     * 正常系：SQLを作成するケース(重複チェックあり、システムカラムを含む)
     * </p>
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void createTableSql01() throws Exception {
        // 入力値を生成
        ExportTargetTableBean tableBean = new ExportTargetTableBean();
        tableBean.setDuplicateCheck(true);
        tableBean.setErrorTableName("IMPORT_TARGET1_ERROR");
        tableBean.setExportTsvColumns(Arrays.asList(new String[]{"aaa", "SID", "VERSION_NO", "RGST_DATE", "UPDT_DATE", "TEMP_SID", "DUPLICATE_FLG", "bbb", "ccc"}));

        // テスト対象クラス実行
        ExportFileLoad load = new ExportFileLoad();
        String result = load.createTableSql("IMPORT_TARGET1", "EXPORT_TEMP_IMPORT_TARGET1_3", tableBean);

        // 結果を検証
        assertEquals("CREATE TABLE EXPORT_TEMP_IMPORT_TARGET1_3 (TEMP_SID BIGINT NOT NULL AUTO_INCREMENT,SID BIGINT NULL,VERSION_NO BIGINT NULL,RGST_DATE DATETIME NULL,UPDT_DATE DATETIME NULL,DUPLICATE_FLG CHAR(1) NULL,PRIMARY KEY (TEMP_SID)) SELECT NULL AS TEMP_SID,SID,VERSION_NO,RGST_DATE,UPDT_DATE, NULL AS DUPLICATE_FLG, aaa,bbb,ccc FROM IMPORT_TARGET1_ERROR LIMIT 0", result);
    }
    /**
     *
     * <p>
     * createTableSqlのテストケース
     * 正常系：SQLを作成するケース(重複チェックなし、システムカラムを含まない)
     * </p>
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void createTableSql02() throws Exception {
        // 入力値を生成
        ExportTargetTableBean tableBean = new ExportTargetTableBean();
        tableBean.setDuplicateCheck(false);
        tableBean.setErrorTableName("IMPORT_TARGET1_ERROR");
        tableBean.setExportTsvColumns(Arrays.asList(new String[]{"aaa", "bbb", "ccc"}));

        // テスト対象クラス実行
        ExportFileLoad load = new ExportFileLoad();
        String result = load.createTableSql("IMPORT_TARGET1", "EXPORT_TEMP_IMPORT_TARGET1_3", tableBean);

        // 結果を検証
        assertEquals("CREATE TABLE EXPORT_TEMP_IMPORT_TARGET1_3 (TEMP_SID BIGINT NOT NULL AUTO_INCREMENT,SID BIGINT NULL,VERSION_NO BIGINT NULL,RGST_DATE DATETIME NULL,UPDT_DATE DATETIME NULL,DUPLICATE_FLG CHAR(1) NULL,PRIMARY KEY (TEMP_SID)) SELECT NULL AS TEMP_SID,SID,VERSION_NO,RGST_DATE,UPDT_DATE, NULL AS DUPLICATE_FLG, aaa,bbb,ccc FROM IMPORT_TARGET1 LIMIT 0", result);
    }
}
