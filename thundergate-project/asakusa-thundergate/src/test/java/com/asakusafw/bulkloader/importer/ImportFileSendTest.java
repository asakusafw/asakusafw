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
package com.asakusafw.bulkloader.importer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileCompType;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.StreamFileListProvider;


/**
 * ImportFileSendのテストクラス
 *
 * @author yuta.shirai
 *
 */
@SuppressWarnings("deprecation")
public class ImportFileSendTest {

    private static List<String> properties = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    private static String testTargetName = "target1";
    private static String testBatchId = "batch01";
    private static String testJobflowId = "JOB_FLOW01";
    private static String testExecutionId = "JOB_FLOW01-001";

    /**
     * Temporary folder for this test cases.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initDBServer(testJobflowId, testExecutionId, properties, "target1");
        UnitTestUtil.setUpDB();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }

    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(testJobflowId, testExecutionId, properties, "target1");
        UnitTestUtil.startUp();
    }

    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
    }

    /**
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
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportProtocol(FileList.content("dummy1"));
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setBatchId(testBatchId);
        bean.setTargetName(testTargetName);

        // 圧縮をなしに設定
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_IMP_FILE_COMP_TYPE, FileCompType.STORED.getSymbol());
        ConfigurationLoader.setProperty(p);

        // テスト対象クラス実行
        ImportFileSend send = new Mock("target/asakusa-thundergate/SEND_OUT1.filelist");
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
        assertTrue(result);

        // ファイルの中身を検証
        File resultFile = new File("target/asakusa-thundergate/SEND_OUT1.filelist");
        UnitTestUtil.assertSameFileList(resultFile, importFile);
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
        File importFile2 = new File("src/test/data/importer/IMP_IMPORT_TARGET2.tsv");
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportProtocol(FileList.content("dummy1"));
        tableBean1.setImportFile(importFile1);
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setImportProtocol(FileList.content("dummy2"));
        tableBean2.setImportFile(importFile2);
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("22");
        bean.setExecutionId("22-22");
        bean.setTargetName(testTargetName);

        // テスト対象クラス実行
        ImportFileSend send = new Mock("target/asakusa-thundergate/SEND_OUT2.filelist");
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
        assertTrue(result);

        // 圧縮有無を検証
        File resultFile = new File("target/asakusa-thundergate/SEND_OUT2.filelist");
        assertThat(resultFile.length(), is(lessThan(importFile1.length() + importFile2.length())));

        // ファイルの中身を検証
        UnitTestUtil.assertSameFileList(resultFile, importFile1, importFile2);

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
        tableBean.setImportProtocol(FileList.content("dummy1"));
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setTargetName(testTargetName);

        // テスト対象クラス実行
        ImportFileSend send = new ImportFileSend() {
            @Override
            protected FileListProvider openFileList(
                    String targetName,
                    String batchId,
                    String jobflowId,
                    String executionId) throws IOException {
                throw new IOException();
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
        tableBean.setImportProtocol(FileList.content("dummy1"));
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setBatchId(testBatchId);
        bean.setTargetName(testTargetName);

        // テスト対象クラス実行
        ImportFileSend send = new Mock("target/asakusa-thundergate/SEND_OUT1.filelist");
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
        tableBean.setImportProtocol(FileList.content("dummy1"));
        tableBean.setImportFile(importFile);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setJobflowId("11");
        bean.setExecutionId("11-11");
        bean.setBatchId(testBatchId);
        bean.setTargetName(testTargetName);

        // テスト対象クラス実行
        ImportFileSend send = new Mock("target/asakusa-thundergate/SEND_OUT1.filelist", false);
        boolean result = send.sendImportFile(bean);

        // 戻り値を検証
      assertFalse(result);
    }

    class Mock extends ImportFileSend {

        final String testFile;

        final boolean success;

        Mock(String testFile) {
            this(testFile, true);
        }

        Mock(String testFile, boolean success) {
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
                    return new ByteArrayInputStream(new byte[0]);
                }

                @Override
                protected OutputStream getOutputStream() throws IOException {
                    File file = new File(testFile);
                    return new FileOutputStream(file);
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

