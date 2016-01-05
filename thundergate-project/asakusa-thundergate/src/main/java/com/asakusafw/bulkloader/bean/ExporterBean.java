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
package com.asakusafw.bulkloader.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Exporterで使用するパラメータを保持するオブジェクト。
 * @author yuta.shirai
 */
public class ExporterBean {
    /**
     * リトライ可能エラー時のリトライ回数。
     */
    private int retryCount;
    /**
     * リトライ可能エラー時のリトライインターバル。
     */
    private int retryInterval;
    /**
     * ターゲット名。
     */
    private String targetName;
    /**
     * バッチID。
     */
    private String batchId;
    /**
     * ジョブフローID。
     */
    private String jobflowId;
    /**
     *  ジョブフロー実行ID。
     */
    private String executionId;
    /**
     *  ジョブフローSID。
     */
    private String jobflowSid;
    /**
     * エクスポート対象テーブルの設定（key:テーブル名、value:Export対象テーブルの設定）。
     */
    private Map<String, ExportTargetTableBean> exportTargetTable;
    /**
     * インポート対象テーブルの設定（key:テーブル名、value:Import対象テーブルの設定）。
     */
    private Map<String, ImportTargetTableBean> importTargetTable;

    /**
     * エクスポート対象テーブルの情報を返す。
     * @param tableName 対象のテーブル名
     * @return 対応するテーブルの情報、存在しない場合は{@code null}
     */
    public ExportTargetTableBean getExportTargetTable(String tableName) {
        return exportTargetTable.get(tableName);
    }
    /**
     * エクスポート対象テーブル名の一覧を返す。
     * @return エクスポート対象テーブル名の一覧
     */
    public List<String> getExportTargetTableList() {
        return new ArrayList<String>(exportTargetTable.keySet());
        // return exportTargetTable.keySet().iterator();
    }
    /**
     * エクスポート対象テーブルの一覧を設定する。
     * @param targetTable エクスポート対象テーブル名と、その情報のペア一覧
     */
    public void setExportTargetTable(Map<String, ExportTargetTableBean> targetTable) {
        this.exportTargetTable = targetTable;
    }
    /**
     * インポート対象テーブルの情報を返す。
     * @param tableName 対象のテーブル名
     * @return 対応するテーブルの情報、存在しない場合は{@code null}
     */
    public ImportTargetTableBean getImportTargetTable(String tableName) {
        return importTargetTable.get(tableName);
    }
    /**
     * インポート対象テーブル名の一覧を返す。
     * @return インポート対象テーブル名の一覧
     */
    public List<String> getImportTargetTableList() {
        return new ArrayList<String>(importTargetTable.keySet());
//        return importTargetTable.keySet().iterator();
    }
    /**
     * インポート対象のテーブルの一覧を設定する。
     * @param targetTable インポート対象テーブル名と、その情報のペア一覧
     */
    public void setImportTargetTable(Map<String, ImportTargetTableBean> targetTable) {
        this.importTargetTable = targetTable;
    }

    /**
     * ロック(解除)失敗時のリトライ回数を返す。
     * @return retryCount リトライ回数(0の場合はリトライを行わない)
     */
    public int getRetryCount() {
        return retryCount;
    }
    /**
     * ロック(解除)失敗時のリトライ回数を設定する。
     * @param retryCount リトライ回数(0の場合はリトライを行わない)
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    /**
     * ロック(解除)失敗時のリトライ間隔を返す。
     * @return リトライ間隔(秒)
     */
    public int getRetryInterval() {
        return retryInterval;
    }
    /**
     * ロック(解除)失敗時のリトライ間隔を設定する。
     * @param retryInterval ロック失敗時のリトライ間隔
     */
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    /**
     * 現在処理しているジョブのジョブフローIDを返す。
     * @return ジョブフローID
     */
    public String getJobflowId() {
        return jobflowId;
    }
    /**
     * 現在処理しているジョブのジョブフローIDを設定する。
     * @param jobflowId 設定するジョブフローID
     */
    public void setJobflowId(String jobflowId) {
        this.jobflowId = jobflowId;
    }
    /**
     * 現在処理しているジョブの実行IDを返す。
     * @return 実行ID
     */
    public String getExecutionId() {
        return executionId;
    }
    /**
     * 現在処理しているジョブの実行IDを設定する。
     * @param executionId 設定する実行ID
     */
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    /**
     * 現在処理しているジョブのバッチIDを返す。
     * @return バッチID
     */
    public String getBatchId() {
        return batchId;
    }
    /**
     * 現在処理しているジョブのバッチIDを設定する。
     * @param batchId 設定するバッチID
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
    /**
     * 現在処理しているジョブのジョブフローを識別するシステムIDを返す。
     * @return ジョブフローを識別するシステムID
     */
    public String getJobflowSid() {
        return jobflowSid;
    }
    /**
     * 現在処理しているジョブのジョブフローを識別するシステムIDを設定する。
     * @param jobflowSid 設定するジョブフローシステムID
     */
    public void setJobflowSid(String jobflowSid) {
        this.jobflowSid = jobflowSid;
    }
    /**
     * 現在の処理のターゲット名を返す。
     * @return 現在の処理のターゲット名
     */
    public String getTargetName() {
        return targetName;
    }
    /**
     * 現在の処理のターゲット名を設定する。
     * @param targetName 設定するターゲット名
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}
