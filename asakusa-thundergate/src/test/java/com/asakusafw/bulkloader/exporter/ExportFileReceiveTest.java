/**
 * Copyright 2011 Asakusa Framework Team.
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;

/**
 * ExportFileReceiveのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExportFileReceiveTest {
    /** Importerで読み込むプロパティファイル */
    private static List<String> propertys = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    /** バッチID */
    private static String batchId = "batch01";
    /** ジョブフローID */
    private static String jobflowId1 = "JOB_FLOW01";
    private static String jobflowId2 = "JOB_FLOW02";
    /** ジョブフロー実行ID */
    private static String executionId = "JOB_FLOW01-001";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initDBServer(jobflowId1, executionId, propertys, "target1");
        UnitTestUtil.setUpDB();
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }
    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(jobflowId1, executionId, propertys, "target1");
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
        bean.setJobflowId(jobflowId1);
        bean.setExecutionId(executionId);
        bean.setBatchId(batchId);
        bean.setTargetName("target1");

        // テスト対象クラス実行
        DummyExportFileReceive receive = new DummyExportFileReceive();
        receive.setExportFile(new File("src/test/data/exporter/SEND_OUT1.zip"));
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
        assertTrue(result);

        // プロセスに設定する値を検証
        assertEquals(receive.sshPath, ConfigurationLoader.getProperty(Constants.PROP_KEY_SSH_PATH));
        assertEquals(receive.nameNodeIp, ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_HOST));
        assertEquals(receive.nameNodeUser, ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_USER));
        assertEquals(receive.shellName, ConfigurationLoader.getProperty(Constants.PROP_KEY_COL_SHELL_NAME));
        assertEquals(receive.targetName, bean.getTargetName());
        assertEquals(receive.jobflowId, jobflowId1);
        assertEquals(receive.batchId, batchId);
        assertEquals(receive.executionId, executionId);

        // ファイルの中身を検証
        List<File> target1 = bean.getExportTargetTable("EXP_TARGET1").getExportFiles();
        List<File> target2 = bean.getExportTargetTable("EXP_TARGET2").getExportFiles();
        File[] expectedFile = new File[3];
        expectedFile[0] = target1.get(0);
        expectedFile[1] = target1.get(1);
        expectedFile[2] = target2.get(0);
        File zipFile = new File("src/test/data/exporter/SEND_OUT1.zip");
        assertTrue(UnitTestUtil.assertZipFile(expectedFile, zipFile));
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
        bean.setJobflowId(jobflowId2);
        bean.setExecutionId(executionId);
        bean.setBatchId(batchId);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_FILE_DIR, "target/asakusa-thundergate");
        ConfigurationLoader.setProperty(prop);

        // テスト対象クラス実行
        DummyExportFileReceive receive = new DummyExportFileReceive();
        receive.setExportFile(new File("src/test/data/exporter/SEND_OUT2.zip"));
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
       assertTrue(result);

        // ファイルの中身を検証
        List<File> target1 = bean.getExportTargetTable("EXP_TARGET1").getExportFiles();
        File[] expectedFile = new File[1];
        expectedFile[0] = target1.get(0);
        File zipFile = new File("src/test/data/exporter/SEND_OUT2.zip");
        assertTrue(UnitTestUtil.assertZipFile(expectedFile, zipFile));
    }

    /**
     *
     * <p>
     * 異常系：exportファイルを生成するディレクトリが存在しないケース
     * </p>
     *
     * @throws Exception
     */
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
        bean.setJobflowId(jobflowId2);
        bean.setExecutionId(executionId);
        bean.setBatchId(batchId);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_FILE_DIR, "target/asakusa");
        ConfigurationLoader.setProperty(prop);

        // テスト対象クラス実行
        DummyExportFileReceive receive = new DummyExportFileReceive();
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
        bean.setJobflowId(jobflowId1);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DummyExportFileReceive receive = new DummyExportFileReceive();
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
        bean.setJobflowId(jobflowId1);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DummyExportFileReceive receive = new DummyExportFileReceive() {

            @Override
            protected Process createProcess(String sshPath, String nameNodeIp,
                    String nameNodeUser, String shellName, String batchId, String jobflowId,
                    String jobflowSid, String executionId, String variableTable)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(this.getClass(), "dummy");
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

//        // テストデータの指定
//        String pattern = "patternR01";

        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        targetTable.put("EXP_TARGET2", table2);
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("16");
        bean.setExportTargetTable(targetTable);
        bean.setJobflowId(jobflowId1);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DummyExportFileReceive receive = new DummyExportFileReceive() {

            @Override
            protected Process createProcess(String sshPath, String nameNodeIp,
                    String nameNodeUser, String shellName, String batchId, String jobflowId,
                    String jobflowSid, String executionId, String variableTable)
                    throws BulkLoaderSystemException {
                try {
                    DummyProcess process = new DummyProcess();
                    InputStream is;
                    is = new FileInputStream(new File("src/test/data/exporter/SEND_OUT1.zip"));
                    process.setIs(is);
                    process.setExitValue(1);
                    return process;
                } catch (Exception e) {
                    throw new BulkLoaderSystemException(e, this.getClass(), "dummy");
                }
            }

        };
        boolean result = receive.receiveFile(bean);

        // 戻り値を検証
        assertFalse(result);
    }
}
class DummyExportFileReceive extends ExportFileReceive {
    String sshPath;
    String nameNodeIp;
    String nameNodeUser;
    String shellName;
    String targetName;
    String batchId;
    String jobflowId;
    String executionId;
    File exportFile = new File("src/test/data/exporter/SEND_OUT1.zip");
    /**
     * @param exportFile セットする exportFile
     */
    public void setExportFile(File exportFile) {
        this.exportFile = exportFile;
    }
    @Override
    protected Process createProcess(
            String sshPath,
            String nameNodeIp,
            String nameNodeUser,
            String shellName,
            String targetName,
            String batchId,
            String jobflowId,
            String executionId,
            String variableTable)
            throws BulkLoaderSystemException {
        this.sshPath = sshPath;
        this.nameNodeIp = nameNodeIp;
        this.nameNodeUser = nameNodeUser;
        this.shellName = shellName;
        this.targetName = targetName;
        this.batchId = batchId;
        this.jobflowId = jobflowId;
        this.executionId = executionId;

        try {
            DummyProcess process = new DummyProcess();
            InputStream is;
            is = new FileInputStream(exportFile);
            process.setIs(is);
            process.setExitValue(0);
            return process;
        } catch (Exception e) {
            throw new BulkLoaderSystemException(e, this.getClass(), "dummy");
        }
    }
}
class DummyProcess extends Process {
    OutputStream os;
    InputStream is;
    int exitValue;

    @Override
    public OutputStream getOutputStream() {
        return os;
    }

    @Override
    public InputStream getInputStream() {
        return is;
    }

    @Override
    public InputStream getErrorStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }

    @Override
    public int waitFor() throws InterruptedException {
        return exitValue;
    }

    @Override
    public int exitValue() {
        return exitValue;
    }

    @Override
    public void destroy() {
        // 何もしない
    }

    /**
     * @param os セットする os
     */
    public void setOs(OutputStream os) {
        this.os = os;
    }

    /**
     * @param is セットする is
     */
    public void setIs(InputStream is) {
        this.is = is;
    }

    /**
     * @param exitValue セットする exitValue
     */
    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }
}
