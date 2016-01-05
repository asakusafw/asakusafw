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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ImportTableLockType;
import com.asakusafw.bulkloader.common.ImportTableLockedOperation;
import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.inspect.Cause;


/**
 * TargetDataLockのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class TargetDataLockTest {
    /** Importerで読み込むプロパティファイル */
    private static List<String> PROPERTYS = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});

    /** ターゲット名 */
    private static String targetName = "target1";
    /** ジョブフローID */
    private static String jobflowId = "JOB_FLOW01";
    /** ジョブフロー実行ID */
    private static String executionId = "JOB_FLOW01-001";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, PROPERTYS, targetName);
        UnitTestUtil.setUpDB();
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }
    @Before
    public void setUp() throws Exception {
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, PROPERTYS, "target1");
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
     * 　　　　（「テーブルロック：エラーとする」）
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：テーブルロック
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternL01";

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

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        boolean result = lock.lock(bean);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }
    /**
     *
     * <p>
     * 正常系：Import対象テーブルが2つのケース
     * 　　　　（「ロックしない：ロック有無に関わらず処理対象とする」「行ロック：処理対象から外す」）
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
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest02");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // テストデータの指定
//        String pattern = "patternL02";

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
        tableBean2.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA2"}));
        tableBean2.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean2.setUseCache(false);
        tableBean2.setLockType(ImportTableLockType.RECORD);
        tableBean2.setLockedOperation(ImportTableLockedOperation.OFF);
        tableBean2.setImportTargetType(null);
        tableBean2.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        boolean result = lock.lock(bean);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }
    /**
     * <p>
     * 異常系：行ロックを取得できない
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：TEXTDATA1='testdata1-2'
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
    public void lockTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest03");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL03";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition("TEXTDATA1='testdata1-2'");
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.RECORD);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 正常系：Import対象テーブルが2つのケース
     * 　　　　（「行ロック：エラーとする」「ロックしない：エラーとする」）
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：INTDATA1=11
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：行ロック
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * ・Import対象テーブル：IMPORT_TARGET2
     * 　- Import対象カラム：TEXTDATA2
     * 　- 検索条件：TEXTDATA2=testdata2-3
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：ロックしない
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest04() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest04");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternL04";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.RECORD);
        tableBean1.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean1.setImportTargetType(null);
        tableBean1.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean1);

        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA2"}));
        tableBean2.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean2.setUseCache(false);
        tableBean2.setLockType(ImportTableLockType.NONE);
        tableBean2.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean2.setImportTargetType(null);
        tableBean2.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET2", tableBean2);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        boolean result = lock.lock(bean);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     * <p>
     * 正常系：RUNNING_JOBFLOWSテーブルに同一ジョブフロー実行IDが存在するケース
     * 　　　　（ロック取得をスキップして正常終了する）
     *
     * </p>
     */
    @Test
    public void lockTest05() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest05");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL05";

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

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        boolean result = lock.lock(bean);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 正常系：「ロックしない：ロック有無に関わらず処理対象とする」でロックされているレコードを処理対象にするケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：ロックしない
     * 　- ロック済みの場合の挙動：ロック有無に関わらず処理対象とする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest06() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest06");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL06";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition(null);
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.NONE);
        tableBean.setLockedOperation(ImportTableLockedOperation.FORCE);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        boolean result = lock.lock(bean);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 正常系：「行ロック：処理対象から外す」で行ロックされているレコードを処理対象から外すケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET2
     * 　- Import対象カラム：TEXTDATA2
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：行ロック
     * 　- ロック済みの場合の挙動：処理対象から外す
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest07() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest07");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternL07";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA2"}));
        tableBean.setSearchCondition(null);
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.RECORD);
        tableBean.setLockedOperation(ImportTableLockedOperation.OFF);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET2", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "5"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        boolean result = lock.lock(bean);

        // 実行結果の検証
        assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     * <p>
     * 異常系：IMPORT_TABLE_LOCKテーブルのTXロック取得に失敗してリトライの後エラーとなるケース
     *
     * </p>
     */
    @Test
    public void lockTest08() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest08");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL03";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition("TEXTDATA1='testdata1-2'");
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.RECORD);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET3", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 異常系：「テーブルロック：エラーとする」でテーブルロックを取得されているケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：テーブルロック
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest09() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest09");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL09";

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
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 異常系：「テーブルロック：エラーとする」で行ロックを取得されているケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：テーブルロック
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest10() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest10");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL10";

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
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 異常系：「行ロック：エラーとする」でテーブルロックを取得されているケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：INTDATA1=11
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
    public void lockTest11() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest11");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL11";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition("INTDATA1=11");
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.RECORD);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 異常系：「行ロック：エラーとする」で行ロックを取得されているケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：INTDATA1=11
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
    public void lockTest12() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest12");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL12";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition("INTDATA1=11");
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.RECORD);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 異常系：「ロックしない：エラーとする」でテーブルロックを取得されているケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET2
     * 　- Import対象カラム：TEXTDATA2
     * 　- 検索条件：TEXTDATA2=testdata2-3
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：ロックしない
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest13() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest13");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternL11";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition("INTDATA1=11");
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.NONE);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * 異常系：「ロックしない：エラーとする」で行ロックを取得されているケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET2
     * 　- Import対象カラム：TEXTDATA2
     * 　- 検索条件：TEXTDATA2=testdata2-3
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：ロックしない
     * 　- ロック済みの場合の挙動：エラーとする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest14() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest14");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

//        // テストデータの指定
//        String pattern = "patternL12";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition("INTDATA1=11");
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.NONE);
        tableBean.setLockedOperation(ImportTableLockedOperation.ERROR);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        try {
            lock.lock(bean);
            fail();
        } catch (BulkLoaderReRunnableException e) {
            // ok.
        }

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }

    /**
     *
     * <p>
     * insertRunningJobFlowのテストケース
     * 正常系：RUNNING_JOBFLOWSへのインサートが成功するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest15() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest15");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL13";

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        String result = lock.insertRunningJobFlow("target1", "BATCH01", jobflowId, executionId, new Date());

        // 実行結果の検証
       assertNotNull(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
    }
    /**
     *
     * <p>
     * 正常系：「行ロック：処理対象外とする」でテーブルロックを取得されているケース
     * 詳細の設定は以下の通り
     * ・Import対象テーブル：IMPORT_TARGET1
     * 　- Import対象カラム：TEXTDATA1,INTDATA1,DATEDATA1
     * 　- 検索条件：なし
     * 　- キャッシュを利用するかしないか：しない
     * 　- ロック取得タイプ：行ロック
     * 　- ロック済みの場合の挙動：処理対象外とする
     * 　- Import対象テーブルに対応するJavaBeanのクラス名：設定なし
     * 　- Import対象テーブルのデータをHDFS上に書き出す際のファイルパス：設定なし
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void lockTest16() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/TargetDataLockTest/lockTest16");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        //        // テストデータの指定
//        String pattern = "patternL09";

        // ImportBeanを生成
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean = new ImportTargetTableBean();
        tableBean.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean.setSearchCondition(null);
        tableBean.setUseCache(false);
        tableBean.setLockType(ImportTableLockType.RECORD);
        tableBean.setLockedOperation(ImportTableLockedOperation.OFF);
        tableBean.setImportTargetType(null);
        tableBean.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean);
        ImportBean bean = createBean(new String[]{jobflowId, executionId, "20101021221015", "3", "1"}, targetTable);

        // テスト対象クラス実行
        TargetDataLock lock = new TargetDataLock();
        boolean result = lock.lock(bean);

        // 実行結果の検証
       assertTrue(result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
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
        // バッチID
        bean.setBatchId("BATCH01");
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

}
