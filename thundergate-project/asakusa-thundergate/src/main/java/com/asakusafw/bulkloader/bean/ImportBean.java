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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Importerで使用するパラメータを保持するオブジェクト。
 * @author yuta.shirai
 */
public class ImportBean {
    /**
     * リトライ可能エラー時のリトライ回数。
     */
    private int retryCount;
    /**
     * リトライ可能エラー時のリトライインターバル(秒)。
     */
    private int retryInterval;
    /**
     * 処理区分。
     */
    private boolean primary;
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
     * ジョブフロー実行ID。
     */
    private String executionId;
    /**
     * ジョブネットの終了予定時刻。
     */
    private Date jobnetEndTime;
    /**
     * リカバリ対象テーブル。
     */
    private List<String> recoveryTables;
    /**
     * インポート対象テーブルの設定（key:テーブル名、value:Import対象テーブルの設定）。
     */
    private Map<String, ImportTargetTableBean> targetTable;

    /**
     * インポート対象テーブルの情報を返す。
     * @param tableName 対象のテーブル名
     * @return 対応するテーブルの情報、存在しない場合は{@code null}
     */
    public ImportTargetTableBean getTargetTable(String tableName) {
        return targetTable.get(tableName);
    }
    /**
     * インポート対象テーブル名の一覧を返す。
     * @return インポート対象テーブル名の一覧
     */
    public List<String> getImportTargetTableList() {
         return new ArrayList<String>(targetTable.keySet());
//        return targetTable.keySet().iterator();
    }
    /**
     * インポート対象テーブルの一覧を設定する。
     * @param targetTable インポート対象テーブル名と、その情報のペア一覧
     */
    public void setTargetTable(Map<String, ImportTargetTableBean> targetTable) {
        this.targetTable = targetTable;
    }

    /**
     * リカバリに利用するテーブル名の一覧を返す。
     * @return リカバリに利用するテーブル名の一覧
     */
    public List<String> getRecoveryTables() {
        return recoveryTables;
    }
    /**
     * リカバリに利用するテーブル名の一覧を設定する。
     * @param recoveryTables 設定するテーブル名の一覧
     */
    public void setRecoveryTables(List<String> recoveryTables) {
        this.recoveryTables = recoveryTables;
    }
    /**
     * ロック(取得)失敗時のリトライ回数を返す。
     * @return retryCount(0の場合はリトライを行わない)
     */
    public int getRetryCount() {
        return retryCount;
    }
    /**
     * ロック(取得)失敗時のリトライ回数を設定する。
     * @param retryCount リトライ回数(0の場合はリトライを行わない)
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    /**
     * ロック(取得)失敗時のリトライ間隔を返す。
     * @return リトライ間隔(秒)
     */
    public int getRetryInterval() {
        return retryInterval;
    }
    /**
     * ロック(取得)失敗時のリトライ間隔を設定する。
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
     * 現在処理しているジョブネット全体の推定終了時刻を返す。
     * @return 現在処理しているジョブネット全体の推定終了時刻
     */
    public Date getJobnetEndTime() {
        if (jobnetEndTime == null) {
            return null;
        } else {
            return (Date) jobnetEndTime.clone();
        }
    }
    /**
     * 現在処理しているジョブネット全体の推定終了時刻を設定する。
     * @param jobnetEndTime 設定する時刻
     */
    public void setJobnetEndTime(Date jobnetEndTime) {
        if (jobnetEndTime == null) {
            this.jobnetEndTime = null;
        } else {
            this.jobnetEndTime = (Date) jobnetEndTime.clone();
        }
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
    /**
     * このインポート処理の処理区分が「primary (主インポーター)」である場合のみ{@code true}を返す。
     * @return primary {@code true}ならば主インポーター、{@code false}ならば補助インポーターとして動作
     */
    public boolean isPrimary() {
        return primary;
    }
    /**
     * このインポート処理の処理区分を設定する。
     * @param primary {@code true}ならば主インポーター、{@code false}ならば補助インポーターとして動作
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
