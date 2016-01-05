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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.ExportTempTableStatus;
import com.asakusafw.bulkloader.common.TsvDeleteType;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.testutil.UnitTestUtil;

/**
 * JudgeExecProcessのテストクラス
 * @author yuta.shirai
 *
 */
public class JudgeExecProcessTest {
    private static List<String> propertys = Arrays.asList(new String[]{"bulkloader-conf-db.properties"});
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
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * <p>
     * 正常系：正常終了(ロード前の実行：TSVファイル削除有り)
     * Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除、中間ファイル削除を行う
     *
     * @throws Exception
     */
    @Test
    public void judgeTest01() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[0];
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertFalse(judge.isExecTempTableDelete());
        assertTrue(judge.isExecReceive());
        assertTrue(judge.isExecLoad());
        assertTrue(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertTrue(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 正常系：正常終了(ロード前の実行：TSVファイル削除無し)
     * Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除
     *
     * @throws Exception
     */
    @Test
    public void judgeTest02() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.FALSE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[0];
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertFalse(judge.isExecTempTableDelete());
        assertTrue(judge.isExecReceive());
        assertTrue(judge.isExecLoad());
        assertTrue(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertFalse(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 正常系：正常終了(ロードが開始前の実行：TSVファイル削除有り)
     * テンポラリテーブル削除、Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除、中間ファイル削除を行う
     *
     * @throws Exception
     */
    @Test
    public void judgeTest03() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
                tempBean[0] = new ExportTempTableBean();
                tempBean[0].setJobflowSid("11");
                tempBean[0].setExportTableName("table1");
                tempBean[0].setTemporaryTableName("teble1_tmp");
                tempBean[0].setTempTableStatus(null);
                tempBean[1] = new ExportTempTableBean();
                tempBean[1].setJobflowSid("11");
                tempBean[1].setExportTableName("table2");
                tempBean[1].setTemporaryTableName("table2_temp");
                tempBean[1].setTempTableStatus(null);
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertTrue(judge.isExecTempTableDelete());
        assertTrue(judge.isExecReceive());
        assertTrue(judge.isExecLoad());
        assertTrue(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertTrue(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 正常系：正常終了(ロードが開始前の実行：TSVファイル削除無し)
     * テンポラリテーブル削除、Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除
     *
     * @throws Exception
     */
    @Test
    public void judgeTest04() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.FALSE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
                tempBean[0] = new ExportTempTableBean();
                tempBean[0].setJobflowSid("11");
                tempBean[0].setExportTableName("table1");
                tempBean[0].setTemporaryTableName("teble1_tmp");
                tempBean[0].setTempTableStatus(null);
                tempBean[1] = new ExportTempTableBean();
                tempBean[1].setJobflowSid("11");
                tempBean[1].setExportTableName("table2");
                tempBean[1].setTemporaryTableName("table2_temp");
                tempBean[1].setTempTableStatus(null);
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertTrue(judge.isExecTempTableDelete());
        assertTrue(judge.isExecReceive());
        assertTrue(judge.isExecLoad());
        assertTrue(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertFalse(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 正常系：正常終了(ロードが中断している場合の実行：TSVファイル削除有り)
     * テンポラリテーブル削除、Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除、中間ファイル削除を行う
     *
     * @throws Exception
     */
    @Test
    public void judgeTest05() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
                tempBean[0] = new ExportTempTableBean();
                tempBean[0].setJobflowSid("11");
                tempBean[0].setExportTableName("table1");
                tempBean[0].setTemporaryTableName("teble1_tmp");
                tempBean[0].setTempTableStatus(ExportTempTableStatus.find("1"));
                tempBean[1] = new ExportTempTableBean();
                tempBean[1].setJobflowSid("11");
                tempBean[1].setExportTableName("table2");
                tempBean[1].setTemporaryTableName("table2_temp");
                tempBean[1].setTempTableStatus(ExportTempTableStatus.find("1"));
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertTrue(judge.isExecTempTableDelete());
        assertTrue(judge.isExecReceive());
        assertTrue(judge.isExecLoad());
        assertTrue(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertTrue(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 正常系：正常終了(ロードが中断している場合の実行：TSVファイル削除無し)
     * テンポラリテーブル削除、Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除
     *
     * @throws Exception
     */
    @Test
    public void judgeTest06() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.FALSE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
                tempBean[0] = new ExportTempTableBean();
                tempBean[0].setJobflowSid("11");
                tempBean[0].setExportTableName("table1");
                tempBean[0].setTemporaryTableName("teble1_tmp");
                tempBean[0].setTempTableStatus(ExportTempTableStatus.find("1"));
                tempBean[1] = new ExportTempTableBean();
                tempBean[1].setJobflowSid("11");
                tempBean[1].setExportTableName("table2");
                tempBean[1].setTemporaryTableName("table2_temp");
                tempBean[1].setTempTableStatus(ExportTempTableStatus.find("1"));
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertTrue(judge.isExecTempTableDelete());
        assertTrue(judge.isExecReceive());
        assertTrue(judge.isExecLoad());
        assertTrue(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertFalse(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 正常系：正常終了(コピー開始後の実行)
     * Exportデータコピー、ロック解除を実行する。
     *
     * @throws Exception
     */
    @Test
    public void judgeTest07() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[2];
                tempBean[0] = new ExportTempTableBean();
                tempBean[0].setJobflowSid("11");
                tempBean[0].setExportTableName("table1");
                tempBean[0].setTemporaryTableName("teble1_tmp");
                tempBean[0].setTempTableStatus(ExportTempTableStatus.find("2"));
                tempBean[1] = new ExportTempTableBean();
                tempBean[1].setJobflowSid("11");
                tempBean[1].setExportTableName("table2");
                tempBean[1].setTemporaryTableName("table2_temp");
                tempBean[1].setTempTableStatus(ExportTempTableStatus.find("2"));
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertFalse(judge.isExecTempTableDelete());
        assertFalse(judge.isExecReceive());
        assertFalse(judge.isExecLoad());
        assertTrue(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertFalse(judge.isExecFileDelete());
    }

    /**
     *
     * <p>
     * 正常系：正常終了(ロック解除のみを行う)
     * Export対象テーブルが存在しない場合、ロック解除を行う
     *
     * @throws Exception
     */
    @Test
    public void judgeTest08() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[0];
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertTrue(result);
        assertFalse(judge.isExecTempTableDelete());
        assertFalse(judge.isExecReceive());
        assertFalse(judge.isExecLoad());
        assertFalse(judge.isExecCopy());
        assertTrue(judge.isExecLockRelease());
        assertFalse(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 異常系：ジョブフローSIDが取得できないケース
     *
     * @throws Exception
     */
    @Test
    public void judgeTest09() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid(null);
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                ExportTempTableBean[] tempBean = new ExportTempTableBean[0];
                return Arrays.asList(tempBean);
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertFalse(result);
        assertFalse(judge.isExecTempTableDelete());
        assertFalse(judge.isExecReceive());
        assertFalse(judge.isExecLoad());
        assertFalse(judge.isExecCopy());
        assertFalse(judge.isExecLockRelease());
        assertFalse(judge.isExecFileDelete());
    }
    /**
     *
     * <p>
     * 異常系：テンポラリ管理テーブルの情報を取得中に例外が発生するケース
     *
     * @throws Exception
     */
    @Test
    public void judgeTest10() throws Exception {
        // 入力値の生成
        ExporterBean bean = new ExporterBean();
        bean.setJobflowSid("11");
        bean.setJobflowId("jobflow1");
        bean.setExecutionId("11-1");
        Map<String, ExportTargetTableBean> targetTable = new HashMap<String, ExportTargetTableBean>();
        targetTable.put("table1", new ExportTargetTableBean());
        targetTable.put("table2", new ExportTargetTableBean());
        bean.setExportTargetTable(targetTable);

        Properties p = ConfigurationLoader.getProperty();
        p.setProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE, TsvDeleteType.TRUE.getSymbol());
        ConfigurationLoader.setProperty(p);

        // 処理の実行
        JudgeExecProcess judge = new JudgeExecProcess() {
            /**
             * @see com.asakusafw.bulkloader.exporter.JudgeExecProcess#getExportTempTable(java.lang.String)
             */
            @Override
            protected List<ExportTempTableBean> getExportTempTable(String jobflowSid)
                    throws BulkLoaderSystemException {
                throw new BulkLoaderSystemException(this.getClass(), "TG-EXPORTER-01001");
            }
        };
        boolean result = judge.judge(bean);

        // 結果の検証
        assertFalse(result);
        assertFalse(judge.isExecTempTableDelete());
        assertFalse(judge.isExecReceive());
        assertFalse(judge.isExecLoad());
        assertFalse(judge.isExecCopy());
        assertFalse(judge.isExecLockRelease());
        assertFalse(judge.isExecFileDelete());
    }
}
