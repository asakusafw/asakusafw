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

import com.asakusafw.bulkloader.common.ExportTempTableStatus;

/**
 * エクスポートテンポラリ管理テーブルの情報を保持するBean。
 * @author yuta.shirai
 */
public class ExportTempTableBean {
    /**
     * ジョブフローSID。
     */
    private String jobflowSid;
    /**
     * テーブル名。
     */
    private String exportTableName;
    /**
     * テンポラリテーブル名。
     */
    private String temporaryTableName;
    /**
     * 重複フラグテーブル名。
     */
    private String duplicateFlagTableName = null;
    /**
     * テンポラリテーブルステータス。
     */
    private ExportTempTableStatus tempTableStatus;
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
     * 処理対象のテーブル名を返す。
     * @return 処理対象のテーブル名
     */
    public String getExportTableName() {
        return exportTableName;
    }
    /**
     * 処理対象のテーブル名を設定する。
     * @param tableName 設定するテーブル名
     */
    public void setExportTableName(String tableName) {
        this.exportTableName = tableName;
    }
    /**
     * 利用するテンポラリテーブルのテーブル名を返す。
     * @return 利用するテンポラリテーブルのテーブル名
     */
    public String getTemporaryTableName() {
        return temporaryTableName;
    }
    /**
     * 利用するテンポラリテーブルのテーブル名を設定する。
     * @param exportTempName 設定するテーブル名
     */
    public void setTemporaryTableName(String exportTempName) {
        this.temporaryTableName = exportTempName;
    }
    /**
     * テンポラリテーブルの現在の内部ステータスを返す。
     * @return テンポラリテーブルの現在の内部ステータス
     */
    public ExportTempTableStatus getTempTableStatus() {
        return tempTableStatus;
    }
    /**
     * テンポラリテーブルの現在の内部ステータスを設定する。
     * @param tempTableStatus 設定するステータス文字列
     */
    public void setTempTableStatus(ExportTempTableStatus tempTableStatus) {
        this.tempTableStatus = tempTableStatus;
    }
    /**
     * 重複チェックの途中結果を保持するテーブルの名前を返す。
     * @return 重複チェックの途中結果を保持するテーブルの名前、重複チェックを行わない場合は{@code null}
     */
    public String getDuplicateFlagTableName() {
        return duplicateFlagTableName;
    }
    /**
     * 重複チェックの途中結果を保持するテーブルの名前を設定する。
     * @param duplicateFlagTableName 設定するテーブル名
     */
    public void setDuplicateFlagTableName(String duplicateFlagTableName) {
        this.duplicateFlagTableName = duplicateFlagTableName;
    }
}
