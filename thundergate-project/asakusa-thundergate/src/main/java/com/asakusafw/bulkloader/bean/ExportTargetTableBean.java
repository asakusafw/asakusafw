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
import java.util.List;

/**
 * Export対象テーブルの設定を保持するBean。
 * @author yuta.shirai
 *
 */
public class ExportTargetTableBean {

    /**
     * 重複チェックを行うか否か。
     */
    private boolean duplicateCheck = false;
    /**
     * Export対象テーブルに対応する異常データテーブル。
     */
    private String errorTableName = null;
    /**
     * Export中間TSVファイルに対応するカラム名。
     */
    private List<String> exportTsvColumns = new ArrayList<String>();
    /**
     * Export対象テーブルのカラム名。
     */
    private List<String> exportTableColumns = new ArrayList<String>();
    /**
     * 異常データテーブルのカラム名。
     */
    private List<String> errorTableColumns = new ArrayList<String>();
    /**
     * キー項目のカラム名。
     */
    private List<String> keyColumns = new ArrayList<String>();
    /**
     * エラーコードを格納するカラム名。
     */
    private String errorCodeColumn = null;
    /**
     * 重複チェックエラーのエラーコードの値。
     */
    private String errorCode = null;
    /**
     * Export対象テーブルに対応するJavaBeanのクラス名。
     */
    private Class<?> exportTargetType;
    /**
     * Export対象テーブルのデータをHDFS上に書き出す際のファイルパス。
     */
    private List<String> dfsFilePaths = new ArrayList<String>();
    /**
     * DBサーバ上のExport対象ファイル。
     */
    private List<File> exportFiles = new ArrayList<File>();
    /**
     * Exportテンポラリテーブル名。
     */
    private String exportTempTableName = null;
    /**
     * 重複フラグテーブル名。
     */
    private String duplicateFlagTableName = null;

    /**
     * エクスポートするカラム名の一覧をTSVと同じ順序で返す。
     * @return エクスポートするカラム名の一覧
     */
    public List<String> getExportTsvColumn() {
        if (exportTsvColumns == null) {
            return null;
        } else {
            return exportTsvColumns;
        }
    }

    /**
     * エクスポートするカラム名の一覧をTSVと同じ順序で設定する。
     * @param exportTsvColumns 設定するカラム名の一覧
     */
    public void setExportTsvColumns(List<String> exportTsvColumns) {
        if (exportTsvColumns == null) {
            this.exportTsvColumns = null;
        } else {
            this.exportTsvColumns = exportTsvColumns;
        }
    }
    /**
     * エクスポート対象とするファイルのDFS上でのパス一覧を返す。
     * @return エクスポート対象とするファイルのDFS上でのパス一覧
     */
    public List<String> getDfsFilePaths() {
        return dfsFilePaths;
    }

    /**
     * エクスポート対象とするファイルのDFS上でのパス一覧を設定する。
     * @param hdfsFilPaths 設定するパス一覧
     */
    public void setDfsFilePaths(List<String> hdfsFilPaths) {
        this.dfsFilePaths = hdfsFilPaths;
    }

    /**
     * エクスポートするTSVの形式に対応するデータモデルクラスを返す。
     * @return エクスポートするTSVの形式に対応するデータモデルクラス
     */
    public Class<?> getExportTargetType() {
        return exportTargetType;
    }

    /**
     * エクスポートするTSVの形式に対応するデータモデルクラスを設定する。
     * @param exportTargetTableBean 設定するデータモデルクラス
     */
    public void setExportTargetType(Class<?> exportTargetTableBean) {
        this.exportTargetType = exportTargetTableBean;
    }

    /**
     * このエクスポート対象テーブルに関して、実際にエクスポートするファイルのパス一覧を返す。
     * @return 実際にエクスポートするファイルのパス一覧
     */
    public List<File> getExportFiles() {
        return exportFiles;
    }

    /**
     * このエクスポート対象テーブルに関して、実際にエクスポートするファイルのパスを追加する。
     * @param file 追加するパス
     */
    public void addExportFile(File file) {
        this.exportFiles.add(file);
    }

    /**
     * エラーテーブルのテーブル名を返す。
     * <p>
     * エラーテーブルは、このエクスポート対象テーブルへのエクスポート操作が失敗した際に
     * 代わりにエクスポート対象として利用される。
     * </p>
     * @return エラーテーブルのテーブル名、重複チェックを行わない場合は{@code null}
     * @see #isDuplicateCheck()
     */
    public String getErrorTableName() {
        return errorTableName;
    }

    /**
     * エラーテーブルのテーブル名を設定する。
     * @param errorTableName 設定するテーブル名
     */
    public void setErrorTableName(String errorTableName) {
        this.errorTableName = errorTableName;
    }

    /**
     * 実際にエクスポートするカラム名の一覧を返す。
     * @return 実際にエクスポートするカラム名の一覧
     */
    public List<String> getExportTableColumns() {
        return exportTableColumns;
    }

    /**
     * 実際にエクスポートするカラム名の一覧を設定する。
     * @param exportTableColumns 設定するカラム名の一覧
     */
    public void setExportTableColumns(List<String> exportTableColumns) {
        this.exportTableColumns = exportTableColumns;
    }

    /**
     * エラーテーブルに対して実際にエクスポートするカラム名の一覧を返す。
     * @return エラーテーブルに対して実際にエクスポートするカラム名の一覧、重複チェックを行わない場合は{@code null}
     * @see #isDuplicateCheck()
     */
    public List<String> getErrorTableColumns() {
        return errorTableColumns;
    }

    /**
     * エラーテーブルに対して実際にエクスポートするカラム名の一覧を設定する。
     * @param errorTableColumns 設定するカラム名の一覧
     */
    public void setErrorTableColumns(List<String> errorTableColumns) {
        this.errorTableColumns = errorTableColumns;
    }

    /**
     * 挿入時の重複チェックを行う際に、重複チェックの対象となるカラム名の一覧を返す。
     * @return 重複チェックの対象となるカラム名の一覧、重複チェックを行わない場合は{@code null}
     * @see #isDuplicateCheck()
     */
    public List<String> getKeyColumns() {
        return keyColumns;
    }

    /**
     * 重複チェックの対象となるカラム名の一覧を設定する。
     * @param keyColumns 設定するカラム名の一覧
     */
    public void setKeyColumns(List<String> keyColumns) {
        this.keyColumns = keyColumns;
    }

    /**
     * エラーテーブルに含まれるエラー情報を格納するカラム名を返す。
     * <p>
     * このカラムは文字列を格納するカラムでなければならない。
     * 実際に格納される文字列は{@link #getErrorCode()}に指定する。
     * </p>
     * @return エラー情報を格納するカラム名、重複チェックを行わない場合は{@code null}
     * @see #isDuplicateCheck()
     */
    public String getErrorCodeColumn() {
        return errorCodeColumn;
    }

    /**
     * エラーテーブルに含まれるエラー情報を格納するカラム名を返す。
     * @param errorCodeColumn 設定するカラム名
     */
    public void setErrorCodeColumn(String errorCodeColumn) {
        this.errorCodeColumn = errorCodeColumn;
    }

    /**
     * 重複チェックのエラー情報を格納するカラムに設定する文字列を返す。
     * @return 重複チェックのエラー情報を格納するカラムに設定する文字列、重複チェックを行わない場合は{@code null}
     * @see #isDuplicateCheck()
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 重複チェックのエラー情報を格納するカラムに設定する文字列を指定する。
     * @param errorCode 設定する文字列
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * このエクスポート対象テーブルに対し、重複チェック機能を利用する場合にのみ{@code true}を返す。
     * @return 重複チェック機能を利用する場合にのみ{@code true}、そうでなければ{@code false}
     */
    public boolean isDuplicateCheck() {
        return duplicateCheck;
    }

    /**
     * このテーブルに対するエクスポート時に、重複チェックの機能を利用するかどうかを設定する。
     * @param duplicateCheck {@code true}ならば重複チェックを利用する、{@code false}ならば利用しない
     */
    public void setDuplicateCheck(boolean duplicateCheck) {
        this.duplicateCheck = duplicateCheck;
    }

    /**
     * この対象テーブルに対してエクスポートするデータを一時的に保持するテーブルの名前を返す。
     * @return エクスポートするデータを一時的に保持するテーブルの名前
     */
    public String getExportTempTableName() {
        // TODO 内部処理の話なのでこのBeanに属するかどうかが微妙なプロパティ
        return exportTempTableName;
    }

    /**
     * この対象テーブルに対してエクスポートするデータを一時的に保持するテーブルの名前を設定する。
     * @param exportTempTableName 設定するテーブルの名前
     */
    public void setExportTempTableName(String exportTempTableName) {
        this.exportTempTableName = exportTempTableName;
    }

    /**
     * 重複チェックの途中結果を保持するテーブルの名前を返す。
     * @return 重複チェックの途中結果を保持するテーブルの名前、重複チェックを行わない場合は{@code null}
     * @see #isDuplicateCheck()
     */
    public String getDuplicateFlagTableName() {
        // TODO 内部処理の話なのでこのBeanに属するかどうかが微妙なプロパティ
        return duplicateFlagTableName;
    }

    /**
     * 重複チェックの途中結果を保持するテーブルの名前を設定する。
     * @param tableName 設定するテーブル名
     */
    public void setDuplicateFlagTableName(String tableName) {
        this.duplicateFlagTableName = tableName;
    }
}
