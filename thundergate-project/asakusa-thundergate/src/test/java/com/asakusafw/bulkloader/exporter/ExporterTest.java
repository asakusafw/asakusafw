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
package com.asakusafw.bulkloader.exporter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
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
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;
import com.asakusafw.testtools.TestUtils;


/**
 * Exporterのテストクラス
 *
 * @author yuta.shirai
 *
 */
public class ExporterTest {
    /** 読み込むプロパティファイル */
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
    }
    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * <p>
     * 正常系：全ての処理が正常に終了するケース(ロード前の実行)
     * Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除、中間ファイル削除を行う
     *
     * @throws Exception
     */
    @Test
    public void executeTest01() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter() {
            /**
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createJudgeExecProcess()
             */
            @Override
            protected JudgeExecProcess createJudgeExecProcess() {
                StubJudgeExecProcess stub = new StubJudgeExecProcess();
                stub.setExecTempTableDelete(false);
                return stub;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }

    /**
     *
     * <p>
     * 正常系：全ての処理が正常に終了するケース(ロードが終了後の実行)
     * Exportデータコピー、ロック解除を実行する。
     *
     * @throws Exception
     */
    @Test
    public void executeTest02() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter() {
            /**
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createJudgeExecProcess()
             */
            @Override
            protected JudgeExecProcess createJudgeExecProcess() {
                StubJudgeExecProcess stub = new StubJudgeExecProcess();
                stub.setExecTempTableDelete(false);
                stub.setExecReceive(false);
                stub.setExecLoad(false);
                stub.setExecFileDelete(false);
                return stub;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 正常系：全ての処理が正常に終了するケース(ロードが中断している場合の実行)
     * テンポラリテーブル削除、Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除、中間ファイル削除を行う
     *
     * @throws Exception
     */
    @Test
    public void executeTest03() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter();
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 正常系：全ての処理が正常に終了するケース(ロック解除のみを行う)
     * ロック解除を行う
     *
     * @throws Exception
     */
    @Test
    public void executeTest04() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter() {
            /**
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createJudgeExecProcess()
             */
            @Override
            protected JudgeExecProcess createJudgeExecProcess() {
                StubJudgeExecProcess stub = new StubJudgeExecProcess();
                stub.setExecTempTableDelete(false);
                stub.setExecReceive(false);
                stub.setExecLoad(false);
                stub.setExecCopy(false);
                stub.setExecFileDelete(false);
                return stub;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 異常系：実行する処理の判断に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest05() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            /**
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createJudgeExecProcess()
             */
            @Override
            protected JudgeExecProcess createJudgeExecProcess() {
                return new StubJudgeExecProcess(false);
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：テンポラリテーブル削除に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest06() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            /**
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createTempTableDelete()
             */
            @Override
            protected TempTableDelete createTempTableDelete() {
                return new StubTempTableDelete(false);
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：Exportファイルの転送に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest07() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            @Override
            protected ExportFileReceive createExportFileReceive() {
                return new StubExportFileReceive(false);
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：ファイルのDBへのロードに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest08() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            @Override
            protected ExportFileLoad createExportFileLoad() {
                return new StubExportFileLoad(false);
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }

    /**
     *
     * <p>
     * 異常系：Exportデータコピーに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest09() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            /**
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createExportDataCopy()
             */
            @Override
            protected ExportDataCopy createExportDataCopy() {
                return new StubExportDataCopy(false, true);
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：ロックの解除に失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest10() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            @Override
            protected LockRelease createLockRelease() {
                return new StubLockRelease(false);
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：処理中に例外が発生するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest11() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            @Override
            protected ExportFileReceive createExportFileReceive() {
                throw new NullPointerException();
            }
        };
        int result = exporter.execute(args);

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
    public void executeTest12() throws Exception {
        // 処理の実行
        String[] args = new String[5];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        args[4] = "11-1";
        Exporter exporter = new StubExporter();
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：Export用DSLプロパティの読み込みに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest13() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){

            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    public boolean loadExportParam(String targetName, String batchId, String jobflowId) {
                        return false;
                    }
                };
                return loder;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：Import用DSLプロパティの読み込みに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest14() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    public boolean loadImportParam(String targetName, String batchId, String jobflowId, boolean isPrimary) {
                        return false;
                    }
                    @Override
                    public boolean loadExportParam(String targetName, String batchId, String jobflowId) {
                        return true;
                    }
                };
                return loder;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 正常系：Export対象テーブルが存在しない場合
     *
     * @throws Exception
     */
    @Test
    public void executeTest15() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    protected Properties getExportProp(File file, String targetName) throws IOException {
                        File propFile = new File("src/test/data/common/export2.propertes");
                        FileInputStream fis = new FileInputStream(propFile);
                        Properties prop = new Properties();
                        prop.load(fis);
                        return prop;
                    }
                    @Override
                    protected Properties getImportProp(File file, String targetName) throws IOException {
                        System.out.println(file);
                        File propFile = new File("src/test/data/common/import1.propertes");
                        FileInputStream fis = new FileInputStream(propFile);
                        Properties prop = new Properties();
                        prop.load(fis);
                        return prop;
                    }
                };
                return loder;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(0, result);
    }
    /**
     *
     * <p>
     * 異常系：Export用DSLプロパティの読み込みに失敗するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest16() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){
            @Override
            protected JobFlowParamLoader createJobFlowParamLoader() {
                JobFlowParamLoader loder = new JobFlowParamLoader(){
                    @Override
                    public boolean loadImportParam(String targetName, String batchId, String jobflowId, boolean isPrimary) {
                        return true;
                    }
                    @Override
                    public boolean loadExportParam(String targetName, String batchId, String jobflowId) {
                        return false;
                    }
                };
                return loder;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
    /**
     *
     * <p>
     * 異常系：更新レコードのコピーが不完全な状態で終了するケース
     *
     * @throws Exception
     */
    @Test
    public void executeTest17() throws Exception {
        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "11-1";
        Exporter exporter = new StubExporter(){

            /* (非 Javadoc)
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createExportDataCopy()
             */
            @Override
            protected ExportDataCopy createExportDataCopy() {
                return new StubExportDataCopy(true, false);
            }

        };
        int result = exporter.execute(args);

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
    public void executeTest18() throws Exception {
        // テストデータを指定
        File testDataDir = new File("src/test/data/exporter/Exporter");
        TestUtils util = new TestUtils(testDataDir);
        // テストデータをセット
        util.storeToDatabase(false);

        // 処理の実行
        String[] args = new String[4];
        args[0] = "target1";
        args[1] = "batch01";
        args[2] = "11";
        args[3] = "JOB_FLOW01-001";
        Exporter exporter = new StubExporter() {
            /**
             * @see com.asakusafw.bulkloader.exporter.StubExporter#createJudgeExecProcess()
             */
            @Override
            protected JudgeExecProcess createJudgeExecProcess() {
                StubJudgeExecProcess stub = new StubJudgeExecProcess();
                stub.setExecTempTableDelete(false);
                return stub;
            }
        };
        int result = exporter.execute(args);

        // 結果の検証
        assertEquals(1, result);
    }
}
class StubExporter extends Exporter {
    @Override
    protected JobFlowParamLoader createJobFlowParamLoader() {
        JobFlowParamLoader loder = new JobFlowParamLoader(){
            @Override
            protected Properties getExportProp(File file, String targetNam) throws IOException {
                File propFile = new File("src/test/data/common/export1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
            @Override
            protected Properties getImportProp(File file, String targetName) throws IOException {
                System.out.println(file);
                File propFile = new File("src/test/data/common/import1.propertes");
                FileInputStream fis = new FileInputStream(propFile);
                Properties prop = new Properties();
                prop.load(fis);
                return prop;
            }
        };
        return loder;
    }
    @Override
    protected ExportFileDelete createExportFileDelete() {
        return new StubExportFileDelete();
    }
    @Override
    protected LockRelease createLockRelease() {
        return new StubLockRelease();
    }
    @Override
    protected ExportFileLoad createExportFileLoad() {
        return new StubExportFileLoad();
    }
    @Override
    protected ExportFileReceive createExportFileReceive() {
        return new StubExportFileReceive();
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.Exporter#createJudgeExecProcess()
     */
    @Override
    protected JudgeExecProcess createJudgeExecProcess() {
        return new StubJudgeExecProcess();
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.Exporter#createTempTableDelete()
     */
    @Override
    protected TempTableDelete createTempTableDelete() {
        return new StubTempTableDelete();
    }
    /* (非 Javadoc)
     * @see com.asakusafw.bulkloader.exporter.Exporter#createExportDataCopy()
     */
    @Override
    protected ExportDataCopy createExportDataCopy() {
        return new StubExportDataCopy();
    }

}
class StubExportFileDelete extends ExportFileDelete {

}
class StubLockRelease extends LockRelease {
    boolean result = true;
    public StubLockRelease() {

    }
    public StubLockRelease(boolean b) {
        this.result = b;
    }
    @Override
    public boolean releaseLock(ExporterBean bean, boolean isEndJobFlow) {
        return result;
    }
}
class StubExportFileLoad extends ExportFileLoad {
    boolean result = true;
    public StubExportFileLoad() {

    }
    public StubExportFileLoad(boolean b) {
        this.result = b;
    }
    @Override
    public boolean loadFile(ExporterBean bean) {
        return result;
    }
}
class StubExportFileReceive extends ExportFileReceive {
    boolean result = true;
    String sid = "1";
    public StubExportFileReceive() {

    }
    public StubExportFileReceive(boolean b) {
        this.result = b;
    }
    public StubExportFileReceive(boolean b, String sid) {
        this.result = b;
        this.sid = sid;
    }
    @Override
    public boolean receiveFile(ExporterBean bean) {
        return result;
    }
}
class StubTempTableDelete extends TempTableDelete {
    boolean result = true;
    public StubTempTableDelete() {
    }
    public StubTempTableDelete(boolean b) {
        this.result = b;
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.TempTableDelete#delete(com.asakusafw.bulkloader.bean.ExportTempTableBean[])
     */
    @Override
    public boolean delete(List<ExportTempTableBean> exportTempTableBean, boolean copyNotEnd) {
        return result;
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.TempTableDelete#deleteTempInfoRecord(java.lang.String, java.lang.String, java.sql.Connection)
     */
    @Override
    public void deleteTempInfoRecord(String jobflowSid, String tableName, boolean copyNotEnd,
            Connection conn) throws BulkLoaderSystemException {
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.TempTableDelete#deleteTempTable(java.lang.String, java.sql.Connection)
     */
    @Override
    public void deleteTempTable(String exportTempName, String duplicateFlagTableName, boolean copyNotEnd, Connection conn)
            throws BulkLoaderSystemException {
    }

}
class StubExportDataCopy extends ExportDataCopy {
    boolean result = true;
    private boolean updateEnd = true;
    public StubExportDataCopy() {
    }
    public StubExportDataCopy(boolean result, boolean updateEnd) {
        this.result = result;
        this.updateEnd = updateEnd;
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.ExportDataCopy#copyData(com.asakusafw.bulkloader.bean.ExporterBean)
     */
    @Override
    public boolean copyData(ExporterBean bean) {
        return result;
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.ExportDataCopy#isUpdateEnd()
     */
    @Override
    public boolean isUpdateEnd() {
        return updateEnd;
    }

}
class StubJudgeExecProcess extends JudgeExecProcess {
    boolean result = true;
    private boolean execTempTableDelete = true;
    private boolean execReceive = true;
    private boolean execLoad = true;
    private boolean execCopy = true;
    private boolean execLockRelease = true;
    private boolean execFileDelete = true;
    List<ExportTempTableBean> exportTempTableBean = null;
    public StubJudgeExecProcess() {
    }
    public StubJudgeExecProcess(boolean b) {
        this.result = b;
    }
    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#judge(com.asakusafw.bulkloader.bean.ExporterBean)
     */
    @Override
    public boolean judge(ExporterBean bean) {
        return result;
    }

    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#isExecReceive()
     */
    @Override
    public boolean isExecReceive() {
        return execReceive;
    }

    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#isExecLoad()
     */
    @Override
    public boolean isExecLoad() {
        return execLoad;
    }

    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#isExecCopy()
     */
    @Override
    public boolean isExecCopy() {
        return execCopy;
    }

    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#isExecLockRelease()
     */
    @Override
    public boolean isExecLockRelease() {
        return execLockRelease;
    }

    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#isExecFileDelete()
     */
    @Override
    public boolean isExecFileDelete() {
        return execFileDelete;
    }

    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#isExecTempTableDelete()
     */
    @Override
    public boolean isExecTempTableDelete() {
        return execTempTableDelete;
    }

    /**
     * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTableBean()
     */
    @Override
    public List<ExportTempTableBean> getExportTempTableBean() {
        return exportTempTableBean;
    }
    /**
     * @param result セットする result
     */
    public void setResult(boolean result) {
        this.result = result;
    }
    /**
     * @param execTempTableDelete セットする execTempTableDelete
     */
    public void setExecTempTableDelete(boolean execTempTableDelete) {
        this.execTempTableDelete = execTempTableDelete;
    }
    /**
     * @param execReceive セットする execReceive
     */
    public void setExecReceive(boolean execReceive) {
        this.execReceive = execReceive;
    }
    /**
     * @param execLoad セットする execLoad
     */
    public void setExecLoad(boolean execLoad) {
        this.execLoad = execLoad;
    }
    /**
     * @param execCopy セットする execCopy
     */
    public void setExecCopy(boolean execCopy) {
        this.execCopy = execCopy;
    }
    /**
     * @param execLockRelease セットする execLockRelease
     */
    public void setExecLockRelease(boolean execLockRelease) {
        this.execLockRelease = execLockRelease;
    }
    /**
     * @param execFileDelete セットする execFileDelete
     */
    public void setExecFileDelete(boolean execFileDelete) {
        this.execFileDelete = execFileDelete;
    }
    /**
     * @param exportTempTableBean セットする exportTempTableBean
     */
    public void setExportTempTableBean(List<ExportTempTableBean> exportTempTableBean) {
        this.exportTempTableBean = exportTempTableBean;
    }

}