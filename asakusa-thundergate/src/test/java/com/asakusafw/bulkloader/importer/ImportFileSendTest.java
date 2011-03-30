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
package com.asakusafw.bulkloader.importer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileCompType;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;


/**
 * ImportFileSendのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ImportFileSendTest {
    /** ターゲット名 */
    private static String targetName = "target1";
    /** Importerで読み込むプロパティファイル */
    private static List<String> propertys = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    /** バッチID */
    private static String batchId = "batch01";
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
     * 正常系：1つのファイルを送信するケース(ZIP圧縮なし)
     * 詳細の設定は以下の通り
     * ・Importファイル1：src/test/data/importer/IMP_IMPORT_TARGET2.tsv
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendImportFileTtest01() throws Exception {
        // ImportBeanを生成
        File importFile = new File("src/test/data/importer/IMP_IMPORT_TARGET1.tsv");
        long fileSize = importFile.length();
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setBatchId(batchId);
        bean.setTargetName(targetName);

        // 圧縮をなしに設定
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_IMP_FILE_COMP_TYPE, FileCompType.STORED.getCompType());
        ConfigurationLoader.setProperty(p);

        // テスト対象クラス実行
        ImportFileSendStub send = new ImportFileSendStub() {

            @Override
            protected Process createProcess(String sshPath, String nameNodeIp,
                    String nameNodeUser, String extractorShellName, String targetName,
                    String batchId, String jobflowId, String executionId, String variableTable)
                    throws BulkLoaderSystemException {
                this.sshPath = sshPath;
                this.nameNodeIp = nameNodeIp;
                this.nameNodeUser = nameNodeUser;
                this.extractorShellName = extractorShellName;
                this.targetName = targetName;
                this.batchId = batchId;
                this.jobflowId = jobflowId;
                this.executionId = executionId;

                DummyProcess process = new DummyProcess();
                File file = new File("target/asakusa-thundergate/SEND_OUT1.zip");
                OutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                InputStream is = System.in;
                process.setOs(os);
                process.setIs(is);
                process.setExitValue(0);
                return process;
            }
        };
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
        assertTrue(result);

        // プロセスに設定する値を検証
        assertEquals(send.sshPath, ConfigurationLoader.getProperty(Constants.PROP_KEY_SSH_PATH));
        assertEquals(send.nameNodeIp, ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_HOST));
        assertEquals(send.nameNodeUser, ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_USER));
        assertEquals(send.extractorShellName, ConfigurationLoader.getProperty(Constants.PROP_KEY_EXT_SHELL_NAME));
        assertEquals(send.targetName, targetName);
        assertEquals(send.batchId, bean.getBatchId());
        assertEquals(send.jobflowId, bean.getJobflowId());
        assertEquals(send.executionId, bean.getExecutionId());


        // 圧縮有無を検証
        File zipFile = new File("target/asakusa-thundergate/SEND_OUT1.zip");
        long zipSize = zipFile.length();
        boolean size = (fileSize / 2) < zipSize;
        assertTrue(size);

        // ファイルの中身を検証
        File[] expectedFile = new File[1];
        expectedFile[0] = importFile;
        assertTrue(UnitTestUtil.assertZipFile(expectedFile, zipFile));
    }
    /**
    *
    * <p>
    * 正常系：複数のファイルを送信するケース(ZIP圧縮あり)
    * 詳細の設定は以下の通り
    * ・Importファイル1：src/test/data/importer/IMP_IMPORT_TARGET1.tsv
    * ・Importファイル2：src/test/data/importer/IMP_IMPORT_TARGET2.tsv
    * </p>
    *
    * @throws Exception
    */
    @Test
    public void sendImportFileTtest02() throws Exception {
        // ImportBeanを生成
        File importFile1 = new File("src/test/data/importer/IMP_IMPORT_TARGET1.tsv");
        long fileSize1 = importFile1.length();
        File importFile2 = new File("src/test/data/importer/IMP_IMPORT_TARGET2.tsv");
        long fileSize2 = importFile2.length();

        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportFile(importFile1);
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setImportFile(importFile2);
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("22");
        bean.setExecutionId("22-22");
        bean.setTargetName(targetName);

        // テスト対象クラス実行
        ImportFileSendStub send = new ImportFileSendStub() {

            @Override
            protected Process createProcess(String sshPath, String nameNodeIp,
                    String nameNodeUser, String extractorShellName, String targetName,
                    String batchId, String jobflowId, String executionId, String variableTable)
                    throws BulkLoaderSystemException {
                this.sshPath = sshPath;
                this.nameNodeIp = nameNodeIp;
                this.nameNodeUser = nameNodeUser;
                this.extractorShellName = extractorShellName;
                this.targetName = targetName;
                this.batchId = batchId;
                this.jobflowId = jobflowId;
                this.executionId = executionId;

                DummyProcess process = new DummyProcess();
                File file = new File("target/asakusa-thundergate/SEND_OUT2.zip");
                OutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                InputStream is = System.in;
                process.setOs(os);
                process.setIs(is);
                process.setExitValue(0);

                return process;
            }

        };
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
        assertTrue(result);

        // プロセスに設定する値を検証
        assertEquals(send.sshPath, ConfigurationLoader.getProperty(Constants.PROP_KEY_SSH_PATH));
        assertEquals(send.nameNodeIp, ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_HOST));
        assertEquals(send.nameNodeUser, ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_USER));
        assertEquals(send.extractorShellName, ConfigurationLoader.getProperty(Constants.PROP_KEY_EXT_SHELL_NAME));
        assertEquals(send.jobflowId, bean.getJobflowId());
        assertEquals(send.targetName, targetName);
        assertEquals(send.executionId, bean.getExecutionId());


        // 圧縮有無を検証
        File zipFile = new File("target/asakusa-thundergate/SEND_OUT2.zip");
        long zipSize = zipFile.length();
        boolean size = ((fileSize1 + fileSize2) / 2) > zipSize;
        assertTrue(size);

        // ファイルの中身を検証
        File[] expectedFile = new File[2];
        expectedFile[0] = importFile1;
        expectedFile[1] = importFile2;
        assertTrue(UnitTestUtil.assertZipFile(expectedFile, zipFile));

    }
    /**
     * <p>
     * 異常系：IOExceptionが発生するケース
     * （以下のオブジェクトをオープンする前に例外発生）
     * ・Process
     * ・OutputStream
     * ・ZipOutputStream
     * ・FileInputStream
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendImportFileTtest03() throws Exception {
        // ImportBeanを生成
        File importFile = new File("src/test/data/importer/IMP_IMPORT_TARGET2.tsv");
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setTargetName(targetName);

        // テスト対象クラス実行
        ImportFileSendStub send = new ImportFileSendStub() {

            @Override
            protected Process createProcess(String sshPath, String nameNodeIp,
                    String nameNodeUser, String extractorShellName, String batchId,
                    String jobflowId, String jobflowSid, String executionId, String variableTable)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(new NullPointerException(), this.getClass(), "dummy");
            }
        };
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
        assertFalse(result);
    }
    /**
     *
     * <p>
     * 異常系：Importファイルが存在しない
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendImportFileTtest04() throws Exception {
        // ImportBeanを生成
        File importFile = new File("src/test/data/importer/IMP_IMPORT_TARGET99.tsv");
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setBatchId(batchId);
        bean.setTargetName(targetName);

        // テスト対象クラス実行
        ImportFileSendStub send = new ImportFileSendStub() {

            @Override
            protected Process createProcess(String sshPath, String nameNodeIp,
                    String nameNodeUser, String extractorShellName, String targetName,
                    String batchId, String jobflowId, String executionId, String variableTable)
                    throws BulkLoaderSystemException {
                this.sshPath = sshPath;
                this.nameNodeIp = nameNodeIp;
                this.nameNodeUser = nameNodeUser;
                this.extractorShellName = extractorShellName;
                this.targetName = targetName;
                this.batchId = batchId;
                this.jobflowId = jobflowId;
                this.executionId = executionId;

                DummyProcess process = new DummyProcess();
                File file = new File("target/asakusa-thundergate/SEND_OUT1.zip");
                OutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                InputStream is = System.in;
                process.setOs(os);
                process.setIs(is);
                process.setExitValue(0);
                return process;
            }
        };
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
       assertFalse(result);
    }
    /**
     *
     * <p>
     * 異常系：サブプロセスの終了コードが0以外
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendImportFileTtest05() throws Exception {
        // ImportBeanを生成
        File importFile = new File("src/test/data/importer/IMP_IMPORT_TARGET1.tsv");
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setBatchId(batchId);
        bean.setTargetName(targetName);

        // テスト対象クラス実行
        ImportFileSendStub send = new ImportFileSendStub() {

            @Override
            protected Process createProcess(String sshPath, String nameNodeIp,
                    String nameNodeUser, String extractorShellName, String targetName,
                    String batchId, String jobflowId, String executionId, String variableTable)
                    throws BulkLoaderSystemException {
                this.sshPath = sshPath;
                this.nameNodeIp = nameNodeIp;
                this.nameNodeUser = nameNodeUser;
                this.extractorShellName = extractorShellName;
                this.targetName = targetName;
                this.batchId = batchId;
                this.jobflowId = jobflowId;
                this.executionId = executionId;

                DummyProcess process = new DummyProcess();
                File file = new File("target/asakusa-thundergate/SEND_OUT1.zip");
                OutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                InputStream is = System.in;
                process.setOs(os);
                process.setIs(is);
                process.setExitValue(1);
                return process;
            }
        };
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
      assertFalse(result);
    }

}
class ImportFileSendStub extends ImportFileSend {
    String sshPath;
    String nameNodeIp;
    String nameNodeUser;
    String extractorShellName;
    String targetName;
    String batchId;
    String jobflowId;
    String executionId;
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