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

import java.util.Date;
import java.util.List;

import com.asakusafw.bulkloader.bean.ExportTempTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.DBAccessUtil;
import com.asakusafw.bulkloader.common.ExportTempTableStatus;
import com.asakusafw.bulkloader.common.TsvDeleteType;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;

/**
 * Exporterが実行する処理を判断するクラス。
 *<pre>
 * ・Export対象テーブルが存在しない場合、以下の処理を行う。
 * 　- ロック解除
 *
 * ・テンポラリテーブルに該当ジョブフローSIDのレコードが存在しない場合、
 * 　Exportデータのロードは行われていないとみなし、以下の処理を行う。
 * 　- Exportファイル受信
 * 　- Exportファイルロード、
 * 　- Exportデータコピー
 * 　- ロック解除
 * 　- 中間ファイル削除
 *
 * ・テンポラリテーブルに該当ジョブフローSIDのレコードが存在し、
 * 　テンポラリテーブルへのロードが中断している場合、以下の処理を行う。
 * 　- テンポラリテーブル削除
 * 　- Exportファイル受信
 * 　- Exportファイルロード
 * 　- Exportデータコピー
 * 　- ロック解除
 * 　- 中間ファイル削除
 *
 * ・テンポラリテーブルに該当ジョブフローSIDのレコードが存在し、
 * 　ロードが終了している場合、以下の処理を行う。
 * 　- Exportデータコピー
 * 　- ロック解除
 *</pre>
 * @author yuta.shirai
 */
public class JudgeExecProcess {

    static final Log LOG = new Log(JudgeExecProcess.class);

    // TODO 状態の見通しをよくする

    /**
     * テンポラリテーブル削除処理を行うかを表すフラグ。
     */
    private boolean execTempTableDelete = false;
    /**
     * Exportファイル受信処理を実行するかを表すフラグ。
     */
    private boolean execReceive = false;
    /**
     * Exportファイルロード処理を実行するかを表すフラグ。
     */
    private boolean execLoad = false;
    /**
     * Exportデータコピー処理を実行するかを表すフラグ。
     */
    private boolean execCopy = false;
    /**
     * ロック解除処理を実行するかを表すフラグ。
     */
    private boolean execLockRelease = false;
    /**
     * 中間ファイル削除処理を実行するかを表すフラグ。
     */
    private boolean execFileDelete = false;
    /**
     * エクスポートテンポラリ管理テーブルの情報。
     */
    List<ExportTempTableBean> exportTempTableBean = null;

    /**
     * 実行する処理を判断する。
     * @param bean パラメータを保持するBean
     * @return 判断が成功したか
     */
    public boolean judge(ExporterBean bean) {
        // ジョブフローSIDが取得できない場合は異常終了する
        if (bean.getJobflowSid() == null || bean.getJobflowSid().isEmpty()) {
            LOG.error("TG-EXPORTER-01011",
                    new Date(),
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
            return false;
        }
        String deleteTsv = ConfigurationLoader.getProperty(Constants.PROP_KEY_EXPORT_TSV_DELETE);
        TsvDeleteType delType = TsvDeleteType.find(deleteTsv);
        boolean isDeleteTsv = false;
        if (TsvDeleteType.TRUE.equals(delType)) {
            isDeleteTsv = true;
        }

        // Export対象テーブルが存在しない場合、ロック解除のみを行う
        List<String> list = bean.getExportTargetTableList();
        if (list == null || list.size() == 0) {
            execLockRelease = true;
            LOG.info("TG-EXPORTER-01031",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    execTempTableDelete,
                    execReceive,
                    execLoad,
                    execCopy,
                    execLockRelease,
                    execFileDelete);
            return true;
        }

        // テンポラリ管理テーブルの情報を取得する
        try {
            exportTempTableBean = getExportTempTable(bean.getJobflowSid());
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            LOG.error("TG-EXPORTER-01012",
                    new Date(),
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
            return false;
        }

        // テンポラリテーブルに該当レコードが存在しない場合、ロードは行われていないとみなし、
        // Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除、中間ファイル削除を行う
        if (exportTempTableBean == null || exportTempTableBean.size() == 0) {
            execReceive = true;
            execLoad = true;
            execCopy = true;
            execLockRelease = true;
            execFileDelete = isDeleteTsv;
            if (!isDeleteTsv) {
                LOG.info("TG-EXPORTER-01032",
                        bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
            }
            LOG.info("TG-EXPORTER-01031",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    execTempTableDelete,
                    execReceive,
                    execLoad,
                    execCopy,
                    execLockRelease,
                    execFileDelete);
            return true;
        }

        if (isCopyStart(exportTempTableBean)) {
            // テンポラリテーブルへのロードが終了している場合、Exportデータコピー、ロック解除を実行する。
            execCopy = true;
            execLockRelease = true;
            LOG.info("TG-EXPORTER-01031",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    execTempTableDelete,
                    execReceive,
                    execLoad,
                    execCopy,
                    execLockRelease,
                    execFileDelete);
            return true;
        } else {
            // テンポラリテーブルへのロードが中断している場合、
            // テンポラリテーブル削除、Exportファイル受信、Exportファイルロード、Exportデータコピー、ロック解除、中間ファイル削除を行う
            execTempTableDelete = true;
            execReceive = true;
            execLoad = true;
            execCopy = true;
            execLockRelease = true;
            execFileDelete = isDeleteTsv;
            if (!isDeleteTsv) {
                LOG.info("TG-EXPORTER-01032",
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId());
            }
            LOG.info("TG-EXPORTER-01031",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    execTempTableDelete,
                    execReceive,
                    execLoad,
                    execCopy,
                    execLockRelease,
                    execFileDelete);
            return true;
        }
    }
    /**
     * テンポラリテーブルからエクスポート対象テーブルへのデータのコピーが開始しているか判断する。
     * テンポラリ管理テーブルに該当レコードが存在し、
     * 全てのテンポラリテーブルステータスがBEFORE_COPY又はCOPY_EXITの場合、
     * データのコピーが開始していると見なす
     * @param tempBean テンポラリ管理テーブルのBean
     * @return データのコピーが開始しているかどうか
     * @see com.asakusafw.bulkloader.common.ExportTempTableStatus
     */
    private boolean isCopyStart(List<ExportTempTableBean> tempBean) {
        boolean isCopyStart = true;
        for (ExportTempTableBean element : tempBean) {
            if (element.getTempTableStatus() == null
                    || element.getTempTableStatus().equals(ExportTempTableStatus.LOAD_EXIT)) {
                isCopyStart = false;
                break;
            }
        }
        return isCopyStart;
    }
    /**
     * エクスポートテンポラリ管理テーブルの情報を取得して返す。
     * @param jobflowSid ジョブフローSID
     * @return エクスポートテンポラリ管理テーブルの情報
     * @throws BulkLoaderSystemException SQL例外が発生した場合
     */
    protected List<ExportTempTableBean> getExportTempTable(
            String jobflowSid) throws BulkLoaderSystemException {
        return DBAccessUtil.getExportTempTable(jobflowSid);
    }

    /**
     * Exportファイル受信処理を実行するかを表すフラグを返す。
     * @return execReceive Exportファイル受信処理を実行するかを表すフラグ
     */
    public boolean isExecReceive() {
        return execReceive;
    }

    /**
     * Exportファイルロード処理を実行するかを表すフラグを返す。
     * @return execLoad Exportファイルロード処理を実行するかを表すフラグ
     */
    public boolean isExecLoad() {
        return execLoad;
    }
    /**
     * Exportデータコピー処理を実行するかを表すフラグを返す。
     * @return execCopy Exportデータコピー処理を実行するかを表すフラグ
     */
    public boolean isExecCopy() {
        return execCopy;
    }
    /**
     * ロック解除処理を実行するかを表すフラグを返す。
     * @return execLockRelease ロック解除処理を実行するかを表すフラグ
     */
    public boolean isExecLockRelease() {
        return execLockRelease;
    }
    /**
     * 中間ファイル削除処理を実行するかを表すフラグを返す。
     * @return execFileDelete 中間ファイル削除処理を実行するかを表すフラグ
     */
    public boolean isExecFileDelete() {
        return execFileDelete;
    }
    /**
     * テンポラリテーブル削除処理を行うかを表すフラグを返す。
     * @return execTempTableDelete テンポラリテーブル削除処理を行うかを表すフラグ
     */
    public boolean isExecTempTableDelete() {
        return execTempTableDelete;
    }
    /**
     * エクスポートテンポラリ管理テーブルの情報を返す。
     * @return exportTempTableBean エクスポートテンポラリ管理テーブルの情報
     */
    public List<ExportTempTableBean> getExportTempTableBean() {
        return exportTempTableBean;
    }
}
