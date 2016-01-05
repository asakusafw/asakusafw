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
package com.asakusafw.bulkloader.extractor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import test.modelgen.table.model.ImportTarget1;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.runtime.io.util.ZipEntryInputStream;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.mapreduce.CacheBuildClient;


/**
 * HdfsFileImportのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class DfsFileImportTest {

    /**
     * Temporary folder for each test case.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /** Importerで読み込むプロパティファイル */
    private static List<String> properties = Arrays.asList(new String[]{"bulkloader-conf-db.properties", "bulkloader-conf-hc.properties"});
    /** ジョブフローID */
    private static String jobflowId = "JOB_FLOW01";
    /** ジョブフロー実行ID */
    private static String executionId = "JOB_FLOW01-001";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initHadoopCluster(jobflowId, executionId, properties);
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        BulkLoaderInitializer.initHadoopCluster(jobflowId, executionId, properties);
        UnitTestUtil.tearDownAfterClass();
        UnitTestUtil.tearDownEnv();
    }
    @Before
    public void setUp() throws Exception {
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
        tableBean1.setDfsFilePath("${execution_id}/import/XXX");
        tableBean1.setImportTargetType(this.getClass());
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("asakusa/import/XXX");
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
                return open("src/test/data/importer/SEND_OUT1.zip");
            }
            @Override
            protected <T> long write(Class<T> targetTableModel,
                    URI hdfsFilePath, InputStream zipEntryInputStream)
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
                return 1;
            }
        };
        boolean result = fileImport.importFile(bean, "hadoop");

        // 戻り値を検証
       assertTrue(result);

       // URIを検証
       assertEquals("file:/tmp/bulkloader/JOB_FLOW01-001/import/XXX", fileImport.getUri()[0].toString());
       assertEquals("file:/tmp/bulkloader/asakusa/import/XXX", fileImport.getUri()[1].toString());

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
                return open("src/test/data/importer/SEND_OUT2.zip");
            }
            @Override
            protected <T> long write(Class<T> targetTableModel,
                    URI hdfsFilePath, InputStream zipEntryInputStream)
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
                return 1;
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
     * Creates a new cache.
     * @throws Exception
     */
    @Test
    public void create_cache() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();

        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("/${user}/${execution_id}/import/c1");
        tableBean1.setImportTargetType(ImportTarget1.class);
        tableBean1.setCacheId("c1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("/${user}/${execution_id}/import/c2");
        tableBean2.setImportTargetType(ImportTarget1.class);
        tableBean2.setCacheId("c2");
        targetTable.put("IMPORT_TARGET2", tableBean2);

        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setExecutionId(executionId);

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer, true);
        final CacheInfo info = new CacheInfo("a", "c1", Calendar.getInstance(), "IMPORT_TARGET1", Arrays.asList("a", "b"), "X", 0);
        writer.openNext(new FileProtocol(
                FileProtocol.Kind.CREATE_CACHE,
                tableBean1.getDfsFilePath(),
                info)
        ).close();
        writer.openNext(new FileProtocol(
                FileProtocol.Kind.CONTENT,
                FileNameUtil.createSendImportFileName("IMPORT_TARGET2"),
                null)
        ).close();
        writer.close();

        final File output = folder.newFolder("output");
        final List<String> files = new ArrayList<String>();
        final List<String> builders = new ArrayList<String>();

        // テスト対象クラス実行
        DummyHdfsFileImport fileImport = new DummyHdfsFileImport(0) {
            @Override
            protected InputStream getInputStream() {
                return new ByteArrayInputStream(buffer.toByteArray());
            }
            @Override
            protected URI resolveLocation(
                    ImportBean ignored, String user, String location) throws BulkLoaderSystemException {
                return new File(output, location).toURI();
            }
            @Override
            protected <T> long write(
                    Class<T> targetTableModel,
                    URI dfsFilePath,
                    InputStream inputStream) throws BulkLoaderSystemException {
                try {
                    inputStream.close();
                    files.add(new File(dfsFilePath).getPath());
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
                return 1;
            }
            @Override
            protected Callable<?> createCacheBuilder(
                    String subcommand,
                    ImportBean ignored,
                    URI location,
                    final CacheInfo target) throws IOException {
                assertThat(subcommand, is(CacheBuildClient.SUBCOMMAND_CREATE));
                assertThat(target, is(info));
                return new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        builders.add(target.getId());
                        return null;
                    }
                };
            }
        };
        boolean result = fileImport.importFile(bean, "hadoop");

        assertTrue(result);

        assertThat(files.size(), is(2));
        assertThat(files.get(0), endsWith("c1/PATCH/part-0"));
        assertThat(files.get(1), endsWith(tableBean2.getDfsFilePath()));

        Collections.sort(builders);
        assertThat(builders.size(), is(1));
        assertThat(builders.get(0), is("c1"));
    }

    /**
     * Update a cache.
     * @throws Exception
     */
    @Test
    public void update_cache() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();

        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("/${user}/${execution_id}/import/c1");
        tableBean1.setImportTargetType(ImportTarget1.class);
        tableBean1.setCacheId("c1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setDfsFilePath("/${user}/${execution_id}/import/c2");
        tableBean2.setImportTargetType(ImportTarget1.class);
        tableBean2.setCacheId("c2");
        targetTable.put("IMPORT_TARGET2", tableBean2);

        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setExecutionId(executionId);

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer, true);
        final CacheInfo info = new CacheInfo("a", "c1", Calendar.getInstance(), "IMPORT_TARGET1", Arrays.asList("a", "b"), "X", 0);
        writer.openNext(new FileProtocol(
                FileProtocol.Kind.UPDATE_CACHE,
                tableBean1.getDfsFilePath(),
                info)
        ).close();
        writer.openNext(new FileProtocol(
                FileProtocol.Kind.CONTENT,
                FileNameUtil.createSendImportFileName("IMPORT_TARGET2"),
                null)
        ).close();
        writer.close();

        final File output = folder.newFolder("output");
        final List<String> files = new ArrayList<String>();
        final List<String> builders = new ArrayList<String>();

        // テスト対象クラス実行
        DummyHdfsFileImport fileImport = new DummyHdfsFileImport(0) {
            @Override
            protected InputStream getInputStream() {
                return new ByteArrayInputStream(buffer.toByteArray());
            }
            @Override
            protected URI resolveLocation(ImportBean _, String user, String location) throws BulkLoaderSystemException {
                return new File(output, location).toURI();
            }
            @Override
            protected <T> long write(
                    Class<T> targetTableModel,
                    URI dfsFilePath,
                    InputStream inputStream) throws BulkLoaderSystemException {
                try {
                    inputStream.close();
                    files.add(new File(dfsFilePath).getPath());
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
                return 1;
            }
            @Override
            protected Callable<?> createCacheBuilder(
                    String subcommand,
                    ImportBean _,
                    URI location,
                    final CacheInfo target) throws IOException {
                assertThat(subcommand, is(CacheBuildClient.SUBCOMMAND_UPDATE));
                assertThat(target, is(info));
                return new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        builders.add(target.getId());
                        return null;
                    }
                };
            }
        };
        boolean result = fileImport.importFile(bean, "hadoop");

        assertTrue(result);

        assertThat(files.size(), is(2));
        assertThat(files.get(0), endsWith("c1/PATCH/part-0"));
        assertThat(files.get(1), endsWith(tableBean2.getDfsFilePath()));

        Collections.sort(builders);
        assertThat(builders.size(), is(1));
        assertThat(builders.get(0), is("c1"));
    }

    /**
     * Abnormal case: content from importer was broken. Extractor should raise error.
     * @throws Exception if failed
     */
    @Test
    public void extract_broken() throws Exception {
        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setDfsFilePath("/${user}/${execution_id}/import/XXX");
        tableBean1.setImportTargetType(ImportTarget1.class);
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportBean bean = new ImportBean();
        bean.setTargetTable(targetTable);
        bean.setExecutionId(executionId);

        final File target = folder.newFile("dummy");

        // テスト対象クラス実行
        DfsFileImport fileImport = new DfsFileImport() {
            @Override
            protected InputStream getInputStream() throws IOException {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                FileList.Writer writer = FileList.createWriter(output, false);
                String name = FileNameUtil.createSendImportFileName("IMPORT_TARGET1");
                OutputStream content = writer.openNext(FileList.content(name));
                content.close();
                output.close(); // FileList will be broken
                return new ByteArrayInputStream(output.toByteArray());
            }
            @Override
            protected URI resolveLocation(ImportBean ignored, String user, String location) {
                return target.toURI();
            }
        };
        boolean result = fileImport.importFile(bean, "hadoop");

        // 戻り値を検証
        assertThat(result, is(false));
    }

    /**
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
                return open("src/test/data/importer/SEND_OUT3.zip");
            }
            @Override
            protected <T> long write(Class<T> targetTableModel,
                    URI hdfsFilePath, InputStream zipEntryInputStream)
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
                return 1;
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
                return open("src/test/data/importer/SEND_OUT1.zip");
            }
            @Override
            protected <T> long write(Class<T> targetTableModel,
                    URI hdfsFilePath, InputStream zipEntryInputStream)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(new NullPointerException(), this.getClass(), "TG-EXTRACTOR-02001");
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
    @SuppressWarnings("deprecation")
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
        prop.setProperty(Constants.PROP_KEY_BASE_PATH, "INVALIDFS://");

        // テスト対象クラス実行
        DfsFileImport fileImport = new DfsFileImport() {
            int count = 0;
            @Override
            protected InputStream getInputStream() {
                return open("src/test/data/importer/SEND_OUT1.zip");
            }
            @Override
            protected <T> long write(Class<T> targetTableModel,
                    URI hdfsFilePath, InputStream zipEntryInputStream)
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
                return 1;
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
    InputStream open(String file) {
        try {
            File temp = folder.newFile("testing");
            UnitTestUtil.createFileList(new File(file), temp);
            return new FileInputStream(temp);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
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
    @Override
    protected Callable<?> createCacheBuilder(
            String subcommand,
            ImportBean bean,
            URI location,
            CacheInfo info) throws IOException {
        return null;
    }
}
