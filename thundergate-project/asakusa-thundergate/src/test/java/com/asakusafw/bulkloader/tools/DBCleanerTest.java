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
package com.asakusafw.bulkloader.tools;


import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.inspect.Cause;

/**
 * DBCleanerのテストクラス
 * @author yuta.shirai
 *
 */
public class DBCleanerTest {
    /** 読み込むプロパティファイル */
    private static List<String> propertys = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    /** ジョブフローID */
    private static String jobflowId = "JOB_FLOW01";
    /** ジョブフロー実行ID */
    private static String executionId = "JOB_FLOW01-001";
    /** ターゲット名 */
    private static String targetName = "target1";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UnitTestUtil.setUpBeforeClass();
        UnitTestUtil.setUpEnv();
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, propertys, targetName);
        UnitTestUtil.setUpDB();
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        UnitTestUtil.setUpEnv();
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
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
     * executeメソッドのテストケース
     *
     * 正常系：クリーニングに成功するケース（削除対象データあり）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/dbCleaner/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);
        createTempTable1();
        createTempTable2();

        // 処理の実行
        String[] args = new String[]{targetName};
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // テーブルの存在チェック
        assertFalse(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET1"));
        assertFalse(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET1_DF"));
        assertFalse(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET2"));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：クリーニングに成功するケース（削除対象データなし）
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/dbCleaner/executeTest02");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);
        createTempTable1();
        createTempTable2();

        // 処理の実行
        String[] args = new String[]{targetName};
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // テーブルの存在チェック
        assertTrue(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET1"));
        assertTrue(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET1_DF"));
        assertTrue(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET2"));

        dropTable();
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：引数が不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest03() throws Exception {
        // 処理の実行
        String[] args = new String[]{""};
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        // 処理の実行
        result = cleaner.execute(new String[]{});

        // 実行結果の検証
        assertEquals(1, result);

    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：環境変数の設定が不正なケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest04() throws Exception {
        Properties p = System.getProperties();
        p.setProperty(Constants.ASAKUSA_HOME, "hoge");
        ConfigurationLoader.setSysProp(p);

        // 処理の実行
        String[] args = new String[]{targetName};
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        p.setProperty(Constants.ASAKUSA_HOME, "src");
        ConfigurationLoader.setSysProp(p);
        System.setProperties(p);


    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：JDBCプロパティの読み込みに失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest05() throws Exception {
        // 処理の実行
        String[] args = new String[]{"dummy"};
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：コネクションの取得に失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest06() throws Exception {
        // 処理の実行
        String[] args = new String[]{"fail"};
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        // 正しいJDBCプロパティを読み込む為のダミー実行
        // tearDownAfterClass()を成功させるため
        args = new String[]{targetName};
        cleaner.execute(args);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：処理中にSQL例外が発生するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest07() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/dbCleaner/executeTest07");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);
        createTempTable1();
        createTempTable2();

        // 処理の実行
        String[] args = new String[]{targetName};
        DBCleaner cleaner = new DBCleaner();
        int result = cleaner.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }
        // テーブルの存在チェック
        assertFalse(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET1"));
        assertFalse(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET1_DF"));
        assertFalse(UnitTestUtil.isExistTable("TEMP_IMPORT_TARGET2"));
    }
    private void createTempTable1() throws Exception {
        String dropTemp1Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String dropDup1Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1_DF";

        StringBuilder temp1Sql = new StringBuilder();
        temp1Sql.append("CREATE  TABLE `TEMP_IMPORT_TARGET1` (");
        temp1Sql.append("  `TEMP_SID` BIGINT AUTO_INCREMENT,");
        temp1Sql.append("  `SID` BIGINT NULL,");
        temp1Sql.append("  `VERSION_NO` BIGINT NULL,");
        temp1Sql.append("  `TEXTDATA1` VARCHAR(45) NULL ,");
        temp1Sql.append("  `INTDATA1` INT NULL ,");
        temp1Sql.append("  `DATEDATA1` DATETIME NULL ,");
        temp1Sql.append("  `RGST_DATE` DATETIME NULL ,");
        temp1Sql.append("  `UPDT_DATE` DATETIME NULL ,");
          temp1Sql.append("`DUPLICATE_FLG` char(1) NULL ,");
        temp1Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;");

        StringBuilder dup1Sql = new StringBuilder();
        dup1Sql.append("CREATE  TABLE `TEMP_IMPORT_TARGET1_DF` (");
        dup1Sql.append("  `TEMP_SID` BIGINT,");
        dup1Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");

        UnitTestUtil.executeUpdate(dropTemp1Sql);
        UnitTestUtil.executeUpdate(dropDup1Sql);
        UnitTestUtil.executeUpdate(temp1Sql.toString());
        UnitTestUtil.executeUpdate(dup1Sql.toString());
    }
    private void createTempTable2() throws Exception {
        String dropTemp2Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";

        StringBuilder temp2Sql = new StringBuilder();
        temp2Sql.append("CREATE  TABLE `TEMP_IMPORT_TARGET2` (");
        temp2Sql.append("  `TEMP_SID` BIGINT AUTO_INCREMENT,");
        temp2Sql.append("  `SID` BIGINT NULL,");
        temp2Sql.append("  `VERSION_NO` BIGINT NULL,");
        temp2Sql.append("  `TEXTDATA2` VARCHAR(45) NULL ,");
        temp2Sql.append("  `INTDATA2` INT NULL ,");
        temp2Sql.append("  `DATEDATA2` DATETIME NULL ,");
        temp2Sql.append("  `RGST_DATE` DATETIME NULL ,");
        temp2Sql.append("  `UPDT_DATE` DATETIME NULL ,");
        temp2Sql.append("  `DUPLICATE_FLG` char(1) NULL ,");
        temp2Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");

        UnitTestUtil.executeUpdate(dropTemp2Sql);
        UnitTestUtil.executeUpdate(temp2Sql.toString());
    }
    private void dropTable() throws Exception {
        String dropTemp1Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String dropDup1Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1_DF";
        String dropTemp2Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";

        UnitTestUtil.executeUpdate(dropTemp1Sql);
        UnitTestUtil.executeUpdate(dropTemp2Sql);
        UnitTestUtil.executeUpdate(dropTemp2Sql);
    }
}
