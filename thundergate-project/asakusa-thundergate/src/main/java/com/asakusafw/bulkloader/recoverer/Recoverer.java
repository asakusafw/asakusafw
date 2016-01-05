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

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.DBConnection;
import com.asakusafw.bulkloader.common.ExportTempTableStatus;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.exporter.ExportDataCopy;
import com.asakusafw.bulkloader.exporter.LockRelease;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.core.context.RuntimeContext;


/**
 * Recovererの実行クラス。
 *
 * @author yuta.shirai
 */
public class Recoverer {

    static final Log LOG = new Log(Recoverer.class);

    /**
     * Exporterで読み込むプロパティファイル。
     */
    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    /**
     * 起動方法 ジョブフロー実行ID指定有無。
     */
    private boolean hasExecutionId = false;
    /**
     * 処理結果 Revovery対象のジョブフローインスタンスが存在したか。
     */
    private boolean isExistJobFlowInstance = false;
    /**
     * 処理結果 ロールバックしたジョブフローインスタンスが存在したか。
     */
    private boolean isExistRollBack = false;
    /**
     * 処理結果 他プロセスで処理中のジョブフローインスタンスが存在したか。
     */
    private boolean isExistExecOthProcess = false;
    /**
     * 処理結果 リカバリに失敗したジョブフローインスタンスが存在したか。
     */
    private boolean isExistRecoveryFail = false;

    /**
     * プログラムエントリ。
     * コマンドライン引数として以下の値をとる。
<pre>
・args[0]=ターゲット名
・args[1]=ジョブフロー実行ID
</pre>
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        Recoverer recoverer = new Recoverer();
        int result = recoverer.execute(args);
        System.exit(result);
    }

    /**
     * Recovererの処理を実行する。
     * 処理結果として以下のコードを返す。
     * ・0：全てのジョブフローインスタンスに対するリカバリ処理がロールフォワードであり処理に成功した場合
     *      または、処理対象のジョブフローインスタンスが存在しなかった場合。
     * ・1：処理が異常終了した場合
     * ・2：一部または全ての処理対象ジョブフローインスタンスに対する処理がロールバックであり処理に成功した場合
     *
     * @param args コマンドライン引数
     * @return 終了コード
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_WARNING
     * @see Constants#EXIT_CODE_ERROR
     */
    protected int execute(String[] args) {
        if (args.length > 2) {
            System.err.println("Recovererに指定する引数の数が不正です。 引数の数：" + args.length);
            return Constants.EXIT_CODE_ERROR;
        }
        String targetName = args[0];
        String executionId;
        if (args.length == 2) {
            executionId = args[1];
            hasExecutionId = true;
        } else {
            executionId = null;
            hasExecutionId = false;
        }

        try {
            // 初期処理
            if (!BulkLoaderInitializer.initDBServer("Recoverer", executionId, PROPERTIES, targetName)) {
                LOG.error("TG-RECOVERER-01003",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            // 開始ログ出力
            LOG.info("TG-RECOVERER-01001",
                    new Date(), targetName, executionId);

            if (RuntimeContext.get().isSimulation()) {
                // check only DB connection
                DBConnection.getConnection().close();
                return Constants.EXIT_CODE_SUCCESS;
            }

            // ジョブフロー実行テーブルの内容を取得する
            List<ExporterBean> beans;
            try {
                beans = selectRunningJobFlow(executionId);
                if (beans.size() > 0) {
                    isExistJobFlowInstance = true;
                }
            } catch (BulkLoaderSystemException e) {
                LOG.log(e);
                LOG.error("TG-RECOVERER-01006",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_ERROR;
            }

            // ジョブフロー実行テーブルのレコード毎にリカバリ処理を行う
            if (isExistJobFlowInstance) {
                assert beans.size() >= 1;
                for (ExporterBean bean : beans) {
                    LOG.info("TG-RECOVERER-01016",
                            targetName,
                            bean.getBatchId(),
                            bean.getJobflowId(),
                            bean.getJobflowSid(),
                            bean.getExecutionId());
                    try {
                        recovery(bean);
                    } catch (BulkLoaderSystemException e) {
                        LOG.log(e);
                        isExistRecoveryFail = true;
                    }
                }
            }

            // 正常終了
            return judgeExitCode(targetName, executionId);
        } catch (Exception e) {
            try {
                LOG.error(e, "TG-RECOVERER-01004",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("Recovererで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        }
    }
    /**
     * 処理結果に応じて戻り値を判定する。
     * @param targetName ターゲット名
     * @param executionId ジョブフロー実行ID
     * @return 戻り値
     */
    private int judgeExitCode(String targetName, String executionId) {
        if (hasExecutionId) {
            // ジョブフロー実行ID指定有りの場合
            if (!isExistJobFlowInstance) {
                LOG.info("TG-RECOVERER-01002",
                        "正常終了（指定されたジョブフローインスタンスが存在しない）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_SUCCESS;
            } else if (isExistRecoveryFail) {
                LOG.error("TG-RECOVERER-02001",
                        "異常終了（指定されたジョブフローインスタンスのリカバリ処理に失敗した）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else if (isExistExecOthProcess) {
                LOG.error("TG-RECOVERER-02001",
                        "異常終了（指定されたジョブフローインスタンスが処理中である）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else if (isExistRollBack) {
                LOG.info("TG-RECOVERER-01002",
                        "正常終了（指定されたジョブフローインスタンスのロールバックを行った）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_WARNING;
            } else {
                LOG.info("TG-RECOVERER-01002",
                        "正常終了（指定されたジョブフローインスタンスのロールフォワードを行った）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_SUCCESS;
            }
        } else {
            // ジョブフロー実行ID指定無しの場合
            if (!isExistJobFlowInstance) {
                LOG.info("TG-RECOVERER-01002",
                        "正常終了（リカバリ対象のジョブフローインスタンスが存在しない）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_SUCCESS;
            } else if (isExistRecoveryFail) {
                LOG.error("TG-RECOVERER-02001",
                        "異常終了（リカバリ処理に失敗したジョブフローインスタンスが存在する）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else if (isExistExecOthProcess) {
                LOG.error("TG-RECOVERER-02001",
                        "異常終了（処理中のジョブフローインスタンスが存在する）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_ERROR;
            } else if (isExistRollBack) {
                LOG.info("TG-RECOVERER-01002",
                        "正常終了（ロールバックを行ったジョブフローインスタンスが存在する）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_WARNING;
            } else {
                LOG.info("TG-RECOVERER-01002",
                        "正常終了（全てのジョブフローインスタンスをロールフォワードした）",
                        new Date(), targetName, executionId);
                return Constants.EXIT_CODE_SUCCESS;
            }
        }
    }

    // CHECKSTYLE:OFF MethodLengthCheck - FIXME refactoring
    /**
     * リカバリ処理を行う。
     * 処理結果として以下の通り戻り値を返す。
<pre>
・ロールバックを行った場合:true
・ロールフォワードを行った場合:false
・当該ジョブフローがリカバリ対象でなかった場合:false
</pre>
     * @param exporterBean Exporterで使用するパラメータを保持するオブジェクト
     * @throws BulkLoaderSystemException リカバリ失敗
     */
    private void recovery(ExporterBean exporterBean) throws BulkLoaderSystemException {
        String executionId = exporterBean.getExecutionId();
        Connection lockConn = null;
        try {
            // ジョブフローインスタンスIDの排他制御
            LOG.info("TG-RECOVERER-01017",
                    exporterBean.getTargetName(),
                    exporterBean.getBatchId(),
                    exporterBean.getJobflowId(),
                    exporterBean.getJobflowSid(),
                    exporterBean.getExecutionId());
            lockConn = DBConnection.getConnection();
            if (!DBAccessUtil.getJobflowInstanceLock(executionId, lockConn)) {
                // 他のプロセスが排他制御を行っている為、リカバリ対象外とする。
                LOG.info("TG-RECOVERER-01008",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId());
                isExistExecOthProcess = true;
                return;
            } else {
                LOG.info("TG-RECOVERER-01018",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId());
            }

            // 当該ジョブフローインスタンスがリカバリ対象か判断する
            if (!isExecRecovery(exporterBean, hasExecutionId)) {
                return;
            }

            // 当該ジョブフローの設定を読み込む
            loadParam(exporterBean);

            // ロールバック可能か判断する
            boolean rollBack = judgeRollBack(exporterBean);

            if (rollBack) {
                LOG.info("TG-RECOVERER-01023",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId(),
                        "ロールバック");
            } else {
                LOG.info("TG-RECOVERER-01023",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId(),
                        "ロールフォワード");
            }

            // ロールバックできない場合、Exportデータのコピーを行う
            boolean updateEnd = true;
            if (!rollBack) {
                // Exportデータコピー処理を実行する
                LOG.info("TG-RECOVERER-01019",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId());
                ExportDataCopy copy = createExportDataCopy();
                if (!copy.copyData(exporterBean)) {
                    throw new BulkLoaderSystemException(getClass(), "TG-RECOVERER-01012",
                            exporterBean.getTargetName(),
                            exporterBean.getBatchId(),
                            exporterBean.getJobflowId(),
                            exporterBean.getJobflowSid(),
                            exporterBean.getExecutionId());
                } else {
                    // 更新レコードのコピーが全て終了しているかを表すフラグを取得
                    updateEnd = copy.isUpdateEnd();
                    LOG.info("TG-RECOVERER-01020",
                            exporterBean.getTargetName(),
                            exporterBean.getBatchId(),
                            exporterBean.getJobflowId(),
                            exporterBean.getJobflowSid(),
                            exporterBean.getExecutionId());
                }
            }

            // エクスポートテンポラリテーブルの削除及びロック解除を行う
            LOG.info("TG-RECOVERER-01021",
                    exporterBean.getTargetName(),
                    exporterBean.getBatchId(),
                    exporterBean.getJobflowId(),
                    exporterBean.getJobflowSid(),
                    exporterBean.getExecutionId());
            LockRelease lock = createLockRelease();
            if (!lock.releaseLock(exporterBean, updateEnd)) {
                // ロックの解除に失敗
                throw new BulkLoaderSystemException(getClass(), "TG-RECOVERER-01013",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId());
            } else {
                LOG.info("TG-RECOVERER-01022",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId());
            }

            // 処理結果をログに出力
            if (rollBack) {
                LOG.info("TG-RECOVERER-01014",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId(),
                        "ロールバック");
            } else {
                if (updateEnd) {
                    LOG.info("TG-RECOVERER-01014",
                            exporterBean.getTargetName(),
                            exporterBean.getBatchId(),
                            exporterBean.getJobflowId(),
                            exporterBean.getJobflowSid(),
                            exporterBean.getExecutionId(),
                            "ロールフォワード");
                } else {
                    throw new BulkLoaderSystemException(getClass(), "TG-RECOVERER-01015",
                            exporterBean.getTargetName(),
                            exporterBean.getBatchId(),
                            exporterBean.getJobflowId(),
                            exporterBean.getJobflowSid(),
                            exporterBean.getExecutionId(),
                            "ロールフォワード");
                }
            }
            if (rollBack) {
                isExistRollBack = true;
            }
        } finally {
            // ジョブフローインスタンスIDの排他を解除
            DBAccessUtil.releaseJobflowInstanceLock(lockConn);
        }
    }
    // CHECKSTYLE:ON MethodLengthCheck

    /**
     * 当該ジョブフローインスタンスがリカバリ対象かどうかを判断する。
     * @param exporterBean Exporterで使用するパラメータを保持するオブジェクト
     * @param hasParam 引数にジョブフローインスタンスIDが指定されているか
     * @return リカバリ対象の場合:true、リカバリ対象外の場合:false
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    protected boolean isExecRecovery(
            ExporterBean exporterBean,
            boolean hasParam) throws BulkLoaderSystemException {
        String executionId = exporterBean.getExecutionId();
        // ジョブフロー実行テーブルにレコードが存在しない場合はエラーとする
        List<ExporterBean> beans = selectRunningJobFlow(executionId);
        if (beans.size() == 0) {
            throw new BulkLoaderSystemException(getClass(), "TG-RECOVERER-01009",
                    exporterBean.getTargetName(),
                    exporterBean.getBatchId(),
                    exporterBean.getJobflowId(),
                    exporterBean.getJobflowSid(),
                    exporterBean.getExecutionId());
        }

        // ジョブフローインスタンスIDが指定されていない場合は、
        // 当該ジョブフローインスタンスが実行中かMMに問い合わせる
        if (!hasParam) {
            if (isRunningJobFlow(executionId)) {
                // 実行中のため、リカバリ対象外とする。
                LOG.info("TG-RECOVERER-01010",
                        exporterBean.getTargetName(),
                        exporterBean.getBatchId(),
                        exporterBean.getJobflowId(),
                        exporterBean.getJobflowSid(),
                        exporterBean.getExecutionId());
                return false;
            }
        }

        return true;
    }

    /**
     * 指定されたジョブフローの設定を読み込んでBeanに設定する。
     * @param exporterBean テンポラリ管理テーブルの内容を保持するBean
     * @throws BulkLoaderSystemException テンポラリ管理テーブルの内容を保持するBean
     */
    protected void loadParam(ExporterBean exporterBean) throws BulkLoaderSystemException {
        // DSLプロパティを読み込み
        JobFlowParamLoader paramLoader = createJobFlowParamLoader();
        if (!paramLoader.loadRecoveryParam(
                exporterBean.getTargetName(), exporterBean.getBatchId(), exporterBean.getJobflowId())) {
            throw new BulkLoaderSystemException(getClass(), "TG-RECOVERER-01007",
                    "DSLプロパティ",
                    MessageFormat.format(
                            "ターゲット名:{0}, バッチID:{1}, ジョブフローID:{2}" ,
                            exporterBean.getTargetName(),
                            exporterBean.getBatchId(),
                            exporterBean.getJobflowId()),
                    exporterBean.getExecutionId());
        }
        exporterBean.setExportTargetTable(paramLoader.getExportTargetTables());
        exporterBean.setImportTargetTable(paramLoader.getImportTargetTables());

        // リトライ回数・リトライインターバルを読み込み
        String count = ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_RETRY_COUNT);
        String interval = ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_RETRY_INTERVAL);
        try {
            exporterBean.setRetryCount(Integer.parseInt(count));
            exporterBean.setRetryInterval(Integer.parseInt(interval));
        } catch (NumberFormatException e) {
            throw new BulkLoaderSystemException(getClass(), "TG-RECOVERER-01007",
                    "リトライ回数,リトライインターバル",
                    count + "," + interval,
                    exporterBean.getExecutionId());
        }
    }

    /**
     * 当該ジョブフローがロールバック可能か判断する。
     * @param exporterBean Exporterで使用するパラメータを保持するオブジェクト
     * @return ロールバック可能な場合:true、ロールバック不可の場合:false
     * @throws BulkLoaderSystemException テンポラリ管理テーブルの検索に失敗した場合
     */
    protected boolean judgeRollBack(ExporterBean exporterBean) throws BulkLoaderSystemException {
        List<ExportTempTableBean> tempBean = null;
        try {
            tempBean = getExportTempTable(exporterBean.getJobflowSid());
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            throw new BulkLoaderSystemException(getClass(), "TG-RECOVERER-01011",
                    exporterBean.getTargetName(),
                    exporterBean.getBatchId(),
                    exporterBean.getJobflowId(),
                    exporterBean.getJobflowSid(),
                    exporterBean.getExecutionId());
        }

        // テンポラリ管理テーブルのレコードが存在しない場合、ロールバック可能と判断する
        if (tempBean == null || tempBean.size() == 0) {
            return true;
        }

        boolean result = false;
        for (ExportTempTableBean tempTable : tempBean) {
            // Exportテンポラリテーブル名をセット
            ExportTargetTableBean tableBean = exporterBean.getExportTargetTable(tempTable.getExportTableName());
            if (tableBean != null) {
                tableBean.setExportTempTableName(tempTable.getTemporaryTableName());
                tableBean.setDuplicateFlagTableName(tempTable.getDuplicateFlagTableName());
            }

            // ExportファイルをエクスポートテンポラリテーブルにLoad中のため、ロールバック可能と判断する
            if (tempTable.getTempTableStatus() == null
                    || tempTable.getTempTableStatus().equals(ExportTempTableStatus.LOAD_EXIT)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * ジョブフローインスタンスが実行中か問い合わせる。
     * @param executionId ジョブフロー実行ID
     * @return 実行中の場合:true、実行中でない場合:false
     */
    protected boolean isRunningJobFlow(String executionId) {
        // TODO 未実装。
        return false;
    }
    /**
     * エクスポートテンポラリ管理テーブルの情報を取得して返す。
     * @param jobflowSid ジョブフローSID
     * @return エクスポートテンポラリ管理テーブルの情報
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    protected List<ExportTempTableBean> getExportTempTable(String jobflowSid) throws BulkLoaderSystemException {
        return DBAccessUtil.getExportTempTable(jobflowSid);
    }
    /**
     * ジョブフロー実行テーブルの内容を取得する。
     * @param executionId 実行ID
     * @return ジョブフロー実行テーブルの内容, never {@code null}
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    protected List<ExporterBean> selectRunningJobFlow(String executionId) throws BulkLoaderSystemException {
        List<ExporterBean> beans = DBAccessUtil.selectRunningJobFlow(executionId);
        return beans;
    }
    /**
     * DSLParamLoaderを生成して返す。
     * @return JobFlowParamLoader
     */
    protected JobFlowParamLoader createJobFlowParamLoader() {
        return new JobFlowParamLoader();
    }
    /**
     * ExportDataCopyを生成して返す。
     * @return ExportDataCopy
     */
    protected ExportDataCopy createExportDataCopy() {
        return new ExportDataCopy();
    }
    /**
     * LockReleaseを生成して返す。
     * @return LockRelease
     */
    protected LockRelease createLockRelease() {
        return new LockRelease();
    }
}
