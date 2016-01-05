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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.common.TsvDeleteType;
import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;


/**
 * Importerのテストクラス
 *
 * @author yuta.shirai
 *
 */
@SuppressWarnings("deprecation")
public class ImporterTest {
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
        UnitTestUtil.tearDownDB();
        UnitTestUtil.tearDownAfterClass();
    }
    @Before
    public void setUp() throws Exception {
    }
    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * <p>
     * 正常系：全ての処理が正常に終了するケース（引数4つ:通常起動）
     *
     * @throws Exception
     */
    @Test
    public void executeTest01() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter();
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 正常系：全ての処理が正常に終了するケース（引数5つ:サブ起動）
     *
     * @throws Exception
     */
    @Test
    public void executeTest02() throws Exception {
        // 処理の実行
        String[] args = new String[7];
        args[0] = "secondary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        args[6] = "IMPRT_TARGET1";
        Importer importer = new StubImporter();
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 異常系：Import対象テーブルのロックを取得に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest03() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected TargetDataLock createTargetDataLock() {
                return new StubTargetDataLock(false);
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：Import対象ファイルの生成に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest04() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected ImportFileCreate createImportFileCreate() {
                return new StubImportFileCreate(false);
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：ファイルの転送に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest05() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected ImportFileSend createImportFileSend() {
                return new StubImportFileSend(false);
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：処理実行中に例外が発生するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest06() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected TargetDataLock createTargetDataLock() {
                throw new NullPointerException();
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：引数の数が不正なケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest07() throws Exception {
        // 処理の実行
        String[] args = new String[5];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        Importer importer = new StubImporter();
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：引数のジョブネットの終了予定時刻の桁数が不正なケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest08() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "201010231020501";
        Importer importer = new StubImporter();
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：引数のジョブネットの終了予定時刻が数値でないケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest09() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "2010102310205-";
        Importer importer = new StubImporter();
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：DSLプロパティの読み込みに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest10() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    public boolean loadImportParam(String targetName, String batchId, String jobflowId, boolean isPrimary) {
                        return false;
                    }

                };
                return loder;
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 正常系：Import対象のテーブルが存在しないケース(通常起動)
     *
     * @throws Exception
     */
    @Test
    public void executeTest11() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    protected Properties getImportProp(File file, String targetName) throws IOException {
                        System.out.println(file);
                        File propFile = new File("src/test/data/common/import3.propertes");
                        FileInputStream fis = new FileInputStream(propFile);
                        Properties prop = new Properties();
                        prop.load(fis);
                        return prop;
                    }
                };
                return loder;
            }

        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 異常系：Import対象のテーブルが存在せず、RUNNING_JOBFLOWSへのInsertに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest12() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    protected Properties getImportProp(File file, String targetName) throws IOException {
                        System.out.println(file);
                        File propFile = new File("src/test/data/common/import3.propertes");
                        FileInputStream fis = new FileInputStream(propFile);
                        Properties prop = new Properties();
                        prop.load(fis);
                        return prop;
                    }
                };
                return loder;
            }

            @Override
            protected TargetDataLock createTargetDataLock() {
                return new StubTargetDataLock(true, null);
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：ジョブフロー実行IDの排他制御に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest13() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/importer/Importer");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "JOB_FLOW01-001";
        args[5] = "20101023102050";
        Importer importer = new StubImporter();
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：引数のImport処理区分が不正なケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest14() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "aaa";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050a";
        Importer importer = new StubImporter();
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 正常系：Import対象のテーブルが存在しないケース(サブ起動)
     *
     * @throws Exception
     */
    @Test
    public void executeTest15() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "secondary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    protected Properties getImportProp(File file, String targetName) throws IOException {
                        System.out.println(file);
                        File propFile = new File("src/test/data/common/import3.propertes");
                        FileInputStream fis = new FileInputStream(propFile);
                        Properties prop = new Properties();
                        prop.load(fis);
                        return prop;
                    }
                };
                return loder;
            }

        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 正常系：TSVファイル削除有りが設定されているケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest16() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter() {
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.importer.StubImporter#createTargetDataLock()
             */
            @Override
            protected TargetDataLock createTargetDataLock() {
                return new StubTargetDataLock(){
                    /* (非 Javadoc)
                     * @see com.asakusafw.bulkloader.importer.StubTargetDataLock#lock(com.asakusafw.bulkloader.bean.ImportBean)
                     */
                    @Override
                    public boolean lock(ImportBean bean) throws BulkLoaderReRunnableException {
                        Properties p = ConfigurationLoader.getProperty();
                        p.setProperty(Constants.PROP_KEY_IMPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
                        ConfigurationLoader.setProperty(p);
                        return super.lock(bean);
                    }

                };
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 正常系：TSVファイル削除無しが設定されているケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest17() throws Exception {
        // 処理の実行
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter() {
            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.importer.StubImporter#createTargetDataLock()
             */
            @Override
            protected TargetDataLock createTargetDataLock() {
                return new StubTargetDataLock(){
                    /* (非 Javadoc)
                     * @see com.asakusafw.bulkloader.importer.StubTargetDataLock#lock(com.asakusafw.bulkloader.bean.ImportBean)
                     */
                    @Override
                    public boolean lock(ImportBean bean) throws BulkLoaderReRunnableException {
                        Properties p = ConfigurationLoader.getProperty();
                        p.setProperty(Constants.PROP_KEY_IMPORT_TSV_DELETE, TsvDeleteType.FALSE.getSymbol());
                        ConfigurationLoader.setProperty(p);
                        return super.lock(bean);
                    }

                };
            }
        };
        int result = importer.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }

    /**
     * Executes importer but {@link BulkLoaderReRunnableException} is thrown trying to acquire cache lock.
     * @throws Exception if failed
     */
    @Test
    public void execute_cache_lock_conflict() throws Exception {
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        Importer importer = new StubImporter() {
            @Override
            protected ImportProtocolDecide createImportProtocolDecide() {
                return new ImportProtocolDecide() {
                    @Override
                    public void execute(ImportBean bean) throws BulkLoaderReRunnableException {
                        throw new BulkLoaderReRunnableException(ImporterTest.class, "TG-IMPORTER-11005", "A", "b");
                    }
                };
            }
        };
        int result = importer.execute(args);
        assertEquals(Constants.EXIT_CODE_RETRYABLE, result);
    }

    /**
     * Executes importer but {@link BulkLoaderReRunnableException} is thrown trying to acquire cache lock.
     * @throws Exception if failed
     */
    @Test
    public void execute_data_lock_conflict() throws Exception {
        String[] args = new String[6];
        args[0] = "primary";
        args[1] = targetName;
        args[2] = "batch01";
        args[3] = "11";
        args[4] = "11-1";
        args[5] = "20101023102050";
        final AtomicBoolean cacheReleased = new AtomicBoolean(false);
        Importer importer = new StubImporter() {
            @Override
            protected ImportProtocolDecide createImportProtocolDecide() {
                return new ImportProtocolDecide() {
                    @Override
                    public void execute(ImportBean bean) throws BulkLoaderReRunnableException {
                        cacheReleased.set(false);
                    }
                    @Override
                    public void cleanUpForRetry(ImportBean bean) throws BulkLoaderSystemException {
                        cacheReleased.set(true);
                    }
                };
            }
            @Override
            protected TargetDataLock createTargetDataLock() {
                return new StubTargetDataLock() {
                    @Override
                    public boolean lock(ImportBean bean) throws BulkLoaderReRunnableException {
                        throw new BulkLoaderReRunnableException(ImporterTest.class, "TG-IMPORTER-11005", "A", "b");
                    }
                };
            }
        };
        int result = importer.execute(args);
        assertEquals(Constants.EXIT_CODE_RETRYABLE, result);
        assertTrue(cacheReleased.get());
    }
}
class StubImporter extends Importer {
    @Override
    protected ImportFileDelete createImportFileDelete() {
        return new StubImportFileDelete();
    }
    @Override
    protected ImportFileSend createImportFileSend() {
        return new StubImportFileSend();
    }
    @Override
    protected ImportFileCreate createImportFileCreate() {
        return new StubImportFileCreate();
    }
    @Override
    protected TargetDataLock createTargetDataLock() {
        return new StubTargetDataLock();
    }
    @Override
    protected ImportProtocolDecide createImportProtocolDecide() {
        return new StubImportProtocolDecide();
    }
    @Override
    protected JobFlowParamLoader createJobFlowParamLoader() {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getImportProp(File file, String targetName) throws IOException {
                System.out.println(file);
                File propFile = new File("src/test/data/importer/import.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        return loder;
    }
}
class StubImportFileDelete extends ImportFileDelete {
    public StubImportFileDelete() {
        return;
    }
    @Override
    public void deleteFile(ImportBean bean) {
        return;
    }
}
class StubImportFileSend extends ImportFileSend {
    boolean result = true;
    public StubImportFileSend(boolean result) {
        this.result = result;
    }
    public StubImportFileSend() {
        return;
    }
    @Override
    public boolean sendImportFile(ImportBean bean) {
        return result;
    }
}
class StubImportFileCreate extends ImportFileCreate {
    boolean result = true;
    public StubImportFileCreate(boolean result) {
        this.result = result;
    }
    public StubImportFileCreate() {
        return;
    }
    @Override
    public boolean createImportFile(ImportBean bean, String jobFlowSid) {
        return result;
    }
}
class StubTargetDataLock extends TargetDataLock {
    boolean result = true;
    String sid = "1";
    public StubTargetDataLock(boolean result) {
        this.result = result;
    }
    public StubTargetDataLock(boolean result, String sid) {
        this.result = result;
        this.sid = sid;
    }
    public StubTargetDataLock() {
        return;
    }
    @Override
    public boolean lock(ImportBean bean) throws BulkLoaderReRunnableException {
        return result;
    }
    @Override
    public String insertRunningJobFlow(String targetName, String batchId, String jobflowId, String executionId, Date jobnetEndTime) {
        return sid;
    }
}
class StubImportProtocolDecide extends ImportProtocolDecide {
    @Override
    protected Map<String, CacheInfo> collectRemoteCacheInfo(ImportBean bean) throws BulkLoaderSystemException {
        return Collections.emptyMap();
    }
}
