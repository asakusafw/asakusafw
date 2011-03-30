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
package com.asakusafw.bulkloader.extractor;

import static org.junit.Assert.*;

import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.modelgen.table.model.ImportTarget1;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.extractor.DfsFileImport;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.runtime.io.ZipEntryInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * HdfsFileImportのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class DfsFileImportTest {
    /** ターゲット名 */
    private static String targetName = "target1";
    /** Importerで読み込むプロパティファイル */
    private static List<String> propertys = Arrays.asList(new String[]{"bulkloader-conf-db.properties", "bulkloader-conf-hc.properties"});
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
     * 正常系：複数のファイルを含むZIPファイル受信するケース
     * 詳細の設定は以下の通り
     * ・Importファイル：src/test/data/importer/SEND_OUT1.zip
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void importFileTest01() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("/${user}/${execution_id}/import/XXX");
        tableBean1.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("/asakusa/import/XXX");
        tableBean2.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DummyHdfsFileImport fileImport = new DummyHdfsFileImport(2) {
            int count = 0;
            @Override
            protected InputStream getInputStream() {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File("src/test/data/importer/SEND_OUT1.zip"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return fis;
            }
            @Override
            protected <T> void write(Class<T> targetTableModel,
                    URI hdfsFilePath, ZipEntryInputStream zipEntryInputStream)
                    throws BulkLoaderSystemException {
                FileOutputStream fos = null;
                try {
                    uri[count] = hdfsFilePath;
                    count++;
                    File file = new File("target/asakusa-thundergate/SEND_OUT" + String.valueOf(count) + ".tsv");
                    file.createNewFile();
                    fos = new FileOutputStream(file);


                    byte[] b = new byte[1024];
                    while (true) {
                        int read = zipEntryInputStream.read(b);
                        if (read == -1) {
                            break;
                        }
                        fos.write(b, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        boolean result = fileImport.importFile(bean, "hadoop");

        // 戻り値を検証
       assertTrue(result);

       // URIを検証
       assertEquals("hdfs://localhost:8020/user/hadoop/JOB_FLOW01-001/import/XXX", fileImport.getUri()[0].toString());
       assertEquals("hdfs://localhost:8020/user/asakusa/import/XXX", fileImport.getUri()[1].toString());

       // ファイルの中身を検証
       assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET2-1.tsv"), new File("target/asakusa-thundergate/SEND_OUT1.tsv")));
       assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET2-2.tsv"), new File("target/asakusa-thundergate/SEND_OUT2.tsv")));
    }
    /**
     *
     * <p>
     * 正常系：受信したZIPファイルにディレクトリが含まれるケース
     * 詳細の設定は以下の通り
     * ・Importファイル：src/test/data/importer/SEND_OUT2.zip
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void importFileTest02() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("Dummy");
        tableBean1.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("Dummy");
        tableBean2.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DfsFileImport fileImport = new DfsFileImport() {
            int count = 0;
            @Override
            protected InputStream getInputStream() {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File("src/test/data/importer/SEND_OUT2.zip"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return fis;
            }
            @Override
            protected <T> void write(Class<T> targetTableModel,
                    URI hdfsFilePath, ZipEntryInputStream zipEntryInputStream)
                    throws BulkLoaderSystemException {
                FileOutputStream fos = null;
                try {
                    count++;
                    File file = new File("target/asakusa-thundergate/SEND_OUT" + String.valueOf(count) + ".tsv");
                    file.createNewFile();
                    fos = new FileOutputStream(file);


                    byte[] b = new byte[1024];
                    while (true) {
                        int read = zipEntryInputStream.read(b);
                        if (read == -1) {
                            break;
                        }
                        fos.write(b, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        boolean result = fileImport.importFile(bean, "asakusa");

        // 戻り値を検証
       assertTrue(result);

       // ファイルの中身を検証
       assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET2-1.tsv"), new File("target/asakusa-thundergate/SEND_OUT1.tsv")));
       assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET2-2.tsv"), new File("target/asakusa-thundergate/SEND_OUT2.tsv")));
    }
    /**
     *
     * <p>
     * 異常系：ZIPエントリに対応するテーブルの定義がDSL存在しないケース
     * 詳細の設定は以下の通り
     * ・Importファイル：src/test/data/importer/SEND_OUT3.zip
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void importFileTest03() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("Dummy");
        tableBean1.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("Dummy");
        tableBean2.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);

        // テスト対象クラス実行
        DfsFileImport fileImport = new DfsFileImport() {
            int count = 0;
            @Override
            protected InputStream getInputStream() {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File("src/test/data/importer/SEND_OUT3.zip"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return fis;
            }
            @Override
            protected <T> void write(Class<T> targetTableModel,
                    URI hdfsFilePath, ZipEntryInputStream zipEntryInputStream)
                    throws BulkLoaderSystemException {
                FileOutputStream fos = null;
                try {
                    count++;
                    File file = new File("target/asakusa-thundergate/SEND_OUT" + String.valueOf(count) + ".tsv");
                    file.createNewFile();
                    fos = new FileOutputStream(file);


                    byte[] b = new byte[1024];
                    while (true) {
                        int read = zipEntryInputStream.read(b);
                        if (read == -1) {
                            break;
                        }
                        fos.write(b, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        boolean result = fileImport.importFile(bean, "asakusa");

        // 戻り値を検証
       assertFalse(result);
    }
    /**
     *
     * <p>
     * 異常系：ファイルのHDFSへの書き出し中にIOExceptionが発生するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void importFileTest04() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("Dummy");
        tableBean1.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("Dummy");
        tableBean2.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DfsFileImport fileImport = new DfsFileImport() {
            @Override
            protected InputStream getInputStream() {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File("src/test/data/importer/SEND_OUT1.zip"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return fis;
            }
            @Override
            protected <T> void write(Class<T> targetTableModel,
                    URI hdfsFilePath, ZipEntryInputStream zipEntryInputStream)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(new NullPointerException(), this.getClass(), "dummy");
            }
        };
        boolean result = fileImport.importFile(bean, "asakusa");

        // 戻り値を検証
       assertFalse(result);
    }
    /**
     *
     * <p>
     * 異常系：HDFSのURIが不正なケース
     * 詳細の設定は以下の通り
     * ・Importファイル：src/test/data/importer/SEND_OUT1.zip
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void importFileTest05() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("/asakusa/import/XXX");
        tableBean1.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("/asakusa/import/XXX");
        tableBean2.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setExecutionId(executionId);

        // プロパティを修正
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST, "あいう｜|://");

        // テスト対象クラス実行
        DfsFileImport fileImport = new DfsFileImport() {
            int count = 0;
            @Override
            protected InputStream getInputStream() {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File("src/test/data/importer/SEND_OUT1.zip"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return fis;
            }
            @Override
            protected <T> void write(Class<T> targetTableModel,
                    URI hdfsFilePath, ZipEntryInputStream zipEntryInputStream)
                    throws BulkLoaderSystemException {
                FileOutputStream fos = null;
                try {
                    count++;
                    File file = new File("target/asakusa-thundergate/SEND_OUT" + String.valueOf(count) + ".tsv");
                    file.createNewFile();
                    fos = new FileOutputStream(file);


                    byte[] b = new byte[1024];
                    while (true) {
                        int read = zipEntryInputStream.read(b);
                        if (read == -1) {
                            break;
                        }
                        fos.write(b, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        boolean result = fileImport.importFile(bean, "hadoop");

        // 戻り値を検証
     assertFalse(result);

    }
    /**
    *
    * <p>
    * writeメソッドのテストケース
    *
    * 正常系：ファイルの書き出しに成功するケース
    * 詳細の設定は以下の通り
    * ・入力ファイル：src/test/data/extractor/IMPORT_TARGET1.zip
    * ・出力ファイル：target/asakusa-thundergate/WRITE_IMPORT_TARGET1-1
    * </p>
    *
    * @throws Exception
    */
    @Test
    public void writeTest01() throws Exception {
        File inFile = new File("src/test/data/extractor/IMPORT_TARGET1.zip");
        File outFile = new File("target/asakusa-thundergate/WRITE_IMPORT_TARGET1-1");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;

        // テスト対象クラス実行
        DfsFileImport fileImport = new DfsFileImport();
        try {
            ZipInputStream zipIs = new ZipInputStream(new FileInputStream(inFile));
            ZipEntry zipEntry = null;
            while((zipEntry = zipIs.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    // エントリがディレクトリの場合はスキップする
                    continue;
                } else {
                    break;
                }
            }
            fileImport.write(targetTableModel, outFile.toURI(), new ZipEntryInputStream(zipIs));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // ファイルを検証
        assertTrue(outFile.exists());

        // ファイルを削除
        outFile.delete();
    }
    /**
    *
    * <p>
    * getCompTypeメソッドのテストケース
    *
    * 正常系：NONEのケース
    *
    * </p>
    *
    * @throws Exception
    */
    @Test
    public void getCompType01() throws Exception {
        DfsFileImport fileImport = new DfsFileImport();
        CompressionType compType = fileImport.getCompType("NONE");
        assertEquals(CompressionType.NONE, compType);
    }
    /**
    *
    * <p>
    * getCompTypeメソッドのテストケース
    *
    * 正常系：BLOCKのケース
    *
    * </p>
    *
    * @throws Exception
    */
    @Test
    public void getCompType02() throws Exception {
        DfsFileImport fileImport = new DfsFileImport();
        CompressionType compType = fileImport.getCompType("BLOCK");
        assertEquals(CompressionType.BLOCK, compType);
    }
    /**
    *
    * <p>
    * getCompTypeメソッドのテストケース
    *
    * 正常系：RECORDのケース
    *
    * </p>
    *
    * @throws Exception
    */
    @Test
    public void getCompType03() throws Exception {
        DfsFileImport fileImport = new DfsFileImport();
        CompressionType compType = fileImport.getCompType("RECORD");
        assertEquals(CompressionType.RECORD, compType);
    }
    /**
    *
    * <p>
    * getCompTypeメソッドのテストケース
    *
    * 正常系：存在しないタイプのケース
    *
    * </p>
    *
    * @throws Exception
    */
    @Test
    public void getCompType04() throws Exception {
        DfsFileImport fileImport = new DfsFileImport();
        CompressionType compType = fileImport.getCompType("DUMMY");
        assertEquals(CompressionType.NONE, compType);
    }
}

class DummyHdfsFileImport extends DfsFileImport {
    URI[] uri = null;
    DummyHdfsFileImport(int tableCount) {
        uri = new URI[tableCount];
    }
    public URI[] getUri() {
        return uri;
    }

}