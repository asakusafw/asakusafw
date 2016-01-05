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
package com.asakusafw.bulkloader.collector;

import java.util.Date;
import java.util.List;

import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.core.context.RuntimeContext;


/**
 * Collectorの実行クラス。
 * @author yuta.shirai
 *
 */
public class Collector {

    static final Log LOG = new Log(Collector.class);

    private static final List<String> PROPERTIES = Constants.PROPERTIES_HC;

    /**
     * プログラムエントリ。
     * <p>
     * コマンドライン引数として以下の値をとる。
     * </p>
<pre>
・args[0]=ターゲット名(必須)
・args[1]=バッチID(必須)
・args[2]=ジョブフローID(必須)
・args[3]=ジョブフローの実行ID(必須)
・args[4]=OSのユーザー名(必須)
</pre>
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SystemOutManager.changeSystemOutToSystemErr();
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        RuntimeContext.get().verifyApplication(Collector.class.getClassLoader());
        Collector collector = new Collector();
        int result = collector.execute(args);
        System.exit(result);
    }
    /**
     * Collectorの処理を実行する。
     * @param args コマンドライン引数
     * @return 処理全体の終了コード
     * @see Constants#EXIT_CODE_SUCCESS
     * @see Constants#EXIT_CODE_WARNING
     * @see Constants#EXIT_CODE_ERROR
     */
    protected int execute(String[] args) {
        if (args.length != 5) {
            System.err.println("Collectorに指定する引数の数が不正です。 引数の数：" + args.length);
            return Constants.EXIT_CODE_ERROR;
        }
        String targetName = args[0];
        String batchId = args[1];
        String jobflowId = args[2];
        String executionId = args[3];
        String user = args[4];

        try {
            // 初期処理
            if (!BulkLoaderInitializer.initHadoopCluster(jobflowId, executionId, PROPERTIES)) {
                LOG.error(
                        "TG-COLLECTOR-01003",
                        new Date(), targetName, batchId, jobflowId, executionId, user);
                return Constants.EXIT_CODE_ERROR;
            }

            // 開始ログ出力
            LOG.info("TG-COLLECTOR-01001",
                    new Date(), targetName, batchId, jobflowId, executionId, user);

            // パラメータオブジェクトを作成
            ExporterBean bean = createBean(targetName, batchId, jobflowId, executionId);
            if (bean == null) {
                // パラメータのチェックでエラー
                LOG.error("TG-COLLECTOR-01006",
                        new Date(), targetName, batchId, jobflowId, executionId, user);
                return Constants.EXIT_CODE_ERROR;
            }

            // ExportファイルをDBサーバに送信
            LOG.info("TG-COLLECTOR-01008", targetName, batchId, jobflowId, executionId, user);
            ExportFileSend fileSend = createExportFileSend();
            if (RuntimeContext.get().canExecute(fileSend)) {
                if (!fileSend.sendExportFile(bean, user)) {
                    // Exportファイルの送信に失敗
                    LOG.error("TG-COLLECTOR-01007",
                            new Date(), targetName, batchId, jobflowId, executionId, user);
                    return Constants.EXIT_CODE_ERROR;
                } else {
                    LOG.info("TG-COLLECTOR-01009",
                            targetName, batchId, jobflowId, executionId, user);
                }
            }

            // 正常終了
            LOG.info("TG-COLLECTOR-01002",
                    new Date(), targetName, batchId, jobflowId, executionId, user);
            return Constants.EXIT_CODE_SUCCESS;
        } catch (Exception e) {
            try {
                LOG.error(e, "TG-COLLECTOR-01004",
                        new Date(), targetName, batchId, jobflowId, executionId, user);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("Collectorで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        }
    }
    /**
     * パラメータを保持するBeanを作成する。
     *
     * @param targetName ターゲット名
     * @param batchId バッチID
     * @param jobFlowId ジョブフローID
     * @param jobnetInctanceId ジョブフロー実行ID
     * @return パラメータを保持するBean
     */
    private ExporterBean createBean(String targetName, String batchId, String jobFlowId, String jobnetInctanceId) {
        ExporterBean bean = new ExporterBean();
        // ターゲット名
        bean.setTargetName(targetName);
        // バッチID
        bean.setBatchId(batchId);
        // ジョブフローID
        bean.setJobflowId(jobFlowId);
        // ジョブフロー実行ID
        bean.setExecutionId(jobnetInctanceId);

        // DSLプロパティを読み込み
        JobFlowParamLoader dslLoader = createJobFlowParamLoader();
        if (!dslLoader.loadExportParam(bean.getTargetName(), bean.getBatchId(), bean.getJobflowId())) {
            return null;
        }
        bean.setExportTargetTable(dslLoader.getExportTargetTables());

        return bean;
    }
    /**
     * このCollectorが利用する{@link JobFlowParamLoader}を生成して返す。
     * @return 生成したインスタンス
     */
    protected JobFlowParamLoader createJobFlowParamLoader() {
        return new JobFlowParamLoader();
    }
    /**
     * このCollectorが利用するエクスポーターにファイルを送るロジックを生成して返す。
     * @return 生成したインスタンス
     */
    protected ExportFileSend createExportFileSend() {
        return new ExportFileSend();
    }
}
