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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.inspect.Cause;
/**
 * ExportDataCopyのテストクラス
 * @author yuta.shirai
 *
 */
public class ExportDataCopyTest {
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
     * copyDataのテストケース
     * 正常系：複数のテーブルのコピーを行うケース
     * ・IMPORT_TARGET1:テーブルロック
     * ・IMPORT_TARGET2_レコードロック
     * ・新規レコード：有り（最大コピー件数を超えない）
     * ・更新レコード：有り（最大コピー件数を超えない）
     * ・重複レコード：有り（最大コピー件数を超えない）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest01() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest01"));
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(true);
        table2.setExportTempTableName("TEMP_IMPORT_TARGET2");
        table2.setDuplicateFlagTableName("TEMP_IMPORT_TARGET2_DF");
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorTableName("IMPORT_TARGET2_ERROR");
        table2.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorCodeColumn("ERROR_CODE");
        table2.setErrorCode("ERR02");
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertTrue(copy.isUpdateEnd());

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        assertTrue(UnitTestUtil.countAssert("IMPORT_TARGET1_RL", 2));
        assertTrue(UnitTestUtil.countAssert("IMPORT_TARGET2_RL", 4));

    }
    /**
     *
     * <p>
     * copyDataのテストケース
     * 正常系：最大件数を超える新規レコードのコピーを行うケース
     * ・テーブルロック：無し
     * ・レコードロック：無し
     * ・新規レコード：有り（最大コピー件数を超える）
     * ・更新レコード：無し
     * ・重複レコード：無し
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest02() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest02"));
        // テストデータをセット
        util.storeToDatabase(false);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD, "1");
        ConfigurationLoader.setProperty(prop);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertTrue(copy.isUpdateEnd());

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // IMPORT_TARGET1_RLの内容を検証
        assertTrue(UnitTestUtil.countAssert("IMPORT_TARGET1_RL", 0));
    }
    /**
     *
     * <p>
     * copyDataのテストケース
     * 正常系：最大件数を超える更新レコードのコピーを行うケース
     * ・テーブルロック：無し
     * ・レコードロック：有り
     * ・新規レコード：無し
     * ・更新レコード：有り（最大コピー件数を超える）
     * ・重複レコード：無し
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest03() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest03"));
        // テストデータをセット
        util.storeToDatabase(false);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD, "1");
        ConfigurationLoader.setProperty(prop);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertTrue(copy.isUpdateEnd());

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // IMPORT_TARGET1_RLの内容を検証
        assertTrue(UnitTestUtil.countAssert("IMPORT_TARGET1_RL", 0));
    }
    /**
     *
     * <p>
     * copyDataのテストケース
     * 正常系：最大件数を超える重複レコードのコピーを行うケース
     * ・テーブルロック：無し
     * ・レコードロック：有り
     * ・新規レコード：無し
     * ・更新レコード：無し
     * ・重複レコード：有り（最大コピー件数を超える）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest04() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest04"));
        // テストデータをセット
        util.storeToDatabase(false);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD, "1");
        ConfigurationLoader.setProperty(prop);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertTrue(copy.isUpdateEnd());

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // IMPORT_TARGET1_RLの内容を検証
        assertTrue(UnitTestUtil.countAssert("IMPORT_TARGET1_RL", 0));
    }
    /**
     *
     * <p>
     * copyDataのテストケース
     * 異常系：処理中に例外が発生するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest05() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest05"));
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(true);
        table2.setExportTempTableName("TEMP_IMPORT_TARGET2");
        table2.setDuplicateFlagTableName("TEMP_IMPORT_TARGET2_DF");
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorTableName("IMPORT_TARGET2_ERROR");
        table2.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorCodeColumn("ERROR_CODE");
        table2.setErrorCode("ERR02");
        targetTable.put("IMPORT_TARGET3", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

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
        assertTrue(UnitTestUtil.countAssert("IMPORT_TARGET1_RL", 1));
    }
    /**
     *
     * <p>
     * copyDataのテストケース
     * 正常系：既にコピーが完了したテーブルのコピーを行うケース
     * ・新規レコード：有り（コピー完了状態）
     * ・更新レコード：有り（コピー完了状態）
     * ・重複レコード：有り（コピー完了状態）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest06() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest06"));
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1_1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(true);
        table2.setExportTempTableName("TEMP_IMPORT_TARGET2_1");
        table2.setDuplicateFlagTableName("TEMP_IMPORT_TARGET2_1_DF");
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorTableName("IMPORT_TARGET2_ERROR");
        table2.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorCodeColumn("ERROR_CODE");
        table2.setErrorCode("ERR02");
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertTrue(copy.isUpdateEnd());

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
     * copyDataのテストケース
     * 異常系：更新レコードに対するExport対象テーブルのレコードが存在しないケース
     * ・テーブルロック：有り
     * ・レコードロック：無し
     * ・新規レコード：有り（最大コピー件数を超えない）
     * ・更新レコード：有り（最大コピー件数を超えない）
     * ・重複レコード：有り（最大コピー件数を超えない）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest07() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest07"));
        // テストデータをセット
        util.storeToDatabase(false);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD, "1");
        ConfigurationLoader.setProperty(prop);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(true);
        table2.setExportTempTableName("TEMP_IMPORT_TARGET2");
        table2.setDuplicateFlagTableName("TEMP_IMPORT_TARGET2_DF");
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorTableName("IMPORT_TARGET2_ERROR");
        table2.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorCodeColumn("ERROR_CODE");
        table2.setErrorCode("ERR02");
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertFalse(copy.isUpdateEnd());

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
     * copyDataのテストケース
     * 正常系：エクスポートテンポラリテーブルが存在しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest08() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest08"));
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(false);
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(false);
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertTrue(copy.isUpdateEnd());

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
     * copyDataのテストケース
     * 正常系：Importerでロックが取得されているケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void copyDataTest09() throws Exception {
        // テストデータを指定
        TestUtils util = new TestUtils(new File("src/test/data/exporter/ExportDataCopyTest/copyDataTest09"));
        // テストデータをセット
        util.storeToDatabase(false);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_COPY_MAX_RECORD, "1");
        ConfigurationLoader.setProperty(prop);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setDuplicateCheck(true);
        table1.setExportTempTableName("TEMP_IMPORT_TARGET1");
        table1.setDuplicateFlagTableName("TEMP_IMPORT_TARGET1_DF");
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        table1.setErrorTableName("IMPORT_TARGET1_ERROR");
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setErrorCodeColumn("ERROR_CODE");
        table1.setErrorCode("ERR01");
        targetTable.put("IMPORT_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setDuplicateCheck(true);
        table2.setExportTempTableName("TEMP_IMPORT_TARGET2");
        table2.setDuplicateFlagTableName("TEMP_IMPORT_TARGET2_DF");
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorTableName("IMPORT_TARGET2_ERROR");
        table2.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2", "DATEDATA2"}));
        table2.setErrorCodeColumn("ERROR_CODE");
        table2.setErrorCode("ERR02");
        targetTable.put("IMPORT_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setJobflowSid("11");
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);

        // テスト対象クラスを生成
        ExportDataCopy copy = new ExportDataCopy();

        // テスト対象クラス実行
        boolean result = copy.copyData(bean);

        // 実行結果の検証
        assertTrue(result);
        assertTrue(copy.isUpdateEnd());

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }
}
