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
package com.asakusafw.bulkloader.collector;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import test.modelgen.table.model.ImportTarget1;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;

/**
 * ExportFileSendのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExportFileSendTest {

    /**
     * A temporary folder.
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
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownAfterClass();
    }
    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, properties, "target1");
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
        list1.add("src/test/data/collector1");
        list1.add("${execution_id}/data/collector2");
        table1.setDfsFilePaths(list1);
        table1.setExportTargetType(NullWritable.class);
        targetTable.put("EXP_TARGET1", table1);

        ExportTargetTableBean table2 = new ExportTargetTableBean();
        List<String> list2 = new ArrayList<String>();
        list2.add("src/test/data/collector3");
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
        assertEquals("file:/tmp/bulkloader/src/test/data/collector1", dirs.get(0));
        assertEquals("file:/tmp/bulkloader/JOB_FLOW01-001/data/collector2", dirs.get(1));
        assertEquals("file:/tmp/bulkloader/src/test/data/collector3", dirs.get(2));

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
                    Class<T> targetTableModel,
                    String dir,
                    FileList.Writer writer,
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
        File inFile = prepareInput("src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-1.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            FileList.Writer writer = FileList.createWriter(new FileOutputStream(outFile), true);
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, writer, tableName);
            writer.close();
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
        File inFile = prepareInput("src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-2.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // ファイル分割サイズを変更
        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXP_LOAD_MAX_SIZE, "10");
        ConfigurationLoader.setProperty(p);

        // テスト対象クラス実行
        try {
            FileList.Writer writer = FileList.createWriter(new FileOutputStream(outFile), true);
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, writer, tableName);
            writer.close();
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
            FileList.Writer writer = FileList.createWriter(new FileOutputStream(outFile), true);
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, writer, tableName);
            writer.close();
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
        File inFile = prepareInput("src/test/data/collector/sendTest04/READ_EXPORT_TARGET1-4.seq");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-4.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            FileList.Writer writer = FileList.createWriter(new FileOutputStream(outFile), true);
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, writer, tableName);
            writer.close();
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
            FileList.Writer writer = FileList.createWriter(new FileOutputStream(outFile), true);
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, writer, tableName);
            writer.close();
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
    * ・入力ファイル：src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq
    * ・出力ファイル：target/asakusa-thundergate/READ_EXPORT_TARGET1-1.tsv
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void sendTest06() throws Exception {
        File inFile = prepareInput("src/test/data/collector/sendTest01/READ_EXPORT_TARGET1-1.seq");
        inFile = new File(inFile.getParentFile(), inFile.getName() + "*");
        File outFile = new File("target/asakusa-thundergate/READ_EXPORT_TARGET1-1.zip");
        Class<ImportTarget1> targetTableModel = ImportTarget1.class;
        String tableName = "EXP_TARGET1";

        // テスト対象クラス実行
        try {
            FileList.Writer writer = FileList.createWriter(new FileOutputStream(outFile), true);
            ExportFileSend send = new ExportFileSend();
            URI inUri = inFile.toURI();
            String inStr = inUri.toString();
            send.send(targetTableModel, inStr, writer, tableName);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // ファイルを検証
        assertTrue(outFile.exists());

        // ファイルを削除
        outFile.delete();
    }

    @SuppressWarnings("unchecked")
    private File prepareInput(String path) throws IOException {
        File result = folder.newFile();
        Path p = new Path(new File(path).toURI());
        FileSystem fs = p.getFileSystem(new Configuration());
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, p, fs.getConf());
        try {
            Writable buffer = (Writable) reader.getValueClass().newInstance();
            ModelOutput<Writable> output = (ModelOutput<Writable>) TemporaryStorage.openOutput(
                    fs.getConf(),
                    reader.getValueClass(),
                    new BufferedOutputStream(new FileOutputStream(result)));
            try {
                while (reader.next(NullWritable.get(), buffer)) {
                    output.write(buffer);
                }
            } finally {
                output.close();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            reader.close();
        }
        return result;
    }
}

class DummyExportFileSend extends ExportFileSend {
    List<String> dirs = new ArrayList<String>();
    @Override
    protected <T extends Writable> long send(
            Class<T> targetTableModel,
            String filePath,
            FileList.Writer writer,
            String tableName) throws BulkLoaderSystemException {
        dirs.add(filePath);
        return 1;
    }
    public List<String> getDirs() {
        return dirs;
    }
}