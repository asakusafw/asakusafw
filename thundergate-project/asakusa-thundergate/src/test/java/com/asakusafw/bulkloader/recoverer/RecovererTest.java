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
package com.asakusafw.bulkloader.recoverer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.exporter.ExportDataCopy;
import com.asakusafw.bulkloader.exporter.LockRelease;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.testtools.inspect.Cause;

/**
 * Recovererのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class RecovererTest {
    static final Log LOG = new Log(RecovererTest.class);
    /** ターゲット名 */
    private static String targetName = "target1";
    /** Importerで読み込むプロパティファイル */
    private static List<String> PROPERTYS = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
    /** ジョブフローID */
    private static String jobflowId = "Recoverer";
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
        BulkLoaderInitializer.initDBServer(jobflowId, executionId, PROPERTYS, targetName);
        UnitTestUtil.startUp();
    }
    @After
    public void tearDown() throws Exception {
        createTable();
        UnitTestUtil.tearDown();
    }
    private void createTable() throws Exception {
        String dropTemp1Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String dropTemp2Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String dropDup1Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1_DF";
        String dropDup2Sql = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2_DF";

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

        StringBuilder dup1Sql = new StringBuilder();
        dup1Sql.append("CREATE  TABLE `TEMP_IMPORT_TARGET1_DF` (");
        dup1Sql.append("  `TEMP_SID` BIGINT,");
        dup1Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");

        StringBuilder dup2Sql = new StringBuilder();
        dup2Sql.append("CREATE  TABLE `TEMP_IMPORT_TARGET2_DF` (");
        dup2Sql.append("  `TEMP_SID` BIGINT,");
        dup2Sql.append("  PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB");

        UnitTestUtil.executeUpdate(dropTemp1Sql);
        UnitTestUtil.executeUpdate(dropTemp2Sql);
        UnitTestUtil.executeUpdate(dropDup1Sql);
        UnitTestUtil.executeUpdate(dropDup2Sql);
        UnitTestUtil.executeUpdate(temp1Sql.toString());
        UnitTestUtil.executeUpdate(temp2Sql.toString());
        UnitTestUtil.executeUpdate(dup1Sql.toString());
        UnitTestUtil.executeUpdate(dup2Sql.toString());
    }

    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Importerでロック取得前に異常終了したジョブフローのリカバリを行うケース
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：何も行わない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

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
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Importerでロック取得後に異常終了したジョブフローのリカバリを行うケース
     * 　　　　以下の失敗も当ケースに含まれる（同じ状態でのリカバリになる）
     * 　　　　・Importファイル生成
     * 　　　　・Importファイル送信に失敗
     * 　　　　・M/Rの処理に失敗
     * 　　　　・Exportファイル受信に失敗
     * 　　　　・
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest02");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

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
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExport管理テーブルのInsert後、
     * 　　　　Exportテンポラリテーブルの作成前に異常終了したジョブフローのリカバリを行うケース
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest03() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest03");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

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
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExportテンポラリテーブルの作成中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　一部のExportテンポラリテーブルは作成済みである事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest04() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest04");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_6";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_6";
        String createSql1 = "CREATE  TABLE `TEMP_6` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_7";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_7";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExportテンポラリテーブルの作成後、
     * 　　　　Exportファイルのロード前に異常終了したジョブフローのリカバリを行うケース
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest05() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest04");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_6";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_6";
        String createSql1 = "CREATE  TABLE `TEMP_6` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_7";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_7";
        String createSql2 = "CREATE  TABLE `TEMP_7` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExportファイルのロード中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定Exportテンポラリテーブルに対応する一部のTSVファイルのロードが終了した時点で異常終了する
     * 　　　　事を想定する。（特定Exportテンポラリテーブルに対応するロードは完了していない）
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest06() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest06");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest06_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExportファイルのロード中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定Exportテンポラリテーブルに対応する全てのTSVファイルのロードが終了した後、
     * 　　　　重複チェックに失敗する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     * ※実質的には「executeTest06」と同一のテスト。（DBの状態のBefore/Afterは一緒）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest07() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest07");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest07_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExportファイルのロード中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　一部テーブルに対するロード及び重複チェックは完了し、他テーブルのロードが開始していない状態
     * 　　　　で異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest08() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest08");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest08_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExportファイルのロード中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　全てのテーブルに対するロード及び重複チェックが完了後、テンポラリ管理テーブルの
     * 　　　　ステータスをコピー開始前に更新する前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest09() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest09");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest09_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：ExporterでExportファイルのロードに終了し、データのコピー前に異常終了したジョブフローのリカバリを行うケース
     * 　　　　ロードが終了してステータスをコピー開始前に更新した後、データのコピーを開始する前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest10() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest10");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest10_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定テーブルのレコードロックテーブルの更新を行った後、新規データコピー前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest11() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest11");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest11_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定テーブルの新規データの一部をコピーした時点で異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest12() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest12");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest12_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定テーブルの新規データのコピーが完了後、重複データのコピー前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest13() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest13");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest13_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定テーブルの重複データの一部をコピーした時点で異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest14() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest14");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest14_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定テーブルの重複データのコピーが完了後、更新データのコピー前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest15() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest15");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest15_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定テーブルの更新データの一部をコピーした時点で異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest16() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest16");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest16_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　特定テーブルの更新データのコピーが完了後、
     * 　　　　エクスポートテンポラリ管理テーブルのステータスを更新前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest17() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest17");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest17_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのコピー中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　一部テーブルのデータコピー及びエクスポートテンポラリ管理テーブルのステータス更新が完了後、
     * 　　　　次のテーブルのデータコピーが始まる前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest18() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest18");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest18_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのロック解除中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　全てのテーブルのデータコピー及びエクスポートテンポラリ管理テーブルのステータス更新が完了後、
     * 　　　　エクスポートテンポラリテーブル削除前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest19() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest19");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest19_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのロック解除中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　一部のエクスポートテンポラリテーブル削除が終了した状態で異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest20() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest20");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

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

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのロック解除中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　全てのエクスポートテンポラリテーブル削除が終了後、
     * 　　　　エクスポートテンポラリ管理テーブルのレコード削除を行う前に異常終了する事を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールフォワード
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest21() throws Exception {
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest20");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

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
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterでデータのロック解除後、Exportファイル削除中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　以下の失敗も当ケースに含まれる（同じ状態でのリカバリになる）
     * 　　　　・Importer/Exporterが正常終了した状態でRecovererを実行するケース
     *
     * 期待する動作：何も行わない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest22() throws Exception {
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest22");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

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
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterを再実行し、Exportテンポラリテーブルを削除中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　一部テーブルの削除に成功している状態を想定する。
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest23() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest23");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest23_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：Exporterを再実行し、Exportテンポラリテーブルを削除後、
     * 　　　　エクスポートテンポラリ管理テーブルのレコード削除中に異常終了したジョブフローのリカバリを行うケース
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：ロールバック
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest24() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest24");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(2, result);

        // DBの結果を検証
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：リカバリ対象のジョブフローが存在しないケース
     * 　　　　（ジョブフロー実行IDの指定なし）
     *
     * 期待する動作：何も行わない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest26() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest26");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

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
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：複数のリカバリ対象のジョブフローが存在するケース
     * 　　　　以下のジョブフローが存在する事を想定する
     * 　　　　・Importerでロック取得後に異常終了したジョブフロー（リカバリ対象：ロールバック）
     * 　　　　・ExporterでLoad終了後に異常終了したジョブフロー（リカバリ対象：ロールフォワード）
     * 　　　　・多プロセスで実行中のジョブフロー（リカバリ対象外）
     *
     * 期待する動作：何も行わない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest27() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest27");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest27_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：ジョブフロー実行IDの排他制御に失敗するケース
     *
     * 期待する動作：排他制御に失敗したジョブネットインスタンスはリカバリ対象外となる
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest28() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest28");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

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
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：指定したジョブフロー実行IDに対応するジョブフロー実行テーブルのレコードを取得後、
     * 　　　　ジョブフロー実行IDによる排他制御を行う前に当該ジョブフローインスタンスに対する処理が
     * 　　　　正常終了して、リカバリ対象外となるケース
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：何も行わない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest29() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new StubRecoverer(){
            /**
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#selectRunningJobFlow(java.lang.String)
             */
            @Override
            protected List<ExporterBean> selectRunningJobFlow(
                    String executionId) throws BulkLoaderSystemException {
                ExporterBean bean = new ExporterBean();
                bean.setExecutionId(executionId);
                bean.setBatchId("BATCH03");
                bean.setJobflowId("JOB_FLOW03");
                bean.setJobflowSid("13");
                return Arrays.asList(new ExporterBean[]{bean});
            }
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#isExecRecovery(com.asakusafw.bulkloader.bean.ExporterBean, boolean)
             */
            @Override
            protected boolean isExecRecovery(ExporterBean exporterBean,
                    boolean hasParam) throws BulkLoaderSystemException {
                return false;
            }
        };
        int result = recoverer.execute(args);

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
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：ロールフォワードで更新レコードに該当するエクスポート対象テーブルのレコードが存在しないケース
     *
     * 期待する動作：コピーが完了しなかったテンポラリテーブルが削除されない。また、ジョブフロー実行テーブルのレコードも削除されない。
     * 　　　　　　　後続のジョブフローインスタンスに対すリカバリを行った後に異常終了する
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest30() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest29");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(1, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest29_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
        // テンポラリテーブルが存在する事を確認
        assertTrue(UnitTestUtil.isExistTable(tempTable1));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：引数の数が不正なケース
     *
     * 期待する動作：異常終了する
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest31() throws Exception {
        // 処理の実行
        String[] args = new String[]{"aaa", "bbb"};
        Recoverer recoverer = new StubRecoverer();
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：ジョブフロー実行テーブルの取得に失敗するケース
     *
     * 期待する動作：異常終了する
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest32() throws Exception {
        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer(){

            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#selectRunningJobFlow(java.lang.String)
             */
            @Override
            protected List<ExporterBean> selectRunningJobFlow(
                    String executionId) throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(this.getClass(), "TG-RECOVERER-01001");
            }

        };
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：処理中に予期しない例外が発生するケース
     *
     * 期待する動作：異常終了する
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest33() throws Exception {
        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer(){

            /**
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#selectRunningJobFlow(java.lang.String)
             */
            @Override
            protected List<ExporterBean> selectRunningJobFlow(
                    String executionId) throws BulkLoaderSystemException {
                throw new NullPointerException();
            }

        };
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：ロールフォワード時のExportデータのコピーに失敗するケース
     *
     * 期待する動作：異常終了する
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest34() throws Exception {
        // テンポラリテーブルを作成
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest10");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer(){
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#createExportDataCopy()
             */
            @Override
            protected ExportDataCopy createExportDataCopy() {
                ExportDataCopy copy = new ExportDataCopy() {
                    /* (非 Javadoc)
                     * @see com.asakusafw.bulkloader.exporter.ExportDataCopy#copyData(com.asakusafw.bulkloader.bean.ExporterBean)
                     */
                    @Override
                    public boolean copyData(ExporterBean bean) {
                        return false;
                    }
                };
                return copy;
            }
        };
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：ロールバック時のロック解除に失敗するケース
     *
     * 期待する動作：異常終了する
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest35() throws Exception {
        // テンポラリテーブルを作成
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest10");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName};
        Recoverer recoverer = new StubRecoverer(){

            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#createLockRelease()
             */
            @Override
            protected LockRelease createLockRelease() {
                LockRelease lock = new LockRelease(){
                    /* (非 Javadoc)
                     * @see com.asakusafw.bulkloader.exporter.LockRelease#releaseLock(com.asakusafw.bulkloader.bean.ExporterBean, boolean)
                     */
                    @Override
                    public boolean releaseLock(ExporterBean bean,
                            boolean isEndJobFlow) {
                        return false;
                    }
                };
                return lock;
            }

        };
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 正常系：最低限の設定でリカバリを行うケース
     * 　　　　（ジョブフロー実行IDの指定あり）
     *
     * 期待する動作：何も行わない
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest36() throws Exception {
        // テンポラリテーブルを作成
        String tempTable1 = "TEMP_IMPORT_TARGET1";
        String dropSql1 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET1";
        String createSql1 = "CREATE  TABLE `TEMP_IMPORT_TARGET1` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA1` VARCHAR(45) NULL ,`INTDATA1` INT NULL ,`DATEDATA1` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        String tempTable2 = "TEMP_IMPORT_TARGET2";
        String dropSql2 = "DROP TABLE IF EXISTS TEMP_IMPORT_TARGET2";
        String createSql2 = "CREATE  TABLE `TEMP_IMPORT_TARGET2` (`TEMP_SID` BIGINT AUTO_INCREMENT,`SID` BIGINT NULL,`VERSION_NO` BIGINT NULL DEFAULT 1,`TEXTDATA2` VARCHAR(45) NULL ,`INTDATA2` INT NULL ,`DATEDATA2` DATETIME NULL ,`RGST_DATE` DATETIME NULL ,`UPDT_DATE` DATETIME NULL ,`DUPLICATE_FLG` CHAR(1) NULL ,PRIMARY KEY (`TEMP_SID`) ) ENGINE=InnoDB;";
        UnitTestUtil.executeUpdate(dropSql1);
        UnitTestUtil.executeUpdate(createSql1);
        UnitTestUtil.executeUpdate(dropSql2);
        UnitTestUtil.executeUpdate(createSql2);
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest10");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[]{targetName, executionId};
        Recoverer recoverer = new Recoverer(){
            /**
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#createJobFlowParamLoader()
             */
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    protected Properties getImportProp(File dslFile, String targetName) throws IOException {
                        Properties prop = new Properties();
                        prop.setProperty("import.target-table", "IMPORT_TARGET1,IMPORT_TARGET2");
                        return prop;
                    }
                    @Override
                    protected Properties getExportProp(File dslFile, String targetName) throws IOException {
                        Properties prop = new Properties();
                        prop.setProperty("export.target-table", "IMPORT_TARGET1,IMPORT_TARGET2");
                        prop.setProperty("IMPORT_TARGET1.export-table-column", "TEXTDATA1,INTDATA1,DATEDATA1");
                        prop.setProperty("IMPORT_TARGET2.export-table-column", "SID,VERSION_NO,TEXTDATA2,INTDATA2,DATEDATA2,RGST_DATE,UPDT_DATE");
                        prop.setProperty("IMPORT_TARGET1.tsv-column", "SID,VERSION_NO,TEXTDATA1,INTDATA1,DATEDATA1,RGST_DATE,UPDT_DATE");
                        prop.setProperty("IMPORT_TARGET2.tsv-column", "SID,VERSION_NO,TEXTDATA2,INTDATA2,DATEDATA2,RGST_DATE,UPDT_DATE");
                        prop.setProperty("IMPORT_TARGET1.error-table", "IMPORT_TARGET1_ERROR");
                        prop.setProperty("IMPORT_TARGET2.error-table", "IMPORT_TARGET2_ERROR");
                        prop.setProperty("IMPORT_TARGET1.error-table-column", "SID,VERSION_NO,TEXTDATA1,INTDATA1,DATEDATA1,RGST_DATE,UPDT_DATE");
                        prop.setProperty("IMPORT_TARGET2.error-table-column", "TEXTDATA2,INTDATA2,DATEDATA2");
                        prop.setProperty("IMPORT_TARGET1.error-column", "ERROR_CODE");
                        prop.setProperty("IMPORT_TARGET2.error-column", "ERROR_CODE");
                        prop.setProperty("IMPORT_TARGET1.error-code", "ERR99");
                        prop.setProperty("IMPORT_TARGET2.error-code", "99");

                        return prop;
                    }
                };
                return loder;
            }
        };
        int result = recoverer.execute(args);

        // 実行結果の検証
        assertEquals(0, result);

        // DBの結果を検証
        util = new TestUtils(new File("src/test/data/recoverer/executeTest10_assert"));
        util.loadFromDatabase();
        if (!util.inspect()) {
            for (Cause cause : util.getCauses()) {
                System.out.println(cause.getMessage());
            }
            fail(util.getCauseMessage());
        }

        // テンポラリテーブルが存在しない事を確認
        assertFalse(UnitTestUtil.isExistTable(tempTable1));
        assertFalse(UnitTestUtil.isExistTable(tempTable2));
    }
    /**
     *
     * <p>
     * executeメソッドのテストケース
     *
     * 異常系：引数が不正なケース
     *
     * 期待する動作：異常終了する
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void executeTest37() throws Exception {
        // 処理の実行
        Recoverer recoverer = new StubRecoverer();

        // 実行結果の検証
        int result = recoverer.execute(new String[]{targetName, executionId, "3"});
        assertEquals(1, result);

    }
    /**
     *
     * <p>
     * isExecRecoveryメソッドのテストケース
     *
     * 正常系： ジョブフロー実行テーブルにレコードが存在しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void isExecRecoveryTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        ExporterBean bean = new ExporterBean();
        bean.setExecutionId("JOB_FLOW01-001");
        bean.setJobflowId("JOBFLOW-01");
        bean.setBatchId("BATCH01");
        bean.setJobflowSid("11");
        Recoverer recoverer = new StubRecoverer();
        try {
            recoverer.isExecRecovery(bean, false);
            fail();
        } catch (BulkLoaderSystemException e) {
            e.printStackTrace();
            assertTrue(true);
        }
    }
    /**
     *
     * <p>
     * isExecRecoveryメソッドのテストケース
     *
     * 正常系： ジョブフロー実行テーブルにレコードが存在しないケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void isExecRecoveryTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        ExporterBean bean = new ExporterBean();
        bean.setExecutionId("JOB_FLOW01-002");
        bean.setJobflowId("JOBFLOW-02");
        bean.setBatchId("BATCH02");
        bean.setJobflowSid("12");
        Recoverer recoverer = new StubRecoverer(){
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#isRunningJobFlow(java.lang.String)
             */
            @Override
            protected boolean isRunningJobFlow(String executionId) {
                return true;
            }
        };
        boolean result = recoverer.isExecRecovery(bean, false);

        // 実行結果の検証
        assertFalse(result);
    }
    /**
     *
     * <p>
     * loadParamメソッドのテストケース
     *
     * 正常系： ジョブフロー設定の読み込みに失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadParamTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        ExporterBean bean = new ExporterBean();
        bean.setExecutionId("JOB_FLOW01-002");
        bean.setJobflowId("JOBFLOW-02");
        bean.setBatchId("BATCH02");
        bean.setJobflowSid("12");
        Recoverer recoverer = new StubRecoverer(){
            /*
             * @see com.asakusafw.bulkloader.recoverer.StubRecoverer#createJobFlowParamLoader()
             */
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loader = new JobFlowParamLoader(){
                    /* (非 Javadoc)
                     * @see com.asakusafw.bulkloader.common.JobFlowParamLoader#loadRecoveryParam(java.lang.String, java.lang.String, java.lang.String)
                     */
                    @Override
                    public boolean loadRecoveryParam(String targetName,
                            String batchId, String jobflowId) {
                        return false;
                    }

                };
                return loader;
            }
        };
        try {
            recoverer.loadParam(bean);
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
    /**
     *
     * <p>
     * loadParamメソッドのテストケース
     *
     * 正常系：リトライ回数の読み込みに失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void loadParamTest02() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // プロパティの書き換え
        Properties prop = ConfigurationLoader.getProperty();
        prop.setProperty(Constants.PROP_KEY_EXP_RETRY_COUNT, "aaa");

        // 処理の実行
        ExporterBean bean = new ExporterBean();
        bean.setExecutionId("JOB_FLOW01-002");
        bean.setJobflowId("JOBFLOW-02");
        bean.setBatchId("BATCH02");
        bean.setJobflowSid("12");
        Recoverer recoverer = new StubRecoverer();
        try {
            recoverer.loadParam(bean);
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
    /**
     *
     * <p>
     * judgeRollBackメソッドのテストケース
     *
     * 異常系：エクスポートテンポラリ管理テーブルの情報の取得に失敗するケース
     *
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void judgeRollBackTest01() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/recoverer/executeTest01");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        ExporterBean bean = new ExporterBean();
        bean.setExecutionId("JOB_FLOW01-002");
        bean.setJobflowId("JOBFLOW-02");
        bean.setBatchId("BATCH02");
        bean.setJobflowSid("12");
        Recoverer recoverer = new StubRecoverer(){
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.recoverer.Recoverer#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(this.getClass(), "TG-RECOVERER-01001");
            }
        };
        try {
            recoverer.judgeRollBack(bean);
            fail();
        } catch (BulkLoaderSystemException e) {
            LOG.info(e.getCause(), e.getMessageId(), e.getMessageArgs());
        }
    }
}

class StubRecoverer extends Recoverer {
    /**
     * @see com.asakusafw.bulkloader.recoverer.Recoverer#createJobFlowParamLoader()
     */
    @Override
    protected JobFlowParamLoader createJobFlowParamLoader() {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File file, String targetName) throws IOException {
                File propFile = new File("src/test/data/recoverer/export.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
            @Override
            protected Properties getImportProp(File file, String targetName) throws IOException {
                System.out.println(file);
                File propFile = new File("src/test/data/recoverer/import.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        return loder;
    }
}