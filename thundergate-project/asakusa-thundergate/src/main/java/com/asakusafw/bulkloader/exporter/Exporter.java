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

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * Exporterの実行クラス。
 * @author yuta.shirai
 */
public class Exporter {

    static final Log LOG = new Log(Exporter.class);

    /**
     * Exporterで読み込むプロパティファイル。
     */
    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    /**
     * プログラムエントリ。
     * コマンドライン引数として以下の値をとる。
<pre>
・args[0]=ターゲット名
・args[1]=バッチID
・args[2]=ジョブフローID
・args[3]=ジョブフロー実行ID
</pre>
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        RuntimeContext.get().verifyApplication(Exporter.class.getClassLoader());
        Exporter exporter = new Exporter();
        int result = exporter.execute(args);
        System.exit(result);
    }

    // CHECKSTYLE:OFF MethodLengthCheck - FIXME refactoring
    /**
     * Exporterの処理を実行する。
     * @param args コマンドライン引数
     * @return 終了コード
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_WARNING
     * @see Constants#EXIT_CODE_ERROR
     */
    protected int execute(String[] args) {
        if (args.length != 4) {
            System.err.println("Exporterに指定する引数の数が不正です。 引数の数：" + args.length);
            return Constants.EXIT_CODE_ERROR;
        }
        String targetName = args[0];
        String batchId = args[1];
        String jobflowId = args[2];
        String executionId = args[3];

        Connection lockConn = null;
        try {
            // 初期処理
            if (!BulkLoaderInitializer.initDBServer(jobflowId, executionId, PROPERTIES, targetName)) {
                LOG.error("TG-EXPORTER-01003",
                        new Date(), targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            // 開始ログ出力
            LOG.info("TG-EXPORTER-01001",
                    new Date(), targetName, batchId, jobflowId, executionId);

            // パラメータオブジェクトを作成
            ExporterBean bean = createBean(targetName, batchId, jobflowId, executionId);
            if (bean == null) {
                // パラメータのチェックでエラー
                LOG.error("TG-EXPORTER-01006",
                        new Date(), targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            if (RuntimeContext.get().isSimulation()) {
                // check only DB connection
                DBConnection.getConnection().close();
                return Constants.EXIT_CODE_SUCCESS;
            }

            // ジョブフロー実行IDの排他制御
            LOG.info("TG-EXPORTER-01018",
                    targetName, batchId, jobflowId, executionId);
            try {
                lockConn = DBConnection.getConnection();
                if (!DBAccessUtil.getJobflowInstanceLock(bean.getExecutionId(), lockConn)) {
                    LOG.error("TG-EXPORTER-01016",
                            new Date(), targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_ERROR;
                } else {
                    LOG.info("TG-EXPORTER-01019",
                            targetName, batchId, jobflowId, executionId);
                }
            } catch (BulkLoaderSystemException e) {
                LOG.log(e);
                LOG.error("TG-EXPORTER-01017",
                        new Date(), targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            // ジョブフローSIDを取得する
            try {
                String jobflowSid = DBAccessUtil.selectJobFlowSid(bean.getExecutionId());
                bean.setJobflowSid(jobflowSid);
            } catch (BulkLoaderSystemException e) {
                LOG.log(e);
                LOG.error("TG-EXPORTER-01010",
                        new Date(), targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            // 実行する処理を判断する
            JudgeExecProcess judge = createJudgeExecProcess();
            if (!judge.judge(bean)) {
                return Constants.EXIT_CODE_ERROR;
            }

            // TODO ファイル転送とDBロードをマルチスレッドで起動処理する

            // テンポラリテーブル削除処理を実行する
            if (judge.isExecTempTableDelete()) {
                LOG.info("TG-EXPORTER-01020",
                        targetName, batchId, jobflowId, executionId, bean.getJobflowSid());
                TempTableDelete tempDelete = createTempTableDelete();
                if (!tempDelete.delete(judge.getExportTempTableBean(), true)) {
                    LOG.error("TG-EXPORTER-01013",
                            new Date(), targetName, batchId, jobflowId, executionId, bean.getJobflowSid());
                    return Constants.EXIT_CODE_ERROR;
                } else {
                    LOG.info("TG-EXPORTER-01021",
                            targetName, batchId, jobflowId, executionId, bean.getJobflowSid());
                }
            }

            // Exportファイル受信処理処理を実行する
            if (judge.isExecReceive()) {
                LOG.info("TG-EXPORTER-01022", targetName, batchId, jobflowId, executionId);
                ExportFileReceive receive = createExportFileReceive();
                if (!receive.receiveFile(bean)) {
                    // Exportファイルの転送に失敗
                    LOG.error("TG-EXPORTER-01007",
                            new Date(), targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_ERROR;
                } else {
                    LOG.info("TG-EXPORTER-01023",
                            targetName, batchId, jobflowId, executionId);
                }
            }

            // Exportファイルロード処理を実行する
            if (judge.isExecLoad()) {
                LOG.info("TG-EXPORTER-01024", targetName, batchId, jobflowId, executionId);
                ExportFileLoad road = createExportFileLoad();
                if (!road.loadFile(bean)) {
                    // Exportファイルのロードに失敗
                    LOG.error("TG-EXPORTER-01008",
                            new Date(), targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_ERROR;
                } else {
                    LOG.info("TG-EXPORTER-01025",
                            targetName, batchId, jobflowId, executionId);
                }
            }

            // Exportデータコピー処理を実行する
            boolean updateEnd = true;
            if (judge.isExecCopy()) {
                LOG.info("TG-EXPORTER-01026",
                        targetName, batchId, jobflowId, executionId);
                ExportDataCopy copy = createExportDataCopy();
                if (!copy.copyData(bean)) {
                    LOG.error("TG-EXPORTER-01014",
                            new Date(), targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_ERROR;
                } else {
                    // 更新レコードのコピーが全て終了しているかを表すフラグを取得
                    updateEnd = copy.isUpdateEnd();
                    LOG.info("TG-EXPORTER-01027",
                            targetName, batchId, jobflowId, executionId);
                }
            }

            // ロック解除処理を実行する
            if (judge.isExecLockRelease()) {
                LOG.info("TG-EXPORTER-01028",
                        targetName, batchId, jobflowId, executionId);
                LockRelease lock = createLockRelease();
                if (!lock.releaseLock(bean, updateEnd)) {
                    // ロックの解除に失敗
                    LOG.error("TG-EXPORTER-01009",
                            new Date(), targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_ERROR;
                } else {
                    LOG.info("TG-EXPORTER-01029",
                            targetName, batchId, jobflowId, executionId);
                }
            }

            // 中間ファイル削除処理を実行する
            if (judge.isExecFileDelete()) {
                LOG.info("TG-EXPORTER-01030",
                        targetName, batchId, jobflowId, executionId);
                ExportFileDelete delete = createExportFileDelete();
                delete.deleteFile(bean);
            }

            if (updateEnd) {
                // 正常終了
                LOG.info("TG-EXPORTER-01002",
                        new Date(), targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_SUCCESS;
            } else {
                // 更新レコードのコピーが不完全な状態で異常終了
                LOG.error("TG-EXPORTER-01015",
                        new Date(), targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }
        } catch (Exception e) {
            try {
                LOG.error(e, "TG-EXPORTER-01004",
                        new Date(), targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("Exporterで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        } finally {
            // ジョブフロー実行IDの排他を解除
            DBAccessUtil.releaseJobflowInstanceLock(lockConn);
        }
    }
    // CHECKSTYLE:ON MethodLengthCheck

    /**
     * パラメータを保持するBeanを作成する。
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobFlowId ジョブフローID
     * @param executionId ジョブフロー実行ID
     * @return Exporterで使用するパラメータを保持するオブジェクト
     */
    private ExporterBean createBean(String targetName, String batchId, String jobFlowId, String executionId) {
        ExporterBean bean = new ExporterBean();
        // ターゲット名
        bean.setTargetName(targetName);
        // バッチID
        bean.setBatchId(batchId);
        // ジョブフローID
        bean.setJobflowId(jobFlowId);
        // ジョブフロー実行ID
        bean.setExecutionId(executionId);

        // リトライ回数・リトライインターバル
        bean.setRetryCount(Integer.parseInt(
                ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_RETRY_COUNT)));
        bean.setRetryInterval(Integer.parseInt(
                ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_RETRY_INTERVAL)));

        // DSLプロパティを読み込み
        JobFlowParamLoader dslLoader = createJobFlowParamLoader();
        if (!dslLoader.loadExportParam(bean.getTargetName(), bean.getBatchId(), bean.getJobflowId())) {
            return null;
        }
        bean.setExportTargetTable(dslLoader.getExportTargetTables());
        if (!dslLoader.loadImportParam(bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), true)) {
            return null;
        }
        bean.setImportTargetTable(dslLoader.getImportTargetTables());

        return bean;
    }
    /**
     * DSLParamLoaderを生成して返す。
     * @return JobFlowParamLoader
     */
    protected JobFlowParamLoader createJobFlowParamLoader() {
        return new JobFlowParamLoader();
    }
    /**
     * ExportFileDeleteを生成して返す。
     * @return ExportFileDelete
     */
    protected ExportFileDelete createExportFileDelete() {
        return new ExportFileDelete();
    }
    /**
     * LockReleaseを生成して返す。
     * @return LockRelease
     */
    protected LockRelease createLockRelease() {
        return new LockRelease();
    }
    /**
     * ExportFileLoadを生成して返す。
     * @return ExportFileLoad
     */
    protected ExportFileLoad createExportFileLoad() {
        return new ExportFileLoad();
    }
    /**
     * ExportFileReceiveを生成して返す。
     * @return ExportFileReceive
     */
    protected ExportFileReceive createExportFileReceive() {
        return new ExportFileReceive();
    }
    /**
     * JudgeExecProcessを生成して返す。
     * @return JudgeExecProcess
     */
    protected JudgeExecProcess createJudgeExecProcess() {
        return new JudgeExecProcess();
    }
    /**
     * TempTableDeleteを生成して返す。
     * @return TempTableDelete
     */
    protected TempTableDelete createTempTableDelete() {
        return new TempTableDelete();
    }
    /**
     * ExportDataCopyを生成して返す。
     * @return ExportDataCopy
     */
    protected ExportDataCopy createExportDataCopy() {
        return new ExportDataCopy();
    }
}
