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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.asakusafw.bulkloader.common.ImportTableLockType;
import com.asakusafw.bulkloader.common.ImportTableLockedOperation;
import com.asakusafw.bulkloader.transfer.FileProtocol;

/**
 * Import対象テーブルの設定を保持するクラス。
 * @author yuta.shirai
 * @since 0.1.0
 * @version 0.2.3
 */
public class ImportTargetTableBean {
    /**
     * Import対象カラム。
     */
    private List<String> importTargetColumns = new ArrayList<String>();
    /**
     * 検索条件。
     */
    private String searchCondition;
    /**
     * キャッシュを利用するかしないか。
     */
    private boolean useCache;
    /**
     * ロック取得タイプ。
     */
    private ImportTableLockType lockType;
    /**
     * ロック済みの場合の挙動 keyにテーブル名が入る。
     */
    private ImportTableLockedOperation lockedOperation;
    /**
     * Import対象テーブルに対応するJavaBeanのクラス名。
     */
    private Class<?> importTargetType;
    /**
     * The location where the imported data will be deployed.
     * If cache feature is enabled, this represents cache directory.
     */
    private String dfsFilePath;

    /**
     * Cache ID ({@code null} to disable cache feature).
     * @since 0.2.3
     */
    private String cacheId;

    /**
     * The start timestamp of record ({@code null} to process whole records).
     */
    private Calendar startTimestamp;

    /**
     * The protocol to send import contents of this target table.
     * @since 0.2.3
     */
    private FileProtocol importProtocol;

    /**
     * The content to be sent for this target table.
     */
    private File importFile;

    /**
     * インポートしたデータを配置する先のDFS上のパスを返す。
     * @return インポートしたデータを配置する先のDFS上のパス
     */
    public String getDfsFilePath() {
        return dfsFilePath;
    }
    /**
     * インポートしたデータを配置する先のDFS上のパスを設定する。
     * @param hdfsFilePath 設定するパス
     */
    public void setDfsFilePath(String hdfsFilePath) {
        this.dfsFilePath = hdfsFilePath;
    }
    /**
     * インポートする対象のカラム名一覧を返す。
     * <p>
     * 返されるカラムの順序は、生成するTSVのカラムの順序とも一致する。
     * </p>
     * @return インポートする対象のカラム名一覧
     */
    public List<String> getImportTargetColumns() {
        if (importTargetColumns == null) {
            return null;
        } else {
            return importTargetColumns;
        }
    }
    /**
     * インポートする対象のカラム名一覧を設定する。
     * @param importTargetColumns 設定するカラム名の一覧
     */
    public void setImportTargetColumns(List<String> importTargetColumns) {
        if (importTargetColumns == null) {
            this.importTargetColumns = null;
        } else {
            this.importTargetColumns = importTargetColumns;
        }
    }
    /**
     * インポート時の検索条件式を表す文字列を返す。
     * <p>
     * 検索条件は有効なSQLの条件式でなければならない。
     * また、ここに「WHERE」キーワードを含めない。
     * </p>
     * @return インポート時の検索条件、利用しない場合は{@code null}
     */
    public String getSearchCondition() {
        return searchCondition;
    }
    /**
     * インポート時の検索条件式を表す文字列を設定する。
     * @param searchCondition 設定する文字列、利用しない場合は{@code null}
     */
    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }
    /**
     * キャッシュ機構を利用する場合のみ{@code true}を返す。
     * @return キャッシュ機構を利用する場合のみ{@code true}、利用しない場合は{@code false}
     * @deprecated {@link #getCacheId()} instead
     */
    @Deprecated
    public boolean isUseCache() {
        return useCache;
    }
    /**
     * キャッシュ機構の利用の有無を設定する。
     * @param isUseCache {@code true}ならばキャッシュ機構を利用、{@code false}ならば利用しない
     * @deprecated {@link #setCacheId(String)} instead
     */
    @Deprecated
    public void setUseCache(boolean isUseCache) {
        this.useCache = isUseCache;
    }
    /**
     * 対象テーブルに関するインポート時のロックの種類を表す文字列を返す。
     * @return インポート時のロックの種類を表す文字列
     */
    public ImportTableLockType getLockType() {
        return lockType;
    }
    /**
     * インポート時のロックの種類を表す文字列を設定する。
     * @param lockType 設定する文字列
     */
    public void setLockType(ImportTableLockType lockType) {
        this.lockType = lockType;
    }
    /**
     * ロック衝突時の動作を表す文字列を返す。
     * @return ロック衝突時の動作を表す文字列
     */
    public ImportTableLockedOperation getLockedOperation() {
        return lockedOperation;
    }
    /**
     * ロック衝突時の動作を表す文字列を設定する。
     * @param lockedOperation 設定する文字列
     */
    public void setLockedOperation(ImportTableLockedOperation lockedOperation) {
        this.lockedOperation = lockedOperation;
    }
    /**
     * インポートするTSVの形式に対応するデータモデルクラスを返す。
     * @return インポートするTSVの形式に対応するデータモデルクラス
     */
    public Class<?> getImportTargetType() {
        return importTargetType;
    }
    /**
     * インポートするTSVの形式に対応するデータモデルクラスを設定する。
     * @param importTargetTableBean 設定するデータモデルクラス
     */
    public void setImportTargetType(Class<?> importTargetTableBean) {
        this.importTargetType = importTargetTableBean;
    }

    /**
     * Returns cache ID.
     * @return the ID, or {@code null} if cache feature is disabled for this table
     * @since 0.2.3
     */
    public String getCacheId() {
        return cacheId;
    }

    /**
     * Sets cache ID.
     * @param cacheId the ID, or {@code null} to cleare
     * @since 0.2.3
     */
    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    /**
     * Returns the beginning of last modified record timestamp to be processed.
     * This property is for cache mechanism, so that enabled if cache feature is active for this table.
     * @return the timestamp, or {@code null} if whole records should be processed
     * @since 0.2.3
     */
    public Calendar getStartTimestamp() {
        return startTimestamp == null ? null : (Calendar) startTimestamp.clone();
    }

    /**
     * Configures the beginning of last modified record timestamp to be processed.
     * This property is for cache mechanism, so that enabled if cache feature is active for this table.
     * @param startTimestamp the timestamp
     * @since 0.2.3
     */
    public void setStartTimestamp(Calendar startTimestamp) {
        this.startTimestamp = startTimestamp == null ? null : (Calendar) startTimestamp.clone();
    }

    /**
     * Returns the protocol to send import contents of this target table.
     * @return the protocol
     * @since 0.2.3
     */
    public FileProtocol getImportProtocol() {
        return importProtocol;
    }

    /**
     * Configures the protocol to send import contents of this target table.
     * @param importProtocol the protocol to be set
     * @since 0.2.3
     */
    public void setImportProtocol(FileProtocol importProtocol) {
        this.importProtocol = importProtocol;
    }

    /**
     * このインポート対象テーブルに関して、実際にインポートするファイルのパスを返す。
     * @return 実際にインポートするファイルのパス
     */
    public File getImportFile() {
        return importFile;
    }

    /**
     * このインポート対象テーブルに関して、実際にインポートするファイルのパスを設定する。
     * @param importFile 設定するパス
     */
    public void setImportFile(File importFile) {
        this.importFile = importFile;
    }
}
