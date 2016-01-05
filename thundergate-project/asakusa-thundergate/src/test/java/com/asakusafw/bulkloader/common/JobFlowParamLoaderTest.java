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
package com.asakusafw.bulkloader.common;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.runtime.util.VariableTable;

/**
 * DSLParamLoaderのテストクラス
 *
 * @author yuta.shirai
 *
 */
@SuppressWarnings({ "hiding", "deprecation" })
public class JobFlowParamLoaderTest {
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
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, targetName);
        UnitTestUtil.startUp();
    }
    @After
    public void tearDown() throws Exception {
        UnitTestUtil.tearDown();
    }

    /**
     * <p>
     * loadImportParamのテストケース
     * 正常系：Import用のDSLプロパティを読み込むケース（検索条件を置換しない）
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadImportParamTest01() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);
        Map<String, ImportTargetTableBean> importTargetTable = loder.getImportTargetTables();

        // 実行結果を検証
        assertTrue(result);
        // テーブルXXXの検証
        ImportTargetTableBean table1 = importTargetTable.get("XXX");
        // Import対象カラム
        assertEquals(3, table1.getImportTargetColumns().size());
        assertEquals("columnA", table1.getImportTargetColumns().get(0));
        assertEquals("columnB", table1.getImportTargetColumns().get(1));
        assertEquals("columnC", table1.getImportTargetColumns().get(2));
        // 検索条件
        assertEquals("columnA='1' and columnB='2'", table1.getSearchCondition());
        // ロック取得タイプ
        assertEquals(ImportTableLockType.find("1"), table1.getLockType());
        // ロック済みの場合の取り扱い
        assertEquals(ImportTableLockedOperation.find("3"), table1.getLockedOperation());
        // JavaBeansクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportTargetTableBean", table1.getImportTargetType().getName());
        // HDFS上の出力パス
        assertEquals("/${user}/${execution_id}/import_target1", table1.getDfsFilePath());

        // テーブルYYYの検証
        ImportTargetTableBean table2 = importTargetTable.get("YYY");
        // Import対象カラム
        assertEquals(1, table2.getImportTargetColumns().size());
        assertEquals("columnA", table2.getImportTargetColumns().get(0));
        // 検索条件
        assertEquals("columnA='1' or columnB=(select columnB from tableA where column='3')", table2.getSearchCondition());
        // ロック取得タイプ
        assertEquals(ImportTableLockType.find("2"), table2.getLockType());
        // ロック済みの場合の取り扱い
        assertEquals(ImportTableLockedOperation.find("1"), table2.getLockedOperation());
        // JavaBeansクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportBean", table2.getImportTargetType().getName());
        // HDFS上の出力パス
        assertEquals("/asakusa/import/11/YYY", table2.getDfsFilePath());

        // テーブルZZZの検証
        ImportTargetTableBean table3 = importTargetTable.get("ZZZ");
        // Import対象カラム
        assertEquals(2, table3.getImportTargetColumns().size());
        assertEquals("columnA", table3.getImportTargetColumns().get(0));
        assertEquals("columnB", table3.getImportTargetColumns().get(1));
        // 検索条件
        assertNull(table3.getSearchCondition());
        // ロック取得タイプ
        assertEquals(ImportTableLockType.find("3"), table3.getLockType());
        // ロック済みの場合の取り扱い
        assertEquals(ImportTableLockedOperation.find("2"), table3.getLockedOperation());
        // JavaBeansクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ExporterBean", table3.getImportTargetType().getName());
        // HDFS上の出力パス
        assertEquals("/asakusa/import/11/ZZZ", table3.getDfsFilePath());
    }
    /**
     * <p>
     * loadImportParamのテストケース
     * 正常系：Import用のDSLプロパティを読み込むケース（検索条件を置換する）
     * </p>
     * @throws Exception
     */
    @Test
    public void loadImportParamTest02() throws Exception {
        // 環境変数を設定
        VariableTable table = new VariableTable();
        table.defineVariable("user_name", "asakusa");
        table.defineVariable("user_pass", "hadoop");
        Map<String, String> env = new HashMap<String, String>();
        env.put(Constants.THUNDER_GATE_HOME, System.getenv(Constants.THUNDER_GATE_HOME));
        env.put(Constants.ENV_ARGS, table.toSerialString());
        ConfigurationLoader.setEnv(env);

        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import2.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };

        // 実行
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);
        Map<String, ImportTargetTableBean> importTargetTable = loder.getImportTargetTables();

        // 実行結果を検証
        assertTrue(result);
        // テーブルXXXの検証
        ImportTargetTableBean table1 = importTargetTable.get("XXX");
        // 検索条件
        assertEquals("columnA='asakusa' and columnB='hadoop'", table1.getSearchCondition());
    }
    /**
     * <p>
     * loadImportParamのテストケース
     * 正常系：Import対象テーブルが存在しないケース
     * </p>
     * @throws Exception
     */
    @Test
    public void loadImportParamTest03() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import3.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);
        Map<String, ImportTargetTableBean> importTargetTable = loder.getImportTargetTables();

        // 実行結果を検証
        assertTrue(result);
        assertEquals(0, importTargetTable.size());
    }
    /**
     * <p>
     * loadImportParamのテストケース
     * 異常系：Import用のDSLプロパティファイルの読み込みに失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadImportParamTest04() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import4.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);
        Map<String, ImportTargetTableBean> importTargetTable = loder.getImportTargetTables();

        // 実行結果を検証
        assertFalse(result);
        assertNull(importTargetTable);
    }
    /**
     * <p>
     * loadImportParamのテストケース
     * 異常系：検索条件置換文字列に対応する環境変数が存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadImportParamTest05() throws Exception {
        // 環境変数を設定
        VariableTable table = new VariableTable();
        table.defineVariable("user_pass", "hadoop");
        Map<String, String> env = new HashMap<String, String>();
        env.put(Constants.THUNDER_GATE_HOME, System.getenv(Constants.THUNDER_GATE_HOME));
        env.put(Constants.ENV_ARGS, table.toSerialString());
        ConfigurationLoader.setEnv(env);

        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import2.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };

        // 実行
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadImportParamのテストケース
     * 異常系：Import対象テーブルに対応するJavaBeanのクラスが存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadImportParamTest06() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import6.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadImportParamのテストケース
     * 異常系：HDFS上の出力パスが有効でないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadImportParamTest07() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import7.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadImportParamのテストケース
     * 正常系：読み込んだ設定がテーブル名でソートされる事の確認
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadImportParamTest08() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import8.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadImportParam(targetName, "batch01", "11", true);
        Map<String, ImportTargetTableBean> importTargetTable = loder.getImportTargetTables();

        // 実行結果を検証
        assertTrue(result);
        Iterator<String> it = importTargetTable.keySet().iterator();
        assertEquals("XXX", it.next());
        assertEquals("YYY", it.next());
        assertEquals("ZZZ", it.next());
    }
    /**
     * <p>
     * loadExportParamのテストケース
     * 正常系：複数のファイルを含むZIPファイル送信するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadExportParamTest01() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/export1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadExportParam(targetName, "batch01", "21");
        Map<String, ExportTargetTableBean> exportTargetTable = loder.getExportTargetTables();

        // 実行結果を検証
        assertTrue(result);

        // テーブルXXXの検証
        ExportTargetTableBean table1 = exportTargetTable.get("XXX");
        // 重複チェックを行うか否か
        assertTrue(table1.isDuplicateCheck());
        // Export対象テーブルに対応する異常データテーブル
        assertEquals("XXX_ERROR", table1.getErrorTableName());
        // Export中間TSVファイルに対応するカラム名
        assertEquals(5, table1.getExportTsvColumn().size());
        assertEquals("columnA", table1.getExportTsvColumn().get(0));
        assertEquals("columnB", table1.getExportTsvColumn().get(1));
        assertEquals("columnC", table1.getExportTsvColumn().get(2));
        assertEquals("columnD", table1.getExportTsvColumn().get(3));
        assertEquals("columnE", table1.getExportTsvColumn().get(4));
        // Export対象テーブルのカラム名
        assertEquals(2, table1.getExportTableColumns().size());
        assertEquals("columnA", table1.getExportTableColumns().get(0));
        assertEquals("columnB", table1.getExportTableColumns().get(1));
        // 異常データテーブルのカラム名
        assertEquals(5, table1.getErrorTableColumns().size());
        assertEquals("columnA", table1.getErrorTableColumns().get(0));
        assertEquals("columnB", table1.getErrorTableColumns().get(1));
        assertEquals("columnC", table1.getErrorTableColumns().get(2));
        assertEquals("columnD", table1.getErrorTableColumns().get(3));
        assertEquals("columnE", table1.getErrorTableColumns().get(4));
        // キー項目のカラム名
        assertEquals(1, table1.getKeyColumns().size());
        assertEquals("columnA", table1.getKeyColumns().get(0));
        // エラーコードを格納するカラム名
        assertEquals("columnF", table1.getErrorCodeColumn());
        // 重複チェックエラーのエラーコードの値
        assertEquals("ER01", table1.getErrorCode());
        // Export対象テーブルに対応するJavaBeanのクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportTargetTableBean", table1.getExportTargetType().getName());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> path1 = table1.getDfsFilePaths();
        assertEquals(2, path1.size());
        assertEquals("/asakusa/import/11/XXX_1", path1.get(0));
        assertEquals("/asakusa/import/11/XXX_2", path1.get(1));

        // テーブルYYYの検証
        ExportTargetTableBean table2 = exportTargetTable.get("YYY");
        // 重複チェックを行うか否か
        assertFalse(table2.isDuplicateCheck());
        // Export対象テーブルに対応する異常データテーブル
        assertNull(table2.getErrorTableName());
        // Export中間TSVファイルに対応するカラム名
        assertEquals(3, table2.getExportTsvColumn().size());
        assertEquals("columnA", table2.getExportTsvColumn().get(0));
        assertEquals("columnB", table2.getExportTsvColumn().get(1));
        assertEquals("columnC", table2.getExportTsvColumn().get(2));
        // Export対象テーブルのカラム名
        assertEquals(3, table2.getExportTableColumns().size());
        assertEquals("columnA", table2.getExportTableColumns().get(0));
        assertEquals("columnB", table2.getExportTableColumns().get(1));
        assertEquals("columnC", table2.getExportTableColumns().get(2));
        // 異常データテーブルのカラム名
        assertEquals(0, table2.getErrorTableColumns().size());
        // キー項目のカラム名
        assertEquals(0, table2.getKeyColumns().size());
        // エラーコードを格納するカラム名
        assertNull(table2.getErrorCodeColumn());
        // 重複チェックエラーのエラーコードの値
        assertNull(table2.getErrorCode());
        // Export対象テーブルに対応するJavaBeanのクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportBean", table2.getExportTargetType().getName());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> path2 = table2.getDfsFilePaths();
        assertEquals(1, path2.size());
        assertEquals("/asakusa/import/11/YYY", path2.get(0));

        // テーブルZZZの検証
        ExportTargetTableBean table3 = exportTargetTable.get("ZZZ");
        // 重複チェックを行うか否か
        assertFalse(table3.isDuplicateCheck());
        // Export対象テーブルに対応する異常データテーブル
        assertNull(table3.getErrorTableName());
        // Export中間TSVファイルに対応するカラム名
        assertEquals(1, table3.getExportTsvColumn().size());
        assertEquals("columnA", table3.getExportTsvColumn().get(0));
        // Export対象テーブルのカラム名
        assertEquals(1, table3.getExportTableColumns().size());
        assertEquals("columnA", table3.getExportTableColumns().get(0));
        // 異常データテーブルのカラム名
        assertEquals(0, table3.getErrorTableColumns().size());
        // キー項目のカラム名
        assertEquals(0, table3.getKeyColumns().size());
        // エラーコードを格納するカラム名
        assertNull(table3.getErrorCodeColumn());
        // 重複チェックエラーのエラーコードの値
        assertNull(table3.getErrorCode());
        // Export対象テーブルに対応するJavaBeanのクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ExporterBean", table3.getExportTargetType().getName());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> path3 = table3.getDfsFilePaths();
        assertEquals(1, path3.size());
        assertEquals("/asakusa/import/11/ZZZ", path3.get(0));
    }
    /**
     * <p>
     * loadExportParamのテストケース
     * 正常系：Export対象テーブルが存在しないケース
     * </p>
     * @throws Exception
     */
    @Test
    public void loadExportParamTest02() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/export2.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadExportParam(targetName, "batch01", "21");
        Map<String, ExportTargetTableBean> exportTargetTable = loder.getExportTargetTables();

        // 実行結果を検証
        assertTrue(result);
        assertEquals(0, exportTargetTable.size());
    }
    /**
     * <p>
     * loadExportParamのテストケース
     * 異常系：Export用のDSLプロパティファイルの読み込みに失敗するケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadExportParamTest03() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/export3.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadExportParam(targetName, "batch01", "21");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadExportParamのテストケース
     * 異常系：Export対象テーブルに対応するJavaBeanのクラスが存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadExportParamTest04() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/export4.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadExportParam(targetName, "batch01", "21");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadExportParamのテストケース
     * 異常系：HDFS上の出力パスが有効でないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadExportParamTest05() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/export5.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadExportParam(targetName, "batch01", "21");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadExportParamのテストケース
     * 正常系：読み込んだ設定がテーブル名でソートされる事の確認
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadExportParamTest06() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/export6.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadExportParam(targetName, "batch01", "21");
        Map<String, ExportTargetTableBean> exportTargetTable = loder.getExportTargetTables();

        // 実行結果を検証
        assertTrue(result);
        Iterator<String> it = exportTargetTable.keySet().iterator();
        assertEquals("XXX", it.next());
        assertEquals("YYY", it.next());
        assertEquals("ZZZ", it.next());
    }

    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 正常系：全てのチェックに成功
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest01() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("1"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("3"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA2"}));
        tableBean2.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean2.setUseCache(false);
        tableBean2.setLockType(ImportTableLockType.find("2"));
        tableBean2.setLockedOperation(ImportTableLockedOperation.find("1"));
        tableBean2.setImportTargetType(this.getClass());
        tableBean2.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET2", tableBean2);

        ImportTargetTableBean tableBean3 = new ImportTargetTableBean();
        tableBean3.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA3"}));
        tableBean3.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean3.setUseCache(false);
        tableBean3.setLockType(ImportTableLockType.find("2"));
        tableBean3.setLockedOperation(ImportTableLockedOperation.find("3"));
        tableBean3.setImportTargetType(this.getClass());
        tableBean3.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET3", tableBean3);

        ImportTargetTableBean tableBean4 = new ImportTargetTableBean();
        tableBean4.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA3"}));
        tableBean4.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean4.setUseCache(false);
        tableBean4.setLockType(ImportTableLockType.find("3"));
        tableBean4.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean4.setImportTargetType(this.getClass());
        tableBean4.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET4", tableBean4);

        ImportTargetTableBean tableBean5 = new ImportTargetTableBean();
        tableBean5.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA3"}));
        tableBean5.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean5.setUseCache(false);
        tableBean5.setLockType(ImportTableLockType.find("3"));
        tableBean5.setLockedOperation(ImportTableLockedOperation.find("3"));
        tableBean5.setImportTargetType(this.getClass());
        tableBean5.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET5", tableBean5);

        // テスト実行
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertTrue(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：Import対象テーブルに対するImport対象カラムが設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest02() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.NONE);
        tableBean1.setLockedOperation(ImportTableLockedOperation.FORCE);
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA2"}));
        tableBean2.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean2.setUseCache(false);
        tableBean2.setLockType(ImportTableLockType.find("1"));
        tableBean2.setLockedOperation(ImportTableLockedOperation.find("3"));
        tableBean2.setImportTargetType(this.getClass());
        tableBean2.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET2", tableBean2);


        // テスト実行1
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", null}));

        // テスト実行2
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        tableBean2.setImportTargetColumns(Arrays.asList(new String[]{""}));

        // テスト実行3
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：Import対象テーブルに対するロック取得のタイプが設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest03() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("0"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("1"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行1
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setLockType(null);

        // テスト実行2
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：Import対象テーブルに対するロック済みの場合の取り扱いが設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest04() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("1"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("0"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行1
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setLockedOperation(null);

        // テスト実行2
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：Import対象テーブルに対するJavaBeansのクラス名が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest05() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("1"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("3"));
        tableBean1.setImportTargetType(null);
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：Import対象テーブルに対するHDFS上に書き出す際のファイルパスが設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest06() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("1"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("3"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath(null);
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：ロック取得のタイプとロック済みの場合の取り扱いの組合せが不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest07() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("1"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("1"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行1
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setLockedOperation(null);

        // テスト実行2
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：ロック取得のタイプとロック済みの場合の取り扱いの組合せが不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest08() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("1"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行1
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setLockedOperation(null);

        // テスト実行2
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：ロック取得のタイプとロック済みの場合の取り扱いの組合せが不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest09() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("2"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行1
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setLockedOperation(null);

        // テスト実行2
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：ロック取得のタイプとロック済みの場合の取り扱いの組合せが不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest10() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("3"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("1"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行1
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);

        // 設定変更
        tableBean1.setLockedOperation(null);

        // テスト実行2
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", true);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 正常系：サブ起動のケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest11() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("3"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        ImportTargetTableBean tableBean2 = new ImportTargetTableBean();
        tableBean2.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA2"}));
        tableBean2.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean2.setUseCache(false);
        tableBean2.setLockType(ImportTableLockType.find("3"));
        tableBean2.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean2.setImportTargetType(this.getClass());
        tableBean2.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET2", tableBean2);

        ImportTargetTableBean tableBean3 = new ImportTargetTableBean();
        tableBean3.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA3"}));
        tableBean3.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean3.setUseCache(false);
        tableBean3.setLockType(ImportTableLockType.find("3"));
        tableBean3.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean3.setImportTargetType(this.getClass());
        tableBean3.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET3", tableBean3);

        ImportTargetTableBean tableBean4 = new ImportTargetTableBean();
        tableBean4.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA3"}));
        tableBean4.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean4.setUseCache(false);
        tableBean4.setLockType(ImportTableLockType.find("3"));
        tableBean4.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean4.setImportTargetType(this.getClass());
        tableBean4.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET4", tableBean4);

        ImportTargetTableBean tableBean5 = new ImportTargetTableBean();
        tableBean5.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA3"}));
        tableBean5.setSearchCondition("TEXTDATA2='testdata2-3'");
        tableBean5.setUseCache(false);
        tableBean5.setLockType(ImportTableLockType.find("3"));
        tableBean5.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean5.setImportTargetType(this.getClass());
        tableBean5.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET5", tableBean5);

        // テスト実行
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", false);
        assertTrue(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：サブ起動でロック取得タイプが「取得しない」以外のケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest12() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("2"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("2"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", false);
        assertFalse(result);

        tableBean1.setLockType(ImportTableLockType.find("1"));
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", false);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkImportParamのテストケース
     * 異常系：サブ起動でロック取得タイプが「取得しない」以外のケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkImportParamTest13() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ImportTargetTableBean> targetTable = new LinkedHashMap<String, ImportTargetTableBean>();
        ImportTargetTableBean tableBean1 = new ImportTargetTableBean();
        tableBean1.setImportTargetColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        tableBean1.setSearchCondition("INTDATA1=11");
        tableBean1.setUseCache(false);
        tableBean1.setLockType(ImportTableLockType.find("3"));
        tableBean1.setLockedOperation(ImportTableLockedOperation.find("3"));
        tableBean1.setImportTargetType(this.getClass());
        tableBean1.setDfsFilePath("hdfs://localhost/user/asakusa/import/11/XXX_1");
        targetTable.put("IMPORT_TARGET1", tableBean1);

        // テスト実行
        boolean result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", false);
        assertFalse(result);

        tableBean1.setLockedOperation(ImportTableLockedOperation.find("1"));
        result = loader.checkImportParam(targetTable, targetName, "1", "dummyFileName", false);
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 正常系：全てのチェックに成功(重複チェックなし)
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest01() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(false);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName(null);
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn(null);
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode(null);
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        ExportTargetTableBean table2 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table2.setDuplicateCheck(false);
        // Export対象テーブルに対応する異常データテーブル
        table2.setErrorTableName(null);
        // Export中間TSVファイルに対応するカラム名
        table2.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table2.setErrorTableColumns(Arrays.asList(new String[]{}));
        // キー項目のカラム名
        table2.setKeyColumns(Arrays.asList(new String[]{}));
        // エラーコードを格納するカラム名
        table2.setErrorCodeColumn(null);
        // 重複チェックエラーのエラーコードの値
        table2.setErrorCode(null);
        // Export対象テーブルに対応するJavaBeanのクラス名
        table2.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list2 = new ArrayList<String>();
        list2.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table2.setDfsFilePaths(list2);
        targetTable.put("EXP_TARGET2", table2);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertTrue(result);

        // テスト実行2
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertTrue(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 正常系：全てのチェックに成功(重複チェックあり)
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest02() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        ExportTargetTableBean table2 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table2.setDuplicateCheck(false);
        // Export対象テーブルに対応する異常データテーブル
        table2.setErrorTableName(null);
        // Export中間TSVファイルに対応するカラム名
        table2.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table2.setErrorTableColumns(Arrays.asList(new String[]{}));
        // キー項目のカラム名
        table2.setKeyColumns(Arrays.asList(new String[]{}));
        // エラーコードを格納するカラム名
        table2.setErrorCodeColumn(null);
        // 重複チェックエラーのエラーコードの値
        table2.setErrorCode(null);
        // Export対象テーブルに対応するJavaBeanのクラス名
        table2.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list2 = new ArrayList<String>();
        list2.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table2.setDfsFilePaths(list2);
        targetTable.put("EXP_TARGET2", table2);

        // テスト実行
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertTrue(result);
    }

    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：Export中間TSVファイルに対応するカラム名が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest03() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(null);
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", null}));

        // テスト実行2
        result = loader.checkExportParam(targetTable, "1", targetName, "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", ""}));

        // テスト実行3
        result = loader.checkExportParam(targetTable, "1", targetName, "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setExportTsvColumns(Arrays.asList(new String[]{}));

        // テスト実行4
        result = loader.checkExportParam(targetTable, "1", targetName, "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：Export対象テーブルのカラム名が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest04() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", null}));

        // テスト実行2
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", ""}));

        // テスト実行3
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：異常データテーブルのカラム名が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest05() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", null}));

        // テスト実行2
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", ""}));

        // テスト実行3
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：キー項目のカラム名が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest06() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", null}));

        // テスト実行2
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", ""}));

        // テスト実行3
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：エラーコードを格納するカラム名が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest07() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn(null);
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setErrorCodeColumn("");

        // テスト実行2
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：エラーコードのカラムがExport対象テーブルにコピーするカラムに含まれているケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest08() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "ERROR_CODE"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：エラーコードのカラムが異常データテーブルにコピーするカラムに含まれているケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest09() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1", "ERROR_CODE"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：重複チェックエラーのエラーコードの値が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest10() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode(null);
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        table1.setErrorCode("");

        // テスト実行2
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：Export対象テーブルに対応するJavaBeansのクラス名が設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest11() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        table1.setExportTargetType(this.getClass());
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        table2.setExportTargetType(null);
        List<String> list2 = new ArrayList<String>();
        list2.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table2.setDfsFilePaths(list2);
        targetTable.put("EXP_TARGET2", table2);

        // テスト実行
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：Export対象データのHDFS上のパスが設定されていないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest12() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        table1.setExportTargetType(this.getClass());
        targetTable.put("EXP_TARGET1", table1);
        ExportTargetTableBean table2 = new ExportTargetTableBean();
        table2.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        table2.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        table2.setExportTargetType(this.getClass());
        List<String> list2 = new ArrayList<String>();
        list2.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table2.setDfsFilePaths(list2);
        targetTable.put("EXP_TARGET2", table2);

        // テスト実行1
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);

        // 設定変更
        List<String> list1 = new ArrayList<String>();
        list1.add(null);
        table1.setDfsFilePaths(list1);

        // テスト実行2
        result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：Export対象カラムにテンポラリSIDと同一のカラム名が含まれるケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest13() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(true);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName("XXX_ERROR");
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "TEMP_SID"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"INTDATA1"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{"TEXTDATA1"}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn("ERROR_CODE");
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode("ERR01");
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：Export対象カラムがTSVカラムに含まれないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest14() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(false);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName(null);
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA2"}));
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn(null);
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode(null);
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     *
     * <p>
     * checkExportParamのテストケース
     * 異常系：Export対象カラムがTSVカラムに含まれないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void checkExportParamTest15() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader();

        // 設定
        Map<String, ExportTargetTableBean> targetTable = new LinkedHashMap<String, ExportTargetTableBean>();
        ExportTargetTableBean table1 = new ExportTargetTableBean();
        // 重複チェックを行うか否か
        table1.setDuplicateCheck(false);
        // Export対象テーブルに対応する異常データテーブル
        table1.setErrorTableName(null);
        // Export中間TSVファイルに対応するカラム名
        table1.setExportTsvColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "DATEDATA1"}));
        // Export対象テーブルのカラム名
        table1.setExportTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1"}));
        // 異常データテーブル名
        table1.setErrorTableName("ERROR_TABLE");
        // 重複チェック有り
        table1.setDuplicateCheck(true);
        // 異常データテーブルのカラム名
        table1.setErrorTableColumns(Arrays.asList(new String[]{"TEXTDATA1", "INTDATA1", "ERROR_INFO"}));
        // キー項目のカラム名
        table1.setKeyColumns(Arrays.asList(new String[]{}));
        // エラーコードを格納するカラム名
        table1.setErrorCodeColumn(null);
        // 重複チェックエラーのエラーコードの値
        table1.setErrorCode(null);
        // Export対象テーブルに対応するJavaBeanのクラス名
        table1.setExportTargetType(this.getClass());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> list1 = new ArrayList<String>();
        list1.add("hdfs://localhost/user/asakusa/import/11/XXX_1");
        table1.setDfsFilePaths(list1);
        targetTable.put("EXP_TARGET1", table1);

        // テスト実行
        boolean result = loader.checkExportParam(targetTable, targetName, "1", "dummyFileName");
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 正常系：Import用/Export用のDSLプロパティを読み込むケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam01() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/import1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/export1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");
        Map<String, ImportTargetTableBean> importTargetTable = loder.getImportTargetTables();
        Map<String, ExportTargetTableBean> exportTargetTable = loder.getExportTargetTables();

        // 実行結果を検証
        assertTrue(result);

        // Import設定の検証
        // 実行結果を検証
        assertTrue(result);
        // テーブルXXXの検証
        ImportTargetTableBean impTable1 = importTargetTable.get("XXX");
        // Import対象カラム
        assertEquals(3, impTable1.getImportTargetColumns().size());
        assertEquals("columnA", impTable1.getImportTargetColumns().get(0));
        assertEquals("columnB", impTable1.getImportTargetColumns().get(1));
        assertEquals("columnC", impTable1.getImportTargetColumns().get(2));
        // 検索条件
        assertEquals("columnA='1' and columnB='2'", impTable1.getSearchCondition());
        // ロック取得タイプ
        assertEquals(ImportTableLockType.find("1"), impTable1.getLockType());
        // ロック済みの場合の取り扱い
        assertEquals(ImportTableLockedOperation.find("3"), impTable1.getLockedOperation());
        // JavaBeansクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportTargetTableBean", impTable1.getImportTargetType().getName());
        // HDFS上の出力パス
        assertEquals("/${user}/${execution_id}/import_target1", impTable1.getDfsFilePath());

        // テーブルYYYの検証
        ImportTargetTableBean impTable2 = importTargetTable.get("YYY");
        // Import対象カラム
        assertEquals(1, impTable2.getImportTargetColumns().size());
        assertEquals("columnA", impTable2.getImportTargetColumns().get(0));
        // 検索条件
        assertEquals("columnA='1' or columnB=(select columnB from tableA where column='3')", impTable2.getSearchCondition());
        // キャッシュ利用有無
        assertEquals(false, impTable2.isUseCache());
        // ロック取得タイプ
        assertEquals(ImportTableLockType.find("2"), impTable2.getLockType());
        // ロック済みの場合の取り扱い
        assertEquals(ImportTableLockedOperation.find("1"), impTable2.getLockedOperation());
        // JavaBeansクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportBean", impTable2.getImportTargetType().getName());
        // HDFS上の出力パス
        assertEquals("/asakusa/import/11/YYY", impTable2.getDfsFilePath());

        // テーブルZZZの検証
        ImportTargetTableBean impTable3 = importTargetTable.get("ZZZ");
        // Import対象カラム
        assertEquals(2, impTable3.getImportTargetColumns().size());
        assertEquals("columnA", impTable3.getImportTargetColumns().get(0));
        assertEquals("columnB", impTable3.getImportTargetColumns().get(1));
        // 検索条件
        assertNull(impTable3.getSearchCondition());
        // キャッシュ利用有無
        assertEquals(false, impTable3.isUseCache());
        // ロック取得タイプ
        assertEquals(ImportTableLockType.find("3"), impTable3.getLockType());
        // ロック済みの場合の取り扱い
        assertEquals(ImportTableLockedOperation.find("2"), impTable3.getLockedOperation());
        // JavaBeansクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ExporterBean", impTable3.getImportTargetType().getName());
        // HDFS上の出力パス
        assertEquals("/asakusa/import/11/ZZZ", impTable3.getDfsFilePath());

        // Export設定の検証
        // テーブルXXXの検証
        ExportTargetTableBean expTable1 = exportTargetTable.get("XXX");
        // 重複チェックを行うか否か
        assertTrue(expTable1.isDuplicateCheck());
        // Export対象テーブルに対応する異常データテーブル
        assertEquals("XXX_ERROR", expTable1.getErrorTableName());
        // Export中間TSVファイルに対応するカラム名
        assertEquals(5, expTable1.getExportTsvColumn().size());
        assertEquals("columnA", expTable1.getExportTsvColumn().get(0));
        assertEquals("columnB", expTable1.getExportTsvColumn().get(1));
        assertEquals("columnC", expTable1.getExportTsvColumn().get(2));
        assertEquals("columnD", expTable1.getExportTsvColumn().get(3));
        assertEquals("columnE", expTable1.getExportTsvColumn().get(4));
        // Export対象テーブルのカラム名
        assertEquals(2, expTable1.getExportTableColumns().size());
        assertEquals("columnA", expTable1.getExportTableColumns().get(0));
        assertEquals("columnB", expTable1.getExportTableColumns().get(1));
        // 異常データテーブルのカラム名
        assertEquals(5, expTable1.getErrorTableColumns().size());
        assertEquals("columnA", expTable1.getErrorTableColumns().get(0));
        assertEquals("columnB", expTable1.getErrorTableColumns().get(1));
        assertEquals("columnC", expTable1.getErrorTableColumns().get(2));
        assertEquals("columnD", expTable1.getErrorTableColumns().get(3));
        assertEquals("columnE", expTable1.getErrorTableColumns().get(4));
        // キー項目のカラム名
        assertEquals(1, expTable1.getKeyColumns().size());
        assertEquals("columnA", expTable1.getKeyColumns().get(0));
        // エラーコードを格納するカラム名
        assertEquals("columnF", expTable1.getErrorCodeColumn());
        // 重複チェックエラーのエラーコードの値
        assertEquals("ER01", expTable1.getErrorCode());
        // Export対象テーブルに対応するJavaBeanのクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportTargetTableBean", expTable1.getExportTargetType().getName());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> path1 = expTable1.getDfsFilePaths();
        assertEquals(2, path1.size());
        assertEquals("/asakusa/import/11/XXX_1", path1.get(0));
        assertEquals("/asakusa/import/11/XXX_2", path1.get(1));

        // テーブルYYYの検証
        ExportTargetTableBean expTable2 = exportTargetTable.get("YYY");
        // 重複チェックを行うか否か
        assertFalse(expTable2.isDuplicateCheck());
        // Export対象テーブルに対応する異常データテーブル
        assertNull(expTable2.getErrorTableName());
        // Export中間TSVファイルに対応するカラム名
        assertEquals(3, expTable2.getExportTsvColumn().size());
        assertEquals("columnA", expTable2.getExportTsvColumn().get(0));
        assertEquals("columnB", expTable2.getExportTsvColumn().get(1));
        assertEquals("columnC", expTable2.getExportTsvColumn().get(2));
        // Export対象テーブルのカラム名
        assertEquals(3, expTable2.getExportTableColumns().size());
        assertEquals("columnA", expTable2.getExportTableColumns().get(0));
        assertEquals("columnB", expTable2.getExportTableColumns().get(1));
        assertEquals("columnC", expTable2.getExportTableColumns().get(2));
        // 異常データテーブルのカラム名
        assertEquals(0, expTable2.getErrorTableColumns().size());
        // キー項目のカラム名
        assertEquals(0, expTable2.getKeyColumns().size());
        // エラーコードを格納するカラム名
        assertNull(expTable2.getErrorCodeColumn());
        // 重複チェックエラーのエラーコードの値
        assertNull(expTable2.getErrorCode());
        // Export対象テーブルに対応するJavaBeanのクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ImportBean", expTable2.getExportTargetType().getName());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> path2 = expTable2.getDfsFilePaths();
        assertEquals(1, path2.size());
        assertEquals("/asakusa/import/11/YYY", path2.get(0));

        // テーブルZZZの検証
        ExportTargetTableBean expTable3 = exportTargetTable.get("ZZZ");
        // 重複チェックを行うか否か
        assertFalse(expTable3.isDuplicateCheck());
        // Export対象テーブルに対応する異常データテーブル
        assertNull(expTable3.getErrorTableName());
        // Export中間TSVファイルに対応するカラム名
        assertEquals(1, expTable3.getExportTsvColumn().size());
        assertEquals("columnA", expTable3.getExportTsvColumn().get(0));
        // Export対象テーブルのカラム名
        assertEquals(1, expTable3.getExportTableColumns().size());
        assertEquals("columnA", expTable3.getExportTableColumns().get(0));
        // 異常データテーブルのカラム名
        assertEquals(0, expTable3.getErrorTableColumns().size());
        // キー項目のカラム名
        assertEquals(0, expTable3.getKeyColumns().size());
        // エラーコードを格納するカラム名
        assertNull(expTable3.getErrorCodeColumn());
        // 重複チェックエラーのエラーコードの値
        assertNull(expTable3.getErrorCode());
        // Export対象テーブルに対応するJavaBeanのクラス名
        assertEquals("com.asakusafw.bulkloader.bean.ExporterBean", expTable3.getExportTargetType().getName());
        // Export対象テーブルのデータをHDFS上に書き出す際のファイルパス
        List<String> path3 = expTable3.getDfsFilePaths();
        assertEquals(1, path3.size());
        assertEquals("/asakusa/import/11/ZZZ", path3.get(0));

    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 正常系：最低限の設定を読み込むケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam02() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
//                prop.setProperty("tableA.error-table", "tableB");
//                prop.setProperty("tableA.error-table-column", "TEXT");
//                prop.setProperty("tableA.error-column", "ERR");
//                prop.setProperty("tableA.error-table-column", "TEXT");
//                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertTrue(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 正常系：Import/Export対象テーブルが存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam03() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertTrue(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：Import対象テーブルに対応するJavaBeanのクラスが存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam04() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                prop.setProperty("tableA.bean-name", "com.dummy.Dummy");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：Export対象テーブルに対応するJavaBeanのクラスが存在しないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam05() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-code", "99");
                prop.setProperty("tableA.bean-name", "com.dummy.Dummy");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：Export中間TSVファイルに対応するカラム名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam06() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
//                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：Export対象テーブルのカラム名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam07() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
//                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：異常データテーブルのカラム名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam08() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
//                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：エラーコードを格納するカラム名が設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam09() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT");
//                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：エラーコードのカラムがExport対象テーブルにコピーするカラムに含まれているケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam10() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT");
                prop.setProperty("tableA.error-column", "NUMBER");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：エラーコードのカラムが異常データテーブルにコピーするカラムに含まれているケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam11() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT2");
                prop.setProperty("tableA.error-column", "TEXT2");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：重複チェックエラーのエラーコードが設定されていないケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam12() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT2");
                prop.setProperty("tableA.error-column", "ERR");
//                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }
    /**
     * <p>
     * loadRecoveryParamのテストケース
     * 異常系：テンポラリSIDと同一のカラム名がExport対象テーブルのカラム名に設定されているケース
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadRecoveryParam13() throws Exception {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("import.target-table", "tableA,tableB");
                return prop;
            }
            @Override
            protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                Properties prop = new Properties();
                prop.setProperty("export.target-table", "tableA");
                prop.setProperty("tableA.export-table-column", "TEXT,NUMBER,TEMP_SID");
                prop.setProperty("tableA.tsv-column", "TEXT,NUMBER");
                prop.setProperty("tableA.error-table", "tableB");
                prop.setProperty("tableA.error-table-column", "TEXT2");
                prop.setProperty("tableA.error-column", "ERR");
                prop.setProperty("tableA.error-code", "99");

                return prop;
            }
        };
        boolean result = loder.loadRecoveryParam(targetName, "batch01", "11");

        // 実行結果を検証
        assertFalse(result);
    }

    /**
     * Test for {@link JobFlowParamLoader#loadCacheBuildParam(String, String, String)}.
     * @throws Exception if failed
     */
    @Test
    public void loadCacheBuildParam() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/cache-build.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loader.loadCacheBuildParam(targetName, "batch01", "11");
        assertThat(result, is(true));
        assertThat(loader.getImportTargetTables().size(), is(2));
        ImportTargetTableBean target = loader.getImportTargetTables().get("XXX");
        assertThat(target.getCacheId(), is(notNullValue()));
        assertThat(target.getLockType(), is(ImportTableLockType.NONE));
    }

    /**
     * Test for {@link JobFlowParamLoader#loadCacheBuildParam(String, String, String)}.
     * @throws Exception if failed
     */
    @Test
    public void loadCacheBuildParam_invalid() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/cache-build_invalid.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loader.loadCacheBuildParam(targetName, "batch01", "11");
        assertThat(result, is(false));
    }

    /**
     * Test for {@link JobFlowParamLoader#loadExtractParam(String, String, String)}.
     * @throws Exception if failed
     */
    @Test
    public void loadExtractParam() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/cache-build.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loader.loadExtractParam(targetName, "batch01", "11");
        assertThat(result, is(true));
        assertThat(loader.getImportTargetTables().size(), is(2));
    }

    /**
     * Test for {@link JobFlowParamLoader#loadExtractParam(String, String, String)}.
     * @throws Exception if failed
     */
    @Test
    public void loadExtractParam_invalid() throws Exception {
        JobFlowParamLoader loader = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                System.out.println(dslFile);
                File propFile = new File("src/test/data/common/cache-build_invalid.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        boolean result = loader.loadExtractParam(targetName, "batch01", "11");
        assertThat(result, is(false));
    }
}
