/**
 * Copyright 2011 Asakusa Framework Team.
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
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.common.TsvDeleteType;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;


/**
 * Importerの実行クラス。
 * @author yuta.shirai
 */
public class Importer {

    /**
     * このクラス。
     */
    private static final Class<?> CLASS = Importer.class;

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

        Connection lockConn = null;
        ImportBean bean = null;
        try {
            // 初期処理
            if (!BulkLoaderInitializer.initDBServer(jobflowId, executionId, PROPERTIES, targetName)) {
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_INIT_ERROR,
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            // 開始ログ出力
            Log.log(
                    CLASS,
                    MessageIdConst.IMP_START,
                    new Date(), importerType, targetName, batchId, jobflowId, executionId);

            // パラメータオブジェクトを作成
            bean = createBean(importerType, targetName, batchId, jobflowId, executionId, endDate, recoveryTable);
            if (bean == null) {
                // パラメータのチェックでエラー
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_PARAM_ERROR,
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            String jobflowSid;
            if (bean.isPrimary()) {
                // ジョブフロー実行IDの排他制御
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_INSTANCE_ID_LOCK,
                        importerType, targetName, batchId, jobflowId, executionId);
                try {
                    lockConn = DBConnection.getConnection();
                    if (!DBAccessUtil.getJobflowInstanceLock(bean.getExecutionId(), lockConn)) {
                        Log.log(
                                CLASS,
                                MessageIdConst.IMP_INSTANCE_ID_LOCKED,
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_ERROR;
                    } else {
                        Log.log(
                                CLASS,
                                MessageIdConst.IMP_INSTANCE_ID_LOCK_SUCCESS,
                                importerType, targetName, batchId, jobflowId, executionId);
                    }
                } catch (BulkLoaderSystemException e) {
                    Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
                    Log.log(
                            e,
                            CLASS,
                            MessageIdConst.IMP_INSTANCE_ID_LOCK_ERROR,
                            new Date(), importerType, targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_ERROR;
                }
                // Import対象テーブルのロックを取得
                TargetDataLock targetLock = createTargetDataLock();
                List<String> list = bean.getImportTargetTableList();
                if (list != null && list.size() > 0) {
                    Log.log(
                            CLASS,
                            MessageIdConst.IMP_LOCK,
                            importerType, targetName, batchId, jobflowId, executionId);
                    if (!targetLock.lock(bean)) {
                        // ロック取得に失敗
                        Log.log(
                                CLASS,
                                MessageIdConst.IMP_LOCK_ERROR,
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_ERROR;
                    } else {
                        Log.log(
                                CLASS,
                                MessageIdConst.IMP_LOCK_SUCCESS,
                                importerType, targetName, batchId, jobflowId, executionId);
                        jobflowSid = targetLock.getJobFlowSid();
                    }
                } else {
                    // Import対象テーブルが1件もない場合はRUNNING_JOBFLOWSテーブルにレコードをInsertして終了する
                    Log.log(
                            CLASS,
                            MessageIdConst.IMP_TARGET_NO_EXIST,
                            importerType, targetName, batchId, jobflowId, executionId);
                    jobflowSid = targetLock.insertRunningJobFlow(
                            bean.getTargetName(),
                            bean.getBatchId(),
                            bean.getJobflowId(),
                            bean.getExecutionId(), bean.getJobnetEndTime());
                    if (jobflowSid != null) {
                        Log.log(
                                CLASS,
                                MessageIdConst.IMP_TARGET_NO_EXIST_SUCCESS,
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_SUCCESS;
                    } else {
                        Log.log(
                                CLASS,
                                MessageIdConst.IMP_INSERT_RNNINGJOBFLOW_ERROR,
                                new Date(), importerType, targetName, batchId, jobflowId, executionId);
                        return Constants.EXIT_CODE_ERROR;
                    }
                }
            } else {
                // secondary importer
                List<String> list = bean.getImportTargetTableList();
                if (list == null || list.size() == 0) {
                    Log.log(
                            CLASS,
                            MessageIdConst.IMP_TARGET_NO_EXIST_SECONDARY,
                            new Date(), importerType, targetName, batchId, jobflowId, executionId);
                    return Constants.EXIT_CODE_SUCCESS;
                } else {
                    // 補助インポーターではJobflow SIDを利用しない
                    jobflowSid = null;
                }
            }

            // TODO ファイル生成とファイル転送をマルチスレッドで起動処理する

            // Import対象ファイルを生成
            Log.log(
                    CLASS,
                    MessageIdConst.IMP_CREATEFILE,
                    importerType, targetName, batchId, jobflowId, executionId);
            ImportFileCreate fileCreate = createImportFileCreate();
            if (!fileCreate.createImportFile(bean, jobflowSid)) {
                // ファイル生成に失敗
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_CREATEFILE_ERROR,
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else {
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_CREATEFILE_SUCCESS,
                        importerType, targetName, batchId, jobflowId, executionId);
            }

            // Import対象ファイルを転送
            Log.log(
                    CLASS,
                    MessageIdConst.IMP_SENDDATA,
                    importerType, targetName, batchId, jobflowId, executionId);
            ImportFileSend fileSend = createImportFileSend();
            if (!fileSend.sendImportFile(bean)) {
                // ファイル転送に失敗
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_SENDDATA_ERROR,
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else {
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_SENDDATA_SUCCESS,
                        importerType, targetName, batchId, jobflowId, executionId);
            }

            // TODO キャッシュ取り出しの実行
            // キャッシュ取り出しに失敗
            // Log.log(
            //         CLASS,
            //         MessageIdConst.IMP_CACHE_ERROR,
            //         new Date(), importerType, targetName, batchId, jobFlowId, executionId);

            // 生成したImport対象ファイルを削除
            String deleteTsv = ConfigurationLoader.getProperty(Constants.PROP_KEY_IMPORT_TSV_DELETE);
            TsvDeleteType delType = TsvDeleteType.find(deleteTsv);
            if (TsvDeleteType.TRUE.equals(delType)) {
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_TSV_FILE_DELETE,
                        importerType, targetName, batchId, jobflowId, executionId);
                ImportFileDelete fileDelete = createImportFileDelete();
                fileDelete.deleteFile(bean);
            } else {
                Log.log(
                        CLASS,
                        MessageIdConst.IMP_TSV_FILE_NOT_DELETE,
                        importerType, targetName, batchId, jobflowId, executionId);
            }

            // 正常終了
            Log.log(
                    CLASS,
                    MessageIdConst.IMP_EXIT,
                    new Date(), importerType, targetName, batchId, jobflowId, executionId);
            return Constants.EXIT_CODE_SUCCESS;

        } catch (Exception e) {
            try {
                Log.log(
                        e,
                        CLASS,
                        MessageIdConst.IMP_EXCEPRION,
                        new Date(), importerType, targetName, batchId, jobflowId, executionId);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("Importerで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        } finally {
            if (bean != null && bean.isPrimary()) {
                // ジョブフロー実行IDの排他を解除
                DBAccessUtil.releaseJobflowInstanceLock(lockConn);
            }
        }
    }
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
            Log.log(CLASS, MessageIdConst.IMP_PARAMCHECK_ERROR, "Import処理区分", importerType);
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
            Log.log(CLASS, MessageIdConst.IMP_PARAMCHECK_ERROR, "ジョブネットの終了予定時刻", strEndDate);
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date endDate = null;
        try {
            Long.parseLong(strEndDate);
            endDate = sdf.parse(strEndDate);
        } catch (NumberFormatException e) {
            Log.log(CLASS, MessageIdConst.IMP_PARAMCHECK_ERROR, "ジョブネットの終了予定時刻", strEndDate);
            return null;
        } catch (ParseException e) {
            Integer.parseInt(strEndDate);
            Log.log(CLASS, MessageIdConst.IMP_PARAMCHECK_ERROR, "ジョブネットの終了予定時刻", strEndDate);
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
}
