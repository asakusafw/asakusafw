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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.StreamFileListProvider;
import com.asakusafw.testtools.TestUtils;

/**
 * ExportFileReceiveのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExportFileReceiveTest {

    /**
     * Temporary folder for each test case.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static List<String> properties = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    private static String testBatchId = "batch01";
    private static String testJobflowId1 = "JOB_FLOW01";
    private static String testJobflowId2 = "JOB_FLOW02";
    private static String testExecutionId = "JOB_FLOW01-001";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initDBServer(testJobflowId1, testExecutionId, properties, "target1");
        UnitTestUtil.setUpDB();
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }
    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(testJobflowId1, testExecutionId, properties, "target1");
        UnitTestUtil.startUp();
    }
    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
    }
    /**
     *
     * <p>
     * 正常系：複数ファイルを含むZIPファイルを受信するケース(ファイルパスに絶対パスを指定)
     * ・IMPORT_TARGET1
     * 　- EXP_EXP_TARGET1_1.tsv
     * 　- EXP_EXP_TARGET1_2.tsv
     * ・IMPORT_TARGET2
     * 　- EXP_EXP_TARGET2_1.tsv
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void receiveFileTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileReceiveTest");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setExportTargetTable(targetTable);
        bean.setJobflowId(testJobflowId1);
        bean.setExecutionId(testExecutionId);
        bean.setBatchId(testBatchId);
        bean.setTargetName("target1");

        File testFile = folder.newFile("testing");
        ExportFileReceive receive = new Mock(testFile, "src/test/data/exporter/SEND_OUT1.zip");
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
        assertTrue(result);

        // ファイルの中身を検証
        List<File> target1 = bean.getExportTargetTable("EXP_TARGET1").getExportFiles();
        List<File> target2 = bean.getExportTargetTable("EXP_TARGET2").getExportFiles();
        UnitTestUtil.assertSameFileList(testFile, target1.get(0), target1.get(1), target2.get(0));
    }
    /**
     *
     * <p>
     * 正常系：単一ファイルを含むZIPファイルを受信するケース(ファイルパスに相対パスを指定)
     * ・IMPORT_TARGET1
     * 　- EXP_EXP_TARGET1_1.tsv
     * 　- EXP_EXP_TARGET1_2.tsv
     * ・IMPORT_TARGET2
     * 　- EXP_EXP_TARGET2_1.tsv
     * </p>
     *
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Test
    public void receiveFileTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileReceiveTest");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("12");
        bean.setExportTargetTable(targetTable);
        bean.setJobflowId(testJobflowId2);
        bean.setExecutionId(testExecutionId);
        bean.setBatchId(testBatchId);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_FILE_DIR, "target/asakusa-thundergate");
        ConfigurationLoader.setProperty(prop);

        File testFile = folder.newFile("testing");
        ExportFileReceive receive = new Mock(testFile, "src/test/data/exporter/SEND_OUT2.zip");
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
       assertTrue(result);

        // ファイルの中身を検証
        List<File> target1 = bean.getExportTargetTable("EXP_TARGET1").getExportFiles();
        UnitTestUtil.assertSameFileList(testFile, target1.get(0));
    }

    /**
     *
     * <p>
     * 異常系：exportファイルを生成するディレクトリが存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Ignore
    @Test
    public void receiveFileTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileReceiveTest");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("13");
        bean.setExportTargetTable(targetTable);
        bean.setJobflowId(testJobflowId2);
        bean.setExecutionId(testExecutionId);
        bean.setBatchId(testBatchId);

        // プロパティを書き換え
        File missing = folder.newFolder("__MISSING__");
        Assume.assumeTrue(missing.delete());
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_FILE_DIR, missing.getAbsolutePath());
        ConfigurationLoader.setProperty(prop);

        File testFile = folder.newFile("testing");
        ExportFileReceive receive = new Mock(testFile, "src/test/data/exporter/SEND_OUT1.zip");
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
        assertFalse(result);
    }
    /**
     *
     * <p>
     * 異常系：ファイル名抽出したテーブル名に対応するDSL定義が存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void receiveFileTest04() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileReceiveTest");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternR01";

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET11", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET22", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("14");
        bean.setExportTargetTable(targetTable);
        bean.setJobflowId(testJobflowId1);
        bean.setExecutionId(testExecutionId);

        File testFile = folder.newFile("testing");
        ExportFileReceive receive = new Mock(testFile, "src/test/data/exporter/SEND_OUT1.zip");
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
        assertFalse(result);
    }
    /**
     *
     * <p>
     * 異常系：処理中にIOSystemExceptionが発生するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void receiveFileTest05() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileReceiveTest");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternR01";

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("15");
        bean.setExportTargetTable(targetTable);
        bean.setJobflowId(testJobflowId1);
        bean.setExecutionId(testExecutionId);

        File testFile = folder.newFile("testing");
        ExportFileReceive receive = new Mock(testFile, "src/test/data/exporter/SEND_OUT1.zip") {
            @Override
            protected FileListProvider openFileList(
                    String targetName,
                    String batchId,
                    String jobflowId,
                    String executionId) throws IOException {
                throw new IOException();
            }
        };
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
        assertFalse(result);
    }
    /**
     *
     * <p>
     * 異常系：サブプロセスの終了コードが0以外のケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void receiveFileTest06() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/ExportFileReceiveTest");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("16");
        bean.setExportTargetTable(targetTable);
        bean.setJobflowId(testJobflowId1);
        bean.setExecutionId(testExecutionId);
        File testFile = folder.newFile("testing");

        ExportFileReceive receive = new Mock(testFile, "src/test/data/exporter/SEND_OUT1.zip", false);
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
        assertFalse(result);
    }

    static class Mock extends ExportFileReceive {

        final File target;

        final String testFile;

        final boolean success;

        Mock(File target, String testFile) {
            this(target, testFile, true);
        }

        Mock(File target, String testFile, boolean success) {
            this.target = target;
            this.testFile = testFile;
            this.success = success;
        }

        @Override
        protected FileListProvider openFileList(
                String targetName,
                String batchId,
                String jobflowId,
                String executionId) throws IOException {
            return new StreamFileListProvider() {

                @Override
                protected InputStream getInputStream() throws IOException {
                    UnitTestUtil.createFileList(new File(testFile), target);
                    return new FileInputStream(target);
                }

                @Override
                protected OutputStream getOutputStream() throws IOException {
                    return new ByteArrayOutputStream();
                }

                @Override
                protected void waitForDone() throws IOException, InterruptedException {
                    if (success == false) {
                        throw new IOException();
                    }
                }

                @Override
                public void close() throws IOException {
                    return;
                }
            };
        }
    }
}

