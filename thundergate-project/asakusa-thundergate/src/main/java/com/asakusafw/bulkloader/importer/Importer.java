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

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.ImportType;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.common.TsvDeleteType;
import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.core.context.RuntimeContext;


/**
 * Importerの実行クラス。
 * @author yuta.shirai
 */
public class Importer {

    static final Log LOG = new Log(Importer.class);

    /**
     * Importerで読み込むプロパティファイル。
     */
    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    /**
     * プログラムエントリ。
     * コマンドライン引数として以下の値をとる。
<pre>
・args[0]=Import処理区分
・args[1]=ターゲット名
・args[2]=バッチID
・args[3]=ジョブフローID
・args[4]=ジョブフロー実行ID
・args[5]=ジョブフロー終了予定時刻
・args[6]=リカバリ対象テーブル
</pre>
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        RuntimeContext.get().verifyApplication(Importer.class.getClassLoader());
        Importer importer = new Importer();
        int result = importer.execute(args);
        System.exit(result);
    }
    /**
     * Importerの処理を実行する。
     * @param args コマンドライン引数
     * @return 終了コード
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_WARNING
     * @see Constants#EXIT_CODE_ERROR
     */
    protected int execute(String[] args) {
        if (args.length != 6 && args.length != 7) {
            System.err.println("Importerに指定する引数の数が不正です。 引数の数：" + args.length);
            return Constants.EXIT_CODE_ERROR;
        }
        String importerType = args[0];
        String targetName = args[1];
        String batchId = args[2];
        String jobflowId = args[3];
        String executionId = args[4];
        String endDate = args[5];
        String recoveryTable = null;
        if (args.length == 7) {
            recoveryTable = args[6];
        }

        try {
            // 初期処理
            if (!BulkLoaderInitializer.initDBServer(jobflowId, executionId, PROPERTIES, targetName)) {
                LOG.error("TG-IMPORTER-01003",
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            // 開始ログ出力
            LOG.info("TG-IMPORTER-01001",
                    new Date(), importerType, targetName, batchId, jobflowId, executionId);

            // パラメータオブジェクトを作成
            ImportBean bean =
                createBean(importerType, targetName, batchId, jobflowId, executionId, endDate, recoveryTable);
            if (bean == null) {
                // パラメータのチェックでエラー
                LOG.error("TG-IMPORTER-01009",
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            if (RuntimeContext.get().isSimulation()) {
                // check only DB connection
                DBConnection.getConnection().close();
                return Constants.EXIT_CODE_SUCCESS;
            }

            int exitCode = importTables(bean);
            return exitCode;
        } catch (BulkLoaderReRunnableException e) {
            LOG.log(e);
            return Constants.EXIT_CODE_RETRYABLE;
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            return Constants.EXIT_CODE_ERROR;
        } catch (Exception e) {
            try {
                LOG.error(e, "TG-IMPORTER-01010",
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("Importerで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        }
    }

    // CHECKSTYLE:OFF MethodLengthCheck - FIXME refactoring
    /**
     * Imports tables described in the specified bean.
     * @param bean target
     * @return exit code
     * @throws BulkLoaderSystemException if failed to import by system exception
     * @throws BulkLoaderReRunnableException if failed to import but is retryable
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public int importTables(ImportBean bean) throws BulkLoaderSystemException, BulkLoaderReRunnableException {
        if (bean == null) {
            throw new IllegalArgumentException("bean must not be null"); //$NON-NLS-1$
        }
        String importerType = (bean.isPrimary() ? ImportType.PRIMARY : ImportType.SECONDARY).toString();
        String targetName = bean.getTargetName();
        String batchId = bean.getBatchId();
        String jobflowId = bean.getJobflowId();
        String executionId = bean.getExecutionId();

        Connection lockConn = null;
        boolean protocolDecided = false;
        try {
            String jobflowSid;
            if (bean.isPrimary()) {
                // ジョブフロー実行IDの排他制御
                LOG.info("TG-IMPORTER-01015",
                        importerType, targetName, batchId, jobflowId, executionId);
                try {
                    lockConn = DBConnection.getConnection();
                    if (!DBAccessUtil.getJobflowInstanceLock(bean.getExecutionId(), lockConn)) {
                        LOG.error("TG-IMPORTER-01013",
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_ERROR;
                    } else {
                        LOG.info("TG-IMPORTER-01016",
                                importerType, targetName, batchId, jobflowId, executionId);
                    }
                } catch (BulkLoaderSystemException e) {
                    LOG.log(e);
                    LOG.error(e, "TG-IMPORTER-01014",
                            new Date(), importerType, targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_ERROR;
                }

                ImportProtocolDecide protocolDecide = createImportProtocolDecide();
                protocolDecide.execute(bean);
                protocolDecided = true;

                // Import対象テーブルのロックを取得
                TargetDataLock targetLock = createTargetDataLock();
                List<String> list = bean.getImportTargetTableList();
                if (list != null && list.size() > 0) {
                    LOG.info("TG-IMPORTER-01017",
                            importerType, targetName, batchId, jobflowId, executionId);
                    if (!targetLock.lock(bean)) {
                        // ロック取得に失敗
                        LOG.error("TG-IMPORTER-01004",
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_ERROR;
                    } else {
                        LOG.info("TG-IMPORTER-01018",
                                importerType, targetName, batchId, jobflowId, executionId);
                        jobflowSid = targetLock.getJobFlowSid();
                    }
                } else {
                    // Import対象テーブルが1件もない場合はRUNNING_JOBFLOWSテーブルにレコードをInsertして終了する
                    LOG.info("TG-IMPORTER-01019",
                            importerType, targetName, batchId, jobflowId, executionId);
                    jobflowSid = targetLock.insertRunningJobFlow(
                            bean.getTargetName(),
                            bean.getBatchId(),
                            bean.getJobflowId(),
                            bean.getExecutionId(), bean.getJobnetEndTime());
                    if (jobflowSid != null) {
                        LOG.info("TG-IMPORTER-01011",
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_SUCCESS;
                    } else {
                        LOG.error("TG-IMPORTER-01012",
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_ERROR;
                    }
                }
            } else {
                // secondary importer
                List<String> list = bean.getImportTargetTableList();
                if (list == null || list.size() == 0) {
                    LOG.info("TG-IMPORTER-01020",
                            new Date(), importerType, targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_SUCCESS;
                } else {
                    ImportProtocolDecide protocolDecide = createImportProtocolDecide();
                    protocolDecide.execute(bean);

                    // 補助インポーターではJobflow SIDを利用しない
                    jobflowSid = null;
                }
            }

            // TODO ファイル生成とファイル転送をマルチスレッドで起動処理する

            // Import対象ファイルを生成
            LOG.info("TG-IMPORTER-01021",
                    importerType, targetName, batchId, jobflowId, executionId);
            ImportFileCreate fileCreate = createImportFileCreate();
            if (!fileCreate.createImportFile(bean, jobflowSid)) {
                // ファイル生成に失敗
                LOG.error("TG-IMPORTER-01005",
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else {
                LOG.info("TG-IMPORTER-01022",
                        importerType, targetName, batchId, jobflowId, executionId);
            }

            // Import対象ファイルを転送
            LOG.info("TG-IMPORTER-01023",
                    importerType, targetName, batchId, jobflowId, executionId);
            ImportFileSend fileSend = createImportFileSend();
            if (!fileSend.sendImportFile(bean)) {
                // ファイル転送に失敗
                LOG.error("TG-IMPORTER-01006",
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else {
                LOG.info("TG-IMPORTER-01024",
                        importerType, targetName, batchId, jobflowId, executionId);
            }

            // 生成したImport対象ファイルを削除
            String deleteTsv = ConfigurationLoader.getProperty(Constants.PROP_KEY_IMPORT_TSV_DELETE);
            TsvDeleteType delType = TsvDeleteType.find(deleteTsv);
            if (TsvDeleteType.TRUE.equals(delType)) {
                LOG.info("TG-IMPORTER-01025",
                        importerType, targetName, batchId, jobflowId, executionId);
                ImportFileDelete fileDelete = createImportFileDelete();
                fileDelete.deleteFile(bean);
            } else {
                LOG.info("TG-IMPORTER-01026",
                        importerType, targetName, batchId, jobflowId, executionId);
            }

            // 正常終了
            LOG.info("TG-IMPORTER-01002",
                    new Date(), importerType, targetName, batchId, jobflowId, executionId);
            return Constants.EXIT_CODE_SUCCESS;
        } catch (BulkLoaderReRunnableException e) {
            ImportProtocolDecide protocolDecide = createImportProtocolDecide();
            try {
                if (protocolDecided) {
                    protocolDecide.cleanUpForRetry(bean);
                }
                throw e;
            } catch (BulkLoaderSystemException inner) {
                LOG.log(e);
                throw inner;
            }
        } finally {
            if (lockConn != null) {
                DBAccessUtil.releaseJobflowInstanceLock(lockConn);
            }
        }
    }
    // CHECKSTYLE:ON MethodLengthCheck

    /**
     * パラメータを保持するBeanを作成する。
     * @param importerType Import処理区分
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobflowId ジョブフローID
     * @param executionId ジョブフロー実行ID
     * @param strEndDate ジョブフロー終了予定時刻
     * @param recoveryTable リカバリ対象テーブル
     * @return Importerで使用するパラメータを保持するオブジェクト
     */
    private ImportBean createBean(
            String importerType,
            String targetName,
            String batchId,
            String jobflowId,
            String executionId,
            String strEndDate,
            String recoveryTable) {

        // 引数を分解
        ImportBean bean = new ImportBean();
        // Import処理区分
        ImportType importType = ImportType.find(importerType);
        if (ImportType.PRIMARY.equals(importType)) {
            bean.setPrimary(true);
        } else if (ImportType.SECONDARY.equals(importType)) {
            bean.setPrimary(false);
        } else {
            LOG.error("TG-IMPORTER-01008", "Import処理区分", importerType);
            return null;
        }
        // ターゲット名
        bean.setTargetName(targetName);
        // バッチID
        bean.setBatchId(batchId);
        // ジョブフローID
        bean.setJobflowId(jobflowId);
        // ジョブフロー実行ID
        bean.setExecutionId(executionId);
        // ジョブネットの終了予定時刻
        if (strEndDate.length() != 14) {
            LOG.error("TG-IMPORTER-01008", "ジョブネットの終了予定時刻", strEndDate);
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date endDate = null;
        try {
            Long.parseLong(strEndDate);
            endDate = sdf.parse(strEndDate);
        } catch (NumberFormatException e) {
            LOG.error(e, "TG-IMPORTER-01008", "ジョブネットの終了予定時刻", strEndDate);
            return null;
        } catch (ParseException e) {
            Integer.parseInt(strEndDate);
            LOG.error(e, "TG-IMPORTER-01008", "ジョブネットの終了予定時刻", strEndDate);
            return null;
        }
        bean.setJobnetEndTime(endDate);

        // プロパティから値を取得
        // リトライ回数・リトライインターバル
        bean.setRetryCount(Integer.parseInt(
                ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_RETRY_COUNT)));
        bean.setRetryInterval(Integer.parseInt(
                ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_RETRY_INTERVAL)));

        // DSLプロパティを読み込み
        JobFlowParamLoader paramLoader = createJobFlowParamLoader();
        if (!paramLoader.loadImportParam(
                bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.isPrimary())) {
            return null;
        }
        bean.setTargetTable(paramLoader.getImportTargetTables());

        return bean;
    }
    /**
     * JobFlowParamLoaderのインスタンスを生成して返す。
     * @return JobFlowParamLoader
     */
    protected JobFlowParamLoader createJobFlowParamLoader() {
        return new JobFlowParamLoader();
    }
    /**
     * ImportFileDeleteのインスタンスを生成して返す。
     * @return ImportFileDelete
     */
    protected ImportFileDelete createImportFileDelete() {
        return new ImportFileDelete();
    }
    /**
     * ImportFileSendのインスタンスを生成して返す。
     * @return ImportFileSend
     */
    protected ImportFileSend createImportFileSend() {
        return new ImportFileSend();
    }
    /**
     * ImportFileCreateのインスタンスを生成して返す。
     * @return ImportFileCreate
     */
    protected ImportFileCreate createImportFileCreate() {
        return new ImportFileCreate();
    }
    /**
     * TargetDataLockのインスタンスを生成して返す。
     * @return TargetDataLock
     */
    protected TargetDataLock createTargetDataLock() {
        return new TargetDataLock();
    }

    /**
     * Returns import protocol decider.
     * @return {@link ImportProtocolDecide}
     */
    protected ImportProtocolDecide createImportProtocolDecide() {
        return new ImportProtocolDecide();
    }
}
