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
package com.asakusafw.bulkloader.collector;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.modelgen.table.model.ImportTarget1;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileCompType;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;

/**
 * ExportFileSendのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExportFileSendTest {
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
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
     * 正常系：複数のファイルを含むZIPファイル送信するケース（圧縮あり）
     * 詳細の設定は以下の通り
     * ・テーブル：IMPORT_TARGET1
     * 　-ファイル：src/test/data/collector/COL_EXPORT_TARGET1_1.tsv
     * 　-ファイル：src/test/data/collector/COL_EXPORT_TARGET1_2.tsv
     * ・テーブル：IMPORT_TARGET2
     * 　-ファイル：src/test/data/collector/COL_EXPORT_TARGET2_1.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendExportFileTest01() throws Exception {
        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        List<String> list1 = new ArrayList<String>();
        list1.add("/src/test/data/collector1");
        list1.add("/${user}/${execution_id}/data/collector2");
        table1.setDfsFilePaths(list1);
        table1.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET1", table1);

        ExportTargetTableBean table2 = new ExportTargetTableBean();
        List<String> list2 = new ArrayList<String>();
        list2.add("/src/test/data/collector3");
        table2.setDfsFilePaths(list2);
        table2.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET2", table2);

        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DummyExportFileSend send = new DummyExportFileSend();
        boolean result = send.sendExportFile(bean, "hadoop");

        // 戻り値を検証
        assertTrue(result);

       // ディレクトリ名の検証
        List<String> dirs = send.getDirs();
        assertEquals(3, dirs.size());
        assertEquals("hdfs://localhost:8020/user/src/test/data/collector1", dirs.get(0));
        assertEquals("hdfs://localhost:8020/user/hadoop/JOB_FLOW01-001/data/collector2", dirs.get(1));
        assertEquals("hdfs://localhost:8020/user/src/test/data/collector3", dirs.get(2));

    }
    /**
     *
     * <p>
     * 正常系：複数のファイルを含むZIPファイル送信するケース（圧縮あり）
     * 詳細の設定は以下の通り
     * ・テーブル：IMPORT_TARGET1
     * 　-ファイル：src/test/data/collector/COL_EXPORT_TARGET1_1.tsv
     * 　-ファイル：src/test/data/collector/COL_EXPORT_TARGET1_2.tsv
     * ・テーブル：IMPORT_TARGET2
     * 　-ファイル：src/test/data/collector/COL_EXPORT_TARGET2_1.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    // TODO 保留 HDFSを使用したテストに修正
    public void sendExportFileTest02() throws Exception {
        // ExportBeanを生成
        File importFile1 = new File("src/test/data/collector/COL_EXPORT_TARGET1_1.tsv");
        long fileSize1 = importFile1.length();
        File importFile2 = new File("src/test/data/collector/COL_EXPORT_TARGET1_2.tsv");
        long fileSize2 = importFile2.length();
        File importFile3 = new File("src/test/data/collector/COL_EXPORT_TARGET2_1.tsv");
        long fileSize3 = importFile3.length();

        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        List<String> list1 = new ArrayList<String>();
        list1.add(importFile1.getPath().replace(File.separatorChar, '/'));
        list1.add(importFile2.getPath().replace(File.separatorChar, '/'));
        table1.setDfsFilePaths(list1);
        table1.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET1", table1);

        ExportTargetTableBean table2 = new ExportTargetTableBean();
        List<String> list2 = new ArrayList<String>();
        list2.add(importFile3.getPath().replace(File.separatorChar, '/'));
        table2.setDfsFilePaths(list2);
        table2.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET2", table2);

        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);

        // テスト対象クラス実行
        DummyExportFileSend send = new DummyExportFileSend() {

            @Override
            protected <T extends Writable> long send(Class<T> targetTableModel,
                    String dir, ZipOutputStream zos, String tableName)
                    throws BulkLoaderSystemException {
                FileInputStream fis = null;
                try {
                    System.out.println(targetTableModel.getName());
                    fis = new FileInputStream(new File(dir));

                    byte[] b = new byte[1024];
                    while (true) {
                        int read = fis.read(b);
                        if (read == -1) {
                            break;
                        }
                        zos.write(b, 0, read);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return 1;
            }
            @Override
            protected OutputStream getOutputStream() {
                OutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File("target/asakusa-thundergate/SEND_OUT.zip"));
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return fos;
            }
        };
        boolean result = send.sendExportFile(bean, "hadoop");

        // 戻り値を検証
        assertTrue(result);

        // 圧縮有無を検証
        File zipFile = new File("target/asakusa-thundergate/SEND_OUT.zip");
        long zipSize = zipFile.length();
        boolean size = ((fileSize1 + fileSize2 + fileSize3) / 2) > zipSize;
        assertTrue(size);

        // ファイルの中身を検証
        File[] expectedFile = new File[3];
        expectedFile[0] = importFile1;
        expectedFile[1] = importFile2;
        expectedFile[2] = importFile3;
        assertTrue(UnitTestUtil.assertZipFile(expectedFile, zipFile));

    }

    /**
     *
     * <p>
     * 正常系：単一のファイルを含むZIPファイル送信するケース（圧縮なし）
     * 詳細の設定は以下の通り
     * ・テーブル：IMPORT_TARGET2
     * 　-ファイル：src/test/data/collector/COL_EXPORT_TARGET2_1.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    // TODO 保留 HDFSを使用したテストに修正
    public void sendExportFileTest03() throws Exception {
        // ExportBeanを生成
        File importFile1 = new File("src/test/data/collector/COL_EXPORT_TARGET2_1.tsv");
        long fileSize1 = importFile1.length();

        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        List<String> list1 = new ArrayList<String>();
        list1.add(importFile1.getPath().replace(File.separatorChar, '/'));
        table1.setDfsFilePaths(list1);
        table1.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET1", table1);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);

        // 圧縮をなしに設定
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXP_FILE_COMP_TYPE, FileCompType.STORED.getCompType());
        ConfigurationLoader.setProperty(p);

        // テスト対象クラス実行
        ExportFileSend send = new ExportFileSend() {

            @Override
            protected <T extends Writable> long send(Class<T> targetTableModel,
                    String dir, ZipOutputStream zos, String tableName)
                    throws BulkLoaderSystemException {
                FileInputStream fis = null;
                try {
                    System.out.println(targetTableModel.getName());
                    fis = new FileInputStream(new File(dir));

                    byte[] b = new byte[1024];
                    while (true) {
                        int read = fis.read(b);
                        if (read == -1) {
                            break;
                        }
                        zos.write(b, 0, read);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return 1;
            }
            @Override
            protected OutputStream getOutputStream() {
                OutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File("target/asakusa-thundergate/SEND_OUT.zip"));
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return fos;
            }
        };
        boolean result = send.sendExportFile(bean, "export");

        // 戻り値を検証
        assertTrue(result);

        // 圧縮有無を検証
        File zipFile = new File("target/asakusa-thundergate/SEND_OUT.zip");
        long zipSize = zipFile.length();
        boolean size = (fileSize1 / 2) < zipSize;
        assertTrue(size);

        // ファイルの中身を検証
        File[] expectedFile = new File[1];
        expectedFile[0] = importFile1;
        assertTrue(UnitTestUtil.assertZipFile(expectedFile, zipFile));

    }
    /**
     *
     * <p>
     * 異常系：ファイル送信中にIO例外が発生するケース
     *
     * </p>
     *
     * @throws Exception
     */
    // TODO 保留 HDFSを使用したテストに修正
    public void sendExportFileTest04() throws Exception {
        // ExportBeanを生成
        File importFile1 = new File("src/test/data/collector/COL_EXPORT_TARGET2_1.tsv");

        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        List<String> list1 = new ArrayList<String>();
        list1.add(importFile1.getPath().replace(File.separatorChar, '/'));
        table1.setDfsFilePaths(list1);
        table1.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET1", table1);
        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);

        // テスト対象クラス実行
        ExportFileSend send = new ExportFileSend() {

            @Override
            protected <T extends Writable> long send(Class<T> targetTableModel,
                    String dir, ZipOutputStream zos, String tableName)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(new NullPointerException(), this.getClass(), "dummy");
            }
            @Override
            protected OutputStream getOutputStream() {
                OutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File("target/asakusa-thundergate/SEND_OUT.zip"));
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return fos;
            }
        };
        boolean result = send.sendExportFile(bean, "hadoop");

        // 戻り値を検証
        assertFalse(result);
    }
    /**
     *
     * <p>
     * 正常系：EXPORTファイルが存在しないケース
     * 詳細の設定は以下の通り
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendExportFileTest05() throws Exception {
        // ExportBeanを生成
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        List<String> list1 = new ArrayList<String>();
        list1.add("/src/test/data/collector1");
        list1.add("/${user}/${execution_id}/data/collector2");
        table1.setDfsFilePaths(list1);
        table1.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET1", table1);

        ExportTargetTableBean table2 = new ExportTargetTableBean();
        List<String> list2 = new ArrayList<String>();
        list2.add("/src/test/data/collector3");
        table2.setDfsFilePaths(list2);
        table2.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET2", table2);

        ExporterBean bean = new ExporterBean();
        bean.setExportTargetTable(targetTable);
        bean.setExecutionId(executionId);

        // テスト対象クラス実行
        DummyExportFileSend send = new DummyExportFileSend() {

            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.collector.DummyExportFileSend#send(java.lang.Class, java.lang.String, java.util.zip.ZipOutputStream, java.lang.String)
             */
            @Override
            protected <T extends Writable> long send(
                    Class<T> targetTableModel, String dir, ZipOutputStream zos,
                    String tableName) throws BulkLoaderSystemException {
                return -1;
            }

        };
        boolean result = send.sendExportFile(bean, "hadoop");

        // 戻り値を検証
        assertTrue(result);
    }
    /**
     *
     * <p>
     * sendメソッドのテストケース
     *
     * 正常系：ファイルの読み込みに成功するケース
    * 詳細の設定は以下の通り
    * ・入力ファイル：src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq
    * ・出力ファイル：target/asakusa-thundergate/READ_EXPORT_TARGET1-1.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendTest01() throws Exception {
        File inFile = new File("src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-1.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, zos, tableName);
            zos.close();
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
     * sendメソッドのテストケース
     *
     * 正常系：ファイルの読み込みに成功するケース(ファイル分割あり)
     * 詳細の設定は以下の通り
     * ・入力ファイル：src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq
     * ・出力ファイル：target/asakusa-thundergate/READ_EXPORT_TARGET1-2.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendTest02() throws Exception {
//        File inFile = new File("src/test/data/collector/sendTest02/READ_EXPORT_TARGET1-2.seq");
        File inFile = new File("src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-2.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // ファイル分割サイズを変更
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXP_LOAD_MAX_SIZE, "10");
        ConfigurationLoader.setProperty(p);

        // テスト対象クラス実行
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, zos, tableName);
            zos.close();
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
     * sendメソッドのテストケース
     *
     * 正常系：Exportするファイルが存在しないケース
     * 詳細の設定は以下の通り
     * ・入力ファイル：src/test/data/collector/sendTest03/READ_EXPORT_TARGET1-3.seq
     * ・出力ファイル：target/asakusa-thundergate/READ_EXPORT_TARGET1-3.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendTest03() throws Exception {
        File inFile = new File("src/test/data/collector/sendTest03/READ_EXPORT_TARGET1-3.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-3.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            boolean isPutEntry = send.send(targetTableModel, inStr, zos, tableName) >= 0;
            if (!isPutEntry) {
                ZipEntry ze = new ZipEntry("DUMMY_FILE");
                try {
                    zos.putNextEntry(ze);
                } catch (IOException e) {
                    throw new BulkLoaderSystemException(e, this.getClass(), MessageIdConst.COL_SENDFILE_EXCEPTION, "ZIPファイルへのエントリの追加に失敗");
                }
            }
            zos.close();
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
     * sendメソッドのテストケース
     *
     * 正常系：Exportするファイルが0byteのケース
     * 詳細の設定は以下の通り
     * ・入力ファイル：src/test/data/collector/sendTest04/READ_EXPORT_TARGET1-4.seq
     * ・出力ファイル：target/asakusa-thundergate/READ_EXPORT_TARGET1-4.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendTest04() throws Exception {
        File inFile = new File("src/test/data/collector/sendTest04/READ_EXPORT_TARGET1-4.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-4.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, zos, tableName);
            zos.close();
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
     * sendメソッドのテストケース
     *
     * 正常系：ディレクトリが存在しないケース
     * 詳細の設定は以下の通り
     * ・入力ファイル：src/test/data/collector/sendTest05
     * ・出力ファイル：target/asakusa-thundergate/READ_EXPORT_TARGET1-3.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendTest05() throws Exception {
        File inFile = new File("src/test/data/collector/sendTest03/READ_EXPORT_TARGET1-3.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-3.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            boolean isPutEntry = send.send(targetTableModel, inStr, zos, tableName) >= 0;
            if (!isPutEntry) {
                ZipEntry ze = new ZipEntry("DUMMY_FILE");
                try {
                    zos.putNextEntry(ze);
                } catch (IOException e) {
                    throw new BulkLoaderSystemException(e, this.getClass(), MessageIdConst.COL_SENDFILE_EXCEPTION, "ZIPファイルへのエントリの追加に失敗");
                }
            }
            zos.close();
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
     * sendメソッドのテストケース
     *
     * 正常系：ファイルの読み込みに成功するケース(ファイル名をワイルドカード指定)
    * 詳細の設定は以下の通り
    * ・入力ファイル：src/test/data/collector/sendTest01/READ_*
    * ・出力ファイル：target/asakusa-thundergate/READ_EXPORT_TARGET1-1.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendTest06() throws Exception {
        File inFile = new File("src/test/data/collector/sendTest01/READ_*");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-1.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, zos, tableName);
            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // ファイルを検証
        assertTrue(outFile.exists());

        // ファイルを削除
        outFile.delete();

    }
}

class DummyExportFileSend extends ExportFileSend {
    List<String> dirs = new ArrayList<String>();
    @Override
    protected <T extends Writable> long send(Class<T> targetTableModel,
            String dir, ZipOutputStream zos, String tableName)
            throws BulkLoaderSystemException {
        dirs.add(dir);
        return 1;
    }
    public List<String> getDirs() {
        return dirs;
    }
}