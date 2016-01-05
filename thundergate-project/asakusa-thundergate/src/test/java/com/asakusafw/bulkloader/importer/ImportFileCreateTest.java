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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.ImportTableLockType;
import com.asakusafw.bulkloader.common.ImportTableLockedOperation;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;


/**
 * ImportFileCreateのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ImportFileCreateTest {
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
     * 正常系：Import対象テーブルが1つのケース
     *         (「テーブルロック：エラーとする」）
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：テーブルロック
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * ・Importファイルが既に存在する
     * ・ファイルパスは絶対パス指定
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createImportFileTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テストデータの指定
//        String pattern = "patternC01";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition(null);
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.TABLE);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        String jobflowSid = "1";

        // テスト対象クラス実行
        ImportFileCreate create = new ImportFileCreate();
        boolean result = create.createImportFile(bean, jobflowSid);

        // ファイルを生成
        File[] file = createFile(targetName, jobflowId, executionId, "IMPORT_TARGET1");

        // 実行結果の検証
        assertTrue(result);
        assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET1-1.tsv"),  file[1]));

        // ファイルを削除
        ImportFileDelete delete = new ImportFileDelete();
        delete.deleteFile(bean);
    }
    /**
     *
     * <p>
     * 正常系：Import対象テーブルが1つのケース
     *         (「テーブルロック：エラーとする」）
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：テーブルロック
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * ・Importファイルが既に存在する
     * ・ファイルパスは相対パス指定
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createImportFileTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest02");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition(null);
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.TABLE);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, "JOB_FLOW01-002", "20101021221015", "3", "5"}, targetTable);

        String jobflowSid = "9";


        // テスト対象クラス実行
        ImportFileCreate create = new ImportFileCreate();
        boolean result = create.createImportFile(bean, jobflowSid);

        // ファイルを生成
        File[] file = createFile(targetName, jobflowId, "JOB_FLOW01-002", "IMPORT_TARGET1");

        // 実行結果の検証
       assertTrue(result);
       assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET1-1.tsv"),  file[1]));

       // ファイルを削除
       ImportFileDelete delete = new ImportFileDelete();
       delete.deleteFile(bean);
    }
    /**
     *
     * <p>
     * 正常系：Import対象テーブルが2つのケース
     *         (「ロックを取得しない：ロック有無に関わらず処理対象とする」「行ロック：処理対象から外す」）
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：INTDATA1=11
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：ロックしない
     * 　- ロック済みの場合の挙動：ロック有無に関わらず処理対象とする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * ・Import対象テーブル：IMPORT_TARGET2
     * 　- Import対象カラム：TEXTDATA2
     * 　- 検索条件：TEXTDATA2=testdata2-3
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：行ロック
     * 　- ロック済みの場合の挙動：処理対象から外す
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * ・Importファイルが存在しない
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createImportFileTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest03");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternC02";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.NONE);
        tableBean1.setLockedOperation(ImportTableLockedOperation.FORCE);
        tableBean1.setImportTargetType(null);
        tableBean1.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean1);

        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA2", "INTDATA2"}));
        tableBean2.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean2.setUseCache(false);
        tableBean2.setLockType(ImportTableLockType.RECORD);
        tableBean2.setLockedOperation(ImportTableLockedOperation.OFF);
        tableBean2.setImportTargetType(null);
        tableBean2.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = createBean(new String[]{jobflowId, "JOB_FLOW01-003", "20101021221015", "3", "5"}, targetTable);

        String jobflowSid = "2";

        // テスト対象クラス実行
        ImportFileCreate create = new ImportFileCreate();
        boolean result = create.createImportFile(bean, jobflowSid);

        // ファイルを生成
        File[] file1 = createFile(targetName, jobflowId, "JOB_FLOW01-003", "IMPORT_TARGET1");
        File[] file2 = createFile(targetName, jobflowId, "JOB_FLOW01-003", "IMPORT_TARGET2");

        // 実行結果の検証
        assertTrue(result);
        assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET1-2.tsv"),  file1[1]));
        assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET2-2.tsv"),  file2[1]));

        // ファイルを削除
        ImportFileDelete delete = new ImportFileDelete();
        delete.deleteFile(bean);
    }
    /**
     *
     * <p>
     * 正常系：Import対象テーブルが1つのケース
     *         (「行ロック：エラーとする」）
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：行ロック
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createImportFileTest04() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest04");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternC03";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"INTDATA1", "TEXTDATA1"}));
        tableBean1.setSearchCondition(null);
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.RECORD);
        tableBean1.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean1.setImportTargetType(null);
        tableBean1.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean1);
        ImportBean bean = createBean(new String[]{jobflowId, "JOB_FLOW01-004", "20101021221015", "3", "5"}, targetTable);

        String jobflowSid = "3";

        // テスト対象クラス実行
        ImportFileCreate create = new ImportFileCreate();
        boolean result = create.createImportFile(bean, jobflowSid);

        // ファイルを生成
        File[] file1 = createFile(targetName, jobflowId, "JOB_FLOW01-004", "IMPORT_TARGET1");

        // 実行結果の検証
       assertTrue(result);
       assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET1-3.tsv"),  file1[1]));

       // ファイルを削除
       ImportFileDelete delete = new ImportFileDelete();
       delete.deleteFile(bean);
    }
    /**
     *
     * <p>
     * 異常系：importファイルが既に存在し、削除に失敗するケース
     * </p>
     *
     * @throws Exception
     */
    // Linux上ではオープンしているファイルを消すことができるため当テストは成功しない。
    // よって、テスト対象から除外。（Windows上では動作を確認済み）
    public void createImportFileTest05() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest05");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternC03";

        FileOutputStream fos = null;
        try {
            // ファイルを生成
            String jobflowSid = "4";
            File[] file = createFile(targetName, jobflowId, executionId, "IMPORT_TARGET1");
            file[0].mkdirs();
            fos = new FileOutputStream(file[1]);
            fos.write(123);

            // ImportBeanを生成
            Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
            ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
            tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"INTDATA1", "TEXTDATA1"}));
            tableBean1.setSearchCondition(null);
            tableBean1.setUseCache(false);
            tableBean1.setLockType(ImportTableLockType.RECORD);
            tableBean1.setLockedOperation(ImportTableLockedOperation.ERROR);
            tableBean1.setImportTargetType(null);
            tableBean1.setDfsFilePath(null);
            targetTable.put("IMPORT_TARGET1", tableBean1);
            ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

            // テスト対象クラス実行
            ImportFileCreate create = new ImportFileCreate();
            boolean result = create.createImportFile(bean, jobflowSid);

            // 実行結果の検証
            assertFalse(result);

            // ファイルを削除
            ImportFileDelete delete = new ImportFileDelete();
            delete.deleteFile(bean);

        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
    /**
     *
     * <p>
     * 異常系：SQL例外が発生し、importファイルの生成に失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createImportFileTest06() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest06");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternC01";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition(null);
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.TABLE);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET3", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        String jobflowSid = "5";

        // テスト対象クラス実行
        ImportFileCreate create = new ImportFileCreate();
        boolean result = create.createImportFile(bean, jobflowSid);

        // ファイルを生成
        File[] file = createFile(targetName, jobflowId, executionId, "IMPORT_TARGET3");

        // 実行結果の検証
        assertFalse(result);
        assertFalse(file[1].exists());
        assertTrue(file[0].exists());

        // ファイルを削除
        ImportFileDelete delete = new ImportFileDelete();
        delete.deleteFile(bean);
    }
    /**
     *
     * <p>
     * 正常系：Import対象のデータが存在せず、0byteのファイルを生成するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void createImportFileTest07() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest07");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternC01";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition("1 < 0");
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.TABLE);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        String jobflowSid = "7";

        // テスト対象クラス実行
        ImportFileCreate create = new ImportFileCreate();
        boolean result = create.createImportFile(bean, jobflowSid);

        // ファイルを生成
        File[] file = createFile(targetName, jobflowId, executionId, "IMPORT_TARGET1");

        // 実行結果の検証
       assertTrue(result);
       assertTrue(UnitTestUtil.assertFile(new File("src/test/data/importer/IMP_IMPORT_TARGET1-7.tsv"),  file[1]));

       // ファイルを削除
       ImportFileDelete delete = new ImportFileDelete();
       delete.deleteFile(bean);
    }
    /**
     *
     * <p>
     * 異常系：Importファイルを生成するディレクトリが存在しない
     * </p>
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void createImportFileTest08() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/ImportFileCreateTest/createImportFileTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テストデータの指定
//        String pattern = "patternC01";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition(null);
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.TABLE);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        // プロパティを書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_IMP_FILE_DIR, "src/test/data/importer/temp");
        ConfigurationLoader.setProperty(prop);

        String jobflowSid = "1";

        // テスト対象クラス実行
        ImportFileCreate create = new ImportFileCreate();
        boolean result = create.createImportFile(bean, jobflowSid);

        // ファイルを生成
        File[] file = createFile(targetName, jobflowId, executionId, "IMPORT_TARGET1");

        // 実行結果の検証
       assertFalse(result);
    }
    /**
     * パラメータを保持するBeanを作成する
     * ・args[0]=ジョブフローID
     * ・args[1]=ジョブフロー実行ID
     * ・args[2]=ジョブネットの終了予定時刻
     * ・args[3]=リトライ回数
     * ・args[4]=リトライインターバル
     * @param args
     * @param targetTable
     * @return ImportBean
     */
    private static ImportBean createBean(String[] args, Map<String, ImportTargetTableBean> targetTable) {
        ImportBean bean = new ImportBean();
        // ターゲット名
        bean.setTargetName(targetName);
        // ジョブフローID
        bean.setJobflowId(args[0]);
        // ジョブフロー実行ID
        bean.setExecutionId(args[1]);
        // ジョブネットの終了予定時刻
        String date = args[2];
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
        cal.set(Calendar.DATE, Integer.parseInt(date.substring(6, 8)));
        cal.set(Calendar.HOUR, Integer.parseInt(date.substring(8, 10)));
        cal.set(Calendar.MINUTE, Integer.parseInt(date.substring(10, 12)));
        cal.set(Calendar.SECOND, Integer.parseInt(date.substring(12, 14)));
        bean.setJobnetEndTime(cal.getTime());
        // リトライ回数・リトライインターバル
        bean.setRetryCount(Integer.parseInt(args[3]));
        bean.setRetryInterval(Integer.parseInt(args[4]));

        // テーブル毎の設定
        bean.setTargetTable(targetTable);

        return bean;
    }
    /**
     * ファイル名を作成する
     * @param jobflowId
     * @param executionId
     * @param tableName
     * @return
     * @throws IOException
     */
    private File[] createFile(String targetName, String jobflowId, String executionId, String tableName) throws IOException {
        // ディレクトリを作成
        File fileDirectry = new File(ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_DIR));

        // ファイルを作成
        StringBuffer strFileNmae = new StringBuffer(Constants.IMPORT_FILE_PREFIX);
        strFileNmae.append(Constants.IMPORT_FILE_DELIMITER);
        strFileNmae.append(targetName);
        strFileNmae.append(Constants.IMPORT_FILE_DELIMITER);
        strFileNmae.append(jobflowId);
        strFileNmae.append(Constants.IMPORT_FILE_DELIMITER);
        strFileNmae.append(executionId);
        strFileNmae.append(Constants.IMPORT_FILE_DELIMITER);
        strFileNmae.append(tableName);
        strFileNmae.append(Constants.IMPORT_FILE_EXTENSION);
        File file = new File (fileDirectry, strFileNmae.toString());

        File[] dirFile = new File[2];
        dirFile[0] = fileDirectry;
        dirFile[1] = file;

        return dirFile;
    }
}
