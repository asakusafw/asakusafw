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
package com.asakusafw.bulkloader.common;


import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
/**
 * DBAccessUtilのテストクラス
 * @author yuta.shirai
 *
 */
public class DBAccessUtilTest {
    /** ターゲット名 */
    private static String targetName = "target1";
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
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, targetName);
        UnitTestUtil.setUpDB();
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }

    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, targetName);
        UnitTestUtil.startUp();
    }

    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
    }
    /**
     *
     * <p>
     * createRecordLockTableNameのテストケース
     * 正常系：ジョブフローSIDの取得に成功
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createRecordLockTableNameTest01() throws Exception {
        // テスト対象クラス実行
        String name = DBAccessUtil.createRecordLockTableName("table");

        // 実行結果の検証
        assertEquals("table_RL", name);
    }

    /**
     *
     * <p>
     * selectJobFlowSidのテストケース
     * 正常系：ジョブフローSIDの取得に成功
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void selectJobFlowSidTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/selectJobFlowSidTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        String jobFlowSid = DBAccessUtil.selectJobFlowSid("JOB_FLOW01-002");

        // 実行結果の検証
        assertEquals("2", jobFlowSid);
    }
    /**
     *
     * <p>
     * selectJobFlowSidのテストケース
     * 正常系：ジョブフローSIDが存在しない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void selectJobFlowSidTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/selectJobFlowSidTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        String jobFlowSid = DBAccessUtil.selectJobFlowSid("JOB_FLOW01-999");

        // 実行結果の検証
        assertNull(jobFlowSid);
    }

    /**
     *
     * <p>
     * selectJobFlowSidのテストケース
     * 正常系：ジョブフローが終了していない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void selectJobFlowSidTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/selectJobFlowSidTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        String jobFlowSid = DBAccessUtil.selectJobFlowSid("JOB_FLOW01-003");

        // 実行結果の検証
        assertEquals("3", jobFlowSid);
    }
    /**
     *
     * <p>
     * getExportTempTableのテストケース
     * 正常系：エクスポートテンポラリ管理テーブルの情報を取得
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getExportTempTable01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/getExportTempTable01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        List<ExportTempTableBean> bean = DBAccessUtil.getExportTempTable("99");

        // 実行結果の検証
        assertEquals(2, bean.size());
        assertEquals("99", bean.get(0).getJobflowSid());
        assertEquals("Table1", bean.get(0).getExportTableName());
        assertEquals("Temp_1", bean.get(0).getTemporaryTableName());
        assertEquals("Temp_1_DF", bean.get(0).getDuplicateFlagTableName());
        assertEquals(null, bean.get(0).getTempTableStatus());
        assertEquals("99", bean.get(1).getJobflowSid());
        assertEquals("Table2", bean.get(1).getExportTableName());
        assertEquals("Temp_4", bean.get(1).getTemporaryTableName());
        assertEquals("Temp_4_DF", bean.get(1).getDuplicateFlagTableName());
        assertEquals(ExportTempTableStatus.find("1"), bean.get(1).getTempTableStatus());
    }
    /**
     *
     * <p>
     * getExportTempTableのテストケース
     * 正常系：エクスポートテンポラリ管理テーブルの情報を取得した結果が0件
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getExportTempTable02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/getExportTempTable01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        List<ExportTempTableBean> bean = DBAccessUtil.getExportTempTable("999");

        // 実行結果の検証
        assertEquals(0, bean.size());
    }
    /**
     *
     * <p>
     * delSystemColumnのテストケース
     * 正常系：Export対象テーブルのシステムカラムが全て除かれるケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void delSystemColumn01() throws Exception {
        // テスト対象クラス実行
        List<String> list = new ArrayList<String>();
        list.add("SID");
        list.add("VERSION_NO");
        list.add("RGST_DATE");
        list.add("UPDT_DATE");
        list.add("TEMP_SID");
        list.add("DUPLICATE_FLG");
        list.add("aaa");
        List<String> result = DBAccessUtil.delSystemColumn(list);

        // 実行結果の検証
        assertEquals(3, result.size());
        assertEquals("TEMP_SID", result.get(0));
        assertEquals("DUPLICATE_FLG", result.get(1));
        assertEquals("aaa", result.get(2));
    }
    /**
     *
     * <p>
     * delSystemColumnのテストケース
     * 正常系：Export対象テーブルのシステムカラムの一部が除かれるケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void delSystemColumn02() throws Exception {
        // テスト対象クラス実行
        List<String> list = new ArrayList<String>();
        list.add("RGST_DATE");
        list.add("UPDT_DATE");
        list.add("TEMP_SID");
        list.add("DUPLICATE_FLG");
        list.add("aaa");
        List<String> result = DBAccessUtil.delSystemColumn(list);

        // 実行結果の検証
        assertEquals(3, result.size());
        assertEquals("TEMP_SID", result.get(0));
        assertEquals("DUPLICATE_FLG", result.get(1));
        assertEquals("aaa", result.get(2));
    }
    /**
     *
     * <p>
     * delErrorSystemColumnのテストケース
     * 正常系：異常データテーブルのシステムカラム名とエラーコードを格納するカラム名が除かれるケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void delErrorSystemColumn01() throws Exception {
        // テスト対象クラス実行
        List<String> list = new ArrayList<String>();
        list.add("SID");
        list.add("VERSION_NO");
        list.add("RGST_DATE");
        list.add("UPDT_DATE");
        list.add("TEMP_SID");
        list.add("DUPLICATE_FLG");
        list.add("SID");
        list.add("SID");
        list.add("aaa");
        list.add("ERR_CODE");

        // テスト対象クラス実行
        List<String> result = DBAccessUtil.delErrorSystemColumn(list, "ERR_CODE");

        // 実行結果の検証
        assertEquals(3, result.size());
        assertEquals("TEMP_SID", result.get(0));
        assertEquals("DUPLICATE_FLG", result.get(1));
        assertEquals("aaa", result.get(2));
    }
    /**
     *
     * <p>
     * joinColumnArrayのテストケース
     * 正常系：カラムの配列が結合されるケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void joinColumnArray01() throws Exception {
        // テスト対象クラス実行
        List<String> list = new ArrayList<String>();
        list.add("SID");
        list.add("VERSION_NO");
        list.add("RGST_DATE");
        list.add("UPDT_DATE");
        list.add("TEMP_SID");
        list.add("DUPLICATE_FLG");
        list.add("aaa");
        String result = DBAccessUtil.joinColumnArray(list);

        // 実行結果の検証
        assertEquals("SID,VERSION_NO,RGST_DATE,UPDT_DATE,TEMP_SID,DUPLICATE_FLG,aaa", result);
    }
    /**
     *
     * <p>
     * selectRunningJobFlowのテストケース
     * 正常系：引数にジョブフロー実行IDが指定されているケース(検索結果が存在する)
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void selectRunningJobFlowTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/selectJobFlowSidTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        List<ExporterBean> bean = DBAccessUtil.selectRunningJobFlow("JOB_FLOW01-002");

        // 実行結果の検証
        assertEquals(1, bean.size());
        assertEquals("BATCH02", bean.get(0).getBatchId());
        assertEquals("JOB01-02", bean.get(0).getJobflowId());
        assertEquals("2", bean.get(0).getJobflowSid());
        assertEquals("target1", bean.get(0).getTargetName());
        assertEquals("JOB_FLOW01-002", bean.get(0).getExecutionId());
    }
    /**
     *
     * <p>
     * selectRunningJobFlowのテストケース
     * 正常系：引数にジョブフロー実行IDが指定されているケース(検索結果が存在しない)
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void selectRunningJobFlowTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/selectJobFlowSidTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        List<ExporterBean> bean = DBAccessUtil.selectRunningJobFlow("JOB_FLOW01-999");

        // 実行結果の検証
        assertEquals(0, bean.size());
    }
    /**
     *
     * <p>
     * selectRunningJobFlowのテストケース
     * 正常系：引数にジョブフロー実行IDが指定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void selectRunningJobFlowTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/common/DBAccessUtilTest/selectJobFlowSidTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テスト対象クラス実行
        List<ExporterBean> bean = DBAccessUtil.selectRunningJobFlow(null);

        // 実行結果の検証
        assertEquals(5, bean.size());
        assertEquals("BATCH01", bean.get(0).getBatchId());
        assertEquals("JOB01-01", bean.get(0).getJobflowId());
        assertEquals("1", bean.get(0).getJobflowSid());
        assertEquals("target1", bean.get(0).getTargetName());
        assertEquals("JOB_FLOW01-001", bean.get(0).getExecutionId());

        assertEquals("BATCH02", bean.get(1).getBatchId());
        assertEquals("JOB01-02", bean.get(1).getJobflowId());
        assertEquals("2", bean.get(1).getJobflowSid());
        assertEquals("target1", bean.get(1).getTargetName());
        assertEquals("JOB_FLOW01-002", bean.get(1).getExecutionId());

        assertEquals("BATCH03", bean.get(2).getBatchId());
        assertEquals("JOB01-03", bean.get(2).getJobflowId());
        assertEquals("3", bean.get(2).getJobflowSid());
        assertEquals("target1", bean.get(2).getTargetName());
        assertEquals("JOB_FLOW01-003", bean.get(2).getExecutionId());

        assertEquals("BATCH04", bean.get(3).getBatchId());
        assertEquals("JOB01-04", bean.get(3).getJobflowId());
        assertEquals("4", bean.get(3).getJobflowSid());
        assertEquals("target1", bean.get(3).getTargetName());
        assertEquals("JOB_FLOW01-004", bean.get(3).getExecutionId());

        assertEquals("BATCH05", bean.get(4).getBatchId());
        assertEquals("JOB01-05", bean.get(4).getJobflowId());
        assertEquals("5", bean.get(4).getJobflowSid());
        assertEquals("target1", bean.get(4).getTargetName());
        assertEquals("JOB_FLOW01-005", bean.get(4).getExecutionId());

    }
    /**
     *
     * <p>
     * getJobflowInstanceLockのテストケース
     * 正常系：ロック取得・解除に成功するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void getJobflowInstanceLockTest01() throws Exception {
        // テスト対象クラス実行
        Connection conn = DBConnection.getConnection();
        boolean result = DBAccessUtil.getJobflowInstanceLock("JOB_FLOW01-002", conn);

        assertTrue(result);

        DBAccessUtil.releaseJobflowInstanceLock(conn);
    }
    /**
     *
     * <p>
     * getJobflowInstanceLockのテストケース
     * 異常系：ロック取得に失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    // TODO テスト実施に1分弱かかるためCIでは除外。DBAccessUtil改修には実施する。
    @Test
    @Ignore
    public void getJobflowInstanceLockTest02() throws Exception {
        // ロック取得
        Connection conn1 = DBConnection.getConnection();
        boolean result = DBAccessUtil.getJobflowInstanceLock("JOB_FLOW01-002", conn1);

        assertTrue(result);

        Connection conn2 = DBConnection.getConnection();
        result = DBAccessUtil.getJobflowInstanceLock("JOB_FLOW01-002", conn2);

        assertFalse(result);

        DBAccessUtil.releaseJobflowInstanceLock(conn2);
        DBAccessUtil.releaseJobflowInstanceLock(conn1);
    }
}
